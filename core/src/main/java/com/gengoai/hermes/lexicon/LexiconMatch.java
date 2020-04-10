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
 */

package com.gengoai.hermes.lexicon;

import com.gengoai.hermes.HString;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

/**
 * Value class for matches made by lexicons
 *
 * @author David B. Bracewell
 */
@Value
public class LexiconMatch implements Serializable {
   private static final long serialVersionUID = 1L;
   String matchedString;
   double score;
   HString span;
   String tag;

   /**
    * Instantiates a new Lexicon match.
    *
    * @param span  the span
    * @param entry the entry
    */
   public LexiconMatch(@NonNull HString span, @NonNull LexiconEntry entry) {
      this.span = span;
      this.score = entry.getProbability();
      this.matchedString = entry.getLemma();
      this.tag = entry.getTag();
   }

   /**
    * Instantiates a new Lexicon match.
    *
    * @param span          the span
    * @param score         the score
    * @param matchedString the matched string
    * @param tag           the tag
    */
   public LexiconMatch(@NonNull HString span, double score, @NonNull String matchedString, String tag) {
      this.span = span;
      this.score = score;
      this.matchedString = matchedString;
      this.tag = tag;
   }
}//END OF LexiconMatch
