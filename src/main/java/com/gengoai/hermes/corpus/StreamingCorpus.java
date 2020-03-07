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

import com.gengoai.function.SerializableConsumer;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.AnnotationPipeline;
import com.gengoai.hermes.Document;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Generic corpus implementation backed my an MStream.
 *
 * @author David B. Bracewell
 */
public class StreamingCorpus implements Corpus, Serializable {
   private static final long serialVersionUID = 1L;
   private transient MStream<Document> stream;

   /**
    * Instantiates a new Streaming corpus.
    *
    * @param stream the stream
    */
   public StreamingCorpus(@NonNull MStream<Document> stream) {
      this.stream = stream;
   }

   @Override
   public Corpus annotate(@NonNull AnnotatableType... annotatableTypes) {
      return update(new AnnotationPipeline(annotatableTypes)::annotate);
   }

   @Override
   public boolean add(@NonNull Document document) {
      this.stream = this.stream.union(stream.getContext().stream(document));
      return true;
   }

   @Override
   public Corpus cache() {
      stream = stream.cache();
      return this;
   }

   @Override
   public void close() throws Exception {
      stream.close();
   }

   @Override
   public Corpus filter(@NonNull SerializablePredicate<? super Document> filter) {
      return new StreamingCorpus(stream.filter(filter));
   }

   @Override
   public Document get(@NonNull String id) {
      return stream.filter(doc -> doc.getId().equals(id)).first().orElse(null);
   }

   @Override
   public StreamingContext getStreamingContext() {
      return stream.getContext();
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public Iterator<Document> iterator() {
      return stream.iterator();
   }

   @Override
   public boolean remove(@NonNull Document document) {
      return false;
   }

   @Override
   public boolean remove(@NonNull String id) {
      return false;
   }

   @Override
   public Corpus repartition(int numPartitions) {
      stream = stream.repartition(numPartitions);
      return this;
   }

   @Override
   public MStream<Document> parallelStream() {
      return stream.parallel();
   }

   @Override
   public MStream<Document> stream() {
      return stream;
   }

   @Override
   public boolean update(@NonNull Document document) {
      return false;
   }

   @Override
   public Corpus update(@NonNull SerializableConsumer<Document> documentProcessor) {
      stream = stream.map(doc -> {
         documentProcessor.accept(doc);
         return doc;
      });
      return this;
   }

   @Override
   public boolean isEmpty() {
      return stream.isEmpty();
   }
}//END OF StreamingCorpus
