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
   */

  package com.gengoai.hermes.corpus;

  /**
   * @author David B. Bracewell
   */
  public class CorpusTest {

//
//     @Test
//     public void inMemory() {
//        Config.initializeTest();
//        Corpus corpus = new InMemoryCorpus(DocumentFactory.getInstance()
//                                                          .create("This is the first document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the second document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the third document."));
//        corpus.add(DocumentFactory.getInstance().create("This is the fourth document."));
//        assertEquals(4, corpus.size());
//        assertEquals("This is the first document.", corpus.stream().first().get().toString());
//        corpus.annotate(Types.TOKEN);
//        Counter<String> cntr = corpus.termCount(PhraseExtractor.create());
//        assertEquals(4, cntr.get("the"), 0d);
//        assertEquals(4, cntr.get("document"), 0d);
//        assertEquals(4, cntr.get("This"), 0d);
//        assertEquals(4, cntr.get("is"), 0d);
//        assertEquals(4, cntr.get("."), 0d);
//        assertEquals(1, cntr.get("first"), 0d);
//        assertEquals(1, cntr.get("second"), 0d);
//        assertEquals(1, cntr.get("third"), 0d);
//        assertEquals(1, cntr.get("fourth"), 0d);
//     }
//
//
//     @Test
//     public void searchTest() {
//        Config.initializeTest();
//        Corpus corpus = new InMemoryCorpus(DocumentFactory.getInstance()
//                                                          .create("This is the first document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the second document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the third document."));
//        assertFalse(corpus.size() < 1);
//        corpus.annotate(Types.TOKEN);
//        try {
//           assertEquals(1, corpus.query("first").size(), 0d);
//        } catch (ParseException e) {
//           throw new RuntimeException(e);
//        }
//     }
//
//
//     @Test
//     public void sampleTest() {
//        Config.initializeTest();
//        Corpus corpus = new InMemoryCorpus(DocumentFactory.getInstance()
//                                                          .create("This is the first document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the second document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the third document."));
//        Corpus sample = corpus.sample(2);
//        assertEquals(2, sample.stream().distinct().count());
//     }
//
//
//     @Test
//     public void groupByTest() {
//        Config.initializeTest();
//        Corpus corpus = new InMemoryCorpus(DocumentFactory.getInstance()
//                                                          .create("This is the first document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the second document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the third document."),
//                                           DocumentFactory.getInstance()
//                                                          .create("This is the first long document."));
//        Multimap<String, Document> map = corpus.groupBy(Document::toString);
//        assertEquals(4, map.size(), 0d);
//     }
//
//
//     @Test
//     public void mStreamTest() throws IOException {
//        Config.initializeTest();
//        Corpus corpus = Corpus.reader("TEXT")
//                              .read(Resources.fromClasspath("com/gengoai/com.gengoai.hermes/docs/txt"));
//        corpus = corpus.annotate(Types.TOKEN)
//                       .filter(document -> document.tokenLength() > 2);
//        assertEquals(3d, corpus.size(), 0d);
//     }
//
//     @Test
//     public void resourceTest() throws IOException {
//        Config.initializeTest();
//        Corpus corpus = Corpus.reader("TEXT")
//                              .read(Resources.fromClasspath("com/gengoai/com.gengoai.hermes/docs/txt"))
//                              .annotate(Types.TOKEN, Types.SENTENCE)
//                              .cache();
//
//        assertFalse(corpus.isEmpty());
//        assertEquals(3, corpus.size());
//        corpus = corpus.annotate(Types.TOKEN);
//        Counter<String> cntr = corpus.termCount(PhraseExtractor.create());
//        assertEquals(3, cntr.get("the"), 0d);
//        assertEquals(3, cntr.get("document"), 0d);
//        assertEquals(3, cntr.get("This"), 0d);
//        assertEquals(3, cntr.get("is"), 0d);
//        assertEquals(3, cntr.get("."), 0d);
//        assertEquals(1, cntr.get("first"), 0d);
//        assertEquals(1, cntr.get("second"), 0d);
//        assertEquals(1, cntr.get("third"), 0d);
//        assertEquals(1, corpus.filter(d -> d.contains("third")).size(), 0d);
//
//        Counter<String> ngrams = corpus.nGramFrequencies(NGramExtractor.create()
//                                                                       .setMinOrder(1))
//                                       .mapKeys(tuple -> tuple.get(0).toString());
//        assertEquals(cntr, ngrams);
//
//        Counter<Tuple> bigrams = corpus.nGramFrequencies(NGramExtractor.create()
//                                                                       .setMinOrder(2)
//                                                                       .setMaxOrder(2));
//        assertEquals(3d, bigrams.get(Tuple2.of("This", "is")), 0d);
//        assertEquals(1d, bigrams.get(Tuple2.of("the", "first")), 0d);
//
//        Counter<Tuple> trigrams = corpus.nGramFrequencies(NGramExtractor.create().setMinOrder(3));
//        assertEquals(3d, trigrams.get(Tuple3.of("This", "is", "the")), 0d);
//
//
//        Counter<Tuple> quadgrams = corpus.nGramFrequencies(NGramExtractor.create()
//                                                                         .setMinOrder(4)
//                                                                         .setMaxOrder(4));
//        assertEquals(1d, quadgrams.get(Tuple4.of("This", "is", "the", "first")), 0d);
//
//        Counter<Tuple> fivegrams = corpus.nGramFrequencies(NGramExtractor.create().setMinOrder(5));
//        assertEquals(1d, fivegrams.get(NTuple.of("This", "is", "the", "first", "document")), 0d);
//
//
//        cntr = corpus.documentCount(PhraseExtractor.create());
//        assertEquals(3, cntr.get("the"), 0d);
//        assertEquals(3, cntr.get("document"), 0d);
//        assertEquals(3, cntr.get("This"), 0d);
//        assertEquals(3, cntr.get("is"), 0d);
//        assertEquals(3, cntr.get("."), 0d);
//        assertEquals(1, cntr.get("first"), 0d);
//        assertEquals(1, cntr.get("second"), 0d);
//        assertEquals(1, cntr.get("third"), 0d);
//     }
//
//     @Test
//     public void distributed() {
//        Config.initializeTest();
//        Config.setProperty("spark.master", "local");
//        Corpus corpus = new SparkCorpus(StreamingContext.distributed()
//                                                        .stream(
//                                                           DocumentFactory.getInstance()
//                                                                          .create("This is the first document.")
//                                                                          .toJson(),
//                                                           DocumentFactory.getInstance()
//                                                                          .create("This is the second document.")
//                                                                          .toJson(),
//                                                           DocumentFactory.getInstance()
//                                                                          .create("This is the third document.")
//                                                                          .toJson()));
//        assertEquals(3, corpus.size());
//        assertEquals("This is the first document.", corpus.stream().first().get().toString());
//        corpus = corpus.annotate(Types.TOKEN);
//        Counter<String> cntr = corpus.termCount(PhraseExtractor.create());
//        assertEquals(3, cntr.get("the"), 0d);
//        assertEquals(3, cntr.get("document"), 0d);
//        assertEquals(3, cntr.get("This"), 0d);
//        assertEquals(3, cntr.get("is"), 0d);
//        assertEquals(3, cntr.get("."), 0d);
//        assertEquals(1, cntr.get("first"), 0d);
//        assertEquals(1, cntr.get("second"), 0d);
//        assertEquals(1, cntr.get("third"), 0d);
//        assertEquals(1, corpus.filter(d -> d.contains("third")).size(), 0d);
//
//
//        Resource r = Resources.temporaryDirectory();
//        r.delete();
//        try {
//           corpus.writer("JSON_OPL").write(r);
//        } catch (IOException e) {
//           throw new RuntimeException(e);
//        }
//
//     }


  }