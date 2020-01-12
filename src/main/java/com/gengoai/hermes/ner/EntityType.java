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
 *
 */

package com.gengoai.hermes.ner;

import com.gengoai.HierarchicalEnumValue;
import com.gengoai.HierarchicalRegistry;
import com.gengoai.conversion.TypeConversionException;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * <p>Tag type associated with Entity annotations. Entities are defined in a hierarchy, e.g. <code>Location ->
 * Country</code></p>
 */
public final class EntityType extends HierarchicalEnumValue<EntityType> {
   private static final HierarchicalRegistry<EntityType> registry = new HierarchicalRegistry<>(
      EntityType::new,
      EntityType.class,
      "ROOT");
   /**
    * The constant ROOT.
    */
   public static final EntityType ROOT = registry.ROOT;
   private static final long serialVersionUID = 1L;

   private EntityType(String name) {
      super(name);
   }

   /**
    * Makes a new or retrieves an existing EntityType with the given parent and name
    *
    * @param parent the parent EntityType
    * @param name   the name of the EntityType
    * @return the EntityType
    */
   public static EntityType make(EntityType parent, String name) {
      return registry.make(parent, name);
   }

   /**
    * Makes a new or retrieves an existing EntityType.
    *
    * @param name the name of the EntityType
    * @return the EntityType
    */
   public static EntityType make(String name) {
      return registry.make(name);
   }

   /**
    * Returns a collection of all currently registered EntityType
    *
    * @return the collection of EntityType
    */
   public static Collection<EntityType> values() {
      return registry.values();
   }


   @Override
   protected HierarchicalRegistry<EntityType> registry() {
      return registry;
   }


   /**
    * The type Converter.
    */
   @MetaInfServices
   public static class Converter implements com.gengoai.conversion.TypeConverter {

      @Override
      public Object convert(Object source, Type... parameters) throws TypeConversionException {
         if (source instanceof EntityType) {
            return source;
         } else if (source instanceof CharSequence) {
            return EntityType.make(source.toString());
         }
         throw new TypeConversionException(source, EntityType.class);
      }

      @Override
      public Class[] getConversionType() {
         return new Class[]{EntityType.class};
      }
   }

}// END OF EntityType

