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

package com.gengoai.hermes.tools.ui.components;

import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import lombok.NonNull;

import java.util.*;

public class AvailableAnnotationLayers {
   private static final Map<String, AnnotationLayer> layers = new TreeMap<>();

   public static AnnotationLayer get(String name) {
      return layers.get(name);
   }

   public static void loadFrom(@NonNull Resource dir) throws Exception {
      for(Resource child : dir.getChildren("*.json")) {
         AnnotationLayer layer = Json.parse(child, AnnotationLayer.class);
         layers.put(layer.getName(), layer);
      }
   }

   public static Set<String> names() {
      return layers.keySet();
   }

   public static Collection<AnnotationLayer> values() {
      return Collections.unmodifiableCollection(layers.values());
   }

}//END OF AnnotationLayerList
