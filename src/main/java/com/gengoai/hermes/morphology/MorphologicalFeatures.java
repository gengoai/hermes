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

import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;
import com.gengoai.tuple.IntPair;
import com.gengoai.tuple.Tuple2;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode
public class MorphologicalFeatures {
   private static final Pattern FV_PATTERN = Pattern.compile("\\S+=\\S+");
   private String featureSetString = Strings.EMPTY;

   public MorphologicalFeatures() {

   }

   @SafeVarargs
   public MorphologicalFeatures(@NonNull Tuple2<Feature, Value>... entries) {
      this.featureSetString = Stream.of(entries)
                                    .map(t -> makeString(t.v1, t.v2))
                                    .sorted()
                                    .distinct()
                                    .collect(Collectors.joining(" "));
   }

   public static void main(String[] args) {
      MorphologicalFeatures f = parse("Degree=Cmp Aspect=Hab");
      System.out.println(f);
      f.remove(Feature.Aspect, Value.ASPECT_PERFECT);
      f.add(Feature.Aspect, Value.ASPECT_HABITUAL);
      f.add(Feature.Degree, Value.DEGREE_COMPARATIVE);
      System.out.println(f);
   }

   private static String makeString(Feature feature, Value value) {
      return feature.name() + "=" + value.getTag();
   }

   public static MorphologicalFeatures parse(@NonNull String string) {
      if(Strings.isNullOrBlank(string)) {
         return new MorphologicalFeatures();
      }
      for(Iterator<IntPair> itr = Strings.matchIterator(string, FV_PATTERN); itr.hasNext(); ) {
         IntPair ip = itr.next();
         String[] pair = string.substring(ip.v1, ip.v2).split("=", 2);
         Feature.valueOf(pair[0]);
         Value.valueOf(pair[1]);
      }
      MorphologicalFeatures f = new MorphologicalFeatures();
      f.featureSetString = string;
      return f;
   }

   public void add(@NonNull Feature feature, @NonNull Value value) {
      final String property = makeString(feature, value);
      if(!featureSetString.contains(property)) {
         featureSetString += " " + property;
         featureSetString = featureSetString.replaceAll("\\s+", " ").strip();
      }
   }

   public boolean contains(@NonNull Feature feature, @NonNull Value value) {
      return featureSetString.contains(makeString(feature, value));
   }

   public boolean containsFeature(@NonNull Feature feature) {
      return featureSetString.contains(feature.name() + "=");
   }

   public Optional<Value> get(@NonNull Feature feature) {
      Iterator<IntPair> itr = Strings.findIterator(featureSetString, feature.name() + "=");
      if(itr.hasNext()) {
         return Optional.of(getValue(itr.next().v2));
      }
      return Optional.empty();
   }

   public Set<Value> getAll(@NonNull Feature feature) {
      return Streams.asStream(Strings.findIterator(featureSetString, feature.name() + "="))
                    .map(i -> getValue(i.v2))
                    .collect(Collectors.toSet());
   }

   private Value getValue(int vStart) {
      int end = featureSetString.indexOf(' ', vStart);
      if(end <= 0) {
         end = featureSetString.length();
      }
      return Value.valueOf(featureSetString.substring(vStart, end));
   }

   public void remove(@NonNull Feature feature, @NonNull Value value) {
      featureSetString = featureSetString.replace(makeString(feature, value), "").replaceAll("\\s+", " ").strip();
   }

   @Override
   public String toString() {
      return featureSetString;
   }

}//END OF MorphologicalFeatures
