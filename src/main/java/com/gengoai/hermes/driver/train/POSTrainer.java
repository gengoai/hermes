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

package com.gengoai.hermes.driver.train;

import com.gengoai.Language;
import com.gengoai.Lazy;
import com.gengoai.apollo.ml.*;
import com.gengoai.apollo.ml.data.DatasetType;
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.ml.preprocess.MinCountTransform;
import com.gengoai.apollo.ml.preprocess.PreprocessorList;
import com.gengoai.apollo.ml.sequence.GreedyAvgPerceptron;
import com.gengoai.apollo.ml.sequence.PerInstanceEvaluation;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.application.CommandLineApplication;
import com.gengoai.application.Option;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.en.ENPOSValidator;
import com.gengoai.hermes.ml.Mode;
import com.gengoai.hermes.ml.POSTagger;
import com.gengoai.hermes.ml.feature.AffixFeaturizer;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;

import java.util.Collections;

import static com.gengoai.apollo.ml.Featurizer.booleanFeaturizer;
import static com.gengoai.apollo.ml.Featurizer.predicateFeaturizer;

/**
 * The type Pos trainer.
 *
 * @author David B. Bracewell
 */
public class POSTrainer extends CommandLineApplication {
   private static final long serialVersionUID = 1L;

   /**
    * The Corpus.
    */
   @Option(description = "Location of the corpus to process", required = true)
   Resource corpus;
   /**
    * The Corpus format.
    */
   @Option(name = "format", description = "Format of the corpus", defaultValue = "JSON_OPL")
   String corpusFormat;
   /**
    * The Model.
    */
   @Option(description = "Location to save model", required = true)
   Resource model;
   /**
    * The Min feature count.
    */
   @Option(description = "Minimum count for a feature to be kept", defaultValue = "5")
   int minFeatureCount;
   /**
    * The Mode.
    */
   @Option(description = "TEST or TRAIN", defaultValue = "TRAIN")
   Mode mode;
   /**
    * The Language.
    */
   @Option(description = "The language of the model to train", defaultValue = "ENGLISH")
   Language language;

   private volatile Lazy<FeatureExtractor<HString>> featurizer
         = new Lazy<>(() ->
                            Featurizer.chain(new AffixFeaturizer(3, 3),
                                             Features.LowerCaseWord,
                                             predicateFeaturizer("ALL_UPPERCASE", Strings::isUpperCase),
                                             predicateFeaturizer("START_UPPERCASE",
                                                                 (HString s) -> Character.isUpperCase(s.charAt(0))),
                                             predicateFeaturizer("IS_DIGIT", Features::isDigit),
                                             predicateFeaturizer("IS_PUNCTUATION", Strings::isPunctuation),
                                             booleanFeaturizer((HString a) -> {
                                                String norm = a.toLowerCase();
                                                if(norm.endsWith("es")) {
                                                   return Collections.singleton("ENDING_ES");
                                                } else if(norm.endsWith("s")) {
                                                   return Collections.singleton("ENDING_S");
                                                } else if(norm.endsWith("ed")) {
                                                   return Collections.singleton("ENDING_ED");
                                                } else if(norm.endsWith("ing")) {
                                                   return Collections.singleton("ENDING_ING");
                                                } else if(norm.endsWith("ly")) {
                                                   return Collections.singleton("ENDING_LY");
                                                }
                                                return Collections.emptyList();
                                             })).withContext("WORD[-1]",
                                                             "WORD[-1]|WORD[0]",
                                                             "~WORD[-2]",
                                                             "~WORD[-2]|WORD[-1]",
                                                             "WORD[+1]",
                                                             "WORD[0]|WORD[+1]",
                                                             "~WORD[+2]",
                                                             "~WORD[+1]|WORD[+2]"
                                                            )
   );


   /**
    * Instantiates a new Pos trainer.
    */
   public POSTrainer() {
      super("POSTrainer");
   }

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    */
   public static void main(String[] args) {
      new POSTrainer().run(args);
   }

   private SequenceLabeler getLearner() {
      PreprocessorList preprocessors = new PreprocessorList();
      if(minFeatureCount > 1) {
         preprocessors.add(new MinCountTransform(minFeatureCount));
      }
      return new GreedyAvgPerceptron(new ENPOSValidator(), preprocessors);
   }

   /**
    * Load dataset dataset.
    *
    * @return the dataset
    * @throws Exception the exception
    */
   protected ExampleDataset loadDataset() throws Exception {
      return Corpus.reader(corpusFormat)
                   .read(corpus)
                   .asSequenceDataset(dd -> {
                      dd.labelAttribute(Types.PART_OF_SPEECH);
                      dd.sequenceType(Types.SENTENCE);
                      dd.tokenType(Types.TOKEN);
                      dd.featureExtractor(featurizer.get());
                   }, DatasetType.InMemory);
   }

   @Override
   protected void programLogic() throws Exception {
      if(mode == Mode.TRAIN) {
         train();
      } else if(mode == Mode.TEST) {
         test();
      } else {
         Split trainTestSplits = loadDataset().shuffle().split(0.8);
         SequenceLabeler labeler = getLearner();
         labeler.fit(trainTestSplits.train);
         PerInstanceEvaluation evaluation = new PerInstanceEvaluation();
         evaluation.evaluate(labeler, trainTestSplits.test);
         evaluation.output(true);
      }
   }

   @Override
   public void setup() throws Exception {
   }

   /**
    * Test.
    *
    * @throws Exception the exception
    */
   protected void test() throws Exception {
      POSTagger tagger = POSTagger.read(model);
      ExampleDataset test = loadDataset();
      PerInstanceEvaluation evaluation = new PerInstanceEvaluation();
      evaluation.evaluate(tagger.getSequenceLabeler(), test);
      evaluation.output(true);
   }

   /**
    * Train.
    *
    * @throws Exception the exception
    */
   protected void train() throws Exception {
      ExampleDataset train = loadDataset();
      SequenceLabeler labeler = getLearner();
      FitParameters<?> parameters = labeler.getFitParameters();
      parameters.set(Params.Optimizable.maxIterations, 200);
      parameters.set(Params.verbose, true);
      parameters.set(Params.Optimizable.historySize, 3);
      parameters.set(Params.Optimizable.tolerance, 1e-4);
      labeler.fit(train, parameters);
      POSTagger tagger = new POSTagger(featurizer.get(), labeler);
      tagger.write(model);
   }

}// END OF POSTrainer
