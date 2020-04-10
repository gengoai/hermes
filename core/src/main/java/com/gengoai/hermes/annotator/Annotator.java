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
import com.gengoai.MultithreadedStopwatch;
import com.gengoai.config.Config;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * An annotator processes documents adding one or more {@link AnnotatableType}s. An annotator may require specific
 * types of annotations to be present on the document before it can process the document (by default it requires none).
 * Annotators define a provider name ({@link #getProvider(Language)}) that identifies the model, version, lexicon, etc.
 * used by
 * the annotator to produce its annotations (by default this is set "SimpleClassName v1.0").
 * </p>
 * <p><b>Note</b>: Annotator implementations should be implemented in a thread safe manner.</p>
 *
 * @author David B. Bracewell
 */
public abstract class Annotator implements Serializable {
   private static final long serialVersionUID = 1L;
   private final MultithreadedStopwatch stopwatch = new MultithreadedStopwatch("Annotator." + getClass().getSimpleName());

   /**
    * Annotates a document with one or more AnnotatableType  defined in <code>satisfies()</code>.
    *
    * @param document The document to annotate
    */
   public final void annotate(@NonNull Document document) {
      stopwatch.start();
      annotateImpl(document);
      stopwatch.stop();
   }

   /**
    * Annotates a document with one or more AnnotatableType  defined in <code>satisfies()</code>.
    *
    * @param document The document to annotate
    */
   protected abstract void annotateImpl(Document document);

   /**
    * Gets the provider information for this annotator.. The provider information should relate to a version number,
    * model used, or something else to identify the settings of the annotator.
    *
    * @param language
    * @return the provider
    */
   public String getProvider(Language language) {
      return Config.get(this.getClass(), "version")
                   .asString(getClass().getSimpleName() + " v1.0");
   }

   /**
    * The annotation types required to be on a document before this annotator can annotate
    *
    * @return the set of required annotation types
    */
   public Set<AnnotatableType> requires() {
      return Collections.emptySet();
   }

   /**
    * The set of annotation types that this annotator satisfies by this annotator
    *
    * @return the set of satisfied annotation types
    */
   public abstract Set<AnnotatableType> satisfies();

}//END OF Annotator
