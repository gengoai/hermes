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

import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * The type Bio label maker.
 *
 * @author David B. Bracewell
 */
public class IOBLabelMaker implements SerializableFunction<HString, Object> {
   private static final long serialVersionUID = 1L;
   private final AnnotationType annotationType;
   private final Set<String> validTags;

   /**
    * Instantiates a new Bio label maker.
    *
    * @param annotationType the annotation type
    */
   public IOBLabelMaker(@NonNull AnnotationType annotationType) {
      this.annotationType = annotationType;
      this.validTags = Collections.emptySet();
   }

   /**
    * Instantiates a new Bio label maker.
    *
    * @param annotationType the annotation type
    * @param validTags      the valid tags
    */
   public IOBLabelMaker(@NonNull AnnotationType annotationType, Set<String> validTags) {
      this.annotationType = annotationType;
      this.validTags = validTags;
   }

   @Override
   public Object apply(HString annotation) {
      Optional<Annotation> target = annotation.annotations(annotationType).stream().findFirst();
      return target.map(a -> {
         String tag = a.getTag().name();
         if(Strings.isNotNullOrBlank(tag) && (validTags.isEmpty() || validTags.contains(tag))) {
            if(a.start() == annotation.start()) {
               return "B-" + a.getTag().name();
            } else {
               return "I-" + a.getTag().name();
            }
         } else {
            return "O";
         }
      }).orElse("O");
   }

}//END OF IOBLabelMaker
