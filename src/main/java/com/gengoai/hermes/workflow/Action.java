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

import com.gengoai.config.Config;
import com.gengoai.hermes.corpus.Corpus;

import java.io.Serializable;

/**
 * The interface Action.
 *
 * @author David B. Bracewell
 */
public interface Action extends Serializable {

   /**
    * Gets the override status for this processing module, which can be defined using configuration in the form
    * <code>fully.qualified.class.name.override=true</code> or all processing can be reperformed using
    * <code>processing.override.all=true</code>. By default, the status is false, which means try to load the previous
    * state.
    *
    * @return True force reprocessing, False try to load the previous state.
    */
   default boolean getOverrideStatus() {
      return Config.get("processing.override.all")
                   .asBooleanValue(Config.get(this.getClass(), "override")
                                         .asBooleanValue(false));
   }

   /**
    * Loads from a previous processing state.
    *
    * @param corpus  the corpus being processed
    * @param context the context of the processor
    * @return the processing state (NOT_LOADED by default meaning there is no previous state).
    */
   default State loadPreviousState(Corpus corpus, Context context) {
      return State.NOT_LOADED();
   }

   /**
    * Process corpus.
    *
    * @param corpus  the corpus
    * @param context the context
    * @return the corpus
    * @throws Exception the exception
    */
   Corpus process(Corpus corpus, Context context) throws Exception;


}//END OF Action
