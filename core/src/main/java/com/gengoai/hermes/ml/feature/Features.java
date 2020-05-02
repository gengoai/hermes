package com.gengoai.hermes.ml.feature;

import com.gengoai.Language;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.BasicCategories;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.string.StringMatcher;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;

import java.util.Set;

import static com.gengoai.hermes.ml.feature.PredefinedFeatures.*;
import static com.gengoai.hermes.morphology.PartOfSpeech.PUNCTUATION;
import static com.gengoai.hermes.morphology.PartOfSpeech.SYMBOL;

/**
 * The type Features.
 *
 * @author David B. Bracewell
 */
public final class Features {
   private static final Set<String> currencySymbols = Set.of("〒",
                                                             "$",
                                                             "£",
                                                             "¥",
                                                             "৳",
                                                             "฿",
                                                             "៛",
                                                             "₡",
                                                             "₣",
                                                             "₤",
                                                             "₦",
                                                             "₩",
                                                             "₪",
                                                             "₫",
                                                             "€",
                                                             "₭",
                                                             "₮",
                                                             "₱",
                                                             "₲",
                                                             "₴",
                                                             "₵",
                                                             "₹",
                                                             "Af",
                                                             "B/.",
                                                             "Br",
                                                             "Bs",
                                                             "Bs.",
                                                             "C$",
                                                             "D",
                                                             "Db",
                                                             "din",
                                                             "Ft",
                                                             "ƒ",
                                                             "G",
                                                             "K",
                                                             "Kč",
                                                             "Kn",
                                                             "kr",
                                                             "Kr",
                                                             "Kz",
                                                             "L",
                                                             "Le",
                                                             "m",
                                                             "MK",
                                                             "MTn",
                                                             "Nfk",
                                                             "P",
                                                             "Q",
                                                             "R",
                                                             "R$",
                                                             "RM",
                                                             "Rp",
                                                             "Rs",
                                                             "₨",
                                                             "S/.",
                                                             "Sh",
                                                             "T",
                                                             "T$",
                                                             "UM",
                                                             "Vt",
                                                             "ZK",
                                                             "zł",
                                                             "ден",
                                                             "ЅМ",
                                                             "КМ",
                                                             "лв",
                                                             "ман",
                                                             "р.",
                                                             "ლ",
                                                             "Դ");
   public static final PredefinedFeaturizer DependencyRelation = predefinedValueFeature("DepRel",
                                                                                        h -> {
                                                                                           Tuple2<String, Annotation> t = h
                                                                                                 .dependency();
                                                                                           if(t.v2.isEmpty()) {
                                                                                              return null;
                                                                                           }
                                                                                           return t.v1 + "::" + t.v2.toLowerCase();
                                                                                        });
   /**
    * The constant Word.
    */
   public static final PredefinedFeaturizer Word = predefinedValueFeature("Word",
                                                                          HString::toString);

   /**
    * The constant hasCapital.
    */
   public static final PredefinedFeaturizer HasCapital = predefinedPredicateFeature("HasCapital",
                                                                                    StringMatcher.HasUpperCase);
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
   public static final PredefinedFeaturizer IsCardinalNumber = predefinedPredicateFeature("IsCardinalNumber",
                                                                                          LexicalFeatures::isCardinalNumber);
   public static final PredefinedFeaturizer IsCurrency = predefinedPredicateFeature("IsCurrency",
                                                                                    h -> {
                                                                                       return currencySymbols.contains(h.toString()) ||
                                                                                             h.contentEqualsIgnoreCase(
                                                                                                   "dollar") ||
                                                                                             h.contentEqualsIgnoreCase(
                                                                                                   "dollars");
                                                                                    });
   /**
    * The constant IsDigit.
    */
   public static final PredefinedFeaturizer IsDigit = predefinedPredicateFeature("IsDigit", LexicalFeatures::isDigit);
   public static final PredefinedFeaturizer IsEndOfSentence =
         predefinedPredicateFeature("IsEndOfSentence",
                                    h -> {
                                       Annotation lt = h.lastToken();
                                       Annotation next = lt.next();
                                       int send = lt.sentence().end();
                                       return lt.end() == send ||
                                             (next.end() == send && next.pos().isInstance(PUNCTUATION, SYMBOL));
                                    }
                                   );
   /**
    * The constant IsHuman.
    */
   public static final PredefinedFeaturizer IsHuman = predefinedPredicateFeature("IsHuman",
                                                                                 h -> h.isA(BasicCategories.HUMAN));
   /**
    * The constant IsLanguageName.
    */
   public static final PredefinedFeaturizer IsLanguageName = predefinedPredicateFeature("IsLanguageName",
                                                                                        h -> Language.fromString(h.toString()) != Language.UNKNOWN);
   public static final PredefinedFeaturizer IsMonth = predefinedPredicateFeature("isMonth",
                                                                                 h -> h.isA(BasicCategories.MONTHS));
   public static final PredefinedFeaturizer IsOrdinalNumber = predefinedPredicateFeature("IsOrdinalNumber",
                                                                                         LexicalFeatures::isOrdinalNumber);
   /**
    * The constant isOrganization.
    */
   public static final PredefinedFeaturizer IsOrganization = predefinedPredicateFeature("isOrganization",
                                                                                        h -> h.isA(BasicCategories.ORGANIZATIONS));
   public static final PredefinedFeaturizer IsPercent = predefinedPredicateFeature("IsPercent",
                                                                                   LexicalFeatures::isPercent);
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
    * The constant IsInitialCapital.
    */
   public static final PredefinedFeaturizer IsTitleCase = predefinedPredicateFeature("isTitleCase",
                                                                                     Strings::isTitleCase);
   /**
    * The constant Lemma.
    */
   public static final PredefinedFeaturizer Lemma = predefinedValueFeature("Lemma",
                                                                           HString::getLemma);
   /**
    * The constant LowerCaseWord.
    */
   public static final PredefinedFeaturizer LowerCaseWord = predefinedValueFeature("LowerWord",
                                                                                   HString::toLowerCase);
   /**
    * The constant PartOfSpeech.
    */
   public static final PredefinedFeaturizer PartOfSpeech = predefinedValueFeature("POS",
                                                                                  hString -> hString.pos().tag());
   public static final PredefinedFeaturizer PhraseChunkBIO = predefinedValueFeature("PhraseChunkBIO", h -> {
      Annotation pc = h.first(Types.PHRASE_CHUNK);
      if(pc.isEmpty()) {
         return "O";
      }
      return pc.start() == h.start()
             ? "B-" + pc.pos()
             : "I-" + pc.pos();
   });   public static final PredefinedFeaturizer WordClass = predefinedValueFeature("WordClass",
                                                                               LexicalFeatures::wordClass);
   public static final PredefinedFeaturizer WordShape = predefinedValueFeature("WordShape",
                                                                               LexicalFeatures::shape);
   public static final PredefinedFeaturizer punctuationType = predefinedValueFeature("PunctuationType",
                                                                                     Features::punctuationClass);

   private static String punctClassToName(int i) {
      switch(Character.getType(i)) {
         case Character.FINAL_QUOTE_PUNCTUATION:
            return "FINAL_QUOTE";
         case Character.INITIAL_QUOTE_PUNCTUATION:
            return "INITIAL_QUOTE";
         case Character.END_PUNCTUATION:
            return "END_PUNCTUATION";
         case Character.CONNECTOR_PUNCTUATION:
            return "CONNECTOR_PUNCTUATION";
         case Character.DASH_PUNCTUATION:
            return "DASH_PUNCTUATION";
         case Character.START_PUNCTUATION:
            return "START_PUNCTUATION";
         default:
            return "OTHER_PUNCTUATION";
      }
   }

   public static String punctuationClass(CharSequence string) {
      if(Strings.isPunctuation(string)) {
         int pclass = Character.getType(string.charAt(0));
         for(int i = 1; i < string.length(); i++) {
            int piclass = Character.getType(string.charAt(i));
            if(piclass != pclass) {
               return "MIXED";
            }
         }
         return punctClassToName(pclass);
      }
      return null;
   }

   private Features() {
      throw new IllegalArgumentException();
   }

}//END OF Features
