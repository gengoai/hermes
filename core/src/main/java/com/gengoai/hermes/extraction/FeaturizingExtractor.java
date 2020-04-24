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
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.lyre.Lyre;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.json.JsonEntry;

import java.lang.reflect.Type;

/**
 * Combines an {@link Extractor} with an Apollo <code>Featurizer</code> allowing for the output of the extractor to be
 * directly used as features for machine learning.
 *
 * @author David B. Bracewell
 */
@JsonHandler(value = FeaturizingExtractor.Marshaller.class, isHierarchical = false)
public abstract class FeaturizingExtractor extends Featurizer<HString> implements Extractor {
   private static final long serialVersionUID = 1L;

   /**
    * Marshaller for reading/writing LyreExpressions to and from json
    */
   public static class Marshaller extends com.gengoai.json.JsonMarshaller<FeaturizingExtractor> {

      @Override
      protected LyreExpression deserialize(JsonEntry entry, Type type) {
         return Lyre.parse(entry.getAsString());
      }

      @Override
      protected JsonEntry serialize(FeaturizingExtractor lyreExpression, Type type) {
         throw new UnsupportedOperationException();
      }
   }

}//END OF Extractor
