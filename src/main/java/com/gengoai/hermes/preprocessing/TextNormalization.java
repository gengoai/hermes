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
import com.gengoai.Validation;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.config.Config;
import com.gengoai.json.JsonEntry;
import com.gengoai.logging.Logger;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * <p>Class takes care of normalizing text using a number of {@link com.gengoai.hermes.preprocessing.TextNormalizer}s.
 * </p>
 *
 * @author David B. Bracewell
 */
@JsonHandler(TextNormalization.Marshaller.class)
public class TextNormalization implements Serializable {
   private static final long serialVersionUID = 1L;

   private static volatile TextNormalization INSTANCE;

   private static final Logger log = Logger.getLogger(TextNormalization.class);
   private static final String LIST_CONFIG = "hermes.preprocessing.normalizers";
   private final List<TextNormalizer> preprocessors;


   /**
    * The type Marshaller.
    */
   public static class Marshaller extends com.gengoai.json.JsonMarshaller<TextNormalization> {

      @Override
      protected TextNormalization deserialize(JsonEntry entry, Type type) {
         return new TextNormalization(entry.getAsArray(TextNormalizer.class));
      }

      @Override
      protected JsonEntry serialize(TextNormalization textNormalization, Type type) {
         return JsonEntry.from(textNormalization.preprocessors);
      }
   }

   /**
    * Configured instance text normalization.
    *
    * @return A TextNormalization class configured via config files.
    */
   public static TextNormalization configuredInstance() {
      if (INSTANCE == null) {
         synchronized (TextNormalization.class) {
            if (INSTANCE == null) {
               INSTANCE = new TextNormalization();
               INSTANCE.initConfig();
            }
         }
      }
      return INSTANCE;
   }

   /**
    * No op instance text normalization.
    *
    * @return A TextNormalization that does no normalization
    */
   public static TextNormalization noOpInstance() {
      TextNormalization factory = new TextNormalization();
      factory.preprocessors.clear();
      return factory;
   }

   /**
    * Create instance text normalization.
    *
    * @param normalizers the normalizers
    * @return the text normalization
    */
   public static TextNormalization createInstance(Collection<? extends TextNormalizer> normalizers) {
      return new TextNormalization(normalizers);
   }

   private TextNormalization() {
      preprocessors = new ArrayList<>();
   }

   private TextNormalization(Collection<? extends TextNormalizer> normalizers) {
      if (normalizers == null) {
         preprocessors = Collections.emptyList();
      } else {
         preprocessors = new ArrayList<>(normalizers);
      }
   }


   private void initConfig() {
      if (Config.hasProperty(LIST_CONFIG)) {
         for (TextNormalizer normalizer : Config.get(LIST_CONFIG).asList(TextNormalizer.class)) {
            Validation.notNull(normalizer,
                               "Error in Config: " + Config.get(LIST_CONFIG).asString() + " : " + Config.get(
                                  LIST_CONFIG).asList(TextNormalizer.class));
            preprocessors.add(normalizer);
            if (log.isLoggable(Level.FINE)) {
               log.fine("Adding normalizer: {0}", normalizer.getClass());
            }
         }
      }
   }

   /**
    * Normalizes a string with a number of text normalizers.
    *
    * @param input    The input string
    * @param language The language of the input string
    * @return A normalized version of the string
    */
   public String normalize(String input, Language language) {
      if (input == null) {
         return null;
      }
      String finalString = input;
      for (TextNormalizer textNormalizer : preprocessors) {
         finalString = textNormalizer.apply(finalString, language);
      }
      return finalString;
   }


}//END OF TextNormalizer
