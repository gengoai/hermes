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

import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.LabeledSequence;
import com.gengoai.apollo.ml.sequence.Labeling;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.PartOfSpeech;
import lombok.NonNull;

/**
 * The type Pos tagger.
 *
 * @author David B. Bracewell
 */
public class POSTagger extends SequenceTagger {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Pos tagger.
    *
    * @param featurizer the featurizer
    * @param labeler    the labeler
    */
   public POSTagger(@NonNull FeatureExtractor<HString> featurizer,
                    @NonNull SequenceLabeler labeler,
                    @NonNull String version) {
      super(featurizer, labeler, version);
   }

   protected void addPOS(Annotation sentence, Labeling result) {
      for(int i = 0; i < sentence.tokenLength(); i++) {
         Annotation token = sentence.tokenAt(i);
         token.put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(result.getLabel(i)));
      }
   }

   @Override
   public final void tag(Annotation sentence) {
      addPOS(sentence, labeler.label(featurizer.extractExample(new LabeledSequence<>(sentence.tokens()))));
   }

}// END OF POSTagger
