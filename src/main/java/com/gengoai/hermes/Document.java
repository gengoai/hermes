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

import com.gengoai.Language;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.collection.Collect;
import com.gengoai.collection.Sets;
import com.gengoai.collection.tree.Span;
import com.gengoai.conversion.Cast;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>The document is the central object class in the TIPSTER architecture. It serves as repository for Attributes and
 * Annotations. In the TIPSTER architecture a document is part of one or more collections and can only be accessed as a
 * member of that collection. In this architecture the document is independent of the collection, but  can be linked
 * back to the collection through an Attribute.</p>
 * <p>Documents are not normally constructed directly, instead they are built through the {@link DocumentFactory} which
 * takes care of normalizing and parsing the underlying text. Pre-tokenized text can be converted into a document using
 * the {@link DocumentFactory#fromTokens(Iterable)} method.</p>
 *
 * @author David B. Bracewell
 */

@JsonHandler(Document.DocumentMarshaller.class)
public interface Document extends HString {

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text the text content making up the document
    * @return the document
    */
   static Document create(String text) {
      return DocumentFactory.getInstance().create(text);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text     the text content making up the document
    * @param language the language of the content
    * @return the document
    */
   static Document create(String text, Language language) {
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
   static Document create(String text, Language language, Map<AttributeType<?>, ?> attributes) {
      return DocumentFactory.getInstance().create(text, language, attributes);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text       the text content making up the document
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   static Document create(String text, Map<AttributeType<?>, ?> attributes) {
      return DocumentFactory.getInstance().create(text, Hermes.defaultLanguage(), attributes);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id   the document id
    * @param text the text content making up the document
    * @return the document
    */
   static Document create(String id, String text) {
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
   static Document create(String id, String text, Language language) {
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
   static Document create(String id, String text, Language language, Map<AttributeType<?>, ?> attributes) {
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
   static Document create(String id, String text, Map<AttributeType<?>, ?> attributes) {
      return DocumentFactory.getInstance().create(id, text, Hermes.defaultLanguage(), attributes);
   }

   /**
    * Creates a document from a JSON representation (created by the write or toJson methods)
    *
    * @param jsonString the json string
    * @return the document
    */
   static Document fromJson(String jsonString) {
      try {
         return Json.parse(jsonString, Document.class);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Has annotations boolean.
    *
    * @param json  the json
    * @param types the types
    * @return the boolean
    */
   static boolean hasAnnotations(String json, AnnotatableType... types) {
      if(types.length == 0) {
         return true;
      }
      Set<AnnotatableType> target = Sets.asLinkedHashSet(Arrays.asList(types));
      Type mapType = new TypeToken<Map<String, Object>>() {
      }.getType();
      Gson gson = new Gson();
      Map<String, Object> rawDoc = gson.fromJson(json, mapType);
      return rawDoc.containsKey("completed") && Cast.<Map<String, Object>>as(rawDoc.get("completed"))
            .keySet()
            .stream()
            .map(AnnotatableType::valueOf)
            .collect(Collectors.toSet())
            .containsAll(target);
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
   List<Annotation> annotations(AnnotationType type, Span span, Predicate<? super Annotation> filter);

   void attach(@NonNull Annotation annotation);

   @Override
   default List<Annotation> children(String relation) {
      return annotations();
   }

   /**
    * Gets the set of completed AnnotatableType.
    *
    * @return the set of completed AnnotatableType
    */
   Set<AnnotatableType> completed();

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
   Annotation createAnnotation(@NonNull AnnotationType type,
                               int start,
                               int end,
                               @NonNull Map<AttributeType<?>, ?> attributeMap,
                               @NonNull List<Relation> relations);

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

   String getAnnotationProvider(AnnotatableType type);

   /**
    * Gets the id of the document
    *
    * @return The id of the document
    */
   String getId();

   /**
    * Sets the id of the document. If a null or blank id is given a random id will generated.
    *
    * @param id The new id of the document
    */
   void setId(String id);

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

   Annotation next(@NonNull Annotation annotation, @NonNull AnnotationType type);

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

   Annotation previous(@NonNull Annotation annotation, @NonNull AnnotationType type);

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

   void setCompleted(AnnotatableType type, String annotatorInformation);

   void setUncompleted(AnnotatableType type);

   /**
    * Converts the document to json
    *
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

      public AnnotationBuilder from(@NonNull HString hString) {
         return bounds(hString).attributes(hString)
                               .relation(hString.outgoingRelations());
      }

      public AnnotationBuilder relation(@NonNull Relation relation) {
         this.relations.add(relation);
         return this;
      }

      public AnnotationBuilder relation(@NonNull Iterable<Relation> relation) {
         Collect.addAll(this.relations, relation);
         return this;
      }

   }//END OF AnnotationBuilder

   /**
    * The type Document marshaller.
    */
   class DocumentMarshaller extends com.gengoai.json.JsonMarshaller<Document> {

      @Override
      protected Document deserialize(JsonEntry entry, Type type) {
         DefaultDocumentImpl document = new DefaultDocumentImpl(entry.getStringProperty("id"),
                                                                entry.getStringProperty("content"));

         if(entry.hasProperty("attributes")) {
            document.attributeMap().putAll(entry.getProperty("attributes").getAs(AttributeMap.class));
         }

         AtomicLong id = new AtomicLong(0);
         if(entry.hasProperty("completed")) {
            entry.getProperty("completed")
                 .propertyIterator()
                 .forEachRemaining(e -> {
                    AnnotatableType at = AnnotatableType.valueOf(e.getKey());
                    document.setCompleted(at, e.getValue().getAsString());
                 });
         }

         if(entry.hasProperty("annotations")) {
            entry.getProperty("annotations")
                 .elementIterator()
                 .forEachRemaining(ae -> {
                    Annotation annotation = new DefaultAnnotationImpl(document,
                                                                      ae.getProperty("type")
                                                                        .getAs(AnnotationType.class),
                                                                      ae.getIntProperty("start"),
                                                                      ae.getIntProperty("end"));
                    long aid = ae.getLongProperty("id");
                    annotation.setId(aid);
                    if(aid > id.get()) {
                       id.set(aid + 1);
                    }
                    if(ae.hasProperty("attributes")) {
                       annotation.attributeMap().putAll(ae.getProperty("attributes").getAs(AttributeMap.class));
                    }
                    if(ae.hasProperty("relations")) {
                       annotation.addAll(ae.getProperty("relations")
                                           .getAsArray(Relation.class));
                    }
                    document.annotationSet.add(annotation);
                 });
         }

         document.idGenerator.set(id.get());

         return document;
      }

      @Override
      protected JsonEntry serialize(Document document, Type type) {
         JsonEntry entry = JsonEntry.object()
                                    .addProperty("id", document.getId())
                                    .addProperty("content", document.toString())
                                    .addProperty("attributes", document.attributeMap());

         Set<AnnotatableType> completedTypes = document.completed();
         if(completedTypes.size() > 0) {
            JsonEntry completed = JsonEntry.object();
            for(AnnotatableType annotatableType : completedTypes) {
               completed.addProperty(annotatableType.canonicalName(), document.getAnnotationProvider(annotatableType));
            }
            entry.addProperty("completed", completed);
         }

         if(document.numberOfAnnotations() > 0) {
            final JsonEntry annotations = JsonEntry.array();
            document.annotationStream().forEach(annotation -> {
               JsonEntry aj = JsonEntry.object()
                                       .addProperty("id", annotation.getId())
                                       .addProperty("type", annotation.getType().name())
                                       .addProperty("start", annotation.start())
                                       .addProperty("end", annotation.end());
               if(annotation.attributeMap().size() > 0) {
                  aj.addProperty("attributes", annotation.attributeMap());
               }
               if(annotation.outgoingRelations(false).size() > 0) {
                  aj.addProperty("relations", annotation.outgoingRelations(false));
               }
               annotations.addValue(aj);
            });
            entry.addProperty("annotations", annotations);
         }

         return entry;
      }
   }
}//END OF Document
