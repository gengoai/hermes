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

package com.gengoai.hermes.morphology;

import com.gengoai.config.Config;
import com.gengoai.hermes.en.ENLemmatizer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class ENLemmatizerTest {

  @Test
  public void testLemmatize() throws Exception {
    Config.initializeTest();

    Lemmatizer lemmatizer = ENLemmatizer.getInstance();
    assertEquals("walk", lemmatizer.lemmatize("walking", PartOfSpeech.VERB));
    assertEquals("walk", lemmatizer.lemmatize("walked", PartOfSpeech.VERB));
    assertEquals("walk", lemmatizer.lemmatize("walks", PartOfSpeech.VERB));

    assertEquals("be", lemmatizer.lemmatize("is", PartOfSpeech.VERB));
    assertEquals("be", lemmatizer.lemmatize("was", PartOfSpeech.VERB));
    assertEquals("be", lemmatizer.lemmatize("were", PartOfSpeech.VERB));

    assertEquals("troll", lemmatizer.lemmatize("trolling", PartOfSpeech.VERB));

    assertEquals("pan", lemmatizer.lemmatize("pans", PartOfSpeech.NOUN));
    assertEquals("clothes", lemmatizer.lemmatize("clothes", PartOfSpeech.NOUN));
    assertEquals("tax", lemmatizer.lemmatize("taxes", PartOfSpeech.NOUN));

    assertEquals("knife", lemmatizer.lemmatize("knives", PartOfSpeech.NOUN));
    assertEquals("life", lemmatizer.lemmatize("lives", PartOfSpeech.NOUN));

    //Walking may be correct if we do not the pos
    assertEquals("walking", lemmatizer.lemmatize("walking", PartOfSpeech.ANY));

    //Walking may be correct if we do not the pos
    assertEquals("walking", lemmatizer.lemmatize("walking"));

//    Document document = DocumentFactory.getInstance().create("I was walking to the shore.", Language.ENGLISH);
//    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
//    assertEquals("walking", lemmatizer.lemmatize(document.find("walking")));
//
//    document.find("was").first(Types.TOKEN).put(Types.PART_OF_SPEECH, PartOfSpeech.VBD);
//    document.find("walking").first(Types.TOKEN).put(Types.PART_OF_SPEECH, PartOfSpeech.VBG);
//    document.getAnnotationSet().setIsCompleted(Types.PART_OF_SPEECH, true, "");
//    Pipeline.process(document, Types.LEMMA);
//    assertEquals("be walk", document.find("was walking").getLemma());
  }
}

