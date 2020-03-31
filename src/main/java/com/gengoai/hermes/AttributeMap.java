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

package com.gengoai.hermes;

import com.gengoai.annotation.JsonHandler;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import lombok.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Specialized HashMap for storing {@link AttributeType}s and their values that correctly handles json serialization /
 * deserialization and allows for checked type gets.
 *
 * @author David B. Bracewell
 */
@JsonHandler(AttributeMap.AttributeMapMarshaller.class)
public class AttributeMap extends HashMap<AttributeType<?>, Object> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Attribute map.
    */
   public AttributeMap() {
      super(5);
   }

   /**
    * Gets the value of the given attribute
    *
    * @param <T>           the type of the attribute
    * @param attributeType the attribute type
    * @return the value of the attribute
    */
   public <T> T get(@NonNull AttributeType<T> attributeType) {
      return Cast.as(get((Object) attributeType));
   }

   /**
    * Gets the value of the given attribute or returns the default value if it is not in the map
    *
    * @param <T>           the type of the attribute
    * @param attributeType the attribute type
    * @param defaultValue  the default value
    * @return the value of the attribute or the default value if the attribute is not in the map
    */
   public <T> T getOrDefault(@NonNull AttributeType<T> attributeType, T defaultValue) {
      return Cast.as(getOrDefault((Object) attributeType, defaultValue));
   }

   @Override
   public Object put(@NonNull AttributeType attributeType, Object value) {
      if(value == null) {
         return Cast.as(remove(attributeType));
      } else if(value instanceof Val) {
         value = Cast.<Val>as(value).as(attributeType.getValueType());
      }
      return Cast.as(super.put(attributeType, attributeType.decode(value)));
   }

   @Override
   public void putAll(@NonNull Map<? extends AttributeType<?>, ?> map) {
      map.forEach(this::put);
   }

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      this.putAll(Json.parse(ois.readUTF(), AttributeMap.class));
   }

   private void writeObject(ObjectOutputStream oos) throws IOException {
      oos.writeUTF(Json.dumps(this));
   }

   static class AttributeMapMarshaller extends com.gengoai.json.JsonMarshaller<AttributeMap> {

      @Override
      protected AttributeMap deserialize(JsonEntry entry, Type type) {
         AttributeMap map = new AttributeMap();
         entry.propertyIterator()
              .forEachRemaining(e -> {
                 AttributeType<?> at = AttributeType.make(e.getKey());
                 map.put(at, e.getValue().getAs(at.getValueType()));
              });
         return map;
      }

      @Override
      protected JsonEntry serialize(AttributeMap attributeMap, Type type) {
         JsonEntry map = JsonEntry.object();
         attributeMap.forEach((k, v) -> map.addProperty(k.name(), v));
         return map;
      }
   }

}//END OF AttributeMap
