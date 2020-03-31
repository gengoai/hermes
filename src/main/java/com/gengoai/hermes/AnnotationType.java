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

import com.gengoai.HierarchicalEnumValue;
import com.gengoai.HierarchicalRegistry;
import com.gengoai.Tag;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;

import java.util.Collection;

/**
 * <p> An AnnotationType defines an {@link Annotation}, which is a <b>typed</b> (e.g. token, sentence, phrase chunk)
 * span of text on a document having a defined set of attributes and relations. AnnotationTypes are hierarchical
 * meaning that each type has a parent (<i>ANNOTATION</i> by default) and can have subtypes.Additionally, each
 * AnnotationType has an associated {@link Tag} attribute type, which represents the central attribute of the
 * annotation type (e.g. entity type for entities and part-of-speech for tokens.). By default, an annotation's tag type
 * is inherited from the parent or defined as being a StringTag.
 * </p>
 *
 * <p>The following code snippet illustrates creating a simple AnnotationType with the default parent and a and an
 * AnnotationType whose parent is <i>ENTITY</i>.:
 * <pre>
 * {@code
 * // Assume that SENSE_TAG is a predefined AttributeType
 * AnnotationType WORD_SENSE=AnnotationType.make("WORD_SENSE",SENSE_TAG);
 * // MY_ENTITY will be a type of ENTITY and have an ENTITY_TYPE tag attribute inherited from ENTITY
 * AnnotationType MY_ENTITY=AnnotationType.make(ENTITY,"MY_ENTITY");
 * }
 * </pre>
 * </p>
 */
@JsonHandler(value = AnnotatableType.Marshaller.class, isHierarchical = false)
public final class AnnotationType extends HierarchicalEnumValue<AnnotationType> implements AnnotatableType {
   private static final HierarchicalRegistry<AnnotationType> registry = new HierarchicalRegistry<>(
         AnnotationType::new,
         AnnotationType.class,
         "ROOT");
   private static final long serialVersionUID = 1L;
   /**
    * The constant ROOT representing the base annotation type.
    */
   public static final AnnotationType ROOT = registry.ROOT;
   /**
    * The constant TYPE name.
    */
   public static final String TYPE = "Annotation";
   private volatile AttributeType<? extends Tag> tagAttributeType = null;

   private AnnotationType(String name) {
      super(name);
   }

   /**
    * Determines if the given name is a defined AnnotationType
    *
    * @param name the name
    * @return True if the name is a defined AnnotationType, False otherwise
    */
   public static boolean isDefined(String name) {
      return registry.contains(name);
   }

   /**
    * Makes a new or retrieves an existing AnnotationType with the given parent and name
    *
    * @param parent           the parent AnnotationType
    * @param name             the name of the AnnotationType
    * @param tagAttributeType the tag attribute type
    * @return the AnnotationType
    */
   @SuppressWarnings("unchecked")
   public static AnnotationType make(AnnotationType parent,
                                     String name,
                                     AttributeType<? extends Tag> tagAttributeType) {
      AnnotationType annotationType = registry.make(parent, name);
      if(tagAttributeType == null && annotationType.tagAttributeType == null) {
         annotationType.tagAttributeType = Config.get(TYPE, annotationType.name(), "tag").as(AttributeType.class);
      } else if(tagAttributeType != null && annotationType.tagAttributeType == null) {
         annotationType.tagAttributeType = tagAttributeType;
         Config.setProperty(TYPE + "." + annotationType.name() + ".tag", tagAttributeType.name());
      } else if(tagAttributeType != null && !annotationType.tagAttributeType.equals(tagAttributeType)) {
         throw new IllegalArgumentException(
               "Attempting to change tag of " + name + " from " + annotationType.tagAttributeType + " to " + tagAttributeType);
      }
      return annotationType;
   }

   /**
    * Makes a new or retrieves an existing AnnotationType.
    *
    * @param name             the name of the AnnotationType
    * @param tagAttributeType the tag attribute type
    * @return the AnnotationType
    */
   public static AnnotationType make(String name, AttributeType<? extends Tag> tagAttributeType) {
      return make(ROOT, name, tagAttributeType);
   }

   /**
    * Makes a new or retrieves an existing AnnotationType with the given parent and name
    *
    * @param parent the parent AnnotationType
    * @param name   the name of the AnnotationType
    * @return the AnnotationType
    */
   public static AnnotationType make(AnnotationType parent, String name) {
      return make(parent, name, null);
   }

   /**
    * Makes a new or retrieves an existing AnnotationType.
    *
    * @param name the name of the AnnotationType
    * @return the AnnotationType
    */
   public static AnnotationType make(String name) {
      return make(ROOT, name, null);
   }

   /**
    * @return the collection of all currently registered AnnotationType
    */
   public static Collection<AnnotationType> values() {
      return registry.values();
   }

   /**
    * @return the attribute associated with the tag of this annotation.
    */
   public AttributeType<Tag> getTagAttribute() {
      if(tagAttributeType == null) {
         synchronized(this) {
            if(tagAttributeType == null) {
               AnnotationType t = parent();
               while(tagAttributeType == null && t != null) {
                  tagAttributeType = t.tagAttributeType;
                  t = t.parent();
               }
               if(tagAttributeType == null) {
                  tagAttributeType = Types.TAG;
               }
            }
         }
      }
      return Cast.as(tagAttributeType);
   }

   @Override
   protected HierarchicalRegistry<AnnotationType> registry() {
      return registry;
   }

   @Override
   public String type() {
      return TYPE;
   }

}//END OF AnnotationType
