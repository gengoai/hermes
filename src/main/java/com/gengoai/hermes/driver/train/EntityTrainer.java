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

package com.gengoai.hermes.driver.train;

import com.gengoai.Stopwatch;
import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.Featurizer;
import com.gengoai.apollo.ml.FitParameters;
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.ml.sequence.Crf;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.BaseWordCategorization;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.ml.BIOLabelMaker;
import com.gengoai.hermes.ml.BIOTrainer;

import java.io.IOException;

import static com.gengoai.hermes.ml.feature.Features.*;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.lenientContext;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.strictContext;

/**
 * The type Entity trainer.
 *
 * @author David B. Bracewell
 */
public class EntityTrainer extends BIOTrainer {
   private static final long serialVersionUID = 1L;

   public EntityTrainer() {
      super("EntityTrainer", Types.ML_ENTITY);
      minFeatureCount = 10;
   }

   public static void main(String[] args) throws Exception {
      new EntityTrainer().run(args);
   }

   @Override
   protected ExampleDataset getDataset(FeatureExtractor<HString> featurizer) throws IOException {
      Stopwatch read = Stopwatch.createStarted();
      Corpus c = Corpus.read(corpusSpecification)
                       .update(BaseWordCategorization.INSTANCE::categorize);
      read.stop();
      logInfo("Completed reading corpus in: {0}", read);
      if(required().length > 0) {
         c.annotate(required());
      }
      return c.asSequenceDataset(s -> {
         s.featureExtractor(getFeaturizer());
         s.labelGenerator(new BIOLabelMaker(trainingAnnotation, validTags()));
      });
   }

   @Override
   protected FitParameters<?> getFitParameters() {
      return new Crf.Parameters()
            .verbose.set(true)
            .maxIterations.set(500)
            .minFeatureFreq.set(5);
   }

   @Override
   protected FeatureExtractor<HString> getFeaturizer() {
      return Featurizer.chain(LowerCaseWord,
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
                                    strictContext(LowerCaseWord, 0, LowerCaseWord, +1, LowerCaseWord, +2),
                                    strictContext(LowerCaseWord, 0, PartOfSpeech, +1),
                                    strictContext(IsPlace, -1, IsPunctuation, 0, IsPlace, +1),
                                    strictContext(IsTime, -1, IsPunctuation, 0, IsTime, +1));
   }


   @Override
   protected SequenceLabeler getLearner() {
      return new Crf(getPreprocessors());
   }


//   @Override
//   protected Set<String> validTags() {
//      return hashSetOf("PERSON", "LOCATION", "ORGANIZATION", "CARDINAL", "ORDINAL");
//   }

   @Override
   protected AnnotatableType[] required() {
      return new AnnotatableType[]{Types.TOKEN, Types.SENTENCE};
   }


}//END OF EntityTrainer
