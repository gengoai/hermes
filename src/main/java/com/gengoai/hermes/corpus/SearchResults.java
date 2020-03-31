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

package com.gengoai.hermes.corpus;

import com.gengoai.hermes.Document;

/**
 * <p>A collection of {@link Document} obtained via querying a {@link Corpus}. Search results can be converted
 * to a limited use {@link Corpus} using the <code>asCorpus</code> method, but not all corpus functionality is
 * allowed.</p>
 *
 * @author David B. Bracewell
 */
public interface SearchResults extends DocumentCollection {

   /**
    * Gets the query generating this search results.
    *
    * @return the query generating this search results
    */
   Query getQuery();

}//END OF SearchResults
