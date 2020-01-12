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
 *
 */

package com.gengoai.hermes.corpus;

import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.QueryBuilder;

import java.io.Serializable;

import static com.gengoai.hermes.corpus.LuceneCorpus.CONTENT_FIELD;
import static com.gengoai.hermes.corpus.LuceneCorpus.ID_FIELD;


/**
 * Defines the methodology for matching documents based on simple boolean logic over term and document level
 * attributes.
 *
 * @author David B. Bracewell
 */
public abstract class Query implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Tests if the document matches the query
    *
    * @param document the document
    * @return True if the test passes, False if not
    */
   public abstract boolean matches(Document document);

   /**
    * Converts the Query into a Lucene Search Query
    *
    * @return the Lucene search Query
    */
   public abstract org.apache.lucene.search.Query toLucene();

   static class And extends Query {
      private static final long serialVersionUID = 1L;
      private final Query q1;
      private final Query q2;

      And(Query q1, Query q2) {
         this.q1 = q1;
         this.q2 = q2;
      }

      @Override
      public String toString() {
         return String.format("(%s AND %s)", q1, q2);
      }

      @Override
      public boolean matches(Document document) {
         return q1.matches(document) && q2.matches(document);
      }

      @Override
      public org.apache.lucene.search.Query toLucene() {
         BooleanQuery.Builder builder = new BooleanQuery.Builder();
         if (q1 instanceof Not) {
            builder.add(((Not) q1).query.toLucene(), BooleanClause.Occur.MUST_NOT);
         } else {
            builder.add(q1.toLucene(), BooleanClause.Occur.MUST);
         }
         if (q2 instanceof Not) {
            builder.add(((Not) q2).query.toLucene(), BooleanClause.Occur.MUST_NOT);
         } else {
            builder.add(q2.toLucene(), BooleanClause.Occur.MUST);
         }
         return builder.build();
      }
   }

   static class Not extends Query {
      private static final long serialVersionUID = 1L;
      final Query query;

      Not(Query query) {
         this.query = query;
      }


      @Override
      public String toString() {
         return String.format("-(%s)", query);
      }

      @Override
      public boolean matches(Document document) {
         return !query.matches(document);
      }

      @Override
      public org.apache.lucene.search.Query toLucene() {
         return new BooleanQuery.Builder()
            .add(query.toLucene(), BooleanClause.Occur.MUST_NOT)
            .add(new WildcardQuery(new Term(ID_FIELD, "*")), BooleanClause.Occur.SHOULD)
            .build();
      }
   }

   static class Or extends Query {
      private static final long serialVersionUID = 1L;
      private final Query q1;
      private final Query q2;

      /**
       * Instantiates a new Or.
       *
       * @param q1 the q 1
       * @param q2 the q 2
       */
      Or(Query q1, Query q2) {
         this.q1 = q1;
         this.q2 = q2;
      }

      @Override
      public String toString() {
         return String.format("(%s OR %s)", q1, q2);
      }

      @Override
      public boolean matches(Document document) {
         return q1.matches(document) || q2.matches(document);
      }

      @Override
      public org.apache.lucene.search.Query toLucene() {
         BooleanQuery.Builder builder = new BooleanQuery.Builder();
         builder.add(q1.toLucene(), BooleanClause.Occur.SHOULD);
         builder.add(q2.toLucene(), BooleanClause.Occur.SHOULD);
         return builder.build();
      }
   }

   static class PhraseQuery extends Query {
      private static final long serialVersionUID = 1L;
      private final String phrase;

      PhraseQuery(String phrase) {
         this.phrase = phrase;
      }

      @Override
      public String toString() {
         return String.format("'%s'", phrase);
      }

      @Override
      public boolean matches(Document document) {
         return document.contains(phrase);
      }

      @Override
      public org.apache.lucene.search.Query toLucene() {
         return new QueryBuilder(new StandardAnalyzer()).createPhraseQuery(CONTENT_FIELD, phrase);
      }
   }

   static class TermQuery extends Query {
      private static final long serialVersionUID = 1L;
      private final String field;
      private final Object targetValue;

      TermQuery(String field, Object targetValue) {
         this.field = field;
         this.targetValue = targetValue;
      }

      @Override
      public String toString() {
         switch (field) {
            case CONTENT_FIELD:
               return targetValue.toString();
            default:
               return String.format("$%s(%s)", field, targetValue);

         }
      }

      @Override
      public boolean matches(Document document) {
         switch (field) {
            case ID_FIELD:
               return document.getId().equals(targetValue);
            case CONTENT_FIELD:
               return document.contains(targetValue.toString());
            default:
               AttributeType<?> attributeType = Types.attribute(field);
               return document.attributeEquals(attributeType, targetValue);

         }
      }


      public org.apache.lucene.search.Query toLucene() {
         return new org.apache.lucene.search.TermQuery(new Term(field, targetValue.toString()));
      }

   }
}//END OF Query
