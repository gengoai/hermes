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

package com.gengoai.hermes.tools;

import com.gengoai.apollo.ml.embedding.Embedding;
import com.gengoai.application.Option;
import com.gengoai.io.resource.Resource;

/**
 * The type Embedding converter.
 *
 * @author David B. Bracewell
 */
public class EmbeddingConverter extends HermesCLI {
   @Option(description = "The word2vec text source.", required = true)
   private Resource in;
   @Option(description = "Where to save the output.", required = true)
   private Resource out;

   public static void main(String[] args) {
      new EmbeddingConverter().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      Embedding.readWord2VecTextFormat(in).write(out);
   }

}// END OF EmbeddingConverter
