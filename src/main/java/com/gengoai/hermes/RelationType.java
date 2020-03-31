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

import java.util.Collection;

/**
 * <p>Dynamic enumeration of known types of relations that can exist between annotations. Relations represent edges
 * between two annotations. Examples include syntactic (dependency parse) and semantic (semantic roles) relations.</p>
 *
 * @author David B. Bracewell
 */
@JsonHandler(value = AnnotatableType.Marshaller.class, isHierarchical = false)
public final class RelationType extends EnumValue implements AnnotatableType {
   private static final Registry<RelationType> registry = new Registry<>(RelationType::new, RelationType.class);
   private static final long serialVersionUID = 1L;
   /**
    * The constant TYPE.
    */
   public static final String TYPE = "Relation";

   private RelationType(String name) {
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
    * Makes a new or retrieves an existing RelationType with the given name
    *
    * @param name the name of the RelationType
    * @return the RelationType
    */
   public static RelationType make(String name) {
      return registry.make(name);
   }

   /**
    * Value of relation type.
    *
    * @param name the name
    * @return the relation type
    */
   public static RelationType valueOf(String name) {
      return registry.valueOf(name);
   }

   /**
    * Returns a collection of all known RelationType in the enumeration.
    *
    * @return the collection of known RelationType
    */
   public static Collection<RelationType> values() {
      return registry.values();
   }

   @Override
   protected Registry<RelationType> registry() {
      return registry;
   }

   @Override
   public String type() {
      return TYPE;
   }

}//END OF RelationType

