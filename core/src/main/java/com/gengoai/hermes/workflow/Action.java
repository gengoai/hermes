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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.config.Config;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;

import java.io.Serializable;

/**
 * <p>An action defines a processing step to perform on a {@link Corpus} with a given {@link Context} which results in
 * either modifying the corpus or the context. Action implementations can persist their state to be reused at a later
 * time including across jvm instances & runs. This is done by implementing the {@link
 * #loadPreviousState(DocumentCollection, Context)} method. An action can ignore its state and reprocess the corpus when
 * either the config setting  <code>processing.override.all</code> is set to true or the config setting
 * <code>className.override</code> is set tp true.
 * </p>
 *
 * @author David B. Bracewell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public interface Action extends Serializable {

   /**
    * Gets the override status for this processing module, which can be defined using configuration in the form
    * <code>fully.qualified.class.name.override=true</code> or all processing can be reperformed using
    * <code>processing.override.all=true</code>. By default, the status is false, which means try to load the previous
    * state.
    *
    * @return True force reprocessing, False try to load the previous state.
    */
   @JsonIgnore
   default boolean getOverrideStatus() {
      return Config.get("processing.override.all")
                   .asBooleanValue(Config.get(this.getClass(), "override").asBooleanValue(false));
   }

   /**
    * Loads from a previous processing state.
    *
    * @param corpus  the corpus being processed
    * @param context the context of the processor
    * @return the processing state (NOT_LOADED by default meaning there is no previous state).
    */
   default State loadPreviousState(DocumentCollection corpus, Context context) {
      return State.NOT_LOADED;
   }

   /**
    * Process corpus.
    *
    * @param corpus  the corpus
    * @param context the context
    * @throws Exception the exception
    */
   DocumentCollection process(DocumentCollection corpus, Context context) throws Exception;

}//END OF Action
