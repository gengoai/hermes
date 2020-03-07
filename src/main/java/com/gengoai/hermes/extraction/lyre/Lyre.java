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

import com.gengoai.conversion.Cast;
import com.gengoai.function.CheckedFunction;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.HString;
import com.gengoai.parsing.*;
import com.gengoai.string.Strings;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gengoai.parsing.ParserGenerator.parserGenerator;

/**
 * Class for generating lambda functions using the LYRE (Linguistic querY and extRaction languagE).
 *
 * @author David B. Bracewell
 */
public class Lyre {
   private static final Evaluator<LyreExpression> evaluator = new Evaluator<LyreExpression>() {
      {
         $(LyreExpression.class, CheckedFunction.identity());
      }
   };

   private static final Grammar LYRE_GRAMMAR = new Grammar() {
      {
         for (LyreType value : LyreType.values()) {
            value.register(this);
         }
      }
   };

   private static final ParserGenerator PARSER_GENERATOR = parserGenerator(LYRE_GRAMMAR,
                                                                           Lexer.create(LyreType.values()));

   /**
    * Parse the given LYRE pattern into a LyreExpression Expression
    *
    * @param pattern the LYRE pattern
    * @return the LyreExpression Expression
    */
   public static LyreExpression parse(String pattern) {
      try {
         List<LyreExpression> expressions = PARSER_GENERATOR.create(pattern).evaluateAll(evaluator);
         if (expressions.size() != 1) {
            throw new ParseException(
               "Invalid number of expressions parsed (" + expressions.size() + "): " + expressions);
         }
         return expressions.get(0);
      } catch (ParseException e) {
         throw new RuntimeException(e);
      }
   }

   static Object postProcess(Object o) {
      if (o instanceof Collection) {
         Collection<?> c = Cast.<Collection<?>>as(o)
            .stream()
            .filter(Objects::nonNull)
            .filter(h -> !(h instanceof HString) || !Cast.<HString>as(h).isEmpty())
            .filter(cs -> !(cs instanceof CharSequence) || Strings.isNotNullOrBlank(cs.toString()))
            .collect(Collectors.toList());
         if (c.isEmpty()) {
            return null;
         }
         if (c.size() == 1) {
            return c.iterator().next();
         }
      }
      return o;
   }


   static Object process(boolean ignoreNulls, Object o, SerializableFunction<Object, ?> function) {
      if (ignoreNulls && o == null) {
         return null;
      }
      if (o instanceof Collection) {
         return postProcess(Cast.<Collection<?>>as(o)
                               .stream()
                               .map(v -> postProcess(function.apply(v)))
                               .filter(Objects::nonNull)
                               .filter(cs -> !(cs instanceof CharSequence) || Strings.isNotNullOrBlank(Cast.as(cs)))
                               .collect(Collectors.toList()));
      }
      return postProcess(function.apply(o));
   }

   static Object filter(Object o, SerializablePredicate<Object> function) {
      if (o instanceof Collection) {
         return postProcess(Cast.<Collection<?>>as(o)
                               .stream()
                               .map(o2 -> filter(o2, function))
                               .filter(Objects::nonNull)
                               .collect(Collectors.toList())
                           );
      }
      return function.test(o) ? o : null;
   }

   static Object processPred(Object o, SerializablePredicate<Object> function) {
      if (o instanceof Collection) {
         return postProcess(Cast.<Collection<?>>as(o)
                               .stream()
                               .filter(Objects::nonNull)
                               .map(o2 -> filter(o2, function))
                               .filter(Objects::nonNull)
                               .filter(cs -> !(cs instanceof CharSequence) || Strings.isNotNullOrBlank(Cast.as(cs)))
                               .collect(Collectors.toList()));
      }
      return function.test(o);
   }
}//END OF HQLParser
