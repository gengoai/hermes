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
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a logger that keeps track of the number of documents and words processed and reports processing statistics on
 * a given interval.
 */
public interface ProgressLogger extends Serializable {

   static ProgressLogger create(@NonNull DocumentCollection owner, @NonNull String operation) {
      return create(owner.getStreamingContext(),
                    LogUtils.getLogger(owner instanceof Corpus
                                       ? Corpus.class
                                       : DocumentCollection.class),
                    Config.get(DocumentCollection.REPORT_LEVEL)
                          .as(Level.class, Level.FINEST),
                    Config.get(DocumentCollection.REPORT_INTERVAL)
                          .as(int.class, 500),
                    operation);
   }

   /**
    * Creates a progress logger for the given streaming context.
    *
    * @param streamingContext the streaming context
    * @param logger           the logger to use to log messages
    * @param level            the level to log at
    * @param interval         the interval between number of documents to process for reporting.
    * @return the progress logger
    */
   static ProgressLogger create(@NonNull StreamingContext streamingContext,
                                @NonNull Logger logger,
                                @NonNull Level level,
                                long interval,
                                @NonNull String operationName) {
      if(streamingContext.isDistributed()) {
         return new NoOptProgressLogger();
      }
      return new LocalProgressLogger(logger, level, interval, operationName);
   }

   /**
    * @return the number of documents per second processed.
    */
   double documentsPerSecond();

   /**
    * @return the number of documents processed.
    */
   long documentsProcessed();

   /**
    * Reports processing statistics.
    */
   void report();

   /**
    * Starts the processing timer
    */
   void start();

   /**
    * Stops the current processing timer
    *
    * @param wordsProcessed the number of words processed
    */
   void stop(long wordsProcessed);

   /**
    * @return the number of words per second processed.
    */
   double wordsPerSecond();

   /**
    * @return the number of words processed
    */
   long wordsProcessed();

}//END OF ProgressLogger
