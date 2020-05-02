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
import com.gengoai.hermes.format.POSCorrection;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.gengoai.hermes.format.CoNLLFormat.EMPTY_FIELD;

/**
 * Processes part-of-speech fields
 *
 * @author David B. Bracewell
 */
@MetaInfServices
public final class POSFieldProcessor implements CoNLLColumnProcessor {

   @Override
   public String getFieldName() {
      return "POS";
   }

   @Override
   public void processInput(Document document,
                            List<CoNLLRow> documentRows,
                            Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
      AtomicBoolean completed = new AtomicBoolean(true);
      documentRows.forEach(row -> {
         String posStr = row.getPos();
         if(posStr.contains("|")) {
            posStr = posStr.substring(0, posStr.indexOf('|'));
         }
         if(posStr.equals("XX")) { //SPECIAL CASE FOR ONTONOTES
            completed.set(false);
         } else {
            document.annotation(row.getAnnotationID())
                    .put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(POSCorrection.pos(row.getWord(), posStr)));
         }
      });

      if(completed.get()) {
         document.setCompleted(Types.PART_OF_SPEECH, "PROVIDED");
      }
   }

   @Override
   public String processOutput(HString sentence, Annotation token, int index) {
      PartOfSpeech pos = token.pos();
      return pos == null || pos == PartOfSpeech.ANY
             ? EMPTY_FIELD
             : pos.tag();
   }

   @Override
   public void updateRow(CoNLLRow row, String part) {
      row.setPos(part);
   }
}//END OF POSFieldProcessor
