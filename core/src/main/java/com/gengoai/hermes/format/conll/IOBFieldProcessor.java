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

import com.gengoai.StringTag;
import com.gengoai.Tag;
import com.gengoai.hermes.*;
import com.gengoai.hermes.format.CoNLLColumnProcessor;
import com.gengoai.hermes.format.CoNLLRow;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;

import java.util.List;
import java.util.Map;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

/**
 * Base processor for IOB (Inside, Outside, Beginning) annotations in CoNLL Files
 *
 * @author David B. Bracewell
 */
public abstract class IOBFieldProcessor implements CoNLLColumnProcessor {

   private final AnnotationType annotationType;
   private final AttributeType<?> attributeType;

   /**
    * Instantiates a new IOBFieldProcessor.
    *
    * @param annotationType the annotation type
    * @param attributeType  the attribute type
    */
   public IOBFieldProcessor(AnnotationType annotationType, AttributeType<?> attributeType) {
      this.annotationType = annotationType;
      this.attributeType = attributeType;
   }

   private boolean isI(String value, String target) {
      if(value == null || value.startsWith("O") || value.startsWith("B-")) {
         return false;
      }
      return value.startsWith("I-") && value.substring(2).toUpperCase().equals(target);
   }

   /**
    * Normalize tag string.
    *
    * @param tag the tag
    * @return the string
    */
   protected String normalizeTag(String tag) {
      return tag;
   }

   @Override
   public void processInput(Document document,
                            List<CoNLLRow> rows,
                            Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
      final String TYPE = getFieldName();

      for(int i = 0; i < rows.size(); ) {
         if(rows.get(i).hasOther(TYPE)) {

            String value = rows.get(i).getOther(TYPE).toUpperCase();
            if(Strings.isNotNullOrBlank(value) && (value.startsWith("B-") || value.startsWith("I-"))) {
               int start = rows.get(i).getStart();
               String tag = value.substring(2);
               i++;
               while(i < rows.size() && isI(rows.get(i).getOther(TYPE), tag)) {
                  i++;
               }
               i--;
               int end = rows.get(i).getEnd();

               String normalizedTag = normalizeTag(tag);
               if(Strings.isNotNullOrBlank(normalizedTag)) {
                  var a = document.createAnnotation(annotationType,
                                                    start,
                                                    end,
                                                    hashMapOf($(attributeType,
                                                                attributeType.decode(normalizedTag))));
               }
            }
         }
         i++;
      }
      document.setCompleted(annotationType, "PROVIDED");
   }

   @Override
   public String processOutput(HString document, Annotation token, int index) {
      Annotation a = token.first(annotationType);
      if(a.isDetached()) {
         return "O";
      }
      Tag tag = a.getTag(new StringTag("O"));
      if(a.hasTag()) {
         if(a.firstToken() == token) {
            return "B-" + tag.name();
         }
         return "I-" + tag.name();
      }
      return "O";
   }

}//END OF IOBFieldProcessor
