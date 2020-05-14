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
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.PartOfSpeech;
import lombok.NonNull;

/**
 * <p>
 * A {@link SequenceTagger} for assigning {@link PartOfSpeech} to tokens.
 * </p>
 *
 * @author David B. Bracewell
 */
public class POSTagger extends BaseHStringMLModel {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new POSTagger.
    *
    * @param inputGenerator the generator to convert HString into input for the model
    * @param labeler        the model to use to perform the part-of-speech tagging
    */
   public POSTagger(@NonNull HStringDataSetGenerator inputGenerator, @NonNull Model labeler) {
      super(labeler, inputGenerator);
   }

   @Override
   protected void onEstimate(HString sentence, Datum result) {
      Sequence<?> sequence = result.get(getOutput()).asSequence();
      for(int i = 0; i < sentence.tokenLength(); i++) {
         Annotation token = sentence.tokenAt(i);
         token.put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(sequence.get(i).asVariable().getName()));
      }
   }

}// END OF POSTagger
