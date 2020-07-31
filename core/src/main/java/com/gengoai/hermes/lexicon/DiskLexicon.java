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

package com.gengoai.hermes.lexicon;

import com.gengoai.Validation;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.Sets;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.HString;
import com.gengoai.kv.KeyValueStoreConnection;
import com.gengoai.kv.NavigableKeyValueStore;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link PersistentLexicon} that stores {@link LexiconEntry} on disk facilitating the use of very lexicons with
 * little memory overhead.
 */
public class DiskLexicon extends PersistentLexicon implements PrefixSearchable {
   private static final long serialVersionUID = 1L;
   private final NavigableKeyValueStore<String, List<LexiconEntry>> lexicon;
   private final NavigableKeyValueStore<String, Object> metadata;
   private final String name;

   /**
    * Instantiates a new DiskLexicon
    *
    * @param connection      the KeyValueStoreConnection describing how to connect to the underlying storage.
    * @param isCaseSensitive True - the lexicon is case-sensitive, False case-insensitive (Note: if the lexicon already
    *                        exists this value will be ignored.)
    */
   public DiskLexicon(@NonNull KeyValueStoreConnection connection, boolean isCaseSensitive) {
      this.name = connection.getNamespace();
      this.lexicon = connection.connect();
      connection.setNamespace(connection.getNamespace() + "_metadata");
      this.metadata = connection.connect();
      if(!this.metadata.containsKey("isCaseSensitive")) {
         this.metadata.put("isCaseSensitive", isCaseSensitive);
      }
   }

   @Override
   public void add(@NonNull LexiconEntry lexiconEntry) {
      addAll(Collections.singleton(lexiconEntry));
   }

   @Override
   public synchronized void addAll(@NonNull Iterable<LexiconEntry> lexiconEntries) {
      boolean changed = false;
      int maxTokenLength = getMaxTokenLength();
      int maxLemmaLength = getMaxLemmaLength();
      boolean isProbabilistic = isProbabilistic();

      for(LexiconEntry lexiconEntry : lexiconEntries) {
         if(Strings.isNotNullOrBlank(lexiconEntry.getLemma())) {
            String lemma = normalize(Validation.notNullOrBlank(lexiconEntry.getLemma()));
            if(lexiconEntry.getProbability() > 0 && lexiconEntry.getProbability() < 1) {
               isProbabilistic = true;
            } else {
               lexiconEntry = LexiconEntry.of(lexiconEntry.getLemma(),
                                              1.0,
                                              lexiconEntry.getTag(),
                                              lexiconEntry.getConstraint(),
                                              lexiconEntry.getTokenLength());
            }
            List<LexiconEntry> lemmaEntries = lexicon.getOrDefault(lemma, new ArrayList<LexiconEntry>());
            lemmaEntries.add(lexiconEntry);
            lexicon.put(lemma, lemmaEntries);
            maxTokenLength = Math.max(maxTokenLength, lexiconEntry.getTokenLength());
            maxLemmaLength = Math.max(maxLemmaLength, lemma.length());
            changed = true;
         }
      }

      if(changed) {
         metadata.put("isProbabilistic", isProbabilistic);
         metadata.put("maxLemmaLength", maxLemmaLength);
         metadata.put("maxTokenLength", maxTokenLength);
         metadata.commit();
         lexicon.commit();
      }
   }

   @Override
   public boolean contains(String string) {
      return lexicon.containsKey(string);
   }

   @Override
   public Set<LexiconEntry> entries() {
      return Sets.asHashSet(Iterables.flatten(lexicon.values()));
   }

   @Override
   public Set<LexiconEntry> get(String word) {
      word = normalize(word);
      if(lexicon.containsKey(word)) {
         return new HashSet<>(lexicon.get(word));
      }
      return Collections.emptySet();
   }

   @Override
   public int getMaxLemmaLength() {
      return (Integer) metadata.getOrDefault("maxLemmaLength", 0);
   }

   @Override
   public int getMaxTokenLength() {
      return (Integer) metadata.getOrDefault("maxTokenLength", 0);
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public boolean isCaseSensitive() {
      return (Boolean) metadata.get("isCaseSensitive");
   }

   @Override
   public boolean isPrefixMatch(@NonNull HString hString) {
      return isPrefixMatch(hString.toString()) || isPrefixMatch(hString.getLemma());
   }

   @Override
   public boolean isPrefixMatch(String hString) {
      String normed = normalize(Validation.notNullOrBlank(hString));
      Iterator<String> itr = lexicon.keyIterator(normed);
      if(itr.hasNext()) {
         String n = itr.next();
         return normed.equals(n) || n.startsWith(normed);
      } else {
         return false;
      }
   }

   @Override
   public boolean isProbabilistic() {
      return (Boolean) metadata.getOrDefault("isProbabilistic", false);
   }

   @Override
   public Iterator<String> iterator() {
      return lexicon.keySet().iterator();
   }

   @Override
   public List<LexiconEntry> match(@NonNull HString string) {
      String str = normalize(string);
      if(!lexicon.containsKey(str)) {
         if(isCaseSensitive() && Strings.isUpperCase(string)) {
            return Collections.emptyList();
         }
         str = normalize(string.getLemma());
      }
      if(lexicon.containsKey(str)) {
         return Cast.as(lexicon.get(str)
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
      if(lexicon.containsKey(str)) {
         return Cast.as(lexicon.get(str)
                               .stream()
                               .sorted()
                               .collect(Collectors.toList()));
      }
      return Collections.emptyList();
   }

   @Override
   public Set<String> prefixes(String string) {
      Iterator<String> itr = lexicon.keyIterator(Validation.notNullOrBlank(string));
      Set<String> prefixes = new HashSet<>();
      while(itr.hasNext()) {
         String p = itr.next();
         if(p.startsWith(string)) {
            prefixes.add(p);
         } else {
            break;
         }
      }
      return prefixes;
   }

   @Override
   public int size() {
      return entries().size();
   }

}//END OF DiskLexicon
