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

/**
 * The enum Formats.
 *
 * @author David B. Bracewell
 */
public enum Formats implements CharSequence {
   /**
    * Conll formats.
    */
   CONLL,
   /**
    * Csv formats.
    */
   CSV,
   /**
    * Json formats.
    */
   JSON,
   /**
    * Json opl formats.
    */
   JSON_OPL,
   /**
    * Lucene formats.
    */
   LUCENE,
   /**
    * Pos opl formats.
    */
   POS_OPL,
   /**
    * Text formats.
    */
   TEXT,
   /**
    * Text opl formats.
    */
   TEXT_OPL;

   @Override
   public int length() {
      return name().length();
   }

   @Override
   public char charAt(int i) {
      return name().charAt(i);
   }

   @Override
   public CharSequence subSequence(int i, int i1) {
      return name().substring(i, i1);
   }
}//END OF Formats
