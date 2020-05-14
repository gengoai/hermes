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

import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.evaluation.Evaluation;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>
 * Creates annotations based on the IOB tag output of an underlying model.
 * </p>
 *
 * @author David B. Bracewell
 */
public class IOBTagger extends BaseHStringMLModel {
   private static final long serialVersionUID = 1L;
   /**
    * The Annotation type.
    */
   @Getter
   final AnnotationType annotationType;

   /**
    * Instantiates a new IOBTagger
    *
    * @param inputGenerator the generator to convert HString into input for the model
    * @param annotationType the type of annotation to add during tagging.
    * @param labeler        the model to use to perform the IOB tagging
    */
   public IOBTagger(@NonNull HStringDataSetGenerator inputGenerator,
                    @NonNull AnnotationType annotationType,
                    @NonNull Model labeler) {
      super(labeler, inputGenerator);
      this.annotationType = annotationType;
   }

   @Override
   public Evaluation getEvaluator() {
      return new CoNLLEvaluation(getOutput());
   }

   @Override
   protected void onEstimate(HString sentence, Datum datum) {
      IOB.decode(sentence, datum.get(getOutput()).asSequence(), annotationType);
   }

}// END OF IOBTagger
