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

package com.gengoai.hermes.corpus;

import com.gengoai.collection.Sets;
import com.gengoai.collection.counter.Counter;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableConsumer;
import com.gengoai.hermes.*;
import com.gengoai.hermes.extraction.caduceus.CaduceusProgram;
import com.gengoai.hermes.extraction.regex.TokenMatch;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.hermes.format.DocFormat;
import com.gengoai.hermes.format.DocFormatService;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.hermes.workflow.SequentialWorkflow;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.specification.Specification;
import lombok.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Represents a collection of documents each having a unique document ID. Corpus formats are  defined via corresponding
 * <code>CorpusFormat</code> objects, which are registered using Java's service loader functionality. When constructing
 * a corpus the format can be appended with <code>_OPL</code> to denote that individual file will have one document per
 * line in the given format. For example, TEXT_OPL would relate to a format where every line of a file equates to a
 * document in plain text format.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface Corpus extends DocumentCollection {

   static Corpus open(@NonNull Resource resource) {
      return new LuceneCorpus(resource.asFile().orElseThrow());
   }

   static Corpus open(@NonNull String resource) {
      return open(Resources.from(resource));
   }

   /**
    * Adds a document to the corpus
    *
    * @param document the document to add
    * @return True if added, False if not
    */
   boolean add(Document document);

   /**
    * Adds multiple documents to the corpus.
    *
    * @param documents the documents
    */
   default void addAll(@NonNull Iterable<Document> documents) {
      documents.forEach(this::add);
   }

   @Override
   default Corpus annotate(@NonNull AnnotatableType... annotatableTypes) {
      AnnotationPipeline pipeline = new AnnotationPipeline(Sets.difference(Arrays.asList(annotatableTypes),
                                                                           getCompletedAnnotations()));
      if(pipeline.requiresUpdate()) {
         return update(pipeline::annotate);
      }
      return this;
   }

   @Override
   default Corpus apply(Lexicon lexicon, SerializableConsumer<HString> onMatch) {
      return Cast.as(DocumentCollection.super.apply(lexicon, onMatch));
   }

   @Override
   default Corpus apply(TokenRegex pattern, SerializableConsumer<TokenMatch> onMatch) {
      return Cast.as(DocumentCollection.super.apply(pattern, onMatch));
   }

   /**
    * Compact corpus.
    *
    * @return the corpus
    */
   default Corpus compact() {
      return this;
   }

   /**
    * Gets attribute count.
    *
    * @param <T>  the type parameter
    * @param type the type
    * @return the attribute count
    */
   <T> Counter<T> getAttributeCount(@NonNull AttributeType<T> type);

   /**
    * Gets attribute types.
    *
    * @return the attribute types
    */
   Set<AttributeType<?>> getAttributeTypes();

   /**
    * Gets completed annotations.
    *
    * @return the completed annotations
    */
   Set<AnnotatableType> getCompletedAnnotations();

   /**
    * Gets a document by id
    *
    * @param id the id of the document
    * @return the document or null if it doesn't exist
    */
   default Document getDocument(String id) {
      return parallelStream().filter(d -> d.getId().equals(id)).first().orElse(null);
   }

   default List<String> getIds() {
      return parallelStream().map(Document::getId).sorted(true).collect();
   }

   default Corpus importDocuments(@NonNull String inputSpecification) throws IOException {
      Specification inSpec = Specification.parse(inputSpecification);
      DocFormat format = DocFormatService.create(inSpec);
      addAll(format.read(Resources.from(inSpec.getPath())));
      return this;
   }

   /**
    * Processes the corpus using the given {@link SequentialWorkflow}
    *
    * @param processor the processor
    * @return this Corpus
    * @throws Exception the exception
    */
   default Corpus process(@NonNull SequentialWorkflow processor) throws Exception {
      processor.process(null, new Context());
      return this;
   }

   /**
    * Removes a document from the corpus
    *
    * @param document the document to remove
    * @return True of removed, False otherwise
    */
   boolean remove(Document document);

   /**
    * Removes a document by its id.
    *
    * @param id the id of the document to remove
    * @return True of removed, False otherwise
    */
   boolean remove(String id);

   @Override
   default Corpus repartition(int numPartitions) {
      return this;
   }

   @Override
   Corpus update(@NonNull SerializableConsumer<Document> documentProcessor);

   @Override
   default DocumentCollection update(@NonNull CaduceusProgram program) {
      return this.update(program::execute);
   }

   /**
    * Updates the given document
    *
    * @param document the document to update
    * @return True if the document is updated, False if not
    */
   boolean update(Document document);

}//END OF Corpus
