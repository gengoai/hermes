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

package com.gengoai.hermes.morphology;

import com.gengoai.Validation;
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.io.ObjectStreamException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.gengoai.hermes.morphology.PartOfSpeech.ANY;
import static com.gengoai.hermes.morphology.PartOfSpeech.SYMBOL;
import static com.gengoai.hermes.morphology.PennTreeBank.*;

public final class POSTagSetManager {
   private static final Map<String, PartOfSpeech> tags = new ConcurrentHashMap<>();

   @SafeVarargs
   public static PartOfSpeech create(String name,
                                     String tag,
                                     @NonNull PartOfSpeech parent,
                                     boolean isPhraseTag,
                                     Tuple2<Feature, Value>... features) {
      Validation.notNullOrBlank(name);
      Validation.notNullOrBlank(tag);
      name = name.toUpperCase();
      tag = tag.toUpperCase();
      PartOfSpeech pos = null;
      if(tags.containsKey(name)) {
         pos = tags.get(name);
      } else if(tags.containsKey(tag)) {
         pos = tags.get(tag);
      }

      if(pos == null) {
         pos = new POSImpl(name, tag, parent, isPhraseTag, features);
         registerTagSet(pos);
         return pos;
      }

      if(pos.name().equalsIgnoreCase(name) &&
            pos.asString().equalsIgnoreCase(tag) &&
            pos.isPhraseTag() == isPhraseTag &&
            pos.parent().equals(parent)) {
         return pos;
      }
      System.out.println("NO: " + name);
      throw new IllegalStateException("Duplicate tag name: " + name);
   }

   public static void registerTagSet(@NonNull PartOfSpeech... tagset) {
      for(PartOfSpeech tag : tagset) {
         tags.put(tag.name().toUpperCase(), tag);
         tags.put(tag.asString().toUpperCase(), tag);
      }
   }

   public static PartOfSpeech valueOf(@NonNull String name) {
      name = name.toUpperCase();
      if(tags.containsKey(name)) {
         return tags.get(name);
      } else if(name.equals(";") || name.equals("...") || name.equals("-") || name.equals("--")) {
         return COLON;
      } else if(name.equals("?") || name.equals("!")) {
         return PERIOD;
      } else if(name.equals("``") || name.equals("''") || name.equals("\"\"") || name.equals("'") || name.equals("\"")) {
         return QUOTE;
      } else if(name.equals("UH")) {
         return UH;
      } else if(name.endsWith("{")) {
         return LCB;
      } else if(name.endsWith("}")) {
         return RCB;
      } else if(name.endsWith("[")) {
         return LSB;
      } else if(name.endsWith("]")) {
         return RSB;
      } else if(name.endsWith("(")) {
         return LRB;
      } else if(name.endsWith(")")) {
         return RRB;
      } else if(!Strings.hasLetter(name)) {
         return SYMBOL;
      } else if(name.equalsIgnoreCase("ANY")) {
         return ANY;
      }
      throw new IllegalArgumentException(name + " is not a known PartOfSpeech");
   }

   private static class POSImpl implements PartOfSpeech {
      private final String name;
      private final String tag;
      private final PartOfSpeech parent;
      private final boolean isPhraseTag;
      private final HashSetMultimap<Feature, Value> features;

      @SafeVarargs
      private POSImpl(String name,
                      String tag,
                      PartOfSpeech parent,
                      boolean isPhraseTag,
                      Tuple2<Feature, Value>... features) {
         this.name = name;
         this.tag = tag;
         this.parent = parent;
         this.isPhraseTag = isPhraseTag;
         if(features.length == 0) {
            this.features = null;
         } else {
            this.features = new HashSetMultimap<Feature, Value>();
            for(Tuple2<Feature, Value> feature : features) {
               this.features.put(feature.v1, feature.v2);
            }
         }
      }

      @Override
      public String asString() {
         return tag;
      }

      @Override
      public boolean equals(Object o) {
         if(this == o) return true;
         if(o instanceof PartOfSpeech) {
            PartOfSpeech pos = Cast.as(o);
            return pos.name().equalsIgnoreCase(name);
         }
         return false;
      }

      @Override
      public Optional<Set<Value>> getFeatureValues(Feature feature) {
         if( features == null){
            return Optional.empty();
         }
         return Optional.ofNullable(features.get(feature));
      }

      @Override
      public int hashCode() {
         return Objects.hash(name);
      }

      @Override
      public boolean isPhraseTag() {
         return isPhraseTag;
      }

      @Override
      public boolean isUniversalTag() {
         return false;
      }

      @Override
      public String name() {
         return name;
      }

      @Override
      public PartOfSpeech parent() {
         return parent;
      }

      protected Object readResolve() throws ObjectStreamException {
         return create(name, tag, parent, isPhraseTag);
      }

      @Override
      public String toString() {
         return name;
      }

   }

}//END OF POSTagSetManager
