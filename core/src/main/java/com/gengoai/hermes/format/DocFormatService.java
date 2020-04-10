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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Service for handling {@link DocFormat} and {@link DocFormatProvider}.</p>
 */
public final class DocFormatService {
   /**
    * The suffix to add to formats to indicate the input has one document per line.
    */
   public static final String ONE_PER_LINE_SUFFIX = "_OPL";
   private static Map<String, DocFormatProvider> providerMap = new ConcurrentHashMap<>();

   static {
      for(DocFormatProvider provider : ServiceLoader.load(DocFormatProvider.class)) {
         providerMap.put(provider.getName().toUpperCase(), provider);
      }
   }

   private DocFormatService() {
      throw new IllegalAccessError();
   }

   /**
    * <p>Creates a {@link DocFormat} from the given specification, where the specification is in the form of:
    * <code>format_name[_opl]::[RESOURCE];PARAMETERS</code></p>
    *
    * @param specification the specification
    * @return the doc format
    */
   public static DocFormat create(@NonNull Specification specification) {
      String formatName = specification.getSchema().toUpperCase().strip();
      boolean isOPL = false;
      if(formatName.endsWith(ONE_PER_LINE_SUFFIX)) {
         formatName = formatName.substring(0, formatName.length() - ONE_PER_LINE_SUFFIX.length());
         isOPL = true;
      }
      DocFormatProvider provider = getProvider(formatName);
      DocFormatParameters formatParameters = provider.getDefaultFormatParameters();
      for(Map.Entry<String, String> e : specification.getQueryParameters()) {
         formatParameters.set(e.getKey(), e.getValue());
      }
      DocFormat format = provider.create(formatParameters);
      if(isOPL) {
         format = new OPLFormat(format);
      }
      return format;
   }

   /**
    * <p>Creates a {@link DocFormat} from the given specification, where the specification is in the form of:
    * <code>format_name[_opl]::[RESOURCE];PARAMETERS</code></p>
    *
    * @param specification the specification
    * @return the doc format
    */
   public static DocFormat create(@NonNull String specification) {
      return create(Specification.parse(specification));
   }

   /**
    * Gets the {@link DocFormatProvider} for the given format name.
    *
    * @param name the format name
    * @return the DocFormatProvider
    */
   public static DocFormatProvider getProvider(@NonNull String name) {
      name = name.toUpperCase();
      if(providerMap.containsKey(name)) {
         return providerMap.get(name);
      }
      throw new IllegalArgumentException("Invalid DocFormat: '" + name + "'");
   }

   /**
    * @return all registered {@link DocFormatProvider}s.
    */
   public static Collection<DocFormatProvider> getProviders() {
      return Collections.unmodifiableCollection(providerMap.values());
   }

}//END OF DocFormatService
