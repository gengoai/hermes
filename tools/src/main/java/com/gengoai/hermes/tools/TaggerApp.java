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
import com.gengoai.apollo.ml.FitParameters;
import com.gengoai.apollo.ml.data.ExampleDataset;
import com.gengoai.apollo.ml.sequence.PerInstanceEvaluation;
import com.gengoai.apollo.ml.sequence.SequenceLabelerEvaluation;
import com.gengoai.application.Option;
import com.gengoai.config.Config;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.hermes.application.HermesCLI;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.BIOEvaluation;
import com.gengoai.hermes.ml.BIOTagger;
import com.gengoai.hermes.ml.SequenceTagger;
import com.gengoai.hermes.ml.trainer.EnPOSTrainer;
import com.gengoai.hermes.ml.trainer.EntityTrainer;
import com.gengoai.hermes.ml.trainer.PhraseChunkTrainer;
import com.gengoai.hermes.ml.trainer.SequenceTaggerTrainer;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
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
   private static final Map<String, SequenceTaggerTrainer<?>> NAMED_TRAINERS =
         Collections.unmodifiableMap(hashMapOf(
               $("PHRASE_CHUNK", new PhraseChunkTrainer()),
               $("ENTITY", new EntityTrainer()),
               $("EN_POS", new EnPOSTrainer())
                                              ));

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

   public static void main(String[] args) {
      new TaggerApp().run(args);
   }

   private DocumentCollection getDocumentCollection() {
      Stopwatch sw = Stopwatch.createStarted();
      DocumentCollection dc = DocumentCollection.create(notNullOrBlank(documentCollectionSpec,
                                                                       "No Document Collection Specified!"));
      sw.stop();
      logInfo(log, "Loaded ''{0}'' in {1}", documentCollectionSpec, sw);
      return dc;
   }

   private SequenceTaggerTrainer<?> getTrainer() {
      if(NAMED_TRAINERS.containsKey(sequenceTagger.toUpperCase())) {
         return NAMED_TRAINERS.get(sequenceTagger.toUpperCase());
      }
      try {
         return Converter.convert(sequenceTagger, SequenceTaggerTrainer.class);
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
            train(getDocumentCollection()).write(Resources.from(model));
            break;
         case "TEST":
            checkState(Strings.isNotNullOrBlank(model), "No Model Specified!");
            test(getDocumentCollection(), SequenceTagger.read(Resources.from(model)));
            break;
         case "PARAMETERS":
            logFitParameters(getTrainer().getDefaultFitParameters());
            break;
         case "LS":
         case "LIST":
            NAMED_TRAINERS.forEach((name, trainer) -> logInfo(log, "{0}   {1}", name, trainer));
            break;
      }
   }

   private void test(DocumentCollection testingCollection, SequenceTagger tagger) {
      logInfo(log, "========================================================");
      logInfo(log, "                         TEST");
      logInfo(log, "========================================================");
      logInfo(log, "   Data: {0}", documentCollectionSpec);
      logInfo(log, " Tagger: {0}", model);
      logInfo(log, "========================================================");
      ExampleDataset testingData = getTrainer().createDataset(testingCollection);
      SequenceLabelerEvaluation evaluation;
      if(tagger instanceof BIOTagger) {
         evaluation = new BIOEvaluation();
      } else {
         evaluation = new PerInstanceEvaluation();
      }
      Stopwatch stopwatch = Stopwatch.createStarted();
      logInfo(log, "Testing Started at {0}",
              LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
      evaluation.evaluate(tagger.getLabeler(), testingData);
      stopwatch.stop();
      logInfo(log, "Testing Stopped at {0} ({1})",
              LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE),
              stopwatch);

      Resource stdOut = new StringResource();
      try(OutputStream os = stdOut.outputStream();
          PrintStream printStream = new PrintStream(os)) {
         evaluation.output(printStream, true);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      try {
         logInfo(log, "\n{0}", stdOut.readToString());
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   private SequenceTagger train(DocumentCollection trainingData) {
      SequenceTaggerTrainer<?> trainer = getTrainer();

      //--Fill in parameters based on command line settings
      FitParameters<?> parameters = trainer.getDefaultFitParameters();
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
      logInfo(log, "Trainer: {0}", sequenceTagger);
      logInfo(log, "Model: {0}", model);
      logInfo(log, "========================================================");
      logFitParameters(parameters);
      Stopwatch stopwatch = Stopwatch.createStarted();
      logInfo(log,
              "Training Started at {0}",
              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
      SequenceTagger tagger = trainer.fit(getDocumentCollection());
      stopwatch.stop();
      logInfo(log, "Training Stopped at {0} ({1})",
              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
              stopwatch);
      return tagger;
   }
}//END OF TaggerApp
