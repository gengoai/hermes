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

package com.gengoai.hermes.corpus;

import com.gengoai.collection.Iterators;
import com.gengoai.function.SerializableConsumer;
import com.gengoai.hermes.Document;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

class LuceneFilteredView implements DocumentCollection {
   @NonNull
   private final LuceneCorpus parent;
   @NonNull
   private final LinkedHashSet<String> ids;

   public LuceneFilteredView(LuceneCorpus parent, LinkedHashSet<String> ids) {
      this.parent = parent;
      this.ids = ids;
   }

   @Override
   public void close() throws Exception {

   }

   @Override
   public StreamingContext getStreamingContext() {
      return StreamingContext.local();
   }

   @Override
   public boolean isEmpty() {
      return ids.isEmpty();
   }

   @Override
   public Iterator<Document> iterator() {
      return Iterators.transform(ids.iterator(), parent::getDocument);
   }

   @Override
   public MStream<Document> parallelStream() {
      return StreamingContext.local()
                             .stream(new ArrayList<>(ids))
                             .parallel()
                             .map(parent::getDocument);
   }

   @Override
   public SearchResults query(@NonNull Query query) {
      return new MStreamSearchResults(parallelStream().filter(query::matches), query);
   }

   @Override
   public long size() {
      return ids.size();
   }

   @Override
   public MStream<Document> stream() {
      return StreamingContext.local()
                             .stream(ids)
                             .map(parent::getDocument);
   }

   @Override
   public DocumentCollection update(@NonNull String operation,
                                    @NonNull SerializableConsumer<Document> documentProcessor) {
      ProgressLogger progressLogger = ProgressLogger.create(this, operation);
      return new MStreamDocumentCollection(stream().map(d -> {
         progressLogger.start();
         documentProcessor.accept(d);
         progressLogger.stop(d.tokenLength());
         return d;
      }));
   }
}//END OF LuceneFilteredCorpus
