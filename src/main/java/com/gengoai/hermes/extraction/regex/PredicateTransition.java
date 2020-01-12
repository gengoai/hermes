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
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.HString;

import java.io.Serializable;

/**
 * The type Predicate matcher.
 */
final class PredicateTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   private final String pattern;
   private final SerializablePredicate<? super HString> predicate;
   private final Tag type;

   /**
    * Instantiates a new Predicate matcher.
    *
    * @param pattern   the pattern
    * @param predicate the predicate
    */
   public PredicateTransition(String pattern,
                              SerializablePredicate<? super HString> predicate,
                              Tag type) {
      this.pattern = pattern;
      this.predicate = predicate;
      this.type = type;
   }

   @Override
   public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
   }

   @Override
   public Tag getType() {
      return type;
   }

   @Override
   public int matches(HString input) {
      return predicate.test(input) ? input.tokenLength() : 0;
   }

   @Override
   public int nonMatches(HString input) {
      return predicate.test(input) ? 0 : input.tokenLength();
   }

   @Override
   public String toString() {
      return pattern;
   }
}
