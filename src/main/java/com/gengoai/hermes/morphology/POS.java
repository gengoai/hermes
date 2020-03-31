/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.hermes.morphology;

import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.string.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration covering the parts-of-speech used in pos tagging, chunking, and parsing.
 *
 * @author David B. Bracewell
 */
public enum POS implements Tag {

   /**
    * The Any.
    */
   ANY(null, "UNKNOWN") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },

   /**
    * Universal Tag Set
    */
   VERB(ANY, "VERB") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Noun.
    */
   NOUN(ANY, "NOUN") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Pronoun.
    */
   PRONOUN(ANY, "PRON") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Adjective.
    */
   ADJECTIVE(ANY, "ADJ") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Adverb.
    */
   ADVERB(ANY, "ADV") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Adposition.
    */
   ADPOSITION(ANY, "ADP") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Conjunction.
    */
   CONJUNCTION(ANY, "CONJ") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Determiner.
    */
   DETERMINER(ANY, "DET") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Number.
    */
   NUMBER(ANY, "NUM") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Particle.
    */
   PARTICLE(ANY, "PART") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Other.
    */
   OTHER(ANY, "X") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },
   /**
    * The Punctuation.
    */
   PUNCTUATION(ANY, "PUNCT") {
      @Override
      public boolean isUniversal() {
         return true;
      }
   },

   /**
    * Phrase Tags
    */
   VP(VERB, "VP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Np.
    */
   NP(NOUN, "NP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Adjp.
    */
   ADJP(ADJECTIVE, "ADJP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Pp.
    */
   PP(ADPOSITION, "PP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Advp.
    */
   ADVP(ADVERB, "ADVP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Sbar.
    */
   SBAR(OTHER, "SBAR") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Conjp.
    */
   CONJP(CONJUNCTION, "CONJP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Prt.
    */
   PRT(PARTICLE, "PRT") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Intj.
    */
   INTJ(OTHER, "INTJ") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Lst.
    */
   LST(OTHER, "LST") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },
   /**
    * The Ucp.
    */
   UCP(OTHER, "UCP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * Penn Treebank Part of Speech Tags
    */
   CC(CONJUNCTION, "CC"), // Coordinating conjunction
   /**
    * Cd pos.
    */
   CD(NUMBER, "CD"), //Cardinal number
   /**
    * Dt pos.
    */
   DT(DETERMINER, "DT"), //Determiner
   /**
    * Ex pos.
    */
   EX(DETERMINER, "EX"), //Existential there
   /**
    * Fw pos.
    */
   FW(OTHER, "FW"), //Foreign word
   /**
    * In pos.
    */
   IN(ADPOSITION, "IN"), //Preposition or subordinating conjunction
   /**
    * Jj pos.
    */
   JJ(ADJECTIVE, "JJ"), // Adjective
   /**
    * Jjr pos.
    */
   JJR(ADJECTIVE, "JJR"), //Adjective, comparative
   /**
    * Jjs pos.
    */
   JJS(ADJECTIVE, "JJS"), //Adjective, superlative
   /**
    * Ls pos.
    */
   LS(OTHER, "LS"), //List item marker
   /**
    * Md pos.
    */
   MD(VERB, "MD"), //Modal
   /**
    * Nn pos.
    */
   NN(NOUN, "NN"), //Noun, singular or mass
   /**
    * Nns pos.
    */
   NNS(NOUN, "NNS"), //Noun, plural
   /**
    * Nnp pos.
    */
   NNP(NOUN, "NNP"), //Proper noun, singular
   /**
    * Nnps pos.
    */
   NNPS(NOUN, "NNPS"), //Proper noun, plural
   /**
    * Pdt pos.
    */
   PDT(DETERMINER, "PDT"), //Predeterminer
   /**
    * Pos pos.
    */
   POS(PARTICLE, "POS"), //Possessive ending
   /**
    * Prp pos.
    */
   PRP(PRONOUN, "PRP"), //Personal pronoun
   /**
    * Prp pos.
    */
   PRP$(PRONOUN, "PRP$"), //Possessive pronoun
   /**
    * Rb pos.
    */
   RB(ADVERB, "RB"), //Adverb
   /**
    * Rbr pos.
    */
   RBR(ADVERB, "RBR"), //Adverb, comparative
   /**
    * Rbs pos.
    */
   RBS(ADVERB, "RBS"), //Adverb, superlative
   /**
    * Rp pos.
    */
   RP(PARTICLE, "RP"), //Particle
   /**
    * Sym pos.
    */
   SYM(OTHER, "SYM"), //Symbol
   /**
    * To pos.
    */
   TO(PARTICLE, "TO"), //to
   /**
    * Uh pos.
    */
   UH(OTHER, "UH"), //Interjection
   /**
    * Vb pos.
    */
   VB(VERB, "VB"), //Verb, base form
   /**
    * Vbd pos.
    */
   VBD(VERB, "VBD"), //Verb, past tense
   /**
    * Vbg pos.
    */
   VBG(VERB, "VBG"),//Verb, gerund or present participle
   /**
    * Vbn pos.
    */
   VBN(VERB, "VBN"),//Verb, past participle
   /**
    * Vbp pos.
    */
   VBP(VERB, "VBP"),//Verb, non-3rd person singular present
   /**
    * Vbz pos.
    */
   VBZ(VERB, "VBZ"),//Verb, 3rd person singular present
   /**
    * Wdt pos.
    */
   WDT(DETERMINER, "WDT"),//Wh-determiner
   /**
    * Wp pos.
    */
   WP(PRONOUN, "WP"), //Wh-pronoun
   /**
    * Wp pos.
    */
   WP$(PRONOUN, "WP$"), //Possessive wh-pronoun
   /**
    * Wrb pos.
    */
   WRB(ADVERB, "WRB"), //Wh-adverb
   /**
    * Period pos.
    */
   PERIOD(PUNCTUATION, "."),
   /**
    * Hash pos.
    */
   HASH(PUNCTUATION, "#"),
   /**
    * Quote pos.
    */
   QUOTE(PUNCTUATION, "\""),
   /**
    * Dollar pos.
    */
   DOLLAR(PUNCTUATION, "$"),
   /**
    * Lrb pos.
    */
   LRB(PUNCTUATION, "-LRB-"),
   /**
    * Rrb pos.
    */
   RRB(PUNCTUATION, "-RRB-"),
   /**
    * Lcb pos.
    */
   LCB(PUNCTUATION, "-LCB-"),
   /**
    * Rcb pos.
    */
   RCB(PUNCTUATION, "-RCB-"),
   /**
    * Rsb pos.
    */
   RSB(PUNCTUATION, "-RSB-"),
   /**
    * Lsb pos.
    */
   LSB(PUNCTUATION, "-LSB-"),
   /**
    * Comma pos.
    */
   COMMA(PUNCTUATION, ","),
   /**
    * Colon pos.
    */
   COLON(PUNCTUATION, ":"),
   /**
    * Add pos.
    */
   ADD(OTHER, "ADD"),
   /**
    * Afx pos.
    */
   AFX(ADPOSITION, "AFX"),
   /**
    * Nfp pos.
    */
   NFP(PUNCTUATION, "NFP"),

   /**
    * Special Japanese Part of Speech Tags
    */
   ADN(ADJECTIVE, "ADN"),
   /**
    * Aux pos.
    */
   AUX(VERB, "AUX"),
   /**
    * Loc pos.
    */
   LOC(NOUN, "LOC"), // Noun Location
   /**
    * Org pos.
    */
   ORG(NOUN, "ORG"), //Noun Organization
   /**
    * Per pos.
    */
   PER(NOUN, "PER"), // Noun Person
   /**
    * Rpc pos.
    */
   RPC(PARTICLE, "RPC"), //Case Particle

   /**
    * Special CHINESE Part of Speech Tags
    */
   AD(ADVERB, "AD"),
   /**
    * As pos.
    */
   AS(PARTICLE, "AS"),
   /**
    * Ba pos.
    */
   BA(OTHER, "BA"),
   /**
    * Cs pos.
    */
   CS(CONJUNCTION, "CS"),
   /**
    * Dec pos.
    */
   DEC(PARTICLE, "DEC"),
   /**
    * Deg pos.
    */
   DEG(PARTICLE, "DEG"),
   /**
    * Der pos.
    */
   DER(PARTICLE, "DER"),
   /**
    * Dev pos.
    */
   DEV(PARTICLE, "DEV"),
   /**
    * Etc pos.
    */
   ETC(PARTICLE, "PRT"),
   /**
    * Ij pos.
    */
   IJ(OTHER, "IJ"),
   /**
    * Lb pos.
    */
   LB(OTHER, "LB"),
   /**
    * Lc pos.
    */
   LC(PARTICLE, "LC"),
   /**
    * M pos.
    */
   M(NUMBER, "M"),
   /**
    * Msp pos.
    */
   MSP(PARTICLE, "MSP"),
   /**
    * Nr pos.
    */
   NR(NOUN, "NR"),
   /**
    * Nt pos.
    */
   NT(NOUN, "NT"),
   /**
    * Od pos.
    */
   OD(NUMBER, "OD"),
   /**
    * On pos.
    */
   ON(OTHER, "ON"),
   /**
    * P pos.
    */
   P(ADPOSITION, "P"),
   /**
    * Pn pos.
    */
   PN(PRONOUN, "PN"),
   /**
    * Pu pos.
    */
   PU(PUNCTUATION, "PU"),
   /**
    * Sb pos.
    */
   SB(OTHER, "SB"),
   /**
    * Sp pos.
    */
   SP(PARTICLE, "SP"),
   /**
    * Va pos.
    */
   VA(VERB, "VA"),
   /**
    * Vc pos.
    */
   VC(VERB, "VC"),
   /**
    * Ve pos.
    */
   VE(VERB, "VE"),
   /**
    * Vv pos.
    */
   VV(VERB, "VV"),
   /**
    * X pos.
    */
   X(OTHER, "X"),

   /**
    * The Clp.
    */
   CLP(OTHER, "CLP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Cp.
    */
   CP(OTHER, "CP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Dnp.
    */
   DNP(OTHER, "DNP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Dp.
    */
   DP(DETERMINER, "DP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Dvp.
    */
   DVP(OTHER, "DVP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Frag.
    */
   FRAG(OTHER, "FRAG") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Ip.
    */
   IP(OTHER, "IP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Lcp.
    */
   LCP(OTHER, "LCP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Prn.
    */
   PRN(OTHER, "PRN") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   },

   /**
    * The Qp.
    */
   QP(OTHER, "QP") {
      @Override
      public boolean isPhraseTag() {
         return true;
      }
   };

   /**
    * The Index.
    */
   static Map<String, POS> index = new HashMap<>();

   static {
      for(POS pos : values()) {
         index.put(pos.asString().toUpperCase(), pos);
         index.put(pos.toString().toUpperCase(), pos);
      }
   }

   private final String tag;
   private final com.gengoai.hermes.morphology.POS parentType;

   POS(com.gengoai.hermes.morphology.POS parentType, String tag) {
      this.parentType = parentType;
      this.tag = tag;
   }

   /**
    * Determines the best fundamental POS (NOUN, VERB, ADJECTIVE, or ADVERB) for a text.
    *
    * @param text The text
    * @return The part of speech
    */
   public static com.gengoai.hermes.morphology.POS forText(HString text) {
      Validation.notNull(text);

      if(text.hasAttribute(Types.PART_OF_SPEECH)) {
         return text.attribute(Types.PART_OF_SPEECH);
      }
      if(text.tokenLength() == 1 && text.tokenAt(0).hasAttribute(Types.PART_OF_SPEECH)) {
         return text.tokenAt(0).attribute(Types.PART_OF_SPEECH);
      }

      com.gengoai.hermes.morphology.POS tag = ANY;
      for(Annotation token : text.tokens()) {
         Tag temp = token.attribute(Types.PART_OF_SPEECH);
         if(temp != null) {
            if(temp.isInstance(VERB)) {
               return VERB;
            } else if(temp.isInstance(NOUN)) {
               tag = NOUN;
            } else if(temp.isInstance(ADJECTIVE) && tag != NOUN) {
               tag = ADJECTIVE;
            } else if(temp.isInstance(ADVERB) && tag != NOUN) {
               tag = ADVERB;
            }
         }
      }

      return tag;
   }

   /**
    * Parses a String converting the tag (its Enum-String or treebank string) into the appropriate enum value
    *
    * @param tag The tag to parts
    * @return The <code>PartOfSpeech</code>
    */
   public static com.gengoai.hermes.morphology.POS fromString(String tag) {
      if(Strings.isNullOrBlank(tag)) {
         return null;
      } else if(index.containsKey(tag.toUpperCase())) {
         return index.get(tag.toUpperCase());
      } else if(tag.equals(";") || tag.equals("...") || tag.equals("-") || tag.equals("--")) {
         return COLON;
      } else if(tag.equals("?") || tag.equals("!")) {
         return PERIOD;
      } else if(tag.equals("``") || tag.equals("''") || tag.equals("\"\"") || tag.equals("'") || tag.equals("\"")) {
         return QUOTE;
      } else if(tag.equals("UH")) {
         return UH;
      } else if(tag.endsWith("{")) {
         return LCB;
      } else if(tag.endsWith("}")) {
         return RCB;
      } else if(tag.endsWith("[")) {
         return LSB;
      } else if(tag.endsWith("]")) {
         return RSB;
      } else if(tag.endsWith("(")) {
         return LRB;
      } else if(tag.endsWith(")")) {
         return RRB;
      } else if(!Strings.hasLetter(tag)) {
         return SYM;
      } else if(tag.equalsIgnoreCase("ANY")) {
         return ANY;
      }
      throw new IllegalArgumentException(tag + " is not a known PartOfSpeech");
   }

   /**
    * As string string.
    *
    * @return The treebank string representation
    */
   public String asString() {
      return tag;
   }

   /**
    * Gets parent type.
    *
    * @return The parent part of speech or itself if it is a top level pos
    */
   public com.gengoai.hermes.morphology.POS getParentType() {
      return parentType == null
             ? this
             : parentType;
   }

   /**
    * Gets universal tag.
    *
    * @return The universal tag
    */
   public com.gengoai.hermes.morphology.POS getUniversalTag() {
      if(this == ANY) {
         return ANY;
      }
      com.gengoai.hermes.morphology.POS tag = this;
      while(tag != null && tag.getParentType() != ANY && !tag.isUniversal()) {
         tag = tag.getParentType();
      }
      return tag;
   }

   /**
    * Is adjective boolean.
    *
    * @return True if the pos is an adjective form
    */
   public boolean isAdjective() {
      return getUniversalTag() == ADJECTIVE;
   }

   /**
    * Is adposition boolean.
    *
    * @return True if this is an adposition
    */
   public boolean isAdposition() {
      return getUniversalTag() == ADPOSITION;
   }

   /**
    * Is adverb boolean.
    *
    * @return True if the pos is an adverb form
    */
   public boolean isAdverb() {
      return getUniversalTag() == ADVERB;
   }

   /**
    * Is conjunction boolean.
    *
    * @return True if this is a conjunction
    */
   public boolean isConjunction() {
      return getUniversalTag() == CONJUNCTION;
   }

   /**
    * Is determiner boolean.
    *
    * @return True if this is a determiner
    */
   public boolean isDeterminer() {
      return getUniversalTag() == DETERMINER;
   }

   @Override
   public boolean isInstance(Tag tag) {
      if(tag == null) {
         return false;
      }
      if(tag == com.gengoai.hermes.morphology.POS.ANY) {
         return true;
      }
      if(tag instanceof com.gengoai.hermes.morphology.POS) {
         com.gengoai.hermes.morphology.POS other = (com.gengoai.hermes.morphology.POS) tag;
         com.gengoai.hermes.morphology.POS check = this;
         while(check != null) {
            if(check == other) {
               return true;
            }
            if(check == check.getParentType()) {
               return false;
            }
            check = check.getParentType();
         }
         return false;
      }
      return false;
   }

   /**
    * Is noun boolean.
    *
    * @return True if the pos is a noun form
    */
   public boolean isNoun() {
      return getUniversalTag() == NOUN;
   }

   /**
    * Is number boolean.
    *
    * @return True if this is a number
    */
   public boolean isNumber() {
      return getUniversalTag() == NUMBER;
   }

   /**
    * Is other boolean.
    *
    * @return True if this is an other
    */
   public boolean isOther() {
      return getUniversalTag() == OTHER;
   }

   /**
    * Is particle boolean.
    *
    * @return True if this is a particle
    */
   public boolean isParticle() {
      return getUniversalTag() == PARTICLE;
   }

   /**
    * Is phrase tag boolean.
    *
    * @return True if the tag is at the phrase level
    */
   public boolean isPhraseTag() {
      return false;
   }

   /**
    * Is pronoun boolean.
    *
    * @return True if the pos is a pronoun form
    */
   public boolean isPronoun() {
      return getUniversalTag() == PRONOUN;
   }

   /**
    * Is punctuation boolean.
    *
    * @return True if this is punctuation
    */
   public boolean isPunctuation() {
      return getUniversalTag() == PUNCTUATION;
   }

   /**
    * Is universal boolean.
    *
    * @return true if the tag is one of the universal tags
    */
   public boolean isUniversal() {
      return false;
   }

   /**
    * Is verb boolean.
    *
    * @return True if the pos is a verb form
    */
   public boolean isVerb() {
      return getUniversalTag() == VERB;
   }

   @Override
   public Tag parent() {
      return parentType;
   }

}// END OF PartOfSpeech
