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

import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.FeaturizingExtractor;
import com.gengoai.hermes.extraction.lyre.LyreDSL;
import lombok.NonNull;

/**
 * Keyword extractor that scores words based on their TFIDF value.
 *
 * @author David B. Bracewell
 */
public class TFIDFKeywordExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private final FeaturizingExtractor termExtractor;
   private Counter<String> inverseDocumentFrequencies;

   /**
    * Instantiates a new TFIDFKeywordExtractor.
    */
   public TFIDFKeywordExtractor() {
      this(LyreDSL.lower(LyreDSL.filter(LyreDSL.annotation(Types.TOKEN), LyreDSL.isContentWord)));
   }

   /**
    * Instantiates a new TFIDFKeywordExtractor.
    *
    * @param termExtractor the specification for filtering and converting annotations to strings
    */
   public TFIDFKeywordExtractor(@NonNull FeaturizingExtractor termExtractor) {
      this.termExtractor = termExtractor;
   }

   @Override
   public Extraction extract(HString source) {
      Counter<String> tf = termExtractor.extract(source).count();
      Counter<String> tfidf = Counters.newCounter();
      final double maxTF = tf.maximumCount();
      tf.forEach((kw, freq) -> tfidf.set(kw, (0.5 + (0.5 * freq) / maxTF) * inverseDocumentFrequencies.get(kw)));
      return Extraction.fromCounter(tfidf);
   }

   @Override
   public void fit(Corpus corpus) {
      final double numDocs = corpus.size();
      this.inverseDocumentFrequencies = corpus.documentCount(termExtractor)
                                              .adjustValuesSelf(d -> Math.log(numDocs / d));
   }

}//END OF TFIDFKeywordExtractor
