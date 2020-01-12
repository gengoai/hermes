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

package com.gengoai.hermes.ner;


import com.gengoai.annotation.Preload;

/**
 * Predefined set of common entities.
 *
 * @author David B. Bracewell
 */
@Preload
public interface Entities {
   /**
    * The constant DATE_TIME.
    */
   EntityType DATE_TIME = EntityType.make("DATE_TIME");
   /**
    * The constant DATE.
    */
   EntityType DATE = EntityType.make(DATE_TIME, "DATE");

   /**
    * The constant INTERNET.
    */
   EntityType INTERNET = EntityType.make("INTERNET");
   /**
    * The constant EMAIL.
    */
   EntityType EMAIL = EntityType.make(INTERNET, "EMAIL");
   /**
    * The constant EMOTICON.
    */
   EntityType EMOTICON = EntityType.make(INTERNET, "EMOTICON");
   /**
    * The constant HASHTAG.
    */
   EntityType HASH_TAG = EntityType.make(INTERNET, "HASH_TAG");
   /**
    * The constant LOCATION.
    */
   EntityType LOCATION = EntityType.make("LOCATION");
   EntityType GPE = EntityType.make(LOCATION, "GPE");
   /**
    * The constant NUMBER.
    */
   EntityType NUMBER = EntityType.make("NUMBER");
   /**
    * The constant CARDINAL.
    */
   EntityType CARDINAL = EntityType.make(NUMBER, "CARDINAL");
   /**
    * The constant MONEY.
    */
   EntityType MONEY = EntityType.make(NUMBER, "MONEY");
   /**
    * The constant ORDIANL.
    */
   EntityType ORDINAL = EntityType.make(NUMBER, "ORDINAL");
   /**
    * The constant ORGANIZATION.
    */
   EntityType ORGANIZATION = EntityType.make("ORGANIZATION");
   /**
    * The constant PERCENTAGE.
    */
   EntityType PERCENTAGE = EntityType.make(NUMBER, "PERCENTAGE");
   /**
    * The constant PERSON.
    */
   EntityType PERSON = EntityType.make("PERSON");
   EntityType PERSON_GROUP = EntityType.make(PERSON, "PERSON_GROUP");
   EntityType NORP = EntityType.make(PERSON_GROUP, "NORP");
   /**
    * The constant PRODUCT.
    */
   EntityType PRODUCT = EntityType.make("PRODUCT");
   EntityType WORK_OF_ART = EntityType.make(PRODUCT, "WORK_OF_ART");
   EntityType RULE = EntityType.make(PRODUCT, "RULE");
   EntityType LAW = EntityType.make(RULE, "LAW");
   EntityType LANGUAGE = EntityType.make(PRODUCT, "LANGUAGE");

   /**
    * The constant QUANTITY.
    */
   EntityType QUANTITY = EntityType.make("QUANTITY");
   /**
    * The constant REPLY.
    */
   EntityType REPLY = EntityType.make(INTERNET, "REPLY");
   /**
    * The constant TIME.
    */
   EntityType TIME = EntityType.make(DATE_TIME, "TIME");
   /**
    * The constant URL.
    */
   EntityType URL = EntityType.make(INTERNET, "URL");



}//END OF Entities
