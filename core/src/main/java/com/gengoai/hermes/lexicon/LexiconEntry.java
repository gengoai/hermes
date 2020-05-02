package com.gengoai.hermes.lexicon;

import com.gengoai.Language;
import com.gengoai.Validation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.string.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

/**
 * An entry in a lexicon defining the lemma, probability, tag, and any constraints on matching
 *
 * @author David B. Bracewell
 */
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class LexiconEntry implements Serializable, Comparable<LexiconEntry> {
   private static final long serialVersionUID = 1L;
   LyreExpression constraint;
   String lemma;
   double probability;
   String tag;
   int tokenLength;

   /**
    * Helper method for  calculating the length in tokens of a phrase for a given language.
    *
    * @param phrase   the phrase
    * @param language the language
    * @return the number of tokens in the phrase.
    */
   public static int calculateTokenLength(@NonNull String phrase, @NonNull Language language) {
      if(language.usesWhitespace()) {
         Document doc = Document.create(phrase, language);
         doc.annotate(Types.TOKEN);
         return doc.tokenLength();
      }
      return phrase.replaceAll("\\s+", "").length();
   }

   /**
    * Empty lexicon entry.
    *
    * @return the lexicon entry
    */
   public static LexiconEntry empty() {
      return new LexiconEntry(Strings.EMPTY, 0, null, null, 0);
   }

   /**
    * Constructs a LexiconEntry for the given lemma which has the given token length.
    *
    * @param lemma       the lemma
    * @param tokenLength the token length
    * @return the lexicon entry
    */
   public static LexiconEntry of(@NonNull String lemma, int tokenLength) {
      return new LexiconEntry(lemma, -1, null, null, tokenLength);
   }

   /**
    * Constructs a LexiconEntry for the given lemma which has the given probability, tag, and token length.
    *
    * @param lemma       the lemma
    * @param probability the probability
    * @param tag         the tag
    * @param tokenLength the token length
    * @return the lexicon entry
    */
   public static LexiconEntry of(@NonNull String lemma, double probability, String tag, int tokenLength) {
      return new LexiconEntry(lemma, probability, tag, null, tokenLength);
   }

   /**
    * Constructs a LexiconEntry for the given lemma which has the given probability, tag, constraint, and token length.
    *
    * @param lemma       the lemma
    * @param probability the probability
    * @param tag         the tag
    * @param constraint  the constraint
    * @param tokenLength the token length
    * @return the lexicon entry
    */
   public static LexiconEntry of(@NonNull String lemma,
                                 double probability,
                                 String tag,
                                 LyreExpression constraint,
                                 int tokenLength) {
      return new LexiconEntry(lemma, probability, tag, constraint, tokenLength);
   }

   /**
    * Constructs a LexiconEntry for the given lemma which has the given probability and token length.
    *
    * @param lemma       the lemma
    * @param probability the probability
    * @param tokenLength the token length
    * @return the lexicon entry
    */
   public static LexiconEntry of(@NonNull String lemma, double probability, int tokenLength) {
      return new LexiconEntry(lemma, probability, null, null, tokenLength);
   }

   /**
    * Constructs a LexiconEntry for the given lemma which has the given tag and token length.
    *
    * @param lemma       the lemma
    * @param tag         the tag
    * @param tokenLength the token length
    * @return the lexicon entry
    */
   public static LexiconEntry of(@NonNull String lemma, String tag, int tokenLength) {
      return new LexiconEntry(lemma, -1, tag, null, tokenLength);
   }

   private LexiconEntry(@NonNull String lemma,
                        double probability,
                        String tag,
                        LyreExpression constraint,
                        int tokenLength) {
      Validation.checkArgument(tokenLength >= 0,
                               "The lexicon token length must be greater than or equal to 0.");
      this.lemma = lemma;
      this.constraint = constraint;
      this.probability = probability;
      this.tag = tag;
      this.tokenLength = tokenLength;
   }

   @Override
   public int compareTo(LexiconEntry o) {
      int d = Double.compare(getProbability(), o.getProbability());
      if(d == 0) {
         d = Double.compare(getLemma().length(), o.getLemma().length());
      }
      return -d;
   }

}// END OF LexiconEntry
