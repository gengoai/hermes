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

import com.gengoai.Stopwatch;
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
import com.gengoai.hermes.morphology.PennTreeBank;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Set;
import java.util.stream.Collectors;

import static com.gengoai.LogUtils.logFine;
import static com.gengoai.hermes.ml.feature.Features.*;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.lenientContext;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.strictContext;

@Log
public class PhraseChunkTrainer extends IOBTaggerTrainer {

   public PhraseChunkTrainer() {
      super(Types.PHRASE_CHUNK);
   }

   @Override
   protected void addInputs(HStringDataSetGenerator.Builder builder) {
      builder.tokenSequence(Datum.DEFAULT_INPUT,
                            Featurizer.chain(LowerCaseWord, PartOfSpeech, WordAndClass)
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
                                                  ));
   }

   @Override
   public DataSet createDataset(@NonNull DocumentCollection data) {
      Stopwatch sw = Stopwatch.createStarted();
      DataSet dataset = data.update("RemovePartOfSpeech", d -> d.setUncompleted(Types.PART_OF_SPEECH))
                            .annotate(Types.PART_OF_SPEECH)
                            .asDataSet(getExampleGenerator());
      sw.stop();
      logFine(log, "Took {0} to create dataset.", sw);
      return dataset;
   }

   @Override
   protected Model createSequenceLabeler(FitParameters<?> parameters) {
      return new Crf(Cast.<Crf.Parameters>as(parameters));
   }

   @Override
   public FitParameters<?> getFitParameters() {
      return new Crf.Parameters()
            .update(p -> {
               p.minFeatureFreq.set(5);
            });
   }

   @Override
   protected Set<String> getValidTags() {
      return com.gengoai.hermes.morphology.PartOfSpeech.values().stream()
                                                       .filter(com.gengoai.hermes.morphology.PartOfSpeech::isPhraseTag)
                                                       .filter(t -> !t.isInstance(PennTreeBank.INTJ,
                                                                                  PennTreeBank.LST,
                                                                                  PennTreeBank.UCP))
                                                       .map(com.gengoai.hermes.morphology.PartOfSpeech::tag)
                                                       .collect(Collectors.toSet());
   }
}//END OF PhraseChunkTrainer
