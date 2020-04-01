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
 */

package com.gengoai.hermes.preprocessing;

import com.gengoai.Language;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.config.Config;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.Reflect;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * The type Text normalizer.
 *
 * @author David B. Bracewell
 */
@JsonHandler(TextNormalizer.Marshaller.class)
public abstract class TextNormalizer implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Performs a pre-processing operation on the input string in the given input language
    *
    * @param input         The input text
    * @param inputLanguage The language of the input
    * @return The post-processed text
    */
   public final String apply(String input, Language inputLanguage) {
      if(input != null && Config.get(this.getClass(), inputLanguage, "apply").asBoolean(true)) {
         return performNormalization(input, inputLanguage);
      }
      return input;
   }

   /**
    * Performs a pre-processing operation on the input string in the given input language
    *
    * @param input    The input text
    * @param language The language of the input
    * @return The post-processed text
    */
   protected abstract String performNormalization(String input, Language language);

   /**
    * The type Marshaller.
    */
   public static class Marshaller extends com.gengoai.json.JsonMarshaller<TextNormalizer> {

      @Override
      protected TextNormalizer deserialize(JsonEntry entry, Type type) {
         try {
            return Reflect.onClass(entry.getAsString()).create().get();
         } catch(Exception e) {
            throw new RuntimeException(e);
         }
      }

      @Override
      protected JsonEntry serialize(TextNormalizer textNormalizer, Type type) {
         return JsonEntry.from(textNormalizer.getClass().getName());
      }
   }

}//END OF TextPreprocessor
