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
import com.gengoai.collection.Lists;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.collection.multimap.TreeSetMultimap;
import lombok.NonNull;

import javax.swing.KeyStroke;
import java.awt.Color;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A model describing a Tag set.
 */
public class TagModel implements Serializable, Iterable<TagInfo> {
   private final Set<TagInfo> roots = new TreeSet<>();
   private final Multimap<Tag, TagInfo> parent2Child = new TreeSetMultimap<>();
   private final Map<Tag, TagInfo> tag2TagInfo = new HashMap<>();
   private final Map<String, TagInfo> name2TagInfo = new HashMap<>();


   private TagModel() {

   }

   public static Builder builder() {
      return new Builder();
   }

   public static Builder builder(@NonNull Collection<? extends Tag> collection) {
      return new Builder().addAll(collection);
   }

   public static Builder builder(@NonNull Tag[] collection) {
      return new Builder().addAll(Arrays.asList(collection));
   }

   public Color getColor(@NonNull Tag tag) {
      return tag2TagInfo.getOrDefault(tag, new TagInfo(tag, Color.GRAY, null)).getColor();
   }

   /**
    * Gets the root tags in the Model
    *
    * @return the root tags
    */
   public Set<TagInfo> getRoots() {
      return Collections.unmodifiableSet(roots);
   }

   public TagInfo getTagInfo(Tag tag) {
      return tag2TagInfo.get(tag);
   }

   public TagInfo getTagInfo(String name) {
      return name2TagInfo.get(name);
   }

   @Override
   public Iterator<TagInfo> iterator() {
      return new BFSIterator();
   }

   public static class Builder {
      private Map<Tag, TagInfo> tagInfo = new HashMap<>();

      public Builder forEach(BiConsumer<Tag,TagInfo> consumer){
         tagInfo.forEach(consumer);
         return this;
      }

      public Builder add(@NonNull Tag tag, Color color, KeyStroke shortcut) {
         tagInfo.put(tag, new TagInfo(tag, color, shortcut));
         return this;
      }

      public Builder add(@NonNull Tag tag) {
         tagInfo.put(tag, new TagInfo(tag));
         return this;
      }

      public Builder add(@NonNull Tag tag, Color color) {
         tagInfo.put(tag, new TagInfo(tag, color, null));
         return this;
      }

      public Builder addAll(@NonNull Collection<? extends Tag> collection) {
         collection.forEach(t -> tagInfo.put(t, new TagInfo(t)));
         return this;
      }

      public TagModel build() {
         TagModel model = new TagModel();
         //First collect all the tags
         model.tag2TagInfo.putAll(tagInfo);
         //Finally build the hierarchy
         model.tag2TagInfo
               .forEach((tag, tagInfo) -> {
                  model.name2TagInfo.put(tagInfo.toString(), tagInfo);
                  if(tagInfo.parent() == null) {
                     model.roots.add(tagInfo);
                  } else {
                     model.parent2Child.put(tagInfo.parent(), tagInfo);
                  }
               });
         return model;
      }

      public Stream<TagInfo> instancesOf(Tag tag) {
         return tagInfo.entrySet().stream().filter(e -> e.getKey().isInstance(tag)).map(Map.Entry::getValue);
      }

      public <E> Builder update(@NonNull Iterator<TagInfo> iterator) {
         iterator.forEachRemaining(t -> tagInfo.put(t.getTag(), t));
         return this;
      }

      /**
       * Updates a tag in the model
       *
       * @param tag      the tag to update
       * @param consumer the consumer to use for updating the associated TagInfo
       */
      public void update(@NonNull Tag tag, @NonNull Consumer<TagInfo> consumer) {
         if(tagInfo.containsKey(tag)) {
            consumer.accept(tagInfo.get(tag));
         }
      }

   }

   private class BFSIterator implements Iterator<TagInfo> {
      /**
       * The Queue.
       */
      Queue<TagInfo> queue = Lists.asLinkedList(roots);

      @Override
      public boolean hasNext() {
         return queue.size() > 0;
      }

      @Override
      public TagInfo next() {
         TagInfo next = queue.remove();
         queue.addAll(parent2Child.get(next.getTag()));
         return next;
      }
   }


}//END OF TagModel
