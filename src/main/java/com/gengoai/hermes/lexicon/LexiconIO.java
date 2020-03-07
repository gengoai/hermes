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
 *
 */

package com.gengoai.hermes.lexicon;

import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.extraction.lyre.Lyre;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVReader;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.json.JsonWriter;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * Utility methods reading and writing Lexicon
 *
 * @author David B. Bracewell
 */
public final class LexiconIO {
   private static final String CONSTRAINT = "constraint";
   private static final String ENTRIES_SECTION = "@entries";
   private static final String IS_CASE_SENSITIVE = "caseSensitive";
   private static final String LEMMA = "lemma";
   private static final String PROBABILITY = "probability";
   private static final String SPECIFICATION_SECTION = "@spec";
   private static final String TAG = "tag";
   private static final String TAG_ATTRIBUTE = "tagAttribute";

   private LexiconIO() {
      throw new IllegalAccessError();
   }

   /**
    * Import csv lexicon.
    *
    * @param csvFile the csv file
    * @param updater the updater
    * @return the lexicon
    * @throws IOException the io exception
    */
   public static Lexicon importCSV(@NonNull Resource csvFile,
                                   @NonNull Consumer<CSVParameters> updater) throws IOException {
      CSVParameters parameters = new CSVParameters();
      updater.accept(parameters);
      Validation.checkArgument(parameters.lemma >= 0,
                               "A non-negative index for the lemma position in the csv file must be defined.");
      LexiconBuilder builder = TrieLexicon.builder(parameters.tagAttribute, parameters.isCaseSensitive);
      builder.setProbabilistic(parameters.probability > 0);
      try(CSVReader reader = parameters.csvFormat.reader(csvFile)) {
         List<String> row;
         while((row = reader.nextRow()) != null) {
            String lemma = row.get(parameters.lemma);
            if(Strings.isNullOrBlank(lemma)) {
               continue;
            }
            double prob = parameters.probability >= 0 && row.size() >= parameters.probability
                          ? Double.parseDouble(row.get(parameters.probability))
                          : 1.0;
            Tag tag = parameters.tag >= 0 && row.size() >= parameters.tag
                      ? parameters.tagAttribute.decode(row.get(parameters.tag))
                      : parameters.defaultTag;

            if(parameters.constraint >= 0 && row.size() > parameters.constraint) {
               builder.add(new LexiconEntry<>(lemma, prob, tag, Lyre.parse(row.get(parameters.constraint))));
            } else {
               builder.add(lemma, prob, tag);
            }
         }
      }
      return builder.build();
   }

   /**
    * Reads a lexicon in Json format from the given lexicon resource
    *
    * @param lexiconResource the lexicon resource
    * @return the lexicon
    * @throws IOException Something went wrong reading the resource
    */
   public static Lexicon read(@NonNull Resource lexiconResource) throws IOException {
      Map<String, JsonEntry> lexicon = Json.parseObject(lexiconResource);
      Map<String, JsonEntry> spec = lexicon.getOrDefault(SPECIFICATION_SECTION, JsonEntry.from(new HashMap<>()))
                                           .getAsMap();
      boolean isCaseSensitive = spec.getOrDefault(IS_CASE_SENSITIVE, JsonEntry.from(false)).getAsBoolean();
      AttributeType<? extends Tag> tagAttribute = spec.getOrDefault(TAG_ATTRIBUTE, JsonEntry.from(Types.TAG))
                                                      .getAs(parameterizedType(AttributeType.class, Tag.class));
      Tag defaultTag = spec.getOrDefault(TAG, JsonEntry.nullValue()).getAs(tagAttribute.getValueType());

      LexiconBuilder builder = TrieLexicon.builder(tagAttribute, isCaseSensitive);
      lexicon.get(ENTRIES_SECTION)
             .elementIterator()
             .forEachRemaining(e -> {
                if(!e.hasProperty(TAG) && defaultTag != null) {
                   e.addProperty(TAG, defaultTag.name());
                }

                if(!e.hasProperty(PROBABILITY)) {
                   e.addProperty(PROBABILITY, 1.0);
                } else {
                   builder.setProbabilistic(true);
                }
                builder.add(e.<LexiconEntry<?>>getAs(parameterizedType(LexiconEntry.class,
                                                                       tagAttribute.getValueType())));
             });

      return builder.build();
   }

   /**
    * Writes the given lexicon to the given lexicon resource in Json format.
    *
    * @param lexicon         the lexicon
    * @param lexiconResource the lexicon resource to write to
    * @param defaultTag      the default tag to assign entries
    * @throws IOException Something went wrong writing the lexicon
    */
   public static void write(Lexicon lexicon, Resource lexiconResource, Tag defaultTag) throws IOException {
      try(JsonWriter writer = Json.createWriter(lexiconResource)) {
         writer.spaceIndent(2);
         writer.beginDocument();
         {
            //Write Specification
            writer.beginObject(SPECIFICATION_SECTION);
            {
               writer.name(IS_CASE_SENSITIVE);
               writer.value(lexicon.isCaseSensitive());
               writer.name(TAG_ATTRIBUTE);
               writer.value(lexicon.getTagAttributeType());
               if(defaultTag != null) {
                  writer.name(TAG);
                  writer.value(defaultTag.name());
               }
            }
            writer.endObject();

            //Write Entries
            writer.beginArray(ENTRIES_SECTION);
            {
               for(LexiconEntry<?> entry : lexicon.entries()) {
                  writer.beginObject();
                  {
                     writer.name(LEMMA);
                     writer.value(entry.getLemma());
                     if(entry.getProbability() != 1.0) {
                        writer.name(PROBABILITY);
                        writer.value(entry.getProbability());
                     }
                     if(entry.getTag() != null && entry.getTag() != defaultTag) {
                        writer.name(TAG);
                        writer.value(entry.getTag().name());
                     }
                     if(entry.getConstraint() != null) {
                        writer.name(CONSTRAINT);
                        writer.value(entry.getConstraint().getPattern());
                     }
                  }
                  writer.endObject();
               }
            }
            writer.endArray();
         }
         writer.endDocument();
      }
   }

   /**
    * The type Csv parameters.
    */
   public static class CSVParameters {
      /**
       * The Constraint.
       */
      public int constraint = -1;
      /**
       * The Csv format.
       */
      @NonNull
      public CSV csvFormat = CSV.csv();
      /**
       * The Default tag.
       */
      public Tag defaultTag = null;
      /**
       * The Is case sensitive.
       */
      public boolean isCaseSensitive = false;
      /**
       * The Lemma.
       */
      public int lemma = 0;
      /**
       * The Probability.
       */
      public int probability = -1;
      /**
       * The Tag.
       */
      public int tag = -1;
      /**
       * The Tag attribute.
       */
      @NonNull
      public AttributeType<? extends Tag> tagAttribute = Types.TAG;
   }


}//END OF LexiconIO
