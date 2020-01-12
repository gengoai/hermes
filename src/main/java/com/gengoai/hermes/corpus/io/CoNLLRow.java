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

package com.gengoai.hermes.corpus.io;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A Row (token) in a CoNLL formatted file
 *
 * @author David B. Bracewell
 */
public final class CoNLLRow implements Serializable {
   private static final long serialVersionUID = 1L;
   private long annotationID = -1L;
   private String depRelation = null;
   private int end = -1;
   private int index = -1;
   private Map<String, String> otherProperties = new HashMap<>(3);
   private int parent = -1;
   private String pos = null;
   private int sentence = -1;
   private int start = -1;
   private String word;


   /**
    * Adds a field not specifically tracked.
    *
    * @param name  the name of the field
    * @param value the value of the field
    */
   public void addOther(String name, String value) {
      otherProperties.put(name.toUpperCase(), value);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof CoNLLRow)) return false;
      CoNLLRow coNLLRow = (CoNLLRow) o;
      return annotationID == coNLLRow.annotationID &&
                end == coNLLRow.end &&
                index == coNLLRow.index &&
                parent == coNLLRow.parent &&
                sentence == coNLLRow.sentence &&
                start == coNLLRow.start &&
                Objects.equals(depRelation, coNLLRow.depRelation) &&
                Objects.equals(otherProperties, coNLLRow.otherProperties) &&
                Objects.equals(pos, coNLLRow.pos) &&
                Objects.equals(word, coNLLRow.word);
   }

   /**
    * Gets the annotation id
    *
    * @return the annotation id
    */
   public long getAnnotationID() {
      return this.annotationID;
   }

   /**
    * Sets the annotation id.
    *
    * @param annotationID the annotation id
    */
   public void setAnnotationID(long annotationID) {
      this.annotationID = annotationID;
   }

   /**
    * Gets the dependency relation.
    *
    * @return the dep relation
    */
   public String getDepRelation() {
      return this.depRelation;
   }

   /**
    * Sets dep relation.
    *
    * @param depRelation the dep relation
    */
   public void setDepRelation(String depRelation) {
      this.depRelation = depRelation;
   }

   /**
    * Gets the end offset.
    *
    * @return the end
    */
   public int getEnd() {
      return this.end;
   }

   /**
    * Sets the end offset.
    *
    * @param end the end
    */
   public void setEnd(int end) {
      this.end = end;
   }

   /**
    * Gets the index of the token.
    *
    * @return the index
    */
   public int getIndex() {
      return this.index;
   }

   /**
    * Sets the index.
    *
    * @param index the index
    */
   public void setIndex(int index) {
      this.index = index;
   }

   /**
    * Gets a non-specific field with the given name.
    *
    * @param name the name
    * @return the value
    */
   public String getOther(String name) {
      return otherProperties.get(name.toUpperCase());
   }

   /**
    * Gets the parent index.
    *
    * @return the parent
    */
   public int getParent() {
      return this.parent;
   }

   /**
    * Sets the parent index.
    *
    * @param parent the parent
    */
   public void setParent(int parent) {
      this.parent = parent;
   }

   /**
    * Gets that part-of-speech.
    *
    * @return the part-of-speech
    */
   public String getPos() {
      return this.pos;
   }

   /**
    * Sets the part-of-speech.
    *
    * @param pos the part-of-speech
    */
   public void setPos(String pos) {
      this.pos = pos;
   }

   /**
    * Gets the sentence index.
    *
    * @return the sentence index
    */
   public int getSentence() {
      return this.sentence;
   }

   /**
    * Sets  sentence index.
    *
    * @param sentence the  sentence index
    */
   public void setSentence(int sentence) {
      this.sentence = sentence;
   }

   /**
    * Gets start offset.
    *
    * @return the start offset
    */
   public int getStart() {
      return this.start;
   }

   /**
    * Sets the start offset.
    *
    * @param start the start offset
    */
   public void setStart(int start) {
      this.start = start;
   }

   /**
    * Gets the word (token).
    *
    * @return the word (token).
    */
   public String getWord() {
      return this.word;
   }

   /**
    * Sets word (token).
    *
    * @param word the word (token).
    */
   public void setWord(String word) {
      this.word = word;
   }

   /**
    * Checks if a value for the given non-specified property name exists
    *
    * @param name the name
    * @return True exists, False otherwise
    */
   public boolean hasOther(String name) {
      return otherProperties.containsKey(name.toUpperCase());
   }

   @Override
   public int hashCode() {
      return Objects.hash(annotationID, depRelation, end, index, otherProperties, parent, pos, sentence, start, word);
   }

   @Override
   public String toString() {
      return "CoNLLRow{" +
                "annotationID=" + annotationID +
                ", depRelation='" + depRelation + '\'' +
                ", end=" + end +
                ", index=" + index +
                ", otherProperties=" + otherProperties +
                ", parent=" + parent +
                ", pos='" + pos + '\'' +
                ", sentence=" + sentence +
                ", start=" + start +
                ", word='" + word + '\'' +
                '}';
   }
}//END OF CoNLLRow
