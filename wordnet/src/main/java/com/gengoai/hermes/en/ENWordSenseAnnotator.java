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

package com.gengoai.hermes.en;

import com.gengoai.annotation.Preload;
import com.gengoai.collection.tree.Span;
import com.gengoai.collection.tree.Trie;
import com.gengoai.hermes.*;
import com.gengoai.hermes.annotator.SentenceLevelAnnotator;
import com.gengoai.hermes.morphology.Lemmatizer;
import com.gengoai.hermes.morphology.Lemmatizers;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.wordnet.Sense;
import com.gengoai.hermes.wordnet.WordNet;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

@Preload
public class ENWordSenseAnnotator extends SentenceLevelAnnotator {
   private static final long serialVersionUID = 1L;
   public static final AttributeType<List<Sense>> SENSE = AttributeType.make("SENSE",
                                                                             parameterizedType(List.class,
                                                                                               Sense.class));

   @Override
   public void annotate(Annotation sentence) {
      List<Annotation> tokens = sentence.tokens();
      Document document = sentence.document();
      Lemmatizer lemmatizer = Lemmatizers.getLemmatizer(sentence.getLanguage());
      for(int i = 0; i < tokens.size(); ) {
         Annotation token = tokens.get(i);
         final Trie<String> lemmas = lemmatizer.allPossibleLemmasAndPrefixes(tokens.get(i).toString(),
                                                                             PartOfSpeech.ANY);

         if(lemmas.size() > 0) {
            HString bestMatch = null;
            if(lemmas.size() == 1 && lemmatizer.canLemmatize(token.toString(), token.pos())) {
               bestMatch = token;
            } else if(lemmas.size() > 1) {
               Set<String> working = getAllLemmas(token, lemmatizer).stream()
                                                                    .filter(s -> lemmas.containsKey(s) || lemmas
                                                                          .prefix(s + " ")
                                                                          .size() > 0)
                                                                    .collect(Collectors.toSet());
               if(lemmatizer.canLemmatize(token.toString(), token.pos())) {
                  bestMatch = token;
               }
               int startChar = token.start();
               int end = i + 1;
               while(end < tokens.size()) {
                  boolean matched = false;
                  token = tokens.get(end);
                  Set<String> nextSet = new HashSet<>();
                  for(String previous : working) {
                     for(String next : getAllLemmas(token, lemmatizer)) {
                        String phrase = previous + " " + next;
                        if(lemmas.containsKey(phrase)) {
                           nextSet.add(phrase);
                           matched = true;
                        } else if(lemmas
                              .prefix(phrase)
                              .size() > 0) {
                           nextSet.add(phrase);
                        }
                     }
                  }
                  working = nextSet;
                  HString span = document.substring(startChar, token.end());
                  if(matched) {
                     bestMatch = span;
                  }
                  if(nextSet.isEmpty()) {
                     break;
                  }
                  end++;
               }
            }

            if(bestMatch == null) {
               i++;
            } else {
               createAnnotation(document, bestMatch);
               i += bestMatch.tokenLength();
            }

         } else {
            i++;
         }
      }
   }

   private Annotation createAnnotation(Document document, Span span) {
      Annotation annotation = document.annotationBuilder(Types.WORD_SENSE)
                                      .bounds(span)
                                      .createAttached();
      List<Sense> senses = WordNet.getInstance().getSenses(annotation.toString(),
                                                           PartOfSpeech.forText(annotation),
                                                           document.getLanguage());
      if(senses.isEmpty()) {
         senses = WordNet.getInstance().getSenses(annotation.toString(),
                                                  PartOfSpeech.ANY,
                                                  document.getLanguage());
      }
      annotation.put(SENSE, senses);
      return annotation;
   }

   @Override
   protected Set<AnnotatableType> furtherRequires() {
      return Collections.singleton(Types.PART_OF_SPEECH);
   }

   private Set<String> getAllLemmas(HString hString, Lemmatizer lemmatizer) {
      Set<String> all = new HashSet<>(lemmatizer.allPossibleLemmas(hString.toString(), PartOfSpeech.ANY));
      all.add(hString.toLowerCase());
      return all;
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.WORD_SENSE);
   }
}//END OF ENWordSenseAnnotator
