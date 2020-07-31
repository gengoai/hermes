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
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconManager;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Annotator that provides annotations based on a lexicon.
 *
 * @author David B. Bracewell
 */
public class LexiconAnnotator extends SentenceLevelAnnotator implements Serializable {
   private static final long serialVersionUID = 1L;
   private final AnnotationType type;
   private final Lexicon lexicon;

   /**
    * Instantiates a new LexiconAnnotator.
    *
    * @param type        the type of annotation to create.
    * @param lexiconName the name of the lexicon to perform annotation based on
    */
   public LexiconAnnotator(@NonNull AnnotationType type, @NonNull String lexiconName) {
      this(type, LexiconManager.getLexicon(lexiconName));
   }

   /**
    * Instantiates a new LexiconAnnotator.
    *
    * @param type    the type of annotation to create.
    * @param lexicon the lexicon to perform annotation based on
    */
   public LexiconAnnotator(@NonNull AnnotationType type, @NonNull Lexicon lexicon) {
      this.lexicon = lexicon;
      this.type = type;
   }

   @Override
   protected void annotate(@NonNull Annotation sentence) {
      lexicon.extract(sentence)
             .forEach(hString -> {
                Annotation annotation = sentence.document().annotationBuilder(type)
                                                .bounds(hString)
                                                .createAttached();
                if(hString.hasAttribute(Types.MATCHED_TAG)) {
                   annotation.put(type.getTagAttribute(),
                                  type.getTagAttribute().decode(hString.attribute(Types.MATCHED_TAG)));
                }
                if(hString.hasAttribute(Types.CONFIDENCE)) {
                   annotation.put(Types.CONFIDENCE, hString.attribute(Types.CONFIDENCE));
                }
             });
   }

   @Override
   public String getProvider(Language language) {
      return lexicon.getName();
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(type);
   }

}//END OF LexiconAnnotator
