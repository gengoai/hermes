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

import com.gengoai.apollo.ml.model.Model;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.IOBLabelMaker;
import com.gengoai.hermes.ml.IOBTagger;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

public abstract class IOBTaggerTrainer extends SequenceTaggerTrainer<IOBTagger> {
   @Getter
   @NonNull
   protected final AnnotationType annotationType;
   @Getter
   @NonNull
   protected final AnnotationType trainingAnnotation;

   public IOBTaggerTrainer(@NonNull AnnotationType annotationType) {
      this(annotationType, annotationType);
   }

   public IOBTaggerTrainer(@NonNull AnnotationType annotationType, @NonNull AnnotationType trainingAnnotation) {
      this.annotationType = annotationType;
      this.trainingAnnotation = trainingAnnotation;
   }

   protected abstract void addInputs(HStringDataSetGenerator.Builder builder);

   @Override
   protected final IOBTagger createTagger(Model labeler, HStringDataSetGenerator featureExtractor) {
      LocalDateTime now = LocalDateTime.now();
      String version = now.format(DateTimeFormatter.ofPattern("YYYY_MM_DD"));
      return new IOBTagger(featureExtractor, annotationType, labeler, version);
   }

   protected final HStringDataSetGenerator getExampleGenerator() {
      HStringDataSetGenerator.Builder builder = HStringDataSetGenerator.builder(Types.SENTENCE)
                                                                       .defaultOutput(new IOBLabelMaker(trainingAnnotation,
                                                                                                        getValidTags()));
      addInputs(builder);
      return builder.build();
   }

   protected Set<String> getValidTags() {
      return Collections.emptySet();
   }

}//END OF IOBTaggerTrainer
