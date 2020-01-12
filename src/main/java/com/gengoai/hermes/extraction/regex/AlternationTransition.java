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
import com.gengoai.hermes.HString;

import java.io.Serializable;

/**
 * The type Alternation.
 */
final class AlternationTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   private final TransitionFunction c1;
   private final TransitionFunction c2;

   /**
    * Instantiates a new Alternation.
    *
    * @param c1 the c 1
    * @param c2 the c 2
    */
   public AlternationTransition(TransitionFunction c1, TransitionFunction c2) {
      this.c1 = c1;
      this.c2 = c2;
   }

   @Override
   public NFA construct() {
      NFA parent = new NFA();
      NFA child1 = c1.construct();
      NFA child2 = c2.construct();

      child1.end.isAccept = false;
      child2.end.isAccept = false;

      parent.start.connect(child1.start);
      parent.start.connect(child2.start);

      child1.end.connect(parent.end);
      child2.end.connect(parent.end);

      return parent;
   }

   @Override
   public Tag getType() {
      return RegexTypes.ALTERNATION;
   }

   @Override
   public int matches(HString token) {
      return Math.max(c1.matches(token), c2.matches(token));
   }

   @Override
   public int nonMatches(HString input) {
      int m1 = c1.nonMatches(input);
      int m2 = c2.nonMatches(input);
      if (m1 > 0 && m2 > 0) {
         return Math.max(m1, m2);
      }
      return 0;
   }

   @Override
   public String toString() {
      return String.format("%s | %s", c1, c2);
   }

}//END OF Alternation