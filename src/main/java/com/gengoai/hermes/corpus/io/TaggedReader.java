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

package com.gengoai.hermes.corpus.io;

import com.gengoai.ParameterDef;
import com.gengoai.conversion.Val;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.BaseWordCategorization;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.hermes.corpus.io.CorpusParameters.DOCUMENT_FACTORY;
import static com.gengoai.tuple.Tuples.$;

/**
 * Reads in a corpus of plain text files with annotations marked in SGML tags where the tag name is used as the tag of
 * the annotation.
 *
 * @author David B. Bracewell
 */
public class TaggedReader extends CorpusReader {
   /**
    * The constant ANNOTATION_TYPE.
    */
   public static final ParameterDef<AnnotationType> ANNOTATION_TYPE = ParameterDef.param("annotationType", AnnotationType.class);
   private static final Pattern TAG_PATTERN = Pattern.compile("<([a-z_]+)>([^<>]+)</\\1>", Pattern.CASE_INSENSITIVE);

   /**
    * Instantiates a new Tagged reader.
    */
   public TaggedReader() {
      super(new TaggedParameters());
   }

   @Override
   public Stream<Document> parse(String resource) {
      DocumentFactory documentFactory = getOptions().get(DOCUMENT_FACTORY);
      String content = resource.trim();
      final AnnotationType annotationType = getOptions().get(ANNOTATION_TYPE);
      int last = 0;
      List<Integer> startPositions = new ArrayList<>();
      List<Integer> endPositions = new ArrayList<>();
      List<String> types = new ArrayList<>();
      Matcher matcher = TAG_PATTERN.matcher(content);
      StringBuilder builder = new StringBuilder();
      while (matcher.find()) {
         if (matcher.start() != last) {
            builder.append(content, last, matcher.start());
         }
         last = matcher.end();
         startPositions.add(builder.length());
         endPositions.add(builder.length() + matcher.group(2).length());
         types.add(matcher.group(1));
         builder.append(matcher.group(2));
      }
      if (last != content.length()) {
         builder.append(content, last, content.length());
      }
      Document document = documentFactory.createRaw(builder.toString());
      for (int i = 0; i < startPositions.size(); i++) {
         document.createAnnotation(annotationType,
                                   startPositions.get(i),
                                   endPositions.get(i),
                                   hashMapOf($(annotationType.getTagAttribute(),
                                               Val.of(types.get(i))
                                                  .as(annotationType.getTagAttribute().getValueType())))
                                  );
      }
      BaseWordCategorization.INSTANCE.categorize(document);
      return Stream.of(document);
   }

   /**
    * The type Tagged parameters.
    */
   public static class TaggedParameters extends CorpusParameters {
      /**
       * The Annotation type.
       */
      public final Parameter<AnnotationType> annotationType = parameter(ANNOTATION_TYPE, Types.ENTITY);
   }
}//END OF TaggedReader
