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

package com.gengoai.hermes.format.conll;

import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.format.CoNLLColumnProcessor;
import com.gengoai.hermes.format.CoNLLRow;
import com.gengoai.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Map;

/**
 * Processes lemma information in CoNLL Files
 *
 * @author David B. Bracewell
 */
@MetaInfServices
public class LemmaProcessor implements CoNLLColumnProcessor {

   @Override
   public String getFieldName() {
      return "LEMMA";
   }

   @Override
   public void processInput(Document document,
                            List<CoNLLRow> documentRows,
                            Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
      documentRows
            .stream()
            .filter(row -> row.hasOther("LEMMA"))
            .forEach(row -> document.annotation(row.getAnnotationID())
                                    .ifNotEmpty(a -> a.put(Types.LEMMA, row.getOther("LEMMA"))));
   }

   @Override
   public String processOutput(HString document, Annotation token, int index) {
      return token.getLemma();
   }
}//END OF LemmaProcessor
