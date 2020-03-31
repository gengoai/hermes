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
import com.gengoai.hermes.morphology.POS;
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
      String word = wordFeature == null ? Strings.EMPTY : wordFeature.getSuffix();
      if (Strings.isNullOrBlank(word)) {
         return true;
      }
      POS pos = POS.fromString(label);
      if (pos == null) {
         return true;
      }

      switch (word.toLowerCase()) {
         case "\"":
         case "``":
         case "''":
         case "\"\"":
         case "`":
            return pos.isInstance(POS.QUOTE);
         case "'":
            return pos.isInstance(POS.QUOTE, POS.POS, POS.COLON);
         case "#":
            return pos.isInstance(POS.HASH);
         case ",":
            return pos.isInstance(POS.COMMA);
         case ":":
         case ";":
         case "...":
         case "--":
         case "::":
         case "-":
            return pos.isInstance(POS.COLON);
         case "$":
            return pos.isInstance(POS.DOLLAR);
         case ".":
         case "!":
         case "?":
            return pos.isInstance(POS.PERIOD, POS.COLON);
         case "{":
            return pos.isInstance(POS.LCB);
         case "}":
            return pos.isInstance(POS.RCB);
         case "[":
            return pos.isInstance(POS.LSB);
         case "]":
            return pos.isInstance(POS.RSB);
         case "(":
            return pos.isInstance(POS.LRB);
         case ")":
            return pos.isInstance(POS.RRB);
         case "&":
            return pos.isInstance(POS.CC, POS.SYM);
         case "i":
            return pos.isInstance(POS.PRP, POS.CD);
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
            return pos.isInstance(POS.PRP);
         case "my":
         case "mine":
         case "your":
         case "yours":
         case "his":
         case "their":
         case "our":
            return pos.isInstance(POS.PRP$);
      }


      boolean hasLetterOrDigit = StringMatcher.HasLetterOrDigit.test(word);
      if (!hasLetterOrDigit && word.endsWith("-")) {
         return pos.isInstance(POS.COLON);
      }

      if (word.contains("$")) {
         return pos.isInstance(POS.SYM, POS.CD, POS.DOLLAR);
      }

      if (word.equals("%")) {
         return pos.isInstance(POS.SYM);
      }

      if (!hasLetterOrDigit) {
         return pos.isInstance(POS.SYM, POS.CD);
      }

      return !pos.isInstance(POS.QUOTE, POS.HASH, POS.COMMA, POS.COLON, POS.DOLLAR, POS.PERIOD, POS.LCB, POS.RCB,
                             POS.LSB,
                             POS.RSB, POS.LRB, POS.RRB);
   }
}//END OF EnglishPOSValidator
