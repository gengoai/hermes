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

package com.gengoai.hermes;

import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.config.Config;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.tools.HermesCLI;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class SparkSVOExample extends HermesCLI implements Serializable {
   private static final long serialVersionUID = 1L;

   public static void main(String[] args) throws Exception {
      new SparkSVOExample().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      //Need to add the spark core jar file to the classpath for this to run
      //We will run it local, so we set the spark master to local[*]
      Config.setProperty("spark.master", "spark://192.168.1.64:7077");

      //Build the DocumentCollection
      //You can substitute the file for one you have. Here I am using a 1,000,000 sentence corpus from news articles with
      // one sentence (treated as a document) per line.
      DocumentCollection corpus = DocumentCollection.create(
            "text_opl::/data/corpora/en/Raw/news_1m_sentences.txt;distributed=true")
                                                    .repartition(100)
                                                    .annotate(Types.DEPENDENCY)
                                                    .cache();

      Counter<String> svoCounts = Counters.newCounter(
            corpus.stream()
                  .flatMap(document -> document.sentenceStream()
                                               .flatMap(sentence -> sentence.tokenStream()
                                                                            .filter(token -> token.pos().isVerb())
                                                                            .flatMap(predicate -> {
                                                                               List<Annotation> nsubjs = predicate.children(
                                                                                     "nsubj");
                                                                               List<Annotation> dobjs = predicate.children(
                                                                                     "dobj");
                                                                               List<String> svo = new LinkedList<>();
                                                                               if(nsubjs.size() > 0 && dobjs.size() > 0) {
                                                                                  for(Annotation nsubj : nsubjs) {
                                                                                     for(Annotation dobj : dobjs) {
                                                                                        svo.add(
                                                                                              nsubj.toLowerCase() + "::" + predicate
                                                                                                    .toLowerCase() + "::" + dobj);
                                                                                     }
                                                                                  }
                                                                               }
                                                                               return svo.stream();
                                                                            })
                                                       ))
                  .countByValue());

      //Calculate term frequencies for the corpus. Note we are saying we want lemmatized versions, but have not
      //run the lemma annotator, instead it will just return the lowercase version of the content.
      svoCounts.entries().forEach(entry -> System.out.println(entry.getKey() + " => " + entry.getValue()));
   }

}//END OF SparkSVOExample
