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

import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.Language;
import com.gengoai.collection.multimap.SetMultimap;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.wordnet.*;

import java.io.Serializable;
import java.util.*;

/**
 * The type Sense.
 *
 * @author David B. Bracewell
 */
public class SenseImpl implements Sense, Serializable {

   private static final long serialVersionUID = -7237141651119077412L;
   private AdjectiveMarker adjectiveMarker;
   private String id;
   private Language language;
   private String lemma;
   private int lexicalId;
   private int sense;
   private Synset synset;
   private List<VerbFrame> verbFrames = new ArrayList<>(0);
   private WordNetPOS partOfSpeech;
   private int synsetPosition;

   /**
    * Add verb frame.
    *
    * @param frame the frame
    */
   public void addVerbFrame(VerbFrame frame) {
      verbFrames.add(frame);
   }

   @Override
   public int compareTo(Sense o) {
      if(o == null) {
         return 1;
      }
      int cmp = lemma.compareToIgnoreCase(o.getLemma());
      if(cmp == 0) {
         cmp = Integer.compare(sense, o.getSenseNumber());
      }
      if(cmp == 0) {
         cmp = partOfSpeech
               .toString()
               .compareTo(o
                                .getPOS()
                                .toString());
      }
      if(cmp == 0) {
         cmp = language.compareTo(o.getLanguage());
      }
      return cmp;
   }

   @Override
   public int depth() {
      return getSynset().depth();
   }

   @Override
   public boolean equals(Object obj) {
      if(this == obj) {
         return true;
      }
      if(obj == null || getClass() != obj.getClass()) {
         return false;
      }
      final SenseImpl other = (SenseImpl) obj;
      return Objects.equals(this.id, other.id);
   }

   @Override
   public AdjectiveMarker getAdjectiveMarker() {
      return adjectiveMarker;
   }

   @Override
   public String getId() {
      return id;
   }

   @Override
   public Language getLanguage() {
      return language;
   }

   @Override
   public String getLemma() {
      return lemma;
   }

   @Override
   public int getLexicalId() {
      return lexicalId;
   }

   @Override
   public PartOfSpeech getPOS() {
      return partOfSpeech.toHermesPOS();
   }

   @Override
   public Set<Sense> getRelatedSenses(WordNetRelation relation) {
      return WordNet
            .getInstance()
            .getRelatedSenses(this, relation);
   }

   @Override
   public SetMultimap<WordNetRelation, Sense> getRelatedSenses() {
      return WordNet
            .getInstance()
            .getRelatedSenses(this);
   }

   @Override
   public int getSenseNumber() {
      return sense;
   }

   @Override
   public Synset getSynset() {
      return synset;
   }

   @Override
   public int getSynsetPosition() {
      return synsetPosition;
   }

   @Override
   public List<VerbFrame> getVerbFrames() {
      return Collections.unmodifiableList(verbFrames);
   }

   @Override
   public int hashCode() {
      return id.hashCode();
   }

   public void setAdjectiveMarker(AdjectiveMarker adjectiveMarker) {
      this.adjectiveMarker = adjectiveMarker;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setLanguage(Language language) {
      this.language = language;
   }

   public void setLemma(String lemma) {
      this.lemma = lemma;
   }

   public void setLexicalId(int lexicalId) {
      this.lexicalId = lexicalId;
   }

   /**
    * Sets part of speech.
    *
    * @param partOfSpeech the part of speech
    */
   public void setPartOfSpeech(WordNetPOS partOfSpeech) {
      this.partOfSpeech = partOfSpeech;
   }

   public void setSense(int sense) {
      this.sense = sense;
   }

   public void setSynset(Synset synset) {
      this.synset = synset;
   }

   /**
    * Sets synset position.
    *
    * @param synsetPosition the synset position
    */
   public void setSynsetPosition(int synsetPosition) {
      this.synsetPosition = synsetPosition;
   }

   @Override
   @JsonValue
   public String toString() {
      return lemma + "#" + partOfSpeech.getTag() + "#" + sense + "#" + language.getCode();
   }

}//END OF Sense
