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

import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.FeaturizingExtractor;
import com.gengoai.hermes.extraction.lyre.LyreDSL;
import lombok.NonNull;

/**
 * <p>Implementation of a {@link KeywordExtractor} that extracts and scores terms based on a given {@link
 * FeaturizingExtractor}*.</p>
 *
 * @author David B. Bracewell
 */
public class TermKeywordExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private final FeaturizingExtractor termExtractor;

   /**
    * Instantiates a new TermSpec keyword extractor that uses a default TermSpec with lowercase words
    */
   public TermKeywordExtractor() {
      this(LyreDSL.lower(LyreDSL.filter(LyreDSL.annotation(Types.TOKEN), LyreDSL.isContentWord)));
   }

   /**
    * Instantiates a new TermSpec keyword extractor.
    *
    * @param termExtractor the specification on how to extract terms
    */
   public TermKeywordExtractor(@NonNull FeaturizingExtractor termExtractor) {
      this.termExtractor = termExtractor;
   }

   @Override
   public Extraction extract(@NonNull HString hString) {
      return termExtractor.extract(hString);
   }

   @Override
   public void fit(DocumentCollection corpus) {

   }

}//END OF TermSpecExtractor
