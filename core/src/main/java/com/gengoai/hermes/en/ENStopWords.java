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

import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.StopWords;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.io.Resources;
import com.gengoai.stream.MStream;
import com.gengoai.string.CharMatcher;
import com.gengoai.string.Strings;

import java.util.HashSet;
import java.util.Set;

/**
 * English StopWords
 *
 * @author David B. Bracewell
 */
public class ENStopWords extends StopWords {
   private static final long serialVersionUID = 1L;
   private static volatile StopWords INSTANCE;
   private final Set<String> stopWords = new HashSet<>();

   private ENStopWords() {
      try(MStream<String> stream = Resources.fromClasspath("com/gengoai/hermes/en/en_stopwords.txt").lines()) {
         stream.forEach(line -> {
            line = CharMatcher.WhiteSpace.trimFrom(line);
            if(Strings.isNotNullOrBlank(line) && !line.startsWith("#")) {
               stopWords.add(line);
            }
         });
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * @return the singleton instance of the ENStopWords class
    */
   public static StopWords getInstance() {
      if(INSTANCE == null) {
         synchronized(ENStopWords.class) {
            if(INSTANCE == null) {
               INSTANCE = new ENStopWords();
            }
         }
      }
      return INSTANCE;
   }

   @Override
   public boolean isStopWord(String word) {
      return Strings.isNullOrBlank(word) ||
            stopWords.contains(word.toLowerCase()) ||
            !Strings.hasLetter(word);
   }

   @Override
   protected boolean isTokenStopWord(Annotation token) {
      TokenType tokenType = token.attribute(Types.TOKEN_TYPE, TokenType.UNKNOWN);
      if(tokenType.equals(TokenType.CHINESE_JAPANESE)) {
         return true;
      }
      if(tokenType.equals(TokenType.URL)) {
         return true;
      }
      if(tokenType.equals(TokenType.EMOTICON)) {
         return true;
      }
      if(tokenType.equals(TokenType.EMAIL)) {
         return true;
      }
      if(tokenType.equals(TokenType.PUNCTUATION)) {
         return true;
      }
      if(tokenType.equals(TokenType.SGML)) {
         return true;
      }
      if(tokenType.equals(TokenType.PROTOCOL)) {
         return true;
      }
      if(token.hasAttribute(Types.PART_OF_SPEECH)) {
         PartOfSpeech tag = token.pos();
         if(tag != null) {
            if(tag.isInstance(PartOfSpeech.ADJECTIVE, PartOfSpeech.ADVERB, PartOfSpeech.NOUN, PartOfSpeech.VERB)) {
               return isStopWord(token.toString()) || isStopWord(token.getLemma());
            }
            return true;
         }
      }
      return isStopWord(token.toString()) || isStopWord(token.getLemma());
   }

}//END OF EnglishStopWords
