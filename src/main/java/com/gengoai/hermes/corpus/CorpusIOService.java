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

package com.gengoai.hermes.corpus;

import com.gengoai.Validation;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.corpus.io.*;
import com.gengoai.specification.Specification;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import static com.gengoai.tuple.Tuples.$;

/**
 * Helper class for getting {@link CorpusReader}s and {@link CorpusWriter}s.
 *
 * @author David B. Bracewell
 */
public class CorpusIOService {
   private static final Map<String, CorpusFormat> formats = new ConcurrentHashMap<>();

   private CorpusIOService() {
      throw new IllegalAccessError();
   }

   /**
    * Gets parameters.
    *
    * @param format the format
    * @return the parameters
    */
   public static CorpusParameters getParameters(@NonNull String format) {
      return parseFormat(format).v2.getFormatParameters();
   }

   /**
    * Gets a {@link CorpusReader} for the given format
    *
    * @param format the format
    * @return the {@link CorpusReader}
    */
   public static CorpusReader getReaderFor(@NonNull String format) {
      Tuple2<Boolean, CorpusFormat> ft = parseFormat(format);
      CorpusReader reader = ft.v2.getCorpusReader();
      if (ft.v1) {
         return new OnePerLineReader(reader);
      }
      return reader;
   }

   /**
    * Gets a {@link CorpusWriter} for the given format.
    *
    * @param format the format of the writer
    * @param corpus the corpus to write
    * @return the {@link CorpusWriter}
    */
   public static CorpusWriter getWriterFor(@NonNull String format, @NonNull Corpus corpus) {
      Tuple2<Boolean, CorpusFormat> ft = parseFormat(format);
      CorpusWriter writer = ft.v2.getCorpusWriter(corpus);
      if (ft.v1) {
         return new OnePerLineWriter(writer);
      }
      return writer;
   }


   /**
    * Parse corpus specification specification.
    *
    * @param specification the specification
    * @return the specification
    */
   public static Specification parseCorpusSpecification(String specification) {
      Validation.notNullOrBlank(specification, "Specification must not be null or blank");
      try {
         return Specification.parse(specification);
      } catch (IllegalArgumentException iae) {
         return Specification.builder()
                             .schema(Hermes.defaultCorpusFormat())
                             .path(specification)
                             .build();
      }
   }

   private static Tuple2<Boolean, CorpusFormat> parseFormat(String format) {
      boolean isOnePerLine = false;
      format = format.toUpperCase();
      if (format.endsWith("_OPL")) {
         isOnePerLine = true;
         format = format.substring(0, format.length() - "_OPL".length());
      }
      if (formats.containsKey(format)) {
         return $(isOnePerLine, formats.get(format));
      }
      throw new IllegalArgumentException("Unknown Format: " + format);
   }

   static {
      for (CorpusFormat df : ServiceLoader.load(CorpusFormat.class)) {
         formats.put(df.getName().toUpperCase(), df);
      }
   }

}//END OF CorpusReaderService
