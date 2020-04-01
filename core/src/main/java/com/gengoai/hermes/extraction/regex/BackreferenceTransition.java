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

public class BackreferenceTransition implements TransitionFunction, Serializable {
   private static final long serialVersionUID = 1L;
   private final String groupName;

   public BackreferenceTransition(String groupName) {
      this.groupName = groupName;
   }

   @Override
   public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
   }

   @Override
   public Tag getType() {
      return RegexTypes.BACKREFERNCE;
   }

   @Override
   public int matches(HString input, ListMultimap<String, HString> namedGroups) {
      return namedGroups.get(groupName)
                        .stream()
                        .anyMatch(h -> h.overlaps(input))
             ? input.length()
             : 0;
   }

   @Override
   public int nonMatches(HString input, ListMultimap<String, HString> namedGroups) {
      return namedGroups.get(groupName)
                        .stream()
                        .anyMatch(h -> h.overlaps(input))
             ? 0
             : input.length();
   }
}//END OF BackreferenceTransition
