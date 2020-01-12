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

import com.gengoai.annotation.JsonHandler;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.corpus.io.CorpusParameters;
import com.gengoai.hermes.corpus.io.CorpusReader;
import com.gengoai.io.FileUtils;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Helper object for the definition of a {@link Corpus} which includes its format, location, and applicable parameters
 * if any. Mainly for use from the command line as objects for web services. Format is as follows:
 * <pre>
 *    {
 *       "reader": <boolean - True looks for reader parameters, false looks for writer parameters for the format>,
 *       "format": "FORMAT_NAME",
 *       "resource": "RESOURCE_LOCATION",
 *       "parameters: {
 *          "isDistributed": <boolean>,
 *          "isParallel": <boolean>,
 *          "saveMode": "<SaveMode ENUM>",
 *          "documentFactory" : {
 *               "defaultLanguage": "<LANGUAGE ENUM>",
 *               "normalizer: ["normalizer_class", ...]
 *          },
 *          ... SPECIFIC PARAMETERS FOR THE DEFINED FORMAT ...
 *       }
 *    }
 * </pre>
 * <code>resource</code> is the only required parameter.
 *
 * @author David B. Bracewell
 */
@JsonHandler(CorpusDefinition.Marshaller.class)
public final class CorpusDefinition implements Serializable {
   private static final long serialVersionUID = 1L;
   private String format = Hermes.defaultCorpusFormat();
   private Resource location;
   private CorpusParameters parameters = null;

   /**
    * Constructs a {@link CorpusDefinition} from a string definition. Attempts to construct the definition in the
    * following order:
    * <ol>
    * <li>Treat definition as Json</li>
    * <li>Treat string as a resource specification to a Json file defining the corpus</li>
    * <li>Treat string as the location of the corpus using default values for all other aspects of the definition. If
    * the specified file is a recognized format (json or text) it will set the appropriate format</li>
    * </ol>
    *
    * @param definition the definition
    * @return the corpus definition
    */
   public static CorpusDefinition fromString(String definition) {
      try {
         return Json.parse(definition, CorpusDefinition.class);
      } catch (Exception e1) {
         try {
            CorpusDefinition cd = Json.parse(Resources.fromFile(definition).readToString(), CorpusDefinition.class);
            if (cd.location != null) {
               return cd;
            }
         } catch (Exception e2) {
            //ignore
         }
         CorpusDefinition cd = new CorpusDefinition();
         String ext = FileUtils.extension(definition.toLowerCase());
         switch (ext) {
            case "json":
            case "jsonl":
            case "json_opl":
               cd.format("JSON_OPL");
               break;
            case "txt":
            case "txtl":
            case "txt_opl":
               cd.format("TEXT_OPL");
         }
         cd.setLocation(Resources.fromFile(definition));
         return cd;
      }
   }

   /**
    * Creates a corpus from the definition.
    *
    * @return the created corpus
    * @throws IOException Something went wrong reading the corpus
    */
   public Corpus create() throws IOException {
      CorpusReader reader = Corpus.reader(format);
      if (parameters != null) {
         reader = reader.options(parameters);
      }
      return reader.read(location);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {return true;}
      if (obj == null || getClass() != obj.getClass()) {return false;}
      final CorpusDefinition other = (CorpusDefinition) obj;
      return Objects.equals(this.format, other.format)
                && Objects.equals(this.parameters, other.parameters)
                && Objects.equals(this.location, other.location);
   }


   /**
    * Gets the format of the corpus
    *
    * @return the format of the corpus
    */
   public String getFormat() {
      return format;
   }

   /**
    * Sets the format of the corpus
    *
    * @param format the format of the corpus or default format if null or blank
    * @return this definition
    */
   public CorpusDefinition format(String format) {
      this.format = Strings.isNotNullOrBlank(format) ? format : Hermes.defaultCorpusFormat();
      return this;
   }

   /**
    * Gets the location of the corpus as a resource
    *
    * @return the resource pointing to the corpus
    */
   public Resource getLocation() {
      return location;
   }

   /**
    * Sets where the corpus is located
    *
    * @param location the resource containing the corpus
    * @return this definition
    */
   public CorpusDefinition setLocation(Resource location) {
      this.location = location;
      return this;
   }

   /**
    * Gets the corpus parameters associated with the definition
    *
    * @return the parameters
    */
   public CorpusParameters getParameters() {
      return parameters;
   }

   /**
    * Sets the corpus parameters for the corpus
    *
    * @param parameters the new corpus parameters
    * @return this definition
    */
   public CorpusDefinition setParameters(CorpusParameters parameters) {
      this.parameters = parameters == null ? new CorpusParameters() : parameters;
      return this;
   }

   @Override
   public int hashCode() {
      return Objects.hash(format, parameters, location);
   }

   @Override
   public String toString() {
      return "CorpusDefinition{" +
                "format='" + format + '\'' +
                (parameters == null ? "" : ", parameters=" + parameters) +
                ", resource=" + location +
                '}';
   }

   /**
    * Marshaller for serializing and deserializing {@link CorpusDefinition}
    */
   public static class Marshaller extends com.gengoai.json.JsonMarshaller<CorpusDefinition> {

      @Override
      protected CorpusDefinition deserialize(JsonEntry entry, Type type) {
         CorpusDefinition definition = new CorpusDefinition();
         definition.format(entry.getStringProperty("format", Hermes.defaultCorpusFormat()));
         final String format = definition.getFormat();
         definition.setLocation(entry.getProperty("resource").getAs(Resource.class));
         entry.getOptionalProperty("parameters")
              .ifPresent(e -> definition.setParameters(e.getAs(CorpusIOService.getParameters(format).getClass())));
         return definition;
      }

      @Override
      protected JsonEntry serialize(CorpusDefinition corpusDefinition, Type type) {
         return JsonEntry.object()
                         .addProperty("format", corpusDefinition.format)
                         .addProperty("parameters", corpusDefinition.parameters)
                         .addProperty("location", corpusDefinition.location);
      }
   }
}//END OF CorpusDefinition
