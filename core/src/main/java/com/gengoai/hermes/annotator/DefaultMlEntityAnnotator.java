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
import com.gengoai.cache.Cache;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringMLModel;

import java.util.Collections;
import java.util.Set;

/**
 * Default Machine-Learning based Entity annotator.
 *
 * @author David B. Bracewell
 */
public class DefaultMlEntityAnnotator extends Annotator {
   private static final Cache<Language, HStringMLModel> cache = ResourceType.MODEL.createCache("Annotation.ML_ENTITY",
                                                                                               "ner");
   private static final long serialVersionUID = 1L;

   @Override
   protected void annotateImpl(Document document) {
      HStringMLModel tagger = cache.get(document.getLanguage());
      if(tagger != null) {
         tagger.apply(document);
      }
   }

   @Override
   public String getProvider(Language language) {
      HStringMLModel tagger = cache.get(language);
      return tagger.getClass().getSimpleName() + " v" + tagger.getVersion();
   }

   public Set<AnnotatableType> requires() {
      return Set.of(Types.SENTENCE, Types.TOKEN, Types.PHRASE_CHUNK, Types.PART_OF_SPEECH);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.ML_ENTITY);
   }

}//END OF DefaultMlEntityAnnotator
