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
 */

package com.gengoai.hermes.wordnet.io.properties;

import com.gengoai.Validation;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.wordnet.Synset;
import com.gengoai.hermes.wordnet.WordNetPOS;
import com.gengoai.hermes.wordnet.io.WordNetDB;
import com.gengoai.hermes.wordnet.io.WordNetPropertyLoader;
import com.gengoai.hermes.wordnet.properties.InformationContent;
import com.gengoai.hermes.wordnet.properties.PropertyName;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;
import com.gengoai.string.Strings;

/**
 * @author David B. Bracewell
 */
public class InformationContentLoader extends WordNetPropertyLoader {

   private final Resource resource;
   private final PropertyName propertyName;

   public InformationContentLoader(Resource resource, String resourceName) {
      Validation.checkArgument(!Strings.isNullOrBlank(resourceName));
      this.resource = Validation.notNull(resource);
      this.propertyName = PropertyName.make(resourceName);
   }

   @Override
   public void load(WordNetDB db) {
      Counter<String> ic = Counters.newCounter();
      Counter<PartOfSpeech> roots = Counters.newCounter();
      try(MStream<String> stream = resource.lines()) {
         stream.forEach(line -> {
            line = line.trim();
            if(!Strings.isNullOrBlank(line) && !line.startsWith("wnver")) {
               String[] parts = line.split("\\s+");
               String key = parts[0];
               double cnt = Double.parseDouble(parts[1]);
               ic.set(key, cnt);
               if(parts.length == 3 && parts[2].equalsIgnoreCase("ROOT")) {
                  roots.increment(WordNetPOS.fromString(key.substring(key.length() - 1)).toHermesPOS(), cnt);
               }
            }
         });
      } catch(Exception ioe) {
         throw new RuntimeException(ioe);
      }
      for(String key : ic.items()) {
         Synset synset = db.getSynsetFromId(
               Strings.padStart(key, 9, '0').toLowerCase());
         setProperty(
               synset,
               propertyName,
               new InformationContent(ic.get(key) / roots.get(synset.getPOS()))
                    );
      }
   }

}//END OF InformationContentLoader
