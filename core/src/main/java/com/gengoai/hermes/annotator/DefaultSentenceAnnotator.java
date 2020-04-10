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

import com.gengoai.hermes.*;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.string.Strings;

import java.util.*;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.hermes.annotator.DefaultSentenceAnnotator.InternalType.*;
import static com.gengoai.tuple.Tuples.$;

/**
 * Default Sentence Annotator that works reasonably well on tokenized text.
 *
 * @author David B. Bracewell
 */
public class DefaultSentenceAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;

   private final char[] endOfSentence = new char[]{
         '\u0021',
         '\u002E',
         '\u002E',
         '\u003F',
         '\u0589',
         '\u061F',
         '\u06D4',
         '\u0700',
         '\u0701',
         '\u0702',
         '\u0964',
         '\u104A',
         '\u104B',
         '\u1362',
         '\u1367',
         '\u1368',
         '\u166E',
         '\u1803',
         '\u1809',
         '\u2024',
         '\u203C',
         '\u203D',
         '\u2047',
         '\u2048',
         '\u2049',
         '\u3002',
         '\uFE52',
         '\uFE52',
         '\uFE57',
         '\uFF01',
         '\uFF0E',
         '\uFF0E',
         '\uFF1F',
         '\uFF61',
   };

   private final char[] sContinue = new char[]{
         '\u002C',
         '\u002D',
         '\u003A',
         '\u055D',
         '\u060C',
         '\u060D',
         '\u07F8',
         '\u1802',
         '\u1808',
         '\u2013',
         '\u2014',
         '\u3001',
         '\uFE10',
         '\uFE11',
         '\uFE13',
         '\uFE31',
         '\uFE32',
         '\uFE50',
         '\uFE51',
         '\uFE55',
         '\uFE58',
         '\uFE63',
         '\uFF0C',
         '\uFF0D',
         '\uFF1A',
         '\uFF64'
   };

   private final Set<String> noSentenceBreak = new HashSet<String>() {
      {
         add("admr.");
         add("al.");
         add("ala.");
         add("jan");
         add("feb");
         add("mar");
         add("apr");
         add("jun");
         add("jul");
         add("aug");
         add("sep");
         add("sept");
         add("oct");
         add("nov");
         add("dec");
         add("alta.");
         add("arc.");
         add("ariz.");
         add("ark.");
         add("atty.");
         add("attys.");
         add("ave.");
         add("bld.");
         add("blvd.");
         add("cal.");
         add("calif.");
         add("cl.");
         add("cmdr.");
         add("co.");
         add("col.");
         add("colo.");
         add("conn.");
         add("corp.");
         add("cpl.");
         add("cres.");
         add("ct.");
         add("dak.");
         add("del.");
         add("det.");
         add("dist.");
         add("dr.");
         add("esp.");
         add("etc.");
         add("exp.");
         add("expy.");
         add("fed.");
         add("fla.");
         add("ft.");
         add("fw?y.");
         add("fy.");
         add("ga.");
         add("gen.");
         add("gov.");
         add("hway.");
         add("hwy.");
         add("ia.");
         add("id.");
         add("ida.");
         add("ill.");
         add("inc.");
         add("ind.");
         add("is.");
         add("jr.");
         add("kan.");
         add("kans.");
         add("ken.");
         add("ky.");
         add("la.");
         add("lt.");
         add("ltd.");
         add("maj.");
         add("man.");
         add("mass.");
         add("md.");
         add("me.");
         add("mex.");
         add("mich.");
         add("minn.");
         add("miss.");
         add("mo.");
         add("mont.");
         add("mr.");
         add("mrs.");
         add("ms.");
         add("mt.");
         add("neb.");
         add("nebr.");
         add("nev.");
         add("ok.");
         add("okla.");
         add("ont.");
         add("ore.");
         add("p.m.");
         add("pa.");
         add("pd.");
         add("pde?.");
         add("penn.");
         add("penna.");
         add("pl.");
         add("plz.");
         add("prof.");
         add("pvt.");
         add("qué.");
         add("rd.");
         add("rep.");
         add("reps.");
         add("rev.");
         add("sask.");
         add("sen.");
         add("sens.");
         add("sgt.");
         add("sr.");
         add("supt.");
         add("tce.");
         add("tenn.");
         add("tex.");
         add("tx.");
         add("u.s.");
         add("u.s.a.");
         add("us.");
         add("usa.");
         add("usafa.");
         add("ut.");
         add("va.");
         add("vs.");
         add("vt.");
         add("wash.");
         add("wis.");
         add("wisc.");
         add("wy.");
         add("wyo.");
         add("yuk.");
         add("st.");
      }
   };

   private boolean addSentence(Document doc, int start, int end, int index) {
      while(start < doc.length() && Character.isWhitespace(doc.charAt(start))) {
         start++;
      }
      if(start <= end) {
         doc.createAnnotation(Types.SENTENCE,
                              start,
                              end,
                              hashMapOf($(Types.INDEX, index))
                             );
         return true;
      }
      return false;
   }

   @Override
   protected void annotateImpl(Document doc) {
      List<Annotation> tokens = doc.tokens();
      int start = -1;
      int sentenceIndex = 0;
      int lastEnd = -1;

      int quoteCount = 0;
      for(int ti = 0; ti < tokens.size(); ti++) {
         Annotation cToken = tokens.get(ti);
         Annotation nToken = getToken(tokens, ti + 1);

         if(start == -1) {
            start = cToken.start();
         }

         Set<InternalType> cTypes = getTypes(cToken);
         Set<InternalType> nTypes = getTypes(nToken);

         if(cTypes.contains(QUOTATION_MARK)) {
            quoteCount++;
         }

         if((cTypes.contains(ABBREVIATION) && nTypes.contains(CAPITALIZED) && quoteCount % 2 == 0)
               || (!cTypes.contains(ABBREVIATION) && cTypes.contains(END_OF_SENTENCE) && !nTypes.contains(
               PERSON_TITLE))) {

            while(nTypes.contains(END_OF_SENTENCE)) {
               ti++;
               cToken = nToken;
               nToken = getToken(tokens, ti + 1);
               nTypes = getTypes(nToken);
            }

            if((nTypes.contains(END_BRACKET) || nTypes.contains(QUOTATION_MARK)) && distance(cToken, nToken) == 0) {
               ti++;
               cToken = nToken;
            }

            if(!noSentenceBreak.contains(cToken.toLowerCase()) && !nTypes.contains(CONTINUE_SENTENCE) && addSentence(
                  doc, start, cToken.end(), sentenceIndex)) {
               sentenceIndex++;
               lastEnd = cToken.end();
               start = -1;
               quoteCount = 0;
            }
         } else {
            int newLines = countNewLineBeforeNext(doc, cToken, nToken);
            if(newLines > 1
                  || (newLines == 1 && nTypes.contains(CAPITALIZED))
                  || (newLines == 1 && nTypes.contains(LIST_MARKER))) {
               //Two or more line ends typically signifies a section heading, so treat it as a sentence.
               if(addSentence(doc, start, cToken.end(), sentenceIndex)) {
                  sentenceIndex++;
                  lastEnd = cToken.end();
                  start = -1;
                  quoteCount = 0;
               }
            }
         }
      }
      if(tokens.size() > 0 && lastEnd < tokens.get(tokens.size() - 1).end()) {
         addSentence(doc, start, tokens.get(tokens.size() - 1).end(), sentenceIndex);
      }

   }

   private int countNewLineBeforeNext(Document doc, Annotation cToken, Annotation nToken) {
      if(nToken.isEmpty()) {
         return 0;
      }
      int count = 0;
      char prev = '\0';
      for(int i = cToken.end(); i < nToken.start(); i++) {
         if(doc.charAt(i) == '\r' || (prev != '\r' && doc.charAt(i) == '\n')) {
            count++;
         }
         prev = doc.charAt(i);
      }
      return count;
   }

   private int distance(Annotation a1, Annotation a2) {
      return a2.start() - a1.end();
   }

   private Annotation getToken(List<Annotation> tokens, int index) {
      if(index < 0 || index >= tokens.size()) {
         return Fragments.orphanedAnnotation(Types.TOKEN);
      }
      return tokens.get(index);
   }

   private Set<InternalType> getTypes(Annotation annotation) {
      Set<InternalType> types = new HashSet<>();
      if(isQuotation(annotation)) {
         types.add(QUOTATION_MARK);
      }
      if(isAbbreviation(annotation)) {
         types.add(ABBREVIATION);
      }
      if(isListMarker(annotation)) {
         types.add(LIST_MARKER);
      }
      if(isEndOfSentenceMark(annotation)) {
         types.add(END_OF_SENTENCE);
      }
      if(isContinue(annotation)) {
         types.add(CONTINUE_SENTENCE);
      }
      if(annotation.attribute(Types.TOKEN_TYPE, TokenType.UNKNOWN).equals(TokenType.PERSON_TITLE)) {
         types.add(PERSON_TITLE);
      }
      if(isCapitalized(annotation)) {
         types.add(CAPITALIZED);
      }
      if(isEndBracket(annotation)) {
         types.add(END_BRACKET);
      }
      if(types.isEmpty()) {
         types.add(OTHER);
      }

      return types;
   }

   private boolean isAbbreviation(Annotation token) {
      TokenType type = token.attribute(Types.TOKEN_TYPE, TokenType.UNKNOWN);
      return type != null && (type.equals(TokenType.ACRONYM)
            || (type.equals(TokenType.TIME)
            && (token.next().isEmpty() || Character.isUpperCase(token.next().charAt(0)))));
   }

   private boolean isCapitalized(Annotation token) {
      if(token.length() == 1 && token.contentEquals("I")) {
         return true;
      } else if(token.length() > 1) {
         return !Strings.hasLetter(token) || Character.isUpperCase(token.charAt(0));
      }
      return false;
   }

   private boolean isContinue(Annotation token) {
      char c = token.isEmpty()
               ? ' '
               : token.charAt(token.length() - 1);
      return Arrays.binarySearch(sContinue, c) >= 0;
   }

   private boolean isEndBracket(Annotation annotation) {
      if(annotation.length() == 1) {
         switch(annotation.charAt(0)) {
            case ')':
            case ']':
            case '>':
               return true;
         }
      }
      return false;
   }

   private boolean isEndOfSentenceMark(Annotation token) {
      if(token.isEmpty() || token.attribute(Types.TOKEN_TYPE, TokenType.UNKNOWN).isInstance(
            TokenType.EMOTICON,
            TokenType.PERSON_TITLE)) {
         return false;
      }
      char c = token.charAt(token.length() - 1);
      return Arrays.binarySearch(endOfSentence, c) >= 0;
   }

   private boolean isEndPunctuation(Annotation token) {
      if(token.length() != 1) {
         return false;
      }
      char n = token.charAt(0);
      int type = Character.getType(n);
      return n == '"' || type == Character.FINAL_QUOTE_PUNCTUATION || type == Character.END_PUNCTUATION;
   }

   private boolean isListMarker(Annotation token) {
      return token.contentEquals("*") || token.contentEquals("+") || token.contentEquals(">");
   }

   private boolean isQuotation(Annotation annotation) {
      if(annotation.length() == 1) {
         int type = Character.getType(annotation.charAt(0));
         return annotation.contentEquals("\"")
               || annotation.contentEquals("'")
               || type == Character.INITIAL_QUOTE_PUNCTUATION
               || type == Character.FINAL_QUOTE_PUNCTUATION;
      }
      return false;
   }

   @Override
   public Set<AnnotatableType> requires() {
      return Collections.singleton(Types.TOKEN);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.SENTENCE);
   }

   /**
    * The enum Internal type.
    */
   enum InternalType {
      /**
       * Abbreviation internal type.
       */
      ABBREVIATION,
      /**
       * List marker internal type.
       */
      LIST_MARKER,
      /**
       * Quotation mark internal type.
       */
      QUOTATION_MARK,
      /**
       * End of sentence internal type.
       */
      END_OF_SENTENCE,
      /**
       * Continue sentence internal type.
       */
      CONTINUE_SENTENCE,
      /**
       * Person title internal type.
       */
      PERSON_TITLE,
      /**
       * Capitalized internal type.
       */
      CAPITALIZED,
      /**
       * End bracket internal type.
       */
      END_BRACKET,
      /**
       * Other internal type.
       */
      OTHER
   }

}//END OF DefaultSentenceAnnotator
