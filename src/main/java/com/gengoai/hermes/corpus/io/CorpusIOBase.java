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

import com.gengoai.ParameterDef;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.function.Unchecked;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

abstract class CorpusIOBase<T extends CorpusIOBase> implements Serializable {
   private static final long serialVersionUID = 1L;
   @NonNull
   private final CorpusParameters parameters;

   /**
    * Instantiates a new Corpus io base.
    *
    * @param parameters the parameters
    */
   public CorpusIOBase(CorpusParameters parameters) {
      this.parameters = parameters;
   }

   /**
    * Instantiates a new Corpus io base.
    */
   public CorpusIOBase() {
      this.parameters = new CorpusParameters();
   }

   /**
    * Gets options.
    *
    * @return the options
    */
   public CorpusParameters getOptions() {
      return parameters;
   }

   /**
    * Option corpus reader.
    *
    * @param name  the name
    * @param value the value
    * @return the corpus reader
    */
   public final T option(String name, Object value) {
      getOptions().set(name, value);
      return Cast.as(this);
   }

   /**
    * Option corpus reader.
    *
    * @param <E>       the type parameter
    * @param parameter the parameter
    * @param value     the value
    * @return the corpus reader
    */
   public final <E> T option(ParameterDef<E> parameter, E value) {
      getOptions().set(parameter, value);
      return Cast.as(this);
   }

   /**
    * Options corpus reader.
    *
    * @param resource the resource
    * @return the corpus reader
    * @throws IOException the io exception
    */
   public final T options(Resource resource) throws IOException {
      Json.parseObject(resource).forEach(this::option);
      return Cast.as(this);
   }

   public final T options(CorpusParameters parameters)  {
      parameters.parameterNames().forEach(k -> option(k, parameters.get(k)));
      return Cast.as(this);
   }

   /**
    * Options corpus reader.
    *
    * @param options the options
    * @return the corpus reader
    */
   public final T options(Map<String, ?> options) {
      options.forEach(Unchecked.biConsumer((k, v) -> {
         ParameterDef<?> param = getOptions().getParam(k);
         option(param, Cast.as(Converter.convert(v, param.type)));
      }));
      return Cast.as(this);
   }

}//END OF CorpusIOBase
