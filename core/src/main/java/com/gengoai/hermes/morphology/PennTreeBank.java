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

import com.gengoai.annotation.Preload;

import static com.gengoai.hermes.morphology.UniversalFeature.*;
import static com.gengoai.hermes.morphology.UniversalFeatureValue.*;
import static com.gengoai.tuple.Tuples.$;
import static com.gengoai.hermes.morphology.PartOfSpeech.*;

/**
 * Part-of-speech tags defined by Penn Treebank
 */
@Preload
public final class PennTreeBank {
   //--------------------------------------------------------------------------------------------------------------
   // Phrase Tags
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech NP = create("NP", "NP", NOUN, true);
   public static final PartOfSpeech PP = create("PP", "PP", ADPOSITION, true);
   public static final PartOfSpeech VP = create("VP", "VP", VERB, true);
   public static final PartOfSpeech ADVP = create("ADVP", "ADVP", ADVERB, true);
   public static final PartOfSpeech ADJP = create("ADJP", "ADJP", ADJECTIVE, true);
   public static final PartOfSpeech SBAR = create("SBAR", "SBAR", SCONJ, true);
   public static final PartOfSpeech PRT = create("PRT", "PRT", PARTICLE, true);
   public static final PartOfSpeech INTJ = create("INTJ", "INTJ", INTERJECTION, true);

   //--------------------------------------------------------------------------------------------------------------
   // Constituency Parse Tags
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech QP = create("QP", "QP", NOUN, true);
   public static final PartOfSpeech WHADJP = create("WHADJP", "WHADJP", ADJECTIVE, true);
   public static final PartOfSpeech WHADVP = create("WHADVP", "WHADVP", ADVERB, true);
   public static final PartOfSpeech WHNP = create("WHNP", "WHNP", NOUN, true);
   public static final PartOfSpeech WHPP = create("WHPP", "WHPP", ADPOSITION, true);
   public static final PartOfSpeech PRN = create("PRN", "PRN", OTHER, true);
   public static final PartOfSpeech FRAG = create("FRAG", "FRAG", OTHER, true);
   public static final PartOfSpeech NAC = create("NAC", "NAC", OTHER, true);
   public static final PartOfSpeech NX = create("NX", "NX", NOUN, true);
   public static final PartOfSpeech RRC = create("RRC", "RRC", CCONJ, true);
   public static final PartOfSpeech S = create("S", "S", OTHER, true);
   public static final PartOfSpeech LST = create("LST", "LST", OTHER, true);
   public static final PartOfSpeech UCP = create("UCP", "UCP", CCONJ, true);
   public static final PartOfSpeech CONJP = create("CONJP", "CONJP", CCONJ, true);
   public static final PartOfSpeech SBARQ = create("SBARQ", "SBARQ", SCONJ, true);
   public static final PartOfSpeech SINV = create("SINV", "SINV", OTHER, true);
   public static final PartOfSpeech SQ = create("SQ", "SQ", OTHER, true);

   //--------------------------------------------------------------------------------------------------------------
   // Symbols
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech SYM = SYMBOL;
   public static final PartOfSpeech HASH = create("HASH", "#", SYMBOL, false);
   public static final PartOfSpeech DOLLAR = create("DOLLAR", "$", SYMBOL, false);


   //--------------------------------------------------------------------------------------------------------------
   // Punctuation
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech QUOTE = create("QUOTE", "\"", PUNCTUATION, false,
                                                   $(PunctType, Quot));
   public static final PartOfSpeech COLON = create("COLON", ":", PUNCTUATION, false,
                                                   $(PunctType, Colo));
   public static final PartOfSpeech COMMA = create("COMMA", ",", PUNCTUATION, false,
                                                   $(PunctType, Comm));
   public static final PartOfSpeech LCB = create("LCB", "-LCB-", PUNCTUATION, false,
                                                 $(PunctType, Brck));
   public static final PartOfSpeech LRB = create("LRB", "-LRB-", PUNCTUATION, false,
                                                 $(PunctType, Brck));
   public static final PartOfSpeech LSB = create("LSB", "-LSB-", PUNCTUATION, false,
                                                 $(PunctType, Brck));
   public static final PartOfSpeech RCB = create("RCB", "-RCB-", PUNCTUATION, false,
                                                 $(PunctType, Brck));
   public static final PartOfSpeech RRB = create("RRB", "-RRB-", PUNCTUATION, false,
                                                 $(PunctType, Brck));
   public static final PartOfSpeech RSB = create("RSB", "-RSB-", PUNCTUATION, false,
                                                 $(PunctType, Brck));
   public static final PartOfSpeech PERIOD = create("PERIOD", ".", PUNCTUATION, false,
                                                    $(PunctType, Peri));
   public static final PartOfSpeech HYPH = create("HYPH", "-", PUNCTUATION, false,
                                                  $(PunctType, Dash));

   //--------------------------------------------------------------------------------------------------------------
   // Adjectives
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech AFX = create("AFX", "AFX", ADJECTIVE, false,
                                                 $(Hyph, Yes));
   public static final PartOfSpeech JJ = create("JJ", "JJ", ADJECTIVE, false,
                                                $(Degree, Pos)); // Adjective
   public static final PartOfSpeech JJR = create("JJR", "JJR", ADJECTIVE, false,
                                                 $(Degree, Cmp)); //Adjective, comparative
   public static final PartOfSpeech JJS = create("JJS", "JJS", ADJECTIVE, false,
                                                 $(Degree, Sup)); //Adjective, superlative

   //--------------------------------------------------------------------------------------------------------------
   // NOUNS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech NN = create("NN", "NN", NOUN, false,
                                                $(Number, Sing));
   public static final PartOfSpeech NNP = create("NNP", "NNP", PROPER_NOUN, false,
                                                 $(Number, Sing)); //Proper noun, singular
   public static final PartOfSpeech NNPS = create("NNPS", "NNPS", PROPER_NOUN, false,
                                                  $(Number, Plur)); //Proper noun, plural
   public static final PartOfSpeech NNS = create("NNS", "NNS", NOUN, false,
                                                 $(Number, Plur));


   //--------------------------------------------------------------------------------------------------------------
   // NUMERALS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech CD = create("CD", "CD", NUMERAL, false,
                                                $(NumType, Card));


   //--------------------------------------------------------------------------------------------------------------
   // CONJUNCTIONS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech CC = create("CC",
                                                "CC",
                                                CCONJ,
                                                false);

   //--------------------------------------------------------------------------------------------------------------
   // DETERMINERS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech DT = create("DT", "DT", DETERMINER, false);
   public static final PartOfSpeech PDT = create("PDT", "PDT", DETERMINER, false); //Predeterminer
   public static final PartOfSpeech WDT = create("WDT", "WDT", DETERMINER, false,
                                                 $(PronType, Ind),
                                                 $(PronType, Rel));//Wh-determiner
   public static final PartOfSpeech WP$ = create("WP$", "WP$", DETERMINER, false,
                                                 $(Poss, Yes),
                                                 $(PronType, Ind),
                                                 $(PronType, Rel)); //Possessive wh-pronoun
   public static final PartOfSpeech PRP$ = create("PRP$", "PRP$", DETERMINER, false,
                                                  $(Poss, Yes),
                                                  $(PronType, Prs)); //Possessive pronoun


   //--------------------------------------------------------------------------------------------------------------
   // PRONOUNS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech EX = create("EX", "EX", PRONOUN, false,
                                                $(AdvType, Ex));
   public static final PartOfSpeech PRP = create("PRP", "PRP", PRONOUN, false,
                                                 $(PronType, Prs)); //Personal pronoun
   public static final PartOfSpeech WP = create("WP", "WP", PRONOUN, false,
                                                $(PronType, Ind),
                                                $(PronType, Rel)); //Wh-pronoun

   //--------------------------------------------------------------------------------------------------------------
   // ADPOSITION
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech IN = create("IN",
                                                "IN",
                                                ADPOSITION,
                                                false); //Preposition or subordinating conjunction

   //--------------------------------------------------------------------------------------------------------------
   // VERBS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech MD = create("MD", "MD", VERB, false,
                                                $(VerbType, Mod));
   public static final PartOfSpeech VB = create("VB", "VB", VERB, false,
                                                $(VerbForm, Inf));
   public static final PartOfSpeech VBD = create("VBD", "VBD", VERB, false,
                                                 $(VerbForm, Fin),
                                                 $(Tense, Past));
   public static final PartOfSpeech VBG = create("VBG", "VBG", VERB, false,
                                                 $(Aspect, Prog),
                                                 $(VerbForm, Part),
                                                 $(Tense, Pres));
   public static final PartOfSpeech VBN = create("VBN", "VBN", VERB, false,
                                                 $(Aspect, Perf),
                                                 $(VerbForm, Part),
                                                 $(Tense, Past));
   public static final PartOfSpeech VBP = create("VBP", "VBP", VERB, false,
                                                 $(VerbForm, Fin),
                                                 $(Tense, Pres));
   public static final PartOfSpeech VBZ = create("VBZ", "VBZ", VERB, false,
                                                 $(Number, Sing),
                                                 $(Person, Third),
                                                 $(VerbForm, Fin),
                                                 $(Tense, Pres));




   //--------------------------------------------------------------------------------------------------------------
   // OTHER
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech FW = create("FW", "FW", OTHER, false,
                                                $(Foreign, Yes));
   public static final PartOfSpeech NIL = create("NIL", "NIL", OTHER, false);
   public static final PartOfSpeech LS = create("LS", "LS", OTHER, false,
                                                $(NumType, Ord));


   //--------------------------------------------------------------------------------------------------------------
   // PARTICLES
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech POS = create("POS", "POS", PARTICLE, false,
                                                 $(Poss, Yes));
   public static final PartOfSpeech RP = create("RP", "RP", PARTICLE, false,
                                                $(PartType, Vbp));
   public static final PartOfSpeech TO = create("TO", "TO", PARTICLE, false,
                                                $(PartType, Inf),
                                                $(VerbForm, Inf));

   //--------------------------------------------------------------------------------------------------------------
   // ADVERBS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech RB = create("RB", "RB", ADVERB, false,
                                                $(Degree, Pos));
   public static final PartOfSpeech RBR = create("RBR", "RBR", ADVERB, false,
                                                 $(Degree, Cmp));
   public static final PartOfSpeech RBS = create("RBS", "RBS", ADVERB, false,
                                                 $(Degree, Sup));
   public static final PartOfSpeech WRB = create("WRB", "WRB", ADVERB, false,
                                                 $(PronType, Ind),
                                                 $(PronType, Rel));

   //--------------------------------------------------------------------------------------------------------------
   // INTERJECTIONS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech UH = create("UH", "UH", INTERJECTION, false); //Interjection


   public static final PartOfSpeech ADD = create("ADD", "ADD", OTHER, false);
   public static final PartOfSpeech NFP = create("NFP", "NFP", PUNCTUATION, false);





}//END OF PennTreeBank
