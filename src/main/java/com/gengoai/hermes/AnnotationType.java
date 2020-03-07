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
 * <p> An <code>AnnotationType</code> serves to define the structure and source of a specific annotation. The
 * definition provided by the type facilitates the portability of the annotation between different modules. An
 * annotation type defines the type name, parent type, annotator, and the tag type. </p>
 *
 * <p> Annotation types are hierarchical and all types have a parent defined. If no parent is explicitly declared, its
 * parent is resolved to the <code>ROOT</code> type. Annotation types inherit their parent's tag attribute. A tag can be
 * defined for a type during creation or by using the <code>tag</code> (e.g. <code>TypeName.tag=AttributeType</code>)
 * property, which defines the attribute to return on calls to <code>getTag()</code>. Children will inherit the tag type
 * of their parent unless explicitly specified.</p>
 *
 * <p>Type information can be defined during creation or via configuration. An Example is as follows:
 * <pre> {@code
 * Annotation {
 *       ENTITY {
 *          tag = ENTITY_TYPE
 *       }
 *
 *       REGEX_ENTITY {
 *          parent = ENTITY
 *          annotator = @{DEFAULT_ENTITY_REGEX}
 *       }
 * }
 * }**
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
    * The constant TYPE.
    */
   public static final String TYPE = "Annotation";
   /**
    * The constant ROOT representing the base annotation type.
    */
   public static final AnnotationType ROOT = registry.ROOT;
   private volatile AttributeType<? extends Tag> tagAttributeType = null;

   private AnnotationType(String name) {
      super(name);
   }

   /**
    * Is defined boolean.
    *
    * @param name the name
    * @return the boolean
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
    * Returns a collection of all currently registered AnnotationType
    *
    * @return the collection of AnnotationType
    */
   public static Collection<AnnotationType> values() {
      return registry.values();
   }

   /**
    * Gets the attribute associated with the tag of this annotation.
    *
    * @return the tag attribute
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
