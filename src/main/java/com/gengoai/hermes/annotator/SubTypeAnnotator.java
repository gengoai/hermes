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
import com.gengoai.logging.Logger;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;

/**
 * The type Sub type annotator.
 *
 * @author David B. Bracewell
 */
public class SubTypeAnnotator implements Annotator, Serializable {
   private static final Logger log = Logger.getLogger(SubTypeAnnotator.class);
   private static final long serialVersionUID = 1L;
   private final AnnotationType annotationType;
   private final boolean nonOverlapping;
   private final Set<AnnotationType> subTypes;

   /**
    * Instantiates a new Sub type annotator.
    *
    * @param annotationType the annotation type
    * @param nonOverlapping the non overlapping
    * @param subTypes       the sub types
    * @throws IllegalArgumentException - If one or more of the sub types is not an instance of annotationType
    */
   public SubTypeAnnotator(AnnotationType annotationType, boolean nonOverlapping, Collection<AnnotationType> subTypes) {
      for (AnnotationType subType : subTypes) {
         if (!subType.isInstance(annotationType)) {
            log.severe("{0} is not an instance of {1}", subType.label(), annotationType.name());
            throw new IllegalArgumentException(subType.label() + " is not a sub type of " + annotationType.name());
         }
      }
      this.annotationType = annotationType;
      this.subTypes = new HashSet<>(subTypes);
      this.nonOverlapping = nonOverlapping;
   }

   /**
    * Instantiates a new Sub type annotator.
    *
    * @param annotationType the annotation type
    * @param subTypes       the sub types
    */
   public SubTypeAnnotator(@NonNull AnnotationType annotationType, @NonNull Collection<AnnotationType> subTypes) {
      this(annotationType, true, subTypes);
   }

   @Override
   public final void annotate(Document document) {
      subTypes.forEach(document::annotate);
      if (nonOverlapping) {
         List<Annotation> annotations = getAnnotations(document);
         for (Annotation a : annotations) {
            //Make sure the annotation is still on the document
            if (!document.contains(a)) {
               continue;
            }

            //Go through all overlapping annotations
            for (Annotation a2 : getAnnotations(a)) {
               //Ignore itself
               if (a == a2) {
                  continue;
               }

               //Remove one
               if (a == compare(a, a2)) {
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

   private Annotation compare(Annotation a1, Annotation a2) {
      if (a1 == null) {
         return a2;
      }
      if (a2 == null) {
         return a1;
      }

      double a1Confidence = a1.attribute(Types.CONFIDENCE, 1.0);
      double a2Confidence = a2.attribute(Types.CONFIDENCE, 1.0);

      if (a1Confidence > a2Confidence) {
         return a1;
      } else if (a2Confidence > a1Confidence) {
         return a2;
      } else if (a1.tokenLength() > a2.tokenLength()) {
         return a1;
      } else if (a2.tokenLength() > a1.tokenLength()) {
         return a2;
      }

      return a1;
   }

   private List<Annotation> getAnnotations(HString fragment) {
      List<Annotation> annotations = new ArrayList<>();
      for (AnnotationType subType : subTypes) {
         annotations.addAll(fragment.annotations(subType));
      }
      return annotations;
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(annotationType);
   }

}//END OF SubTypeAnnotator
