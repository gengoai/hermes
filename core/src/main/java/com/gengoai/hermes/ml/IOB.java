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

import com.gengoai.apollo.ml.feature.ObservationExtractor;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class IOB {

   public static void decode(@NonNull HString sentence,
                             @NonNull Sequence<?> result,
                             @NonNull AnnotationType annotationType) {
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
            while(i < sentence.tokenLength() &&
                  result.get(i).asVariable().getName().equals(insideType)) {
               p *= score;
               i++;
            }
            Annotation end = sentence.tokenAt(i - 1);
            HString span = start.union(end);
            Annotation annotation = sentence.document()
                                            .annotationBuilder(annotationType)
                                            .bounds(span)
                                            .createAttached();
            annotation.put(annotationType.getTagAttribute(),
                           annotationType.getTagAttribute().decode(type));
            annotation.put(Types.CONFIDENCE, p);
         }
      }
   }

   public static ObservationExtractor<HString> encoder(@NonNull final AnnotationType annotationType,
                                                       @NonNull final Set<String> validTags) {
      return new IOBEncoder(annotationType, validTags);
   }

   public static ObservationExtractor<HString> encoder(@NonNull final AnnotationType annotationType) {
      return new IOBEncoder(annotationType, Collections.emptySet());
   }

   private IOB() {
      throw new IllegalAccessError();
   }

   private static class IOBEncoder implements ObservationExtractor<HString> {
      private static final long serialVersionUID = 1L;
      private final AnnotationType annotationType;
      private final Set<String> validTags;

      private IOBEncoder(AnnotationType annotationType, Set<String> validTags) {
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
   }

}//END OF IOBUtils
