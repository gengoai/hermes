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

package com.gengoai.hermes.tools.ui.components;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.Tag;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.AttributeType;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.RMethod;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import com.gengoai.reflection.TypeUtils;
import com.gengoai.swing.Colors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.awt.Color;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The type Annotation layer.
 */
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.NONE,
      setterVisibility = JsonAutoDetect.Visibility.NONE,
      getterVisibility = JsonAutoDetect.Visibility.NONE,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class AnnotationLayer implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The constant DEFAULT_COLOR.
    */
   public static final Color DEFAULT_COLOR = new Color(200, 200, 200);
   String name;
   AnnotationType annotationType;
   AttributeType<? extends Tag> attributeType;
   Map<Tag, Color> tagColors = new ConcurrentHashMap<>();
   Set<Tag> validTags = new TreeSet<>(Comparator.comparing(Tag::name));
   boolean includeAllTags;

   private static Collection<Tag> generateAllKnownTagsFor(@NonNull AttributeType<? extends Tag> attributeType) {
      try {
         RMethod valuesMethod = Reflect.onClass(attributeType.getValueType()).getMethod("values");
         if(TypeUtils.isArray(valuesMethod.getType())) {
            return Arrays.asList(valuesMethod.<Tag[]>invoke());
         } else {
            return valuesMethod.<Collection<Tag>>invoke();
         }
      } catch(ReflectionException e) {
         throw new RuntimeException(e);
      }
   }

   @JsonCreator
   private AnnotationLayer(@JsonProperty("name") @NonNull String name,
                           @JsonProperty("annotationType") @NonNull AnnotationType annotationType,
                           @JsonProperty("attributeType") AttributeType<? extends Tag> attributeType,
                           @JsonProperty("includeAllTags") boolean includeTagValues,
                           @JsonProperty("tags") @NonNull Map<String, String> tags
                          ) {
      this.name = name;
      this.annotationType = annotationType;
      this.attributeType = attributeType == null
                           ? annotationType.getTagAttribute()
                           : attributeType;
      this.includeAllTags = includeTagValues;
      if(includeTagValues) {
         validTags.addAll(generateAllKnownTagsFor(getAttributeType()));
      }
      tags.forEach((tagStr, colorStr) -> {
         Tag tag = getAttributeType().decode(tagStr);
         validTags.add(tag);
         tagColors.put(tag, Colors.parseColor(colorStr));
      });
   }

   /**
    * Instantiates a new Annotation layer.
    *
    * @param annotationType the annotation type
    * @param attributeType  the attribute type
    */
   public AnnotationLayer(@NonNull String name,
                          @NonNull AnnotationType annotationType,
                          @NonNull AttributeType<? extends Tag> attributeType) {
      this(name, annotationType, attributeType, generateAllKnownTagsFor(attributeType), Collections.emptyMap(), true);

   }

   /**
    * Instantiates a new Annotation layer.
    *
    * @param annotationType the annotation type
    * @param attributeType  the attribute type
    * @param validTags      the valid tags
    * @param colors         the colors
    */
   public AnnotationLayer(@NonNull String name,
                          @NonNull AnnotationType annotationType,
                          @NonNull AttributeType<? extends Tag> attributeType,
                          @NonNull Collection<? extends Tag> validTags,
                          @NonNull Map<Tag, Color> colors,
                          boolean includeAllTags) {
      this.name = name;
      this.annotationType = annotationType;
      this.attributeType = attributeType;
      this.validTags.addAll(validTags);
      this.tagColors.putAll(colors);
      this.includeAllTags = includeAllTags;
   }

   /**
    * Instantiates a new Annotation layer.
    *
    * @param annotationType the annotation type
    */
   public AnnotationLayer(@NonNull String name, @NonNull AnnotationType annotationType) {
      this(name, annotationType, annotationType.getTagAttribute());
   }

   /**
    * Gets color.
    *
    * @param tag the tag
    * @return the color
    */
   public Color getColor(@NonNull String tag) {
      return getColor(getAttributeType().decode(tag));
   }

   /**
    * Gets color.
    *
    * @param tag the tag
    * @return the color
    */
   public Color getColor(@NonNull Tag tag) {
      if(tagColors.containsKey(tag)) {
         return tagColors.get(tag);
      } else {
         var parent = tag.parent();
         while(parent != null) {
            if(validTags.contains(parent) && tagColors.containsKey(parent)) {
               var color = tagColors.get(parent);
               tagColors.put(tag, color);
               return color;
            }
            parent = parent.parent();
         }
         return new Color(200, 200, 200);
      }
   }

   /**
    * Gets valid tag labels.
    *
    * @return the valid tag labels
    */
   public List<String> getValidTagLabels() {
      return validTags.stream().map(Tag::label).sorted().collect(Collectors.toList());
   }

   /**
    * Gets valid tags.
    *
    * @return the valid tags
    */
   public Set<Tag> getValidTags() {
      return Collections.unmodifiableSet(validTags);
   }

   /**
    * Get valid tags array tag [ ].
    *
    * @return the tag [ ]
    */
   public Tag[] getValidTagsArray() {
      return validTags.toArray(new Tag[0]);
   }

   /**
    * Has color defined boolean.
    *
    * @param tag the tag
    * @return the boolean
    */
   public boolean hasColorDefined(Tag tag) {
      return tagColors.containsKey(tag);
   }

   /**
    * Is valid tag boolean.
    *
    * @param tag the tag
    * @return the boolean
    */
   public boolean isValidTag(Tag tag) {
      return validTags.contains(tag);
   }

   /**
    * Sets color.
    *
    * @param tag   the tag
    * @param color the color
    * @return the color
    */
   public boolean setColor(@NonNull Tag tag, @NonNull Color color) {
      if(validTags.contains(tag)) {
         tagColors.put(tag, color);
         return true;
      }
      return false;
   }

   /**
    * Sets valid tags.
    *
    * @param tagSet the tag set
    */
   public void setValidTags(@NonNull Collection<? extends Tag> tagSet) {
      validTags.clear();
      validTags.addAll(tagSet);
   }

   @JsonValue
   protected JsonEntry toJson() {
      JsonEntry obj = JsonEntry.object();
      obj.addProperty("name", name);
      obj.addProperty("annotationType", annotationType.name());
      obj.addProperty("attributeType", attributeType.name());
      obj.addProperty("includeAllTags", includeAllTags);
      JsonEntry tags = JsonEntry.object();
      tagColors.forEach((tag, color) -> tags.addProperty(tag.label(),
                                                         Colors.toHexString(color)));
      obj.addProperty("tags", tags);
      return obj;
   }

   @Override
   public String toString() {
      return name;
   }

}//END OF AnnotationLayer
