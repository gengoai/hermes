/*
 * (c) 2005 David B. Bracewell
 *
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
 *
 */

package com.gengoai.hermes.extraction.regex;

import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.collection.tree.SimpleSpan;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The type Token match.
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = false)
public final class TokenMatch extends SimpleSpan implements Serializable {
   private static final long serialVersionUID = 1L;
   @NonNull
   private final ListMultimap<String, HString> captures;
   @NonNull
   private final HString input;
   @Getter
   private final int tokenEnd;
   @Getter
   private final int tokenStart;

   /**
    * Instantiates a new Token match.
    *
    * @param input    the input
    * @param start    the start
    * @param end      the end
    * @param captures the captures
    */
   public TokenMatch(HString input,
                     int start,
                     int end,
                     ListMultimap<String, HString> captures
                    ) {
      super(start >= 0 ? input.tokenAt(start).start() : -1,
            end > 0 ? input.tokenAt(end - 1).end() : -1);
      this.tokenStart = start;
      this.tokenEnd = end;
      this.input = input;
      this.captures = captures;
   }

   /**
    * Gets document.
    *
    * @return the document
    */
   public Document getDocument() {
      return input.document();
   }

   /**
    * Group span.
    *
    * @return the span
    */
   public HString group() {
      return input.document().substring(start(), end());
   }

   /**
    * Group list.
    *
    * @param groupName the group name
    * @return the list
    */
   public List<HString> group(String groupName) {
      return Collections.unmodifiableList(captures.get(groupName));
   }

   /**
    * Group names set.
    *
    * @return the set
    */
   public Set<String> groupNames() {
      return Collections.unmodifiableSet(captures.keySet());
   }

}//END OF TokenMatch
