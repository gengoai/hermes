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

import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.TensorFlowModel;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.string.Strings;
import lombok.NonNull;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class ElmoTokenEmbedding extends TensorFlowModel implements HStringMLModel {
   private static final long serialVersionUID = 1L;
   public static final int DIMENSION = 1024;

   public ElmoTokenEmbedding() {
      super(new Transformer(Collections.emptyList()),
            Collections.singleton(Datum.DEFAULT_INPUT),
            Collections.singleton(Datum.DEFAULT_INPUT));
   }

   @Override
   public HString apply(@NonNull HString hString) {
      final int numSentences = hString.sentences().size();
      final int maxLength = hString.sentences().stream().mapToInt(h -> h.tokens().size()).max().orElse(0);

      byte[][][] tokens = new byte[numSentences][][];
      int[] sequenceLength = new int[numSentences];

      int si = 0;
      for(Annotation sentence : hString.sentences()) {
         byte[][] example = new byte[maxLength][];
         for(int i = 0; i < sentence.tokens().size(); i++) {
            example[i] = sentence.tokenAt(i).toString().getBytes();
         }
         for(int i = sentence.tokenLength(); i < maxLength; i++) {
            example[i] = Strings.EMPTY.getBytes();
         }
         sequenceLength[si] = sentence.tokenLength();
         tokens[si] = example;
         si++;
      }

      float[][][] embeddings = process(tokens, sequenceLength, getTensorflowModel());

      si = 0;
      for(Annotation sentence : hString.sentences()) {
         for(int i = 0; i < sentence.tokenLength(); i++) {
            sentence.tokenAt(i).put(Types.EMBEDDING, NDArrayFactory.ND.array(embeddings[si][i]));
         }
         si++;
      }
      return hString;
   }

   @Override
   public Model delegate() {
      return this;
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .tokenSequence(Datum.DEFAULT_INPUT, h -> Variable.binary(h.toString()))
                                    .build();
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if(name.equals(getOutput())) {
         return LabelType.NDArray;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model");
   }

   @Override
   public String getVersion() {
      return "https://tfhub.dev/google/elmo/3";
   }

   private float[][][] process(byte[][][] tokens, int[] sequenceLength, SavedModelBundle model) {
      List<Tensor<?>> tensors = model.session()
                                     .runner()
                                     .feed("tokens", Tensor.create(tokens))
                                     .feed("sequence_len", Tensor.create(sequenceLength))
                                     .fetch("output")
                                     .run();
      Tensor<?> tensor = tensors.get(0);
      float[][][] f = new float[(int) tensor.shape()[0]][(int) tensor.shape()[1]][(int) tensor.shape()[2]];
      tensor.copyTo(f);
      return f;
   }

   @Override
   protected void process(Datum datum, SavedModelBundle model) {
      byte[][][] tokens = new byte[datum.size()][][];
      int[] sequenceLength = new int[datum.size()];
      String[] name = new String[datum.size()];
      int i = 0;
      for(String s : new TreeSet<>(datum.keySet())) {
         Sequence<?> sequence = datum.get(s).asSequence();
         byte[][] sentence = new byte[sequence.size()][];
         for(int j = 0; j < sequence.size(); j++) {
            sentence[j] = sequence.get(j).asVariable().getName().getBytes();
         }
         tokens[i] = sentence;
         name[i] = s;
         sequenceLength[i] = sequence.size();
         i++;
      }

      float[][][] embeddings = process(tokens, sequenceLength, getTensorflowModel());
      for(int j = 0; j < datum.size(); j++) {
         datum.put(name[j], NDArrayFactory.ND.array(embeddings[j]));
      }
   }

   @Override
   public void setVersion(String version) {
      throw new UnsupportedOperationException();
   }

   public HString transform(@NonNull HString hString, @NonNull AnnotationType annotationType) {
      Datum datum = new Datum();
      for(Annotation annotation : hString.annotations(annotationType)) {
         datum.put(Long.toString(annotation.getId()), Variable.binary(annotation.toString()));
      }
      process(datum, getTensorflowModel());
      for(String s : datum.keySet()) {
         Annotation annotation = hString.document().annotation(Long.parseLong(s));
         annotation.put(Types.EMBEDDING, datum.get(s).asNDArray());
      }
      return hString;
   }

}//END OF UniversalSentenceEncoder
