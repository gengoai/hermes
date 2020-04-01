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

import com.gengoai.Tag;
import com.gengoai.annotation.JsonHandler;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.tools.annotation.AnnotationModel;
import com.gengoai.json.JsonEntry;
import com.gengoai.json.JsonMarshaller;
import com.gengoai.reflection.RMethod;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import com.gengoai.reflection.TypeUtils;
import com.gengoai.swing.ColorUtils;
import lombok.NonNull;
import lombok.Value;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The type Annotation layer.
 */
@Value
@JsonHandler(AnnotationLayer.Marshaller.class)
public class AnnotationLayer implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The constant DEFAULT_COLOR.
    */
   public static final Color DEFAULT_COLOR = new Color(200, 200, 200);
   AnnotationType annotationType;
   AttributeType<? extends Tag> attributeType;
   Map<Tag, Color> tagColors = new ConcurrentHashMap<>();
   Set<Tag> validTags = new TreeSet<>(Comparator.comparing(Tag::name));

   /**
    * Instantiates a new Annotation layer.
    *
    * @param annotationType the annotation type
    * @param attributeType  the attribute type
    */
   public AnnotationLayer(@NonNull AnnotationType annotationType,
                          @NonNull AttributeType<? extends Tag> attributeType) {
      this(annotationType, attributeType, generateAllKnownTagsFor(attributeType), Collections.emptyMap());
   }

   /**
    * Instantiates a new Annotation layer.
    *
    * @param annotationType the annotation type
    * @param attributeType  the attribute type
    * @param validTags      the valid tags
    * @param colors         the colors
    */
   public AnnotationLayer(@NonNull AnnotationType annotationType,
                          @NonNull AttributeType<? extends Tag> attributeType,
                          @NonNull Collection<? extends Tag> validTags,
                          @NonNull Map<Tag, Color> colors) {
      this.annotationType = annotationType;
      this.attributeType = attributeType;
      this.validTags.addAll(validTags);
//      colors.forEach((tag, color) -> tagColors.put(tag.label(), color));
      this.tagColors.putAll(colors);
   }

   /**
    * Instantiates a new Annotation layer.
    *
    * @param annotationType the annotation type
    */
   public AnnotationLayer(@NonNull AnnotationType annotationType) {
      this(annotationType, annotationType.getTagAttribute());
   }

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

   /**
    * Create annotation model for annotation model.
    *
    * @param hString the h string
    * @return the annotation model
    */
   public AnnotationModel createAnnotationModelFor(@NonNull HString hString) {
      AnnotationModel model = new AnnotationModel(annotationType);
      model.addAnnotations(hString);
      return model;
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

   /**
    * The type Marshaller.
    */
   public static class Marshaller extends JsonMarshaller<AnnotationLayer> {

      @Override
      protected AnnotationLayer deserialize(JsonEntry entry, Type type) {
         final AnnotationType annotationType = Types.annotation(entry.getStringProperty("annotationType"));
         final AttributeType<? extends Tag> attributeType;
         if(entry.hasProperty("attributeType")) {
            attributeType = Cast.as(Types.attribute(entry.getStringProperty("attributeType")));
         } else {
            attributeType = annotationType.getTagAttribute();
         }
         final Collection<Tag> validTags;
         if(entry.getBooleanProperty("includeAllTags", false)) {
            validTags = new HashSet<>(generateAllKnownTagsFor(attributeType));
         } else {
            validTags = new ArrayList<>();
         }
         final Map<Tag, Color> colors = new HashMap<>();
         if(entry.hasProperty("tags")) {
            entry.getProperty("tags")
                 .propertyIterator()
                 .forEachRemaining(e -> {
                    Tag tag = attributeType.decode(e.getKey());
                    validTags.add(tag);
                    colors.put(tag, ColorUtils.parseColor(e.getValue().getAsString()));
                 });
         }

         return new AnnotationLayer(annotationType, attributeType, validTags, colors);
      }

      @Override
      protected JsonEntry serialize(AnnotationLayer annotationLayer, Type type) {
         JsonEntry obj = JsonEntry.object();
         obj.addProperty("annotationType", annotationLayer.annotationType.name());
         obj.addProperty("attributeType", annotationLayer.attributeType.name());
         obj.addProperty("includeAllTags",
                         annotationLayer.getValidTagLabels()
                                        .size() == generateAllKnownTagsFor(annotationLayer.attributeType).size());
         JsonEntry tags = JsonEntry.object();
         annotationLayer.tagColors.forEach((tag, color) -> tags.addProperty(tag.label(),
                                                                            ColorUtils.toHexString(color)));
         obj.addProperty("tags", tags);
         return obj;
      }
   }

}//END OF AnnotationLayer
