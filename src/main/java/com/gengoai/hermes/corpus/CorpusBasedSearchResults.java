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

import com.gengoai.hermes.Document;
import lombok.NonNull;

import java.util.Iterator;

/**
 * The type Corpus based search results.
 *
 * @author David B. Bracewell
 */
class CorpusBasedSearchResults implements SearchResults {
   private final Corpus corpus;
   private final Query query;

   /**
    * Instantiates a new Corpus based search results.
    *
    * @param corpus the corpus
    * @param query  the query
    */
   CorpusBasedSearchResults(@NonNull Corpus corpus, @NonNull Query query) {
      this.corpus = corpus;
      this.query = query;
   }

   @Override
   public Query getQuery() {
      return query;
   }

   @Override
   public long getTotalHits() {
      return corpus.size();
   }

   @Override
   public Iterator<Document> iterator() {
      return corpus.iterator();
   }

   @Override
   public Corpus asCorpus() {
      return corpus;
   }
}//END OF CorpusBasedSearchResults
