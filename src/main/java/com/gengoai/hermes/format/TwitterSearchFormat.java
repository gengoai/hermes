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

import com.gengoai.Language;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.Types;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TwitterSearchFormat extends OneDocPerFileFormat {
   private final DocFormatParameters parameters;

   public TwitterSearchFormat(@NonNull DocFormatParameters parameters) {
      this.parameters = parameters;
   }

   private Stream<Document> processFile(String json) {
      DocumentFactory documentFactory = parameters.documentFactory.value();
      Map<String, ?> file = null;
      try {
         file = Json.parseObject(json);
      } catch(IOException e) {
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
         if(!Val.of(status.get("in_reply_to_screen_name")).isNull()) {
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

   @Override
   public Iterator<Document> read(Resource inputResource) {
      return StreamingContext.local()
                             .textFile(inputResource, true)
                             .flatMap(this::processFile)
                             .iterator();
   }

   @Override
   public void write(Document document, Resource outputResource) throws IOException {
      throw new UnsupportedOperationException();
   }

   @MetaInfServices
   public static class Provider implements DocFormatProvider {

      @Override
      public DocFormat create(@NonNull DocFormatParameters parameters) {
         return new TwitterSearchFormat(parameters);
      }

      @Override
      public String getName() {
         return "TWITTER_SEARCH";
      }

      @Override
      public boolean isWriteable() {
         return false;
      }
   }

}//END OF TwitterSearchFormat
