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

package com.gengoai.hermes.en;

import com.gengoai.Lazy;
import com.gengoai.function.Unchecked;
import com.gengoai.hermes.lexicon.TrieWordList;
import com.gengoai.hermes.lexicon.WordList;
import com.gengoai.io.Resources;

import java.util.stream.Stream;

/**
 * <p>Lexicons used by the English Tokenizer.</p>
 *
 * @author David B. Bracewell
 */
public enum ENLexicons {
   EMOTICONS,
   GENERIC_ABBREVIATIONS,
   ORGANIZATION,
   PERSON_TITLE,
   PLACE,
   TIME,
   TLDS,
   UNITS,
   ALL_ABBREVIATION(GENERIC_ABBREVIATIONS, ORGANIZATION, PERSON_TITLE, PLACE, TIME, UNITS);

   private final Lazy<TrieWordList> wordList;

   ENLexicons(ENLexicons... combined) {
      this.wordList = new Lazy<>(Unchecked.supplier(() -> new TrieWordList(Stream.of(combined)
                                                                                 .map(ENLexicons::get)
                                                                                 .toArray(WordList[]::new))));
   }

   ENLexicons() {
      this.wordList = new Lazy<>(Unchecked.supplier(() -> TrieWordList.read(Resources.fromClasspath(
            "com/gengoai/hermes/lexicon/" + this.name().toLowerCase() + ".txt"))));
   }

   public TrieWordList get() {
      return wordList.get();
   }

}//END OF ENTokenizerLexicons
