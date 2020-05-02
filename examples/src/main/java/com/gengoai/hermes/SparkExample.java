package com.gengoai.hermes;

import com.gengoai.collection.counter.Counter;
import com.gengoai.config.Config;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.TermExtractor;
import com.gengoai.hermes.tools.HermesCLI;

import java.io.Serializable;

public class SparkExample extends HermesCLI implements Serializable {
   private static final long serialVersionUID = 1L;

   public static void main(String[] args) throws Exception {
      new SparkExample().run(args);
   }

   @Override
   public void programLogic() throws Exception {
      //Need to add the spark core jar file to the classpath for this to run
      //We will run it local, so we set the spark master to local[*]
      Config.setProperty("spark.master", "spark://192.168.1.64:7077");

      //Build the corpus
      //You can substitute the file for one you have. Here I am using a 1,000,000 sentence corpus from news articles with
      // one sentence (treated as a document) per line.
      DocumentCollection corpus = DocumentCollection.create(
            "text_opl::/data/corpora/en/Raw/news_1m_sentences.txt;distributed=true")
                                                    .repartition(100)
                                                    .annotate(Types.TOKEN);

      //Calculate term frequencies for the corpus. Note we are saying we want lemmatized versions, but have not
      //run the lemma annotator, instead it will just return the lowercase version of the content.
      Counter<String> counts = corpus.termCount(TermExtractor.builder().toLemma().build());
      counts.entries().forEach(entry -> System.out.println(entry.getKey() + " => " + entry.getValue()));
   }

}
