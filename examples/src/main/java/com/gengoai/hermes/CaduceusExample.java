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

package com.gengoai.hermes;

import com.gengoai.config.Config;
import com.gengoai.hermes.extraction.caduceus.CaduceusProgram;
import com.gengoai.io.Resources;

/**
 * @author David B. Bracewell
 */
public class CaduceusExample {

   public static void main(String[] args) throws Exception {
      //Initializes configuration settings
      Config.initialize("CaduceusExample", args);

      //We use the example program
      CaduceusProgram program = CaduceusProgram.read(Resources.fromClasspath("com/gengoai/hermes/example.cg"));

      Document document = DocumentFactory.getInstance().create(
         "John Doe spooked his family while they were on vacation in St. George Falls. He was also spooked though."
                                                              );

      document.annotate(Types.DEPENDENCY, Types.PHRASE_CHUNK);
      program.execute(document);

      RelationType eventRole = Types.relation("EVENT_ROLE");
      AnnotationType eventType = Types.annotation("EVENT");
      document.annotations(eventType).forEach(
         event -> {
            Annotation spooker = event.incoming(eventRole, "SPOOKER").stream().findFirst().orElse(null);
            Annotation spookee = event.incoming(eventRole, "SPOOKEE").stream().findFirst().orElse(null);
            System.out.println("EVENT := " + event);
            System.out.println("\tSPOOKER := " + spooker);
            System.out.println("\tSPOOKEE := " + spookee);
            System.out.println("==================================");
         }
                                             );

   }

}//END OF Sandbox
