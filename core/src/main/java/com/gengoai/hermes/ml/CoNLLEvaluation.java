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

package com.gengoai.hermes.ml;

import com.gengoai.Validation;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.evaluation.SequenceLabelerEvaluation;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.collection.Sets;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.MStream;
import com.gengoai.string.Strings;
import com.gengoai.string.TableFormatter;
import com.gengoai.tuple.Tuple3;
import lombok.NonNull;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * Evaluation used in CoNLL for Named Entity Recognition. Defined as: "Precision is the percentage of named entities
 * found by the learning system that are correct. Recall is the percentage of named entities present in the corpus that
 * are found by the system. A named entity is correct only if it is an exact match of the corresponding entity in the
 * data file"
 * </p>
 * <p>
 * Sang, Erik F. Tjong Kim and Fien De Meulder. “Introduction to the CoNLL-2003 Shared Task: Language-Independent Named
 * Entity Recognition.” ArXiv cs.CL/0306050 (2003): n. pag.
 * </p>
 *
 * @author David B. Bracewell
 */
public class CoNLLEvaluation implements SequenceLabelerEvaluation {

   private final Counter<String> incorrect = Counters.newCounter();
   private final Counter<String> correct = Counters.newCounter();
   private final Counter<String> missed = Counters.newCounter();
   private final Set<String> tags = new HashSet<>();
   private final String outputName;
   private double totalPhrasesGold = 0;
   private double totalPhrasesFound = 0;

   /**
    * Instantiates a new CoNLLEvaluation.
    *
    * @param outputName the output name of the model to evaluate
    */
   public CoNLLEvaluation(String outputName) {
      this.outputName = Validation.notNullOrBlank(outputName);
   }

   /**
    * <p>Calculates the accuracy of the system as defined as:</p>
    * <pre>
    * {@code
    *    accuracy = correct / (correct + incorrect + missed)
    * }
    * </pre>
    *
    * @return the accuracy of the model
    */
   public double accuracy() {
      return correct.sum() / (correct.sum() + incorrect.sum() + missed.sum());
   }

   private void entry(Set<Tuple3<Integer, Integer, String>> gold, Set<Tuple3<Integer, Integer, String>> pred) {
      totalPhrasesFound += pred.size();
      totalPhrasesGold += gold.size();
      Sets.union(gold, pred).stream()
          .map(Tuple3::getV3)
          .forEach(tags::add);
      Sets.intersection(gold, pred).stream()
          .map(Tuple3::getV3)
          .forEach(correct::increment);
      Sets.difference(gold, pred).stream()
          .map(Tuple3::getV3)
          .forEach(missed::increment);
      Sets.difference(pred, gold).stream()
          .map(Tuple3::getV3)
          .forEach(incorrect::increment);
   }

   @Override
   public void evaluate(@NonNull Model model, @NonNull DataSet dataset) {
      dataset.forEach(datum -> entry(tags(datum.get(outputName).asSequence()),
                                     tags(model.transform(datum).get(outputName).asSequence())));
   }

   @Override
   public void evaluate(@NonNull Model model, @NonNull MStream<Datum> dataset) {
      dataset.forEach(datum -> entry(tags(datum.get(outputName).asSequence()),
                                     tags(model.transform(datum).get(outputName).asSequence())));
   }

   private double f1(double p, double r) {
      if(p + r == 0) {
         return 0;
      }
      return (2 * p * r) / (p + r);
   }

   /**
    * <p>Calculates the F1 measure of the system as defined as:</p>
    * <pre>
    * {@code
    *    F1 = (2 * precision * recall) / (precision + recall)
    * }
    * </pre>
    *
    * @return the F1 measure of the model
    */
   public double f1(String label) {
      return f1(precision(label), recall(label));
   }

   /**
    * <p>Calculates the macro F1-measure or the arithmetic mean of the per-class F1-scores.</p>
    *
    * @return the macro F1-measure
    */
   public double macroF1() {
      double avg = 0;
      for(String tag : tags) {
         avg += f1(tag);
      }
      return avg / tags.size();
   }

   /**
    * <p>Calculates the macro precision or the arithmetic mean of the per-class precision-scores.</p>
    *
    * @return the macro precision
    */
   public double macroPrecision() {
      double avg = 0;
      for(String tag : tags) {
         avg += precision(tag);
      }
      return avg / tags.size();
   }

   /**
    * <p>Calculates the macro recall or the arithmetic mean of the per-class recall-scores.</p>
    *
    * @return the macro recall
    */
   public double macroRecall() {
      double avg = 0;
      for(String tag : tags) {
         avg += recall(tag);
      }
      return avg / tags.size();
   }

   @Override
   public void merge(@NonNull SequenceLabelerEvaluation evaluation) {
      Validation.checkArgument(evaluation instanceof CoNLLEvaluation);
      CoNLLEvaluation other = Cast.as(evaluation);
      incorrect.merge(other.incorrect);
      correct.merge(other.correct);
      missed.merge(other.missed);
      tags.addAll(other.tags);
   }

   /**
    * <p>Calculates the micro F1-measure which is the F1-measure over the micro-precision and micro-recall</p>
    *
    * @return the micro F1-measure
    */
   public double microF1() {
      return f1(microPrecision(), microRecall());
   }

   /**
    * <p>Calculates the micro-precision of the system as defined as:</p>
    * <pre>
    * {@code
    *    micro-precision = sum(correct) / ( sum(correct) + sum(incorrect) )
    * }
    * </pre>
    *
    * @return the micro-precision of the model
    */
   public double microPrecision() {
      double c = correct.sum();
      double i = incorrect.sum();
      if(i + c <= 0) {
         return 1.0;
      }
      return c / (c + i);
   }

   /**
    * <p>Calculates the micro-recall of the system as defined as:</p>
    * <pre>
    * {@code
    *    micro-recall = sum(correct) / ( sum(correct) + sum(missed) )
    * }
    * </pre>
    *
    * @return the micro-recall of the model
    */
   public double microRecall() {
      double c = correct.sum();
      double m = missed.sum();
      if(m + c <= 0) {
         return 1.0;
      }
      return c / (c + m);
   }

   @Override
   public void output(@NonNull PrintStream printStream, boolean printConfusionMatrix) {
      Set<String> sorted = new TreeSet<>(tags);
      printStream.println("Total Gold Phrases: " + totalPhrasesGold);
      printStream.println("Total Predicted Phrases: " + totalPhrasesFound);
      printStream.println("Total Correct: " + correct.sum());
      TableFormatter tableFormatter = new TableFormatter();
      tableFormatter.setMinCellWidth(5);
      tableFormatter.setNumberFormatter(new DecimalFormat("#,###"));
      DecimalFormat pct = new DecimalFormat("0.0%");
      tableFormatter
            .title("Tag Metrics")
            .header(
                  Arrays.asList(Strings.EMPTY, "Precision", "Recall", "F1-Measure", "Correct", "Missed", "Incorrect"));
      sorted.forEach(g ->
                           tableFormatter.content(Arrays.asList(
                                 g,
                                 pct.format(precision(g)),
                                 pct.format(recall(g)),
                                 pct.format(f1(g)),
                                 correct.get(g),
                                 missed.get(g),
                                 incorrect.get(g)
                                                               ))
                    );
      tableFormatter.footer(Arrays.asList(
            "micro",
            pct.format(microPrecision()),
            pct.format(microRecall()),
            pct.format(microF1()),
            correct.sum(),
            missed.sum(),
            incorrect.sum()
                                         ));
      tableFormatter.footer(Arrays.asList(
            "macro",
            pct.format(macroPrecision()),
            pct.format(macroRecall()),
            pct.format(macroF1()),
            "-",
            "-",
            "-"
                                         ));
      tableFormatter.print(printStream);

   }

   /**
    * <p>Calculates the precision of the system for a given label defined as:</p>
    * <pre>
    * {@code
    *    precision = correct / ( correct + incorrect )
    * }
    * </pre>
    *
    * @return the precision of the model
    */
   public double precision(String label) {
      Validation.notNullOrBlank(label);
      double c = correct.get(label);
      double i = incorrect.get(label);
      if(i + c <= 0) {
         return 1.0;
      }
      return c / (c + i);
   }

   /**
    * <p>Calculates the recall of the system for a given label defined as:</p>
    * <pre>
    * {@code
    *    precision = correct / ( correct + missed )
    * }
    * </pre>
    *
    * @return the recall of the model
    */
   public double recall(String label) {
      double c = correct.get(label);
      double m = missed.get(label);
      if(m + c <= 0) {
         return 1.0;
      }
      return c / (c + m);
   }

   private Set<Tuple3<Integer, Integer, String>> tags(Sequence<?> sequence) {
      Set<Tuple3<Integer, Integer, String>> tags = new HashSet<>();
      for(int i = 0; i < sequence.size(); ) {
         String lbl = sequence.get(i).asVariable().getName();
         if(lbl.equals("O")) {
            i++;
         } else {
            String tag = lbl.substring(2);
            int start = i;
            i++;
            while(i < sequence.size() && sequence.get(i).asVariable().getName().startsWith("I-")) {
               i++;
            }
            tags.add(Tuple3.of(start, i, tag));
         }
      }
      return tags;
   }

}// END OF CoNLLEvaluation
