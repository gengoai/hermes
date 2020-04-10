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
 * A persistent collection of documents each having a unique document ID. In addition to the functionality provided by
 * {@link DocumentCollection}, corpora allow:
 * <ul>
 *    <li>Access to individual documents via {@link #getDocument(String)}, {@link #remove(String)},
 *    {@link #remove(Document)}, and {@link #update(Document)} methods.</li>
 *    <li>Ability to add new documents via the {@link #add(Document)}, {@link #addAll(Iterable)}, and
 *    {@link #importDocuments(String)} methods.</li>
 *    <li>Aggregation of document level metadata via {@link #getAttributeValueCount(AttributeType)}</li>
 *    <li>AnnotatableType completed at the corpus level via {@link #getCompleted()}</li>
 *    <li>Aggregation of the document ids in the corpus via {@link #getIds()}</li>
 * </ul>
 * </p>
 * <p>
 * Corpora are opened using the {@link #open(String)} or {@link #open(Resource)} methods which will load the appropriate
 * corpus implementation based on the resource type.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface Corpus extends DocumentCollection {

   /**
    * Opens the corpus at the given resource.
    *
    * @param resource the resource pertaining to the corpus
    * @return the corpus
    */
   static Corpus open(@NonNull Resource resource) {
      return new LuceneCorpus(resource.asFile().orElseThrow());
   }

   /**
    * Opens the corpus at the given resource.
    *
    * @param resource the resource pertaining to the corpus
    * @return the corpus
    */
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
                                                                           getCompleted()));
      if(pipeline.requiresUpdate()) {
         return update("Annotate", pipeline::annotate);
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
    * Compacts the storage used for the corpus.
    *
    * @return This corpus
    */
   default Corpus compact() {
      return this;
   }

   /**
    * Gets a count of the values for the given attribute across documents in the corpus.
    *
    * @param <T>  the attribute value type parameter
    * @param type the AttributeType we want to count
    * @return A Counter over the attribute values.
    */
   <T> Counter<T> getAttributeValueCount(@NonNull AttributeType<T> type);

   /**
    * @return the set of attribute types found across the documents in the corpus
    */
   Set<AttributeType<?>> getAttributes();

   /**
    * @return the set of completed AnnotatableType where completed means completed by every document in the corpus.
    */
   Set<AnnotatableType> getCompleted();

   /**
    * Gets the document with the given document id
    *
    * @param id the id of the document
    * @return the document or null if it doesn't exist
    */
   default Document getDocument(String id) {
      return parallelStream().filter(d -> d.getId().equals(id)).first().orElse(null);
   }

   /**
    * @return the document ids of all documents in the corpus
    */
   default List<String> getIds() {
      return parallelStream().map(Document::getId).sorted(true).collect();
   }

   /**
    * Imports documents from the given document collection specification.
    *
    * @param specification the document format specification with path to documents.
    * @return the corpus
    * @throws IOException Something went wrong loading the documents
    */
   default Corpus importDocuments(@NonNull String specification) throws IOException {
      Specification inSpec = Specification.parse(specification);
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
   Corpus update(@NonNull String operation, @NonNull SerializableConsumer<Document> documentProcessor);

   @Override
   default Corpus update(@NonNull CaduceusProgram program) {
      return Cast.as(DocumentCollection.super.update(program));
   }

   /**
    * Updates the given document
    *
    * @param document the document to update
    * @return True if the document is updated, False if not
    */
   boolean update(Document document);

}//END OF Corpus
