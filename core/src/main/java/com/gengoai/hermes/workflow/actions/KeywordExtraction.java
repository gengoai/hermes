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

package com.gengoai.hermes.workflow.actions;

import com.gengoai.Validation;
import com.gengoai.collection.counter.Counter;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.extraction.keyword.KeywordExtractor;
import com.gengoai.hermes.extraction.keyword.TermKeywordExtractor;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.stream.MCounterAccumulator;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class KeywordExtraction implements Action {
   private static final long serialVersionUID = 1L;
   private int N = Integer.MAX_VALUE;
   private KeywordExtractor extractor = new TermKeywordExtractor();
   private boolean keepGlobalCounts = false;

   public KeywordExtraction(@NonNull KeywordExtractor extractor, int n, boolean keepGlobalCounts) {
      this.extractor = extractor;
      this.keepGlobalCounts = keepGlobalCounts;
      Validation.checkArgument(n > 0, "N must be >0");
      this.N = n;
   }

   public KeywordExtraction() {

   }

   public static Counter<String> getKeywords(@NonNull Context context) {
      return Cast.as(context.get(Types.KEYWORDS.name()));
   }

   @Override
   public void process(Corpus corpus, Context context) throws Exception {
      extractor.fit(corpus);
      final MCounterAccumulator<String> globalKeywordCounts = keepGlobalCounts
                                                              ? corpus.getStreamingContext().counterAccumulator()
                                                              : null;
      corpus.update("KeywordExtraction", doc -> {
         List<String> keywords = new ArrayList<>(extractor.extract(doc).count().topN(N).items());
         doc.put(Types.KEYWORDS, keywords);
         if(keepGlobalCounts) {
            keywords.forEach(k -> globalKeywordCounts.increment(k, 1));
         }
      });
      if(keepGlobalCounts) {
         context.property(Types.KEYWORDS.name(), globalKeywordCounts.value());
      }
   }

}//END OF KeywordExtraction
