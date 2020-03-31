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
 */

package com.gengoai.hermes.ml;

import com.gengoai.LogUtils;
import com.gengoai.Stopwatch;
import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.FitParameters;
import com.gengoai.apollo.ml.Params;
import com.gengoai.apollo.ml.Split;
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.ml.preprocess.MinCountTransform;
import com.gengoai.apollo.ml.preprocess.PreprocessorList;
import com.gengoai.apollo.ml.sequence.Labeling;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.apollo.ml.sequence.SequenceValidator;
import com.gengoai.application.CommandLineApplication;
import com.gengoai.application.Option;
import com.gengoai.config.Config;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.io.resource.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import static com.gengoai.LogUtils.logInfo;

/**
 * The type Bio trainer.
 *
 * @author David B. Bracewell
 */
public abstract class BIOTrainer extends CommandLineApplication {
   private static final long serialVersionUID = 1L;

   /**
    * The Annotation type.
    */
   protected final AnnotationType annotationType;
   /**
    * The Corpus.
    */
   @Option(description = "The specification of the document collection to use as the training data", required = true)
   protected String data;
   /**
    * The Model.
    */
   @Option(description = "Location to save model", required = true)
   protected Resource model;
   /**
    * The Min feature count.
    */
   @Option(description = "Minimum count for a feature to be kept", defaultValue = "5")
   protected int minFeatureCount;
   /**
    * The Mode.
    */
   @Option(description = "TEST or TRAIN", defaultValue = "TEST")
   protected Mode mode;
   /**
    * The Training annotation.
    */
   @Option(name = "annotation", description = "The annotation type in the corpus to train with", aliases = "a")
   protected AnnotationType trainingAnnotation;

   /**
    * Instantiates a new Bio trainer.
    *
    * @param name           the name
    * @param annotationType the annotation type
    */
   public BIOTrainer(String name, AnnotationType annotationType) {
      super(name);
      this.annotationType = annotationType;
      if(this.trainingAnnotation == null) {
         this.trainingAnnotation = annotationType;
      }
   }

   /**
    * Gets dataset.
    *
    * @param featurizer the featurizer
    * @return the dataset
    * @throws IOException the io exception
    */
   protected ExampleDataset getDataset(FeatureExtractor<HString> featurizer) throws IOException {
      Stopwatch read = Stopwatch.createStarted();
      DocumentCollection collection = DocumentCollection.create(data);
      read.stop();
      logInfo(LogUtils.getLogger(getClass()), "Completed reading corpus in: {0}", read);
      if(required().length > 0) {
         collection = collection.annotate(required());
      }
      return collection.asSequenceDataset(s -> {
         s.featureExtractor(getFeaturizer());
         s.labelGenerator(new BIOLabelMaker(trainingAnnotation, validTags()));
      });
   }

   @Override
   public Set<String> getDependentPackages() {
      return Collections.singleton(Hermes.HERMES_PACKAGE);
   }

   /**
    * Gets featurizer.
    *
    * @return the featurizer
    */
   protected abstract FeatureExtractor<HString> getFeaturizer();

   protected FitParameters<?> getFitParameters() {
      return null;
   }

   /**
    * Gets learner.
    *
    * @return the learner
    */
   protected abstract SequenceLabeler getLearner();

   /**
    * Gets preprocessors.
    *
    * @return the preprocessors
    */
   protected PreprocessorList getPreprocessors() {
      if(minFeatureCount > 1) {
         return new PreprocessorList(new MinCountTransform(minFeatureCount));
      }
      return new PreprocessorList();
   }

   /**
    * Gets validator.
    *
    * @return the validator
    */
   protected SequenceValidator getValidator() {
      return new BIOValidator();
   }

   /**
    * Label.
    *
    * @throws Exception the exception
    */
   protected void label() throws Exception {
      BIOTagger tagger = BIOTagger.read(model);
      ExampleDataset test = getDataset(tagger.featurizer).sample(true, 25);
      String fname = Config.get("wordFeature").asString("WORD");
      test.forEach(seq -> {
         Labeling labeling = tagger.labeler.label(seq);
         for(int i = 0; i < seq.size(); i++) {
            String word = seq.getExample(i)
                             .getFeatureByPrefix(fname)
                             .getSuffix();
            System.out.print(word + "/" + labeling.getLabel(i) + "/" + seq.getExample(i).getDiscreteLabel() + " ");
         }
         System.out.println();
      });
   }

   @Override
   protected void programLogic() throws Exception {
      switch(mode) {
         case TEST:
            test();
            break;
         case TRAIN:
            train();
            break;
         case SPLIT:
            split();
            break;
         case LABEL:
            label();
            break;
      }
   }

   /**
    * Required annotatable type [ ].
    *
    * @return the annotatable type [ ]
    */
   protected AnnotatableType[] required() {
      return new AnnotatableType[0];
   }

   /**
    * Split.
    *
    * @throws Exception the exception
    */
   protected void split() throws Exception {
      //TODO: UPDATE
      final FeatureExtractor<HString> featurizer = getFeaturizer();
      ExampleDataset all = getDataset(featurizer);
      all = all.shuffle(new Random(56789));
      Split trainTest = all.split(0.80);
      PreprocessorList preprocessors = getPreprocessors();
      if(preprocessors != null && preprocessors.size() > 0) {
         SequenceLabeler labeler = getLearner();
         labeler.setSequenceValidator(getValidator());
         labeler.fit(trainTest.train);
         BIOTagger tagger = new BIOTagger(featurizer, annotationType, labeler);
         BIOEvaluation eval = new BIOEvaluation();
         eval.evaluate(tagger.labeler, trainTest.test);
         eval.output();
      }
   }

   /**
    * Test.
    *
    * @throws Exception the exception
    */
   protected void test() throws Exception {
      BIOTagger tagger = BIOTagger.read(model);
      ExampleDataset test = getDataset(tagger.featurizer);
      BIOEvaluation eval = new BIOEvaluation();
      eval.evaluate(tagger.labeler, test);
      eval.output();
   }

   /**
    * Train.
    *
    * @throws Exception the exception
    */
   protected void train() throws Exception {
      final FeatureExtractor<HString> featurizer = getFeaturizer();
      ExampleDataset train = getDataset(featurizer);
      SequenceLabeler labeler = getLearner();
      labeler.setSequenceValidator(getValidator());
      FitParameters<?> fitParameters = getFitParameters();
      if(fitParameters == null) {
         fitParameters = labeler.getFitParameters().set(Params.verbose, true);
      }
      labeler.fit(train, fitParameters);
      BIOTagger tagger = new BIOTagger(featurizer, annotationType, labeler);
      tagger.write(model);
   }

   /**
    * Valid tags set.
    *
    * @return the set
    */
   protected Set<String> validTags() {
      return Collections.emptySet();
   }

}// END OF BIOTrainer
