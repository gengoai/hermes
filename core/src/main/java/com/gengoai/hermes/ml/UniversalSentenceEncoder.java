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
import com.gengoai.apollo.ml.model.ModelIO;
import com.gengoai.apollo.ml.model.TensorFlowModel;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.hermes.*;
import com.gengoai.io.Resources;
import lombok.NonNull;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class UniversalSentenceEncoder extends TensorFlowModel implements HStringMLModel {
   private static final long serialVersionUID = 1L;
   public static final int DIMENSION = 512;

   public static void main(String[] args) throws Exception {
      UniversalSentenceEncoder use = ModelIO.load(Resources.from("/data/hermes/en/models/sentence_encoder"));
      Document d = Document.create("This is a test. This is the second sentence of a different size.");
      d.annotate(Types.SENTENCE, Types.TOKEN);
      use.transform(d);
      for(Annotation sentence : d.sentences()) {
         System.out.println(sentence.attribute(Types.EMBEDDING));
      }
   }

   public UniversalSentenceEncoder() {
      super(new Transformer(Collections.emptyList()),
            Collections.singleton(Datum.DEFAULT_INPUT),
            Collections.singleton(Datum.DEFAULT_OUTPUT));
   }

   @Override
   public HString apply(@NonNull HString hString) {
      Datum datum = new Datum();
      for(Annotation sentence : hString.sentences()) {
         datum.put(Integer.toString(sentence.attribute(Types.INDEX)), Variable.binary(sentence.toString()));
      }
      process(datum, getTensorflowModel());
      for(String s : datum.keySet()) {
         int index = Integer.parseInt(s);
         hString.sentences().get(index).put(Types.EMBEDDING, datum.get(s).asNDArray());
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
                                    .defaultInput(h -> Variable.binary(h.toString()))
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
      return "https://tfhub.dev/google/universal-sentence-encoder-large/3";
   }

   @Override
   protected void process(Datum datum, SavedModelBundle model) {
      byte[][] input = new byte[datum.size()][];
      String[] name = new String[datum.size()];
      int i = 0;
      for(String s : new TreeSet<>(datum.keySet())) {
         input[i] = datum.get(s)
                         .asVariable()
                         .getName()
                         .toLowerCase()
                         .replaceAll("\\p{Punct}+", " ")
                         //                         .replaceAll("\\d+", " ")
                         .replaceAll("\\s+", " ")
                         .getBytes();
         name[i] = s;
         i++;
      }

      List<Tensor<?>> tensors = model.session()
                                     .runner()
                                     .feed("input", Tensor.create(input))
                                     .fetch("output")
                                     .run();
      for(Tensor<?> tensor : tensors) {
         float[][] f = new float[datum.size()][512];
         tensor.copyTo(f);
         for(int j = 0; j < datum.size(); j++) {
            datum.put(name[j], NDArrayFactory.ND.array(f[j]));
         }
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
