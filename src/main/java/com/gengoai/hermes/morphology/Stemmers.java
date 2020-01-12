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

import com.gengoai.Language;
import com.gengoai.config.Config;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Hermes;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import lombok.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * <p>Factory class for creating/retrieving stemmers for a given language</p>
 *
 * @author David B. Bracewell
 */
public final class Stemmers {
   private static volatile ConcurrentMap<Language, Stemmer> stemmerMap = new ConcurrentHashMap<>();

   /**
    * Gets the stemmer for the given language as defined in the config option
    * <code>iknowledge.latte.morphology.Stemmer.LANGUAGE</code>. if no stemmer is specified a no-op stemmer is
    * returned.
    *
    * @param language The language
    * @return The stemmer for the language
    */
   public static Stemmer getStemmer(@NonNull Language language) {
      if (!stemmerMap.containsKey(language)) {
         if (Config.hasProperty("hermes.Stemmer", language)) {
            Stemmer stemmer = Config.get("hermes.Stemmer", language).as(Stemmer.class);
            stemmerMap.putIfAbsent(language, stemmer);
         } else {
            Class<?> clazz = Hermes.defaultImplementation(language, "Stemmer");
            if (clazz != null) {
               try {
                  stemmerMap.put(language, Reflect.onClass(clazz).create().get());
               } catch (ReflectionException e) {
                  throw new RuntimeException(e);
               }
            } else {
               stemmerMap.put(language, NoOptStemmer.INSTANCE);
            }
         }
      }
      return stemmerMap.get(language);
   }

   /**
    * A stemmer implementation that returns the input
    */
   private enum NoOptStemmer implements Stemmer {
      /**
       * Instance no opt stemmer.
       */
      INSTANCE;

      @Override
      public String stem(String string) {
         return string;
      }

      @Override
      public String stem(HString text) {
         return text.toString();
      }
   }//END OF Stemmer$NoOptStemmer

}//END OF Stemmers
