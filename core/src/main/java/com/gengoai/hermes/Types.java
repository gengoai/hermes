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
import com.gengoai.Tag;
import com.gengoai.annotation.Preload;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.hermes.morphology.UniversalFeatureSet;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * <p>Common Annotatable Types. The predefined types are pre-loaded on initialization.</p>
 *
 * @author David B. Bracewell
 */
@Preload
public interface Types {
   /**
    * Attribute type defining the name (String) of the document's author
    */
   AttributeType<String> AUTHOR = AttributeType.make("AUTHOR", String.class);
   /**
    * Attribute added to extractions created by Caduceus to identify the name of the rule that created the extraction.
    */
   AttributeType<String> CADUCEUS_RULE = AttributeType.make("CADUCEUS_RULE", String.class);
   /**
    * Attribute defining the basic categories for a word/phrase.
    */
   AttributeType<Set<BasicCategories>> CATEGORY = AttributeType.make("CATEGORY",
                                                                     parameterizedType(Set.class,
                                                                                       BasicCategories.class));
   /**
    * Special Attribute for loading BasicCategories lexicons
    */
   AttributeType<BasicCategories> CATEGORY_TAG = AttributeType.make("CATEGORY_TAG", BasicCategories.class);
   /**
    * Attribute defining the numeric confidence of an extraction.
    */
   AttributeType<Double> CONFIDENCE = AttributeType.make("CONFIDENCE", double.class);
   /**
    * AnnotationType denoting that a constituent parse has be performed. Note that no annotations of this type are
    * added, but instead NON_TERMINAL_NODE annotation and, SYNTACTIC_HEAD relations are.
    */
   AnnotationType CONSTITUENT_PARSE = AnnotationType.make("CONSTITUENT_PARSE");
   /**
    * RelationType defining dependency relations.
    */
   RelationType DEPENDENCY = RelationType.make("DEPENDENCY");
   AttributeType<NDArray> EMBEDDING = AttributeType.make("EMBEDDING", NDArray.class);
   /**
    * Attribute defining the type of entity for Entity annotations
    */
   AttributeType<EntityType> ENTITY_TYPE = AttributeType.make("ENTITY_TYPE", EntityType.class);
   /**
    * Entity annotation type
    */
   AnnotationType ENTITY = AnnotationType.make("ENTITY", ENTITY_TYPE);
   /**
    * Attribute defining the file from which the document was created
    */
   AttributeType<String> FILE = AttributeType.make("FILE", String.class);
   AttributeType<String> IMPORT_DATE = AttributeType.make("IMPORT_DATE", String.class);
   /**
    * The index of a span with regards to a document
    */
   AttributeType<Integer> INDEX = AttributeType.make("INDEX", int.class);
   /**
    * Attributed specifying if a word/phrase is negated or not.
    */
   AttributeType<Boolean> IS_NEGATED = AttributeType.make("NEGATION", Boolean.class);
   /**
    * List of String attribute for defining a collection of keywords/phrases
    */
   AttributeType<List<String>> KEYWORDS = AttributeType.make("KEYWORDS", parameterizedType(List.class, String.class));
   /**
    * Language attribute defining the language a specific span is written in
    */
   AttributeType<Language> LANGUAGE = AttributeType.make("LANGUAGE", Language.class);
   /**
    * Attribute defining the lemma of a word/phrase
    */
   AttributeType<String> LEMMA = AttributeType.make("LEMMA", String.class);
   /**
    * AnnotationType for lexicon matches
    */
   AnnotationType LEXICON_MATCH = AnnotationType.make("LEXICON_MATCH");
   /**
    * String attribute defining what was matched in a lexicon
    */
   AttributeType<String> MATCHED_STRING = AttributeType.make("MATCHED_STRING", String.class);
   /**
    * String attribute defining what tag was matched in a lexicon
    */
   AttributeType<String> MATCHED_TAG = AttributeType.make("MATCHED_TAG", String.class);
   /**
    * Machine learning provided Entities
    */
   AnnotationType ML_ENTITY = AnnotationType.make(ENTITY, "ML_ENTITY");
   /**
    * Attribute defining the Morphological Features for a given span of text.
    */
   AttributeType<UniversalFeatureSet> MORPHOLOGICAL_FEATURES = AttributeType.make("MORPHO_FEATURES",
                                                                                  UniversalFeatureSet.class);
   /**
    * The part-of-speech associated with a span
    */
   AttributeType<PartOfSpeech> PART_OF_SPEECH = AttributeType.make("PART_OF_SPEECH", PartOfSpeech.class);
   /**
    * Non-Terminal node in a constituency parse
    */
   AnnotationType NON_TERMINAL_NODE = AnnotationType.make("NON_TERMINAL", PART_OF_SPEECH);
   /**
    * Phrase Chunk annotation type
    */
   AnnotationType PHRASE_CHUNK = AnnotationType.make("PHRASE_CHUNK", PART_OF_SPEECH);
   /**
    * LocalDateTime attributed defining when a piece of content was published
    */
   AttributeType<LocalDateTime> PUBLICATION_DATE = AttributeType.make("PUBLICATION_DATE", LocalDateTime.class);
   AttributeType<Double> SCORE = AttributeType.make("SCORE", double.class);
   /**
    * Sentence annotation type
    */
   AnnotationType SENTENCE = AnnotationType.make("SENTENCE");
   /**
    * String attribute defining the source of a document (e.g. url, name of news agency, etc.)
    */
   AttributeType<String> SOURCE = AttributeType.make("SOURCE", String.class);
   /**
    * String attribute defining a correction to the spelling of the associated word/phrase.
    */
   AttributeType<String> SPELLING_CORRECTION = AttributeType.make("SPELLING", String.class);
   AttributeType<String> SPLIT = AttributeType.make("SPLIT", String.class);
   /**
    * String attribute denoting the stemmed version of the word/phrase.
    */
   AttributeType<String> STEM = AttributeType.make("STEM", String.class);
   /**
    * The functional part of a syntactic pos tag
    */
   AttributeType<String> SYNTACTIC_FUNCTION = AttributeType.make("SYNTACTIC_FUNCTION");
   /**
    * Relation type denoting that the target is the syntactic head of the source
    */
   RelationType SYNTACTIC_HEAD = RelationType.make("HEAD");
   /**
    * Tag attribute associated with the span.
    */
   AttributeType<Tag> TAG = AttributeType.make("TAG", StringTag.class);
   /**
    * Multi-word expression annotations
    */
   AnnotationType MWE = AnnotationType.make("MWE", TAG);
   /**
    * String attribute defining the title of the document.
    */
   AttributeType<String> TITLE = AttributeType.make("TITLE", String.class);
   /**
    * Token annotation type
    */
   AnnotationType TOKEN = AnnotationType.make("TOKEN", PART_OF_SPEECH);
   AnnotatableType[] BASE_ANNOTATIONS = {Types.TOKEN, Types.SENTENCE, Types.PART_OF_SPEECH, Types.CATEGORY, Types.PHRASE_CHUNK, Types.ENTITY, Types.DEPENDENCY};
   /**
    * TokenType attribute defining the type of token.
    */
   AttributeType<TokenType> TOKEN_TYPE = AttributeType.make("TOKEN_TYPE", TokenType.class);
   /**
    * AnnotationType for Entities identified using TokenTypes.
    */
   AnnotationType TOKEN_TYPE_ENTITY = AnnotationType.make(ENTITY, "TOKEN_TYPE_ENTITY");
   /**
    * String attribute defining a transliteration for a given word/phrase.
    */
   AttributeType<String> TRANSLITERATION = AttributeType.make("TRANSLITERATION", String.class);
   /**
    * Set of String attribute defining the associated Wikipedia categories for a document or span of text.
    */
   AttributeType<Set<String>> WIKI_CATEGORIES = AttributeType.make("WIKI_CATEGORIES",
                                                                   parameterizedType(Set.class, String.class));
   AnnotationType WORD_SENSE = AnnotationType.make("WORD_SENSE");

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
      if(!type.endsWith(".")) {
         type = type + ".";
      }
      return name.toLowerCase().startsWith(type)
             ? name.substring(type.length())
             : name;
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
      if(dot < 0) {
         return Strings.toTitleCase(type) + "." + name;
      }

      String sub = name.substring(0, dot);
      if(sub.equalsIgnoreCase(type)) {
         return Strings.toTitleCase(type) + name.substring(dot);
      }

      return Strings.toTitleCase(type) + "." + name;
   }
}//END OF AnnotationTypes
