package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.FitParameters;
import com.gengoai.apollo.ml.Split;
import com.gengoai.apollo.ml.classification.Classification;
import com.gengoai.apollo.ml.classification.Classifier;
import com.gengoai.apollo.ml.classification.ClassifierEvaluation;
import com.gengoai.apollo.ml.data.Dataset;
import com.gengoai.apollo.ml.preprocess.PreprocessorList;
import com.gengoai.application.CommandLineParser;
import com.gengoai.application.NamedOption;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.util.Random;

import static com.gengoai.hermes.Hermes.HERMES_PACKAGE;

/**
 * The type Basic text classifier.
 *
 * @author David B. Bracewell
 */
public abstract class BasicTextClassifier implements TextClassifier {
   private static final long serialVersionUID = 1L;

   /**
    * The Classifier.
    */
   protected Classifier classifier;

   @Override
   public final void classify(@NonNull HString text) {
      text.document().annotate(required());
      onClassify(text, classifier.predict(getFeatureExtractor().extractExample(text)));
   }

   /**
    * Driver.
    *
    * @param args the args
    * @throws Exception the exception
    */
   public void cli(String[] args) throws Exception {
      CommandLineParser cli = new CommandLineParser();
      cli.addOption(NamedOption.builder()
                               .name("data")
                               .description("Data to use for training or testing")
                               .required(true)
                               .type(Resource.class)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("format")
                               .description("Format for reading data")
                               .defaultValue("JSON_OPL")
                               .type(String.class)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("mode")
                               .description("Train/Test/Split")
                               .defaultValue("TEST")
                               .type(Mode.class)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("model")
                               .description("Model to save/load")
                               .type(Resource.class)
                               .required(true)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("undersample")
                               .description("Undersample")
                               .type(Boolean.class)
                               .required(false)
                               .defaultValue(false)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("oversample")
                               .description("Oversample")
                               .type(Boolean.class)
                               .required(false)
                               .defaultValue(false)
                               .build());
      cli.parse(args);
      Config.loadPackageConfig(HERMES_PACKAGE);

      Dataset data = getDataset(cli.get("data"), cli.get("format"),
                                cli.get("undersample"),
                                cli.get("oversample"));

      Mode mode = cli.get("mode");

      if (mode == null) {
         throw new IllegalArgumentException("Invalid Mode");
      }

      switch (mode) {
         case TEST:
            try {
               this.classifier = Cast.<BasicTextClassifier>as(TextClassifier.read(cli.get("model"))).classifier;
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
            test(data);
            break;
         case SPLIT:
            Split split = data.split(0.8);
            train(split.train);
            test(split.test);
            break;
         case TRAIN:
            train(data);
            try {
               write(cli.get("model"));
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
            break;
         case CV3:
         case CV10:
            Split[] folds = data.fold(mode == Mode.CV3 ? 3 : 10);
            ClassifierEvaluation evaluation = null;
            for (Split fold : folds) {
               train(fold.train);
               ClassifierEvaluation current = test(fold.test, false);
               if (evaluation == null) {
                  evaluation = current;
               } else {
                  evaluation.merge(current);
               }
            }
            evaluation.output();
            break;
         default:
            System.err.println("Unknown option");
      }
   }

   /**
    * Gets dataset.
    *
    * @param data        the data
    * @param format      the format
    * @param undersample the undersample
    * @param oversample  the oversample
    * @return the dataset
    * @throws IOException the io exception
    */
   protected Dataset getDataset(Resource data,
                                String format,
                                boolean undersample,
                                boolean oversample) throws IOException {
      Corpus corpus = Corpus.reader(format)
                            .read(data);
      AnnotatableType[] required = required();
      if (required != null && required.length > 0) {
         corpus = corpus.annotate(required);
      }


      Corpus filtered = corpus;
      SerializablePredicate<HString> predicate = getTextFilter();
      if (predicate != null) {
         filtered = corpus.filter(getTextFilter());
      }
      Dataset dataset = filtered.asInstanceDataset(def -> {
         def.featureExtractor(getFeatureExtractor());
      });
      if (undersample) {
         dataset = dataset.undersample();
      }
      if (oversample) {
         dataset = dataset.oversample();
      }

      return dataset.shuffle(new Random(34));
   }

   /**
    * Gets featurizer.
    *
    * @return the featurizer
    */
   protected abstract FeatureExtractor<HString> getFeatureExtractor();

   /**
    * Gets learner.
    *
    * @return the learner
    */
   protected abstract Classifier createModel();

   /**
    * Gets oracle.
    *
    * @return the oracle
    */
   protected abstract SerializableFunction<HString, Object> getOracle();

   /**
    * Gets preprocessors.
    *
    * @return the preprocessors
    */
   protected PreprocessorList getPreprocessors() {
      return new PreprocessorList();
   }

   /**
    * Gets text filter.
    *
    * @return the text filter
    */
   protected SerializablePredicate<HString> getTextFilter() {
      return null;
   }

   /**
    * On classify.
    *
    * @param text           the text
    * @param classification the classification
    */
   protected abstract void onClassify(HString text, Classification classification);

   /**
    * Required annotatable type [ ].
    *
    * @return the annotatable type [ ]
    */
   protected abstract AnnotatableType[] required();

   /**
    * Test.
    *
    * @param dataset the dataset
    * @return the classifier evaluation
    */
   protected ClassifierEvaluation test(Dataset dataset) {
      return test(dataset, true);
   }

   /**
    * Test classifier evaluation.
    *
    * @param dataset the dataset
    * @param output  the output
    * @return the classifier evaluation
    */
   protected ClassifierEvaluation test(Dataset dataset, boolean output) {
      ClassifierEvaluation eval = ClassifierEvaluation.evaluateClassifier(classifier, dataset);
      if (output) {
         eval.output();
      }
      return eval;
   }

   /**
    * Update fit parameters fit parameters.
    *
    * @param fitParameters the fit parameters
    * @return the fit parameters
    */
   protected FitParameters updateFitParameters(FitParameters fitParameters) {
      return fitParameters;
   }

   /**
    * Train.
    *
    * @param dataset the dataset
    */
   protected void train(Dataset dataset) {
      classifier = createModel();
      classifier.fit(dataset, updateFitParameters(classifier.getFitParameters()));
   }


}// END OF BasicTextClassifier
