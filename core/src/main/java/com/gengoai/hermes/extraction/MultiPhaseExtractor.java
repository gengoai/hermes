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
import com.gengoai.apollo.ml.observation.Variable;
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
import lombok.EqualsAndHashCode;
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
 * <p>
 * A {@link FeaturizingExtractor} that breaks the extraction process into the follow parts:
 * <ol>
 *    <li>Extracts annotations of the given types.</li>
 *    <li>Trims the extractions, if a trim method is defined.</li>
 *    <li>Filters the extractions, if a trim method is defined.</li>
 * </ol>
 * </p>
 *
 * In addition, a <code>toString</code> method is provided to map the extracted HString into a String representation.
 * Additionally, a ValueCalculator that defines how the extractions are counted.
 *
 * @author David B. Bracewell
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class MultiPhaseExtractor extends FeaturizingExtractor implements Copyable<FeaturizingExtractor> {
   private static final long serialVersionUID = 1L;
   private final @NonNull AnnotationType[] annotationTypes;
   private final LyreExpression filter;
   private final String prefix;
   private final @NonNull LyreExpression toString;
   private final LyreExpression trim;
   private final @NonNull ValueCalculator valueCalculator;

   @Override
   public final List<Variable> applyAsFeatures(@NonNull HString input) {
      return extract(input).count()
                           .entries()
                           .stream()
                           .map(e -> Variable.real(getPrefix(), e.getKey(), e.getValue()))
                           .collect(Collectors.toList());
   }

   @Override
   public FeaturizingExtractor copy() {
      return Cast.as(Copyable.deepCopy(this));
   }

   /**
    * Creates a stream of extractions from the given input
    *
    * @param hString the input text
    * @return the stream of extractions
    */
   protected abstract Stream<HString> createStream(HString hString);

   @Override
   public Extraction extract(@NonNull HString hString) {
      return new HStringExtraction(stream(hString).collect(Collectors.toList()), toString, valueCalculator);
   }

   private Stream<HString> stream(HString hString) {
      Stream<HString> stream = createStream(hString);
      if(trim != null) {
         stream = stream.map(h -> h.trim(trim));
      }
      if(filter != null) {
         stream = stream.filter(filter);
      }
      return stream.filter(h -> !h.isEmpty());
   }

   /**
    * Converts the Extractor into a builder.
    *
    * @return the builder initialized with values from this extractor
    */
   public abstract MultiPhaseExtractorBuilder<?, ?> toBuilder();

   protected JsonEntry toJson() {
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

   protected static abstract class MultiPhaseExtractorBuilder<T extends MultiPhaseExtractor,
         V extends MultiPhaseExtractorBuilder<T, V>> {
      protected @NonNull AnnotationType[] annotationTypes = {Types.TOKEN};
      protected LyreExpression filter = null;
      protected String prefix = Strings.EMPTY;
      protected @NonNull LyreExpression toString = LyreDSL.string;
      protected LyreExpression trim = null;
      protected @NonNull ValueCalculator valueCalculator = ValueCalculator.Frequency;

      /**
       * The annotations to base the extraction on
       *
       * @param types the annotation types
       * @return this builder
       */
      public V annotations(List<AnnotationType> types) {
         if(types == null || types.size() == 0) {
            this.annotationTypes = Arrays2.arrayOf(Types.TOKEN);
         } else {
            this.annotationTypes = types.toArray(new AnnotationType[0]);
         }
         return Cast.as(this);
      }

      /**
       * The annotations to base the extraction on
       *
       * @param types the annotation types
       * @return this builder
       */
      public V annotations(AnnotationType... types) {
         if(types == null || types.length == 0) {
            this.annotationTypes = Arrays2.arrayOf(Types.TOKEN);
         } else {
            this.annotationTypes = types;
         }
         return Cast.as(this);
      }

      /**
       * @return the constructed Extractor from this builder
       */
      public abstract T build();

      /**
       * Sets the filter to use to eliminate annotations from the extraction
       *
       * @param expression the Lyre expression to use as the filter.
       * @return this builder
       */
      public V filter(LyreExpression expression) {
         this.filter = expression;
         return Cast.as(this);
      }

      /**
       * Sets the filter to use to eliminate annotations from the extraction
       *
       * @param expression the Lyre expression to use as the filter.
       * @return this builder
       */
      public V filter(String expression) {
         if(Strings.isNullOrBlank(expression)) {
            this.filter = null;
         } else {
            filter(Lyre.parse(expression));
         }
         return Cast.as(this);
      }

      /**
       * Copies the values from the given extractor to this builder
       *
       * @param extractor the extractor to copy from
       * @return this extractor
       */
      public V fromExtractor(@NonNull MultiPhaseExtractor extractor) {
         return toString(extractor.toString)
               .trim(extractor.trim)
               .filter(extractor.filter)
               .prefix(extractor.prefix)
               .annotations(extractor.annotationTypes)
               .valueCalculator(extractor.valueCalculator);
      }

      /**
       * Initializes this builder from the given JsonEntry.
       *
       * @param entry the json entry
       * @return this builder
       */
      public V fromJson(@NonNull JsonEntry entry) {
         return toString(entry.getStringProperty("toString", null))
               .prefix(entry.getStringProperty("prefix", null))
               .trim(entry.getStringProperty("trim", null))
               .filter(entry.getStringProperty("filter", null))
               .annotations(entry.getProperty("annotationTypes")
                                 .getAsArray(AnnotationType.class))
               .valueCalculator(entry.getProperty("valueCalculator", ValueCalculator.class));
      }

      /**
       * Set the filter to ignore stopwords.
       *
       * @return this builder
       */
      public V ignoreStopwords() {
         this.filter = LyreDSL.isContentWord;
         return Cast.as(this);
      }

      /**
       * Sets the prefix to use when building features.
       *
       * @param prefix the feature prefix
       * @return this builder
       */
      public V prefix(String prefix) {
         this.prefix = prefix;
         return Cast.as(this);
      }

      /**
       * Set the toString method to use the lemma form of the annotation.
       *
       * @return this builder
       */
      public V toLemma() {
         this.toString = LyreDSL.lemma;
         return Cast.as(this);
      }

      /**
       * Set the toString method to use the lowercase form of the annotation.
       *
       * @return this builder
       */
      public V toLowerCase() {
         this.toString = LyreDSL.lower;
         return Cast.as(this);
      }

      /**
       * Sets the method for mapping annotations to Strings.
       *
       * @param expression the Lyre expression to use for mapping.
       * @return this builder
       */
      public V toString(String expression) {
         if(Strings.isNullOrBlank(expression)) {
            this.toString = LyreDSL.string;
         } else {
            return toString(Lyre.parse(expression));
         }
         return Cast.as(this);
      }

      /**
       * Sets the method for mapping annotations to Strings.
       *
       * @param expression the Lyre expression to use for mapping.
       * @return this builder
       */
      public V toString(LyreExpression expression) {
         if(expression == null) {
            this.toString = LyreDSL.string;
         } else {
            checkArgument(toString.isInstance(STRING, OBJECT),
                          "Expecting a STRING OR OBJECT expression, but received a " + toString.getType());
            this.toString = expression;
         }
         return Cast.as(this);
      }

      /**
       * Sets the method for trimming annotations.
       *
       * @param expression the Lyre expression to use for trimming.
       * @return this builder
       */
      public V trim(String expression) {
         if(Strings.isNullOrBlank(expression)) {
            this.trim = null;
         } else {
            trim(Lyre.parse(expression));
         }
         return Cast.as(this);
      }

      /**
       * Sets the method for trimming annotations.
       *
       * @param expression the Lyre expression to use for trimming.
       * @return this builder
       */
      public V trim(LyreExpression expression) {
         this.trim = expression;
         return Cast.as(this);
      }

      /**
       * Sets the method for calculating the final values in counter-based extraction
       *
       * @param valueCalculator the value calculator
       * @return this builder
       */
      public V valueCalculator(ValueCalculator valueCalculator) {
         if(valueCalculator == null) {
            this.valueCalculator = ValueCalculator.Frequency;
         } else {
            this.valueCalculator = valueCalculator;
         }
         return Cast.as(this);
      }

   }

}//END OF MultiPhaseExtractor
