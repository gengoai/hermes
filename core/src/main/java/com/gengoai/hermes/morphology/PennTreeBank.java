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

import static com.gengoai.hermes.morphology.Feature.*;
import static com.gengoai.hermes.morphology.Value.*;
import static com.gengoai.tuple.Tuples.$;
import static com.gengoai.hermes.morphology.PartOfSpeech.*;

@Preload
public final class PennTreeBank {
   //--------------------------------------------------------------------------------------------------------------
   // Phrase Tags
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech VP = create("VP", "VP", VERB, true);
   public static final PartOfSpeech NP = create("NP", "NP", NOUN, true);
   public static final PartOfSpeech ADJP = create("ADJP", "ADJP", ADJECTIVE, true);
   public static final PartOfSpeech PP = create("PP", "PP", ADPOSITION, true);
   public static final PartOfSpeech ADVP = create("ADVP", "ADVP", ADVERB, true);
   public static final PartOfSpeech SBAR = create("SBAR", "SBAR", SCONJ, true);
   public static final PartOfSpeech CONJP = create("CONJP", "CONJP", CCONJ, true);
   public static final PartOfSpeech PRT = create("PRT", "PRT", PARTICLE, true);
   public static final PartOfSpeech INTJ = create("INTJ", "INTJ", INTERJECTION, true);
   public static final PartOfSpeech LST = create("LST", "LST", OTHER, true);
   public static final PartOfSpeech UCP = create("UCP", "UCP", CCONJ, true);

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
                                                   $(PunctType, PUNCTUATION_TYPE_QUOTE));
   public static final PartOfSpeech COLON = create("COLON", ":", PUNCTUATION, false,
                                                   $(PunctType, PUNCTUATION_TYPE_COLON));
   public static final PartOfSpeech COMMA = create("COMMA", ",", PUNCTUATION, false,
                                                   $(PunctType, PUNCTUATION_TYPE_COMMA));
   public static final PartOfSpeech LCB = create("LCB", "-LCB-", PUNCTUATION, false,
                                                 $(PunctType, PUNCTUATION_TYPE_BRACKET));
   public static final PartOfSpeech LRB = create("LRB", "-LRB-", PUNCTUATION, false,
                                                 $(PunctType, PUNCTUATION_TYPE_BRACKET));
   public static final PartOfSpeech LSB = create("LSB", "-LSB-", PUNCTUATION, false,
                                                 $(PunctType, PUNCTUATION_TYPE_BRACKET));
   public static final PartOfSpeech RCB = create("RCB", "-RCB-", PUNCTUATION, false,
                                                 $(PunctType, PUNCTUATION_TYPE_BRACKET));
   public static final PartOfSpeech RRB = create("RRB", "-RRB-", PUNCTUATION, false,
                                                 $(PunctType, PUNCTUATION_TYPE_BRACKET));
   public static final PartOfSpeech RSB = create("RSB", "-RSB-", PUNCTUATION, false,
                                                 $(PunctType, PUNCTUATION_TYPE_BRACKET));
   public static final PartOfSpeech PERIOD = create("PERIOD", ".", PUNCTUATION, false,
                                                    $(PunctType, PUNCTUATION_TYPE_PERIOD));
   public static final PartOfSpeech HYPH = create("HYPH", "-", PUNCTUATION, false,
                                                  $(PunctType, PUNCTUATION_TYPE_DASH));

   //--------------------------------------------------------------------------------------------------------------
   // Adjectives
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech AFX = create("AFX", "AFX", ADJECTIVE, false,
                                                 $(Hyphenated, YES));
   public static final PartOfSpeech JJ = create("JJ", "JJ", ADJECTIVE, false,
                                                $(Degree, DEGREE_POSITIVE)); // Adjective
   public static final PartOfSpeech JJR = create("JJR", "JJR", ADJECTIVE, false,
                                                 $(Degree, DEGREE_COMPARATIVE)); //Adjective, comparative
   public static final PartOfSpeech JJS = create("JJS", "JJS", ADJECTIVE, false,
                                                 $(Degree, DEGREE_SUPERLATIVE)); //Adjective, superlative

   //--------------------------------------------------------------------------------------------------------------
   // NOUNS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech NN = create("NN", "NN", NOUN, false,
                                                $(Number, NUMBER_SINGULAR));
   public static final PartOfSpeech NNP = create("NNP", "NNP", NOUN, false,
                                                 $(Number, NUMBER_SINGULAR)); //Proper noun, singular
   public static final PartOfSpeech NNPS = create("NNPS", "NNPS", NOUN, false,
                                                  $(Number, NUMBER_PLURAL)); //Proper noun, plural
   public static final PartOfSpeech NNS = create("NNS", "NNS", NOUN, false,
                                                 $(Number, NUMBER_PLURAL));


   //--------------------------------------------------------------------------------------------------------------
   // NUMERALS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech CD = create("CD", "CD", NUMERAL, false,
                                                $(NumType, NUMBER_TYPE_CARDINAL));


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
                                                 $(PronType, PRONOUN_TYPE_INDEFINITE),
                                                 $(PronType, PRONOUN_TYPE_RELATIVE));//Wh-determiner
   public static final PartOfSpeech WP$ = create("WP$", "WP$", DETERMINER, false,
                                                 $(Poss, YES),
                                                 $(PronType, PRONOUN_TYPE_INDEFINITE),
                                                 $(PronType, PRONOUN_TYPE_RELATIVE)); //Possessive wh-pronoun
   public static final PartOfSpeech PRP$ = create("PRP$", "PRP$", DETERMINER, false,
                                                  $(Poss, YES),
                                                  $(PronType, PRONOUN_TYPE_PERSONAL)); //Possessive pronoun


   //--------------------------------------------------------------------------------------------------------------
   // PRONOUNS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech EX = create("EX", "EX", PRONOUN, false,
                                                $(AdvType, ADVERB_TYPE_EXISTENTIAL));
   public static final PartOfSpeech PRP = create("PRP", "PRP", PRONOUN, false,
                                                 $(PronType, PRONOUN_TYPE_PERSONAL)); //Personal pronoun
   public static final PartOfSpeech WP = create("WP", "WP", PRONOUN, false,
                                                $(PronType, PRONOUN_TYPE_INDEFINITE),
                                                $(PronType, PRONOUN_TYPE_RELATIVE)); //Wh-pronoun

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
                                                $(VerbType, VERB_TYPE_MODAL));
   public static final PartOfSpeech VB = create("VB", "VB", VERB, false,
                                                $(VerbForm, VERB_FORM_INFINITIVE));
   public static final PartOfSpeech VBD = create("VBD", "VBD", VERB, false,
                                                 $(VerbForm, VERB_FORM_FINITE),
                                                 $(Tense, TENSE_PAST));
   public static final PartOfSpeech VBG = create("VBG", "VBG", VERB, false,
                                                 $(Aspect, ASPECT_PROGRESSIVE),
                                                 $(VerbForm, VERB_FORM_PARTICIPLE),
                                                 $(Tense, TENSE_PRESENT));
   public static final PartOfSpeech VBN = create("VBN", "VBN", VERB, false,
                                                 $(Aspect, ASPECT_PERFECT),
                                                 $(VerbForm, VERB_FORM_PARTICIPLE),
                                                 $(Tense, TENSE_PAST));
   public static final PartOfSpeech VBP = create("VBP", "VBP", VERB, false,
                                                 $(VerbForm, VERB_FORM_FINITE),
                                                 $(Tense, TENSE_PRESENT));
   public static final PartOfSpeech VBZ = create("VBZ", "VBZ", VERB, false,
                                                 $(Number, NUMBER_SINGULAR),
                                                 $(Person, PERSON_3),
                                                 $(VerbForm, VERB_FORM_FINITE),
                                                 $(Tense, TENSE_PRESENT));




   //--------------------------------------------------------------------------------------------------------------
   // OTHER
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech FW = create("FW", "FW", OTHER, false,
                                                $(Foreign, YES));
   public static final PartOfSpeech NIL = create("NIL", "NIL", OTHER, false);
   public static final PartOfSpeech LS = create("LS", "LS", OTHER, false,
                                                $(NumType, NUMBER_TYPE_ORDINAL));


   //--------------------------------------------------------------------------------------------------------------
   // PARTICLES
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech POS = create("POS", "POS", PARTICLE, false,
                                                 $(Poss, YES));
   public static final PartOfSpeech RP = create("RP", "RP", PARTICLE, false,
                                                $(PartType, PARTICLE_TYPE_SEPARATED_VERB_PREFIX));
   public static final PartOfSpeech TO = create("TO", "TO", PARTICLE, false,
                                                $(PartType, PARTICLE_TYPE_INFINITIVE),
                                                $(VerbForm, VERB_FORM_INFINITIVE));

   //--------------------------------------------------------------------------------------------------------------
   // ADVERBS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech RB = create("RB", "RB", ADVERB, false,
                                                $(Degree, DEGREE_POSITIVE));
   public static final PartOfSpeech RBR = create("RBR", "RBR", ADVERB, false,
                                                 $(Degree, DEGREE_COMPARATIVE));
   public static final PartOfSpeech RBS = create("RBS", "RBS", ADVERB, false,
                                                 $(Degree, DEGREE_SUPERLATIVE));
   public static final PartOfSpeech WRB = create("WRB", "WRB", ADVERB, false,
                                                 $(PronType, PRONOUN_TYPE_INDEFINITE),
                                                 $(PronType, PRONOUN_TYPE_RELATIVE));

   //--------------------------------------------------------------------------------------------------------------
   // INTERJECTIONS
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech UH = create("UH", "UH", INTERJECTION, false); //Interjection


   public static final PartOfSpeech ADD = create("ADD", "ADD", OTHER, false);
   public static final PartOfSpeech NFP = create("NFP", "NFP", PUNCTUATION, false);





}//END OF PennTreeBank
