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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.json.Json;
import lombok.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Specialized HashMap for storing {@link AttributeType}s and their values that correctly handles json serialization /
 * deserialization and allows for checked type gets.
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(keyUsing = AnnotatableType.KeyDeserializer.class)
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

   @JsonAnySetter
   private void put(String a, Object o) {
      put(AttributeType.make(a), o);
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

}//END OF AttributeMap
