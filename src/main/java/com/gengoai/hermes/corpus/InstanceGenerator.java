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

import com.gengoai.Validation;
import com.gengoai.apollo.ml.Example;
import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.LabeledDatum;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.TermExtractor;
import com.gengoai.hermes.extraction.lyre.LyreDSL;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.extraction.lyre.LyreExpressionType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author David B. Bracewell
 */
@Getter
@Setter
@Accessors(fluent = true)
public class InstanceGenerator implements ExampleGenerator {
   private static final long serialVersionUID = 1L;
   private LyreExpression labelGenerator;
   @NonNull
   private LyreExpression exampleExtractor = LyreDSL.$_;
   @NonNull
   private FeatureExtractor<HString> featureExtractor = TermExtractor.builder().build();

   public InstanceGenerator exampleExtractor(@NonNull LyreExpression exampleExtractor) {
      this.exampleExtractor = Validation.validate(exampleExtractor.isInstance(LyreExpressionType.HSTRING),
                                                  () -> new IllegalArgumentException(
                                                     "exampleExtractor expected HSTRING expression, but found "
                                                        + exampleExtractor.getType()),
                                                  exampleExtractor);
      return this;
   }

   public InstanceGenerator labelAttribute(@NonNull AttributeType<?> type) {
      this.labelGenerator = LyreDSL.attribute(type);
      return this;
   }

   @Override
   public Stream<Example> apply(HString hString) {
      List<HString> examples = exampleExtractor.applyAsList(hString, HString.class);
      if (labelGenerator == null) {
         return examples.stream().map(h -> featureExtractor.extractExample(h));
      }
      return examples.stream().map(h -> featureExtractor.extractExample(LabeledDatum.of(labelGenerator.apply(h), h)));
   }

}//END OF InstanceGenerator
