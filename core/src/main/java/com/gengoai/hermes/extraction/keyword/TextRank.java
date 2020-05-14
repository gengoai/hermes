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

package com.gengoai.hermes.extraction.keyword;

import com.gengoai.collection.Lists;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.graph.Edge;
import com.gengoai.graph.Graph;
import com.gengoai.graph.scoring.PageRank;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.StopWords;
import com.gengoai.tuple.Tuple;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TextRank implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private int windowSize = 2;
   private PartOfSpeech[] validPartsOfSpeech = {PartOfSpeech.ADJECTIVE, PartOfSpeech.NOUN, PartOfSpeech.PROPER_NOUN};
   private boolean weighted = false;
   private double ratio = 0.33;

   @Override
   public Extraction extract(@NonNull HString hString) {
      Graph<String> g = Graph.undirected();

      //Generate the words for the graph
      List<String> tokens = hString.tokenStream()
                                   .filter(t -> t.pos().isInstance(validPartsOfSpeech))
                                   .filter(StopWords.isContentWord())
                                   .map(HString::getLemma)
                                   .collect(Collectors.toList());

      //Add the tokens to the graph
      g.addVertices(new HashSet<>(tokens));

      for(int i = 0; i < hString.tokenLength() - windowSize; i++) {
         String tiStr = hString.tokenAt(i).toLowerCase();

         if(!g.containsVertex(tiStr)) {
            continue;
         }

         Iterable<Tuple> edges = Lists.combinations(hString.tokens().subList(i, i + windowSize), 2);
         for(Tuple edge : edges) {
            String tjStr = ((HString) edge.get(1)).getLemma();
            if(!g.containsVertex(tjStr)) {
               continue;
            }
            Edge<String> e;
            if(g.containsEdge(tiStr, tjStr)) {
               e = g.getEdge(tiStr, tjStr);
            } else {
               e = g.addEdge(tiStr, tjStr);
            }
            e.setWeight(e.getWeight() + 1);
         }
      }

      PageRank<String> pageRank = new PageRank<>(30, 0.85, 0.0001);
      Counter<String> scores = pageRank.score(g);
      scores = scores.topN((int) (scores.size() * ratio));

      Multimap<String, HString> lemmaToWord = new ArrayListMultimap<>();
      List<HString> keywords = new ArrayList<>();

      for(int i = 0; i < hString.tokenLength(); i++) {
         String tiStr = hString.tokenAt(i).getLemma();
         if(scores.contains(tiStr)) {
            int j = i + 1;
            double score = scores.get(tiStr);
            while(j < hString.tokenLength() &&
                  hString.tokenAt(j).sentence() == hString.tokenAt(i).sentence() &&
                  scores.contains(hString.tokenAt(j).getLemma())) {
               score += scores.get(hString.tokenAt(j).getLemma());
               j++;
            }
            HString h = HString.union(List.of(hString.tokenAt(i), hString.tokenAt(j).previous()));
            h.put(Types.SCORE, score / (j - i));
            keywords.add(h);
            i = j;
         }
      }

      return Extraction.fromHStringList(keywords, HString::getLemma);
   }

   @Override
   public void fit(@NonNull DocumentCollection corpus) {

   }
}//END OF TextRank
