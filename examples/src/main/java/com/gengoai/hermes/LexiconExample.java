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
import com.gengoai.config.Config;
import com.gengoai.hermes.annotator.LexiconAnnotator;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconIO;
import com.gengoai.hermes.tools.HermesCLI;
import com.gengoai.io.Resources;

/**
 * @author David B. Bracewell
 */
public class LexiconExample extends HermesCLI {

   public static void main(String[] args) throws Exception {
      new LexiconExample().run(args);
   }

   @Override
   public void programLogic() throws Exception {
      //Load the config that defines the lexicon and annotator
      Config.loadConfig(Resources.fromClasspath("com/gengoai/hermes/example.conf"));
      DocumentCollection.create("text_opl::classpath:com/gengoai/hermes/example_docs.txt")
                        .annotate(Types.TOKEN, Types.SENTENCE, Types.ENTITY)
                        .forEach(document -> document.annotations(Types.ENTITY)
                                                     .forEach(entity -> System.out.println(entity + "/" + entity.getTag())));
      System.out.println();

      //Alternatively we can do everything in code if we are not working in a distributed environment
      Lexicon lexicon = LexiconIO.read(Resources.fromClasspath("com/gengoai/hermes/people.dict.json"));

      //Register a lexicon annotator using the lexicon we created above to provide ENTITY annotations
      AnnotatorCache.getInstance()
                    .setAnnotator(Types.ENTITY, Language.ENGLISH, new LexiconAnnotator(Types.ENTITY, lexicon));

      DocumentCollection.create("text_opl::classpath:com/gengoai/hermes/example_docs.txt")
                        .annotate(Types.TOKEN, Types.SENTENCE, Types.ENTITY)
                        .forEach(document -> document.annotations(Types.ENTITY)
                                                     .forEach(entity -> System.out.println(
                                                           entity + "/" + entity.getTag())));
      System.out.println();

   }

}//END OF LexiconExample
