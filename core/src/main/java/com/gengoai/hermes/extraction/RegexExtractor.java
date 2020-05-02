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
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An Extractor implementation that searches for a given regular expression pattern in the document.
 */
@Value
public class RegexExtractor implements Extractor {
   Pattern pattern;
   boolean fuzzyMatch;

   /**
    * Instantiates a new RegexExtractor.
    *
    * @param pattern    the pattern to search for
    * @param fuzzyMatch True - allow sub-word matching (i.e. "is" will be matched in "This"), False must match full
    *                   spans of text.
    */
   @JsonCreator
   public RegexExtractor(@JsonProperty("pattern") @NonNull Pattern pattern,
                         @JsonProperty("fuzzyMatch") boolean fuzzyMatch) {
      this.pattern = pattern;
      this.fuzzyMatch = fuzzyMatch;
   }

   /**
    * Instantiates a new RegexExtractor.
    *
    * @param pattern       the pattern to search for
    * @param caseSensitive True - must match case, False - case does not matter
    * @param fuzzyMatch    True - allow sub-word matching (i.e. "is" will be matched in "This"), False must match full
    *                      spans of text.
    */
   public RegexExtractor(@NonNull String pattern,
                         boolean caseSensitive,
                         boolean fuzzyMatch) {
      if(!caseSensitive) {
         pattern = "(?i)" + pattern;
      }
      this.pattern = Pattern.compile(pattern);
      this.fuzzyMatch = fuzzyMatch;
   }

   @Override
   public Extraction extract(@NonNull HString hString) {
      final Matcher matcher = pattern.matcher(hString);
      var matches = new ArrayList<HString>();
      while(matcher.find()) {
         HString match;
         if(hString.document() != null && hString.document().isCompleted(Types.TOKEN)) {
            match = HString.union(hString.substring(matcher.start(), matcher.end()).tokens());
         } else {
            match = hString.substring(matcher.start(), matcher.end());
         }
         if(fuzzyMatch || matcher.group().equalsIgnoreCase(match.toString())) {
            matches.add(match);
         }
      }
      return Extraction.fromHStringList(matches);
   }

}//END OF RegexExtractor
