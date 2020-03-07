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

import com.gengoai.hermes.corpus.Corpus;

import java.io.Serializable;

/**
 * Defines a {@link CorpusReader} and {@link CorpusWriter} to read and write in given format.
 *
 * @author David B. Bracewell
 */
public interface CorpusFormat extends Serializable {

   /**
    * Gets corpus reader.
    *
    * @return the {@link CorpusReader}
    */
   CorpusReader getCorpusReader();

   /**
    * Gets corpus writer.
    *
    * @param corpus the corpus
    * @return the {@link CorpusWriter}
    */
   CorpusWriter getCorpusWriter(Corpus corpus);

   /**
    * Gets the default set of {@link CorpusParameters} associated with this format
    *
    * @return the default set of {@link CorpusParameters} for this format
    */
   default CorpusParameters getFormatParameters() {
      return new CorpusParameters();
   }

   /**
    * Gets the name of the format which is used in calls to {@link Corpus#reader(CharSequence)} } and {@link
    * Corpus#writer(CharSequence)}*
    *
    * @return the name of the format
    */
   String getName();

}//END OF CorpusFormat
