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

import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.Sets;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.HString;
import com.gengoai.io.resource.Resource;
import com.gengoai.kv.KeyValueStoreConnection;
import com.gengoai.kv.NavigableKeyValueStore;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class DiskLexicon extends Lexicon implements PrefixSearchable {
   private static final long serialVersionUID = 1L;
   private final NavigableKeyValueStore<String, List<LexiconEntry<?>>> lexiconEntries;
   private final NavigableKeyValueStore<String, Object> metadata;


   protected DiskLexicon(KeyValueStoreConnection connection) {
      connection.setReadOnly(true);
      this.lexiconEntries = connection.connect();
      connection.setNamespace(connection.getNamespace() + "_metadata");
      this.metadata = connection.connect();
   }

   public static DiskLexiconBuilder builder(AttributeType tagAttributeType,
                                            boolean caseSensitive,
                                            String name,
                                            Resource path) {
      return new DiskLexiconBuilder(tagAttributeType, caseSensitive, name, path);
   }

   @Override
   public boolean contains(String string) {
      return lexiconEntries.containsKey(string);
   }

   @Override
   public Set<LexiconEntry<?>> entries(String lemma) {
      lemma = normalize(lemma);
      if(lexiconEntries.containsKey(lemma)) {
         return new HashSet<>(lexiconEntries.get(lemma));
      }
      return Collections.emptySet();
   }

   @Override
   public Set<LexiconEntry<?>> entries() {
      return Sets.asHashSet(Iterables.flatten(lexiconEntries.values()));
   }

   @Override
   public List<LexiconEntry<?>> getEntries(@NonNull HString hString) {
      String str = normalize(hString);
      if(!lexiconEntries.containsKey(str)) {
         if(isCaseSensitive() && Strings.isUpperCase(hString)) {
            return Collections.emptyList();
         }
         str = normalize(hString.getLemma());
      }
      if(lexiconEntries.containsKey(str)) {
         return Cast.as(lexiconEntries.get(str)
                                      .stream()
                                      .filter(le -> le.getConstraint() == null || le.getConstraint().test(hString))
                                      .sorted()
                                      .collect(Collectors.toList()));
      }
      return Collections.emptyList();
   }

   @Override
   public List<LexiconEntry<?>> getEntries(String hString) {
      String str = normalize(hString);
      if(lexiconEntries.containsKey(str)) {
         return Cast.as(lexiconEntries.get(str)
                                      .stream()
                                      .sorted()
                                      .collect(Collectors.toList()));
      }
      return Collections.emptyList();
   }

   @Override
   public int getMaxLemmaLength() {
      return (Integer) metadata.getOrDefault("maxLemmaLength", "0");
   }

   @Override
   public int getMaxTokenLength() {
      return (Integer) metadata.getOrDefault("maxTokenLength", "0");
   }

   @Override
   public AttributeType<Tag> getTagAttributeType() {
      return AttributeType.make((String) metadata.get("attributeType"));
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
      Iterator<String> itr = lexiconEntries.keyIterator(normalize(Validation.notNullOrBlank(hString)));
      while(true) {
         if(itr.hasNext()) {
            String n = itr.next();
            if(!hString.equals(n)) {
               return hString.startsWith(n);
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean isProbabilistic() {
      return (Boolean) metadata.get("isProbabilistic");
   }

   @Override
   public Iterator<String> iterator() {
      return lexiconEntries.keySet().iterator();
   }

   @Override
   public Set<String> prefixes(String string) {
      Iterator<String> itr = lexiconEntries.keyIterator(Validation.notNullOrBlank(string));
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

   public static class DiskLexiconBuilder extends LexiconBuilder {
      private final KeyValueStoreConnection connection;
      private final NavigableKeyValueStore<String, List<LexiconEntry<?>>> lexiconEntries;
      private final NavigableKeyValueStore<String, Object> metdata;

      protected DiskLexiconBuilder(@NonNull AttributeType<?> attributeType,
                                   boolean isCaseSensitive,
                                   String name,
                                   @NonNull Resource path) {
         super(attributeType, isCaseSensitive);
         Validation.notNullOrBlank(name);
         this.connection = new KeyValueStoreConnection();
         connection.setPath(path.path());
         connection.setCompressed(true);
         connection.setNamespace(name);
         connection.setNavigable(true);
         connection.setType("disk");
         this.lexiconEntries = connection.connect();
         connection.setNamespace(name + "_metadata");
         this.metdata = connection.connect();
         metdata.put("attributeType", attributeType.name());
         metdata.put("isCaseSensitive", false);
         metdata.put("isProbabilistic", false);
         metdata.put("isConstrained", false);
         connection.setNamespace(name);
      }

      @Override
      public LexiconBuilder add(@NonNull LexiconEntry<?> entry) {
         String norm = normalize(entry.getLemma());
         List<LexiconEntry<?>> entries = lexiconEntries.getOrDefault(norm, new LinkedList<>());
         updateMax(norm, entry.getTokenLength());
         entries.add(entry);
         if(entry.getProbability() != 1.0) {
            metdata.put("isProbabilistic", true);
         }
         if(entry.getConstraint() != null) {
            metdata.put("isConstrained", true);
         }
         lexiconEntries.put(norm, entries);
         return this;
      }

      @Override
      public Lexicon build() {
         lexiconEntries.commit();
         metdata.put("maxLemmaLength", getMaxLemmaLength());
         metdata.put("maxTokenLength", getMaxTokenLength());
         metdata.commit();
         connection.setReadOnly(true);
         return new DiskLexicon(connection);
      }
   }

}//END OF DiskLexicon
