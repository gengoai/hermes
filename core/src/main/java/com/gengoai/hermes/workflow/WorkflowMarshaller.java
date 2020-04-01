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
import com.gengoai.json.JsonMarshaller;
import com.gengoai.reflection.BeanMap;
import com.gengoai.reflection.Reflect;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class WorkflowMarshaller extends JsonMarshaller<Workflow> {

   protected static Action createBean(String name, JsonEntry entry, Map<String, Action> singletons) throws Exception {
      boolean isSingleton = entry.getBooleanProperty("@singleton", false);

      if (isSingleton && singletons.containsKey(name)) {
         return singletons.get(name);
      }

      BeanMap beanMap = new BeanMap(Reflect.onClass(entry.getStringProperty("@class")).create().get());
      Iterator<Map.Entry<String, JsonEntry>> itr = Iterators.filter(entry.propertyIterator(),
                                                                    e -> !e.getKey().startsWith("@"));
      while (itr.hasNext()) {
         Map.Entry<String, JsonEntry> e = itr.next();
         beanMap.put(e.getKey(), resolve(e.getValue()).getAs(beanMap.getType(e.getKey())));
      }
      if (isSingleton) {
         singletons.put(name, Cast.as(beanMap.getBean()));
      }
      return Cast.as(beanMap.getBean());
   }

   protected static JsonEntry resolve(JsonEntry e) {
      if (e.isString()) {
         String resolved = Config.resolveVariables(e.getAsString());
         if (resolved.equals(e.getAsString())) {
            return e;
         }
         try {
            return Json.parse(resolved);
         } catch (IOException ex) {
            Json.asJsonEntry(resolved);
         }
      }
      if (e.isArray()) {
         return JsonEntry.array(Iterables.transform(e::elementIterator, WorkflowMarshaller::resolve));
      }
      if (e.isObject()) {
         JsonEntry obj = JsonEntry.object();
         e.propertyIterator().forEachRemaining(a -> obj.addProperty(a.getKey(), resolve(a.getValue())));
         return obj;
      }
      return e;
   }

   @Override
   protected Workflow deserialize(JsonEntry entry, Type type) {
      switch (entry.getStringProperty("type", "Sequential").toLowerCase()) {
         case "sequential":
            return entry.getAs(SequentialWorkflow.class);
      }
      throw new IllegalStateException("Invalid Workflow type: '" + entry.getStringProperty("type") + "'");
   }

   protected Map<String, JsonEntry> readBeans(JsonEntry e) {
      Map<String, JsonEntry> beans = new HashMap<>();
      if (e.hasProperty("beans")) {
         e.getProperty("beans")
          .propertyIterator()
          .forEachRemaining(je -> {
             beans.put("@" + je.getKey(), je.getValue());
          });
      }
      return beans;
   }

   protected Context readDefaultContext(JsonEntry e) {
      Context defaultContext = new Context();
      if (e.hasProperty("context")) {
         e.getProperty("context")
          .propertyIterator()
          .forEachRemaining(je -> {
             Object o = je.getValue().get();
             defaultContext.property(je.getKey(), o);
          });
      }
      return defaultContext;
   }

   @Override
   protected JsonEntry serialize(Workflow workflow, Type type) {
      return JsonEntry.object()
                      .addProperty("type", workflow.getType())
                      .addProperty("context", workflow.getStartingContext());
   }

}//END OF WorkflowMarshaller
