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

package com.gengoai.hermes.lexicon;

import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.function.SerializableFunction;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.function.Consumer;

public abstract class LexiconBuilder implements Serializable {
   private static final String CONSTRAINT = "constraint";
   private static final String ENTRIES_SECTION = "@entries";
   private static final String IS_CASE_SENSITIVE = "caseSensitive";
   private static final String LEMMA = "lemma";
   private static final String PROBABILITY = "probability";
   private static final String SPECIFICATION_SECTION = "@spec";
   private static final String TAG = "tag";
   private static final String TAG_ATTRIBUTE = "tagAttribute";
   private static final long serialVersionUID = 1L;
   @Getter
   private final AttributeType<?> attributeType;
   @Getter
   private final boolean isCaseSensitive;
   @Getter
   private final SerializableFunction<String, String> normalizer;
   @Getter
   @Setter
   private boolean isProbabilistic = false;
   @Getter
   private int maxLemmaLength = 0;
   @Getter
   private int maxTokenLength = 0;

   protected LexiconBuilder(@NonNull AttributeType<?> attributeType, boolean isCaseSensitive) {
      this.attributeType = attributeType;
      this.isCaseSensitive = isCaseSensitive;
      this.normalizer = isCaseSensitive ? SerializableFunction.identity() : String::toLowerCase;
   }

   /**
    * Adds a lemma to the lexicon
    *
    * @param lemma the lemma
    */
   public final LexiconBuilder add(String lemma) {
      Validation.notNullOrBlank(lemma, "Lemma must not be null or blank");
      add(new LexiconEntry<>(normalize(lemma), 1.0, null, null));
      return this;
   }

   /**
    * Adds a lemma associated with a given tag to the lexicon
    *
    * @param lemma the lemma
    * @param tag   the tag
    */
   public final LexiconBuilder add(String lemma, Tag tag) {
      Validation.notNullOrBlank(lemma, "Lemma must not be null or blank");
      add(new LexiconEntry<>(normalize(lemma), 1.0, tag, null));
      return this;
   }

   /**
    * Adds a lemma associated with a given tag with a given probability
    *
    * @param lemma       the lemma
    * @param probability the probability
    * @param tag         the tag
    */
   public final LexiconBuilder add(String lemma, double probability, Tag tag) {
      Validation.notNullOrBlank(lemma, "Lemma must not be null or blank");
      Validation.checkArgument(probability >= 0, "Probability must be >= 0");
      add(new LexiconEntry<>(normalize(lemma), probability, tag, null));
      return this;
   }

   /**
    * Adds a lemma with a given probability to the lexicon
    *
    * @param lemma       the lemma
    * @param probability the probability
    */
   public final LexiconBuilder add(String lemma, double probability) {
      Validation.notNullOrBlank(lemma, "Lemma must not be null or blank");
      Validation.checkArgument(probability >= 0, "Probability must be >= 0");
      add(new LexiconEntry<>(normalize(lemma), probability, null, null));
      return this;
   }

   /**
    * Adds a lexicon entry to the lexicon
    *
    * @param entry the entry
    */
   public abstract LexiconBuilder add(LexiconEntry<?> entry);

   public LexiconBuilder add(@NonNull Consumer<LexiconEntryParameter> updater) {
      LexiconEntryParameter parameter = new LexiconEntryParameter();
      updater.accept(parameter);
      return add(new LexiconEntry<>(normalize(parameter.lemma),
                                    parameter.probability,
                                    parameter.tag,
                                    parameter.constraint,
                                    parameter.tokenLength));
   }

   /**
    * Adds all lexicon entries in th given iterable to the lexicon
    *
    * @param entries the entries
    */
   public final LexiconBuilder addAll(@NonNull Iterable<LexiconEntry<?>> entries) {
      entries.forEach(this::add);
      return this;
   }

   public abstract Lexicon build();

   public final LexiconBuilder merge(@NonNull Lexicon lexicon) {
      lexicon.entries().forEach(this::add);
      return this;
   }

   protected String normalize(CharSequence charSequence) {
      return normalizer.apply(charSequence.toString());
   }

   /**
    * Updates the longest lemma length to optimize search
    *
    * @param lemma the lemma
    */
   protected void updateMax(String lemma, int numberOfTokens) {
      this.maxTokenLength = Math.max(this.maxTokenLength, numberOfTokens);
      this.maxLemmaLength = Math.max(this.maxLemmaLength, lemma.length());
   }

   public static class LexiconEntryParameter implements Serializable {
      public LyreExpression constraint = null;
      public String lemma;
      public double probability = 0d;
      public Tag tag = null;
      public int tokenLength = 0;
   }

}//END OF LexiconBuilder
