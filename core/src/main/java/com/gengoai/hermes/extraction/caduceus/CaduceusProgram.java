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

import com.gengoai.Validation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.io.resource.Resource;
import com.gengoai.parsing.ParseException;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Caduceus, pronounced <b>ca·du·ceus</b>, is a rule-based information extraction system. Caduceus programs consist
 * of a list of rules for extracting arbitrary spans of text to define annotations (e.g. entities and events) and
 * relations (e.g. event roles). Each rule starts with a unique name declared in square brackets, e.g.
 * <code>[my_rule]</code>. Following the rule name is the <i>trigger</i>, which is a {@link
 * com.gengoai.hermes.extraction.regex.TokenRegex} that captures the text causing the rule to fire.</p>
 *
 * <p>
 * Rules construct annotations and/or relations based on the matched trigger. A rule may have define zero or more
 * annotations to be constructed. Each annotation is defined using <code>annotation:</code> and requires the following
 * options to be specified:
 * <pre>
 * {@code
 * `capture=(\*|GROUP_NAME)`: The text span which will make up the annotation, where `\*` represents the full trigger match and `GROUP_NAME` represents a named group from the trigger match.
 * `type=ANNOTATION_TYPE`: The name of the annotation type to construct.
 * }
 * </pre>
 * Additionally, attributes can be defined using as follows: <code>$ATTRIBUTE_NAME = VALUE</code>.
 * </p>
 */
@ToString
@EqualsAndHashCode
public final class CaduceusProgram implements Serializable, Extractor {
   private final List<Rule> rules;

   CaduceusProgram(List<Rule> rules) {
      this.rules = rules;
   }

   /**
    * Reads a Caduceus program from the given resource.
    *
    * @param resource the resource containing the Caduceus program
    * @return the CaduceusProgram
    * @throws IOException    Something went wrong reading from the resource
    * @throws ParseException Something went wrong parsing the Caduceus program
    */
   public static CaduceusProgram read(@NonNull Resource resource) throws IOException, ParseException {
      return CaduceusParser.parse(resource);
   }

   /**
    * Executes the program over the given document.
    *
    * @param document the document to execute the program on
    */
   public void execute(@NonNull Document document) {
      rules.forEach(r -> r.execute(document));
   }

   @Override
   public Extraction extract(@NonNull HString hString) {
      Validation.checkArgument(hString instanceof Document, "Caduceus only accepts Document input");
      return Extraction.fromHStringList(rules.stream()
                                             .flatMap(r -> r.execute(hString.document()).stream())
                                             .collect(Collectors.toList()));
   }
}//END OF CaduceusProgram
