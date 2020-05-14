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

package com.gengoai.hermes.format;

import com.gengoai.string.StringMatcher;

/**
 * Corrects POS tags to conform to HERMES format
 *
 * @author David B. Bracewell
 */
public interface POSCorrection {

   /**
    * Pos string.
    *
    * @param word the word
    * @param pos  the pos
    * @return the string
    */
   static String pos(String word, String pos) {
      switch(word) {
         case "-":
            return ":";
         case "%":
            return "SYM";
         case "[":
            return "-LSB-";
         case "]":
            return "-RSB-";
         case "(":
            return "-LRB-";
         case ")":
            return "-RRB-";
         case "{":
            return "-LCB-";
         case "}":
            return "-RCB-";
      }

      switch(pos) {
         case "HYPH":
            return ":";
      }

      if(StringMatcher.HasLetterOrDigit.negate().test(word) && pos.startsWith("NN")) {
         return "SYM";
      }

      return pos;
   }

   /**
    * Word string.
    *
    * @param word the word
    * @param pos  the pos
    * @return the string
    */
   static String word(String word, String pos) {
      switch(pos) {
         case "``":
         case "''":
            return "\"";
         case "-LRB-":
            return "(";
         case "-LSB-":
            return "[";
         case "-LCB-":
            return "{";
         case "-RRB-":
            return ")";
         case "-RCB-":
            return "}";
         case "-RSB-":
            return "]";
         case "\\/":
            return "/";
      }
      switch(word) {
         case "\"\"":
         case "``":
         case "''":
         case "”":
         case "“":
            return "\"";
         case "-LRB-":
            return "(";
         case "-RRB-":
            return ")";
         case "-LSB-":
            return "[";
         case "-RSB-":
            return "]";
         case "-LCB-":
            return "{";
         case "-RCB-":
            return "}";
         case "’s":
            return "'s";
         case "’":
            return "'";
      }
      return word;
   }
}//END OF POSCorrection