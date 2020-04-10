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
import com.gengoai.collection.Sets;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.Lemmatizers;

import java.util.Collections;
import java.util.Set;

/**
 * Default Lemmatization annotator that uses the {@link com.gengoai.hermes.morphology.Lemmatizer} registered with the
 * token's language to perform lemmatization.
 *
 * @author David B. Bracewell
 */
public class DefaultLemmaAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;

   @Override
   protected void annotateImpl(Document document) {
      document.tokens().forEach(token -> {
         String lemma = Lemmatizers.getLemmatizer(token.getLanguage()).lemmatize(token);
         token.put(Types.LEMMA, lemma.toLowerCase());
      });
   }

   @Override
   public String getProvider(Language language) {
      return Lemmatizers.getLemmatizer(language).getClass().getSimpleName();
   }

   @Override
   public Set<AnnotatableType> requires() {
      return Sets.hashSetOf(Types.TOKEN, Types.PART_OF_SPEECH);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.LEMMA);
   }

}//END OF DefaultLemmaAnnotator

