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

package com.gengoai.hermes.extraction.lyre;

import com.gengoai.Validation;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.apollo.ml.Feature;
import com.gengoai.collection.Lists;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.FeaturizingExtractor;
import com.gengoai.hermes.lexicon.WordList;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.json.JsonEntry;
import com.gengoai.math.Math2;
import com.gengoai.parsing.Expression;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.gengoai.hermes.HString.toHString;
import static com.gengoai.hermes.extraction.lyre.LyreExpressionType.*;
import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * The type Lyre expression.
 *
 * @author David B. Bracewell
 */
@JsonHandler(value = LyreExpression.Marshaller.class)
public final class LyreExpression extends FeaturizingExtractor implements Expression,
                                                                          SerializableFunction<HString, String>,
                                                                          SerializablePredicate<HString> {
   private final SerializableFunction<Object, Object> function;
   private final String pattern;
   private final LyreExpressionType type;

   /**
    * Instantiates a new Lyre expression.
    *
    * @param pattern  the pattern
    * @param type     the type
    * @param function the function
    */
   LyreExpression(String pattern, LyreExpressionType type, SerializableFunction<Object, Object> function) {
      this.pattern = pattern;
      this.type = type;
      this.function = function;
   }

   @Override
   public String apply(HString hString) {
      return applyAsString(hString);
   }

   @Override
   public Extraction extract(@NonNull HString hString) {
      Validation.checkArgument(isInstance(HSTRING, STRING, COUNTER, FEATURE),
                               "Invalid Expression for Extraction: HSTRING, STRING, COUNTER, or FEATURE required, but found " + getType());
      if (isInstance(COUNTER, FEATURE)) {
         return Extraction.fromCounter(count(hString));
      }
      if (isInstance(HSTRING)) {
         return Extraction.fromHStringList(applyAsList(hString, HString.class));
      }
      return Extraction.fromStringList(applyAsList(hString, String.class));
   }

   /**
    * Applies this expression to the given object converting it to a double
    *
    * @param hString the object to apply the expression on
    * @return the double value
    */
   public double applyAsDouble(HString hString) {
      return this.applyAsDouble((Object) hString);
   }

   /**
    * Applies this expression to the given object converting it to a double
    *
    * @param object the object to apply the expression on
    * @return the double value
    */
   public double applyAsDouble(Object object) {
      Object o = applyAsObject(object);
      if (o == null) {
         return Double.NaN;
      }
      if (o instanceof Number) {
         return Cast.<Number>as(o).doubleValue();
      }
      if (o instanceof CharSequence) {
         Double d = Math2.tryParseDouble(o.toString());
         return d == null ? Double.NaN : d;
      }
      if (o instanceof Boolean) {
         return Cast.<Boolean>as(o) ? 1.0 : 0.0;
      }
      return Double.NaN;
   }

   @Override
   public List<Feature> applyAsFeatures(HString hString) {
      if (isInstance(LyreExpressionType.FEATURE)) {
         return Cast.as(applyAsList(hString));
      } else if (isInstance(COUNTER)) {
         return Cast.<Counter<?>>as(applyAsObject(hString))
            .entries()
            .stream()
            .map(e -> Feature.realFeature(e.getKey().toString(), e.getValue()))
            .collect(Collectors.toList());
      }
      List<Object> list = applyAsList(hString);
      if (list == null || list.isEmpty()) {
         return Collections.emptyList();
      }
      return list.stream()
                 .map(o -> Feature.booleanFeature(o.toString()))
                 .collect(Collectors.toList());
   }

   /**
    * Applies this expression to the given HString converting the result into an HString
    *
    * @param string the HString to apply the expression against
    * @return the resulting HString
    */
   public HString applyAsHString(HString string) {
      return toHString(applyAsObject(string));
   }

   /**
    * Applies the given lambda against the given HString returning a list of of the given element type.
    *
    * @param <T>         the list element parameter
    * @param object      the object to apply the lambda against
    * @param elementType the type information for the list elements
    * @return the list of elements of the given type or null if the generated value is not convertible to a list of the
    * given type
    */
   public <T> List<T> applyAsList(Object object, Class<T> elementType) {
      List<Object> list = applyAsList(object);
      if (list.isEmpty()) {
         return Cast.as(list);
      } else if (elementType.isInstance(list.get(0))) {
         return Cast.cast(list);
      } else if (elementType == HString.class) {
         return Cast.cast(Lists.transform(list, HString::toHString));
      }
      return Converter.convertSilently(list, parameterizedType(List.class, elementType));
   }

   /**
    * Applies the given lambda against the given HString returning a list
    *
    * @param object the object to apply the lambda against
    * @return the list of objects or null if not convertible
    */
   public List<Object> applyAsList(Object object) {
      Object obj = applyAsObject(object);
      if (obj == null) {
         return Collections.emptyList();
      } else if (obj instanceof List) {
         return Cast.as(obj);
      }
      return Collections.singletonList(obj);
   }

   /**
    * Applies this expression to given object.
    *
    * @param object the object to apply the expression on
    * @return the result of the expression evaluation
    */
   public Object applyAsObject(Object object) {
      return function.apply(object);
   }

   /**
    * Applies this expression to given object return a String value.
    *
    * @param object the object to apply the expression on
    * @return the result of the expression evaluation as a String
    */
   public String applyAsString(Object object) {
      Object o = applyAsObject(object);
      return o == null ? null : o.toString();
   }


   /**
    * Count counter.
    *
    * @param hString the h string
    * @return the counter
    */
   public Counter<String> count(@NonNull HString hString) {
      Counter<String> cntr;
      if (getType().isInstance(COUNTER)) {
         cntr = Cast.<Counter<?>>as(applyAsObject(hString)).mapKeys(Object::toString);
      } else if (getType().isInstance(LyreExpressionType.FEATURE)) {
         cntr = Counters.newCounter();
         for (Feature feature : applyAsList(hString, Feature.class)) {
            cntr.set(feature.getName(), feature.getValue());
         }
      } else {
         cntr = Counters.newCounter(applyAsList(hString, String.class));
      }
      return cntr.filterByKey(Strings::isNotNullOrBlank);
   }

   /**
    * Gets the Lyre Pattern that can generate this expression
    *
    * @return the Lyre pattern
    */
   public String getPattern() {
      return toString();
   }

   @Override
   public LyreExpressionType getType() {
      return type;
   }

   @Override
   public boolean test(HString hString) {
      return testObject(hString);
   }

   /**
    * Tests the given object against this LyreExpression
    *
    * @param object the object
    * @return boolean based on expression evaluation
    */
   public boolean testObject(Object object) {
      Object o = applyAsObject(object);
      if (o == null) {
         return false;
      } else if (o instanceof Boolean) {
         return Cast.as(o);
      } else if (o instanceof Collection) {
         return Cast.<Collection<?>>as(o).size() > 0;
      } else if (o instanceof CharSequence) {
         return Strings.isNotNullOrBlank(o.toString());
      } else if (o instanceof WordList && object instanceof HString) {
         return Cast.<WordList>as(o).contains(Cast.<HString>as(object));
      } else if (o instanceof WordList && object instanceof CharSequence) {
         return Cast.<WordList>as(o).contains(object.toString());
      } else if (o instanceof Number) {
         return Double.isFinite(Cast.<Number>as(o).doubleValue());
      } else if (o instanceof PartOfSpeech) {
         return Cast.<PartOfSpeech>as(o) != PartOfSpeech.ANY;
      }
      return true;
   }

   @Override
   public String toString() {
      return pattern;
   }

   /**
    * Marshaller for reading/writing LyreExpressions to and from json
    */
   public static class Marshaller extends com.gengoai.json.JsonMarshaller<LyreExpression> {

      @Override
      protected LyreExpression deserialize(JsonEntry entry, Type type) {
         if (entry.isArray() && entry.size() == 1) {
            return Lyre.parse(entry.getAsArray().get(0).getAsString());
         }
         return Lyre.parse(entry.getAsString());
      }

      @Override
      protected JsonEntry serialize(LyreExpression lyreExpression, Type type) {
         return JsonEntry.from(lyreExpression.toString());
      }
   }
}//END OF LyreExpression
