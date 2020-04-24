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

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.model.sequence.Crf;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import lombok.NonNull;

import static com.gengoai.hermes.ml.feature.Features.*;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.lenientContext;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.strictContext;

public class EntityTrainer extends IOBTaggerTrainer {

   public EntityTrainer() {
      super(Types.ML_ENTITY, Types.ENTITY);
   }

   @Override
   protected void addInputs(@NonNull HStringDataSetGenerator.Builder builder) {
      builder.tokenSequence(Datum.DEFAULT_INPUT,
                            Featurizer.chain(LowerCaseWord,
                                             IsInitialCapital,
                                             IsAllCaps,
                                             hasCapital,
                                             IsDigit,
                                             IsAlphaNumeric,
                                             IsPunctuation,
                                             PartOfSpeech,
                                             IsLanguageName,
                                             IsPlace,
                                             IsStateOrPrefecture,
                                             IsMonth,
                                             IsOrganization,
                                             IsHuman,
                                             WordAndClass)
                                      .withContext(lenientContext(LowerCaseWord, -1),
                                                   strictContext(LowerCaseWord, -2),
                                                   strictContext(PartOfSpeech, -1),
                                                   strictContext(LowerCaseWord, -1, LowerCaseWord, 0),
                                                   strictContext(PartOfSpeech, -1, LowerCaseWord, 0),
                                                   strictContext(LowerCaseWord, -2, LowerCaseWord, -1),
                                                   strictContext(LowerCaseWord, -2,
                                                                 LowerCaseWord, -1,
                                                                 LowerCaseWord, 0),
                                                   strictContext(LowerCaseWord, -3,
                                                                 LowerCaseWord, -2,
                                                                 LowerCaseWord, -1,
                                                                 LowerCaseWord, 0),
                                                   lenientContext(LowerCaseWord, +1),
                                                   strictContext(PartOfSpeech, +1),
                                                   strictContext(LowerCaseWord, 0, LowerCaseWord, +1),
                                                   strictContext(LowerCaseWord, +2),
                                                   strictContext(LowerCaseWord, +1, LowerCaseWord, +2),
                                                   strictContext(LowerCaseWord,
                                                                 0,
                                                                 LowerCaseWord,
                                                                 +1,
                                                                 LowerCaseWord,
                                                                 +2),
                                                   strictContext(LowerCaseWord, 0, PartOfSpeech, +1),
                                                   strictContext(IsPlace, -1, IsPunctuation, 0, IsPlace, +1),
                                                   strictContext(IsTime, -1, IsPunctuation, 0, IsTime, +1)));
   }

   @Override
   public DataSet createDataset(@NonNull DocumentCollection data) {
      return data.annotate(Types.CATEGORY)
                 .asDataSet(getExampleGenerator());
   }

   @Override
   protected Model createSequenceLabeler(FitParameters<?> fitParameters) {
      return new Crf(Cast.<Crf.Parameters>as(fitParameters));
   }

   @Override
   public FitParameters<?> getFitParameters() {
      return new Crf.Parameters()
            .update(p -> {
               p.minFeatureFreq.set(5);
               p.maxIterations.set(500);
            });
   }

}//END OF EntityTrainer
