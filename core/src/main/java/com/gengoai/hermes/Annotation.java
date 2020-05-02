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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p>
 * An annotation is an {@link HString} that associates an {@link AnnotationType}, e.g. token, sentence, named entity, to
 * a specific span of characters in a document, which may include the entire document. Annotations typically have
 * attributes, e.g. part-of-speech, entity type, etc, and relations, e.g. dependency and co-reference, associated with
 * them. Annotations are assigned a <b>long</b> id when attached to a document, which uniquely identifies it within that
 * document.
 * </p>
 *
 * <p> Commonly, annotations have an associated <code>Tag</code> attribute which acts as label. Examples of tags
 * include part-of-speech and entity type. Tags can be retrieved using the {@link #getTag()} method. Annotation types
 * specify the tag attribute when it is created. Note that an annotation can have multiple attributes that are tags, but
 * the <code>getTag</code> method returns the primary tag as defined by the annotation type.</p>
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(as = DefaultAnnotationImpl.class)
public interface Annotation extends HString {
   /**
    * The ID associated with a detached annotation
    */
   long DETACHED_ID = Long.MIN_VALUE;

   /**
    * Attaches the annotation the document is contained in
    */
   default void attach() {
      if(isDetached()) {
         Validation.checkState(document() != null, "Attempting to attach an annotation that has no owning document.");
         document().attach(this);
      }
   }

   @Override
   default <T> T attribute(@NonNull AttributeType<T> attributeType) {
      if(attributeType == Types.TAG) {
         return Cast.as(attributeMap().get(getType().getTagAttribute()));
      }
      return attributeMap().get(attributeType);
   }

   @Override
   default Tuple2<String, Annotation> dependency() {
      return outgoingRelationStream(true)
            .filter(r -> r.getType().equals(Types.DEPENDENCY))
            .filter(r -> !r.getTarget(this).isEmpty())
            .filter(r -> !this.overlaps(r.getTarget(this)))
            .map(r -> Tuple2.of(r.getValue(), r.getTarget(this)))
            .findFirst()
            .orElse($(Strings.EMPTY, Fragments.orphanedAnnotation(AnnotationType.ROOT)));
   }

   /**
    * Gets the unique id associated with the annotation.
    *
    * @return the id of the annotation that is unique with in its document or <code>Annotation.DETACHED_ID</code> if the
    * annotation is not attached to the document.
    */
   @JsonProperty("id")
   long getId();

   /**
    * @return The value of the tag attribute for the annotation.
    */
   default Tag getTag() {
      return attribute(getType().getTagAttribute());
   }

   /**
    * Gets the tag, if one, associated with the annotation.
    *
    * @param defaultTag The default tag if one is not on the annotation
    * @return An optional containing the tag if present
    */
   default Tag getTag(@NonNull Tag defaultTag) {
      AttributeType<? extends Tag> tagAttributeType = Cast.as(getType().getTagAttribute());
      Tag out = attribute(tagAttributeType);
      return out == null
             ? defaultTag
             : out;
   }

   /**
    * @return the type of the annotation
    */
   @JsonProperty("type")
   AnnotationType getType();

   /**
    * @return True if the annotation has an tag value set, false otherwise
    */
   default boolean hasTag() {
      return attribute(getType().getTagAttribute()) != null;
   }

   @Override
   default boolean isAnnotation() {
      return true;
   }

   /**
    * @return True if the annotation is detached, False otherwise
    */
   default boolean isDetached() {
      return document() == null || getId() == DETACHED_ID;
   }

   @Override
   default boolean isInstance(@NonNull AnnotationType type) {
      return getType().isInstance(type);
   }

   /**
    * @return The next annotation with the same type as this one or an empty fragment
    */
   default Annotation next() {
      return next(getType());
   }

   /**
    * @return The previous annotation with the same type as this one or an empty fragment
    */
   default Annotation previous() {
      return previous(getType());
   }

   @Override
   default <T> T put(@NonNull AttributeType<T> attributeType, T value) {
      return HString.super.put(attributeType == Types.TAG
                               ? Cast.as(getType().getTagAttribute())
                               : attributeType,
                               value);
   }

   /**
    * Sets the unique id of the annotation.
    *
    * @param id the id
    */
   void setId(long id);

   /**
    * Determines if this annotation's tag is an instance of the given tag.
    *
    * @param tag the tag to check
    * @return True if this annotation's tag is an instance of the given tag.
    */
   default boolean tagIsA(@NonNull Object tag) {
      Tag target = (tag instanceof Val)
                   ? Cast.<Val>as(tag).as(getType().getTagAttribute().getValueType())
                   : getType().getTagAttribute().decode(tag);
      return target != null && getTag() != null && getTag().isInstance(target);
   }

}//END OF Annotation
