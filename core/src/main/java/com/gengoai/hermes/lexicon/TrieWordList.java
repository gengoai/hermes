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

import com.gengoai.collection.Iterators;
import com.gengoai.collection.tree.Trie;
import com.gengoai.hermes.HString;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.gengoai.collection.Maps.asHashMap;

/**
 * Implementation of a {@link WordList} backed by a Trie
 *
 * @author David B. Bracewell
 */
public class TrieWordList implements WordList, PrefixSearchable, Serializable {
   private static final long serialVersionUID = 1L;
   private final Trie<Boolean> words;

   /**
    * <p>
    * Reads the word list from the given resource where each term is on its own line and "#" represents comments.
    * </p>
    * <p>
    * Note that convention states that if the first line of a word list is a comment stating "case-insensitive" then
    * loading of that word list will result in all words being lower-cased.
    * </p>
    *
    * @param resource the resource
    * @return the trie word list
    * @throws IOException the io exception
    */
   public static TrieWordList read(@NonNull Resource resource) throws IOException {
      TrieWordList twl = new TrieWordList();
      boolean firstLine = true;
      boolean isLowerCase = false;
      try(MStream<String> lines = resource.lines()) {
         for(String line : lines) {
            line = line.strip();
            if(firstLine && line.startsWith("#")) {
               isLowerCase = line.contains("case-insensitive");
            }
            firstLine = false;
            if(!line.startsWith("#")) {
               if(isLowerCase) {
                  twl.words.put(line.toLowerCase(), true);
               } else {
                  twl.words.put(line, true);
               }
            }
         }
      } catch(Exception e) {
         throw new IOException(e);
      }
      return twl;
   }

   /**
    * Instantiates a new TrieWordList
    *
    * @param words the words
    */
   public TrieWordList(@NonNull Iterable<String> words) {
      this.words = new Trie<>(asHashMap(words, s -> Boolean.TRUE));
   }

   /**
    * Instantiates a new TrieWordList
    *
    * @param wordLists the wordLists
    */
   public TrieWordList(@NonNull WordList... wordLists) {
      this.words = new Trie<>();
      for(WordList wordList : wordLists) {
         words.putAll(asHashMap(wordList, s -> Boolean.TRUE));
      }
   }

   private TrieWordList() {
      this.words = new Trie<>();
   }

   @Override
   public boolean contains(String string) {
      return words.containsKey(string);
   }

   @Override
   public boolean isPrefixMatch(HString hString) {
      return !words.prefix(hString.toString()).isEmpty();
   }

   @Override
   public boolean isPrefixMatch(String hString) {
      return !words.prefix(hString).isEmpty();
   }

   @Override
   public Iterator<String> iterator() {
      return Iterators.unmodifiableIterator(words.keySet().iterator());
   }

   @Override
   public Set<String> prefixes(String string) {
      return words.prefix(string).keySet();
   }

   @Override
   public int size() {
      return words.size();
   }

   /**
    * Suggests potential matches based on the given elements.
    *
    * @param string the string to generate suggestions for
    * @return the map of suggestions with their costs
    */
   public Map<String, Integer> suggest(String string) {
      return words.suggest(string);
   }

   /**
    * Suggests potential matches based on the given elements.
    *
    * @param string  the string to generate suggestions for
    * @param maxCost the maximum cost of the suggestions
    * @return the map of suggestions with their costs
    */
   public Map<String, Integer> suggest(String string, int maxCost) {
      return words.suggest(string, maxCost);
   }

   /**
    * Suggests potential matches based on the given elements.
    *
    * @param string           the string to generate suggestions for
    * @param maxCost          the maximum cost of the suggestions
    * @param substitutionCost the cost for string substitutions
    * @return the map of suggestions with their costs
    */
   public Map<String, Integer> suggest(String string, int maxCost, int substitutionCost) {
      return words.suggest(string, maxCost, substitutionCost);
   }

}//END OF TrieWordList
