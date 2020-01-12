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
import com.gengoai.StringTag;
import com.gengoai.annotation.Preload;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.hermes.ner.EntityType;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * <p>Common Annotation Types. The predefined types are pre-loaded on initialization.</p>
 *
 * @author David B. Bracewell
 */
@Preload
public interface Types {
   /**
    * Document author
    */
   AttributeType<String> AUTHOR = AttributeType.make("AUTHOR", String.class);
   /**
    * The constant CADUCEUS_RULE.
    */
   AttributeType<String> CADUCEUS_RULE = AttributeType.make("CADUCEUS_RULE", String.class);
   /**
    * Document CATEGORY
    */
   AttributeType<Set<String>> CATEGORY = AttributeType.make("CATEGORY", parameterizedType(Set.class, String.class));
   /**
    * Confidence value associated with an annotation
    */
   AttributeType<Double> CONFIDENCE = AttributeType.make("CONFIDENCE", double.class);
   /**
    * The constant DEPENDENCY.
    */
   RelationType DEPENDENCY = RelationType.make("DEPENDENCY");
   /**
    * The constant ENTITY_TYPE.
    */
   AttributeType<EntityType> ENTITY_TYPE = AttributeType.make("ENTITY_TYPE", EntityType.class);
   /**
    * Entity annotation type
    */
   AnnotationType ENTITY = AnnotationType.make("ENTITY", ENTITY_TYPE);
   /**
    * File used to make the document
    */
   AttributeType<String> FILE = AttributeType.make("FILE", String.class);
   /**
    * The index of a span with regards to a document
    */
   AttributeType<Integer> INDEX = AttributeType.make("INDEX", int.class);
   /**
    *
    */
   AttributeType<List<String>> KEYWORDS = AttributeType.make("KEYWORDS", parameterizedType(List.class, String.class));
   /**
    * The Language associated with a span
    */
   AttributeType<Language> LANGUAGE = AttributeType.make("LANGUAGE", Language.class);
   /**
    * The lemma version of a span
    */
   AttributeType<String> LEMMA = AttributeType.make("LEMMA", String.class);
   /**
    * lexicon match annotation type
    */
   AnnotationType LEXICON_MATCH = AnnotationType.make("LEXICON_MATCH");
   /**
    * The constant MATCHED_STRING.
    */
   AttributeType<String> MATCHED_STRING = AttributeType.make("MATCHED_STRING", String.class);
   /**
    * The constant ML_ENTITY.
    */
   AnnotationType ML_ENTITY = AnnotationType.make(ENTITY, "ML_ENTITY");
   /**
    * The part-of-speech assocaited with a span
    */
   AttributeType<POS> PART_OF_SPEECH = AttributeType.make("PART_OF_SPEECH", POS.class);
   /**
    * phrase chunk annotation type
    */
   AnnotationType PHRASE_CHUNK = AnnotationType.make("PHRASE_CHUNK", PART_OF_SPEECH);
   /**
    * Date content was published
    */
   AttributeType<Date> PUBLICATION_DATE = AttributeType.make("PUBLICATION_DATE", Date.class);
   /**
    * The constant SENSE.
    */
   AttributeType<String> SENSE = AttributeType.make("SENSE", String.class);
   /**
    * sentence annotation type
    */
   AnnotationType SENTENCE = AnnotationType.make("SENTENCE");
   /**
    * Document source
    */
   AttributeType<String> SOURCE = AttributeType.make("SOURCE", String.class);
   /**
    * The type of token
    */
   AttributeType<String> SPELLING_CORRECTION = AttributeType.make("SPELLING", String.class);
   /**
    * The STEM.
    */
   AttributeType<String> STEM = AttributeType.make("STEM", String.class);
   /**
    * The tag associated with a span
    */
   AttributeType<StringTag> TAG = AttributeType.make("TAG", StringTag.class);
   /**
    * The constant MWE.
    */
   AnnotationType MWE = AnnotationType.make("MWE", TAG);
   /**
    * Document title
    */
   AttributeType<String> TITLE = AttributeType.make("TITLE", String.class);
   /**
    * token annotation type
    */
   AnnotationType TOKEN = AnnotationType.make("TOKEN", PART_OF_SPEECH);
   /**
    * The type of token
    */
   AttributeType<TokenType> TOKEN_TYPE = AttributeType.make("TOKEN_TYPE", TokenType.class);
   /**
    * The constant TOKEN_TYPE_ENTITY.
    */
   AnnotationType TOKEN_TYPE_ENTITY = AnnotationType.make(ENTITY, "TOKEN_TYPE_ENTITY");
   /**
    * The TRANSLITERATION.
    */
   AttributeType<String> TRANSLITERATION = AttributeType.make("TRANSLITERATION", String.class);
   /**
    * The constant WORD_SENSE.
    */
   AnnotationType WORD_SENSE = AnnotationType.make("WORD_SENSE", PART_OF_SPEECH);

   /**
    * Annotation annotation type.
    *
    * @param name the name
    * @return the annotation type
    */
   static AnnotationType annotation(String name) {
      return AnnotationType.make(name);
   }

   /**
    * Attribute attribute type.
    *
    * @param name the name
    * @return the attribute type
    */
   static AttributeType<?> attribute(String name) {
      return AttributeType.make(name);
   }

   /**
    * Attribute attribute type.
    *
    * @param <T>  the type parameter
    * @param name the name
    * @param type the type
    * @return the attribute type
    */
   static <T> AttributeType<T> attribute(String name, Type type) {
      return AttributeType.make(name);
   }

   /**
    * Attribute attribute type.
    *
    * @param <T>  the type parameter
    * @param name the name
    * @param type the type
    * @return the attribute type
    */
   static <T> AttributeType<T> attribute(String name, Class<T> type) {
      return AttributeType.make(name);
   }


   /**
    * Relation relation type.
    *
    * @param name the name
    * @return the relation type
    */
   static RelationType relation(String name) {
      return RelationType.make(name);
   }

   /**
    * To name string.
    *
    * @param type the type
    * @param name the name
    * @return the string
    */
   static String toName(@NonNull String type, @NonNull String name) {
      type = type.toLowerCase();
      if (!type.endsWith(".")) {
         type = type + ".";
      }
      return name.toLowerCase().startsWith(type) ? name.substring(type.length()) : name;
   }

   /**
    * To type name string.
    *
    * @param type the type
    * @param name the name
    * @return the string
    */
   static String toTypeName(@NonNull String type, @NonNull String name) {
      int dot = name.indexOf('.');
      if (dot < 0) {
         return Strings.toTitleCase(type) + "." + name;
      }

      String sub = name.substring(0, dot);
      if (sub.equalsIgnoreCase(type)) {
         return Strings.toTitleCase(type) + name.substring(dot);
      }

      return Strings.toTitleCase(type) + "." + name;
   }
}//END OF AnnotationTypes
