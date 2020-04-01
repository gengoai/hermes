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

import com.gengoai.Validation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.BaseWordCategorization;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class POSFormat extends WholeFileTextFormat implements OneDocPerFileFormat, Serializable {
   private static final long serialVersionUID = 1L;
   private final DocFormatParameters parameters;

   public POSFormat(@NonNull DocFormatParameters parameters) {
      this.parameters = parameters;
   }

   @Override
   public DocFormatParameters getParameters() {
      return parameters;
   }

   @Override
   protected Stream<Document> readSingleFile(String content) {
      DocumentFactory documentFactory = parameters.getDocumentFactory();
      List<String> tokens = new LinkedList<>();
      List<String> pos = new ArrayList<>();
      String[] parts = content.split("\\s+");
      for(String part : parts) {
         int lpos = part.lastIndexOf('_');
         String w = part.substring(0, lpos);
         String p = part.substring(lpos + 1);
         w = POSCorrection.word(w, p);
         p = POSCorrection.pos(w, p);
         if(!Strings.isNullOrBlank(w)) {
            tokens.add(w);
            pos.add(p);
         }
      }
      Document document = documentFactory.fromTokens(tokens);
      for(int i = 0; i < tokens.size(); i++) {
         PartOfSpeech p = PartOfSpeech.valueOf(pos.get(i));
         Validation.notNull(p);
         Validation.checkArgument(!p.isPhraseTag() && !p.equals(PartOfSpeech.ANY));
         document.tokenAt(i).put(Types.PART_OF_SPEECH, p);
      }
      document.createAnnotation(Types.SENTENCE, 0, document.length(), Collections.emptyMap());
      document.setCompleted(Types.SENTENCE, "PROVIDED");
      document.setCompleted(Types.TOKEN, "PROVIDED");
      document.setCompleted(Types.PART_OF_SPEECH, "PROVIDED");
      BaseWordCategorization.INSTANCE.categorize(document);
      return Stream.of(document);
   }

   @Override
   public void write(Document document, Resource outputResource) throws IOException {
      outputResource.write(document.tokenStream().map(HString::toPOSString).collect(Collectors.joining(" ")));
   }

   @MetaInfServices
   public static class Provider implements DocFormatProvider {

      @Override
      public DocFormat create(DocFormatParameters parameters) {
         return new POSFormat(parameters);
      }

      @Override
      public String getName() {
         return "pos";
      }

      @Override
      public boolean isWriteable() {
         return true;
      }
   }

}//END OF POSFormat
