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
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.FixedEncoder;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.evaluation.Evaluation;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.TensorFlowModel;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.vectorizer.CountVectorizer;
import com.gengoai.apollo.ml.transform.vectorizer.IndexingVectorizer;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.BasicCategories;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.feature.Features;
import lombok.NonNull;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gengoai.hermes.ResourceType.WORD_LIST;

public class NERTensorFlowModel extends TensorFlowModel implements HStringMLModel {

   public NERTensorFlowModel() {
      super(Collections.singleton("label"),
            Map.of("words", new FixedEncoder(WORD_LIST.locate("glove", Language.ENGLISH).orElseThrow(), "--UNKNOWN--"),
                   "chars", new IndexEncoder("-PAD-"),
                   "pos", new IndexEncoder("-PAD-"),
                   "word_shape", new IndexEncoder("-PAD-"),
                   "features", new IndexEncoder("-PAD-"),
                   "chunk", new IndexEncoder("O"),
                   "category", new IndexEncoder(BasicCategories.ROOT.label()),
                   "label", new IndexEncoder("O")));
   }

   @Override
   public HString apply(HString hString) {
      //Perform inference over the entire document at once
      List<Annotation> sentences = hString.sentences();
      int maxSentenceLength = sentences.stream().mapToInt(HString::tokenLength).max().orElse(1);
      NDArray words = NDArrayFactory.ND.array(sentences.size(), maxSentenceLength);
      NDArray chars = NDArrayFactory.ND.array(sentences.size(), maxSentenceLength, 10);
      NDArray pos = NDArrayFactory.ND.array(sentences.size(), maxSentenceLength);
      NDArray word_shape = NDArrayFactory.ND.array(sentences.size(), maxSentenceLength);
      NDArray chunk = NDArrayFactory.ND.array(sentences.size(), maxSentenceLength, encoders.get("chunk").size());
      NDArray category = NDArrayFactory.ND.array(sentences.size(), maxSentenceLength, encoders.get("category").size());
      for(int i = 0; i < sentences.size(); i++) {
         Annotation sentence = sentences.get(i);
         Datum datum = transform(getDataGenerator().apply(sentence));
         words.setRow(i, datum.get("words").asNDArray().padRowPost(maxSentenceLength));
         word_shape.setRow(i, datum.get("word_shape").asNDArray().padRowPost(maxSentenceLength));
         chars.setMatrix(0, i, datum.get("chars").asNDArray().padPost(maxSentenceLength, 10));
         pos.setRow(i, datum.get("pos").asNDArray().padRowPost(maxSentenceLength));
         chunk.setMatrix(0, i, datum.get("chunk").asNDArray());
         category.setMatrix(0, i, datum.get("category").asNDArray());
      }
      NDArray result = process(getTensorFlowModel(),
                               words,
                               chars,
                               pos,
                               chunk,
                               word_shape,
                               category);

      for(int i = 0; i < result.channels(); i++) {
         Annotation sentence = sentences.get(i);
         IOB.decode(sentence, decode(result.slice(i)), Types.ENTITY);
      }
      return hString;
   }

   protected Transformer createTransformer() {
      IndexingVectorizer glove = new IndexingVectorizer(encoders.get("words"));
      glove.source("words");

      IndexingVectorizer charIndexer = new IndexingVectorizer(encoders.get("chars"));
      charIndexer.source("chars");

      IndexingVectorizer posIndexer = new IndexingVectorizer(encoders.get("pos"));
      posIndexer.source("pos");

      CountVectorizer phraseChunkIndexer = new CountVectorizer(encoders.get("chunk"));
      phraseChunkIndexer.source("chunk");

      CountVectorizer catVectorizer = new CountVectorizer(encoders.get("category"));
      catVectorizer.source("category");

      CountVectorizer featureVectorizer = new CountVectorizer(encoders.get("features"));
      featureVectorizer.source("features");

      IndexingVectorizer labelVectorizer = new IndexingVectorizer(encoders.get("label"));
      labelVectorizer.source("label");

      IndexingVectorizer shapeVectorizer = new IndexingVectorizer(encoders.get("word_shape"));
      shapeVectorizer.source("word_shape");

      return new Transformer(List.of(glove,
                                     charIndexer,
                                     posIndexer,
                                     phraseChunkIndexer,
                                     catVectorizer,
                                     shapeVectorizer,
                                     featureVectorizer,
                                     labelVectorizer));
   }

   private Sequence<?> decode(NDArray slice) {
      VariableSequence sequence = new VariableSequence();
      Encoder encoder = encoders.get(getOutput());
      String previous = "O";
      for(int word = 0; word < slice.rows(); word++) {
         NDArray matrix = slice.getRow(word);
         int l = (int) matrix.argmax();
         String tag = encoder.decode(l);
         while(!IOBValidator.INSTANCE.isValid(tag, previous, matrix)) {
            matrix.set(l, Double.NEGATIVE_INFINITY);
            l = (int) matrix.argmax();
            tag = encoder.decode(l);
         }
         previous = tag;
         sequence.add(Variable.real(tag, matrix.get(l)));
      }
      return sequence;
   }

   @Override
   public Model delegate() {
      return this;
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .tokenSequence("words",
                                                   Featurizer.valueFeaturizer(HString::toLowerCase))
                                    .tokenSequence("chars",
                                                   Featurizer.booleanFeaturizer(h -> h.charNGrams(1)
                                                                                      .stream()
                                                                                      .map(HString::toLowerCase)
                                                                                      .collect(Collectors.toList())))
                                    .tokenSequence("pos",
                                                   Featurizer.valueFeaturizer(h -> h.pos().name()))
                                    .source("chunk",
                                            IOB.encoder(Types.PHRASE_CHUNK))
                                    .tokenSequence("features", Featurizer.chain(Features.WordClass,
                                                                                Features.IsTitleCase,
                                                                                Features.IsAllCaps))
                                    .tokenSequence("word_shape", Features.WordShape)
                                    .tokenSequence("category",
                                                   Featurizer.booleanFeaturizer(h -> h.categories()
                                                                                      .stream()
                                                                                      .map(BasicCategories::label)
                                                                                      .collect(Collectors.toList())))
                                    .source("label",
                                            IOB.encoder(Types.ENTITY))

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

   protected NDArray process(SavedModelBundle model,
                             NDArray words,
                             NDArray chars,
                             NDArray pos,
                             NDArray chunk,
                             NDArray word_shape,
                             NDArray category) {
      List<Tensor<?>> tensors = model.session()
                                     .runner()
                                     .feed("words", Tensor.create(words.toFloatArray2()))
                                     .feed("chars", Tensor.create(chars.toFloatArray3()))
                                     .feed("pos", Tensor.create(pos.toFloatArray2()))
                                     .feed("chunk", Tensor.create(chunk.toFloatArray3()))
                                     .feed("word_shape", Tensor.create(word_shape.toFloatArray2()))
                                     //                                     .feed("category", Tensor.create(category.toFloatArray3()))
                                     .fetch("label/truediv")
                                     .run();
      return NDArrayFactory.ND.fromTensorFlowTensor(tensors.get(0));
   }

   @Override
   protected void process(Datum d, SavedModelBundle model) {
      NDArray n = process(model,
                          d.get("words").asNDArray().T(),
                          d.get("chars").asNDArray().padColumnPost(10),
                          d.get("pos").asNDArray().T(),
                          d.get("chunk").asNDArray(),
                          d.get("word_shape").asNDArray().T(),
                          d.get("category").asNDArray());
      d.put("label", decode(n.slice(0)));
   }

   @Override
   public void setVersion(String version) {
      throw new UnsupportedOperationException();
   }

   @Override
   public DataSet transform(@NonNull DocumentCollection documentCollection) {
      return documentCollection.annotate(Types.CATEGORY,
                                         Types.PHRASE_CHUNK)
                               .asDataSet(getDataGenerator());
   }
}//END OF NERTensorFlowModel
