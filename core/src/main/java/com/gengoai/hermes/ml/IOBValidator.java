package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.model.sequence.SequenceValidator;

/**
 * <p>Sequence validator ensuring correct IOB tag output</p>
 *
 * @author David B. Bracewell
 */
public class IOBValidator implements SequenceValidator {
   private static final long serialVersionUID = 1L;

   @Override
   public boolean isValid(String label, String previousLabel, Observation instance) {
      if(label.startsWith("I-")) {
         if(previousLabel == null) {
            return false;
         }
         if(previousLabel.startsWith("O")) {
            return false;
         }
         if(previousLabel.startsWith("I-") && !label.equals(previousLabel)) {
            return false;
         }
         return !previousLabel.startsWith("B-") || label.substring(2).equals(previousLabel.substring(2));
      }
      return true;
   }
}// END OF IOBValidator
