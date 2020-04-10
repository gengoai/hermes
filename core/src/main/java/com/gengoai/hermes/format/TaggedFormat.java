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

package com.gengoai.hermes.format;

import com.gengoai.ParameterDef;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.hermes.*;
import com.gengoai.io.resource.Resource;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>Format Name: <b>tagged</b></p>
 * <p>Format with words separated by whitespace and sequences labeled in SGML like tags, e.g. &lt;TAG&gt;My
 * text&lt;/TAG&gt;. The annotation type of the tagged spans is set via the "annotationType" parameter.</p>
 */
public class TaggedFormat extends WholeFileTextFormat implements OneDocPerFileFormat, Serializable {
   private static final long serialVersionUID = 1L;
   private static final Pattern TAG_PATTERN = Pattern.compile("<([a-z_]+)>([^<>]+)</\\1>", Pattern.CASE_INSENSITIVE);
   /**
    * The constant ANNOTATION_TYPE.
    */
   public static final ParameterDef<AnnotationType> ANNOTATION_TYPE = ParameterDef.param("annotationType",
                                                                                         AnnotationType.class);
   private final TaggedParameters parameters;

   TaggedFormat(TaggedParameters parameters) {
      this.parameters = parameters;
   }

   @Override
   public DocFormatParameters getParameters() {
      return parameters;
   }

   protected Stream<Document> readSingleFile(String content) {
      DocumentFactory documentFactory = parameters.getDocumentFactory();
      final AnnotationType annotationType = parameters.annotationType.value();
      int last = 0;
      List<Integer> startPositions = new ArrayList<>();
      List<Integer> endPositions = new ArrayList<>();
      List<String> types = new ArrayList<>();
      Matcher matcher = TAG_PATTERN.matcher(content);
      StringBuilder builder = new StringBuilder();
      while(matcher.find()) {
         if(matcher.start() != last) {
            builder.append(content, last, matcher.start());
         }
         last = matcher.end();
         startPositions.add(builder.length());
         endPositions.add(builder.length() + matcher.group(2).length());
         types.add(matcher.group(1));
         builder.append(matcher.group(2));
      }
      if(last != content.length()) {
         builder.append(content, last, content.length());
      }
      Document document = documentFactory.createRaw(builder.toString());
      for(int i = 0; i < startPositions.size(); i++) {
         document.createAnnotation(annotationType,
                                   startPositions.get(i),
                                   endPositions.get(i),
                                   hashMapOf($(annotationType.getTagAttribute(),
                                               Val.of(types.get(i))
                                                  .as(annotationType.getTagAttribute().getValueType())))
                                  );
      }
      return Stream.of(document);
   }

   @Override
   public void write(Document document, Resource outputResource) throws IOException {
      StringBuilder output = new StringBuilder();
      int lastStart = 0;
      for(Annotation annotation : document.annotations(parameters.annotationType.value())) {
         if(annotation.start() != lastStart) {
            output.append(document.substring(lastStart, annotation.start()).toString());
         }
         output.append("<").append(annotation.getTag().label()).append(">")
               .append(annotation.toString())
               .append("</").append(annotation.getTag().label()).append(">");
         lastStart = annotation.end();
      }
      if(lastStart < document.end()) {
         output.append(document.substring(lastStart, document.end()).toString());
      }
      outputResource.write(output.toString());
   }

   /**
    * The type Provider.
    */
   @MetaInfServices
   public static class Provider implements DocFormatProvider {

      @Override
      public DocFormat create(@NonNull DocFormatParameters parameters) {
         if(parameters instanceof TaggedParameters) {
            return new TaggedFormat(Cast.as(parameters));
         }
         throw new IllegalArgumentException("Invalid parameter class, expecting: " +
                                                  TaggedParameters.class.getName() +
                                                  ", but received: " +
                                                  parameters.getClass().getName());
      }

      @Override
      public DocFormatParameters getDefaultFormatParameters() {
         return new TaggedParameters();
      }

      @Override
      public String getName() {
         return "TAGGED";
      }

      @Override
      public boolean isWriteable() {
         return true;
      }
   }

   /**
    * The type Tagged parameters.
    */
   public static class TaggedParameters extends DocFormatParameters {
      /**
       * The Annotation type.
       */
      public final Parameter<AnnotationType> annotationType = parameter(ANNOTATION_TYPE, Types.ENTITY);
   }
}//END OF TaggedFormat
