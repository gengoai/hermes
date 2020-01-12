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

package com.gengoai.hermes.extraction.regex;

import com.gengoai.hermes.HString;
import com.gengoai.parsing.*;

import java.io.Serializable;
import java.util.Optional;

import static com.gengoai.parsing.ParserGenerator.parserGenerator;


/**
 * The type Token regex.
 *
 * @author David B. Bracewell
 */
public final class TokenRegex implements Serializable {
   private static final ParserGenerator GENERATOR = parserGenerator(new Grammar(RegexTypes.values()),
                                                                    Lexer.create(RegexTypes.values()));
   private static final long serialVersionUID = 1L;
   private final NFA nfa;
   private final String pattern;

   private TokenRegex(TransitionFunction transitionFunction) {
      this.nfa = transitionFunction.construct();
      this.pattern = transitionFunction.toString();
   }


   /**
    * Compiles the regular expression
    *
    * @param pattern The token regex pattern
    * @return A compiled TokenRegex
    * @throws ParseException the parse exception
    */
   public static TokenRegex compile(String pattern) throws ParseException {
      Parser parser = GENERATOR.create(pattern);
      TransitionFunction top = null;
      while (parser.hasNext()) {
         TransitionFunction temp = parser.parseExpression().as(TransitionFunction.class);
         top = (top == null)
               ? temp
               : new SequenceTransition(top, temp);
      }
      if (top == null) {
         throw new IllegalStateException();
      }
      return new TokenRegex(top);
   }

   /**
    * Match first optional.
    *
    * @param text the text
    * @return the optional
    */
   public Optional<HString> matchFirst(HString text) {
      TokenMatcher matcher = new TokenMatcher(nfa, text);
      if (matcher.find()) {
         return Optional.of(matcher.group());
      }
      return Optional.empty();
   }

   /**
    * Creates a <code>TokenMatcher</code> to match against the given text.
    *
    * @param text  The text to run the TokenRegex against
    * @param start Which token to start the TokenRegex on
    * @return A TokenMatcher
    */
   public TokenMatcher matcher(HString text, int start) {
      return new TokenMatcher(nfa, text, start);
   }

   /**
    * Creates a <code>TokenMatcher</code> to match against the given text.
    *
    * @param text The text to run the TokenRegex against
    * @return A TokenMatcher
    */
   public TokenMatcher matcher(HString text) {
      return new TokenMatcher(nfa, text);
   }

   /**
    * Matches boolean.
    *
    * @param text the text
    * @return the boolean
    */
   public boolean matches(HString text) {
      return new TokenMatcher(nfa, text).find();
   }

   /**
    * Pattern string.
    *
    * @return The token regex pattern as a string
    */
   public String pattern() {
      return pattern;
   }


   @Override
   public String toString() {
      return pattern;
   }
}//END OF TokenRegex
