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

package com.gengoai.hermes.similarity;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.statistics.measure.Similarity;
import com.gengoai.collection.HashMapIndex;
import com.gengoai.collection.Index;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.hermes.extraction.TermExtractor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class TokenSimilarity implements HStringSimilarity {
   private static final long serialVersionUID = 1L;
   @NonNull
   Similarity measure;
   @NonNull
   Extractor termExtractor;

   public TokenSimilarity(@NonNull Similarity measure) {
      this(measure, TermExtractor.builder()
                                 .ignoreStopwords()
                                 .toLemma()
                                 .build());
   }

   public TokenSimilarity(@NonNull Similarity measure,
                          @NonNull Extractor termExtractor) {
      this.measure = measure;
      this.termExtractor = termExtractor;
   }

   @Override
   public double calculate(@NonNull HString first, @NonNull HString second) {
      Counter<String> c1 = Counters.newCounter(termExtractor.extract(first).string());
      Counter<String> c2 = Counters.newCounter(termExtractor.extract(second).string());
      if(c1.isEmpty() || c2.isEmpty()) {
         return 0d;
      }
      Index<String> index = new HashMapIndex<>();
      index.addAll(c1.items());
      index.addAll(c2.items());
      NDArray n1 = NDArrayFactory.SPARSE.array(index.size());
      c1.forEach((s, v) -> n1.set(index.getId(s), v));
      NDArray n2 = NDArrayFactory.SPARSE.array(index.size());
      c2.forEach((s, v) -> n2.set(index.getId(s), v));
      return measure.calculate(n1, n2);
   }

   @Override
   public void fit(@NonNull DocumentCollection corpus) {

   }
}//END OF TokenSimilarity
