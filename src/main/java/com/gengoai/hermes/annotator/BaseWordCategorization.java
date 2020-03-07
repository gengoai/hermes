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

package com.gengoai.hermes.annotator;

import com.gengoai.Language;
import com.gengoai.cache.AutoCalculatingLRUCache;
import com.gengoai.cache.Cache;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.BasicCategories;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconEntry;
import com.gengoai.hermes.lexicon.LexiconManager;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.stream.Collectors;

public class BaseWordCategorization {
   public static final BaseWordCategorization INSTANCE = new BaseWordCategorization();
   private Cache<Language, Lexicon> cache = new AutoCalculatingLRUCache<>(100,
                                                                          language -> LexiconManager.getLexicon(
                                                                                "base_categories", language));

   public void categorize(@NonNull Document document) {
      Lexicon lexicon = cache.get(document.getLanguage());
      lexicon.extract(document)
             .forEach(h -> {
                for(Annotation token : h.tokens()) {
                   token.putAdd(Types.CATEGORY, lexicon.getEntries(h).stream()
                                                       .map(LexiconEntry::getTag)
                                                       .map(Cast::<BasicCategories>as)
                                                       .collect(Collectors.toList()));
                }
             });
      for(Annotation token : document.tokens()) {
         if(Strings.isDigit(token) || token.attributeIsA(Types.TOKEN_TYPE, TokenType.NUMBER)) {
            token.putAdd(Types.CATEGORY, Collections.singleton(BasicCategories.NUMBERS));
         } else if(token.attributeIsA(Types.TOKEN_TYPE, TokenType.TIME)) {
            token.putAdd(Types.CATEGORY, Collections.singleton(BasicCategories.TIME));
         } else if(token.attributeIsA(Types.TOKEN_TYPE, TokenType.EMOTICON)) {
            token.putAdd(Types.CATEGORY, Collections.singleton(BasicCategories.INTERNET));
         } else if(token.attributeIsA(Types.TOKEN_TYPE, TokenType.URL)) {
            token.putAdd(Types.CATEGORY, Collections.singleton(BasicCategories.INTERNET));
         }
      }
   }

}//END OF WordCategorization
