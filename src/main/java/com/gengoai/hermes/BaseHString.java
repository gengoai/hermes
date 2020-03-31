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

import com.gengoai.string.Strings;
import lombok.NonNull;

/**
 * <p> Base implementation of an {@link HString} providing an {@link AttributeMap} and {@link
 * com.gengoai.collection.tree.Span} implementation. Does not allow for {@link Relation} to be added.</p>
 *
 * @author David B. Bracewell
 */
abstract class BaseHString implements HString {
   private static final long serialVersionUID = 1L;
   private final AttributeMap attributeMap = new AttributeMap();
   private final int end;
   private final int start;

   /**
    * Instantiates a new BaseHString.
    *
    * @param start the starting char offset of the string
    * @param end   the ending char offset of the string
    */
   BaseHString(int start, int end) {
      this.start = start;
      this.end = end;
   }

   @Override
   public void add(Relation relation) {
      throw new UnsupportedOperationException();
   }

   @Override
   public final AttributeMap attributeMap() {
      return attributeMap;
   }

   @Override
   public final int end() {
      return end;
   }

   @Override
   public final boolean equals(Object other) {
      return this == other;
   }

   @Override
   public void removeRelation(@NonNull Relation relation) {
      throw new UnsupportedOperationException();
   }

   @Override
   public final int start() {
      return start;
   }

   @Override
   public String toString() {
      if(document() == null || isEmpty()) {
         return Strings.EMPTY;
      }
      return document().toString().substring(start(), end());
   }
}//END OF BaseHString
