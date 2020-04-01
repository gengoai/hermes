package com.gengoai.hermes.preprocessing;

import com.gengoai.Language;
import com.gengoai.config.Config;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class TraditionalToSimplifiedTest {

   @Test
   public void testPerformNormalization() throws Exception {
      Config.initializeTest();
      Document document = DocumentFactory.builder()
                                         .normalizer(new TraditionalToSimplified())
                                         .defaultLanguage(Language.CHINESE)
                                         .build()
                                         .create("電腦是新的。");
      assertEquals("电脑是新的。", document.toString());
   }
}