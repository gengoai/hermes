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
import com.gengoai.conversion.Converter;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Fragments;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * <p>
 * A traditional approach to information extraction incorporates the use of lexicons, also called gazetteers, for
 * finding specific lexical items in text. Hermes's Lexicon classes provide the ability to match lexical items using a
 * greedy longest match first or maximum span probability strategy. Both matching strategies allow for case-sensitive or
 * case-insensitive matching and the use of constraints (using the Lyre expression language), such as part-of-speech, on
 * the match.
 * </p>
 * <p>
 * Lexicons are managed using the {@link LexiconManager}, which acts as a cache associating lexicons with a name and a
 * language. This allows for lexicons to be defined via configuration and then to be loaded and retrieved by their name
 * (this is particularly useful for annotators that use lexicons).
 * </p>
 * <p>
 * Lexicons are defined using a {@link LexiconSpecification} in the following format:
 * </p>
 * <pre>
 * {@code
 * lexicon:(mem|disk):name(:(csv|json))*::RESOURCE(;ARG=VALUE)*
 * }**
 * </pre>
 * <p>
 * The schema of the specification is "lexicon" and the currently supported protocols are: mem: An in-memory Trie-based
 * lexicon. disk: A persistent on-disk based lexicon.The name of the lexicon is used during annotation to mark the
 * provider. Additionally, a format (csv or json) can be specified, with json being the default if none is provided, to
 * specify the lexicon format when creating in-memory lexicons. Finally, a number of query parameters (ARG=VALUE) can be
 * given from the following choices:
 * <ul>
 * <li><code>caseSensitive=(true|false)</code>: Is the lexicon case-sensitive (</b>true<b>) or case-insensitive (</b>false<b>) (default </b>false<b>).</li>
 * <li><code>defaultTag=TAG</code>: The default tag value for entry when one is not defined (default null).</li>
 * <li><code>language=LANGUAGE</code>: The default language of entries in the lexicon (default Hermes.defaultLanguage()).</li>
 * </p>
 * and the following for CSV lexicons:
 * <ul>
 * <li><code>lemma=INDEX</code>: The index in the csv row containing the lemma (default 0).</li>
 * <li><code>tag=INDEX</code>: The index in the csv row containing the tag (default 1).</li>
 * <li><code>probability=INDEX</code>: The index in the csv row containing the probability (default 2).</li>
 * <li><code>constraint=INDEX</code>: The index in the csv row containing the constraint (default 3).</li>
 * </ul>
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class Lexicon implements Predicate<HString>, WordList, Extractor, PrefixSearchable, Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Adds an entry to the lexicon
    *
    * @param lexiconEntry the lexicon entry to add
    */
   public abstract void add(LexiconEntry lexiconEntry);

   /**
    * Adds all lexicon entries in the given iterable to the lexicon
    *
    * @param lexiconEntries the lexicon entries to add
    */
   public void addAll(@NonNull Iterable<LexiconEntry> lexiconEntries) {
      lexiconEntries.forEach(this::add);
   }

   private HString createFragment(LexiconMatch match) {
      HString tmp = match.getSpan().document().substring(match.getSpan().start(), match.getSpan().end());
      tmp.put(Types.CONFIDENCE, match.getScore());
      tmp.put(Types.MATCHED_STRING, match.getMatchedString());
      if(Strings.isNotNullOrBlank(match.getTag())) {
         tmp.put(Types.MATCHED_TAG, match.getTag());
      }
      return tmp;
   }

   /**
    * @return the set of lexicon entries in the lexicon
    */
   public abstract Set<LexiconEntry> entries();

   @Override
   public Extraction extract(@NonNull HString source) {
      if(isProbabilistic()) {
         return Extraction.fromHStringList(viterbi(source));
      }
      return Extraction.fromHStringList(longestMatchFirst(source));
   }

   /**
    * Returns the {@link LexiconEntry} associated with a given word in the Lexicon or an empty set if there are none.
    *
    * @param word the word in the lexicon whose entries we want
    * @return the {@link LexiconEntry} associated with a given word in the Lexicon or an empty set if there are none.
    */
   public abstract Set<LexiconEntry> get(@NonNull String word);

   /**
    * @return the max lemma length
    */
   public abstract int getMaxLemmaLength();

   /**
    * @return the max token length
    */
   public abstract int getMaxTokenLength();

   /**
    * @return the name of the lexicon
    */
   public abstract String getName();

   /**
    * Gets the maximum probability for matching the given {@link HString}
    *
    * @param hString the {@link HString} to match against
    * @return the maximum probability for the {@link HString}
    */
   public final double getProbability(@NonNull HString hString) {
      return match(hString)
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
   public final double getProbability(@NonNull String lemma) {
      return getProbability(Fragments.stringWrapper(lemma));
   }

   /**
    * Gets the maximum probability for matching the given {@link HString} with the given Tag
    *
    * @param hString the {@link HString} to match against
    * @param tag     the tag that must be present for the match
    * @return the maximum probability for the {@link HString} with the given tag
    */
   public final double getProbability(@NonNull HString hString, @NonNull Tag tag) {
      return match(hString).stream()
                           .filter(le -> {
                              if(Strings.isNullOrBlank(le.getTag())) {
                                 return false;
                              }
                              Tag leTag = Converter.convertSilently(le.getTag(), tag.getClass());
                              return leTag != null && leTag.isInstance(tag);
                           })
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
   public final double getProbability(@NonNull String string, @NonNull Tag tag) {
      return getProbability(Fragments.stringWrapper(string), tag);
   }

   /**
    * Gets the first matched tag, if one, for the given String
    *
    * @param lemma the  String to match against
    * @return the first matched tag for the String
    */
   public final Optional<String> getTag(@NonNull String lemma) {
      return getTag(Fragments.stringWrapper(lemma));
   }

   /**
    * Gets the first matched tag, if one, for the given {@link HString}
    *
    * @param hString the {@link HString} to match against
    * @return the first matched tag for the {@link HString}
    */
   public final Optional<String> getTag(@NonNull HString hString) {
      return match(hString).stream()
                           .filter(e -> e.getTag() != null)
                           .map(LexiconEntry::getTag)
                           .findFirst();
   }

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

   private List<HString> longestMatchFirst(@NonNull HString source) {
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
               List<LexiconEntry> entries = match(temp);
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
            results.add(createFragment(new LexiconMatch(token, match(token).get(0))));
            i++;
         } else {
            i++;
         }
      }

      return results;
   }

   /**
    * Gets the matched entries for a given {@link HString}
    *
    * @param string the {@link HString} to match against
    * @return the entries matching the {@link HString}
    */
   public abstract List<LexiconEntry> match(@NonNull HString string);

   /**
    * Returns the {@link LexiconEntry} associated with a given word in the Lexicon or an empty set if there are none.
    *
    * @param term the word in the lexicon whose entries we want
    * @return the {@link LexiconEntry} associated with a given word in the Lexicon or an empty set if there are none.
    */
   public abstract List<LexiconEntry> match(@NonNull String term);

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
   public final boolean test(@NonNull HString hString) {
      return match(hString)
            .stream()
            .map(LexiconEntry::getLemma)
            .findFirst()
            .isPresent();
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
            LexiconEntry entry = match(span).stream().findFirst().orElse(LexiconEntry.empty());
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
