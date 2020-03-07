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

import com.gengoai.Stopwatch;
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
import com.gengoai.parsing.ParseException;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

/**
 * Persistent corpus implementation backed by a Lucene Index. All changes are updated in-place.
 *
 * @author David B. Bracewell
 */
public class LuceneCorpus implements Corpus {
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
   public Corpus cache() {
      return this;
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
               if(liveDocs.get(doc)) {
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
   public Corpus filter(SerializablePredicate<? super Document> filter) {
      return new StreamingCorpus(stream().filter(filter));
   }

   @Override
   public Document get(long index) {
      return loadDocument((int) index);
   }

   @Override
   public Document get(String id) {
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
   public <T> Counter<T> getAttributeCount(@NonNull AttributeType<T> type) {
      return Counters.newCounter(count(type.name(), type::decode));
   }

   @Override
   public Set<AttributeType<?>> getAttributeTypes() {
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
   public Set<AnnotatableType> getCompletedAnnotations() {
      final long size = size();
      return count(ANNOTATIONS_FIELD, AnnotatableType::valueOf).filterByValue(d -> d >= size).items();
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
   public boolean isPersistent() {
      return true;
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
         IndexReader reader = getIndexReader();
         return new LuceneSearchResults(ResourceMonitor.monitor(reader), query);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public SearchResults query(@NonNull String query) throws ParseException {
      return query(QueryParser.parse(query));
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
   public Corpus sample(int size) {
      return sample(size, new Random());
   }

   @Override
   public Corpus sample(int count, @NonNull Random random) {
      TopDocs docs;
      try(IndexReader reader = getIndexReader()) {
         IndexSearcher searcher = new IndexSearcher(reader);
         Sort sort = new Sort();
         sort.setSort(new SortField("", new FieldComparatorSource() {
            @Override
            public FieldComparator<?> newComparator(String s, int i, int i1, boolean b) {
               return new RandomOrderComparator(random);
            }
         }));
         docs = searcher.search(new MatchAllDocsQuery(), count, sort);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      return new StreamingCorpus(StreamingContext.local()
                                                 .stream(docs.scoreDocs)
                                                 .parallel()
                                                 .map(sd -> loadDocument(sd.doc)));
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

   private Corpus update(SerializablePredicate<Document> processor) {
      final Stopwatch timer = Stopwatch.createStopped();
      final UpdateConsumer consumer = new UpdateConsumer(processor, timer);
      Broker<Document> broker = Broker.<Document>builder()
            .addProducer(new IterableProducer<>(this))
            .bufferSize(10_000)
            .addConsumer(consumer, Runtime.getRuntime().availableProcessors() * 2)
            .build();
      timer.start();
      broker.run();
      timer.stop();
      try {
         consumer.writer.close();
      } catch(IOException e) {
         e.printStackTrace();
      }
      logInfo("Documents/Second: {0}", consumer.documentCounter.get() / timer.elapsed(TimeUnit.SECONDS));
      return this;
   }

   @Override
   public Corpus update(SerializableConsumer<Document> documentProcessor) {
      return update(d -> {
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

   private class LuceneSearchIterator implements Iterator<Document> {
      private final org.apache.lucene.search.Query query;
      private final MonitoredObject<IndexReader> reader;
      private final IndexSearcher searcher;
      int index = 0;
      private TopDocs docs = null;
      private boolean hasMoreDocs = true;
      private ScoreDoc scoreDoc = null;

      public LuceneSearchIterator(MonitoredObject<IndexReader> reader, org.apache.lucene.search.Query query) {
         this.reader = reader;
         this.searcher = new IndexSearcher(reader.object);
         this.query = query;
      }

      private boolean advance() {
         if(!hasMoreDocs) {
            return false;
         }
         if(scoreDoc != null) {
            return true;
         }
         try {

            if(docs == null) {
               docs = searcher.search(query, 10_000);
               hasMoreDocs = docs.scoreDocs.length > 0;
               scoreDoc = docs.scoreDocs[0];
               index = 1;
               return hasMoreDocs;
            } else if(index >= docs.scoreDocs.length) {
               docs = searcher.searchAfter(scoreDoc, query, 10_000);
               hasMoreDocs = docs.scoreDocs.length > 0;
               scoreDoc = docs.scoreDocs[0];
               index = 1;
               return hasMoreDocs;
            }
            scoreDoc = docs.scoreDocs[index];
            index++;
            return true;
         } catch(IOException e) {
            hasMoreDocs = false;
            return false;
         }
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public Document next() {
         if(!advance()) {
            throw new NoSuchElementException();
         }
         Document doc = loadDocument(reader.object, scoreDoc.doc);
         scoreDoc = null;
         return doc;
      }
   }

   private class LuceneSearchResults implements SearchResults {
      private final Query hermesQuery;
      private final org.apache.lucene.search.Query luceneQuery;
      private final MonitoredObject<IndexReader> reader;
      Long size = null;

      public LuceneSearchResults(MonitoredObject<IndexReader> reader, Query hermesQuery) {
         this.reader = reader;
         this.hermesQuery = hermesQuery;
         this.luceneQuery = hermesQuery.toLucene();
      }

      @Override
      public Query getQuery() {
         return hermesQuery;
      }

      @Override
      public long getTotalHits() {
         if(size != null) {
            return size;
         }
         long cnt = 0;
         try {
            IndexSearcher searcher = new IndexSearcher(reader.object);
            TopDocs d = searcher.search(luceneQuery, 10_000);
            cnt += d.scoreDocs.length;
            while(true) {
               d = searcher.searchAfter(d.scoreDocs[d.scoreDocs.length - 1], luceneQuery, 10_000);
               cnt += d.scoreDocs.length;
               if(d.scoreDocs.length == 0) {
                  size = cnt;
                  return size;
               }
            }
         } catch(IOException ioe) {
            throw new RuntimeException(ioe);
         }
      }

      @Override
      public Iterator<Document> iterator() {
         return new LuceneSearchIterator(reader, luceneQuery);
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
      final AtomicLong documentCounter = new AtomicLong();
      private final SerializablePredicate<Document> documentProcessor;
      private final Level logLevel = Config.get("Corpus.reportLevel").as(Level.class, Level.INFO);
      private final long reportInterval = Config.get("Corpus.reportInterval").asLongValue(5_000);
      private final Stopwatch stopwatch;
      private final AtomicLong wordCounter = new AtomicLong();
      private final IndexWriter writer;


      /**
       * Instantiates a new Update consumer.
       *
       * @param documentProcessor the document processor
       */
      public UpdateConsumer(@NonNull SerializablePredicate<Document> documentProcessor, @NonNull Stopwatch stopwatch) {
         this.documentProcessor = documentProcessor;
         this.stopwatch = stopwatch;
         try {
            this.writer = getIndexWriter();
         } catch(IOException e) {
            throw new RuntimeException(e);
         }
      }


      @Override
      public void accept(Document document) {
         if(documentProcessor.test(document)) {
            try {
               writer.updateDocument(new Term(ID_FIELD, document.getId()), toDocument(document));
               long docCount;
               long wrdCount = wordCounter.addAndGet(document.tokenLength());
               if((docCount = documentCounter.incrementAndGet()) % reportInterval == 0) {
                  log(logLevel, "Documents Update: {0}", docCount);
                  if(stopwatch != null) {
                     log(logLevel, "Updating at {0} documents/second", docCount / stopwatch.elapsed(TimeUnit.SECONDS));
                     if(wrdCount > 0) {
                        log(logLevel, "Updating at {0} words/second", wrdCount / stopwatch.elapsed(TimeUnit.SECONDS));
                     }
                  }
               }
            } catch(IOException e) {
               throw new RuntimeException(e);
            }
         }
      }


   }

}//END OF LuceneCorpus
