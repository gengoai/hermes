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
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.parsing.*;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gengoai.parsing.ParserGenerator.parserGenerator;

/**
 * <p>
 * Hermes provides a token-based regular expression engine that allows for matches on arbitrary annotation types,
 * relation types, and attributes, while providing many of the operators that are possible using standard Java regular
 * expressions. As with Java regular expressions, the token regular expression is specified as a string and is compiled
 * into an instance of of TokenRegex. The TokenRegex class has many of the same methods as Java’s regular expression,
 * but returns a {@link TokenMatcher} instead of Matcher. The TokenMatcher class allows for iterating of the matches,
 * extracting the match or named-groups within the match, the starting and ending offset of the match, and conversion
 * into a TokenMatch object which records the current state of the match. Token regular expressions can act as
 * extractors where the extraction generates the HStrings matched for the default group. An example of compiling a
 * regular expression, creating a match, and iterating over the matches is as follows:
 * </p>
 * <pre>
 * {@code
 *    TokenRegex regex = TokenRegex.compile(pattern);
 *    TokenMatcher matcher = regex.matcher(document);
 *    while (matcher.find()) {
 *            System.out.println(matcher.group());
 *    }
 * }
 * </pre>
 * <p>
 * The syntax for token-based regular expressions borrows from the Lyre Expression Language where possible. Token-based
 * regular expressions differ from Lyre in that they work over sequences of HStrings whereas Lyre is working on single
 * HString units. As such, there are differences in the syntax between Lyre. Details on the syntax can be found in the
 * Hermes User Guide.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class TokenRegex implements Serializable, Extractor {
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
    * Compiles the given pattern into a TokenRegex object
    *
    * @param pattern The token regex pattern
    * @return A compiled TokenRegex
    * @throws ParseException The given pattern has a syntax error
    */
   public static TokenRegex compile(@NonNull String pattern) throws ParseException {
      Parser parser = GENERATOR.create(pattern);
      TransitionFunction top = null;
      while(parser.hasNext()) {
         TransitionFunction temp = parser.parseExpression().as(TransitionFunction.class);
         top = (top == null)
               ? temp
               : new SequenceTransition(top, temp);
      }
      if(top == null) {
         throw new ParseException();
      }
      return new TokenRegex(top);
   }

   @Override
   public Extraction extract(@NonNull HString hString) {
      TokenMatcher matcher = matcher(hString);
      final List<HString> hits = new ArrayList<>();
      while(matcher.find()) {
         hits.add(matcher.group());
      }
      return Extraction.fromHStringList(hits);
   }

   /**
    * Runs the pattern over the given input text returning the first match if one exists.
    *
    * @param text the text to run the pattern over
    * @return an optional of the match
    */
   public Optional<HString> matchFirst(HString text) {
      TokenMatcher matcher = new TokenMatcher(nfa, text);
      if(matcher.find()) {
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
    * Determines if the regex matches the entire region of the given input text.
    *
    * @param text the text to match
    * @return True if the pattern matches  the entire region of the input text, False otherwise
    */
   public boolean matches(HString text) {
      return matchFirst(text).map(h -> h.length() == text.length()).orElse(false);
   }

   /**
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
