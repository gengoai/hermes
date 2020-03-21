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

import com.gengoai.hermes.*;
import com.gengoai.hermes.corpus.io.CoNLLColumnProcessor;
import com.gengoai.hermes.corpus.io.CoNLLRow;
import com.gengoai.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Map;

import static com.gengoai.hermes.corpus.io.CoNLLReader.EMPTY_FIELD;
import static com.gengoai.tuple.Tuples.$;

/**
 * Processes dependency governor (parent) information in CoNLL Files
 *
 * @author David B. Bracewell
 */
@MetaInfServices
public class DependencyLinkProcessor implements CoNLLColumnProcessor {

   @Override
   public void processInput(Document document, List<CoNLLRow> documentRows, Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
      documentRows.forEach(row -> {
         if (row.getParent() > 0) {
            long target = sentenceIndexToAnnotationId.get($(row.getSentence(), row.getParent()));
            document.annotation(row.getAnnotationID())
                    .add(new Relation(Types.DEPENDENCY, row.getDepRelation(), target));
         }
      });
   }

   @Override
   public String processOutput(HString document, Annotation token, int index) {
      Annotation targetAnnotation = token.dependency().v2;
      long targetID = targetAnnotation.isEmpty() ? -1 : targetAnnotation.getId();
      if (targetID < 0L) {
         return "0";
      }
      List<Annotation> sentence = document.tokens();
      for (int i = 0; i < sentence.size(); i++) {
         if (sentence.get(i).getId() == targetID) {
            return Integer.toString(i + 1);
         }
      }
      return EMPTY_FIELD;
   }

   @Override
   public String getFieldName() {
      return "HEAD";
   }

   @Override
   public void updateRow(CoNLLRow row, String part) {
      row.setParent(Integer.parseInt(part));
   }

}//END OF DependencyLinkProcessor
