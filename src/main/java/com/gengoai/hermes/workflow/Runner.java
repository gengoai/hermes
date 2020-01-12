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

package com.gengoai.hermes.workflow;

import com.gengoai.application.Option;
import com.gengoai.hermes.application.HermesCorpusCLI;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.specification.Specification;

public class Runner extends HermesCorpusCLI {
   public static final String CONTEXT_OUTPUT = "CONTEXT_OUTPUT";
   /**
    * Name of the context parameter for the location of the input corpus
    */
   public static final String INPUT_LOCATION = "INPUT_LOCATION";
   /**
    * Name of the context parameter for the location to write the resulting corpus to.
    */
   public static final String OUTPUT_LOCATION = "OUTPUT_LOCATION";
   private static final long serialVersionUID = 1L;
   @Option(name = "context-output", description = "Location to save context", aliases = {"oc"})
   private Resource contextOutputLocation = null;
   @Option(name = "definition", description = "Workflow definition")
   private Resource definition = null;

   public static void main(String[] args) throws Exception {
      new Runner().run(args);
   }

   protected boolean differentCorpusOutput(Corpus inCorpus, Corpus outCorpus) {
      Specification in = getInputSpecification();
      Specification out = getOutputSpecification();
      return (inCorpus != outCorpus) ||
         !inCorpus.isPersistent() ||
         !in.getSchema().equals(out.getSchema()) ||
         !in.getPath().equals(out.getPath());
   }

   @Override
   protected void programLogic() throws Exception {
      Context context = Context.builder()
                               .property(INPUT_LOCATION, getInputSpecification())
                               .property(OUTPUT_LOCATION, getOutputSpecification())
                               .property(CONTEXT_OUTPUT, contextOutputLocation)
                               .build();
      Workflow workflow = Json.parse(definition, Workflow.class);
      try (Corpus input = getCorpus()) {
         try (Corpus output = workflow.process(input, context)) {
            if (getOutputSpecification() != null && differentCorpusOutput(input, output)) {
               writeCorpus(output);
            }
            if (contextOutputLocation != null) {
               contextOutputLocation.write(Json.asJsonEntry(context).pprint(3));
            }
         }
      }
   }

}//END OF Runner
