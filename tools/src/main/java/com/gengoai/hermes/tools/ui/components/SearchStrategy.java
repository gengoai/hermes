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

package com.gengoai.hermes.tools.ui.components;

import com.gengoai.collection.Lists;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.parsing.ParseException;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SearchStrategy {
   CASE_INSENSITIVE_EXACT_MATCH {
      @Override
      public List<HString> findAll(@NonNull String searchText, @NonNull HString hString) {
         return SearchStrategy.stringSearch(searchText, hString, false, true);
      }
   },
   CASE_INSENSITIVE_FUZZY_MATCH {
      @Override
      public List<HString> findAll(@NonNull String searchText, @NonNull HString hString) {
         return SearchStrategy.stringSearch(searchText, hString, false, false);
      }
   },
   CASE_SENSITIVE_EXACT_MATCH {
      @Override
      public List<HString> findAll(@NonNull String searchText, @NonNull HString hString) {
         return SearchStrategy.stringSearch(searchText, hString, true, true);
      }
   },
   CASE_SENSITIVE_FUZZY_MATCH {
      @Override
      public List<HString> findAll(@NonNull String searchText, @NonNull HString hString) {
         return SearchStrategy.stringSearch(searchText, hString, true, false);
      }
   },
   REGEX_EXACT_MATCH {
      @Override
      public List<HString> findAll(@NonNull String searchText, @NonNull HString hString) {
         return SearchStrategy.regexSearch(searchText, hString, true);
      }

      @Override
      public Exception tryParse(String searchText) {
         try {
            Pattern.compile(searchText);
         } catch(Exception e) {
            return e;
         }
         return null;
      }
   },
   REGEX_FUZZY_MATCH {
      @Override
      public List<HString> findAll(@NonNull String searchText, @NonNull HString hString) {
         return SearchStrategy.regexSearch(searchText, hString, true);
      }

      @Override
      public Exception tryParse(String searchText) {
         try {
            Pattern.compile(searchText);
         } catch(Exception e) {
            return e;
         }
         return null;
      }
   },
   TOKEN_REGEX_MATCH {
      @Override
      public List<HString> findAll(@NonNull String searchText, @NonNull HString hString) {
         try {
            return Lists.asArrayList(TokenRegex.compile(searchText).extract(hString));
         } catch(ParseException e) {
            throw new RuntimeException(e);
         }
      }

      @Override
      public Exception tryParse(String searchText) {
         try {
            TokenRegex.compile(searchText);
         } catch(Exception e) {
            return e;
         }
         return null;
      }
   };

   private static List<HString> regexSearch(String searchText,
                                            HString hString,
                                            boolean exactMatch) {
      final String docString = hString.toString();
      final Pattern pattern = Pattern.compile(searchText);
      final Matcher matcher = pattern.matcher(docString);
      var matches = new ArrayList<HString>();
      while(matcher.find()) {
         HString match;
         if(hString.document() != null && hString.document().isCompleted(Types.TOKEN)) {
            match = HString.union(hString.substring(matcher.start(), matcher.end()).tokens());
         } else {
            match = hString.substring(matcher.start(), matcher.end());
         }
         if(!exactMatch || matcher.group().equalsIgnoreCase(match.toString())) {
            matches.add(match);
         }
      }
      return matches;
   }

   private static List<HString> stringSearch(String searchText,
                                             HString hString,
                                             boolean caseSensitive,
                                             boolean exactMatch) {
      final String docString;
      if(caseSensitive) {
         docString = hString.toString();
      } else {
         docString = hString.toLowerCase();
         searchText = searchText.toLowerCase();
      }

      var matches = new ArrayList<HString>();
      int start = 0;
      while((start = docString.indexOf(searchText, start)) != -1) {
         HString match;
         if(hString.document() != null && hString.document().isCompleted(Types.TOKEN)) {
            match = HString.union(hString.substring(start, start + searchText.length()).tokens());
         } else {
            match = hString.substring(start, start + searchText.length());
         }
         if(!exactMatch || (caseSensitive && match.contentEquals(searchText)) || (!caseSensitive && match.contentEqualsIgnoreCase(
               searchText))) {
            matches.add(match);
         }
         start = start + match.length();
      }
      return matches;
   }

   public abstract List<HString> findAll(@NonNull String searchText, @NonNull HString hString);

   public Exception tryParse(String searchText) {
      return null;
   }

}//END OF SearchStrategy
