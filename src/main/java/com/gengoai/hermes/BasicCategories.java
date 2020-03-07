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

import com.gengoai.EnumValue;
import com.gengoai.Lazy;
import com.gengoai.Registry;
import com.gengoai.Tag;
import com.gengoai.annotation.Preload;
import com.google.common.collect.Lists;

import java.util.*;

@Preload
public class BasicCategories extends EnumValue<BasicCategories> {
   private static final Registry<BasicCategories> registry = new Registry<>(BasicCategories::new,
                                                                            BasicCategories.class);
   public static final BasicCategories ROOT = make("ROOT", null);
   public static final BasicCategories ABSTRACT_ENTITY = make("ABSTRACT_ENTITY", ROOT);
   public static final BasicCategories PHYSICAL_ENTITY = make("PHYSICAL_ENTITY", ROOT);
   public static final BasicCategories COMMUNICATION = make("COMMUNICATION", ABSTRACT_ENTITY);
   public static final BasicCategories HUMAN_BEHAVIOUR = make("HUMAN_BEHAVIOUR", ABSTRACT_ENTITY);
   public static final BasicCategories NATURE = make("NATURE", ABSTRACT_ENTITY);
   public static final BasicCategories PROCESSES = make("PROCESSES", ABSTRACT_ENTITY);
   public static final BasicCategories MEASUREMENT = make("MEASUREMENT", ABSTRACT_ENTITY);
   public static final BasicCategories NUMBERS = make("NUMBERS", ABSTRACT_ENTITY);
   public static final BasicCategories SCIENCES = make("SCIENCES", ABSTRACT_ENTITY);
   public static final BasicCategories SOCIETY = make("SOCIETY", ABSTRACT_ENTITY);
   public static final BasicCategories TIME = make("TIME", ABSTRACT_ENTITY);
   public static final BasicCategories FACILITIES = make("FACILITIES", PHYSICAL_ENTITY);
   public static final BasicCategories FOOD_AND_DRINK = make("FOOD_AND_DRINK", PHYSICAL_ENTITY);
   public static final BasicCategories PRODUCTS = make("PRODUCTS", PHYSICAL_ENTITY);
   public static final BasicCategories MATERIALS = make("MATERIALS", PHYSICAL_ENTITY);
   public static final BasicCategories LIVING_THING = make("LIVING_THING", PHYSICAL_ENTITY);
   public static final BasicCategories MATTER = make("MATTER", PHYSICAL_ENTITY);
   public static final BasicCategories PLACES = make("PLACES", PHYSICAL_ENTITY);
   public static final BasicCategories LANGUAGE = make("LANGUAGE", COMMUNICATION);
   public static final BasicCategories NONVERBAL_COMMUNICATION = make("NONVERBAL_COMMUNICATION", COMMUNICATION);
   public static final BasicCategories TELECOMMUNICATIONS = make("TELECOMMUNICATIONS", COMMUNICATION);
   public static final BasicCategories MASS_MEDIA = make("MASS_MEDIA", COMMUNICATION);
   public static final BasicCategories DRINKING = make("DRINKING", HUMAN_BEHAVIOUR);
   public static final BasicCategories HUMAN_ACTIVITY = make("HUMAN_ACTIVITY", HUMAN_BEHAVIOUR);
   public static final BasicCategories VIOLENCE = make("VIOLENCE", HUMAN_BEHAVIOUR);
   public static final BasicCategories SOCIAL_ACTS = make("SOCIAL_ACTS", HUMAN_BEHAVIOUR);
   public static final BasicCategories COGNITIVE_PROCESSES = make("COGNITIVE_PROCESSES", PROCESSES);
   public static final BasicCategories CHEMICAL_PROCESSES = make("CHEMICAL_PROCESSES", PROCESSES);
   public static final BasicCategories NATURAL_PHENOMENA = make("NATURAL_PHENOMENA", PROCESSES);
   public static final BasicCategories UNITS_OF_MEASURE = make("UNITS_OF_MEASURE", MEASUREMENT);
   public static final BasicCategories NATURAL_SCIENCES = make("NATURAL_SCIENCES", SCIENCES);
   public static final BasicCategories FORMAL_SCIENCES = make("FORMAL_SCIENCES", SCIENCES);
   public static final BasicCategories AERONAUTICS = make("AERONAUTICS", SCIENCES);
   public static final BasicCategories APPLIED_SCIENCES = make("APPLIED_SCIENCES", SCIENCES);
   public static final BasicCategories MEDICINE = make("MEDICINE", SCIENCES);
   public static final BasicCategories PSEUDOSCIENCE = make("PSEUDOSCIENCE", SCIENCES);
   public static final BasicCategories SOCIAL_SCIENCES = make("SOCIAL_SCIENCES", SCIENCES);
   public static final BasicCategories CULTURE = make("CULTURE", SOCIETY);
   public static final BasicCategories CRAFTS = make("CRAFTS", SOCIETY);
   public static final BasicCategories DISCRIMINATION = make("DISCRIMINATION", SOCIETY);
   public static final BasicCategories EDUCATION = make("EDUCATION", SOCIETY);
   public static final BasicCategories HOME = make("HOME", SOCIETY);
   public static final BasicCategories IDEOLOGIES = make("IDEOLOGIES", SOCIETY);
   public static final BasicCategories LAW = make("LAW", SOCIETY);
   public static final BasicCategories ORGANIZATIONS = make("ORGANIZATIONS", SOCIETY);
   public static final BasicCategories SECURITY = make("SECURITY", SOCIETY);
   public static final BasicCategories TIME_PERIOD = make("TIME_PERIOD", TIME);
   public static final BasicCategories TIMEKEEPING = make("TIMEKEEPING", TIME);
   public static final BasicCategories BUILDINGS_AND_STRUCTURES = make("BUILDINGS_AND_STRUCTURES", FACILITIES);
   public static final BasicCategories GEOGRAPHICAL_AND_ORGANIZATIONAL_ENTITY = make(
         "GEOGRAPHICAL_AND_ORGANIZATIONAL_ENTITY",
         FACILITIES);
   public static final BasicCategories LINES = make("LINES", FACILITIES);
   public static final BasicCategories BEVERAGES = make("BEVERAGES", FOOD_AND_DRINK);
   public static final BasicCategories COOKING = make("COOKING", FOOD_AND_DRINK);
   public static final BasicCategories DAIRY_PRODUCTS = make("DAIRY_PRODUCTS", FOOD_AND_DRINK);
   public static final BasicCategories DIETS = make("DIETS", FOOD_AND_DRINK);
   public static final BasicCategories FOODS = make("FOODS", FOOD_AND_DRINK);
   public static final BasicCategories TOILETRIES = make("TOILETRIES", PRODUCTS);
   public static final BasicCategories CLOTHING = make("CLOTHING", PRODUCTS);
   public static final BasicCategories DRUGS = make("DRUGS", PRODUCTS);
   public static final BasicCategories FURNITURE = make("FURNITURE", PRODUCTS);
   public static final BasicCategories COMPUTING = make("COMPUTING", PRODUCTS);
   public static final BasicCategories ELECTRONICS = make("ELECTRONICS", PRODUCTS);
   public static final BasicCategories MACHINES = make("MACHINES", PRODUCTS);
   public static final BasicCategories TOOLS = make("TOOLS", PRODUCTS);
   public static final BasicCategories WEAPONS = make("WEAPONS", PRODUCTS);
   public static final BasicCategories NATURAL_MATERIALS = make("NATURAL_MATERIALS", MATERIALS);
   public static final BasicCategories PAPER = make("PAPER", MATERIALS);
   public static final BasicCategories MICROORGANISMS = make("MICROORGANISMS", LIVING_THING);
   public static final BasicCategories ANIMALS = make("ANIMALS", LIVING_THING);
   public static final BasicCategories BIOLOGICAL_ATTRIBUTES = make("BIOLOGICAL_ATTRIBUTES", LIVING_THING);
   public static final BasicCategories PLANTS = make("PLANTS", LIVING_THING);
   public static final BasicCategories ACIDS = make("ACIDS", MATTER);
   public static final BasicCategories CHEMICAL_ELEMENTS = make("CHEMICAL_ELEMENTS", MATTER);
   public static final BasicCategories GASES = make("GASES", MATTER);
   public static final BasicCategories LIQUIDS = make("LIQUIDS", MATTER);
   public static final BasicCategories METALS = make("METALS", MATTER);
   public static final BasicCategories ORGANIC_COMPOUNDS = make("ORGANIC_COMPOUNDS", MATTER);
   public static final BasicCategories POISONS = make("POISONS", MATTER);
   public static final BasicCategories SUBATOMIC_PARTICLES = make("SUBATOMIC_PARTICLES", MATTER);
   public static final BasicCategories GEOGRAPHIC_REGION = make("GEOGRAPHIC_REGION", PLACES);
   public static final BasicCategories CELESTIAL_BODIES = make("CELESTIAL_BODIES", PLACES);
   public static final BasicCategories DIRECTIONS = make("DIRECTIONS", PLACES);
   public static final BasicCategories GEOPOLITICAL_ENTITIES = make("GEOPOLITICAL_ENTITIES", PLACES);
   public static final BasicCategories LANGUAGES = make("LANGUAGES", LANGUAGE);
   public static final BasicCategories RHETORIC = make("RHETORIC", LANGUAGE);
   public static final BasicCategories WRITING = make("WRITING", LANGUAGE);
   public static final BasicCategories ABBREVIATIONS = make("ABBREVIATIONS", LANGUAGE);
   public static final BasicCategories ACRONYMS = make("ACRONYMS", LANGUAGE);
   public static final BasicCategories BROADCASTING = make("BROADCASTING", TELECOMMUNICATIONS);
   public static final BasicCategories TELEPHONY = make("TELEPHONY", TELECOMMUNICATIONS);
   public static final BasicCategories PERIODICALS = make("PERIODICALS", MASS_MEDIA);
   public static final BasicCategories HYGIENE = make("HYGIENE", HUMAN_ACTIVITY);
   public static final BasicCategories EXERCISE = make("EXERCISE", HUMAN_ACTIVITY);
   public static final BasicCategories HUNTING = make("HUNTING", HUMAN_ACTIVITY);
   public static final BasicCategories MANUFACTURING = make("MANUFACTURING", HUMAN_ACTIVITY);
   public static final BasicCategories RECREATION = make("RECREATION", HUMAN_ACTIVITY);
   public static final BasicCategories TRAVEL = make("TRAVEL", HUMAN_ACTIVITY);
   public static final BasicCategories SEX = make("SEX", HUMAN_ACTIVITY);
   public static final BasicCategories CLEANING = make("CLEANING", HUMAN_ACTIVITY);
   public static final BasicCategories SPORTS = make("SPORTS", HUMAN_ACTIVITY);
   public static final BasicCategories WAR = make("WAR", VIOLENCE);
   public static final BasicCategories EMOTIONS = make("EMOTIONS", COGNITIVE_PROCESSES);
   public static final BasicCategories SENSES = make("SENSES", COGNITIVE_PROCESSES);
   public static final BasicCategories COMBUSTION = make("COMBUSTION", CHEMICAL_PROCESSES);
   public static final BasicCategories ATMOSPHERIC_PHENOMENA = make("ATMOSPHERIC_PHENOMENA", NATURAL_PHENOMENA);
   public static final BasicCategories WEATHER = make("WEATHER", NATURAL_PHENOMENA);
   public static final BasicCategories ASTRONOMY = make("ASTRONOMY", NATURAL_SCIENCES);
   public static final BasicCategories BIOLOGY = make("BIOLOGY", NATURAL_SCIENCES);
   public static final BasicCategories CHEMISTRY = make("CHEMISTRY", NATURAL_SCIENCES);
   public static final BasicCategories EARTH_SCIENCES = make("EARTH_SCIENCES", NATURAL_SCIENCES);
   public static final BasicCategories PHYSICS = make("PHYSICS", NATURAL_SCIENCES);
   public static final BasicCategories MATHEMATICS = make("MATHEMATICS", FORMAL_SCIENCES);
   public static final BasicCategories COMPUTER_SCIENCE = make("COMPUTER_SCIENCE", FORMAL_SCIENCES);
   public static final BasicCategories ARCHITECTURE = make("ARCHITECTURE", APPLIED_SCIENCES);
   public static final BasicCategories ENGINEERING = make("ENGINEERING", APPLIED_SCIENCES);
   public static final BasicCategories INFORMATION_SCIENCE = make("INFORMATION_SCIENCE", APPLIED_SCIENCES);
   public static final BasicCategories ALTERNATIVE_MEDICINE = make("ALTERNATIVE_MEDICINE", MEDICINE);
   public static final BasicCategories PATHOLOGY = make("PATHOLOGY", MEDICINE);
   public static final BasicCategories SURGERY = make("SURGERY", MEDICINE);
   public static final BasicCategories FORTEANA = make("FORTEANA", PSEUDOSCIENCE);
   public static final BasicCategories ECONOMICS = make("ECONOMICS", SOCIAL_SCIENCES);
   public static final BasicCategories LINGUISTICS = make("LINGUISTICS", SOCIAL_SCIENCES);
   public static final BasicCategories PSYCHOLOGY = make("PSYCHOLOGY", SOCIAL_SCIENCES);
   public static final BasicCategories SOCIOLOGY = make("SOCIOLOGY", SOCIAL_SCIENCES);
   public static final BasicCategories ART = make("ART", CULTURE);
   public static final BasicCategories ENTERTAINMENT = make("ENTERTAINMENT", CULTURE);
   public static final BasicCategories FANDOM = make("FANDOM", CULTURE);
   public static final BasicCategories FOLKLORE = make("FOLKLORE", CULTURE);
   public static final BasicCategories HOPI_CULTURE = make("HOPI_CULTURE", CULTURE);
   public static final BasicCategories HUMANITIES = make("HUMANITIES", CULTURE);
   public static final BasicCategories MYTHOLOGY = make("MYTHOLOGY", CULTURE);
   public static final BasicCategories RELIGION = make("RELIGION", CULTURE);
   public static final BasicCategories CULTURAL_REGION = make("CULTURAL_REGION", CULTURE);
   public static final BasicCategories FORMS_OF_DISCRIMINATION = make("FORMS_OF_DISCRIMINATION", DISCRIMINATION);
   public static final BasicCategories COMPETITION_LAW = make("COMPETITION_LAW", LAW);
   public static final BasicCategories COPYRIGHT = make("COPYRIGHT", LAW);
   public static final BasicCategories CRIMINAL_LAW = make("CRIMINAL_LAW", LAW);
   public static final BasicCategories POLITICAL_ORGANIZATIONS = make("POLITICAL_ORGANIZATIONS", ORGANIZATIONS);
   public static final BasicCategories PERIODIC_OCCURRENCES = make("PERIODIC_OCCURRENCES", TIME_PERIOD);
   public static final BasicCategories CALENDAR_TERMS = make("CALENDAR_TERMS", TIMEKEEPING);
   public static final BasicCategories BUILDINGS = make("BUILDINGS", BUILDINGS_AND_STRUCTURES);
   public static final BasicCategories ROOMS = make("ROOMS", BUILDINGS_AND_STRUCTURES);
   public static final BasicCategories SCHOOLS = make("SCHOOLS", GEOGRAPHICAL_AND_ORGANIZATIONAL_ENTITY);
   public static final BasicCategories WATERWAYS = make("WATERWAYS", LINES);
   public static final BasicCategories RAILROADS = make("RAILROADS", LINES);
   public static final BasicCategories ROADS = make("ROADS", LINES);
   public static final BasicCategories ALCOHOLIC_BEVERAGES = make("ALCOHOLIC_BEVERAGES", BEVERAGES);
   public static final BasicCategories CONDIMENTS = make("CONDIMENTS", FOODS);
   public static final BasicCategories DESSERTS = make("DESSERTS", FOODS);
   public static final BasicCategories MEATS = make("MEATS", FOODS);
   public static final BasicCategories NUTS = make("NUTS", FOODS);
   public static final BasicCategories SAUCES = make("SAUCES", FOODS);
   public static final BasicCategories SPICES_AND_HERBS = make("SPICES_AND_HERBS", FOODS);
   public static final BasicCategories FASHION = make("FASHION", CLOTHING);
   public static final BasicCategories JEWELRY = make("JEWELRY", CLOTHING);
   public static final BasicCategories NETWORKING = make("NETWORKING", COMPUTING);
   public static final BasicCategories SOFTWARE = make("SOFTWARE", COMPUTING);
   public static final BasicCategories MECHANISMS = make("MECHANISMS", MACHINES);
   public static final BasicCategories VEHICLES = make("VEHICLES", MACHINES);
   public static final BasicCategories CONTAINERS = make("CONTAINERS", TOOLS);
   public static final BasicCategories KITCHENWARE = make("KITCHENWARE", TOOLS);
   public static final BasicCategories MUSICAL_INSTRUMENTS = make("MUSICAL_INSTRUMENTS", TOOLS);
   public static final BasicCategories BACTERIA = make("BACTERIA", MICROORGANISMS);
   public static final BasicCategories FUNGI = make("FUNGI", MICROORGANISMS);
   public static final BasicCategories INVERTEBRATES = make("INVERTEBRATES", ANIMALS);
   public static final BasicCategories VERTEBRATES = make("VERTEBRATES", ANIMALS);
   public static final BasicCategories GENDER = make("GENDER", BIOLOGICAL_ATTRIBUTES);
   public static final BasicCategories VASCULAR_PLANTS = make("VASCULAR_PLANTS", PLANTS);
   public static final BasicCategories NON_VASCULAR_PLANTS = make("NON_VASCULAR_PLANTS", PLANTS);
   public static final BasicCategories BODILY_FLUIDS = make("BODILY_FLUIDS", LIQUIDS);
   public static final BasicCategories CARBOHYDRATES = make("CARBOHYDRATES", ORGANIC_COMPOUNDS);
   public static final BasicCategories FERMIONS = make("FERMIONS", SUBATOMIC_PARTICLES);
   public static final BasicCategories HADRONS = make("HADRONS", SUBATOMIC_PARTICLES);
   public static final BasicCategories LANDFORMS = make("LANDFORMS", GEOGRAPHIC_REGION);
   public static final BasicCategories PLANETS = make("PLANETS", CELESTIAL_BODIES);
   public static final BasicCategories MOONS = make("MOONS", CELESTIAL_BODIES);
   public static final BasicCategories ADMINISTRATIVE_DIVISION = make("ADMINISTRATIVE_DIVISION", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories COUNTIES = make("COUNTIES", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories CITIES = make("CITIES", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories COUNTRIES = make("COUNTRIES", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories STATES_OR_PREFECTURES = make("STATES_OR_PREFECTURES", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories AUTONOMOUS_REGION = make("AUTONOMOUS_REGION", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories PROVINCES = make("PROVINCES", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories TERRITORIES = make("TERRITORIES", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories MICROSTATES = make("MICROSTATES", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories KRAIS = make("KRAIS", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories ADMINISTRATIVE_REGIONS = make("ADMINISTRATIVE_REGIONS", GEOPOLITICAL_ENTITIES);
   public static final BasicCategories ORTHOGRAPHY = make("ORTHOGRAPHY", WRITING);
   public static final BasicCategories WRITING_SYSTEMS = make("WRITING_SYSTEMS", WRITING);
   public static final BasicCategories LITERATURE = make("LITERATURE", WRITING);
   public static final BasicCategories BATHING = make("BATHING", HYGIENE);
   public static final BasicCategories YOGA = make("YOGA", EXERCISE);
   public static final BasicCategories GAMES = make("GAMES", RECREATION);
   public static final BasicCategories HOBBIES = make("HOBBIES", RECREATION);
   public static final BasicCategories TOYS = make("TOYS", RECREATION);
   public static final BasicCategories SEXUALITY = make("SEXUALITY", SEX);
   public static final BasicCategories BALL_GAMES = make("BALL_GAMES", SPORTS);
   public static final BasicCategories BOARD_SPORTS = make("BOARD_SPORTS", SPORTS);
   public static final BasicCategories EQUESTRIANISM = make("EQUESTRIANISM", SPORTS);
   public static final BasicCategories HOCKEY = make("HOCKEY", SPORTS);
   public static final BasicCategories MARTIAL_ARTS = make("MARTIAL_ARTS", SPORTS);
   public static final BasicCategories MOTOR_RACING = make("MOTOR_RACING", SPORTS);
   public static final BasicCategories RACQUET_SPORTS = make("RACQUET_SPORTS", SPORTS);
   public static final BasicCategories WATER_SPORTS = make("WATER_SPORTS", SPORTS);
   public static final BasicCategories WINTER_SPORTS = make("WINTER_SPORTS", SPORTS);
   public static final BasicCategories FEAR = make("FEAR", EMOTIONS);
   public static final BasicCategories LOVE = make("LOVE", EMOTIONS);
   public static final BasicCategories SMELL = make("SMELL", SENSES);
   public static final BasicCategories VISION = make("VISION", SENSES);
   public static final BasicCategories CONSTELLATIONS = make("CONSTELLATIONS", ASTRONOMY);
   public static final BasicCategories PLANETOLOGY = make("PLANETOLOGY", ASTRONOMY);
   public static final BasicCategories ANATOMY = make("ANATOMY", BIOLOGY);
   public static final BasicCategories BOTANY = make("BOTANY", BIOLOGY);
   public static final BasicCategories DEVELOPMENTAL_BIOLOGY = make("DEVELOPMENTAL_BIOLOGY", BIOLOGY);
   public static final BasicCategories GENETICS = make("GENETICS", BIOLOGY);
   public static final BasicCategories MICROBIOLOGY = make("MICROBIOLOGY", BIOLOGY);
   public static final BasicCategories MYCOLOGY = make("MYCOLOGY", BIOLOGY);
   public static final BasicCategories NEUROSCIENCE = make("NEUROSCIENCE", BIOLOGY);
   public static final BasicCategories SYSTEMATICS = make("SYSTEMATICS", BIOLOGY);
   public static final BasicCategories ZOOLOGY = make("ZOOLOGY", BIOLOGY);
   public static final BasicCategories BIOCHEMISTRY = make("BIOCHEMISTRY", CHEMISTRY);
   public static final BasicCategories ORGANIC_CHEMISTRY = make("ORGANIC_CHEMISTRY", CHEMISTRY);
   public static final BasicCategories GEOGRAPHY = make("GEOGRAPHY", EARTH_SCIENCES);
   public static final BasicCategories GEOLOGY = make("GEOLOGY", EARTH_SCIENCES);
   public static final BasicCategories ENERGY = make("ENERGY", PHYSICS);
   public static final BasicCategories ELECTROMAGNETISM = make("ELECTROMAGNETISM", PHYSICS);
   public static final BasicCategories MECHANICS = make("MECHANICS", PHYSICS);
   public static final BasicCategories NUCLEAR_PHYSICS = make("NUCLEAR_PHYSICS", PHYSICS);
   public static final BasicCategories ALGEBRA = make("ALGEBRA", MATHEMATICS);
   public static final BasicCategories APPLIED_MATHEMATICS = make("APPLIED_MATHEMATICS", MATHEMATICS);
   public static final BasicCategories GEOMETRY = make("GEOMETRY", MATHEMATICS);
   public static final BasicCategories MATHEMATICAL_ANALYSIS = make("MATHEMATICAL_ANALYSIS", MATHEMATICS);
   public static final BasicCategories PROBABILITY = make("PROBABILITY", MATHEMATICS);
   public static final BasicCategories PROGRAMMING = make("PROGRAMMING", COMPUTER_SCIENCE);
   public static final BasicCategories CONSTRUCTION = make("CONSTRUCTION", ENGINEERING);
   public static final BasicCategories ELECTRICAL_ENGINEERING = make("ELECTRICAL_ENGINEERING", ENGINEERING);
   public static final BasicCategories DISEASES = make("DISEASES", PATHOLOGY);
   public static final BasicCategories OCCULT = make("OCCULT", FORTEANA);
   public static final BasicCategories BUSINESS = make("BUSINESS", ECONOMICS);
   public static final BasicCategories GRAMMAR = make("GRAMMAR", LINGUISTICS);
   public static final BasicCategories DANCE = make("DANCE", ART);
   public static final BasicCategories DESIGN = make("DESIGN", ART);
   public static final BasicCategories MUSIC = make("MUSIC", ART);
   public static final BasicCategories FILM = make("FILM", ENTERTAINMENT);
   public static final BasicCategories THEATER = make("THEATER", ENTERTAINMENT);
   public static final BasicCategories NARRATOLOGY = make("NARRATOLOGY", HUMANITIES);
   public static final BasicCategories HISTORY = make("HISTORY", HUMANITIES);
   public static final BasicCategories PHILOSOPHY = make("PHILOSOPHY", HUMANITIES);
   public static final BasicCategories CELTIC_MYTHOLOGY = make("CELTIC_MYTHOLOGY", MYTHOLOGY);
   public static final BasicCategories MYTHOLOGICAL_CREATURES = make("MYTHOLOGICAL_CREATURES", MYTHOLOGY);
   public static final BasicCategories ABRAHAMISM = make("ABRAHAMISM", RELIGION);
   public static final BasicCategories BUDDHISM = make("BUDDHISM", RELIGION);
   public static final BasicCategories GNOSTICISM = make("GNOSTICISM", RELIGION);
   public static final BasicCategories GODS = make("GODS", RELIGION);
   public static final BasicCategories HINDUISM = make("HINDUISM", RELIGION);
   public static final BasicCategories MYSTICISM = make("MYSTICISM", RELIGION);
   public static final BasicCategories PAGANISM = make("PAGANISM", RELIGION);
   public static final BasicCategories GOVERNMENT = make("GOVERNMENT", POLITICAL_ORGANIZATIONS);
   public static final BasicCategories MONTHS = make("MONTHS", PERIODIC_OCCURRENCES);
   public static final BasicCategories SEASONS = make("SEASONS", PERIODIC_OCCURRENCES);
   public static final BasicCategories HOLIDAYS = make("HOLIDAYS", CALENDAR_TERMS);
   public static final BasicCategories SEXAGENARY_CYCLE = make("SEXAGENARY_CYCLE", CALENDAR_TERMS);
   public static final BasicCategories HOUSING = make("HOUSING", BUILDINGS);
   public static final BasicCategories WINES = make("WINES", ALCOHOLIC_BEVERAGES);
   public static final BasicCategories GEMS = make("GEMS", JEWELRY);
   public static final BasicCategories INTERNET = make("INTERNET", NETWORKING);
   public static final BasicCategories VIDEO_GAMES = make("VIDEO_GAMES", SOFTWARE);
   public static final BasicCategories AUTOMOBILES = make("AUTOMOBILES", VEHICLES);
   public static final BasicCategories WATERCRAFT = make("WATERCRAFT", VEHICLES);
   public static final BasicCategories CYCLING = make("CYCLING", VEHICLES);
   public static final BasicCategories NAUTICAL = make("NAUTICAL", VEHICLES);
   public static final BasicCategories ARTHROPODS = make("ARTHROPODS", INVERTEBRATES);
   public static final BasicCategories MOLLUSKS = make("MOLLUSKS", INVERTEBRATES);
   public static final BasicCategories WORMS = make("WORMS", INVERTEBRATES);
   public static final BasicCategories AMPHIBIANS = make("AMPHIBIANS", VERTEBRATES);
   public static final BasicCategories BIRDS = make("BIRDS", VERTEBRATES);
   public static final BasicCategories FISH = make("FISH", VERTEBRATES);
   public static final BasicCategories MAMMALS = make("MAMMALS", VERTEBRATES);
   public static final BasicCategories REPTILES = make("REPTILES", VERTEBRATES);
   public static final BasicCategories MALE = make("MALE", GENDER);
   public static final BasicCategories SEED_BEARING_PLANTS = make("SEED_BEARING_PLANTS", VASCULAR_PLANTS);
   public static final BasicCategories SPORE_BEARING_PLANTS = make("SPORE_BEARING_PLANTS", VASCULAR_PLANTS);
   public static final BasicCategories WOODY_PLANTS = make("WOODY_PLANTS", VASCULAR_PLANTS);
   public static final BasicCategories ALGAE = make("ALGAE", NON_VASCULAR_PLANTS);
   public static final BasicCategories BODIES_OF_WATER = make("BODIES_OF_WATER", LANDFORMS);
   public static final BasicCategories CONTINENTS = make("CONTINENTS", LANDFORMS);
   public static final BasicCategories ISLANDS = make("ISLANDS", LANDFORMS);
   public static final BasicCategories ATMOSPHERE = make("ATMOSPHERE", PLANETS);
   public static final BasicCategories CAPITAL_CITIES = make("CAPITAL_CITIES", CITIES);
   public static final BasicCategories LETTERS_SYMBOLS_AND_PUNCTUATION = make("LETTERS_SYMBOLS_AND_PUNCTUATION",
                                                                              ORTHOGRAPHY);
   public static final BasicCategories BOOKS = make("BOOKS", LITERATURE);
   public static final BasicCategories COMICS = make("COMICS", LITERATURE);
   public static final BasicCategories FICTION = make("FICTION", LITERATURE);
   public static final BasicCategories HORROR = make("HORROR", LITERATURE);
   public static final BasicCategories LITERARY_GENRES = make("LITERARY_GENRES", LITERATURE);
   public static final BasicCategories BOARD_GAMES = make("BOARD_GAMES", GAMES);
   public static final BasicCategories CARD_GAMES = make("CARD_GAMES", GAMES);
   public static final BasicCategories GAMBLING = make("GAMBLING", GAMES);
   public static final BasicCategories FOOTBALL = make("FOOTBALL", BALL_GAMES);
   public static final BasicCategories RUGBY = make("RUGBY", BALL_GAMES);
   public static final BasicCategories COLORS = make("COLORS", VISION);
   public static final BasicCategories BODY = make("BODY", ANATOMY);
   public static final BasicCategories HORTICULTURE = make("HORTICULTURE", BOTANY);
   public static final BasicCategories MEDICAL_GENETICS = make("MEDICAL_GENETICS", GENETICS);
   public static final BasicCategories NEUROLOGY = make("NEUROLOGY", NEUROSCIENCE);
   public static final BasicCategories TAXONOMY = make("TAXONOMY", SYSTEMATICS);
   public static final BasicCategories ANTHROPOLOGY = make("ANTHROPOLOGY", ZOOLOGY);
   public static final BasicCategories ARTHROPODOLOGY = make("ARTHROPODOLOGY", ZOOLOGY);
   public static final BasicCategories MALACOLOGY = make("MALACOLOGY", ZOOLOGY);
   public static final BasicCategories BIOMOLECULES = make("BIOMOLECULES", BIOCHEMISTRY);
   public static final BasicCategories PHARMACOLOGY = make("PHARMACOLOGY", BIOCHEMISTRY);
   public static final BasicCategories LIGHT = make("LIGHT", ENERGY);
   public static final BasicCategories SOUND = make("SOUND", ENERGY);
   public static final BasicCategories INFORMATION_THEORY = make("INFORMATION_THEORY", APPLIED_MATHEMATICS);
   public static final BasicCategories HTML = make("HTML", PROGRAMMING);
   public static final BasicCategories DISEASE = make("DISEASE", DISEASES);
   public static final BasicCategories DIVINATION = make("DIVINATION", OCCULT);
   public static final BasicCategories ADMINISTRATION = make("ADMINISTRATION", BUSINESS);
   public static final BasicCategories BUSINESSES = make("BUSINESSES", BUSINESS);
   public static final BasicCategories FINANCE = make("FINANCE", BUSINESS);
   public static final BasicCategories INDUSTRIES = make("INDUSTRIES", BUSINESS);
   public static final BasicCategories MONEY = make("MONEY", BUSINESS);
   public static final BasicCategories TRADING = make("TRADING", BUSINESS);
   public static final BasicCategories PARTS_OF_SPEECH = make("PARTS_OF_SPEECH", GRAMMAR);
   public static final BasicCategories DRAMA = make("DRAMA", THEATER);
   public static final BasicCategories PLOT_DEVICES = make("PLOT_DEVICES", NARRATOLOGY);
   public static final BasicCategories ANCIENT_NEAR_EAST = make("ANCIENT_NEAR_EAST", HISTORY);
   public static final BasicCategories HERALDRY = make("HERALDRY", HISTORY);
   public static final BasicCategories LOGIC = make("LOGIC", PHILOSOPHY);
   public static final BasicCategories ETHICS = make("ETHICS", PHILOSOPHY);
   public static final BasicCategories CHRISTIANITY = make("CHRISTIANITY", ABRAHAMISM);
   public static final BasicCategories FORMS_OF_GOVERNMENT = make("FORMS_OF_GOVERNMENT", GOVERNMENT);
   public static final BasicCategories POLITICS = make("POLITICS", GOVERNMENT);
   public static final BasicCategories SOCIAL_MEDIA = make("SOCIAL_MEDIA", INTERNET);
   public static final BasicCategories WORLD_WIDE_WEB = make("WORLD_WIDE_WEB", INTERNET);
   public static final BasicCategories ARACHNIDS = make("ARACHNIDS", ARTHROPODS);
   public static final BasicCategories CRUSTACEANS = make("CRUSTACEANS", ARTHROPODS);
   public static final BasicCategories INSECTS = make("INSECTS", ARTHROPODS);
   public static final BasicCategories CEPHALOPODS = make("CEPHALOPODS", MOLLUSKS);
   public static final BasicCategories GASTROPODS = make("GASTROPODS", MOLLUSKS);
   public static final BasicCategories SALAMANDERS = make("SALAMANDERS", AMPHIBIANS);
   public static final BasicCategories BIRDS_OF_PREY = make("BIRDS_OF_PREY", BIRDS);
   public static final BasicCategories FOWLS = make("FOWLS", BIRDS);
   public static final BasicCategories FRESHWATER_BIRDS = make("FRESHWATER_BIRDS", BIRDS);
   public static final BasicCategories PERCHING_BIRDS = make("PERCHING_BIRDS", BIRDS);
   public static final BasicCategories RATITES = make("RATITES", BIRDS);
   public static final BasicCategories SEABIRDS = make("SEABIRDS", BIRDS);
   public static final BasicCategories SHOREBIRDS = make("SHOREBIRDS", BIRDS);
   public static final BasicCategories LABROID_FISH = make("LABROID_FISH", FISH);
   public static final BasicCategories PERCOID_FISH = make("PERCOID_FISH", FISH);
   public static final BasicCategories CARNIVORES = make("CARNIVORES", MAMMALS);
   public static final BasicCategories EVENTOED_UNGULATES = make("EVENTOED_UNGULATES", MAMMALS);
   public static final BasicCategories LAGOMORPHS = make("LAGOMORPHS", MAMMALS);
   public static final BasicCategories ODDTOED_UNGULATES = make("ODDTOED_UNGULATES", MAMMALS);
   public static final BasicCategories PRIMATES = make("PRIMATES", MAMMALS);
   public static final BasicCategories RODENTS = make("RODENTS", MAMMALS);
   public static final BasicCategories LIZARDS = make("LIZARDS", REPTILES);
   public static final BasicCategories SNAKES = make("SNAKES", REPTILES);
   public static final BasicCategories ANGIOSPERMS = make("ANGIOSPERMS", SEED_BEARING_PLANTS);
   public static final BasicCategories GYMNOSPERMS = make("GYMNOSPERMS", SEED_BEARING_PLANTS);
   public static final BasicCategories SEAS = make("SEAS", BODIES_OF_WATER);
   public static final BasicCategories LETTER_NAMES = make("LETTER_NAMES", LETTERS_SYMBOLS_AND_PUNCTUATION);
   public static final BasicCategories AMERICAN_FICTION = make("AMERICAN_FICTION", FICTION);
   public static final BasicCategories BRITISH_FICTION = make("BRITISH_FICTION", FICTION);
   public static final BasicCategories FAIRY_TALE = make("FAIRY_TALE", FICTION);
   public static final BasicCategories FICTIONAL_CHARACTERS = make("FICTIONAL_CHARACTERS", FICTION);
   public static final BasicCategories FICTIONAL_LOCATIONS = make("FICTIONAL_LOCATIONS", FICTION);
   public static final BasicCategories SHAHNAMEH = make("SHAHNAMEH", FICTION);
   public static final BasicCategories CHESS = make("CHESS", BOARD_GAMES);
   public static final BasicCategories PIGMENTS = make("PIGMENTS", COLORS);
   public static final BasicCategories BODY_PARTS = make("BODY_PARTS", BODY);
   public static final BasicCategories DEATH = make("DEATH", BODY);
   public static final BasicCategories GAITS = make("GAITS", BODY);
   public static final BasicCategories HAIR = make("HAIR", BODY);
   public static final BasicCategories HEALTH = make("HEALTH", BODY);
   public static final BasicCategories MIND = make("MIND", BODY);
   public static final BasicCategories PREGNANCY = make("PREGNANCY", BODY);
   public static final BasicCategories PROTEINS = make("PROTEINS", BIOMOLECULES);
   public static final BasicCategories PUBLIC_ADMINISTRATION = make("PUBLIC_ADMINISTRATION", ADMINISTRATION);
   public static final BasicCategories PUBLISHING = make("PUBLISHING", INDUSTRIES);
   public static final BasicCategories CURRENCY = make("CURRENCY", MONEY);
   public static final BasicCategories CATHOLICISM = make("CATHOLICISM", CHRISTIANITY);
   public static final BasicCategories PROTESTANTISM = make("PROTESTANTISM", CHRISTIANITY);
   public static final BasicCategories DEMOCRACY = make("DEMOCRACY", FORMS_OF_GOVERNMENT);
   public static final BasicCategories UK_POLITICS = make("UK_POLITICS", POLITICS);
   public static final BasicCategories US_POLITICS = make("US_POLITICS", POLITICS);
   public static final BasicCategories BEETLES = make("BEETLES", INSECTS);
   public static final BasicCategories BUTTERFLIES = make("BUTTERFLIES", INSECTS);
   public static final BasicCategories COCKROACHES = make("COCKROACHES", INSECTS);
   public static final BasicCategories HYMENOPTERANS = make("HYMENOPTERANS", INSECTS);
   public static final BasicCategories MOTHS = make("MOTHS", INSECTS);
   public static final BasicCategories ANATIDS = make("ANATIDS", FRESHWATER_BIRDS);
   public static final BasicCategories CERTHIOID_BIRDS = make("CERTHIOID_BIRDS", PERCHING_BIRDS);
   public static final BasicCategories MELIPHAGOID_BIRDS = make("MELIPHAGOID_BIRDS", PERCHING_BIRDS);
   public static final BasicCategories WARBLERS = make("WARBLERS", PERCHING_BIRDS);
   public static final BasicCategories CANIDS = make("CANIDS", CARNIVORES);
   public static final BasicCategories FELIDS = make("FELIDS", CARNIVORES);
   public static final BasicCategories PINNIPEDS = make("PINNIPEDS", CARNIVORES);
   public static final BasicCategories ANTELOPES = make("ANTELOPES", EVENTOED_UNGULATES);
   public static final BasicCategories CERVIDS = make("CERVIDS", EVENTOED_UNGULATES);
   public static final BasicCategories CETACEANS = make("CETACEANS", EVENTOED_UNGULATES);
   public static final BasicCategories EQUIDS = make("EQUIDS", ODDTOED_UNGULATES);
   public static final BasicCategories HOMINIDS = make("HOMINIDS", PRIMATES);
   public static final BasicCategories MONKEYS = make("MONKEYS", PRIMATES);
   public static final BasicCategories ALISMATALES_ORDER_PLANTS = make("ALISMATALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories ANTHEMIDEAE_TRIBE_PLANTS = make("ANTHEMIDEAE_TRIBE_PLANTS", ANGIOSPERMS);
   public static final BasicCategories APIALES_ORDER_PLANTS = make("APIALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories ASPARAGALES_ORDER_PLANTS = make("ASPARAGALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories ASTERALES_ORDER_PLANTS = make("ASTERALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories BRASSICALES_ORDER_PLANTS = make("BRASSICALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories CARYOPHYLLALES_ORDER_PLANTS = make("CARYOPHYLLALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories COMMELINIDS = make("COMMELINIDS", ANGIOSPERMS);
   public static final BasicCategories ERICALES_ORDER_PLANTS = make("ERICALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories FAGALES_ORDER_PLANTS = make("FAGALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories GENTIANALES_ORDER_PLANTS = make("GENTIANALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories LAMIALES_ORDER_PLANTS = make("LAMIALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories LEGUMES = make("LEGUMES", ANGIOSPERMS);
   public static final BasicCategories LILIALES_ORDER_PLANTS = make("LILIALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories MAGNOLIIDS = make("MAGNOLIIDS", ANGIOSPERMS);
   public static final BasicCategories MALPIGHIALES_ORDER_PLANTS = make("MALPIGHIALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories MALVALES_ORDER_PLANTS = make("MALVALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories MYRTALES_ORDER_PLANTS = make("MYRTALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories NIGHTSHADES = make("NIGHTSHADES", ANGIOSPERMS);
   public static final BasicCategories RANUNCULALES_ORDER_PLANTS = make("RANUNCULALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories ROSALES_ORDER_PLANTS = make("ROSALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories SAPINDALES_ORDER_PLANTS = make("SAPINDALES_ORDER_PLANTS", ANGIOSPERMS);
   public static final BasicCategories CONIFERS = make("CONIFERS", GYMNOSPERMS);
   public static final BasicCategories TISSUES = make("TISSUES", BODY_PARTS);
   public static final BasicCategories ENZYMES = make("ENZYMES", PROTEINS);
   public static final BasicCategories PUBLIC_SAFETY = make("PUBLIC_SAFETY", PUBLIC_ADMINISTRATION);
   public static final BasicCategories DOGS = make("DOGS", CANIDS);
   public static final BasicCategories CATS = make("CATS", FELIDS);
   public static final BasicCategories HUMAN = make("HUMAN", HOMINIDS);
   public static final BasicCategories CELERY_FAMILY_PLANTS = make("CELERY_FAMILY_PLANTS", APIALES_ORDER_PLANTS);
   public static final BasicCategories SUCCULENTS = make("SUCCULENTS", ASPARAGALES_ORDER_PLANTS);
   public static final BasicCategories CRUCIFERS = make("CRUCIFERS", BRASSICALES_ORDER_PLANTS);
   public static final BasicCategories GRASSES = make("GRASSES", COMMELINIDS);
   public static final BasicCategories ZINGIBERALES_ORDER_PLANTS = make("ZINGIBERALES_ORDER_PLANTS", COMMELINIDS);
   public static final BasicCategories BEECH_FAMILY_PLANTS = make("BEECH_FAMILY_PLANTS", FAGALES_ORDER_PLANTS);
   public static final BasicCategories MINT_FAMILY_PLANTS = make("MINT_FAMILY_PLANTS", LAMIALES_ORDER_PLANTS);
   public static final BasicCategories MALLOW_FAMILY_PLANTS = make("MALLOW_FAMILY_PLANTS", MALVALES_ORDER_PLANTS);
   public static final BasicCategories MYRTLE_FAMILY_PLANTS = make("MYRTLE_FAMILY_PLANTS", MYRTALES_ORDER_PLANTS);
   public static final BasicCategories POME_FRUITS = make("POME_FRUITS", ROSALES_ORDER_PLANTS);
   public static final BasicCategories STONE_FRUITS = make("STONE_FRUITS", ROSALES_ORDER_PLANTS);
   public static final BasicCategories RUE_FAMILY_PLANTS = make("RUE_FAMILY_PLANTS", SAPINDALES_ORDER_PLANTS);
   public static final BasicCategories CRIME_PREVENTION = make("CRIME_PREVENTION", PUBLIC_SAFETY);
   public static final BasicCategories EMERGENCY_SERVICES = make("EMERGENCY_SERVICES", PUBLIC_SAFETY);
   public static final BasicCategories FEMALE_PEOPLE = make("FEMALE_PEOPLE", HUMAN);
   public static final BasicCategories MALE_PEOPLE = make("MALE_PEOPLE", HUMAN);
   public static final BasicCategories CHILDREN = make("CHILDREN", HUMAN);
   public static final BasicCategories FAMILY = make("FAMILY", HUMAN);
   public static final BasicCategories OCCUPATIONS = make("OCCUPATIONS", HUMAN);
   public static final BasicCategories GROUPS_OF_PEOPLE = make("GROUPS_OF_PEOPLE", HUMAN);
   public static final BasicCategories GRAINS = make("GRAINS", GRASSES);
   public static final BasicCategories LAW_ENFORCEMENT = make("LAW_ENFORCEMENT", CRIME_PREVENTION);
   public static final BasicCategories FEMALE_OCCUPATIONS = make("FEMALE_OCCUPATIONS", FEMALE_PEOPLE);
   public static final BasicCategories FEMALE_NATIONALITIES = make("FEMALE_NATIONALITIES", FEMALE_PEOPLE);
   public static final BasicCategories MALE_OCCUPATIONS = make("MALE_OCCUPATIONS", MALE_PEOPLE);
   public static final BasicCategories MALE_NATIONALITIES = make("MALE_NATIONALITIES", MALE_PEOPLE);
   public static final BasicCategories FAMILY_MEMBERS = make("FAMILY_MEMBERS", FAMILY);
   public static final BasicCategories ARTISTS = make("ARTISTS", OCCUPATIONS);
   public static final BasicCategories ATHLETES = make("ATHLETES", OCCUPATIONS);
   public static final BasicCategories AUTHORS = make("AUTHORS", OCCUPATIONS);
   public static final BasicCategories HEADS_OF_STATE = make("HEADS_OF_STATE", OCCUPATIONS);
   public static final BasicCategories HEALTHCARE_OCCUPATIONS = make("HEALTHCARE_OCCUPATIONS", OCCUPATIONS);
   public static final BasicCategories MUSICIANS = make("MUSICIANS", OCCUPATIONS);
   public static final BasicCategories SCIENTISTS = make("SCIENTISTS", OCCUPATIONS);
   public static final BasicCategories ETHNIC_GROUPS = make("ETHNIC_GROUPS", GROUPS_OF_PEOPLE);
   public static final BasicCategories NATIONALITIES = make("NATIONALITIES", GROUPS_OF_PEOPLE);
   public static final BasicCategories TRIBES = make("TRIBES", NATIONALITIES);
   private final Lazy<Set<BasicCategories>> ancestors;
   private BasicCategories[] parents;

   private BasicCategories(String name) {
      super(name);
      this.ancestors = new Lazy<>(() -> {
         Set<BasicCategories> ancestors = new HashSet<>();
         Queue<BasicCategories> queue = Lists.newLinkedList(Arrays.asList(parents));
         while(queue.size() > 0) {
            BasicCategories bc = queue.remove();
            if(ancestors.contains(bc)) {
               continue;
            }
            ancestors.add(bc);
            queue.addAll(Arrays.asList(bc.parents));
         }
         return Collections.unmodifiableSet(ancestors);
      });
   }


   public static BasicCategories valueOf(String name){
      return registry.valueOf(name);
   }

   private static BasicCategories make(String name, BasicCategories... parents) {
      BasicCategories basicCategories = registry.make(name);
      if(parents == null) {
         basicCategories.parents = new BasicCategories[0];
      } else {
         basicCategories.parents = parents;
      }
      return basicCategories;
   }

    public static BasicCategories make(String name) {
      return registry.valueOf(name);
   }

   public Set<BasicCategories> ancestors() {
      return ancestors.get();
   }

   @Override
   protected Registry<BasicCategories> registry() {
      return registry;
   }

   public boolean isPerson() {
      return isInstance(HUMAN);
   }

   public boolean isAnimal() {
      return isInstance(ANIMALS);
   }

   public boolean isLivingThing() {
      return isInstance(LIVING_THING);
   }

   public boolean isEmotion() {
      return isInstance(EMOTIONS);
   }

   public boolean isVehicle() {
      return isInstance(VEHICLES);
   }

   public boolean isOrganization() {
      return isInstance(ORGANIZATIONS);
   }

   @Override
   public boolean isInstance(Tag tag) {
      if(!(tag instanceof BasicCategories)) {
         return false;
      }
      if(this == tag || tag == ROOT) {
         return true;
      }
      if(parents.length == 0) {
         return false;
      }
      for(BasicCategories parent : parents) {
         if(parent.isInstance(tag)) {
            return true;
         }
      }
      return false;
   }

}//END OF BC
