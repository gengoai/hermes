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

import com.gengoai.conversion.Cast;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.conversion.TypeConverter;
import com.gengoai.json.JsonEntry;
import org.kohsuke.MetaInfServices;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Mango Converter to automatically Convert other objects (Json and Strings) into {@link AnnotatableType}s
 *
 * @author David B. Bracewell
 */
@MetaInfServices
public class AnnotatableTypeConverter implements TypeConverter, Serializable {
   private static final long serialVersionUID = 1L;

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      Type target = parameters == null ? AnnotatableType.class : parameters[0];
      if (source instanceof JsonEntry) {
         return Cast.<JsonEntry>as(source).getAs(target);
      }
      return AnnotatableType.valueOf(source.toString());
   }

   @Override
   public Class[] getConversionType() {
      return new Class[]{AnnotatableType.class, AnnotationType.class, AttributeType.class, RelationType.class};
   }

}//END OF AnnotatableTypeConverter
