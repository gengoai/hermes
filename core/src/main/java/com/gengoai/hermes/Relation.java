package com.gengoai.hermes;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

/**
 * <p>
 * Relations provide a mechanism to link two Annotations.  Relations are directional, i.e. they have a source and a
 * target, and form a directed graph between annotations on the document.  Relations can represent any type of link,
 * but often represent syntactic (e.g. dependency relations), semantic (e.g. semantic roles), or pragmatic (e.g. dialog
 * acts) information. Relations, like attributes, are stored as key value pairs with the key being the {@link
 * RelationType}  and the value being a String representing the label. Relations are associated with individual
 * annotations (i.e. tokens for dependency relations, entities for  co-reference).
 * </p>
 *
 * @author David B. Bracewell
 */
@Data
public final class Relation implements Serializable {
   private static final long serialVersionUID = 1L;
   private final long target;
   private final RelationType type;
   private String value;

   /**
    * Instantiates a new Relation.
    *
    * @param type   the relation type
    * @param value  the relation value
    * @param target the id of the target relation
    */
   public Relation(@NonNull RelationType type, @NonNull String value, long target) {
      this.type = type;
      this.value = value;
      this.target = target;
   }

   /**
    * Gets the target of the relation.
    *
    * @param hString the HString to use identify the target annotation.
    * @return the target annotation.
    */
   public Annotation getTarget(@NonNull HString hString) {
      if(hString == null || hString.document() == null) {
         return Fragments.orphanedAnnotation(AnnotationType.ROOT);
      }
      return hString.document().annotation(target);
   }

}// END OF Relation
