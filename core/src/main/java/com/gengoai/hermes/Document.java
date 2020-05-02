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

package com.gengoai.hermes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.Language;
import com.gengoai.collection.Collect;
import com.gengoai.collection.tree.Span;
import com.gengoai.json.Json;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

/**
 * <p>
 * A document represents text content with an accompanying set of metadata (Attributes), linguistic overlays
 * (Annotations), and relations between elements in the document. Documents are represented as {@link HString} with
 * additional  methods for adding, removing, and accessing the annotations over its content. Every document has an id
 * associated with it, which should be unique within a corpus.
 * </p>
 *
 * <p>
 * Documents are created using a {@link DocumentFactory}, which defines the preprocessing (e.g whitespace and unicode
 * normalization) steps (TextNormalizers) to be performed on raw text before creating a document and the default
 * language with which the documents are written. Additionally, the Document class provides a number of convenience
 * <code>create</code> methods for constructing documents using the default DocumentFactory instance.
 * </p>
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(as = DefaultDocumentImpl.class)
public interface Document extends HString {

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text the text content making up the document
    * @return the document
    */
   static Document create(@NonNull String text) {
      return DocumentFactory.getInstance().create(text);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text     the text content making up the document
    * @param language the language of the content
    * @return the document
    */
   static Document create(@NonNull String text, @NonNull Language language) {
      return DocumentFactory.getInstance().create(text, language);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text       the text content making up the document
    * @param language   the language of the content
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   static Document create(@NonNull String text,
                          @NonNull Language language,
                          @NonNull Map<AttributeType<?>, ?> attributes) {
      return DocumentFactory.getInstance().create(text, language, attributes);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text       the text content making up the document
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   static Document create(@NonNull String text, @NonNull Map<AttributeType<?>, ?> attributes) {
      return DocumentFactory.getInstance().create(text, Hermes.defaultLanguage(), attributes);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id   the document id
    * @param text the text content making up the document
    * @return the document
    */
   static Document create(@NonNull String id, @NonNull String text) {
      return DocumentFactory.getInstance().create(id, text);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id       the document id
    * @param text     the text content making up the document
    * @param language the language of the content
    * @return the document
    */
   static Document create(@NonNull String id, @NonNull String text, @NonNull Language language) {
      return DocumentFactory.getInstance().create(id, text, language);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id         the document id
    * @param text       the text content making up the document
    * @param language   the language of the content
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   static Document create(@NonNull String id,
                          @NonNull String text,
                          @NonNull Language language,
                          @NonNull Map<AttributeType<?>, ?> attributes) {
      return DocumentFactory.getInstance().create(id, text, language, attributes);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id         the document id
    * @param text       the text content making up the document
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   static Document create(@NonNull String id, @NonNull String text, @NonNull Map<AttributeType<?>, ?> attributes) {
      return DocumentFactory.getInstance().create(id, text, Hermes.defaultLanguage(), attributes);
   }

   /**
    * Creates a document from a JSON representation (created by the write or toJson methods)
    *
    * @param jsonString the json string
    * @return the document
    */
   static Document fromJson(@NonNull String jsonString) {
      try {
         return Json.parse(jsonString, Document.class);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Convenience method for annotating the document with the given annotatable types.
    *
    * @param types the types to annotate
    */
   void annotate(AnnotatableType... types);

   /**
    * Gets an annotation on the document by its ID.
    *
    * @param id the id of the annotation to retrieve
    * @return the annotation
    */
   Annotation annotation(long id);

   /**
    * Creates an annotation builder for adding annotations to the document.
    *
    * @return the annotation builder
    */
   default AnnotationBuilder annotationBuilder(@NonNull AnnotationType type) {
      return new AnnotationBuilder(this, type);
   }

   /**
    * Gets annotations of the given type that overlap with the given span.
    *
    * @param type the type of annotation
    * @param span the span to search for overlapping annotations
    * @return All annotations of the given type on the document that overlap with the give span.
    */
   List<Annotation> annotations(AnnotationType type, Span span);

   /**
    * Gets annotations of the given type that overlap with the given span and meet the given filter.
    *
    * @param type   the type of annotation
    * @param span   the span to search for overlapping annotations
    * @param filter the filter to use on the annotations
    * @return All annotations of the given type on the document that overlap with the give span and meet the given
    * filter.
    */
   List<Annotation> annotations(AnnotationType type,
                                Span span,
                                Predicate<? super Annotation> filter);

   /**
    * Attaches the given annotation to the document.
    *
    * @param annotation The annotation to attach to the document.
    */
   void attach(Annotation annotation);

   @Override
   default List<Annotation> children(@NonNull String relation) {
      return annotations();
   }

   /**
    * Gets the set of completed AnnotatableType on this document.
    *
    * @return the set of completed AnnotatableType
    */
   Set<AnnotatableType> completed();

   /**
    * Determines if the given annotation is attached to this document.
    *
    * @param annotation The annotation to check
    * @return True if this annotation is attached to this document, false otherwise.
    */
   boolean contains(Annotation annotation);

   /**
    * Creates an annotation of the given type encompassing the given span and having the given attributes. The
    * annotation is added to the document and has a unique id assigned.
    *
    * @param type         the type of annotation
    * @param start        the start of the span
    * @param end          the end of the span
    * @param attributeMap the attributes associated with the annotation
    * @param relations    the relations to add on the annotation
    * @return the created annotation
    */
   Annotation createAnnotation(AnnotationType type,
                               int start,
                               int end,
                               Map<AttributeType<?>, ?> attributeMap,
                               List<Relation> relations);

   /**
    * Creates an annotation of the given type encompassing the given span and having the given attributes. The
    * annotation is added to the document and has a unique id assigned.
    *
    * @param type         the type of annotation
    * @param start        the start of the span
    * @param end          the end of the span
    * @param attributeMap the attributes associated with the annotation
    * @return the created annotation
    */
   Annotation createAnnotation(AnnotationType type,
                               int start,
                               int end,
                               Map<AttributeType<?>, ?> attributeMap);

   @Override
   default Document document() {
      return this;
   }

   @Override
   default List<Annotation> enclosedAnnotations() {
      return annotations();
   }

   /**
    * Gets the provider for the given AnnotatableType when that type is completed on the document.
    *
    * @param type the annotatable type whose provider we want
    * @return The provider of the given annotatable type
    */
   String getAnnotationProvider(AnnotatableType type);

   /**
    * Gets the id of the document
    *
    * @return The id of the document
    */
   String getId();

   @Override
   default Language getLanguage() {
      if(hasAttribute(Types.LANGUAGE)) {
         return attribute(Types.LANGUAGE);
      }
      return Hermes.defaultLanguage();
   }

   @Override
   default List<Annotation> incoming(RelationType type, String value, boolean includeSubAnnotations) {
      return Collections.emptyList();
   }

   @Override
   default List<Annotation> incoming(RelationType type, boolean includeSubAnnotations) {
      return Collections.emptyList();
   }

   @Override
   default List<Relation> incomingRelations(boolean includeSubAnnotations) {
      return Collections.emptyList();
   }

   @Override
   default List<Relation> incomingRelations(RelationType relationType, boolean includeSubAnnotations) {
      return Collections.emptyList();
   }

   /**
    * Checks is if a given {@link AnnotatableType} is completed, i.e. been added by an annotator.
    *
    * @param type the type to check
    * @return True if the type is complete, False if not
    */
   boolean isCompleted(AnnotatableType type);

   @Override
   default boolean isDocument() {
      return true;
   }

   /**
    * Determines the next annotation of the given type after the given annotation (e.g. what is the token after the
    * current token)
    *
    * @param annotation The current annotation.
    * @param type       The type of annotation we want to find after the current annotation.
    * @return The annotation of the given type after the current annotation or an empty HString if there are none.
    */
   Annotation next(Annotation annotation, AnnotationType type);

   /**
    * @return The number of annotations on the document
    */
   int numberOfAnnotations();

   @Override
   default List<Annotation> outgoing(RelationType type, boolean includeSubAnnotations) {
      return Collections.emptyList();
   }

   @Override
   default List<Annotation> outgoing(RelationType type, String value, boolean includeSubAnnotations) {
      return Collections.emptyList();
   }

   @Override
   default List<Relation> outgoingRelations(boolean includeSubAnnotations) {
      return Collections.emptyList();
   }

   @Override
   default List<Relation> outgoingRelations(@NonNull RelationType relationType, boolean includeSubAnnotations) {
      return Collections.emptyList();
   }

   /**
    * Determines the previous annotation of the given type after the given annotation (e.g. what is the token before the
    * current token)
    *
    * @param annotation The current annotation.
    * @param type       The type of annotation we want to find before the current annotation.
    * @return The annotation of the given type the the current annotation or an empty HString if there are none.
    */
   Annotation previous(Annotation annotation, AnnotationType type);

   @JsonProperty("completed")
   Map<AnnotatableType, String> providers();

   /**
    * Removes the given annotation from the document
    *
    * @param annotation the annotation to remove
    * @return True if the annotation was successfully removed, False otherwise
    */
   boolean remove(Annotation annotation);

   /**
    * Removes all annotations of a given type.
    *
    * @param type the type of to remove
    */
   void removeAnnotationType(AnnotationType type);

   /**
    * Marks the given AnnotatableType as being completed by the given provider.
    *
    * @param type     The AnnotatableType to mark as completed.
    * @param provider The provided that satisfied the given AnnotatableType
    */
   void setCompleted(AnnotatableType type, String provider);

   /**
    * Sets the id of the document. If a null or blank id is given a random id will generated.
    *
    * @param id The new id of the document
    */
   void setId(String id);

   /**
    * Marks the given AnnotatableType as not being completed. Useful for reannotating for a given type.
    *
    * @param type The AnnotatableType to mark as uncompleted.
    */
   void setUncompleted(AnnotatableType type);

   /**
    * @return JSON representation of the document
    */
   default String toJson() {
      return Json.dumps(this);
   }

   /**
    * Annotation builder for creating annotations associated with a document
    */
   @Data
   @Accessors(fluent = true)
   class AnnotationBuilder {
      private final Document document;
      private final List<Relation> relations = new ArrayList<>();
      private final AnnotationType type;
      private Map<AttributeType<?>, Object> attributes = new HashMap<>();
      private int end = -1;
      private int start = -1;

      /**
       * Instantiates a new Annotation builder.
       *
       * @param document the document
       */
      AnnotationBuilder(Document document, AnnotationType type) {
         this.document = document;
         this.type = type;
      }


      /**
       * Sets the value of the given AttributeType on the new Annotation to the given value.
       *
       * @param type  the attribute type
       * @param value the attribute value
       * @return this annotation builder
       */
      public AnnotationBuilder attribute(AttributeType type, Object value) {
         this.attributes.put(type, value);
         return this;
      }

      /**
       * Adds multiple attributes to the annotation
       *
       * @param map the map of attribute types and values
       * @return this annotation builder
       */
      public AnnotationBuilder attributes(Map<AttributeType<?>, ?> map) {
         this.attributes.putAll(map);
         return this;
      }

      /**
       * Adds attributes to this annotation by copying the attributes of another HString object.
       *
       * @param copy the HString object whose attributes will be copied
       * @return this annotation builder
       */
      public AnnotationBuilder attributes(HString copy) {
         this.attributes.putAll(copy.attributeMap());
         return this;
      }

      /**
       * Sets the bounds of this annotation from the given span
       *
       * @param span the span to use for the bounds of the annotation
       * @return this annotation builder
       */
      public AnnotationBuilder bounds(Span span) {
         this.start = span.start();
         this.end = span.end();
         return this;
      }

      /**
       * Creates the annotation and attaches it to the document
       *
       * @return the annotation
       */
      public Annotation createAttached() {
         return document.createAnnotation(type, start, end, attributes, relations);
      }

      /**
       * Creates the annotation associated, but not attached, to the document
       *
       * @return the annotation
       */
      public Annotation createDetached() {
         Annotation annotation = new DefaultAnnotationImpl(document, type, start, end);
         annotation.putAll(attributes);
         annotation.addAll(relations);
         return annotation;
      }

      /**
       * Sets the bounds, and adds the relations and attributes from the given HString to this builder.
       *
       * @param hString The HString to copy from
       * @return This builder
       */
      public AnnotationBuilder from(@NonNull HString hString) {
         return bounds(hString).attributes(hString)
                               .relation(hString.outgoingRelations());
      }

      /**
       * Adds the given relation.
       *
       * @param relation The relation to add
       * @return This builder
       */
      public AnnotationBuilder relation(@NonNull Relation relation) {
         this.relations.add(relation);
         return this;
      }

      /**
       * Adds the given iterable of relations.
       *
       * @param relations The relations to add
       * @return This builder
       */
      public AnnotationBuilder relation(@NonNull Iterable<Relation> relations) {
         Collect.addAll(this.relations, relations);
         return this;
      }

   }//END OF AnnotationBuilder

}//END OF Document
