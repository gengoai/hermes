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

import com.gengoai.Validation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.POS;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.BaseWordCategorization;
import com.gengoai.string.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.gengoai.hermes.corpus.io.CorpusParameters.DOCUMENT_FACTORY;

/**
 * The type Pos reader.
 *
 * @author David B. Bracewell
 */
public class POSReader extends CorpusReader {
   private static final long serialVersionUID = 1L;

   @Override
   public Stream<Document> parse(String resource) {
      DocumentFactory documentFactory = getOptions().get(DOCUMENT_FACTORY);
      List<String> tokens = new LinkedList<>();
      List<String> pos = new ArrayList<>();
      String[] parts = resource.split("\\s+");
      for (String part : parts) {
         int lpos = part.lastIndexOf('_');
         String w = part.substring(0, lpos);
         String p = part.substring(lpos + 1);
         w = POSCorrection.word(w, p);
         p = POSCorrection.pos(w, p);
         if (!Strings.isNullOrBlank(w)) {
            tokens.add(w);
            pos.add(p);
         }
      }
      Document document = documentFactory.fromTokens(tokens);
      boolean complete = false;
      for (int i = 0; i < tokens.size(); i++) {
         POS p = POS.fromString(pos.get(i));
         if (p != null && !p.equals(POS.ANY)) {
            complete = true;
            Validation.checkArgument(!p.isPhraseTag(), p.asString());
            document.tokenAt(i).put(Types.PART_OF_SPEECH, p);
         }
      }
      document.createAnnotation(Types.SENTENCE, 0, document.length(), Collections.emptyMap());
      document.setCompleted(Types.SENTENCE, "PROVIDED");
      document.setCompleted(Types.TOKEN, "PROVIDED");
      if (complete) {
         document.setCompleted(Types.PART_OF_SPEECH, "PROVIDED");
      }
      BaseWordCategorization.INSTANCE.categorize(document);
      return Stream.of(document);
   }


}//END OF POSReader
