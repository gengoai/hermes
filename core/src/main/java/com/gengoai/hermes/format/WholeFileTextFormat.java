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
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;

import java.util.stream.Stream;

/**
 * <p>Defines a format in which files need to be completely read in order to generate documents. Most formats will us
 * this as a base class.</p>
 */
public abstract class WholeFileTextFormat implements DocFormat {

   @Override
   public final MStream<Document> read(Resource inputResource) {
      MStream<Document> stream = StreamingContext.get(getParameters().distributed.value())
                                                 .textFile(inputResource, true)
                                                 .flatMap(this::readSingleFile);
      if(getParameters().distributed.value()) {
         stream = stream.cache();
      }
      return stream;
   }

   /**
    * Converts the content of an entire file into one ore more documents.
    *
    * @param content the content
    * @return the stream of documents.
    */
   protected abstract Stream<Document> readSingleFile(String content);

}//END OF WholeFileTextFormat
