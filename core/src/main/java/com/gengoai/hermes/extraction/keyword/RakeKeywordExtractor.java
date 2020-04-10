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

package com.gengoai.hermes.extraction.keyword;

import com.gengoai.Validation;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.FeaturizingExtractor;
import com.gengoai.hermes.extraction.lyre.LyreDSL;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.extraction.lyre.LyreExpressionType;
import com.gengoai.hermes.morphology.StopWords;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the RAKE keyword extraction algorithm as presented in:
 * <pre>
 * Rose, S., Engel, D., Cramer, N., & Cowley, W. (2010). Automatic Keyword Extraction from Individual Documents.
 * In M. W. Berry & J. Kogan (Eds.), Text Mining: Theory and Applications: John Wiley & Sons.
 * </pre>
 *
 * @author David B. Bracewell
 */
public class RakeKeywordExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private final LyreExpression toStringExpression;

   /**
    * Instantiates a new Rake keyword extractor using a default {@link FeaturizingExtractor} that lower cases words.
    */
   public RakeKeywordExtractor() {
      this(LyreDSL.lower);
   }

   /**
    * Instantiates a new Rake keyword extractor.
    *
    * @param toStringExpression the specification for how to convert tokens/phrases to strings (all other options are
    *                           ignored).
    */
   public RakeKeywordExtractor(@NonNull LyreExpression toStringExpression) {
      Validation.checkArgument(toStringExpression.isInstance(LyreExpressionType.STRING),
                               "Must give a STRING expression");
      this.toStringExpression = toStringExpression;
   }

   @Override
   public Extraction extract(@NonNull HString source) {
      List<HString> spans = new ArrayList<>();
      source.document().annotate(Types.SENTENCE);

      //Step 1: Extract candidate phrases
      final StopWords stopWords = StopWords.getStopWords(source.getLanguage());
      source.sentenceStream().forEach(sentence -> {
         List<HString> buffer = new ArrayList<>();
         sentence.tokenStream().forEach(token -> {
            if(stopWords.isStopWord(token) && !buffer.isEmpty()) {
               spans.add(HString.union(buffer));
               buffer.clear();
            } else if(!stopWords.isStopWord(token)) {
               buffer.add(token);
            }
         });
         if(buffer.size() > 0) {
            spans.add(HString.union(buffer));
         }
      });

      //Step 2: Score the candidates
      Counter<String> wordFreqs = Counters.newCounter();
      Counter<String> wordDegree = Counters.newCounter();
      spans.forEach(span -> span.tokenStream().forEach(word -> {
         wordFreqs.increment(toStringExpression.apply(word));
         wordDegree.increment(toStringExpression.apply(word), span.tokenLength() - 1);
      }));

      Counter<String> wordScores = Counters.newCounter();
      wordFreqs.forEach((word, freq) -> wordScores.increment(word, (wordDegree.get(word) + freq) / freq));

      Counter<String> phraseScores = Counters.newCounter();
      spans.forEach(span -> {
         double score = 0;
         for(Annotation word : span.tokens()) {
            score += wordScores.get(toStringExpression.apply(word));
         }
         phraseScores.increment(toStringExpression.apply(span), score);
      });

      return Extraction.fromCounter(phraseScores);
   }

   @Override
   public void fit(Corpus corpus) {

   }

}//END OF RakeKeywordExtractor
