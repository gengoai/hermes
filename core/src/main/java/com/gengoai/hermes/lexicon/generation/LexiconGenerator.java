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

package com.gengoai.hermes.lexicon.generation;

import com.gengoai.Tag;
import com.gengoai.collection.multimap.Multimap;

/**
 * Defines a methodology for constructing a lexicon for a set of tags.
 *
 * @param <T> the tag type parameter
 * @author David B. Bracewell
 */
public interface LexiconGenerator<T extends Tag> {

   /**
    * Generate a lexicon (collection of strings) for a set of Tags
    *
    * @return the multimap of tag to lemma entries
    */
   Multimap<T, String> generate();

}//END OF LexiconGenerator
