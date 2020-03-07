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

import com.gengoai.EnumValue;
import com.gengoai.Registry;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.json.JsonEntry;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * <p> An <code>Attribute</code> represents a name and value type. Attributes are crated via the {@link
 * #make(String)} or the {@link #make(String, Type)} static methods. The value type of an attribute is
 * either defined via the create method or via a config parameter (<pre>{@code Attribute.AttributeName =
 * VALUE_TYPE}***</pre>). Attributes that do not have a defined type default to being Object types.</p>
 *
 * <p> Attribute names are normalized, so that an Attribute created with the name <code>partofspeech</code> and one
 * created with the name <code>PartOfSpeech</code> are equal (see {@link EnumValue} for normalization information).</p>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
@JsonHandler(value = AnnotatableType.Marshaller.class, isHierarchical = false)
public final class AttributeType<T> extends EnumValue<AttributeType> implements AnnotatableType {
   private static final Registry<AttributeType> registry = new Registry<>(AttributeType::new, AttributeType.class);
   private static final long serialVersionUID = 1L;
   /**
    * Type information for AnnotatableType
    */
   public static final String TYPE = "Attribute";
   private volatile Type valueType;

   private AttributeType(String name) {
      super(name);
   }

   /**
    * checks if an attribute with the given name is defined
    *
    * @param name the name
    * @return True - defined, False otherwise
    */
   public static boolean isDefined(String name) {
      return registry.contains(name);
   }

   /**
    * Makes a new or retrieves an existing AttributeType with the given name
    *
    * @param <T>  the type parameter
    * @param name the name of the AttributeType
    * @return the AttributeType
    */
   public static <T> AttributeType<T> make(String name) {
      return make(name, null);
   }

   /**
    * Makes a new or retrieves an existing AttributeType with the given name
    *
    * @param <T>       the type parameter
    * @param name      the name of the AttributeType
    * @param valueType the value type
    * @return the AttributeType
    */
   public static <T> AttributeType<T> make(String name, Type valueType) {
      AttributeType<T> attributeType = Cast.as(registry.make(name));
      if(valueType == null && attributeType.valueType == null) {
         attributeType.valueType = Config.get(TYPE, attributeType.name(), "type").as(Type.class, Object.class);
      } else if(valueType != null && attributeType.valueType == null) {
         attributeType.valueType = valueType;
         Config.setProperty(TYPE + "." + attributeType.name() + ".type", valueType.getTypeName());
      } else if(valueType != null && !valueType.equals(attributeType.valueType)) {
         throw new IllegalArgumentException(
               "Attempting to change value type of " + name + " from " + attributeType.getValueType());
      }
      return attributeType;
   }

   /**
    * Makes a new or retrieves an existing AttributeType with the given name
    *
    * @param <T>       the type parameter
    * @param name      the name of the AttributeType
    * @param valueType the value type
    * @return the AttributeType
    */
   public static <T> AttributeType<T> make(String name, Class<? extends T> valueType) {
      return make(name, Cast.<Type>as(valueType));
   }

   /**
    * Returns the existing AttributeType for the given name throwing an exception if the type is not defined
    *
    * @param <T>  the type parameter
    * @param name the name of the attribute
    * @return the attribute type
    */
   public static <T> AttributeType<T> valueOf(String name) {
      return Cast.as(registry.valueOf(name));
   }

   /**
    * Returns a collection of all known AttributeType in the enumeration.
    *
    * @return the collection of known AttributeType
    */
   public static Collection<AttributeType> values() {
      return registry.values();
   }

   /**
    * Decodes an object into the correct value type for this Attribute
    *
    * @param o the object
    * @return conversion of given object into correct object type for this attribute
    */
   public T decode(Object o) {
      try {
         return Converter.convert(o, valueType);
      } catch(TypeConversionException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Gets class information for the type of values this attribute is expected to have. Types are defined via
    * configuration as follows: <code>Attribute.NAME.type = class</code>. If not defined String.class will be returned.
    *
    * @return The class associated with this attributes values
    */
   public Type getValueType() {
      return valueType;
   }

   @Override
   protected Registry<AttributeType> registry() {
      return registry;
   }

   /**
    * Generates JsonEntry for the given object converted ensuring it is the correct type.
    *
    * @param o the object to to convert to Json
    * @return the JsonEntry
    */
   public JsonEntry toJson(Object o) {
      try {
         return JsonEntry.from(Converter.convert(o, valueType));
      } catch(TypeConversionException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public String type() {
      return TYPE;
   }

}//END OF Attribute
