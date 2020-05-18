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

package com.gengoai.hermes.extraction.summarization;

import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extractor;
import lombok.NonNull;

import java.io.Serializable;

public interface Summarizer extends Extractor, Serializable {

   /**
    * In certain cases a Summarizer needs to collect corpus level statistics or construct a model of what a good
    * summarization looks like. The fit method allows implementations to perform this logic at a corpus level.
    *
    * @param corpus the corpus to fit the extractor to
    */
   void fit(@NonNull DocumentCollection corpus);

}//END OF Summarizer
