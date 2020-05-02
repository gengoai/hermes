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

import com.gengoai.Language;
import com.gengoai.ParamMap;
import com.gengoai.ParameterDef;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.preprocessing.TextNormalization;
import com.gengoai.hermes.preprocessing.TextNormalizer;
import com.gengoai.io.SaveMode;

import java.util.List;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * The type Doc format parameters.
 */
public class DocFormatParameters extends ParamMap<DocFormatParameters> {
   /**
    * Defines the default language for new documents
    */
   public static final ParameterDef<Language> DEFAULT_LANGUAGE = ParameterDef.param("defaultLanguage", Language.class);
   /**
    * Defines if a document collection should be distributed (true) or local (false)
    */
   public static final ParameterDef<Boolean> DISTRIBUTED = ParameterDef.param("distributed", Boolean.class);
   /**
    * Defines the text normalization to use when constructing documents
    */
   public static final ParameterDef<List<TextNormalizer>> NORMALIZERS = ParameterDef.param("normalizers",
                                                                                           parameterizedType(List.class,
                                                                                                             TextNormalizer.class));
   /**
    * Defines the {@link SaveMode} when writing a Corpus
    */
   public static final ParameterDef<SaveMode> SAVE_MODE = ParameterDef.param("saveMode", SaveMode.class);

   /**
    * Whether to overwrite, ignore, or throw an error when writing a corpus to an existing file/directory (default
    * ERROR).
    */
   public final Parameter<SaveMode> saveMode = parameter(SAVE_MODE, SaveMode.ERROR);
   /**
    * Creates a distributed document collection when the value is set to true (default false).
    */
   public final Parameter<Boolean> distributed = parameter(DISTRIBUTED, false);
   /**
    * The default language for new documents. (default calls Hermes.defaultLanguage())
    */
   public final Parameter<Language> defaultLanguage = parameter(DEFAULT_LANGUAGE, Hermes.defaultLanguage());
   /**
    * The class names of the text normalizes to use when constructing documents. (default calls
    * TextNormalization.configuredInstance().getPreprocessors())
    */
   public final Parameter<List<TextNormalizer>> normalizers = parameter(NORMALIZERS,
                                                                        TextNormalization.configuredInstance()
                                                                                         .getNormalizers());

   /**
    * @return Creates a document factory based on the this set of parameters
    */
   public DocumentFactory getDocumentFactory() {
      return DocumentFactory.builder()
                            .defaultLanguage(defaultLanguage.value())
                            .normalizers(normalizers.value())
                            .build();
   }

}//END OF DocFormatParameters
