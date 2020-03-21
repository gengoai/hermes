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
import com.gengoai.hermes.corpus.LuceneCorpus;
import com.gengoai.io.resource.Resource;

import java.io.IOException;

import static com.gengoai.hermes.corpus.io.CorpusParameters.SAVE_MODE;

/**
 * Writes Corpora to Lucene Indexes
 *
 * @author David B. Bracewell
 */
public class HCFWriter extends CorpusWriter {

   /**
    * Instantiates a new Lucene writer.
    *
    * @param corpus the corpus
    */
   public HCFWriter(Corpus corpus) {
      super(corpus);
   }

   @Override
   public void write(Resource location) throws IOException {
      final SaveMode saveMode = getOptions().get(SAVE_MODE);
      if(saveMode.validate(location)) {
         LuceneCorpus luceneCorpus = new LuceneCorpus(location.asFile().orElseThrow(IllegalArgumentException::new));
         luceneCorpus.addAll(this.corpus);
         luceneCorpus.close();
      }
   }

   @Override
   public String writeToString(Document document) {
      throw new UnsupportedOperationException();
   }
}//END OF HCFWriter
