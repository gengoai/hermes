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

package com.gengoai.hermes;

import com.gengoai.graph.Edge;

import java.util.Objects;

/**
 * A specialized annotation graph edge that stores relation type and value.
 *
 * @author David B. Bracewell
 */
public class RelationEdge extends Edge<Annotation> {
   private static final long serialVersionUID = 1L;
   private String relation;
   private RelationType relationType;

   /**
    * Instantiates a new Relation edge.
    *
    * @param source the source vertex
    * @param target the target vertex
    */
   public RelationEdge(Annotation source, Annotation target) {
      super(source, target);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof RelationEdge)) return false;
      RelationEdge that = (RelationEdge) o;
      return Objects.equals(relation, that.relation) &&
                Objects.equals(relationType, that.relationType) &&
                Objects.equals(getFirstVertex(), that.getFirstVertex()) &&
                Objects.equals(getSecondVertex(), that.getSecondVertex());
   }

   /**
    * Gets relation.
    *
    * @return the relation
    */
   public String getRelation() {
      return relation;
   }

   /**
    * Sets relation.
    *
    * @param relation the relation
    */
   public void setRelation(String relation) {
      this.relation = relation;
   }

   /**
    * Gets relation type.
    *
    * @return the relation type
    */
   public RelationType getRelationType() {
      return relationType;
   }

   /**
    * Sets relation type.
    *
    * @param relationType the relation type
    */
   public void setRelationType(RelationType relationType) {
      this.relationType = relationType;
   }

   @Override
   public int hashCode() {
      return Objects.hash(relation, relationType, getFirstVertex(), getSecondVertex());
   }

   @Override
   public boolean isDirected() {
      return true;
   }

   @Override
   public String toString() {
      return "RelationEdge{" +
                "vertex1=" + vertex1 +
                ", vertex2=" + vertex2 +
                ", relation='" + relation + '\'' +
                ", relationType=" + relationType +
                '}';
   }
}//END OF RelationEdge
