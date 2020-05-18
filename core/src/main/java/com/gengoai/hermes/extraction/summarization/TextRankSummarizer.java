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

package com.gengoai.hermes.extraction.summarization;

import com.gengoai.apollo.math.statistics.measure.Similarity;
import com.gengoai.collection.counter.Counter;
import com.gengoai.graph.Graph;
import com.gengoai.graph.scoring.PageRank;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.similarity.EmbeddingSimilarity;
import com.gengoai.hermes.similarity.HStringSimilarity;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class TextRankSummarizer implements Summarizer {
   private static final long serialVersionUID = 1L;
   private double ratio = 0.2;
   private int numberOfSentences = -1;
   @NonNull
   private HStringSimilarity similarityMeasure = new EmbeddingSimilarity(Similarity.Cosine);
   private double similarityThreshold = 1e-10;

   @Override
   public Extraction extract(@NonNull HString hString) {
      List<Annotation> sentences = hString.sentences();
      Graph<Annotation> g = Graph.undirected();
      g.addVertices(sentences);

      for(int i = 0; i < sentences.size(); i++) {
         Annotation si = sentences.get(i);
         for(int j = i + 1; j < sentences.size(); j++) {
            Annotation sj = sentences.get(j);
            double similarity = similarityMeasure.calculate(si, sj);
            if(similarity >= similarityThreshold) {
               g.addEdge(si, sj, similarity);
            }
         }
      }

      PageRank<Annotation> pageRank = new PageRank<>(30, 0.85, 0.0001);
      Counter<Annotation> scores = pageRank.score(g);
      int summaryLength = numberOfSentences > 0
                          ? numberOfSentences
                          : (int) Math.floor(sentences.size() * ratio);
      List<HString> extraction = new ArrayList<>(scores.topN(summaryLength).items());
      extraction.sort(Comparator.comparingInt(a -> a.attribute(Types.INDEX)));
      return Extraction.fromHStringList(extraction);
   }

   @Override
   public void fit(@NonNull DocumentCollection corpus) {
      similarityMeasure.fit(corpus);
   }

}//END OF TextRankSummarizer
