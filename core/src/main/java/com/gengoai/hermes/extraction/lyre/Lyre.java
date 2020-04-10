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

import com.gengoai.function.CheckedFunction;
import com.gengoai.parsing.*;

import java.util.List;

import static com.gengoai.parsing.ParserGenerator.parserGenerator;

/**
 * <p>
 * Lyre (Linguistic querY and extRaction languagE) provides a means for querying, extracting, and transforming
 * HStrings.
 * </p>
 *
 * @author David B. Bracewell
 */
public class Lyre {
   private static final Evaluator<LyreExpression> evaluator = new Evaluator<>() {
      {
         $(LyreExpression.class, CheckedFunction.identity());
      }
   };
   private static final ParserGenerator PARSER_GENERATOR = parserGenerator(new Grammar(LyreType.values()),
                                                                           Lexer.create(LyreType.values()));

   /**
    * Parse the given pattern into a {@link LyreExpression}.
    *
    * @param pattern the pattern
    * @return the LyreExpression Expression
    */
   public static LyreExpression parse(String pattern) {
      try {
         List<LyreExpression> expressions = PARSER_GENERATOR.create(pattern).evaluateAll(evaluator);
         if(expressions.size() != 1) {
            throw new ParseException(
                  "Invalid number of expressions parsed (" + expressions.size() + "): " + expressions);
         }
         return expressions.get(0);
      } catch(ParseException e) {
         throw new RuntimeException(e);
      }
   }

}//END OF Lyre
