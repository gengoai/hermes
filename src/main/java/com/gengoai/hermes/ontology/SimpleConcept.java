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

import com.gengoai.Validation;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class SimpleConcept implements Concept {
   private static final long serialVersionUID = 1L;
   @NonNull
   private final String id;
   @NonNull
   private final String name;
   private final Concept parent;
   @NonNull
   private final Set<Concept> children;

   public SimpleConcept(String id,
                        String name,
                        Concept parent,
                        Set<Concept> children) {
      this.id = Validation.notNullOrBlank(id);
      this.name = Validation.notNullOrBlank(name);
      this.parent = parent;
      this.children = children == null ? Collections.emptySet() : children;
   }

   @Override
   public String id() {
      return id;
   }

   @Override
   public Concept getParent() {
      return parent;
   }

   @Override
   public Set<Concept> getChildren() {
      return Collections.unmodifiableSet(children);
   }

   @Override
   public String name() {
      return name;
   }

}//END OF SimpleConcept
