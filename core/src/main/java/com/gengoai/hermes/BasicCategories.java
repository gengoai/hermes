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

package com.gengoai.hermes;

import com.gengoai.Tag;
import com.gengoai.annotation.Preload;

/**
 * A basic set of categories to describe words which is useful for inferring higher level concepts.
 */
@Preload
public enum BasicCategories implements Tag {
   ROOT(null),
   ABSTRACT_ENTITY(ROOT),
   PHYSICAL_ENTITY(ROOT),
   COMMUNICATION(ABSTRACT_ENTITY),
   HUMAN_BEHAVIOUR(ABSTRACT_ENTITY),
   NATURE(ABSTRACT_ENTITY),
   PROCESSES(ABSTRACT_ENTITY),
   MEASUREMENT(ABSTRACT_ENTITY),
   NUMBERS(ABSTRACT_ENTITY),
   SCIENCES(ABSTRACT_ENTITY),
   SOCIETY(ABSTRACT_ENTITY),
   TIME(ABSTRACT_ENTITY),
   FACILITIES(PHYSICAL_ENTITY),
   FOOD_AND_DRINK(PHYSICAL_ENTITY),
   PRODUCTS(PHYSICAL_ENTITY),
   MATERIALS(PHYSICAL_ENTITY),
   LIVING_THING(PHYSICAL_ENTITY),
   MATTER(PHYSICAL_ENTITY),
   PLACES(PHYSICAL_ENTITY),
   LANGUAGE(COMMUNICATION),
   NONVERBAL_COMMUNICATION(COMMUNICATION),
   TELECOMMUNICATIONS(COMMUNICATION),
   MASS_MEDIA(COMMUNICATION),
   DRINKING(HUMAN_BEHAVIOUR),
   HUMAN_ACTIVITY(HUMAN_BEHAVIOUR),
   VIOLENCE(HUMAN_BEHAVIOUR),
   SOCIAL_ACTS(HUMAN_BEHAVIOUR),
   COGNITIVE_PROCESSES(PROCESSES),
   CHEMICAL_PROCESSES(PROCESSES),
   NATURAL_PHENOMENA(PROCESSES),
   UNITS_OF_MEASURE(MEASUREMENT),
   NATURAL_SCIENCES(SCIENCES),
   FORMAL_SCIENCES(SCIENCES),
   AERONAUTICS(SCIENCES),
   APPLIED_SCIENCES(SCIENCES),
   MEDICINE(SCIENCES),
   PSEUDOSCIENCE(SCIENCES),
   SOCIAL_SCIENCES(SCIENCES),
   CULTURE(SOCIETY),
   CRAFTS(SOCIETY),
   DISCRIMINATION(SOCIETY),
   EDUCATION(SOCIETY),
   HOME(SOCIETY),
   IDEOLOGIES(SOCIETY),
   LAW(SOCIETY),
   ORGANIZATIONS(SOCIETY),
   SECURITY(SOCIETY),
   TIME_PERIOD(TIME),
   TIMEKEEPING(TIME),
   BUILDINGS_AND_STRUCTURES(FACILITIES),
   GEOGRAPHICAL_AND_ORGANIZATIONAL_ENTITY(FACILITIES),
   LINES(FACILITIES),
   BEVERAGES(FOOD_AND_DRINK),
   COOKING(FOOD_AND_DRINK),
   DAIRY_PRODUCTS(FOOD_AND_DRINK),
   DIETS(FOOD_AND_DRINK),
   FOODS(FOOD_AND_DRINK),
   TOILETRIES(PRODUCTS),
   CLOTHING(PRODUCTS),
   DRUGS(PRODUCTS),
   FURNITURE(PRODUCTS),
   COMPUTING(PRODUCTS),
   ELECTRONICS(PRODUCTS),
   MACHINES(PRODUCTS),
   TOOLS(PRODUCTS),
   WEAPONS(PRODUCTS),
   NATURAL_MATERIALS(MATERIALS),
   PAPER(MATERIALS),
   MICROORGANISMS(LIVING_THING),
   ANIMALS(LIVING_THING),
   BIOLOGICAL_ATTRIBUTES(LIVING_THING),
   PLANTS(LIVING_THING),
   ACIDS(MATTER),
   CHEMICAL_ELEMENTS(MATTER),
   GASES(MATTER),
   LIQUIDS(MATTER),
   METALS(MATTER),
   ORGANIC_COMPOUNDS(MATTER),
   POISONS(MATTER),
   SUBATOMIC_PARTICLES(MATTER),
   GEOGRAPHIC_REGION(PLACES),
   CELESTIAL_BODIES(PLACES),
   DIRECTIONS(PLACES),
   GEOPOLITICAL_ENTITIES(PLACES),
   LANGUAGES(LANGUAGE),
   RHETORIC(LANGUAGE),
   WRITING(LANGUAGE),
   ABBREVIATIONS(LANGUAGE),
   ACRONYMS(LANGUAGE),
   BROADCASTING(TELECOMMUNICATIONS),
   TELEPHONY(TELECOMMUNICATIONS),
   PERIODICALS(MASS_MEDIA),
   HYGIENE(HUMAN_ACTIVITY),
   EXERCISE(HUMAN_ACTIVITY),
   HUNTING(HUMAN_ACTIVITY),
   MANUFACTURING(HUMAN_ACTIVITY),
   RECREATION(HUMAN_ACTIVITY),
   TRAVEL(HUMAN_ACTIVITY),
   SEX(HUMAN_ACTIVITY),
   CLEANING(HUMAN_ACTIVITY),
   SPORTS(HUMAN_ACTIVITY),
   WAR(VIOLENCE),
   EMOTIONS(COGNITIVE_PROCESSES),
   SENSES(COGNITIVE_PROCESSES),
   COMBUSTION(CHEMICAL_PROCESSES),
   ATMOSPHERIC_PHENOMENA(NATURAL_PHENOMENA),
   WEATHER(NATURAL_PHENOMENA),
   ASTRONOMY(NATURAL_SCIENCES),
   BIOLOGY(NATURAL_SCIENCES),
   CHEMISTRY(NATURAL_SCIENCES),
   EARTH_SCIENCES(NATURAL_SCIENCES),
   PHYSICS(NATURAL_SCIENCES),
   MATHEMATICS(FORMAL_SCIENCES),
   COMPUTER_SCIENCE(FORMAL_SCIENCES),
   ARCHITECTURE(APPLIED_SCIENCES),
   ENGINEERING(APPLIED_SCIENCES),
   INFORMATION_SCIENCE(APPLIED_SCIENCES),
   ALTERNATIVE_MEDICINE(MEDICINE),
   PATHOLOGY(MEDICINE),
   SURGERY(MEDICINE),
   FORTEANA(PSEUDOSCIENCE),
   ECONOMICS(SOCIAL_SCIENCES),
   LINGUISTICS(SOCIAL_SCIENCES),
   PSYCHOLOGY(SOCIAL_SCIENCES),
   SOCIOLOGY(SOCIAL_SCIENCES),
   ART(CULTURE),
   ENTERTAINMENT(CULTURE),
   FANDOM(CULTURE),
   FOLKLORE(CULTURE),
   HOPI_CULTURE(CULTURE),
   HUMANITIES(CULTURE),
   MYTHOLOGY(CULTURE),
   RELIGION(CULTURE),
   CULTURAL_REGION(CULTURE),
   FORMS_OF_DISCRIMINATION(DISCRIMINATION),
   COMPETITION_LAW(LAW),
   COPYRIGHT(LAW),
   CRIMINAL_LAW(LAW),
   POLITICAL_ORGANIZATIONS(ORGANIZATIONS),
   PERIODIC_OCCURRENCES(TIME_PERIOD),
   CALENDAR_TERMS(TIMEKEEPING),
   BUILDINGS(BUILDINGS_AND_STRUCTURES),
   ROOMS(BUILDINGS_AND_STRUCTURES),
   SCHOOLS(GEOGRAPHICAL_AND_ORGANIZATIONAL_ENTITY),
   WATERWAYS(LINES),
   RAILROADS(LINES),
   ROADS(LINES),
   ALCOHOLIC_BEVERAGES(BEVERAGES),
   CONDIMENTS(FOODS),
   DESSERTS(FOODS),
   MEATS(FOODS),
   NUTS(FOODS),
   SAUCES(FOODS),
   SPICES_AND_HERBS(FOODS),
   FASHION(CLOTHING),
   JEWELRY(CLOTHING),
   NETWORKING(COMPUTING),
   SOFTWARE(COMPUTING),
   MECHANISMS(MACHINES),
   VEHICLES(MACHINES),
   CONTAINERS(TOOLS),
   KITCHENWARE(TOOLS),
   MUSICAL_INSTRUMENTS(TOOLS),
   BACTERIA(MICROORGANISMS),
   FUNGI(MICROORGANISMS),
   INVERTEBRATES(ANIMALS),
   VERTEBRATES(ANIMALS),
   GENDER(BIOLOGICAL_ATTRIBUTES),
   VASCULAR_PLANTS(PLANTS),
   NON_VASCULAR_PLANTS(PLANTS),
   BODILY_FLUIDS(LIQUIDS),
   CARBOHYDRATES(ORGANIC_COMPOUNDS),
   FERMIONS(SUBATOMIC_PARTICLES),
   HADRONS(SUBATOMIC_PARTICLES),
   LANDFORMS(GEOGRAPHIC_REGION),
   PLANETS(CELESTIAL_BODIES),
   MOONS(CELESTIAL_BODIES),
   ADMINISTRATIVE_DIVISION(GEOPOLITICAL_ENTITIES),
   COUNTIES(GEOPOLITICAL_ENTITIES),
   CITIES(GEOPOLITICAL_ENTITIES),
   COUNTRIES(GEOPOLITICAL_ENTITIES),
   STATES_OR_PREFECTURES(GEOPOLITICAL_ENTITIES),
   AUTONOMOUS_REGION(GEOPOLITICAL_ENTITIES),
   PROVINCES(GEOPOLITICAL_ENTITIES),
   TERRITORIES(GEOPOLITICAL_ENTITIES),
   MICROSTATES(GEOPOLITICAL_ENTITIES),
   KRAIS(GEOPOLITICAL_ENTITIES),
   ADMINISTRATIVE_REGIONS(GEOPOLITICAL_ENTITIES),
   ORTHOGRAPHY(WRITING),
   WRITING_SYSTEMS(WRITING),
   LITERATURE(WRITING),
   BATHING(HYGIENE),
   YOGA(EXERCISE),
   GAMES(RECREATION),
   HOBBIES(RECREATION),
   TOYS(RECREATION),
   SEXUALITY(SEX),
   BALL_GAMES(SPORTS),
   BOARD_SPORTS(SPORTS),
   EQUESTRIANISM(SPORTS),
   HOCKEY(SPORTS),
   MARTIAL_ARTS(SPORTS),
   MOTOR_RACING(SPORTS),
   RACQUET_SPORTS(SPORTS),
   WATER_SPORTS(SPORTS),
   WINTER_SPORTS(SPORTS),
   FEAR(EMOTIONS),
   LOVE(EMOTIONS),
   SMELL(SENSES),
   VISION(SENSES),
   CONSTELLATIONS(ASTRONOMY),
   PLANETOLOGY(ASTRONOMY),
   ANATOMY(BIOLOGY),
   BOTANY(BIOLOGY),
   DEVELOPMENTAL_BIOLOGY(BIOLOGY),
   GENETICS(BIOLOGY),
   MICROBIOLOGY(BIOLOGY),
   MYCOLOGY(BIOLOGY),
   NEUROSCIENCE(BIOLOGY),
   SYSTEMATICS(BIOLOGY),
   ZOOLOGY(BIOLOGY),
   BIOCHEMISTRY(CHEMISTRY),
   ORGANIC_CHEMISTRY(CHEMISTRY),
   GEOGRAPHY(EARTH_SCIENCES),
   GEOLOGY(EARTH_SCIENCES),
   ENERGY(PHYSICS),
   ELECTROMAGNETISM(PHYSICS),
   MECHANICS(PHYSICS),
   NUCLEAR_PHYSICS(PHYSICS),
   ALGEBRA(MATHEMATICS),
   APPLIED_MATHEMATICS(MATHEMATICS),
   GEOMETRY(MATHEMATICS),
   MATHEMATICAL_ANALYSIS(MATHEMATICS),
   PROBABILITY(MATHEMATICS),
   PROGRAMMING(COMPUTER_SCIENCE),
   CONSTRUCTION(ENGINEERING),
   ELECTRICAL_ENGINEERING(ENGINEERING),
   DISEASES(PATHOLOGY),
   OCCULT(FORTEANA),
   BUSINESS(ECONOMICS),
   GRAMMAR(LINGUISTICS),
   DANCE(ART),
   DESIGN(ART),
   MUSIC(ART),
   FILM(ENTERTAINMENT),
   THEATER(ENTERTAINMENT),
   NARRATOLOGY(HUMANITIES),
   HISTORY(HUMANITIES),
   PHILOSOPHY(HUMANITIES),
   CELTIC_MYTHOLOGY(MYTHOLOGY),
   MYTHOLOGICAL_CREATURES(MYTHOLOGY),
   ABRAHAMISM(RELIGION),
   BUDDHISM(RELIGION),
   GNOSTICISM(RELIGION),
   GODS(RELIGION),
   HINDUISM(RELIGION),
   MYSTICISM(RELIGION),
   PAGANISM(RELIGION),
   GOVERNMENT(POLITICAL_ORGANIZATIONS),
   MONTHS(PERIODIC_OCCURRENCES),
   SEASONS(PERIODIC_OCCURRENCES),
   HOLIDAYS(CALENDAR_TERMS),
   SEXAGENARY_CYCLE(CALENDAR_TERMS),
   HOUSING(BUILDINGS),
   WINES(ALCOHOLIC_BEVERAGES),
   GEMS(JEWELRY),
   INTERNET(NETWORKING),
   VIDEO_GAMES(SOFTWARE),
   AUTOMOBILES(VEHICLES),
   WATERCRAFT(VEHICLES),
   CYCLING(VEHICLES),
   NAUTICAL(VEHICLES),
   ARTHROPODS(INVERTEBRATES),
   MOLLUSKS(INVERTEBRATES),
   WORMS(INVERTEBRATES),
   AMPHIBIANS(VERTEBRATES),
   BIRDS(VERTEBRATES),
   FISH(VERTEBRATES),
   MAMMALS(VERTEBRATES),
   REPTILES(VERTEBRATES),
   MALE(GENDER),
   SEED_BEARING_PLANTS(VASCULAR_PLANTS),
   SPORE_BEARING_PLANTS(VASCULAR_PLANTS),
   WOODY_PLANTS(VASCULAR_PLANTS),
   ALGAE(NON_VASCULAR_PLANTS),
   BODIES_OF_WATER(LANDFORMS),
   CONTINENTS(LANDFORMS),
   ISLANDS(LANDFORMS),
   ATMOSPHERE(PLANETS),
   CAPITAL_CITIES(CITIES),
   LETTERS_SYMBOLS_AND_PUNCTUATION(ORTHOGRAPHY),
   BOOKS(LITERATURE),
   COMICS(LITERATURE),
   FICTION(LITERATURE),
   HORROR(LITERATURE),
   LITERARY_GENRES(LITERATURE),
   BOARD_GAMES(GAMES),
   CARD_GAMES(GAMES),
   GAMBLING(GAMES),
   FOOTBALL(BALL_GAMES),
   RUGBY(BALL_GAMES),
   COLORS(VISION),
   BODY(ANATOMY),
   HORTICULTURE(BOTANY),
   MEDICAL_GENETICS(GENETICS),
   NEUROLOGY(NEUROSCIENCE),
   TAXONOMY(SYSTEMATICS),
   ANTHROPOLOGY(ZOOLOGY),
   ARTHROPODOLOGY(ZOOLOGY),
   MALACOLOGY(ZOOLOGY),
   BIOMOLECULES(BIOCHEMISTRY),
   PHARMACOLOGY(BIOCHEMISTRY),
   LIGHT(ENERGY),
   SOUND(ENERGY),
   INFORMATION_THEORY(APPLIED_MATHEMATICS),
   HTML(PROGRAMMING),
   DISEASE(DISEASES),
   DIVINATION(OCCULT),
   ADMINISTRATION(BUSINESS),
   BUSINESSES(BUSINESS),
   FINANCE(BUSINESS),
   INDUSTRIES(BUSINESS),
   MONEY(BUSINESS),
   TRADING(BUSINESS),
   PARTS_OF_SPEECH(GRAMMAR),
   DRAMA(THEATER),
   PLOT_DEVICES(NARRATOLOGY),
   ANCIENT_NEAR_EAST(HISTORY),
   HERALDRY(HISTORY),
   LOGIC(PHILOSOPHY),
   ETHICS(PHILOSOPHY),
   CHRISTIANITY(ABRAHAMISM),
   FORMS_OF_GOVERNMENT(GOVERNMENT),
   POLITICS(GOVERNMENT),
   SOCIAL_MEDIA(INTERNET),
   WORLD_WIDE_WEB(INTERNET),
   ARACHNIDS(ARTHROPODS),
   CRUSTACEANS(ARTHROPODS),
   INSECTS(ARTHROPODS),
   CEPHALOPODS(MOLLUSKS),
   GASTROPODS(MOLLUSKS),
   SALAMANDERS(AMPHIBIANS),
   BIRDS_OF_PREY(BIRDS),
   FOWLS(BIRDS),
   FRESHWATER_BIRDS(BIRDS),
   PERCHING_BIRDS(BIRDS),
   RATITES(BIRDS),
   SEABIRDS(BIRDS),
   SHOREBIRDS(BIRDS),
   LABROID_FISH(FISH),
   PERCOID_FISH(FISH),
   CARNIVORES(MAMMALS),
   EVENTOED_UNGULATES(MAMMALS),
   LAGOMORPHS(MAMMALS),
   ODDTOED_UNGULATES(MAMMALS),
   PRIMATES(MAMMALS),
   RODENTS(MAMMALS),
   LIZARDS(REPTILES),
   SNAKES(REPTILES),
   ANGIOSPERMS(SEED_BEARING_PLANTS),
   GYMNOSPERMS(SEED_BEARING_PLANTS),
   SEAS(BODIES_OF_WATER),
   LETTER_NAMES(LETTERS_SYMBOLS_AND_PUNCTUATION),
   AMERICAN_FICTION(FICTION),
   BRITISH_FICTION(FICTION),
   FAIRY_TALE(FICTION),
   FICTIONAL_CHARACTERS(FICTION),
   FICTIONAL_LOCATIONS(FICTION),
   SHAHNAMEH(FICTION),
   CHESS(BOARD_GAMES),
   PIGMENTS(COLORS),
   BODY_PARTS(BODY),
   DEATH(BODY),
   GAITS(BODY),
   HAIR(BODY),
   HEALTH(BODY),
   MIND(BODY),
   PREGNANCY(BODY),
   PROTEINS(BIOMOLECULES),
   PUBLIC_ADMINISTRATION(ADMINISTRATION),
   PUBLISHING(INDUSTRIES),
   CURRENCY(MONEY),
   CATHOLICISM(CHRISTIANITY),
   PROTESTANTISM(CHRISTIANITY),
   DEMOCRACY(FORMS_OF_GOVERNMENT),
   UK_POLITICS(POLITICS),
   US_POLITICS(POLITICS),
   BEETLES(INSECTS),
   BUTTERFLIES(INSECTS),
   COCKROACHES(INSECTS),
   HYMENOPTERANS(INSECTS),
   MOTHS(INSECTS),
   ANATIDS(FRESHWATER_BIRDS),
   CERTHIOID_BIRDS(PERCHING_BIRDS),
   MELIPHAGOID_BIRDS(PERCHING_BIRDS),
   WARBLERS(PERCHING_BIRDS),
   CANIDS(CARNIVORES),
   FELIDS(CARNIVORES),
   PINNIPEDS(CARNIVORES),
   ANTELOPES(EVENTOED_UNGULATES),
   CERVIDS(EVENTOED_UNGULATES),
   CETACEANS(EVENTOED_UNGULATES),
   EQUIDS(ODDTOED_UNGULATES),
   HOMINIDS(PRIMATES),
   MONKEYS(PRIMATES),
   ALISMATALES_ORDER_PLANTS(ANGIOSPERMS),
   ANTHEMIDEAE_TRIBE_PLANTS(ANGIOSPERMS),
   APIALES_ORDER_PLANTS(ANGIOSPERMS),
   ASPARAGALES_ORDER_PLANTS(ANGIOSPERMS),
   ASTERALES_ORDER_PLANTS(ANGIOSPERMS),
   BRASSICALES_ORDER_PLANTS(ANGIOSPERMS),
   CARYOPHYLLALES_ORDER_PLANTS(ANGIOSPERMS),
   COMMELINIDS(ANGIOSPERMS),
   ERICALES_ORDER_PLANTS(ANGIOSPERMS),
   FAGALES_ORDER_PLANTS(ANGIOSPERMS),
   GENTIANALES_ORDER_PLANTS(ANGIOSPERMS),
   LAMIALES_ORDER_PLANTS(ANGIOSPERMS),
   LEGUMES(ANGIOSPERMS),
   LILIALES_ORDER_PLANTS(ANGIOSPERMS),
   MAGNOLIIDS(ANGIOSPERMS),
   MALPIGHIALES_ORDER_PLANTS(ANGIOSPERMS),
   MALVALES_ORDER_PLANTS(ANGIOSPERMS),
   MYRTALES_ORDER_PLANTS(ANGIOSPERMS),
   NIGHTSHADES(ANGIOSPERMS),
   RANUNCULALES_ORDER_PLANTS(ANGIOSPERMS),
   ROSALES_ORDER_PLANTS(ANGIOSPERMS),
   SAPINDALES_ORDER_PLANTS(ANGIOSPERMS),
   CONIFERS(GYMNOSPERMS),
   TISSUES(BODY_PARTS),
   ENZYMES(PROTEINS),
   PUBLIC_SAFETY(PUBLIC_ADMINISTRATION),
   DOGS(CANIDS),
   CATS(FELIDS),
   HUMAN(HOMINIDS),
   CELERY_FAMILY_PLANTS(APIALES_ORDER_PLANTS),
   SUCCULENTS(ASPARAGALES_ORDER_PLANTS),
   CRUCIFERS(BRASSICALES_ORDER_PLANTS),
   GRASSES(COMMELINIDS),
   ZINGIBERALES_ORDER_PLANTS(COMMELINIDS),
   BEECH_FAMILY_PLANTS(FAGALES_ORDER_PLANTS),
   MINT_FAMILY_PLANTS(LAMIALES_ORDER_PLANTS),
   MALLOW_FAMILY_PLANTS(MALVALES_ORDER_PLANTS),
   MYRTLE_FAMILY_PLANTS(MYRTALES_ORDER_PLANTS),
   POME_FRUITS(ROSALES_ORDER_PLANTS),
   STONE_FRUITS(ROSALES_ORDER_PLANTS),
   RUE_FAMILY_PLANTS(SAPINDALES_ORDER_PLANTS),
   CRIME_PREVENTION(PUBLIC_SAFETY),
   EMERGENCY_SERVICES(PUBLIC_SAFETY),
   FEMALE_PEOPLE(HUMAN),
   MALE_PEOPLE(HUMAN),
   CHILDREN(HUMAN),
   FAMILY(HUMAN),
   OCCUPATIONS(HUMAN),
   GROUPS_OF_PEOPLE(HUMAN),
   GRAINS(GRASSES),
   LAW_ENFORCEMENT(CRIME_PREVENTION),
   FEMALE_OCCUPATIONS(FEMALE_PEOPLE),
   FEMALE_NATIONALITIES(FEMALE_PEOPLE),
   MALE_OCCUPATIONS(MALE_PEOPLE),
   MALE_NATIONALITIES(MALE_PEOPLE),
   FAMILY_MEMBERS(FAMILY),
   ARTISTS(OCCUPATIONS),
   ATHLETES(OCCUPATIONS),
   AUTHORS(OCCUPATIONS),
   HEADS_OF_STATE(OCCUPATIONS),
   HEALTHCARE_OCCUPATIONS(OCCUPATIONS),
   MUSICIANS(OCCUPATIONS),
   SCIENTISTS(OCCUPATIONS),
   ETHNIC_GROUPS(GROUPS_OF_PEOPLE),
   NATIONALITIES(GROUPS_OF_PEOPLE),
   TRIBES(NATIONALITIES);

   private BasicCategories parent;

   BasicCategories(BasicCategories parent) {
      this.parent = parent;
   }

   @Override
   public boolean isInstance(Tag tag) {
      if(!(tag instanceof BasicCategories)) {
         return false;
      }
      if(this == tag || tag == ROOT) {
         return true;
      }
      Tag p = parent();
      while(p != null) {
         if(p == tag) {
            return true;
         }
         p = p.parent();
      }
      return false;
   }

   @Override
   public Tag parent() {
      return parent;
   }

}//END OF BasicCategories