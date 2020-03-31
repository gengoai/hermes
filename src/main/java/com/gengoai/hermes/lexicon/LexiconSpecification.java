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

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystemNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gengoai.string.Re.*;

/**
 * The type Lexicon specification.
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
   private int tag = 1;

   /**
    * Parse lexicon specification.
    *
    * @param specification the specification
    * @return the lexicon specification
    */
   public static LexiconSpecification parse(String specification) {
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
    * Create lexicon.
    *
    * @return the lexicon
    * @throws IOException the io exception
    */
   public Lexicon create() throws IOException {
      if(protocol.equals("mem")) {
         if(format == null || format.equals("json")) {
            return LexiconIO.read(resource);
         } else if(format.equals("csv")) {
            return LexiconIO.importCSV(resource, c -> {
               c.tagAttribute = Cast.as(tagAttribute);
               c.defaultTag = defaultTag == null
                              ? null
                              : Cast.as(tagAttribute.decode(defaultTag));
               c.lemma = lemma;
               c.tag = tag;
               c.probability = probability;
               c.constraint = constraint;
               c.isCaseSensitive = caseSensitive;
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
         return new DiskLexicon(connection);
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
