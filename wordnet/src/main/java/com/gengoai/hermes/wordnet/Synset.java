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

package com.gengoai.hermes.wordnet;

import com.gengoai.collection.multimap.SetMultimap;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.wordnet.properties.Property;
import com.gengoai.hermes.wordnet.properties.PropertyName;
import com.gengoai.tuple.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The interface Synset.
 *
 * @author David B. Bracewell
 */
public interface Synset {

   /**
    * Depth int.
    *
    * @return the int
    */
   int depth();

   /**
    * Gets gloss.
    *
    * @return the gloss
    */
   String getGloss();

   default Synset getHypernym() {
      return WordNet.getInstance().getHypernym(this);
   }

   default Set<Synset> getHyponyms() {
      return WordNet.getInstance().getHyponyms(this);
   }

   /**
    * Gets id.
    *
    * @return the id
    */
   String getId();

   /**
    * Gets lexicographer file.
    *
    * @return the lexicographer file
    */
   LexicographerFile getLexicographerFile();

   /**
    * Gets pOS.
    *
    * @return the pOS
    */
   PartOfSpeech getPOS();

   <T extends Property> T getProperty(PropertyName name);

   /**
    * Gets related synsets.
    *
    * @param relation the relation
    * @return the related synsets
    */
   Set<Synset> getRelatedSynsets(WordNetRelation relation);

   /**
    * Gets related synsets.
    *
    * @return the related synsets
    */
   SetMultimap<WordNetRelation, Synset> getRelatedSynsets();

   default Synset getRoot() {
      if(isRoot()) {
         return this;
      }
      return WordNet.getInstance().getRoots().stream()
                    .map(root -> Tuple2.of(root, WordNet.getInstance().distance(this, root)))
                    .filter(tuple -> Double.isFinite(tuple.getV2()))
                    .sorted(Map.Entry.comparingByValue())
                    .findFirst()
                    .map(Tuple2::getV1)
                    .orElse(this);
   }

   /**
    * Gets senses.
    *
    * @return the senses
    */
   List<Sense> getSenses();

   /**
    * Is adjective satelitie.
    *
    * @return the boolean
    */
   boolean isAdjectiveSatelitie();

   default boolean isRoot() {
      return WordNet.getInstance().getRoots().contains(this);
   }

}//END OF Synset
