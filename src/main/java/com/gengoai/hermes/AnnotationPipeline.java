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
 *
 */

package com.gengoai.hermes;

import com.gengoai.Language;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.collection.multimap.SetMultimap;
import com.gengoai.hermes.annotator.Annotator;
import lombok.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Helper class for determining the required sequence of annotation for a document to fulfill a required set of desired
 * annotatable types.
 *
 * @author David B. Bracewell
 */
public class AnnotationPipeline implements Serializable {
   private static final long serialVersionUID = 1L;
   private final AnnotatableType[] types;
   private transient ArrayListMultimap<Language, Annotator> annotators = new ArrayListMultimap<>();
   private transient SetMultimap<Language, AnnotatableType> provided = new HashSetMultimap<>();

   /**
    * Instantiates a new Annotate sequence with the given set of annotatable types to be annotated.
    *
    * @param types the types
    */
   public AnnotationPipeline(AnnotatableType... types) {
      this(Arrays.asList(types));
   }

   /**
    * Instantiates a new Annotate sequence.
    *
    * @param types the types
    */
   public AnnotationPipeline(@NonNull Collection<? extends AnnotatableType> types) {
      this.types = types.stream().filter(Objects::nonNull).toArray(AnnotatableType[]::new);
   }

   /**
    * Annotates a document to fulfill the desired set of annotatable types
    *
    * @param document the document to annotate
    * @return the document with annotations
    */
   public boolean annotate(Document document) {
      AtomicBoolean updated = new AtomicBoolean(false);
      getAnnotators(document).forEach(annotator -> {
         annotator.annotate(document);
         updated.set(true);
         for (AnnotatableType type : annotator.satisfies()) {
            document.setCompleted(type, annotator.getClass().getName() + "::" + annotator.getVersion());
         }
      });
      return updated.get();
   }

   private Stream<Annotator> getAnnotators(Document document) {
      return getSequence(document.getLanguage()).stream()
                                                .filter(a -> !document.completed()
                                                                      .containsAll(a.satisfies()));
   }

   private List<Annotator> getSequence(Language language) {
      if (!annotators.containsKey(language)) {
         synchronized (types) {
            if (!annotators.containsKey(language)) {
               for (AnnotatableType type : types) {
                  processType(type, language);
               }
            }
         }
      }
      return annotators.get(language);
   }

   private void processType(AnnotatableType type, Language language) {
      Annotator annotator = AnnotatorCache.getInstance().get(type, language);
      if (annotator == null) {
         throw new IllegalStateException("Could not get annotator for " + type);
      }
      if (!annotator.satisfies().contains(type)) {
         throw new IllegalStateException(
            annotator.getClass().getName() + " does not satisfy " + type);
      }
      boolean providesNew = false;
      for (AnnotatableType satisfy : annotator.satisfies()) {
         if (!provided.contains(language, satisfy)) {
            providesNew = true;
         }
         provided.put(language, satisfy);
      }
      if (providesNew) {
         for (AnnotatableType prereq : annotator.requires()) {
            processType(prereq, language);
         }
         annotators.put(language, annotator);
      }
   }

   private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
      aInputStream.defaultReadObject();
      this.annotators = new ArrayListMultimap<>();
      this.provided = new HashSetMultimap<>();
   }

   /**
    * Requires update boolean.
    *
    * @return the boolean
    */
   public boolean requiresUpdate() {
      return types.length > 0;
   }


}//END OF AnnotationPipeline
