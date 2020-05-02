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

import com.gengoai.Language;
import com.gengoai.hermes.annotator.RegexAnnotator;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.tools.HermesCLI;

import static com.gengoai.hermes.Types.SENTENCE;
import static com.gengoai.hermes.Types.TOKEN;

/**
 * @author David B. Bracewell
 */
public class CustomAnnotator extends HermesCLI {

   public static void main(String[] args) throws Exception {
      new CustomAnnotator().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      //Create an ANIMAL_MENTION annotation type that is added to documents using a regular expression annotator.
      AnnotationType animalMention = AnnotationType.make("ANIMAL_MENTION");
      //Here we will forgo the normal configuration setup and set the annotator directly on the pipeline.
      //Note: that this only works in a non-distributed environment
      //We will use a RegexAnnotator, which will add "\b" to the beginning and end of the pattern if it is not already there.
      AnnotatorCache.getInstance()
                    .setAnnotator(animalMention, Language.ENGLISH, new RegexAnnotator("(fox|dog)", animalMention));

      //Create a VERBS annotation type that is added to documents using a regular expression annotator.
      AnnotationType verbs = AnnotationType.make("VERBS");
      AnnotatorCache.getInstance()
                    .setAnnotator(verbs, Language.ENGLISH, new RegexAnnotator("(is|jumps?|come)", verbs));

      //Build a corpus from plain text with one document per line in a String resource
      DocumentCollection.create(Document.create("The quick brown fox jumps over the lazy dog."),
                                Document.create("Now is the time for all good men to come to aid of their country."))
                        //Annotate the document for tokens, sentences, animal mentions, and verbs
                        .annotate(TOKEN, SENTENCE, animalMention, verbs)
                        //for each of the documents print out the animal mentions and verbs
                        .forEach(document -> {
                           document.annotations(animalMention).forEach(a -> System.out.println("ANIMAL: " + a));
                           document.annotations(verbs).forEach(a -> System.out.println("VERB: " + a));
                        });

   }
}//END OF CustomAnnotator
