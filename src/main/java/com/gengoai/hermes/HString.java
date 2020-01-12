package com.gengoai.hermes;

import com.gengoai.Language;
import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.apollo.ml.LabeledDatum;
import com.gengoai.apollo.ml.LabeledSequence;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.Iterators;
import com.gengoai.collection.Streams;
import com.gengoai.collection.tree.Span;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.extraction.regex.TokenMatcher;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.hermes.morphology.Stemmers;
import com.gengoai.reflection.TypeUtils;
import com.gengoai.string.StringLike;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.Validation.checkArgument;
import static com.gengoai.Validation.notNull;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p> Represents a java string on steroids. HStrings act as a <code>CharSequence</code> and have methods similar to
 * those on <code>String</code>. Methods that do not modify the underlying string representation, e.g. substring, find,
 * will return another HString whereas methods that modify the string, e.g. lower casing, return a String object.
 * Additionally, HStrings have a span (i.e. start and end positions), attributes, and annotations. HStrings allow for
 * methods to process documents, fragments, and annotations in a uniform fashion. Methods on the HString facilitate
 * determining if the object is an annotation ({@link #isAnnotation()} or is a document ({@link #isDocument()}). </p>
 *
 * <p>
 * An <code>AttributedObject</code> is an object that has zero or more attributes associated with it. An attribute may
 * represent a learned property (e.g. part of speech or word sense), metadata (e.g. author or source url), or a feature
 * (e.g. suffix or phonetic encoding).
 * </p>
 *
 * <p>An object that can has relations (edges) defined between itself and other objects. Convenience methods ({@link
 * #parent()}***, {@link #enclosedAnnotations()}, {@link #dependency()}) exist for dealing with dependency relations,
 * which are the most common relation used in Hermes. Examples of other relations include, co-reference, lexical chains,
 * and discourse structure.</p>
 *
 * @author David B. Bracewell
 */
public interface HString extends Span, StringLike, Serializable {
   /**
    * Helper function for converting an Object into an HString. Will construct fragments for nulls and strings. Objects
    * not convertible into HStrings result in detached empty annotaitons.
    *
    * @param o the object to convert
    * @return the HString result
    */
   static HString toHString(Object o) {
      if (o == null) {
         return Fragments.detachedEmptyAnnotation();
      }
      if (o instanceof HString) {
         return Cast.as(o);
      }
      if (o instanceof CharSequence) {
         return Fragments.string(o.toString());
      }
      return Fragments.detachedEmptyAnnotation();
   }

   /**
    * Creates a new string by performing a union over the spans of two or more HStrings. The new HString will have a
    * span that starts at the minimum starting position of the given strings and end at the maximum ending position of
    * the given strings.
    *
    * @param first  the first HString
    * @param second the second HString
    * @param others the other HStrings to union
    * @return A new HString representing the union over the spans of the given HStrings.
    */
   static HString union(@NonNull HString first, @NonNull HString second, @NonNull HString... others) {
      return union(() -> Iterators.concat(Collections.singleton(first),
                                          Collections.singleton(second),
                                          Arrays.asList(others)));
   }

   /**
    * Creates a new string by performing a union over the spans of two or more HStrings. The new HString will have a
    * span that starts at the minimum starting position of the given strings and end at the maximum ending position of
    * the given strings.
    *
    * @param strings the HStrings to union
    * @return A new HString representing the union over the spans of the given HStrings.
    */
   static HString union(@NonNull Iterable<? extends HString> strings) {
      int start = Integer.MAX_VALUE;
      int end = Integer.MIN_VALUE;
      Document owner = null;
      for (HString hString : strings) {
         if (!hString.isEmpty()) {
            if (owner == null && hString.document() != null) {
               owner = hString.document();
            } else if (hString.document() == null || owner != hString.document()) {
               throw new IllegalArgumentException("Cannot union strings from different documents");
            }
            start = Math.min(start, hString.start());
            end = Math.max(end, hString.end());
         }
      }
      if (start < 0 || start >= end) {
         return Fragments.empty(owner);
      }
      return Fragments.fragment(owner, start, end);
   }

   /**
    * Adds an outgoing relation to the object.
    *
    * @param relation the relation to add
    */
   void add(Relation relation);

   /**
    * Adds multiple outgoing relations to the object.
    *
    * @param relations the relations to add
    */
   default void addAll(@NonNull Iterable<Relation> relations) {
      relations.forEach(this::add);
   }

   /**
    * <p>Constructs a relation graph with the given relation types as the edges and the given annotation types as the
    * vertices (the {@link #interleaved(AnnotationType...)} method is used to get the annotations). Relations will be
    * determine for annotations by including the relations of their sub-annotations (i.e. sub-spans). This allows, for
    * example, a dependency graph to be built over other annotation types, e.g. phrase chunks.</p>
    *
    * @param relationTypes   the relation types making up the edges
    * @param annotationTypes annotation types making up the vertices
    * @return the relation graph
    */
   default RelationGraph annotationGraph(Tuple relationTypes, AnnotationType... annotationTypes) {
      RelationGraph g = new RelationGraph();
      g.addVertices(interleaved(annotationTypes));
      Set<RelationType> relationTypeList = Streams.asStream(relationTypes.iterator())
                                                  .filter(r -> r instanceof RelationType)
                                                  .map(Cast::<RelationType>as)
                                                  .collect(Collectors.toSet());
      for (Annotation source : g.vertices()) {
         for (Relation relation : source.outgoingRelations(true)) {
            if (!relationTypeList.contains(relation.getType())) {
               continue;
            }
            relation.getTarget(this)
                    .map(target -> g.containsVertex(target)
                                   ? target
                                   : target.annotationStream()
                                           .filter(g::containsVertex)
                                           .findFirst()
                                           .orElse(null))
                    .ifPresent(target -> {
                       if (!g.containsEdge(source, target)) {
                          RelationEdge edge = g.addEdge(source, target);
                          edge.setRelation(relation.getValue());
                          edge.setRelationType(relation.getType());
                       }
                    });
         }
      }
      return g;
   }

   /**
    * Gets a java Stream over all annotations overlapping this object.
    *
    * @return the stream of annotations
    */
   default Stream<Annotation> annotationStream() {
      return annotations().stream();
   }

   /**
    * Gets a java Stream over annotations of the given type overlapping this object.
    *
    * @param type the type of annotation making up the stream
    * @return the stream of given annotation type
    */
   default Stream<Annotation> annotationStream(@NonNull AnnotationType type) {
      return annotations(type).stream();
   }

   /**
    * Gets all annotations overlapping this object
    *
    * @return all annotations overlapping with this object.
    */
   default List<Annotation> annotations() {
      if (document() == null) {
         return Collections.emptyList();
      }
      return document().annotations(AnnotationType.ROOT, this);
   }

   /**
    * Gets annotations of a given type and that test positive for the given filter that overlap with this object.
    *
    * @param type   the type of annotation wanted
    * @param filter The filter that annotations must pass in order to be accepted
    * @return the list of annotations of given type meeting the given filter that overlap with this object
    */
   default List<Annotation> annotations(@NonNull AnnotationType type, @NonNull Predicate<? super Annotation> filter) {
      if (document() == null || type == null || filter == null) {
         return Collections.emptyList();
      }
      return document().annotations(type, this, filter);
   }

   /**
    * Gets annotations of a given type that overlap with this object.
    *
    * @param type the type of annotation wanted
    * @return the list of annotations of given type that overlap with this object
    */
   default List<Annotation> annotations(@NonNull AnnotationType type) {
      if (document() == null || type == null) {
         return Collections.emptyList();
      }
      return annotations(type, annotation -> annotation.isInstance(type) && annotation.overlaps(this));
   }

   default boolean anyMatch(@NonNull SerializablePredicate<? super Annotation> predicate,
                            @NonNull AnnotationType... types) {
      return interleaved(types).stream().anyMatch(predicate);
   }

   /**
    * Gets this HString as an annotation. If the HString is already an annotation it is simply cast. Otherwise a
    * detached annotation of type <code>AnnotationType.ROOT</code> is created.
    *
    * @return An annotation.
    */
   default Annotation asAnnotation() {
      if (this instanceof Annotation) {
         return Cast.as(this);
      } else if (document() != null) {
         return document()
            .annotationBuilder(AnnotationType.ROOT)
            .from(this)
            .createDetached();
      }
      return Fragments.detachedAnnotation(AnnotationType.ROOT, start(), end());
   }

   default Annotation asAnnotation(@NonNull AnnotationType type) {
      if (this instanceof Annotation && asAnnotation().isInstance(type)) {
         return Cast.as(this);
      } else if (document() != null) {
         return document()
            .annotationBuilder(type)
            .from(this)
            .createDetached();
      }
      return Fragments.detachedAnnotation(type, start(), end());
   }

   /**
    * Creates a labeled data point from this HString applying the given label function to determine the label, i.e.
    * class.
    *
    * @param labelFunction the label function to determine the label (or class) of the data point (e.g. part-of-speech,
    *                      sentiment, etc.)
    * @return the labeled datum
    */
   default LabeledDatum<HString> asLabeledData(@NonNull Function<HString, ?> labelFunction) {
      return LabeledDatum.of(labelFunction.apply(this), this);
   }

   /**
    * Creates a labeled data point from this HString using the value of the given attribute type as the label, i.e.
    * class.
    *
    * @param <T>                the type parameter
    * @param attributeTypeLabel the attribute type whose value will become the label of the data point
    * @return the labeled datum
    */
   default <T> LabeledDatum<HString> asLabeledData(@NonNull AttributeType<T> attributeTypeLabel) {
      return LabeledDatum.of(attribute(attributeTypeLabel), this);
   }

   /**
    * Creates a labeled {@link LabeledSequence} of tokens from this HString applying the given label function to each
    * token to determine its label, i.e. class..
    *
    * @param labelFunction the label function to determine the label (or class) of the data point (e.g. part-of-speech,
    *                      sentiment, etc.)
    * @return the sequence input
    */
   default LabeledSequence<Annotation> asLabeledSequence(@NonNull Function<? super Annotation, String> labelFunction) {
      LabeledSequence<Annotation> si = new LabeledSequence<>();
      for (Annotation token : tokens()) {
         si.add(labelFunction.apply(token), token);
      }
      return si;
   }

   /**
    * Creates a labeled {@link LabeledSequence} of tokens from this HString using the given attribute type as the
    * labels.
    *
    * @param attributeTypeLabel the attribute type whose value will become the label of the data point
    * @return the sequence input
    */
   default LabeledSequence<Annotation> asLabeledSequence(@NonNull AttributeType<String> attributeTypeLabel) {
      return asLabeledSequence(a -> a.attribute(attributeTypeLabel));
   }

   /**
    * Gets the value for a given attribute type
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @return the value associated with the attribute or null
    */
   default <T> T attribute(@NonNull AttributeType<T> attributeType) {
      return attributeMap().get(attributeType);
   }

   /**
    * Gets the value for a given attribute type
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @param defaultValue  the defualt value
    * @return the value associated with the attribute or null
    */
   default <T> T attribute(@NonNull AttributeType<T> attributeType, T defaultValue) {
      return attributeMap().getOrDefault(attributeType, defaultValue);
   }

   /**
    * Checks if the HString has an attribute of the given type that is <code>equal</code> to the given target value is
    * used.
    *
    * @param <T>           the attribute type parameter
    * @param attributeType the attribute value to check
    * @param targetValue   the value we are checking if this string's attribute value is equal
    * @return True if the HString has the attribute and it is equal to the given target value
    */
   default <T> boolean attributeEquals(@NonNull AttributeType<T> attributeType, Object targetValue) {
      return hasAttribute(attributeType) && attribute(attributeType).equals(attributeType.decode(targetValue));
   }

   /**
    * Checks if the HString has an attribute of the given type that <code>is a</code> instance of the given target
    * value. When the attribute type is a Tag, the <code>isInstance</code> method is used otherwise <code>equals</code>
    * is used.
    *
    * @param <T>           the attribute type parameter
    * @param attributeType the attribute value to check
    * @param targetValue   the value we are checking if this string's attribute value is an instance of
    * @return True if the HString has the attribute and it is an instance of the given target value
    */
   default <T> boolean attributeIsA(@NonNull AttributeType<T> attributeType, Object targetValue) {
      if (hasAttribute(attributeType)) {
         if (Tag.class.isAssignableFrom(TypeUtils.asClass(attributeType.getValueType()))) {
            Tag myTag = (Tag) attribute(attributeType);
            return myTag.isInstance((Tag) attributeType.decode(targetValue));
         }
         return attribute(attributeType).equals(attributeType.decode(targetValue));
      }
      return false;
   }

   /**
    * Exposes the underlying attributes as a Map
    *
    * @return The attribute names and values as a map
    */
   AttributeMap attributeMap();

   default Set<String> categories() {
      return categories(true);
   }

   default Set<String> categories(boolean includeSuperAnnotations) {
      Set<String> categories = new HashSet<>(attribute(Types.CATEGORY, Collections.emptySet()));
      if (includeSuperAnnotations) {
         annotationStream()
            //.filter(a -> !this.encloses(a) || (a.start() == start() && a.end() == end()))
                           .flatMap(a -> a.attribute(Types.CATEGORY, Collections.emptySet()).stream())
                           .forEach(categories::add);
      }
      return categories;
   }

   @Override
   default char charAt(int index) {
      if (index < 0 || index > length()) {
         throw new IndexOutOfBoundsException();
      }
      notNull(document(), getClass() + "Attempting to accessing the content of an HString that has no owner");
      return document().charAt(start() + index);
   }

   /**
    * Extracts character n-grams of the given order (e.g. 1=unigram, 2=bigram, etc.)
    *
    * @param order the order of the n-gram to extract
    * @return the list of character n-grams of given order making up this HString
    */
   default List<HString> charNGrams(int order) {
      return charNGrams(order, order);
   }

   /**
    * Extracts all character n-grams from the given minimum to given maximum order (e.g. 1=unigram, 2=bigram, etc.)
    *
    * @param minOrder the minimum order
    * @param maxOrder the maximum order
    * @return the list of character n-grams of order <code>minOrder</code> to <code>maxOrder</code> making up this
    * HString
    * @throws IllegalArgumentException If minOrder > maxOrder or minOrder <= 0
    */
   default List<HString> charNGrams(int minOrder, int maxOrder) {
      Validation.checkArgument(minOrder <= maxOrder,
                               "minimum ngram order must be less than or equal to the maximum ngram order");
      Validation.checkArgument(minOrder > 0, "minimum ngram order must be greater than 0.");
      List<HString> ngrams = new ArrayList<>();
      for (int i = 0; i < length(); i++) {
         for (int j = i + minOrder; j <= length() && j <= i + maxOrder; j++) {
            ngrams.add(substring(i, j));
         }
      }
      return ngrams;
   }

   /**
    * Gets all child annotations, i.e. those annotations that have a dependency relation pointing this HString, with the
    * given dependency relation.
    *
    * @param relation The dependency relation value
    * @return the list of child annotations
    */
   default List<Annotation> children(@NonNull String relation) {
      return incoming(Types.DEPENDENCY, relation, true);
   }

   /**
    * Gets all child annotations, i.e. those annotations that have a dependency relation pointing this HString.
    *
    * @return the list of child annotations
    */
   default List<Annotation> children() {
      return incoming(Types.DEPENDENCY, true);
   }

   /**
    * Sets the value of an attribute if a value is not already set. Removes the attribute if the value is null and
    * ignores setting a value if the attribute is null.
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @param supplier      the supplier to generate the new value
    * @return The old value of the attribute or null
    */
   default <T> T computeIfAbsent(@NonNull AttributeType<T> attributeType, @NonNull Supplier<T> supplier) {
      return Cast.as(attributeMap().computeIfAbsent(attributeType, a -> supplier.get()));
   }

   /**
    * Context h string.
    *
    * @param windowSize the window size
    * @return the h string
    */
   default HString context(int windowSize) {
      return context(Types.TOKEN, windowSize);
   }

   /**
    * Context h string.
    *
    * @param type       the type
    * @param windowSize the window size
    * @return the h string
    */
   default HString context(@NonNull AnnotationType type, int windowSize) {
      checkArgument(windowSize > 0, "Window size must be > 0");
      return leftContext(type, windowSize).union(rightContext(type, windowSize));
   }

   /**
    * Get dependency relation optional.
    *
    * @return the optional
    */
   default Tuple2<String, Annotation> dependency() {
      if (head().isAnnotation()) {
         return head().asAnnotation().dependency();
      }
      return $(Strings.EMPTY, Fragments.emptyAnnotation(document()));
   }

   /**
    * Creates a {@link RelationGraph} with dependency edges and token vertices.
    *
    * @return the dependency relation graph
    */
   default RelationGraph dependencyGraph() {
      return annotationGraph($(Types.DEPENDENCY), Types.TOKEN);
   }

   /**
    * Creates a {@link RelationGraph} with dependency edges and vertices made up of the given types.
    *
    * @param types The annotation types making up the vertices of the dependency relation graph.
    * @return the dependency relation graph
    */
   default RelationGraph dependencyGraph(AnnotationType... types) {
      return annotationGraph($(Types.DEPENDENCY), types);
   }

   /**
    * Dependency is a boolean.
    *
    * @param values the values
    * @return the boolean
    */
   default boolean dependencyIsA(String... values) {
      String rel = dependency().v1;
      if (values == null || values.length == 0 || (values.length == 1 && values[0].equals("null"))) {
         return Strings.isNullOrBlank(rel);
      }
      return Arrays.asList(values).contains(rel);
   }

   /**
    * Returns the document that the annotated object is associated with
    *
    * @return The document that this object is associated with
    */
   Document document();

   /**
    * Gets all annotations enclosed by this HString
    *
    * @return the enclosed annotations
    */
   default List<Annotation> enclosedAnnotations() {
      return annotations().stream()
                          .filter(this::encloses)
                          .filter(h -> h != this)
                          .collect(Collectors.toList());
   }

   /**
    * Gets all annotations of the given type enclosed by this HString
    *
    * @param annotationType the annotation type we want
    * @return the enclosed annotations
    */
   default List<Annotation> enclosedAnnotations(AnnotationType annotationType) {
      return annotations(annotationType, this::encloses);
   }

   /**
    * Checks if this HString encloses the given other.
    *
    * @param other The other HString
    * @return True of this one encloses the given other.
    */
   default boolean encloses(HString other) {
      return other != null &&
         (document() != null && other.document() != null) &&
         (document() == other.document()) &&
         Span.super.encloses(other);
   }

   default List<Annotation> filter(LyreExpression lambda) {
      List<Annotation> result = new ArrayList<>();
      for (int i = 0; i < tokens().size(); ) {
         Object o = lambda.applyAsObject(tokenAt(i));
         if (o == null) {
            i++;
         } else if (o instanceof Annotation) {
            Annotation a = Cast.as(o);
            result.add(a);
            i += a.tokenLength();
         } else if (o instanceof Boolean && !Cast.<Boolean>as(o)) {
            i++;
         } else {
            result.add(tokenAt(i));
            i++;
         }
      }
      return result;
   }

   default Stream<Annotation> filter(SerializablePredicate<? super Annotation> predicate, AnnotationType... types) {
      return interleaved(types).stream().filter(predicate);
   }

   /**
    * Finds the given text in this HString starting from the beginning of this HString. If the document is annotated
    * with tokens, the match will extend to the token(s) covering the match.
    *
    * @param text the text to search for
    * @return the HString for the match or empty if no match is found.
    */
   default HString find(String text) {
      return find(text, 0);
   }

   /**
    * Finds the given text in this HString starting from the given start index of this HString. If the document is
    * annotated with tokens, the match will extend to the token(s) covering the match.
    *
    * @param text  the text to search for
    * @param start the index to start the search from
    * @return the HString for the match or empty if no match is found.
    */
   default HString find(String text, int start) {
      Validation.checkElementIndex(start, length());
      int pos = indexOf(text, start);
      if (pos == -1) {
         return Fragments.empty(document());
      }

      //If we have tokens expand the match to the overlapping tokens.
      if (document() != null && document().isCompleted(Types.TOKEN)) {
         return union(substring(pos, pos + text.length()).tokens());
      }

      return substring(pos, pos + text.length());
   }

   /**
    * Finds all occurrences of the given text in this HString starting
    *
    * @param text the text to search for
    * @return A list of HString that are matches to the given string
    */
   default Stream<HString> findAll(String text) {
      return Streams.asStream(new Iterator<HString>() {
         Integer pos = null;
         int start = 0;

         private boolean advance() {
            if (pos == null) {
               pos = indexOf(text, start);
            }
            return pos != -1;
         }

         @Override
         public boolean hasNext() {
            return advance();
         }

         @Override
         public HString next() {
            if (!advance()) {
               throw new NoSuchElementException();
            }
            int n = pos;
            pos = null;
            start = n + 1;
            //If we have tokens expand the match to the overlaping tokens.
            if (document() != null && document().isCompleted(Types.TOKEN)) {
               return union(substring(n, n + text.length()).tokens());
            }
            return substring(n, n + text.length());
         }
      });
   }

   /**
    * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this
    * HString
    *
    * @param regex the regular expression to search for
    * @return A list of HString that are matches to the given regular expression
    */
   default Stream<HString> findAllPatterns(String regex) {
      return findAllPatterns(Pattern.compile(regex));
   }

   /**
    * Finds all matches to the given token regular expression in this HString.
    *
    * @param regex the token regex to match
    * @return Stream of matches
    */
   default Stream<HString> findAllPatterns(TokenRegex regex) {
      return Streams.asStream(new Iterator<HString>() {
         TokenMatcher m = regex.matcher(HString.this);
         HString nextMatch = null;

         private boolean advance() {
            if (nextMatch == null && m.find()) {
               nextMatch = m.group();
            }
            return nextMatch != null;
         }

         @Override
         public boolean hasNext() {
            return advance();
         }

         @Override
         public HString next() {
            if (!advance()) {
               throw new NoSuchElementException();
            }
            HString toReturn = nextMatch;
            nextMatch = null;
            return toReturn;
         }
      });
   }

   /**
    * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this
    * HString
    *
    * @param regex the regular expression to search for
    * @return A list of HString that are matches to the given regular expression
    */
   default Stream<HString> findAllPatterns(Pattern regex) {
      return Streams.asStream(new Iterator<HString>() {
         int end = -1;
         Matcher m = regex.matcher(HString.this);
         int start = -1;

         private boolean advance() {
            if (start == -1) {
               if (m.find()) {
                  start = m.start();
                  end = m.end();
               }
            }
            return start != -1;
         }

         @Override
         public boolean hasNext() {
            return advance();
         }

         @Override
         public HString next() {
            if (!advance()) {
               throw new NoSuchElementException();
            }
            HString sub = substring(start, end);
            start = -1;
            end = -1;
            //If we have tokens expand the match to the overlaping tokens.
            if (document() != null && document().isCompleted(Types.TOKEN)) {
               sub = union(sub.tokens());
            }
            return sub;
         }
      });
   }

   /**
    * Finds the given regular expression in this HString starting from the beginning of this HString
    *
    * @param regex the regular expression to search for
    * @return the HString for the match or empty if no match is found.
    */
   default HString findPattern(String regex) {
      return findPattern(Pattern.compile(regex));
   }

   /**
    * Finds the given regular expression in this HString starting from the beginning of this HString
    *
    * @param regex the regular expression to search for
    * @return the HString for the match or empty if no match is found.
    */
   default HString findPattern(@NonNull Pattern regex) {
      Matcher m = matcher(regex);
      if (m.find()) {
         return union(substring(m.start(), m.end()).tokens());
      }
      return Fragments.empty(document());
   }

   /**
    * Gets the first annotation overlapping this object with the given annotation type.
    *
    * @param type the annotation type
    * @return the first annotation of the given type overlapping this object or an empty annotation if there is none.
    */
   default Annotation first(@NonNull AnnotationType type) {
      return Iterables.getFirst(annotations(type))
                      .orElse(Fragments.detachedEmptyAnnotation());
   }

   /**
    * Gets the first token annotation overlapping this object.
    *
    * @return the forst token annotation
    */
   default Annotation firstToken() {
      return tokenAt(0);
   }

   /**
    * Convenience method for processing annotations of a given type.
    *
    * @param type     the annotation type
    * @param consumer the consumer to use for processing annotations
    */
   default void forEach(@NonNull AnnotationType type, @NonNull Consumer<? super Annotation> consumer) {
      annotations(type).forEach(consumer);
   }

   @Override
   default Language getLanguage() {
      if (hasAttribute(Types.LANGUAGE)) {
         return attribute(Types.LANGUAGE);
      }
      if (document() == null) {
         return Hermes.defaultLanguage();
      }
      return document().getLanguage();
   }

   /**
    * Sets the language of the HString
    *
    * @param language The language of the HString.
    */
   default void setLanguage(Language language) {
      put(Types.LANGUAGE, language);
   }

   /**
    * Gets the lemmatized version of the HString. Lemmas of longer phrases are constructed from token lemmas.
    *
    * @return The lemmatized version of the HString.
    */
   default String getLemma() {
      if (isInstance(Types.TOKEN)) {
         if (hasAttribute(Types.LEMMA)) {
            return attribute(Types.LEMMA);
         }
         if (hasAttribute(Types.SPELLING_CORRECTION)) {
            return attribute(Types.SPELLING_CORRECTION);
         }
         return toLowerCase();
      }
      return tokens()
         .stream()
         .map(HString::getLemma)
         .collect(Collectors.joining(getLanguage().usesWhitespace() ? " " : ""));
   }

   /**
    * Gets the stemmed version of the HString. Stems of token are determined using the <code>Stemmer</code> associated
    * with the language that the token is in. Tokens store their stem using the <code>STEM</code> attribute, so that the
    * stem only needs to be calculated once.Stems of longer phrases are constructed from token stems.
    *
    * @return The stemmed version of the HString.
    */
   default String getStemmedForm() {
      if (isInstance(Types.TOKEN)) {
         return computeIfAbsent(Types.STEM, () -> Stemmers.getStemmer(getLanguage()).stem(this));
      }
      return tokenStream().map(HString::getStemmedForm)
                          .collect(Collectors.joining(getLanguage().usesWhitespace() ? " " : ""));
   }

   /**
    * Determines if a annotation of a given type is associated with the object
    *
    * @param annotationType The annotation type
    * @return True if an annotation of the given type is associated with the object, False otherwise
    */
   default boolean hasAnnotation(@NonNull AnnotationType annotationType) {
      return annotationStream().anyMatch(r -> r.getType().equals(annotationType));
   }

   /**
    * Determines if an attribute of a given type is associated with the object
    *
    * @param attributeType The attribute type
    * @return True if the attribute is associated with the object, False otherwise
    */
   default boolean hasAttribute(@NonNull AttributeType<?> attributeType) {
      return attributeMap().containsKey(attributeType);
   }

   /**
    * Checks if the HString has at least one incoming relation of the given type with the given value. Will check
    * sub-annotations as well.
    *
    * @param type  the relation type
    * @param value the relation value
    * @return True if there as an incoming relation to this HString or a sub-annotation of the given type with the given
    * value.
    */
   default boolean hasIncomingRelation(@NonNull RelationType type, String value) {
      return incoming(type, value, true).size() > 0;
   }

   /**
    * Determines if an incoming relation of a given type is associated with the object
    *
    * @param relationType The relation type
    * @return True if the relation is associated with the object, False otherwise
    */
   default boolean hasIncomingRelation(@NonNull RelationType relationType) {
      return incomingRelationStream().anyMatch(r -> r.getType().equals(relationType));
   }

   /**
    * Checks if the HString has at least one outgoing relation of the given type with the given value. Will check
    * sub-annotations as well.
    *
    * @param type  the relation type
    * @param value the relation value
    * @return True if there as an outgoing relation to this HString or a sub-annotation of the given type with the given
    * value.
    */
   default boolean hasOutgoingRelation(@NonNull RelationType type, String value) {
      return outgoing(type, value, true).size() > 0;
   }

   /**
    * Determines if an outgoing relation of a given type is associated with the object
    *
    * @param relationType The relation type
    * @return True if the relation is associated with the object, False otherwise
    */
   default boolean hasOutgoingRelation(@NonNull RelationType relationType) {
      return relationType != null && outgoingRelationStream().anyMatch(r -> r.getType().equals(relationType));
   }

   /**
    * Gets the token that is highest in the dependency tree for this HString
    *
    * @return the head
    */
   default HString head() {
      return tokens()
         .stream()
         .filter(t -> t.parent().isEmpty())
         .map(Cast::<HString>as)
         .findFirst()
         .orElseGet(() -> tokens()
            .stream()
            .filter(t -> !this.overlaps(t.parent()))
            .map(Cast::<HString>as)
            .findFirst()
            .orElse(this));
   }

   /**
    * Gets all annotations that have relation with this HString as the target where this HString includes all
    * sub-annotations.
    *
    * @param type  the relation type
    * @param value the value of the relation
    * @return the annotations
    */
   default List<Annotation> incoming(RelationType type, String value) {
      return incoming(type, value, true);
   }

   /**
    * Gets all annotations that have relation with this HString as the target. If <code>includedSubAnnotations</code>
    * is
    * <code>true</code> then all sub-annotations are examined as potential targets.
    *
    * @param type                  the relation type
    * @param value                 the relation value
    * @param includeSubAnnotations True - this HString or any of its sub-annotations can be the target, False - only
    *                              relations with this exact HString as the target.
    * @return the annotations
    */
   default List<Annotation> incoming(@NonNull RelationType type, @NonNull String value, boolean includeSubAnnotations) {
      return incomingRelationStream(includeSubAnnotations)
         .filter(r -> r.getType().isInstance(type) && r.getValue().equals(value))
         .map(r -> document().annotation(r.getTarget())
                             .orElseThrow(() -> new IllegalStateException(r.getTarget() + " is invalid annotation id")))
         .collect(Collectors.toList());
   }

   /**
    * Gets all annotations that have relation with this HString as the target where this HString includes all
    * sub-annotations.
    *
    * @param type the relation type
    * @return the annotations
    */
   default List<Annotation> incoming(@NonNull RelationType type) {
      return incoming(type, true);
   }

   /**
    * Gets all annotations that have relation with this HString as the target. If <code>includedSubAnnotations</code>
    * is
    * <code>true</code> then all sub-annotations are examined as potential targets.
    *
    * @param type                  the relation type
    * @param includeSubAnnotations True - this HString or any of its sub-annotations can be the target, False - only
    *                              relations with this exact HString as the target.
    * @return the annotations
    */
   default List<Annotation> incoming(@NonNull RelationType type, boolean includeSubAnnotations) {
      if (type == null) {
         return Collections.emptyList();
      }
      return incomingRelationStream(includeSubAnnotations).filter(r -> r.getType().isInstance(type))
                                                          .map(r -> document().annotation(r.getTarget())
                                                                              .orElseThrow(
                                                                                 () -> new IllegalStateException(
                                                                                    r.getTarget() + " is invalid annotation id")))
                                                          .distinct()
                                                          .collect(Collectors.toList());
   }

   /**
    * Get all incoming relations to this HString and its sub-annotations.
    *
    * @return the stream of relations
    */
   default Stream<Relation> incomingRelationStream() {
      return incomingRelationStream(true);
   }

   /**
    * Gets all incoming relations to this HString.
    *
    * @param includeSubAnnotations True - include relations to sub-annotations
    * @return the stream of relations
    */
   default Stream<Relation> incomingRelationStream(boolean includeSubAnnotations) {
      if (includeSubAnnotations) {
         return annotations().stream()
                             .filter(a -> a != this)
                             .flatMap(a -> a.incomingRelationStream(false))
                             .filter(rel -> rel.getTarget(document())
                                               .map(aa -> !aa.overlaps(this))
                                               .orElse(false));
      }
      return Stream.empty();
   }

   /**
    * Get all incoming relations to this HString and its sub-annotations.
    *
    * @return the collection of relations
    */
   default List<Relation> incomingRelations() {
      return incomingRelations(true);
   }

   /**
    * Gets all incoming relations to this HString.
    *
    * @param includeSubAnnotations True - include relations to sub-annotations
    * @return the collection of relations
    */
   default List<Relation> incomingRelations(boolean includeSubAnnotations) {
      return incomingRelationStream(includeSubAnnotations).collect(Collectors.toList());
   }

   /**
    * Gets all relations of the given type targeting this HString or one of its sub-annotations.
    *
    * @param relationType the relation type
    * @return the relations
    */
   default List<Relation> incomingRelations(@NonNull RelationType relationType) {
      return incomingRelations(relationType, true);
   }

   /**
    * Gets all relations of the given type targeting this HString.
    *
    * @param relationType          the relation type
    * @param includeSubAnnotations True - include relations to sub-annotations
    * @return the relations
    */
   default List<Relation> incomingRelations(@NonNull RelationType relationType, boolean includeSubAnnotations) {
      return incomingRelationStream(includeSubAnnotations).filter(r -> r.getType().equals(relationType))
                                                          .collect(Collectors.toList());
   }

   /**
    * <p> Returns the annotations of the given types that overlap this string in a maximum match fashion. Each token in
    * the string is examined and the annotation type with the longest span on that token is chosen. If more than one
    * type has the span length, the first one found will be chose, i.e. the order in which the types are passed in to
    * the method can effect the outcome. </p> <p> Examples where this is useful is when dealing with multi-word
    * expressions. Using the interleaved method you can retrieve all tokens and multi-word expressions to fully match
    * the span of the string. </p>
    *
    * @param types The other types to examine
    * @return The list of interleaved annotations
    */
   default List<Annotation> interleaved(AnnotationType... types) {
      if (types == null || types.length == 0) {
         return Collections.emptyList();
      }
      if (types.length == 1) {
         return annotations(types[0]);
      }
      List<Annotation> annotations = new ArrayList<>();
      for (int i = 0; i < tokenLength(); ) {
         Annotation annotation = Fragments.detachedEmptyAnnotation();
         for (AnnotationType other : types) {
            for (Annotation temp : tokenAt(i).annotations(other)) {
               if (temp.tokenLength() > annotation.tokenLength()) {
                  annotation = temp;
               }
            }
         }

         if (annotation.isEmpty()) {
            i++;
         } else {
            i += annotation.tokenLength();
            annotations.add(annotation);
         }
      }
      return annotations;
   }

   /**
    * Is this HString an annotation?
    *
    * @return True if this HString represents an annotation
    */
   default boolean isAnnotation() {
      return false;
   }

   /**
    * Is this HString a document?
    *
    * @return True if this HString represents a document
    */
   default boolean isDocument() {
      return false;
   }

   default boolean isEndOfSentence() {
      Annotation sentence = sentence();
      if (sentence.isEmpty()) {
         return false;
      }
      Annotation nextToken = lastToken().next();
      return nextToken.isEmpty() || nextToken.sentence().getId() != sentence.getId();
   }

   /**
    * Returns true this HString is an instance of the given annotation type
    *
    * @param type the annotation type
    * @return True if this HString is an annotation of the given type
    */
   default boolean isInstance(AnnotationType type) {
      return false;
   }

   /**
    * Gets the last annotation overlapping this object with the given annotation type.
    *
    * @param type the annotation type
    * @return the last annotation of the given type overlapping this object or a detached empty annotation if there is
    * none.
    */
   default Annotation last(@NonNull AnnotationType type) {
      List<Annotation> annotations = annotations(type);
      return annotations.isEmpty()
             ? Fragments.detachedEmptyAnnotation()
             : annotations.get(annotations.size() - 1);
   }

   /**
    * Ges the last token annotation overlapping this object
    *
    * @return the last token annotation
    */
   default Annotation lastToken() {
      return tokenAt(tokenLength() - 1);
   }

   /**
    * Generates an HString representing the <code>windowSize</code> tokens to the left of the start of this HString.
    *
    * @param windowSize the number of tokens in the context.
    * @return the HString context
    */
   default HString leftContext(int windowSize) {
      return leftContext(Types.TOKEN, windowSize);
   }

   /**
    * Generates an HString representing the <code>windowSize</code> of given annotation types to the left of the start
    * of this HString.
    *
    * @param type       the annotation type to create the context of.
    * @param windowSize the number of tokens in the context.
    * @return the HString context
    */
   default HString leftContext(@NonNull AnnotationType type, int windowSize) {
      windowSize = Math.abs(windowSize);
      Validation.checkArgument(windowSize >= 0, "Window size must not be 0");
      int sentenceStart = sentence().start();
      if (windowSize == 0 || start() <= sentenceStart) {
         return Fragments.detachedEmptyHString();
      }
      HString context = firstToken().previous(type);
      for (int i = 1; i < windowSize; i++) {
         HString next = context
            .firstToken()
            .previous(type);
         if (next.end() <= sentenceStart) {
            break;
         }
         context = context.union(next);
      }
      return context;
   }

   @Override
   default int length() {
      return end() - start();
   }

   default <T> Stream<T> map(@NonNull SerializableFunction<? super Annotation, T> mapper,
                             @NonNull AnnotationType... types) {
      return interleaved(types).stream().map(mapper);
   }

   /**
    * Gets the annotation of a given type that is next in order (of span) to this one
    *
    * @param type the type of annotation wanted
    * @return the next annotation of the given type or null
    */
   default Annotation next(@NonNull AnnotationType type) {
      return document() == null ? Fragments.detachedEmptyAnnotation() : document().next(asAnnotation(), type);
   }

   default boolean noneMatch(@NonNull SerializablePredicate<? super Annotation> predicate,
                             @NonNull AnnotationType... types) {
      return interleaved(types).stream().noneMatch(predicate);
   }

   /**
    * Gets all annotations with which this HString or any of its sub-annotations has an outgoing relation of the given
    * type.
    *
    * @param type the relation type
    * @return the annotations
    */
   default List<Annotation> outgoing(RelationType type) {
      return outgoing(type, true);
   }

   /**
    * Gets all annotations with which this HString has an outgoing relation of the given type.
    *
    * @param type                  the relation type
    * @param includeSubAnnotations True - include annotations for which any of the sub-annotations has an outgoing
    *                              relation.
    * @return the annotations
    */
   default List<Annotation> outgoing(@NonNull RelationType type, boolean includeSubAnnotations) {
      return outgoingRelationStream(includeSubAnnotations)
         .filter(r -> r.getType().equals(type))
         .map(r -> document().annotation(r.getTarget())
                             .orElseThrow(
                                () -> new IllegalStateException(
                                   r.getTarget() + " is invalid annotation id")))
         .filter(Objects::nonNull)
         .distinct()
         .collect(Collectors.toList());
   }

   /**
    * Gets all annotations with which this HString or any of its sub-annotations has an outgoing relation of the given
    * type and value.
    *
    * @param type  the relation type
    * @param value the relation value
    * @return the annotations
    */
   default List<Annotation> outgoing(RelationType type, String value) {
      return outgoing(type, value, true);
   }

   /**
    * Gets all annotations with which this HString has an outgoing relation of the given type and value.
    *
    * @param type                  the relation type
    * @param value                 the relation value
    * @param includeSubAnnotations True - include annotations for which any of the sub-annotations has an outgoing
    *                              relation.
    * @return the annotations
    */
   default List<Annotation> outgoing(@NonNull RelationType type, String value, boolean includeSubAnnotations) {
      return outgoingRelationStream(includeSubAnnotations)
         .filter(r -> r.getType().equals(type) && Strings.safeEquals(r.getValue(), value, true))
         .map(r -> document().annotation(r.getTarget())
                             .orElseThrow(
                                () -> new IllegalStateException(
                                   r.getTarget() + " is invalid annotation id")))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   /**
    * Get all outgoing relations to this HString and its sub-annotations.
    *
    * @return the stream of relations
    */
   default Stream<Relation> outgoingRelationStream() {
      return outgoingRelationStream(true);
   }

   /**
    * Gets all outgoing relations to this HString.
    *
    * @param includeSubAnnotations True - include relations to sub-annotations
    * @return the stream of relations
    */
   default Stream<Relation> outgoingRelationStream(boolean includeSubAnnotations) {
      if (includeSubAnnotations) {
         return annotations().stream()
                             .flatMap(a -> a.outgoingRelationStream(false))
                             .filter(a -> !overlaps(this))
                             .distinct();
      }
      return Stream.empty();
   }

   /**
    * Get all outgoing relations to this HString and its sub-annotations.
    *
    * @return the collection of relations
    */
   default List<Relation> outgoingRelations() {
      return outgoingRelations(true);
   }

   /**
    * Gets all outgoing relations to this HString.
    *
    * @param includeSubAnnotations True - include relations to sub-annotations
    * @return the collection of relations
    */
   default List<Relation> outgoingRelations(boolean includeSubAnnotations) {
      return outgoingRelationStream(includeSubAnnotations).collect(Collectors.toList());
   }

   /**
    * Gets all relations of the given type originating from this HString or one of its sub-annotations.
    *
    * @param relationType the relation type
    * @return the relations
    */
   default List<Relation> outgoingRelations(@NonNull RelationType relationType) {
      return outgoingRelations(relationType, true);
   }

   /**
    * Gets all relations of the given type originating from this HString.
    *
    * @param relationType          the relation type
    * @param includeSubAnnotations True - include relations to sub-annotations
    * @return the relations
    */
   default List<Relation> outgoingRelations(@NonNull RelationType relationType, boolean includeSubAnnotations) {
      return outgoingRelationStream(includeSubAnnotations).filter(r -> r.getType().equals(relationType))
                                                          .collect(Collectors.toList());
   }

   /**
    * Checks if this HString overlaps with the given other.
    *
    * @param other The other HString
    * @return True of this one overlaps with the given other.
    */
   default boolean overlaps(HString other) {
      return other != null &&
         (document() != null && other.document() != null) &&
         (document() == other.document()) &&
         Span.super.overlaps(other);
   }

   /**
    * Gets the dependency parent of this HString
    *
    * @return the parent
    */
   default Annotation parent() {
      return dependency().v2;
   }

   /**
    * Gets the part-of-speech of the HString
    *
    * @return The best part-of-speech for the HString
    */
   default POS pos() {
      return POS.forText(this);
   }

   /**
    * Gets the annotation of a given type that is previous in order (of span) to this one
    *
    * @param type the type of annotation wanted
    * @return the previous annotation of the given type or null
    */
   default Annotation previous(@NonNull AnnotationType type) {
      return document() == null ? Fragments.detachedEmptyAnnotation() : document().previous(asAnnotation(), type);
   }

   /**
    * Sets the value of an attribute. Removes the attribute if the value is null and ignores setting a value if the
    * attribute is null.
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @param value         the value
    * @return The old value of the attribute or null
    */
   default <T> T put(@NonNull AttributeType<T> attributeType, T value) {
      return Cast.as(attributeMap().put(attributeType, value));
   }

   default <E, T extends Collection<E>> void putAdd(@NonNull AttributeType<T> attributeType,
                                                    @NonNull Iterable<E> items) {
      attributeMap().compute(attributeType, (type, collection) -> {
         if (collection == null) {
            return attributeType.decode(items);
         }
         final Collection<E> c = Cast.as(collection);
         items.forEach(c::add);
         return c;
      });
   }

   /**
    * Sets all attributes in a given map.
    *
    * @param map the attribute-value map
    */
   default void putAll(@NonNull Map<AttributeType<?>, ?> map) {
      attributeMap().putAll(map);
   }

   /**
    * Sets all attributes found in the given attributed object on this object.
    *
    * @param attributedObject The attributed object whose attributes we want to copy.
    */
   default void putAll(@NonNull HString attributedObject) {
      attributeMap().putAll(attributedObject.attributeMap());
   }

   /**
    * Sets the value of an attribute if a value is not already set. Removes the attribute if the value is null and
    * ignores setting a value if the attribute is null.
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @param value         the value to put
    * @return The old value of the attribute or null
    */
   default <T> T putIfAbsent(@NonNull AttributeType<T> attributeType, T value) {
      return Cast.as(attributeMap().putIfAbsent(attributeType, value));
   }

   /**
    * Removes an attribute from the object.
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @return the value that was associated with the attribute
    */
   default <T> T removeAttribute(@NonNull AttributeType<T> attributeType) {
      return Cast.as(attributeMap().remove(attributeType));
   }

   /**
    * Remove relation.
    *
    * @param relation the relation
    */
   void removeRelation(@NonNull Relation relation);

   /**
    * Generates an HString representing the <code>windowSize</code> tokens to the right of the start of this HString.
    *
    * @param windowSize the number of tokens in the context.
    * @return the HString context
    */
   default HString rightContext(int windowSize) {
      return rightContext(Types.TOKEN, windowSize);
   }

   /**
    * Generates an HString representing the <code>windowSize</code> of given annotation types to the right of the start
    * of this HString.
    *
    * @param type       the annotation type to create the context of.
    * @param windowSize the number of tokens in the context.
    * @return the HString context
    */
   default HString rightContext(@NonNull AnnotationType type, int windowSize) {
      Validation.checkArgument(windowSize >= 0, "Window size must be > 0");
      int sentenceEnd = sentence().end();
      if (windowSize == 0 || end() >= sentenceEnd) {
         return Fragments.detachedEmptyHString();
      }
      HString context = lastToken().next(type);
      for (int i = 1; i < windowSize; i++) {
         HString next = context
            .lastToken()
            .next(type);
         if (next.start() >= sentenceEnd) {
            break;
         }
         context = context.union(next);
      }
      return context;
   }

   /**
    * Assumes the object only overlaps with a single sentence and returns it. This is equivalent to calling {@link
    * #first(AnnotationType)}** with the annotation type set to <code>Types.SENTENCE</code>
    *
    * @return Returns the first, and possibly only, sentence this object overlaps with.
    */
   default Annotation sentence() {
      return first(Types.SENTENCE);
   }

   /**
    * Gets a java Stream over the sentences overlapping this object.
    *
    * @return the stream of sentences
    */
   default Stream<Annotation> sentenceStream() {
      return annotationStream(Types.SENTENCE);
   }

   /**
    * Gets the sentences overlapping this object
    *
    * @return the sentences overlapping this annotation.
    */
   default List<Annotation> sentences() {
      return annotations(Types.SENTENCE);
   }

   /**
    * Splits this HString using the given predicate to apply against tokens. An example of where this might be useful is
    * when we want to split long phrases on different punctuation, e.g. commas or semicolons.
    *
    * @param delimiterPredicate the predicate to use to determine if a token is a delimiter or not
    * @return the list of split HString
    */
   default List<HString> split(Predicate<? super Annotation> delimiterPredicate) {
      List<HString> result = new ArrayList<>();
      int start = -1;
      for (int i = 0; i < tokenLength(); i++) {
         if (delimiterPredicate.test(tokenAt(i))) {
            if (start != -1) {
               result.add(tokenAt(start).union(tokenAt(i - 1)));
            }
            start = -1;
         } else if (start == -1) {
            start = i;
         }
      }
      if (start != -1) {
         result.add(tokenAt(start).union(tokenAt(tokenLength() - 1)));
      }
      return result;
   }

   /**
    * Gets annotations of a given type that have the same starting offset as this object.
    *
    * @param type the type of annotation wanted
    * @return the list of annotations of given type have the same starting offset as this object.
    */
   default List<Annotation> startingHere(@NonNull AnnotationType type) {
      return annotations(type, annotation -> annotation.start() == start() && annotation.isInstance(type));
   }

   /**
    * Returns a new HString that is a substring of this one. The substring begins at the specified relativeStart and
    * extends to the character at index relativeEnd - 1. Thus the length of the substring is relativeEnd-relativeStart.
    *
    * @param relativeStart the relative start within in this HString
    * @param relativeEnd   the relative end within this HString
    * @return the specified substring.
    * @throws IndexOutOfBoundsException - if the relativeStart is negative, or relativeEnd is larger than the length of
    *                                   this HString object, or relativeStart is larger than relativeEnd.
    */
   default HString substring(int relativeStart, int relativeEnd) {
      Validation.checkPositionIndex(relativeStart, relativeEnd);
      Validation.checkPositionIndex(relativeEnd, length());
      return Fragments.fragment(document(), start() + relativeStart, start() + relativeEnd);
   }

   default Document toDocument() {
      if (this instanceof Document) {
         return Cast.as(this);
      }
      DefaultDocumentImpl doc = new DefaultDocumentImpl(String.format("%s-%d:%d", document().getId(), start(), end()),
                                                        toString());
      Map<Long, Long> idMap = new HashMap<>();
      for (Annotation annotation : enclosedAnnotations()) {
         long id = doc.annotationBuilder(annotation.getType())
                      .start(annotation.start() - start())
                      .end(end() - annotation.end())
                      .attributes(annotation)
                      .createAttached()
                      .getId();
         idMap.put(annotation.getId(), id);
      }
      for (Annotation annotation : enclosedAnnotations()) {
         final Annotation targetAnnotation = document().annotation(annotation.getId()).orElseThrow(
            IllegalStateException::new);
         annotation.outgoingRelationStream(false)
                   .filter(r -> idMap.containsKey(r.getTarget()))
                   .forEach(r -> targetAnnotation.add(new Relation(r.getType(),
                                                                   r.getValue(),
                                                                   idMap.get(r.getTarget()))));
      }
      return doc;
   }

   /**
    * Converts the HString to a string with part-of-speech information attached using <code>_</code> as the delimiter
    *
    * @return the HString with part-of-speech information attached to tokens
    */
   default String toPOSString() {
      return toPOSString('_');
   }

   /**
    * Converts the HString to a string with part-of-speech information attached using the given delimiter
    *
    * @param delimiter the delimiter to use to separate word and part-of-speech
    * @return the HString with part-of-speech information attached to tokens
    */
   default String toPOSString(char delimiter) {
      return tokens()
         .stream()
         .map(t -> t.toString() + delimiter + t.attribute(Types.PART_OF_SPEECH, POS.ANY).asString())
         .collect(Collectors.joining(" "));
   }

   /**
    * <p> Gets the token at the given token index which is a relative offset from this object. For example, given the
    * document with the following tokens: <code>["the", "quick", "brown", "fox", "jumps", "over", "the", "lazy",
    * "dog"]</code> and this annotated object spanning <code>["quick", "brown", "fox"]</code> "quick" would have a
    * relative offset in this object of 0 and document offset of 1. </p>
    *
    * @param tokenIndex the token index relative to the tokens overlapping this object.
    * @return the token annotation at the relative offset
    */
   default Annotation tokenAt(int tokenIndex) {
      if (tokenIndex < 0 || tokenIndex >= tokenLength()) {
         return Fragments.detachedEmptyAnnotation();
      }
      return tokens().get(tokenIndex);
   }

   /**
    * The length of the object in tokens
    *
    * @return the number of tokens in this annotation
    */
   default int tokenLength() {
      return tokens().size();
   }

   /**
    * Gets a java Stream over the tokens overlapping this object.
    *
    * @return the stream of tokens
    */
   default Stream<Annotation> tokenStream() {
      return annotationStream(Types.TOKEN);
   }

   /**
    * Gets the tokens overlapping this object.
    *
    * @return the tokens overlapping this annotation.
    */
   default List<Annotation> tokens() {
      return annotations(Types.TOKEN);
   }

   /**
    * Trims tokens off the left and right of this HString that match the given predicate.
    *
    * @param toTrimPredicate the predicate to use to determine if a token should be removed (evaulate to TRUE) or kept
    *                        (evaluate to FALSE).
    * @return the trimmed HString
    */
   default HString trim(@NonNull Predicate<? super HString> toTrimPredicate) {
      return trimRight(toTrimPredicate).trimLeft(toTrimPredicate);
   }

   /**
    * Trims tokens off the left of this HString that match the given predicate.
    *
    * @param toTrimPredicate the predicate to use to determine if a token should be removed (evaulate to TRUE) or kept
    *                        (evaluate to FALSE).
    * @return the trimmed HString
    */
   default HString trimLeft(@NonNull Predicate<? super HString> toTrimPredicate) {
      if (tokenLength() == 0) {
         return toTrimPredicate.test(this)
                ? Fragments.detachedEmptyAnnotation()
                : this;
      }
      int start = 0;
      while (start < tokenLength() - 1 && toTrimPredicate.test(tokenAt(start))) {
         start++;
      }
      if (start < tokenLength()) {
         return union(tokens().subList(start, tokenLength()));
      }
      return Fragments.empty(document());
   }

   /**
    * Trims tokens off the right of this HString that match the given predicate.
    *
    * @param toTrimPredicate the predicate to use to determine if a token should be removed (evaulate to TRUE) or kept
    *                        (evaluate to FALSE).
    * @return the trimmed HString
    */
   default HString trimRight(@NonNull Predicate<? super HString> toTrimPredicate) {
      if (tokenLength() == 0) {
         return toTrimPredicate.test(this)
                ? Fragments.detachedEmptyAnnotation()
                : this;
      }
      int end = tokenLength() - 1;
      while (end >= 0 && toTrimPredicate.test(tokenAt(end))) {
         end--;
      }
      if (end > 0) {
         return tokenAt(0).union(tokenAt(end));
      } else if (end == 0) {
         return tokenAt(0);
      }
      return Fragments.empty(document());
   }

   /**
    * Creates a new string by performing a union over the spans of this HString and at least one more HString. The new
    * HString will have a span that starts at the minimum starting position of the given strings and end at the maximum
    * ending position of the given strings.
    *
    * @param other the HString to union with
    * @return A new HString representing the union over the spans of the given HStrings.
    */
   default HString union(@NonNull HString other) {
      return union(this, other);
   }
}//END OF HString
