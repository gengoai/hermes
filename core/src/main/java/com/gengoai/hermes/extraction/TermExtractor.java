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

import com.gengoai.conversion.Cast;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.ml.feature.ValueCalculator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

/**
 * Implementation of the {@link MultiPhaseExtractor} for extracting terms where a term is a single annotation (TOKEN by
 * default).
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class TermExtractor extends MultiPhaseExtractor {
   private static final long serialVersionUID = 1L;

   /**
    * @return A term extractor builder
    */
   public static Builder builder() {
      return new Builder();
   }

   private TermExtractor(AnnotationType[] annotationTypes,
                         LyreExpression filter,
                         String prefix,
                         LyreExpression toString,
                         LyreExpression trim,
                         ValueCalculator valueCalculator) {
      super(annotationTypes, filter, prefix, toString, trim, valueCalculator);
   }

   @Override
   protected Stream<HString> createStream(HString hString) {
      return hString.interleaved(getAnnotationTypes())
                    .stream()
                    .map(Cast::as);
   }

   @Override
   public TermExtractor.Builder toBuilder() {
      return builder().fromExtractor(this);
   }

   /**
    * Builder Class for constructing {@link TermExtractor}
    */
   public static class Builder extends MultiPhaseExtractorBuilder<TermExtractor, Builder> {

      @Override
      public TermExtractor build() {
         return new TermExtractor(annotationTypes, filter, prefix, toString, trim, valueCalculator);
      }
   }//END OF Builder

}//END OF TermExtractor
