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

package com.gengoai.hermes.ml.feature;

import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.hermes.BasicCategories;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.en.ENLexicons;
import com.gengoai.hermes.morphology.TokenType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class BasicCategoryFeature extends Featurizer<HString> {
   private static final long serialVersionUID = 1L;
   public static BasicCategoryFeature INSTANCE = new BasicCategoryFeature();

   @Override
   public List<Variable> applyAsFeatures(HString input) {
      Set<String> categories = new HashSet<>();
      if(input.isA((BasicCategories.STATES_OR_PREFECTURES))) {
         categories.add("IsStateOrPrefecture");
      }
      if(input.isA(BasicCategories.BUSINESS, BasicCategories.ORGANIZATIONS)
            || ENLexicons.ORGANIZATION.get().contains(input)
      ) {
         categories.add("IsOrganization");
      }
      if(input.isA(BasicCategories.HUMAN)) {
         categories.add("isHuman");
      }
      if(input.isA(BasicCategories.OCCUPATIONS)) {
         categories.add("isOccupation");
      }
      if(input.isA(BasicCategories.FACILITIES)) {
         categories.add("isFacility");
      }
      if(input.isA(BasicCategories.TIME)) {
         categories.add("isTime");
      }
      if(input.isA(BasicCategories.MONTHS)) {
         categories.add("isMonth");
      }
      if(input.isA(BasicCategories.PLACES)
            || ENLexicons.PLACE.get().contains(input)) {
         categories.add("isPlace");
      }
      if(input.isA(BasicCategories.GEOPOLITICAL_ENTITIES)) {
         categories.add("isGPE");
      }
      if(input.isA(BasicCategories.CULTURE)) {
         categories.add("isCulture");
      }
      if(input.attribute(Types.TOKEN_TYPE) == TokenType.NUMBER || input.pos().isNumeral()) {
         categories.add("isNumber");
      }
      if(ENLexicons.TIME.get().contains(input)) {
         categories.add("isTime");
      }
      if(ENLexicons.UNITS.get().contains(input)) {
         categories.add("isUnit");
      }
      if(ENLexicons.PERSON_TITLE.get().contains(input)) {
         categories.add("isPersonTitle");
      }
      return categories.stream().map(Variable::binary).collect(Collectors.toList());
   }

   @Override
   public String toString() {
      return "BasicCategoryFeature";
   }
}//END OF BasicCategoryFeature
