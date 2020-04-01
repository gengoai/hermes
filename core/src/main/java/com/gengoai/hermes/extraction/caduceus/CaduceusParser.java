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

package com.gengoai.hermes.extraction.caduceus;

import com.gengoai.hermes.AttributeMap;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.extraction.lyre.Lyre;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.io.resource.Resource;
import com.gengoai.parsing.*;

import java.io.IOException;

import static com.gengoai.string.Re.*;
import static com.gengoai.tuple.Tuples.$;

enum CaduceusParser implements TokenDef {
   COMMENT(re("//", oneOrMore(notChars("\r\n")))),
   RULE_NAME(re(e('['), namedGroup("", oneOrMore("\\w")), e(']'))),
   TRIGGER(re("trigger",
              zeroOrMore(WHITESPACE),
              e(':'),
              zeroOrMore(WHITESPACE),
              namedGroup("", oneOrMore(notChars("\r\n")))
             )),
   ANNOTATION(re("annotation",
                 zeroOrMore(WHITESPACE),
                 e(':'),
                 zeroOrMore(WHITESPACE),
                 namedGroup("",
                            oneOrMore(zeroOrMore(WHITESPACE),
                                      or("capture", "type", re(e('$'), Hermes.IDENTIFIER), "requires"),
                                      zeroOrMore(WHITESPACE),
                                      e('='),
                                      zeroOrMore(WHITESPACE),
                                      oneOrMore(notChars("\r\n")),
                                      zeroOrMore(chars("\r\n")))
                           ))),
   RELATION(re("relation",
               zeroOrMore(WHITESPACE),
               e(':'),
               zeroOrMore(WHITESPACE),
               namedGroup("", oneOrMore(notChars("\r\n"))),
               zeroOrMore(chars("\r\n")),
               namedGroup("",
                          oneOrMore(zeroOrMore(WHITESPACE),
                                    or("value", "type", "requires", "bidirectional",
                                       re(or(q("@>"), q("@<")),
                                          zeroOrOne(e('{'),
                                                    Hermes.IDENTIFIER,
                                                    e('}'))
                                         )),
                                    zeroOrMore(WHITESPACE),
                                    e('='),
                                    zeroOrMore(WHITESPACE),
                                    oneOrMore(notChars("\r\n")),
                                    zeroOrMore(chars("\r\n")))
                         )));


   final String pattern;

   CaduceusParser(String pattern) {
      this.pattern = pattern;
   }

   private static String[] createComponents(String in) {
      return in.trim().replaceAll("\n\\s+", "\n").split("[\r?\n]+");
   }

   public static CaduceusProgram parse(Resource resource) throws ParseException, IOException {
      Lexer lexer = Lexer.create(CaduceusParser.values());
      TokenStream ts = lexer.lex(resource);
      final CaduceusProgram program = new CaduceusProgram();
      Rule.RuleBuilder rule = null;
      while (ts.hasNext()) {
         ParserToken token = ts.consume();
         if (token.isInstance(RULE_NAME)) {
            if (rule != null) {
               program.rules.add(rule.build());
            }
            rule = Rule.builder();
            rule.name(token.getVariable(0));
            rule.programFile(resource.descriptor());
         } else if (token.isInstance(TRIGGER)) {
            if (rule == null) {
               throw new ParseException("Found a TRIGGER outside of a Rule");
            }
            rule.trigger(TokenRegex.compile(token.getVariable(0)));
         } else if (token.isInstance(ANNOTATION)) {
            if (rule == null) {
               throw new ParseException("Found an ANNOTATION provider outside of a Rule");
            }
            rule.annotationProvider(processAnnotation(createComponents(token.getVariable(0))));
         } else if (token.isInstance(RELATION)) {
            if (rule == null) {
               throw new ParseException("Found a RELATION provider outside of a Rule");
            }
            rule.relationProvider(processRelation(token.getVariable(0).trim(),
                                                  createComponents(token.getVariable(1))));
         }
      }
      if (rule != null) {
         program.rules.add(rule.build());
      }
      return program;
   }

   private static AnnotationProvider processAnnotation(String[] components) {
      AnnotationProvider.AnnotationProviderBuilder builder = AnnotationProvider.builder();
      final AttributeMap attributeMap = new AttributeMap();
      for (String component : components) {
         String[] keyValue = component.split("\\s*=\\s*", 2);
         if (keyValue[0].equals("capture")) {
            builder.capture(keyValue[1].trim());
         } else if (keyValue[0].equals("type")) {
            builder.type(Types.annotation(keyValue[1].trim()));
         } else if (keyValue[0].equals("requires")) {
            builder.requires(keyValue[1].trim());
         } else if (keyValue[0].startsWith("$")) {
            AttributeType<?> attributeType = Types.attribute(keyValue[0].substring(1).trim());
            attributeMap.put(attributeType, attributeType.decode(keyValue[1].trim()));
         } else {
            throw new IllegalStateException("Invalid Key-Value: " + component);
         }
      }
      builder.attributeMap(attributeMap);
      return builder.build();
   }

   private static RelationProvider processRelation(String name, String[] components) {
      RelationProvider.RelationProviderBuilder builder = RelationProvider.builder();
      builder.name(name);
      for (String component : components) {
         String[] keyValue = component.split("\\s*=\\s*", 2);
         if (keyValue[0].equals("type")) {
            builder.type(Types.relation(keyValue[1].trim()));
         } else if (keyValue[0].equals("requires")) {
            builder.requires(keyValue[1].trim());
         } else if (keyValue[0].equals("value")) {
            builder.value(keyValue[1].trim());
         } else if (keyValue[0].equals("bidirectional")) {
            builder.bidirectional(Boolean.parseBoolean(keyValue[1].trim()));
         } else if (keyValue[0].startsWith("@>") || keyValue[0].startsWith("@<")) {
            int index = keyValue[0].indexOf('{');
            String capture = "*";
            if (index > 0) {
               capture = keyValue[0].substring(index + 1, keyValue[0].length() - 1);
            }
            if (keyValue[0].startsWith("@>")) {
               builder.source($(capture, Lyre.parse(keyValue[1])));
            } else {
               builder.target($(capture, Lyre.parse(keyValue[1])));
            }
         } else {
            throw new IllegalStateException("Invalid Key-Value: " + component);
         }
      }
      return builder.build();
   }

   @Override
   public String getPattern() {
      return pattern;
   }
}//END OF CaduceusParser
