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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.ml.feature.ValueCalculator;
import com.gengoai.stream.Streams;
import com.gengoai.tuple.Tuple;
import com.gengoai.tuple.Tuple0;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

/**
 * A {@link MultiPhaseExtractor} implementation that extracts n-grams over the desired annotation types. In addition to
 * the standard extraction methods, this extractor provides the {@link #extractStringTuples(HString)} method for
 * returning a list of String tuples of the extractions.
 *
 * @author David B. Bracewell
 */
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@JsonDeserialize(as = NGramExtractor.class)
public class NGramExtractor extends MultiPhaseExtractor {
   private static final long serialVersionUID = 1L;
   private int maxOrder;
   private int minOrder;

   /**
    * @return @return An builder initialized for bigrams
    */
   public static Builder bigrams() {
      return builder(2, 2);
   }

   /**
    * @return @return An builder initialized for unigrams
    */
   public static Builder builder() {
      return new Builder();
   }

   /**
    * Creates a builder initialized to extract n-grams of the given order.
    *
    * @param n the n-gram order
    * @return the builder
    */
   public static Builder builder(int n) {
      return new Builder().minOrder(n);
   }

   /**
    * Creates a builder initialized to extract n-grams ranging from the given minimum to the given maximum order.
    *
    * @param minOrder the minimum order
    * @param maxOrder the maximum order
    * @return the builder
    */
   public static Builder builder(int minOrder, int maxOrder) {
      return new Builder().minOrder(minOrder).maxOrder(maxOrder);
   }

   /**
    * @return An builder initialized for trigrams
    */
   public static Builder trigrams() {
      return builder(3, 3);
   }

   private NGramExtractor(int minOrder,
                          int maxOrder,
                          AnnotationType[] annotationTypes,
                          LyreExpression filter,
                          String prefix,
                          LyreExpression toString,
                          LyreExpression trim,
                          ValueCalculator valueCalculator) {
      super(annotationTypes, filter, prefix, toString, trim, valueCalculator);
      this.minOrder = minOrder;
      this.maxOrder = maxOrder;
   }

   @Override
   protected Stream<HString> createStream(HString hString) {
      return Streams.asStream(new NGramHStringIterator(hString.interleaved(getAnnotationTypes())));
   }

   /**
    * Extracts NGrams as a List of String tuples.
    *
    * @param hString the input text
    * @return the list of String tuples
    */
   public List<Tuple> extractStringTuples(@NonNull HString hString) {
      return tupleStream(hString).map(t -> t.mapValues(getToString()::applyAsString))
                                 .collect(Collectors.toList());
   }

   @Override
   public Builder toBuilder() {
      return builder().fromExtractor(this);
   }

   @Override
   public String toString() {
      return "NGramExtractor{" +
            "maxOrder=" + maxOrder +
            ", minOrder=" + minOrder +
            ", annotationTypes=" + Arrays.toString(getAnnotationTypes()) +
            ", toString=" + getToString() +
            ", filter=" + getFilter() +
            ", trim=" + getTrim() +
            ", valueCalculator=" + getValueCalculator() +
            '}';
   }

   private Stream<Tuple> tupleStream(HString string) {
      List<Annotation> annotations;
      if(getAnnotationTypes().length > 1) {
         annotations = string.interleaved(getAnnotationTypes());
      } else {
         annotations = string.annotations(getAnnotationTypes()[0]);
      }
      Stream<Tuple> stream = Streams.asStream(new NGramTupleIterator(annotations));
      if(getTrim() != null) {
         stream = stream.map(h -> {
            Tuple t = Tuple0.INSTANCE;
            for(Object o : h) {
               HString anno = Cast.as(o);
               if(!getTrim().test(anno)) {
                  t = t.appendRight(anno);
               }
            }
            return t;
         }).filter(t -> t.degree() > 0);
      }
      if(getFilter() != null) {
         Predicate<HString> p = getFilter().negate();
         stream = stream.filter(t -> {
            HString union = t.degree() == 1
                            ? t.get(0)
                            : HString.union(t.get(0), t.get(t.degree() - 1));
            return p.test(union);
         });
      }

      return stream;
   }

   /**
    * Builder Class for constructing {@link NGramExtractor}
    */
   public static class Builder extends MultiPhaseExtractorBuilder<NGramExtractor, Builder> {
      private int maxOrder = 1;
      private int minOrder = 1;

      @Override
      public NGramExtractor build() {
         Validation.checkArgument(minOrder > 0, "minOrder must be greater than or equal to 1");
         Validation.checkArgument(maxOrder >= minOrder, "maxOrder must be greater than or equal to minOrder");
         return new NGramExtractor(minOrder, maxOrder, annotationTypes, filter, prefix, toString, trim,
                                   valueCalculator);
      }

      @Override
      public Builder fromExtractor(@NonNull MultiPhaseExtractor extractor) {
         NGramExtractor nge = Cast.as(extractor);
         return super.fromExtractor(extractor)
                     .minOrder(nge.minOrder)
                     .maxOrder(nge.maxOrder);
      }

      /**
       * Sets the maximum n-gram order.
       *
       * @param maxOrder the max order
       * @return this builder
       */
      public Builder maxOrder(int maxOrder) {
         Validation.checkArgument(maxOrder > 0, "Max Order must be greater than 0");
         this.maxOrder = maxOrder;
         this.minOrder = Math.min(minOrder, maxOrder);
         return this;
      }

      /**
       * Sets the minimum n-gram order.
       *
       * @param minOrder the min order
       * @return this builder
       */
      public Builder minOrder(int minOrder) {
         Validation.checkArgument(minOrder > 0, "Min Order must be greater than 0");
         this.minOrder = minOrder;
         this.maxOrder = Math.max(minOrder, maxOrder);
         return this;
      }
   }

   private class NGramHStringIterator implements Iterator<HString> {
      private final List<Annotation> annotations;
      private final LinkedList<HString> buffer = new LinkedList<>();
      private int i = 0;

      private NGramHStringIterator(List<Annotation> annotations) {
         this.annotations = annotations;
         advance();
      }

      private boolean advance() {
         while(i < annotations.size() && buffer.isEmpty()) {
            for(int j = i + getMinOrder() - 1; j < annotations.size() && j < i + getMaxOrder(); j++) {
               HString union = annotations.get(i).union(annotations.get(j));
               if(!union.isEmpty()) {
                  buffer.add(union);
               }
            }
            i++;
         }
         return !buffer.isEmpty();
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public HString next() {
         if(!advance()) {
            throw new NoSuchElementException();
         }
         return buffer.removeFirst();
      }
   }

   private class NGramTupleIterator implements Iterator<Tuple> {
      private final List<Annotation> annotations;
      private final LinkedList<Tuple> buffer = new LinkedList<>();
      private int i = 0;

      private NGramTupleIterator(List<Annotation> annotations) {
         this.annotations = annotations;
         advance();
      }

      private Tuple add(Tuple tuple) {
         if(tuple.degree() >= getMinOrder() && tuple.degree() <= getMaxOrder()) {
            buffer.add(tuple.copy());
         }
         return tuple;
      }

      private boolean advance() {
         while(i < annotations.size() && buffer.isEmpty()) {
            Tuple tuple = add($(annotations.get(i)));
            for(int j = 1; j <= getMaxOrder() && j + i < annotations.size(); j++) {
               tuple = add(tuple.appendRight(annotations.get(j + i)));
            }
            i++;
         }
         return !buffer.isEmpty();
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public Tuple next() {
         if(!advance()) {
            throw new NoSuchElementException();
         }
         return buffer.removeFirst();
      }
   }
}//END OF NGramExtractor
