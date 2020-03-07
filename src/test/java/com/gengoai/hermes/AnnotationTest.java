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

import com.gengoai.config.Config;
import com.gengoai.hermes.annotator.DocumentProvider;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.hermes.ner.Entities;
import com.gengoai.hermes.ner.EntityType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gengoai.hermes.Types.*;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class AnnotationTest {

   @Test
   public void makeTest() {
      Annotation a = new DefaultAnnotationImpl(Fragments.string("test"), ENTITY);
      a.put(ENTITY_TYPE, Entities.EMAIL);

      assertNotNull(a);
      assertNull(a.document());
      assertEquals(ENTITY, a.getType());
      assertTrue(a.isInstance(ENTITY));

      assertTrue(a.isAnnotation());

      assertTrue(a.tagIsA(Entities.INTERNET));
      assertTrue(a.tagIsA("INTERNET"));


   }

   @Test
   public void nextPreviousTest() {
      Document document = DocumentFactory.getInstance().fromTokens(Arrays.asList("This", "is", "simple"));
      List<Annotation> tokens = document.tokens();
      assertTrue(tokens.get(0).previous().isEmpty());
      assertTrue(tokens.get(0).previous().isDetached());
      assertEquals("is", tokens.get(0).next().toString());
      assertEquals("This", tokens.get(1).previous().toString());
      assertEquals("simple", tokens.get(1).next().toString());
      assertEquals("is", tokens.get(2).previous().toString());
      assertTrue(tokens.get(2).next().isEmpty());
   }

   @Test
   public void parentChildTest() {
      Document document = DocumentFactory.getInstance().fromTokens(Arrays.asList("This", "is", "simple"));
      List<Annotation> tokens = document.tokens();
      tokens.get(0).add(new Relation(DEPENDENCY, "nsubj", tokens.get(2).getId()));
      tokens.get(1).add(new Relation(DEPENDENCY, "cop", tokens.get(2).getId()));
      assertFalse(tokens.get(0).parent().isEmpty());
      assertEquals("simple", tokens.get(0).parent().toString());
      assertEquals(1, tokens.get(0).outgoingRelations(DEPENDENCY).size(), 0d);
      assertEquals("simple", tokens.get(0).outgoing(DEPENDENCY, "nsubj").get(0).toString());
      assertEquals("simple", tokens.get(0).outgoing(DEPENDENCY).get(0).toString());
      assertFalse(tokens.get(1).parent().isEmpty());
      assertEquals("simple", tokens.get(1).parent().toString());
      assertEquals(0, tokens.get(2).enclosedAnnotations().size());

//      Pipeline.process(document, SENTENCE);
//      assertEquals(2, tokens.get(2).enclosedAnnotations().size());
//      tokens.get(0).remove(tokens.get(0).outgoingRelations(DEPENDENCY).iterator().next());
//      assertEquals(1, tokens.get(2).enclosedAnnotations().size());
//
//      Annotation sentence = document.first(SENTENCE);
//      assertFalse(sentence.isEmpty());
//      assertEquals(sentence, tokens.get(0).first(SENTENCE));
//      assertEquals(sentence, tokens.get(1).first(SENTENCE));
//      assertEquals(sentence, tokens.get(2).first(SENTENCE));
   }

   @Test
   public void putAttributeTest() {
      Document document = DocumentProvider.getAnnotatedDocument();
      List<Annotation> tokens = document.tokens();

      TokenType type = tokens.get(0).attribute(TOKEN_TYPE);
      tokens.get(0).computeIfAbsent(TOKEN_TYPE, () -> TokenType.make("REALLYSTRANGETYPE"));
      assertEquals(type, tokens.get(0).attribute(TOKEN_TYPE));

      tokens.get(0).computeIfAbsent(TOKEN_TYPE, () -> TokenType.make("REALLYSTRANGETYPE"));
      assertEquals(type, tokens.get(0).attribute(TOKEN_TYPE));

      tokens.get(0).computeIfAbsent(ENTITY_TYPE, () -> EntityType.make("REALLYSTRANGETYPE"));
      assertEquals(EntityType.make("REALLYSTRANGETYPE"), tokens.get(0).attribute(ENTITY_TYPE));

      tokens.get(0).removeAttribute(CATEGORY);
      tokens.get(0).computeIfAbsent(CATEGORY, () -> Collections.singleton(BasicCategories.ANGIOSPERMS));
      assertEquals(Collections.singleton(BasicCategories.ANGIOSPERMS), tokens.get(0).attribute(CATEGORY));

   }

   @Test
   public void sentenceTest() {
      Document document = DocumentProvider.getAnnotatedDocument();
      for (Annotation token : document.tokens()) {
         assertEquals(1, token.sentences().size(), 0d);
         assertEquals(token.sentences().get(0), token.last(SENTENCE));
         assertEquals(token.sentences().get(0), token.first(SENTENCE));
         assertEquals(token, token.firstToken());
         assertEquals(token, token.lastToken());
      }
   }

   @Before
   public void setUp() throws Exception {
      Config.initializeTest();
   }


}
