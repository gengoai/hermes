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

import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.feature.FeatureExtractor;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.model.sequence.Crf;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.PennTreeBank;

import java.util.Set;
import java.util.stream.Collectors;

import static com.gengoai.hermes.ml.feature.Features.*;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.lenientContext;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.strictContext;

public class PhraseChunkTagger extends IOBTagger {

   private static FeatureExtractor<HString> getFeaturizer() {
      return Featurizer.chain(LowerCaseWord, PartOfSpeech, WordClass)
                       .withContext(lenientContext(LowerCaseWord, -1),
                                    strictContext(LowerCaseWord, -1, LowerCaseWord, 0),
                                    lenientContext(LowerCaseWord, -2),
                                    lenientContext(LowerCaseWord, +1),
                                    strictContext(LowerCaseWord, 0, LowerCaseWord, +1),
                                    lenientContext(LowerCaseWord, +2),
                                    lenientContext(PartOfSpeech, -1),
                                    strictContext(PartOfSpeech, -1, LowerCaseWord, 0),
                                    strictContext(PartOfSpeech, -1, PartOfSpeech, 0),
                                    lenientContext(PartOfSpeech, -2),
                                    lenientContext(PartOfSpeech, +1),
                                    lenientContext(PartOfSpeech, +2),
                                    strictContext(PartOfSpeech, 0, LowerCaseWord, +1),
                                    strictContext(PartOfSpeech, 0, PartOfSpeech, +1)
                                   );
   }

   private static Set<String> getValidTags() {
      return com.gengoai.hermes.morphology.PartOfSpeech.values().stream()
                                                       .filter(com.gengoai.hermes.morphology.PartOfSpeech::isPhraseTag)
                                                       .filter(t -> !t.isInstance(PennTreeBank.INTJ,
                                                                                  PennTreeBank.LST,
                                                                                  PennTreeBank.UCP))
                                                       .map(com.gengoai.hermes.morphology.PartOfSpeech::tag)
                                                       .collect(Collectors.toSet());
   }

   public PhraseChunkTagger() {
      super(HStringDataSetGenerator.builder(Types.SENTENCE)
                                   .defaultOutput(IOB.encoder(Types.PHRASE_CHUNK, getValidTags()))
                                   .tokenSequence(Datum.DEFAULT_INPUT, getFeaturizer())
                                   .build(),
            Types.PHRASE_CHUNK,
            new Crf(p -> {
               p.minFeatureFreq.set(5);
            }));
   }

}//END OF PhraseChunkTagger
