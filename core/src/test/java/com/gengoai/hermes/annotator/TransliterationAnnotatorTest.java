package com.gengoai.hermes.annotator;

import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class TransliterationAnnotatorTest {

  @Test
  public void testAnnotate() throws Exception {
    Config.initializeTest();
    Document document = DocumentProvider.getChineseDocument();
    DefaultTransliterationAnnotator annotator = new DefaultTransliterationAnnotator();
    annotator.annotate(document);
    List<Annotation> tokens = document.tokens();
    assertEquals("wǒ", tokens.get(0).attribute(Types.TRANSLITERATION));
    assertEquals("ài", tokens.get(1).attribute(Types.TRANSLITERATION));
    assertEquals("nǐ", tokens.get(2).attribute(Types.TRANSLITERATION));
  }
}