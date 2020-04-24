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
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.SequenceTagger;
import lombok.NonNull;
import lombok.extern.java.Log;

import static com.gengoai.LogUtils.logInfo;

/**
 * The type Sequence tagger trainer.
 *
 * @param <T> the type parameter
 */
@Log
public abstract class SequenceTaggerTrainer<T extends SequenceTagger> {
   /**
    * Create dataset example dataset.
    *
    * @param data the data
    * @return the example dataset
    */
   public DataSet createDataset(@NonNull DocumentCollection data) {
      Stopwatch sw = Stopwatch.createStarted();
      DataSet dataset = data.asDataSet(getExampleGenerator()).cache();
      sw.stop();
      logInfo(log, "Took {0} to create dataset with {1} examples.", sw, dataset.size());
      return dataset;
   }

   /**
    * Create sequence labeler sequence labeler.
    *
    * @return the sequence labeler
    */
   protected abstract Model createSequenceLabeler(FitParameters<?> fitParameters);

   /**
    * Create tagger t.
    *
    * @param labeler          the labeler
    * @param featureExtractor the feature extractor
    * @return the t
    */
   protected abstract T createTagger(Model labeler, HStringDataSetGenerator featureExtractor);

   /**
    * Fit t.
    *
    * @param trainingData  the training data
    * @param fitParameters the fit parameters
    * @return the t
    */
   public T fit(DocumentCollection trainingData, FitParameters<?> fitParameters) {
      Model labeler = createSequenceLabeler(fitParameters);
      labeler.estimate(createDataset(trainingData));
      return createTagger(labeler, getExampleGenerator());
   }

   protected abstract HStringDataSetGenerator getExampleGenerator();

   public abstract FitParameters<?> getFitParameters();

}//END OF SequenceTaggerTrainer
