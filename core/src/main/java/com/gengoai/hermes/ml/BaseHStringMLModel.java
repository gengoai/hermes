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

import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.hermes.HString;
import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * The type Base h string ml model.
 */
public abstract class BaseHStringMLModel implements HStringMLModel {
   private static final long serialVersionUID = 1L;
   private final Model delegate;
   @Getter
   private final HStringDataSetGenerator dataGenerator;
   @Getter
   @Setter
   private String version = Strings.EMPTY;

   /**
    * Instantiates a new Base h string ml model.
    *
    * @param delegate      the delegate
    * @param dataGenerator the data set generator
    */
   public BaseHStringMLModel(@NonNull Model delegate,
                             @NonNull HStringDataSetGenerator dataGenerator) {
      this.delegate = delegate;
      this.dataGenerator = dataGenerator;
   }

   @Override
   public final HString apply(@NonNull HString hString) {
      onEstimate(hString, transform(dataGenerator.apply(hString)));
      return hString;
   }

   @Override
   public Model delegate() {
      return delegate;
   }

   /**
    * On estimate.
    *
    * @param hString the h string
    * @param datum   the datum
    */
   protected abstract void onEstimate(HString hString, Datum datum);

}//END OF BaseHStringMLModel
