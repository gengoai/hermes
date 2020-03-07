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

import com.gengoai.SystemInfo;
import com.gengoai.concurrent.BrokerIterator;
import com.gengoai.concurrent.StreamProducer;
import com.gengoai.function.SerializableSupplier;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.InMemoryCorpus;
import com.gengoai.hermes.corpus.SparkCorpus;
import com.gengoai.hermes.corpus.StreamingCorpus;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import com.gengoai.stream.Streams;
import com.gengoai.stream.local.LocalReusableMStream;

import java.io.IOException;
import java.util.stream.Stream;

import static com.gengoai.hermes.corpus.io.CorpusParameters.*;

/**
 * Meta-reader to read one-document-per-line
 *
 * @author David B. Bracewell
 */
public class OnePerLineReader extends CorpusReader {
   private static final long serialVersionUID = 1L;
   private final CorpusReader subReader;

   /**
    * Instantiates a new One per line reader.
    *
    * @param subReader the sub reader
    */
   public OnePerLineReader(CorpusReader subReader) {
      this.subReader = subReader;
   }

   @Override
   public CorpusParameters getOptions() {
      return subReader.getOptions();
   }


   @Override
   public Corpus read(Resource resource) throws IOException {
      if(subReader.getOptions().getOrDefault(IS_DISTRIBUTED, false)) {
         MStream<String> lines = StreamingContext.distributed().textFile(resource, "*.*");
         if(subReader instanceof JsonReader) {
            return new SparkCorpus(lines);
         }
         return new SparkCorpus(lines.flatMap(line -> subReader.parse(line)
                                                               .map(Document::toJson)).cache());
      }

      SerializableSupplier<Stream<Document>> streamSupplier = () -> {
         MStream<String> stream = StreamingContext.local().textFile(resource);
         if(getOptions().getOrDefault(IS_PARALLEL, false)) {
            stream = stream.parallel();
         }
         return Streams.asStream(new BrokerIterator<>(new StreamProducer<>(stream.javaStream()),
                                                      subReader::parse,
                                                      10_000,
                                                      SystemInfo.NUMBER_OF_PROCESSORS));
      };
      if(getOptions().getOrDefault(IN_MEMORY, false)) {
         return new InMemoryCorpus(streamSupplier.get());
      }
      return new StreamingCorpus(new LocalReusableMStream<>(streamSupplier));
   }

   @Override
   public Stream<Document> parse(String content) {
      return subReader.parse(content);
   }
}//END OF OnePerLineReader
