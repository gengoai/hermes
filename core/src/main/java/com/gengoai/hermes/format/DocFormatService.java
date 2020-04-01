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

import com.gengoai.specification.Specification;
import lombok.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DocFormatService {
   public static final String ONE_PER_LINE_SUFFIX = "_OPL";
   private static Map<String, DocFormatProvider> providerMap = new ConcurrentHashMap<>();

   static {
      for(DocFormatProvider provider : ServiceLoader.load(DocFormatProvider.class)) {
         providerMap.put(provider.getName().toUpperCase(), provider);
      }
   }

   public static DocFormat create(@NonNull Specification spec) {
      String formatName = spec.getSchema().toUpperCase().strip();
      boolean isOPL = false;
      if(formatName.endsWith(ONE_PER_LINE_SUFFIX)) {
         formatName = formatName.substring(0, formatName.length() - ONE_PER_LINE_SUFFIX.length());
         isOPL = true;
      }
      DocFormatProvider provider = getProvider(formatName);
      DocFormatParameters formatParameters = provider.getDefaultFormatParameters();
      for(Map.Entry<String, String> e : spec.getQueryParameters()) {
         formatParameters.set(e.getKey(), e.getValue());
      }
      DocFormat format = provider.create(formatParameters);
      if(isOPL) {
         format = new OPLFormat(format);
      }
      return format;
   }

   public static DocFormat create(String specification) {
      return create(Specification.parse(specification));
   }


   public static Collection<DocFormatProvider> getProviders(){
      return Collections.unmodifiableCollection(providerMap.values());
   }

   public static DocFormatProvider getProvider(@NonNull String name) {
      name = name.toUpperCase();
      if(providerMap.containsKey(name)) {
         return providerMap.get(name);
      }
      throw new IllegalArgumentException("Invalid DocFormat: '" + name + "'");
   }

}//END OF DocFormatService
