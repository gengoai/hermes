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

package com.gengoai.hermes.ml.feature;

import com.gengoai.Validation;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.HString;
import lombok.Getter;
import lombok.NonNull;

import java.io.ObjectStreamException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Predefined features.
 */
public final class PredefinedFeatures {
   private static final Map<String, Featurizer<HString>> features = new ConcurrentHashMap<>();

   /**
    * Get featurizer.
    *
    * @param name the name
    * @return the featurizer
    */
   public static Featurizer<HString> get(String name) {
      if(features.containsKey(name)) {
         return features.get(name);
      }
      throw new IllegalArgumentException(String.format("%s is not a predefined feature.", name));
   }

   /**
    * Lenient context string.
    *
    * @param objects the objects
    * @return the string
    */
   public static String lenientContext(@NonNull Object... objects) {
      return strictContext(false, objects);
   }

   /**
    * Predefined feature predefined featurizer.
    *
    * @param name       the name
    * @param featurizer the featurizer
    * @return the predefined featurizer
    */
   public static PredefinedFeaturizer predefinedFeature(String name, @NonNull Featurizer<HString> featurizer) {
      Validation.notNullOrBlank(name, "The predefined feature name must not be null or blank");
      if(features.containsKey(name)) {
         throw new IllegalStateException(String.format("Attempting to redefine a predefined feature of name '%s'",
                                                       name));
      }
      features.put(name, featurizer);
      return new PredefinedFeaturizer(name, featurizer);
   }

   /**
    * Predefined predicate feature predefined featurizer.
    *
    * @param name       the name
    * @param featurizer the featurizer
    * @return the predefined featurizer
    */
   public static PredefinedFeaturizer predefinedPredicateFeature(String name,
                                                                 @NonNull SerializablePredicate<? super HString> featurizer) {
      return predefinedFeature(name, Featurizer.predicateFeaturizer(name, featurizer));
   }

   /**
    * Predefined value feature predefined featurizer.
    *
    * @param name       the name
    * @param featurizer the featurizer
    * @return the predefined featurizer
    */
   public static PredefinedFeaturizer predefinedValueFeature(String name,
                                                             @NonNull SerializableFunction<? super HString, String> featurizer) {
      return predefinedFeature(name, Featurizer.valueFeaturizer(name, featurizer));
   }


   /**
    * Strict context string.
    *
    * @param objects the objects
    * @return the string
    */
   public static String strictContext(@NonNull Object... objects) {
      return strictContext(true, objects);
   }

   /**
    * Strict context string.
    *
    * @param mustMatch the must match
    * @param objects   the objects
    * @return the string
    */
   public static String strictContext(boolean mustMatch, @NonNull Object... objects) {
      Validation.checkArgument(objects.length % 2 == 0, "Must have an even number of elements");
      StringBuilder sb = new StringBuilder();
      if(mustMatch) {
         sb.append("~");
      }
      for(int i = 0; i < objects.length; i += 2) {
         if(i > 0) {
            sb.append("|");
         }
         @NonNull PredefinedFeaturizer f = Cast.as(objects[i]);
         @NonNull Integer index = Cast.as(objects[i + 1]);
         sb.append(f.name)
           .append("[")
           .append(index)
           .append("]");
      }
      return sb.toString();
   }

   /**
    * Instantiates a new Predefined features.
    */
   public PredefinedFeatures() {
      throw new IllegalAccessError();
   }

   /**
    * The type Predefined featurizer.
    */
   @Getter
   public static class PredefinedFeaturizer extends Featurizer<HString> {
      private static final long serialVersionUID = 1L;
      private final String name;
      private final Featurizer<HString> featurizer;

      private PredefinedFeaturizer(String name, Featurizer<HString> featurizer) {
         this.name = name;
         this.featurizer = featurizer;
      }

      @Override
      public List<Variable> applyAsFeatures(HString input) {
         return featurizer.applyAsFeatures(input);
      }

      /**
       * Read resolve object.
       *
       * @return the object
       * @throws ObjectStreamException the object stream exception
       */
      protected Object readResolve() throws ObjectStreamException {
         return features.get(name);
      }

      @Override
      public String toString() {
         return name;
      }
   }//END OF PredefinedFeaturizer

}//END OF PredefinedFeatures
