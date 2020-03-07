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
 */

package com.gengoai.hermes.corpus;

import com.gengoai.collection.Iterators;
import com.gengoai.stream.Streams;
import com.gengoai.conversion.Cast;
import com.gengoai.function.*;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.AnnotationPipeline;
import com.gengoai.hermes.Document;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.*;
import com.gengoai.stream.spark.SparkStream;
import com.gengoai.stream.spark.SparkStreamingContext;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Spark document stream.
 *
 * @author David B. Bracewell
 */
class SparkDocumentStream implements MStream<Document>, Serializable {
   private static final long serialVersionUID = 1L;
   private SparkStream<String> source;

   /**
    * Instantiates a new Spark document stream.
    *
    * @param source the source
    */
   public SparkDocumentStream(@NonNull MStream<String> source) {
      this.source = new SparkStream<>(source);
   }

   /**
    * Annotate spark document stream.
    *
    * @param types the types
    * @return the spark document stream
    */
   public SparkDocumentStream annotate(final AnnotatableType... types) {
      final AnnotationPipeline annotationPipeline = new AnnotationPipeline(types);
      return new SparkDocumentStream(
         source.mapPartitions(jsonIterable -> Streams.asStream(Cast.<String>cast(jsonIterable)).map(json -> {
            if (Document.hasAnnotations(json, types)) {
               return json;
            }
            Document document = Document.fromJson(json);
            annotationPipeline.annotate(document);
            return document.toJson();
         })));
   }

   @Override
   public MStream<Document> cache() {
      return of(source.cache());
   }

   @Override
   public void close() throws IOException {
      source.close();
   }

   @Override
   public List<Document> collect() {
      return source.map(json -> Document.fromJson(json)).collect();
   }

   @Override
   public <R> R collect(Collector<? super Document, ?, R> collector) {
      return source.map(Document::fromJson).collect(collector);
   }

   @Override
   public long count() {
      return source.count();
   }

   @Override
   public Map<Document, Long> countByValue() {
      return source.map(json -> Document.fromJson(json)).countByValue();
   }

   @Override
   public MStream<Document> distinct() {
      return of(source.distinct());
   }

   @Override
   public MStream<Document> filter(@NonNull SerializablePredicate<? super Document> predicate) {
      return of(source.filter(json -> {

         return predicate.test(Document.fromJson(json));
      }));
   }

   @Override
   public Optional<Document> first() {
      return source.first().map(Document::fromJson);
   }

   @Override
   public <R> MStream<R> flatMap(@NonNull SerializableFunction<? super Document, Stream<? extends R>> mapper) {
      return source.flatMap(json -> {

         return mapper.apply(Document.fromJson(json));
      });
   }

   @Override
   public <R, U> MPairStream<R, U> flatMapToPair(@NonNull SerializableFunction<? super Document, Stream<? extends Map.Entry<? extends R, ? extends U>>> function) {
      return source.flatMapToPair(json -> {

         return function.apply(Document.fromJson(json));
      });
   }

   @Override
   public Document fold(@NonNull Document zeroValue, @NonNull SerializableBinaryOperator<Document> operator) {
      return source.map(json -> Document.fromJson(json)).fold(zeroValue, operator);
   }

   @Override
   public void forEach(SerializableConsumer<? super Document> consumer) {
      source.forEach(json -> {

         consumer.accept(Document.fromJson(json));
      });
   }

   @Override
   public void forEachLocal(SerializableConsumer<? super Document> consumer) {
      source.forEachLocal(json -> {

         consumer.accept(Document.fromJson(json));
      });
   }

   @Override
   public StreamingContext getContext() {
      return source.getContext();
   }

   /**
    * Gets source.
    *
    * @return the source
    */
   protected MStream<String> getSource() {
      return source;
   }

   @Override
   public <U> MPairStream<U, Iterable<Document>> groupBy(@NonNull SerializableFunction<? super Document, ? extends U> function) {
      return source.map(json -> Document.fromJson(json)).groupBy(document -> {

         return function.apply(document);
      });
   }

   @Override
   public MStream<Document> intersection(MStream<Document> other) {
      if (other instanceof SparkDocumentStream) {
         SparkDocumentStream sdsOther = Cast.as(other);
         return new SparkDocumentStream(source.intersection(sdsOther.source));
      }
      return toDistributedStream().intersection(other);
   }

   @Override
   public boolean isDistributed() {
      return true;
   }

   @Override
   public boolean isEmpty() {
      return source.isEmpty();
   }

   @Override
   public Iterator<Document> iterator() {
      return Iterators.transform(source.iterator(), Document::fromJson);
   }

   @Override
   public Stream<Document> javaStream() {
      return null;
   }

   @Override
   public MStream<Document> limit(long number) {
      return of(source.limit(number));
   }

   @Override
   public <R> MStream<R> map(@NonNull SerializableFunction<? super Document, ? extends R> function) {
      return source.map(json -> {

         return function.apply(Document.fromJson(json));
      });
   }

   @Override
   public MDoubleStream mapToDouble(@NonNull SerializableToDoubleFunction<? super Document> function) {
      return source.mapToDouble(json -> {

         return function.applyAsDouble(Document.fromJson(json));
      });
   }

   @Override
   public <R, U> MPairStream<R, U> mapToPair(@NonNull SerializableFunction<? super Document, ? extends Map.Entry<? extends R, ? extends U>> function) {
      return source.mapToPair(json -> {

         return function.apply(Document.fromJson(json));
      });
   }

   @Override
   public Optional<Document> max(@NonNull SerializableComparator<? super Document> comparator) {
      return source.map(Document::fromJson).max(comparator);
   }

   @Override
   public Optional<Document> min(@NonNull SerializableComparator<? super Document> comparator) {
      return source.map(Document::fromJson).min(comparator);
   }

   private SparkDocumentStream of(@NonNull MStream<String> source) {
      return new SparkDocumentStream(source);
   }

   @Override
   public MStream<Document> onClose(SerializableRunnable closeHandler) {
      source.onClose(closeHandler);
      return this;
   }

   @Override
   public MStream<Document> parallel() {
      return this;
   }

   @Override
   public MStream<Stream<Document>> partition(long partitionSize) {
      return source.partition(partitionSize).map(json -> json.map(Document::fromJson));
   }

   @Override
   public MStream<Document> persist(StorageLevel storageLevel) {
      source.persist(storageLevel);
      return this;
   }

   @Override
   public Optional<Document> reduce(@NonNull SerializableBinaryOperator<Document> reducer) {
      return source.map(json -> Document.fromJson(json)).reduce(reducer);
   }

   @Override
   public MStream<Document> repartition(int numPartition) {
      source = source.repartition(numPartition);
      return this;
   }

   @Override
   public MStream<Document> sample(boolean withReplacement, int number) {
      return of(source.sample(withReplacement, number));
   }

   @Override
   public void saveAsTextFile(@NonNull Resource location) {
      source.saveAsTextFile(location);
   }

   @Override
   public void saveAsTextFile(@NonNull String location) {
      source.saveAsTextFile(location);
   }

   @Override
   public MStream<Document> shuffle(Random random) {
      return new SparkDocumentStream(source.shuffle(random));
   }

   @Override
   public MStream<Document> skip(long n) {
      return of(source.skip(n));
   }

   @Override
   public <R extends Comparable<R>> MStream<Document> sortBy(boolean ascending,
                                                             SerializableFunction<? super Document, ? extends R> keyFunction) {
      return of(source.sortBy(ascending, k -> {

         return keyFunction.apply(Document.fromJson(k));
      }));
   }

   @Override
   public MStream<Document> sorted(boolean ascending) {
      return of(source.sorted(ascending));
   }

   @Override
   public List<Document> take(int n) {
      return source.take(n).stream().map(Document::fromJson).collect(Collectors.toList());
   }

   @Override
   public MStream<Document> union(@NonNull MStream<Document> other) {
      if (other instanceof SparkDocumentStream) {
         return of(source.union(Cast.<SparkDocumentStream>as(other).source));
      }
      return of(source.union(other.map(Document::toJson)));
   }

   @Override
   public void updateConfig() {
      SparkStreamingContext.INSTANCE.updateConfig();
   }

   @Override
   public <U> MPairStream<Document, U> zip(@NonNull MStream<U> other) {
      return source.map(Document::fromJson).zip(other);
   }

   @Override
   public MPairStream<Document, Long> zipWithIndex() {
      return source.map(Document::fromJson).zipWithIndex();
   }

}//END OF SparkDocumentStream