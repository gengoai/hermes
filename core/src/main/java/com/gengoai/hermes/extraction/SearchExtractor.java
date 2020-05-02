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

package com.gengoai.hermes.extraction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.string.StringMatcher;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;

/**
 * An Extractor implementation that searches for a given search text in the document.
 */
@Value
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonDeserialize(as = SearchExtractor.class)
public class SearchExtractor implements Extractor {
   String searchText;
   boolean caseSensitive;
   boolean fuzzyMatch;

   /**
    * Instantiates a new SearchExtractor.
    *
    * @param searchText    the text to search for
    * @param caseSensitive True - must match case, False - case does not matter
    * @param fuzzyMatch    True - allow sub-word matching (i.e. "is" will be matched in "This"), False must match full
    *                      spans of text.
    */
   @JsonCreator
   public SearchExtractor(@JsonProperty("searchText") @NonNull String searchText,
                          @JsonProperty("caseSensitive") boolean caseSensitive,
                          @JsonProperty("fuzzyMatch") boolean fuzzyMatch) {
      this.caseSensitive = caseSensitive;
      this.searchText = caseSensitive
                        ? searchText
                        : searchText.toLowerCase();
      this.fuzzyMatch = fuzzyMatch;
   }

   @Override
   public Extraction extract(@NonNull HString hString) {
      final String docString;
      if(caseSensitive) {
         docString = hString.toString();
      } else {
         docString = hString.toLowerCase();
      }
      final StringMatcher matcher = StringMatcher.matches(searchText, caseSensitive);
      var matches = new ArrayList<HString>();
      int start = 0;
      while((start = docString.indexOf(searchText, start)) >= 0) {
         HString match;
         if(hString.document() != null && hString.document().isCompleted(Types.TOKEN)) {
            match = HString.union(hString.substring(start, start + searchText.length()).tokens());
         } else {
            match = hString.substring(start, start + searchText.length());
         }
         if(fuzzyMatch || matcher.test(match)) {
            matches.add(match);
         }
         start = match.end();
      }
      return Extraction.fromHStringList(matches);
   }

}//END OF SearchExtractor
