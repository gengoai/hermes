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

package com.gengoai.hermes.workflow;

import com.gengoai.hermes.corpus.Corpus;
import lombok.NonNull;

import java.io.Serializable;

/**
 * The type Processing state.
 *
 * @author David B. Bracewell
 */
public class State implements Serializable {
   private static final long serialVersionUID = 1L;

   private final Corpus corpus;

   private State(Corpus corpus) {
      this.corpus = corpus;
   }

   /**
    * Loaded processing state.
    *
    * @param corpus the corpus
    * @return the processing state
    */
   public static State LOADED(@NonNull Corpus corpus) {
      return new State(corpus);
   }

   /**
    * Not loaded processing state.
    *
    * @return the processing state
    */
   public static State NOT_LOADED() {
      return new State(null);
   }

   /**
    * Is loaded boolean.
    *
    * @return the boolean
    */
   public boolean isLoaded() {
      return corpus != null;
   }

   /**
    * Gets corpus.
    *
    * @return the corpus
    */
   public Corpus getCorpus() {
      return corpus;
   }

}//END OF ProcessingState
