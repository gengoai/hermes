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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.hermes.corpus.DocumentCollection;
import lombok.NonNull;

import java.io.Serializable;

/**
 * <p>
 * A workflow represents a set of _actions_ to perform on an document collection. Actions fall into one or more of the
 * following three categories:
 * </P>
 * <p><ol>
 * <li>Modify - The action modifies the documents in the collection, e.g. adds new annotations or attributes.</li>
 * <li>Compute - The action generates information that is added to the {@link Context} for use by downstream
 * actions.</li>
 * <li>Output - The action generates an output for consumption by external processes and/or downstream actions.</li>
 * </ol></p>
 * <p>Actions share a common key-value memory store, called a {@link Context}, in which information they require or
 * they generate can be added.</p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
      @JsonSubTypes.Type(value = SequentialWorkflow.class, name = "Sequential")
})
public interface Workflow extends Serializable {

   /**
    * Gets starting context.
    *
    * @return the starting context
    */
   Context getStartingContext();

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
   DocumentCollection process(@NonNull DocumentCollection input, @NonNull Context context) throws Exception;

   /**
    * Sets starting context.
    *
    * @param context the context
    */
   void setStartingContext(Context context);

}//END OF Workflow
