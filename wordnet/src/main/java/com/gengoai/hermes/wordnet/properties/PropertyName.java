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

package com.gengoai.hermes.wordnet.properties;

import com.gengoai.EnumValue;
import com.gengoai.Registry;
import lombok.NonNull;

import java.util.Collection;

/**
 * <p>Dynamic enum for property names.</p>
 *
 * @author David B. Bracewell
 */
public class PropertyName extends EnumValue<PropertyName> implements Comparable<PropertyName> {
   private static final long serialVersionUID = 1L;
   private static final Registry<PropertyName> registry = new Registry<>(PropertyName::new, PropertyName.class);

   /**
    * The constant DOMAIN.
    */
   public static final PropertyName DOMAIN = make("DOMAIN");
   /**
    * The constant INFO_CONTENT.
    */
   public static final PropertyName INFO_CONTENT = make("INFORMATION_CONTENT");
   /**
    * The constant INFO_CONTENT_RESNIK.
    */
   public static final PropertyName INFO_CONTENT_RESNIK = make("INFORMATION_CONTENT_RESNIK");
   /**
    * The constant SENTIMENT.
    */
   public static final PropertyName SENTIMENT = make("SENTIMENT");
   /**
    * The constant SUMO_CONCEPT.
    */
   public static final PropertyName SUMO_CONCEPT = make("SUMO_CONCEPT");

   /**
    * <p>Creates a new or retrieves an existing instance of PropertyName with the given name.</p>
    *
    * @return The instance of PropertyName corresponding th the give name.
    */
   public static PropertyName make(@NonNull String name) {
      return registry.make(name);
   }

   /**
    * <p>Returns the constant of PropertyName with the specified name.The normalized version of the specified name will
    * be matched allowing for case and space variations.</p>
    *
    * @return The constant of PropertyName with the specified name
    * @throws IllegalArgumentException if the specified name is not a member of PropertyName.
    */
   public static PropertyName valueOf(@NonNull String name) {
      return registry.valueOf(name);
   }

   /**
    * <p>Retrieves all currently known values of PropertyName.</p>
    *
    * @return An unmodifiable collection of currently known values for PropertyName.
    */
   public static Collection<PropertyName> values() {
      return registry.values();
   }

   private PropertyName(String name) {
      super(name);
   }

   @Override
   public int compareTo(@NonNull PropertyName o) {
      return this.canonicalName().compareTo(o.canonicalName());
   }

   @Override
   protected Registry<PropertyName> registry() {
      return registry;
   }
}//END OF PropertyName
