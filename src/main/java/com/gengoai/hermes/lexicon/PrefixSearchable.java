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
 */

package com.gengoai.hermes.lexicon;

import com.gengoai.hermes.HString;

import java.util.Set;

/**
 * Interface defining a lexicon that can be searched using prefixes
 *
 * @author David B. Bracewell
 */
public interface PrefixSearchable {

   /**
    * Check if a prefix matches the given {@link HString}
    *
    * @param hString the {@link HString} to check for a prefix match
    * @return True if a prefix matches, False otherwise
    */
   boolean isPrefixMatch(HString hString);


   /**
    * Gets the prefixes that match the given string
    *
    * @param string the string
    * @return the set of matching prefixes
    */
   Set<String> prefixes(String string);


   /**
    * Check if a prefix matches the given String
    *
    * @param hString the String to check for a prefix match
    * @return True if a prefix matches, False otherwise
    */
   boolean isPrefixMatch(String hString);

}//END OF PrefixSearchable
