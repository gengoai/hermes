/*
 * (c) 2005 David B. Bracewell
 *
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
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import lombok.NonNull;

/**
 * The type Bio tagger.
 *
 * @author David B. Bracewell
 */
public class BIOTagger extends SequenceTagger {
   private static final long serialVersionUID = 1L;
   /**
    * The Annotation type.
    */
   final AnnotationType annotationType;

   /**
    * Instantiates a new Bio tagger.
    *
    * @param featurizer     the featurizer
    * @param annotationType the annotation type
    * @param labeler        the labeler
    */
   public BIOTagger(@NonNull FeatureExtractor<HString> featurizer,
                    AnnotationType annotationType,
                    @NonNull SequenceLabeler labeler) {
      super(featurizer, labeler);
      this.annotationType = annotationType;
   }

   /**
    * Tag labeling result.
    *
    * @param sentence the sentence
    */
   @Override
   public void tag(Annotation sentence) {
      LabeledSequence<Annotation> sequenceInput = new LabeledSequence<>(sentence.tokens());
      Labeling result = labeler.label(featurizer.extractExample(sequenceInput));
      for(int i = 0; i < sentence.tokenLength(); ) {
         if(result.getLabel(i).equals("O")) {
            i++;
         } else {
            Annotation start = sentence.tokenAt(i);
            String type = result.getLabel(i).substring(2);
            double p = result.getScore(i);
            i++;
            final String insideType = "I-" + type;
            while(i < sentence.tokenLength() && result.getLabel(i).equals(insideType)) {
               p *= result.getScore(i);
               i++;
            }
            Annotation end = sentence.tokenAt(i - 1);
            HString span = start.union(end);
            Annotation entity = sentence.document()
                                        .annotationBuilder(annotationType)
                                        .bounds(span)
                                        .createAttached();
            entity.put(annotationType.getTagAttribute(),
                       annotationType.getTagAttribute().decode(type));
            entity.put(Types.CONFIDENCE, p);
         }
      }
   }

}// END OF BIOTagger