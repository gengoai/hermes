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

package com.gengoai.hermes;

import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Stream;

class DefaultAnnotationImpl extends BaseHString implements Annotation {
   private static final long serialVersionUID = 1L;

   final Set<Relation> incomingRelations = new HashSet<>();
   final Set<Relation> outgoingRelations = new HashSet<>();
   private final AnnotationType annotationType;
   private final Document owner;
   private long id = DETACHED_ID;
   private volatile transient Annotation[] tokens;

   /**
    * Instantiates a new Annotation.
    *
    * @param owner          the document that owns this annotation
    * @param annotationType The type of annotation
    * @param start          the character starting offset in the document
    * @param end            the character ending offset in the document
    */
   public DefaultAnnotationImpl(@NonNull Document owner, @NonNull AnnotationType annotationType, int start, int end) {
      super(start, end);
      Validation.checkArgument(start <= end,
                               "Annotations must have a start character index that is less than or equal to the ending index.");
      this.annotationType = annotationType;
      this.owner = owner;
   }


   /**
    * Instantiates a new Annotation.
    *
    * @param string         the string that this annotation will encompass
    * @param annotationType the annotation type
    */
   public DefaultAnnotationImpl(@NonNull HString string, @NonNull AnnotationType annotationType) {
      super(string.start(), string.end());
      this.owner = string.document();
      this.annotationType = annotationType;
   }

   /**
    * Instantiates a new Annotation.
    */
   protected DefaultAnnotationImpl() {
      super(0, 0);
      this.owner = null;
      this.annotationType = AnnotationType.ROOT;
   }


   /**
    * Instantiates a new orphaned Annotation.
    *
    * @param type  The type of annotation
    * @param start the character starting offset in the document
    * @param end   the character ending offset in the document
    */
   protected DefaultAnnotationImpl(@NonNull AnnotationType type, int start, int end) {
      super(start, end);
      this.owner = null;
      this.annotationType = type == null ? AnnotationType.ROOT : type;
   }

   @Override
   public void add(@NonNull Relation relation) {
      outgoingRelations.add(relation);
      if (!isDetached()) {
         relation.getTarget(this)
                 .ifPresent(
                    a -> Cast.<DefaultAnnotationImpl>as(a).incomingRelations.add(
                       new Relation(relation.getType(), relation.getValue(), getId())));
      }
   }

   @Override
   public Document document() {
      return owner;
   }

   @Override
   public long getId() {
      return id;
   }

   @Override
   public void setId(long id) {
      this.id = id;
   }

   @Override
   public final AnnotationType getType() {
      return annotationType;
   }

   @Override
   public Stream<Relation> incomingRelationStream(boolean includeSubAnnotations) {
      Stream<Relation> relationStream = incomingRelations.stream();
      if (this.getType() != Types.TOKEN && includeSubAnnotations) {
         relationStream = Stream.concat(relationStream,
                                        annotations().stream()
                                                     .filter(a -> a != this)
                                                     .flatMap(a -> a.incomingRelationStream(false))
                                                     .filter(rel -> rel.getTarget(document())
                                                                       .map(aa -> !aa.overlaps(this))
                                                                       .orElse(false)))
                                .distinct();
      }
      return relationStream;
   }

   @Override
   public Stream<Relation> outgoingRelationStream(boolean includeSubAnnotations) {
      Stream<Relation> relationStream = outgoingRelations.stream();
      if (this.getType() != Types.TOKEN && includeSubAnnotations) {
         relationStream = Stream.concat(relationStream,
                                        annotations().stream()
                                                     .filter(a -> a != this)
                                                     .flatMap(a -> a.outgoingRelationStream(false))
                                                     .filter(rel -> rel.getTarget(document())
                                                                       .map(aa -> !aa.overlaps(this))
                                                                       .orElse(false)))
                                .distinct();
      }
      return relationStream;
   }

   @Override
   public void removeRelation(@NonNull Relation relation) {
      if (outgoingRelations.remove(relation)) {
         relation.getTarget(this)
                 .ifPresent(a -> a.removeRelation(new Relation(relation.getType(), relation.getValue(), getId())));
      }
   }

   @Override
   public List<Annotation> tokens() {
      if (tokens == null) {
         synchronized (this) {
            if (tokens == null) {
               List<Annotation> tokenList = super.tokens();
               if (!tokenList.isEmpty()) {
                  tokens = tokenList.toArray(new Annotation[0]);
               }
            }
         }
      }
      return tokens == null ? Collections.emptyList() : Arrays.asList(tokens);
   }
}//END OF DefaultAnnotationImpl
