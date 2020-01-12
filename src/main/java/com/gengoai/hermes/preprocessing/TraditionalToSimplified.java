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

package com.gengoai.hermes.preprocessing;

import com.gengoai.Language;
import com.ibm.icu.text.Transliterator;

/**
 * <p>
 * Preprocessor that converts traditional characters into simplified characters. Will only perform the normalization if
 * the language is CHINESE.
 * </p>
 *
 * @author David B. Bracewell
 */
public class TraditionalToSimplified extends TextNormalizer {
  private static final long serialVersionUID = 1L;

  @Override
  protected String performNormalization(String input, Language language) {
    if (language == Language.CHINESE) {
      return Transliterator.getInstance("Traditional-Simplified").transform(input);
    }
    return input;
  }

}//END OF TraditionalToSimplified
