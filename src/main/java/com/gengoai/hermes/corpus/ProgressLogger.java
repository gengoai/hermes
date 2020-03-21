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
import com.gengoai.MultithreadedStopwatch;
import com.gengoai.stream.StreamingContext;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface ProgressLogger extends Serializable {

   public static ProgressLogger getProgressLogger(StreamingContext streamingContext) {
      if(streamingContext.isDistributed()) {
         return new NoOptLogger();
      }
      return new LocalProgressLogger();
   }

   double documentsPerSecond();

   long documentsProcessed();

   void report(Logger logger, Level level);

   void start();

   void stop(long wordsProcessed);

   double wordsPerSecond();

   long wordsProcessed();

   class NoOptLogger implements ProgressLogger {

      @Override
      public double documentsPerSecond() {
         return 0;
      }

      @Override
      public long documentsProcessed() {
         return 0;
      }

      @Override
      public void report(Logger logger, Level level) {

      }

      @Override
      public void start() {

      }

      @Override
      public void stop(long wordsProcessed) {

      }

      @Override
      public double wordsPerSecond() {
         return 0;
      }

      @Override
      public long wordsProcessed() {
         return 0;
      }
   }

   class LocalProgressLogger implements ProgressLogger {
      private final MultithreadedStopwatch sw = new MultithreadedStopwatch("ProgressLogger");
      private final AtomicLong documentsProcessed = new AtomicLong();
      private final AtomicLong wordsProcessed = new AtomicLong();

      @Override
      public double documentsPerSecond() {
         return documentsProcessed.get() / (double) sw.elapsed(ChronoUnit.SECONDS);
      }

      @Override
      public long documentsProcessed() {
         return documentsProcessed.get();
      }

      @Override
      public void report(Logger logger, Level level) {
         LogUtils.log(logger,
                      level,
                      "Elapsed Time: {0}, Documents Processed: {1} ({2,number,0.3} docs/sec, {3,number,0.3} words/sec)",
                      sw.getElapsedTimeAsString(),
                      documentsProcessed.get(),
                      documentsPerSecond(),
                      wordsPerSecond());
      }

      @Override
      public void start() {
         sw.start();
      }

      @Override
      public void stop(long numTokens) {
         sw.stop();
         documentsProcessed.incrementAndGet();
         wordsProcessed.addAndGet(numTokens);
      }

      @Override
      public double wordsPerSecond() {
         return wordsProcessed.get() / (double) sw.elapsed(ChronoUnit.SECONDS);
      }

      @Override
      public long wordsProcessed() {
         return wordsProcessed.get();
      }
   }

}//END OF ProgressLogger
