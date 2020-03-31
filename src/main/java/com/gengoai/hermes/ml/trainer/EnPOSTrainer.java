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

package com.gengoai.hermes.ml.trainer;

import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.Featurizer;
import com.gengoai.apollo.ml.FitParameters;
import com.gengoai.apollo.ml.sequence.GreedyAvgPerceptron;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ml.POSTagger;
import com.gengoai.hermes.ml.SequenceGenerator;
import com.gengoai.hermes.ml.feature.AffixFeaturizer;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.string.Strings;

import java.util.Collections;

import static com.gengoai.apollo.ml.Featurizer.booleanFeaturizer;
import static com.gengoai.apollo.ml.Featurizer.predicateFeaturizer;

public class EnPOSTrainer extends SequenceTaggerTrainer<POSTagger> {
   @Override
   public FeatureExtractor<HString> createFeatureExtractor() {
      return Featurizer.chain(new AffixFeaturizer(3, 3),
                              Features.LowerCaseWord,
                              predicateFeaturizer("ALL_UPPERCASE", Strings::isUpperCase),
                              predicateFeaturizer("START_UPPERCASE",
                                                  (HString s) -> Character.isUpperCase(s.charAt(0))),
                              predicateFeaturizer("IS_DIGIT", Features::isDigit),
                              predicateFeaturizer("IS_PUNCTUATION", Strings::isPunctuation),
                              booleanFeaturizer((HString a) -> {
                                 String norm = a.toLowerCase();
                                 if(norm.endsWith("es")) {
                                    return Collections.singleton("ENDING_ES");
                                 } else if(norm.endsWith("s")) {
                                    return Collections.singleton("ENDING_S");
                                 } else if(norm.endsWith("ed")) {
                                    return Collections.singleton("ENDING_ED");
                                 } else if(norm.endsWith("ing")) {
                                    return Collections.singleton("ENDING_ING");
                                 } else if(norm.endsWith("ly")) {
                                    return Collections.singleton("ENDING_LY");
                                 }
                                 return Collections.emptyList();
                              })).withContext("WORD[-1]",
                                              "WORD[-1]|WORD[0]",
                                              "~WORD[-2]",
                                              "~WORD[-2]|WORD[-1]",
                                              "WORD[+1]",
                                              "WORD[0]|WORD[+1]",
                                              "~WORD[+2]",
                                              "~WORD[+1]|WORD[+2]"
                                             );
   }

   @Override
   protected SequenceLabeler createSequenceLabeler() {
      return new GreedyAvgPerceptron();
   }

   @Override
   protected POSTagger createTagger(SequenceLabeler labeler, FeatureExtractor<HString> featureExtractor) {
      return new POSTagger(featureExtractor, labeler);
   }

   @Override
   public FitParameters<?> getDefaultFitParameters() {
      return new GreedyAvgPerceptron.Parameters();
   }

   @Override
   protected SequenceGenerator getSequenceGenerator() {
      return new SequenceGenerator().featureExtractor(createFeatureExtractor());
   }
}//END OF EnPOSTrainer
