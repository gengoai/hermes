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
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.collection.multimap.SetMultimap;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.morphology.POS;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.NGramExtractor;
import com.gengoai.hermes.morphology.StopWords;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p> Implementation of the NP Clustering Keyword Extractor presented in:
 * <pre>
 *   Bracewell, David B., Yan, Jiajun, and Ren, Fuji, (2008), Single Document Keyword Extraction For Internet News
 * Articles, International Journal of Innovative Computing, Information and Control, 4, 905—913
 * </pre>
 * </p>
 *
 * @author David B. Bracewell
 */
public class NPClusteringKeywordExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;

   @Override
   public void fit(Corpus corpus) {

   }

   @Override
   public Extraction extract(@NonNull HString source) {
      source.document().annotate(Types.PHRASE_CHUNK, Types.LEMMA);

      Counter<String> tf = Counters.newCounter(source.tokenStream()
                                                     .map(HString::getLemma)
                                                     .collect(Collectors.toList()));


      List<HString> chunks = source.annotationStream(Types.PHRASE_CHUNK)
                                       .filter(pc -> pc.pos().isInstance(POS.NOUN))
                                       .flatMap(pc -> pc.split(a -> a.pos().isInstance(POS.PUNCTUATION)).stream())
                                       .map(pc -> pc.trim(StopWords.isStopWord()))
                                       .filter(pc -> !pc.isEmpty())
                                       .collect(Collectors.toList());

      Counter<String> npFreqs = Counters.newCounter(chunks.stream()
                                                          .map(HString::getLemma)
                                                          .collect(Collectors.toList()));


      Counter<String> npScores = Counters.newCounter();

      chunks.forEach(pc -> {
         String lemma = pc.getLemma();
         double npFreq = npFreqs.get(lemma);
         double termSum = pc.tokenStream().mapToDouble(token -> tf.get(token.getLemma())).sum();
         double score = Math.log(pc.tokenLength() + (termSum / pc.tokenLength()) * npFreq);
         if (score > npScores.get(lemma)) {
            npScores.set(lemma, score);
         }
      });


      SetMultimap<String, String> clusters = new HashSetMultimap<>();

      chunks.stream().filter(pc -> pc.tokenLength() == 1)
            .forEach(pc -> clusters.put(pc.getLemma(), pc.getLemma()));


      Map<String, HString> notAdded = new HashMap<>();
      chunks.stream()
            .filter(pc -> pc.tokenLength() > 1)
            .forEach(pc -> {
               boolean added = false;
               for (Annotation token : pc.tokens()) {
                  if (clusters.containsKey(token.getLemma())) {
                     added = true;
                     clusters.put(token.getLemma(), pc.getLemma());
                  }
               }
               if (!added) {
                  notAdded.put(pc.getLemma(), pc);
               }
            });


      NGramExtractor nGramExtractor = NGramExtractor.builder(2, 4).build();
      notAdded.forEach((lemma, pc) -> {
         for (HString ng : nGramExtractor.extract(pc)) {
            String ngLemma = ng.getLemma();
            boolean added = false;
            for (String cKey : clusters.keySet()) {
               if (clusters.get(cKey).contains(ngLemma)) {
                  added = true;
                  clusters.put(cKey, ngLemma);
               }
            }
            if (added) {
               break;
            }
         }
      });


      Counter<String> clusterScores = Counters.newCounter();
      clusters.keySet().forEach(cluster -> {
         double totalNPScore = clusters.get(cluster).stream().mapToDouble(npScores::get).sum();
         clusterScores.set(cluster, totalNPScore / clusters.get(cluster).size());
      });


      Counter<String> keywords = Counters.newCounter();
      clusterScores.forEach((c, score) -> {
         String centroid = clusters.get(c)
                                   .stream()
                                   .max((p1, p2) -> -Double.compare(npScores.get(p1), npScores.get(p2)))
                                   .orElse(c);

         keywords.set(centroid, clusterScores.get(c) + npScores.get(centroid));
      });

      return Extraction.fromCounter(keywords);
   }

}//END OF NPClusteringKeywordExtractor
