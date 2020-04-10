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

package com.gengoai.hermes.format;

import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;

import java.io.IOException;

/**
 * <p>A DocFormat defines how to read and write documents in a given format. Each document format has an associated set
 * of {@link DocFormatParameters} that define the various options for reading and writing in the format. By default the
 * following parameters can be set:</p>
 * <p><ul>
 * <li>defaultLanguage - The default language for new documents. (default calls Hermes.defaultLanguage())</li>
 * <li>normalizers - The class names of the text normalizes to use when constructing documents. (default calls
 * TextNormalization.configuredInstance().getPreprocessors())</li>
 * <li>distributed - Creates a distributed document collection when the value is set to true (default false).</li>
 * <li>saveMode -Whether to overwrite, ignore, or throw an error when writing a corpus to an existing file/directory
 * (default ERROR).</li>
 * </ul></p>
 */
public interface DocFormat {

   /**
    * @return the {@link DocFormatParameters} set for the instance of this foramt
    */
   DocFormatParameters getParameters();

   /**
    * Reads documents in this format from the given input resource.
    *
    * @param inputResource the input resource
    * @return the stream of documents read
    */
   MStream<Document> read(Resource inputResource);

   /**
    * Writes a corpus of documents in this format to the given output resource
    *
    * @param corpus         the corpus
    * @param outputResource the output resource
    * @throws IOException Something went wrong writing the corpus
    */
   void write(Corpus corpus, Resource outputResource) throws IOException;

   /**
    * Writes the given document in this format to the given output resource.
    *
    * @param document       the document
    * @param outputResource the output resource
    * @throws IOException Something went wrong writing the document
    */
   void write(Document document, Resource outputResource) throws IOException;

}//END OF DocFormat
