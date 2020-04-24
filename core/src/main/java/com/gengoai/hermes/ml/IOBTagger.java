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

import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>
 * Creates annotations based on the IOB tag output of an underlying model.
 * </p>
 *
 * @author David B. Bracewell
 */
public class IOBTagger extends SequenceTagger {
   private static final long serialVersionUID = 1L;
   /**
    * The Annotation type.
    */
   @Getter
   final AnnotationType annotationType;

   /**
    * Instantiates a new IOBTagger
    *
    * @param inputGenerator the generator to convert HString into input for the model
    * @param annotationType the type of annotation to add during tagging.
    * @param labeler        the model to use to perform the IOB tagging
    * @param version        the version of the model to be used as part of the provider of the annotation.
    */
   public IOBTagger(@NonNull HStringDataSetGenerator inputGenerator,
                    @NonNull AnnotationType annotationType,
                    @NonNull Model labeler,
                    @NonNull String version) {
      super(inputGenerator, labeler, version);
      this.annotationType = annotationType;
   }

   /**
    * Tags the given Sentence adding annotations of a predefined type and setting the tag and confidence based on the
    * underlying Model using the IOB tag scheme.
    *
    * @param sentence the sentence to tag
    */
   @Override
   public void tag(@NonNull Annotation sentence) {
      Sequence<?> result = labeler.transform(inputGenerator.apply(sentence))
                                  .get(outputName)
                                  .asSequence();
      for(int i = 0; i < sentence.tokenLength(); ) {
         String label = result.get(i).asVariable().getName();
         double score = result.get(i).asVariable().getValue();
         if(label.equals("O")) {
            i++;
         } else {
            Annotation start = sentence.tokenAt(i);
            String type = label.substring(2);
            double p = score;
            i++;
            final String insideType = "I-" + type;
            while(i < sentence.tokenLength() && result.get(i).asVariable().getName().equals(insideType)) {
               p *= score;
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

}// END OF IOBTagger
