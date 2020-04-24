package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.DataSetGenerator;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.collection.Iterables;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.io.resource.Resource;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;

/**
 * Processes the tokens in the sentence creating annotations (e.g. entities and phrase chunks) and/or assigning
 * attributes to the tokens (e.g. part-of-speech) or constructed annotations (e.g. entity type).
 *
 * @author David B. Bracewell
 */
public abstract class SequenceTagger implements Serializable {
   private static final long serialVersionUID = 1L;
   @Getter
   protected final DataSetGenerator<HString> inputGenerator;
   @Getter
   protected final Model labeler;
   @Getter
   protected final String version;
   @Getter
   protected final String outputName;

   /**
    * Read the tagger model.
    *
    * @param <T>      the tagger type
    * @param resource the resource containing  the saved model
    * @return the loaded model
    * @throws Exception something went wrong reading the model
    */
   public static <T extends SequenceTagger> T read(@NonNull Resource resource) throws Exception {
      return resource.readObject();
   }

   /**
    * Instantiates a new SequenceTagger.
    *
    * @param inputGenerator the generator to convert HString into input for the model
    * @param labeler        the model to use to perform the part-of-speech tagging
    * @param version        the version of the model to be used as part of the provider of the part-of-speech.
    */
   protected SequenceTagger(DataSetGenerator<HString> inputGenerator,
                            Model labeler,
                            String version) {
      this.inputGenerator = inputGenerator;
      this.labeler = labeler;
      this.version = version;
      this.outputName = Iterables.getFirst(labeler.getOutputs())
                                 .orElseThrow(() -> new IllegalArgumentException("Model has no outputs"));
   }

   /**
    * Processes the tokens in the sentence creating annotations (e.g. entities and phrase chunks) and/or assigning
    * attributes to the tokens (e.g. part-of-speech) or constructed annotations (e.g. entity type).
    *
    * @param sentence the sentence to tag
    */
   public abstract void tag(Annotation sentence);

   /**
    * Writes the model out to the given resource.
    *
    * @param resource the resource to write the model to
    * @throws Exception Something went wrong writing the model
    */
   public void write(@NonNull Resource resource) throws Exception {
      resource.setIsCompressed(true).writeObject(this);
   }

}// END OF SequenceTagger
