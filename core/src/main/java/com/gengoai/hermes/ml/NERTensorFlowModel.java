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

import com.gengoai.Language;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.FixedEncoder;
import com.gengoai.apollo.ml.evaluation.Evaluation;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.TensorFlowModel;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.vectorizer.IndexingVectorizer;
import com.gengoai.collection.Sets;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import lombok.NonNull;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.gengoai.hermes.ResourceType.WORD_LIST;

public class NERTensorFlowModel extends TensorFlowModel implements HStringMLModel {

   private static Transformer createTransformer() {
      IndexingVectorizer glove = new IndexingVectorizer(
            new FixedEncoder(WORD_LIST.locate("glove", Language.ENGLISH).orElseThrow(), "--UNKNOWN--"));
      glove.source("words");
      IndexingVectorizer charIndexer = new IndexingVectorizer("-PAD-");
      charIndexer.source("chars");
      IndexingVectorizer posIndexer = new IndexingVectorizer("-PAD-");
      posIndexer.source("pos");
      //      IndexingVectorizer phraseChunkIndexer = new IndexingVectorizer("-PAD-");
      //      phraseChunkIndexer.source("chunk");
      IndexingVectorizer labelVectorizer = new IndexingVectorizer("O");
      labelVectorizer.source("label");
      return new Transformer(List.of(glove, charIndexer, posIndexer, labelVectorizer));
   }

   public NERTensorFlowModel() {
      super(createTransformer(),
            Sets.linkedHashSetOf("words", "chars"),
            Collections.singleton("label"));
   }

   @Override
   public HString apply(HString hString) {
      for(Annotation sentence : hString.sentences()) {
         Datum datum = transform(getDataGenerator().apply(sentence));
         IOB.decode(sentence, datum.get(getOutput()).asSequence(), Types.ENTITY);
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
                                    .tokenSequence("words", Featurizer.valueFeaturizer(HString::toLowerCase))
                                    .tokenSequence("chars",
                                                   Featurizer.booleanFeaturizer(h -> h.charNGrams(1)
                                                                                      .stream()
                                                                                      .map(HString::toLowerCase)
                                                                                      .collect(Collectors.toList())))
                                    .tokenSequence("pos", Featurizer.valueFeaturizer(h -> h.pos().name()))
                                    //                                    .source("chunk", IOB.encoder(Types.PHRASE_CHUNK))
                                    .source("label", IOB.encoder(Types.ENTITY))
                                    .build();
   }

   @Override
   public Evaluation getEvaluator() {
      return new CoNLLEvaluation(getOutput());
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if(name.equals("label")) {
         return LabelType.Sequence;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   @Override
   public String getVersion() {
      return "1.0";
   }

   @Override
   protected void process(Datum d, SavedModelBundle model) {
      NDArray words = d.get("words").asNDArray();
      float[][] wordMatrix = new float[1][words.rows()];
      for(int r = 0; r < words.rows(); r++) {
         wordMatrix[0][r] = (float) words.get(r);
      }
      NDArray pos = d.get("pos").asNDArray();
      float[][] posMatrix = new float[1][pos.rows()];
      for(int r = 0; r < pos.rows(); r++) {
         posMatrix[0][r] = (float) pos.get(r);
      }
      NDArray chars = d.get("chars").asNDArray();
      float[][][] charMatrix = new float[1][chars.rows()][10];
      for(int r = 0; r < chars.rows(); r++) {
         for(int c = 0; c < Math.min(10, chars.columns()); c++) {
            charMatrix[0][r][c] = (float) chars.get(r, c);
         }
      }
      List<Tensor<?>> tensors = model.session()
                                     .runner()
                                     .feed("words", Tensor.create(wordMatrix))
                                     .feed("chars", Tensor.create(charMatrix))
                                     .feed("pos", Tensor.create(posMatrix))
                                     .fetch("label/dense/Sigmoid")
                                     .run();
      Encoder encoder = transformer.getTransforms().stream()
                                   .filter(t -> t.getInputs().contains("label"))
                                   .findFirst()
                                   .map(t -> Cast.<IndexingVectorizer>as(t).getEncoder())
                                   .orElseThrow();

      VariableSequence sequence = new VariableSequence();
      for(Tensor<?> tensor : tensors) {
         long[] shape = tensor.shape();
         float[][] floats = new float[(int) shape[0]][(int) shape[1]];
         tensor.copyTo(floats);
         String previous = "O";
         for(float[] aFloat : floats) {
            NDArray matrix = NDArrayFactory.ND.rowVector(aFloat);
            int l = (int) matrix.argmax();
            String tag = encoder.decode(l);
            while(!IOBValidator.INSTANCE.isValid(tag, previous, matrix)) {
               matrix.set(l, Double.NEGATIVE_INFINITY);
               l = (int) matrix.argmax();
               tag = encoder.decode(l);
            }
            previous = tag;
            sequence.add(Variable.real(tag, aFloat[l]));
         }
      }
      d.put("label", sequence);
   }

   @Override
   public void setVersion(String version) {

   }
}//END OF NERTensorFlowModel
