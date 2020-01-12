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

package com.gengoai.hermes.extraction.lyre;

import com.gengoai.collection.tree.Span;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.hermes.*;
import com.gengoai.hermes.annotator.DocumentProvider;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconManager;
import com.gengoai.hermes.lexicon.TrieLexicon;
import com.gengoai.hermes.morphology.StopWords;
import com.gengoai.hermes.ner.Entities;
import com.gengoai.math.Math2;
import com.gengoai.string.Strings;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static org.junit.Assert.*;


/**
 * @author David B. Bracewell
 */
public class LyreExpressionTest {
   private Document document = null;


   private static Object processList(List<?> list) {
      if (list.isEmpty()) {
         return null;
      }
      return list.size() == 1 ? list.get(0) : list;
   }


   @Test
   public void annotation() {
      final Document pos = Document.create("I am a test.");
      pos.annotate(Types.SENTENCE);
      pos.annotationBuilder(Types.PHRASE_CHUNK)
         .bounds(pos.tokenAt(0))
         .attribute(Types.PART_OF_SPEECH, POS.NP)
         .createAttached();
      pos.annotationBuilder(Types.PHRASE_CHUNK)
         .bounds(pos.tokenAt(1))
         .attribute(Types.PART_OF_SPEECH, POS.VP)
         .createAttached();
      pos.annotationBuilder(Types.PHRASE_CHUNK)
         .bounds(pos.tokenAt(2).union(pos.tokenAt(3)))
         .attribute(Types.PART_OF_SPEECH, POS.NP)
         .createAttached();
      testLambda(Lyre.parse("@PHRASE_CHUNK($_)"),
                 pos,
                 h -> !h.contentEqualsIgnoreCase("."),
                 h -> h.contentEqualsIgnoreCase(".") ? null : h.annotations(Types.PHRASE_CHUNK).get(0).toString(),
                 h -> h.contentEqualsIgnoreCase(".") ? null : h.annotations(Types.PHRASE_CHUNK).get(0),
                 h -> Double.NaN);

   }

   @Test
   public void array() {
      testLambda(Lyre.parse("[1,2, 'abc']"),
                 document,
                 h -> true,
                 h -> Arrays.toString(new Object[]{1.0, 2.0, "abc"}),
                 h -> Arrays.asList(1.0, 2.0, "abc"),
                 h -> Double.NaN);
      testLambda(Lyre.parse("[ ]"),
                 document,
                 h -> false,
                 h -> Arrays.toString(new Object[]{}),
                 h -> Collections.emptyList(),
                 h -> Double.NaN);
   }

   @Test
   public void attrAndTag() {
      final Document pos = Document.create("I am a test.");
      pos.annotate(Types.SENTENCE);
      pos.tokenAt(0).put(Types.PART_OF_SPEECH, POS.PRP);
      pos.tokenAt(0).put(Types.CONFIDENCE, 0.8);
      pos.tokenAt(1).put(Types.PART_OF_SPEECH, POS.VERB);
      pos.tokenAt(1).put(Types.CATEGORY, Collections.singleton("A"));
      pos.tokenAt(2).put(Types.PART_OF_SPEECH, POS.DT);
      pos.tokenAt(3).put(Types.PART_OF_SPEECH, POS.NN);
      pos.tokenAt(4).put(Types.PART_OF_SPEECH, POS.PERIOD);
      testLambda(Lyre.parse("$PART_OF_SPEECH"),
                 pos,
                 h -> true,
                 h -> h.pos().toString(),
                 HString::pos,
                 h -> Double.NaN);
      testLambda(Lyre.parse("$PART_OF_SPEECH = 'NOUN'"),
                 pos,
                 h -> h.pos().isNoun(),
                 h -> Boolean.toString(h.pos().isNoun()),
                 h -> h.pos().isNoun(),
                 h -> h.pos().isNoun() ? 1.0 : 0.0);
      testLambda(Lyre.parse("#NOUN"),
                 pos,
                 h -> h.pos().isNoun(),
                 h -> Boolean.toString(h.pos().isNoun()),
                 h -> h.pos().isNoun(),
                 h -> h.pos().isNoun() ? 1.0 : 0.0);
   }

   @Test
   public void concatenation() {
      testLambda(Lyre.parse("$_ + 'ss'"),
                 document,
                 h -> true,
                 h -> h.toString() + "ss",
                 h -> h.toString() + "ss",
                 h -> Double.NaN);

      testLambda(Lyre.parse("1 + 2"),
                 document,
                 h -> true,
                 h -> Double.toString(3.0),
                 h -> 3.0,
                 h -> 3);

      testLambda(Lyre.parse("[1] + 2"),
                 document,
                 h -> true,
                 h -> Arrays.toString(new double[]{1, 2}),
                 h -> new double[]{1, 2},
                 h -> Double.NaN);

   }

   @Test
   public void contentWord() {
      testLambda(Lyre.parse("isContentWord"),
                 document,
                 StopWords.isStopWord().negate(),
                 h -> Boolean.toString(!StopWords.isStopWord().test(h)),
                 h -> !StopWords.isStopWord().test(h),
                 h -> !StopWords.isStopWord().test(h) ? 1 : 0);
   }


   @Test
   public void context() {
      Document document = Document.create("Token1 Token2 Token3");
      document.annotate(Types.TOKEN);
      LyreExpression lambda = Lyre.parse("cxt($_, -1)");
      for (int i = 0; i < document.tokenLength(); i++) {
         HString p = lambda.applyAsHString(document.tokenAt(i));
         if (i == 0) {
            assertTrue(p.isEmpty());
         } else {
            assertEquals("Token" + i, p.toString());
         }
      }
      lambda = Lyre.parse("cxt($_, 1)");
      for (int i = 0; i < document.tokenLength(); i++) {
         HString p = lambda.applyAsHString(document.tokenAt(i));
         if (i == document.tokenLength() - 1) {
            assertTrue(p.isEmpty());
         } else {
            assertEquals("Token" + (i + 2), p.toString());
         }
      }
   }


   @Test
   public void dep() {
      final Document pos = Document.create("I am a test.");
      pos.annotate(Types.SENTENCE);
      pos.tokenAt(0).put(Types.PART_OF_SPEECH, POS.PRP);
      pos.tokenAt(1).put(Types.PART_OF_SPEECH, POS.VERB);
      pos.tokenAt(2).put(Types.PART_OF_SPEECH, POS.DT);
      pos.tokenAt(3).put(Types.PART_OF_SPEECH, POS.NN);
      pos.tokenAt(4).put(Types.PART_OF_SPEECH, POS.PERIOD);
      pos.tokenAt(0).add(new Relation(Types.DEPENDENCY, "nsubj", pos.tokenAt(1).getId()));
      pos.tokenAt(2).add(new Relation(Types.DEPENDENCY, "det", pos.tokenAt(3).getId()));
      pos.tokenAt(3).add(new Relation(Types.DEPENDENCY, "dobj", pos.tokenAt(1).getId()));
      pos.tokenAt(4).add(new Relation(Types.DEPENDENCY, "dep", pos.tokenAt(1).getId()));
      testLambda(Lyre.parse("@<dep"),
                 pos,
                 h -> h.children().size() > 0,
                 h -> h.children().isEmpty() ? null : processList(h.children()).toString(),
                 h -> processList(h.children()),
                 h -> Double.NaN);
      testLambda(Lyre.parse("@>dep"),
                 pos,
                 h -> !h.dependency().v2.isEmpty(),
                 h -> String.valueOf(h.dependency().v2),
                 h -> h.dependency().v2,
                 h -> Double.NaN);
      testLambda(Lyre.parse("@<dep{'nsubj'}"),
                 pos,
                 h -> h.children("nsubj").size() > 0,
                 h -> h.children("nsubj").isEmpty() ? null : processList(h.children("nsubj")).toString(),
                 h -> processList(h.children("nsubj")),
                 h -> Double.NaN);
      testLambda(Lyre.parse("@>dep{'nsubj'}"),
                 pos,
                 h -> h.dependencyIsA("nsubj"),
                 h -> h.dependencyIsA("nsubj") ? h.dependency().v2.toString() : null,
                 h -> h.dependencyIsA("nsubj") ? h.dependency().v2 : null,
                 h -> Double.NaN);
      testLambda(Lyre.parse("@<DEPENDENCY"),
                 pos,
                 h -> h.children().size() > 0,
                 h -> h.children().isEmpty() ? null : processList(h.children()).toString(),
                 h -> processList(h.children()),
                 h -> Double.NaN);
      testLambda(Lyre.parse("@>DEPENDENCY"),
                 pos,
                 h -> !h.dependency().v2.isEmpty(),
                 h -> h.dependency().v2.isEmpty() ? null : h.dependency().v2.toString(),
                 h -> h.dependency().v2.isEmpty() ? null : h.dependency().v2,
                 h -> Double.NaN);
      testLambda(Lyre.parse("@<DEPENDENCY{'nsubj'}"),
                 pos,
                 h -> h.children("nsubj").size() > 0,
                 h -> h.children("nsubj").isEmpty() ? null : processList(h.children("nsubj")).toString(),
                 h -> processList(h.children("nsubj")),
                 h -> Double.NaN);
      testLambda(Lyre.parse("@>DEPENDENCY{'nsubj'}"),
                 pos,
                 h -> h.dependencyIsA("nsubj"),
                 h -> h.dependencyIsA("nsubj") ? h.dependency().v2.toString() : null,
                 h -> h.dependencyIsA("nsubj") ? h.dependency().v2 : null,
                 h -> Double.NaN);
   }

   @Test
   public void exists() {
      testLambda(Lyre.parse("exists"),
                 document,
                 Strings::isNotNullOrBlank,
                 h -> Boolean.toString(Strings.isNotNullOrBlank(h)),
                 Strings::isNotNullOrBlank,
                 h -> Strings.isNotNullOrBlank(h) ? 1 : 0);
   }

   @Test
   public void falseValue() {
      testLambda(Lyre.parse("false"),
                 document,
                 h -> false,
                 h -> "false",
                 h -> false,
                 h -> 0);
   }

   @Test
   public void ifTest() {
      testLambda(Lyre.parse("if( isStopWord, true, false )"),
                 document,
                 StopWords.isStopWord(),
                 h -> Boolean.toString(StopWords.isStopWord().test(h)),
                 h -> StopWords.isStopWord().test(h),
                 h -> StopWords.isStopWord().test(h) ? 1 : 0);

      testLambda(Lyre.parse("if( isStopWord, '', $_ )"),
                 document,
                 h -> !StopWords.isStopWord().test(h),
                 h -> StopWords.isStopWord().test(h) ? "" : h.toString(),
                 h -> StopWords.isStopWord().test(h) ? "" : h,
                 h -> Double.NaN);

      testLambda(Lyre.parse("if( isStopWord, null, $_ )"),
                 document,
                 h -> !StopWords.isStopWord().test(h),
                 h -> StopWords.isStopWord().test(h) ? null : h.toString(),
                 h -> StopWords.isStopWord().test(h) ? null : h,
                 h -> Double.NaN);
   }

   @Test
   public void inf() {
      testLambda(Lyre.parse("INF"),
                 document,
                 h -> false,
                 h -> Double.toString(Double.POSITIVE_INFINITY),
                 h -> Double.POSITIVE_INFINITY,
                 h -> Double.POSITIVE_INFINITY);
   }

   @Test
   public void isAlphaNumeric() {
      testLambda(Lyre.parse("isAlphaNumeric"),
                 document,
                 Strings::isAlphaNumeric,
                 h -> Boolean.toString(Strings.isAlphaNumeric(h)),
                 Strings::isAlphaNumeric,
                 h -> Strings.isAlphaNumeric(h) ? 1 : 0);
   }

   @Test
   public void isDigit() {
      testLambda(Lyre.parse("isDigit"),
                 document,
                 Strings::isDigit,
                 h -> Boolean.toString(Strings.isDigit(h)),
                 Strings::isDigit,
                 h -> Strings.isDigit(h) ? 1 : 0);
   }

   @Test
   public void isLetter() {
      testLambda(Lyre.parse("isLetter"),
                 document,
                 Strings::isLetter,
                 h -> Boolean.toString(Strings.isLetter(h)),
                 Strings::isLetter,
                 h -> Strings.isLetter(h) ? 1 : 0);
   }

   @Test
   public void isLower() {
      testLambda(Lyre.parse("isLower"),
                 document,
                 Strings::isLowerCase,
                 h -> Boolean.toString(Strings.isLowerCase(h)),
                 Strings::isLowerCase,
                 h -> Strings.isLowerCase(h) ? 1 : 0);
   }

   @Test
   public void isPunctuation() {
      testLambda(Lyre.parse("isPunctuation"),
                 document,
                 Strings::isPunctuation,
                 h -> Boolean.toString(Strings.isPunctuation(h)),
                 Strings::isPunctuation,
                 h -> Strings.isPunctuation(h) ? 1 : 0);
   }

   @Test
   public void isUpper() {
//      testLambda(Lyre.parse("isUpper"),
//                 document,
//                 Strings::isUpperCase,
//                 h -> Boolean.toString(Strings.isUpperCase(h)),
//                 Strings::isUpperCase,
//                 h -> Strings.isUpperCase(h) ? 1 : 0);


      List<Object> list = Lyre.parse("isUpper(['A','b',['C','d','E']])").applyAsList(
         Fragments.detachedEmptyAnnotation());
      List<Object> expected = Arrays.asList("A", Arrays.asList("C", "E"));
      assertEquals(expected, list);
   }

   @Test
   public void lemma() {
      testLambda(Lyre.parse("lemma"),
                 document,
                 h -> true,
                 HString::getLemma,
                 HString::getLemma,
                 h -> Double.NaN);
   }

   @Test
   public void len() {
      testLambda(Lyre.parse("len"),
                 document,
                 h -> true,
                 h -> Double.toString(h.length()),
                 h -> (double) h.length(),
                 Span::length);

      assertEquals(3.0, Lyre.parse("len(['1','2','3'])").applyAsDouble(Fragments.string("test")), 0.0);
   }

   @Test
   public void lexicon() {
      Lexicon lexicon = TrieLexicon.builder(Types.ENTITY_TYPE, false)
                                   .add("alice", Entities.PERSON).build();
      LexiconManager.register("alice", lexicon);
      testLambda(Lyre.parse("%alice"),
                 document,
                 h -> h.contentEqualsIgnoreCase("alice"),
                 h -> lexicon.toString(),
                 h -> lexicon,
                 h -> Double.NaN);
   }

   @Test
   public void literal() {
      testLambda(Lyre.parse("'literal expression'"),
                 document,
                 h -> true,
                 h -> "literal expression",
                 h -> "literal expression",
                 h -> Double.NaN);
   }

   @Test
   public void lower() {
      testLambda(Lyre.parse("lower"),
                 document,
                 h -> true,
                 HString::toLowerCase,
                 HString::toLowerCase,
                 h -> Double.NaN);
   }

   @Test
   public void nan() {
      testLambda(Lyre.parse("NaN"),
                 document,
                 h -> false,
                 h -> "NaN",
                 h -> Double.NaN,
                 h -> Double.NaN);
   }

   @Test
   public void negLookAhead() {
      testLambda(Lyre.parse("(?!> $_ = '.')"),
                 document,
                 h -> !h.next(Types.TOKEN).contentEquals("."),
                 h -> Boolean.toString(!h.next(Types.TOKEN).contentEquals(".")),
                 h -> !h.next(Types.TOKEN).contentEquals("."),
                 h -> !h.next(Types.TOKEN).contentEquals(".") ? 1 : 0
                );

      testLambda(Lyre.parse("(?!> ~)"),
                 document,
                 h -> false,
                 h -> Boolean.toString(false),
                 h -> false,
                 h -> 0.0
                );
   }

   @Test
   public void negLookBehind() {
      testLambda(Lyre.parse("(?!< $_ = '.')"),
                 document,
                 h -> !h.previous(Types.TOKEN).contentEquals("."),
                 h -> Boolean.toString(!h.previous(Types.TOKEN).contentEquals(".")),
                 h -> !h.previous(Types.TOKEN).contentEquals("."),
                 h -> !h.previous(Types.TOKEN).contentEquals(".") ? 1 : 0
                );

      testLambda(Lyre.parse("(?!< ~)"),
                 document,
                 h -> false,
                 h -> Boolean.toString(false),
                 h -> false,
                 h -> 0.0
                );
   }

   @Test
   public void neginf() {
      testLambda(Lyre.parse("-INF"),
                 document,
                 h -> false,
                 h -> Double.toString(Double.NEGATIVE_INFINITY),
                 h -> Double.NEGATIVE_INFINITY,
                 h -> Double.NEGATIVE_INFINITY);
   }

   @Test
   public void number() {
      testLambda(Lyre.parse("10.3"),
                 document,
                 h -> true,
                 h -> "10.3",
                 h -> 10.3,
                 h -> 10.3);
      testLambda(Lyre.parse("1e-4"),
                 document,
                 h -> true,
                 h -> Double.toString(1e-4),
                 h -> 1e-4,
                 h -> 1e-4);
      testLambda(Lyre.parse("1"),
                 document,
                 h -> true,
                 h -> Double.toString(1),
                 h -> 1.0,
                 h -> 1.0);
   }

   @Test
   public void pos() {
      final Document pos = Document.create("I am a test.");
      pos.annotate(Types.SENTENCE);
      pos.tokenAt(0).put(Types.PART_OF_SPEECH, POS.PRP);
      pos.tokenAt(1).put(Types.PART_OF_SPEECH, POS.VERB);
      pos.tokenAt(2).put(Types.PART_OF_SPEECH, POS.DT);
      pos.tokenAt(3).put(Types.PART_OF_SPEECH, POS.NN);
      pos.tokenAt(4).put(Types.PART_OF_SPEECH, POS.PERIOD);
      LyreExpression posFunc = Lyre.parse("pos");

//      testLambda(posFunc,
//                 pos,
//                 h -> true,
//                 h -> h.pos().toString(),
//                 HString::pos,
//                 h -> Double.NaN);


      assertFalse(posFunc.test(Fragments.detachedEmptyHString()));

      posFunc = Lyre.parse("pos(@TOKEN)");
      List<POS> list = Cast.as(posFunc.applyAsObject(pos));
      assertEquals(POS.PRP, list.get(0));
      assertEquals(POS.VERB, list.get(1));
      assertEquals(POS.DT, list.get(2));
      assertEquals(POS.NN, list.get(3));
      assertEquals(POS.PERIOD, list.get(4));

   }

   @Test
   public void posLookAhead() {
      testLambda(Lyre.parse("(?> $_ = '.')"),
                 document,
                 h -> h.next(Types.TOKEN).contentEquals("."),
                 h -> Boolean.toString(h.next(Types.TOKEN).contentEquals(".")),
                 h -> h.next(Types.TOKEN).contentEquals("."),
                 h -> h.next(Types.TOKEN).contentEquals(".") ? 1 : 0
                );

      testLambda(Lyre.parse("(?> ~)"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> true,
                 h -> 1.0
                );
   }

   @Test
   public void posLookBehind() {
      testLambda(Lyre.parse("(?< $_ = '.')"),
                 document,
                 h -> h.previous(Types.TOKEN).contentEquals("."),
                 h -> Boolean.toString(h.previous(Types.TOKEN).contentEquals(".")),
                 h -> h.previous(Types.TOKEN).contentEquals("."),
                 h -> h.previous(Types.TOKEN).contentEquals(".") ? 1 : 0
                );

      testLambda(Lyre.parse("(?< ~)"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> true,
                 h -> 1.0
                );
   }

   @Test
   public void regex() {
      testLambda(Lyre.parse("/.*/"),
                 document,
                 h -> true,
                 h -> "true",
                 h -> true,
                 h -> 1.0);

   }

   @Before
   public void setUp() throws Exception {
      Config.initializeTest();
      Config.loadPackageConfig("com.gengoai.hermes");
      document = DocumentProvider.getAnnotatedDocument();
      Lexicon ner = TrieLexicon.builder(Types.ENTITY_TYPE, false)
                               .add("Alice", Entities.PERSON)
                               .add("rabbit-hole", Entities.LOCATION)
                               .add("daisies", Entities.PRODUCT)
                               .add("sister", Entities.PERSON)
                               .build();
      ner.extract(document)
         .forEach(h -> document.annotationBuilder(Types.ENTITY).from(h).createAttached());
   }

   @Test
   public void slice() {
      testLambda(Lyre.parse("$_[:1]"),
                 document,
                 h -> true,
                 h -> h.substring(0, 1).toString(),
                 h -> h.substring(0, 1),
                 h -> Math2.tryParseDouble(h.substring(0, 1).toString()) == null
                      ? Double.NaN
                      : Double.parseDouble(h.substring(0, 1).toString())
                );
      testLambda(Lyre.parse("$_[0:1]"),
                 document,
                 h -> true,
                 h -> h.substring(0, 1).toString(),
                 h -> h.substring(0, 1),
                 h -> Math2.tryParseDouble(h.substring(0, 1).toString()) == null
                      ? Double.NaN
                      : Double.parseDouble(h.substring(0, 1).toString())
                );
   }

   @Test
   public void stem() {
      testLambda(Lyre.parse("stem"),
                 document,
                 h -> true,
                 HString::getStemmedForm,
                 HString::getStemmedForm,
                 h -> Double.NaN);
   }

   @Test
   public void stopword() {
      testLambda(Lyre.parse("isStopWord"),
                 document,
                 StopWords.isStopWord(),
                 h -> Boolean.toString(StopWords.isStopWord().test(h)),
                 h -> StopWords.isStopWord().test(h),
                 h -> StopWords.isStopWord().test(h) ? 1 : 0);
   }

   @Test
   public void substitute() {
      testLambda(Lyre.parse("s/.*/1/"),
                 document,
                 h -> true,
                 h -> "1",
                 h -> "1",
                 h -> 1.0);
      testLambda(Lyre.parse("s/.*//"),
                 document,
                 h -> false,
                 h -> "",
                 h -> "",
                 h -> Double.NaN);
   }

   @Test
   public void testAny() {
      testLambda(Lyre.parse("~"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> true,
                 h -> 1.0);
   }

   @Test
   public void testApplies() {
      testLambda(Lyre.parse("'alpha' ~= upper"),
                 document,
                 h -> true,
                 h -> "ALPHA",
                 h -> "ALPHA",
                 h -> Double.NaN
                );
      testLambda(Lyre.parse("upper('alpha' ~= s/a/i/g)"),
                 document,
                 h -> true,
                 h -> "ILPHI",
                 h -> "ILPHI",
                 h -> Double.NaN
                );
      testLambda(Lyre.parse("$_ ~= upper"),
                 document,
                 h -> true,
                 HString::toUpperCase,
                 HString::toUpperCase,
                 h -> Double.NaN
                );

   }

   @Test
   public void testComparison() {
      testLambda(Lyre.parse("192 < 1000"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("192 <= 192"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("192 > 10"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("192 >= 192"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("'a' < 'b'"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("'a' <= 'a'"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("'z' > 'a'"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("'zaa' >= 'za'"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
   }

   @Test
   public void testEquality() {
      testLambda(Lyre.parse("$_ = 'a'"),
                 document,
                 h -> h.contentEquals("a"),
                 h -> Boolean.toString(h.contentEquals("a")),
                 h -> h.contentEquals("a"),
                 h -> h.contentEquals("a") ? 1.0 : 0.0
                );
      testLambda(Lyre.parse("$_ != 'a'"),
                 document,
                 h -> !h.contentEquals("a"),
                 h -> Boolean.toString(!h.contentEquals("a")),
                 h -> !h.contentEquals("a"),
                 h -> !h.contentEquals("a") ? 1.0 : 0.0
                );
      testLambda(Lyre.parse("$_ = $_"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("$_ != $_"),
                 document,
                 h -> false,
                 h -> Boolean.toString(false),
                 h -> Boolean.FALSE,
                 h -> 0.0
                );
      testLambda(Lyre.parse("192.0 = 192"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("192.0 != 122"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("[192.0, 12] = [192, 12]"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("[192.0, 12] != 12"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
   }

   @Test
   public void testHas() {
      testLambda(Lyre.parse("$_ has @TOKEN"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
      testLambda(Lyre.parse("$_ has $TOKEN_TYPE"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );
   }

   @Test
   public void testIn() {
      testLambda(Lyre.parse("$_ in ['Alice', 'rabbit']"),
                 document,
                 h -> h.contentEquals("Alice") || h.contentEquals("rabbit"),
                 h -> Boolean.toString(h.contentEquals("Alice") || h.contentEquals("rabbit")),
                 h -> h.contentEquals("Alice") || h.contentEquals("rabbit"),
                 h -> (h.contentEquals("Alice") || h.contentEquals("rabbit")) ? 1 : 0
                );

      testLambda(Lyre.parse("192  in [102, 45, 192]"),
                 document,
                 h -> true,
                 h -> Boolean.toString(true),
                 h -> Boolean.TRUE,
                 h -> 1.0
                );

      testLambda(Lyre.parse("'lice' in $_"),
                 document,
                 h -> h.contains("lice"),
                 h -> Boolean.toString(h.contains("lice")),
                 h -> h.contains("lice"),
                 h -> (h.contains("lice")) ? 1 : 0
                );

   }

   private void testLambda(LyreExpression it,
                           Document document,
                           Predicate<HString> predicate,
                           Function<HString, String> toString,
                           Function<HString, Object> toObject,
                           ToDoubleFunction<HString> toDouble
                          ) {
      for (int i = 0; i < document.tokenLength(); i++) {
         HString t = document.tokenAt(i);
         assertEquals(predicate.test(t), it.test(t));
         assertEquals(toString.apply(t), it.apply(t));
         Object l = toObject.apply(t);
         Object r = it.applyAsObject(t);
         if (l instanceof HString && r instanceof HString) {
            assertEquals(l.toString(), r.toString());
         } else if (l != null && l.getClass().isArray()) {
            List<Object> ll = Converter.convertSilently(l, parameterizedType(List.class, Object.class));
            List<Object> rr = Converter.convertSilently(r, parameterizedType(List.class, Object.class));
            assertEquals(ll, rr);
         } else {
            assertEquals(l, r);
         }
         assertEquals(toDouble.applyAsDouble(t), it.applyAsDouble(t), 0d);
      }
   }

   @Test
   public void testListFunctions() {
      List<String> list = Cast.as(Lyre.parse("map(['a','b','c','d', ['e']], upper)")
                                      .applyAsObject(null));
      assertEquals(Arrays.asList("A", "B", "C", "D", "E"), list);
      list = Cast.as(Lyre.parse("string(filter(['A','B','C','D'], !/[aieou]/i))")
                         .applyAsObject(null));
      assertEquals(Arrays.asList("B", "C", "D"), list);

      Object o = Lyre.parse("map(['a'], upper)").applyAsObject(null);
      assertEquals(Collections.singletonList("A"), o);
      o = Lyre.parse("map([], upper)").applyAsObject(null);
      assertTrue(Cast.<Collection>as(o).isEmpty());
      o = Lyre.parse("get([], 0)").applyAsObject(null);
      assertNull(o);
      o = Lyre.parse("first(['A', 'B'])").applyAsObject(null);
      assertEquals("A", o);
      o = Lyre.parse("last(['A', 'B', 'C'])").applyAsObject(null);
      assertEquals("C", o);


      list = Lyre.parse("flatten(['A', 'B', ['C',['D']]])").applyAsList(null, String.class);
      assertEquals(Arrays.asList("A", "B", "C", "D"), list);
   }

   @Test
   public void testLogic() {
      assertTrue(Lyre.parse("'a' = 'a' && 1 < 2").test(null));
      assertTrue(Lyre.parse("'a' = 'b' || 1 < 2").test(null));
      assertTrue(Lyre.parse("'a' = 'b' || 'z'").test(null));
      assertTrue(Lyre.parse("!('a' = 'b' && 1 < 2)").test(null));
      assertTrue(Lyre.parse("true || false").test(null));
      assertTrue(Lyre.parse("true ^ false").test(null));
   }

   @Test
   public void testThis() {
      testLambda(Lyre.parse("$_"),
                 document,
                 h -> true,
                 HString::toString,
                 h -> h,
                 h -> Double.NaN);

      testLambda(Lyre.parse("[$_]"),
                 document,
                 h -> true,
                 h -> Collections.singletonList(h.toString()).toString(),
                 Collections::singletonList,
                 h -> Double.NaN);


   }

   @Test
   public void trim() {
      testLambda(Lyre.parse("trim"),
                 document,
                 StopWords.isNotStopWord(),
                 h -> StopWords.isStopWord().test(h) ? "" : h.toString(),
                 h -> StopWords.isStopWord().test(h) ? Fragments.detachedEmptyAnnotation() : h,
                 h -> Double.NaN
                );

      testLambda(Lyre.parse("trim($_, /[aieou]/)"),
                 document,
                 h -> !h.matcher("[aieou]").find(),
                 h -> h.matcher("[aieou]").find() ? "" : h.toString(),
                 h -> h.matcher("[aieou]").find() ? Fragments.detachedEmptyAnnotation() : h,
                 h -> Double.NaN
                );


   }

   @Test
   public void trueValue() {
      testLambda(Lyre.parse("true"),
                 document,
                 h -> true,
                 h -> "true",
                 h -> true,
                 h -> 1);
   }

   @Test
   public void upper() {
      testLambda(Lyre.parse("upper"),
                 document,
                 h -> true,
                 HString::toUpperCase,
                 HString::toUpperCase,
                 h -> Double.NaN);
   }
}