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

import com.gengoai.application.CommandLineApplication;
import com.gengoai.application.Option;
import com.gengoai.hermes.lexicon.DiskLexicon;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconIO;
import com.gengoai.hermes.lexicon.LexiconSpecification;

public class csv2Lexicon extends CommandLineApplication {

   @Option(description = "The CSV specification", required = true)
   private String csv;

   @Option(description = "The lexicon specification", required = true)
   private String lexicon;

   public static void main(String[] args) throws Exception {
      new csv2Lexicon().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      LexiconSpecification csvSpec = LexiconSpecification.parse(csv);
      if (!csvSpec.getFormat().equals("csv")) {
         throw new IllegalStateException("Expecting CSV format, but found " + csvSpec.getFormat());
      }

      LexiconSpecification outSpec = LexiconSpecification.parse(lexicon);
      if (outSpec.getProtocol().equals("mem")) {
         Lexicon out = csvSpec.create();
         LexiconIO.write(out, outSpec.getResource(), null);
      } else {
         Lexicon out = csvSpec.create();
         DiskLexicon.DiskLexiconBuilder builder = DiskLexicon.builder(out.getTagAttributeType(), out.isCaseSensitive(),
                                                                      outSpec.getName(),
                                                                      outSpec.getResource());
         builder.addAll(out.entries());
         builder.build();
      }


   }
}//END OF csv2Lexicon
