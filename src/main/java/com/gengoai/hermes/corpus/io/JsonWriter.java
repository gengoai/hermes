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

import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.Corpus;

/**
 * Writer for HERMES Json Format
 *
 * @author David B. Bracewell
 */
public class JsonWriter extends CorpusWriter {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Json writer.
    *
    * @param corpus the corpus
    */
   public JsonWriter(Corpus corpus) {
      super(corpus);
   }


   @Override
   public String writeToString(Document document) {
      return document.toJson();
   }
}//END OF JsonWriter
