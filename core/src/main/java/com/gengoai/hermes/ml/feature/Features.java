package com.gengoai.hermes.ml.feature;

import com.gengoai.Language;
import com.gengoai.hermes.BasicCategories;
import com.gengoai.hermes.HString;
import com.gengoai.string.StringMatcher;
import com.gengoai.string.Strings;

import static com.gengoai.hermes.ml.feature.PredefinedFeatures.*;

/**
 * The type Features.
 *
 * @author David B. Bracewell
 */
public final class Features {

   /**
    * The constant IsAllCaps.
    */
   public static final PredefinedFeaturizer IsAllCaps = predefinedPredicateFeature("IsAllCaps",
                                                                                   Strings::isUpperCase);
   /**
    * The constant IsAlphaNumeric.
    */
   public static final PredefinedFeaturizer IsAlphaNumeric = predefinedPredicateFeature("IsAlphaNumeric",
                                                                                        Strings::isAlphaNumeric);
   public static final PredefinedFeaturizer IsBeginOfSentence = predefinedPredicateFeature("IsBeginOfSentence",
                                                                                           h -> h.start() == h.sentence()
                                                                                                              .start()
                                                                                          );
   /**
    * The constant IsDigit.
    */
   public static final PredefinedFeaturizer IsDigit = predefinedPredicateFeature("IsDigit", Strings::isDigit);
   /**
    * The constant IsHuman.
    */
   public static final PredefinedFeaturizer IsHuman = predefinedPredicateFeature("IsHuman",
                                                                                 h -> h.isA(BasicCategories.HUMAN));
   /**
    * The constant IsInitialCapital.
    */
   public static final PredefinedFeaturizer IsInitialCapital = predefinedPredicateFeature("IsInitialCapital",
                                                                                          h -> Character.isUpperCase(
                                                                                                h.charAt(0)));
   /**
    * The constant IsLanguageName.
    */
   public static final PredefinedFeaturizer IsLanguageName = predefinedValueFeature("IsLanguageName",
                                                                                    h -> {
                                                                                       Language language = Language.fromString(
                                                                                             h.toString());
                                                                                       if(language != Language.UNKNOWN) {
                                                                                          return language.getCode();
                                                                                       }
                                                                                       return null;
                                                                                    });
   public static final PredefinedFeaturizer IsMonth = predefinedPredicateFeature("isMonth",
                                                                                 h -> h.isA(BasicCategories.MONTHS));
   /**
    * The constant isOrganization.
    */
   public static final PredefinedFeaturizer IsOrganization = predefinedPredicateFeature("isOrganization",
                                                                                        h -> h.isA(BasicCategories.ORGANIZATIONS));
   /**
    * The constant IsPlace.
    */
   public static final PredefinedFeaturizer IsPlace = predefinedPredicateFeature("IsPlace",
                                                                                 h -> h.isA(BasicCategories.PLACES));
   /**
    * The constant IsPunctuation.
    */
   public static final PredefinedFeaturizer IsPunctuation = predefinedPredicateFeature("IsPunctuation",
                                                                                       Strings::isPunctuation);
   public static final PredefinedFeaturizer IsStateOrPrefecture = predefinedPredicateFeature("IsStateOrPrefecture",
                                                                                             h -> h.isA(BasicCategories.STATES_OR_PREFECTURES));
   /**
    * The constant isTime.
    */
   public static final PredefinedFeaturizer IsTime = predefinedPredicateFeature("isTime",
                                                                                h -> h.isA(BasicCategories.TIME));
   /**
    * The constant Lemma.
    */
   public static final PredefinedFeaturizer Lemma = predefinedValueFeature("Lemma", HString::getLemma);
   /**
    * The constant LowerCaseWord.
    */
   public static final PredefinedFeaturizer LowerCaseWord = predefinedValueFeature("LowerWord", HString::toLowerCase);
   /**
    * The constant PartOfSpeech.
    */
   public static final PredefinedFeaturizer PartOfSpeech = predefinedValueFeature("POS",
                                                                                  hString -> hString.pos() == null
                                                                                             ? null
                                                                                             : hString.pos()
                                                                                                      .tag());
   /**
    * The constant Word.
    */
   public static final PredefinedFeaturizer Word = predefinedValueFeature("Word", HString::toString);
   /**
    * The constant WordAndClass.
    */
   public static final PredefinedFeaturizer WordAndClass = predefinedValueFeature("WordAndClass",
                                                                                  string -> string.toString() +
                                                                                        "," +
                                                                                        WordClassFeaturizer.INSTANCE
                                                                                              .applyAsFeatures(
                                                                                                    string)
                                                                                              .get(0)
                                                                                              .getSuffix());
   /**
    * The constant hasCapital.
    */
   public static final PredefinedFeaturizer hasCapital = predefinedPredicateFeature("HasCapital",
                                                                                    StringMatcher.HasUpperCase);

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
