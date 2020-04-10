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

import com.gengoai.Language;
import com.gengoai.Validation;
import com.gengoai.hermes.Hermes;
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
   private static final String LANGUAGE = "language";

   private LexiconIO() {
      throw new IllegalAccessError();
   }

   /**
    * Imports a CSV file into an in-memory lexicon.
    *
    * @param name    the lexicon name
    * @param csvFile the csv file
    * @param updater consumer to set the CSVParameters
    * @return the lexicon
    * @throws IOException Something went wrong reading the CSV file
    */
   public static Lexicon importCSV(@NonNull String name,
                                   @NonNull Resource csvFile,
                                   @NonNull Consumer<CSVParameters> updater) throws IOException {
      CSVParameters parameters = new CSVParameters();
      updater.accept(parameters);
      Validation.checkArgument(parameters.lemma >= 0,
                               "A non-negative index for the lemma position in the csv file must be defined.");
      TrieLexicon lexicon = new TrieLexicon(name, parameters.isCaseSensitive);
      final Language language = parameters.language;
      try(CSVReader reader = parameters.csvFormat.reader(csvFile)) {
         List<String> row;
         while((row = reader.nextRow()) != null) {
            String lemma = row.get(parameters.lemma);
            if(Strings.isNullOrBlank(lemma)) {
               continue;
            }
            double prob = parameters.probability >= 0 && row.size() >= parameters.probability
                          ? Double.parseDouble(row.get(parameters.probability))
                          : -1.0;
            String tag = parameters.tag >= 0 && row.size() >= parameters.tag
                         ? row.get(parameters.tag)
                         : parameters.defaultTag;
            if(parameters.constraint >= 0 && row.size() > parameters.constraint) {
               lexicon.add(LexiconEntry.of(lemma,
                                           prob,
                                           tag,
                                           Lyre.parse(row.get(parameters.constraint)),
                                           LexiconEntry.calculateTokenLength(lemma, language)));
            } else {
               lexicon.add(LexiconEntry.of(lemma, prob, tag, LexiconEntry.calculateTokenLength(lemma, language)));
            }
         }
      }
      return lexicon;
   }

   /**
    * Imports a CSV file into an in-memory lexicon.
    *
    * @param csvFile the csv file
    * @param updater consumer to set the CSVParameters
    * @return the lexicon
    * @throws IOException Something went wrong reading the CSV file
    */
   public static Lexicon importCSV(@NonNull Resource csvFile,
                                   @NonNull Consumer<CSVParameters> updater) throws IOException {
      return importCSV(csvFile.baseName(), csvFile, updater);
   }

   /**
    * Reads a lexicon in Json format from the given lexicon resource
    *
    * @param name            the name of the lexicon
    * @param lexiconResource the lexicon resource
    * @return the lexicon
    * @throws IOException Something went wrong reading the resource
    */
   public static Lexicon read(@NonNull String name, @NonNull Resource lexiconResource) throws IOException {
      Map<String, JsonEntry> lexJson = Json.parseObject(lexiconResource);
      Map<String, JsonEntry> spec = lexJson.getOrDefault(SPECIFICATION_SECTION, JsonEntry.from(new HashMap<>()))
                                           .getAsMap();
      final boolean isCaseSensitive = spec.getOrDefault(IS_CASE_SENSITIVE, JsonEntry.from(false)).getAsBoolean();
      final String defaultTag = spec.getOrDefault(TAG, JsonEntry.nullValue()).getAsString();
      final Language language = spec.getOrDefault(LANGUAGE, JsonEntry.from(Hermes.defaultLanguage()))
                                    .getAs(Language.class);
      TrieLexicon lexicon = new TrieLexicon(name, isCaseSensitive);
      lexJson.get(ENTRIES_SECTION)
             .elementIterator()
             .forEachRemaining(e -> {
                if(!e.hasProperty(TAG) && defaultTag != null) {
                   e.addProperty(TAG, defaultTag);
                }
                if(!e.hasProperty(PROBABILITY)) {
                   e.addProperty(PROBABILITY, -1.0);
                }
                if(!e.hasProperty("tokenLength")) {
                   e.addProperty("tokenLength",
                                 LexiconEntry.calculateTokenLength(e.getStringProperty("lemma"), language));
                }
                lexicon.add(e.getAs(LexiconEntry.class));
             });

      return lexicon;
   }

   /**
    * Reads a lexicon in Json format from the given lexicon resource
    *
    * @param lexiconResource the lexicon resource
    * @return the lexicon
    * @throws IOException Something went wrong reading the resource
    */
   public static Lexicon read(@NonNull Resource lexiconResource) throws IOException {
      return read(lexiconResource.baseName(), lexiconResource);
   }

   /**
    * Writes the given lexicon to the given lexicon resource in Json format.
    *
    * @param lexicon         the lexicon
    * @param lexiconResource the lexicon resource to write to
    * @param defaultTag      the default tag to assign entries
    * @throws IOException Something went wrong writing the lexicon
    */
   public static void write(Lexicon lexicon, Resource lexiconResource, String defaultTag) throws IOException {
      try(JsonWriter writer = Json.createWriter(lexiconResource)) {
         writer.spaceIndent(2);
         writer.beginDocument();
         {
            //Write Specification
            writer.beginObject(SPECIFICATION_SECTION);
            {
               writer.name(IS_CASE_SENSITIVE);
               writer.value(lexicon.isCaseSensitive());
               if(defaultTag != null) {
                  writer.name(TAG);
                  writer.value(defaultTag);
               }
            }
            writer.endObject();

            //Write Entries
            writer.beginArray(ENTRIES_SECTION);
            {
               for(LexiconEntry entry : lexicon.entries()) {
                  writer.beginObject();
                  {
                     writer.name(LEMMA);
                     writer.value(entry.getLemma());
                     writer.name("tokenLength");
                     writer.value(entry.getTokenLength());
                     if(entry.getProbability() != 1.0) {
                        writer.name(PROBABILITY);
                        writer.value(entry.getProbability());
                     }
                     if(entry.getTag() != null && !entry.getTag().equals(defaultTag)) {
                        writer.name(TAG);
                        writer.value(entry.getTag());
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
      public String defaultTag = null;
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
       * The Language
       */
      public Language language = Hermes.defaultLanguage();
   }

}//END OF LexiconIO
