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
import com.gengoai.hermes.Document;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.util.Iterator;

/**
 * The type Search corpus.
 *
 * @author David B. Bracewell
 */
class SearchCorpus implements Corpus {
   private final SearchResults searchResults;

   /**
    * Instantiates a new Search corpus.
    *
    * @param searchResults the iterator
    */
   SearchCorpus(SearchResults searchResults) {
      this.searchResults = searchResults;
   }

   @Override
   public boolean add(Document document) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Corpus cache() {
      return new InMemoryCorpus(stream().javaStream());
   }

   @Override
   public void close() {

   }

   @Override
   public Corpus filter(SerializablePredicate<? super Document> filter) {
      return new StreamingCorpus(stream().filter(filter));
   }

   @Override
   public Document get(@NonNull String id) {
      return stream().filter(d -> d.getId().equals(id)).first().orElse(null);
   }

   @Override
   public StreamingContext getStreamingContext() {
      return StreamingContext.local();
   }

   @Override
   public boolean isEmpty() {
      return size() == 0;
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public Iterator<Document> iterator() {
      return searchResults.iterator();
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
   public long size() {
      return searchResults.getTotalHits();
   }

   @Override
   public MStream<Document> stream() {
      return getStreamingContext().stream(searchResults);
   }

   @Override
   public boolean update(Document document) {
      return false;
   }

   @Override
   public Corpus update(@NonNull SerializableConsumer<Document> documentProcessor) {
      return new StreamingCorpus(stream().map(d -> {
         documentProcessor.accept(d);
         return d;
      }));
   }

}//END OF LuceneSearchCorpus
