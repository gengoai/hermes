package com.gengoai.hermes;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * The type Relation.
 *
 * @author David B. Bracewell
 */
public final class Relation implements Serializable {
   private static final long serialVersionUID = 1L;
   private final long target;
   private final RelationType type;
   private String value;

   /**
    * Instantiates a new Relation.
    *
    * @param type   the type
    * @param value  the value
    * @param target the target
    */
   public Relation(RelationType type, String value, long target) {
      this.type = type;
      this.value = value;
      this.target = target;
   }

   /**
    * Gets target.
    *
    * @param hString the h string
    * @return the target
    */
   public Optional<Annotation> getTarget(HString hString) {
      if (hString == null || hString.document() == null) {
         return Optional.empty();
      }
      return hString.document().annotation(target);
   }

   /**
    * Gets target.
    *
    * @return the target
    */
   public long getTarget() {
      return target;
   }

   /**
    * Gets type.
    *
    * @return the type
    */
   public RelationType getType() {
      return type;
   }

   /**
    * Gets value.
    *
    * @return the value
    */
   public String getValue() {
      return value;
   }

   /**
    * Sets value.
    *
    * @param value the value
    */
   public void setValue(String value) {
      this.value = value;
   }


   @Override
   public String toString() {
      return "Relation{" +
                "target=" + target +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Relation)) return false;
      Relation relation = (Relation) o;
      return target == relation.target &&
                Objects.equals(type, relation.type) &&
                Objects.equals(value, relation.value);
   }

   @Override
   public int hashCode() {
      return Objects.hash(target, type, value);
   }

}// END OF Relation
