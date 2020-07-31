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

package com.gengoai.hermes.workflow;

import com.gengoai.collection.Iterables;
import com.gengoai.collection.Iterators;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.BeanMap;
import com.gengoai.reflection.Reflect;
import lombok.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class BaseWorkflowIO {

   public static Action createBean(String name, JsonEntry entry, Map<String, Action> singletons) throws Exception {
      boolean isSingleton = entry.getBooleanProperty("@singleton", false);

      if(isSingleton && singletons.containsKey(name)) {
         return singletons.get(name);
      }
      System.out.println(entry.getStringProperty("@class"));
      BeanMap beanMap = new BeanMap(Reflect.onClass(entry.getStringProperty("@class")).create().get());
      Iterator<Map.Entry<String, JsonEntry>> itr = Iterators.filter(entry.propertyIterator(),
                                                                    e -> !e.getKey().startsWith("@"));
      while(itr.hasNext()) {
         Map.Entry<String, JsonEntry> e = itr.next();
         beanMap.put(e.getKey(), resolve(e.getValue()).as(beanMap.getType(e.getKey())));
      }
      if(isSingleton) {
         singletons.put(name, Cast.as(beanMap.getBean()));
      }
      return Cast.as(beanMap.getBean());
   }

   public static Map<String, JsonEntry> readBeans(JsonEntry e) {
      Map<String, JsonEntry> beans = new HashMap<>();
      if(e.hasProperty("beans")) {
         e.getProperty("beans")
          .propertyIterator()
          .forEachRemaining(je -> {
             beans.put("@" + je.getKey(), je.getValue());
          });
      }
      return beans;
   }

   public static Context readDefaultContext(JsonEntry e) {
      Context defaultContext = new Context();
      if(e.hasProperty("context")) {
         e.getProperty("context")
          .propertyIterator()
          .forEachRemaining(je -> {
             Object o = je.getValue().get();
             defaultContext.property(je.getKey(), o);
          });
      }
      return defaultContext;
   }

   protected static JsonEntry resolve(JsonEntry e) {
      if(e.isString()) {
         String resolved = Config.resolveVariables(e.asString());
         if(resolved.equals(e.asString())) {
            return e;
         }
         try {
            return Json.parse(resolved);
         } catch(IOException ex) {
            Json.asJsonEntry(resolved);
         }
      }
      if(e.isArray()) {
         return JsonEntry.array(Iterables.transform(e::elementIterator, BaseWorkflowIO::resolve));
      }
      if(e.isObject()) {
         JsonEntry obj = JsonEntry.object();
         e.propertyIterator().forEachRemaining(a -> obj.addProperty(a.getKey(), resolve(a.getValue())));
         return obj;
      }
      return e;
   }

   public static JsonEntry serialize(@NonNull Workflow workflow) {
      return JsonEntry.object()
                      .addProperty("context", workflow.getStartingContext());
   }

}//END OF BaseWorkflowIO
