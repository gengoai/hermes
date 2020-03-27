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
import com.gengoai.stream.StreamingContext;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.Iterator;

public class HermesJsonFormat extends OneDocPerFileFormat {
   private static final long serialVersionUID = 1L;

   @Override
   public Iterator<Document> read(Resource inputResource) {
      return StreamingContext.local()
                             .textFile(inputResource, true)
                             .map(Document::fromJson)
                             .iterator();
   }

   @Override
   public void write(Document document, Resource outputResource) throws IOException {
      outputResource.write(document.toJson());
   }

   @MetaInfServices
   public static class Provider implements DocFormatProvider {

      @Override
      public DocFormat create(DocFormatParameters parameters) {
         return new HermesJsonFormat();
      }

      @Override
      public String getName() {
         return "HJSON";
      }

      @Override
      public boolean isWriteable() {
         return true;
      }
   }

}//END OF HermesJsonFormat
