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

package com.gengoai.hermes.corpus.io;

import com.gengoai.Language;
import com.gengoai.ParameterDef;
import com.gengoai.collection.HashMapIndex;
import com.gengoai.collection.Index;
import com.gengoai.collection.Lists;
import com.gengoai.collection.Streams;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.io.CSV;
import com.gengoai.io.Resources;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.hermes.corpus.io.CorpusParameters.DOCUMENT_FACTORY;
import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * Reader for delimited separated files (e.g. CSV and TSV)
 *
 * @author David B. Bracewell
 */
public class CSVReader extends CorpusReader {
   private static final long serialVersionUID = 1L;
   /**
    * List of strings representing the column names
    */
   public static final ParameterDef<List<String>> COLUMN_NAMES = ParameterDef.param("columns",
                                                                                    parameterizedType(List.class,
                                                                                                      String.class));
   /**
    * The name of the column containing the content
    */
   public static final ParameterDef<String> CONTENT_COLUMN = ParameterDef.strParam("content");
   /**
    * The name of the column representing the id
    */
   public static final ParameterDef<String> ID_COLUMN = ParameterDef.strParam("id");
   /**
    * The name of the column representing the document language
    */
   public static final ParameterDef<String> LANGUAGE_COLUMN = ParameterDef.strParam("language");
   /**
    * The character representing a commented line
    */
   public static ParameterDef<Character> COMMENT_CHAR = ParameterDef.param("comment", Character.class);
   /**
    * The character representing the column delimiter
    */
   public static ParameterDef<Character> DELIMITER_CHAR = ParameterDef.param("delimiter", Character.class);
   /**
    * True when the CSV file has a header, False when not
    */
   public static ParameterDef<Boolean> HAS_HEADER = ParameterDef.boolParam("hasHeader");

   private final Index<String> columnNames = new HashMapIndex<>();

   /**
    * Instantiates a new Csv reader.
    */
   public CSVReader() {
      super(new CSVParameters());
   }

   /**
    * Creates a document from a row (i.e. list of column values)
    *
    * @param row             the row
    * @param documentFactory the document factory
    * @return the document
    */
   private Document createDocument(List<String> row, DocumentFactory documentFactory) {
      String id = Strings.EMPTY;
      String content = Strings.EMPTY;
      Language language = documentFactory.getDefaultLanguage();
      Map<AttributeType<?>, Object> attributeMap = new HashMap<>();

      String idField = getOptions().get(ID_COLUMN);
      String contentField = getOptions().get(CONTENT_COLUMN);
      String languageField = getOptions().get(LANGUAGE_COLUMN);
      Index<String> fields = getColumnNames();

      for (int i = 0; i < row.size() && i < columnNames.size(); i++) {
         String field = row.get(i);
         String fieldName = fields.get(i);
         if (idField.equalsIgnoreCase(fieldName)) {
            id = field;
         } else if (contentField.equalsIgnoreCase(fieldName)) {
            content = field;
         } else if (languageField.equalsIgnoreCase(fieldName)) {
            language = Language.fromString(field);
         } else {
            AttributeType attributeType = AttributeType.make(fieldName);
            try {
               attributeMap.put(attributeType, Converter.convert(field, attributeType.getValueType()));
            } catch (TypeConversionException e) {
               e.printStackTrace();
            }
         }
      }
      return documentFactory.create(id, content, language, attributeMap);
   }

   /**
    * Gets the names of the columns specified
    *
    * @return the column names
    */
   protected Index<String> getColumnNames() {
      if (columnNames.isEmpty()) {
         synchronized (columnNames) {
            if (columnNames.isEmpty()) {
               columnNames.addAll(getOptions().get(COLUMN_NAMES)
                                              .stream()
                                              .map(String::toUpperCase)
                                              .collect(Collectors.toList()));
            }
         }
      }
      return columnNames;
   }

   @Override
   public final Stream<Document> parse(String content) {
      final DocumentFactory documentFactory = getOptions().get(DOCUMENT_FACTORY);
      final LinkedList<List<String>> rows = Cast.as(Lists.asLinkedList(rows(content)));
      return Streams.asStream(new Iterator<Document>() {

         @Override
         public boolean hasNext() {
            return !rows.isEmpty();
         }

         @Override
         public Document next() {
            if (rows.isEmpty()) {
               throw new NoSuchElementException();
            }
            return createDocument(rows.removeFirst(), documentFactory);
         }
      });
   }

   private Iterable<List<String>> rows(String fileContent) {
      try {
         CSV csv = CSV.builder().delimiter(getOptions().get(DELIMITER_CHAR)).comment(getOptions().get(COMMENT_CHAR));

         com.gengoai.io.CSVReader reader;
         if (getOptions().get(HAS_HEADER)) {
            csv.hasHeader();
            columnNames.clear();
            reader = csv.reader(Resources.fromString(fileContent));
            columnNames.addAll(reader.getHeader().stream().map(String::toUpperCase).collect(Collectors.toList()));
         } else {
            csv.header(getColumnNames().asList());
            reader = csv.reader(Resources.fromString(fileContent));
         }
         return reader.readAll();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * The type Csv parameters.
    */
   public static class CSVParameters extends CorpusParameters {
      private static final long serialVersionUID = 1L;
      /**
       * The character representing a commented line
       */
      public final Parameter<Character> comment = parameter(COMMENT_CHAR, '#');
      /**
       * The character representing the column delimiter
       */
      public final Parameter<Character> delimiter = parameter(DELIMITER_CHAR, ',');
      /**
       * True when the CSV file has a header, False when not
       */
      public final Parameter<Boolean> hasHeader = parameter(HAS_HEADER, false);
      /**
       * List of strings representing the column names
       */
      public final Parameter<List<String>> columns = parameter(COLUMN_NAMES, Collections.emptyList());
      /**
       * The name of the column containing the content
       */
      public final Parameter<String> content = parameter(CONTENT_COLUMN, "content");
      /**
       * The name of the column representing the id
       */
      public final Parameter<String> id = parameter(ID_COLUMN, "id");
      /**
       * The name of the column representing the document language
       */
      public final Parameter<String> language = parameter(LANGUAGE_COLUMN, "language");
   }
}//END OF CSVReader
