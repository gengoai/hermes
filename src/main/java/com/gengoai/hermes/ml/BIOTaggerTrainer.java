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

package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ml.trainer.SequenceTaggerTrainer;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

public abstract class BIOTaggerTrainer extends SequenceTaggerTrainer<BIOTagger> {
   @Getter
   @NonNull
   protected final AnnotationType annotationType;
   @Getter
   @NonNull
   protected final AnnotationType trainingAnnotation;

   public BIOTaggerTrainer(@NonNull AnnotationType annotationType) {
      this(annotationType, annotationType);
   }

   public BIOTaggerTrainer(@NonNull AnnotationType annotationType, @NonNull AnnotationType trainingAnnotation) {
      this.annotationType = annotationType;
      this.trainingAnnotation = trainingAnnotation;
   }

   @Override
   protected final BIOTagger createTagger(SequenceLabeler labeler, FeatureExtractor<HString> featureExtractor) {
      return new BIOTagger(featureExtractor, annotationType, labeler);
   }

   @Override
   protected SequenceGenerator getSequenceGenerator() {
      return new SequenceGenerator()
            .labelGenerator(new BIOLabelMaker(trainingAnnotation, getValidTags()))
            .featureExtractor(createFeatureExtractor());
   }

   protected Set<String> getValidTags() {
      return Collections.emptySet();
   }

}//END OF BIOTaggerTrainer
