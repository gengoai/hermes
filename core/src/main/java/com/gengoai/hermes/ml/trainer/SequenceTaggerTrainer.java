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
   public ExampleDataset createDataset(@NonNull DocumentCollection data) {
      Stopwatch sw = Stopwatch.createStarted();
      ExampleDataset dataset = data.asDataset(getSequenceGenerator(), DatasetType.InMemory);
      sw.stop();
      logInfo(log, "Took {0} to create dataset with {1} examples.", sw, dataset.size());
      return dataset;
   }

   /**
    * Create feature extractor feature extractor.
    *
    * @return the feature extractor
    */
   public abstract FeatureExtractor<HString> createFeatureExtractor();

   /**
    * Create sequence labeler sequence labeler.
    *
    * @return the sequence labeler
    */
   protected abstract SequenceLabeler createSequenceLabeler();

   /**
    * Create tagger t.
    *
    * @param labeler          the labeler
    * @param featureExtractor the feature extractor
    * @return the t
    */
   protected abstract T createTagger(SequenceLabeler labeler, FeatureExtractor<HString> featureExtractor);

   /**
    * Fit t.
    *
    * @param trainingData  the training data
    * @param fitParameters the fit parameters
    * @return the t
    */
   public T fit(DocumentCollection trainingData, FitParameters<?> fitParameters) {
      SequenceLabeler labeler = createSequenceLabeler();
      labeler.fit(createDataset(trainingData), fitParameters);
      return createTagger(labeler, createFeatureExtractor());
   }

   /**
    * Gets default fit parameters.
    *
    * @return the default fit parameters
    */
   public abstract FitParameters<?> getDefaultFitParameters();

   /**
    * Gets sequence generator.
    *
    * @return the sequence generator
    */
   protected abstract SequenceGenerator getSequenceGenerator();

}//END OF SequenceTaggerTrainer
