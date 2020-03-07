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

import com.gengoai.apollo.ml.data.DatasetType;
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.statistics.measure.Association;
import com.gengoai.apollo.statistics.measure.ContingencyTable;
import com.gengoai.apollo.statistics.measure.ContingencyTableCalculator;
import com.gengoai.collection.Sets;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.config.Config;
import com.gengoai.function.SerializableConsumer;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.*;
import com.gengoai.hermes.corpus.io.CorpusParameters;
import com.gengoai.hermes.corpus.io.CorpusReader;
import com.gengoai.hermes.corpus.io.CorpusWriter;
import com.gengoai.hermes.corpus.io.SaveMode;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.hermes.extraction.NGramExtractor;
import com.gengoai.hermes.extraction.regex.TokenMatch;
import com.gengoai.hermes.extraction.regex.TokenMatcher;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.hermes.workflow.SequentialWorkflow;
import com.gengoai.io.resource.Resource;
import com.gengoai.logging.Loggable;
import com.gengoai.parsing.ParseException;
import com.gengoai.specification.Specification;
import com.gengoai.stream.MCounterAccumulator;
import com.gengoai.stream.MLongAccumulator;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import com.gengoai.tuple.Tuple;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.Validation.notNull;
import static com.gengoai.collection.counter.Counters.newCounter;

/**
 * <p>
 * Represents a collection of documents each having a unique document ID. Corpus formats are  defined via corresponding
 * <code>CorpusFormat</code> objects, which are registered using Java's service loader functionality. When constructing
 * a corpus the format can be appended with <code>_OPL</code> to denote that individual file will have one document per
 * line in the given format. For example, TEXT_OPL would relate to a format where every line of a file equates to a
 * document in plain text format.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface Corpus extends Iterable<Document>, AutoCloseable, Loggable {
   /**
    * The constant REPORT_INTERVAL.
    */
   String REPORT_INTERVAL = "Corpus.reportInterval";
   /**
    * The constant REPORT_LEVEL.
    */
   String REPORT_LEVEL = "Corpus.reportLevel";

   /**
    * Creates an in-memory corpus for the given documents.
    *
    * @param documents the documents
    * @return the corpus
    */
   static Corpus create(@NonNull Document... documents) {
      return create(Arrays.asList(documents));
   }

   /**
    * Creates an in-memory corpus for the given documents.
    *
    * @param documents the documents
    * @return the corpus
    */
   static Corpus create(@NonNull List<Document> documents) {
      return new InMemoryCorpus(documents);
   }

   /**
    * Creates a corpus for the given MStream of documents
    *
    * @param documents the documents
    * @return the corpus
    */
   static Corpus create(@NonNull MStream<Document> documents) {
      if(documents.isDistributed()) {
         MStream<String> strStream = documents.map(Document::toJson).cache();
         strStream.count();
         return new SparkCorpus(strStream);
      }
      return new StreamingCorpus(documents);
   }

   /**
    * Creates a corpus for the given Stream of documents
    *
    * @param documents the documents
    * @return the corpus
    */
   static Corpus create(@NonNull Stream<Document> documents) {
      return new StreamingCorpus(StreamingContext.local().stream(documents));
   }

   /**
    * Reads in a  corpus stored at the given resource checking the configuration option
    * <code>com.gengoai.com.gengoai.hermes.corpus.Corpus.defaultFormat</code> and defaulting to <code>LUCENE</code>
    * when not specified.
    *
    * @param specification the specification of the corpus
    * @return the corpus
    * @throws IOException Something went wrong reading the corpus
    */
   static Corpus read(@NonNull String specification) throws IOException {
      return read(CorpusIOService.parseCorpusSpecification(specification));
   }

   /**
    * Reads in a  corpus stored at the given resource checking the configuration option
    * <code>com.gengoai.com.gengoai.hermes.corpus.Corpus.defaultFormat</code> and defaulting to <code>LUCENE</code>
    * when not specified.
    *
    * @param specification the specification of the corpus
    * @return the corpus
    * @throws IOException Something went wrong reading the corpus
    */
   static Corpus read(@NonNull Specification specification) throws IOException {
      CorpusReader reader = CorpusIOService.getReaderFor(specification.getSchema().toUpperCase());
      reader.getOptions()
            .parameterNames()
            .forEach(name -> {
               if(specification.getQueryValue(name, null) != null) {
                  reader.option(name, specification.getQueryValue(name, null));
               }
            });
      return reader.read(specification.getPath());
   }

   /**
    * Reads in a  corpus stored at the given resource checking the configuration option
    * <code>com.gengoai.com.gengoai.hermes.corpus.Corpus.defaultFormat</code> and defaulting to <code>LUCENE</code>
    * when not specified.
    *
    * @param resource the location of the corpus
    * @return the corpus
    * @throws IOException Something went wrong reading the corpus
    */
   static Corpus read(@NonNull Resource resource) throws IOException {
      return reader(Hermes.defaultCorpusFormat()).read(resource);
   }

   /**
    * Creates a {@link CorpusReader} for the default corpus format (<code>Corpus.defaultFormat</code>)
    *
    * @return the corpus reader
    */
   static CorpusReader reader() {
      return CorpusIOService.getReaderFor(Hermes.defaultCorpusFormat());
   }

   /**
    * Creates a {@link CorpusReader} for the given corpus format
    *
    * @param format the format to get a reader for
    * @return the corpus reader
    */
   static CorpusReader reader(@NonNull CharSequence format) {
      return CorpusIOService.getReaderFor(format.toString());
   }

   /**
    * Adds a document to the corpus
    *
    * @param document the document to add
    * @return True if added, False if not
    */
   boolean add(Document document);

   /**
    * Adds multiple documents to the corpus.
    *
    * @param documents the documents
    */
   default void addAll(@NonNull Iterable<Document> documents) {
      documents.forEach(this::add);
   }

   /**
    * Annotates this corpus with the given annotation types and returns a new corpus with the given annotation types
    * present
    *
    * @param annotatableTypes The annotation types to annotate
    * @return A new corpus with the given annotation types present.
    */
   default Corpus annotate(@NonNull AnnotatableType... annotatableTypes) {
      AnnotationPipeline pipeline = new AnnotationPipeline(Sets.difference(Arrays.asList(annotatableTypes),
                                                                           getCompletedAnnotations()));
      if(pipeline.requiresUpdate()) {
         return update(pipeline::annotate);
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
   default Corpus apply(Lexicon lexicon, SerializableConsumer<HString> onMatch) {
      notNull(lexicon, "Lexicon must not be null");
      notNull(onMatch, "OnMatch consumer must not be null");
      update(doc -> lexicon.extract(doc).forEach(onMatch));
      return this;
   }

   /**
    * Applies token regular expression to the corpus creating annotations of the given type for matches.
    *
    * @param pattern the pattern
    * @param onMatch the on match
    * @return the corpus
    */
   default Corpus apply(TokenRegex pattern, SerializableConsumer<TokenMatch> onMatch) {
      notNull(pattern, "Lexicon must not be null");
      notNull(onMatch, "OnMatch consumer must not be null");
      update(doc -> {
         TokenMatcher matcher = pattern.matcher(doc);
         while(matcher.find()) {
            onMatch.accept(matcher.asTokenMatch());
         }
      });
      return this;
   }

   /**
    * Constructs a Dataset from the Corpus according to the {@link ExampleGenerator}
    *
    * @param exampleGenerator the example generator to use to construct the dataset
    * @return the dataset
    */
   default ExampleDataset asDataset(@NonNull ExampleGenerator exampleGenerator, @NonNull DatasetType datasetType) {
      return ExampleDataset.builder()
                           .type(datasetType)
                           .source(stream().flatMap(exampleGenerator::apply));
   }


   default List<String> getIds(){
      return stream().map(Document::getId).sorted(true).collect();
   }

   MStream<Document> parallelStream();

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
    * @param updater the dataset definition
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
    * @param updater the dataset definition
    * @return the dataset
    */
   default ExampleDataset asSequenceDataset(@NonNull Consumer<SequenceGenerator> updater,
                                            @NonNull DatasetType datasetType) {
      return asDataset(ExampleGenerator.sequence(updater), datasetType);
   }

   /**
    * Caches the corpus
    *
    * @return the cached corpus
    */
   Corpus cache();

   /**
    * Compact corpus.
    *
    * @return the corpus
    */
   default Corpus compact() {
      return this;
   }

   /**
    * Calculates the document frequency of annotations of the given annotation type in the corpus. Annotations are
    * transformed into strings using the given toString function.
    *
    * @param extractor the LyreExpression to use for extracting terms
    * @return A counter containing document frequencies of the given annotation type
    */
   default Counter<String> documentCount(@NonNull Extractor extractor) {
      MLongAccumulator counter = getStreamingContext().longAccumulator();
      MCounterAccumulator<String> termCounts = getStreamingContext().counterAccumulator();
      final long reportInterval = Config.get(REPORT_INTERVAL).asLongValue(5_000);
      final Level reportLevel = Config.get(REPORT_LEVEL).as(Level.class, Level.FINE);
      stream().forEach(doc -> {
         counter.add(1);
         counter.report(count -> count % reportInterval == 0,
                        count -> log(reportLevel, "documentFrequencies: Processed {0} documents", count));
         extractor.extract(doc)
                  .count()
                  .forEach((term, count) -> termCounts.increment(term, 1.0));
      });
      return termCounts.value();
   }

   /**
    * Constructs a new Corpus made up of this Corpus's documents filtered using the given filter.
    *
    * @param filter the filter
    * @return the corpus
    */
   Corpus filter(SerializablePredicate<? super Document> filter);

   /**
    * Get document.
    *
    * @param index the index
    * @return the document
    */
   default Document get(long index) {
      return stream().skip(index).first().orElseThrow(NoSuchElementException::new);
   }

   /**
    * Gets a document by id
    *
    * @param id the id of the document
    * @return the document or null if it doesn't exist
    */
   Document get(String id);

   /**
    * Gets attribute count.
    *
    * @param <T>  the type parameter
    * @param type the type
    * @return the attribute count
    */
   default <T> Counter<T> getAttributeCount(@NonNull AttributeType<T> type) {
      return Counters.newCounter(stream().map(d -> d.attribute(type)).countByValue());
   }

   /**
    * Gets attribute types.
    *
    * @return the attribute types
    */
   default Set<AttributeType<?>> getAttributeTypes() {
      return stream().flatMap(d -> d.attributeMap().keySet().stream())
                     .distinct()
                     .collect(Collectors.toSet());
   }

   /**
    * Gets completed annotations.
    *
    * @return the completed annotations
    */
   default Set<AnnotatableType> getCompletedAnnotations() {
      return stream().parallel()
                     .flatMap(d -> d.completed().stream())
                     .countByValue()
                     .entrySet()
                     .stream()
                     .filter(e -> e.getValue() == size())
                     .map(Map.Entry::getKey)
                     .collect(Collectors.toSet());
   }

   /**
    * Gets streaming context.
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
    * Is empty boolean.
    *
    * @return the boolean
    */
   boolean isEmpty();

   /**
    * Checks if this corpus is persistent or not. Persistent corpora are updated in place whereas non-persistent corpora
    * need to have their changes written to disk to be carried over to future runs.
    *
    * @return True if persistent, False otherwise
    */
   boolean isPersistent();

   /**
    * Calculates the total corpus frequencies for NGrams extracted using the given extractor. Note tha all n-grams are
    * returned in their string form as Tuples.
    *
    * @param nGramExtractor the extractor
    * @return the counter of string tuples representing the ngrams
    */
   default Counter<Tuple> nGramCount(@NonNull NGramExtractor nGramExtractor) {
      MLongAccumulator counter = getStreamingContext().longAccumulator();
      final long reportInterval = Config.get(REPORT_INTERVAL).asLongValue(5_000);
      final Level reportLevel = Config.get(REPORT_LEVEL).as(Level.class, Level.FINE);
      return newCounter(stream().parallel()
                                .flatMap(doc -> {
                                   counter.add(1);
                                   counter.report(
                                         count -> count % reportInterval == 0,
                                         count -> log(reportLevel,
                                                      "documentFrequencies: Processed {0} documents",
                                                      count));
                                   return nGramExtractor.extractStringTuples(doc).stream();
                                })
                                .countByValue());
   }

   /**
    * Processes the corpus using the given {@link SequentialWorkflow}
    *
    * @param processor the processor
    * @return this Corpus
    * @throws Exception the exception
    */
   default Corpus process(@NonNull SequentialWorkflow processor) throws Exception {
      processor.process(null, new Context());
      return this;
   }

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
   default SearchResults query(@NonNull Query query) {
      return new CorpusBasedSearchResults(filter(query::matches), query);
   }

   /**
    * Removes a document from the corpus
    *
    * @param document the document to remove
    * @return True of removed, False otherwise
    */
   boolean remove(Document document);

   /**
    * Removes a document by its id.
    *
    * @param id the id of the document to remove
    * @return True of removed, False otherwise
    */
   boolean remove(String id);

   /**
    * Repartitions the corpus.
    *
    * @param numPartitions the number of partitions
    * @return the corpus
    */
   default Corpus repartition(int numPartitions) {
      return this;
   }

   /**
    * Create a sample of this corpus using <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir
    * sampling</a>.
    *
    * @param size the number of documents to include in the sample
    * @return the sampled corpus
    */
   default Corpus sample(int size) {
      return new StreamingCorpus(stream().sample(false, size));
   }

   /**
    * Create a sample of this corpus using <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir
    * sampling</a>.
    *
    * @param count  the number of documents to include in the sample
    * @param random Random number generator to use for selection
    * @return the sampled corpus
    */
   default Corpus sample(int count, @NonNull Random random) {
      if(count <= 0) {
         return new StreamingCorpus(StreamingContext.local().empty());
      }
      List<Document> sample = stream().limit(count).collect();
      AtomicInteger k = new AtomicInteger(count + 1);
      stream().skip(count).forEach(document -> {
         int rndIndex = random.nextInt(k.getAndIncrement());
         if(rndIndex < count) {
            sample.set(rndIndex, document);
         }
      });
      return new StreamingCorpus(StreamingContext.local().stream(sample).parallel());
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
      return stream().count();
   }

   /**
    * Gets a stream over the documents in the corpus
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
      MLongAccumulator counter = getStreamingContext().longAccumulator();
      MCounterAccumulator<String> termCounts = getStreamingContext().counterAccumulator();
      final long reportInterval = Config.get(REPORT_INTERVAL).asLongValue(5_000);
      final Level reportLevel = Config.get(REPORT_LEVEL).as(Level.class, Level.FINE);
      stream().parallel()
              .forEach(doc -> {
                 counter.add(1);
                 counter.report(count -> count % reportInterval == 0,
                                count -> log(reportLevel, "termCount: Processed {0} documents", count));
                 termCounts.merge(extractor.extract(doc)
                                           .count());
              });
      return termCounts.value();
   }

   /**
    * Converts the corpus to a distributed corpus using Apache Spark
    *
    * @return the distributed corpus
    */
   default Corpus toDistributed() {
      if(this instanceof SparkCorpus) {
         return this;
      }
      return new SparkCorpus(stream().map(Document::toJson).toDistributedStream());
   }

   /**
    * Converts the corpus to an in-memory corpus backed as a list of documents
    *
    * @return the in-memory corpus
    */
   default Corpus toInMemory() {
      if(this instanceof InMemoryCorpus) {
         return this;
      }
      return new InMemoryCorpus(this.stream().javaStream());
   }

   /**
    * Updates the given document
    *
    * @param document the document to update
    * @return True if the document is updated, False if not
    */
   boolean update(Document document);

   /**
    * Updates all documents in the corpus using the given document processor
    *
    * @param documentProcessor the document processor
    * @return this corpus with updates
    */
   Corpus update(@NonNull SerializableConsumer<Document> documentProcessor);

   /**
    * Writes the corpus to disk as a LUCENE index
    *
    * @param resource the directory to write the corpus to
    * @throws IOException Something went wrong writing the corpus
    */
   default void write(@NonNull Resource resource) throws IOException {
      writer(Hermes.defaultCorpusFormat()).write(resource);
   }

   /**
    * Writes the corpus to disk as a LUCENE index
    *
    * @param resource the directory to write the corpus to
    * @param saveMode Mode to save the corpus in
    * @throws IOException Something went wrong writing the corpus
    */
   default void write(@NonNull Resource resource, @NonNull SaveMode saveMode) throws IOException {
      writer(Hermes.defaultCorpusFormat()).option(CorpusParameters.SAVE_MODE, saveMode).write(resource);
   }

   /**
    * Writes the corpus to disk as a LUCENE index
    *
    * @param specification the specification of the writer
    * @param saveMode      Mode to save the corpus in
    * @throws IOException Something went wrong writing the corpus
    */
   default void write(@NonNull String specification, @NonNull SaveMode saveMode) throws IOException {
      write(CorpusIOService.parseCorpusSpecification(specification)
                           .setQueryParameter(CorpusParameters.SAVE_MODE.name, saveMode));
   }

   /**
    * Writes the corpus to disk as a LUCENE index
    *
    * @param specification the specification of the writer
    * @throws IOException Something went wrong writing the corpus
    */
   default void write(@NonNull Specification specification) throws IOException {
      final CorpusWriter writer = writer(specification.getSchema().toUpperCase());
      writer.getOptions()
            .parameterNames()
            .forEach(name -> {
               if(specification.getQueryValue(name, null) != null) {
                  writer.option(name, specification.getQueryValue(name, null));
               }
            });
      writer.write(specification.getPath());
   }

   /**
    * Writes the corpus to disk as a LUCENE index
    *
    * @param specification the specification of the writer
    * @throws IOException Something went wrong writing the corpus
    */
   default void write(@NonNull String specification) throws IOException {
      write(CorpusIOService.parseCorpusSpecification(specification));
   }

   /**
    * Creates a {@link CorpusWriter} for the given format.
    *
    * @return the {@link CorpusWriter}
    */
   default CorpusWriter writer() {
      return CorpusIOService.getWriterFor(Hermes.defaultCorpusFormat(), this);
   }

   /**
    * Creates a {@link CorpusWriter} for the given format.
    *
    * @param format the format to write in
    * @return the {@link CorpusWriter}
    */
   default CorpusWriter writer(@NonNull CharSequence format) {
      return CorpusIOService.getWriterFor(format.toString(), this);
   }

}//END OF Corpus
