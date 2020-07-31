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

package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.transform.Transform;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import lombok.NonNull;

public interface ContextualizedEmbedding extends Transform {

   default HString transform(@NonNull HString hString, @NonNull AnnotationType annotationType) {
      Datum datum = new Datum();
      for (Annotation annotation : hString.annotations(annotationType)) {
         datum.put(Long.toString(annotation.getId()), Variable.binary(annotation.toString()));
      }
      datum = transform(datum);
      for (String s : datum.keySet()) {
         Annotation annotation = hString.document().annotation(Long.parseLong(s));
         annotation.put(Types.EMBEDDING, datum.get(s).asNDArray());
      }
      return hString;
   }

}//END OF ContextualizedEmbedding
