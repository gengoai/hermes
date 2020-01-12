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
import com.gengoai.apollo.ml.data.Dataset;
import com.gengoai.apollo.ml.sequence.MalletCRF;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.BaseWordCategorization;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.ml.BIOLabelMaker;
import com.gengoai.hermes.ml.BIOTrainer;
import com.gengoai.hermes.ml.feature.AffixFeaturizer;
import com.gengoai.hermes.ml.feature.Features;

import java.io.IOException;

import static com.gengoai.hermes.corpus.io.CorpusParameters.IN_MEMORY;

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
   protected Dataset getDataset(FeatureExtractor<HString> featurizer) throws IOException {
      Stopwatch read = Stopwatch.createStarted();
      Corpus c = Corpus.reader(corpusFormat)
                       .option(IN_MEMORY, true)
                       .read(corpus)
                       .update(BaseWordCategorization.INSTANCE::categorize);
      read.stop();
      logInfo("Completed reading corpus in: {0}", read);
      if (required().length > 0) {
         c.annotate(required());
      }
      return c.asSequenceDataset(s -> {
         s.featureExtractor(getFeaturizer());
         s.labelGenerator(new BIOLabelMaker(trainingAnnotation, validTags()));
      });
   }

   @Override
   protected FeatureExtractor<HString> getFeaturizer() {
      return Featurizer.chain(Features.Word,
                              Features.IsLanguageName,
                              Features.WordAndClass,
                              Features.PartOfSpeech,
                              Features.Categories,
                              new AffixFeaturizer(3, 3))
                       .withContext("WORD[-1]",
                                    "POS[-1]",
                                    "~WORD[-1]|WORD[0]",
                                    "~WORD[-2]",
                                    "~POS[-1]",
                                    "~WORD[-2]|WORD[-1]",
                                    "~POS[-2]|POS[-1]",
                                    "~WORD[-2]|WORD[-1]|WORD[0]",
                                    "WORD[+1]",
                                    "POS[+1]",
                                    "~WORD[0]|WORD[+1]",
                                    "~WORD[+2]",
                                    "~POS[+2]",
                                    "~WORD[+1]|WORD[+2]",
                                    "~POS[+1]|POS[+2]",
                                    "~WORD[0]|WORD[+1]|WORD[+2]"
                                   );
   }


   @Override
   protected SequenceLabeler getLearner() {
      return new MalletCRF(getPreprocessors());
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
