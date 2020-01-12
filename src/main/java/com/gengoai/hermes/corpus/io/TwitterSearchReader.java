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

import com.gengoai.Language;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.Types;
import com.gengoai.io.Resources;
import com.gengoai.json.Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.gengoai.hermes.corpus.io.CorpusParameters.DOCUMENT_FACTORY;

/**
 * Reader for JSON files from the official Twitter Search API
 *
 * @author David B. Bracewell
 */
public class TwitterSearchReader extends CorpusReader {
   private static final long serialVersionUID = 1L;

   @Override
   public Stream<Document> parse(String fileContent) {
      DocumentFactory documentFactory = getOptions().get(DOCUMENT_FACTORY);
      Map<String, ?> file = null;
      try {
         file = Json.parseObject(Resources.fromString(fileContent));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      List<Document> documentList = new ArrayList<>();
      List<Map<String, ?>> statuses = Cast.as(Val.of(file.get("statuses")).asList(Map.class));
      statuses.forEach(status -> {
         String id = String.format("%.0f", Val.of(status.get("id")).asDoubleValue());
         String content = Val.of(status.get("text")).asString();
         Language language = Language.fromString(
            Val.of(status.get("metadata")).asMap(String.class, Val.class).get("iso_language_code").asString()
                                                );
         Document document = documentFactory.create(id, content, language);
         documentList.add(document);
         Map<String, Val> user = Val.of(status.get("user")).asMap(String.class, Val.class);
         document.put(Types.AUTHOR, user.get("name").asString());
         if (!Val.of(status.get("in_reply_to_screen_name")).isNull()) {
            document.put(Types.attribute("reply_to", String.class),
                         Val.of(status.get("in_reply_to_screen_name")).asString());
         }
         document.put(Types.attribute("is_retweet", Boolean.class), content.startsWith("RT"));
         document.put(Types.attribute("twitter_user_id", String.class),
                      String.format("%.0f", Val.of(user.get("id")).asDoubleValue()));
         document.put(Types.attribute("followers", Integer.class), user.get("followers_count").asIntegerValue());
         document.put(Types.attribute("created_at", String.class), Val.of(status.get("created_at")).asString());
      });
      return documentList.stream();
   }

}//END OF TwitterSearchReader
