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
import com.gengoai.Validation;
import com.gengoai.apollo.ml.Feature;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.Lists;
import com.gengoai.collection.Sets;
import com.gengoai.collection.Sorting;
import com.gengoai.collection.counter.Counters;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.*;
import com.gengoai.hermes.lexicon.LexiconManager;
import com.gengoai.hermes.lexicon.SimpleWordList;
import com.gengoai.hermes.lexicon.WordList;
import com.gengoai.hermes.ml.feature.ValueCalculator;
import com.gengoai.hermes.morphology.POS;
import com.gengoai.hermes.morphology.StopWords;
import com.gengoai.math.NumericComparison;
import com.gengoai.reflection.TypeUtils;
import com.gengoai.stream.Streams;
import com.gengoai.string.Re;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.hermes.HString.toHString;
import static com.gengoai.hermes.RelationDirection.OUTGOING;
import static com.gengoai.hermes.extraction.lyre.Lyre.process;
import static com.gengoai.hermes.extraction.lyre.Lyre.processPred;
import static com.gengoai.hermes.extraction.lyre.LyreExpressionType.*;
import static java.util.Comparator.comparingInt;

/**
 * Static functions allowing for a functional style DSL for constructing LyreExpressions.
 *
 * @author David B. Bracewell
 */
public final class LyreDSL {

   private static final Pattern FLAGS = Pattern.compile("^\\(\\?[a-z]+\\)", Pattern.CASE_INSENSITIVE);
   /**
    * Returns the current Object being processed.
    * Note that one-argument methods in Lyre (e.g. lower, isUpper, etc.) have an implied `$_` argument if none is given.
    * <pre>
    *    Usage:  $_
    * </pre>
    */
   public static final LyreExpression $_ = new LyreExpression("$_", HSTRING, SerializableFunction.identity());
   /**
    * Returns a constant false value
    * <pre>
    *    Usage:  false
    * </pre>
    */
   public static final LyreExpression FALSE = new LyreExpression("false", PREDICATE, o -> false);
   /**
    * Returns a constant true value for any object
    * <pre>
    *    Usage:  ~
    * </pre>
    */
   public static final LyreExpression MATCH_ALL = new LyreExpression("~", PREDICATE, o -> true);
   /**
    * Returns a constant NaN value
    * <pre>
    *    Usage:  NaN
    * </pre>
    */
   public static final LyreExpression NAN = new LyreExpression("NaN", NUMERIC, o -> Double.NaN);
   /**
    * Returns a constant Negative Infinity value
    * <pre>
    *    Usage:  -INF
    * </pre>
    */
   public static final LyreExpression NEGATIVE_INFINITY = new LyreExpression("-INF", NUMERIC,
                                                                             o -> Double.NEGATIVE_INFINITY);
   /**
    * Returns a constant null value.
    * <pre>
    *    Usage:  null
    * </pre>
    */
   public static final LyreExpression NULL = new LyreExpression("null", OBJECT, o -> null);
   /**
    * Returns a constant Positive Infinity value
    * <pre>
    *    Usage:  INF
    * </pre>
    */
   public static final LyreExpression POSITIVE_INFINITY = new LyreExpression("INF", NUMERIC,
                                                                             o -> Double.POSITIVE_INFINITY);
   /**
    * Returns a constant true value
    * <pre>
    *    Usage:  true
    * </pre>
    */
   public static final LyreExpression TRUE = new LyreExpression("true", PREDICATE, o -> true);
   /**
    * Converts a list of values into binary features over the current HString.
    * <pre>
    *    Usage: binary{'prefix'}(expression)
    *    e.g.: binary{'WORD'}(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: binary(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: binary{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: binary -- create bag-of-words binary features over the current HString
    * </pre>
    */
   public static final LyreExpression binary = feature(ValueCalculator.Binary);
   /**
    * Predicate checking if the current Object is null. CharSequences are also checked for being blank and Collections
    * are checked whether they are empty or not.
    * <pre>
    *    Usage:  isEmpty
    * </pre>
    */
   public static final LyreExpression exists = exists($_);
   /**
    * Converts a list of values into features with raw counts (i.e. frequency).
    * <pre>
    *    Usage: frequency{'prefix'}(expression)
    *    e.g.: frequency{'WORD'}(@TOKEN) -- create bag-of-words with raw counts features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: frequency(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: frequency{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: frequency -- create bag-of-words binary features over the current HString
    * </pre>
    */
   public static final LyreExpression frequency = feature(ValueCalculator.Frequency);
   /**
    * Predicate checking if the current HString contains a stop word
    * <pre>
    *    Usage:  hasStopWord
    * </pre>
    */
   public static final LyreExpression hasStopWord = hasStopWord($_);
   /**
    * Predicate checking if the current HString contains all alphanumeric characters
    * <pre>
    *    Usage:  isAlphaNumeric
    * </pre>
    */
   public static final LyreExpression isAlphaNumeric = isAlphaNumeric($_);
   /**
    * Predicate checking if the current HString is a content word
    * <pre>
    *    Usage:  isContentWord
    * </pre>
    */
   public static final LyreExpression isContentWord = isContentWord($_);
   /**
    * Predicate checking if the current HString is a digit
    * <pre>
    *    Usage:  isDigit
    * </pre>
    */
   public static final LyreExpression isDigit = isDigit($_);
   /**
    * Predicate checking if the current HString is all letters
    * <pre>
    *    Usage:  isLetter
    * </pre>
    */
   public static final LyreExpression isLetter = isLetter($_);
   /**
    * Predicate checking if the current HString is lower case
    * <pre>
    *    Usage:  isLower
    * </pre>
    */
   public static final LyreExpression isLower = isLower($_);
   /**
    * Predicate checking if the current HString is all punctuation
    * <pre>
    *    Usage:  isPunctuation
    * </pre>
    */
   public static final LyreExpression isPunctuation = isPunctuation($_);
   /**
    * Predicate checking if the current HString is a stop word
    * <pre>
    *    Usage:  isStopWord
    * </pre>
    */
   public static final LyreExpression isStopWord = isStopWord($_);
   /**
    * Predicate checking if the current HString is upper case
    * <pre>
    *    Usage:  isUpper
    * </pre>
    */
   public static final LyreExpression isUpper = isUpper($_);
   /**
    * Returns TRUE if the given object is whitespace
    * <pre>
    *    Usage:  isWhitespace
    * </pre>
    */
   public static final LyreExpression isWhitespace = isWhitespace($_);
   /**
    * Converts a list of values into features with L1-normalized counts.
    * <pre>
    *    Usage: L1{'prefix'}(expression)
    *    e.g.: L1{'WORD'}(@TOKEN) -- create bag-of-words with L1-normalized counts features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: L1(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: L1{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: L1 -- create bag-of-words binary features over the current HString
    * </pre>
    */
   public static final LyreExpression l1 = feature(ValueCalculator.L1);
   /**
    * Gets the lemmatized form of the current HString.
    * <pre>
    *    Usage:  lemma
    * </pre>
    */
   public static final LyreExpression lemma = lemma($_);
   /**
    * Determines the length of the current Object (characters if CharSequence, elements if Collection)
    * <pre>
    *    Usage:  len
    * </pre>
    */
   public static final LyreExpression len = len($_);
   /**
    * Determines the size of the current Object treating it as a list
    * <pre>
    *    Usage:  llen
    * </pre>
    */
   public static final LyreExpression llen = llen($_);
   /**
    * Gets the lower-cased string form of the current HString.
    * <pre>
    *    Usage:  lower
    * </pre>
    */
   public static final LyreExpression lower = lower($_);
   /**
    * Returns the part-of-speech of the current HString.
    * <pre>
    *    usage: pos
    * </pre>
    */
   public static final LyreExpression pos = pos($_);
   /**
    * Returns all SENTENCES of the given type overlapping with the current HString
    * <pre>
    *    Usage:  @ANNOTATION_TYPE
    *    e.g.: @SENTENCE
    * </pre>
    */
   public static final LyreExpression sentences = annotation(Types.SENTENCE, $_);
   /**
    * Gets the stemmed form of the current HString.
    * <pre>
    *    Usage:  stem
    * </pre>
    */
   public static final LyreExpression stem = stem($_);
   /**
    * Gets the string form of the current Object.
    * <pre>
    *    Usage:  string
    * </pre>
    */
   public static final LyreExpression string = string($_);
   /**
    * Determines the length of the current Object (characters if CharSequence, elements if Collection)
    * <pre>
    *    Usage:  len
    * </pre>
    */
   public static final LyreExpression tlen = tlen($_);
   /**
    * Returns all TOKENS of the given type overlapping with the current HString
    * <pre>
    *    Usage:  @ANNOTATION_TYPE
    *    e.g.: @TOKEN
    * </pre>
    */
   public static final LyreExpression tokens = annotation(Types.TOKEN, $_);
   /**
    * Trims stopwords from the current HString
    * <pre>
    *    Usage: trim()
    * </pre>
    */
   public static final LyreExpression trim = trim($_, isStopWord);
   /**
    * Returns the universal part-of-speech of the current HString.
    * <pre>
    *    usage: upos
    * </pre>
    */
   public static final LyreExpression upos = upos($_);
   /**
    * Gets the upper-cased string form of the current HString.
    * <pre>
    *    Usage:  upper
    * </pre>
    */
   public static final LyreExpression upper = upper($_);

   /**
    * Returns <b>true</b> if all items in the given list evaluates to *true* for the given predicate expression.
    * <pre>
    *    usage: all( list_expression, predicate_expression )
    *    e.g.: all( @TOKEN, #VERB ) -- returns true if all token on the current HString is a verb
    * </pre>
    *
    * @param list      the list of items to apply the predicate to
    * @param predicate the predicate to use for testing
    * @return the LyreExpression
    */
   public static LyreExpression all(@NonNull LyreExpression list, @NonNull LyreExpression predicate) {
      return new LyreExpression(formatMethod("all", list, predicate),
                                PREDICATE,
                                o -> list.applyAsList(o).stream().allMatch(predicate::testObject));
   }

   /**
    * Returns true when the left-hand and right-hand expressions evaluate to true
    * <pre>
    *    Usage:  expresion && expresion
    *    e.g.: isContentWord && len > 3 -- returns true if the current HString is a content word and has 3 or more characters
    * </pre>
    *
    * @param left  the left-hand expresion to evaluate as a predicate
    * @param right the right-hand expresion to evaluate as a predicate
    * @return the LyreExpression
    */
   public static LyreExpression and(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s && %s", left, right),
                                PREDICATE,
                                o -> left.testObject(o) && right.testObject(o));
   }

   /**
    * Sequentially processes each expression with the output of the previous expression or the input object for the
    * first expression.
    * <pre>
    *    Usage: expression1 &> expression2 &> ... &> expression
    * </pre>
    *
    * @param expressions The expressions to chain together.
    * @return the lyre expression
    */
   public static LyreExpression andPipe(@NonNull LyreExpression... expressions) {
      Validation.checkArgument(expressions.length > 1);
      return new LyreExpression(Stream.of(expressions)
                                      .map(Object::toString)
                                      .collect(Collectors.joining(" &> ")),
                                LyreExpressionType.determineCommonType(Arrays.asList(expressions)),
                                o -> {
                                   Object out = o;
                                   for(LyreExpression expression : expressions) {
                                      out = expression.applyAsObject(out);
                                   }
                                   return out;
                                });
   }

   /**
    * Returns all annotations of the given type overlapping with the current HString
    * <pre>
    *    Usage:  @ANNOTATION_TYPE
    *    e.g.: @PHRASE_CHUNK
    * </pre>
    *
    * @param type the annotation type
    * @return the LyreExpression
    */
   public static LyreExpression annotation(@NonNull AnnotationType type) {
      return annotation(type, $_);
   }

   /**
    * Returns all annotations of the given type overlapping with the current HString
    * <pre>
    *    Usage:  @ANNOTATION_TYPE
    *    e.g.: @PHRASE_CHUNK
    * </pre>
    *
    * @param type the annotation type
    * @param tag  the tag to filter the annotations on
    * @return the LyreExpression
    */
   public static LyreExpression annotation(@NonNull AnnotationType type, String tag) {
      return annotation(type, $_, tag);
   }

   /**
    * Returns all annotations of the given type overlapping with the HString resulting from the given expression.
    * <pre>
    *    Usage:  @ANNOTATION_TYPE(expression)
    *    e.g.: @TOKEN(@PHRASE_CHUNK) -- returns a list of tokens over the phrase chunks on the current HString
    * </pre>
    *
    * @param type       the annotation type
    * @param expression the expression returning the HString from which annotations will be extracted.
    * @return the LyreExpression
    */
   public static LyreExpression annotation(@NonNull AnnotationType type, @NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(STRING, HSTRING, OBJECT),
                               "Illegal Expression: annotation only accepts a STRING, HSTRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod(String.format("@%s", type), expression),
                                HSTRING,
                                o -> process(true, expression.applyAsObject(o),
                                             h -> toHString(h).annotations(type)));
   }

   /**
    * Returns all annotations of the given type overlapping with the HString resulting from the given expression.
    * <pre>
    *    Usage:  @ANNOTATION_TYPE(expression)
    *    e.g.: @TOKEN(@PHRASE_CHUNK) -- returns a list of tokens over the phrase chunks on the current HString
    * </pre>
    *
    * @param type       the annotation type
    * @param expression the expression returning the HString from which annotations will be extracted.
    * @param tag        the tag to filter the annotations on
    * @return the LyreExpression
    */
   public static LyreExpression annotation(@NonNull AnnotationType type,
                                           @NonNull LyreExpression expression,
                                           String tag) {
      Validation.checkArgument(expression.isInstance(STRING, HSTRING, OBJECT),
                               "Illegal Expression: annotation only accepts a STRING, HSTRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod(String.format("@%s", type), expression),
                                HSTRING,
                                o -> process(true, expression.applyAsObject(o),
                                             h -> {
                                                if(Strings.isNullOrBlank(tag)) {
                                                   return toHString(h).annotations(type);
                                                } else {
                                                   return toHString(h).annotationStream(type)
                                                                      .filter(a -> a.tagIsA(tag))
                                                                      .collect(Collectors.toList());
                                                }
                                             }));
   }

   /**
    * Returns <b>true</b> if any item in the given list evaluates to *true* for the given predicate expression.
    * <pre>
    *    usage: any( list_expression, predicate_expression )
    *    e.g.: any( @TOKEN, #VERB ) -- returns true if any token on the current HString is a verb
    * </pre>
    *
    * @param list      the list of items to apply the predicate to
    * @param predicate the predicate to use for testing
    * @return the LyreExpression
    */
   public static LyreExpression any(@NonNull LyreExpression list, @NonNull LyreExpression predicate) {
      return new LyreExpression(formatMethod("any", list, predicate),
                                PREDICATE,
                                o -> list.applyAsList(o).stream().anyMatch(predicate::testObject));
   }

   /**
    * Applies the right-hand expression on the object resulting from the left-hand expression.
    * <pre>
    *    Usage:  object_expression ~= operator_expression
    *    e.g.: 'abcdef' ~= /a/b/ -- will apply the regular expression substitution <code>/a/b/</code> to the literal
    *          <code>'abcdef'</code>
    * </pre>
    *
    * @param object   the expression returning the object the operator should be applied on
    * @param operator the operator to apply to the object
    * @return the LyreExpression
    */
   public static LyreExpression apply(@NonNull LyreExpression object, @NonNull LyreExpression operator) {
      return new LyreExpression(String.format("%s ~= %s", object, operator),
                                operator.getType(),
                                o -> operator.applyAsObject(object.applyAsObject(o)));
   }

   /**
    * Creates an array of expressions.
    * <pre>
    *    usage: [ expression1, expresion2, ..., expressionN ]
    *    e.g.: ['a', 'b', 'c'] -- returns a list of the given literal values.
    *
    *    Advanced Usage Example: $_ ~= [ @PHRASE_CHUNK, @ENTITY ]
    *    -- Applies the list of expressions to the current token resulting in a list of the phrase chunks and
    *    -- entities on the current HString
    *
    *    Advanced Usage Example: $_ ~= [ concat('W=', lower), concat('isDigit=', isDigit) ]
    *    -- Applies the list of expressions to the current token resulting in a list of Strings as follows:
    *    -- given $_ = 'test' the resulting array would be [ 'W=test', 'isDigit=false' ]
    * </pre>
    *
    * @param elements the array elements
    * @return the LyreExpression
    */
   public static LyreExpression array(@NonNull LyreExpression... elements) {
      return list(Arrays.asList(elements));
   }

   /**
    * Returns the value of the given AttributeType on the current HString.
    * <pre>
    *    Usage:  $ATTRIBUTE_TYPE
    *    e.g.: $PART_OF_SPEECH
    * </pre>
    *
    * @param type the attribute type
    * @return the LyreExpression
    */
   public static LyreExpression attribute(@NonNull AttributeType<?> type) {
      return attribute(type, $_);
   }

   /**
    * Returns the value of the given AttributeType on the HString resulting from the given expression
    * <pre>
    *    Usage:  $ATTRIBUTE_TYPE(expression)
    *    e.g.: $PART_OF_SPEECH(@PHRASE_CHUNK) --return the part-of-speech of the phrase chunk annotations
    * </pre>
    *
    * @param type       the attribute type
    * @param expression the expression returning the HString from which the attribute value will be extracted.
    * @return the LyreExpression
    */
   public static LyreExpression attribute(@NonNull AttributeType<?> type, @NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: attribute only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      LyreExpressionType eType = TypeUtils.isAssignable(type.getValueType(), String.class)
                                 ? STRING
                                 : OBJECT;
      return new LyreExpression(formatMethod(String.format("$%s", type), expression),
                                eType,
                                o -> process(false, expression.applyAsObject(o),
                                             h -> toHString(h).attribute(type)));
   }

   /**
    * Converts a list of values into binary features.
    * <pre>
    *    Usage: binary{'prefix'}(expression)
    *    e.g.: binary{'WORD'}(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: binary(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: binary{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: binary -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param prefix     the feature prefix (null is acceptable)
    * @param expression the expression returning the objects to convert into features
    * @return the LyreExpression
    */
   public static LyreExpression binary(String prefix, @NonNull LyreExpression expression) {
      return feature(prefix, ValueCalculator.Binary, expression);
   }

   /**
    * Converts a list of values into binary features over the current HString.
    * <pre>
    *    Usage: binary{'prefix'}(expression)
    *    e.g.: binary{'WORD'}(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: binary(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: binary{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: binary -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param prefix the feature prefix (null is acceptable)
    * @return the LyreExpression
    */
   public static LyreExpression binary(String prefix) {
      return feature(prefix, ValueCalculator.Binary);
   }

   /**
    * Converts a list of values into binary features.
    * <pre>
    *    Usage: binary{'prefix'}(expression)
    *    e.g.: binary{'WORD'}(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: binary(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: binary{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: binary -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param expression the expression returning the objects to convert into features
    * @return the LyreExpression
    */
   public static LyreExpression binary(@NonNull LyreExpression expression) {
      return feature(ValueCalculator.Binary, expression);
   }

   private static boolean compareObjectPredicate(Object l, Object r, NumericComparison comparison) {
      if(l == null || r == null) {
         switch(comparison) {
            case EQ:
               return l == r;
            case NE:
               return l != r;
            default:
               return false;
         }
      }
      if(l instanceof Number && r instanceof Number) {
         return comparison.compare(Cast.<Number>as(l).doubleValue(),
                                   Cast.<Number>as(r).doubleValue());
      }
      if(l instanceof Tag && r instanceof Tag) {
         Tag lTag = Cast.as(l);
         Tag rTag = Cast.as(r);
         switch(comparison) {
            case EQ:
               return lTag.isInstance(rTag);
            case NE:
               return !lTag.isInstance(rTag);
            default:
               return comparison.compare(Sorting.compare(lTag.name(), rTag.name()), 0);
         }
      }
      if(l.getClass().isInstance(r) || r.getClass().isInstance(l)) {
         if(comparison == NumericComparison.EQ) {
            return l.equals(r);
         }
         if(comparison == NumericComparison.NE) {
            return !l.equals(r);
         }
         return comparison.compare(Sorting.compare(l, r), 0);
      }

      if((l instanceof CharSequence) && (r instanceof CharSequence)) {
         String lStr = l.toString();
         String rStr = r.toString();
         if(comparison == NumericComparison.EQ) {
            return Objects.equals(lStr, rStr);
         }
         if(comparison == NumericComparison.NE) {
            return !Objects.equals(lStr, rStr);
         }
         return comparison.compare(Sorting.compare(lStr, rStr), 0);
      }

      if(!(r instanceof CharSequence)) {
         Object lConv = Converter.convertSilently(l, r.getClass());
         if(lConv != null) {
            return compareObjectPredicate(lConv, r, comparison);
         }
         if(r instanceof Number) {
            return compareObjectPredicate(Double.NaN, r, comparison);
         }
      }

      if(!(l instanceof CharSequence)) {
         Object rConv = Converter.convertSilently(r, l.getClass());
         if(rConv != null) {
            return compareObjectPredicate(l, rConv, comparison);
         }
         if(l instanceof Number) {
            return compareObjectPredicate(l, Double.NaN, comparison);
         }
      }

      return false;
   }

   /**
    * Gets a contextual (previous or next) token for the given HString at the given position (relative).
    * <pre>
    *    Usage: cxt( hstring_expression, relative_position )
    *    e.g.: cxt( $_, -1) -- gets the token left of the current HString
    *    e.g.: cxt( $_, 10) -- gets the token 10 to the right of the current HString
    * </pre>
    *
    * @param expression        the expression returning the HString to get the context of
    * @param relative_position the relative position
    * @return the LyreExpression
    */
   public static LyreExpression context(@NonNull LyreExpression expression, int relative_position) {
      return context(expression, number(relative_position));
   }

   /**
    * Gets a contextual (previous or next) token for the given HString at the given position (relative).
    * <pre>
    *    Usage: cxt( hstring_expression, relative_position )
    *    e.g.: cxt( $_, -1) -- gets the token left of the current HString
    *    e.g.: cxt( $_, 10) -- gets the token 10 to the right of the current HString
    * </pre>
    *
    * @param expression        the expression returning the HString to get the context of
    * @param relative_position the relative position
    * @return the LyreExpression
    */
   public static LyreExpression context(@NonNull LyreExpression expression, @NonNull LyreExpression relative_position) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: context only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("cxt", expression, relative_position),
                                HSTRING,
                                o -> {
                                   HString targ = toHString(expression.applyAsObject(o));
                                   int pos = (int) relative_position.applyAsDouble(toHString(o));
                                   if(pos == 0) {
                                      return targ;
                                   } else if(pos > 0) {
                                      for(int i = 0; i < pos; i++) {
                                         targ = targ.next(Types.TOKEN);
                                      }
                                   } else {
                                      for(int i = 0; i < Math.abs(pos); i++) {
                                         targ = targ.previous(Types.TOKEN);
                                      }
                                   }
                                   return targ;
                                });
   }

   /**
    * Counts the items resulting from the given expression (treated as a list) and applies a Frequency ValueCalculator
    * to the result. Accepts OBJECT, HSTRING, and STRING expressions.
    *
    * <pre>
    *    Usage: count(expression, valueCalculator)
    *    e.g.: count(@TOKEN, 'Binary') -- creates binary counts of all tokens in the HString
    *    e.g.: count(ngram(2, @TOKEN)) -- creates frequency counts of all bigrams in the HString
    * </pre>
    *
    * @param expression the expression returning the objects to convert into counts
    * @return the LyreExpression
    */
   public static LyreExpression count(@NonNull LyreExpression expression) {
      return count(expression, ValueCalculator.Frequency);
   }

   /**
    * Counts the items resulting from the given expression (treated as a list) and applies the given ValueCalculator to
    * the result. Accepts OBJECT, HSTRING, and STRING expressions.
    *
    * <pre>
    *    Usage: count(expression, valueCalculator)
    *    e.g.: count(@TOKEN, 'Binary') -- creates binary counts of all tokens in the HString
    *    e.g.: count(ngram(2, @TOKEN)) -- creates frequency counts of all bigrams in the HString
    * </pre>
    *
    * @param expression      the expression returning the objects to convert into counts
    * @param valueCalculator the ValueCalculator to use to adjust the values
    * @return the LyreExpression
    */
   public static LyreExpression count(@NonNull LyreExpression expression, @NonNull LyreExpression valueCalculator) {
      Validation.checkArgument(valueCalculator.isInstance(STRING),
                               "Illegal Expression: valueCalculater only accepts a STRING representing the ValueCalculator to use, but '"
                                     + valueCalculator + "' was provided which is of type " + valueCalculator.getType());
      return count(expression, ValueCalculator.valueOf(valueCalculator.apply(null)));
   }

   /**
    * Counts the items resulting from the given expression (treated as a list) and applies the given ValueCalculator to
    * the result. Accepts OBJECT, HSTRING, and STRING expressions.
    *
    * <pre>
    *    Usage: count(expression, valueCalculator)
    *    e.g.: count(@TOKEN, 'Binary') -- creates binary counts of all tokens in the HString
    *    e.g.: count(ngram(2, @TOKEN)) -- creates frequency counts of all bigrams in the HString
    * </pre>
    *
    * @param expression      the expression returning the objects to convert into counts
    * @param valueCalculator the ValueCalculator to use to adjust the values
    * @return the LyreExpression
    */
   public static LyreExpression count(@NonNull LyreExpression expression, @NonNull ValueCalculator valueCalculator) {
      Validation.checkArgument(expression.isInstance(OBJECT, HSTRING, STRING),
                               "Illegal Expression: count only accepts OBJECT, HSTRING, or STRING expressions, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("count", expression),
                                COUNTER,
                                o -> valueCalculator.adjust(Counters.newCounter(expression.applyAsList(o))));
   }

   /**
    * Gets the annotation(s) having a dependency relation in the given direction (INCOMING or OUTGOING).
    * <pre>
    *    Usage:  @>dep -- annotations from outgoing dependency of given relation
    *    Usage:  @<dep -- annotations from incoming dependency of given relation
    *    e.g.: @> -- return the annotation(s) that the annotations reaching from the current token via a dependency relation.
    * </pre>
    *
    * @param direction the direction of the relation (INCOMING or OUTGOING)
    * @return the LyreExpression
    */
   public static LyreExpression dep(@NonNull RelationDirection direction) {
      return dep(direction, null, $_);
   }

   /**
    * Gets the annotation(s) having the following dependency relation in the given direction (INCOMING or OUTGOING).
    * <pre>
    *    Usage:  @>dep{'relation'} -- annotations from outgoing dependency of given relation
    *    Usage:  @<dep{'relation'} -- annotations from incoming dependency of given relation
    *    e.g.: @>{nsubj} -- return the annotation that the current token has an nsubj relation with
    * </pre>
    *
    * @param direction the direction of the relation (INCOMING or OUTGOING)
    * @param relation  the desired dependency relation
    * @return the LyreExpression
    */
   public static LyreExpression dep(@NonNull RelationDirection direction, String relation) {
      return dep(direction, relation, $_);
   }

   /**
    * Gets the annotation(s) having the following dependency relation in the given direction (INCOMING or OUTGOING).
    * <pre>
    *    Usage:  @>dep{'relation'} -- annotations from outgoing dependency of given relation
    *    Usage:  @<dep{'relation'} -- annotations from incoming dependency of given relation
    *    e.g.: @>{nsubj} -- return the annotation that the current token has an nsubj relation with
    * </pre>
    *
    * @param direction  the direction of the relation (INCOMING or OUTGOING)
    * @param expression the expression returning the HString to extract dependency relations over
    * @return the LyreExpression
    */
   public static LyreExpression dep(@NonNull RelationDirection direction, @NonNull LyreExpression expression) {
      return dep(direction, null, expression);
   }

   /**
    * Gets the annotation(s) having the following dependency relation in the given direction (INCOMING or OUTGOING).
    * <pre>
    *    Usage:  @>dep{'relation'}( expression ) -- annotations from outgoing dependency of given relation
    *    Usage:  @<dep{'relation'}( expression ) -- annotations from incoming dependency of given relation
    *    e.g.: @>{nsubj}( @PHRASE_CHUNK ) -- return the annotation that the phrase chunks on the current token
    *                                        have an nsubj relation with
    * </pre>
    *
    * @param direction  the direction of the relation (INCOMING or OUTGOING)
    * @param relation   the desired dependency relation
    * @param expression the expression returning the HString to extract dependency relations over
    * @return the LyreExpression
    */
   public static LyreExpression dep(@NonNull RelationDirection direction, String relation,
                                    @NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: dep only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod(String.format("@%s%s",
                                                           direction == OUTGOING
                                                           ? ">"
                                                           : "<",
                                                           Strings.isNullOrBlank(relation)
                                                           ? ""
                                                           : "{" + relation + "}"),
                                             expression),
                                HSTRING, o -> process(true, expression.applyAsObject(o), a -> {
         HString h = toHString(a);
         if(direction == OUTGOING) {
            if(Strings.isNullOrBlank(relation) || h.dependencyIsA(relation)) {
               return h.dependency().v2;
            }
            return null;
         } else {
            if(Strings.isNullOrBlank(relation)) {
               return h.children();
            }
            return h.children(relation);
         }
      })
      );
   }

   /**
    * Checks if the left and right expression are equal using <code>.equals</code>, checking for content equality for
    * HStrings and literals, and using {@link Tag#isInstance(Tag)} for tags.
    * <pre>
    *    Usage: left_expression = right_expresison
    *    e.g.: $_ = 'man' -- checks if the current object is equal to man
    * </pre>
    *
    * @param left  the left hand expression
    * @param right the right hand expression
    * @return the LyreExpression
    */
   public static LyreExpression eq(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s = %s", left, right),
                                PREDICATE,
                                o -> compareObjectPredicate(left.applyAsObject(o),
                                                            right.applyAsObject(o),
                                                            NumericComparison.EQ));
   }

   /**
    * Checks if the left and right expression are equal using <code>.equals</code>, checking for content equality for
    * HStrings and literals, and using {@link Tag#isInstance(Tag)} for tags.
    * <pre>
    *    Usage: left_expression = right_expresison
    *    e.g.: $_ = 'man' -- checks if the current object is equal to man
    * </pre>
    *
    * @param left  the left hand expression
    * @param value the right hand value
    * @return the LyreExpression
    */
   public static LyreExpression eq(LyreExpression left, double value) {
      return eq(left, number(value));
   }

   /**
    * Predicate checking if the Object returned from the given expression exists meaning it is not null and not a blank
    * CharSequence or empty list.
    * <pre>
    *    Usage:  exists( expression )
    *    e.g.: exists( @ENTITY ) -- checks if the result of the entity annotation is an empty or null list
    * </pre>
    *
    * @param expression the expression returning an HString to check is empty
    * @return the LyreExpression
    */
   public static LyreExpression exists(@NonNull LyreExpression expression) {
      return new LyreExpression(formatMethod("exists", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> {
                                                    if(a instanceof List) {
                                                       return Cast.<List>as(a).size() > 0;
                                                    }
                                                    return Optional.ofNullable(a)
                                                                   .map(Object::toString)
                                                                   .map(Strings::isNotNullOrBlank)
                                                                   .orElse(false);
                                                 }));
   }

   /**
    * Converts a list of values into features.
    *
    * @param calculator the {@link ValueCalculator} for calculating feature values
    * @return the LyreExpression
    */
   public static LyreExpression feature(@NonNull ValueCalculator calculator) {
      return feature(null, calculator, $_);
   }

   /**
    * Converts a list of values into features.
    *
    * @param calculator the {@link ValueCalculator} for calculating feature values
    * @param expression the expression returning the objects to convert into features
    * @return the LyreExpression
    */
   public static LyreExpression feature(@NonNull ValueCalculator calculator, @NonNull LyreExpression expression) {
      return feature(null, calculator, expression);
   }

   /**
    * Converts a list of values into features.
    *
    * @param prefix     the feature prefix (null is acceptable)
    * @param calculator the {@link ValueCalculator} for calculating feature values
    * @return the LyreExpression
    */
   public static LyreExpression feature(String prefix, @NonNull ValueCalculator calculator) {
      return feature(prefix, calculator, $_);
   }

   /**
    * Converts a list of values into features.
    *
    * @param prefix     the feature prefix (null is acceptable)
    * @param calculator the {@link ValueCalculator} for calculating feature values
    * @param expression the expression returning the objects to convert into features
    * @return the LyreExpression
    */
   public static LyreExpression feature(String prefix,
                                        @NonNull ValueCalculator calculator,
                                        @NonNull LyreExpression expression) {
      StringBuilder name = new StringBuilder();
      switch(calculator) {
         case Binary:
            name.append("binary");
            break;
         case L1:
            name.append("L1");
            break;
         case Frequency:
            name.append("frequency");
      }
      if(Strings.isNotNullOrBlank(prefix)) {
         name.append("{'").append(prefix).append("'}");
      }
      return new LyreExpression(formatMethod(name.toString(), expression),
                                FEATURE,
                                o -> calculator.adjust(expression.count(toHString(o)))
                                               .entries()
                                               .stream()
                                               .map(e -> Feature.realFeature(prefix, e.getKey(), e.getValue()))
                                               .collect(Collectors.toList()));
   }

   /**
    * filters a list keeping only those elements that given predicate evaluates true for.
    * <pre>
    *    Usage: filter( list_expression, predicate_expression )
    *    e.g.: filter( @TOKEN, isContentWord ) -- keep all tokens on the current HString that are content words
    * </pre>
    *
    * @param list      the list
    * @param predicate the predicate
    * @return the lyre expression
    */
   public static LyreExpression filter(@NonNull LyreExpression list, @NonNull LyreExpression predicate) {
      return new LyreExpression(formatMethod("filter", list, predicate),
                                list.getType(),
                                obj -> list.applyAsList(obj).stream().filter(predicate::testObject).collect(
                                      Collectors.toList()));
   }

   /**
    * Return the first element of a list expression or null if none.
    * <pre>
    *    Usage: first( list_expression )
    *    e.g.: first( @ENTITY ) -- return the first entity annotation on this HString
    * </pre>
    *
    * @param list the list expression
    * @return the LyreExpression
    */
   public static LyreExpression first(@NonNull LyreExpression list) {
      return new LyreExpression(formatMethod("first", list),
                                list.getType(),
                                o -> Iterables.getFirst(list.applyAsList(o)).orElse(null));
   }

   /**
    * Flattens all elements in a list recursively.
    * <pre>
    *    Usage: flatten( list_expression )
    *    e.g.: flatten( map(@TOKEN, [ 'p1=' + $_[:-1], 'p2=' + $_[:-2] ] )  ) -- create a flattened list of unigram
    *           and bigram prefixes of all tokens on the current HString.
    * </pre>
    *
    * @param list the list
    * @return the LyreExpression
    */
   public static LyreExpression flatten(@NonNull LyreExpression list) {
      return new LyreExpression(formatMethod("flatten", list),
                                list.getType(), o -> flatten(list.applyAsList(o)));
   }

   private static List<Object> flatten(Collection<Object> list) {
      List<Object> output = new ArrayList<>();
      for(Object o : list) {
         if(o instanceof Collection) {
            output.addAll(flatten(Cast.<Collection<Object>>as(o)));
         } else {
            output.add(o);
         }
      }
      return output;
   }

   private static String formatMethod(String methodName, LyreExpression... arguments) {
      if(arguments == null || arguments.length < 1 || (arguments.length == 1 && arguments[0] == $_)) {
         return methodName;
      }
      return String.format("%s(%s)", methodName, Stream.of(arguments)
                                                       .map(Object::toString)
                                                       .collect(Collectors.joining(", ")));
   }

   /**
    * Converts a list of values into features with raw counts (i.e. frequency).
    * <pre>
    *    Usage: frequency{'prefix'}(expression)
    *    e.g.: frequency{'WORD'}(@TOKEN) -- create bag-of-words with raw counts features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: frequency(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: frequency{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: frequency -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param prefix     the feature prefix (null is acceptable)
    * @param expression the expression returning the objects to convert into features
    * @return the LyreExpression
    */
   public static LyreExpression frequency(String prefix, @NonNull LyreExpression expression) {
      return feature(prefix, ValueCalculator.Frequency, expression);
   }

   /**
    * Converts a list of values into features with raw counts (i.e. frequency).
    * <pre>
    *    Usage: frequency{'prefix'}(expression)
    *    e.g.: frequency{'WORD'}(@TOKEN) -- create bag-of-words with raw counts features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: frequency(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: frequency{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: frequency -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param prefix the feature prefix (null is acceptable)
    * @return the LyreExpression
    */
   public static LyreExpression frequency(String prefix) {
      return feature(prefix, ValueCalculator.Frequency);
   }

   /**
    * Converts a list of values into features with raw counts (i.e. frequency).
    * <pre>
    *    Usage: frequency{'prefix'}(expression)
    *    e.g.: frequency{'WORD'}(@TOKEN) -- create bag-of-words with raw counts features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: frequency(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: frequency{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: frequency -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param expression the expression returning the objects to convert into features
    * @return the LyreExpression
    */
   public static LyreExpression frequency(@NonNull LyreExpression expression) {
      return feature(ValueCalculator.Frequency, expression);
   }

   /**
    * Gets the i-th element in the given list or null if the index is invalid
    * <pre>
    *    Usage: get( index, list_expression )
    *    e.g.: get(@TOKEN, 0) -- get the first token
    *    e.g.: get(@TOKEN, 10) -- get the 10th token
    * </pre>
    *
    * @param list  the container
    * @param index the index
    * @return the LyreExpression
    */
   public static LyreExpression get(@NonNull LyreExpression list, int index) {
      return get(number(index), list);
   }

   /**
    * Gets the i-th element in the given list or null if the index is invalid
    * <pre>
    *    Usage: get( index, list_expression )
    *    e.g.: get(@TOKEN, 0) -- get the first token
    *    e.g.: get(@TOKEN, 10) -- get the 10th token
    * </pre>
    *
    * @param listExpression the container
    * @param index          the index
    * @return the LyreExpression
    */
   public static LyreExpression get(@NonNull LyreExpression listExpression, @NonNull LyreExpression index) {
      Validation.checkArgument(index.isInstance(NUMERIC),
                               "Illegal Expression: index only accepts a NUMERIC, but '"
                                     + index + "' was provided which is of type " + index.getType());
      return new LyreExpression(formatMethod("get", listExpression, index),
                                listExpression.getType(),
                                o -> {
                                   List<?> list = listExpression.applyAsList(o);
                                   int ei = (int) index.applyAsDouble(toHString(o));
                                   if(ei < 0) {
                                      ei = Math.max(list.size() + ei, 0);
                                   }
                                   return Iterables.get(list, ei).orElse(null);
                                });
   }

   /**
    * Checks if the left-hand expression is greater than the right-hand expression. Will default to string comparisons
    * for non-numeric values.
    * <pre>
    *    Usage: left_expression > right_expresison
    *    e.g.: $CONFIDENCE > 0.9 -- checks if the numeric confidence attributes is greater than 0.9
    * </pre>
    *
    * @param left  the left hand expression
    * @param right the right hand expression
    * @return the LyreExpression
    */
   public static LyreExpression gt(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s > %s", left, right),
                                PREDICATE, o -> compareObjectPredicate(left.applyAsObject(o),
                                                                       right.applyAsObject(o),
                                                                       NumericComparison.GT));
   }

   /**
    * Checks if the left-hand expression is greater than the right-hand expression. Will default to string comparisons
    * for non-numeric values.
    * <pre>
    *    Usage: left_expression > right_expresison
    *    e.g.: $CONFIDENCE > 0.9 -- checks if the numeric confidence attributes is greater than 0.9
    * </pre>
    *
    * @param left  the left hand expression
    * @param value the right hand value
    * @return the LyreExpression
    */
   public static LyreExpression gt(@NonNull LyreExpression left, double value) {
      return gt(left, number(value));
   }

   /**
    * Checks if the left-hand expression is greater than or equal to the right-hand expression. Will default to string
    * comparisons for non-numeric values.
    * <pre>
    *    Usage: left_expression >= right_expresison
    *    e.g.: $CONFIDENCE >= 0.9 -- checks if the numeric confidence attributes is greater than or equal  to 0.9
    * </pre>
    *
    * @param left  the left hand expression
    * @param right the right hand expression
    * @return the LyreExpression
    */
   public static LyreExpression gte(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s >= %s", left, right),
                                PREDICATE, o -> compareObjectPredicate(left.applyAsObject(o),
                                                                       right.applyAsObject(o),
                                                                       NumericComparison.GTE));
   }

   /**
    * Checks if the left-hand expression is greater than or equal to the right-hand expression. Will default to string
    * comparisons for non-numeric values.
    * <pre>
    *    Usage: left_expression >= right_expresison
    *    e.g.: $CONFIDENCE >= 0.9 -- checks if the numeric confidence attributes is greater than or equal  to 0.9
    * </pre>
    *
    * @param left  the left hand expression
    * @param value the right hand value
    * @return the LyreExpression
    */
   public static LyreExpression gte(@NonNull LyreExpression left, double value) {
      return gte(left, number(value));
   }

   /**
    * Checks if any annotations on the HString resulting from the left-hand expression evaluates to true using the
    * right-hand expression.
    * <pre>
    *    Usage:  hstring_expression in predicate_expression -- does the HString from the hstring_expression have an
    *                                                          annotation matching the predicate_expression
    *    e.g.:  $_ has #NP(@PHRASE_CHUNK) -- return true if the current HString contains a phrase chunk with POS NP
    * </pre>
    *
    * @param container  the expression returning the container (HString) to search in
    * @param expression the expression to evaluate the HString with
    * @return the LyreExpression
    */
   public static LyreExpression has(@NonNull LyreExpression container, @NonNull LyreExpression expression) {
      Validation.checkArgument(container.isInstance(HSTRING),
                               "Illegal Expression: has.container only accepts a HSTRING, but '"
                                     + container + "' was provided which is of type " + container.getType());
      return new LyreExpression(String.format("(%s has %s)", container, expression),
                                PREDICATE,
                                obj -> {
                                   HString bucket = toHString(container.applyAsObject(obj));
                                   final SerializablePredicate<HString> effective;
                                   if(expression.getType().isInstance(PREDICATE)) {
                                      effective = expression;
                                   } else {
                                      effective = v -> expression.applyAsObject(v) != null;
                                   }
                                   return bucket.annotationStream().anyMatch(effective);
                                });
   }

   /**
    * Predicate checking if the HString returned from the given expression contains a stop word
    * <pre>
    *    Usage:  hasStopWord( expression )
    *    e.g.: hasStopWord( @TOKEN ) -- check if the tokens on the current HString are stop words
    * </pre>
    *
    * @param expression the expression returning an HString to check if it has a stop word.
    * @return the LyreExpression
    */
   public static LyreExpression hasStopWord(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: hasStopWord only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("hasStopWord", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(HString::toHString)
                                                              .map(h -> StopWords.hasStopWord().test(h))
                                                              .orElse(false)));
   }

   /**
    * IF then function to perform a given true or false expression based on a given condition.
    * <pre>
    *    Usage:  if(condition_expression, true_expression, false_expression)
    *    e.g.: if(isDigit, '[:digit:]', $_) --return the literal value '[:digit:]' when the current HString is a digit
    *          or the HString itself when not a digit.
    * </pre>
    *
    * @param condition the condition expression
    * @param whenTrue  the expression to apply when the condition evaluates to true
    * @param whenFalse the expression to apply when the condition evaluates to false
    * @return the LyreExpression
    */
   public static LyreExpression ifThen(@NonNull LyreExpression condition,
                                       @NonNull LyreExpression whenTrue,
                                       @NonNull LyreExpression whenFalse) {
      return new LyreExpression(formatMethod("if", condition, whenTrue, whenFalse),
                                LyreExpressionType.determineCommonType(Arrays.asList(whenFalse, whenTrue)),
                                o -> condition.testObject(o)
                                     ? whenTrue.applyAsObject(o)
                                     : whenFalse.applyAsObject(o));
   }

   /**
    * Checks if the left-hand object is "in" the right-hand object, where in means "contains".
    * <pre>
    *    Usage:  object_expression in container_expression -- is te object from the object_expression contained in the container
    *                                                         from the container_expression
    *    e.g.: 'a' in 'hat' -- return true if 'a' is the string 'hat'
    *    e.g.: 'dog' in ['cat', 'dog', 'bird'] -- return true if 'dog's in the list ['cat', 'dog', 'bird']
    * </pre>
    *
    * @param object    the expression that returns the object to search for
    * @param container the expression returning the container to search in
    * @return the LyreExpression
    */
   public static LyreExpression in(@NonNull LyreExpression object, @NonNull LyreExpression container) {
      return new LyreExpression(String.format("(%s in %s)", object, container),
                                PREDICATE,
                                o -> {
                                   Object bucket = container.applyAsObject(o);
                                   Object searchingFor = object.applyAsObject(o);
                                   if(bucket == null || searchingFor == null) {
                                      return false;
                                   }
                                   if(bucket instanceof WordList) {
                                      WordList wordList = Cast.as(bucket);
                                      return wordList.contains(searchingFor.toString());
                                   }
                                   if(bucket instanceof Collection) {
                                      Collection<?> c = Cast.as(bucket);
                                      if(c.isEmpty()) {
                                         return false;
                                      }
                                      if(searchingFor instanceof HString) {
                                         return c.contains(searchingFor) || c.contains(searchingFor.toString());
                                      }
                                      return c.contains(searchingFor);
                                   }
                                   return bucket.toString().contains(searchingFor.toString());
                                });
   }

   /**
    * Returns all annotations of the given types in interleaved fashion (see {@link
    * HString#interleaved(AnnotationType...)}**
    * <pre>
    *    Usage:  interleave( ANNOTATION_TYPE1, ANNOTATION_TYPE2, ..., ANNOTATION_TYPEN )
    *    e.g.: interleave( PHRASE_CHUNK, TOKEN ) -- return a list of annotations first trying to get phrase chunks and
    *                                               falling back to tokens if not found
    * </pre>
    *
    * @param types the annotation types to interleave
    * @return the LyreExpression
    */
   public static LyreExpression interleave(@NonNull AnnotationType... types) {
      final LyreExpression[] expressions = Stream.of(types)
                                                 .map(t -> literal(t.name()))
                                                 .toArray(LyreExpression[]::new);
      return new LyreExpression(formatMethod("interleave", expressions),
                                HSTRING,
                                o -> toHString($_.applyAsObject(o)).interleaved(types));
   }

   /**
    * Generates IOB-formatted tags for the given expression
    * <pre>
    *    Usage: iob{ANNOTATION TYPE}( expression )
    *    e.g.: iob{PHRASE_CHUNK} -- generates iob formatted chunk part-of-speech tags over the current HString
    *    e.g.: iob{PHRASE_CHUNK}(@SENTENCE) -- generates iob formatted chunk part-of-speech tags over the sentences in
    *                                            current HString
    * </pre>
    *
    * @param type the annotation type
    * @return the LyreExpression
    */
   public static LyreExpression iob(@NonNull LyreExpression type) {
      return iob(type, $_);
   }

   /**
    * Generates IOB-formatted tags for the given expression
    * <pre>
    *    Usage: iob{ANNOTATION TYPE}( expression )
    *    e.g.: iob{PHRASE_CHUNK} -- generates iob formatted chunk part-of-speech tags over the current HString
    *    e.g.: iob{PHRASE_CHUNK}(@SENTENCE) -- generates iob formatted chunk part-of-speech tags over the sentences in
    *                                            current HString
    * </pre>
    *
    * @param type the annotation type
    * @return the LyreExpression
    */
   public static LyreExpression iob(@NonNull AnnotationType type) {
      return iob(literal(type.name()));
   }

   /**
    * Generates IOB-formatted tags for the given expression
    * <pre>
    *    Usage: iob('ANNOTATION_TYPE', expression )
    *    e.g.: iob('PHRASE_CHUNK')-- generates iob formatted chunk part-of-speech tags over the current HString
    *    e.g.: iob('PHRASE_CHUNK', @SENTENCE)-- generates iob formatted chunk part-of-speech tags over the sentences in
    *                                            current HString
    * </pre>
    *
    * @param type       the annotation type
    * @param expression the expression returning the Annotations to generate IOB tags for
    * @return the LyreExpression
    */
   public static LyreExpression iob(@NonNull AnnotationType type, @NonNull LyreExpression expression) {
      return iob(literal(type.name()), expression);
   }

   /**
    * Generates IOB-formatted tags for the given expression
    * <pre>
    *    Usage: iob('ANNOTATION_TYPE', expression )
    *    e.g.: iob('PHRASE_CHUNK')-- generates iob formatted chunk part-of-speech tags over the current HString
    *    e.g.: iob('PHRASE_CHUNK', @SENTENCE)-- generates iob formatted chunk part-of-speech tags over the sentences in
    *                                            current HString
    * </pre>
    *
    * @param type       the annotation type
    * @param expression the expression returning the Annotations to generate IOB tags for
    * @return the LyreExpression
    */
   public static LyreExpression iob(@NonNull LyreExpression type, @NonNull LyreExpression expression) {
      Validation.checkArgument(type.isInstance(STRING),
                               "Illegal Expression: type only accepts a STRING, but '"
                                     + type + "' was provided which is of type " + type.getType());
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: annotation only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("iob", type, expression),
                                STRING,
                                o -> {
                                   final AnnotationType aType = Types.annotation(type.apply(toHString(o)));
                                   final LyreExpression token = annotation(Types.TOKEN);
                                   return process(false, andPipe(expression, token).applyAsObject(o), o2 -> {
                                      if(o2 == null) {
                                         return "O";
                                      }
                                      final HString h = toHString(o2);
                                      if(h.isEmpty() || !h.hasAnnotation(aType)) {
                                         return "O";
                                      }
                                      final Annotation a = Iterables.getFirst(h.annotations(aType), null);
                                      String tag = a.hasTag()
                                                   ? a.getTag().name()
                                                   : null;
                                      if(h.start() == a.start()) {
                                         return "B-" + tag;
                                      }
                                      return "I-" + tag;
                                   });
                                });
   }

   /**
    * Predicate checking if the HString returned from the given expression contains all alphanumeric characters
    * <pre>
    *    Usage:  isAlphaNumeric( expression )
    *    e.g.: isAlphaNumeric( @TOKEN ) -- check if the tokens on the current HString are alphanumeric
    * </pre>
    *
    * @param expression the expression returning an HString to check for all alphanumeric characters.
    * @return the LyreExpression
    */
   public static LyreExpression isAlphaNumeric(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: isAlphaNumeric only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("isAlphaNumeric", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(Object::toString)
                                                              .map(Strings::isAlphaNumeric)
                                                              .orElse(false)));
   }

   /**
    * Predicate checking if the HString returned from the given expression is a content word
    * <pre>
    *    Usage:  isContentWord( expression )
    *    e.g.: isContentWord( @TOKEN ) -- check if the tokens on the current HString are content words
    * </pre>
    *
    * @param expression the expression returning an HString to check is a content word.
    * @return the LyreExpression
    */
   public static LyreExpression isContentWord(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: isContentWord only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("isContentWord", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(HString::toHString)
                                                              .map(h -> StopWords.isNotStopWord().test(h))
                                                              .orElse(false)));
   }

   /**
    * Predicate checking if the HString returned from the given expression is a digit
    * <pre>
    *    Usage:  isDigit( expression )
    *    e.g.: isDigit( @TOKEN ) -- check if the tokens on the current HString are digits
    * </pre>
    *
    * @param expression the expression returning an HString to check is a digit.
    * @return the LyreExpression
    */
   public static LyreExpression isDigit(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: isDigit only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("isDigit", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(Object::toString)
                                                              .map(Strings::isDigit)
                                                              .orElse(false)));
   }

   /**
    * Predicate checking if the HString returned from the given expression is all letters
    * <pre>
    *    Usage:  isLetter( expression )
    *    e.g.: isLetter( @TOKEN ) -- check if the tokens on the current HString are all letters
    * </pre>
    *
    * @param expression the expression returning an HString to check is all letters.
    * @return the LyreExpression
    */
   public static LyreExpression isLetter(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: isLetter only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("isLetter", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(Object::toString)
                                                              .map(Strings::isLetter)
                                                              .orElse(false)));
   }

   /**
    * Predicate checking if the HString returned from the given expression is lower case
    * <pre>
    *    Usage:  isLetter( expression )
    *    e.g.: isLetter( @TOKEN ) -- check if the tokens on the current HString are are lower case
    * </pre>
    *
    * @param expression the expression returning an HString to check is lower case
    * @return the LyreExpression
    */
   public static LyreExpression isLower(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: isLower only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("isLower", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(Object::toString)
                                                              .map(Strings::isLowerCase)
                                                              .orElse(false)));
   }

   private static boolean isNullOrEmpty(Object o) {
      if(o == null) {
         return true;
      }
      if(o instanceof Collection) {
         return Cast.<Collection<?>>as(o).isEmpty();
      }
      if(o instanceof CharSequence) {
         return Strings.isNullOrBlank(o.toString());
      }
      if(o instanceof Number) {
         return Double.isFinite(Cast.<Number>as(o).doubleValue());
      }
      return false;
   }

   /**
    * Predicate checking if the HString returned from the given expression is all punctuation
    * <pre>
    *    Usage:  isPunctuation( expression )
    *    e.g.: isPunctuation( @TOKEN ) -- check if the tokens on the current HString are all punctuation
    * </pre>
    *
    * @param expression the expression returning an HString to check is all punctuation.
    * @return the LyreExpression
    */
   public static LyreExpression isPunctuation(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: isPunctuation only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("isPunctuation", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(Object::toString)
                                                              .map(Strings::isPunctuation)
                                                              .orElse(false)));
   }

   /**
    * Predicate checking if the HString returned from the given expression is a stop word
    * <pre>
    *    Usage:  isStopWord( expression )
    *    e.g.: isStopWord( @TOKEN ) -- check if the tokens on the current HString are stop words
    * </pre>
    *
    * @param expression the expression returning an HString to check is a stop word.
    * @return the LyreExpression
    */
   public static LyreExpression isStopWord(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: isStopWord only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("isStopWord", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(HString::toHString)
                                                              .map(h -> StopWords.isStopWord().test(h))
                                                              .orElse(false)));
   }

   /**
    * Predicate checking if the HString returned from the given expression is uppercase
    * <pre>
    *    Usage:  isUpper( expression )
    *    e.g.: isUpper( @TOKEN ) -- check if the tokens on the current HString are uppercase
    * </pre>
    *
    * @param expression the expression returning an HString to check is uppercase
    * @return the LyreExpression
    */
   public static LyreExpression isUpper(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: isUpper only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("isUpper", expression),
                                PREDICATE,
                                o -> processPred(expression.applyAsObject(o),
                                                 a -> Optional.ofNullable(a)
                                                              .map(Object::toString)
                                                              .map(Strings::isUpperCase)
                                                              .orElse(false)));
   }

   /**
    * Predicate checking if the HString returned from the given expression is all whitespace
    * <pre>
    *    Usage:  isWhitespace( expression )
    *    e.g.: isWhitespace( @TOKEN ) -- check if the tokens on the current HString are all whitespace
    * </pre>
    *
    * @param expression the expression returning an HString to check is all whitespace.
    * @return the LyreExpression
    */
   public static LyreExpression isWhitespace(@NonNull LyreExpression expression) {
      return new LyreExpression(formatMethod("isWhitespace", expression),
                                PREDICATE,
                                o -> processPred(o, a -> a != null && Strings.isNullOrBlank(a.toString())));
   }

   /**
    * Converts a list of values into features with L1-normalized counts.
    * <pre>
    *    Usage: L1{'prefix'}(expression)
    *    e.g.: L1{'WORD'}(@TOKEN) -- create bag-of-words with L1-normalized counts features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: L1(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: L1{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: L1 -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param prefix     the feature prefix (null is acceptable)
    * @param expression the expression returning the objects to convert into features
    * @return the LyreExpression
    */
   public static LyreExpression l1(String prefix, @NonNull LyreExpression expression) {
      return feature(prefix, ValueCalculator.L1, expression);
   }

   /**
    * Converts a list of values into features with L1-normalized counts.
    * <pre>
    *    Usage: L1{'prefix'}(expression)
    *    e.g.: L1{'WORD'}(@TOKEN) -- create bag-of-words with L1-normalized counts features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: L1(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: L1{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: L1 -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param prefix the feature prefix (null is acceptable)
    * @return the LyreExpression
    */
   public static LyreExpression l1(String prefix) {
      return feature(prefix, ValueCalculator.L1);
   }

   /**
    * Converts a list of values into features with L1-normalized counts.
    * <pre>
    *    Usage: L1{'prefix'}(expression)
    *    e.g.: L1{'WORD'}(@TOKEN) -- create bag-of-words with L1-normalized counts features over the tokens on the current HString
    *                                    with each feature having the prefix "WORD"
    *    e.g.: L1(@TOKEN) -- create bag-of-words binary features over the tokens on the current HString
    *    e.g.: L1{'BOW'} -- create bag-of-words binary features over the current HString with the prefix "BOW"
    *    e.g.: L1 -- create bag-of-words binary features over the current HString
    * </pre>
    *
    * @param expression the expression returning the objects to convert into features
    * @return the LyreExpression
    */
   public static LyreExpression l1(@NonNull LyreExpression expression) {
      return feature(ValueCalculator.L1, expression);
   }

   /**
    * Return the last element of a list expression or null if none.
    * <pre>
    *    Usage: last( list_expression )
    *    e.g.: last( @ENTITY ) -- return the last entity annotation on this HString
    * </pre>
    *
    * @param list the list expression
    * @return the LyreExpression
    */
   public static LyreExpression last(@NonNull LyreExpression list) {
      return new LyreExpression(formatMethod("last", list),
                                list.getType(),
                                o -> Iterables.getLast(list.applyAsList(o)).orElse(null));
   }

   /**
    * Gets the lemmatized form of the current HString resulting from the given expression.
    * <pre>
    *    Usage:  lemma( expression )
    *    e.g.: lemma( @TOKEN ) -- returns the lemmatized form all tokens on the current HString
    * </pre>
    *
    * @param expression the expression returning an HString to lemmatize
    * @return the LyreExpression
    */
   public static LyreExpression lemma(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: lemma only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("lemma", expression),
                                STRING,
                                o -> process(true, expression.applyAsObject(o), a -> toHString(a).getLemma()));
   }

   /**
    * Determines the length of the current Object (characters if CharSequence, elements if Collection)
    * <pre>
    *    Usage:  len( expression )
    *    e.g.: len( 'abc' ) -- returns the number of characters in the given literal
    *    e.g.: len( @TOKEN ) -- returns the number of tokens contained in the current HString
    * </pre>
    *
    * @param expression the expression returning an Object to calculate the length of
    * @return the LyreExpression
    */
   public static LyreExpression len(@NonNull LyreExpression expression) {
      return new LyreExpression(formatMethod("len", expression),
                                NUMERIC,
                                o -> {
                                   Object a = expression.applyAsObject(o);
                                   if(a == null) {
                                      return 0.0;
                                   }
                                   if(a instanceof Collection) {
                                      return (double) Cast.<Collection>as(a).size();
                                   }
                                   return (double) a.toString().length();
                                });
   }

   /**
    * Returns the {@link com.gengoai.hermes.lexicon.Lexicon} with the given name.
    * <pre>
    *    Usage:  %LEXICON_NAME
    *    e.g.: %person -- return the lexicon named "person"
    * </pre>
    *
    * @param name the name of the lexicon
    * @return the LyreExpression
    */
   public static LyreExpression lexicon(@NonNull String name) {
      return new LyreExpression("%" + name, OBJECT, o -> LexiconManager.getLexicon(name));
   }

   /**
    * Creates an array of expressions.
    * <pre>
    *    usage: [ expression1, expresion2, ..., expressionN ]
    *    e.g.: ['a', 'b', 'c'] -- returns a list of the given literal values.
    *
    *    Advanced Usage Example: $_ ~= [ @PHRASE_CHUNK, @ENTITY ]
    *    -- Applies the list of expressions to the current token resulting in a list of the phrase chunks and
    *    -- entities on the current HString
    *
    *    Advanced Usage Example: $_ ~= [ concat('W=', lower), concat('isDigit=', isDigit) ]
    *    -- Applies the list of expressions to the current token resulting in a list of Strings as follows:
    *    -- given $_ = 'test' the resulting array would be [ 'W=test', 'isDigit=false' ]
    * </pre>
    *
    * @param elements the array elements
    * @return the LyreExpression
    */
   public static LyreExpression list(@NonNull List<LyreExpression> elements) {
      return new LyreExpression(elements.stream()
                                        .map(Object::toString)
                                        .collect(Collectors.joining(", ", "[", "]")),
                                LyreExpressionType.determineCommonType(elements),
                                o -> {
                                   Object out = process(false, o, h -> elements.stream()
                                                                               .map(e -> e.applyAsObject(h))
                                                                               .collect(Collectors.toList()));
                                   if(out == null) {
                                      return Collections.emptyList();
                                   }
                                   if(!(out instanceof Collection)) {
                                      return Collections.singletonList(out);
                                   }
                                   return out;
                                });
   }

   /**
    * Lyre allows for string literals to be specified using single quotes (').
    * The backslash character can be use to escape a single quote if it is required in the literal.
    * <pre>
    *    usage: 'value'
    *    e.g.: 'now is the time...'
    * </pre>
    *
    * @param value the value
    * @return the LyreExpression
    */
   public static LyreExpression literal(@NonNull String value) {
      return new LyreExpression(String.format("'%s'", value), STRING, o -> value);
   }

   /**
    * Convenience method for constructing an array (list) of literal values.
    *
    * @param literals the literal values
    * @return the LyreExpression
    */
   public static LyreExpression literalArray(Iterator<String> literals) {
      return list(Streams.asStream(literals)
                         .map(LyreDSL::literal)
                         .collect(Collectors.toList()));
   }

   /**
    * Determines the length of the current Object treating it as a list.
    * <pre>
    *    Usage:  len( expression )
    *    e.g.: llen( 'abc' ) -- returns the 1
    *    e.g.: llen( ['a', 2]) -- returns 2
    * </pre>
    *
    * @param expression the expression returning an Object to calculate the length of
    * @return the LyreExpression
    */
   public static LyreExpression llen(@NonNull LyreExpression expression) {
      return new LyreExpression(formatMethod("llen", expression),
                                NUMERIC,
                                o -> {
                                   Object a = expression.applyAsObject(o);
                                   if(a == null) {
                                      return 0.0;
                                   }
                                   if(a instanceof Collection) {
                                      return (double) Cast.<Collection>as(a).size();
                                   }
                                   return 1;
                                });
   }

   /**
    * Return the longest (character length) element of a list expression or null if none.
    * <pre>
    *    Usage: longest( list_expression )
    *    e.g.: longest( @ENTITY ) -- return the entity annotation with the longest span on this HString
    * </pre>
    *
    * @param list the list expression
    * @return the LyreExpression
    */
   public static LyreExpression longest(@NonNull LyreExpression list) {
      return new LyreExpression(formatMethod("longest", list),
                                list.getType(),
                                o -> list.applyAsList(o)
                                         .stream()
                                         .max(comparingInt(h -> h.toString().length()))
                                         .orElse(null));
   }

   /**
    * Determines if the next annotation matches the given condition (Positive Lookahead).
    * <pre>
    *    usage: (?> expression)
    *    e.g.: (?> #VERB) -- returns true if the next Token is a verb
    * </pre>
    *
    * @param condition  the condition to be met by the next Token in order to evaluate to True
    * @param expression the expression
    * @return the LyreExpression
    */
   public static LyreExpression lookAhead(@NonNull LyreExpression condition, @NonNull LyreExpression expression) {
      return new LyreExpression(String.format("%s (?> %s)", expression, condition),
                                PREDICATE,
                                o -> {
                                   HString hString = toHString(o).next(Types.TOKEN);
                                   if(condition.test(hString)) {
                                      return expression.applyAsObject(o);
                                   }
                                   return null;
                                });
   }

   /**
    * Determines if the previous annotation matches the given expression (Positive Lookbehind).
    * <pre>
    *    usage: (?< expression)
    *    e.g.: (?< #VERB) -- returns true if the previous Token is a verb
    * </pre>
    *
    * @param condition  the condition to be met by the previous Token in order to evaluate to True
    * @param expression the expression
    * @return the LyreExpression
    */
   public static LyreExpression lookBehind(@NonNull LyreExpression condition, @NonNull LyreExpression expression) {
      return new LyreExpression(String.format("(?< %s) %s", condition, expression),
                                PREDICATE,
                                o -> {
                                   HString hString = toHString(o).previous(Types.TOKEN);
                                   if(condition.test(hString)) {
                                      return expression.applyAsObject(o);
                                   }
                                   return null;
                                });
   }

   /**
    * Gets the lower-cased string form of the current HString resulting from the given expression.
    * <pre>
    *    Usage:  lower( expression )
    *    e.g.: lower( @TOKEN ) -- returns the lower-cased string form  all tokens on the current HString
    * </pre>
    *
    * @param expression the expression returning an HString to lower case
    * @return the LyreExpression
    */
   public static LyreExpression lower(@NonNull LyreExpression expression) {
      return new LyreExpression(formatMethod("lower", expression),
                                STRING,
                                o -> process(true, expression.applyAsObject(o), a -> a.toString().toLowerCase()));
   }

   /**
    * Pads the left-side of CharSequence with a given pad-character ensuring that length of CharSequence is at least the
    * given minimum length.
    * <pre>
    *    Usage: lpad( expression, minimum_length, padding_character )
    *    e.g.: lpad( $_, 5, '*' ) -- add * to the front of the current HString until the length is at least 5
    *                                  characters long
    * </pre>
    *
    * @param expression       the expression resulting in the string to left-pad
    * @param minimumLength    the minimum length of the resulting padded string
    * @param paddingCharacter the character to use for padding
    * @return the LyreExpression
    */
   public static LyreExpression lpad(@NonNull LyreExpression expression, int minimumLength, char paddingCharacter) {
      return lpad(expression, number(minimumLength), literal(Character.toString(paddingCharacter)));
   }

   /**
    * Pads the left-side of CharSequence with a given pad-character ensuring that length of CharSequence is at least the
    * given minimum length.
    * <pre>
    *    Usage: lpad( expression, minimum_length, padding_character )
    *    e.g.: lpad( $_, 5, '*' ) -- add * to the front of the current HString until the length is at least 5
    *                                  characters long
    * </pre>
    *
    * @param expression       the expression resulting in the string to left-pad
    * @param minimumLength    the minimum length of the resulting padded string
    * @param paddingCharacter the character to use for padding
    * @return the LyreExpression
    */
   public static LyreExpression lpad(@NonNull LyreExpression expression,
                                     @NonNull LyreExpression minimumLength,
                                     @NonNull LyreExpression paddingCharacter) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: lpad expression only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      Validation.checkArgument(minimumLength.isInstance(NUMERIC),
                               "Illegal Expression: lpad minimumLength expression only accepts a NUMERIC, but '"
                                     + minimumLength + "' was provided which is of type " + minimumLength.getType());
      Validation.checkArgument(paddingCharacter.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: lpad paddingCharacter only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + paddingCharacter + "' was provided which is of type " + paddingCharacter.getType());
      return new LyreExpression(formatMethod("lpad", expression, minimumLength, paddingCharacter),
                                STRING,
                                o -> {
                                   int amt = (int) minimumLength.applyAsDouble(o);
                                   String pad = paddingCharacter.applyAsString(o);
                                   if(Strings.isNullOrBlank(pad) || pad.length() > 1) {
                                      throw new IllegalArgumentException("Invalid padding character: " + pad);
                                   }
                                   return process(true,
                                                  expression.applyAsObject(o),
                                                  h -> Strings.padStart(h.toString(), amt, pad.charAt(0)));
                                });
   }

   /**
    * Checks if the left-hand expression is less than the right-hand expression. Will default to string comparisons for
    * non-numeric values.
    * <pre>
    *    Usage: left_expression < right_expresison
    *    e.g.: $CONFIDENCE < 0.9 -- checks if the numeric confidence attributes is less than  0.9
    * </pre>
    *
    * @param left  the left hand expression
    * @param right the right hand expression
    * @return the LyreExpression
    */
   public static LyreExpression lt(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s < %s", left, right),
                                PREDICATE, o -> compareObjectPredicate(left.applyAsObject(o),
                                                                       right.applyAsObject(o),
                                                                       NumericComparison.LT));
   }

   /**
    * Checks if the left-hand expression is less than the right-hand expression. Will default to string comparisons for
    * non-numeric values.
    * <pre>
    *    Usage: left_expression < right_expresison
    *    e.g.: $CONFIDENCE < 0.9 -- checks if the numeric confidence attributes is less than  0.9
    * </pre>
    *
    * @param left  the left hand expression
    * @param value the right hand value
    * @return the LyreExpression
    */
   public static LyreExpression lt(@NonNull LyreExpression left, double value) {
      return lt(left, number(value));
   }

   /**
    * Checks if the left-hand expression is less than or equal to the right-hand expression. Will default to string
    * comparisons for non-numeric values.
    * <pre>
    *    Usage: left_expression <= right_expresison
    *    e.g.: $CONFIDENCE <= 0.9 -- checks if the numeric confidence attributes is less than or equal  to 0.9
    * </pre>
    *
    * @param left  the left hand expression
    * @param right the right hand expression
    * @return the LyreExpression
    */
   public static LyreExpression lte(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s <= %s", left, right),
                                PREDICATE, o -> compareObjectPredicate(left.applyAsObject(o),
                                                                       right.applyAsObject(o),
                                                                       NumericComparison.LTE));
   }

   /**
    * Checks if the left-hand expression is less than or equal to the right-hand expression. Will default to string
    * comparisons for non-numeric values.
    * <pre>
    *    Usage: left_expression <= right_expresison
    *    e.g.: $CONFIDENCE <= 0.9 -- checks if the numeric confidence attributes is less than or equal  to 0.9
    * </pre>
    *
    * @param left  the left hand expression
    * @param value the right hand value
    * @return the LyreExpression
    */
   public static LyreExpression lte(LyreExpression left, double value) {
      return lte(left, number(value));
   }

   /**
    * Applies the given operator to each element of the given list.
    * <pre>
    *    usage: map( list_expression, operator_expression )
    *    e.g.: map( @TOKEN, lower ) -- applies the <code>lower</code> operation to each token in the current HString
    *         ** Note this is the same as <code>lower(@TOKEN)</code>
    *    e.g.: map( @TOKEN, if(isDigit, '[:digit:]', $_) ) -- maps the if statement to each token in the current HString
    *         ** Note that this is different from if( isDigit(@TOKEN), '[:digit:]', this)
    * </pre>
    *
    * @param list     the list of items to apply the operator to
    * @param operator the operator to apply
    * @return the LyreExpression
    */
   public static LyreExpression map(@NonNull LyreExpression list, @NonNull LyreExpression operator) {
      return new LyreExpression(formatMethod("map", list, operator),
                                operator.getType(),
                                o -> recursiveListApply(list.applyAsList(o), operator::applyAsObject));
   }

   /**
    * Return the annotation in the list expression with maximum <code>confidence</code> as obtained via the
    * <code>CONFIDENCE</code> attribute or null if none.
    * <pre>
    *    Usage: max( list_expression )
    *    e.g.: max( @ENTITY ) -- return the entity annotation with the maximum confidence on this HString
    * </pre>
    *
    * @param list the list expression
    * @return the LyreExpression
    */
   public static LyreExpression max(@NonNull LyreExpression list) {
      return new LyreExpression(formatMethod("max", list),
                                list.getType(),
                                o -> list.applyAsList(o)
                                         .stream()
                                         .max(Comparator.comparingDouble(h -> (h instanceof HString)
                                                                              ? Cast.<HString>as(h)
                                                                                    .attribute(Types.CONFIDENCE, 0.0)
                                                                              : 0.0
                                                                        ))
                                         .orElse(null));
   }

   /**
    * Checks if the left and right expression are not equal using <code>.equals</code>, checking for content equality
    * for HStrings and literals, and using {@link Tag#isInstance(Tag)} for tags.
    * <pre>
    *    Usage: left_expression != right_expresison
    *    e.g.: $_ != 'man' -- checks if the current object is not equal to man
    * </pre>
    *
    * @param left  the left hand expression
    * @param right the right hand expression
    * @return the LyreExpression
    */
   public static LyreExpression ne(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s != %s", left, right),
                                PREDICATE, o -> compareObjectPredicate(left.applyAsObject(o),
                                                                       right.applyAsObject(o),
                                                                       NumericComparison.NE));
   }

   /**
    * Checks if the left and right expression are not equal using <code>.equals</code>, checking for content equality
    * for HStrings and literals, and using {@link Tag#isInstance(Tag)} for tags.
    * <pre>
    *    Usage: left_expression != right_expresison
    *    e.g.: $_ != 'man' -- checks if the current object is not equal to man
    * </pre>
    *
    * @param left  the left hand expression
    * @param value the right hand value
    * @return the LyreExpression
    */
   public static LyreExpression ne(@NonNull LyreExpression left, double value) {
      return ne(left, number(value));
   }

   /**
    * Determines if the next annotation does not match the given expression (Negative Lookahead).
    * <pre>
    *    usage: (?!> expression)
    *    e.g.: (?!> #VERB) -- returns true if the next Token is NOT a verb
    * </pre>
    *
    * @param condition  the condition that must NOT be met by the next Token in order to evaluate to True
    * @param expression the expression
    * @return the LyreExpression
    */
   public static LyreExpression negLookAhead(@NonNull LyreExpression condition, @NonNull LyreExpression expression) {
      return new LyreExpression(String.format("%s (?!> %s)", expression, condition),
                                PREDICATE,
                                o -> {
                                   HString hString = toHString(o).next(Types.TOKEN);
                                   if(!condition.test(hString)) {
                                      return expression.applyAsObject(o);
                                   }
                                   return null;
                                });
   }

   /**
    * Determines if the previous annotation does not match the given expression (Negative Lookbehind).
    * <pre>
    *    usage: (?!< expression)
    *    e.g.: (?!< #VERB) -- returns true if the previous Token is NOT a verb
    * </pre>
    *
    * @param condition  the condition that must NOT be met by the previous Token in order to evaluate to True
    * @param expression the expression
    * @return the LyreExpression
    */
   public static LyreExpression negLookBehind(@NonNull LyreExpression condition, @NonNull LyreExpression expression) {
      return new LyreExpression(String.format("(?!< %s) %s", condition, expression),
                                PREDICATE,
                                o -> {
                                   HString $ = expression.applyAsHString(toHString(o));
                                   HString hString = $.previous(Types.TOKEN);
                                   if(!condition.test(hString)) {
                                      return $;
                                   }
                                   return null;
                                });
   }

   /**
    * Returns <b>true</b> if none of the items in the given list evaluates to *true* for the given predicate expression.
    * <pre>
    *    usage: none( list_expression, predicate_expression )
    *    e.g.: none( @TOKEN, #VERB ) -- returns true if none of the tokens on the current HString is a verb
    * </pre>
    *
    * @param list      the list of items to apply the predicate to
    * @param predicate the predicate to use for testing
    * @return the LyreExpression
    */
   public static LyreExpression none(@NonNull LyreExpression list, @NonNull LyreExpression predicate) {
      return new LyreExpression(formatMethod("none", list, predicate),
                                PREDICATE,
                                o -> list.applyAsList(o).stream().noneMatch(predicate::testObject));
   }

   /**
    * Negates the given predicate expression
    * <pre>
    *    Usage: !predicate_expression
    *    e.g.: !isDigit -- checks if the current HString is not a digit
    * </pre>
    *
    * @param predicate the predicate expression to negate
    * @return the LyreExpression
    */
   public static LyreExpression not(@NonNull LyreExpression predicate) {
      return new LyreExpression(String.format("!%s", predicate),
                                PREDICATE,
                                o -> !predicate.testObject(o));
   }

   /**
    * Checks if the result a given expression is null or not and when it is not null returns the result and when is null
    * returns a default value.
    * <pre>
    *    Usage: nn( result_expression, default_value_expression )
    *    e.g.:  nn( @ENTITY, 'non-entity' ) -- returns all entities on the current HString or the string "non-entity" if
    *                                          none
    * </pre>
    *
    * @param expression   the expression whose result is being examined.
    * @param defaultValue the default value when the expression results in a null value
    * @return the LyreExpression
    */
   public static LyreExpression notNull(@NonNull LyreExpression expression, @NonNull LyreExpression defaultValue) {
      return new LyreExpression(formatMethod("nn", expression, defaultValue),
                                LyreExpressionType.determineCommonType(Arrays.asList(expression, defaultValue)),
                                o -> {
                                   Object o1 = expression.applyAsObject(o);
                                   if(o1 == null) {
                                      return defaultValue.applyAsObject(o);
                                   }
                                   return o1;
                                });
   }

   /**
    * Lyres accepts numerical literal values in the form of ints and doubles and allows for scientific notation.
    * <pre>
    *    e.g.: 123.4
    *    e.g.: 1e-5
    *    e.g.: -123.123
    * </pre>
    *
    * @param number the number
    * @return the LyreExpression
    */
   public static LyreExpression number(final double number) {
      return new LyreExpression(Double.toString(number), NUMERIC, o -> number);
   }

   /**
    * Returns true when the left-hand or right-hand expressions evaluate to true
    * <pre>
    *    Usage:  expresion || expresion
    *    e.g.: isContentWord || len > 3 -- returns true if the current HString is a content word or has 3 or more characters
    * </pre>
    *
    * @param left  the left-hand expresion to evaluate as a predicate
    * @param right the right-hand expresion to evaluate as a predicate
    * @return the LyreExpression
    */
   public static LyreExpression or(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s || %s", left, right),
                                PREDICATE,
                                o -> left.testObject(o) || right.testObject(o));
   }

   /**
    * Sequentially processes each expression with the input object, returning the result of the first expression that
    * evaluates to a non-null, non-empty list, or finite numeric value.
    * <pre>
    *    Usage: expression1 |> expression2 |> ... |> expresisonN
    * </pre>
    *
    * @param expressions The expressions to chain together.
    * @return the lyre expression
    */
   public static LyreExpression orPipe(@NonNull LyreExpression... expressions) {
      Validation.checkArgument(expressions.length > 1);
      return new LyreExpression(Stream.of(expressions)
                                      .map(Object::toString)
                                      .collect(Collectors.joining(" |> ")),
                                LyreExpressionType.determineCommonType(Arrays.asList(expressions)),
                                o -> {
                                   for(LyreExpression expression : expressions) {
                                      Object out = expression.applyAsObject(o);
                                      if(!isNullOrEmpty(out)) {
                                         return out;
                                      }
                                   }
                                   return null;
                                });
   }

   /**
    * The plus operator can be used to concatenate strings, perform addition on numeric values, or append to a list.
    *
    * @param expressions the expressions to concatenate
    * @return the LyreExpression
    */
   public static LyreExpression plus(@NonNull LyreExpression... expressions) {
      Validation.checkArgument(expressions.length > 1, "Must concatenate two or more expressions");
      LyreExpression toReturn = plus(expressions[0], expressions[1]);
      for(int i = 2; i < expressions.length; i++) {
         toReturn = plus(toReturn, expressions[i]);
      }
      return toReturn;
   }

   /**
    * The plus operator can be used to concatenate strings, perform addition on numeric values, or append to a list.
    *
    * @param left  the left expression to concatenate
    * @param right the right  expression to concatenate
    * @return the LyreExpression
    */
   public static LyreExpression plus(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("(%s + %s)", left, right),
                                LyreExpressionType.determineCommonType(Arrays.asList(left, right)),
                                o -> {
                                   Object l = left.applyAsObject(o);
                                   Object r = right.applyAsObject(o);
                                   if(l instanceof Collection<?>) {
                                      List<?> list = new ArrayList<>(Cast.as(l));
                                      if(r instanceof Collection<?>) {
                                         list.addAll(Cast.as(r));
                                      } else if(r != null) {
                                         list.add(Cast.as(r));
                                      }
                                      return list;
                                   }
                                   if(r instanceof Collection && l == null) {
                                      return r;
                                   }
                                   if(l instanceof HString && r instanceof HString) {
                                      return toHString(l).union(toHString(r));
                                   }
                                   if(l instanceof Number && r instanceof Number) {
                                      return Cast.<Number>as(l).doubleValue() + Cast.<Number>as(r).doubleValue();
                                   } else if(l == null && r == null) {
                                      return Collections.emptyList();
                                   } else if(l == null) {
                                      return r;
                                   } else if(r == null) {
                                      return l;
                                   }
                                   return l.toString() + r.toString();
                                });
   }

   /**
    * Gets the part-of-speech for the HString resulting from the given expression.
    * <pre>
    *    Usage:  pos( expression )
    *    e.g.: pos( @TOKEN ) -- returns the part-of-speech for all tokens on the current HString
    * </pre>
    *
    * @param expression the expression returning an HString whose part-of-speech we want
    * @return the LyreExpression
    */
   public static LyreExpression pos(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: pos only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("pos", expression),
                                OBJECT,
                                o -> process(true, expression.applyAsObject(o), a -> toHString(a).pos()));
   }

   private static List<?> recursiveListApply(Collection<?> c, Function<Object, ?> operator) {
      if(c == null) {
         return null;
      }
      List<Object> output = new ArrayList<>();
      for(Object o : c) {
         Object toAdd;
         if(o instanceof Collection) {
            toAdd = Lyre.postProcess(recursiveListApply(Cast.as(o), operator));
         } else {
            toAdd = Lyre.postProcess(operator.apply(o));
         }
         if(toAdd != null) {
            output.add(toAdd);
         }
      }
      return output;
   }

   /**
    * Creates a predicate expression to be applied to Strings testing them against the given regular expression
    * pattern.
    * <pre>
    *   usage: /java-regex/ig
    *   optional: i - case insensitive, g - match full span
    *   e.g.: /[aieou]/i -- match vowels in a case insensitive manner.
    * </pre>
    * To apply:
    * <pre>
    *    $_ ~= /[aieou]/i -- returns true if the current HString has at least on vowel in a case insensitive manner.
    * </pre>
    *
    * @param pattern the Java regular expression pattern (Note that when parsing lyre expects the form in usage
    *                statement)
    * @return the LyreExpression
    */
   public static LyreExpression regex(@NonNull String pattern) {
      return regex(pattern, false);
   }

   /**
    * Creates a predicate expression to be applied to Strings testing them against the given regular expression
    * pattern.
    * <pre>
    *   usage: /java-regex/ig
    *   optional: i - case insensitive, g - match full span
    *   e.g.: /[aieou]/i -- match vowels in a case insensitive manner.
    * </pre>
    * To apply:
    * <pre>
    *    $_ ~= /[aieou]/i -- returns true if the current HString has at least on vowel in a case insensitive manner.
    * </pre>
    *
    * @param pattern       the Java regular expression pattern (Note that when parsing lyre expects the form in usage
    *                      statement)
    * @param matchFullSpan True - the regular expression must match the full HString and not just a substring of it
    * @return the LyreExpression
    */
   public static LyreExpression regex(@NonNull String pattern, boolean matchFullSpan) {
      String str = "/" + FLAGS.matcher(pattern).replaceFirst("") + "/"
            + (Re.next(FLAGS.matcher(pattern)).contains("i")
               ? "i"
               : "")
            + (matchFullSpan
               ? "g"
               : "");
      final Pattern regex = Pattern.compile(pattern);
      return new LyreExpression(str,
                                PREDICATE,
                                o -> {
                                   HString hString = toHString(o);
                                   if(matchFullSpan) {
                                      return regex.matcher(hString).matches();
                                   }
                                   return regex.matcher(hString).find();
                                });
   }

   /**
    * Gets the annotation(s) having the given relation type in the given direction (INCOMING or OUTGOING).
    * <pre>
    *    Usage:  @>type -- annotations reachable from outgoing  given relation
    *    Usage:  @<type -- annotations reachable from incoming given relation
    *    e.g.: @>COREFERENCE -- return the annotation that the current token has a COREFERENCE relation with
    * </pre>
    *
    * @param direction the direction of the relation (INCOMING or OUTGOING)
    * @param type      the desired  relation
    * @return the LyreExpression
    */
   public static LyreExpression rel(@NonNull RelationDirection direction, @NonNull RelationType type) {
      return rel(direction, type, null, $_);
   }

   /**
    * Gets the annotation(s) having the the given relation type with the given relation value in the given direction
    * (INCOMING or OUTGOING).
    * <pre>
    *    Usage:  @>type{'relation'} -- annotations reachable from outgoing relation of given type and value
    *    Usage:  @<type{'relation'} -- annotations  reachable from incoming relation of given type and value
    *    e.g.: @>COREFERNCE{'pronominal'} -- return the annotation having a COREFERENCE relation with value pronominal
    * </pre>
    *
    * @param direction the direction of the relation (INCOMING or OUTGOING)
    * @param type      the desired dependency relation
    * @param relation  the desired relation value
    * @return the LyreExpression
    */
   public static LyreExpression rel(@NonNull RelationDirection direction, @NonNull RelationType type, String relation) {
      return rel(direction, type, relation, $_);
   }

   /**
    * Gets the annotation(s) having the given relation type in the given direction (INCOMING or OUTGOING).
    * <pre>
    *    Usage:  @>type{'relation'} -- annotations reachable from outgoing relation of given type and value
    *    Usage:  @<type{'relation'} -- annotations reachable from incoming relation of given type and value
    *    e.g.: @>COREFERNCE{'pronominal'} -- return the annotation having a COREFERENCE relation with value pronominal
    * </pre>
    *
    * @param direction  the direction of the relation (INCOMING or OUTGOING)
    * @param type       the desired dependency relation
    * @param expression the expression returning the HString to extract dependency relations over
    * @return the LyreExpression
    */
   public static LyreExpression rel(@NonNull RelationDirection direction,
                                    @NonNull RelationType type,
                                    @NonNull LyreExpression expression) {
      return rel(direction, type, null, expression);
   }

   /**
    * Gets the annotation(s) having the the given relation type with the given relation value in the given direction
    * (INCOMING or OUTGOING).
    * <pre>
    *    Usage:  @>type{'relation'}(expression) -- annotations reachable from outgoing relation of given type and value
    *    Usage:  @<type{'relation'}(expression) -- annotations reachable from incoming relation of given type and value
    *    e.g.: @>COREFERNCE{'pronominal'}(@PHRASE_CHUNK) -- return the annotation that the phrase chunks on the current token
    *                                                       have an COREFERENCE relation with value pronominal
    * </pre>
    *
    * @param direction  the direction of the relation (INCOMING or OUTGOING)
    * @param type       the desired dependency relation
    * @param relation   the desired relation value
    * @param expression the expression returning the HString to extract dependency relations over
    * @return the LyreExpression
    */
   public static LyreExpression rel(@NonNull RelationDirection direction,
                                    @NonNull RelationType type,
                                    String relation,
                                    @NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: rel only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod(String.format("@%s%s%s",
                                                           direction == OUTGOING
                                                           ? ">"
                                                           : "<",
                                                           type.name(),
                                                           Strings.isNullOrBlank(relation)
                                                           ? ""
                                                           : "{" + relation + "}"),
                                             expression),
                                HSTRING,
                                o -> process(true, expression.applyAsObject(o), a -> {
                                   HString h = toHString(a);
                                   if(direction == OUTGOING) {
                                      if(Strings.isNullOrBlank(relation)) {
                                         return h.outgoing(type);
                                      }
                                      return h.outgoing(type, relation);
                                   } else {
                                      if(Strings.isNullOrBlank(relation)) {
                                         return h.incoming(type);
                                      }
                                      return h.incoming(type, relation);
                                   }
                                })
      );
   }

   /**
    * Pads the right-side of CharSequence with a given pad-character ensuring that length of CharSequence is at least
    * the given minimum length.
    * <pre>
    *    Usage: rpad( expression, minimum_length, padding_character )
    *    e.g.: rpad( $_, 5, '*' ) -- add * to the end of the current HString until the length is at least 5
    *                                  characters long
    * </pre>
    *
    * @param expression       the expression resulting in the string to left-pad
    * @param minimumLength    the minimum length of the resulting padded string
    * @param paddingCharacter the character to use for padding
    * @return the LyreExpression
    */
   public static LyreExpression rpad(@NonNull LyreExpression expression, int minimumLength, char paddingCharacter) {
      return rpad(expression, number(minimumLength), literal(Character.toString(paddingCharacter)));
   }

   /**
    * Pads the right-side of CharSequence with a given pad-character ensuring that length of CharSequence is at least
    * the given minimum length.
    * <pre>
    *    Usage: rpad( expression, minimum_length, padding_character )
    *    e.g.: rpad( $_, 5, '*' ) -- add * to the end of the current HString until the length is at least 5
    *                                  characters long
    * </pre>
    *
    * @param expression       the expression resulting in the string to left-pad
    * @param minimumLength    the minimum length of the resulting padded string
    * @param paddingCharacter the character to use for padding
    * @return the LyreExpression
    */
   public static LyreExpression rpad(@NonNull LyreExpression expression,
                                     @NonNull LyreExpression minimumLength,
                                     @NonNull LyreExpression paddingCharacter) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: rpad expression only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      Validation.checkArgument(minimumLength.isInstance(NUMERIC),
                               "Illegal Expression: rpad minimumLength expression only accepts a NUMERIC, but '"
                                     + minimumLength + "' was provided which is of type " + minimumLength.getType());
      Validation.checkArgument(paddingCharacter.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: rpad paddingCharacter only accepts a HSTRING, STRING, or OBJECT, but '"
                                     + paddingCharacter + "' was provided which is of type " + paddingCharacter.getType());
      return new LyreExpression(formatMethod("rpad", expression, minimumLength, paddingCharacter),
                                STRING,
                                o -> {
                                   int amt = (int) minimumLength.applyAsDouble(o);
                                   String pad = paddingCharacter.applyAsString(o);
                                   if(Strings.isNullOrBlank(pad) || pad.length() > 1) {
                                      throw new IllegalArgumentException("Invalid padding character: " + pad);
                                   }
                                   return process(true,
                                                  expression.applyAsObject(o),
                                                  h -> Strings.padEnd(h.toString(), amt, pad.charAt(0)));
                                });
   }

   /**
    * Creates a regular expression substitution expression to be applied to Strings.
    *
    * <pre>
    *   usage: /java-regex/replacement/ig
    *   optional: i - case insensitive, g - replace all matches
    *   e.g.: /[aieou]/v/i -- replace all vowels with a v in a case insensitive manner.
    * </pre>
    * To apply:
    * <pre>
    *    $_ ~= /[aieou]/v/i  -- replace all vowels with a v in the current HString in a case insensitive manner.
    * </pre>
    *
    * @param pattern     the Java regular expression pattern (Note that when parsing lyre expects the form in usage
    *                    statement)
    * @param replacement the string to replace the matched sections with
    * @return the LyreExpression
    */
   public static LyreExpression rsub(@NonNull String pattern, @NonNull String replacement) {
      return rsub(pattern, replacement, true);
   }

   /**
    * Creates a regular expression substitution expression to be applied to Strings.
    *
    * <pre>
    *   usage: /java-regex/replacement/ig
    *   optional: i - case insensitive, g - replace all matches
    *   e.g.: /[aieou]/v/i -- replace all vowels with a v in a case insensitive manner.
    * </pre>
    * To apply:
    * <pre>
    *    $_ ~= /[aieou]/v/i  -- replace all vowels with a v in the current HString in a case insensitive manner.
    * </pre>
    *
    * @param pattern     the Java regular expression pattern (Note that when parsing lyre expects the form in usage
    *                    statement)
    * @param replacement the string to replace the matched sections with
    * @param replaceAll  True - replace all matches in the HString
    * @return the LyreExpression
    */
   public static LyreExpression rsub(@NonNull String pattern, @NonNull String replacement, boolean replaceAll) {
      String str = "/" + FLAGS.matcher(pattern).replaceFirst("") + "/"
            + replacement + "/"
            + (Re.next(FLAGS.matcher(pattern)).contains("i")
               ? "i"
               : "")
            + (replaceAll
               ? "g"
               : "");
      final Pattern regex = Pattern.compile(pattern);
      return new LyreExpression(str,
                                STRING,
                                o -> {
                                   HString hString = toHString(o);
                                   if(replaceAll) {
                                      return regex.matcher(hString).replaceAll(replacement);
                                   }
                                   return regex.matcher(hString).replaceFirst(replacement);
                                });
   }

   /**
    * Performs a slice (substring and sublist) on Strings and Collections.
    * <pre>
    *    Usage: expression[start:end] -- Performs a slice from start to end on the output of the given expression
    *    e.g.:  'abc'[:-1] -- return the substring of 'abc' starting at 0 and going to length-1
    * </pre>
    *
    * @param expression the expression to take substring of
    * @param start      the start offset
    * @return the LyreExpression
    */
   public static LyreExpression slice(@NonNull LyreExpression expression, int start) {
      return slice(expression, start, 0);
   }

   /**
    * Performs a slice (substring and sublist) on Strings and Collections.
    * <pre>
    *    Usage: expression[start:end] -- Performs a slice from start to end on the output of the given expression
    *    e.g.:  'abc'[:-1] -- return the substring of 'abc' starting at 0 and going to length-1
    * </pre>
    *
    * @param expression the expression to take substring of
    * @param start      the start offset
    * @param end        the end offset
    * @return the LyreExpression
    */
   public static LyreExpression slice(@NonNull LyreExpression expression, int start, int end) {
      return new LyreExpression(String.format("%s[%s:%s]", expression, start, end),
                                expression.getType(),
                                obj -> {
                                   Object o = expression.applyAsObject(obj);
                                   if(o == null) {
                                      return null;
                                   }
                                   if(o instanceof Collection) {
                                      List<?> list = Lists.asArrayList(Cast.<Iterable<?>>as(o));
                                      if(list.isEmpty()) {
                                         return list;
                                      }
                                      int s = start >= 0
                                              ? start
                                              : Math.max(0, list.size() + start);
                                      int e = Math.min(end > 0
                                                       ? end
                                                       : Math.max(0, list.size() + end),
                                                       list.size());
                                      if(e - s == 1) {
                                         return list.get(s);
                                      }
                                      return list.subList(s, e);
                                   }
                                   int s = start >= 0
                                           ? start
                                           : Math.max(0, o.toString().length() + start);
                                   int e = end > 0
                                           ? Math.min(end, o.toString().length())
                                           : Math.max(0, o.toString().length() + end);
                                   if(o instanceof HString) {
                                      return Cast.<HString>as(o).substring(s, e);
                                   }
                                   if(s >= o.toString().length()) {
                                      return null;
                                   }
                                   return o.toString().substring(s, e);
                                });
   }

   /**
    * Gets the stemmed form of the current HString resulting from the given expression.
    * <pre>
    *    Usage:  stem( expression )
    *    e.g.: stem( @TOKEN ) -- returns the stemmed form all tokens on the current HString
    * </pre>
    *
    * @param expression the expression returning an HString to stem
    * @return the LyreExpression
    */
   public static LyreExpression stem(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: stem only accepts a HSTRING, STRING, OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("stem", expression),
                                STRING,
                                o -> process(true, expression.applyAsObject(o),
                                             a -> toHString(a).getStemmedForm()));
   }

   /**
    * Gets the string form of the Object resulting from the given expression.
    * <pre>
    *    Usage:  string( expression )
    *    e.g.: string( 123.5 ) -- returns the string form of the numerical literal
    * </pre>
    *
    * @param expression the expression returning an Object to turn into a string
    * @return the LyreExpression
    */
   public static LyreExpression string(@NonNull LyreExpression expression) {
      return new LyreExpression(formatMethod("string", expression),
                                STRING,
                                o -> process(true, expression.applyAsObject(o), Object::toString));
   }

   /**
    * Checks if the Tag on the current HString is of the given Tag value.
    * <pre>
    *    Usage: #TAG_VAlUE
    *    e.g.: #PERSON -- check if the tag has value PERSON
    * </pre>
    *
    * @param value the tag value to test for
    * @return the LyreExpression
    */
   public static LyreExpression tag(@NonNull Tag value) {
      return tag(value, $_);
   }

   /**
    * Checks if the Tag on the current HString is of the given Tag value.
    * <pre>
    *    Usage: #TAG_VAlUE
    *    e.g.: #PERSON -- check if the tag has value PERSON
    * </pre>
    *
    * @param value the tag value to test for
    * @return the LyreExpression
    */
   public static LyreExpression tag(@NonNull String value) {
      return tag(value, $_);
   }

   /**
    * Checks if the Tag on the HString resulting from the given expression is of the given Tag value.
    * <pre>
    *    Usage: #TAG_VAlUE( expression )
    *    e.g.: #PERSON( @ENTITIY ) -- check if the tag of the entity on the current HString has value PERSON
    * </pre>
    *
    * @param value      the tag value to test for
    * @param expression the expression resulting in the HString to check the tag of
    * @return the LyreExpression
    */
   public static LyreExpression tag(@NonNull Tag value, @NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: tag only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod(String.format("#%s", value.name()), expression),
                                PREDICATE,
                                o -> process(true,
                                             expression.applyAsObject(o),
                                             h -> toHString(h).asAnnotation().tagIsA(value)));
   }

   /**
    * Checks if the Tag on the HString resulting from the given expression is of the given Tag value.
    * <pre>
    *    Usage: #TAG_VAlUE( expression )
    *    e.g.: #PERSON( @ENTITIY ) -- check if the tag of the entity on the current HString has value PERSON
    * </pre>
    *
    * @param value      the tag value to test for
    * @param expression the expression resulting in the HString to check the tag of
    * @return the LyreExpression
    */
   public static LyreExpression tag(@NonNull String value, @NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: tag only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod(String.format("#%s", value), expression),
                                PREDICATE,
                                o -> process(true,
                                             expression.applyAsObject(o),
                                             h -> {
                                                Annotation annotation = toHString(h).asAnnotation();
                                                if(annotation.tagIsA(value)) {
                                                   return true;
                                                }
                                                try {
                                                   return annotation.pos().isInstance(POS.fromString(value));
                                                } catch(IllegalArgumentException e) {
                                                   return false;
                                                }
                                             }));
   }

   /**
    * Returns the length of the underlying HString in Tokens
    * <pre>
    *    Usage: tlen( object_expression )
    *    e.g.: tlen( @PHRASE_CHUNK ) -- returns the number of tokens per phrase chunk
    * </pre>
    *
    * @param expression the expression resulting in the HString object to calculate the token length of
    * @return the LyreExpression
    */
   public static LyreExpression tlen(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING),
                               "Illegal Expression: tlen only accepts a HSTRING or STRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression("tokenLen", NUMERIC, o -> toHString(expression.applyAsObject(o)).tokenLength());
   }

   /**
    * Removes tokens from the left and right of the input HString if they evaluate to true with the given predicate.
    * <pre>
    *    Usage: trim( object_expression, predicate_expression )
    *    e.g.: trim( $_, isStopWord || len < 3 ) -- remove tokens that are stopwords or have a length less than three
    *                                                 from the current HString
    * </pre>
    *
    * @param expression the expression resulting in the HString object to trim
    * @param predicate  the predicate expression to use for trimming.
    * @return the LyreExpression
    */
   public static LyreExpression trim(@NonNull LyreExpression expression, @NonNull LyreExpression predicate) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: trim only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("trim", expression, predicate),
                                expression.getType(),
                                o -> process(true,
                                             expression.applyAsObject(o),
                                             h -> toHString(h).trim(predicate)));
   }

   /**
    * Gets the universal part-of-speech for the HString resulting from the given expression.
    * <pre>
    *    Usage:  upos( expression )
    *    e.g.: upos( @TOKEN ) -- returns the universal part-of-speech for all tokens on the current HString
    * </pre>
    *
    * @param expression the expression returning an HString whose universal part-of-speech we want
    * @return the LyreExpression
    */
   public static LyreExpression upos(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING),
                               "Illegal Expression: upos only accepts a HSTRING, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("upos", expression),
                                OBJECT,
                                o -> process(true,
                                             expression.applyAsObject(o),
                                             a -> toHString(a).pos().getUniversalTag()));
   }

   /**
    * Gets the upper-cased string form of the current HString resulting from the given expression.
    * <pre>
    *    Usage:  upper( expression )
    *    e.g.: upper( @TOKEN ) -- returns the upper-cased string form  all tokens on the current HString
    * </pre>
    *
    * @param expression the expression returning an HString to upper case
    * @return the LyreExpression
    */
   public static LyreExpression upper(@NonNull LyreExpression expression) {
      Validation.checkArgument(expression.isInstance(HSTRING, STRING, OBJECT),
                               "Illegal Expression: upper only accepts a HSTRING, STRING, OBJECT, but '"
                                     + expression + "' was provided which is of type " + expression.getType());
      return new LyreExpression(formatMethod("upper", expression),
                                STRING,
                                o -> process(true, expression.applyAsObject(o), a -> a.toString().toUpperCase()));
   }

   /**
    * Performs the given expression when the given condition is true and returns null when the condition is false.
    * <pre>
    *    Usage: when( condition_expresison, true_expression )
    *    e.g.: when( len > 3, 's3=' + $_[-3:] ) -- return the trigram suffix only when the length of the current
    *                                                HString is greater than 3
    * </pre>
    *
    * @param condition  the condition expression
    * @param expression the expression to perform when the condition evaluates to true
    * @return the LyreExpression
    */
   public static LyreExpression when(@NonNull LyreExpression condition, @NonNull LyreExpression expression) {
      return new LyreExpression(formatMethod("when", condition, expression),
                                expression.getType(),
                                o -> {
                                   if(condition.testObject(o)) {
                                      return expression.applyAsObject(o);
                                   }
                                   return null;
                                });
   }

   /**
    * Constructs a temporary word list to use as a predicate or as a container for checking for the existence of words
    * and phrases.
    *
    * @param wordList the word list
    * @return the LyreExpression
    */
   public static LyreExpression wordList(@NonNull WordList wordList) {
      return new LyreExpression(formatMethod("wordList", literalArray(wordList.iterator())),
                                OBJECT,
                                o -> wordList);
   }

   /**
    * Constructs a temporary word list to use as a predicate or as a container for checking for the existence of words
    * and phrases.
    *
    * @param wordList the word list expression
    * @return the LyreExpression
    */
   public static LyreExpression wordList(@NonNull LyreExpression wordList) {
      final WordList wl = new SimpleWordList(Sets.asHashSet(wordList.applyAsList(null, String.class)));
      return new LyreExpression(formatMethod("wordList", wordList),
                                OBJECT,
                                o -> wl);
   }

   /**
    * Returns true when the left-hand or right-hand expressions evaluate to true but not both
    * <pre>
    *    Usage:  expresion ^ expresion
    *    e.g.: isContentWord ^ len > 3 -- returns true if the current HString is a content word or has 3 or more characters
    *                                     but not both
    * </pre>
    *
    * @param left  the left-hand expresion to evaluate as a predicate
    * @param right the right-hand expresion to evaluate as a predicate
    * @return the LyreExpression
    */
   public static LyreExpression xor(@NonNull LyreExpression left, @NonNull LyreExpression right) {
      return new LyreExpression(String.format("%s ^ %s", left, right),
                                PREDICATE,
                                o -> left.testObject(o) ^ right.testObject(o));
   }

}//END OF LyreDSL
