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

package com.gengoai.hermes.workflow.actions;

import com.gengoai.apollo.ml.embedding.Embedding;
import com.gengoai.apollo.statistics.measure.Similarity;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.config.Config;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.extraction.TermExtractor;
import com.gengoai.hermes.lexicon.SimpleWordList;
import com.gengoai.hermes.lexicon.TrieWordList;
import com.gengoai.hermes.lexicon.WordList;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import static com.gengoai.hermes.extraction.lyre.LyreDSL.*;
import static com.gengoai.tuple.Tuples.$;

/**
 * The type Spellchecker module.
 *
 * @author David B. Bracewell
 */
public class SpellChecker implements Action, Serializable {
   private static final long serialVersionUID = 1L;
   private final TrieWordList dictionary;
   private final int maxCost;
   private final Embedding spellingEmbedding;

   /**
    * Instantiates a new Spellchecker module.
    *
    * @param spellingEmbedding the spelling embedding
    */
   public SpellChecker(@NonNull Embedding spellingEmbedding) {
      this(spellingEmbedding,
           Config
              .get("SpellcheckerModule.dictionary")
              .asResource(Resources.fromString()),
           2
          );
   }

   /**
    * Instantiates a new Spellchecker module.
    *
    * @param spellingEmbedding the spelling embedding
    * @param dictionary        the dictionary
    * @param maxCost           the max cost
    */
   public SpellChecker(@NonNull Embedding spellingEmbedding, @NonNull Resource dictionary, int maxCost) {
      this.spellingEmbedding = spellingEmbedding;
      try {
         this.dictionary = TrieWordList.read(dictionary, true);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      this.maxCost = maxCost;
   }

   @Override
   public Corpus process(@NonNull Corpus corpus, @NonNull Context context) throws Exception {
      final WordList wordList = new SimpleWordList(spellingEmbedding.getAlphabet());
      final Counter<String> unigrams = corpus.documentCount(TermExtractor
                                                               .builder()
                                                               .annotations(Types.TOKEN)
                                                               .filter(and(and(gte(len($_), 3),
                                                                               in($_, wordList(wordList))),
                                                                           or(isLetter, isWhitespace)))
                                                               .toLemma().build()
                                                           );

      final Map<String, String> spelling = corpus
         .getStreamingContext()
         .stream(unigrams.items())
         .filter(w -> !dictionary.contains(w))
         .mapToPair(oov -> {
            Map<String, Integer> suggestions = dictionary.suggest(oov,
                                                                  maxCost);
            final Counter<String> adjusted = Counters.newCounter();
            int min = suggestions
               .values()
               .stream()
               .mapToInt(i -> i)
               .min()
               .orElse(2);
            suggestions
               .entrySet()
               .stream()
               .filter(e -> e.getValue() <= min)
               .filter(e -> spellingEmbedding.contains(e.getKey()))
               .filter(e -> unigrams.get(e.getKey()) >= 10)
               .forEach(e -> {
                  double sim = Similarity.Cosine.calculate(
                     spellingEmbedding.lookup(e.getKey()),
                     spellingEmbedding.lookup(oov));
                  if (sim > 0) {
                     adjusted.increment(e.getKey(), sim);
                  }
               });
            return $(oov, adjusted.max());
         })
         .collectAsMap();


      return corpus.update(document -> document
         .tokenStream()
         .forEach(token -> {
            if (spelling.containsKey(token.getLemma())) {
               token.put(Types.SPELLING_CORRECTION, spelling.get(token.getLemma()));
            }
         }));
   }

}// END OF SpellcheckerModule
