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

package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.data.Dataset;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.CorpusDefinition;
import com.gengoai.hermes.corpus.ExampleGenerator;
import com.gengoai.hermes.extraction.lyre.Lyre;
import com.gengoai.hermes.extraction.lyre.LyreExpression;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author David B. Bracewell
 */
public class ClassifierConfiguration implements Serializable {
   private static final long serialVersionUID = 1L;
   private CorpusDefinition corpusDefinition;
   private ExampleGenerator datasetDefinition;
   private LyreExpression documentFilter = null;

   public ClassifierConfiguration corpusDefinition(CorpusDefinition definition) {
      this.corpusDefinition = definition;
      return this;
   }

   public ClassifierConfiguration datasetDefinition(ExampleGenerator definition) {
      this.datasetDefinition = definition;
      return this;
   }

   public ClassifierConfiguration documentFilter(LyreExpression pattern) {
      this.documentFilter = pattern;
      return this;
   }

   public ClassifierConfiguration documentFilter(String pattern) {
      this.documentFilter = Lyre.parse(pattern);
      return this;
   }

   public Dataset getDataset() throws IOException {
      Corpus corpus = corpusDefinition.create();
      if (documentFilter != null) {
         corpus = corpus.filter(documentFilter);
      }
      return corpus.asDataset(datasetDefinition);
   }

}//END OF ClassifierConfiguration
