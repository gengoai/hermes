/*
 * (c) 2005 David B. Bracewell
 *
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

package com.gengoai.hermes.annotator;

import com.gengoai.Language;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.ibm.icu.text.Transliterator;

import java.util.Collections;
import java.util.Set;

/**
 * Annotates tokens with their transliteration using ICU4Js Transliterator class.
 *
 * @author David B. Bracewell
 */
public class DefaultTransliterationAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;

   @Override
   protected void annotateImpl(Document document) {
      String id = getId(document.getLanguage());
      if(id != null) {
         Transliterator transliterator = Transliterator.getInstance(id);
         document.tokens()
                 .forEach(token -> token.put(Types.TRANSLITERATION, transliterator.transform(token.toString())));
      }
   }

   private String getId(Language language) {
      switch(language) {
         case CHINESE:
            return "Han-Latin";
         case ARABIC:
            return "Arabic-Latin";
         case ARMENIAN:
            return "Armenian-Latin";
         case AZERBAIJANI:
            return "Azerbaijani-Latin/BGN";
         case AMHARIC:
            return "Amharic-Latin/BGN";
         case BENGALI:
            return "Bengali-Latin";
         case BULGARIAN:
            return "Bulgarian-Latin/BGN";
         case GEORGIAN:
            return "Georgian-Latin";
         case GREEK:
            return "Greek-Latin";
         case KOREAN:
            return "Hangul-Latin";
         case HEBREW:
            return "Hebrew-Latin";
         case KANNADA:
            return "Kannada-Latin";
         case KAZAKH:
            return "Kazakh-Latin/BGN";
         case THAI:
            return "Thai-Latin";
         case TELUGU:
            return "Telugu-Latin";
         case TAMIL:
            return "Tamil-Latin";
         case UKRAINIAN:
            return "Ukrainian-Latin/BGN";
         case RUSSIAN:
            return "Russian-Latin/BGN";
      }
      return null;
   }

   @Override
   public String getProvider(Language language) {
      return "ICU4J";
   }

   @Override
   public Set<AnnotatableType> requires() {
      return Collections.singleton(Types.TOKEN);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.TRANSLITERATION);
   }

}//END OF DefaultTransliterationAnnotator
