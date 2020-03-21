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

import com.gengoai.MultithreadedStopwatch;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.Tokenizer;
import com.gengoai.hermes.morphology.Tokenizers;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * A <code>BreakIterator</code> backed token annotator. The locale used for the break iterator is based on the language
 * of the document.
 * </p>
 *
 * @author David B. Bracewell
 */
public class DefaultTokenAnnotator implements Annotator, Serializable {
   private static final long serialVersionUID = 1L;
   private final MultithreadedStopwatch stopwatch = new MultithreadedStopwatch("Annotator.DefaultTokenAnnotator");


   @Override
   public void annotate(Document document) {
      stopwatch.start();
      Tokenizer tokenizer = Tokenizers.getTokenizer(document.getLanguage());
      for(Tokenizer.Token token : tokenizer.tokenize(document.toString())) {
         Annotation aToken = document.createAnnotation(Types.TOKEN, token.charStartIndex, token.charEndIndex,
                                                       token.properties);
         aToken.put(Types.TOKEN_TYPE, token.type);
      }
      BaseWordCategorization.INSTANCE.categorize(document);
      stopwatch.stop();
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.TOKEN);
   }

}//END OF DefaultTokenAnnotator
