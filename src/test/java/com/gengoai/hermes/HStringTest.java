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
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class HStringTest {

   @Before
   public void setUp() throws Exception {
      Config.initializeTest();
   }

   @Test
   public void testCharNGrams() {
      HString hString = Fragments.string("abcdef");
      List<HString> unigrams = hString.charNGrams(1);
      assertEquals(6, unigrams.size());
      List<HString> bigrams = hString.charNGrams(2);
      assertEquals(5, bigrams.size());
      List<HString> trigrams = hString.charNGrams(3);
      assertEquals(4, trigrams.size());
   }

   @Test
   public void testCounts() {
      Document document = DocumentFactory.getInstance().create(
         "Once upon a time there lived a princess who was stuck in time.");
      document.annotate(Types.TOKEN);

      List<HString> patterns = document.findAllPatterns(Pattern.compile("\\ba\\s+\\w+\\b")).collect(
         Collectors.toList());
      assertTrue(patterns.get(0).contentEquals("a time"));
      assertTrue(patterns.get(1).contentEquals("a princess"));

      patterns = document.findAll("a time").collect(Collectors.toList());
      assertEquals(1, patterns.size(), 0d);
      assertTrue(patterns.get(0).contentEquals("a time"));

      assertTrue(document.find("z").isEmpty());
      assertTrue(document.find("c").start() == 0);

      assertTrue(document.tokenAt(0).isAnnotation());
      assertTrue(document.tokenAt(0).matches("(?i)once"));

      assertTrue(document.isDocument());
   }

   @Test
   public void testStringFunctions() {
      HString hString = Fragments.string("abcdef");
      assertTrue(hString.contentEquals("abcdef"));
      assertTrue(hString.contentEqualsIgnoreCase("ABCDEF"));

      assertEquals("abcdef", hString.toLowerCase());
      assertEquals("ABCDEF", hString.toUpperCase());
      assertEquals("gbcdef", hString.replace("a", "g"));
      assertEquals("gbcdgf", hString.replaceAll("[aieou]", "g"));
      assertEquals("gbcdef", hString.replaceFirst("[aieou]", "g"));

      Matcher m = hString.matcher("[aieou]");
      assertTrue(m.find());
      assertEquals("a", m.group());

      List<HString> patterns = hString.findAllPatterns(Pattern.compile("[aieou]")).collect(Collectors.toList());
      assertEquals(2, patterns.size(), 0d);
      assertTrue(patterns.get(0).contentEquals("a"));
      assertTrue(patterns.get(1).contentEquals("e"));

      patterns = hString.findAll("a").collect(Collectors.toList());
      assertEquals(1, patterns.size(), 0d);
      assertTrue(patterns.get(0).contentEquals("a"));

      assertTrue(hString.find("z").isEmpty());
      assertTrue(hString.find("a").start() == 0);

      assertEquals(0, hString.indexOf("a"));
      assertEquals(-1, hString.indexOf("x"));
      assertEquals(-1, hString.indexOf("a", 1));

      assertTrue(hString.isAnnotation());

      assertFalse(hString.isDocument());
   }

   @Test
   public void testTokenNgrams() {
      Document document = DocumentFactory.getInstance().create(
         "Once upon a time there lived a princess who was stuck in time.");
      document.annotate(Types.TOKEN);

//      List<HString> ngrams = NGramExtractor.unigrams().stream(document).collect(Collectors.toList());
//      assertEquals(14, ngrams.size());
//
//      ngrams = NGramExtractor.bigrams().setAnnotationTypes(Types.TOKEN).stream(document).collect(Collectors.toList());
//      assertEquals(13, ngrams.size());
   }

   @Test
   public void testTokenPatterns() {
      Document document = DocumentFactory.getInstance().create(
         "Once upon a time there lived a princess who was stuck in time.");
      document.annotate(Types.TOKEN, Types.SENTENCE);
      List<HString> patterns = document.findAllPatterns(Pattern.compile("\\ba\\s+\\w+\\b")).collect(
         Collectors.toList());
      assertEquals(2, patterns.size(), 0d);
      assertTrue(patterns.get(0).contentEquals("a time"));
      assertTrue(patterns.get(1).contentEquals("a princess"));

      patterns = document.findAll("a time").collect(Collectors.toList());
      assertEquals(1, patterns.size(), 0d);
      assertTrue(patterns.get(0).contentEquals("a time"));

      assertTrue(document.find("z").isEmpty());
      assertTrue(document.find("c").start() == 0);

      assertTrue(document.tokenAt(0).startsWith("O"));
      assertTrue(document.tokenAt(0).endsWith("ce"));

      assertTrue(document.first(Types.SENTENCE).encloses(document.tokenAt(0)));
      assertTrue(document.first(Types.SENTENCE).overlaps(document.tokenAt(0)));

      assertTrue(document.tokenAt(0).isAnnotation());
      assertTrue(document.tokenAt(0).matches("(?i)once"));

      assertTrue(document.isDocument());
   }


}