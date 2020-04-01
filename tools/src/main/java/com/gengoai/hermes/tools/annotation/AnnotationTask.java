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

package com.gengoai.hermes.tools.annotation;

import com.gengoai.Tag;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.AttributeType;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.json.JsonMarshaller;
import com.gengoai.reflection.RMethod;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import com.gengoai.reflection.TypeUtils;
import com.gengoai.swing.ColorUtils;
import com.gengoai.hermes.tools.ui.components.TagInfo;
import com.gengoai.hermes.tools.ui.components.TagModel;
import lombok.Value;

import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Value
@JsonHandler(AnnotationTask.Marshaller.class)
public class AnnotationTask implements Serializable {
   private String name;
   private String view;
   private AnnotationType annotationType;
   private AttributeType<? extends Tag> attributeType;
   private boolean includeAllValues;
   private boolean randomColors;
   private List<TagInfo> tagInfos;

   public static AnnotationTask load(Resource resource) {
      try {
         return Json.parseObject(resource, AnnotationTask.class);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   public TagModel createTagModel() {
      try {
         RMethod valuesMethod = Reflect.onClass(attributeType.getValueType()).getMethod("values");
         TagModel.Builder builder;
         if(includeAllValues) {
            if(TypeUtils.isArray(valuesMethod.getType())) {
               builder = TagModel.builder(valuesMethod.<Tag[]>invoke());
            } else {
               builder = TagModel.builder(valuesMethod.<Collection<Tag>>invoke());
            }
         } else {
            builder = TagModel.builder();
         }

         builder.update(tagInfos.iterator());
         for(TagInfo tagInfo : tagInfos) {
            builder.instancesOf(tagInfo.getTag())
                   .filter(TagInfo::isDefaultColor)
                   .forEach(ti -> ti.setColor(tagInfo.getColor()));
         }

         if(randomColors) {
            builder.forEach((tag, ti) -> {
               if(ti.isDefaultColor()) {
                  tagInfos.add(ti);
                  ti.setColor(ColorUtils.randomColor());
               }
            });
         }

         return builder.build();
      } catch(ReflectionException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public String toString() {
      return name;
   }

   public static class Marshaller extends JsonMarshaller<AnnotationTask> {

      @Override
      protected AnnotationTask deserialize(JsonEntry entry, Type type) {
         final AnnotationType annotationType = entry.getProperty("annotationType").getAs(AnnotationType.class);
         final String name = entry.getStringProperty("name", annotationType.name());
         final AttributeType<? extends Tag> attributeType = annotationType.getTagAttribute();
         final boolean randomColors = entry.getBooleanProperty("randomColors", false);
         final boolean includeAllValues = entry.getBooleanProperty("includeTagValues", true);
         final String view = entry.getStringProperty("view");
         final List<TagInfo> tagInfos = new ArrayList<>();
         if(entry.hasProperty("tags")) {
            entry.getProperty("tags")
                 .propertyIterator()
                 .forEachRemaining(e -> {
                    final String color = e.getValue().getStringProperty("color", null);
                    final String shortcut = e.getValue().getStringProperty("shortcut", null);
                    tagInfos.add(new TagInfo(
                          attributeType.decode(e.getKey()),
                          color == null
                          ? null
                          : ColorUtils.parseColor(color),
                          shortcut == null
                          ? null
                          : KeyStroke.getKeyStroke(shortcut)
                    ));
                 });
         }
         return new AnnotationTask(name, view, annotationType, attributeType, includeAllValues, randomColors, tagInfos);
      }

      @Override
      protected JsonEntry serialize(AnnotationTask annotationTask, Type type) {
         return null;
      }
   }

}//END OF AnnotationTask
