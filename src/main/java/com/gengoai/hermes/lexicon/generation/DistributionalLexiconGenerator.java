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

package com.gengoai.hermes.lexicon.generation;

import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.apollo.linear.NDArray;
import com.gengoai.apollo.linear.NDArrayFactory;
import com.gengoai.apollo.ml.embedding.Embedding;
import com.gengoai.apollo.ml.embedding.VSQuery;
import com.gengoai.apollo.statistics.measure.Similarity;
import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.collection.counter.MultiCounters;
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.collection.multimap.SetMultimap;
import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Distributional lexicon generator.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public class DistributionalLexiconGenerator<T extends Tag> implements LexiconGenerator<T> {
   private final SetMultimap<T, String> negativeSeedTerms = new HashSetMultimap<>();
   private final SetMultimap<T, String> seedTerms = new HashSetMultimap<>();
   private final Embedding wordEmbeddings;
   @Getter
   @Setter
   private int maximumTermCount = 100;
   @Getter
   @Setter
   private double threshold = 0.4;

   /**
    * Instantiates a new Distributional lexicon generator.
    *
    * @param wordEmbeddings the word embeddings
    */
   public DistributionalLexiconGenerator(@NonNull Embedding wordEmbeddings) {
      this.wordEmbeddings = wordEmbeddings;
   }

   /**
    * Instantiates a new Distributional lexicon generator.
    *
    * @param wordEmbeddings the word embeddings
    * @param seedTerms      the seed terms
    */
   public DistributionalLexiconGenerator(@NonNull Embedding wordEmbeddings, @NonNull Multimap<T, String> seedTerms) {
      this.wordEmbeddings = wordEmbeddings;
      this.seedTerms.putAll(seedTerms);
   }

   /**
    * Instantiates a new Distributional lexicon generator.
    *
    * @param wordEmbeddings the word embeddings
    * @param seedTerms      the seed terms
    * @param threshold      the threshold
    */
   public DistributionalLexiconGenerator(@NonNull Embedding wordEmbeddings, @NonNull Multimap<T, String> seedTerms, double threshold) {
      this.wordEmbeddings = wordEmbeddings;
      this.seedTerms.putAll(seedTerms);
      this.threshold = threshold;
   }

   /**
    * Add negative seed boolean.
    *
    * @param tag    the tag
    * @param phrase the phrase
    * @return the boolean
    */
   public boolean addNegativeSeed(@NonNull T tag, String phrase) {
      Validation.checkArgument(Strings.isNotNullOrBlank(phrase), "Phrase must not be null or blank");
      negativeSeedTerms.put(tag, phrase);
      return true;
   }

   /**
    * Add seed boolean.
    *
    * @param tag    the tag
    * @param phrase the phrase
    * @return the boolean
    */
   public boolean addSeed(@NonNull T tag, String phrase) {
      Validation.checkArgument(Strings.isNotNullOrBlank(phrase), "Phrase must not be null or blank");
      if (seedTerms.containsValue(phrase)) {
         return false;
      }
      seedTerms.put(tag, phrase);
      return true;
   }

   @Override
   public Multimap<T, String> generate() {
      SetMultimap<T, String> lexicon = new HashSetMultimap<>();
      if (seedTerms.size() > 0) {
         Map<T, NDArray> vectors = new HashMap<>();
         Map<T, NDArray> negVectors = new HashMap<>();
         seedTerms.keySet().forEach(tag -> {
            NDArray v = NDArrayFactory.DENSE.array(wordEmbeddings.dimension());
            seedTerms.get(tag).stream()
                     .filter(wordEmbeddings::contains)
                     .forEach(s -> v.addi(wordEmbeddings.lookup(s)));
            v.divi(seedTerms.size());
            vectors.put(tag, v);

            NDArray negV = NDArrayFactory.DENSE.array(wordEmbeddings.dimension());
            negativeSeedTerms.get(tag)
                             .stream()
                             .filter(wordEmbeddings::contains)
                             .forEach(s -> negV.addi(wordEmbeddings.lookup(s)));
            negVectors.put(tag, negV);
         });
         lexicon.putAll(seedTerms);
         MultiCounter<String, T> scores = MultiCounters.newMultiCounter();
         vectors.forEach((tag, vector) -> wordEmbeddings.query(VSQuery.vectorQuery(vector)
                                                                      .limit(maximumTermCount * 10))
                                                        .filter(slv -> !seedTerms.containsValue(slv.getLabel()))
                                                        .forEach(slv -> {
                                                           double neg = 0;
                                                           if (negVectors.get(tag).norm2() > 0) {
                                                              neg = Similarity.Cosine.calculate(negVectors.get(tag),
                                                                                                slv);
                                                           }
                                                           scores.set(slv.getLabel(), tag, slv.getWeight() - neg);
                                                        }));
         MultiCounter<T, String> selection = MultiCounters.newMultiCounter();
         scores.firstKeys().forEach(k -> {
            if (!seedTerms.containsValue(k)) {
               T best = scores.get(k).filterByValue(d -> d >= threshold).max();
               selection.set(best, k, scores.get(k, best));
            }
         });
         selection.firstKeys().forEach(k -> lexicon.putAll(k, selection.get(k).topN(maximumTermCount).items()));
      }
      return lexicon;
   }

}//END OF DistributionalLexiconGenerator
