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
import com.gengoai.cache.Cache;
import com.gengoai.hermes.*;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconEntry;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.string.Strings;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default annotator for basic categories, which is limited to nouns.
 */
public class DefaultCategoryAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;
   private Cache<Language, Lexicon> cache = ResourceType.LEXICON.createCache("basic_categories", "basic_categories");

   @Override
   protected void annotateImpl(Document document) {
      Lexicon lexicon = cache.get(document.getLanguage());
      lexicon.extract(document)
             .forEach(h -> {
                if(!h.pos().isVerb()) {
                   for(Annotation token : h.tokens()) {
                      token.putAdd(Types.CATEGORY, lexicon.match(h).stream()
                                                          .map(LexiconEntry::getTag)
                                                          .map(BasicCategories::valueOf)
                                                          .collect(Collectors.toList()));
                   }
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

   @Override
   public String getProvider(Language language) {
      return "basic_categories_" + language.name().toLowerCase();
   }

   @Override
   public Set<AnnotatableType> requires() {
      return Collections.singleton(Types.PART_OF_SPEECH);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.CATEGORY);
   }

}//END OF DefaultCategoryAnnotator
