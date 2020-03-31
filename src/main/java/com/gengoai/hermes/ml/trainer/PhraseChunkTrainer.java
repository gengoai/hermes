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
import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.Featurizer;
import com.gengoai.apollo.ml.FitParameters;
import com.gengoai.apollo.ml.data.DatasetType;
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.ml.sequence.Crf;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.morphology.POS;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.BIOTaggerTrainer;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.LogUtils.logFine;
import static com.gengoai.hermes.ml.feature.Features.LowerCaseWord;
import static com.gengoai.hermes.ml.feature.Features.PartOfSpeech;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.lenientContext;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.strictContext;

@Log
public class PhraseChunkTrainer extends BIOTaggerTrainer {

   public PhraseChunkTrainer() {
      super(Types.PHRASE_CHUNK);
   }

   @Override
   public ExampleDataset createDataset(@NonNull DocumentCollection data) {
      Stopwatch sw = Stopwatch.createStarted();
      ExampleDataset dataset = data.update(d -> d.setUncompleted(Types.PART_OF_SPEECH))
                                   .annotate(Types.PART_OF_SPEECH)
                                   .asDataset(getSequenceGenerator(), DatasetType.InMemory);
      sw.stop();
      logFine(log, "Took {0} to create dataset.", sw);
      return dataset;
   }

   @Override
   public FeatureExtractor<HString> createFeatureExtractor() {
      return Featurizer.chain(LowerCaseWord, PartOfSpeech)
                       .withContext(lenientContext(LowerCaseWord, -1),
                                    strictContext(LowerCaseWord, -1, LowerCaseWord, 0),
                                    strictContext(PartOfSpeech, -1),
                                    strictContext(PartOfSpeech, -1, PartOfSpeech, 0),
                                    strictContext(PartOfSpeech, -1, LowerCaseWord, 0));
   }

   @Override
   protected SequenceLabeler createSequenceLabeler() {
      return new Crf();
   }

   @Override
   public FitParameters<?> getDefaultFitParameters() {
      return new Crf.Parameters();
   }

   @Override
   protected Set<String> getValidTags() {
      return Stream.of(POS.values())
                   .filter(POS::isPhraseTag)
                   .filter(t -> !t.isInstance(POS.INTJ,
                                              POS.LST,
                                              POS.UCP))
                   .map(POS::label)
                   .collect(Collectors.toSet());
   }
}//END OF PhraseChunkTrainer
