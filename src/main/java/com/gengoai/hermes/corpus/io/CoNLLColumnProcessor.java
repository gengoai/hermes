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

package com.gengoai.hermes.corpus.io;

import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.tuple.Tuple2;

import java.util.List;
import java.util.Map;

/**
 * Interface defining how to process a column from a CoNLL formatted document.
 *
 * @author David B. Bracewell
 */
public interface CoNLLColumnProcessor {

   /**
    * Processes a set of CoNLL rows making up a document
    *
    * @param document                    the document
    * @param documentRows                the CoNLL rows making up the document
    * @param sentenceIndexToAnnotationId the index of the token in the sentence to annotation id
    */
   void processInput(Document document, List<CoNLLRow> documentRows, Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId);

   /**
    * Generates output data in CoNLL format
    *
    * @param document the {@link HString} representing the document.
    * @param token    the token
    * @param index    the index
    * @return the string
    */
   String processOutput(HString document, Annotation token, int index);

   /**
    * Gets the name of the field
    *
    * @return the field name
    */
   String getFieldName();

   /**
    * Updates a CoNLL row with this field
    *
    * @param row  the row to update
    * @param part the part to update
    */
   default void updateRow(CoNLLRow row, String part) {
      row.addOther(getFieldName(), part);
   }

}//END OF CoNLLColumnProcessor
