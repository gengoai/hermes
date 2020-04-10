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

import com.gengoai.collection.Iterables;
import com.gengoai.collection.Sets;
import com.gengoai.collection.tree.Trie;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.HString;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Implementation of <code>Lexicon</code> usng a Trie data structure.</p>
 *
 * @author David B. Bracewell
 */
public class TrieLexicon extends Lexicon {
   private static final long serialVersionUID = 1L;
   private final String name;
   private final boolean caseSensitive;
   private final Trie<List<LexiconEntry>> trie = new Trie<>();
   private int maxLemmaLength = 0;
   private int maxTokenLength = 0;
   private boolean probabilistic = false;

   /**
    * Instantiates a new TrieLexicon.
    *
    * @param name          the name
    * @param caseSensitive True - if case matters, False case does not matter
    */
   public TrieLexicon(String name,
                      boolean caseSensitive) {
      this.name = name;
      this.caseSensitive = caseSensitive;
   }

   public synchronized void add(@NonNull LexiconEntry lexiconEntry) {
      if(Strings.isNotNullOrBlank(lexiconEntry.getLemma())) {
         String norm = normalize(lexiconEntry.getLemma());
         this.maxTokenLength = Math.max(maxTokenLength, lexiconEntry.getTokenLength());
         this.maxLemmaLength = Math.max(maxLemmaLength, norm.length());
         if(lexiconEntry.getProbability() > 0 && lexiconEntry.getProbability() <= 1) {
            this.probabilistic = true;
         } else {
            lexiconEntry = LexiconEntry.of(lexiconEntry.getLemma(),
                                           1.0,
                                           lexiconEntry.getTag(),
                                           lexiconEntry.getConstraint(),
                                           lexiconEntry.getTokenLength());
         }
         trie.putIfAbsent(norm, new ArrayList<>());
         trie.get(norm).add(lexiconEntry);
      }
   }

   @Override
   public boolean contains(String string) {
      return trie.containsKey(normalize(string));
   }

   @Override
   public Set<LexiconEntry> entries() {
      return Sets.asHashSet(Iterables.flatten(trie.values()));
   }

   @Override
   public Set<LexiconEntry> get(String word) {
      word = normalize(word);
      if(trie.containsKey(word)) {
         return new HashSet<>(trie.get(word));
      }
      return Collections.emptySet();
   }

   @Override
   public int getMaxLemmaLength() {
      return maxLemmaLength;
   }

   @Override
   public int getMaxTokenLength() {
      return maxTokenLength;
   }

   @NonNull
   public String getName() {
      return name;
   }

   @Override
   public boolean isCaseSensitive() {
      return caseSensitive;
   }

   @Override
   public boolean isPrefixMatch(HString hString) {
      return trie.prefix(normalize(hString)).size() > 0 || trie.prefix(normalize(hString.getLemma())).size() > 0;
   }

   @Override
   public boolean isPrefixMatch(String string) {
      return trie.prefix(normalize(string)).size() > 0;
   }

   @Override
   public boolean isProbabilistic() {
      return probabilistic;
   }

   @Override
   public Iterator<String> iterator() {
      return trie.keySet().iterator();
   }

   @Override
   public List<LexiconEntry> match(HString string) {
      String str = normalize(string);
      if(!trie.containsKey(str)) {
         if(isCaseSensitive() && Strings.isUpperCase(string)) {
            return Collections.emptyList();
         }
         str = normalize(string.getLemma());
      }
      if(trie.containsKey(str)) {
         return Cast.as(trie.get(str)
                            .stream()
                            .filter(le -> le.getConstraint() == null || le.getConstraint().test(string))
                            .sorted()
                            .collect(Collectors.toList()));
      }
      return Collections.emptyList();
   }

   @Override
   public List<LexiconEntry> match(String hString) {
      String str = normalize(hString);
      if(trie.containsKey(str)) {
         return Cast.as(trie.get(str)
                            .stream()
                            .sorted()
                            .collect(Collectors.toList()));
      }
      return Collections.emptyList();
   }

   @Override
   public Set<String> prefixes(String string) {
      return trie.prefix(string).keySet();
   }

   @Override
   public int size() {
      return trie.size();
   }

   /**
    * Suggest map.
    *
    * @param element the element
    * @return the map
    */
   public Map<String, Integer> suggest(String element) {
      return trie.suggest(element);
   }

   /**
    * Suggest map.
    *
    * @param element the element
    * @param maxCost the max cost
    * @return the map
    */
   public Map<String, Integer> suggest(String element, int maxCost) {
      return trie.suggest(element, maxCost);
   }

   /**
    * Suggest map.
    *
    * @param element          the element
    * @param maxCost          the max cost
    * @param substitutionCost the substitution cost
    * @return the map
    */
   public Map<String, Integer> suggest(String element, int maxCost, int substitutionCost) {
      return trie.suggest(element, maxCost, substitutionCost);
   }

}//END OF BaseTrieLexicon


