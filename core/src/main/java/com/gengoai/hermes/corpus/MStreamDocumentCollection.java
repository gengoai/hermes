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

import com.gengoai.LogUtils;
import com.gengoai.config.Config;
import com.gengoai.function.SerializableConsumer;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.Document;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.util.logging.Level;

class MStreamDocumentCollection implements DocumentCollection {
   private MStream<Document> stream;

   public MStreamDocumentCollection(MStream<Document> stream) {
      this.stream = stream;
   }

   @Override
   public DocumentCollection cache() {
      stream = stream.cache();
      return this;
   }

   @Override
   public void close() throws Exception {
      stream.close();
   }

   @Override
   public DocumentCollection filter(@NonNull SerializablePredicate<Document> predicate) {
      return new MStreamDocumentCollection(stream.filter(predicate));
   }

   @Override
   public StreamingContext getStreamingContext() {
      return stream.getContext();
   }

   @Override
   public MStream<Document> parallelStream() {
      return stream.parallel();
   }

   @Override
   public SearchResults query(@NonNull Query query) {
      return new MStreamSearchResults(stream.filter(query::matches), query);
   }

   @Override
   public MStream<Document> stream() {
      return stream;
   }

   @Override
   public DocumentCollection update(@NonNull String operation,
                                    @NonNull SerializableConsumer<Document> documentProcessor) {
      ProgressLogger progressLogger = ProgressLogger.create(getStreamingContext(),
                                                            LogUtils.getLogger(DocumentCollection.class),
                                                            Config.get(DocumentCollection.REPORT_LEVEL)
                                                                  .as(Level.class, Level.FINEST),
                                                            Config.get(DocumentCollection.REPORT_INTERVAL)
                                                                  .as(int.class, 500),
                                                            operation);
      stream = stream.map(d -> {
         progressLogger.start();
         documentProcessor.accept(d);
         progressLogger.stop(d.tokenLength());
         return d;
      }).onClose(progressLogger::report);
      return this;
   }

}//END OF StreamingDocumentCollection
