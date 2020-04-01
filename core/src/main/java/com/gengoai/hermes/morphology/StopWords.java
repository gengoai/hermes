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

import com.gengoai.Language;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.Types;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Stop words.
 *
 * @author David B. Bracewell
 */
public abstract class StopWords implements Serializable {
   private static final long serialVersionUID = 1L;

   private static volatile Map<Language, StopWords> stopWordLists = new ConcurrentHashMap<>();

   /**
    * Gets instance.
    *
    * @param language the language
    * @return the instance
    */
   public static StopWords getStopWords(Language language) {
      if (!stopWordLists.containsKey(language)) {
         synchronized (StopWords.class) {
            if (Config.hasProperty("hermes.StopWords", language, "class")) {
               stopWordLists.put(language, Config.get("hermes.StopWords", language, "class")
                                                 .as(StopWords.class, new NoOptStopWords()));
            } else {
               Class<?> clazz = Hermes.defaultImplementation(language, "StopWords");
               if (clazz != null) {
                  try {
                     stopWordLists.put(language, Reflect.onClass(clazz).create().get());
                  } catch (ReflectionException e) {
                     throw new RuntimeException(e);
                  }
               } else {
                  stopWordLists.put(language, new NoOptStopWords());
               }
            }
         }
      }
      return stopWordLists.get(language);
   }

   /**
    * Has stop word serializable predicate.
    *
    * @return the serializable predicate
    */
   public static SerializablePredicate<HString> hasStopWord() {
      return hString -> {
         if (hString == null) {
            return true;
         }
         return hString.tokens().stream().anyMatch(isStopWord());
      };
   }

   /**
    * Is not stop word serializable predicate.
    *
    * @return the serializable predicate
    */
   public static SerializablePredicate<HString> isNotStopWord() {
      return isStopWord().negate();
   }

   /**
    * Is stop word serializable predicate.
    *
    * @return the serializable predicate
    */
   public static SerializablePredicate<HString> isStopWord() {
      return hString -> {
         if (hString == null) {
            return true;
         }
         return StopWords.getStopWords(hString.getLanguage()).isStopWord(hString);
      };
   }

   /**
    * Not has stop word serializable predicate.
    *
    * @return the serializable predicate
    */
   public static SerializablePredicate<HString> notHasStopWord() {
      return hasStopWord().negate();
   }

   /**
    * Returns true if any token in the supplied text is a stop word.
    *
    * @param text the text
    * @return boolean
    */
   public boolean hasStopWord(HString text) {
      if (text == null) {
         return true;
      } else if (text.isInstance(Types.TOKEN)) {
         return isTokenStopWord(Cast.as(text));
      }
      return text.tokens().stream().anyMatch(this::isTokenStopWord);
   }

   /**
    * Is stop word.
    *
    * @param text the text
    * @return the boolean
    */
   public boolean isStopWord(HString text) {
      if (text == null) {
         return true;
      } else if (text.isInstance(Types.TOKEN)) {
         return isTokenStopWord(Cast.as(text));
      } else if (text.tokenLength() > 0) {
         return text.tokens().stream().allMatch(this::isTokenStopWord);
      } else {
         return isStopWord(text.toString());
      }
   }

   /**
    * Is stop word.
    *
    * @param word the word
    * @return the boolean
    */
   public abstract boolean isStopWord(String word);

   /**
    * Is token stop word boolean.
    *
    * @param token the token
    * @return the boolean
    */
   protected abstract boolean isTokenStopWord(Annotation token);

   /**
    * The type No opt stop words.
    */
   public static class NoOptStopWords extends StopWords {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isStopWord(String word) {
         return false;
      }

      @Override
      protected boolean isTokenStopWord(Annotation token) {
         return false;
      }


   }//END OF StopWords$EmptyStopWords


}//END OF StopWords
