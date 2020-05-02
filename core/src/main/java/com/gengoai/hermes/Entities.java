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

package com.gengoai.hermes;


import com.gengoai.annotation.Preload;

/**
 * Predefined set of common entities.
 *
 * @author David B. Bracewell
 */
@Preload
public interface Entities {
   //-----------------------------------------------------------------------------------------
   // DATE_TIME ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType DATE_TIME = EntityType.make("DATE_TIME");
   EntityType DATE = EntityType.make(DATE_TIME, "DATE");
   EntityType TIME = EntityType.make(DATE_TIME, "TIME");
   EntityType ERA = EntityType.make(DATE_TIME, "ERA");
   EntityType PERIOD = EntityType.make(DATE_TIME, "PERIOD");
   EntityType TIME_PERIOD = EntityType.make(PERIOD, "TIME_PERIOD");
   EntityType DATE_PERIOD = EntityType.make(PERIOD, "DATE_PERIOD");
   EntityType WEEK_PERIOD = EntityType.make(PERIOD, "WEEK_PERIOD");
   EntityType MONTH_PERIOD = EntityType.make(PERIOD, "MONTH_PERIOD");
   EntityType YEAR_PERIOD = EntityType.make(PERIOD, "YEAR_PERIOD");

   //-----------------------------------------------------------------------------------------
   // INTERNET ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType INTERNET = EntityType.make("INTERNET");
   EntityType EMAIL = EntityType.make(INTERNET, "EMAIL");
   EntityType EMOTICON = EntityType.make(INTERNET, "EMOTICON");
   EntityType HASH_TAG = EntityType.make(INTERNET, "HASH_TAG");
   EntityType REPLY = EntityType.make(INTERNET, "REPLY");
   EntityType URL = EntityType.make(INTERNET, "URL");

   //-----------------------------------------------------------------------------------------
   // LOCATION ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType LOCATION = EntityType.make("LOCATION");
   EntityType RELATIVE_LOCATION = EntityType.make(LOCATION, "RELATIVE_LOCATION");
   EntityType GPE = EntityType.make(LOCATION, "GPE");
   EntityType CITY = EntityType.make(GPE, "CITY");
   EntityType COUNTY = EntityType.make(GPE, "COUNTY");
   EntityType PROVINCE = EntityType.make(GPE, "PROVINCE");
   EntityType COUNTRY = EntityType.make(GPE, "COUNTRY");
   EntityType REGION = EntityType.make(LOCATION, "REGION");
   EntityType GEOLOGICAL_REGION = EntityType.make(LOCATION, "GEOLOGICAL_REGION");
   EntityType LANDFORM = EntityType.make(GEOLOGICAL_REGION, "LANDFORM");
   EntityType WATER_FORM = EntityType.make(GEOLOGICAL_REGION, "WATER_FORM");
   EntityType SEA = EntityType.make(GEOLOGICAL_REGION, "SEA");
   EntityType ASTRAL_BODY = EntityType.make(LOCATION, "ASTRAL_BODY");
   EntityType STAR = EntityType.make(ASTRAL_BODY, "STAR");
   EntityType PLANET = EntityType.make(ASTRAL_BODY, "PLANET");
   EntityType POSTAL_ADDRESS = EntityType.make(LOCATION, "POSTAL_ADDRESS");

   //-----------------------------------------------------------------------------------------
   // FACILITY ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType FACILITY = EntityType.make("FACILITY");
   EntityType GOE = EntityType.make(FACILITY, "GOE");
   EntityType SCHOOL = EntityType.make(GOE, "SCHOOL");
   EntityType MUSEUM = EntityType.make(GOE, "MUSEUM");
   EntityType AMUSEMENT_PARK = EntityType.make(GOE, "AMUSEMENT_PARK");
   EntityType STATION_TOP = EntityType.make(FACILITY, "STATION_TOP");
   EntityType AIRPORT = EntityType.make(STATION_TOP, "AIRPORT");
   EntityType STATION = EntityType.make(STATION_TOP, "STATION");
   EntityType PORT = EntityType.make(STATION_TOP, "PORT");
   EntityType CAR_STOP = EntityType.make(STATION_TOP, "CAR_STOP");
   EntityType LINE = EntityType.make(FACILITY, "LINE");
   EntityType RAILROAD = EntityType.make(LINE, "RAILROAD");
   EntityType ROAD = EntityType.make(LINE, "ROAD");
   EntityType WATERWAY = EntityType.make(LINE, "WATERWAY");
   EntityType TUNNEL = EntityType.make(LINE, "TUNNEL");
   EntityType BRIDGE = EntityType.make(LINE, "BRIDGE");
   EntityType PARK = EntityType.make(FACILITY, "PARK");
   EntityType MONUMENT = EntityType.make(FACILITY, "MONUMENT");

   //-----------------------------------------------------------------------------------------
   // NUMBER ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType NUMBER = EntityType.make("NUMBER");
   EntityType CARDINAL = EntityType.make(NUMBER,"CARDINAL");
   EntityType ORDINAL = EntityType.make(NUMBER,"ORDINAL");
   EntityType POINT = EntityType.make(NUMBER, "POINT");
   EntityType MONEY = EntityType.make(NUMBER, "MONEY");
   EntityType MULTIPLICATION = EntityType.make(NUMBER, "MULTIPLICATION");
   EntityType PERCENT = EntityType.make(NUMBER, "PERCENT");
   EntityType FREQUENCY = EntityType.make(NUMBER, "FREQUENCY");
   EntityType RANK = EntityType.make(NUMBER, "RANK");
   EntityType AGE = EntityType.make(NUMBER, "AGE");
   EntityType QUANTITY = EntityType.make(NUMBER, "QUANTITY");

   //-----------------------------------------------------------------------------------------
   // MEASUREMENT ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType MEASUREMENT = EntityType.make("MEASUREMENT");
   EntityType PHYSICAL_EXTENT = EntityType.make(MEASUREMENT, "PHYSICAL_EXTENT");
   EntityType SPACE = EntityType.make(MEASUREMENT, "SPACE");
   EntityType VOLUME = EntityType.make(MEASUREMENT, "VOLUME");
   EntityType WEIGHT = EntityType.make(MEASUREMENT, "WEIGHT");
   EntityType SPEED = EntityType.make(MEASUREMENT, "SPEED");
   EntityType INTENSITY = EntityType.make(MEASUREMENT, "INTENSITY");
   EntityType TEMPERATURE = EntityType.make(MEASUREMENT, "TEMPERATURE");
   EntityType CALORIE = EntityType.make(MEASUREMENT, "CALORIE");
   EntityType SEISMIC_INTENSITY = EntityType.make(MEASUREMENT, "SEISMIC_INTENSITY");

   //-----------------------------------------------------------------------------------------
   // ORGANIZATION ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType ORGANIZATION = EntityType.make("ORGANIZATION");
   EntityType COMPANY = EntityType.make(ORGANIZATION, "COMPANY");
   EntityType COMPANY_GROUP = EntityType.make(ORGANIZATION, "COMPANY_GROUP");
   EntityType MILITARY = EntityType.make(ORGANIZATION, "MILITARY");
   EntityType INSTITUTE = EntityType.make(ORGANIZATION, "INSTITUTE");
   EntityType MARKET = EntityType.make(ORGANIZATION, "MARKET");
   EntityType POLITICAL_ORGANIZATION = EntityType.make(ORGANIZATION, "POLITICAL_ORGANIZATION");
   EntityType GOVERNMENT = EntityType.make(POLITICAL_ORGANIZATION, "GOVERNMENT");
   EntityType POLITICAL_PARTY = EntityType.make(POLITICAL_ORGANIZATION, "POLITICAL_PARTY");
   EntityType PUBLIC_INSTITUTION = EntityType.make(POLITICAL_ORGANIZATION, "PUBLIC_INSTITUTION");
   EntityType GROUP = EntityType.make(ORGANIZATION, "GROUP");
   EntityType SPORTS_TEAM = EntityType.make(GROUP, "SPORTS_TEAM");


   //-----------------------------------------------------------------------------------------
   // PERSON ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType PERSON = EntityType.make("PERSON");
   EntityType PERSON_GROUP = EntityType.make(PERSON, "PERSON_GROUP");
   EntityType ETHNIC_GROUP = EntityType.make(PERSON_GROUP, "ETHNIC_GROUP");
   EntityType NATIONALITY = EntityType.make(PERSON_GROUP, "NATIONALITY");
   EntityType TITLE = EntityType.make(PERSON, "TITLE");
   EntityType POSITION_TITLE = EntityType.make(TITLE, "POSITION_TITLE");
   EntityType OCCUPATION = EntityType.make(PERSON, "OCCUPATION");


   //-----------------------------------------------------------------------------------------
   // PRODUCT ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType PRODUCT = EntityType.make("PRODUCT");
   EntityType WORK_OF_ART = EntityType.make(PRODUCT, "WORK_OF_ART");
   EntityType RULE = EntityType.make(PRODUCT, "RULE");
   EntityType LAW = EntityType.make(RULE, "LAW");
   EntityType LANGUAGE = EntityType.make(PRODUCT, "LANGUAGE");

   //-----------------------------------------------------------------------------------------
   // EVENT ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType EVENT = EntityType.make("EVENT");

   //-----------------------------------------------------------------------------------------
   // MISC ENTITY TYPES
   //-----------------------------------------------------------------------------------------
   EntityType MISC = EntityType.make("MISC");

}//END OF Entities
