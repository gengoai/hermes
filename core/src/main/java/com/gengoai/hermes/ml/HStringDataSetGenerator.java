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

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.DataSetGenerator;
import com.gengoai.apollo.ml.DataSetType;
import com.gengoai.apollo.ml.feature.ObservationExtractor;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * An extension to a DataSetGenerator that allows for the incoming documents to be broken up into multiple Datum based
 * on a given {@link AnnotationType}.
 * </p>
 *
 * @author David B. Bracewell
 */
public class HStringDataSetGenerator extends DataSetGenerator<HString> {
   private static final long serialVersionUID = 1L;
   private final AnnotationType datumAnnotationType;

   /**
    * Creates a builder which will build an HStringDataSetGenerator where the given {@link AnnotationType} will be the
    * basis for the Observation signals. For example, specifying a <code>SENTENCE</code> annotation would create a Datum
    * per sentence in the document.
    *
    * @param datumAnnotationType the annotation type to use for generating Datum.
    * @return the builder
    */
   public static Builder builder(@NonNull AnnotationType datumAnnotationType) {
      return new Builder(datumAnnotationType);
   }

   /**
    * Creates a builder which will build an HStringDataSetGenerator where there the generated datum represents the
    * entire {@link com.gengoai.hermes.Document}.
    *
    * @return the builder
    */
   public static Builder builder() {
      return new Builder();
   }

   private HStringDataSetGenerator(AnnotationType datumAnnotationType,
                                   @NonNull DataSetType dataSetType,
                                   @NonNull Collection<DataSetGenerator.GeneratorInfo<HString>> generators) {
      super(dataSetType, generators);
      this.datumAnnotationType = datumAnnotationType;
   }

   @Override
   public DataSet generate(@NonNull Collection<? extends HString> data) {
      if(datumAnnotationType != null) {
         return super.generate(data.parallelStream()
                                   .flatMap(d -> d.annotationStream(datumAnnotationType))
                                   .collect(Collectors.toList()));
      }
      return super.generate(data);
   }

   @Override
   public DataSet generate(@NonNull MStream<? extends HString> data) {
      if(datumAnnotationType != null) {
         return super.generate(data.parallel().flatMap(d -> d.annotationStream(datumAnnotationType)));
      }
      return super.generate(data);
   }

   /**
    * Builder Class for HStringDataSetGenerator
    */
   public static class Builder extends DataSetGenerator.Builder<HString> {
      private final AnnotationType datumAnnotationType;

      private Builder(AnnotationType datumAnnotationType) {
         this.datumAnnotationType = datumAnnotationType;
      }

      private Builder() {
         this.datumAnnotationType = null;
      }

      @Override
      public HStringDataSetGenerator build() {
         return new HStringDataSetGenerator(datumAnnotationType, dataSetType, generators);
      }

      @Override
      public Builder dataSetType(DataSetType dataSetType) {
         return Cast.as(super.dataSetType(dataSetType));
      }

      @Override
      public Builder defaultInput(@NonNull ObservationExtractor<? super HString> extractor) {
         return Cast.as(super.defaultInput(extractor));
      }

      @Override
      public Builder defaultInput(@NonNull ObservationExtractor<? super HString> extractor,
                                  @NonNull SerializableFunction<? super HString, List<? extends HString>> toSequence) {
         return Cast.as(super.defaultInput(extractor, toSequence));
      }

      @Override
      public Builder defaultOutput(@NonNull ObservationExtractor<? super HString> extractor) {
         return Cast.as(super.defaultOutput(extractor));
      }

      @Override
      public Builder defaultOutput(@NonNull ObservationExtractor<? super HString> extractor,
                                   @NonNull SerializableFunction<? super HString, List<? extends HString>> toSequence) {
         return Cast.as(super.defaultOutput(extractor, toSequence));
      }

      @Override
      public Builder source(@NonNull String name,
                            @NonNull ObservationExtractor<? super HString> extractor,
                            @NonNull SerializableFunction<? super HString, List<? extends HString>> toSequence) {
         return Cast.as(super.source(name, extractor, toSequence));
      }

      @Override
      public Builder source(@NonNull String name,
                            @NonNull ObservationExtractor<? super HString> extractor) {
         return Cast.as(super.source(name, extractor));
      }

      /**
       * Creates a Sequence Observation where the sequence is defined over token-based Observations.
       *
       * @param name      the name of the Observation
       * @param extractor the extractor to extract token-level Observations
       * @return the builder
       */
      public Builder tokenSequence(@NonNull String name,
                                   @NonNull ObservationExtractor<? super HString> extractor) {
         return source(name, extractor, HString::tokens);
      }

   }//END OF Builder

}//END OF HStringDataSetGenerator
