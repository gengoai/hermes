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

import com.gengoai.Tag;
import com.gengoai.hermes.*;
import com.gengoai.parsing.*;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gengoai.hermes.Hermes.IDENTIFIER;
import static com.gengoai.string.Re.*;
import static com.gengoai.tuple.Tuples.$;

enum LyreType implements TokenDef {
   TAG(re(e('#'), namedGroup("", IDENTIFIER))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.tag(token.getVariable(0));
               case 1:
                  return LyreDSL.tag(token.getVariable(0), arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   PIPE(re(q("&>"))) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this,
                         (parser, token, left) -> LyreDSL.andPipe(left.as(LyreExpression.class),
                                                                  parser.parseExpression(LyreExpression.class)),
                         1);
      }
   },
   ORE(q("|>")) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.orPipe(left.as(LyreExpression.class),
                                                                       parser.parseExpression(token)
                                                                             .as(LyreExpression.class)), 1);
      }
   },
   THIS(re(e('$'), "_")) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.$_);
      }
   },
   LITERAL("'(?<>(?:\\\\'|[^'])*)'") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.literal(token.getVariable(0)));
      }
   },
   NULL("null") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.NULL);
      }
   },
   LOOKAHEAD(re(e('('), e('?'), e('>'))) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> {
            LyreExpression condition = parser.parseExpression(LyreExpression.class);
            parser.consume(CLOSE_PARENS);
            return LyreDSL.lookAhead(condition, left.as(LyreExpression.class));
         }, 100);
      }
   },
   LOOKBEHIND(re(e('('), e('?'), e('<'))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            LyreExpression condition = parser.parseExpression(LyreExpression.class);
            parser.consume(CLOSE_PARENS);
            return LyreDSL.lookBehind(condition, parser.parseExpression(LyreExpression.class));
         });
      }
   },
   NEGLOOKAHEAD(re(e('('), e('?'), e('!'), e('>'))) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> {
            LyreExpression condition = parser.parseExpression(LyreExpression.class);
            parser.consume(CLOSE_PARENS);
            return LyreDSL.negLookAhead(condition, left.as(LyreExpression.class));
         }, 100);
      }
   },
   NEGLOOKBEHIND(re(e('('), e('?'), e('!'), e('<'))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            LyreExpression exp = parser.parseExpression(LyreExpression.class);
            parser.consume(CLOSE_PARENS);
            return LyreDSL.negLookBehind(exp, parser.parseExpression(LyreExpression.class));
         });
      }
   },
   TOKEN_LENGTH("tlen") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.tlen;
               case 1:
                  return LyreDSL.tlen(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   TRUE("true") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.TRUE);
      }
   },
   FALSE("false") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.FALSE);
      }
   },
   NAN("NaN") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.NAN);
      }
   },
   INFITITY("INF") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.POSITIVE_INFINITY);
      }
   },
   NEGINFITITY("-INF") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.NEGATIVE_INFINITY);
      }
   },
   NUMBER(re(zeroOrOne(chars("-")), or(re(zeroOrMore(DIGIT), nonMatchingGroup(e('.'), oneOrMore(DIGIT))),
                                       re(oneOrMore(DIGIT), zeroOrOne(nonMatchingGroup(e('.'), oneOrMore(DIGIT))))),
             zeroOrOne("e", zeroOrOne(e('-')), oneOrMore(DIGIT)))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.number(Double.parseDouble(token.getText())));
      }
   },
   STRING(re("string")) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.string;
               case 1:
                  return LyreDSL.string(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   LEMMA("lemma") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.lemma;
               case 1:
                  return LyreDSL.lemma(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   STEM("stem") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.stem;
               case 1:
                  return LyreDSL.stem(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   POS("pos") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.pos;
               case 1:
                  return LyreDSL.pos(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   UPOS("upos") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.upos;
               case 1:
                  return LyreDSL.upos(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   LOWER_CASE("lower") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.lower;
               case 1:
                  return LyreDSL.lower(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   UPPER_CASE("upper") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.upper;
               case 1:
                  return LyreDSL.upper(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   LENGTH("len") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.len;
               case 1:
                  return LyreDSL.len(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   LIST_LENGTH("llen") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.llen;
               case 1:
                  return LyreDSL.llen(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   REGEX(re(e('/'),
            namedGroup("", "(?<>(?:\\\\/|[^/])*)"),
            e('/'),
            namedGroup("", "[ig]*"))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final Tuple2<Boolean, String> regex = createPattern(token.getVariable(0), token.getVariable(2));
            return LyreDSL.regex(regex.v2, regex.v1);
         });
      }
   },
   SUBSTITUTE(re("s",
                 e('/'),
                 namedGroup("", "(?:\\\\/|[^/])*"),
                 e('/'),
                 namedGroup("", "(?:\\\\/|[^/])*"),
                 e('/'),
                 namedGroup("", "[ig]*"))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final Tuple2<Boolean, String> regex = createPattern(token.getVariable(0), token.getVariable(2));
            return LyreDSL.rsub(regex.v2, token.getVariable(1), regex.v1);
         });
      }
   },

   ATTRIBUTE(re(e('$'), namedGroup("", IDENTIFIER))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            AttributeType<?> attributeType = Types.attribute(token.getVariable(0));
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.attribute(attributeType);
               case 1:
                  return LyreDSL.attribute(attributeType, arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   LEXICON(re(e('%'), namedGroup("", IDENTIFIER))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.lexicon(token.getVariable(0)));
      }
   },
   OUTGOING_DEPENDENCY(re(e('@'), e('>'), "dep",
                          zeroOrOne(e('{'), LITERAL.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.dep(RelationDirection.OUTGOING, token.getVariable(0));
                  }
                  return LyreDSL.dep(RelationDirection.OUTGOING);
               case 1:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.dep(RelationDirection.OUTGOING, token.getVariable(0), arguments.get(0));
                  }
                  return LyreDSL.dep(RelationDirection.OUTGOING, arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   INCOMING_DEPENDENCY(re(e('@'), e('<'), "dep",
                          zeroOrOne(e('{'), LITERAL.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.dep(RelationDirection.INCOMING, token.getVariable(0));
                  }
                  return LyreDSL.dep(RelationDirection.INCOMING);
               case 1:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.dep(RelationDirection.INCOMING, token.getVariable(0), arguments.get(0));
                  }
                  return LyreDSL.dep(RelationDirection.INCOMING, arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   OUTGOING_RELATION(re(e('@'), e('>'), namedGroup("", IDENTIFIER),
                        zeroOrOne(e('{'), LITERAL.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            final RelationType type = Types.relation(token.getVariable(0));
            switch(arguments.size()) {
               case 0:
                  if(token.getVariableCount() > 1) {
                     return LyreDSL.rel(RelationDirection.OUTGOING, type, token.getVariable(1));
                  }
                  return LyreDSL.rel(RelationDirection.OUTGOING, type);
               case 1:
                  if(token.getVariableCount() > 1) {
                     return LyreDSL.rel(RelationDirection.OUTGOING, type, token.getVariable(1), arguments.get(0));
                  }
                  return LyreDSL.rel(RelationDirection.OUTGOING, type, arguments.get(1));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   INCOMING_RELATION(re(e('@'), e('<'), namedGroup("", IDENTIFIER),
                        zeroOrOne(e('{'), LITERAL.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            final RelationType type = Types.relation(token.getVariable(0));
            switch(arguments.size()) {
               case 0:
                  if(token.getVariableCount() > 1) {
                     return LyreDSL.rel(RelationDirection.INCOMING, type, token.getVariable(1));
                  }
                  return LyreDSL.rel(RelationDirection.INCOMING, type);
               case 1:
                  if(token.getVariableCount() > 1) {
                     return LyreDSL.rel(RelationDirection.INCOMING, type, token.getVariable(1), arguments.get(0));
                  }
                  return LyreDSL.rel(RelationDirection.INCOMING, type, arguments.get(1));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   SLICE(re(e('['),
            namedGroup("", zeroOrMore(zeroOrOne("-"), DIGIT)),
            ":",
            namedGroup("", zeroOrMore(zeroOrOne("-"), DIGIT)),
            e(']'))) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> {
            int start = Strings.isNullOrBlank(token.getVariable(0))
                        ? 0
                        : Integer.parseInt(token.getVariable(0));
            int end = Strings.isNullOrBlank(token.getVariable(1))
                      ? -0
                      : Integer.parseInt(token.getVariable(1));
            return LyreDSL.slice(left.as(LyreExpression.class), start, end);
         }, 10);
      }
   },
   APPLY(re(e('~'), e('='))) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.apply(left.as(LyreExpression.class),
                                                                      parser.parseExpression(token)
                                                                            .as(LyreExpression.class)), 3);
      }
   },
   COUNT("count") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 1:
                  return LyreDSL.count(arguments.get(0));
               case 2:
                  return LyreDSL.count(arguments.get(0), arguments.get(1));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   HAS_STOPWORD("hasStopWord") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.hasStopWord;
               case 1:
                  return LyreDSL.hasStopWord(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_STOPWORD("isStopWord") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isStopWord;
               case 1:
                  return LyreDSL.isStopWord(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_CONTENTWORD("isContentWord") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isContentWord;
               case 1:
                  return LyreDSL.isContentWord(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_UPPERCASE("isUpper") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isUpper;
               case 1:
                  return LyreDSL.isUpper(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_LOWERCASE("isLower") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isLower;
               case 1:
                  return LyreDSL.isLower(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_LETTER("isLetter") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isLetter;
               case 1:
                  return LyreDSL.isLetter(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_DIGIT("isDigit") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isDigit;
               case 1:
                  return LyreDSL.isDigit(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_ALPHANUMERIC("isAlphaNumeric") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isAlphaNumeric;
               case 1:
                  return LyreDSL.isAlphaNumeric(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_WHITESPACE("isWhitespace") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isWhitespace;
               case 1:
                  return LyreDSL.isWhitespace(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IS_PUNCTUATION("isPunctuation") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.isPunctuation;
               case 1:
                  return LyreDSL.isPunctuation(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   EXISTS("exists") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.exists;
               case 1:
                  return LyreDSL.exists(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   IN("in") {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.in(left.as(LyreExpression.class),
                                                                   parser.parseExpression(token)
                                                                         .as(LyreExpression.class)), 1);
      }
   },
   IF("if") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 3) {
               return LyreDSL.ifThen(arguments.get(0), arguments.get(1), arguments.get(2));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   HAS("has") {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.has(left.as(LyreExpression.class),
                                                                    parser.parseExpression(token)
                                                                          .as(LyreExpression.class)), 1);
      }
   },
   COMMA(",") {
      @Override
      public void register(Grammar grammar) {
      }
   },
   ARRAY_START(e('[')) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.list(parser.parseExpressionList(ARRAY_END, COMMA)
                                                                    .stream()
                                                                    .map(e -> e.as(LyreExpression.class))
                                                                    .collect(Collectors.toList())));
      }
   },
   ARRAY_END(e(']')) {
      @Override
      public void register(Grammar grammar) {

      }
   },
   NOT_EQUALS("!=") {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.ne(left.as(LyreExpression.class),
                                                                   parser.parseExpression(token)
                                                                         .as(LyreExpression.class)), 3);
      }
   },
   NEGATION("!") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.not(parser.parseExpression(LyreExpression.class)));
      }
   },
   LESS_THAN_EQUALS("<=") {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.lte(left.as(LyreExpression.class),
                                                                    parser.parseExpression(token)
                                                                          .as(LyreExpression.class)), 3);
      }
   },
   LESS_THAN(e('<')) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.lt(left.as(LyreExpression.class),
                                                                   parser.parseExpression(token)
                                                                         .as(LyreExpression.class)), 3);
      }
   },
   GREATER_THAN_EQUALS(">=") {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.gte(left.as(LyreExpression.class),
                                                                    parser.parseExpression(token)
                                                                          .as(LyreExpression.class)), 3);
      }
   },
   GREATER_THAN(">") {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.gt(left.as(LyreExpression.class),
                                                                   parser.parseExpression(token)
                                                                         .as(LyreExpression.class)), 3);
      }
   },
   EQUALS("=") {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.eq(left.as(LyreExpression.class),
                                                                   parser.parseExpression(token)
                                                                         .as(LyreExpression.class)), 3);
      }
   },
   AND(q("&&")) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.and(left.as(LyreExpression.class),
                                                                    parser.parseExpression(token)
                                                                          .as(LyreExpression.class)), 1);
      }
   },
   OR(q("||")) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.or(left.as(LyreExpression.class),
                                                                   parser.parseExpression(token)
                                                                         .as(LyreExpression.class)), 1);
      }
   },
   XOR(e('^')) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.xor(left.as(LyreExpression.class),
                                                                    parser.parseExpression(token)
                                                                          .as(LyreExpression.class)), 1);
      }
   },
   CONCATENATION(e('+')) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> LyreDSL.plus(left.as(LyreExpression.class),
                                                                     parser.parseExpression(token)
                                                                           .as(LyreExpression.class)));
      }
   },
   OPEN_PARENS(e('(')) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(OPEN_PARENS, (parser, token) -> {
            Expression exp = parser.parseExpression();
            parser.consume(CLOSE_PARENS);
            return exp;
         });
      }
   },
   CLOSE_PARENS(e(')')) {
      @Override
      public void register(Grammar grammar) {

      }
   },
   GET("get") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.get(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   FIRST("first") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 1) {
               return LyreDSL.first(arguments.get(0));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   LAST("last") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 1) {
               return LyreDSL.last(arguments.get(0));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   LONGEST("longest") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 1) {
               return LyreDSL.longest(arguments.get(0));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   WORD_LIST("wordList") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 1) {
               return LyreDSL.wordList(arguments.get(0));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   MAX("max") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 1) {
               return LyreDSL.max(arguments.get(0));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   MATCHALL(e('~')) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> LyreDSL.MATCH_ALL);
      }
   },
   ALL("all") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.all(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   NONE("none") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.none(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   ANY("any") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.any(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   MAP("map") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.map(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   FILTER("filter") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.filter(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   TRIM("trim") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.trim;
               case 2:
                  return LyreDSL.trim(arguments.get(0), arguments.get(1));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   FLATTEN("flatten") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 1) {
               return LyreDSL.flatten(arguments.get(0));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   BINARY_FEATURIZER(re("binary", zeroOrOne(e('{'), LITERAL.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.binary(token.getVariable(0));
                  }
                  return LyreDSL.binary;
               case 1:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.binary(token.getVariable(0), arguments.get(0));
                  }
                  return LyreDSL.binary(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   FREQ_FEATURIZER(re("frequency", zeroOrOne(e('{'), LITERAL.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.frequency(token.getVariable(0));
                  }
                  return LyreDSL.frequency;
               case 1:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.frequency(token.getVariable(0), arguments.get(0));
                  }
                  return LyreDSL.frequency(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   L1_FEATURIZER(re("L1", zeroOrOne(e('{'), LITERAL.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 0:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.l1(token.getVariable(0));
                  }
                  return LyreDSL.l1;
               case 1:
                  if(token.getVariableCount() > 0) {
                     return LyreDSL.l1(token.getVariable(0), arguments.get(0));
                  }
                  return LyreDSL.l1(arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   ANNOTATION(re(e('@'), namedGroup("", IDENTIFIER))) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            AnnotationType annotationType = Types.annotation(token.getVariable(0));
            switch(arguments.size()) {
               case 0:
                  return LyreDSL.annotation(annotationType);
               case 1:
                  return LyreDSL.annotation(annotationType, arguments.get(0));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   CONTEXT(re("cxt")) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.context(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   IOB(re("iob")) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            switch(arguments.size()) {
               case 1:
                  return LyreDSL.iob(arguments.get(0));
               case 2:
                  return LyreDSL.iob(arguments.get(0), arguments.get(1));
               default:
                  throw new ParseException("Illegal number of arguments for " + token.getText());
            }
         });
      }
   },
   NONULL(re("nn")) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.notNull(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   INTERLEAVE(re("interleave")) {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            AnnotationType[] types = arguments.stream()
                                              .map(e -> Types.annotation(e.as(LyreExpression.class).apply(null)))
                                              .toArray(AnnotationType[]::new);
            return LyreDSL.interleave(types);
         });
      }
   },
   WHEN("when") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 2) {
               return LyreDSL.when(arguments.get(0), arguments.get(1));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   LPAD("lpad") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 3) {
               return LyreDSL.lpad(arguments.get(0), arguments.get(1), arguments.get(2));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   },
   RPAD("rpad") {
      @Override
      public void register(Grammar grammar) {
         method(grammar, (parser, token, arguments) -> {
            if(arguments.size() == 3) {
               return LyreDSL.rpad(arguments.get(0), arguments.get(1), arguments.get(2));
            }
            throw new ParseException("Illegal number of arguments for " + token.getText());
         });
      }
   };

   private final String pattern;

   LyreType(String pattern) {
      this.pattern = pattern;
   }

   private static Tuple2<Boolean, String> createPattern(String pattern, String options) {
      boolean matchAll = options.contains("g");
      options = options.replace("g", "");
      options = options.length() > 0
                ? String.format("(?%s)", options)
                : "";
      return $(matchAll, options + pattern);
   }

   @Override
   public String getPattern() {
      return pattern;
   }

   @Override
   public boolean isInstance(Tag tag) {
      return tag == this;
   }

   void method(Grammar grammar, MethodRegisterer registerer) {
      grammar.prefix(this, (parser, token) -> {
         final List<LyreExpression> arguments = new ArrayList<>();
         if(parser.peek().isInstance(LyreType.OPEN_PARENS)) {
            parser.consume(LyreType.OPEN_PARENS);
            parser.parseExpressionList(LyreType.CLOSE_PARENS, LyreType.COMMA)
                  .stream()
                  .map(e -> e.as(LyreExpression.class))
                  .forEach(arguments::add);
         }
         return registerer.handle(parser, token, arguments);
      });
   }

   public abstract void register(Grammar grammar);

   interface MethodRegisterer {
      LyreExpression handle(Parser parser, ParserToken token, List<LyreExpression> arguments) throws ParseException;
   }
}//END OF HQLType
