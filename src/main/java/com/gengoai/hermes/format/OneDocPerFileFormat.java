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

package com.gengoai.hermes.format;

import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.io.Serializable;

public interface OneDocPerFileFormat extends DocFormat, Serializable {

   @Override
   default void write(Corpus corpus, Resource outputResource) throws IOException {
      if(!outputResource.isDirectory()) {
         throw new IOException(outputResource.descriptor() + " must be a directory");
      }
      outputResource.mkdirs();
      int nw = Long.toString(corpus.size()).length();
      long i = 0;
      for(Document document : corpus) {
         write(document, outputResource.getChild("part-" + Strings.padStart(Long.toString(i), nw, '0')));
         i++;
      }
   }

}//END OF OneDocPerFileFormat
