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

/**
 * The type Look ahead transition.
 */
final class LookAheadTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   private final TransitionFunction child;
   private final TransitionFunction lookAhead;
   private final boolean negativeLookAhead;

   /**
    * Instantiates a new Look ahead.
    *
    * @param child             the child
    * @param lookAhead         the look ahead
    * @param negativeLookAhead the negative look ahead
    */
   public LookAheadTransition(TransitionFunction child, TransitionFunction lookAhead, boolean negativeLookAhead) {
      this.child = child;
      this.lookAhead = lookAhead;
      this.negativeLookAhead = negativeLookAhead;
   }

   @Override
   public NFA construct() {
      NFA nEnd = new NFA();
      nEnd.start.connect(nEnd.end, this);
      return nEnd;
   }

   @Override
   public Tag getType() {
      return negativeLookAhead
             ? RegexTypes.NEGATIVE_LOOKAHEAD
             : RegexTypes.LOOKAHEAD;
   }

   @Override
   public int matches(HString input, ListMultimap<String, HString> namedGroups) {
      int m = child.matches(input, namedGroups);
      if(m > 0) {
         int n = lookAhead.matches(input.asAnnotation().next(), namedGroups);
         if(n <= 0 && negativeLookAhead) {
            return m;
         } else if(n > 0 && !negativeLookAhead) {
            return m;
         }
      }
      return 0;
   }

   @Override
   public int nonMatches(HString input, ListMultimap<String, HString> namedGroups) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString() {
      return String.format("%s (?%s> %s)",
                           child,
                           (negativeLookAhead
                            ? "!"
                            : ""),
                           lookAhead);
   }

}//END OF LookAheadTransition
