/*
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

package com.gengoai.hermes.extraction.regex;

import com.gengoai.Tag;
import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.hermes.HString;

import java.io.Serializable;

final class NegationTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   private final TransitionFunction c1;

   /**
    * Instantiates a new Not.
    *
    * @param c1 the c 1
    */
   public NegationTransition(TransitionFunction c1) {
      this.c1 = c1;
   }

   @Override
   public NFA construct() {
      NFA parent = new NFA();
      NFA child1 = c1.construct();
      child1.end.isAccept = false;
      parent.start.connect(child1.start);
      child1.start.connect(parent.end, this);
      return parent;
   }

   @Override
   public Tag getType() {
      return RegexTypes.NEGATION;
   }

   @Override
   public int matches(HString token, ListMultimap<String, HString> namedGroups) {
      return c1.nonMatches(token, namedGroups);
   }

   @Override
   public int nonMatches(HString input, ListMultimap<String, HString> namedGroups) {
      return c1.matches(input, namedGroups);
   }

   @Override
   public String toString() {
      return String.format("^%s", c1);
   }

}//END OF NegationTransition
