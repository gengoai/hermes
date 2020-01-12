package com.gengoai.hermes.lexicon;

import com.gengoai.Tag;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.string.Strings;
import lombok.Value;

import java.io.Serializable;

/**
 * An entry in a lexicon defining the lemma, probability, tag, and any constraints on matching
 *
 * @author David B. Bracewell
 */
@Value
public class LexiconEntry<T extends Tag> implements Serializable, Comparable<LexiconEntry<T>> {
   private static final long serialVersionUID = 1L;
   private final LyreExpression constraint;
   private final String lemma;
   private final double probability;
   private final T tag;
   private final int tokenLength;

   /**
    * Instantiates a new LexiconEntry.
    *
    * @param lemma       the lemma
    * @param probability the probability
    * @param tag         the tag
    * @param constraint  the constraint
    */
   public LexiconEntry(String lemma, double probability, T tag, LyreExpression constraint) {
      this(lemma, probability, tag, constraint, Strings.nullToEmpty(lemma).split("\\s+").length);
   }

   /**
    * Instantiates a new LexiconEntry.
    *
    * @param lemma       the lemma
    * @param probability the probability
    * @param tag         the tag
    * @param constraint  the constraint
    */
   public LexiconEntry(String lemma, double probability, T tag, LyreExpression constraint, int tokenLength) {
      this.lemma = lemma;
      this.constraint = constraint;
      this.probability = probability;
      this.tag = tag;
      this.tokenLength = tokenLength == 0 ? Strings.nullToEmpty(lemma).split("\\s+").length : tokenLength;
   }

   @Override
   public int compareTo(LexiconEntry<T> o) {
      int d = Double.compare(getProbability(), o.getProbability());
      if (d == 0) {
         d = Double.compare(getLemma().length(), o.getLemma().length());
      }
      return -d;
   }


}// END OF LexiconEntry
