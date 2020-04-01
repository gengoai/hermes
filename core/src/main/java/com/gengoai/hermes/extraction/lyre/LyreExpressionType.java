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

package com.gengoai.hermes.extraction.lyre;

import com.gengoai.Tag;
import lombok.NonNull;

import java.util.Collection;

public enum LyreExpressionType implements Tag {
   PREDICATE {
      @Override
      protected LyreExpressionType selectMostCommon(LyreExpressionType other) {
         return other.isInstance(PREDICATE) ? PREDICATE : OBJECT;
      }
   },
   HSTRING {
      @Override
      protected LyreExpressionType selectMostCommon(LyreExpressionType other) {
         switch (other) {
            case HSTRING:
               return HSTRING;
            case STRING:
               return STRING;
            default:
               return OBJECT;
         }
      }
   },
   STRING {
      @Override
      protected LyreExpressionType selectMostCommon(LyreExpressionType other) {
         switch (other) {
            case HSTRING:
            case STRING:
            case OBJECT:
               return STRING;
            default:
               return OBJECT;
         }
      }
   },
   FEATURE {
      @Override
      protected LyreExpressionType selectMostCommon(LyreExpressionType other) {
         return other.isInstance(FEATURE) ? FEATURE : OBJECT;
      }
   },
   OBJECT {
      @Override
      protected LyreExpressionType selectMostCommon(LyreExpressionType other) {
         return OBJECT;
      }
   },
   NUMERIC {
      @Override
      protected LyreExpressionType selectMostCommon(LyreExpressionType other) {
         return other.isInstance(NUMERIC) ? NUMERIC : OBJECT;
      }
   },
   COUNTER {
      @Override
      protected LyreExpressionType selectMostCommon(LyreExpressionType other) {
         return other.isInstance(COUNTER) ? COUNTER : OBJECT;
      }
   };

   protected abstract LyreExpressionType selectMostCommon(LyreExpressionType other);

   public static LyreExpressionType determineCommonType(@NonNull Collection<LyreExpression> expressions) {
      LyreExpressionType bestType = null;
      for (LyreExpression expression : expressions) {
         if (bestType == null) {
            bestType = expression.getType();
         } else {
            bestType = bestType.selectMostCommon(expression.getType());
         }
         if (bestType == OBJECT) {
            return OBJECT;
         }
      }
      return bestType == null ? OBJECT : bestType;
   }


}//END OF LyreExpressionType
