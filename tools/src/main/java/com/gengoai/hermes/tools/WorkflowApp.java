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

import com.gengoai.application.Option;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.hermes.workflow.Workflow;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.specification.Specification;

public class WorkflowApp extends HermesCLI {
   private static final long serialVersionUID = 1L;
   public static final String CONTEXT_OUTPUT = "CONTEXT_OUTPUT";
   /**
    * Name of the context parameter for the location of the input corpus
    */
   public static final String INPUT_LOCATION = "INPUT_LOCATION";
   @Option(name = "context-output", description = "Location to save context", aliases = {"oc"})
   private Resource contextOutputLocation = null;
   @Option(name = "definition", description = "Workflow definition")
   private Resource definition = null;
   @Option(description = "The specification or location the document collection to process.", required = true, aliases = {"i", "corpus"})
   private String input;

   public static void main(String[] args) throws Exception {
      new WorkflowApp().run(args);
   }

   public DocumentCollection getDocumentCollection(String spec) {
      try {
         Specification.parse(spec);
         return DocumentCollection.create(spec);
      } catch(Exception e) {
         return Corpus.open(spec);
      }
   }

   @Override
   protected void programLogic() throws Exception {
      Context context = Context.builder()
                               .property(INPUT_LOCATION, input)
                               .property(CONTEXT_OUTPUT, contextOutputLocation)
                               .build();
      Workflow workflow = Json.parse(definition, Workflow.class);
      try(DocumentCollection inputCorpus = getDocumentCollection(input)) {
         try(DocumentCollection outputCorpus = workflow.process(inputCorpus, context)) {
            if(contextOutputLocation != null) {
               contextOutputLocation.write(Json.dumpsPretty(context));
            }
         }
      }
   }

}//END OF Runner
