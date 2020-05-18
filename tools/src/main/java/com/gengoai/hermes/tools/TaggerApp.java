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

import com.gengoai.Stopwatch;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.evaluation.ClassifierEvaluation;
import com.gengoai.apollo.ml.evaluation.Evaluation;
import com.gengoai.apollo.ml.evaluation.SequenceLabelerEvaluation;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.ModelIO;
import com.gengoai.application.Option;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.en.ENPOSTagger;
import com.gengoai.hermes.ml.EntityTagger;
import com.gengoai.hermes.ml.HStringMLModel;
import com.gengoai.hermes.ml.NERTensorFlowModel;
import com.gengoai.hermes.ml.PhraseChunkTagger;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import com.gengoai.parsing.ParseException;
import com.gengoai.string.Strings;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

import static com.gengoai.LogUtils.logInfo;
import static com.gengoai.Validation.checkState;
import static com.gengoai.Validation.notNullOrBlank;
import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

@Log
public class TaggerApp extends HermesCLI {
   private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

   private static final Map<String, HStringMLModel> NAMED_TRAINERS =
         Collections.unmodifiableMap(hashMapOf($("PHRASE_CHUNK", new PhraseChunkTagger()),
                                               $("ENTITY", new EntityTagger()),
                                               $("TF_ENTITY", new NERTensorFlowModel()),
                                               $("EN_POS", new ENPOSTagger())));

   @Option(description = "The specification or location the corpus or document collection to process.",
         name = "docFormat",
         aliases = {"df"})
   private String documentCollectionSpec;
   @Option(description = "The name or class of the sequence tagger to train.",
         aliases = {"tagger"},
         required = true)
   private String sequenceTagger;
   @Option(description = "Location to save model",
         aliases = {"m"})
   private String model;
   @Option(description = "Print a Confusion Matrix",
         defaultValue = "false")
   private boolean printCM;
   @Option(description = "Query to generate data",
         defaultValue = "")
   private String query;

   public static void main(String[] args) {
      new TaggerApp().run(args);
   }

   private DocumentCollection getDocumentCollection() {
      DocumentCollection docs = DocumentCollection.create(notNullOrBlank(documentCollectionSpec,
                                                                         "No Document Collection Specified!"));
      if(Strings.isNotNullOrBlank(query)) {
         try {
            docs = docs.query(query);
         } catch(ParseException e) {
            throw new RuntimeException(e);
         }
      }
      return docs;
   }

   private HStringMLModel getTrainer() {
      if(NAMED_TRAINERS.containsKey(sequenceTagger.toUpperCase())) {
         return NAMED_TRAINERS.get(sequenceTagger.toUpperCase());
      }
      try {
         return Converter.convert(sequenceTagger, HStringMLModel.class);
      } catch(TypeConversionException e) {
         throw new RuntimeException(e);
      }
   }

   private void logFitParameters(FitParameters<?> parameters) {
      logInfo(log, "========================================================");
      logInfo(log, "                 FitParameters");
      logInfo(log, "========================================================");
      for(String name : parameters.parameterNames()) {
         logInfo(log,
                 "{0} ({1}), value={2}",
                 name,
                 parameters.getParam(name).type.getSimpleName(),
                 parameters.get(name));
      }
      logInfo(log, "========================================================");
   }

   @Override
   protected void programLogic() throws Exception {
      checkState(getPositionalArgs().length > 0, "No Mode specified!");
      switch(getPositionalArgs()[0].toUpperCase()) {
         case "TRAIN":
            checkState(Strings.isNotNullOrBlank(model), "No Model Specified!");
            ModelIO.save(train(getDocumentCollection()), Resources.from(model));
            break;
         case "TEST":
            checkState(Strings.isNotNullOrBlank(model), "No Model Specified!");
            test(getDocumentCollection(), Cast.as(ModelIO.load(Resources.from(model))));
            break;
         case "PARAMETERS":
            logFitParameters(getTrainer().getFitParameters());
            break;
         case "LS":
         case "LIST":
            NAMED_TRAINERS.forEach((name, trainer) -> logInfo(log, "{0}   {1}", name, trainer));
            break;
      }
   }

   private void test(DocumentCollection testingCollection, HStringMLModel tagger) {
      logInfo(log, "========================================================");
      logInfo(log, "                         TEST");
      logInfo(log, "========================================================");
      logInfo(log, "   Data: {0}", documentCollectionSpec);
      logInfo(log, " Tagger: {0}", model);
      logInfo(log, "========================================================");
      logInfo(log, "Loading data set");
      DataSet testingData = tagger.transform(testingCollection);
      Evaluation evaluation = tagger.getEvaluator();
      Stopwatch stopwatch = Stopwatch.createStarted();
      logInfo(log, "Testing Started at {0}", LocalDateTime.now().format(TIME_FORMATTER));
      evaluation.evaluate(tagger.delegate(), testingData);
      stopwatch.stop();
      logInfo(log, "Testing Stopped at {0} ({1})",
              LocalDateTime.now().format(TIME_FORMATTER),
              stopwatch);

      Resource stdOut = new StringResource();
      try(OutputStream os = stdOut.outputStream();
          PrintStream printStream = new PrintStream(os)) {
         if(evaluation instanceof SequenceLabelerEvaluation) {
            ((SequenceLabelerEvaluation) evaluation).output(printStream, printCM);
         } else if(evaluation instanceof ClassifierEvaluation) {
            ((ClassifierEvaluation) evaluation).output(printStream, printCM);
         } else {
            evaluation.output(printStream);
         }
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      try {
         logInfo(log, "\n{0}", stdOut.readToString());
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   private HStringMLModel train(DocumentCollection trainingData) {
      HStringMLModel trainer = getTrainer();

      //--Fill in parameters based on command line settings
      FitParameters<?> parameters = trainer.getFitParameters();
      for(String parameterName : parameters.parameterNames()) {
         String confName = "param." + parameterName;
         if(Config.hasProperty(confName)) {
            parameters.set(parameterName, Config.get(confName));
         }
      }
      logInfo(log, "========================================================");
      logInfo(log, "                         Train");
      logInfo(log, "========================================================");
      logInfo(log, "   Data: {0}", documentCollectionSpec);
      if(Strings.isNotNullOrBlank(query)) {
         logInfo(log, "  Query: {0}", query);
      }
      logInfo(log, "Trainer: {0}", sequenceTagger);
      logInfo(log, "  Model: {0}", model);
      logInfo(log, "========================================================");
      logFitParameters(parameters);
      Stopwatch stopwatch = Stopwatch.createStarted();
      logInfo(log,
              "Training Started at {0}",
              LocalDateTime.now().format(TIME_FORMATTER));
      trainer.estimate(trainingData);
      stopwatch.stop();
      logInfo(log, "Training Stopped at {0} ({1})",
              LocalDateTime.now().format(TIME_FORMATTER),
              stopwatch);
      return trainer;
   }
}//END OF TaggerApp
