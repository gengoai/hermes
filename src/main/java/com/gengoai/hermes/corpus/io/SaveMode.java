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

package com.gengoai.hermes.corpus.io;

import com.gengoai.io.resource.Resource;

import java.io.IOException;

/**
 * The enum Save mode.
 *
 * @author David B. Bracewell
 */
public enum SaveMode {
   /**
    * Throws an error if the directory / file exists
    */
   ERROR {
      @Override
      public boolean validate(Resource resource) throws IOException {
         if (resource.exists()) {
            throw new IOException(resource.path() + " already exists");
         }
         return true;
      }
   },
   /**
    * Ignores writing the corpus
    */
   IGNORE {
      @Override
      public boolean validate(Resource resource) throws IOException {
         return !resource.exists();
      }
   },
   /**
    * Overwrites the file/directory
    */
   OVERWRITE {
      @Override
      public boolean validate(Resource resource) throws IOException {
         if (resource.exists()) {
            resource.delete(true);
         }
         return true;
      }
   };

   /**
    * Performs validation on the given resource to check if writing is possible
    *
    * @param resource the resource to write to
    * @return True if the corpus can be written
    * @throws IOException Error in validation
    */
   public abstract boolean validate(Resource resource) throws IOException;
}//END OF SaveMode
