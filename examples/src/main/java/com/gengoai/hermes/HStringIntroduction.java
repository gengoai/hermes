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
import com.gengoai.hermes.tools.HermesCLI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HStringIntroduction extends HermesCLI {

   public static void main(String[] args) {
      new HStringIntroduction().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      HString hStr = Fragments.stringWrapper("This an HString with no document");

      //Our hStr is not annotation so isInstance will always return false
      System.out.println(hStr.isInstance(Types.SENTENCE));

      //We can do all the basic String-stuff
      System.out.println(hStr.toLowerCase());
      System.out.println(hStr.toUpperCase());
      System.out.println(hStr.indexOf(" with "));

      final Pattern pattern = Pattern.compile("(an|no|with|this)", Pattern.CASE_INSENSITIVE);
      Matcher m = pattern.matcher(hStr);
      while(m.find()) {
         System.out.print(m.group() + " ");
      }
      System.out.println();

      //We can use the convenience method and do the same
      m = hStr.matcher(pattern);
      while(m.find()) {
         System.out.print(m.group() + " ");
      }
      System.out.println();

      //Since we don't have an associated document, we don't have real tokens, but
      // because we used the Fragments#string(String) method the entire fragment is
      // a token.
      System.out.println(hStr.tokens());

      //We can add, retrieve, check for, and remove attributes
      hStr.put(Types.SOURCE, "My Source");
      System.out.println(hStr.attribute(Types.SOURCE));
      System.out.println(hStr.hasAttribute(Types.SOURCE));
      System.out.println(hStr.attributeEquals(Types.SOURCE, "My Source"));
      hStr.removeAttribute(Types.SOURCE);
      System.out.println(hStr.hasAttribute(Types.SOURCE));

      Document doc = Document.create("GengoAI is the best company in the world, but GengoAI is god.");
      int startAt = 0;
      HString mention;
      while(!(mention = doc.find("GengoAI", startAt)).isEmpty()) {
         doc.annotationBuilder(Types.ENTITY)
            .bounds(mention)
            .attribute(Types.ENTITY_TYPE, Entities.ORGANIZATION)
            .createAttached();
         startAt = mention.end();
      }
      for(Annotation annotation : doc.annotations(Types.ENTITY)) {
         System.out.println(annotation);
         Tag entityType = annotation.attribute(Types.TAG);
         System.out.println(entityType);
      }

   }

}//END OF HStringIntroduction
