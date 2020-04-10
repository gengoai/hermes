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

package com.gengoai.hermes.en;

import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.Featurizer;
import com.gengoai.apollo.ml.FitParameters;
import com.gengoai.apollo.ml.Params;
import com.gengoai.apollo.ml.preprocess.MinCountTransform;
import com.gengoai.apollo.ml.sequence.GreedyAvgPerceptron;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ml.POSTagger;
import com.gengoai.hermes.ml.SequenceGenerator;
import com.gengoai.hermes.ml.feature.AffixFeaturizer;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.hermes.ml.trainer.SequenceTaggerTrainer;
import com.gengoai.string.Strings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static com.gengoai.apollo.ml.Featurizer.booleanFeaturizer;
import static com.gengoai.apollo.ml.Featurizer.predicateFeaturizer;

public class ENPOSTrainer extends SequenceTaggerTrainer<POSTagger> {
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
      return new GreedyAvgPerceptron(new ENPOSValidator(),
                                     new MinCountTransform(5));
   }

   @Override
   protected POSTagger createTagger(SequenceLabeler labeler, FeatureExtractor<HString> featureExtractor) {
      LocalDateTime now = LocalDateTime.now();
      String version = now.format(DateTimeFormatter.ofPattern("YYYY_MM_DD"));
      return new ENPOSTagger(featureExtractor, labeler, version);
   }

   @Override
   public FitParameters<?> getDefaultFitParameters() {
      GreedyAvgPerceptron.Parameters parameters = new GreedyAvgPerceptron.Parameters();
      parameters.set(Params.Optimizable.maxIterations, 200);
      parameters.set(Params.verbose, true);
      parameters.set(Params.Optimizable.historySize, 3);
      parameters.set(Params.Optimizable.tolerance, 1e-4);
      return parameters;
   }

   @Override
   protected SequenceGenerator getSequenceGenerator() {
      return new SequenceGenerator()
            .labelGenerator(HString::pos)
            .featureExtractor(createFeatureExtractor());
   }
}//END OF ENPOSTrainer