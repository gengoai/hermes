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

package com.gengoai.hermes.corpus.io;

import com.gengoai.ParamMap;
import com.gengoai.ParameterDef;
import com.gengoai.hermes.DocumentFactory;

/**
 * Specialized ParamMap for {@link CorpusReader}s and {@link CorpusWriter}s.
 *
 * @author David B. Bracewell
 */
public class CorpusParameters extends ParamMap<CorpusParameters> {
   /**
    * Defines the {@link DocumentFactory} to use for constructing documents
    */
   public static final ParameterDef<DocumentFactory> DOCUMENT_FACTORY = ParameterDef.param("documentFactory", DocumentFactory.class);
   /**
    * True create a distributed corpus, False non-distributed
    */
   public static final ParameterDef<Boolean> IS_DISTRIBUTED = ParameterDef.boolParam("isDistributed");
   /**
    * True create a parallel corpus, False sequential
    */
   public static final ParameterDef<Boolean> IS_PARALLEL = ParameterDef.boolParam("isParallel");
   /**
    * True create a parallel corpus, False sequential
    */
   public static final ParameterDef<Boolean> IN_MEMORY = ParameterDef.boolParam("inMemory");
   /**
    * Defines the {@link SaveMode} when writing a Corpus
    */
   public static final ParameterDef<SaveMode> SAVE_MODE = ParameterDef.param("saveMode", SaveMode.class);
   /**
    * Defines the {@link DocumentFactory} to use for constructing documents
    */
   public final Parameter<DocumentFactory> documentFactory = parameter(DOCUMENT_FACTORY, DocumentFactory.getInstance());
   /**
    * True create a distributed corpus, False non-distributed
    */
   public final Parameter<Boolean> isDistributed = parameter(IS_DISTRIBUTED, Boolean.FALSE);
   /**
    * True create a parallel corpus, False sequential
    */
   public final Parameter<Boolean> isParallel = parameter(IS_PARALLEL, Boolean.FALSE);
   /**
    * True create an in-memory corpus, False streaming
    */
   public final Parameter<Boolean> inMemory = parameter(IN_MEMORY, Boolean.FALSE);
   /**
    * Defines the {@link SaveMode} when writing a Corpus
    */
   public final Parameter<SaveMode> saveMode = parameter(SAVE_MODE, SaveMode.ERROR);


}//END OF CorpusReaderParameters
