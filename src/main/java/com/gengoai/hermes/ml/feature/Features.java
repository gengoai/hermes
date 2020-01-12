package com.gengoai.hermes.ml.feature;

import com.gengoai.Language;
import com.gengoai.apollo.ml.Featurizer;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.string.Strings;

import java.util.Collections;

import static com.gengoai.apollo.ml.Featurizer.multiValueFeaturizer;
import static com.gengoai.apollo.ml.Featurizer.valueFeaturizer;

/**
 * The type Features.
 *
 * @author David B. Bracewell
 */
public final class Features {

   public static final Featurizer<HString> Categories = multiValueFeaturizer("CATEGORY",
                                                                             h -> h.attribute(Types.CATEGORY,
                                                                                              Collections.emptySet()));
   /*
    *
    * The constant IsLanguageName.
    */
   public static final Featurizer<HString> IsLanguageName = valueFeaturizer("IsLanguageName",
                                                                            h -> {
                                                                               Language language = Language.fromString(
                                                                                  h.toPOSString());
                                                                               if (language != Language.UNKNOWN) {
                                                                                  return language.getCode();
                                                                               }
                                                                               return null;
                                                                            });
   /**
    * The constant Lemma.
    */
   public static final Featurizer<HString> Lemma = valueFeaturizer("LEMMA", HString::getLemma);
   /**
    * The constant LowerCaseWord.
    */
   public static final Featurizer<HString> LowerCaseWord = valueFeaturizer("WORD", HString::toLowerCase);
   /**
    * The constant PartOfSpeech.
    */
   public static final Featurizer<HString> PartOfSpeech = valueFeaturizer("POS",
                                                                          hString -> hString.pos() == null
                                                                                     ? null
                                                                                     : hString.pos()
                                                                                              .asString());
   /**
    * The constant Word.
    */
   public static final Featurizer<HString> Word = valueFeaturizer("WORD", HString::toString);
   /**
    * The constant WordAndClass.
    */
   public static final Featurizer<HString> WordAndClass = valueFeaturizer("WORD_AND_CLASS",
                                                                          string -> string.toString() +
                                                                             "," +
                                                                             WordClassFeaturizer.INSTANCE
                                                                                .applyAsFeatures(
                                                                                   string)
                                                                                .get(0)
                                                                                .getSuffix());

   private Features() {
      throw new IllegalArgumentException();
   }

   /**
    * Is digit boolean.
    *
    * @param word the word
    * @return the boolean
    */
   public static boolean isDigit(HString word) {
      String norm = word.toLowerCase();
      return Strings.isDigit(word) ||
         norm.equals("one") ||
         norm.equals("two") ||
         norm.equals("three") ||
         norm.equals("four") ||
         norm.equals("five") ||
         norm.equals("six") ||
         norm.equals("seven") ||
         norm.equals("eight") ||
         norm.equals("nine") ||
         norm.equals("ten") ||
         norm.equals("eleven") ||
         norm.equals("twelve") ||
         norm.equals("thirteen") ||
         norm.equals("fourteen") ||
         norm.equals("fifteen") ||
         norm.equals("sixteen") ||
         norm.equals("seventeen") ||
         norm.equals("eighteen") ||
         norm.equals("nineteen") ||
         norm.equals("twenty") ||
         norm.equals("thirty") ||
         norm.equals("forty") ||
         norm.equals("fifty") ||
         norm.equals("sixty") ||
         norm.equals("seventy") ||
         norm.equals("eighty") ||
         norm.equals("ninety") ||
         norm.equals("hundred") ||
         norm.equals("thousand") ||
         norm.equals("million") ||
         norm.equals("billion") ||
         norm.equals("trillion") ||
         Strings.isDigit(word.replaceAll("\\W+", ""));
   }


}//END OF Features
