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

import com.gengoai.hermes.morphology.PorterStemmer;
import com.gengoai.hermes.morphology.Stemmer;
import lombok.NonNull;

import java.io.Serializable;

/**
 * The type English stemmer.
 *
 * @author David B. Bracewell
 */
public class ENStemmer implements Stemmer, Serializable {
  private static final long serialVersionUID = -8723194306867645802L;

  @Override
  public String stem(@NonNull String string) {
    PorterStemmer stemmer = new PorterStemmer();
    stemmer.add(string.toCharArray(), string.length());
    stemmer.stem();
    if (stemmer.getResultLength() > 0) {
      return new String(stemmer.getResultBuffer(), 0, stemmer.getResultLength());
    }
    return string;
  }


}//END OF EnglishStemmer
