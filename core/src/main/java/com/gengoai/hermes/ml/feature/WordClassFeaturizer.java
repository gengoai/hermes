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

package com.gengoai.hermes.ml.feature;

import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.hermes.HString;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The type Word class featurizer.
 *
 * @author David B. Bracewell
 */
public class WordClassFeaturizer extends Featurizer<HString> {
   private static final long serialVersionUID = 1L;
   /**
    * The constant INSTANCE.
    */
   public static final WordClassFeaturizer INSTANCE = new WordClassFeaturizer();
   /**
    * The constant featurePrefix.
    */
   public static String featurePrefix = "WORD_CLASS";
   private static Pattern capPeriod = Pattern.compile("^[A-Z]\\.$");

   @Override
   public List<Variable> applyAsFeatures(HString string) {
      if(Character.getType(string.charAt(0)) == Character.CURRENCY_SYMBOL) {
         return Collections.singletonList(Variable.binary(featurePrefix, "CURRENCY"));
      }

      if(string.contentEqualsIgnoreCase("'s")) {
         return Collections.singletonList(Variable.binary(featurePrefix, "POSSESSIVE"));
      }

      StringPattern pattern = StringPattern.recognize(string.toString());

      String feat;
      if(pattern.isAllLowerCaseLetter()) {
         feat = "lc";
      } else if(pattern.digits() == 2) {
         feat = "2d";
      } else if(pattern.digits() == 4) {
         feat = "4d";
      } else if(pattern.containsDigit()) {
         if(pattern.containsLetters()) {
            feat = "an";
         } else if(pattern.containsHyphen()) {
            feat = "dd";
         } else if(pattern.containsSlash()) {
            feat = "ds";
         } else if(pattern.containsComma()) {
            feat = "dc";
         } else if(pattern.containsPeriod()) {
            feat = "dp";
         } else {
            feat = "num";
         }
      } else if(pattern.isAllCapitalLetter() && string.length() == 1) {
         feat = "sc";
      } else if(pattern.isAllCapitalLetter()) {
         feat = "ac";
      } else if(capPeriod.matcher(string).find()) {
         feat = "cp";
      } else if(pattern.isInitialCapitalLetter()) {
         feat = "ic";
      } else {
         feat = "other";
      }

      return Collections.singletonList(Variable.binary(featurePrefix, feat));
   }

}//END OF WordClassFeaturizer
