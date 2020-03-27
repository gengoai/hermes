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

package com.gengoai.hermes.format;

import com.gengoai.ParamMap;
import com.gengoai.ParameterDef;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.corpus.io.SaveMode;

public class DocFormatParameters extends ParamMap<DocFormatParameters> {
   /**
    * Defines the {@link DocumentFactory} to use for constructing documents
    */
   public static final ParameterDef<DocumentFactory> DOCUMENT_FACTORY = ParameterDef.param("documentFactory",
                                                                                           DocumentFactory.class);
   /**
    * Defines the {@link SaveMode} when writing a Corpus
    */
   public static final ParameterDef<SaveMode> SAVE_MODE = ParameterDef.param("saveMode", SaveMode.class);

   /**
    * Defines the {@link DocumentFactory} to use for constructing documents
    */
   public final Parameter<DocumentFactory> documentFactory = parameter(DOCUMENT_FACTORY, DocumentFactory.getInstance());
   /**
    * Defines the {@link SaveMode} when writing a Corpus
    */
   public final Parameter<SaveMode> saveMode = parameter(SAVE_MODE, SaveMode.ERROR);
}//END OF DocFormatParameters
