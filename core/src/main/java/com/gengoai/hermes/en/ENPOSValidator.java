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

package com.gengoai.hermes.en;

import com.gengoai.apollo.ml.Example;
import com.gengoai.apollo.ml.Feature;
import com.gengoai.apollo.ml.sequence.SequenceValidator;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.PennTreeBank;
import com.gengoai.string.StringMatcher;
import com.gengoai.string.Strings;

/**
 * The type English pos validator.
 *
 * @author David B. Bracewell
 */
public class ENPOSValidator implements SequenceValidator {
   private static final long serialVersionUID = 1L;

   @Override
   public boolean isValid(String label, String previousLabel, Example instance) {
      Feature wordFeature = instance.getFeatureByPrefix("WORD", null);
      String word = wordFeature == null
                    ? Strings.EMPTY
                    : wordFeature.getSuffix();
      if(Strings.isNullOrBlank(word)) {
         return true;
      }
      PartOfSpeech pos = PartOfSpeech.valueOf(label);
      if(pos == null) {
         return true;
      }

      switch(word.toLowerCase()) {
         case "\"":
         case "``":
         case "''":
         case "\"\"":
         case "`":
            return pos.isInstance(PennTreeBank.QUOTE);
         case "'":
            return pos.isInstance(PennTreeBank.QUOTE, PennTreeBank.POS, PennTreeBank.COLON);
         case "#":
            return pos.isInstance(PennTreeBank.HASH);
         case ",":
            return pos.isInstance(PennTreeBank.COMMA);
         case ":":
         case ";":
         case "...":
         case "--":
         case "::":
         case "-":
            return pos.isInstance(PennTreeBank.COLON);
         case "$":
            return pos.isInstance(PennTreeBank.DOLLAR);
         case ".":
         case "!":
         case "?":
            return pos.isInstance(PennTreeBank.PERIOD, PennTreeBank.COLON);
         case "{":
            return pos.isInstance(PennTreeBank.LCB);
         case "}":
            return pos.isInstance(PennTreeBank.RCB);
         case "[":
            return pos.isInstance(PennTreeBank.LSB);
         case "]":
            return pos.isInstance(PennTreeBank.RSB);
         case "(":
            return pos.isInstance(PennTreeBank.LRB);
         case ")":
            return pos.isInstance(PennTreeBank.RRB);
         case "&":
            return pos.isInstance(PennTreeBank.CC, PennTreeBank.SYM);
         case "i":
            return pos.isInstance(PennTreeBank.PRP, PennTreeBank.CD);
         case "me":
         case "myself":
         case "you":
         case "yourself":
         case "he":
         case "him":
         case "himself":
         case "she":
         case "we":
         case "us":
         case "they":
            return pos.isInstance(PennTreeBank.PRP);
         case "my":
         case "mine":
         case "your":
         case "yours":
         case "his":
         case "their":
         case "our":
            return pos.isInstance(PennTreeBank.PRP$);
      }

      boolean hasLetterOrDigit = StringMatcher.HasLetterOrDigit.test(word);
      if(!hasLetterOrDigit && word.endsWith("-")) {
         return pos.isInstance(PennTreeBank.COLON);
      }

      if(word.contains("$")) {
         return pos.isInstance(PennTreeBank.SYM, PennTreeBank.CD, PennTreeBank.DOLLAR);
      }

      if(word.equals("%")) {
         return pos.isInstance(PennTreeBank.SYM);
      }

      if(!hasLetterOrDigit) {
         return pos.isInstance(PennTreeBank.SYM, PennTreeBank.CD);
      }

      return !pos.isInstance(PennTreeBank.QUOTE,
                             PennTreeBank.HASH,
                             PennTreeBank.COMMA,
                             PennTreeBank.COLON,
                             PennTreeBank.DOLLAR,
                             PennTreeBank.PERIOD,
                             PennTreeBank.LCB,
                             PennTreeBank.RCB,
                             PennTreeBank.LSB,
                             PennTreeBank.RSB,
                             PennTreeBank.LRB,
                             PennTreeBank.RRB);
   }
}//END OF EnglishPOSValidator
