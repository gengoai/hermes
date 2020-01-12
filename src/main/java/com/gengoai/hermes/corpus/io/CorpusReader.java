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

import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.InMemoryCorpus;
import com.gengoai.hermes.corpus.SparkCorpus;
import com.gengoai.hermes.corpus.StreamingCorpus;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.io.IOException;
import java.util.stream.Stream;

import static com.gengoai.hermes.corpus.io.CorpusParameters.*;

/**
 * Base class for Corpus reader implementations that construct a Corpus from a specific format.
 *
 * @author David B. Bracewell
 */
public abstract class CorpusReader extends CorpusIOBase<CorpusReader> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new CorpusReader.
    */
   public CorpusReader() {
      super();
   }

   /**
    * Instantiates a new CorpusReader
    *
    * @param parameters the parameters of the reader
    */
   public CorpusReader(CorpusParameters parameters) {
      super(parameters);
   }

   /**
    * Creates a {@link Corpus} by reading in from the given Resource
    *
    * @param resource the resource containing the {@link Corpus} in a specific format
    * @return the corpus
    * @throws IOException Something went wrong reading
    */
   public Corpus read(@NonNull Resource resource) throws IOException {
      if (getOptions().getOrDefault(IS_DISTRIBUTED, false)) {
         return new SparkCorpus(StreamingContext.distributed()
                                                .textFile(resource, true)
                                                .flatMap(line -> parse(line).map(Document::toJson)).cache());
      }
      resource.mkdirs();
      MStream<String> stream = StreamingContext.local().textFile(resource, true);
      if (getOptions().getOrDefault(IS_PARALLEL, false)) {
         stream = stream.parallel();
      }
      if( getOptions().getOrDefault(IN_MEMORY, false)){
         return new InMemoryCorpus(stream.flatMap(this::parse).javaStream());
      }
      return new StreamingCorpus(stream.flatMap(this::parse));
   }


   /**
    * Creates a {@link Corpus} by reading in from the given resource specified in string form
    *
    * @param resource the resource containing the {@link Corpus} in a specific format
    * @return the corpus
    * @throws IOException Something went wrong reading
    */
   public final Corpus read(String resource) throws IOException {
      return read(Resources.from(resource));
   }

   /**
    * Parses content in string form generating a stream of Document
    *
    * @param content the content
    * @return the stream of documents
    */
   public abstract Stream<Document> parse(String content);


}//END OF CorpusReader
