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

package com.gengoai.hermes;

import com.gengoai.Language;
import com.gengoai.SystemInfo;
import com.gengoai.config.Config;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.reflection.Reflect;
import lombok.NonNull;

import java.util.Locale;

import static com.gengoai.string.Re.*;

/**
 * <p>Convenience methods for getting common configuration options. </p>
 *
 * @author David B. Bracewell
 */
public final class Hermes {
   /**
    * The Hermes package
    */
   public static final String HERMES_PACKAGE = "com.gengoai.hermes";
   /**
    * The config key for Hermes's resource directory
    */
   public static final String HERMES_RESOURCES_CONFIG = "hermes.resources.dir";
   /**
    * Definition of an Identifier string for use in various components of Hermes
    */
   public static final String IDENTIFIER = oneOrMore(chars(LETTER,
                                                           DIGIT,
                                                           "_",
                                                           e('$'),
                                                           e('.')));

   private Hermes() {
      throw new IllegalAccessError();
   }

   /**
    * Attempts to find and return the Class for the default implementation of the given resource for the given language
    * as defined by: <code>com.gengoai.hermes.[LANGUAGE_CODE_LOWER].[LANGUAGE_CODE_UPPER]Resource</code>, e.g.
    * <code>com.gengoai.hermes.en.ENStemmer</code>.
    *
    * @param language the language
    * @param resource the resource type
    * @return the class or null
    */
   public static Class<?> defaultImplementation(@NonNull Language language,
                                                @NonNull String resource) {
      return Reflect.getClassForNameQuietly(String.format("%s.%s.%s%s",
                                                          HERMES_PACKAGE,
                                                          language.getCode().toLowerCase(),
                                                          language.getCode(),
                                                          resource));
   }

   /**
    * Get the default language. The default language is specified using <code>hermes.defaultLanguage/code>.
    * If the configuration option is not set, it will default to the language matching the system locale.
    *
    * @return the default language
    */
   public static Language defaultLanguage() {
      return Config.get("hermes.defaultLanguage")
                   .as(Language.class, Language.fromLocale(Locale.getDefault()));
   }

   /**
    * @return The directory containing Hermes's resource files
    */
   public static Resource getResourcesDir() {
      return Config.get(HERMES_RESOURCES_CONFIG)
                   .asResource(Resources.from(SystemInfo.USER_HOME).getChild("hermes"));
   }

}//END OF Hermes
