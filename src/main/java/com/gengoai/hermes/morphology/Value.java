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

import lombok.Getter;

public enum Value {
   ADVERB_TYPE_AD_ADJECTIVE("Adadj"),
   ADVERB_TYPE_CAUSE("Cau"),
   ADVERB_TYPE_DEGREE("Deg"),
   ADVERB_TYPE_EXISTENTIAL("Ex"),
   ADVERB_TYPE_LOCATION("Loc"),
   ADVERB_TYPE_MANNER("Man"),
   ADVERB_TYPE_MOD("Mod"),
   ADVERB_TYPE_STATE("Sta"),
   ADVERB_TYPE_TIME("Tim"),
   ASPECT_HABITUAL("Hab"),
   ASPECT_IMPERFECT("Imp"),
   ASPECT_ITERATIVE("Iter"),
   ASPECT_PERFECT("Perf"),
   ASPECT_PROGRESSIVE("Prog"),
   ASPECT_PROSPECTIVE("Prosp"),
   DEGREE_ABSOLUTE_SUPERLATIVE("Abs"),
   DEGREE_COMPARATIVE("Cmp"),
   DEGREE_EQUATIVE("Equ"),
   DEGREE_POSITIVE("Pos"),
   DEGREE_SUPERLATIVE("Sup"),
   NO("No"),
   NUMBER_DUAL("Dual"),
   NUMBER_GREATER_PAUCAL("Grpa"),
   NUMBER_GREATER_PLURAL("Grpl"),
   NUMBER_INVERSE_COUNT("Count"),
   NUMBER_INVERSE_NUMBER("Inv"),
   NUMBER_INVERSE_PLURALE_COLLECTIVE("Coll"),
   NUMBER_INVERSE_PLURALE_TANTUM("Ptan"),
   NUMBER_PAUCAL("Pauc"),
   NUMBER_PLURAL("Plur"),
   NUMBER_SINGULAR("Sing"),
   NUMBER_TRIAL("Tri"),
   NUMBER_TYPE_CARDINAL("Card"),
   NUMBER_TYPE_DISTRIBUTIVE("Dist"),
   NUMBER_TYPE_FRACTION("Frac"),
   NUMBER_TYPE_MULTIPLICATIVE("Mult"),
   NUMBER_TYPE_ORDINAL("Ord"),
   NUMBER_TYPE_RANGE("Range"),
   NUMBER_TYPE_SETS("Sets"),
   PARTICLE_TYPE_EMPHASIS("Emp"),
   PARTICLE_TYPE_INFINITIVE("Inf"),
   PARTICLE_TYPE_MODAL("Mod"),
   PARTICLE_TYPE_RESPONSE("Res"),
   PARTICLE_TYPE_SEPARATED_VERB_PREFIX("Vbp"),
   PERSON_0("0"),
   PERSON_1("1"),
   PERSON_2("2"),
   PERSON_3("3"),
   PERSON_4("4"),
   PRONOUN_TYPE_ARTICLE("Art"),
   PRONOUN_TYPE_DEMONSTRATIVE("Dem"),
   PRONOUN_TYPE_EMPHATIC("Emp"),
   PRONOUN_TYPE_EXCLAMATIVE("Exc"),
   PRONOUN_TYPE_INDEFINITE("Ind"),
   PRONOUN_TYPE_INTERROGATIVE("Int"),
   PRONOUN_TYPE_NEGATIVE("Neg"),
   PRONOUN_TYPE_PERSONAL("Prs"),
   PRONOUN_TYPE_RECIPROCAL("Rcp"),
   PRONOUN_TYPE_RELATIVE("Rel"),
   PRONOUN_TYPE_TOTAL("Tot"),
   PUNCTUATION_TYPE_BRACKET("Brck"),
   PUNCTUATION_TYPE_COLON("Colo"),
   PUNCTUATION_TYPE_COMMA("Comm"),
   PUNCTUATION_TYPE_DASH("Dash"),
   PUNCTUATION_TYPE_EXCLAMATION("Excl"),
   PUNCTUATION_TYPE_PERIOD("Peri"),
   PUNCTUATION_TYPE_QUESTION("Qest"),
   PUNCTUATION_TYPE_QUOTE("Quot"),
   PUNCTUATION_TYPE_SEMICOLON("Semi"),
   PUNCTUATION_TYPE_SYMBOL("Symb"),
   TENSE_FUTURE("Fut"),
   TENSE_IMPERFECT("Imp"),
   TENSE_PAST("Past"),
   TENSE_PLUPERFECT("Pqp"),
   TENSE_PRESENT("Pres"),
   VERB_FORM_CONVERB("Conv"),
   VERB_FORM_FINITE("Fin"),
   VERB_FORM_GERUND("Ger"),
   VERB_FORM_GERUNDIVE_("Gdv"),
   VERB_FORM_INFINITIVE("Inf"),
   VERB_FORM_PARTICIPLE("Part"),
   VERB_FORM_SUPINE("Sup"),
   VERB_FORM_VNOUN("Vnoun"),
   VERB_TYPE_AUXILIARY("Aux"),
   VERB_TYPE_COPULA("Cop"),
   VERB_TYPE_LIGHT("Light"),
   VERB_TYPE_MODAL("Mod"),
   YES("Yes");

   @Getter
   private final String tag;

   Value(String tag) {
      this.tag = tag;
   }

}//END OF MorphologicalFeatureValue

