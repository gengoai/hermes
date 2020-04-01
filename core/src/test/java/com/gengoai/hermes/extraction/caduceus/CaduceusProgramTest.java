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

package com.gengoai.hermes.extraction.caduceus;

import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.RelationType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.DocumentProvider;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class CaduceusProgramTest {

   @Test
   public void testExecute() throws Exception {
      Config.initializeTest();
      CaduceusProgram p = CaduceusProgram.read(
         Resources.fromClasspath("com/gengoai/hermes/extraction/caduceus/example.cg"));
      Document doc = DocumentProvider.getDocument();
      doc.annotate(Types.TOKEN, Types.SENTENCE);
      p.execute(doc);
      List<Annotation> entities = doc.annotations(Types.ENTITY);
      assertEquals(9, entities.size());

      assertEquals("Alice", entities.get(0).toString());
      assertEquals("Alice", entities.get(1).toString());
      assertEquals("White Rabbit", entities.get(2).toString());
      assertEquals("eyes", entities.get(3).toString());
      assertEquals("Alice", entities.get(4).toString());
      assertEquals("Rabbit", entities.get(5).toString());
      assertEquals("Rabbit", entities.get(6).toString());
      assertEquals("Alice", entities.get(7).toString());
      assertEquals("rabbit", entities.get(8).toString());


      assertEquals(1, entities.get(2).outgoingRelations().size());
      assertEquals(RelationType.make("ATTRIBUTE"), entities.get(2)
                                                           .outgoingRelations()
                                                           .stream()
                                                           .findFirst()
                                                           .get()
                                                           .getType());
      assertEquals("HAS_A", entities.get(2).outgoingRelations().stream().findFirst().get().getValue());

   }
}