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

package com.gengoai.hermes;

import com.gengoai.collection.Iterators;
import com.gengoai.collection.tree.IntervalTree;

/**
 * <p>Annotation tree using a Red-Black backed Interval tree.</p>
 *
 * @author David B. Bracewell
 */
class AnnotationTree extends IntervalTree<Annotation> {
   private static final long serialVersionUID = 1L;

   /**
    * Ceiling annotation.
    *
    * @param annotation the annotation
    * @param type       the type
    * @return the annotation
    */
   public Annotation ceiling(Annotation annotation, AnnotationType type) {
      if(annotation == null || type == null) {
         return Fragments.detachedEmptyAnnotation();
      }
      return Iterators.first(Iterators.filter(ceilingIterator(annotation),
                                              ann -> ann.getType().isInstance(type) && ann != annotation))
                      .orElse(Fragments.detachedEmptyAnnotation());
//      Iterator<Annotation> itr = Iterators.filter(new NodeIterator<>(root, annotation.end(), Integer.MAX_VALUE, true),
//                                                  ann -> ann.getType().isInstance(type));
//      for (Annotation a : Iterables.asIterable(itr)) {
//         if (a.isInstance(type) && a != annotation) {
//            return a;
//         }
//      }
//      return Fragments.detachedEmptyAnnotation();
   }

   /**
    * Floor annotation.
    *
    * @param annotation the annotation
    * @param type       the type
    * @return the annotation
    */
   public Annotation floor(Annotation annotation, AnnotationType type) {
      if(annotation == null || type == null) {
         return Fragments.detachedEmptyAnnotation();
      }
      return Iterators.first(Iterators.filter(floorIterator(annotation),
                                              ann -> ann.getType().isInstance(type) &&
                                                    ann != annotation &&
                                                    !ann.overlaps(annotation)))
                      .orElse(Fragments.detachedEmptyAnnotation());
   }

}//END OF AnnotationTree
