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

package com.gengoai.hermes.extraction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import lombok.NonNull;

/**
 * Fundamental to text mining in Hermes is the concept of a <code>Extractor<code> and the {@link Extraction} it
 * produces. Extractors are responsible for taking an {@link HString} as input and producing an
 * <code>Extraction</code>.
 *
 * @author David B. Bracewell
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,defaultImpl = LyreExpression.class)
public interface Extractor {

   /**
    * Generate an {@link Extraction} from the given {@link HString}.
    *
    * @param hString the source text from which we will generate an Extraction
    * @return the Extraction
    */
   Extraction extract(@NonNull HString hString);

}//END OF Extractor
