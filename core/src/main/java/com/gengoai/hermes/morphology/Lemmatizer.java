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

package com.gengoai.hermes.morphology;

import cc.mallet.util.Strings;
import com.gengoai.collection.tree.Trie;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Defines the interface for lemmatizing tokens.</p>
 *
 * @author David B. Bracewell
 */
public interface Lemmatizer {

   /**
    * Determines the best lemma for a string
    *
    * @param string the string to lemmatize
    * @return the lemmatized version of the string
    */
   default String lemmatize(@NonNull String string) {
      return allPossibleLemmas(string, PartOfSpeech.ANY).stream().findFirst().orElse(string.toLowerCase());
   }

   /**
    * Determines the best lemma for a string given a part of speech
    *
    * @param string       the string
    * @param partOfSpeech the part of speech
    * @return the lemmatized version of the string
    */
   default String lemmatize(@NonNull String string, @NonNull PartOfSpeech partOfSpeech) {
      String minStr = null;
      double minD = Double.MAX_VALUE;
      for(String lemma : allPossibleLemmas(string, partOfSpeech)) {
         double dist = Strings.levenshteinDistance(string, lemma);
         if(dist <= minD) {
            minD = dist;
            minStr = lemma;
         }
      }
      return minStr == null
             ? string.toLowerCase()
             : minStr;
   }

   /**
    * Gets all lemmas.
    *
    * @param string       the string
    * @param partOfSpeech the part of speech
    * @return the all lemmas
    */
   List<String> allPossibleLemmas(String string, PartOfSpeech partOfSpeech);

   /**
    * Gets prefixed lemmas.
    *
    * @param string       the string
    * @param partOfSpeech the part of speech
    * @return the prefixed lemmas
    */
   Trie<String> allPossibleLemmasAndPrefixes(String string, PartOfSpeech partOfSpeech);

   /**
    * Can lemmatize boolean.
    *
    * @param input        the input
    * @param partOfSpeech the part of speech
    * @return the boolean
    */
   boolean canLemmatize(String input, PartOfSpeech partOfSpeech);

   /**
    * Lemmatizes a token.
    *
    * @param fragment the fragment to lemmatize
    * @return the lemmatized version of the token
    */
   default String lemmatize(@NonNull HString fragment) {
      if(fragment.isInstance(Types.TOKEN)) {
         PartOfSpeech pos = fragment.pos();
         if(pos == null) {
            pos = PartOfSpeech.ANY;
         }
         return lemmatize(fragment.toString(), pos);
      }
      return fragment.tokens().stream()
                     .map(this::lemmatize)
                     .collect(Collectors.joining(fragment.getLanguage().usesWhitespace()
                                                 ? " "
                                                 : ""));
   }


}//END OF Lemmatizer
