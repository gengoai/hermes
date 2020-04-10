/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * <p>http:</p>
//www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.hermes.morphology;

import lombok.NonNull;

/**
 * Enumeration of the Universal features as defined in<a href="https://universaldependencies.org/u/feat/index.html">Universal
 * Dependencies (UD) framework</a>
 */
public enum UniversalFeature {
   /**
    * <p>Abbreviation:</p>
    * Yes / No
    */
   Abbr {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Yes, UniversalFeatureValue.No);
      }
   },
   /**
    * <p>Adposition Type:</p>
    * Prep (Preposition)<br/> Post (Postposition)<br/> Circ (Circumposition)<br/> Voc (Vocalized Preposition)
    */
   AdpType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Prep, UniversalFeatureValue.Post, UniversalFeatureValue.Circ, UniversalFeatureValue.Voc);
      }
   },
   /**
    * <p>Adverb Type:</p>
    * Man (Manner)<br/> Loc (Location)<br/> Tim (Time)<br/> Deg (Degree)<br/> Cau (Cause)<br/> Mod (Modal)<br/> Sta
    * (State)<br/> Ex (Existential)<br/> Adadj (Ad-Adjective)
    */
   AdvType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Man,
                                 UniversalFeatureValue.Loc,
                                 UniversalFeatureValue.Tim,
                                 UniversalFeatureValue.Deg,
                                 UniversalFeatureValue.Cau,
                                 UniversalFeatureValue.Mod,
                                 UniversalFeatureValue.Sta,
                                 UniversalFeatureValue.Ex,
                                 UniversalFeatureValue.Adadj);
      }
   },
   /**
    * <p>Animacy:</p>
    * Anim (Animate)<br/> Inan (Inanimate)<br/> Hum (Human)<br/> Nhum (Non-Human)
    */
   Animacy {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Anim, UniversalFeatureValue.Inan, UniversalFeatureValue.Hum, UniversalFeatureValue.Nhum);
      }
   },
   /**
    * <p>Aspect:</p>
    * Hab (Habitual)<br/> Imp (Imperfect)<br/> Perf (Perfect)<br/> Prosp (Prospective)<br/> Prog (Progressive)<br/> Iter
    * (Iterative)
    */
   Aspect {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Hab,
                                 UniversalFeatureValue.Imp,
                                 UniversalFeatureValue.Perf,
                                 UniversalFeatureValue.Prosp,
                                 UniversalFeatureValue.Prog,
                                 UniversalFeatureValue.Iter);
      }
   },
   /**
    * <p>Case:</p>
    * Abe (abessive)<br/> Abl (ablative)<br/> Abs (absolutive)<br/> Acc (accusative / oblique)<br/> Add (additive)<br/>
    * Ade (adessive)<br/> All (allative)<br/> Ben (benefactive / destinative)<br/> Cau (causative / motivative /
    * purposive)<br/> Cmp (comparative)<br/> Cns (considerative)<br/> Com (comitative / associative)<br/> Dat
    * (dative)<br/> Del (delative)<br/> Dis (distributive)<br/> Ela (elative)<br/> Equ (equative)<br/> Erg
    * (ergative)<br/> Ess (essive / prolative)<br/> Ill (illative)<br/> Ine (inessive)<br/> Ins (instrumental /
    * instructive)<br/> Lat (lative / directional allative)<br/> Loc (locative)<br/> Nom (nominative / direct)<br/> Par
    * (partitive)<br/> Per (perlative)<br/> Sub (sublative)<br/> Sup (superessive)<br/> Tem (temporal)<br/> Ter
    * (terminative / terminal allative)<br/> Tra (translative / factive)<br/> Voc (vocative)<br/>
    */
   Case {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Abe,
                                 UniversalFeatureValue.Abl,
                                 UniversalFeatureValue.Abs,
                                 UniversalFeatureValue.Acc,
                                 UniversalFeatureValue.Add,
                                 UniversalFeatureValue.Ade,
                                 UniversalFeatureValue.All,
                                 UniversalFeatureValue.Ben,
                                 UniversalFeatureValue.Cau,
                                 UniversalFeatureValue.Cmp,
                                 UniversalFeatureValue.Cns,
                                 UniversalFeatureValue.Com,
                                 UniversalFeatureValue.Dat,
                                 UniversalFeatureValue.Del,
                                 UniversalFeatureValue.Dis,
                                 UniversalFeatureValue.Ela,
                                 UniversalFeatureValue.Equ,
                                 UniversalFeatureValue.Erg,
                                 UniversalFeatureValue.Ess,
                                 UniversalFeatureValue.Ill,
                                 UniversalFeatureValue.Ine,
                                 UniversalFeatureValue.Ins,
                                 UniversalFeatureValue.Lat,
                                 UniversalFeatureValue.Loc,
                                 UniversalFeatureValue.Nom,
                                 UniversalFeatureValue.Par,
                                 UniversalFeatureValue.Per,
                                 UniversalFeatureValue.Sub,
                                 UniversalFeatureValue.Sup,
                                 UniversalFeatureValue.Tem,
                                 UniversalFeatureValue.Ter,
                                 UniversalFeatureValue.Tra,
                                 UniversalFeatureValue.Voc);
      }
   },
   /**
    * <p>Clusivity:</p>
    * In (Inclusive)<br/> Ex (Exclusive)
    */
   Clusivity {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Ex, UniversalFeatureValue.In);
      }
   },
   /**
    * <p>Conjunction Type:</p>
    * Comp (Comparing Conjunction)<br/> Oper (Mathematical Operator)
    */
   ConjType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Comp, UniversalFeatureValue.Oper);
      }
   },
   /**
    * <p>Definite Type:</p>
    * Com (Complex)<br/> Ind (Indefinite)<br/> Spec (Specific)<br/> Def (Definite)<br/> Cons (Construct State / Reduced
    * Definiteness)
    */
   Definite {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Com, UniversalFeatureValue.Ind, UniversalFeatureValue.Spec, UniversalFeatureValue.Def, UniversalFeatureValue.Cons);
      }
   },
   /**
    * <p>Degree:</p>
    * Abs (Absolute Superlative)<br/> Pos (Positive, First Degree)<br/> Equ (Equative)<br/> Cmp (Comparative, second
    * degree)<br/> Sup (Superlative, third degree).
    */
   Degree {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Abs, UniversalFeatureValue.Pos, UniversalFeatureValue.Equ, UniversalFeatureValue.Cmp, UniversalFeatureValue.Sup);
      }
   },
   /**
    * <p>Echo word or a Reduplicative:</p>
    * Yes, No
    */
   Echo {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Yes, UniversalFeatureValue.No);
      }
   },
   /**
    * <p>Evidentiality:</p>
    * Fh (Firsthand)<br/> Nfh (Non-Firsthand)
    */
   Evident {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Fh, UniversalFeatureValue.Nfh);
      }
   },
   /**
    * Foreign Word?
    */
   Foreign {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Yes, UniversalFeatureValue.No);
      }
   },
   /**
    * <p>Gender:</p>
    * Masc (Masculine Gender)<br/> Fem (Feminine Gender)<br/> Neut (Neuter Gender)<br/> Com (Common Gender).
    */
   Gender {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Masc, UniversalFeatureValue.Fem, UniversalFeatureValue.Neut, UniversalFeatureValue.Com);
      }
   },
   /**
    * <p>Hyphenated Compound or part of it</p>
    * Yes / No
    */
   Hyph {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Yes, UniversalFeatureValue.No);
      }
   },
   /**
    * <p>Mood:</p>
    * Ind (Indicative)<br/> Imp (Imperative)<br/> Cnd (Conditional)<br/> Pot (Potential)<br/> Sub (Subjunctive /
    * Conjunctive)<br/> Jus (Jussive / Injunctive)<br/> Prp (Purposive)<br/> Qot (Quotative)<br/> Opt (Optative)<br/>
    * Des (Desiderative)<br/> Nec (Necessitative)<br/> Adm (Admirative)
    */
   Mood {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Ind,
                                 UniversalFeatureValue.Imp,
                                 UniversalFeatureValue.Cnd,
                                 UniversalFeatureValue.Pot,
                                 UniversalFeatureValue.Sub,
                                 UniversalFeatureValue.Jus,
                                 UniversalFeatureValue.Prp,
                                 UniversalFeatureValue.Qot,
                                 UniversalFeatureValue.Opt,
                                 UniversalFeatureValue.Des,
                                 UniversalFeatureValue.Nec,
                                 UniversalFeatureValue.Adm);
      }
   },
   /**
    * <p>Named Entity Type:</p>
    * Geo (Geographical Name)<br/> Prs (Person Name)<br/> Giv (Given Name)<br/> Sur (Surname)<br/> Nat
    * (Nationality)<br/> Com (Company Name)<br/> Pro (Product)<br/> Oth (Other)
    */
   NameType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Geo,
                                 UniversalFeatureValue.Prs,
                                 UniversalFeatureValue.Giv,
                                 UniversalFeatureValue.Sur,
                                 UniversalFeatureValue.Nat,
                                 UniversalFeatureValue.Com,
                                 UniversalFeatureValue.Pro,
                                 UniversalFeatureValue.Oth);
      }
   },
   /**
    * <p>Numeral Form:</p>
    * Word (Number expressed as a Word)<br/> Digit (Number expressed using digits)<br/> Roman (Roman Numeral)
    */
   NumForm {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Word,
                                 UniversalFeatureValue.Digit,
                                 UniversalFeatureValue.Roman);
      }
   },
   /**
    * <p>Numeral Type:</p>
    * Card (Cardinal Number)<br/> Ord (Ordinal Number)<br/> Mult (Multiplicative Numeral)<br/> Frac (Fraction)<br/> Sets
    * (Number of sets of things; collective numeral)<br/> Dist (Distributive Numeral)<br/> Range (Range of Values)
    */
   NumType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Card,
                                 UniversalFeatureValue.Ord,
                                 UniversalFeatureValue.Mult,
                                 UniversalFeatureValue.Frac,
                                 UniversalFeatureValue.Sets,
                                 UniversalFeatureValue.Dist,
                                 UniversalFeatureValue.Range);
      }
   },
   /**
    * <p>Number:</p>
    * Sing (Singular Number)<br/> Plur (Plural Number)<br/> Dual (Dual Number)<br/> Tri (Trial Number)<br/> Pauc (Paucal
    * Number)<br/> Grpa (Greater Paucal Number)<br/> Grpl (Greater Plural Number)<br/> Inv (Inverse Number)<br/> Count
    * (Count Plural)<br/> Ptan (Plurale tantum)<br/> Coll (Collective / Mass / Singulre Tantum)
    */
   Number {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Sing,
                                 UniversalFeatureValue.Plur,
                                 UniversalFeatureValue.Dual,
                                 UniversalFeatureValue.Tri,
                                 UniversalFeatureValue.Pauc,
                                 UniversalFeatureValue.Grpa,
                                 UniversalFeatureValue.Grpl,
                                 UniversalFeatureValue.Inv,
                                 UniversalFeatureValue.Count,
                                 UniversalFeatureValue.Ptan,
                                 UniversalFeatureValue.Coll);
      }
   },
   /**
    * <p>Particle Type:</p>
    * Mod (Modal Particle)<br/> Emp (Particle of Emphasis)<br/> Res (Particle of Response)<br/> Inf (Infinitive
    * Marker)<br/> Vbp (Separated verb prefix)
    */
   PartType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Mod,
                                 UniversalFeatureValue.Emp,
                                 UniversalFeatureValue.Res,
                                 UniversalFeatureValue.Inf,
                                 UniversalFeatureValue.Vbp);
      }
   },
   /**
    * <p>Person:</p>
    * 0 (Zero Person)<br/> 1 (First Person)<br/> 2 (Second Person)<br/> 3 (Third Person) 4 (Fourth Person).
    */
   Person {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Zero,
                                 UniversalFeatureValue.First,
                                 UniversalFeatureValue.Second,
                                 UniversalFeatureValue.Third,
                                 UniversalFeatureValue.Fourth);
      }
   },
   /**
    * <p>Polarity:</p>
    * Neg (Negative)<br/> Pos (Positive)
    */
   Polarity {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Neg,
                                 UniversalFeatureValue.Pos);
      }
   },
   /**
    * <p>Politeness:</p>
    * Infm (Informal Register)<br/> Form (Formal Register)<br/> Elev (Referent Elevating)<br/> Humb (Speaker Humbling)
    */
   Polite {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Infm,
                                 UniversalFeatureValue.form,
                                 UniversalFeatureValue.Elev,
                                 UniversalFeatureValue.Humb);
      }
   },
   /**
    * <p>Possessive:</p>
    * Yes /No
    */
   Poss {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Yes, UniversalFeatureValue.No);
      }
   },
   /**
    * Possessor's Gender: Masc (Masculine Possessor)<br/> Fem (Feminine Possessor)
    */
   PossGender {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Masc, UniversalFeatureValue.Fem);
      }
   },
   /**
    * Possessor's Number: Sing (Singular Possessor)<br/> Plur (Plural Possessor)
    */
   PossNumber {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Sing, UniversalFeatureValue.Plur);
      }
   },
   /**
    * Possessor's Person: 1 (First Person)<br/> 2 (Second Person)<br/> 3 (Third Person)
    */
   PossPerson {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.First, UniversalFeatureValue.Second, UniversalFeatureValue.Third);
      }
   },
   /**
    * Possessed Object's Number: Sing (Singular Possession)<br/> Plur (Plural Possession)
    */
   PossedNumber {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Sing, UniversalFeatureValue.Plur);
      }
   },
   /**
    * <p>Word functions as a prefix in a compound construction:</p>
    * Yes /No
    */
   Prefix {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Yes, UniversalFeatureValue.No);
      }
   },
   /**
    * <p>Case form sensitive to Prepositions:</p>
    * Npr (Non-Prepositional Case)<br/> Pre (Prepositional Case)
    */
   PrepCase {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Npr, UniversalFeatureValue.Pre);
      }
   },
   /**
    * <p>Pronominal Type:</p>
    * Prs (Personal or Possessive Personal Pronoun or Determiner)<br/> Rcp (Reciprocal Pronoun)<br/> Art (Article)<br/>
    * Int (Interrogative Pronoun, Determiner, Numeral or Adverb)<br/> Rel (Relative Pronoun, Determiner, Numeral or
    * Adverb)<br/> Exc (Exclamative Determiner)<br/> Dem (Demonstrative Pronoun, Determiner, Numeral or Adverb)<br/> Emp
    * (Emphatic Determiner)<br/> Tot (Total Pronoun, Determiner, or Adverb)<br/> Neg (Negative Pronoun, Determiner or
    * Adverb)<br/> Ind (Indefinite Pronoun, Determiner, Numeral or Adverb)<br/>
    */
   PronType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Prs,
                                 UniversalFeatureValue.Rcp,
                                 UniversalFeatureValue.Art,
                                 UniversalFeatureValue.Rel,
                                 UniversalFeatureValue.Exc,
                                 UniversalFeatureValue.Dem,
                                 UniversalFeatureValue.Int,
                                 UniversalFeatureValue.Tot,
                                 UniversalFeatureValue.Emp,
                                 UniversalFeatureValue.Neg,
                                 UniversalFeatureValue.Ind);
      }
   },
   /**
    * <p>Punction Side</p>
    * Ini (Initial)<br/> Fin (Final)<br/>
    */
   PunctSide {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Ini, UniversalFeatureValue.Fin);
      }
   },
   /**
    * <p>Punctation Type:</p>
    * Peri (Period at the end of sentence)</br> Qest (question mark)</br> Excl (exclamation mark)</br> Quot (quotation
    * marks)</br> Brck (bracket)</br> Comm (comma)</br> Colo (colon;)</br> Semi (semicolon)</br> Dash (dash,
    * hyphen)</br> Symb (symbol)</br>
    */
   PunctType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Peri,
                                 UniversalFeatureValue.Qest,
                                 UniversalFeatureValue.Excl,
                                 UniversalFeatureValue.Quot,
                                 UniversalFeatureValue.Brck,
                                 UniversalFeatureValue.Comm,
                                 UniversalFeatureValue.Colo,
                                 UniversalFeatureValue.Semi,
                                 UniversalFeatureValue.Dash,
                                 UniversalFeatureValue.Symb);
      }
   },
   /**
    * <p>Reflexive:</p>
    * Yes /No
    */
   Reflex {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Yes, UniversalFeatureValue.No);
      }
   },
   /**
    * <p>tyle or sublanguage to which this word form belongs:</p>
    * Arch (Archaic, Obsolete)<br/> Rare (Rare)<br/> Form (Formal, Literary)<br/> Poet (Poetic)<br/> Norm (Normal,
    * Neutral)<br/> Coll (colloquial)<br/> Vrnc (vernacular)<br/> Slng (slang)<br/> Expr (expressive, emotional)<br/>
    * Derg (derogative)<br/> Vulg (vulgar)<br/>
    */
   Style {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Arch,
                                 UniversalFeatureValue.Rare,
                                 UniversalFeatureValue.Form,
                                 UniversalFeatureValue.Poet,
                                 UniversalFeatureValue.Norm,
                                 UniversalFeatureValue.Coll,
                                 UniversalFeatureValue.Vrnc,
                                 UniversalFeatureValue.Slng,
                                 UniversalFeatureValue.Expr,
                                 UniversalFeatureValue.Derg,
                                 UniversalFeatureValue.Vulg);
      }
   },
   /**
    * <p>Subcategorization:</p>
    * Intr (Intransitive Verb)<br/> Tran (Transitive Verb)<br/>
    */
   Subcat {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Intr, UniversalFeatureValue.Tran);
      }
   },
   /**
    * <p>Tense:</p>
    * Past (Past Tense)<br/> Pres (Present Tense)<br/> Fut (Future Tense)<br/> Imp (Imperfect)<br/> Pqp
    * (Pluperfect)<br/>
    */
   Tense {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Past,
                                 UniversalFeatureValue.Pres,
                                 UniversalFeatureValue.Fut,
                                 UniversalFeatureValue.Imp,
                                 UniversalFeatureValue.Pqp);
      }
   },
   /**
    * <p>Is this a misspelled word:</p>
    * Yes/No
    */
   Typo {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Yes, UniversalFeatureValue.No);
      }
   },
   /**
    * <p>Form of verb or deverbative:</p>
    * Fin (Finite Verb)<br/> Inf (Infinitive)<br/> Sup (Supine)<br/> Part (Participle, Verbal Adjective) <br/> Conv
    * (Converb, Transgressive, Adverbial Participle, Verbal Adverb)<br/> Gdv (Gerundive)<br/> Ger (Gerund)<br/> Vnoun
    * (Verbal Noun, Masdar)<br/>
    */
   VerbForm {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Fin,
                                 UniversalFeatureValue.Inf,
                                 UniversalFeatureValue.Sup,
                                 UniversalFeatureValue.Part,
                                 UniversalFeatureValue.Conv,
                                 UniversalFeatureValue.Gdv,
                                 UniversalFeatureValue.Ger,
                                 UniversalFeatureValue.Vnoun);
      }
   },
   /**
    * <p>Verb Type:</p>
    * Aux (Auxiliary Verb)<br/> Cop (Copula Verb)<br/> Mod (Modal Verb)<br/> Light (Light (support) Verb)<br/>
    */
   VerbType {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Aux,
                                 UniversalFeatureValue.Cop,
                                 UniversalFeatureValue.Mod,
                                 UniversalFeatureValue.Light);
      }
   },
   /**
    * <p>Voice:</p>
    * Act (Active Voice)<br/> Mid (Middle Voice)<br/> Pass (Passive Voice)<br/> Antip (Antipassive Voice)<br/> Dir
    * (Direct Voice)<br/> Inv (Inverse Voice)<br/> Rcp (Reciprocal Voice)<br/> Cau (Causative Voice)<br/>
    */
   Voice {
      @Override
      public boolean isValidValue(@NonNull UniversalFeatureValue value) {
         return value.isInstance(UniversalFeatureValue.Act,
                                 UniversalFeatureValue.Mid,
                                 UniversalFeatureValue.Pass,
                                 UniversalFeatureValue.Antip,
                                 UniversalFeatureValue.Dir,
                                 UniversalFeatureValue.Inv,
                                 UniversalFeatureValue.Rcp,
                                 UniversalFeatureValue.Cau);
      }
   };

   /**
    * Parses the given string to create a UniversalFeature
    *
    * @param string the string
    * @return the feature
    */
   public static UniversalFeature parse(@NonNull String string) {
      return valueOf(string);
   }

   /**
    * Checks if the given value is valid for this feature
    *
    * @param value the value
    * @return True if the given value is valid for this feature.
    */
   public abstract boolean isValidValue(@NonNull UniversalFeatureValue value);

}//END OF Feature
