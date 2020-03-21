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

package com.gengoai.hermes.workflow;

import com.gengoai.annotation.JsonHandler;
import com.gengoai.hermes.corpus.Corpus;
import lombok.NonNull;

import java.io.Serializable;

/**
 * The interface Workflow.
 */
@JsonHandler(WorkflowMarshaller.class)
public interface Workflow extends Serializable {

   /**
    * Gets starting context.
    *
    * @return the starting context
    */
   Context getStartingContext();

   /**
    * Sets starting context.
    *
    * @param context the context
    */
   void setStartingContext(Context context);

   /**
    * Gets type.
    *
    * @return the type
    */
   String getType();

   /**
    * Process corpus.
    *
    * @param input   the input
    * @param context the context
    * @return the corpus
    * @throws Exception the exception
    */
   Corpus process(@NonNull Corpus input, @NonNull Context context) throws Exception;

}//END OF Workflow
