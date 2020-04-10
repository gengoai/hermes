package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.FeatureExtractor;
import com.gengoai.apollo.ml.sequence.SequenceLabeler;
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
   protected final FeatureExtractor<HString> featurizer;
   @Getter
   protected final SequenceLabeler labeler;
   @Getter
   protected final String version;

   protected SequenceTagger(FeatureExtractor<HString> featurizer,
                            SequenceLabeler labeler,
                            String version) {
      this.featurizer = featurizer;
      this.labeler = labeler;
      this.version = version;
   }

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
