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

package com.gengoai.hermes.ml.trainer;

import com.gengoai.Stopwatch;
import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.FitParameters;
import com.gengoai.apollo.ml.data.DatasetType;
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.SequenceGenerator;
import com.gengoai.hermes.ml.SequenceTagger;
import lombok.NonNull;
import lombok.extern.java.Log;

import static com.gengoai.LogUtils.logFine;

@Log
public abstract class SequenceTaggerTrainer<T extends SequenceTagger> {

   public ExampleDataset createDataset(@NonNull DocumentCollection data) {
      Stopwatch sw = Stopwatch.createStarted();
      ExampleDataset dataset = data.asDataset(getSequenceGenerator(), DatasetType.InMemory);
      sw.stop();
      logFine(log, "Took {0} to create dataset.", sw);
      return dataset;
   }

   public abstract FeatureExtractor<HString> createFeatureExtractor();

   protected abstract SequenceLabeler createSequenceLabeler();

   protected abstract T createTagger(SequenceLabeler labeler, FeatureExtractor<HString> featureExtractor);

   public T fit(DocumentCollection trainingData) {
      SequenceLabeler labeler = createSequenceLabeler();
      labeler.fit(createDataset(trainingData), getDefaultFitParameters());
      return createTagger(labeler, createFeatureExtractor());
   }

   public abstract FitParameters<?> getDefaultFitParameters();

   protected abstract SequenceGenerator getSequenceGenerator();

}//END OF SequenceTaggerTrainer
