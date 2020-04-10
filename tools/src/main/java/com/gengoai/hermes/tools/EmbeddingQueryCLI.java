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

import com.gengoai.apollo.ml.Model;
import com.gengoai.apollo.ml.embedding.Embedding;
import com.gengoai.apollo.ml.embedding.VSQuery;
import com.gengoai.application.Option;
import com.gengoai.io.resource.Resource;

import java.io.Console;

/**
 * The type Embedding query.
 *
 * @author David B. Bracewell
 */
public class EmbeddingQueryCLI extends HermesCLI {

   @Option(description = "The embedding model to query.", required = true)
   private Resource model;

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    * @throws Exception the exception
    */
   public static void main(String[] args) throws Exception {
      String[] nargs = new String[args.length + 2];
      System.arraycopy(args, 0, nargs, 0, args.length);
      nargs[nargs.length - 2] = "--input";
      nargs[nargs.length - 1] = "/dev/null";
      new EmbeddingQueryCLI().run(nargs);
   }

   @Override
   protected void programLogic() throws Exception {
      Embedding embedding = Model.read(model);


      Console console = System.console();
      String line;
      do {
         line = console.readLine("query:> ");
         if (line.equals("?quit") || line.equals("?q")) {
            System.exit(0);
         } else if (line.startsWith("?search") || line.startsWith("?s")) {
            String search = line.substring(line.indexOf(' ')).trim();
            embedding.getAlphabet().parallelStream()
                     .filter(term -> term.startsWith(search))
                     .forEach(term -> System.out.println("  " + term));
         } else if (embedding.contains(line)) {
            embedding.query(VSQuery.termQuery(line.toLowerCase()).limit(10)).forEach(
               slv -> System.out.println("  " + slv.getLabel() + " : " + slv.getWeight()));
            System.out.println();
         } else {
            System.out.println("!! " + line + " is not in the dictionary");
         }

      } while (!line.equals("q!"));

   }

}//END OF EmbeddingQuery
