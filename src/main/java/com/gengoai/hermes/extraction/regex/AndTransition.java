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
import com.gengoai.Validation;
import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.hermes.HString;

import java.io.Serializable;

/**
 * The type And transition.
 */
final class AndTransition implements TransitionFunction, Serializable {
   private final TransitionFunction left;
   private final TransitionFunction right;

   /**
    * Instantiates a new And transition.
    *
    * @param left  the left
    * @param right the right
    */
   public AndTransition(TransitionFunction left, TransitionFunction right) {
      Validation.checkArgument(!(left instanceof SequenceTransition), "AND does not work with sequences");
      Validation.checkArgument(!(right instanceof SequenceTransition), "AND does not work with sequences");
      this.left = left;
      this.right = right;
   }

   @Override
   public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
   }

   @Override
   public Tag getType() {
      return RegexTypes.AND;
   }

   @Override
   public int matches(HString input, ListMultimap<String, HString> namedGroups) {
      int m = left.matches(input, namedGroups);
      if(m > 0) {
         int n = right.matches(input, namedGroups);
         if(n > 0) {
            return Math.max(m, n);
         }
      }
      return 0;
   }

   @Override
   public int nonMatches(HString input, ListMultimap<String, HString> namedGroups) {
      int m = left.nonMatches(input, namedGroups);
      int n = right.nonMatches(input, namedGroups);
      return Math.max(m, n);
   }
}//END OF AndTransition
