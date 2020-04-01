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

package com.gengoai.hermes.ml.feature;

import com.gengoai.apollo.ml.Feature;
import com.gengoai.apollo.ml.Featurizer;
import com.gengoai.hermes.HString;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Affix featurizer.
 *
 * @author David B. Bracewell
 */
public class AffixFeaturizer extends Featurizer<HString> {
   private static final long serialVersionUID = 1L;
   private final int prefixSize;
   private final int suffixSize;

   /**
    * Instantiates a new Affix featurizer.
    *
    * @param prefixSize the prefix size
    * @param suffixSize the suffix size
    */
   public AffixFeaturizer(int prefixSize, int suffixSize) {
      this.prefixSize = prefixSize;
      this.suffixSize = suffixSize;
   }

   @Override
   public List<Feature> applyAsFeatures(HString word) {
      List<Feature> features = new ArrayList<>();
      for (int i = 1; word.length() > i && i <= suffixSize; i++) {
         features.add(
            Feature.booleanFeature(i + "-SUFFIX", word.substring(word.length() - i, word.length()).toString()));
      }
      for (int i = 1; word.length() > i && i <= prefixSize; i++) {
         features.add(Feature.booleanFeature(i + "-PREFIX", word.substring(0, i).toString()));
      }
      return features;
   }

}//END OF AffixFeaturizer
