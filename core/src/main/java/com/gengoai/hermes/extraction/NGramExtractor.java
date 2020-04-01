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

import com.gengoai.Validation;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.stream.Streams;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.ml.feature.ValueCalculator;
import com.gengoai.json.JsonEntry;
import com.gengoai.tuple.Tuple;
import com.gengoai.tuple.Tuple0;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
@Getter
@JsonHandler(value = NGramExtractor.Marshaller.class)
public class NGramExtractor extends MultiPhaseExtractor<NGramExtractor> {
   private static final long serialVersionUID = 1L;
   private int maxOrder = 1;
   private int minOrder = 1;

   public NGramExtractor(int minOrder,
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

   public static NGramExtractor bigrams(Consumer<Builder> consumer) {
      return nGramExtractor(builder(2), consumer);
   }

   public static Builder bigrams() {
      return builder(2, 2);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static Builder builder(int n) {
      return new Builder().minOrder(n);
   }

   public static Builder builder(int minOrder, int maxOrder) {
      return new Builder().minOrder(minOrder).maxOrder(maxOrder);
   }

   public static NGramExtractor nGramExtractor(int n, Consumer<Builder> consumer) {
      return nGramExtractor(builder(n), consumer);
   }

   public static NGramExtractor nGramExtractor(int minOrder, int maxOrder, Consumer<Builder> consumer) {
      return nGramExtractor(builder(minOrder, maxOrder), consumer);
   }

   public static NGramExtractor nGramExtractor(Consumer<Builder> consumer) {
      return nGramExtractor(builder(), consumer);
   }

   private static NGramExtractor nGramExtractor(Builder builder, @NonNull Consumer<Builder> consumer) {
      consumer.accept(builder);
      return builder.build();
   }

   public static NGramExtractor trigrams(Consumer<Builder> consumer) {
      return nGramExtractor(builder(3), consumer);
   }

   public static Builder trigrams() {
      return builder(3, 3);
   }

   @Override
   protected Stream<HString> createStream(HString hString) {
      return Streams.asStream(new NGramHStringIterator(hString.interleaved(getAnnotationTypes())));
   }

   public List<Tuple> extractStringTuples(@NonNull HString hString) {
      return tupleStream(hString).map(t -> t.mapValues(getToString()::applyAsString))
                                 .collect(Collectors.toList());
   }

   @Override
   public Builder toBuilder() {
      return builder().fromExtractor(this);
   }

   @Override
   public JsonEntry toJson() {
      return super.toJson()
                  .addProperty("minOrder", minOrder)
                  .addProperty("maxOrder", maxOrder);
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
      if (getAnnotationTypes().length > 1) {
         annotations = string.interleaved(getAnnotationTypes());
      } else {
         annotations = string.annotations(getAnnotationTypes()[0]);
      }
      Stream<Tuple> stream = Streams.asStream(new NGramTupleIterator(annotations));
      if (getTrim() != null) {
         stream = stream.map(h -> {
            Tuple t = Tuple0.INSTANCE;
            for (Object o : h) {
               HString anno = Cast.as(o);
               if (!getTrim().test(anno)) {
                  t = t.appendRight(anno);
               }
            }
            return t;
         }).filter(t -> t.degree() > 0);
      }
      if (getFilter() != null) {
         Predicate<HString> p = getFilter().negate();
         stream = stream.filter(t -> {
            HString union = t.degree() == 1 ? t.get(0) : HString.union(t.get(0), t.get(t.degree() - 1));
            return p.test(union);
         });
      }

      return stream;
   }

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
      public Builder fromExtractor(@NonNull MultiPhaseExtractor<NGramExtractor> extractor) {
         NGramExtractor nge = Cast.as(extractor);
         return super.fromExtractor(extractor)
                     .minOrder(nge.minOrder)
                     .maxOrder(nge.maxOrder);
      }

      @Override
      public Builder fromJson(@NonNull JsonEntry entry) {
         return super.fromJson(entry)
                     .minOrder(entry.getIntProperty("minOrder"))
                     .maxOrder(entry.getIntProperty("maxOrder"));
      }

      public Builder maxOrder(int maxOrder) {
         Validation.checkArgument(maxOrder > 0, "Max Order must be greater than 0");
         this.maxOrder = maxOrder;
         this.minOrder = Math.min(minOrder, maxOrder);
         return this;
      }

      public Builder minOrder(int minOrder) {
         Validation.checkArgument(minOrder > 0, "Min Order must be greater than 0");
         this.minOrder = minOrder;
         this.maxOrder = Math.max(minOrder, maxOrder);
         return this;
      }
   }

   /**
    * Marshaller for reading/writing LyreExpressions to and from json
    */
   public static class Marshaller extends com.gengoai.json.JsonMarshaller<NGramExtractor> {

      @Override
      protected NGramExtractor deserialize(JsonEntry entry, Type type) {
         return builder().fromJson(entry).build();
      }

      @Override
      protected JsonEntry serialize(NGramExtractor n, Type type) {
         return n.toJson();
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
         while (i < annotations.size() && buffer.isEmpty()) {
            for (int j = i + getMinOrder() - 1; j < annotations.size() && j < i + getMaxOrder(); j++) {
               HString union = annotations.get(i).union(annotations.get(j));
               if (!union.isEmpty()) {
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
         if (!advance()) {
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
         if (tuple.degree() >= getMinOrder() && tuple.degree() <= getMaxOrder()) {
            buffer.add(tuple.copy());
         }
         return tuple;
      }

      private boolean advance() {
         while (i < annotations.size() && buffer.isEmpty()) {
            Tuple tuple = add($(annotations.get(i)));
            for (int j = 1; j <= getMaxOrder() && j + i < annotations.size(); j++) {
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
         if (!advance()) {
            throw new NoSuchElementException();
         }
         return buffer.removeFirst();
      }
   }
}//END OF NGramExtractor
