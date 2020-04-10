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
 * <p>Defines a methodology for determining if an HString or String is a stopword for a given language.</p>
 *
 * @author David B. Bracewell
 */
public abstract class StopWords implements Serializable {
   private static final long serialVersionUID = 1L;

   private static volatile Map<Language, StopWords> stopWordLists = new ConcurrentHashMap<>();

   /**
    * Gets the Stopwords instance for the given language.
    *
    * @param language the language
    * @return the Stopwords instance
    */
   public static StopWords getStopWords(Language language) {
      if(!stopWordLists.containsKey(language)) {
         synchronized(StopWords.class) {
            if(Config.hasProperty("hermes.StopWords", language, "class")) {
               stopWordLists.put(language, Config.get("hermes.StopWords", language, "class")
                                                 .as(StopWords.class, new NoOptStopWords()));
            } else {
               Class<?> clazz = Hermes.defaultImplementation(language, "StopWords");
               if(clazz != null) {
                  try {
                     stopWordLists.put(language, Reflect.onClass(clazz).create().get());
                  } catch(ReflectionException e) {
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
    * @return predicate returning true when all tokens in the given HString are content words (i.e. not a stopword)
    */
   public static SerializablePredicate<HString> hasOnlyContentWords() {
      return hasStopWord().negate();
   }

   /**
    * @return predicate returning true when any token in a given HString is a stopword
    */
   public static SerializablePredicate<HString> hasStopWord() {
      return hString -> {
         if(hString == null) {
            return true;
         }
         return hString.tokens().stream().anyMatch(isStopWord());
      };
   }

   /**
    * @return predicate returning true when the given HString is a content word (i.e. not a stopword)
    */
   public static SerializablePredicate<HString> isContentWord() {
      return isStopWord().negate();
   }

   /**
    * @return @return predicate returning true when the given HString is a stopword.
    */
   public static SerializablePredicate<HString> isStopWord() {
      return hString -> {
         if(hString == null) {
            return true;
         }
         return StopWords.getStopWords(hString.getLanguage()).isStopWord(hString);
      };
   }

   /**
    * Returns true when any token in a given HString is a stopword
    *
    * @param text the text
    * @return true when any token in a given HString is a stopword
    */
   public boolean hasStopWord(HString text) {
      if(text == null) {
         return true;
      } else if(text.isInstance(Types.TOKEN)) {
         return isTokenStopWord(Cast.as(text));
      }
      return text.tokens().stream().anyMatch(this::isTokenStopWord);
   }

   /**
    * Checks if the given text is a stopword
    *
    * @param text the text
    * @return True if a stopword, False if a content word.
    */
   public boolean isStopWord(HString text) {
      if(text == null) {
         return true;
      } else if(text.isInstance(Types.TOKEN)) {
         return isTokenStopWord(Cast.as(text));
      } else if(text.tokenLength() > 0) {
         return text.tokens().stream().allMatch(this::isTokenStopWord);
      } else {
         return isStopWord(text.toString());
      }
   }

   /**
    * Checks if the given word is a stopword
    *
    * @param word the word
    * @return True if a stopword, False if a content word.
    */
   public abstract boolean isStopWord(String word);

   /**
    * Checks if the given token is a stopword
    *
    * @param token the token
    * @return True if a stopword, False if a content word.
    */
   protected abstract boolean isTokenStopWord(Annotation token);

   /**
    * StopWords implementation that treats everything as a content word.
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
