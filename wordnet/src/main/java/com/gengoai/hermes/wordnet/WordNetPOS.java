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

package com.gengoai.hermes.wordnet;

import com.gengoai.Validation;
import com.gengoai.hermes.morphology.PartOfSpeech;

/**
 * The enum Word net pOS.
 *
 * @author David B. Bracewell
 */
public enum WordNetPOS {
   NOUN('n', "noun", PartOfSpeech.NOUN),
   VERB('v', "verb", PartOfSpeech.VERB),
   ADJECTIVE('a', "adj", PartOfSpeech.ADJECTIVE),
   ADVERB('r', "adv", PartOfSpeech.ADVERB),
   ANY('*', "any", PartOfSpeech.ANY);

   private final char tag;
   private final String shortForm;
   private final PartOfSpeech hermesPOS;

   public static WordNetPOS fromHermesPOS(PartOfSpeech pos) {
      if(pos == null) {
         return ANY;
      }
      if(pos.isNoun()) {
         return NOUN;
      }
      if(pos.isVerb()) {
         return VERB;
      }
      if(pos.isAdjective()) {
         return ADJECTIVE;
      }
      if(pos.isAdverb()) {
         return ADVERB;
      }
      return ANY;
   }

   /**
    * From string.
    *
    * @param string the string
    * @return the word net pOS
    */
   public static WordNetPOS fromString(String string) {
      Validation.notNull(string);
      if(string.equalsIgnoreCase("S")) {
         return ADJECTIVE;
      }
      for(WordNetPOS pos : values()) {
         if(pos.shortForm.equalsIgnoreCase(string) || pos.tag == Character.toLowerCase(string.charAt(0))) {
            return pos;
         }
      }
      return WordNetPOS.valueOf(string);
   }

   WordNetPOS(char tag, String shortForm, PartOfSpeech hermesPOS) {
      this.tag = tag;
      this.shortForm = shortForm;
      this.hermesPOS = hermesPOS;
   }

   /**
    * Get short form.
    *
    * @return the string
    */
   public String getShortForm() {
      return shortForm;
   }

   /**
    * Gets tag.
    *
    * @return the tag
    */
   public char getTag() {
      return tag;
   }

   public PartOfSpeech toHermesPOS() {
      return hermesPOS;
   }

}//END OF WordNetPOS
