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

import com.gengoai.HierarchicalEnumValue;
import com.gengoai.Tag;
import com.gengoai.conversion.Cast;
import lombok.Data;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

/**
 * Information associated with a Tag for visualization
 */
@Data
public class TagInfo implements Serializable, Comparable<TagInfo> {
   public static final Color DEFAULT_COLOR = Color.GRAY;

   @NonNull
   private final Tag tag;
   private final Tag parent;
   private final KeyStroke shortcut;
   private Color color;

   /**
    * Instantiates a new TagInfo for the given tag with a default color of Silver and no keyboard shortcut
    *
    * @param tag the tag
    */
   public TagInfo(@NonNull Tag tag) {
      this(tag, Color.GRAY, null);
   }

   /**
    * Instantiates a new TagInfo for the given tag with the given color and keyboard short
    *
    * @param tag      the tag
    * @param color    the color
    * @param shortcut the shortcut
    */
   public TagInfo(@NonNull Tag tag, Color color, KeyStroke shortcut) {
      this.parent = tag.parent();
      this.tag = tag;
      this.color = color == null
                   ? DEFAULT_COLOR
                   : color;
      this.shortcut = shortcut;

   }

   @Override
   public int compareTo(TagInfo tagInfo) {
      return tag.name().compareTo(tagInfo.tag.name());
   }

   public boolean isDefaultColor() {
      return color == DEFAULT_COLOR;
   }

   /**
    * Gets the tag's parent or null if there is none
    *
    * @return the tag's parent
    */
   public Tag parent() {
      return parent;
   }

   public void setColor(Color color) {
      this.color = color == null
                   ? DEFAULT_COLOR
                   : color;
   }

   @NonNull
   public String toString() {
      if(tag instanceof HierarchicalEnumValue) {
         HierarchicalEnumValue<?> he = Cast.as(tag);
         return he.label();
      }
      return tag.name();
   }
}//END OF TagInfo
