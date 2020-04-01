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

import com.gengoai.Language;
import com.gengoai.collection.Sets;
import com.gengoai.hermes.*;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.FileResource;
import com.gengoai.io.resource.Resource;
import lombok.extern.java.Log;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.concurrent.graph.ConcurrentDependencyEdge;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;
import org.maltparser.concurrent.graph.ConcurrentDependencyNode;
import org.maltparser.core.exception.MaltChainedException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.gengoai.LogUtils.logFine;

/**
 * The type Default dependency annotator.
 *
 * @author David B. Bracewell
 */
@Log
public class DefaultDependencyAnnotator extends SentenceLevelAnnotator {
   private static final long serialVersionUID = 1L;
   private static volatile Map<Language, ConcurrentMaltParserModel> models = new ConcurrentHashMap<>();

   @Override
   public void annotate(Annotation sentence) {
      ConcurrentMaltParserModel model = getModel(sentence.getLanguage());
      List<Annotation> tokens = sentence.tokens();
      String[] input = new String[tokens.size()];
      for(int i = 0; i < tokens.size(); i++) {
         Annotation token = tokens.get(i);
         input[i] = (i + 1) + "\t" + token.toString() + "\t" + token.getLemma() + "\t" + token.pos().tag() + "\t" +
               token.pos().tag() + "\t_";
      }
      try {
         ConcurrentDependencyGraph graph = model.parse(input);
         for(int i = 1; i <= graph.nTokenNodes(); i++) {
            ConcurrentDependencyNode node = graph.getTokenNode(i);
            ConcurrentDependencyEdge edge = node.getHeadEdge();
            Annotation child = tokens.get(node.getIndex() - 1);
            if(edge.getSource().getIndex() != 0) {
               Annotation parent = tokens.get(edge.getSource().getIndex() - 1);
               child.add(new Relation(Types.DEPENDENCY, edge.getLabel("DEPREL"), parent.getId()));
            }
         }
      } catch(MaltChainedException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   protected Set<AnnotatableType> furtherRequires() {
      return Sets.hashSetOf(Types.PART_OF_SPEECH, Types.LEMMA);
   }

   private ConcurrentMaltParserModel getModel(Language language) {
      if(!models.containsKey(language)) {
         synchronized(this) {
            if(!models.containsKey(language)) {
               Resource r = ResourceType.MODEL.locate("Relation.DEPENDENCY", "dependency", language)
                                              .orElse(null);
               Exception thrownException = null;
               if(r != null && r.exists()) {
                  if(!(r instanceof FileResource)) {
                     Resource tmpLocation = Resources.temporaryFile();
                     tmpLocation.deleteOnExit();
                     try {
                        logFine(log, "Writing dependency model to temporary file [{0}].", tmpLocation);
                        tmpLocation.write(r.readBytes());
                        r = tmpLocation;
                     } catch(IOException e) {
                        //no opt
                     }
                  }
                  if(r instanceof FileResource) {
                     try {
                        models.put(language,
                                   ConcurrentMaltParserService.initializeParserModel(r.asURL().get()));
                        return models.get(language);
                     } catch(Exception e) {
                        thrownException = e;
                     }
                  }
               }
               if(thrownException == null) {
                  throw new RuntimeException("Dependency model does not exist");
               } else {
                  throw new RuntimeException(thrownException);
               }
            }
         }
      }
      return models.get(language);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.DEPENDENCY);
   }
}//END OF MaltParserAnnotator
