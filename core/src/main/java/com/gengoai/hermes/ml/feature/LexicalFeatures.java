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

package com.gengoai.hermes.ml.feature;

import com.gengoai.hermes.*;
import com.gengoai.hermes.morphology.PennTreeBank;
import com.gengoai.math.Math2;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author David B. Bracewell
 */
public final class LexicalFeatures {
   private static final Pattern capPeriod = Pattern.compile("^[A-Z]\\.$");
   private static final Pattern ordinalPattern = Pattern.compile("\\d+(nd|rd|st|th)");
   private static final Set<String> cardinalNames = Set.of("one",
                                                           "two",
                                                           "three",
                                                           "four",
                                                           "five",
                                                           "six",
                                                           "seven",
                                                           "eight",
                                                           "nine",
                                                           "ten",
                                                           "eleven",
                                                           "twelve",
                                                           "thirteen",
                                                           "fourteen",
                                                           "fifteen",
                                                           "sixteen",
                                                           "seventeen",
                                                           "eighteen",
                                                           "nineteen",
                                                           "twenty",
                                                           "thirty",
                                                           "forty",
                                                           "fifty",
                                                           "sixty",
                                                           "seventy",
                                                           "eighty",
                                                           "ninety",
                                                           "hundred",
                                                           "thousand",
                                                           "million",
                                                           "billion",
                                                           "trillion");
   private static final Set<String> ordinalNames = Set.of("first",
                                                          "second",
                                                          "third",
                                                          "fourth",
                                                          "fifth",
                                                          "sixth",
                                                          "seventh",
                                                          "eighth",
                                                          "ninth",
                                                          "tenth",
                                                          "eleventh",
                                                          "twelfth",
                                                          "thirteenth",
                                                          "fourteenth",
                                                          "fifteenth",
                                                          "sixteenth",
                                                          "seventeenth",
                                                          "eighteenth",
                                                          "nineteenth",
                                                          "twentieth",
                                                          "thirtieth",
                                                          "fortieth",
                                                          "fiftieth",
                                                          "sixtieth",
                                                          "seventieth",
                                                          "eightieth",
                                                          "ninetieth",
                                                          "hundredth",
                                                          "thousandth",
                                                          "millionth",
                                                          "billionth");

   private static final Set<String> percentNames = Set.of("%", "pct", "pct.", "percent");

   public static boolean isCardinalNumber(@NonNull HString word) {
      String norm = word.toLowerCase();
      if(Strings.isDigit(word) ||
            cardinalNames.contains(norm) ||
            word.pos().isInstance(PennTreeBank.CD) ||
            Strings.isDigit(word.replaceAll("\\W+", "")) ||
            Math2.tryParseDouble(norm) != null) {
         return true;
      }
      if(norm.contains("-") && norm.length() > 1) {
         return Stream.of(norm.split("-")).allMatch(s -> isCardinalNumber(Fragments.stringWrapper(s)));
      }
      if(norm.contains("/") && norm.length() > 1) {
         return Stream.of(norm.split("/")).allMatch(s -> isCardinalNumber(Fragments.stringWrapper(s)));
      }
      return false;
   }

   public static boolean isDigit(HString word) {
      return isCardinalNumber(word) || isOrdinalNumber(word) || word.pos().isNumeral();
   }

   public static boolean isOrdinalNumber(@NonNull HString word) {
      final String lower = word.toLowerCase();
      if(ordinalNames.contains(lower) || ordinalPattern.matcher(lower).matches()) {
         return true;
      }
      if(lower.contains("-") && lower.length() > 1) {
         String[] parts = lower.split("-");
         if(parts.length > 1) {
            for(int i = 0; i < parts.length - 1; i++) {
               if(!isDigit(Fragments.stringWrapper(parts[i]))) {
                  return false;
               }
            }
            return isOrdinalNumber(Fragments.stringWrapper(parts[parts.length - 1]));
         }
      }
      return false;
   }

   public static boolean isPercent(@NonNull HString word) {
      if(percentNames.contains(word.toLowerCase())) {
         return true;
      } else if(isCardinalNumber(word)) {
         Annotation next = word.next(Types.TOKEN);
         return next.sentence() == word.sentence() && isPercent(next);
      }
      return false;
   }

   public static void main(String[] args) {
      Document d = Document.create("33% of the Lts. in the Navy who contract the disease die within 23 days.");
      d.annotate(Types.TOKEN, Types.SENTENCE);
      for(Annotation token : d.tokens()) {
         System.out.println(token + " : " + isDigit(token) + " : " + isPercent(token));
         System.out.println(Features.IsEndOfSentence.applyAsFeatures(token));
         System.out.println();
      }
   }

   public static String shape(@NonNull HString string) {
      if(Strings.isNullOrBlank(string)) {
         return Strings.EMPTY;
      }
      StringBuilder builder = new StringBuilder();
      String last = Strings.EMPTY;
      String cur = Strings.EMPTY;
      int repeated = 0;
      for(int ci = 0; ci < string.length(); ci++) {
         char c = string.charAt(ci);
         if(Character.isUpperCase(c)) {
            cur = "X";
         } else if(Character.isLowerCase(c)) {
            cur = "x";
         } else if(Character.isDigit(c)) {
            cur = "d";
         } else if(c == '.' || c == ',') {
            cur = ".";
         } else if(c == ';' || c == ':' || c == '?' || c == '!') {
            cur = ";";
         } else if(c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == '|' || c == '_') {
            cur = "-";
         } else if(c == '(' || c == '{' || c == '[' || c == '<') {
            cur = "(";
         } else if(c == ')' || c == '}' || c == ']' || c == '>') {
            cur = ")";
         } else {
            cur = Character.toString(c);
         }
         if(last.equals(cur)) {
            repeated++;
         } else {
            repeated = 0;
         }
         if(repeated < 4) {
            builder.append(cur);
         }
      }
      return builder.toString();
   }

   public static String wordClass(@NonNull HString string) {
      if(Character.getType(string.charAt(0)) == Character.CURRENCY_SYMBOL) {
         return "CURRENCY";
      }

      if(string.contentEqualsIgnoreCase("'s")) {
         return "POSSESSIVE";
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

      return feat;
   }

   private LexicalFeatures() {
      throw new IllegalAccessError();
   }

}//END OF LexicalFeatures
