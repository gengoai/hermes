package com.gengoai.hermes.lexicon;

import com.gengoai.StringTag;
import com.gengoai.collection.Lists;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Fragments;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.Entities;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class LexiconTest {


   @Test
   public void test() {
      Config.initializeTest();
      TrieLexicon lexicon = Cast.as(TrieLexicon.builder(Types.TAG, true)
                                               .add("test") //Add an entry with no tag and no probability
                                               .add("testing", 0.8, new StringTag("TEST"))
                                               .add("bark", 0.8)
                                               .add("barking", new StringTag("TEST"))
                                               .add("barking skills", new StringTag("TEST"))
                                               .build());


      Document document = Document.create("The dog was testing his barking skills on the wall.");
      document.annotate(Types.TOKEN);
      assertEquals(Lists.arrayListOf("testing", "barking skills"),
                   Lists.asArrayList(lexicon.extract(document).string()));

      //Items in the lexicon
      assertTrue(lexicon.test(Fragments.stringWrapper("test")));
      assertTrue(lexicon.test(Fragments.stringWrapper("testing")));
      assertTrue(lexicon.test(Fragments.stringWrapper("bark")));
      assertTrue(lexicon.test(Fragments.stringWrapper("barking")));

      //Items not in the lexicon
      assertFalse(lexicon.test(Fragments.stringWrapper("BARK")));
      assertFalse(lexicon.test(Fragments.stringWrapper("missing")));

      //Tags
      assertEquals(new StringTag("TEST"), lexicon.getTag("testing").get());
      assertEquals(new StringTag("TEST"), lexicon.getTag("barking").get());

      //No Tags
      assertFalse(lexicon.getTag("test").isPresent());
      assertFalse(lexicon.getTag("bark").isPresent());
      assertFalse(lexicon.getTag("missing").isPresent());

      //Words that exist
      assertEquals(1.0d, lexicon.getProbability("test"), 0d);
      assertEquals(0.8d, lexicon.getProbability("testing"), 0d);
      assertEquals(0.8d, lexicon.getProbability("testing", new StringTag("TEST")), 0d);
      assertEquals(0.8d, lexicon.getProbability("bark"), 0d);
      assertEquals(1.0d, lexicon.getProbability("barking"), 0d);

      //Words that are missing
      assertEquals(0d, lexicon.getProbability("missing"), 0d);
      assertEquals(0d, lexicon.getProbability("test", Entities.DATE), 0d);


   }


}