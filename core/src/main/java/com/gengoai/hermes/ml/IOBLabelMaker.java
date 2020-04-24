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

import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.feature.ObservationExtractor;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * Creates <code>IOB</code> tags for a given annotation type over a sequence. For example, given an sentence:
 * </p>
 * <center>
 * <code>The man on the hill.</code>
 * </center>
 * <p>with the Phrase Chunks:</p>
 * <center>
 * <code>NP[The man] on NP[the hill].</code>
 * </center>
 * <p>we would generate the following Observation Sequence:</p>
 * <center>
 * <code>[ B-NP, I-NP, O, B-NP, I-NP ]</code>
 * </center>
 *
 * @author David B. Bracewell
 */
public class IOBLabelMaker implements ObservationExtractor<HString> {
   private static final long serialVersionUID = 1L;
   private final AnnotationType annotationType;
   private final Set<String> validTags;

   /**
    * Instantiates a new IOBLabelMaker.
    *
    * @param annotationType the annotation type whose tag will be used to generate the IOB tags
    */
   public IOBLabelMaker(@NonNull AnnotationType annotationType) {
      this.annotationType = annotationType;
      this.validTags = Collections.emptySet();
   }

   /**
    * Instantiates a new IOBLabelMaker.
    *
    * @param annotationType the annotation type whose tag will be used to generate the IOB tags
    * @param validTags      the valid set of tags. Used for filtering out unwanted tags.
    */
   public IOBLabelMaker(@NonNull AnnotationType annotationType, @NonNull Set<String> validTags) {
      this.annotationType = annotationType;
      this.validTags = validTags;
   }

   @Override
   public Observation extractObservation(@NonNull HString annotation) {
      VariableSequence sequence = new VariableSequence();
      for(Annotation token : annotation.tokens()) {
         Optional<Annotation> target = token.annotations(annotationType).stream().findFirst();
         String label = target.map(a -> {
            String tag = a.getTag().name();
            if(Strings.isNotNullOrBlank(tag) && (validTags.isEmpty() || validTags.contains(tag))) {
               if(a.start() == token.start()) {
                  return "B-" + a.getTag().name();
               } else {
                  return "I-" + a.getTag().name();
               }
            } else {
               return "O";
            }
         }).orElse("O");
         sequence.add(Variable.binary(label));
      }
      return sequence;
   }

}//END OF IOBLabelMaker
