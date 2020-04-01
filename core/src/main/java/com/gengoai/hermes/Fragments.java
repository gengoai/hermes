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
 */

package com.gengoai.hermes;

import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Convenience methods for constructing orphaned and empty fragments.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class Fragments {

   private Fragments() {
      throw new IllegalAccessError();
   }

   /**
    * Creates an empty HString associated with the given document.
    *
    * @param document the document the HString is span on
    * @return the new HString (associated with the given document if it is not null)
    */
   public static HString emptyHString(Document document) {
      return new Fragment(document, 0, 0);
   }

   /**
    * Creates an orphaned empty annotation, i.e. an empty span and no document associated with it.
    *
    * @return the annotation
    */
   public static Annotation orphanedAnnotation(AnnotationType annotationType) {
      return new DefaultAnnotationImpl(annotationType, 0, 0);
   }

   /**
    * Creates an HString which is a span, i.e. fragment, of text on the given document.
    *
    * @param document the document
    * @param start    the start
    * @param end      the end
    * @return the h string
    */
   public static HString span(Document document, int start, int end) {
      return new Fragment(document, start, end);
   }

   /**
    * Creates a new HString that has content, but no document associated with it
    *
    * @param content the content of the string
    * @return the new HString
    */
   public static HString stringWrapper(@NonNull String content) {
      return new SingleToken(content);
   }

   private static class Fragment extends BaseHString {
      private static final long serialVersionUID = 1L;
      private final Document owner;

      private Fragment(Document owner, int start, int end) {
         super(start, end);
         this.owner = owner;
      }

      @Override
      public Document document() {
         return owner;
      }

   }

   private static class SingleToken extends DefaultAnnotationImpl {
      private static final long serialVersionUID = 1L;
      private final String content;

      private SingleToken(String content) {
         super(Types.TOKEN, 0, content.length());
         this.content = content;
      }

      @Override
      public List<Annotation> annotations() {
         return annotations(Types.TOKEN);
      }

      @Override
      public List<Annotation> annotations(AnnotationType type, Predicate<? super Annotation> filter) {
         return annotations(Types.TOKEN).stream().filter(filter).collect(Collectors.toList());
      }

      @Override
      public List<Annotation> annotations(AnnotationType type) {
         if(type == Types.TOKEN) {
            return Collections.singletonList(this);
         }
         return Collections.emptyList();
      }

      @Override
      public char charAt(int index) {
         return content.charAt(index);
      }

      @Override
      public Document document() {
         return null;
      }

//      @Override
//      public int end() {
//         return content.length();
//      }

//      @Override
//      public HString find(String text, int start) {
//         Validation.checkElementIndex(start, length());
//         int pos = indexOf(text, start);
//         if (pos == -1) {
//            return Fragments.detachedEmptyHString();
//         }
//         return new SingleToken(content.substring(pos, pos + text.length()));
//      }

//      @Override
//      public int start() {
//         return 0;
//      }

      @Override
      public HString substring(int relativeStart, int relativeEnd) {
         return new SingleToken(content.substring(relativeStart, relativeEnd));
      }

      @Override
      public String toString() {
         return this.content;
      }

      @Override
      public List<Annotation> tokens() {
         return Collections.singletonList(this);
      }

   }

}//END OF Fragments
