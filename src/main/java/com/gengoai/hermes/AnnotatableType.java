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

import com.gengoai.HierarchicalEnumValue;
import com.gengoai.Language;
import com.gengoai.LogUtils;
import com.gengoai.Validation;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.annotator.Annotator;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.BeanUtils;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.lang.reflect.Type;

import static com.gengoai.LogUtils.logFine;
import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.hermes.Hermes.HERMES_PACKAGE;

/**
 * <p> An annotatable type is one that can be added to a document through annotation either by {@link
 * com.gengoai.hermes.corpus.Corpus#annotate(AnnotatableType...)} or {@link Document#annotate(AnnotatableType...)}. The
 * interface exists to unify {@link AnnotationType}s, {@link AttributeType}s, and {@link RelationType}s.</p>
 *
 * @author David B. Bracewell
 */
@JsonHandler(AnnotatableType.Marshaller.class)
public interface AnnotatableType {
   /**
    * Package to look for default annotator implementations.
    */
   String ANNOTATOR_PACKAGE = HERMES_PACKAGE + ".annotator";

   /**
    * Determines the correct AnnotatableType from the given name. The name should be prepended with either the class
    * name or shorthand name of the AnnotatableType.
    *
    * @param name the name
    * @return the AnnotatableType
    */
   static AnnotatableType valueOf(String name) {
      int index = name.lastIndexOf('.');
      if(index > 0) {
         String typeName = name.substring(0, index);
         name = name.substring(index + 1);
         switch(typeName) {
            case "AnnotationType":
            case "Annotation":
               return AnnotationType.make(name);
            case "RelationType":
            case "Relation":
               return RelationType.make(name);
            case "AttributeType":
            case "Attribute":
               return AttributeType.make(name);
         }
         //Backward support for fully qualified names
         Class<?> c = Reflect.getClassForNameQuietly(typeName);
         if(AnnotationType.class == c) {
            return AnnotationType.make(name);
         }
         if(RelationType.class == c) {
            return RelationType.make(name);
         }
         if(AttributeType.class == c) {
            return AttributeType.make(name);
         }
      }
      if(AnnotationType.isDefined(name)) {
         return AnnotationType.make(name);
      }
      if(AttributeType.isDefined(name)) {
         return AttributeType.make(name);
      }
      if(RelationType.isDefined(name)) {
         return RelationType.make(name);
      }
      throw new IllegalStateException("Unable to determine type of " + name);
   }

   /**
    * The canonical name of the type (typically in the form of <code>PackageName.ClassName.Name</code>)
    *
    * @return the canonical form of the name
    */
   String canonicalName();

   /**
    * Gets the annotator associated with this type for a given language. First, an annotator is checked for in the
    * config using <code>Type.Language.Name.Annotator</code> where the language is optional. If not found, it will then
    * check for classes that meet common conventions with class names of <code>Default[Language][Type]Annotator</code>
    * or <code>Default[Type]Annotator</code>, where type is is camel-cased and non-alphabetic characters removed (e.g.
    * <code>MY_ENTITY</code> would become MyEntity) and the class is expected be in the package
    * <code>com.gengoai.com.gengoai.hermes.annotator</code>
    *
    * @param language the language for which the annotator is needed.
    * @return the annotator for this type and the given language
    * @throws IllegalStateException If no annotator is defined or the defined annotator does not satisfy this type.
    */
   default Annotator getAnnotator(@NonNull Language language) {
      String leaf = (this instanceof HierarchicalEnumValue)
                    ? Cast.<HierarchicalEnumValue>as(this).label()
                    : name();
      //Step 1: Check for a config override
      String key = Config.findKey(type(), language, leaf, "annotator");
      Annotator annotator = null;


      if(Strings.isNotNullOrBlank(key)) {
         //Annotator is defined via configuration (this will override defaults)
         annotator = Config.get(key).as(Annotator.class);
      } else {
         //Check for annotator using convention of Default[LANGUAGE]?[TypeName]Annotator
         //This only works for annotators in the package "com.gengoai.com.gengoai.hermes.annotator"
         String typeName = Strings.toTitleCase(leaf.replaceAll("[^a-zA-Z]", " ")
                                                   .trim()
                                                   .toLowerCase()).replaceAll("\\s+", "");
         String languageName = Strings.toTitleCase(language.name().toLowerCase());

         Class<?> annotatorClass = null;
         for(String candidate : arrayOf(
               HERMES_PACKAGE + "." + language.getCode().toLowerCase() + "." + language.getCode()
                                                                                       .toUpperCase() + typeName + "Annotator",
               ANNOTATOR_PACKAGE + ".Default" + languageName + typeName + "Annotator",
               ANNOTATOR_PACKAGE + ".Default" + typeName + "Annotator")) {
            annotatorClass = Reflect.getClassForNameQuietly(candidate);
            if(annotatorClass != null) {
               break;
            }
         }

         if(annotatorClass != null) {
            try {
               annotator = Cast.as(BeanUtils.getBean(annotatorClass));
            } catch(ReflectionException e) {
               logFine(LogUtils.getGlobalLogger(), e);
            }
         }
      }

      if(annotator == null) {
         throw new IllegalStateException("No annotator is defined for " + leaf + " and " + language);
      }

      Validation.checkState(annotator.satisfies().contains(this),
                            "Attempting to register " + annotator.getClass()
                                                                 .getName() + " for " + leaf + " which it does not provide.");
      return annotator;
   }

   /**
    * The annotatable type's name (e.g. TOKEN, PART_OF_SPEECH)
    *
    * @return the name
    */
   String name();

   /**
    * The type (Annotation, Attribute, Relation)
    *
    * @return the type
    */
   String type();

   /**
    * Json Marshaller
    */
   class Marshaller extends com.gengoai.json.JsonMarshaller<AnnotatableType> {

      @Override
      protected AnnotatableType deserialize(JsonEntry entry, Type type) {
         return valueOf(entry.getAsString());
      }

      @Override
      protected JsonEntry serialize(AnnotatableType annotatableType, Type type) {
         return JsonEntry.from(annotatableType.name());
      }
   }

}//END OF Annotatable
