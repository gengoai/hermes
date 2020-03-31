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

import com.gengoai.apollo.ml.Example;
import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.HString;
import lombok.NonNull;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The interface Example generator.
 *
 * @author David B. Bracewell
 */
public interface ExampleGenerator extends SerializableFunction<HString, Stream<Example>> {

   /**
    * Instance instance generator.
    *
    * @return the instance generator
    */
   static InstanceGenerator instance() {
      return new InstanceGenerator();
   }

   /**
    * Instance instance generator.
    *
    * @param updater the updater
    * @return the instance generator
    */
   static InstanceGenerator instance(@NonNull Consumer<InstanceGenerator> updater) {
      InstanceGenerator ig = new InstanceGenerator();
      updater.accept(ig);
      return ig;
   }

   /**
    * Sequence sequence generator.
    *
    * @return the sequence generator
    */
   static SequenceGenerator sequence() {
      return new SequenceGenerator();
   }

   /**
    * Sequence sequence generator.
    *
    * @param updater the updater
    * @return the sequence generator
    */
   static SequenceGenerator sequence(@NonNull Consumer<SequenceGenerator> updater) {
      SequenceGenerator sg = new SequenceGenerator();
      updater.accept(sg);
      return sg;
   }

}//END OF ExampleGenerator
