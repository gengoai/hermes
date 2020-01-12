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

import com.gengoai.Tag;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.Sets;
import com.gengoai.collection.tree.Trie;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.AttributeType;
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
   private final boolean caseSensitive;
   private final int maxLemmaLength;
   private final int maxTokenLength;
   private final boolean probabilistic;
   private final AttributeType<Tag> tagAttributeType;
   private final Trie<List<LexiconEntry<?>>> trie = new Trie<>();

   /**
    * Instantiates a new Base lexicon.
    *
    * @param caseSensitive    True - if case matters, False case does not matter
    * @param probabilistic    True - the lexicon provides probability information
    * @param tagAttributeType Attribute to use when setting the tag
    */
   protected TrieLexicon(boolean caseSensitive,
                         boolean probabilistic,
                         AttributeType tagAttributeType,
                         int maxTokenLength,
                         int maxLemmaLength) {
      this.caseSensitive = caseSensitive;
      this.probabilistic = probabilistic;
      this.tagAttributeType = tagAttributeType;
      this.maxLemmaLength = maxLemmaLength;
      this.maxTokenLength = maxTokenLength;
   }

   public static LexiconBuilder builder(@NonNull AttributeType<?> attributeType, boolean isCaseSensitive) {
      return new TrieLexiconBuilder(attributeType, isCaseSensitive);
   }

   @Override
   public boolean contains(String string) {
      return trie.containsKey(normalize(string));
   }

   @Override
   public Set<LexiconEntry<?>> entries(String lemma) {
      lemma = normalize(lemma);
      if (trie.containsKey(lemma)) {
         return new HashSet<>(trie.get(lemma));
      }
      return Collections.emptySet();
   }

   @Override
   public Set<LexiconEntry<?>> entries() {
      return Sets.asHashSet(Iterables.flatten(trie.values()));
   }

   @Override
   public List<LexiconEntry<?>> getEntries(HString hString) {
      String str = normalize(hString);
      if (!trie.containsKey(str)) {
         if (isCaseSensitive() && Strings.isUpperCase(hString)) {
            return Collections.emptyList();
         }
         str = normalize(hString.getLemma());
      }
      if (trie.containsKey(str)) {
         return Cast.as(trie.get(str)
                            .stream()
                            .filter(le -> le.getConstraint() == null || le.getConstraint().test(hString))
                            .sorted()
                            .collect(Collectors.toList()));
      }
      return Collections.emptyList();
   }

   @Override
   public int getMaxLemmaLength() {
      return maxLemmaLength;
   }

   @Override
   public int getMaxTokenLength() {
      return maxTokenLength;
   }

   @Override
   public AttributeType<Tag> getTagAttributeType() {
      return tagAttributeType;
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

   private static class TrieLexiconBuilder extends LexiconBuilder {
      private static final long serialVersionUID = 1L;
      private final Trie<List<LexiconEntry<?>>> trie = new Trie<>();

      private TrieLexiconBuilder(AttributeType<?> attributeType, boolean isCaseSensitive) {
         super(attributeType, isCaseSensitive);
      }

      @Override
      public LexiconBuilder add(LexiconEntry<?> entry) {
         String norm = normalize(entry.getLemma());
         if (!trie.containsKey(norm)) {
            trie.put(norm, new LinkedList<>());
         }
         updateMax(norm, entry.getTokenLength());
         trie.get(norm).add(entry);
         return this;
      }

      @Override
      public Lexicon build() {
         TrieLexicon trieLexicon = new TrieLexicon(isCaseSensitive(),
                                                   isProbabilistic(),
                                                   getAttributeType(),
                                                   getMaxTokenLength(),
                                                   getMaxLemmaLength());
         trieLexicon.trie.putAll(Cast.as(trie));
         return trieLexicon;
      }

   }

}//END OF BaseTrieLexicon


