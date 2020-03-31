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
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.ml.sequence.Crf;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.morphology.POS;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.BIOLabelMaker;
import com.gengoai.hermes.ml.BIOTrainer;

import java.io.IOException;

import static com.gengoai.hermes.ml.feature.Features.LowerCaseWord;
import static com.gengoai.hermes.ml.feature.Features.PartOfSpeech;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.lenientContext;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.strictContext;

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
   protected ExampleDataset getDataset(FeatureExtractor<HString> featurizer) throws IOException {
      return DocumentCollection.create(data)
                               .update(d -> d.setUncompleted(Types.PART_OF_SPEECH))
                               .annotate(Types.PART_OF_SPEECH)
                               .update(d -> d.annotations(Types.PHRASE_CHUNK).forEach(annotation -> {
                                  if(annotation.attribute(Types.PART_OF_SPEECH).isInstance(POS.INTJ,
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
      return Featurizer.chain(LowerCaseWord, PartOfSpeech)
                       .withContext(lenientContext(LowerCaseWord, -1),
                                    strictContext(LowerCaseWord, -1, LowerCaseWord, 0),
                                    strictContext(PartOfSpeech, -1),
                                    strictContext(PartOfSpeech, -1, PartOfSpeech, 0),
                                    strictContext(PartOfSpeech, -1, LowerCaseWord, 0));
   }

   @Override
   protected SequenceLabeler getLearner() {
      return new Crf(getPreprocessors());
   }

}// END OF PhraseChunkTrainer
