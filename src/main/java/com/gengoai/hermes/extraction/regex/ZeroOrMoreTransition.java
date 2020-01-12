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
 * The type Kleene star.
 */
final class ZeroOrMoreTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   private final TransitionFunction child;

   /**
    * Instantiates a new Kleene star.
    *
    * @param child the child
    */
   public ZeroOrMoreTransition(TransitionFunction child) {
      this.child = child;
   }


   public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end);

      NFA childNFA = child.construct();
      childNFA.end.isAccept = false;

      nfa.start.connect(childNFA.start);
      childNFA.end.connect(childNFA.start);
      childNFA.end.connect(nfa.end);

      return nfa;
   }

   @Override
   public Tag getType() {
      return RegexTypes.ZERO_OR_MORE;
   }

   @Override
   public int matches(HString input) {
      return child.matches(input);
   }

   @Override
   public int nonMatches(HString input) {
      return child.nonMatches(input);
   }

   @Override
   public String toString() {
      return child.toString() + "*";
   }

}//END OF KleeneStar