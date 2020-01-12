package com.gengoai.hermes.ml;

import com.gengoai.hermes.HString;
import com.gengoai.io.resource.Resource;

import java.io.Serializable;

/**
 * Defines a methodology for performing a classification on an HString.
 *
 * @author David B. Bracewell
 */
public interface TextClassifier extends Serializable {

   /**
    * Reads a serialized TextClassifier from a resource
    *
    * @param <T>      the TextClassifier implementation parameter
    * @param resource the resource containing the serialized classifier
    * @return the deserialized TextClassifier
    * @throws Exception Something went wrong reading the classifier
    */
   static <T extends TextClassifier> T read(Resource resource) throws Exception {
      return resource.readObject();
   }

   /**
    * Classifies the text
    *
    * @param text the text to classify
    */
   void classify(HString text);


   /**
    * Serializes the TextClassifier to the given resource
    *
    * @param resource the resource to serialize the classifier to
    * @throws Exception Something went wrong serializing the classifier
    */
   default void write(Resource resource) throws Exception {
      resource.setIsCompressed(true).writeObject(this);
   }

}// END OF TextClassifier
