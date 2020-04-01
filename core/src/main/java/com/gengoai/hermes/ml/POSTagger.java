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
import com.gengoai.hermes.morphology.PennTreeBank;
import lombok.NonNull;

import java.util.regex.Pattern;

/**
 * The type Pos tagger.
 *
 * @author David B. Bracewell
 */
public class POSTagger extends SequenceTagger {
   private static final long serialVersionUID = 1L;
   private static final Pattern ORDINAL = Pattern.compile("^\\d+(rd|th|st|nd)$", Pattern.CASE_INSENSITIVE);

   /**
    * Instantiates a new Pos tagger.
    *
    * @param featurizer the featurizer
    * @param labeler    the labeler
    */
   public POSTagger(@NonNull FeatureExtractor<HString> featurizer, @NonNull SequenceLabeler labeler) {
      super(featurizer, labeler);
   }

   @Override
   public void tag(Annotation sentence) {
      LabeledSequence<Annotation> sequenceInput = new LabeledSequence<>(sentence.tokens());
      Labeling result = labeler.label(featurizer.extractExample(sequenceInput));
      for(int i = 0; i < sentence.tokenLength(); i++) {
         Annotation token = sentence.tokenAt(i);
         if((token.contentEqualsIgnoreCase("'s") || token.contentEqualsIgnoreCase("s'")) &&
               token.previous().pos().isNoun()) {
            token.put(Types.PART_OF_SPEECH, PennTreeBank.POS);
         } else if(ORDINAL.matcher(token).matches()) {
            token.put(Types.PART_OF_SPEECH, PennTreeBank.JJ);
         } else if(sentence.tokenAt(i - 1).pos().isPronoun()
               && token.contentEqualsIgnoreCase("like")
         ) {
            token.put(Types.PART_OF_SPEECH, PennTreeBank.VB);
         } else if(sentence.tokenAt(i - 1).pos().isVerb() &&
               sentence.tokenAt(i + 1).contentEqualsIgnoreCase("to") &&
               token.toLowerCase().endsWith("ing")) {
            //Common error of MODAL + GERUND (where GERUND form is commonly a noun) + to => VBG
            token.put(Types.PART_OF_SPEECH, PennTreeBank.VBG);
         } else {
            token.put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(result.getLabel(i)));
         }
      }
   }

}// END OF POSTagger
