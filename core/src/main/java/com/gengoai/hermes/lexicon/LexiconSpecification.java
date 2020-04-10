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
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.Types;
import com.gengoai.io.resource.Resource;
import com.gengoai.kv.KeyValueStoreConnection;
import com.gengoai.specification.*;
import com.gengoai.string.Strings;
import lombok.Data;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystemNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gengoai.string.Re.*;

/**
 * <p>
 * Lexicons are defined using a {@link LexiconSpecification} in the following format:
 * </p>
 * <pre>
 * {@code
 * lexicon:(mem|disk):name(:(csv|json))*::RESOURCE(;ARG=VALUE)*
 * }**
 * </pre>
 * <p>
 * The schema of the specification is "lexicon" and the currently supported protocols are: mem: An in-memory Trie-based
 * lexicon. disk: A persistent on-disk based lexicon.The name of the lexicon is used during annotation to mark the
 * provider. Additionally, a format (csv or json) can be specified, with json being the default if none is provided, to
 * specify the lexicon format when creating in-memory lexicons. Finally, a number of query parameters (ARG=VALUE) can be
 * given from the following choices:
 * <ul>
 * <li><code>caseSensitive=(true|false)</code>: Is the lexicon case-sensitive (</b>true<b>) or case-insensitive (</b>false<b>) (default </b>false<b>).</li>
 * <li><code>defaultTag=TAG</code>: The default tag value for entry when one is not defined (default null).</li>
 * <li><code>language=LANGUAGE</code>: The default language of entries in the lexicon (default Hermes.defaultLanguage()).</li>
 * </p>
 * and the following for CSV lexicons:
 * <ul>
 * <li><code>lemma=INDEX</code>: The index in the csv row containing the lemma (default 0).</li>
 * <li><code>tag=INDEX</code>: The index in the csv row containing the tag (default 1).</li>
 * <li><code>probability=INDEX</code>: The index in the csv row containing the probability (default 2).</li>
 * <li><code>constraint=INDEX</code>: The index in the csv row containing the constraint (default 3).</li>
 * </ul>
 * </p>
 *
 * @author David B. Bracewell
 */
@Data
public class LexiconSpecification implements Specifiable, Serializable {
   private static final Pattern HERMES_RESOURCE_SUB =
         Pattern.compile(re("<", q(Hermes.HERMES_RESOURCES_CONFIG), zeroOrOne(":", group("[A-Z_]+")), ">"));
   private static final long serialVersionUID = 1L;
   @QueryParameter
   private AttributeType<?> tagAttribute = Types.TAG;
   @QueryParameter
   private boolean caseSensitive = false;
   @QueryParameter
   private int constraint = -1;
   @QueryParameter
   private String defaultTag = null;
   @SubProtocol(1)
   private String format;
   @QueryParameter
   private int lemma = 0;
   @SubProtocol(0)
   private String name;
   @QueryParameter
   private int probability = -1;
   @Protocol
   private String protocol;
   @Path
   private Resource resource;
   @QueryParameter
   private Language language = Hermes.defaultLanguage();
   @QueryParameter
   private int tag = 1;

   /**
    * Parse the given specification string constructing a LexiconSpecification
    *
    * @param specification the specification
    * @return the LexiconSpecification
    */
   public static LexiconSpecification parse(@NonNull String specification) {
      LexiconSpecification lex = Specification.parse(specification, LexiconSpecification.class);
      Validation.notNullOrBlank(lex.name, "Invalid lexicon specification no name given: " + specification);
      Validation.notNullOrBlank(lex.protocol, "Invalid lexicon specification no protocol given: " + specification);
      Validation.notNull(lex.resource, "Invalid lexicon specification no resource given: " + specification);
      String path = lex.getResource().path();
      Matcher m = HERMES_RESOURCE_SUB.matcher(path);
      if(m.find()) {
         String language = m.group(1);
         path = path.substring(m.end());
         Language lng = Language.UNKNOWN;
         if(Strings.isNotNullOrBlank(language)) {
            lng = Language.fromString(language);
         }
         lex.setResource(ResourceType.LEXICON.locate(path, lng)
                                             .orElseThrow(() -> new FileSystemNotFoundException(specification)));
      }
      return lex;
   }

   /**
    * Create the lexicon from this specification
    *
    * @return the lexicon
    * @throws IOException Something went wrong during the construction
    */
   public Lexicon create() throws IOException {
      if(protocol.equals("mem")) {
         if(format == null || format.equals("json")) {
            if(Strings.isNullOrBlank(name)) {
               return LexiconIO.read(resource);
            }
            return LexiconIO.read(name, resource);
         } else if(format.equals("csv")) {
            String lexName = Strings.isNotNullOrBlank(name)
                             ? name
                             : resource.baseName();
            return LexiconIO.importCSV(lexName, resource, c -> {
               c.defaultTag = defaultTag == null
                              ? null
                              : Cast.as(tagAttribute.decode(defaultTag));
               c.lemma = lemma;
               c.tag = tag;
               c.probability = probability;
               c.constraint = constraint;
               c.isCaseSensitive = caseSensitive;
               c.language = language;
            });
         }
         throw new IllegalStateException("Invalid lexicon format: " + format);
      } else if(protocol.equals("disk")) {
         KeyValueStoreConnection connection = new KeyValueStoreConnection();
         connection.setPath(getResource().path());
         connection.setCompressed(true);
         connection.setNamespace(name);
         connection.setNavigable(true);
         connection.setType("disk");
         return new DiskLexicon(connection, isCaseSensitive());
      }
      throw new IllegalStateException("Invalid Lexicon Protocol: " + protocol);
   }

   @Override
   public String getSchema() {
      return "lexicon";
   }

   @Override
   public String toString() {
      return toSpecification();
   }

}//END OF LexiconSpecification
