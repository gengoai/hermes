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

package com.gengoai.hermes.morphology;

import com.gengoai.Copyable;
import com.gengoai.Validation;
import com.gengoai.collection.Iterators;
import com.gengoai.function.Unchecked;
import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;
import com.gengoai.tuple.IntPair;
import com.gengoai.tuple.Tuple2;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

/**
 * A set of {@link UniversalFeature} and their associated {@link UniversalFeatureValue}
 */
@EqualsAndHashCode
public class UniversalFeatureSet implements Iterable<Tuple2<UniversalFeature, UniversalFeatureValue>>, Copyable<UniversalFeatureSet> {
   private static final String VALUE_SEPARATOR = "|";
   private final String set;

   /**
    * Instantiates a new empty UniversalFeatureSet.
    */
   public UniversalFeatureSet() {
      this.set = Strings.EMPTY;
   }

   /**
    * Instantiates a new UniversalFeatureSet by taking the union of existing sets.
    *
    * @param collection the collection of UniversalFeatureSet to union together to create this set
    */
   public UniversalFeatureSet(@NonNull Collection<UniversalFeatureSet> collection) {
      this.set = collection.stream()
                           .flatMap(Streams::asStream)
                           .map(t -> makeString(t.v1, t.v2))
                           .distinct()
                           .sorted()
                           .collect(Collectors.joining(VALUE_SEPARATOR));
   }

   /**
    * Instantiates a new UniversalFeatureSet from a number of features and values.
    *
    * @param entries the tuple entries defining features and associated values
    */
   @SafeVarargs
   public UniversalFeatureSet(@NonNull Tuple2<UniversalFeature, UniversalFeatureValue>... entries) {
      for(Tuple2<UniversalFeature, UniversalFeatureValue> entry : entries) {
         validate(entry.v1, entry.v2);
      }
      this.set = Stream.of(entries)
                       .map(t -> makeString(t.v1, t.v2))
                       .sorted()
                       .distinct()
                       .collect(Collectors.joining(VALUE_SEPARATOR));
   }

   private UniversalFeatureSet(String set) {
      this.set = set;
   }

   private static String makeString(UniversalFeature feature, UniversalFeatureValue value) {
      return feature + "=" + value;
   }

   /**
    * Parses the given string to construct a {@link UniversalFeatureSet}
    *
    * @param string the string
    * @return the UniversalFeatureSet
    */
   public static UniversalFeatureSet parse(@NonNull String string) {
      if(Strings.isNullOrBlank(string)) {
         return new UniversalFeatureSet();
      }
      return new UniversalFeatureSet(Stream.of(string.split("\\|"))
                                           .map(Unchecked.function(kv -> {
                                              String[] pair = kv.split("=", 2);
                                              UniversalFeature f = UniversalFeature.parse(pair[0]);
                                              UniversalFeatureValue v = UniversalFeatureValue.parse(pair[1]);
                                              validate(f, v);
                                              return makeString(f, v);
                                           }))
                                           .sorted()
                                           .distinct()
                                           .collect(Collectors.joining(VALUE_SEPARATOR)));
   }

   private static void validate(UniversalFeature feature, UniversalFeatureValue value) {
      Validation.checkArgument(feature.isValidValue(value),
                               "'" + value + "' is an invalid value for '" + feature + "'");
   }

   /**
    * Checks if the given feature and value combination are in the set.
    *
    * @param feature the feature
    * @param value   the value
    * @return True in the set, False otherwise
    */
   public boolean contains(@NonNull UniversalFeature feature, @NonNull UniversalFeatureValue value) {
      return set.contains(makeString(feature, value));
   }

   /**
    * Checks if the given feature is in the set
    *
    * @param feature the feature
    * @return True in the set, False otherwise
    */
   public boolean containsFeature(@NonNull UniversalFeature feature) {
      return set.contains(feature + "=");
   }

   @Override
   public UniversalFeatureSet copy() {
      return new UniversalFeatureSet(set);
   }

   /**
    * Gets the first value of the given feature in the set.
    *
    * @param feature the feature
    * @return the value of the given feature or null
    */
   public UniversalFeatureValue get(@NonNull UniversalFeature feature) {
      Iterator<IntPair> itr = Strings.findIterator(set, feature + "=");
      if(itr.hasNext()) {
         return getValue(itr.next().v2);
      }
      return null;
   }

   /**
    * Gets all values associated with the given feature.
    *
    * @param feature the feature
    * @return all values for the given feature
    */
   public Set<UniversalFeatureValue> getAll(@NonNull UniversalFeature feature) {
      return Streams.asStream(Strings.findIterator(set, feature + "="))
                    .map(i -> getValue(i.v2))
                    .collect(Collectors.toSet());
   }

   private UniversalFeatureValue getValue(int vStart) {
      int end = set.indexOf('|', vStart);
      if(end <= 0) {
         end = set.length();
      }
      return UniversalFeatureValue.parse(set.substring(vStart, end));
   }

   @Override
   public Iterator<Tuple2<UniversalFeature, UniversalFeatureValue>> iterator() {
      return Iterators.transform(Stream.of(set.split("\\|")).iterator(),
                                 kv -> {
                                    String[] pair = kv.split("=", 2);
                                    return $(UniversalFeature.parse(pair[0]), UniversalFeatureValue.parse(pair[1]));
                                 });
   }

   @Override
   public String toString() {
      return set;
   }

}//END OF UniversalFeatureSet
