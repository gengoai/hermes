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

package com.gengoai.hermes.ontology;

import com.gengoai.Tag;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Interface defining a Concept within an Ontology or Taxonomy.
 *
 * @author David B. Bracewell
 */
public interface Concept extends Tag, Serializable {

   /**
    * The unique id of the concept
    *
    * @return The unique id of the concept
    */
   String id();

   /**
    * The parent of the concept or null if it is the root
    *
    * @return the parent
    */
   Concept getParent();

   /**
    * The children of the concept
    *
    * @return the children
    */
   Set<Concept> getChildren();

   /**
    * Returns the siblings of this concept
    *
    * @return the siblings of this concept
    */
   default Set<Concept> getSiblings() {
      return Optional.ofNullable(getParent())
                     .map(p -> p.getChildren().stream().filter(c -> !c.equals(this)).collect(Collectors.toSet()))
                     .orElse(Collections.emptySet());
   }

   /**
    * Generates the path from this concept to the root.
    *
    * @return the path to root
    */
   default List<Concept> getPathToRoot() {
      LinkedList<Concept> path = new LinkedList<>();
      Concept c = this;
      while (c.getParent() != null) {
         c = c.getParent();
         path.addLast(c);
      }
      return path;
   }

   /**
    * Gets all ancestors to this concept
    *
    * @return the ancestors
    */
   default Set<Concept> getAncestors() {
      Set<Concept> ancestors = new HashSet<>();
      Queue<Concept> horizon = new LinkedList<>(getChildren());
      while (horizon.size() > 0) {
         Concept c = horizon.remove();
         ancestors.add(c);
         horizon.addAll(c.getChildren());
      }
      return ancestors;
   }

}//END OF Concept
