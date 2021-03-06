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

package com.gengoai.hermes.lexicon;

import com.gengoai.Language;
import com.gengoai.Validation;
import com.gengoai.cache.Cache;
import com.gengoai.cache.LRUCache;
import com.gengoai.function.Unchecked;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.ResourceType;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.gengoai.LogUtils.logWarning;

/**
 * Manages the creation and access to Lexicons
 *
 * @author David B. Bracewell
 */
@Log
public final class LexiconManager implements Serializable {
   private static final Lexicon EmptyLexicon = new Lexicon() {

      @Override
      public void add(LexiconEntry lexiconEntry) {

      }

      @Override
      public boolean contains(String string) {
         return false;
      }

      @Override
      public Set<LexiconEntry> entries() {
         return Collections.emptySet();
      }

      @Override
      public Set<LexiconEntry> get(String word) {
         return Collections.emptySet();
      }

      @Override
      public int getMaxLemmaLength() {
         return 0;
      }

      @Override
      public int getMaxTokenLength() {
         return 0;
      }

      @Override
      public String getName() {
         return "EMPTY";
      }

      @Override
      public boolean isCaseSensitive() {
         return false;
      }

      @Override
      public boolean isPrefixMatch(HString hString) {
         return false;
      }

      @Override
      public boolean isPrefixMatch(String hString) {
         return false;
      }

      @Override
      public boolean isProbabilistic() {
         return false;
      }

      @Override
      public Iterator<String> iterator() {
         return Collections.emptyIterator();
      }

      @Override
      public List<LexiconEntry> match(HString string) {
         return Collections.emptyList();
      }

      @Override
      public List<LexiconEntry> match(String hString) {
         return Collections.emptyList();
      }

      @Override
      public Set<String> prefixes(String string) {
         return Collections.emptySet();
      }

      @Override
      public int size() {
         return 0;
      }
   };
   private static final Cache<String, Lexicon> lexiconCache = new LRUCache<>(500);
   private static final long serialVersionUID = 1L;

   private LexiconManager() {
      throw new IllegalAccessError();
   }

   /**
    * Clears all loaded lexicons.
    */
   public static void clear() {
      lexiconCache.invalidateAll();
   }

   /**
    * Gets the lexicon with the given name for the given Language
    *
    * @param name     the name of the lexicon
    * @param language the language of the lexicon
    * @return the lexicon
    */
   public static Lexicon getLexicon(String name, @NonNull Language language) {
      Validation.notNullOrBlank(name, "Lexicon name must not be null or blank.");
      return lexiconCache.get(String.format("%s::%s", language.getCode(), name),
                              () -> loadLexicon(name, language));
   }

   /**
    * Gets the lexicon of the given name for the default language as defined in {@link Hermes#defaultLanguage()}.
    *
    * @param name the name of the lexicon
    * @return the lexicon
    */
   public static Lexicon getLexicon(String name) {
      return getLexicon(name, Hermes.defaultLanguage());
   }

   protected static Lexicon loadLexicon(String name, Language language) {
      try {
         LexiconSpecification specification = safeParse(ResourceType.LEXICON.getConfigValue(name, language)
                                                                            .orElse(null));
         if(specification == null) {
            //No Lexicon Specification defined, we will try loading a lexicon
            return ResourceType.LEXICON
                  .locate(name, language)
                  .map(r -> {
                     if(r.baseName().endsWith(".json")) {
                        return "lexicon:mem:json::" + r.descriptor();
                     }
                     return "lexicon:disk:" + name + "::" + r.descriptor();
                  }).map(Unchecked.function(s -> LexiconSpecification.parse(s).create()))
                  .orElseGet(() -> {
                     logWarning(log, "Unable to find lexicon: {0} in {1}", name, language);
                     return EmptyLexicon;
                  });
         }
         return specification.create();
      } catch(IOException e) {
         logWarning(log, e);
         return EmptyLexicon;
      }
   }

   /**
    * Registers a lexicon with a given name for the default language as defined in {@link Hermes#defaultLanguage()}.
    *
    * @param name    the name
    * @param lexicon the lexicon
    */
   public static void register(String name, @NonNull Lexicon lexicon) {
      register(Validation.notNullOrBlank(name), Hermes.defaultLanguage(), lexicon);
   }

   /**
    * Registers a lexicon with the given name for the given language
    *
    * @param name     the name
    * @param language the language
    * @param lexicon  the lexicon
    */
   public static void register(String name, @NonNull Language language, @NonNull Lexicon lexicon) {
      lexiconCache.put(String.format("%s::%s", language.getCode(), Validation.notNullOrBlank(name)), lexicon);
   }

   /**
    * Removes the lexicon with the given name for the default language as defined in {@link Hermes#defaultLanguage()}.
    *
    * @param name the name
    */
   public static void remove(String name) {
      remove(Validation.notNullOrBlank(name), Hermes.defaultLanguage());
   }

   /**
    * Removes the lexicon with the given name for the given language
    *
    * @param name     the name
    * @param language the language
    */
   public static void remove(String name, @NonNull Language language) {
      lexiconCache.invalidate(String.format("%s::%s", language.getCode(), Validation.notNullOrBlank(name)));
   }

   protected static LexiconSpecification safeParse(String spec) {
      try {
         return LexiconSpecification.parse(spec);
      } catch(Exception e) {
         return null;
      }
   }

}//END OF LexiconManager
