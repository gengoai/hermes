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

import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.Featurizer;
import com.gengoai.apollo.ml.data.Dataset;
import com.gengoai.apollo.ml.sequence.CrfSuiteLoader;
import com.gengoai.apollo.ml.sequence.MalletCRF;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.POS;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.ml.BIOLabelMaker;
import com.gengoai.hermes.ml.BIOTrainer;
import com.gengoai.hermes.ml.feature.Features;

import java.io.IOException;

/**
 * The type Phrase chunk trainer.
 *
 * @author David B. Bracewell
 */
public class PhraseChunkTrainer extends BIOTrainer {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Phrase chunk trainer.
    */
   public PhraseChunkTrainer() {
      super("PhraseChunkTrainer", Types.PHRASE_CHUNK);
   }

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    */
   public static void main(String[] args) {
      new PhraseChunkTrainer().run(args);
   }

   @Override
   protected Dataset getDataset(FeatureExtractor<HString> featurizer) throws IOException {
      return Corpus.reader(corpusFormat)
                   .read(corpus)
                   .update(d -> d.setUncompleted(Types.PART_OF_SPEECH))
                   .annotate(Types.PART_OF_SPEECH)
                   .update(d -> d.annotations(Types.PHRASE_CHUNK).forEach(annotation -> {
                      if (annotation.attribute(Types.PART_OF_SPEECH).isInstance(POS.INTJ,
                                                                                POS.LST,
                                                                                POS.UCP)) {
                         d.remove(annotation);
                      }
                   }))
                   .asSequenceDataset(def -> {
                      def.labelGenerator(new BIOLabelMaker(annotationType));
                      def.featureExtractor(featurizer);
                   });
   }

   @Override
   protected FeatureExtractor<HString> getFeaturizer() {
      return Featurizer.chain(Features.LowerCaseWord,
                              Features.PartOfSpeech)
                       .withContext("WORD[-1]",
                                    "~WORD[-1]|WORD[0]",
                                    "POS[-1]",
                                    "~POS[-1]|POS[0]",
                                    "~WORD[-2]",
                                    "~WORD[-2]|WORD[-1]",
                                    "~WORD[-2]|WORD[-1]|WORD[0]",
                                    "~POS[-2]",
                                    "~POS[-2]|POS[-1]",
                                    "~POS[-2]|POS[-1]|POS[0]"
                                   );
   }

   @Override
   protected SequenceLabeler getLearner() {
      return new MalletCRF(getPreprocessors());
   }

   @Override
   public void setup() throws Exception {
      CrfSuiteLoader.INSTANCE.load();
   }

}// END OF PhraseChunkTrainer
