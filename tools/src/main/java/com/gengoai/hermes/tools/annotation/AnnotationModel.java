/*
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

package com.gengoai.hermes.tools.annotation;

import com.gengoai.HierarchicalEnumValue;
import com.gengoai.Tag;
import com.gengoai.collection.tree.Span;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import lombok.NonNull;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AnnotationModel extends AbstractTableModel implements Iterable<Annotation> {
   private final List<Annotation> annotations = new CopyOnWriteArrayList<>();
   private final List<String> lastValues = new CopyOnWriteArrayList<>();
   private final AnnotationType annotationType;
   private final Vector<BiConsumer<AnnotationModel, Annotation>> removeAnnotationListeners = new Vector<>();

   public AnnotationModel(AnnotationType annotationType) {
      this.annotationType = annotationType;
   }

   public void addAnnotation(@NonNull Annotation annotation) {
      annotations.add(annotation);
      lastValues.add(getTagName(annotation));
      fireTableDataChanged();
   }

   public void addAnnotations(@NonNull HString hString) {
      hString.enclosedAnnotations(annotationType).forEach(this::addAnnotation);
   }

   public void clear() {
      this.annotations.clear();
      this.lastValues.clear();
   }

   public Annotation get(int row) {
      return annotations.get(row);
   }

   @Override
   public Class<?> getColumnClass(int columnIndex) {
      switch(columnIndex) {
         case 0:
         case 1:
            return Integer.class;
         case 3:
            return HString.class;
         default:
            return String.class;

      }
   }

   @Override
   public int getColumnCount() {
      return 4;
   }

   @Override
   public String getColumnName(int column) {
      switch(column) {
         case 0:
            return "Start";
         case 1:
            return "End";
         case 2:
            return "Type";
         default:
            return "Surface";
      }
   }

   public List<Annotation> getEnclosingAnnotations(int start, int end) {
      final Span span = Span.of(start, end);
      return annotations.stream().filter(span::encloses).collect(Collectors.toList());
   }

   public String getLastTag(int row) {
      return lastValues.get(row);
   }

   public List<Annotation> getOverlappingAnnotations(int start, int end) {
      final Span span = Span.of(start, end);
      return annotations.stream()
                        .filter(span::overlaps)
                        .sorted(Comparator.comparingInt(Span::length).reversed())
                        .collect(Collectors.toList());
   }

   @Override
   public int getRowCount() {
      return annotations.size();
   }

   private String getTagName(Annotation a) {
      Tag t = a.getTag();
      if(t == null) {
         return "DEFAULT";
      }
      if(t instanceof HierarchicalEnumValue) {
         return Cast.<HierarchicalEnumValue<?>>as(t).label();
      }
      return t.name();
   }

   @Override
   public Object getValueAt(int row, int column) {
      Annotation a = annotations.get(row);
      switch(column) {
         case 0:
            return a.start();
         case 1:
            return a.end();
         case 2:
            return getTagName(a);
         default:
            return a;
      }
   }

   public int indexOf(Annotation annotation) {
      return annotations.indexOf(annotation);
   }

   @Override
   public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex == 2;
   }

   @Override
   public Iterator<Annotation> iterator() {
      return annotations.iterator();
   }

   public AnnotationModel onRemoveAnnotation(@NonNull BiConsumer<AnnotationModel, Annotation> listener) {
      removeAnnotationListeners.add(listener);
      return this;
   }

   public void remove(@NonNull Annotation annotation) {
      int index = annotations.indexOf(annotation);
      if(index >= 0) {
         annotations.remove(index);
         lastValues.remove(index);
         annotation.document().remove(annotation);
         fireTableRowsDeleted(index, index);
         removeAnnotationListeners.forEach(c -> c.accept(this, annotation));
      }
   }

   public void removeAll(Collection<Annotation> toRemove) {
      toRemove.forEach(this::remove);
   }

   @Override
   public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if(columnIndex == 2) {
         lastValues.set(rowIndex, getValueAt(rowIndex, 2).toString());
         annotations.get(rowIndex)
                    .put(annotationType.getTagAttribute(), annotationType.getTagAttribute().decode(aValue));
         fireTableCellUpdated(rowIndex, columnIndex);
      }
   }

   public int size() {
      return annotations.size();
   }

   public AnnotationModel tableModelListener(@NonNull BiConsumer<AnnotationModel, TableModelEvent> listener) {
      addTableModelListener(l -> listener.accept(this, l));
      return this;
   }
}//END OF AnnotationModel
