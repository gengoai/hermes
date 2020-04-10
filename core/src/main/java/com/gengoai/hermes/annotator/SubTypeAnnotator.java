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

package com.gengoai.hermes.annotator;

import com.gengoai.hermes.*;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.*;

import static com.gengoai.LogUtils.logSevere;

/**
 * An annotator that provides its annotation by annotating for sub-types. During creation of the annotator
 * "non-overlapping" mode can specified which will not allow overlapping sub-type annotations. The best annotation is
 * determined first on confidence of annotation and then on number of tokens matched. This behavior can be overidden in
 * child classes by overriding the protected {@link #compare(Annotation, Annotation)} method.
 *
 * @author David B. Bracewell
 */
@Log
public class SubTypeAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;
   private final AnnotationType annotationType;
   private final boolean nonOverlapping;
   private final Set<AnnotationType> subTypes;

   /**
    * Instantiates a new SubTypeAnnotator.
    *
    * @param annotationType the annotation type being provided
    * @param nonOverlapping True - if the sub-types should not overlap one another where the best annotation is picked
    *                       using longest match / highest probability. False allow overlapping annotations.
    * @param subTypes       the collection of AnnotationType representing the sub-types to annotate.
    * @throws IllegalArgumentException - If one or more of the sub types is not an instance of annotationType
    */
   public SubTypeAnnotator(@NonNull AnnotationType annotationType,
                           boolean nonOverlapping,
                           @NonNull Collection<AnnotationType> subTypes) {

      for(AnnotationType subType : subTypes) {
         if(!subType.isInstance(annotationType)) {
            logSevere(log, "{0} is not an instance of {1}", subType.label(), annotationType.name());
            throw new IllegalArgumentException(subType.label() + " is not a sub type of " + annotationType.name());
         }
      }
      this.annotationType = annotationType;
      this.subTypes = new HashSet<>(subTypes);
      this.nonOverlapping = nonOverlapping;
   }

   /**
    * Instantiates a new SubTypeAnnotator where overlapping annotations is not allowed..
    *
    * @param annotationType the annotation type being provided
    * @param subTypes       the collection of AnnotationType representing the sub-types to annotate.
    * @throws IllegalArgumentException - If one or more of the sub types is not an instance of annotationType
    */
   public SubTypeAnnotator(@NonNull AnnotationType annotationType,
                           @NonNull Collection<AnnotationType> subTypes) {
      this(annotationType, true, subTypes);
   }

   @Override
   protected final void annotateImpl(Document document) {
      subTypes.forEach(document::annotate);
      if(nonOverlapping) {
         List<Annotation> annotations = getAnnotations(document);
         for(Annotation a : annotations) {
            //Make sure the annotation is still on the document
            if(!document.contains(a)) {
               continue;
            }
            //Go through all overlapping annotations
            for(Annotation a2 : getAnnotations(a)) {
               //Ignore itself
               if(a == a2) {
                  continue;
               }
               //Remove one
               if(a == compare(a, a2)) {
                  document.remove(a2);
               } else {
                  //Removed itself so stop processing it
                  document.remove(a);
                  break;
               }
            }
         }
      }
   }

   protected Annotation compare(Annotation a1, Annotation a2) {
      if(a1 == null) {
         return a2;
      }
      if(a2 == null) {
         return a1;
      }

      double a1Confidence = a1.attribute(Types.CONFIDENCE, 1.0);
      double a2Confidence = a2.attribute(Types.CONFIDENCE, 1.0);

      if(a1Confidence > a2Confidence) {
         return a1;
      } else if(a2Confidence > a1Confidence) {
         return a2;
      } else if(a1.tokenLength() > a2.tokenLength()) {
         return a1;
      } else if(a2.tokenLength() > a1.tokenLength()) {
         return a2;
      }

      return a1;
   }

   private List<Annotation> getAnnotations(HString fragment) {
      List<Annotation> annotations = new ArrayList<>();
      for(AnnotationType subType : subTypes) {
         annotations.addAll(fragment.annotations(subType));
      }
      return annotations;
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(annotationType);
   }

}//END OF SubTypeAnnotator
