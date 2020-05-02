/*
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

package com.gengoai.hermes.morphology;

import com.gengoai.EnumValue;
import com.gengoai.Registry;
import com.gengoai.annotation.Preload;

import java.util.Collection;

/**
 * Defines the type for a given token.
 *
 * @author David B. Bracewell
 */
@Preload
public final class TokenType extends EnumValue<TokenType> {
   private static final long serialVersionUID = 1L;
   private static final Registry<TokenType> registry = new Registry<>(TokenType::new, TokenType.class);
   /**
    * The constant ACRONYM.
    */
   public static final TokenType ACRONYM = make("ACRONYM");
   /**
    * The constant ALPHA_NUMERIC.
    */
   public static final TokenType ALPHA_NUMERIC = make("ALPHA_NUMERIC");
   /**
    * The constant CHINESE_JAPANESE.
    */
   public static final TokenType CHINESE_JAPANESE = make("CHINESE_JAPANESE");
   /**
    * The constant COMPANY.
    */
   public static final TokenType COMPANY = make("COMPANY");
   /**
    * The constant CONTRACTION.
    */
   public static final TokenType CONTRACTION = make("CONTRACTION");
   /**
    * The constant EMAIL.
    */
   public static final TokenType EMAIL = make("EMAIL");
   /**
    * The constant EMOTICON.
    */
   public static final TokenType EMOTICON = make("EMOTICON");
   /**
    * The constant HASH_TAG.
    */
   public static final TokenType HASH_TAG = make("HASH_TAG");
   /**
    * The constant HYPHEN.
    */
   public static final TokenType HYPHEN = make("HYPHEN");
   /**
    * The constant MONEY.
    */
   public static final TokenType MONEY = make("MONEY");
   /**
    * The constant NUMBER.
    */
   public static final TokenType NUMBER = make("NUMBER");
   /**
    * The constant PERSON_TITLE.
    */
   public static final TokenType PERSON_TITLE = make("PERSON_TITLE");
   /**
    * The constant PROTOCOL.
    */
   public static final TokenType PROTOCOL = make("PROTOCOL");
   /**
    * The constant PUNCTUATION.
    */
   public static final TokenType PUNCTUATION = make("PUNCTUATION");
   /**
    * The constant REPLY.
    */
   public static final TokenType REPLY = make("REPLY");
   /**
    * The constant SGML.
    */
   public static final TokenType SGML = make("SGML");
   /**
    * The constant TIME.
    */
   public static final TokenType TIME = make("TIME");
   /**
    * The constant UNKNOWN.
    */
   public static final TokenType UNKNOWN = make("UNKNOWN");
   /**
    * The constant URL.
    */
   public static final TokenType URL = make("URL");

   /**
    * Makes a new or retrieves an existing TokenType with the given name
    *
    * @param name the name of the TokenType
    * @return the TokenType
    */
   public static TokenType make(String name) {
      return registry.make(name);
   }

   /**
    * Returns a collection of all known TokenType in the enumeration.
    *
    * @return the collection of known TokenType
    */
   public static Collection<TokenType> values() {
      return registry.values();
   }

   private TokenType(String name) {
      super(name);
   }

   @Override
   protected Registry<TokenType> registry() {
      return registry;
   }

}//END OF TokenType
