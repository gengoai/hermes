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

import com.gengoai.annotation.Preload;

import java.util.Optional;
import java.util.Set;

@Preload
public enum UPos implements PartOfSpeech {
   ANY("UNKNOWN"),
   ADJECTIVE("ADJ"),
   ADPOSITION("ADP"),
   ADVERB("ADV"),
   AUXILIARY("AUX"),
   COORDINATING_CONJUNCTION("CCONJ"),
   DETERMINER("DET"),
   INTERJECTION("INT"),
   NOUN("NOUN"),
   NUMERAL("NUM"),
   PARTICLE("PART"),
   PROPER_NOUN("PROPN"),
   PRONOUN("PRON"),
   PUNCTUATION("PUNCT"),
   SUBORDINATING_CONJUNCTION("SCONJ"),
   SYMBOL("SYM"),
   VERB("VERB"),
   OTHER("X");

   static {
      POSTagSetManager.registerTagSet(values());
   }

   private final String tag;

   UPos(String tag) {
      this.tag = tag;
   }

   @Override
   public String asString() {
      return tag;
   }

   @Override
   public Optional<Set<Value>> getFeatureValues(Feature feature) {
      return Optional.empty();
   }

   @Override
   public boolean isPhraseTag() {
      return false;
   }

   @Override
   public boolean isUniversalTag() {
      return true;
   }

   @Override
   public PartOfSpeech parent() {
      return this == ANY
             ? null
             : ANY;
   }
}//END OF UPos
