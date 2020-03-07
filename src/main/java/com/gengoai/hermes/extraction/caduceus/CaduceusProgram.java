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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
public final class CaduceusProgram implements Serializable, Extractor {
   final List<Rule> rules = new ArrayList<>();

   public static CaduceusProgram read(@NonNull Resource resource) throws IOException, ParseException {
      return CaduceusParser.parse(resource);
   }

   public void execute(@NonNull Document document) {
      for(Rule rule : rules) {
         rule.execute(document);
      }
   }

   @Override
   public Extraction extract(@NonNull HString hString) {
      Validation.checkArgument(hString instanceof Document, "Caduceus only accepts Document input");
      return Extraction.fromHStringList(rules.stream()
                                             .flatMap(r -> r.execute(hString.document()).stream())
                                             .collect(Collectors.toList()));
   }
}//END OF CaduceusProgram
