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
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;

import java.io.IOException;

import static com.gengoai.hermes.corpus.io.CorpusParameters.SAVE_MODE;

/**
 * Base class for defining how to write a {@link Corpus} to an output format
 *
 * @author David B. Bracewell
 */
public abstract class CorpusWriter extends CorpusIOBase<CorpusWriter> {
   private static final long serialVersionUID = 1L;
   /**
    * The Corpus.
    */
   protected final Corpus corpus;

   /**
    * Instantiates a new CorpusWriter.
    *
    * @param corpus the corpus
    */
   protected CorpusWriter(Corpus corpus) {
      this(corpus, new CorpusParameters());
   }

   /**
    * Instantiates a new CorpusWriter.
    *
    * @param corpus     the corpus
    * @param parameters the parameters
    */
   protected CorpusWriter(Corpus corpus, CorpusParameters parameters) {
      super(parameters);
      this.corpus = corpus;
   }


   /**
    * Write the corpus to given location
    *
    * @param location the location
    * @throws IOException Something went wrong writing
    */
   public final void write(String location) throws IOException {
      write(Resources.from(location));
   }

   /**
    * Write the corpus to given location
    *
    * @param location the location
    * @throws IOException Something went wrong writing
    */
   public void write(Resource location) throws IOException {
      final SaveMode saveMode = getOptions().get(SAVE_MODE);
      if (saveMode.validate(location)) {
         for (Document document : corpus) {
            Resource fileName = location.getChild(document.getId());
            fileName.write(writeToString(document));
         }
      }
   }

   /**
    * Writes the given {@link Document} in this format returning it as a String
    *
    * @param document the document
    * @return the string representation of the document.
    */
   public abstract String writeToString(Document document);


}//END OF CorpusWriter
