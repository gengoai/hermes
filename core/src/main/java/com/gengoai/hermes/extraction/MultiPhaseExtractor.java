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

import com.gengoai.Copyable;
import com.gengoai.apollo.ml.Feature;
import com.gengoai.collection.Arrays2;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.extraction.lyre.Lyre;
import com.gengoai.hermes.extraction.lyre.LyreDSL;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.ml.feature.ValueCalculator;
import com.gengoai.json.JsonEntry;
import com.gengoai.string.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.Validation.checkArgument;
import static com.gengoai.hermes.extraction.lyre.LyreExpressionType.OBJECT;
import static com.gengoai.hermes.extraction.lyre.LyreExpressionType.STRING;

/**
 * @author David B. Bracewell
 */
@Getter
@AllArgsConstructor
public abstract class MultiPhaseExtractor<T> extends FeaturizingExtractor implements Copyable<T> {
   private static final long serialVersionUID = 1L;
   private final @NonNull AnnotationType[] annotationTypes;
   private final LyreExpression filter;
   private final String prefix;
   private final @NonNull LyreExpression toString;
   private final LyreExpression trim;
   private final @NonNull ValueCalculator valueCalculator;


   public AnnotationType[] getAnnotationTypes() {
      return Arrays.copyOf(annotationTypes, annotationTypes.length);
   }

   @Override
   public final List<Feature> applyAsFeatures(@NonNull HString input) {
      return extract(input).count()
                           .entries()
                           .stream()
                           .map(e -> Feature.realFeature(getPrefix(), e.getKey(), e.getValue()))
                           .collect(Collectors.toList());
   }

   @Override
   public T copy() {
      return Cast.as(Copyable.deepCopy(this));
   }

   protected abstract Stream<HString> createStream(HString hString);

   @Override
   public Extraction extract(@NonNull HString hString) {
      return new HStringExtraction(stream(hString).collect(Collectors.toList()), toString, valueCalculator);
   }

   protected final Stream<HString> stream(HString hString) {
      Stream<HString> stream = createStream(hString);
      if (trim != null) {
         stream = stream.map(h -> h.trim(trim));
      }
      if (filter != null) {
         stream = stream.filter(filter);
      }
      return stream.filter(h -> !h.isEmpty());
   }

   public abstract MultiPhaseExtractorBuilder toBuilder();

   public JsonEntry toJson() {
      return JsonEntry.object(getClass())
                      .addProperty("toString", getToString())
                      .addProperty("prefix", getPrefix())
                      .addProperty("trim", getTrim())
                      .addProperty("filter", getFilter())
                      .addProperty("annotationTypes", getAnnotationTypes())
                      .addProperty("valueCalculator", getValueCalculator());
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "{" +
         "annotationTypes=" + Arrays.toString(getAnnotationTypes()) +
         ", toString=" + getToString() +
         ", filter=" + getFilter() +
         ", trim=" + getTrim() +
         ", valueCalculator=" + getValueCalculator() +
         '}';
   }

   protected static abstract class MultiPhaseExtractorBuilder<T extends MultiPhaseExtractor<T>,
      V extends MultiPhaseExtractorBuilder<T, V>> {
      protected @NonNull AnnotationType[] annotationTypes = {Types.TOKEN};
      protected LyreExpression filter = null;
      protected String prefix = Strings.EMPTY;
      protected @NonNull LyreExpression toString = LyreDSL.string;
      protected LyreExpression trim = null;
      protected @NonNull ValueCalculator valueCalculator = ValueCalculator.Frequency;


      public V ignoreStopwords() {
         this.filter = LyreDSL.isContentWord;
         return Cast.as(this);
      }

      public V toLemma() {
         this.toString = LyreDSL.lemma;
         return Cast.as(this);
      }

      public V toLowerCase() {
         this.toString = LyreDSL.lower;
         return Cast.as(this);
      }

      public V annotations(List<AnnotationType> types) {
         if (types == null || types.size() == 0) {
            this.annotationTypes = Arrays2.arrayOf(Types.TOKEN);
         } else {
            this.annotationTypes = types.toArray(new AnnotationType[0]);
         }
         return Cast.as(this);
      }

      public V annotations(AnnotationType... types) {
         if (types == null || types.length == 0) {
            this.annotationTypes = Arrays2.arrayOf(Types.TOKEN);
         } else {
            this.annotationTypes = types;
         }
         return Cast.as(this);
      }

      public abstract T build();

      public V filter(LyreExpression expression) {
         this.filter = expression;
         return Cast.as(this);
      }

      public V filter(String expression) {
         if (Strings.isNullOrBlank(expression)) {
            this.filter = null;
         } else {
            filter(Lyre.parse(expression));
         }
         return Cast.as(this);
      }

      public V fromExtractor(@NonNull MultiPhaseExtractor<T> extractor) {
         return toString(extractor.toString)
            .trim(extractor.trim)
            .filter(extractor.filter)
            .prefix(extractor.prefix)
            .annotations(extractor.annotationTypes)
            .valueCalculator(extractor.valueCalculator);
      }

      public V fromJson(@NonNull JsonEntry entry) {
         return toString(entry.getStringProperty("toString", null))
            .prefix(entry.getStringProperty("prefix", null))
            .trim(entry.getStringProperty("trim", null))
            .filter(entry.getStringProperty("filter", null))
            .annotations(entry.getProperty("annotationTypes")
                              .getAsArray(AnnotationType.class))
            .valueCalculator(entry.getProperty("valueCalculator", ValueCalculator.class));
      }

      public V prefix(String prefix) {
         this.prefix = prefix;
         return Cast.as(this);
      }

      public V toString(String expression) {
         if (Strings.isNullOrBlank(expression)) {
            this.toString = LyreDSL.string;
         } else {
            return toString(Lyre.parse(expression));
         }
         return Cast.as(this);
      }

      public V toString(LyreExpression expression) {
         if (expression == null) {
            this.toString = LyreDSL.string;
         } else {
            checkArgument(toString.isInstance(STRING, OBJECT),
                          "Expecting a STRING OR OBJECT expression, but received a " + toString.getType());
            this.toString = expression;
         }
         return Cast.as(this);
      }

      public V trim(String expression) {
         if (Strings.isNullOrBlank(expression)) {
            this.trim = null;
         } else {
            trim(Lyre.parse(expression));
         }
         return Cast.as(this);
      }

      public V trim(LyreExpression expression) {
         this.trim = expression;
         return Cast.as(this);
      }

      public V valueCalculator(ValueCalculator valueCalculator) {
         if (valueCalculator == null) {
            this.valueCalculator = ValueCalculator.Frequency;
         } else {
            this.valueCalculator = valueCalculator;
         }
         return Cast.as(this);
      }

   }

}//END OF MultiPhaseExtractor
