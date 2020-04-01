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

package com.gengoai.hermes.workflow.actions;

/**
 * The type Embedding processor.
 *
 * @author David B. Bracewell
 */
public class EmbeddingProcessor {
//   implements
//} ProcessingModule {
//   public static final String EMBEDDING_PROPERTY = "EMBEDDING";
//   private static final long serialVersionUID = 1L;
//   @Getter
//   @Setter
//   private int dimension = 300;
//   @Getter
//   @Setter
//   private int minCount = 5;
//   @Getter
//   @Setter
//   private int windowSize = 5;
//   @Getter
//   @Setter
//   private Resource output = null;
//   @Getter
//   @Setter
//   private AnnotationType[] annotations = {Types.TOKEN};
//   @Getter
//   @Setter
//   private Retrofitting retrofitting = null;
//   @Getter
//   @Setter
//   private TYPE type = TYPE.WORD2VEC;
//   @Getter
//   @Setter
//   private int maxVocabulary = Integer.MAX_VALUE;
//
//   public enum TYPE {
//      SVD {
//         @Override
//         public EmbeddingLearner getLearner() {
//            return new SVDEmbedding();
//         }
//
//         @Override
//         public boolean topNFilter() {
//            return true;
//         }
//
//         @Override
//         public boolean minCountFilter() {
//            return true;
//         }
//
//      },
//      WORD2VEC {
//         @Override
//         public EmbeddingLearner getLearner() {
//            return new SparkWord2Vec();
//         }
//      };
//
//      public abstract EmbeddingLearner getLearner();
//
//      public boolean topNFilter() {
//         return false;
//      }
//
//      public boolean minCountFilter() {
//         return false;
//      }
//
//   }
//
//   @Override
//   public Corpus process(Corpus corpus, ProcessorContext context) throws Exception {
//      EmbeddingLearner learner = type.getLearner();
//      learner.setDimension(dimension);
//
//      Map<String, ?> parameters = learner.getParameters();
//
//      if (parameters.containsKey("minCount")) {
//         learner.setParameter("minCount", minCount);
//      }
//      if (parameters.containsKey("windowSize")) {
//         learner.setParameter("windowSize", windowSize);
//      }
//
//
//      logInfo("Embedding type={0} dimension={1} annotations={2}",
//              type,
//              learner.getDimension(),
//              Arrays.toString(annotations));
//
//      Dataset<Sequence> dataset;
//      if (annotations.length > 1) {
//         dataset = corpus.asEmbeddingDataset(annotations);
//      } else {
//         dataset = corpus.asEmbeddingDataset(annotations[0]);
//      }
//
//      PreprocessorList<Sequence> preprocessors = PreprocessorList.create();
//      if (type.minCountFilter()) {
//         preprocessors.add(new MinCountFilter(minCount).asSequenceProcessor());
//      }
//      if (type.topNFilter() && maxVocabulary > 0 && maxVocabulary < Integer.MAX_VALUE) {
//         preprocessors.add(new TopNFilter(maxVocabulary).asSequenceProcessor());
//      }
//
//      if (!preprocessors.isEmpty()) {
//         dataset = dataset.preprocess(preprocessors);
//      }
//
//      Embedding embedding = learner.train(dataset);
//
//      if (retrofitting != null) {
//         embedding = retrofitting.process(embedding);
//      }
//
//
//      context.property(EMBEDDING_PROPERTY, embedding);
//      if (output != null) {
//         output.getParent().mkdirs();
//         embedding.write(output);
//      }
//      return corpus;
//   }
//
//   @Override
//   public ProcessingState loadPreviousState(Corpus corpus, ProcessorContext context) {
//      if (output != null && output.exists()) {
//         try {
//            Embedding embedding = output.readObject();
//            logInfo("Loaded embedding with vocab size = {0} and dimension = {1}", embedding.size(),
//                    embedding.dimension());
//            context.property(EMBEDDING_PROPERTY, embedding);
//            return ProcessingState.LOADED(corpus);
//         } catch (Exception e) {
//            e.printStackTrace();
//         }
//      }
//      return ProcessingState.NOT_LOADED();
//   }
}//END OF EmbeddingProcessor
