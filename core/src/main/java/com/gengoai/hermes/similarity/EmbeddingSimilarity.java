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

import com.gengoai.Validation;
import com.gengoai.apollo.math.statistics.measure.Similarity;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.DocumentCollection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

/**
 * <p>Implementation of a {@link HStringSimilarity} that calculates similarity based on the similarity between the
 * HStrings in embedding space. This expects that embeddings have been assigned to the HString or tokens.</p>
 */
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class EmbeddingSimilarity implements HStringSimilarity, Serializable {
   private static final long serialVersionUID = 1L;
   @NonNull Similarity measure;

   /**
    * Instantiates a new EmbeddingSimilarity.
    *
    * @param measure the similarity measure to use
    */
   public EmbeddingSimilarity(@NonNull Similarity measure) {
      this.measure = measure;
   }

   @Override
   public double calculate(@NonNull HString first, @NonNull HString second) {
      Validation.notNull(first.embedding());
      Validation.notNull(second.embedding());
      return measure.calculate(first.embedding(), second.embedding());
   }

   @Override
   public void fit(@NonNull DocumentCollection corpus) {

   }

}//END OF EmbeddingSimilarity
