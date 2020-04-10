/*
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
 */

package com.gengoai.hermes.corpus;

import com.gengoai.apollo.ml.data.DatasetType;
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.statistics.measure.Association;
import com.gengoai.apollo.statistics.measure.ContingencyTable;
import com.gengoai.apollo.statistics.measure.ContingencyTableCalculator;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.function.SerializableConsumer;
import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.AnnotationPipeline;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.hermes.extraction.NGramExtractor;
import com.gengoai.hermes.extraction.caduceus.CaduceusProgram;
import com.gengoai.hermes.extraction.regex.TokenMatch;
import com.gengoai.hermes.extraction.regex.TokenMatcher;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.hermes.format.DocFormatService;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.ml.ExampleGenerator;
import com.gengoai.hermes.ml.InstanceGenerator;
import com.gengoai.hermes.ml.SequenceGenerator;
import com.gengoai.io.Resources;
import com.gengoai.parsing.ParseException;
import com.gengoai.specification.Specification;
import com.gengoai.stream.MCounterAccumulator;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import com.gengoai.tuple.Tuple;
import lombok.NonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.gengoai.collection.counter.Counters.newCounter;

/**
 * <p>
 * A document collection represents a temporary collection of documents often used for ad-hoc analytics or to import
 * documents into a corpus
 * </p>
 * <p>
 * Hermes provides a straightforward way of reading and writing document collections in a number of formats, including
 * plain text, csv, and json. In addition, many formats can be used in a "one-per-line" corpus where each line
 * represents a single document in the given format. For example, a json one-per-line corpus has a single json object
 * representing a document on each line of the file. Each document format has an associated set of _DocFormatParameters_
 * that define the various options for reading and writing in the format.
 * </p>
 */
public interface DocumentCollection extends Iterable<Document>, AutoCloseable {
   /**
    * Configuration option for setting the reporting interval for when updating a DocumentCollection or Corpus
    */
   String REPORT_INTERVAL = "Corpus.reportInterval";
   /**
    * Configuration option for setting the reporting log level for when updating a DocumentCollection or Corpus
    */
   String REPORT_LEVEL = "Corpus.reportLevel";

   /**
    * Creates a document collection for one or more documents.
    *
    * @param documents the documents
    * @return the document collection
    */
   static DocumentCollection create(@NonNull Document... documents) {
      return new MStreamDocumentCollection(StreamingContext.local().stream(documents));
   }

   /**
    * Creates a document collection for one or more documents.
    *
    * @param documents the documents
    * @return the document collection
    */
   static DocumentCollection create(@NonNull Iterable<Document> documents) {
      return new MStreamDocumentCollection(StreamingContext.local().stream(documents));
   }

   /**
    * Creates a document collection for a stream of  documents.
    *
    * @param documents the documents
    * @return the document collection
    */
   static DocumentCollection create(@NonNull Stream<Document> documents) {
      return new MStreamDocumentCollection(StreamingContext.local().stream(documents));
   }

   /**
    * Creates a document collection for a stream of  documents.
    *
    * @param documents the documents
    * @return the document collection
    */
   static DocumentCollection create(@NonNull MStream<Document> documents) {
      return new MStreamDocumentCollection(documents);
   }

   /**
    * Creates a document collection from a specification detailing the document format and path of the documents. The
    * specification should have the document format as the schema, e.g. <code>FORMAT::PATH;OPTIONS</code>
    *
    * @param specification the specification
    * @return the document collection
    */
   static DocumentCollection create(@NonNull String specification) {
      return create(Specification.parse(specification));
   }

   /**
    * Creates a document collection from a specification detailing the document format and path of the documents. The
    * specification should have the document format as the schema, e.g. <code>FORMAT::PATH;OPTIONS</code>
    *
    * @param specification the specification
    * @return the document collection
    */
   static DocumentCollection create(@NonNull Specification specification) {
      if(specification.getSchema().equalsIgnoreCase("corpus")) {
         return Corpus.open(specification.getPath());
      }
      return create(DocFormatService.create(specification)
                                    .read(Resources.from(specification.getPath())));
   }

   /**
    * Annotates this corpus with the given annotation types and returns a new corpus with the given annotation types
    * present
    *
    * @param annotatableTypes The annotation types to annotate
    * @return A new corpus with the given annotation types present.
    */
   default DocumentCollection annotate(@NonNull AnnotatableType... annotatableTypes) {
      AnnotationPipeline pipeline = new AnnotationPipeline(annotatableTypes);
      if(pipeline.requiresUpdate()) {
         return update("Annotate", pipeline::annotate);
      }
      return this;
   }

   /**
    * Applies a lexicon to the corpus creating annotations of the given type for matches.
    *
    * @param lexicon the lexicon to match
    * @param onMatch the on match
    * @return the corpus
    */
   default DocumentCollection apply(@NonNull Lexicon lexicon, @NonNull SerializableConsumer<HString> onMatch) {
      return update("ApplyLexicon", doc -> lexicon.extract(doc).forEach(onMatch));
   }

   /**
    * Applies token regular expression to the corpus creating annotations of the given type for matches.
    *
    * @param pattern the pattern
    * @param onMatch the on match
    * @return the corpus
    */
   default DocumentCollection apply(@NonNull TokenRegex pattern, @NonNull SerializableConsumer<TokenMatch> onMatch) {
      return update("ApplyTokenRegex", doc -> {
         TokenMatcher matcher = pattern.matcher(doc);
         while(matcher.find()) {
            onMatch.accept(matcher.asTokenMatch());
         }
      });
   }

   /**
    * Constructs a Dataset from the Corpus according to the {@link ExampleGenerator}
    *
    * @param exampleGenerator the example generator to use to construct the dataset
    * @param datasetType      the dataset type
    * @return the dataset
    */
   default ExampleDataset asDataset(@NonNull ExampleGenerator exampleGenerator, @NonNull DatasetType datasetType) {
      return ExampleDataset.builder()
                           .type(datasetType)
                           .source(stream().flatMap(exampleGenerator::apply));
   }

   /**
    * Constructs a Dataset from the Corpus according to the {@link ExampleGenerator}
    *
    * @param exampleGenerator the example generator to use to construct the dataset
    * @return the dataset
    */
   default ExampleDataset asDataset(@NonNull ExampleGenerator exampleGenerator) {
      return asDataset(exampleGenerator, getStreamingContext().isDistributed()
                                         ? DatasetType.Distributed
                                         : DatasetType.InMemory);
   }

   /**
    * Constructs a Dataset of Instances from the Corpus according to an {@link InstanceGenerator}
    *
    * @param updater     the dataset definition
    * @param datasetType the dataset type
    * @return the dataset
    */
   default ExampleDataset asInstanceDataset(@NonNull Consumer<InstanceGenerator> updater,
                                            @NonNull DatasetType datasetType) {
      return asDataset(ExampleGenerator.instance(updater), datasetType);
   }

   /**
    * Constructs a Dataset of Instances from the Corpus according to an {@link InstanceGenerator}
    *
    * @param updater the dataset definition
    * @return the dataset
    */
   default ExampleDataset asInstanceDataset(@NonNull Consumer<InstanceGenerator> updater) {
      return asDataset(ExampleGenerator.instance(updater));
   }

   /**
    * Constructs a Dataset of Sequences from the Corpus according to an {@link SequenceGenerator}
    *
    * @param updater the dataset definition
    * @return the dataset
    */
   default ExampleDataset asSequenceDataset(@NonNull Consumer<SequenceGenerator> updater) {
      return asDataset(ExampleGenerator.sequence(updater));
   }

   /**
    * Constructs a Dataset of Sequences from the Corpus according to an {@link SequenceGenerator}
    *
    * @param updater     the dataset definition
    * @param datasetType the dataset type
    * @return the dataset
    */
   default ExampleDataset asSequenceDataset(@NonNull Consumer<SequenceGenerator> updater,
                                            @NonNull DatasetType datasetType) {
      return asDataset(ExampleGenerator.sequence(updater), datasetType);
   }

   /**
    * Calculates the document frequency of annotations of the given annotation type in the corpus. Annotations are
    * transformed into strings using the given toString function.
    *
    * @param extractor the LyreExpression to use for extracting terms
    * @return A counter containing document frequencies of the given annotation type
    */
   default Counter<String> documentCount(@NonNull Extractor extractor) {
      ProgressLogger progressLogger = ProgressLogger.create(this, "documentCount");
      MCounterAccumulator<String> documentCounts = getStreamingContext().counterAccumulator();
      parallelStream().forEach(doc -> {
         progressLogger.start();
         extractor.extract(doc)
                  .count()
                  .forEach((term, count) -> documentCounts.increment(term, 1.0));
         progressLogger.stop(doc.tokenLength());
      });
      progressLogger.report();
      return documentCounts.value();
   }

   /**
    * Gets the streaming context associated with this stream
    *
    * @return the streaming context
    */
   StreamingContext getStreamingContext();

   /**
    * Groups documents in the document store using the given function.
    *
    * @param <K>         The key type
    * @param keyFunction Converts the document into a key to group the documents  by
    * @return A <code>Multimap</code> of key - document pairs.
    */
   default <K> Multimap<K, Document> groupBy(@NonNull SerializableFunction<? super Document, K> keyFunction) {
      ListMultimap<K, Document> grouping = new ArrayListMultimap<>();
      forEach(document -> grouping.put(keyFunction.apply(document), document));
      return grouping;
   }

   /**
    * @return True if this document collection has no documents.
    */
   default boolean isEmpty() {
      return stream().isEmpty();
   }

   @Override
   default Iterator<Document> iterator() {
      return stream().iterator();
   }

   /**
    * Calculates the total corpus frequencies for NGrams extracted using the given extractor. Note tha all n-grams are
    * returned in their string form as Tuples.
    *
    * @param nGramExtractor the extractor
    * @return the counter of string tuples representing the ngrams
    */
   default Counter<Tuple> nGramCount(@NonNull NGramExtractor nGramExtractor) {
      ProgressLogger progressLogger = ProgressLogger.create(this, "nGramCount");
      Counter<Tuple> counter = newCounter(parallelStream().flatMap(doc -> {
         progressLogger.start();
         Stream<Tuple> stream = nGramExtractor.extractStringTuples(doc).stream();
         progressLogger.stop(doc.tokenLength());
         return stream;
      }).countByValue());
      progressLogger.report();
      return counter;
   }

   /**
    * Gets a parallel stream over the documents in the collection
    *
    * @return the stream of documents
    */
   MStream<Document> parallelStream();

   /**
    * Generates a new Corpus from the results of querying this corpus.
    *
    * @param query the query
    * @return the SearchResult containing documents matching the query
    * @throws ParseException the parse exception
    */
   default SearchResults query(@NonNull String query) throws ParseException {
      return query(QueryParser.parse(query));
   }

   /**
    * Generates a new Corpus from the results of querying this corpus.
    *
    * @param query the query
    * @return the SearchResult containing documents matching the query
    */
   SearchResults query(@NonNull Query query);

   /**
    * Repartitions the corpus.
    *
    * @param numPartitions the number of partitions
    * @return the corpus
    */
   default DocumentCollection repartition(int numPartitions) {
      return this;
   }

   /**
    * Create a sample of this corpus using <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir
    * sampling</a>.
    *
    * @param size the number of documents to include in the sample
    * @return the sampled corpus
    */
   default DocumentCollection sample(int size) {
      return new MStreamDocumentCollection(stream().sample(false, size));
   }

   /**
    * Create a sample of this corpus using <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir
    * sampling</a>.
    *
    * @param count  the number of documents to include in the sample
    * @param random Random number generator to use for selection
    * @return the sampled corpus
    */
   default DocumentCollection sample(int count, @NonNull Random random) {
      if(count <= 0) {
         return new MStreamDocumentCollection(StreamingContext.local().empty());
      }
      List<Document> sample = stream().limit(count).collect();
      AtomicInteger k = new AtomicInteger(count + 1);
      stream().skip(count).forEach(document -> {
         int rndIndex = random.nextInt(k.getAndIncrement());
         if(rndIndex < count) {
            sample.set(rndIndex, document);
         }
      });
      return new MStreamDocumentCollection(StreamingContext.local().stream(sample).parallel());
   }

   /**
    * Calculates the bigrams with a significant co-occurrence using the Mikolov association measure.
    *
    * @param nGramExtractor the extractor to use for extracting NGrams
    * @param minCount       the minimum co-occurrence count for a bigram to be considered
    * @param minScore       the minimum score for a bigram to be significant
    * @return the counter of bigrams and their scores
    */
   default Counter<Tuple> significantBigrams(@NonNull NGramExtractor nGramExtractor, int minCount, double minScore) {
      return significantBigrams(nGramExtractor, minCount, minScore, Association.Mikolov);
   }

   /**
    * Calculates the bigrams with a significant co-occurrence using the given association measure.
    *
    * @param nGramExtractor the extractor to use for extracting NGrams
    * @param minCount       the minimum co-occurrence count for a bigram to be considered
    * @param minScore       the minimum score for a bigram to be significant
    * @param calculator     the association measure to use for determining significance
    * @return the counter of bigrams and their scores
    */
   default Counter<Tuple> significantBigrams(@NonNull NGramExtractor nGramExtractor,
                                             int minCount,
                                             double minScore,
                                             @NonNull ContingencyTableCalculator calculator
                                            ) {
      NGramExtractor temp = nGramExtractor.toBuilder().minOrder(1).maxOrder(2).build();
      Counter<Tuple> ngrams = nGramCount(temp).filterByValue(v -> v >= minCount);
      Counter<Tuple> unigrams = ngrams.filterByKey(t -> t.degree() == 1);
      Counter<Tuple> bigrams = ngrams.filterByKey(t -> t.degree() == 2);
      ngrams.clear();
      Counter<Tuple> filtered = newCounter();
      bigrams.items().forEach(bigram -> {
         double score = calculator.calculate(ContingencyTable.create2X2(bigrams.get(bigram),
                                                                        unigrams.get(bigram.slice(0, 1)),
                                                                        unigrams.get(bigram.slice(1, 2)),
                                                                        unigrams.sum()));
         if(score >= minScore) {
            filtered.set(bigram, score);
         }
      });
      return filtered;
   }

   /**
    * The number of documents in the corpus
    *
    * @return the number of documents in the corpus
    */
   default long size() {
      return parallelStream().count();
   }

   /**
    * Gets a stream over the documents in the collection
    *
    * @return the stream of documents
    */
   MStream<Document> stream();

   /**
    * Calculates the total corpus frequency of terms extracted using the given extractor.
    *
    * @param extractor the extractor to use for generating terms
    * @return the counter of terms with frequencies
    */
   default Counter<String> termCount(@NonNull Extractor extractor) {
      ProgressLogger progressLogger = ProgressLogger.create(this, "termCount");
      MCounterAccumulator<String> termCounts = getStreamingContext().counterAccumulator();
      parallelStream().parallel()
                      .forEach(doc -> {
                         progressLogger.start();
                         termCounts.merge(extractor.extract(doc).count());
                         progressLogger.stop(doc.tokenLength());
                      });
      progressLogger.report();
      return termCounts.value();
   }

   /**
    * Updates all documents in the corpus using the given document processor
    *
    * @param operationName     the name of the update operation being performed
    * @param documentProcessor the document processor
    * @return this corpus with updates
    */
   DocumentCollection update(String operationName, @NonNull SerializableConsumer<Document> documentProcessor);

   /**
    * Updates all documents in the corpus using the given {@link CaduceusProgram}
    *
    * @param program the CaduceusProgram to execute on each document.
    * @return this corpus with updates
    */
   default DocumentCollection update(@NonNull CaduceusProgram program) {
      return update("ExecuteCaduceusProgram", program::execute);
   }
}//END OF DocumentCollection
