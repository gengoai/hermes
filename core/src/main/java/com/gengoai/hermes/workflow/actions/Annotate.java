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

package com.gengoai.hermes.workflow.actions;

import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.string.Strings;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Arrays;

import static com.gengoai.LogUtils.logConfig;

/**
 * The type Annotate processor.
 *
 * @author David B. Bracewell
 */
@Log
public class Annotate implements Action {
   private static final long serialVersionUID = 1L;
   public static final String ANNOTATABLE_TYPE_CONFIG = "ANNOTATE_TYPES";
   private AnnotatableType[] types = {Types.SENTENCE, Types.LEMMA, Types.PHRASE_CHUNK, Types.DEPENDENCY, Types.ENTITY};

   public Annotate() {

   }

   public Annotate(@NonNull AnnotatableType... types) {
      this.types = types;
   }

   /**
    * Get types string [ ].
    *
    * @return the string [ ]
    */
   public String[] getTypes() {
      return Arrays.stream(types).map(AnnotatableType::canonicalName).toArray(String[]::new);
   }

   @Override
   public DocumentCollection process(@NonNull DocumentCollection corpus, @NonNull Context context) throws Exception {
      String contextTypes = context.getString(ANNOTATABLE_TYPE_CONFIG);
      if(Strings.isNotNullOrBlank(contextTypes)) {
         AnnotatableType[] types = Strings.split(contextTypes, ',')
                                          .stream()
                                          .map(AnnotatableType::valueOf)
                                          .toArray(AnnotatableType[]::new);
         logConfig(log, "Annotating corpus for {0}", Arrays.toString(types));
         return corpus.annotate(types);
      } else {
         logConfig(log, "Annotating corpus for {0}", Arrays.toString(types));
         return corpus.annotate(types);
      }
   }

   /**
    * Sets types.
    *
    * @param types the types
    */
   public void setTypes(@NonNull String[] types) {
      this.types = Arrays.stream(types)
                         .map(AnnotatableType::valueOf)
                         .toArray(AnnotatableType[]::new);
   }

   @Override
   public String toString() {
      return "AnnotateProcessor{" +
            "types=" + Arrays.toString(types) +
            '}';
   }
}//END OF AnnotateProcessor
