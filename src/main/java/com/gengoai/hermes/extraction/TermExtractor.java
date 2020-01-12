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

import com.gengoai.annotation.JsonHandler;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.ml.feature.ValueCalculator;
import com.gengoai.json.JsonEntry;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author David B. Bracewell
 */
@JsonHandler(value = TermExtractor.Marshaller.class)
public class TermExtractor extends MultiPhaseExtractor<TermExtractor> {
   private static final long serialVersionUID = 1L;

   public static TermExtractor termExtractor(@NonNull Consumer<Builder> consumer) {
      Builder builder = builder();
      consumer.accept(builder);
      return builder.build();
   }

   protected TermExtractor(AnnotationType[] annotationTypes,
                           LyreExpression filter,
                           String prefix,
                           LyreExpression toString,
                           LyreExpression trim,
                           ValueCalculator valueCalculator) {
      super(annotationTypes, filter, prefix, toString, trim, valueCalculator);
   }

   public static Builder builder() {
      return new Builder();
   }

   @Override
   protected Stream<HString> createStream(HString hString) {
      return hString.interleaved(getAnnotationTypes()).stream().map(Cast::as);
   }

   @Override
   public TermExtractor.Builder toBuilder() {
      return builder().fromExtractor(this);
   }

   public static class Builder extends MultiPhaseExtractorBuilder<TermExtractor, Builder> {

      @Override
      public TermExtractor build() {
         return new TermExtractor(annotationTypes, filter, prefix, toString, trim, valueCalculator);
      }
   }

   /**
    * Marshaller for reading/writing LyreExpressions to and from json
    */
   public static class Marshaller extends com.gengoai.json.JsonMarshaller<TermExtractor> {

      @Override
      protected TermExtractor deserialize(JsonEntry entry, Type type) {
         return builder().fromJson(entry).build();
      }

      @Override
      protected JsonEntry serialize(TermExtractor n, Type type) {
         return n.toJson();
      }
   }

}//END OF TermExtractor
