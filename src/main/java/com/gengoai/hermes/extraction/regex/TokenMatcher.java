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


import com.gengoai.Validation;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;

import java.util.List;
import java.util.Set;

/**
 * A token equivalent of <code>java.util.regex.Matcher</code>
 *
 * @author David B. Bracewell
 */
public class TokenMatcher {
   private final NFA automaton;
   private final HString input;
   private final List<Annotation> tokens;
   private int last = 0;
   private TokenMatch match;
   private int start = 0;

   /**
    * Instantiates a new Token matcher.
    *
    * @param automaton the automaton
    * @param input     the input
    */
   TokenMatcher(NFA automaton, HString input) {
      this.automaton = automaton;
      this.input = input;
      this.tokens = input.tokens();
   }

   /**
    * Instantiates a new Token matcher.
    *
    * @param automaton the automaton
    * @param input     the input
    * @param start     the start
    */
   TokenMatcher(NFA automaton, HString input, int start) {
      this.automaton = automaton;
      this.input = input;
      this.last = start;
      this.tokens = input.tokens();
   }

   /**
    * As token match token match.
    *
    * @return the token match
    */
   public TokenMatch asTokenMatch() {
      Validation.notNull(match, "No Match Found or find() not called");
      return match;
   }

   /**
    * End int.
    *
    * @return The end end token of the match
    */
   public int end() {
      Validation.checkState(match != null, "Have not called find()");
      return last;
   }

   /**
    * Find boolean.
    *
    * @return True if the pattern finds a next match
    */
   public boolean find() {
      start = last;
      for(; start < tokens.size(); start++) {
         match = automaton.matches(input, start);
         if(match.getTokenEnd() != -1) {
            last = match.getTokenEnd();
            return true;
         }
      }
      start = -1;
      match = new TokenMatch(input, -1, -1, null);
      return false;
   }

   /**
    * Group span.
    *
    * @return the span
    */
   public HString group() {
      Validation.notNull(match, "No Match Found or find() not called");
      return match.group();
   }

   /**
    * Group list.
    *
    * @param groupName the group name
    * @return the list
    */
   public List<HString> group(String groupName) {
      Validation.notNull(match, "No Match Found or find() not called");
      return match.group(groupName);
   }

   /**
    * Group names set.
    *
    * @return the set
    */
   public Set<String> groupNames() {
      Validation.notNull(match, "No Match Found or find() not called");
      return match.groupNames();
   }

//   /**
//    * Start int.
//    *
//    * @return The start token of the match
//    */
//   public int start() {
//      Validation.checkState(match != null, "Have not called find()");
//      if (start >= 0) {
//         return tokens.get(start).start();
//      }
//      return -1;
//   }


}//END OF TokenMatcher
