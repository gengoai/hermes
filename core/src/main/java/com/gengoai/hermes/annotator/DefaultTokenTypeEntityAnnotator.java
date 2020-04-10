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

package com.gengoai.hermes.annotator;

import com.gengoai.Language;
import com.gengoai.hermes.*;
import com.gengoai.hermes.morphology.TokenType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default annotator for {@link TokenType} entities that maps <code>TokenType</code>s to {@link EntityType}. All
 * TokenType entities have a confidence of <code>0.6</code>
 *
 * @author David B. Bracewell
 */
public class DefaultTokenTypeEntityAnnotator extends SentenceLevelAnnotator {
   private static final Map<TokenType, EntityType> mapping = new HashMap<TokenType, EntityType>() {
      {
         put(TokenType.EMAIL, Entities.EMAIL);
         put(TokenType.URL, Entities.URL);
         put(TokenType.MONEY, Entities.MONEY);
         put(TokenType.NUMBER, Entities.NUMBER);
         put(TokenType.EMOTICON, Entities.EMOTICON);
         put(TokenType.COMPANY, Entities.ORGANIZATION);
         put(TokenType.HASH_TAG, Entities.HASH_TAG);
         put(TokenType.REPLY, Entities.REPLY);
         put(TokenType.TIME, Entities.TIME);
      }
   };
   private static final long serialVersionUID = 1L;

   @Override
   protected void annotate(Annotation sentence) {
      sentence.tokens().forEach(token -> {
         TokenType type = token.attribute(Types.TOKEN_TYPE, TokenType.UNKNOWN);
         if(mapping.containsKey(type)) {
            sentence.document()
                    .annotationBuilder(Types.TOKEN_TYPE_ENTITY)
                    .bounds(token)
                    .attribute(Types.ENTITY_TYPE, mapping.get(type))
                    .attribute(Types.CONFIDENCE, 0.6)
                    .createAttached();
         }
      });
   }

   @Override
   protected Set<AnnotatableType> furtherRequires() {
      return Collections.singleton(Types.TOKEN);
   }

   @Override
   public String getProvider(Language language) {
      return "TokenType";
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.TOKEN_TYPE_ENTITY);
   }

}//END OF TokenTypeEntityAnnotator
