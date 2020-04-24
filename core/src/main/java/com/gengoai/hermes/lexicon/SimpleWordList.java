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

package com.gengoai.hermes.lexicon;

import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Simple implementation of a {@link WordList} backed by a HashSet
 *
 * @author David B. Bracewell
 */
public class SimpleWordList implements WordList, Serializable {
   private static final long serialVersionUID = 1L;
   private final Set<String> words;

   /**
    * <p>
    * Reads the word list from the given resource where each term is on its own line and "#" represents comments.
    * </p>
    * <p>
    * Note that convention states that if the first line of a word list is a comment stating "case-insensitive" then
    * loading of that word list will result in all words being lower-cased.
    * </p>
    *
    * @param resource the resource
    * @return the word list
    * @throws IOException the io exception
    */
   public static WordList read(Resource resource) throws IOException {
      final Set<String> words = new HashSet<>();
      boolean firstLine = true;
      boolean isLowerCase = false;
      try(MStream<String> lines = resource.lines()) {
         for(String line : lines) {
            line = line.strip();
            if(firstLine && line.startsWith("#")) {
               isLowerCase = line.contains("case-insensitive");
            }
            firstLine = false;
            if(!line.startsWith("#")) {
               if(isLowerCase) {
                  words.add(line.toLowerCase());
               } else {
                  words.add(line);
               }
            }
         }
      } catch(Exception e) {
         throw new IOException(e);
      }
      return new SimpleWordList(words);
   }

   /**
    * Instantiates a new Simple word list.
    *
    * @param words the words
    */
   public SimpleWordList(@NonNull Collection<String> words) {
      this.words = new HashSet<>(words);
   }

   @Override
   public boolean contains(String string) {
      return words.contains(string);
   }

   @Override
   public Iterator<String> iterator() {
      return words.iterator();
   }

   @Override
   public int size() {
      return words.size();
   }

}//END OF SimpleWordList
