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
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;

import java.io.Serializable;

/**
 * The type Sequence.
 */
final class SequenceTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   private final TransitionFunction c1;
   private final TransitionFunction c2;

   /**
    * Instantiates a new Sequence.
    *
    * @param c1 the c 1
    * @param c2 the c 2
    */
   public SequenceTransition(TransitionFunction c1, TransitionFunction c2) {
      this.c1 = c1;
      this.c2 = c2;
   }

   @Override
   public NFA construct() {
      NFA base = new NFA();
      NFA nfa1 = c1.construct();
      NFA nfa2 = c2.construct();

      base.start.connect(nfa1.start);

      nfa1.end.isAccept = false;
      nfa1.end.connect(nfa2.start);

      nfa2.end.isAccept = false;
      nfa2.end.connect(base.end);

      return base;
   }

   @Override
   public Tag getType() {
      return RegexTypes.OPEN_PARENS;
   }

   @Override
   public int matches(HString token) {
      int i = c1.matches(token);
      if (i == 0) {
         return 0;
      }
      Annotation next = token.lastToken();
      for (int j = 0; j < i; j++) {
         next = next.next();
      }
      int j = c2.matches(next);
      if (j == 0) {
         return 0;
      }
      return i + j;
   }

   @Override
   public int nonMatches(HString input) {
      int i = c1.matches(input);
      if (i > 0) {
         Annotation next = input.lastToken().next();
         for (int j = 1; j < i; j++) {
            next = next.next();
         }
         if (next.isEmpty()) {
            return i;
         }
         return c2.nonMatches(next);
      }

      i = c1.nonMatches(input);
      Annotation next = input.lastToken().next();
      for (int j = 1; j < i; j++) {
         next = next.next();
      }
      return i + Math.max(c2.matches(next), c2.nonMatches(next));
   }

   @Override
   public String toString() {
      return c1 + " " + c2;
   }


}//END OF Sequence
