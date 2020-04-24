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

import com.gengoai.apollo.ml.DataSetGenerator;
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
public class POSTagger extends SequenceTagger {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new POSTagger.
    *
    * @param inputGenerator the generator to convert HString into input for the model
    * @param labeler        the model to use to perform the part-of-speech tagging
    * @param version        the version of the model to be used as part of the provider of the part-of-speech.
    */
   public POSTagger(@NonNull DataSetGenerator<HString> inputGenerator,
                    @NonNull Model labeler,
                    @NonNull String version) {
      super(inputGenerator, labeler, version);
   }

   /**
    * Methodology for attaching POS to the token.
    *
    * @param sentence the sentence
    * @param result   the result
    */
   protected void addPOS(Annotation sentence, Sequence<?> result) {
      for(int i = 0; i < sentence.tokenLength(); i++) {
         Annotation token = sentence.tokenAt(i);
         token.put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(result.get(i).asVariable().getName()));
      }
   }

   @Override
   public final void tag(Annotation sentence) {
      addPOS(sentence, labeler.transform(inputGenerator.apply(sentence)).get(outputName).asSequence());
   }

}// END OF POSTagger
