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

package com.gengoai.hermes.format;

import com.gengoai.Tag;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Relation;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.io.resource.Resource;
import com.gengoai.parsing.Lexer;
import com.gengoai.parsing.TokenDef;
import com.gengoai.parsing.TokenStream;
import com.gengoai.string.Strings;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>Format Name: <b>ptb</b></p>
 * <p>
 * Reader for Penn Treebank <code>mrg</code> files. Provides the following AnnotatableType:
 * </p>
 * <ul>
 *    <li>TOKEN</li>
 *    <li>SENTENCE</li>
 *    <li>PART_OF_SPEECH</li>
 *    <li>CONSTITUENT_PARSE, which adds NON_TERMINAL_NODE annotations and SYNTACTIC_HEAD relations</li>
 * </ul>
 * <p>
 * Function tags are represented on the SYNTACTIC_HEAD relation with the NON_TERMINAL_NODE annotations only have the
 * base part-of-speech. Note this removes all <code>-None-</code> entries.
 * </p>
 */
public class PennTreebankFormat extends WholeFileTextFormat implements OneDocPerFileFormat {
   private final DocFormatParameters parameters;

   PennTreebankFormat(@NonNull DocFormatParameters parameters) {
      this.parameters = parameters;
   }

   @Override
   public DocFormatParameters getParameters() {
      return parameters;
   }

   @Override
   protected Stream<Document> readSingleFile(String content) {
      TokenStream tokenStream = Lexer.create(TT.values()).lex(content);
      Node tree;
      List<List<Node>> sentences = new ArrayList<>();
      StringBuilder documentContent = new StringBuilder();
      while(tokenStream.hasNext() && (tree = recurse(tokenStream)) != null) {
         List<Node> S = toTokens(documentContent, tree);
         if(S.size() > 0) {
            sentences.add(S);
         }
         if(tokenStream.peek().getType().isInstance(TT.CLOSE_PARENS)) {
            tokenStream.consume();
         }
      }

      // Create the document and add tokens (with pos), sentences, and non-terminal nodes.
      Document document = getParameters().getDocumentFactory().create(documentContent.toString().strip());
      for(List<Node> sentence : sentences) {
         document.createAnnotation(Types.SENTENCE,
                                   sentence.get(0).start,
                                   sentence.get(sentence.size() - 1).end(),
                                   Collections.emptyMap());

         // Keeps track of annotations already constructed
         Map<Node, Annotation> node2Annotation = new HashMap<>();

         //Add the tokens for the sentence.
         for(Node t : sentence) {
            node2Annotation.put(t, document.createAnnotation(Types.TOKEN,
                                                             t.start,
                                                             t.end(),
                                                             hashMapOf($(Types.PART_OF_SPEECH,
                                                                         PartOfSpeech.valueOf(t.tag)))));

         }

         // Add non-terminal nodes
         for(Node t : sentence) {
            t.traverseParentToRoot(p -> {
               if(!node2Annotation.containsKey(p)) {
                  var pos = PartOfSpeech.valueOf(p.tag.replaceAll("[-=].*$", ""));
                  var nonTerminal = document.createAnnotation(Types.NON_TERMINAL_NODE,
                                                              p.start(),
                                                              p.end(),
                                                              hashMapOf($(Types.PART_OF_SPEECH, pos)));
                  var ftag = p.tag.replaceAll("^[^\\-]+-", "");
                  if(Strings.isNotNullOrBlank(ftag) && !ftag.equalsIgnoreCase(p.tag)) {
                     nonTerminal.put(Types.SYNTACTIC_FUNCTION, ftag);
                  }
                  node2Annotation.put(p, nonTerminal);
               }
            });
         }

         //Add relations
         for(Node t : sentence) {
            t.traverseParentToRoot(p -> {
               for(Node child : p.children) {
                  node2Annotation.get(child)
                                 .add(new Relation(Types.SYNTACTIC_HEAD,
                                                   p.tag,
                                                   node2Annotation.get(p).getId()));
               }
            });
         }

      }

      document.setCompleted(Types.SENTENCE, "PROVIDED");
      document.setCompleted(Types.TOKEN, "PROVIDED");
      document.setCompleted(Types.PART_OF_SPEECH, "PROVIDED");
      document.setCompleted(Types.CONSTITUENT_PARSE, "PROVIDED");
      return Stream.of(document);
   }

   private Node recurse(TokenStream tokenStream) {
      Tag type = tokenStream.peek().getType();
      if(type == TT.OPEN_PARENS) {
         tokenStream.consume();
         final String label;
         if(tokenStream.peek().isInstance(TT.OTHER)) {
            label = tokenStream.consume().getText();
         } else {
            label = Strings.EMPTY;
         }

         if(Strings.isNullOrBlank(label)) {
            //(  (
            return recurse(tokenStream);
         } else if(label.equalsIgnoreCase("-NONE-")) {
            //(-NONE- ...)
            while(!tokenStream.peek().isInstance(TT.CLOSE_PARENS)) {
               tokenStream.consume();
            }
            tokenStream.consume();
            return recurse(tokenStream);
         }

         //(TAG word)
         if(tokenStream.peek().getType() == TT.OTHER) {
            Node nr = Node.tag(label);
            nr.word = tokenStream.consume().getText();
            tokenStream.consume();
            return nr;
         }

         Node nr = Node.tag(label);
         Node st = recurse(tokenStream);
         while(st != null) {
            st.parent = nr;
            if(st.word == null && st.tag == null && st.children.isEmpty()) {
               st = recurse(tokenStream);
               continue;
            }
            if(st.tag == null && nr.tag == null) {
               nr.word = st.word;
            } else {
               nr.children.add(st);
            }
            st = recurse(tokenStream);
         }

         if(nr.word == null && nr.children.isEmpty()) {
            return new Node();
         }

         return nr;
      } else if(type == TT.CLOSE_PARENS) {
         tokenStream.consume();
         return null;
      } else {
         return Node.word(tokenStream.consume().getText());
      }
   }

   private List<Node> toTokens(StringBuilder documentContent, Node sentence) {
      Stack<Node> stack = new Stack<>();
      for(int i = sentence.children.size() - 1; i >= 0; i--) {
         stack.push(sentence.children.get(i));
      }
      var tokens = new ArrayList<Node>();
      while(!stack.isEmpty()) {
         Node t = stack.pop();
         if(t.word != null) {
            t.word = POSCorrection.word(t.word, t.tag);
            t.start = documentContent.length();
            tokens.add(t);
            documentContent.append(t.word);
            if(parameters.defaultLanguage.value().usesWhitespace()) {
               documentContent.append(" ");
            }
         }
         for(int i = t.children.size() - 1; i >= 0; i--) {
            stack.push(t.children.get(i));
         }
      }
      return tokens;
   }

   @Override
   public void write(Document document, Resource outputResource) {
      throw new UnsupportedOperationException();
   }

   private enum TT implements TokenDef {
      OPEN_PARENS("\\("),
      CLOSE_PARENS("\\)"),
      OTHER("[^\\s\\)\\(]+");

      private final String pattern;

      TT(String pattern) {
         this.pattern = pattern;
      }

      @Override
      public String getPattern() {
         return pattern;
      }
   }

   private static class Node {
      private final List<Node> children = new ArrayList<>();
      private Node parent;
      private String tag;
      private String word;
      private int start = -1;

      public static Node tag(String tag) {
         Node n = new Node();
         n.tag = tag;
         return n;
      }

      public static Node word(String word) {
         Node n = new Node();
         n.word = word;
         return n;
      }

      public int end() {
         if(word == null) {
            return children.get(children.size() - 1).end();
         }
         return start + word.length();
      }

      public int start() {
         if(start == -1) {
            return children.get(0).start();
         }
         return start;
      }

      @Override
      public String toString() {
         return "(" + word + " / " + tag + ") ";
      }

      public void traverseParentToRoot(Consumer<Node> consumer) {
         Node p = parent;
         while(p != null) {
            consumer.accept(p);
            p = p.parent;
         }
      }
   }

   @MetaInfServices
   public static class Provider implements DocFormatProvider {

      @Override
      public DocFormat create(DocFormatParameters parameters) {
         return new PennTreebankFormat(parameters);
      }

      @Override
      public String getName() {
         return "PTB";
      }

      @Override
      public boolean isWriteable() {
         return false;
      }
   }//END OF Provider

}//END OF PennTreebankFormat
