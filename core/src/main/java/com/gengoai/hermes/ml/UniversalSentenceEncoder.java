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

package com.gengoai.hermes.ml;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.TensorFlowModel;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.collection.Iterators;
import com.gengoai.collection.Maps;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import lombok.NonNull;
import org.tensorflow.Tensor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.gengoai.tuple.Tuples.$;

public class UniversalSentenceEncoder extends TensorFlowModel implements HStringMLModel, ContextualizedEmbedding {
   public static final int DIMENSION = 512;
   private static final long serialVersionUID = 1L;

   public UniversalSentenceEncoder() {
      super(Collections.singleton(Datum.DEFAULT_INPUT),
            Maps.linkedHashMapOf($(Datum.DEFAULT_OUTPUT, Datum.DEFAULT_OUTPUT)),
            Collections.emptyMap());
   }

   @Override
   public HString apply(@NonNull HString hString) {
      DataSet dataSet = getDataGenerator().generate(Collections.singleton(hString));
      Iterators.zip(hString.sentences().iterator(), processBatch(dataSet).iterator())
               .forEachRemaining(e -> {
                  Annotation sentence = e.getKey();
                  NDArray embeddings = e.getValue().getDefaultOutput().asNDArray();
                  sentence.put(Types.EMBEDDING, embeddings);
               });
      return hString;
   }

   @Override
   protected Map<String, Tensor<?>> createTensors(DataSet batch) {
      byte[][] input = new byte[(int) batch.size()][];
      List<Datum> datum = batch.collect();
      for (int i = 0; i < datum.size(); i++) {
         input[i] = datum.get(i)
                         .getDefaultInput()
                         .asVariable()
                         .getName()
                         .toLowerCase()
                         .replaceAll("\\p{Punct}+", " ")
                         .replaceAll("\\s+", " ")
                         .getBytes();
      }
      return Map.of(Datum.DEFAULT_INPUT, Tensor.create(input));
   }

   @Override
   protected Transformer createTransformer() {
      return new Transformer(Collections.emptyList());
   }

   @Override
   public Model delegate() {
      return this;
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .defaultInput(h -> Variable.binary(h.toString()))
                                    .build();
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if (name.equals(getOutput())) {
         return LabelType.NDArray;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model");
   }

   @Override
   public String getVersion() {
      return "https://tfhub.dev/google/universal-sentence-encoder-large/3";
   }

   @Override
   public void setVersion(String version) {
      throw new UnsupportedOperationException();
   }

}//END OF UniversalSentenceEncoder
