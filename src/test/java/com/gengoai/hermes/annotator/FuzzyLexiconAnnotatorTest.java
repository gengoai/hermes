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

package com.gengoai.hermes.annotator;

import com.gengoai.StringTag;
import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconManager;
import com.gengoai.hermes.lexicon.TrieLexicon;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * @author David B. Bracewell
 */
public class FuzzyLexiconAnnotatorTest {

   @Test
   public void testMatch() {
      Config.initializeTest();
      Document document = DocumentProvider.getDocument();
      document.annotate(Types.TOKEN, Types.SENTENCE);

      Lexicon lexicon = TrieLexicon.builder(Types.TAG, false)
                                   .add("get tired", new StringTag("SLEEPY"))
                                   .add("get very tired", new StringTag("VERY_SLEEPY"))
                                   .add("feel sleepy", new StringTag("SLEEPY"))
                                   .add("she peeped", new StringTag("ACTION"))
                                   .add("sitting on the bank", new StringTag("ACTION"))
                                   .add("rabbit took a watch", new StringTag("ACTION"))
                                   .add("sitting on bank", new StringTag("ACTION"))
                                   .build();

      LexiconManager.register("testing", lexicon);

      FuzzyLexiconAnnotator gappyLexiconAnnotator = new FuzzyLexiconAnnotator(
         Types.LEXICON_MATCH,
         "testing",
         5
      );

      gappyLexiconAnnotator.annotate(document);
      List<Annotation> annotationList = document.annotations(Types.LEXICON_MATCH);
      assertEquals(5, annotationList.size());
      assertEquals("get very tired", annotationList.get(0).toLowerCase());
      assertEquals("VERY_SLEEPY", annotationList.get(0).getTag().name());
      assertEquals("sitting by her sister on the bank", annotationList.get(1).toLowerCase());
      assertEquals("she had peeped", annotationList.get(2).toLowerCase());
      assertEquals("feel very sleepy", annotationList.get(3).toLowerCase());
      assertEquals("rabbit actually took a watch", annotationList.get(4).toLowerCase());

   }

}