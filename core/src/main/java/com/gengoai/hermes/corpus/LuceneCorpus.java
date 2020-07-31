/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.gengoai.hermes.corpus;

import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.concurrent.Broker;
import com.gengoai.concurrent.IterableProducer;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableConsumer;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.io.resource.ByteArrayResource;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

/**
 * Persistent corpus implementation backed by a Lucene Index. All changes are updated in-place.
 *
 * @author David B. Bracewell
 */
@Log
class LuceneCorpus implements Corpus {
   public static final String COMMIT_INTERVAL_CONFIG = "Corpus.commitInterval";
   /**
    * The Lucene Field used to store the completed annotations
    */
   public static final String ANNOTATIONS_FIELD = "@annotations";
   private static final PerFieldAnalyzerWrapper ANALYZER_WRAPPER = new PerFieldAnalyzerWrapper(
         new StandardAnalyzer(),
         hashMapOf($(ANNOTATIONS_FIELD, new KeywordAnalyzer()))
   );
   /**
    * The Lucene Field used to store the raw document content
    */
   public static final String CONTENT_FIELD = "@content";
   /**
    * The Lucene Field used to store the document id
    */
   public static final String ID_FIELD = "@id";
   /**
    * The Lucene Field used to store the document json
    */
   public static final String JSON_FIELD = "@json";
   public static final String SPLIT_SIZE_CONFIG = "Corpus.splitSize";
   private final Directory directory;

   /**
    * Instantiates a new Lucene corpus.
    *
    * @param location the location
    */
   public LuceneCorpus(@NonNull File location) {
      try {
         directory = FSDirectory.open(location.toPath());
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public boolean add(@NonNull Document document) {
      try(IndexWriter writer = getIndexWriter()) {
         writer.updateDocument(new Term(ID_FIELD, document.getId()), toDocument(document));
         writer.commit();
         return true;
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void addAll(@NonNull Iterable<Document> documents) {
      try(IndexWriter writer = getIndexWriter()) {
         for(Document document : documents) {
            writer.updateDocument(new Term(ID_FIELD, document.getId()), toDocument(document));
         }
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void close() throws IOException {
      directory.close();
   }

   @Override
   public Corpus compact() {
      try(IndexWriter writer = getIndexWriter()) {
         writer.forceMergeDeletes();
         writer.deleteUnusedFiles();
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      return this;
   }

   private <T> Counter<T> count(@NonNull String fieldName, @NonNull Function<String, T> converter) {
      try(IndexReader reader = getIndexReader()) {
         final Bits liveDocs = MultiBits.getLiveDocs(reader);
         Counter<T> counter = Counters.newCounter();
         Terms terms = MultiTerms.getTerms(reader, fieldName);
         if(terms == null) {
            return counter;
         }
         TermsEnum termsEnum = terms.iterator();
         BytesRef br;
         while((br = termsEnum.next()) != null) {
            PostingsEnum pe = termsEnum.postings(null, PostingsEnum.POSITIONS);
            long freq = 0;
            int doc;
            while((doc = pe.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
               if(liveDocs == null || liveDocs.get(doc)) {
                  freq++;
               }
            }
            counter.increment(converter.apply(br.utf8ToString()), freq);
         }
         return counter;
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public <T> Counter<T> getAttributeValueCount(@NonNull AttributeType<T> type) {
      return Counters.newCounter(count(type.name(), type::decode));
   }

   @Override
   public Set<AttributeType<?>> getAttributes() {
      try(IndexReader reader = getIndexReader()) {
         final Set<AttributeType<?>> fieldNames = new HashSet<>();
         for(LeafReaderContext leaf : reader.leaves()) {
            leaf.reader()
                .getFieldInfos()
                .forEach(fi -> {
                   if(!fi.name.startsWith("@")) {
                      fieldNames.add(Types.attribute(fi.name));
                   }
                });
         }
         return fieldNames;
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Set<AnnotatableType> getCompleted() {
      final long size = size();
      return count(ANNOTATIONS_FIELD, AnnotatableType::valueOf).filterByValue(d -> d >= size).items();
   }

   @Override
   public Document getDocument(String id) {
      TermQuery idQuery = new TermQuery(new Term(ID_FIELD, id));
      try(IndexReader reader = getIndexReader()) {
         IndexSearcher searcher = new IndexSearcher(reader);
         ScoreDoc[] r = searcher.search(idQuery, 1).scoreDocs;
         if(r.length > 0) {
            return loadDocument(reader, r[0].doc);
         }
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      return null;
   }

   @Override
   public List<String> getIds() {
      try(IndexReader reader = getIndexReader()) {
         List<String> ids = new ArrayList<>();
         Terms terms = MultiTerms.getTerms(reader, ID_FIELD);
         if(terms == null) {
            return ids;
         }
         TermsEnum termsEnum = terms.iterator();
         BytesRef br;
         while((br = termsEnum.next()) != null) {
            ids.add(br.utf8ToString());
         }
         Collections.sort(ids);
         return ids;
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   private IndexReader getIndexReader() throws IOException {
      return DirectoryReader.open(directory);
   }

   private IndexWriter getIndexWriter() throws IOException {
      final IndexWriterConfig writerConfig = new IndexWriterConfig(ANALYZER_WRAPPER);
      return new IndexWriter(directory, writerConfig);
   }

   @Override
   public StreamingContext getStreamingContext() {
      return StreamingContext.local();
   }

   @Override
   public boolean isEmpty() {
      return size() <= 0;
   }

   @Override
   public Iterator<Document> iterator() {
      try {
         return new LuceneDocumentIterator(ResourceMonitor.monitor(getIndexReader()));
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   private Document loadDocument(int id) {
      try(IndexReader reader = getIndexReader()) {
         return loadDocument(reader, id);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   private Document loadDocument(IndexReader reader, int id) {
      try {
         ByteArrayResource resource = new ByteArrayResource(reader.document(id)
                                                                  .getField(JSON_FIELD)
                                                                  .binaryValue().bytes);
         return Document.fromJson(resource.readToString());
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public MStream<Document> parallelStream() {
      return stream();
   }

   @Override
   public SearchResults query(@NonNull Query query) {
      try {
         return new LuceneSearchResults(this, queryAndReturnIds(query), query);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   private LinkedHashSet<String> queryAndReturnIds(Query query) throws IOException {
      LinkedHashSet<String> ids = new LinkedHashSet<>();
      final org.apache.lucene.search.Query luceneQuery = query.toLucene();
      try(IndexReader reader = getIndexReader()) {
         IndexSearcher searcher = new IndexSearcher(reader);
         TopDocs d = searcher.search(luceneQuery, 10_000);
         while(d.scoreDocs.length > 0) {
            ids.addAll(toDocumentIds(reader, d.scoreDocs));
            d = searcher.searchAfter(d.scoreDocs[d.scoreDocs.length - 1], luceneQuery, 10_000);
            if(d.scoreDocs.length == 0) {
               break;
            }
         }
      }
      return ids;
   }

   @Override
   public boolean remove(@NonNull Document document) {
      return remove(document.getId());
   }

   @Override
   public boolean remove(@NonNull String id) {
      try(IndexWriter writer = getIndexWriter()) {
         boolean deleted = writer.deleteDocuments(new Term(ID_FIELD, id)) > 0;
         writer.commit();
         return deleted;
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public DocumentCollection sample(int size) {
      return sample(size, new Random());
   }

   @Override
   public DocumentCollection sample(int count, @NonNull Random random) {
      LinkedHashSet<String> ids;
      try(IndexReader reader = getIndexReader()) {
         IndexSearcher searcher = new IndexSearcher(reader);
         Sort sort = new Sort();
         sort.setSort(new SortField("", new FieldComparatorSource() {
            @Override
            public FieldComparator<?> newComparator(String s, int i, int i1, boolean b) {
               return new RandomOrderComparator(random);
            }
         }));
         ids = toDocumentIds(reader, searcher.search(new MatchAllDocsQuery(), count, sort).scoreDocs);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      return new LuceneFilteredView(this, ids);
   }

   @Override
   public long size() {
      try(IndexReader reader = getIndexReader()) {
         return reader.numDocs();
      } catch(IndexNotFoundException nfe) {
         return 0;
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Spliterator<Document> spliterator() {
      try {
         return new LuceneSplitIterator(ResourceMonitor.monitor(getIndexReader()), null, null);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public MStream<Document> stream() {
      return getStreamingContext().stream(this).parallel();
   }

   private org.apache.lucene.document.Document toDocument(Document document) {
      org.apache.lucene.document.Document iDoc = new org.apache.lucene.document.Document();
      iDoc.add(new StringField(ID_FIELD, document.getId(), Field.Store.YES));
      FieldType ft = new FieldType();
      ft.setTokenized(false);
      ft.setStored(true);
      iDoc.add(new Field(JSON_FIELD, document.toJson().getBytes(StandardCharsets.UTF_8), ft));
      iDoc.add(new TextField(CONTENT_FIELD, document.toString(), Field.Store.NO));
      for(AnnotatableType annotatableType : document.completed()) {
         iDoc.add(new TextField(ANNOTATIONS_FIELD, annotatableType.canonicalName(), Field.Store.NO));
      }
      document.attributeMap()
              .forEach((k, v) -> {
                 if(v instanceof Iterable) {
                    for(Object a : Cast.<Iterable<?>>as(v)) {
                       iDoc.add(toField(k.name(), a));
                    }
                 } else if(v instanceof Map) {
                    for(Map.Entry<?, ?> entry : Cast.<Map<?, ?>>as(v).entrySet()) {
                       iDoc.add(toField(k.name() + "." + entry.getKey().toString(), entry.getValue()));
                    }
                 } else if(v instanceof Multimap) {
                    for(Map.Entry<?, ?> entry : Cast.<Multimap<?, ?>>as(v).entries()) {
                       iDoc.add(toField(k.name() + "." + entry.getKey().toString(), entry.getValue()));
                    }
                 } else if(v instanceof Counter) {
                    for(Map.Entry<?, ?> entry : Cast.<Counter<?>>as(v).asMap().entrySet()) {
                       iDoc.add(toField(k.name() + "." + entry.getKey().toString(), entry.getValue()));
                    }
                 } else {
                    iDoc.add(toField(k.name(), v));
                 }
              });
      return iDoc;
   }

   protected LinkedHashSet<String> toDocumentIds(IndexReader reader, ScoreDoc[] scoreDocs) {
      LinkedHashSet<String> ids = new LinkedHashSet<>();
      for(ScoreDoc scoreDoc : scoreDocs) {
         try {
            ids.add(reader.document(scoreDoc.doc, Collections.singleton(ID_FIELD)).getField(ID_FIELD).stringValue());
         } catch(IOException e) {
            throw new RuntimeException(e);
         }
      }
      return ids;
   }

   private Field toField(String name, Object v) {
      if(v instanceof Float) {
         return new FloatPoint(name, ((Number) v).floatValue());
      } else if(v instanceof Integer || v instanceof Short || v instanceof Byte) {
         return new IntPoint(name, ((Number) v).intValue());
      } else if(v instanceof Long) {
         return new LongPoint(name, ((Number) v).longValue());
      } else if(v instanceof Number) {
         return new DoublePoint(name, ((Number) v).doubleValue());
      } else {
         return new StringField(name, v.toString(), Field.Store.NO);
      }
   }

   @Override
   public boolean update(@NonNull Document document) {
      try(IndexWriter writer = getIndexWriter()) {
         boolean updated = writer.updateDocument(new Term(ID_FIELD, document.getId()), toDocument(document)) > 0;
         writer.commit();
         return updated;
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   private Corpus update(String operation, SerializablePredicate<Document> processor) {
      ProgressLogger progressLogger = ProgressLogger.create(this, operation);
      final UpdateConsumer consumer = new UpdateConsumer(processor, progressLogger);
      Broker<Document> broker = Broker.<Document>builder()
            .addProducer(new IterableProducer<>(this))
            .bufferSize(10_000)
            .addConsumer(consumer, Runtime.getRuntime().availableProcessors())
            .build();
      broker.run();
      try {
         consumer.writer.close();
      } catch(IOException e) {
         e.printStackTrace();
      }
      progressLogger.report();
      return this;
   }

   @Override
   public Corpus update(@NonNull String operation, @NonNull SerializableConsumer<Document> documentProcessor) {
      return update(operation, d -> {
         documentProcessor.accept(d);
         return true;
      });
   }

   private static class RandomOrderComparator extends FieldComparator<Integer> {
      private final Random random;

      private RandomOrderComparator(Random random) {
         this.random = random;
      }

      @Override
      public int compare(int i, int i1) {
         return random.nextInt();
      }

      @Override
      public LeafFieldComparator getLeafComparator(LeafReaderContext leafReaderContext) {
         return new LeafFieldComparator() {
            @Override
            public int compareBottom(int i) {
               return 0;
            }

            @Override
            public int compareTop(int i) {
               return 0;
            }

            @Override
            public void copy(int i, int i1) {

            }

            @Override
            public void setBottom(int i) {

            }

            @Override
            public void setScorer(Scorable scorable) {

            }
         };
      }

      @Override
      public void setTopValue(Integer integer) {

      }

      @Override
      public Integer value(int i) {
         return random.nextInt();
      }
   }

   private class LuceneDocumentIterator implements Iterator<Document> {
      private final Bits liveDocs;
      private final MonitoredObject<IndexReader> reader;
      private int index = -1;

      private LuceneDocumentIterator(MonitoredObject<IndexReader> reader) {
         this.reader = reader;
         this.liveDocs = MultiBits.getLiveDocs(reader.object);
         advance();
      }

      private void advance() {
         index++;
         while(index < reader.object.maxDoc() && liveDocs != null && !liveDocs.get(index)) {
            index++;
         }
      }

      @Override
      public boolean hasNext() {
         return index < reader.object.maxDoc();
      }

      @Override
      public Document next() {
         if(index >= reader.object.maxDoc()) {
            throw new NoSuchElementException();
         }
         Document doc = LuceneCorpus.this.loadDocument(reader.object, index);
         advance();
         return doc;
      }
   }

   private class LuceneSplitIterator implements Spliterator<Document> {
      private final Bits liveDocs;
      private final MonitoredObject<IndexReader> reader;
      private final long splitSize;
      private int current;
      private int end;

      private LuceneSplitIterator(MonitoredObject<IndexReader> reader, Integer lo, Integer hi) {
         this.liveDocs = MultiBits.getLiveDocs(reader.object);
         this.reader = reader;
         this.current = lo == null
                        ? 0
                        : lo;
         this.end = hi == null
                    ? reader.object.maxDoc()
                    : hi;
         this.splitSize = Config.get(SPLIT_SIZE_CONFIG).asLongValue(5000);
      }

      @Override
      public int characteristics() {
         return CONCURRENT;
      }

      @Override
      public long estimateSize() {
         return reader.object.maxDoc();
      }

      @Override
      public boolean tryAdvance(Consumer<? super Document> consumer) {
         while(current < end && liveDocs != null && !liveDocs.get(current)) {
            current++;
         }
         if(current < end) {
            consumer.accept(LuceneCorpus.this.loadDocument(reader.object, current));
            current++;
            return true;
         }
         return false;
      }

      @Override
      public Spliterator<Document> trySplit() {
         int length = end - current;
         if(length < splitSize) {
            return null;
         }
         int lo = current + length / 2;
         int hi = end;
         end = lo;
         return new LuceneSplitIterator(reader, lo, hi);
      }
   }

   private class UpdateConsumer implements Consumer<Document> {
      private final SerializablePredicate<Document> documentProcessor;
      private final ProgressLogger progressLogger;
      private final IndexWriter writer;
      private final int commitInterval;

      /**
       * Instantiates a new Update consumer.
       *
       * @param documentProcessor the document processor
       */
      public UpdateConsumer(@NonNull SerializablePredicate<Document> documentProcessor,
                            @NonNull ProgressLogger progressLogger) {
         this.documentProcessor = documentProcessor;
         this.progressLogger = progressLogger;
         this.commitInterval = Config.get(COMMIT_INTERVAL_CONFIG).asIntegerValue(10_000);
         try {
            this.writer = getIndexWriter();
         } catch(IOException e) {
            throw new RuntimeException(e);
         }
      }

      @Override
      public void accept(Document document) {
         try {
            progressLogger.start();
            if(documentProcessor.test(document)) {
               writer.updateDocument(new Term(ID_FIELD, document.getId()), toDocument(document));
               if(progressLogger.documentsProcessed() % commitInterval == 0) {
                  writer.commit();
               }
            }
         } catch(IOException e) {
            throw new RuntimeException(e);
         } finally {
            progressLogger.stop(document.tokenLength());
         }
      }

   }

}//END OF LuceneCorpus
