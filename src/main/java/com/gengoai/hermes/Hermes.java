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
import com.gengoai.cache.Cache;
import com.gengoai.config.Config;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.reflection.Reflect;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Locale;
import java.util.Optional;

import static com.gengoai.hermes.corpus.Formats.HCF;
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
    * The constant HERMES_RESOURCES_CONFIG.
    */
   public static final String HERMES_RESOURCES_CONFIG = "hermes.resources.dir";
   /**
    * The constant IDENTIFIER.
    */
   public static final String IDENTIFIER = oneOrMore(chars(LETTER,
                                                           DIGIT,
                                                           "_",
                                                           e('$'),
                                                           e('.')));
   /**
    * The constant LEXICON.
    */
   public static final String LEXICON = "lexicons";
   /**
    * The constant MODEL.
    */
   public static final String MODEL = "models";

   private Hermes() {
      throw new IllegalAccessError();
   }

   /**
    * Default corpus format string.
    *
    * @return the string
    */
   public static String defaultCorpusFormat() {
      return Config.get("hermes.defaultCorpusFormat")
                   .asString(HCF.toString());
   }

   public static Class<?> defaultImplementation(@NonNull Language language, @NonNull String resource) {
      return Reflect.getClassForNameQuietly(String.format("%s.%s.%s%s",
                                                          HERMES_PACKAGE,
                                                          language.getCode().toLowerCase(),
                                                          language.getCode(),
                                                          resource));
   }

   /**
    * Get the default language. The default language is specified using <code>com.gengoai.hermes.DefaultLanguage</code>.
    * If the configuration option is not set, it will default to the language matching the system locale.
    *
    * @return the default language
    */
   public static Language defaultLanguage() {
      return Config.get("hermes.defaultLanguageFormat")
                   .as(Language.class, Language.fromLocale(Locale.getDefault()));
   }

   private static String ensureExtension(String name, String type) {
      String extension = null;
      switch (type) {
         case LEXICON:
            extension = ".json";
            break;
         case MODEL:
            extension = ".model.bin";
            break;
      }
      if (extension != null && !name.toLowerCase().endsWith(extension)) {
         name += extension;
      }
      return name;
   }

   /**
    * <p>Common method for finding a resource of a given type</p>
    *
    * <p>The method will look in the following locations in order for the mode:
    * <ol>
    * <li>configProperty.{language}.{type}</li>
    * <li>configProperty.{type}</li>
    * <li>classpath:com.gengoai.hermes/{type}/{name}</li>
    * </ol>
    * where <code>language</code> is the two-letter (lowercased) language code.
    * </p>
    *
    * @param configProperty the config property
    * @param type           the resource type
    * @param language       the language of the model
    * @return the resource location or null
    */
   public static Optional<String> findConfig(@NonNull String configProperty,
                                             @NonNull String type,
                                             @NonNull Language language) {
      for (String r : new String[]{
         Config.get(configProperty, language, type).asString(),
         Config.get(configProperty, language).asString(),
         Config.get(configProperty, type).asString(),
         Config.get(configProperty).asString()
      }) {
         if (Strings.isNotNullOrBlank(r)) {
            return Optional.of(r);
         }
      }
      return Optional.empty();
   }

   /**
    * Find model resource resource.
    *
    * @param name           the name
    * @param configProperty the config property
    * @param language       the language
    * @return the resource
    */
   public static Optional<Resource> findModelResource(@NonNull String name,
                                                      @NonNull String configProperty,
                                                      @NonNull Language language) {
      return findResource(name, configProperty, MODEL, language);
   }

   public static Optional<Resource> findResource(@NonNull String name,
                                                 @NonNull String type,
                                                 @NonNull Language language) {
      String langCode = language.getCode().toLowerCase();
      Resource baseDir = Config.get(HERMES_RESOURCES_CONFIG)
                               .asResource(Resources.from(SystemInfo.USER_HOME)
                                                    .getChild("hermes"));

      Resource classpathDir = Resources.fromClasspath(HERMES_PACKAGE.replace('.', '/') + "/");
      for (Resource r : new Resource[]{
         baseDir.getChild(langCode).getChild(type).getChild(name),
         classpathDir.getChild(langCode).getChild(type).getChild(name),
         baseDir.getChild("default").getChild(type).getChild(name),
         classpathDir.getChild("default").getChild(type).getChild(name),
      }) {
         if (r != null && r.exists()) {
            return Optional.of(r);
         }
      }
      return Optional.empty();
   }

   /**
    * <p>Common method for finding a resource of a given type</p>
    *
    * <p>The method will look in the following locations in order for the mode:
    * <ol>
    * <li>configProperty.{language}.{type}</li>
    * <li>classpath:com.gengoai.hermes/{language}/{type}/{name}</li>
    * <li>modelDir/{language}/{type}/{name}</li>
    * <li>configProperty.{type}</li>
    * <li>classpath:com.gengoai.hermes/{type}/{name}</li>
    * <li>modelDir/{type}/{name}</li>
    * </ol>
    * where <code>language</code> is the two-letter (lowercased) language code.
    * </p>
    *
    * @param name           the resource name @param configProperty the config property to use for locating the model
    *                       location
    * @param configProperty the config property
    * @param type           the resource type
    * @param language       the language of the model
    * @return the resource location or null
    */
   public static Optional<Resource> findResource(@NonNull String name,
                                                 @NonNull String configProperty,
                                                 @NonNull String type,
                                                 @NonNull Language language) {
      String langCode = language.getCode().toLowerCase();
      Resource baseDir = Config.get(HERMES_RESOURCES_CONFIG).asResource(Resources.from(SystemInfo.USER_HOME)
                                                                                 .getChild("hermes"));
      Resource classpathDir = Resources.fromClasspath(HERMES_PACKAGE.replace('.','/') + "/");
      name = ensureExtension(name, type);
      for (Resource r : new Resource[]{
         Config.get(configProperty, language, type).asResource(),
         baseDir.getChild(langCode).getChild(type).getChild(name),
         classpathDir.getChild(langCode).getChild(type).getChild(name),
         Config.get(configProperty, language).asResource(),
         Config.get(configProperty).asResource(),
         Config.get(configProperty, type).asResource(),
         baseDir.getChild("default").getChild(type).getChild(name),
         classpathDir.getChild("default").getChild(type).getChild(name),
      }) {
         if (r != null && r.exists()) {
            return Optional.of(r);
         }
      }
      return Optional.empty();
   }

   private static <T> T loadModel(Language language,
                                  String configProperty,
                                  String modelName) {
      Exception thrownException;
      Optional<Resource> modelFile = findModelResource(modelName, configProperty, language);
      if (modelFile.isPresent()) {
         try {
            return modelFile.get().readObject();
         } catch (Exception e) {
            thrownException = e;
         }
      } else {
         thrownException = new RuntimeException(modelName + " does not exist");
      }
      throw new RuntimeException(thrownException);
   }

   /**
    * Model cache cache.
    *
    * @param <M>            the type parameter
    * @param size           the size
    * @param configProperty the config property
    * @param modelName      the model name
    * @return the cache
    */
   public static <M> Cache<Language, M> modelCache(int size, String configProperty, String modelName) {
      return Cache.create(size, language -> loadModel(language, configProperty, modelName));
   }

   /**
    * Model cache cache.
    *
    * @param <M>            the type parameter
    * @param configProperty the config property
    * @param modelName      the model name
    * @return the cache
    */
   public static <M> Cache<Language, M> modelCache(String configProperty, String modelName) {
      return Cache.create(Integer.MAX_VALUE, language -> loadModel(language, configProperty, modelName));
   }


}//END OF Hermes
