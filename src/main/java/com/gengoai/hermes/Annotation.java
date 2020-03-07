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

import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.util.Objects;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p> Annotations are specialized {@link HString}s representing linguistic overlays on a document that associate a
 * type, e.g. token, sentence, named entity, and a set of attributes, e.g. part of speech and entity type, to  a
 * specific  span of a document, which may include the entire document. Annotation type information is defined via the
 * {@link AnnotationType} class. </p>
 *
 * <p> Annotations provide many convenience methods to make navigating the  annotation and relation graph easier. The
 * {@link #next()}, {@link #next(AnnotationType)}, {@link #previous()}, and {@link #previous(AnnotationType)} methods
 * facilitate retrieval of the next and previous annotation of the same or different type. The <code>sources</code>,
 * <code>targets</code>, and {@link #outgoingRelations(RelationType, boolean)} methods allow retrieval of the
 * annotations connected via relations and the relations (edges) themeselves. </p>
 *
 * <p> Commonly, annotations have an associated <code>Tag</code> attribute which acts as label. Examples of tags
 * include part-of-speech and entity type. Tags can be retrieved using the {@link #getTag()} method. Annotation types
 * specify the attribute that represents the tag of an annotation of its type (in some cases annotations may have
 * multiple tags and this definition allows the primary tag to specified). If no tag is specified, a default attribute
 * of
 * <code>TAG</code> is used. </p>
 *
 * @author David B. Bracewell
 */
public interface Annotation extends HString {
   /**
    * The ID associated with a detached annotation
    */
   long DETACHED_ID = Long.MIN_VALUE;

   default void attach() {
      if(isDetached()) {
         document().attach(this);
      }
   }

   @Override
   default Tuple2<String, Annotation> dependency() {
      return outgoingRelationStream(true)
            .filter(r -> r.getType() == Types.DEPENDENCY)
            .filter(r -> r.getTarget(this).isPresent())
            .filter(r -> !this.overlaps(r.getTarget(this).orElse(null)))
            .map(r -> Tuple2.of(r.getValue(), r.getTarget(this).orElse(null)))
            .findFirst()
            .orElse($(Strings.EMPTY, Fragments.emptyAnnotation(document())));
   }

   @Override
   default <T> T attribute(@NonNull AttributeType<T> attributeType) {
      if( attributeType == Types.TAG ){
         return Cast.as(attributeMap().get(getType().getTagAttribute()));
      }
      return attributeMap().get(attributeType);
   }


   /**
    * Gets the unique id associated with the annotation.
    *
    * @return the id of the annotation that is unique with in its document or <code>Annotation.DETACHED_ID</code> if the
    * annotation is not attached to the document.
    */
   long getId();

   /**
    * Sets the unique id of the annotation.
    *
    * @param id the id
    */
   void setId(long id);

   /**
    * <p> Gets the tag, if one, associated with the annotation. The tag attribute is defined for an annotation type
    * using the <code>tag</code> configuration property, e.g. <code>Annotation.TYPE.tag=fully.qualified.tag.implementation</code>.
    * Tags must implement the <code>Tag</code> interface. If no tag type is defined, the <code>Attrs.TAG</code>
    * attribute will be retrieved. </p>
    *
    * @return An optional containing the tag if present
    */
   default Tag getTag() {
      AttributeType<? extends Tag> tagAttributeType = getType().getTagAttribute();
      return Validation.notNull(attribute(tagAttributeType), "Tag is undefined for " + this);
   }

   /**
    * <p> Gets the tag, if one, associated with the annotation. The tag attribute is defined for an annotation type
    * using the <code>tag</code> configuration property, e.g. <code>Annotation.TYPE.tag=fully.qualified.tag.implementation</code>.
    * Tags must implement the <code>Tag</code> interface. If no tag type is defined, the <code>Attrs.TAG</code>
    * attribute will be retrieved. </p>
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

   default boolean hasTag(){
      return attribute(getType().getTagAttribute()) != null;
   }

   /**
    * Gets the type of the annotation
    *
    * @return the annotation type
    */
   AnnotationType getType();


   @Override
   default boolean isAnnotation() {
      return true;
   }

   /**
    * Is this annotation detached, i.e. not associated with a document?
    *
    * @return True if the annotation is detached
    */
   default boolean isDetached() {
      return document() == null || getId() == DETACHED_ID;
   }

   @Override
   default boolean isInstance(@NonNull AnnotationType type) {
      return getType().isInstance(type);
   }

   /**
    * Gets the next annotation with the same type as this one
    *
    * @return The next annotation with the same type as this one or an empty fragment
    */
   default Annotation next() {
      return next(getType());
   }

   /**
    * Gets the next annotation with the same type as this one
    *
    * @return The next annotation with the same type as this one or an empty fragment
    */
   default Annotation next(int n) {
      Validation.checkArgument(n > 0);
      Annotation next = next(getType());
      while(n > 1) {
         next = next.next(getType());
         n--;
      }
      return next;
   }


   /**
    * Gets the previous annotation with the same type as this one
    *
    * @return The previous annotation with the same type as this one or an empty fragment
    */
   default Annotation previous() {
      return previous(getType());
   }

   /**
    * Determines if this annotation's tag is equal to the given tag.
    *
    * @param tag the tag to check
    * @return True if this annotation's tag is equals of the given tag.
    */
   default boolean tagEquals(Object tag) {
      Tag target = getType().getTagAttribute().decode(tag);
      return target != null && Objects.equals(getTag(), target);
   }

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
