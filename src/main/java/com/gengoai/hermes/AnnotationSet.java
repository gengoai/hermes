package com.gengoai.hermes;

import com.gengoai.collection.Streams;
import com.gengoai.collection.tree.Span;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * <p>
 * An <code>AnnotationSet</code> acts as the storage mechanism for annotations associated with a document. It provides
 * methods for adding, removing, and navigating the annotations. In particular, a <code>AnnotationSet</code> defines
 * sequential methods of accessing annotations ({@link #next(Annotation, AnnotationType)}, {@link #previous(Annotation,
 * AnnotationType)}*), based on criteria {@link #select(Predicate)} and {@link #select(Span, Predicate)}*, and by id
 * {@link #get(long)}.
 * </p>
 * <p>
 * Annotation sets also keep track of completed annotation types, i.e. those processed using a <code>Pipeline</code>.
 * This allows the pipeline to ignore further attempts to annotate a type that is marked complete. In addition to being
 * marked complete, information about the annotator is stored.
 * </p>
 *
 * @author David B. Bracewell
 */
public class AnnotationSet implements Iterable<Annotation>, Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<AnnotatableType, String> completed = new HashMap<>(4);
   private final Map<Long, Annotation> idAnnotationMap = new HashMap<>(4);
   private final AnnotationTree tree = new AnnotationTree();

   /**
    * Adds an annotation to the set
    *
    * @param annotation The annotation to attach
    */
   public boolean add(Annotation annotation) {
      boolean added = tree.add(annotation);
      idAnnotationMap.put(annotation.getId(), annotation);
      return added;
   }

   /**
    * Checks if an annotation is in the set or not
    *
    * @param annotation The annotation to check
    * @return True if the annotation is  in the set, False if not
    */
   public boolean contains(Annotation annotation) {
      return !annotation.isDetached() && idAnnotationMap.get(annotation.getId()) == annotation;
   }

   /**
    * Gets the annotation for the given id
    *
    * @param id The id of the annotation
    * @return The annotation associated with that id or null if one does not exist
    */
   public Annotation get(long id) {
      return idAnnotationMap.get(id);
   }

   /**
    * Gets information on what annotator provided the annotation of the given type
    *
    * @param type The annotation type
    * @return String representing the annotation provider or null
    */
   public String getAnnotationProvider(AnnotatableType type) {
      return completed.get(type);
   }

   /**
    * Gets the set of completed annotation types.
    *
    * @return Set of classes for completed annotations
    */
   public Set<AnnotatableType> getCompleted() {
      return completed.keySet();
   }

   /**
    * Gets if the given annotation type is completed or not
    *
    * @param type the annotation type
    * @return True if the annotation is completed, False if not.
    */
   public boolean isCompleted(AnnotatableType type) {
      return completed.containsKey(type);
   }

   @Override
   public Iterator<Annotation> iterator() {
      return tree.iterator();
   }

   /**
    * Gets the first annotation after a given one of the given type
    *
    * @param annotation The annotation we want the next for
    * @param type       the type of the next annotation wanted
    * @return The next annotation of the same type or null
    */
   public Annotation next(Annotation annotation, AnnotationType type) {
      return tree.ceiling(annotation, type);
   }

   /**
    * Gets the first annotation before a given one of the given type
    *
    * @param annotation The annotation we want the previous for
    * @param type       the type of the previous annotation wanted
    * @return The previous annotation of the same type or null
    */
   public Annotation previous(Annotation annotation, AnnotationType type) {
      return tree.floor(annotation, type);
   }

   /**
    * Removes an annotation from the document
    *
    * @param annotation The annotation to detach
    * @return the boolean
    */
   public boolean remove(Annotation annotation) {
      boolean removed = tree.remove(annotation);
      if (removed) {
         idAnnotationMap.remove(annotation.getId());
      }
      return removed;
   }

   /**
    * Removes all annotations of a given type and marks that type as not completed.
    *
    * @param type the type
    * @return The list of annotations that were removed
    */
   public List<Annotation> removeAll(AnnotationType type) {
      if (type != null) {
         setIsCompleted(type, false, null);
         List<Annotation> annotations = select(a -> a.isInstance(type));
         annotations.forEach(this::remove);
         return annotations;
      }
      return Collections.emptyList();
   }

   /**
    * <p>Selects all annotations of a given annotation type within a given range and matching a given criteria.</p>
    *
    * @param span     the range in which to search form annotations
    * @param criteria the criteria that an annotation must match
    * @return A list of annotations that are an instance of the given class within the given range and matching the
    * given criteria
    */
   public List<Annotation> select(Span span, Predicate<? super Annotation> criteria) {
      return Streams.asStream(tree.overlapping(span)).filter(criteria).sorted().collect(Collectors.toList());
   }

   /**
    * <p>Selects all annotations of a given annotation type and matching a given criteria.</p>
    *
    * @param criteria the criteria that an annotation must match
    * @return A list of annotations that are an instance of the given class and matching the given criteria
    */
   public List<Annotation> select(Predicate<? super Annotation> criteria) {
      return tree.stream()
                 .filter(criteria)
                 .collect(Collectors.toList());
   }

   /**
    * Sets the given annotation type as being completed or not
    *
    * @param type                 the annotation type
    * @param isCompleted          True if the annotation is completed, False if not.
    * @param annotatorInformation the annotator information
    */
   public void setIsCompleted(AnnotatableType type, boolean isCompleted, String annotatorInformation) {
      if (isCompleted) {
         completed.put(type, annotatorInformation);
      } else {
         completed.remove(type);
      }
   }

   /**
    * The number of annotations in the set
    *
    * @return Number of annotations in the set
    */
   public int size() {
      return tree.size();
   }

   /**
    * Stream stream.
    *
    * @return the stream
    */
   public Stream<Annotation> stream() {
      return tree.stream();
   }

   @Override
   public String toString() {
      return tree.toString();
   }

}// END OF DefaultAnnotationSet
