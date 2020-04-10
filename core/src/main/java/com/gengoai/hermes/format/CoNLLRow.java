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

package com.gengoai.hermes.format;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A Row (token) in a CoNLL formatted file
 *
 * @author David B. Bracewell
 */
@Data
public final class CoNLLRow implements Serializable {
   private static final long serialVersionUID = 1L;
   private long annotationID = -1L;
   private String depRelation = null;
   private int end = -1;
   private int index = -1;
   private Map<String, String> otherProperties = new HashMap<>(3);
   private int parent = -1;
   private String pos = null;
   private int sentence = -1;
   private int start = -1;
   private String word;

   /**
    * Adds a field not specifically tracked.
    *
    * @param name  the name of the field
    * @param value the value of the field
    */
   public void addOther(String name, String value) {
      otherProperties.put(name.toUpperCase(), value);
   }

   /**
    * Gets a non-specific field with the given name.
    *
    * @param name the name
    * @return the value
    */
   public String getOther(String name) {
      return otherProperties.get(name.toUpperCase());
   }

   /**
    * Checks if a value for the given non-specified property name exists
    *
    * @param name the name
    * @return True exists, False otherwise
    */
   public boolean hasOther(String name) {
      return otherProperties.containsKey(name.toUpperCase());
   }

}//END OF CoNLLRow