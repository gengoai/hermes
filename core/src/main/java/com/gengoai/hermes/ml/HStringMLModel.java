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
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.evaluation.Evaluation;
import com.gengoai.apollo.ml.evaluation.PerInstanceEvaluation;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.collection.Iterables;
import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.DocumentCollection;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * The interface H string ml model.
 */
public interface HStringMLModel extends Model, SerializableFunction<HString, HString> {

   /**
    * Delegate model.
    *
    * @return the model
    */
   Model delegate();

   /**
    * Estimate.
    *
    * @param documentCollection the document collection
    */
   default void estimate(@NonNull DocumentCollection documentCollection) {
      delegate().estimate(getDataGenerator().generate(documentCollection.parallelStream()));
   }

   @Override
   default void estimate(@NonNull DataSet dataset) {
      delegate().estimate(dataset);
      LocalDateTime now = LocalDateTime.now();
      setVersion(now.format(DateTimeFormatter.ofPattern("yyyy_MM_dd")));
   }

   /**
    * Gets data generator.
    *
    * @return the data generator
    */
   HStringDataSetGenerator getDataGenerator();

   default Evaluation getEvaluator() {
      return new PerInstanceEvaluation(getOutput());
   }

   @Override
   default FitParameters<?> getFitParameters() {
      return delegate().getFitParameters();
   }

   @Override
   default Set<String> getInputs() {
      return delegate().getInputs();
   }

   @Override
   default LabelType getLabelType(@NonNull String name) {
      return delegate().getLabelType(name);
   }

   default String getOutput() {
      return Iterables.getFirst(getOutputs()).orElseThrow(() -> new IllegalStateException("No Output is defined"));
   }

   @Override
   default Set<String> getOutputs() {
      return delegate().getOutputs();
   }

   /**
    * Gets version.
    *
    * @return the version
    */
   String getVersion();

   /**
    * Sets version.
    *
    * @param version the version
    */
   void setVersion(String version);

   default HString transform(@NonNull HString hString) {
      return apply(hString);
   }

   @Override
   default DataSet transform(@NonNull DataSet dataset) {
      return delegate().transform(dataset);
   }

   @Override
   default Datum transform(@NonNull Datum datum) {
      return delegate().transform(datum);
   }

}//END OF HStringMLModel
