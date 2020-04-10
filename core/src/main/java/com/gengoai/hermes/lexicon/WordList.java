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
import lombok.NonNull;

/**
 * <p>Word lists provide a set like interface to set of vocabulary items. Implementations of WordList may implement the
 * {@link PrefixSearchable} interface allowing prefix matching. Word lists are loaded from plain text files with "#" at
 * the beginning of a line denoting a comment. Whereas lexicons provide a robust way to match and label HStrings,
 * _WordList_s provide a simple means of determining if a word/phrase is defined.</p>
 * <p>
 * Note that convention states that if the first line of a word list is a comment stating "case-insensitive" then
 * loading of that word list will result in all words being lower-cased.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface WordList extends Iterable<String> {

   /**
    * Is the String contained in the WordList
    *
    * @param string the string to lookup
    * @return True if the string is in the WordList, False otherwise
    */
   boolean contains(String string);

   /**
    * Is the {@link HString} contained in the WordList
    *
    * @param string the {@link HString} to lookup
    * @return True if the {@link HString} is in the WordList, False otherwise
    */
   default boolean contains(@NonNull HString string) {
      return contains(string.toString());
   }

   /**
    * Number of words in the list
    *
    * @return the number of words in the list
    */
   int size();

}//END OF WordList
