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

package com.gengoai.hermes.format;

import com.gengoai.Validation;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Co nll processors.
 *
 * @author David B. Bracewell
 */
final class CoNLLProcessors {
   private static final Map<String, CoNLLColumnProcessor> processorMap;

   static {
      processorMap = new HashMap<>();
      for(CoNLLColumnProcessor processor : ServiceLoader.load(CoNLLColumnProcessor.class)) {
         processorMap.put(processor.getFieldName().toUpperCase(), processor);
      }
   }

   private CoNLLProcessors() {
      throw new IllegalAccessError();
   }

   /**
    * Get co nll column processor.
    *
    * @param fieldName the field name
    * @return the co nll column processor
    */
   static CoNLLColumnProcessor get(@NonNull String fieldName) {
      Validation.checkArgument(processorMap.containsKey(fieldName), fieldName + " is an unknown processing type.");
      return processorMap.get(fieldName.toUpperCase());
   }

   /**
    * Get list.
    *
    * @param fieldNames the field names
    * @return the list
    */
   static List<CoNLLColumnProcessor> get(@NonNull Collection<String> fieldNames) {
      return fieldNames.stream().map(CoNLLProcessors::get).collect(Collectors.toList());
   }

   /**
    * Get list.
    *
    * @param fieldNames the field names
    * @return the list
    */
   static List<CoNLLColumnProcessor> get(@NonNull String... fieldNames) {
      return Stream.of(fieldNames).map(CoNLLProcessors::get).collect(Collectors.toList());
   }

}//END OF CoNLLProcessors
