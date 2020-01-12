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
import org.kohsuke.MetaInfServices;

/**
 * Format for Corpora stored as Lucene Indexes
 *
 * @author David B. Bracewell
 */
@MetaInfServices
public class LuceneFormat implements CorpusFormat {
   @Override
   public String getName() {
      return "lucene";
   }

   @Override
   public CorpusReader getCorpusReader() {
      return new LuceneReader();
   }

   @Override
   public CorpusWriter getCorpusWriter(Corpus corpus) {
      return new LuceneWriter(corpus);
   }

}//END OF LuceneFormat
