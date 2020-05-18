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

package com.gengoai.hermes.similarity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.DocumentCollection;
import lombok.NonNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface HStringSimilarity {

   double calculate(@NonNull HString first, @NonNull HString second);

   /**
    * In certain cases a HStringSimilarity needs to collect corpus level statistics to determine similarity. The fit
    * method allows implementations to perform this logic at a corpus level.
    *
    * @param corpus the corpus to fit the similarity measure to
    */
   void fit(@NonNull DocumentCollection corpus);

}//END OF HStringSimilarity
