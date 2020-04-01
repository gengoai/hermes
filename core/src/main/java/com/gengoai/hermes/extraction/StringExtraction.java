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

import com.gengoai.collection.Iterators;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ml.feature.ValueCalculator;
import lombok.NonNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author David B. Bracewell
 */
class StringExtraction implements Extraction {
   private static final long serialVersionUID = 1L;
   @NonNull
   private final List<String> list;
   private final ValueCalculator calculator;


   public StringExtraction(@NonNull List<String> list, ValueCalculator calculator) {
      this.list = Collections.unmodifiableList(list);
      this.calculator = calculator;
   }

   @Override
   public Iterator<HString> iterator() {
      return Iterators.transform(list.iterator(), HString::toHString);
   }

   @Override
   public int size() {
      return list.size();
   }

   @Override
   public Iterable<String> string() {
      return list;
   }

   @Override
   public Counter<String> count() {
      Counter<String> counter = Counters.newCounter(string());
      if (calculator != null) {
         counter = calculator.adjust(counter);
      }
      return counter;
   }

}//END OF StringExtraction
