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
import com.gengoai.hermes.RelationType;
import com.gengoai.string.Strings;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * The type Relation transition.
 */
final class RelationTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The Is outgoing.
    */
   final boolean isOutgoing;
   private final Function<? super HString, Integer> matcher;
   private final String pattern;
   private final RelationType type;
   private final String value;

   /**
    * Instantiates a new Relation matcher.
    *
    * @param type       the type
    * @param value      the value
    * @param pattern    the pattern
    * @param isOutgoing the is outgoing
    * @param matcher    the matcher
    */
   public RelationTransition(RelationType type,
                             String value,
                             String pattern,
                             boolean isOutgoing,
                             Function<? super HString, Integer> matcher) {
      this.pattern = pattern;
      this.matcher = matcher;
      this.value = value;
      this.type = type;
      this.isOutgoing = isOutgoing;
   }

   @Override
   public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
   }

   private List<Annotation> getTargets(HString input) {
      if (!input.isAnnotation()) {
         return Collections.emptyList();
      }
      if (Strings.isNullOrBlank(value)) {
         return isOutgoing ? input.asAnnotation().outgoing(type)
                           : input.asAnnotation().incoming(type);
      }
      return isOutgoing ? input.asAnnotation().outgoing(type, value)
                        : input.asAnnotation().incoming(type, value);
   }

   @Override
   public Tag getType() {
      return isOutgoing ? RegexTypes.OUTGOING_RELATION : RegexTypes.INCOMING_RELATION;
   }

   @Override
   public int matches(HString input) {
      for (Annotation a : getTargets(input)) {
         if (matcher.apply(a) > 0) {
            return input.tokenLength();
         }
      }
      return 0;
   }

   @Override
   public int nonMatches(HString input) {
      for (Annotation a : getTargets(input)) {
         if (matcher.apply(a) > 0) {
            return 0;
         }
      }
      return input.tokenLength();
   }

   @Override
   public String toString() {
      String toStr = Strings.isNullOrBlank(value) ? Strings.EMPTY :
                     ":\"" + value.replace("\"", "\\\"") + "\"";
      return "{@" + type.name() + toStr + " " + pattern + "}";
   }
}//END OF RelationTransition
