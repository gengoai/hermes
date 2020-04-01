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
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.*;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.Extractor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * <p>Defines a lexicon in which words/phrases are mapped to categories.</p>
 *
 * @author David B. Bracewell
 */
public abstract class Lexicon implements Predicate<HString>, WordList, Extractor, PrefixSearchable, Serializable {
   private static final long serialVersionUID = 1L;

   private HString createFragment(LexiconMatch match) {
      HString tmp = match.getSpan().document().substring(match.getSpan().start(), match.getSpan().end());
      tmp.put(Types.CONFIDENCE, match.getScore());
      tmp.put(Types.MATCHED_STRING, match.getMatchedString());
      if(getTagAttributeType() != null) {
         tmp.put(getTagAttributeType(), match.getTag());
      }
      return tmp;
   }

   public abstract Set<LexiconEntry<?>> entries(String lemma);

   /**
    * Gets the set of lexicon entries in the lexicon
    *
    * @return the set of lexicon entries the lexicon matches against
    */
   public abstract Set<LexiconEntry<?>> entries();

   @Override
   public Extraction extract(@NonNull HString source) {
      if(isProbabilistic()) {
         return Extraction.fromHStringList(viterbi(source));
      }
      return Extraction.fromHStringList(longestMatchFirst(source));
   }

   /**
    * Gets the matched entries for a given {@link HString}
    *
    * @param hString the {@link HString} to match against
    * @return the entries matching the {@link HString}
    */
   public abstract List<LexiconEntry<?>> getEntries(HString hString);

   /**
    * Gets the matched entries for a given {@link HString}
    *
    * @param hString the {@link HString} to match against
    * @return the entries matching the {@link HString}
    */
   public abstract List<LexiconEntry<?>> getEntries(String hString);

   /**
    * Gets the first matched lemma, if one, for the given {@link HString}
    *
    * @param hString the {@link HString} to match against
    * @return the first matched lemma for the {@link HString}
    */
   public final Optional<String> getMatch(HString hString) {
      return getEntries(hString)
            .stream()
            .map(LexiconEntry::getLemma)
            .findFirst();
   }

   public abstract int getMaxLemmaLength();

   /**
    * Gets max token length.
    *
    * @return the max token length
    */
   public abstract int getMaxTokenLength();

   /**
    * Gets the maximum probability for matching the given {@link HString}
    *
    * @param hString the {@link HString} to match against
    * @return the maximum probability for the {@link HString}
    */
   public final double getProbability(HString hString) {
      return getEntries(hString)
            .stream()
            .mapToDouble(LexiconEntry::getProbability)
            .max()
            .orElse(0d);
   }

   /**
    * Gets the maximum probability for matching the given String
    *
    * @param lemma the String to match against
    * @return the maximum probability for the String
    */
   public final double getProbability(String lemma) {
      return getProbability(Fragments.stringWrapper(lemma));
   }

   /**
    * Gets the maximum probability for matching the given {@link HString} with the given Tag
    *
    * @param hString the {@link HString} to match against
    * @param tag     the tag that must be present for the match
    * @return the maximum probability for the {@link HString} with the given tag
    */
   public final double getProbability(HString hString, Tag tag) {
      return getEntries(hString).stream()
                                .filter(le -> le.getTag() != null && le.getTag().isInstance(tag))
                                .mapToDouble(LexiconEntry::getProbability)
                                .max()
                                .orElse(0d);
   }

   /**
    * Gets the maximum probability for matching the given String with the given tag
    *
    * @param string the String to match against
    * @param tag    the tag that must be present for the match
    * @return the maximum probability for the String with the given tag
    */
   public final double getProbability(String string, Tag tag) {
      return getProbability(Fragments.stringWrapper(string), tag);
   }

   /**
    * Gets the first matched tag, if one, for the given String
    *
    * @param lemma the  String to match against
    * @return the first matched tag for the String
    */
   public final Optional<Tag> getTag(String lemma) {
      return getTag(Fragments.stringWrapper(lemma));
   }

   /**
    * Gets the first matched tag, if one, for the given {@link HString}
    *
    * @param hString the {@link HString} to match against
    * @return the first matched tag for the {@link HString}
    */
   public final Optional<Tag> getTag(HString hString) {
      return getEntries(hString).stream()
                                .filter(e -> e.getTag() != null)
                                .map(LexiconEntry::getTag)
                                .map(Cast::<Tag>as)
                                .findFirst();
   }

   /**
    * Gets the tag attribute assigned by the Lexicon
    *
    * @return the tag attribute
    */
   public abstract AttributeType<Tag> getTagAttributeType();

   /**
    * Is the Lexicon case sensitive or not
    *
    * @return True if the lexicon is case sensitive, False if not
    */
   public abstract boolean isCaseSensitive();

   /**
    * Is the Lexicon case sensitive or not
    *
    * @return True if the lexicon is case sensitive, False if not
    */
   public abstract boolean isProbabilistic();

   private List<HString> longestMatchFirst(HString source) {
      List<HString> results = new LinkedList<>();
      List<Annotation> tokens = source.tokens();

      for(int i = 0; i < tokens.size(); ) {
         Annotation token = tokens.get(i);
         if(this.isPrefixMatch(token)) {
            LexiconMatch bestMatch = null;
            for(int j = i + 1; j <= tokens.size(); j++) {
               HString temp = HString.union(tokens.subList(i, j));
               if(temp.length() > getMaxLemmaLength()) {
                  break;
               }
               List<LexiconEntry<?>> entries = getEntries(temp);
               if(entries.size() > 0) {
                  bestMatch = new LexiconMatch(temp, entries.get(0));
               }
               if(!this.isPrefixMatch(temp)) {
                  break;
               }
            }

            if(bestMatch != null) {
               results.add(createFragment(bestMatch));
               i += bestMatch.getSpan().tokenLength();
            } else {
               i++;
            }

         } else if(test(token)) {
            results.add(createFragment(new LexiconMatch(token, getEntries(token).get(0))));
            i++;
         } else {
            i++;
         }
      }

      return results;
   }

   /**
    * Normalizes the string based whether the lexicon is case sensitive or not.
    *
    * @param sequence the sequence
    * @return the string
    */
   protected String normalize(CharSequence sequence) {
      if(isCaseSensitive()) {
         return sequence.toString();
      }
      return sequence.toString().toLowerCase();
   }

   /**
    * The number of lexical items in the lexicon
    *
    * @return the number of lexical items in the lexicon
    */
   public abstract int size();

   @Override
   public final boolean test(HString hString) {
      return getMatch(hString).isPresent();
   }

   private List<HString> viterbi(HString source) {
      List<Annotation> tokens = source.tokens();
      int n = tokens.size();
      int maxLen = getMaxLemmaLength() + 1;
      LexiconMatch[] matches = new LexiconMatch[n + 1];
      double[] best = new double[n + 1];
      best[0] = 0;
      for(int end = 1; end <= n; end++) {
         matches[end] = new LexiconMatch(tokens.get(end - 1), 0d, "", null);
         for(int start = end - 1; start >= 0; start--) {
            HString span = HString.union(tokens.subList(start, end));
            if(span.length() > maxLen) {
               break;
            }
            LexiconEntry<?> entry = getEntries(span).stream().findFirst().orElse(new LexiconEntry<>("", 0, null, null));
            LexiconMatch score = new LexiconMatch(span, entry.getProbability(), entry.getLemma(), entry.getTag());
            double segmentScore = score.getScore() + best[start];
            if(segmentScore >= best[end]) {
               best[end] = segmentScore;
               matches[end] = score;
            }
         }
      }
      int i = n;
      List<HString> results = new LinkedList<>();
      while(i > 0) {
         LexiconMatch match = matches[i];
         if(match.getScore() > 0) {
            results.add(createFragment(match));
         }
         i = i - matches[i].getSpan().tokenLength();
      }

      Collections.reverse(results);
      return results;
   }
}//END OF Lexicon
