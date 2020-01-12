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

package com.gengoai.hermes.corpus.io.conll;

import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.io.CoNLLColumnProcessor;
import com.gengoai.hermes.corpus.io.CoNLLRow;
import com.gengoai.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Map;

/**
 * Processes token index information in CoNLL Files
 *
 * @author David B. Bracewell
 */
@MetaInfServices
public final class IndexProcessor implements CoNLLColumnProcessor {

   @Override
   public void processInput(Document document, List<CoNLLRow> documentRows, Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {

   }

   @Override
   public String processOutput(HString document, Annotation token, int index) {
      return Integer.toString(index + 1);
   }

   @Override
   public String getFieldName() {
      return "INDEX";
   }

   @Override
   public void updateRow(CoNLLRow row, String part) {
      row.setIndex(Integer.parseInt(part));
   }

}// END OF IndexProcessor
