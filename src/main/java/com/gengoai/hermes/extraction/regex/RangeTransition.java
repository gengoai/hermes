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
 * The type Range transition.
 */
final class RangeTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   private final TransitionFunction child;
   private final int high;
   private final int low;

   /**
    * Instantiates a new Range.
    *
    * @param child the child
    * @param low   the low
    * @param high  the high
    */
   public RangeTransition(TransitionFunction child, int low, int high) {
      this.child = child;
      this.low = low;
      this.high = high;
   }

   @Override
   public NFA construct() {
      NFA nfa = new NFA();

      TransitionFunction lowT = child;
      for(int i = 1; i < low; i++) {
         lowT = new SequenceTransition(lowT, child);
      }

      NFA lowNFA = lowT.construct();
      lowNFA.end.isAccept = false;
      nfa.start.connect(lowNFA.start);
      lowNFA.end.connect(nfa.end);

      if(high == Integer.MAX_VALUE) {
         NFA tmp = child.construct();
         tmp.end.isAccept = false;
         tmp.end.connect(tmp.start);
         tmp.end.connect(nfa.end);
         lowNFA.end.connect(tmp.start);
      } else if(high > low) {
         TransitionFunction highT = child;
         for(int i = 1; i < high; i++) {
            highT = new SequenceTransition(highT, child);
            NFA tmp = highT.construct();
            tmp.end.isAccept = false;
            lowNFA.start.connect(tmp.start);
            tmp.end.connect(nfa.end);
            lowNFA = tmp;
         }
      }

      return nfa;
   }

   @Override
   public Tag getType() {
      return RegexTypes.RANGE;
   }

   @Override
   public int matches(HString token, ListMultimap<String, HString> namedGroups) {
      return child.matches(token, namedGroups);
   }

   @Override
   public int nonMatches(HString input, ListMultimap<String, HString> namedGroups) {
      return child.nonMatches(input, namedGroups);
   }

   @Override
   public String toString() {
      if(high == Integer.MAX_VALUE) {
         return String.format("%s{%d,*}", child, low);
      } else if(high < 0) {
         return String.format("%s{%d}", child, low);
      }
      return String.format("%s{%d,%d}", child, low, high);
   }
}//END OF RangeTransition
