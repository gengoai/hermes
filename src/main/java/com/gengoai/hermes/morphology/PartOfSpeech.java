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

package com.gengoai.hermes.morphology;

import com.gengoai.Tag;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public interface PartOfSpeech extends Tag, Serializable {
   PartOfSpeech ADJECTIVE = UPos.ADJECTIVE;
   PartOfSpeech ADPOSITION = UPos.ADPOSITION;
   PartOfSpeech ADVERB = UPos.ADVERB;
   PartOfSpeech ANY = UPos.ANY;
   PartOfSpeech AUXILIARY = UPos.AUXILIARY;
   PartOfSpeech COORDINATING_CONJUNCTION = UPos.COORDINATING_CONJUNCTION;
   PartOfSpeech DETERMINER = UPos.DETERMINER;
   PartOfSpeech INTERJECTION = UPos.INTERJECTION;
   PartOfSpeech NOUN = UPos.NOUN;
   PartOfSpeech NUMERAL = UPos.NUMERAL;
   PartOfSpeech OTHER = UPos.OTHER;
   PartOfSpeech PARTICLE = UPos.PARTICLE;
   PartOfSpeech PRONOUN = UPos.PRONOUN;
   PartOfSpeech PROPER_NOUN = UPos.PROPER_NOUN;
   PartOfSpeech PUNCTUATION = UPos.PUNCTUATION;
   PartOfSpeech SUBORDINATING_CONJUNCTION = UPos.SUBORDINATING_CONJUNCTION;
   PartOfSpeech SYMBOL = UPos.SYMBOL;
   PartOfSpeech VERB = UPos.VERB;

   static PartOfSpeech valueOf(String name) {
      return POSTagSetManager.valueOf(name);
   }

   String asString();

   Optional<Set<Value>> getFeatureValues(Feature feature);

   /**
    * Gets universal tag.
    *
    * @return The universal tag
    */
   default PartOfSpeech getUniversalTag() {
      if(this == ANY) {
         return ANY;
      }
      PartOfSpeech tag = this;
      while(tag != null && !tag.isUniversalTag()) {
         tag = tag.parent();
      }
      return tag;
   }

   default boolean hasValue(@NonNull Feature feature, Value value) {
      return getFeatureValues(feature)
            .map(s -> s.contains(value))
            .orElse(false);
   }

   @Override
   default boolean isInstance(@NonNull Tag tag) {
      if(tag == ANY) {
         return false;
      }
      return Tag.super.isInstance(tag);
   }

   default boolean isNoun() {
      return isInstance(NOUN);
   }

   boolean isPhraseTag();

   boolean isUniversalTag();

   default boolean isVerb() {
      return isInstance(VERB);
   }

   @Override
   PartOfSpeech parent();

}//END OF PartOfSpeech
