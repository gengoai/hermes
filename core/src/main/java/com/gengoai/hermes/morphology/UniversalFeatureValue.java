/*
 * Licensed to the Apache Software Foundation  under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 ; you may not use this file except in compliance
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
import lombok.NonNull;

/**
 * Values associated with {@link UniversalFeature}s
 */
public enum UniversalFeatureValue implements Tag {
   /**
    * (Case) abessive<br/>
    **/
   Abe,
   /**
    * (Case) ablative<br/>
    **/
   Abl,
   /**
    * (Case) Absolutive<br/> (Degree) Absolute Superlative<br/>
    **/
   Abs,
   /**
    * (Case) accusative / oblique<br/>
    **/
   Acc,
   /**
    * (Voice) Active Voice <br/>
    */
   Act,
   /**
    * (AdvType) Ad-Adjective<br/>
    */
   Adadj,
   /**
    * (Case)  additive<br/>
    **/
   Add,
   /**
    * (Case) adessive<br/>
    **/
   Ade,
   /***
    * (Mood) Admirative<br/>
    */
   Adm,
   /**
    * (Case) allative<br/>
    **/
   All,
   /**
    * (Animacy) Animate<br/>
    */
   Anim,
   /**
    * (Voice)  Antipassive Voice<br/>
    */
   Antip,
   /**
    * (Style) Archaic, Obsolete<br/>
    */
   Arch,
   /**
    * (PronType) Article <br/>
    */
   Art,
   /**
    * (VerbType) Auxiliary Verb<br/>
    */
   Aux,
   /**
    * (Case) benefactive / destinative<br/>
    **/
   Ben,
   /**
    * (PunctType) bracket<br/>
    */
   Brck,
   /**
    * (NumType) Cardinal Number<br/>
    */
   Card,
   /**
    * (AdvType) Cause<br/> (Case) Causative / motivative / purposive<br/> (Voice) Causative Voice<br/>
    */
   Cau,
   /**
    * (AdpType) Circumposition<br/>
    */
   Circ,
   /**
    * (Case) comparative<br/> (Degree) Comparative, second degree<br/>
    **/
   Cmp,
   /***
    * (Mood) Conditional<br/>
    */
   Cnd,
   /**
    * (Case) considerative<br/>
    **/
   Cns,
   /**
    * (Number) Collective / Mass / Singulre Tantum<br/> (Style) colloquial<br/>
    */
   Coll,
   /**
    * (PunctType) colon<br/>
    */
   Colo,
   /**
    * (Case) comitative / associative<br/> (Definite) Complex<br/> (Gender) Common Gender<br/> (NameType) Company
    * Name<br/>
    **/
   Com,
   /**
    * (PunctType) comma<br/>
    */
   Comm,
   /**
    * (ConjType) Comparing conjunction<br/>
    */
   Comp,
   /**
    * (Definite) Construct State / Reduced Definiteness<br/>
    */
   Cons,
   /**
    * (VerbForm) Converb, Transgressive, Adverbial Participle, Verbal Adverb<br/>
    */
   Conv,
   /**
    * (VerbType) Copula Verb<br/>
    */
   Cop,
   /**
    * (Number) Count Plural<br/>
    */
   Count,
   /**
    * (PunctType) dash, hyphen<br/>
    */
   Dash,
   /**
    * (Case) dative<br/>
    **/
   Dat,
   /**
    * (Definite) Definite<br/>
    */
   Def,
   /**
    * (AdvType) Degree<br/>
    */
   Deg,
   /**
    * (Case) delative<br/>
    **/
   Del,
   /**
    * (PronType) Demonstrative Pronoun, Determiner, Numeral or Adverb <br/>
    */
   Dem,
   /**
    * (Style) derogative<br/>
    */
   Derg,
   /***
    * (Mood) Desiderative<br/>
    */
   Des,
   /**
    * (NumForm) Number expressed using digits<br/>
    */
   Digit,
   /**
    * (Voice)  Direct Voice<br/>
    */
   Dir,
   /**
    * (Case) distributive<br/>
    **/
   Dis,
   /**
    * (NumType) Distributive Numeral<br/>
    */
   Dist,
   /**
    * (Number) Dual Number<br/>
    */
   Dual,
   /**
    * (Case) elative<br/>
    **/
   Ela,
   /**
    * (Polite) Referent Elevating<br/>
    */
   Elev,
   /**
    * (PartType) Particle of Emphasis<br/> (PronType) Emphatic Determiner<br/>
    */
   Emp,
   /**
    * (Case) equative<br/> (Degree) Equative<br/>
    **/
   Equ,
   /**
    * (Case) ergative<br/>
    **/
   Erg,
   /**
    * (Case) essive / prolative<br/>
    **/
   Ess,
   /**
    * (AdvType) Existential<br/> (Clusivity) Exclusive<br/>
    */
   Ex,
   /**
    * (PronType) Exclamative Determiner<br/>
    */
   Exc,
   /**
    * (PunctType) exclamation mark<br/>
    */
   Excl,
   /**
    * (Style) expressive, emotional<br/>
    */
   Expr,
   /**
    * (Gender) Feminine Gender<br/> (PossGender) Feminine Possessor<br/>
    */
   Fem,
   /**
    * (Evident) Firsthand<br/>
    */
   Fh,
   /**
    * (PunctSize) Final<br/> (VerbForm) Finite Verb<br/>
    */
   Fin,
   /**
    * (Person) First Person<br/> (PossPerson) First Person<br/>
    */
   First {
      @Override
      public String label() {
         return "1";
      }
   },
   /**
    * (Style) Formal, Literary<br/>
    */
   Form,
   /**
    * (Person) Fourth Person<br/>
    */
   Fourth {
      @Override
      public String label() {
         return "4";
      }
   },
   /**
    * (NumType) Fraction<br/>
    */
   Frac,
   /**
    * (Tense) Future Tense<br/>
    */
   Fut,
   /**
    * (VerbForm) Gerundive<br/>
    */
   Gdv,
   /**
    * (NameType) Geographical Name<br/>
    */
   Geo,
   /**
    * (VerbForm) Gerund<br/>
    */
   Ger,
   /**
    * (NameType) Given Name<br/>
    */
   Giv,
   /**
    * (Number) Greater Paucal Number<br/>
    */
   Grpa,
   /**
    * (Number) Greater Plural Number<br/>
    */
   Grpl,
   /**
    * (Aspect) Habitual<br/>
    */
   Hab,
   /**
    * (Animacy) Human.<br/>
    */
   Hum,
   /**
    * (Polite) Speaker Humbling<br/>
    */
   Humb,
   /**
    * (Case) illative<br/>
    **/
   Ill,
   /**
    * (Aspect) Imperfect<br/> (Mood) Imperative<br/> (Tense) Imperfect<br/>
    */
   Imp,
   /**
    * (Clusivity) Inclusive<br/>
    */
   In,
   /**
    * (Animacy) Inanimate<br/>
    */
   Inan,
   /**
    * (Definite) Indefinite<br/> (Mood) Indicative<br/> (PronType) Indefinite Pronoun, Determiner, Numeral or
    * Adverb<br/>
    */
   Ind,
   /**
    * (Case) inessive<br/>
    **/
   Ine,
   /**
    * (PartType) Infinitive Marker<br/> (VerbForm) Infinitive<br/>
    */
   Inf,
   /**
    * (Polite) Informal Register<br/>
    */
   Infm,
   /**
    * (PunctSize) Initial<br/>
    */
   Ini,
   /**
    * instrumental / instructive<br/>
    **/
   Ins,
   /**
    * (PronType) Interrogative Pronoun, Determiner, Numeral or Adverb<br/>
    */
   Int,
   /**
    * (Subcat) Intransitive<br/>
    */
   Intr,
   /**
    * (Number) Inverse Number<br/> (Voice) Inverse Voice<br/>
    */
   Inv,
   /**
    * (Aspect) Iterative<br/>
    */
   Iter,
   /***
    * (Mood) Jussive / Injunctive<br/>
    */
   Jus,
   /**
    * (Case) lative / directional allative<br/>
    **/
   Lat,
   /**
    * (VerbType) Light (support) Verb<br/>
    */
   Light,
   /**
    * (AdvType) Location<br/> (Case) Locative<br/>
    */
   Loc,
   /**
    * (AdvType) Manner<br/>
    */
   Man,
   /**
    * (Gender) Masculine Gender<br/> (PossGender) Masculine Possessor<br/>
    */
   Masc,
   /**
    * (Voice) Middle Voice <br/>
    */
   Mid,
   /**
    * (AdvType) Modal<br/> (PartType) Modal Particle<br/> (VerbType) Modal Verb<br/>
    */
   Mod,
   /**
    * (NumType) Multiplicative Numeral<br/>
    */
   Mult,
   /**
    * (NameType) Nationality<br/>
    */
   Nat,
   /***
    * (Mood) Necessitative<br/>
    */
   Nec,
   /**
    * (Polarity) Negative<br/> (PronType) Negative Pronoun, Determiner or Adverb<br/>
    */
   Neg,
   /**
    * (Gender) Neuter Gender<br/>
    */
   Neut,
   /**
    * (Evident) Non-Firsthand<br/>
    */
   Nfh,
   /**
    * (Animacy) Non-human<br/>
    */
   Nhum,
   /**
    * Abbr<br/> Echo<br/> Foreign<br/> Hyph<br/> Poss<br/> Prefix<br/> Reflex<br/> Typo<br/>
    */
   No,
   /**
    * (Case) nominative / direct<br/>
    **/
   Nom,
   /**
    * (Style) Normal, Neutral<br/>
    */
   Norm,
   /**
    * (PrepCase) Non-Prepositional Case<br/>
    */
   Npr,
   /**
    * (ConjType) Mathematical Operator<br/>
    */
   Oper,
   /***
    * (Mood) Optative<br/>
    */
   Opt,
   /**
    * (NumType) Ordinal Number<br/>
    */
   Ord,
   /**
    * (NameType) Other<br/>
    */
   Oth,
   /**
    * (Case) partitive<br/>
    **/
   Par,
   /**
    * (VerbForm) Participle, Verbal Adjective<br/>
    */
   Part,
   /**
    * (Voice)  Passive Voice<br/>
    */
   Pass,
   /**
    * (Tense) Past Tense<br/>
    */
   Past,
   /**
    * (Number) Paucal Number<br/>
    */
   Pauc,
   /**
    * (Case) perlative<br/>
    **/
   Per,
   /**
    * (Aspect) Perfect<br/>
    */
   Perf,
   /**
    * (PunctType) Period at the end of sentence<br/>
    */
   Peri,
   /**
    * (Number) Plural Number<br/> (PossNumber) Plural Possessor<br/> (PossedNumber) Plural Possession<br/>
    */
   Plur,
   /**
    * (Style) Poetic<br/>
    */
   Poet,
   /**
    * (Degree) Positive, First Degree<br/> (Polarity) Positive<br/>
    */
   Pos,
   /**
    * (AdpType) Postposition<br/>
    */
   Post,
   /***
    * (Mood) Potential<br/>
    */
   Pot,
   /**
    * (Tense)Pluperfect <br/>
    */
   Pqp,
   /**
    * (PrepCase) Prepositional Case<br/>
    */
   Pre,
   /**
    * (AdpType) Preposition<br/>
    */
   Prep,

   /**
    * (Tense) Present Tense <br/>
    */
   Pres,
   /**
    * (NameType) Product<br/>
    */
   Pro,
   /**
    * (Aspect) Progressive<br/>
    */
   Prog,
   /**
    * (Aspect) Prospective<br/>
    */
   Prosp,
   /***
    * (Mood) Purposive<br/>
    */
   Prp,
   /**
    * (NameType) Person Name<br/> (PronType) Personal or Possessive Personal Pronoun or Determiner<br/>
    */
   Prs,
   /**
    * (Number) Plurale tantum<br/>
    */
   Ptan,
   /**
    * (PunctType) question mark<br/>
    */
   Qest,
   /***
    * (Mood) Quotative<br/>
    */
   Qot,

   /**
    * (PunctType) quotation marks<br/>
    */
   Quot,
   /**
    * (NumType) Range of Values<br/>
    */
   Range,
   /**
    * (Style) Rare<br/>
    */
   Rare,
   /**
    * (PronType) Reciprocal Pronoun<br/> (Voice) Reciprocal Voice<br/>
    */
   Rcp,
   /**
    * (PronType) Relative Pronoun, Determiner, Numeral or Adverb<br/>
    */
   Rel,
   /**
    * (PartType) Particle of Response<br/>
    */
   Res,
   /**
    * (NumForm) Other Roman Numeral<br/>
    */
   Roman,
   /**
    * (Person) Second Person<br/> (PossPerson) Second Person<br/>
    */
   Second {
      @Override
      public String label() {
         return "2";
      }
   },
   /**
    * (PunctType) semicolon<br/>
    */
   Semi,
   /**
    * (NumType) Number of sets of things; collective numeral<br/>
    */
   Sets,
   /**
    * (Number) Singular Number<br/> (PossNumber) Singular Possessor<br/> (PossedNumber) Singular Possession<br/>
    */
   Sing,
   /**
    * (Style) Slang<br/>
    */
   Slng,
   /**
    * (Definite) Specific<br/>
    */
   Spec,
   /**
    * (AdvType) State<br/>
    */
   Sta,
   /**
    * (Case) sublative<br/> (Mood) Subjunctive / Conjunctive<br/>
    **/
   Sub,
   /**
    * (Case) superessive<br/> (Degree) Superlative, third degree<br/> (VerbForm) Supine<br/>
    **/
   Sup,
   /**
    * (NameType) Surname<br/>
    */
   Sur,
   /**
    * (PunctType) symbol<br/>
    */
   Symb,
   /**
    * (Case) temporal<br/>
    **/
   Tem,
   /**
    * (Case) terminative / terminal allative<br/>
    **/
   Ter,
   /**
    * (Person) Third Person<br/> (PossPerson) Third Person<br/>
    */
   Third {
      @Override
      public String label() {
         return "3";
      }
   },
   /**
    * (AdvType) Time<br/>
    */
   Tim,
   /**
    * (PronType) Total Pronoun, Determiner, or Adverb<br/>
    */
   Tot,
   /**
    * (Case) translative / factive<br/>
    **/
   Tra,
   /**
    * (Subcat) Transitive<br/>
    */
   Tran,
   /**
    * (Number) Trial Number<br/>
    */
   Tri,

   /**
    * (PartType) Separated verb prefix<br/>
    */
   Vbp,
   /**
    * (VerbForm) Verbal Noun, Masdar<br/>
    */
   Vnoun,

   /**
    * (AdpType) Vocalized Preposition<br/> (Case) Vocative<br/>
    **/
   Voc,
   /**
    * (Style) vernacular<br/>
    */
   Vrnc,
   /**
    * (Style) vulgar<br/>
    */
   Vulg,
   /**
    * (NumForm) Number expressed as a Word<br/>
    */
   Word,
   /**
    * Abbr<br/> Echo<br/> Foreign<br/> Hyph<br/> Poss<br/> Prefix<br/> Reflex<br/> Typo<br/>
    */
   Yes,

   /**
    * (Person) Zero Person<br/>
    */
   Zero {
      @Override
      public String label() {
         return "0";
      }
   },
   /**
    * (Polite) Formal Register<br/>
    */
   form;

   /**
    * Parses the given string to a Value
    *
    * @param string the string to parse
    * @return the value
    */
   public static UniversalFeatureValue parse(@NonNull String string) {
      switch(string) {
         case "0":
            return Zero;
         case "1":
            return First;
         case "2":
            return Second;
         case "3":
            return Third;
         case "4":
            return Fourth;
         default:
            return valueOf(string);
      }
   }

   @Override
   public boolean isInstance(@NonNull Tag tag) {
      return this == tag;
   }

   @Override
   public String toString() {
      return label();
   }

}//END OF Value



