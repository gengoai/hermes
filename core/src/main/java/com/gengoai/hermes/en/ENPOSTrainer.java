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

import com.gengoai.apollo.ml.DataSetType;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.feature.FeatureExtractor;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.model.PipelineModel;
import com.gengoai.apollo.ml.model.sequence.GreedyAvgPerceptron;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.transform.MinCountFilter;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.POSTagger;
import com.gengoai.hermes.ml.feature.AffixFeaturizer;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.hermes.ml.trainer.SequenceTaggerTrainer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.gengoai.hermes.ml.feature.Features.LowerCaseWord;
import static com.gengoai.hermes.ml.feature.Features.WordAndClass;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.strictContext;

public class ENPOSTrainer extends SequenceTaggerTrainer<POSTagger> {
   private FeatureExtractor<HString> createFeatureExtractor() {
      return Featurizer.chain(new AffixFeaturizer(3, 3),
                              LowerCaseWord,
                              WordAndClass,
                              Features.IsPunctuation,
                              Features.IsInitialCapital,
                              Features.IsAllCaps,
                              Features.IsInitialCapital,
                              Features.IsDigit)
                       .withContext(
                             "LowerWord[-1]",
                             "~LowerWord[-2]",
                             "LowerWord[+1]",
                             "~LowerWord[+2]",
                             strictContext(WordAndClass, -1),
                             strictContext(WordAndClass, -2),
                             strictContext(LowerCaseWord, 1),
                             strictContext(LowerCaseWord, 2)
                                   );
   }

   @Override
   protected Model createSequenceLabeler(FitParameters<?> parameters) {
      return PipelineModel.builder()
                          .defaultInput(new MinCountFilter(5))
                          .build(new GreedyAvgPerceptron(Cast.<GreedyAvgPerceptron.Parameters>as(parameters)));
   }

   @Override
   protected POSTagger createTagger(Model labeler, HStringDataSetGenerator featureExtractor) {
      LocalDateTime now = LocalDateTime.now();
      String version = now.format(DateTimeFormatter.ofPattern("YYYY_MM_DD"));
      return new ENPOSTagger(featureExtractor, labeler, version);
   }

   @Override
   protected HStringDataSetGenerator getExampleGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .dataSetType(DataSetType.InMemory)
                                    .tokenSequence(Datum.DEFAULT_INPUT, createFeatureExtractor())
                                    .tokenSequence(Datum.DEFAULT_OUTPUT, h -> Variable.binary(h.pos().name()))
                                    .build();
   }

   @Override
   public FitParameters<?> getFitParameters() {
      return new GreedyAvgPerceptron.Parameters()
            .update(parameters -> {
               parameters.set(Params.Optimizable.maxIterations, 50);
               parameters.set(Params.verbose, true);
               parameters.set(Params.Optimizable.historySize, 3);
               parameters.set(Params.Optimizable.tolerance, 1e-4);
               parameters.validator.set(new ENPOSValidator());
            });
   }

}//END OF ENPOSTrainer
