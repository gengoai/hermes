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

/**
 * <p>A provider for {@link DocFormat} for use within Java's service loader framework. Each provider defines the
 * name of the format to identify the document format to read and to use the format with one-per-line, you can append
 * "_opl" to the format name.  </p>
 */
public interface DocFormatProvider {

   /**
    * Creates an instance of the provided {@link DocFormat} with the given set of {@link DocFormatParameters}
    *
    * @param parameters the format parameters
    * @return the doc format
    */
   DocFormat create(DocFormatParameters parameters);

   /**
    * @return the default DocFormatParameters
    */
   default DocFormatParameters getDefaultFormatParameters() {
      return new DocFormatParameters();
   }

   /**
    * @return the format name used to identify it when constructing document collections.
    */
   String getName();

   /**
    * @return True if this format supports writing, False if not.
    */
   boolean isWriteable();

}//END OF DocFormatProvider
