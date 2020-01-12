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
import com.gengoai.hermes.Document;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Specialized Corpus backed by Spark Distributed MStream. Documents are stored in json and converted into documents as
 * needed in order to save memory.
 *
 * @author David B. Bracewell
 */
public class SparkCorpus implements Corpus, Serializable {
   private static final long serialVersionUID = 1L;
   private SparkDocumentStream sparkStream;

   /**
    * Instantiates a new Spark corpus.
    *
    * @param sparkStream the spark stream
    */
   public SparkCorpus(MStream<String> sparkStream) {
      this.sparkStream = new SparkDocumentStream(sparkStream);
   }

   public SparkCorpus(List<Document> documents) {
      this.sparkStream = new SparkDocumentStream(StreamingContext.distributed()
                                                                 .stream(documents.stream().map(Document::toJson)));
   }

   protected SparkCorpus(SparkDocumentStream sparkStream) {
      this.sparkStream = sparkStream;
   }

   @Override
   public Corpus annotate(AnnotatableType... annotatableTypes) {
      sparkStream = sparkStream.annotate(annotatableTypes);
      return this;
   }

   @Override
   public boolean add(Document document) {
      return false;
   }

   @Override
   public Corpus cache() {
      sparkStream.cache();
      return this;
   }

   @Override
   public void close() throws Exception {
      sparkStream.close();
   }

   @Override
   public Corpus filter(SerializablePredicate<? super Document> filter) {
      return new SparkCorpus((SparkDocumentStream) sparkStream.filter(filter));
   }

   @Override
   public Document get(String id) {
      return sparkStream.filter(d -> d.getId().equals(id))
                        .first()
                        .orElse(null);
   }

   @Override
   public StreamingContext getStreamingContext() {
      return StreamingContext.distributed();
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public Iterator<Document> iterator() {
      return sparkStream.iterator();
   }

   @Override
   public boolean remove(Document document) {
      return false;
   }

   @Override
   public boolean remove(String id) {
      return false;
   }

   @Override
   public Corpus repartition(int numPartitions) {
      sparkStream.repartition(numPartitions);
      return this;
   }

   @Override
   public MStream<Document> stream() {
      return sparkStream;
   }

   @Override
   public boolean update(Document document) {
      return false;
   }

   @Override
   public Corpus update(SerializableConsumer<Document> documentProcessor) {
      sparkStream = new SparkDocumentStream(sparkStream.map(doc -> {
         documentProcessor.accept(doc);
         return doc.toJson();
      }));
      return this;
   }

   @Override
   public boolean isEmpty() {
      return sparkStream.isEmpty();
   }


}//END OF SparkCorpus
