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

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Local progress logger.
 */
class LocalProgressLogger implements ProgressLogger {
   private final MultithreadedStopwatch sw = new MultithreadedStopwatch("ProgressLogger", Level.OFF);
   private final AtomicLong documentsProcessed = new AtomicLong();
   private final AtomicLong wordsProcessed = new AtomicLong();
   private final Logger logger;
   private final Level level;
   private final long interval;
   private final String operation;

   LocalProgressLogger(Logger logger, Level level, long interval, String operation) {
      this.logger = logger;
      this.level = level;
      this.interval = interval;
      this.operation = operation;
   }

   @Override
   public double documentsPerSecond() {
      return documentsProcessed.get() / (double) sw.elapsed(ChronoUnit.SECONDS);
   }

   @Override
   public long documentsProcessed() {
      return documentsProcessed.get();
   }

   @Override
   public void report() {
      LogUtils.log(logger,
                   level,
                   "({4}) Elapsed Time: {0}, Documents Processed: {1} ({2,number,0.3} docs/sec, {3,number,0.3} words/sec)",
                   sw.getElapsedTimeAsString(),
                   documentsProcessed.get(),
                   documentsPerSecond(),
                   wordsPerSecond(),
                   operation);
   }

   @Override
   public void start() {
      sw.start();
   }

   @Override
   public void stop(long numTokens) {
      sw.stop();
      long cnt = documentsProcessed.incrementAndGet();
      wordsProcessed.addAndGet(numTokens);
      if(cnt > 0 && cnt % interval == 0) {
         report();
      }
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
