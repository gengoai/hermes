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

package com.gengoai.hermes.en;

import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.collection.tree.Trie;
import com.gengoai.hermes.morphology.Lemmatizer;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVReader;
import com.gengoai.io.Resources;
import com.gengoai.stream.Streams;
import com.gengoai.string.Re;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gengoai.collection.Maps.asHashMap;

/**
 * English language lemmatizer based on WordNet's Morphy
 *
 * @author David B. Bracewell
 */
public class ENLemmatizer implements Lemmatizer, Serializable {
   private static final long serialVersionUID = -6093027604295026727L;
   private static final PartOfSpeech[] ALL_PartOfSpeech = {PartOfSpeech.NOUN, PartOfSpeech.VERB, PartOfSpeech.ADJECTIVE, PartOfSpeech.ADVERB};
   private static volatile ENLemmatizer INSTANCE = null;
   private static Pattern WHITESPACE = Pattern.compile(Re.MULTIPLE_WHITESPACE);
   private final Multimap<PartOfSpeech, DetachmentRule> rules = new ArrayListMultimap<>();
   private final Multimap<Tuple2<PartOfSpeech, String>, String> exceptions = new ArrayListMultimap<>();
   private final Trie<Set<PartOfSpeech>> lemmas;

   /**
    * Instantiates a new ENLemmatizer.
    */
   private ENLemmatizer() {
      rules.put(PartOfSpeech.NOUN, new DetachmentRule("s", ""));
      rules.put(PartOfSpeech.NOUN, new DetachmentRule("ses", "s"));
      rules.put(PartOfSpeech.NOUN, new DetachmentRule("xes", "x"));
      rules.put(PartOfSpeech.NOUN, new DetachmentRule("zes", "z"));
      rules.put(PartOfSpeech.NOUN, new DetachmentRule("ies", "y"));
      rules.put(PartOfSpeech.NOUN, new DetachmentRule("shes", "sh"));
      rules.put(PartOfSpeech.NOUN, new DetachmentRule("ches", "ch"));
      rules.put(PartOfSpeech.NOUN, new DetachmentRule("men", "man"));
      loadException(PartOfSpeech.NOUN);

      rules.put(PartOfSpeech.VERB, new DetachmentRule("s", ""));
      rules.put(PartOfSpeech.VERB, new DetachmentRule("ies", "y"));
      rules.put(PartOfSpeech.VERB, new DetachmentRule("es", "s"));
      rules.put(PartOfSpeech.VERB, new DetachmentRule("es", ""));
      rules.put(PartOfSpeech.VERB, new DetachmentRule("ed", "e"));
      rules.put(PartOfSpeech.VERB, new DetachmentRule("ed", ""));
      rules.put(PartOfSpeech.VERB, new DetachmentRule("ing", "e"));
      rules.put(PartOfSpeech.VERB, new DetachmentRule("ing", ""));
      loadException(PartOfSpeech.VERB);

      rules.put(PartOfSpeech.ADJECTIVE, new DetachmentRule("er", ""));
      rules.put(PartOfSpeech.ADJECTIVE, new DetachmentRule("est", ""));
      rules.put(PartOfSpeech.ADJECTIVE, new DetachmentRule("er", "e"));
      rules.put(PartOfSpeech.ADJECTIVE, new DetachmentRule("est", "e"));
      loadException(PartOfSpeech.ADJECTIVE);
      loadException(PartOfSpeech.ADVERB);

      this.lemmas = new Trie<>();
      try(CSVReader reader = CSV.builder()
                                .delimiter('\t')
                                .reader(Resources.fromClasspath(
                                      "com/gengoai/hermes/en/lemmas.dict.gz"))) {
         reader.forEach(row -> {
            if(row.size() >= 2) {
               String lemma = row.get(0).replace('_', ' ').toLowerCase();
               PartOfSpeech pos = PartOfSpeech.valueOf(row.get(1).toUpperCase());
               if(!lemmas.containsKey(lemma)) {
                  lemmas.put(lemma, new HashSet<>());
               }
               lemmas.get(lemma).add(pos);
            }
         });
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * @return the singleton instance of the Lemmatizer
    */
   public static ENLemmatizer getInstance() {
      if(INSTANCE == null) {
         synchronized(ENLemmatizer.class) {
            if(INSTANCE != null) {
               return INSTANCE;
            }
            INSTANCE = new ENLemmatizer();
         }
      }
      return INSTANCE;
   }

   private Set<String> allAndSelf(String word) {
      Set<String> lemmas = new HashSet<>(doLemmatization(word, true, PartOfSpeech.ANY));
      lemmas.add(word);
      return lemmas;
   }

   @Override
   public List<String> allPossibleLemmas(@NonNull String word, @NonNull PartOfSpeech partOfSpeech) {
      List<String> lemmaList = null;
      if(partOfSpeech == PartOfSpeech.ANY) {
         lemmaList = new ArrayList<>(doLemmatization(word,
                                                     true,
                                                     PartOfSpeech.NOUN,
                                                     PartOfSpeech.VERB,
                                                     PartOfSpeech.ADJECTIVE,
                                                     PartOfSpeech.ADVERB));
      } else if(partOfSpeech.isInstance(PartOfSpeech.NOUN,
                                        PartOfSpeech.VERB,
                                        PartOfSpeech.ADJECTIVE,
                                        PartOfSpeech.ADVERB)) {
         lemmaList = new ArrayList<>(doLemmatization(word, true, partOfSpeech));
      }
      if(lemmaList == null || lemmaList.isEmpty()) {
         lemmaList = Collections.singletonList(word.toLowerCase());
      }
      return lemmaList;
   }

   @Override
   public Trie<String> allPossibleLemmasAndPrefixes(@NonNull String string,
                                                    @NonNull PartOfSpeech partOfSpeech) {
      Trie<String> lemmaSet = new Trie<>();
      for(String lemma : doLemmatization(string, true, partOfSpeech)) {
         lemmaSet.putAll(asHashMap(lemmas.prefix(lemma + " ").keySet(), k -> k));
         if(lemmas.containsKey(lemma)) {
            lemmaSet.put(lemma, lemma);
         }
      }
      return lemmaSet;
   }

   @Override
   public boolean canLemmatize(String input, PartOfSpeech partOfSpeech) {
      return partOfSpeech.isInstance(PartOfSpeech.NOUN,
                                     PartOfSpeech.VERB,
                                     PartOfSpeech.ADJECTIVE,
                                     PartOfSpeech.ADVERB) && doLemmatization(input,
                                                                             false,
                                                                             partOfSpeech
                                                                            ).size() > 0;
   }

   private boolean contains(String string, PartOfSpeech PartOfSpeech) {
      return lemmas.containsKey(string) && (PartOfSpeech == PartOfSpeech.ANY || lemmas.get(string)
                                                                                      .contains(PartOfSpeech.getUniversalTag()));
   }

   private Set<String> doLemmatization(String word, boolean includeSelf, PartOfSpeech... tags) {
      Set<String> tokenLemmas = new LinkedHashSet<>();

      if(tags == null || tags.length == 0 || tags[0] == PartOfSpeech.ANY) {
         tags = ALL_PartOfSpeech;
      }

      word = word.toLowerCase();
      for(PartOfSpeech tag : tags) {
         fill(word, tag, tokenLemmas);
      }

      if(tokenLemmas.isEmpty() && word.contains("-")) {
         String noHyphen = word.replace('-', ' ');
         for(PartOfSpeech tag : tags) {
            fill(noHyphen, tag, tokenLemmas);
         }
      }

      if(tokenLemmas.isEmpty() && word.contains(" ")) {
         String withHyphen = word.replace(' ', '-');
         for(PartOfSpeech tag : tags) {
            fill(withHyphen, tag, tokenLemmas);
         }
      }

      if(tokenLemmas.isEmpty() && WHITESPACE.matcher(word).find()) {
         tokenLemmas.addAll(phraseLemmas(word, tags));
      }

      //If all else fails and we should include the word return it
      if(tokenLemmas.isEmpty() && includeSelf) {
         return Collections.singleton(word.toLowerCase());
      }

      return tokenLemmas;
   }

   private void fill(String word, PartOfSpeech partOfSpeech, Set<String> set) {
      word = word.toLowerCase();
      //Word is already a lemma with the given part of speech
      if(contains(word, partOfSpeech.getUniversalTag())) {
         set.add(word);
         return;
      }

      if(partOfSpeech.isVerb()) {
         if(word.equalsIgnoreCase("'s") || word.equalsIgnoreCase("'re")) {
            set.add("be");
            return;
         } else if(word.equals("'ll")) {
            set.add("will");
            return;
         } else if(word.equals("'ve")) {
            set.add("will");
            return;
         }
      } else if(partOfSpeech.isAdverb()) {
         if(word.equalsIgnoreCase("n't")) {
            set.add("not");
            return;
         }
      } else if(word.equalsIgnoreCase("'d")) {
         set.add("would");
         return;
      }

      //Apply the exceptions
      Tuple2<PartOfSpeech, String> key = Tuple2.of(partOfSpeech.getUniversalTag(), word.toLowerCase());
      if(exceptions.containsKey(key)) {
         set.addAll(exceptions.get(key));
      }

      //Apply the rules
      for(DetachmentRule rule : rules.get(partOfSpeech.getUniversalTag())) {
         String output = rule.apply(word);
         if(contains(output, partOfSpeech.getUniversalTag())) {
            set.add(output);
         }
      }
   }

   private boolean hasPartOfSpeech(String lemma, PartOfSpeech... tags) {
      if(tags == null || tags.length == 0 || tags[0] == PartOfSpeech.ANY) {
         return this.lemmas.containsKey(lemma);
      }
      for(PartOfSpeech PartOfSpeech : this.lemmas.getOrDefault(lemma, Collections.emptySet())) {
         if(PartOfSpeech.isInstance(tags)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String lemmatize(@NonNull String string, @NonNull PartOfSpeech partOfSpeech) {
      if(partOfSpeech == PartOfSpeech.ANY) {
         return Streams.asStream(doLemmatization(string, true, ALL_PartOfSpeech))
                       .findFirst()
                       .orElse(string)
                       .toLowerCase();
      } else if(partOfSpeech.isInstance(ALL_PartOfSpeech)) {
         return Streams.asStream(doLemmatization(string, true, partOfSpeech)).findFirst().orElse(string).toLowerCase();
      }
      return string.toLowerCase();
   }

   private void loadException(PartOfSpeech tag) {
      try {
         for(String line :
               Resources.fromClasspath("com/gengoai/hermes/en")
                        .getChild(tag.tag().toLowerCase() + ".exc")
                        .readLines()) {
            if(!Strings.isNullOrBlank(line)) {
               String[] parts = line.split("\\s+");
               Tuple2<PartOfSpeech, String> key = Tuple2.of(tag.getUniversalTag(), parts[0].replaceAll("_", " "));
               for(int i = 1; i < parts.length; i++) {
                  exceptions.put(key, parts[i]);
               }
            }
         }
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   private Set<String> phraseLemmas(String phrase, PartOfSpeech... tags) {
      String[] words = phrase.split("\\s+");
      if(words.length == 0) {
         return Collections.emptySet();
      }
      if(tags == null || tags.length == 0 || tags[0] == PartOfSpeech.ANY) {
         tags = ALL_PartOfSpeech;
      }
      Trie<String> prefixes = allPossibleLemmasAndPrefixes(words[0], PartOfSpeech.ANY);
      Set<String> lemmas = allAndSelf(words[0]);
      for(int i = 1; i < words.length; i++) {
         Set<String> nextSet = new HashSet<>();
         for(String previous : lemmas) {
            for(String next : allAndSelf(words[i])) {
               String subPhrase = previous + " " + next;
               if(prefixes.containsKey(subPhrase)) {
                  nextSet.add(subPhrase);
               } else if(prefixes.prefix(subPhrase).size() > 0) {
                  nextSet.add(subPhrase);
               }
            }
         }
         if(nextSet.isEmpty()) {
            return Collections.emptySet();
         }
         lemmas = nextSet;
      }

      final PartOfSpeech[] target = tags;
      lemmas = lemmas.stream().filter(lemma -> hasPartOfSpeech(lemma, target)).collect(Collectors.toSet());
      return lemmas;
   }

   private static class DetachmentRule implements Serializable, Function<String, String> {
      private static final long serialVersionUID = 2748362312310767937L;
      /**
       * The Ending.
       */
      public final String ending;
      /**
       * The Replacement.
       */
      public final String replacement;

      private DetachmentRule(String ending, String replacement) {
         this.ending = ending;
         this.replacement = replacement;
      }

      @Override
      public String apply(String input) {
         if(input == null) {
            return null;
         }
         if(input.endsWith(ending)) {
            int end = input.length() - ending.length();
            if(end == 0) {
               return replacement;
            }
            return input.substring(0, end) + replacement;
         }
         return input;
      }

      /**
       * Unapply string.
       *
       * @param input the input
       * @return the string
       */
      public String unapply(String input) {
         if(input == null) {
            return null;
         }
         if(input.endsWith(replacement)) {
            int end = input.length() - replacement.length();
            if(end == 0) {
               return ending;
            }
            return input.substring(0, end) + ending;
         }
         return input;
      }
   }

}//END OF EnglishLemmatizer
