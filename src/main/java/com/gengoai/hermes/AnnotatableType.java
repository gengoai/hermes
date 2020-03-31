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
    * <p>Determines the correct AnnotatableType from the given name. Checks are made in the following order:</p>
    * <ol>
    *    <li>ShorthandTypeName.VALUE<br/>
    *    e.g. Annotation.ENTITY
    *    </li>
    *    <li>FullyQualifiedClassName.VALUE<br/>
    *    e.g. com.gengoai.hermes.morphology.POS.NOUN
    *    </li>
    *    <li>Already defined name in the order:<br/>
    *       (1) Annotation (2) Attribute (3) Relation
    *    </li>
    * </ol>
    *
    * @param name the name
    * @return the AnnotatableType
    */
   static AnnotatableType valueOf(String name) {
      Validation.notNullOrBlank(name);

      //Find the last period for names given in the form of (ClassName|ShortHand).NAME
      int index = name.lastIndexOf('.');
      if(index > 0) {
         //Split the name into the typeName (e.g. Annotation) and the valueName (e.g. ENTITY)
         String typeName = name.substring(0, index);
         String valueName = name.substring(index + 1);

         //Check for Shorthand notation
         switch(typeName) {
            case "AnnotationType":
            case "Annotation":
               return AnnotationType.make(valueName);
            case "RelationType":
            case "Relation":
               return RelationType.make(valueName);
            case "AttributeType":
            case "Attribute":
               return AttributeType.make(valueName);
         }

         //Check for a fully qualified class name
         Class<?> c = Reflect.getClassForNameQuietly(typeName);
         if(AnnotationType.class == c) {
            return AnnotationType.make(valueName);
         }
         if(RelationType.class == c) {
            return RelationType.make(valueName);
         }
         if(AttributeType.class == c) {
            return AttributeType.make(valueName);
         }
      }

      //Finally checked if given name is already defined and return it in the order
      // (1) Annotation (2) Attribute (3) Relation
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
    * config using <code>Type.Language.Name.Annotator</code> (e.g. <code>Annotation.ENGLISH.ENTITY.Annotator</code>
    * where the language is optional. If not found, it will then check for classes that meet common conventions in the
    * following order:
    * <ol>
    *    <li>com.gengoai.hermes.[LANG_CODE_LOWER].[LANG_CODE_UPPER][TYPE_NAME_TITLE_CASE][ANNOTATOR], e.g.
    *    com.gengoai.hermes.en.ENEntityAnnotator
    *    </li>
    *    <li>com.gengoai.hermes.annotator.Default[LANG_NAME_TITLE_CASE][TYPE_NAME_TITLE_CASE][ANNOTATOR], e.g.
    *    com.gengoai.hermes.annotator.DefaultEnglishEntityAnnotator
    *    </li>
    *    <li>com.gengoai.hermes.annotator.Default[TYPE_NAME_TITLE_CASE][ANNOTATOR], e.g.
    *    com.gengoai.hermes.annotator.DefaultEntityAnnotator
    *    </li>
    * </ol>
    *
    * @param language the language for which the annotator is needed.
    * @return the annotator for this type and the given language
    * @throws IllegalStateException If no annotator is defined or the defined annotator does not satisfy this type.
    */
   default Annotator getAnnotator(@NonNull Language language) {
      String leaf = (this instanceof HierarchicalEnumValue)
                    ? Cast.<HierarchicalEnumValue<?>>as(this).label()
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
    * @return The annotatable type's name (e.g. TOKEN, PART_OF_SPEECH)
    */
   String name();

   /**
    * @return The type (Annotation, Attribute, Relation)
    */
   String type();

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
