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

import com.gengoai.collection.Sets;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.DefaultTokenTypeEntityAnnotator;
import com.gengoai.hermes.annotator.SubTypeAnnotator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Default Entity annotator for English
 */
public class ENEntityAnnotator extends SubTypeAnnotator implements Serializable {
   private static final long serialVersionUID = 1L;
   private final DefaultTokenTypeEntityAnnotator tokenTypeEntityAnnotator = new DefaultTokenTypeEntityAnnotator();

   /**
    * Instantiates a new ENEntityAnnotator.
    */
   public ENEntityAnnotator() {
      super(Types.ENTITY, false, Arrays.asList(Types.TOKEN_TYPE_ENTITY, Types.ML_ENTITY));
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Sets.union(Collections.singleton(Types.ENTITY),
                        tokenTypeEntityAnnotator.satisfies());
   }

}//END OF EnEntityAnnotator
