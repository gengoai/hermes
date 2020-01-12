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

package com.gengoai.hermes.morphology;

import com.gengoai.collection.Iterables;
import com.gengoai.io.Resources;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.text.BreakIterator;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * The type Break iterator tokenizer.
 *
 * @author David B. Bracewell
 */
public class BreakIteratorTokenizer implements Tokenizer, Serializable {
   private static final long serialVersionUID = 1L;
   private final Locale locale;

   /**
    * Instantiates a new Break iterator tokenizer.
    *
    * @param locale the locale
    */
   public BreakIteratorTokenizer(Locale locale) {
      this.locale = locale;
   }

   @Override
   public Iterable<Token> tokenize(Reader reader) {
      try {
         return Iterables.asIterable(
            new BreakIteratorStream(Resources.fromReader(reader).readToString(), locale)
                                    );
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Iterable<Token> tokenize(String input) {
      return Iterables.asIterable(
         new BreakIteratorStream(input, locale)
                                 );
   }

   private static class BreakIteratorStream implements Iterator<Token> {
      private final String input;
      private final BreakIterator iterator;
      private int index = 0;
      private Token nextToken;
      private int start = 0;

      private BreakIteratorStream(String input, Locale locale) {
         this.input = input;
         this.iterator = java.text.BreakIterator.getWordInstance(locale);
         this.iterator.setText(input);

      }

      private Token advance() {
         while (nextToken == null && start >= 0) {
            int end = this.iterator.next();
            if (end < 0) {
               start = -1;
               return null;
            }
            if (!Strings.isNullOrBlank(input.substring(start, end))) {
               nextToken = new Token(
                  input.substring(start, end),
                  determineType(input.substring(start, end)),
                  start,
                  end,
                  index
               );
               index++;
               while (end < input.length() && Character.isWhitespace(input.charAt(end))) {
                  end++;
               }
               start = end;
            }
         }
         return nextToken;
      }

      private TokenType determineType(String tokenString) {
         //Simplistic Type assignment
         boolean hasLetter = Strings.hasLetter(tokenString);
         boolean hasDigit = Strings.hasDigit(tokenString);
         TokenType tokenType = TokenType.UNKNOWN;
         if (hasDigit && hasLetter) {
            tokenType = TokenType.ALPHA_NUMERIC;
         } else if (hasDigit) {
            tokenType = TokenType.NUMBER;
         } else if (hasLetter && tokenString.contains(".")) {
            tokenType = TokenType.ACRONYM;
         } else if (hasLetter && tokenString.contains("'")) {
            tokenType = TokenType.CONTRACTION;
         } else if (hasLetter) {
            tokenType = TokenType.ALPHA_NUMERIC;
         } else if (Strings.isPunctuation(tokenString)) {
            tokenType = TokenType.PUNCTUATION;
         }
         return tokenType;
      }

      @Override
      public boolean hasNext() {
         return advance() != null;
      }

      @Override
      public Token next() {
         Token token = advance();
         if (token == null) {
            throw new NoSuchElementException();
         }
         nextToken = null;
         return token;
      }

   }

}//END OF BreakIteratorTokenizer
