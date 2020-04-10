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
 * <p>
 * The TokenMatcher class allows for iterating of the matches, extracting the match or named-groups within the match,
 * the starting and ending offset of the match, and conversion into a TokenMatch object which records the current state
 * of the match.
 * </p>
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

   TokenMatcher(NFA automaton, HString input) {
      this.automaton = automaton;
      this.input = input;
      this.tokens = input.tokens();
   }

   TokenMatcher(NFA automaton, HString input, int start) {
      this.automaton = automaton;
      this.input = input;
      this.last = start;
      this.tokens = input.tokens();
   }

   /**
    * @return then current match as a {@link TokenMatch}
    */
   public TokenMatch asTokenMatch() {
      Validation.notNull(match, "No Match Found or find() not called");
      return match;
   }

   /**
    * @return The index of the end token associated with the match
    */
   public int end() {
      Validation.checkState(match != null, "Have not called find()");
      return last;
   }

   /**
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
    * @return the span of text covering the match
    */
   public HString group() {
      Validation.notNull(match, "No Match Found or find() not called");
      return match.group();
   }

   /**
    * Gets the list of HString associated with a named group.
    *
    * @param groupName the group name
    * @return the list of matches
    */
   public List<HString> group(String groupName) {
      Validation.notNull(match, "No Match Found or find() not called");
      return match.group(groupName);
   }

   /**
    * @return the named groups in the expression
    */
   public Set<String> groupNames() {
      Validation.notNull(match, "No Match Found or find() not called");
      return match.groupNames();
   }

   /**
    * @return The index of the start token associated with the match
    */
   public int start() {
      Validation.checkState(match != null, "Have not called find()");
      return start;
   }

}//END OF TokenMatcher
