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

package com.gengoai.hermes.extraction.regex;


import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.tuple.Tuple2;
import com.gengoai.tuple.Tuples;

import java.io.Serializable;
import java.util.*;

import static com.gengoai.string.Strings.safeEquals;

/**
 * Implementation of a non-deterministic finite state automata that works on a Text
 */
public final class NFA implements Serializable {

   private static final long serialVersionUID = 1L;
   /**
    * The End.
    */
   Node end = new Node(true);
   /**
    * The Start.
    */
   Node start = new Node(false);

   /**
    * Matches the
    *
    * @param input      the input
    * @param startIndex the start index
    * @return the int
    */
   public TokenMatch matches(HString input, int startIndex) {
      //The set of states that the NFA is in
      Set<State> states = new HashSet<>();


      //Add the start state
      states.add(new State(startIndex, start));

      //All the accept states that it enters
      NavigableSet<State> accepts = new TreeSet<>();

      List<Annotation> tokens = input.tokens();


      while (!states.isEmpty()) {
         Set<State> newStates = new HashSet<>(); //states after the next consumption

         for (State s : states) {

            if (s.node.accepts()) {
               if (s.stack.isEmpty() ||
                  (s.stack.size() == 1 && s.node.consumes && safeEquals(s.node.name, s.stack.peek().v1, true))) {
                  accepts.add(s);
               }
            }

            Deque<Tuple2<String, Integer>> currentStack = s.stack;
            if (s.node.emits) {
               currentStack.push(Tuples.$(s.node.name, s.inputPosition));
            }

            for (Node n : s.node.epsilons) {
               if (s.node.consumes) {
                  State next = new State(s.inputPosition, n, currentStack, s.namedGroups);
                  Tuple2<String, Integer> ng = next.stack.pop();
                  next.namedGroups.put(ng.getKey(), HString.union(tokens.subList(ng.v2, s.inputPosition)));
                  newStates.add(next);
               }

               State next = new State(s.inputPosition, n, currentStack, s.namedGroups);
               newStates.add(next);
            }

            if (s.inputPosition >= input.tokenLength()) {
               continue;
            }

            for (Transition t : s.node.transitions) {
               int len = t.transitionFunction.matches(tokens.get(s.inputPosition),s.namedGroups);
               if (len > 0) {
                  State next = new State(s.inputPosition + len, t.destination, currentStack, s.namedGroups);
                  newStates.add(next);
               }
            }
         }
         states.clear();
         states = newStates;
      }

      if (accepts.isEmpty()) {
         return new TokenMatch(input,
                               -1,
                               -1,
                               null);
      }


      State last = accepts.last();
      int max = last.inputPosition;

      State temp = accepts.last();
      while (temp != null && temp.inputPosition >= max) {
         temp = accepts.lower(temp);
      }

      if (max == startIndex) {
         max++;
      }

      return new TokenMatch(input, startIndex, max, last.namedGroups);
   }

   /**
    * The type Node.
    */
   static class Node implements Serializable {
      private static final long serialVersionUID = 1L;
      /**
       * The Epsilons.
       */
      final List<Node> epsilons = new ArrayList<>();
      /**
       * The Transitions.
       */
      final List<Transition> transitions = new ArrayList<>();
      /**
       * The Consumes.
       */
      boolean consumes = false;
      /**
       * The Emits.
       */
      boolean emits = false;
      /**
       * The Is accept.
       */
      boolean isAccept;
      /**
       * The Name.
       */
      String name = null;

      /**
       * Instantiates a new Node.
       *
       * @param accept the accept
       */
      public Node(boolean accept) {
         this.isAccept = accept;
      }

      /**
       * Accepts boolean.
       *
       * @return the boolean
       */
      public boolean accepts() {
         return isAccept;
      }

      /**
       * Connect void.
       *
       * @param node the node
       */
      public void connect(Node node) {
         epsilons.add(node);
      }

      /**
       * Connect void.
       *
       * @param node               the node
       * @param transitionFunction the consumer
       */
      public void connect(Node node, TransitionFunction transitionFunction) {
         transitions.add(new Transition(this, node, transitionFunction));
      }

      @Override
      public String toString() {
         return super.toString() + "[" + accepts() + "]";
      }

   }//END OF NFA$Node

   /**
    * The type State.
    */
   static class State implements Comparable<State> {
      /**
       * The Input position.
       */
      final int inputPosition;
      /**
       * The Named groups.
       */
      final ListMultimap<String, HString> namedGroups = new ArrayListMultimap<>();
      /**
       * The Node.
       */
      final Node node;
      /**
       * The Stack.
       */
      final Deque<Tuple2<String, Integer>> stack;

      /**
       * Instantiates a new State.
       *
       * @param inputPosition the input position
       * @param node          the node
       */
      public State(int inputPosition, Node node) {
         this(inputPosition, node, new LinkedList<>(), new ArrayListMultimap<>());
      }

      /**
       * Instantiates a new State.
       *
       * @param inputPosition the input position
       * @param node          the node
       * @param currentStack  the current stack
       * @param namedGroups   the named groups
       */
      public State(int inputPosition,
                   Node node,
                   Deque<Tuple2<String, Integer>> currentStack,
                   ListMultimap<String, HString> namedGroups) {
         this.inputPosition = inputPosition;
         this.node = node;
         this.stack = new LinkedList<>(currentStack);
         this.namedGroups.putAll(namedGroups);
      }

      @Override
      public int compareTo(State o) {
         return Integer.compare(score(), o.score());
      }

      private int score() {
         return inputPosition + namedGroups.size() * namedGroups.values().stream().mapToInt(HString::tokenLength).sum();
      }
   }//END OF NFA$State

   /**
    * The type Transition.
    */
   static class Transition implements Serializable {

      private static final long serialVersionUID = 1L;
      /**
       * The Source.
       */
      final Node source;
      /**
       * The Transition function.
       */
      final TransitionFunction transitionFunction;
      /**
       * The Destination.
       */
      Node destination;

      /**
       * Instantiates a new Transition.
       *
       * @param source             the source
       * @param destination        the destination
       * @param transitionFunction the consumer
       */
      public Transition(Node source, Node destination, TransitionFunction transitionFunction) {
         this.source = source;
         this.destination = destination;
         this.transitionFunction = transitionFunction;
      }

      @Override
      public String toString() {
         return "[" + destination + ", " + transitionFunction + "]";
      }

   }//END OF NFA$Transition

}//END OF NFA
