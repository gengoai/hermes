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

import com.gengoai.collection.counter.ConcurrentHashMapCounter;
import com.gengoai.collection.counter.Counter;
import com.gengoai.function.SerializableConsumer;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * The type In memory corpus.
 *
 * @author David B. Bracewell
 */
public class InMemoryCorpus implements Corpus, Serializable {
   private static final long serialVersionUID = 1L;
   private List<Document> corpus = new ArrayList<>();
   private Counter<AnnotatableType> typeCounter = new ConcurrentHashMapCounter<>();

   /**
    * Instantiates a new In memory corpus.
    *
    * @param documents the documents
    */
   public InMemoryCorpus(@NonNull Collection<Document> documents) {
      corpus.addAll(documents);
   }

   /**
    * Instantiates a new In memory corpus.
    *
    * @param documents the documents
    */
   public InMemoryCorpus(@NonNull Document... documents) {
      Collections.addAll(corpus, documents);
   }

   /**
    * Instantiates a new In memory corpus.
    *
    * @param documents the documents
    */
   public InMemoryCorpus(@NonNull Stream<Document> documents) {
      documents.forEach(this::add);
   }

   @Override
   public boolean add(@NonNull Document document) {
      corpus.add(document);
      typeCounter.incrementAll(document.completed());
      return true;
   }

   @Override
   public MStream<Document> parallelStream() {
      return stream().parallel();
   }

   @Override
   public Corpus cache() {
      return this;
   }

   @Override
   public void close() {

   }

   @Override
   public Corpus filter(@NonNull SerializablePredicate<? super Document> filter) {
      return new InMemoryCorpus(corpus.parallelStream().filter(filter));
   }

   @Override
   public Document get(@NonNull String id) {
      return corpus.parallelStream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
   }

   @Override
   public Set<AnnotatableType> getCompletedAnnotations() {
      final double size = size();
      return typeCounter.filterByValue(d -> d == size).items();
   }

   @Override
   public StreamingContext getStreamingContext() {
      return StreamingContext.local();
   }

   @Override
   public boolean isEmpty() {
      return corpus.isEmpty();
   }

   @Override
   public boolean isPersistent() {
      return true;
   }

   @Override
   public Iterator<Document> iterator() {
      return corpus.iterator();
   }

   @Override
   public boolean remove(@NonNull Document document) {
      return corpus.remove(document);
   }

   @Override
   public boolean remove(@NonNull String id) {
      Document document = get(id);
      return document != null && corpus.remove(document);
   }

   @Override
   public MStream<Document> stream() {
      return getStreamingContext().stream(corpus);
   }

   @Override
   public boolean update(@NonNull Document document) {
      int index = corpus.indexOf(document);
      if (index < 0) {
         return false;
      }
      typeCounter.decrementAll(corpus.get(index).completed());
      typeCounter.incrementAll(document.completed());
      corpus.set(index, document);
      return true;
   }

   @Override
   public Corpus update(@NonNull SerializableConsumer<Document> documentProcessor) {
      corpus.parallelStream().forEach(document -> {
         typeCounter.decrementAll(document.completed());
         documentProcessor.accept(document);
         typeCounter.incrementAll(document.completed());
      });
      return this;
   }


}//END OF InMemoryCorpus
