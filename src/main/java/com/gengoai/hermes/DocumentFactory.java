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
import com.gengoai.collection.tree.Span;
import com.gengoai.hermes.preprocessing.TextNormalization;
import com.gengoai.hermes.preprocessing.TextNormalizer;
import com.gengoai.string.Strings;
import lombok.Builder;

import java.io.Serializable;
import java.util.*;

/**
 * <p>A document factory facilitates the creation of  document objects. It handles performing any predefined
 * preprocessing and helps in determining the type of document being read. A default factory can be obtained by calling
 * {@link #getInstance()} or a factory can be built using a {@link Builder} constructed using {@link #builder()}.
 * </p>
 * <p>
 * The default factory uses configuration settings to determine the default language and preprocessing normalizers. The
 * default language is defined using the <code>com.gengoai.hermes.DefaultLanguage</code> configuration property and the normalizers
 * are defined using the <code>com.gengoai.hermes.preprocessing.normalizers</code> configuration property.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class DocumentFactory implements Serializable {
   private static final long serialVersionUID = 1L;

   private static volatile DocumentFactory CONFIGURED_INSTANCE;
   private final Language defaultLanguage;
   private final TextNormalization normalizer;


   private DocumentFactory() {
      this.normalizer = TextNormalization.configuredInstance();
      this.defaultLanguage = Hermes.defaultLanguage();
   }


   private DocumentFactory(Set<? extends TextNormalizer> normalizers, Language defaultLanguage) {
      this.normalizer = TextNormalization.createInstance(normalizers);
      this.defaultLanguage = (defaultLanguage == null) ? Hermes.defaultLanguage() : defaultLanguage;
   }

   /**
    * Builder document factory builder.
    *
    * @return the document factory builder
    */
   public static DocumentFactoryBuilder builder() {
      return new DocumentFactoryBuilder();
   }

   /**
    * Gets instance of the document factory configured using configuration settings.
    *
    * @return A document factory whose preprocessors are set via configuration options
    */
   public static DocumentFactory getInstance() {
      if (CONFIGURED_INSTANCE == null) {
         synchronized (DocumentFactory.class) {
            if (CONFIGURED_INSTANCE == null) {
               CONFIGURED_INSTANCE = new DocumentFactory();
            }
         }
      }
      return CONFIGURED_INSTANCE;
   }

   /**
    * Creates a document with the given content.
    *
    * @param content the content
    * @return the document
    */
   public Document create(String content) {
      return create(Strings.EMPTY, content, defaultLanguage, Collections.emptyMap());
   }

   /**
    * Creates a document with the given id and content
    *
    * @param id      the id
    * @param content the content
    * @return the document
    */
   public Document create(String id, String content) {
      return create(id, content, defaultLanguage, Collections.emptyMap());
   }

   /**
    * Creates a document with the given content written in the given language.
    *
    * @param content  the content
    * @param language the language
    * @return the document
    */
   public Document create(String content, Language language) {
      return create(Strings.EMPTY, content, language, Collections.emptyMap());
   }

   /**
    * Creates a document with the given id and content written in the given language.
    *
    * @param id       the id
    * @param content  the content
    * @param language the language
    * @return the document
    */
   public Document create(String id, String content, Language language) {
      return create(id, content, language, Collections.emptyMap());
   }

   /**
    * Creates a document with the given content written in the given language having the given set of attributes.
    *
    * @param content      the content
    * @param language     the language
    * @param attributeMap the attribute map
    * @return the document
    */
   public Document create(String content, Language language, Map<AttributeType<?>, ?> attributeMap) {
      return create("", content, language, attributeMap);
   }

   /**
    * Creates a document with the given id and content written in the given language having the given set of
    * attributes.
    *
    * @param id           the id
    * @param content      the content
    * @param language     the language
    * @param attributeMap the attribute map
    * @return the document
    */
   public Document create(String id, String content, Language language, Map<AttributeType<?>, ?> attributeMap) {
      Document document = new DefaultDocumentImpl(id, normalizer.normalize(content, language), language);
      document.putAll(attributeMap);
      document.setLanguage(language);
      return document;
   }

   /**
    * Creates a document with the given id and content written in the given language having the given set of attributes.
    * This method does not apply any {@link TextNormalizer}
    *
    * @param id           the id
    * @param content      the content
    * @param language     the language
    * @param attributeMap the attribute map
    * @return the document
    */
   public Document createRaw(String id, String content, Language language, Map<AttributeType<?>, ?> attributeMap) {
      Document document = new DefaultDocumentImpl(id, content, language);
      document.putAll(attributeMap);
      document.setLanguage(language);
      return document;
   }

   /**
    * Creates a document with the given content written in the default language.  This method does not apply any {@link
    * TextNormalizer}**
    *
    * @param content the content
    * @return the document
    */
   public Document createRaw(String content) {
      return createRaw(Strings.EMPTY, content, defaultLanguage, Collections.emptyMap());
   }

   /**
    * Creates a document with the given id and content written in the default language. This method does not apply any
    * {@link TextNormalizer}
    *
    * @param id      the id
    * @param content the content
    * @return the document
    */
   public Document createRaw(String id, String content) {
      return createRaw(id, content, defaultLanguage, Collections.emptyMap());
   }

   /**
    * Creates a document with the given content written in the given language. This method does not apply any {@link
    * TextNormalizer}**
    *
    * @param content  the content
    * @param language the language
    * @return the document
    */
   public Document createRaw(String content, Language language) {
      return createRaw("", content, language, Collections.emptyMap());
   }

   /**
    * Creates a document with the given id and content written in the given language. This method does not apply any
    * {@link TextNormalizer}
    *
    * @param id       the id
    * @param content  the content
    * @param language the language
    * @return the document
    */
   public Document createRaw(String id, String content, Language language) {
      return createRaw(id, content, language, Collections.emptyMap());
   }

   /**
    * Creates a document with the given content written in the given language having the given set of attributes. This
    * method does not apply any {@link TextNormalizer}
    *
    * @param content      the content
    * @param language     the language
    * @param attributeMap the attribute map
    * @return the document
    */
   public Document createRaw(String content, Language language, Map<AttributeType<?>, ?> attributeMap) {
      return createRaw("", content, language, attributeMap);
   }

   /**
    * Creates a document from the given tokens. The language parameter controls how the content of the documents is
    * created. If the language has whitespace tokens are joined with a single space between them, otherwise no space is
    * inserted between tokens.
    *
    * @param language the language of the document
    * @param tokens   the tokens making up the document
    * @return the document with tokens provided.
    */
   public Document fromTokens(Language language, String... tokens) {
      return fromTokens(Arrays.asList(tokens), language);
   }

   /**
    * Creates a document from the given tokens using the default language.
    *
    * @param tokens the tokens
    * @return the document
    */
   public Document fromTokens(String... tokens) {
      return fromTokens(Arrays.asList(tokens), getDefaultLanguage());
   }

   /**
    * Creates a document from the given tokens using the default language.
    *
    * @param tokens the tokens
    * @return the document
    */
   public Document fromTokens(Iterable<String> tokens) {
      return fromTokens(tokens, getDefaultLanguage());
   }

   /**
    * Creates a document from the given tokens. The language parameter controls how the content of the documents is
    * created. If the language has whitespace tokens are joined with a single space between them, otherwise no space is
    * inserted between tokens.
    *
    * @param tokens   the tokens
    * @param language the language
    * @return the document
    */
   public Document fromTokens(Iterable<String> tokens, Language language) {
      StringBuilder content = new StringBuilder();
      List<Span> tokenSpans = new ArrayList<>();
      for (String token : tokens) {
         tokenSpans.add(Span.of(content.length(), content.length() + token.length()));
         content.append(token);
         if (language.usesWhitespace()) {
            content.append(" ");
         }
      }
      Document doc = new DefaultDocumentImpl(null, content.toString().trim(), defaultLanguage);
      for (int idx = 0; idx < tokenSpans.size(); idx++) {
         doc.annotationBuilder(Types.TOKEN)
            .bounds(tokenSpans.get(idx))
            .attribute(Types.INDEX, idx).createAttached();
      }
      doc.setCompleted(Types.TOKEN, "PROVIDED");
      return doc;
   }

   /**
    * Gets the default language of the document factory.
    *
    * @return the default language
    */
   public Language getDefaultLanguage() {
      return defaultLanguage;
   }

   /**
    * Builder for {@link DocumentFactory}s
    */
   public static class DocumentFactoryBuilder {
      private final Set<TextNormalizer> normalizers = new HashSet<>();
      private Language defaultLanguage = Hermes.defaultLanguage();

      /**
       * Instantiates a new Document factory builder.
       */
      DocumentFactoryBuilder() {
      }

      /**
       * Build document factory.
       *
       * @return the document factory
       */
      public DocumentFactory build() {
         return new DocumentFactory(normalizers, defaultLanguage);
      }

      /**
       * Clear normalizers document factory builder.
       *
       * @return the document factory builder
       */
      public DocumentFactoryBuilder clearNormalizers() {
         this.normalizers.clear();
         return this;
      }

      /**
       * Default language document factory builder.
       *
       * @param defaultLanguage the default language
       * @return the document factory builder
       */
      public DocumentFactoryBuilder defaultLanguage(Language defaultLanguage) {
         this.defaultLanguage = defaultLanguage;
         return this;
      }

      /**
       * Normalizer document factory builder.
       *
       * @param normalizer the normalizer
       * @return the document factory builder
       */
      public DocumentFactoryBuilder normalizer(TextNormalizer normalizer) {
         this.normalizers.add(normalizer);
         return this;
      }

      /**
       * Normalizers document factory builder.
       *
       * @param normalizers the normalizers
       * @return the document factory builder
       */
      public DocumentFactoryBuilder normalizers(Collection<? extends TextNormalizer> normalizers) {
         this.normalizers.addAll(normalizers);
         return this;
      }

      public String toString() {
         return "DocumentFactory.DocumentFactoryBuilder(normalizers=" + this.normalizers + ", defaultLanguage=" + this.defaultLanguage + ")";
      }
   }
}//END OF DocumentFactory
