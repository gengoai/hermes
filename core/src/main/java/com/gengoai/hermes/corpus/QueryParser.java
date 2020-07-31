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

package com.gengoai.hermes.corpus;

import com.gengoai.Tag;
import com.gengoai.hermes.AttributeType;
import com.gengoai.parsing.*;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

import static com.gengoai.hermes.Hermes.IDENTIFIER;
import static com.gengoai.parsing.ParserGenerator.parserGenerator;
import static com.gengoai.string.Re.*;

/**
 * Simple query to predicate constructor for basic keyword queries over corpora. Syntax is as follows:
 * <ul>
 * <li>'TERM' - keyword or phrase</li>
 * <li>AND - and terms</li>
 * <li>OR - or terms</li>
 * <li>- - not term</li>
 * <li>$ATTRIBUTE(value) - filter based on exact matches for document level attributes</li>
 * </ul>
 * Order of operations can be controlled using parenthesis
 *
 * @author David B. Bracewell
 */
public final class QueryParser {
   private static final Evaluator<Query> evaluator = new Evaluator<Query>() {
      {
         $(QueryExpression.class, p -> p.query);
      }
   };

   private static final ParserGenerator PARSER_GENERATOR = parserGenerator(new Grammar(Types.values()),
                                                                           Lexer.create(Types.values()));

   /**
    * Parses the given query in string form into a {@link Query} object.
    *
    * @param query the query to parse
    * @return the Query represented by the
    * @throws ParseException error parsing the query
    */
   public static Query parse(String query) throws ParseException {
      List<Query> parts = PARSER_GENERATOR.create(query).evaluateAll(evaluator);
      if(parts.size() == 1) {
         return parts.get(0);
      }
      return parts.stream()
                  .reduce(Query.And::new)
                  .orElseThrow(ParseException::new);
   }

   private enum Types implements TokenDef, GrammarRegistrable {
      /**
       * Op types.
       */
      OP(e('(')) {
         @Override
         public void register(Grammar grammar) {
            grammar.prefix(Types.OP, (parser, token) -> {
               QueryExpression pe = parser.parseExpression(QueryExpression.class);
               parser.consume(Types.CP);
               return pe;
            });
         }
      },
      /**
       * Cp types.
       */
      CP(e(')')) {
         @Override
         public void register(Grammar grammar) {

         }
      },
      /**
       * And types.
       */
      AND("\\b[aA][nN][Dd]\\b") {
         @Override
         public void register(Grammar grammar) {
            grammar.postfix(Types.AND, (parser, token, left) -> {
               final Query l = left.as(QueryExpression.class).query;
               final Query r = parser.parseExpression(token, QueryExpression.class).query;
               return new QueryExpression(Types.AND, new Query.And(l, r));
            }, 2);
         }
      },
      /**
       * Or types.
       */
      OR("\\b[oO][Rr]\\b") {
         @Override
         public void register(Grammar grammar) {
            grammar.postfix(Types.OR, (parser, token, left) -> {
               final Query l = left.as(QueryExpression.class).query;
               final Query r = parser.parseExpression(token, QueryExpression.class).query;
               return new QueryExpression(Types.OR, new Query.Or(l, r));
            }, 1);
         }
      },
      /**
       * Term types.
       */
      PHRASE("'(?<>(?:\\\\'|[^'])*)'") {
         @Override
         public void register(Grammar grammar) {
            grammar.prefix(Types.PHRASE, (parser, token) -> new QueryExpression(Types.PHRASE,
                                                                                new Query.PhraseQuery(token.getVariable(
                                                                                      0))));
         }
      },
      /**
       * Not types.
       */
      NOT("-") {
         @Override
         public void register(Grammar grammar) {
            grammar.prefix(Types.NOT, (parser, token) -> {
               final Query r = parser.parseExpression(QueryExpression.class).query;
               return new QueryExpression(Types.NOT, new Query.Not(r));
            });
         }
      },
      /**
       * Attribute types.
       */
      ATTRIBUTE(re(e('$'),
                   namedGroup("", IDENTIFIER),
                   e('='),
                   re("'",
                      namedGroup("", oneOrMore(or(ESC_BACKSLASH + ".", notChars("'")))),
                      "'")
                  )) {
         @Override
         public void register(Grammar grammar) {
            grammar.prefix(Types.ATTRIBUTE, (parser, token) -> {
               AttributeType<?> attributeType = AttributeType.make(token.getVariable(0));
               Object value = attributeType.decode(token.getVariable(1));
               return new QueryExpression(Types.ATTRIBUTE, new Query.TermQuery(attributeType.name(), value));
            });
         }
      },
      WORD(re(oneOrMore(chars(true, "\\(\\)\\s")),
              zeroOrOne(or(NON_WHITESPACE, "\\(", "\\)"),
                        oneOrMore(chars(true, "\\(\\)\\s"))))) {
         @Override
         public void register(Grammar grammar) {
            grammar.prefix(Types.WORD, (parser, token) -> new QueryExpression(Types.PHRASE,
                                                                              new Query.PhraseQuery(token.getText()
                                                                                                         .trim())));
         }
      };
      private final String pattern;

      Types(String pattern) {
         this.pattern = pattern;
      }

      @Override
      public String getPattern() {
         return pattern;
      }

      @Override
      public boolean isInstance(Tag tag) {
         return tag == this;
      }
   }

   @ToString
   private static class QueryExpression extends BaseExpression implements Serializable {
      private static final long serialVersionUID = 1L;
      private final Query query;

      QueryExpression(Tag type, Query query) {
         super(type);
         this.query = query;
      }
   }

}//END OF QueryParser
