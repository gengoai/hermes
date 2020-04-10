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

import com.gengoai.collection.Sets;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;

import java.util.Collections;
import java.util.Set;

/**
 * Base for annotators that work at the sentence level.
 *
 * @author David B. Bracewell
 */
public abstract class SentenceLevelAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;

   /**
    * Annotates a single sentence.
    *
    * @param sentence The sentence to annotate
    */
   protected abstract void annotate(Annotation sentence);

   @Override
   protected final void annotateImpl(Document document) {
      document.sentences().forEach(this::annotate);
   }

   /**
    * The annotation types beyond sentence and token that are also required. By default will return an empty Set.
    *
    * @return The annotations beyond sentence and token that are required for this annotator to perform annotation
    */
   protected Set<AnnotatableType> furtherRequires() {
      return Collections.emptySet();
   }

   @Override
   public final Set<AnnotatableType> requires() {
      return Sets.union(Sets.hashSetOf(Types.SENTENCE, Types.TOKEN), furtherRequires());
   }

}//END OF SentenceLevelAnnotator
