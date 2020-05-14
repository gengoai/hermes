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

package com.gengoai.hermes.wordnet.io;

import com.gengoai.collection.HashBasedTable;
import com.gengoai.collection.Table;
import com.gengoai.collection.multimap.SetMultimap;
import com.gengoai.collection.multimap.TreeSetMultimap;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.wordnet.Sense;
import com.gengoai.hermes.wordnet.Synset;
import com.gengoai.hermes.wordnet.WordNetPOS;
import com.gengoai.hermes.wordnet.WordNetRelation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The type In memory word net db.
 *
 * @author David B. Bracewell
 */
public class InMemoryWordNetDB implements WordNetDB, Serializable {
   private static final long serialVersionUID = 3629513346307838903L;
   protected final SetMultimap<String, Sense> lemmaToSenseMap = new TreeSetMultimap<>();
   protected final Map<String, Synset> idToSynsetMap = new HashMap<>();
   protected final Table<Sense, Sense, WordNetRelation> senseRelations = new HashBasedTable<>();
   protected final Table<String, String, WordNetRelation> synsetRelations = new HashBasedTable<>();
   protected final Set<Synset> roots = new HashSet<>();


   @Override
   public Sense getSenseFromId(String id) {
      String[] parts = id.split("#");
      PartOfSpeech pos = WordNetPOS.fromString(parts[1]).toHermesPOS();
      int num = Integer.parseInt(parts[2]);
      return lemmaToSenseMap.get(parts[0])
                            .stream()
                            .filter(s -> s.getLexicalId() == num && s.getPOS() == pos)
                            .findFirst()
                            .orElse(null);
   }

   @Override
   public boolean containsLemma(String lemma) {
      return lemmaToSenseMap.containsKey(lemma);
   }

   @Override
   public Set<String> getLemmas() {
      return lemmaToSenseMap.keySet();
   }

   @Override
   public Set<Sense> getSenses() {
      return new HashSet<>(lemmaToSenseMap.values());
   }

   @Override
   public Set<Sense> getSenses(String lemma) {
      return lemmaToSenseMap.get(lemma);
   }

   @Override
   public Synset getSynsetFromId(String id) {
      return idToSynsetMap.get(id);
   }

   @Override
   public WordNetRelation getRelation(Sense sense1, Sense sense2) {
      return senseRelations.get(sense1, sense2);
   }

   @Override
   public WordNetRelation getRelation(Synset synset1, Synset synset2) {
      return synsetRelations.get(synset1.getId(), synset2.getId());
   }

   @Override
   public Map<Sense, WordNetRelation> getRelations(Sense sense) {
      return senseRelations.row(sense);
   }

   @Override
   public Map<String, WordNetRelation> getRelations(Synset synset) {
      return synsetRelations.row(synset.getId());
   }

   @Override
   public Set<Synset> getSynsets() {
      return new HashSet<>(idToSynsetMap.values());
   }

   @Override
   public Set<Synset> getRoots() {
      return roots;
   }

   @Override
   public void putSense(String lemma, Sense sense) {
      lemmaToSenseMap.put(lemma.toLowerCase().replace('_', ' '), sense);
   }

   @Override
   public void putSynset(String id, Synset synset) {
      idToSynsetMap.put(id, synset);
   }

   @Override
   public void putRelation(Sense s1, Sense s2, WordNetRelation relation) {
      senseRelations.put(s1, s2, relation);
   }

   @Override
   public void putRelation(String synsetId1, String synsetId2, WordNetRelation relation) {
      synsetRelations.put(synsetId1, synsetId2, relation);
   }

   @Override
   public void addRoot(Synset root) {
      roots.add(root);
   }

   public String toSenseRelationIndex(Sense sense) {
      return sense.getSynset().getId() + "%%" + Integer.toString(sense.getSynsetPosition());
   }


}//END OF WordNetDB
