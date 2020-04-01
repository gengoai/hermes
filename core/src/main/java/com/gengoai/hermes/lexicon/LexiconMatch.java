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

import com.gengoai.Tag;
import com.gengoai.hermes.HString;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value class for matches made by lexicons
 *
 * @author David B. Bracewell
 */
public final class LexiconMatch implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The Matched string.
    */
   public final String matchedString;
   /**
    * The Score associated with the match
    */
   public final double score;
   /**
    * The span on the HString passed to the lexicon that was matched
    */
   public final HString span;
   /**
    * The Tag of the matched string
    */
   public final Tag tag;

   /**
    * Instantiates a new Lexicon match.
    *
    * @param span  the span
    * @param entry the entry
    */
   public LexiconMatch(HString span, LexiconEntry entry) {
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
   public LexiconMatch(HString span, double score, String matchedString, Tag tag) {
      this.span = span;
      this.score = score;
      this.matchedString = matchedString;
      this.tag = tag;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof LexiconMatch)) return false;
      LexiconMatch that = (LexiconMatch) o;
      return Double.compare(that.score, score) == 0 &&
                span.equals(that.span) &&
                matchedString.equals(that.matchedString) &&
                tag.equals(that.tag);
   }

   /**
    * Gets the matched string.
    *
    * @return the matched string
    */
   public String getMatchedString() {
      return matchedString;
   }

   /**
    * Gets the score.
    *
    * @return the score
    */
   public double getScore() {
      return score;
   }

   /**
    * Gets the matched span.
    *
    * @return the span
    */
   public HString getSpan() {
      return span;
   }

   /**
    * Gets the tag.
    *
    * @return the tag
    */
   public Tag getTag() {
      return tag;
   }

   @Override
   public int hashCode() {
      return Objects.hash(span, score, matchedString, tag);
   }

   @Override
   public String toString() {
      return "LexiconMatch{" +
                "span=" + span +
                ", score=" + score +
                ", matchedString='" + matchedString + '\'' +
                ", tag=" + tag +
                '}';
   }
}//END OF LexiconMatch
