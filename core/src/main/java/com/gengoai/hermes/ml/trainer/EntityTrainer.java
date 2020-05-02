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

import com.gengoai.LogUtils;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.feature.FeatureExtractor;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.sequence.Crf;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.feature.BasicCategoryFeature;
import lombok.NonNull;
import lombok.extern.java.Log;

import static com.gengoai.hermes.ml.feature.Features.*;

@Log
public class EntityTrainer extends IOBTaggerTrainer {

   public EntityTrainer() {
      super(Types.ML_ENTITY, Types.ENTITY);
   }

   @Override
   protected void addInputs(@NonNull HStringDataSetGenerator.Builder builder) {
      FeatureExtractor<HString> fe = getFeaturizer();
      LogUtils.logInfo(log, "\n{0}", fe);
      builder.tokenSequence(Datum.DEFAULT_INPUT, fe);
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

   private FeatureExtractor<HString> getFeaturizer() {
      return Featurizer.chain(LowerCaseWord,
                              PartOfSpeech,
                              WordShape,
                              WordClass,
                              IsBeginOfSentence,
                              IsEndOfSentence,
                              IsDigit,
                              IsPercent,
                              IsCardinalNumber,
                              IsOrdinalNumber,
                              IsTitleCase, IsAllCaps, HasCapital,
                              IsLanguageName,
                              IsAlphaNumeric,
                              IsCurrency,
                              IsPunctuation,
                              PhraseChunkBIO,
                              new BasicCategoryFeature()
                             )
                       .withContext(
                             "LowerWord[-1]",
                             "~LowerWord[-2]|LowerWord[-1]",
                             "LowerWord[+1]",
                             "~LowerWord[+1]|LowerWord[+2]",
                             "~LowerWord[-1]|LowerWord[+1]",
                             "~LowerWord[-2]|LowerWord[-1]|LowerWord[+1]|LowerWord[+2]",

                             "LowerWord[-1]|LowerWord[0]",
                             "~LowerWord[-2]|LowerWord[-1]|LowerWord[0]",
                             "LowerWord[0]|LowerWord[+1]",
                             "~LowerWord[0]|LowerWord[+1]|LowerWord[+2]",

                             "~LowerWord[-1]|LowerWord[0]|LowerWord[+1]",
                             "~LowerWord[-2]|LowerWord[-1]|LowerWord[0]|LowerWord[+1]|LowerWord[+2]",

                             "POS[-1]",
                             "~POS[-2]|POS[-1]",
                             "POS[+1]",
                             "~POS[+1]|POS[+2]",
                             "~POS[-1]|POS[+1]",
                             "~POS[-2]|POS[-1]|POS[+1]|POS[+2]",

                             "POS[-1]|LowerWord[0]",
                             "~POS[-2]|POS[-1]|LowerWord[0]",
                             "LowerWord[0]|POS[+1]",
                             "~LowerWord[0]|POS[+1]|POS[+2]",

                             "WordShape[-1]",
                             "~WordShape[-2]|WordShape[-1]",
                             "WordShape[+1]",
                             "~WordShape[+1]|WordShape[+2]",
                             "~WordShape[-1]|WordShape[+1]",
                             "~WordShape[-2]|WordShape[-1]|WordShape[+1]|WordShape[+2]",

                             "WordShape[-1]|LowerWord[0]",
                             "~WordShape[-2]|WordShape[-1]|LowerWord[0]",
                             "LowerWord[0]|WordShape[+1]",
                             "~LowerWord[0]|WordShape[+1]|WordShape[+2]",

                             "WordClass[-1]",
                             "~WordClass[-2]|WordClass[-1]",
                             "WordClass[+1]",
                             "~WordClass[+1]|WordClass[+2]",
                             "~WordClass[-1]|WordClass[+1]",
                             "~WordClass[-2]|WordClass[-1]|WordClass[+1]|WordClass[+2]",

                             "WordClass[-1]|LowerWord[0]",
                             "~WordClass[-2]|WordClass[-1]|LowerWord[0]",
                             "LowerWord[0]|WordClass[+1]",
                             "~LowerWord[0]|WordClass[+1]|WordClass[+2]",

                             "IsBeginOfSentence[-1]|LowerWord[0]",
                             "LowerWord[0]|IsEndOfSentence[+1]",

                             "IsDigit[-1]|IsDigit[0]",
                             "IsPercent[-1]|IsPercent[0]",
                             "IsDigit[-1]|IsOrdinal[0]",
                             "IsOrdinal[-1]|IsOrdinal[0]",
                             "IsCardinal[-1]|IsCardinal[0]",
                             "IsOrdinal[-1]|POS[0]",
                             "IsCardinal[-1]|POS[0]",

                             "IsDigit[-1]|LowerWord[0]",
                             "IsPercent[-1]|LowerWord[0]",
                             "IsDigit[-1]|LowerWord[0]",
                             "IsOrdinal[-1]|LowerWord[0]",
                             "IsCardinal[-1]|LowerWord[0]",
                             "IsOrdinal[-1]|LowerWord[0]",
                             "IsCardinal[-1]|LowerWord[0]",

                             "IsTitleCase[-1]|LowerWord[0]",
                             "IsAllCaps[-1]|LowerWord[0]",
                             "HasCapital[-1]|LowerWord[0]"

                                   );
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
