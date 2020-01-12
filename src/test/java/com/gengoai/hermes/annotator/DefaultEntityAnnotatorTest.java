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

package com.gengoai.hermes.annotator;

import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ner.Entities;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class DefaultEntityAnnotatorTest {


   @Test
   public void testAnnotate() throws Exception {
      Config.initializeTest();
      Config.setProperty("Annotation.ENGLISH.ENTITY.annotator", "com.gengoai.hermes.annotator.DefaultEntityAnnotator");
      Config.setProperty("com.gengoai.hermes.annotator.DefaultEntityAnnotator.subTypes", "ENTITY$TOKEN_TYPE_ENTITY");
      Document document = DocumentProvider.getAnnotatedEmoticonDocument();
      document.annotate(Types.ENTITY);
      List<Annotation> entities = document.annotations(Types.ENTITY);

      assertEquals(";-)", entities.get(0).toString());
      assertEquals(Entities.EMOTICON, entities.get(0).getTag().get());

      assertEquals("http://www.somevideo.com/video.html", entities.get(1).toString());
      assertEquals(Entities.URL, entities.get(1).getTag().get());

      assertEquals("$100", entities.get(2).toString());
      assertEquals(Entities.MONEY, entities.get(2).getTag().get());

   }
}