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

package com.gengoai.hermes.en;

import com.gengoai.apollo.ml.DataSetType;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.feature.FeatureExtractor;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.model.PipelineModel;
import com.gengoai.apollo.ml.model.sequence.GreedyAvgPerceptron;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.transform.MinCountFilter;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.POSTagger;
import com.gengoai.hermes.ml.feature.AffixFeaturizer;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.PennTreeBank;

import java.util.regex.Pattern;

import static com.gengoai.hermes.ml.feature.Features.LowerCaseWord;
import static com.gengoai.hermes.ml.feature.Features.WordClass;
import static com.gengoai.hermes.ml.feature.PredefinedFeatures.strictContext;

/**
 * Default English language Part-of-Speech Annotator that uses a combination of machine learning and post-ml corrective
 * rules.
 *
 * @author David B. Bracewell
 */
public class ENPOSTagger extends POSTagger {
   private static final long serialVersionUID = 1L;
   private static final Pattern ORDINAL = Pattern.compile("^\\d+(rd|th|st|nd)$", Pattern.CASE_INSENSITIVE);

   private static FeatureExtractor<HString> createFeatureExtractor() {
      return Featurizer.chain(new AffixFeaturizer(3, 3),
                              LowerCaseWord,
                              WordClass,
                              Features.punctuationType,
                              Features.IsTitleCase,
                              Features.IsAllCaps,
                              Features.IsTitleCase,
                              Features.IsDigit)
                       .withContext(
                             "LowerWord[-1]",
                             "~LowerWord[-2]",
                             "LowerWord[+1]",
                             "~LowerWord[+2]",
                             strictContext(WordClass, -1),
                             strictContext(WordClass, -2),
                             strictContext(LowerCaseWord, 1),
                             strictContext(LowerCaseWord, 2)
                                   );
   }

   /**
    * Instantiates a new ENPOSTagger.
    */
   public ENPOSTagger() {
      super(HStringDataSetGenerator.builder(Types.SENTENCE)
                                   .dataSetType(DataSetType.InMemory)
                                   .tokenSequence(Datum.DEFAULT_INPUT, createFeatureExtractor())
                                   .tokenSequence(Datum.DEFAULT_OUTPUT, h -> Variable.binary(h.pos().name()))
                                   .build(),
            PipelineModel.builder()
                         .defaultInput(new MinCountFilter(5))
                         .build(new GreedyAvgPerceptron(parameters -> {
                            parameters.set(Params.Optimizable.maxIterations, 50);
                            parameters.set(Params.verbose, true);
                            parameters.set(Params.Optimizable.historySize, 3);
                            parameters.set(Params.Optimizable.tolerance, 1e-4);
                            parameters.validator.set(new ENPOSValidator());
                         })));
   }

   @Override
   protected void onEstimate(HString sentence, Datum datum) {
      Sequence<?> result = datum.get(getOutput()).asSequence();
      for(int i = 0; i < sentence.tokenLength(); i++) {
         Annotation token = sentence.tokenAt(i);
         if((token.contentEqualsIgnoreCase("'s") || token.contentEqualsIgnoreCase("s'")) &&
               token.previous().pos().isNoun()) {
            token.put(Types.PART_OF_SPEECH, PennTreeBank.POS);
         } else if(ORDINAL.matcher(token).matches()) {
            token.put(Types.PART_OF_SPEECH, PennTreeBank.JJ);
         } else if(sentence.tokenAt(i - 1).pos().isPronoun()
               && token.contentEqualsIgnoreCase("like")
         ) {
            token.put(Types.PART_OF_SPEECH, PennTreeBank.VB);
         } else if(sentence.tokenAt(i - 1).pos().isVerb() &&
               sentence.tokenAt(i + 1).contentEqualsIgnoreCase("to") &&
               token.toLowerCase().endsWith("ing")) {
            //Common error of MODAL + GERUND (where GERUND form is commonly a noun) + to => VBG
            token.put(Types.PART_OF_SPEECH, PennTreeBank.VBG);
         } else {
            token.put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(result.get(i).asVariable().getName()));
         }

         //Morphology
         String lower = token.toLowerCase();

      }
   }

}// END OF POSTagger
