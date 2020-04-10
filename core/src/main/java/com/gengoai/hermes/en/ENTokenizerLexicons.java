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
import com.gengoai.hermes.lexicon.SimpleWordList;
import com.gengoai.hermes.lexicon.TrieWordList;
import com.gengoai.hermes.lexicon.WordList;
import com.gengoai.io.Resources;

import java.io.Serializable;

/**
 * <p>Lexicons used by the English Tokenizer.</p>
 *
 * @author David B. Bracewell
 */
public final class ENTokenizerLexicons implements Serializable {
   private static final long serialVersionUID = 1L;
   private static volatile Lazy<TrieWordList> abbreviations = new Lazy<>(Unchecked.supplier(() -> TrieWordList.read(
         Resources.fromClasspath("com/gengoai/hermes/lexicon/abbreviations.txt"))));
   private static volatile Lazy<TrieWordList> emoticons = new Lazy<>(Unchecked.supplier(() -> TrieWordList.read(
         Resources.fromClasspath("com/gengoai/hermes/lexicon/emoticons.txt"))));
   private static volatile Lazy<WordList> tlds = new Lazy<>(Unchecked.supplier(() -> SimpleWordList.read(
         Resources.fromClasspath("com/gengoai/hermes/lexicon/tlds.txt"))));

   private ENTokenizerLexicons() {
      throw new IllegalAccessError();
   }

   /**
    * Gets a lexicon (as a TrieWordList) of common abbreviations.
    *
    * @return the abbreviations
    */
   public static TrieWordList getAbbreviations() {
      return abbreviations.get();
   }

   /**
    * Gets a lexicon (as a TrieWordList) of emoticons.
    *
    * @return the emoticons
    */
   public static TrieWordList getEmoticons() {
      return emoticons.get();
   }

   /**
    * Gets a lexicon (as a WordList) of the top level internet domain names.
    *
    * @return the top level domains
    */
   public static WordList getTopLevelDomains() {
      return tlds.get();
   }

}//END OF ENTokenizerLexicons
