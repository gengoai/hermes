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

package com.gengoai.hermes.corpus;

import com.gengoai.apollo.ml.Example;
import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.LabeledSequence;
import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.*;
import com.gengoai.hermes.extraction.TermExtractor;
import com.gengoai.hermes.extraction.lyre.LyreDSL;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.stream.Stream;

/**
 * @author David B. Bracewell
 */
@Getter
@Setter
@Accessors(fluent = true)
public class SequenceGenerator implements ExampleGenerator {
   private SerializableFunction<HString, Object> labelGenerator;
   @NonNull
   private AnnotationType sequenceType = Types.SENTENCE;
   @NonNull
   private AnnotationType tokenType = Types.TOKEN;
   @NonNull
   private FeatureExtractor<HString> featureExtractor = TermExtractor.builder().build();


   public SequenceGenerator labelAttribute(@NonNull AttributeType<?> attributeType) {
      this.labelGenerator = LyreDSL.attribute(attributeType)::applyAsObject;
      return this;
   }

   @Override
   public Stream<Example> apply(@NonNull HString hString) {
      Stream<Annotation> sequences = hString.annotationStream(sequenceType);
      if (labelGenerator == null) {
         return sequences.map(s -> featureExtractor.extractExample(s.annotations(tokenType)));
      }
      return sequences.map(
         s -> featureExtractor.extractExample(new LabeledSequence<>(s.annotationStream(tokenType), labelGenerator)));
   }
}//END OF SequenceGenerator
