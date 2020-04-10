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

package com.gengoai.hermes.annotator;

import com.gengoai.Language;
import com.gengoai.Validation;
import com.gengoai.collection.Sets;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.collection.multimap.SetMultimap;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.*;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconEntry;
import com.gengoai.hermes.lexicon.LexiconManager;
import com.gengoai.hermes.lexicon.LexiconMatch;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A lexicon annotator that allows gaps to occur in multi-word expressions. For example, "old red car" and "old broke
 * car" would match the lexicon item "old car" with a distance of one.
 * </p>
 *
 * @author David B. Bracewell
 */
public class FuzzyLexiconAnnotator extends ViterbiAnnotator {
   private static final long serialVersionUID = 1L;
   private final Lexicon lexicon;
   private final int maxDistance;
   private final SetMultimap<String, String[]> prefix = new HashSetMultimap<>();
   private final SetMultimap<String, String[]> suffix = new HashSetMultimap<>();
   private final AnnotationType type;
   private final AttributeType<?> attributeType;

   /**
    * Instantiates a new FuzzyLexiconAnnotator.
    *
    * @param annotationType  the annotationType of annotation to create.
    * @param attributeType   the attribute type
    * @param lexicon         the lexicon to perform annotation based on
    * @param lexiconLanguage the language of the lexicon
    * @param maxDistance     the maximum fuzzy distance allowed.
    */
   public FuzzyLexiconAnnotator(@NonNull AnnotationType annotationType,
                                @NonNull AttributeType<?> attributeType,
                                @NonNull Lexicon lexicon,
                                @NonNull Language lexiconLanguage,
                                int maxDistance) {
      super(lexicon.getMaxTokenLength() + maxDistance);
      Validation.checkArgument(maxDistance >= 0, "Maximum fuzzy distance must be > 0");
      this.attributeType = attributeType;
      this.type = annotationType;
      this.lexicon = lexicon;
      this.maxDistance = maxDistance;
      for(String item : this.lexicon) {
         String[] parts = (lexiconLanguage.usesWhitespace()
                           ? item.split("\\s+")
                           : item.split(""));
         if(parts.length > 1) {
            prefix.put(parts[0], parts);
            suffix.put(parts[parts.length - 1], parts);
         }
      }
   }

   /**
    * Instantiates a new FuzzyLexiconAnnotator.
    *
    * @param annotationType  the annotationType of annotation to create.
    * @param attributeType   the attribute type
    * @param lexiconName     the name of the lexicon to perform annotation based on
    * @param lexiconLanguage the language of the lexicon
    * @param maxDistance     the maximum fuzzy distance allowed.
    */
   public FuzzyLexiconAnnotator(@NonNull AnnotationType annotationType,
                                @NonNull AttributeType<?> attributeType,
                                @NonNull String lexiconName,
                                @NonNull Language lexiconLanguage,
                                int maxDistance) {
      this(annotationType, attributeType, LexiconManager.getLexicon(lexiconName), lexiconLanguage, maxDistance);
   }

   @Override
   protected void createAndAttachAnnotation(Document document, LexiconMatch match) {
      if(!Strings.isNullOrBlank(match.getMatchedString())) {
         Annotation annotation = document.annotationBuilder(type).bounds(match.getSpan()).createAttached();
         if(Strings.isNotNullOrBlank(match.getTag())) {
            annotation.put(attributeType, Cast.as(attributeType.decode(match.getTag())));
         }
         annotation.put(Types.CONFIDENCE, match.getScore());
         annotation.put(Types.MATCHED_STRING, match.getMatchedString());
      }
   }

   private double distance(List<Annotation> span, String[] candidate) {
      //Make sure the span contains at least all of the words in the candidate
      Counter<String> cCtr = Counters.newCounter(Arrays.asList(candidate));
      for(Annotation a : span) {
         if(cCtr.contains(a.toString())) {
            cCtr.decrement(a.toString());
         } else if(!lexicon.isCaseSensitive() && cCtr.contains(a.toString().toLowerCase())) {
            cCtr.decrement(a.toString().toLowerCase());
         } else if(cCtr.contains(a.getLemma())) {
            cCtr.decrement(a.getLemma());
         } else if(!lexicon.isCaseSensitive() && cCtr.contains(a.getLemma().toLowerCase())) {
            cCtr.decrement(a.getLemma().toLowerCase());
         }
      }
      if(cCtr.sum() > 0) {
         return Double.POSITIVE_INFINITY;
      }

      double[] row0 = new double[candidate.length + 1];
      double[] row1 = new double[candidate.length + 1];
      for(int i = 0; i < row0.length; i++) {
         row0[i] = i;
      }

      for(int i = 0; i < span.size(); i++) {
         row1[0] = i + 1;
         for(int j = 0; j < candidate.length; j++) {
            double cost =
                  (Strings.safeEquals(candidate[j], span.get(i).toString(), lexicon.isCaseSensitive()) ||
                        Strings.safeEquals(candidate[j], span.get(i).getLemma(), lexicon.isCaseSensitive()))
                  ? 0d
                  : 1d;

            if(cost == 1 && Strings.isPunctuation(span.get(j).toString())) {
               cost = row0.length;
            }

            row1[j + 1] = Math.min(row1[j] + cost, Math.min(row0[j + 1] + cost, row0[j] + cost));
         }
         if(row1[candidate.length] > maxDistance) {
            return Double.POSITIVE_INFINITY;
         }
         System.arraycopy(row1, 0, row0, 0, row0.length);
      }

      return row0[candidate.length];
   }

   private Set<String[]> getCandidates(String prefixStr, String suffixStr) {
      return Sets.intersection(prefix.get(prefixStr), suffix.get(suffixStr));
   }

   @Override
   public String getProvider(Language language) {
      return "FuzzyLexicon(lexicon='" + lexicon.getName() + "', maxDistance=" + maxDistance + ")";
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(type);
   }

   @Override
   protected LexiconEntry scoreSpan(HString span) {
      LexiconEntry entry = lexicon.match(span)
                                  .stream()
                                  .findFirst()
                                  .orElse(null);

      if(entry != null) {
         return entry;
      }

      if(span.tokenLength() > 2) {
         List<Annotation> tokens = span.tokens();
         int TL = tokens.size() - 1;
         Set<String[]> candidates;

         if(lexicon.isCaseSensitive()) {
            candidates = Sets.union(getCandidates(tokens.get(0).toString(),
                                                  tokens.get(TL).toString()),
                                    getCandidates(tokens.get(0).getLemma(),
                                                  tokens.get(TL).getLemma()));
         } else {
            candidates = Sets.union(getCandidates(tokens.get(0).toString().toLowerCase(),
                                                  tokens.get(TL).toString().toLowerCase()),
                                    getCandidates(tokens.get(0).getLemma().toLowerCase(),
                                                  tokens.get(TL).getLemma().toLowerCase()));
         }

         String[] bestCandidate = null;
         double minDist = Double.POSITIVE_INFINITY;
         for(String[] candidate : candidates) {
            if(candidate.length < tokens.size()) {
               double d = distance(tokens, candidate);
               if(d < minDist) {
                  minDist = d;
                  bestCandidate = candidate;
               }
            }
         }

         if(minDist <= maxDistance && bestCandidate != null) {
            String matchedString = Strings.join(bestCandidate,
                                                span.getLanguage().usesWhitespace()
                                                ? " "
                                                : Strings.EMPTY);
            double score = lexicon.getProbability(Fragments.stringWrapper(matchedString)) / (0.1 + minDist);
            return LexiconEntry.of(matchedString,
                                   score,
                                   lexicon.getTag(matchedString).orElse(null),
                                   bestCandidate.length);
         }
      }
      return LexiconEntry.empty();
   }

}//END OF FuzzyLexiconAnnotator
