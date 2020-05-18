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

package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.DataSetType;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.feature.FeatureExtractor;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.model.sequence.Crf;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import lombok.NonNull;
import lombok.extern.java.Log;

import static com.gengoai.hermes.ml.feature.Features.*;

@Log
public class EntityTagger extends IOBTagger {
   private static final long serialVersionUID = 1L;

   private static FeatureExtractor<HString> getFeaturizer() {
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
                              IsPunctuation
                             )
                       .withContext(
                             "LowerWord[-1]",
                             "~LowerWord[-2..-1]",
                             "LowerWord[+1]",
                             "~LowerWord[+1..+2]",
                             "~LowerWord[-1]|LowerWord[+1]",
                             "~LowerWord[-2..-1]|LowerWord[+1..+2]",
                             "~<1,2>LowerWord[-2,-1]|LowerWord[0]",
                             "~LowerWord[0] | <1,2>LowerWord[1,2]",
                             "~LowerWord[-1..+1]",
                             "~LowerWord[-2..+2]",

                             "POS[-1]",
                             "~POS[-2..-1]",
                             "POS[+1]",
                             "~POS[+1..+2]",
                             "~POS[-1]|POS[+1]",
                             "~POS[-2..-1]|POS[+1..+2]",

                             "~<1,2>POS[-2,-1]|LowerWord[0]",
                             "~LowerWord[0] | <1,2>POS[1,2]",
                             "~POS[-1..+1]",
                             "~POS[-2..+2]",

                             "WordShape[-1]",
                             "~WordShape[-2..-1]",
                             "WordShape[+1]",
                             "~WordShape[+1..+2]",
                             "~WordShape[-1]|WordShape[+1]",
                             "~WordShape[-2..-1]|WordShape[+1..+2]",
                             "~<1,2>WordShape[-2,-1]|LowerWord[0]",
                             "~LowerWord[0] | <1,2>WordShape[1,2]",
                             "~WordShape[-1..+1]",
                             "~WordShape[-2..+2]",

                             "WordClass[-1]",
                             "~WordClass[-2..-1]",
                             "WordClass[+1]",
                             "~WordClass[+1..+2]",
                             "~WordClass[-1]|WordClass[+1]",
                             "~WordClass[-2..-1]|WordClass[+1..+2]",
                             "~<1,2>WordClass[-2,-1]|LowerWord[0]",
                             "~LowerWord[0] | <1,2>WordClass[1,2]",
                             "~WordClass[-1..+1]",
                             "~WordClass[-2..+2]",

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

   public EntityTagger() {
      super(HStringDataSetGenerator.builder(Types.SENTENCE)
                                   .defaultOutput(IOB.encoder(Types.ENTITY))
                                   .tokenSequence(Datum.DEFAULT_INPUT, getFeaturizer())
                                   .dataSetType(DataSetType.InMemory)
                                   .build(),
            Types.ML_ENTITY,
            new Crf(p -> {
               p.minFeatureFreq.set(5);
               p.maxIterations.set(500);
            }));
   }

   @Override
   public void estimate(@NonNull DocumentCollection documentCollection) {
      estimate(documentCollection.annotate(Types.CATEGORY,
                                           Types.PHRASE_CHUNK).asDataSet(getDataGenerator()));
   }

}//END OF EntityTagger
