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

package com.gengoai.hermes.application;

import com.gengoai.application.Option;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.specification.Specification;
import lombok.NonNull;

import java.io.IOException;

/**
 * <p>Base class to create command line applications utilizing the Hermes framework. Has preset command line parameters
 * for:
 * <pre>
 *    --input (--i, --corpus) : The input location or specification of the corpus to process (Required)
 *    --output (--o) : The output location or specification to write the corpus to.
 * </pre>
 *
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class HermesCorpusCLI extends HermesCLI {
   private static final long serialVersionUID = 1L;
   /**
    * The specification or location the corpus to process.
    */
   @Option(description = "The specification or location the corpus to process.", required = true, aliases = {"i", "corpus"})
   String input;
   /**
    * The output location to write the corpus to.
    */
   @Option(description = "The specification or location  to save the output of the processing.", aliases = {"o"})
   String output;

   /**
    * Instantiates a new Hermes corpus cli.
    */
   public HermesCorpusCLI() {

   }

   /**
    * Instantiates a new Hermes corpus cli.
    *
    * @param applicationName the application name
    */
   public HermesCorpusCLI(String applicationName) {
      super(applicationName);
   }

   /**
    * Creates a corpus based on the command line parameters.
    *
    * @return the corpus
    * @throws IOException the io exception
    */
   public Corpus getCorpus() throws IOException {
      return null;//Corpus.read(input);
   }

   public Specification getInputSpecification() {
      return null; //CorpusIOService.parseCorpusSpecification(input);
   }

   /**
    * Gets output location.
    *
    * @return the output location
    */
   public Specification getOutputSpecification() {
      if(output == null) {
         return null;
      }
      return null;
//      return CorpusIOService.parseCorpusSpecification(output);
   }

   /**
    * Writes the given corpus based on the output location and format.
    *
    * @param corpus the corpus to write
    * @throws IOException Something went wrong writing the corpus
    */
   public void writeCorpus(@NonNull Corpus corpus) throws IOException {
      //corpus.write(output);
   }

}//END OF HermesCommandLineApp
