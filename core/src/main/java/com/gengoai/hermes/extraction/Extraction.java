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

package com.gengoai.hermes.extraction;

import com.gengoai.collection.counter.Counter;
import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ml.feature.ValueCalculator;
import lombok.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * The interface Extraction.
 *
 * @author David B. Bracewell
 */
public interface Extraction extends Serializable, Iterable<HString> {

   /**
    * Size int.
    *
    * @return the int
    */
   int size();

   /**
    * String iterable.
    *
    * @return the iterable
    */
   Iterable<String> string();

   /**
    * Applies this extractor on the given {@link HString} return a count of the string values..
    *
    * @return the counter
    */
   Counter<String> count();

   /**
    * From string list extraction.
    *
    * @param list the list
    * @return the extraction
    */
   static Extraction fromStringList(@NonNull List<String> list) {
      return new StringExtraction(list, null);
   }

   /**
    * From string list extraction.
    *
    * @param list       the list
    * @param calculator the calculator
    * @return the extraction
    */
   static Extraction fromStringList(@NonNull List<String> list, ValueCalculator calculator) {
      return new StringExtraction(list, calculator);
   }

   /**
    * From h string list extraction.
    *
    * @param list the list
    * @return the extraction
    */
   static Extraction fromHStringList(@NonNull List<HString> list) {
      return new HStringExtraction(list, HString::toString, null);
   }

   /**
    * From h string list extraction.
    *
    * @param list     the list
    * @param toString the to string
    * @return the extraction
    */
   static Extraction fromHStringList(@NonNull List<HString> list,
                                     @NonNull SerializableFunction<HString, String> toString) {
      return new HStringExtraction(list, toString, null);
   }

   /**
    * From h string list extraction.
    *
    * @param list       the list
    * @param toString   the to string
    * @param calculator the calculator
    * @return the extraction
    */
   static Extraction fromHStringList(@NonNull List<HString> list,
                                     @NonNull SerializableFunction<HString, String> toString,
                                     ValueCalculator calculator) {
      return new HStringExtraction(list, toString, calculator);
   }

   /**
    * From counter extraction.
    *
    * @param counter the counter
    * @return the extraction
    */
   static Extraction fromCounter(@NonNull Counter<String> counter) {
      return new CounterExtraction(counter);
   }

}//END OF Extraction
