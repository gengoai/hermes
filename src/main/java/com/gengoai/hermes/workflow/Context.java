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

import com.gengoai.Copyable;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.json.JsonEntry;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = false)
@JsonHandler(Context.JsonMarshaller.class)
public class Context implements Serializable, Copyable<Context> {
   private final Map<String, Object> properties = new HashMap<>();


   /**
    * Instantiates a new Processor context.
    */
   public Context() {

   }

   public Object get(String name){
      return properties.get(name);
   }

   /**
    * Instantiates a new Processor context.
    *
    * @param properties the properties
    */
   @Builder
   public Context(@Singular @NonNull Map<String, ?> properties) {
      this.properties.putAll(properties);
   }

   @Override
   public Context copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Gets as.
    *
    * @param <T>   the type parameter
    * @param name  the name
    * @param clazz the clazz
    * @return the as
    */
   public <T> T getAs(String name, @NonNull Class<T> clazz) {
      if (properties.containsKey(name)) {
         return Cast.as(properties.get(name), clazz);
      }
      return Config.get(name).as(clazz);

   }

   /**
    * Gets as.
    *
    * @param <T>  the type parameter
    * @param name the name
    * @param type the clazz
    * @return the as
    */
   public <T> T getAs(String name, @NonNull Type type) {
      if (properties.containsKey(name)) {
         return Converter.convertSilently(properties.get(name), type);
      }
      return Config.get(name).as(type);

   }

   /**
    * Gets as.
    *
    * @param <T>          the type parameter
    * @param name         the name
    * @param clazz        the clazz
    * @param defaultValue the default value
    * @return the as
    */
   public <T> T getAs(String name, @NonNull Class<T> clazz, T defaultValue) {
      if (properties.containsKey(name)) {
         return Cast.as(properties.getOrDefault(name, defaultValue), clazz);
      }
      return Config.get(name).as(clazz, defaultValue);
   }

   /**
    * Gets double.
    *
    * @param name the name
    * @return the double
    */
   public Double getDouble(String name) {
      return getAs(name, Double.class);
   }

   /**
    * Gets double.
    *
    * @param name         the name
    * @param defaultValue the default value
    * @return the double
    */
   public Double getDouble(String name, double defaultValue) {
      return getAs(name, Double.class, defaultValue);
   }

   /**
    * Gets integer.
    *
    * @param name the name
    * @return the integer
    */
   public Integer getInteger(String name) {
      return getAs(name, Integer.class);
   }

   /**
    * Gets integer.
    *
    * @param name         the name
    * @param defaultValue the default value
    * @return the integer
    */
   public Integer getInteger(String name, int defaultValue) {
      return getAs(name, Integer.class, defaultValue);
   }

   /**
    * Gets string.
    *
    * @param name the name
    * @return the string
    */
   public String getString(String name) {
      return getAs(name, String.class);
   }

   /**
    * Gets string.
    *
    * @param name         the name
    * @param defaultValue the default value
    * @return the string
    */
   public String getString(String name, String defaultValue) {
      return getAs(name, String.class, defaultValue);
   }

   public void merge(@NonNull Context other) {
      this.properties.putAll(other.properties);
   }

   /**
    * Property.
    *
    * @param name  the name
    * @param value the value
    */
   public void property(String name, Object value) {
      this.properties.put(name, value);
   }

   @Override
   public String toString() {
      return "Context" + properties.toString() + "";
   }

   public static class JsonMarshaller extends com.gengoai.json.JsonMarshaller<Context> {

      @Override
      protected Context deserialize(JsonEntry entry, Type type) {
         Context context = new Context();
         entry.propertyIterator()
              .forEachRemaining(e -> context.properties.put(e.getKey(), e.getValue().get()));
         return context;
      }

      @Override
      protected JsonEntry serialize(Context context, Type type) {
         JsonEntry entry = JsonEntry.object();
         context.properties
            .forEach((k, v) -> {
               if (v != null) {
                  entry.addProperty(k, JsonEntry.object(v.getClass(), v));
               }
            });
         return entry;
      }
   }

}//END OF Context
