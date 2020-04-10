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

package com.gengoai.hermes.tools;

import com.gengoai.ParameterDef;
import com.gengoai.application.Application;
import com.gengoai.application.Option;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.corpus.SearchResults;
import com.gengoai.hermes.format.DocFormatParameters;
import com.gengoai.hermes.format.DocFormatProvider;
import com.gengoai.hermes.format.DocFormatService;
import com.gengoai.string.Strings;
import com.gengoai.string.TableFormatter;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.*;

import static com.gengoai.LogUtils.logSevere;

@Application.Description(
      "===========================================================\n" +
            "           Application for working with corpora.\n" +
            "===========================================================\n" +
            "                         Operations\n" +
            "---------------------------------------------------------\n" +
            "INFO -  Displays the number of documents and completed AnnotatableType for the corpus.\n" +
            "QUERY - Queries the corpus with the given query returning the top 10 results.\n" +
            "GET - Gets the given document (or a random one if *rnd* is given) in Json format.\n" +
            "IMPORT - Imports the documents from the input document collection into the corpus.\n" +
            "ANNOTATE - Annotates the corpus with the given annotatable types.\n" +
            "FORMATS - List the available document formats and their parameters.\n" +
            "---------------------------------------------------------\n" +
            "\n                     Command Line Arguments "
)
@Log
public class CorpusApp extends HermesCLI {
   @Option(description = "The specification or location the corpus or document collection to process.",
         name = "docFormat",
         aliases = {"df"})
   private String documentCollectionSpec;
   @Option(description = "The specification or location to save the output of the processing.",
         name = "corpus",
         aliases = {"c"})
   private String corpusLocation;
   @Option(description = "Annotations to add",
         defaultValue = "Annotation.TOKEN,Annotation.SENTENCE,Attribute.PART_OF_SPEECH,Attribute.LEMMA,Relation.DEPENDENCY,Annotation.PHRASE_CHUNK,Annotation.ENTITY",
         aliases = "t")
   private String[] types;

   public static void main(String[] args) throws Exception {
      new CorpusApp().run(args);
   }

   private void corpusAnnotate() throws Exception {
      if(types == null) {
         logSevere(log, "No AnnotatableTypes Given!");
         System.exit(-1);
      }
      try(Corpus corpus = getCorpus()) {
         corpus.annotate(stringToAnnotatableType());
      }
   }

   private void corpusGetDocument() throws Exception {
      ensurePositionalArgument(1, "No Document Id Given!");
      final String id;
      if(getPositionalArgs()[1].equalsIgnoreCase("*rnd*")) {
         final Random rnd = new Random();
         final List<String> ids = getCorpus().getIds();
         id = ids.get(rnd.nextInt(ids.size()));
      } else {
         id = getPositionalArgs()[0];
      }
      try(Corpus corpus = getCorpus()) {
         final Document document = corpus.getDocument(id);
         if(document == null) {
            System.err.println(id + " does not exists in the corpus.");
         } else {
            System.out.println(document.toJson());
         }
      }
   }

   private void corpusInfo() throws Exception {
      try(Corpus corpus = getCorpus()) {
         final long size = corpus.size();
         final Set<AnnotatableType> completedAnnotations = corpus.getCompleted();
         System.out.println("                 Corpus Information");
         System.out.println("========================================================");
         System.out.println("Corpus: " + documentCollectionSpec);
         System.out.println("# of Documents: " + size);
         System.out.println("========================================================");
         System.out.println("              Completed AnnotatableTypes");
         System.out.println("------------------------------------------------------");
         for(AnnotatableType type : completedAnnotations) {
            System.out.println(type);
         }
         System.out.println("========================================================");
      }
   }

   private void corpusQuery() throws Exception {
      ensurePositionalArgument(1, "No Query Given!");
      try(Corpus corpus = getCorpus()) {
         final SearchResults searchResults = corpus.query(getPositionalArgs()[1]);
         System.out.println("                     Query Results");
         System.out.println("========================================================");
         System.out.println("Corpus: " + corpusLocation);
         System.out.println("Query: " + getPositionalArgs()[0]);
         System.out.println("Total hits: " + searchResults.size());
         System.out.println("========================================================");
         System.out.println("                      Top 10 Results");
         System.out.println("------------------------------------------------------");
         getCorpus().query(getPositionalArgs()[0])
                    .stream()
                    .limit(10)
                    .forEach(doc -> {
                       System.out.print("Document ID: ");
                       System.out.println(doc.getId());
                       System.out.print("Content: ");
                       System.out.print(doc.substring(0, Math.min(255, doc.length())));
                       System.out.println("...");
                       System.out.println("===============");
                    });
         System.out.println("========================================================");
      }
   }

   private void ensurePositionalArgument(int length, String message) {
      if(getPositionalArgs().length <= length) {
         logSevere(log, message);
         System.exit(-1);
      }
   }

   private Corpus getCorpus() throws IOException {
      if(Strings.isNullOrBlank(corpusLocation)) {
         logSevere(log, "No Corpus Specified!");
         System.exit(-1);
      }
      return Corpus.open(corpusLocation);
   }

   private DocumentCollection getDocumentCollection() {
      if(Strings.isNullOrBlank(documentCollectionSpec)) {
         logSevere(log, "No Document Collection Specified!");
         System.exit(-1);
      }
      return DocumentCollection.create(documentCollectionSpec);
   }

   private void importDocuments() throws Exception {
      try(DocumentCollection input = getDocumentCollection()) {
         try(Corpus writeTo = getCorpus()) {
            writeTo.addAll(getDocumentCollection());
         }
      }
   }

   private void listFormats() throws Exception {
      System.out.println("                 Document Formats");
      System.out.println("========================================================");
      System.out.println();
      for(DocFormatProvider provider : DocFormatService.getProviders()) {
         TableFormatter tableFormatter = new TableFormatter();
         tableFormatter.title(provider.getName().toUpperCase());
         tableFormatter.header(Arrays.asList("ParameterName", "ParameterType"));
         final DocFormatParameters parameters = provider.getDefaultFormatParameters();
         for(String parameterName : new TreeSet<>(parameters.parameterNames())) {
            ParameterDef<?> param = parameters.getParam(parameterName);
            tableFormatter.content(Arrays.asList(parameterName, param.type.getSimpleName()));
         }
         tableFormatter.print(System.out);
         System.out.println();
      }
      System.out.println("========================================================");
   }

   @Override
   protected void programLogic() throws Exception {
      ensurePositionalArgument(0, "No Operation Given!");
      final String operation = getPositionalArgs()[0];
      switch(operation) {
         case "INFO":
            corpusInfo();
            break;
         case "QUERY":
            corpusQuery();
            break;
         case "IMPORT":
            importDocuments();
            break;
         case "GET":
            corpusGetDocument();
            break;
         case "FORMATS":
            listFormats();
            break;
         case "ANNOTATE":
            corpusAnnotate();
            break;
         default:
            logSevere(log, "Invalid Operation: {0}", operation);
      }
   }

   private AnnotatableType[] stringToAnnotatableType() {
      AnnotatableType[] convert = new AnnotatableType[types.length];
      for(int i = 0; i < types.length; i++) {
         convert[i] = AnnotatableType.valueOf(types[i]);
      }
      return convert;
   }
}//END OF CorpusApp
