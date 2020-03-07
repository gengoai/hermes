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

package com.gengoai.hermes.extraction.regex;

import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.*;
import com.gengoai.hermes.lexicon.LexiconManager;
import com.gengoai.hermes.morphology.StopWords;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.math.NumericComparison;
import com.gengoai.parsing.*;
import com.gengoai.reflection.TypeUtils;
import com.gengoai.string.StringMatcher;
import com.gengoai.string.Strings;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.gengoai.hermes.Hermes.IDENTIFIER;
import static com.gengoai.string.Re.*;
import static com.gengoai.string.StringMatcher.regex;

/**
 * The enum Regex types.
 */
public enum RegexTypes implements TokenDef, GrammarRegistrable {
   /**
    * The Phrase.
    */
   CASE_INSENSITIVE_PHRASE(re("'",
                              namedGroup("", oneOrMore(or(ESC_BACKSLASH + ".", notChars("'")))),
                              "'")) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final String unescaped = Strings.unescape(token.getVariable(0), '\\');
            return new PredicateTransition(token.getText(), h -> h.contentEqualsIgnoreCase(unescaped), this);
         });
      }
   },
   CASE_SENSITIVE_PHRASE(re("\"",
                            namedGroup("", oneOrMore(or(ESC_BACKSLASH + ".", notChars("\"")))),
                            "\"")) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final String unescaped = Strings.unescape(token.getVariable(0), '\\');
            return new PredicateTransition(token.getText(), h -> h.contentEquals(unescaped), this);
         });
      }
   },
   CATEGORY(re(e('$'),
               "CAT(EGORY)?",
               "\\s*",
               "~",
               "\\s*",
               CASE_INSENSITIVE_PHRASE.pattern)) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            BasicCategories bc = BasicCategories.valueOf(token.getVariable(0));
            return new PredicateTransition(token.getText(), h -> h.isA(bc), this);
         });
      }
   },
   LEMMA_PHRASE(re("<",
                   namedGroup("", oneOrMore(or(ESC_BACKSLASH + ".", notChars(">")))),
                   ">")) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final String unescaped = Strings.unescape(token.getVariable(0), '\\');
            return new PredicateTransition(token.getText(), h -> h.getLemma().equalsIgnoreCase(unescaped), this);
         });
      }
   },
   /**
    * The Regex.
    */
   REGEX(re(e('/'),
            namedGroup("", oneOrMore(or(ESC_BACKSLASH + ".", notChars("/")))),
            e('/'),
            namedGroup("", zeroOrOne("i")),
            namedGroup("", zeroOrOne("g")))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            String patternText = Strings.unescape(token.getVariable(0), '\\');
            final Pattern pattern = (Strings.isNullOrBlank(token.getVariable(1)))
                                    ? Pattern.compile(patternText)
                                    : Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
            if(Strings.isNullOrBlank(token.getVariable(2))) {
               return new PredicateTransition(token.getText(), regex(pattern), this);
            }
            return new PredicateTransition(token.getText(), h -> pattern.matcher(h).matches(), this);
         });
      }
   },
   /**
    * The Tag.
    */
   TAG(re(e('#'),
          namedGroup("", IDENTIFIER))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(), h -> {
            AttributeType<?> type = h.asAnnotation().getType().getTagAttribute();
            final Object value = type.decode(token.getVariable(0));
            return h.attributeIsA(type, value);
         }, this));
      }
   },
   /**
    * The Attribute.
    */
   NUMERIC_ATTRIBUTE(re(e('$'),
                        namedGroup("", IDENTIFIER),
                        "\\s*",
                        namedGroup("", or(e('='),
                                          q("!="),
                                          q("<"),
                                          q("<="),
                                          q(">"),
                                          q(">="))),
                        "\\s*",
                        namedGroup("", zeroOrOne(chars("-")),
                                   or(re(zeroOrMore(DIGIT), nonMatchingGroup(e('.'), oneOrMore(DIGIT))),
                                      re(oneOrMore(DIGIT), zeroOrOne(nonMatchingGroup(e('.'), oneOrMore(DIGIT))))),
                                   zeroOrOne("e", zeroOrOne(e('-')), oneOrMore(DIGIT))))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final AttributeType<?> type = Types.attribute(token.getVariable(0));
            Validation.checkArgument(TypeUtils.isPrimitive(type.getValueType())
                                           || TypeUtils.isAssignable(Number.class, type.getValueType()),
                                     () -> "Cannot do numeric comparison with attribute of type: " + type.getValueType());
            final double value = Double.parseDouble(token.getVariable(2));
            final NumericComparison nc = NumericComparison.fromString(token.getVariable(1));
            return new PredicateTransition(token.getText(), h -> {
               if(!h.hasAttribute(type)) {
                  return false;
               }
               try {
                  return nc.compare(Converter.convert(h.attribute(type), double.class), value);
               } catch(TypeConversionException e) {
                  throw new RuntimeException(e);
               }
            }, this);
         });
      }
   },
   /**
    * The Attribute.
    */
   STRING_ATTRIBUTE(re(e('$'),
                       namedGroup("", IDENTIFIER),
                       "\\s*",
                       namedGroup("", or(e('='), q("!="), e('~'))),
                       "\\s*",
                       CASE_INSENSITIVE_PHRASE.pattern)) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final AttributeType<?> type = Types.attribute(token.getVariable(0));
            final String value = Strings.unescape(token.getVariable(2), '\\');
            final String operator = token.getVariable(1);
            switch(operator) {
               case "=":
                  if(TypeUtils.isAssignable(Tag.class, type.getValueType())) {
                     return new PredicateTransition(token.getText(), h -> h.attributeIsA(type, value), this);
                  }
                  return new PredicateTransition(token.getText(), h -> h.attributeEquals(type, value), this);
               case "!=":
                  if(TypeUtils.isAssignable(Tag.class, type.getValueType())) {
                     return new PredicateTransition(token.getText(), h -> !h.attributeIsA(type, value), this);
                  }
                  return new PredicateTransition(token.getText(), h -> !h.attributeEquals(type, value), this);
               case "~":
                  return new PredicateTransition(token.getText(), h -> {
                     System.out.println(type.getValueType());
                     if(TypeUtils.isCollection(type.getValueType())) {
                        Collection<?> c = Cast.as(h.attribute(type));
                        if(c == null) {
                           return false;
                        }
                        Object t = Converter.convertSilently(value, TypeUtils.getOrObject(1, type.getValueType()));
                        System.out.println(t);
                        return c.contains(t);
                     }
                     return h.attributeEquals(type, value);
                  }, this);
            }
            throw new IllegalStateException();
         });
      }
   },
   /**
    * Lexicon regex types.
    */
   LEXICON(re(e('%'), namedGroup("", IDENTIFIER))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         h -> LexiconManager.getLexicon(
                                                                               token.getVariable(0)).test(h), this));
      }
   },
   /**
    * The Annotation.
    */
   ANNOTATION(re(e('@'),
                 namedGroup("", IDENTIFIER))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new AnnotationTransition(Types.annotation(token.getVariable(0)),
                                                                          getChildTransition(parser, token)));
      }
   },
   ANY(e('.')) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         a -> true,
                                                                         this));
      }
   },
   STOPWORD("StopWord") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         StopWords.isStopWord(),
                                                                         this));
      }
   },
   CONTENT_WORD("ContentWord") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         StopWords.isNotStopWord(),
                                                                         this));
      }
   },
   HAS_STOP_WORD("hasStopWord") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         StopWords.hasStopWord(),
                                                                         this));
      }
   },
   UPPERCASE("Upper") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         StringMatcher.UpperCase,
                                                                         this));
      }
   },
   UPPER_INITIAL("UpperInitial") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         h -> !h.isEmpty() && Character.isUpperCase(
                                                                               h.charAt(0)),
                                                                         this));
      }
   },
   LOWERCASE("Lower") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         StringMatcher.LowerCase,
                                                                         this));
      }
   },
   LOWER_INITIAL("LowerInitial") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         h -> !h.isEmpty() && Character.isLowerCase(
                                                                               h.charAt(0)),
                                                                         this));
      }
   },
   LETTER("Letter") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         StringMatcher.Letter,
                                                                         this));
      }
   },
   ALPHANUMERIC("AlphaNumeric") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         StringMatcher.LetterOrDigit,
                                                                         this));
      }
   },
   PUNCTUATION("Punctuation") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         StringMatcher.Punctuation,
                                                                         this));
      }
   },
   NUMBER("Number") {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new PredicateTransition(token.getText(),
                                                                         a -> Strings.isDigit(a) ||
                                                                               a.pos().isInstance(POS.NUMBER) ||
                                                                               TokenType.NUMBER
                                                                                     .equals(a.attribute(Types.TOKEN_TYPE)),
                                                                         this));
      }
   },
   INCOMING_RELATION(re(e('@'),
                        e('<'),
                        namedGroup("", IDENTIFIER),
                        zeroOrOne(e('{'), CASE_INSENSITIVE_PHRASE.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final RelationType relationType = Types.relation(token.getVariable(0));
            return new RelationTransition(relationType,
                                          Strings.unescape(token.getVariable(1), '\\'),
                                          token.getText(),
                                          false,
                                          getRelationMatcher(parser, token));
         });
      }
   },
   OUTGOING_RELATION(re(e('@'),
                        e('>'),
                        namedGroup("", IDENTIFIER),
                        zeroOrOne(e('{'), CASE_INSENSITIVE_PHRASE.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final RelationType relationType = Types.relation(token.getVariable(0));
            return new RelationTransition(relationType,
                                          Strings.unescape(token.getVariable(1), '\\'),
                                          token.getText(),
                                          true,
                                          getRelationMatcher(parser, token));
         });
      }
   },
   INCOMING_DEPENDENCY(re(e('@'),
                          e('<'),
                          zeroOrOne(e('{'), CASE_INSENSITIVE_PHRASE.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            return new RelationTransition(Types.DEPENDENCY,
                                          Strings.unescape(token.getVariable(0), '\\'),
                                          token.getText(),
                                          false,
                                          getRelationMatcher(parser, token));
         });
      }
   },
   OUTGOING_DEPENDENCY(re(e('@'),
                          e('>'),
                          zeroOrOne(e('{'), CASE_INSENSITIVE_PHRASE.pattern, e('}')))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new RelationTransition(Types.DEPENDENCY,
                                                                        Strings.unescape(token.getVariable(0), '\\'),
                                                                        token.getText(),
                                                                        true,
                                                                        getRelationMatcher(parser, token)));
      }
   },
   LOOKAHEAD(re(e('('), e('?'), e('>'))) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> new LookAheadTransition(left.as(TransitionFunction.class),
                                                                                asSequence(parser.parseExpressionList(
                                                                                      CLOSE_PARENS, null)),
                                                                                false), 0);
      }
   },
   NEGATIVE_LOOKAHEAD(re(e('('), e('?'), e('!'), e('>'))) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> new LookAheadTransition(left.as(TransitionFunction.class),
                                                                                asSequence(parser.parseExpressionList(
                                                                                      CLOSE_PARENS, null)),
                                                                                true), 0);
      }
   },
   NAMED_GROUP(re(e('('), e('?'), e('<'), namedGroup("", IDENTIFIER), e('>'))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new GroupTransition(
               asSequence(Cast.cast(parser.parseExpressionList(CLOSE_PARENS, null))), token.getVariable(0)));
      }
   },
   NEGATION(e('^')) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new NegationTransition(
               parser.parseExpression().as(TransitionFunction.class)));
      }
   },
   OPEN_PARENS(e('(')) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> new GroupTransition(
               asSequence(Cast.cast(parser.parseExpressionList(CLOSE_PARENS, null)))));
      }
   },
   CLOSE_PARENS(e(')')),
   ONE_OR_MORE(e('+')) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> new OneOrMoreTransition(left.as(TransitionFunction.class)));
      }
   },
   ZERO_OR_MORE(e('*')) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> new ZeroOrMoreTransition(left.as(TransitionFunction.class)));
      }
   },
   ZERO_OR_ONE(e('?')) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> new ZeroOrOneTransition(left.as(TransitionFunction.class)));
      }
   },
   RANGE(re(e('{'),
            zeroOrMore(WHITESPACE),
            namedGroup("", oneOrMore(DIGIT)),
            zeroOrMore(WHITESPACE),
            zeroOrOne(",",
                      zeroOrMore(WHITESPACE),
                      namedGroup("", or(e('*'), oneOrMore(DIGIT)))),
            zeroOrMore(WHITESPACE),
            e('}')
           )) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) -> {
            int low = Integer.parseInt(token.getVariable(0));
            int high;
            if(token.getVariable(1) == null) {
               high = -1;
            } else if(token.getVariable(1).equals("*")) {
               high = Integer.MAX_VALUE;
            } else {
               high = Integer.parseInt(token.getVariable(1));
            }
            return new RangeTransition(left.as(TransitionFunction.class), low, high);
         }, 5);
      }
   },
   ALTERNATION(e('|')) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this, (parser, token, left) ->
               new AlternationTransition(left.as(TransitionFunction.class),
                                         parser.parseExpression(token).as(TransitionFunction.class)), 10);
      }
   },
   AND(e('&')) {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(this,
                         (parser, token, left) -> new AndTransition(left.as(TransitionFunction.class),
                                                                    parser.parseExpression(token)
                                                                          .as(TransitionFunction.class)), 10);
      }
   };

   private final String pattern;

   RegexTypes(String pattern) {
      this.pattern = pattern;
   }

   /**
    * As sequence transition function.
    */
   static TransitionFunction asSequence(List<TransitionFunction> transitionFunctions) {
      TransitionFunction tf = null;
      for(TransitionFunction transitionFunction : transitionFunctions) {
         tf = (tf == null)
              ? transitionFunction
              : new SequenceTransition(tf, transitionFunction);
      }
      return tf;
   }

   private static TransitionFunction getChildTransition(Parser parser,
                                                        ParserToken token) throws ParseException {
      ParserToken next = parser.peek();
      if(next.isInstance(OPEN_PARENS) && next.getStartOffset() == token.getEndOffset()) {
         return asSequence(parser.parseExpressionList(OPEN_PARENS, CLOSE_PARENS, null));
      }
      return new PredicateTransition(".", h -> true, ANY);
   }

   private static SerializableFunction<HString, Integer> getRelationMatcher(Parser parser,
                                                                            ParserToken token) throws ParseException {
      ParserToken next = parser.peek();
      SerializableFunction<HString, Integer> matcher;
      if(next.isInstance(OPEN_PARENS) && next.getStartOffset() == token.getEndOffset()) {
         matcher = asSequence(parser.parseExpressionList(OPEN_PARENS, CLOSE_PARENS, null))::matches;
      } else {
         matcher = h -> 1;
      }
      return matcher;
   }

   @Override
   public String getPattern() {
      return pattern;
   }

   @Override
   public void register(Grammar grammar) {

   }

}//END OF RegexTypes
