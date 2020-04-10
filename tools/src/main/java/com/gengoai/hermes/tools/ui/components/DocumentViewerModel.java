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

package com.gengoai.hermes.tools.ui.components;

import com.gengoai.LogUtils;
import com.gengoai.Tag;
import com.gengoai.collection.tree.Span;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.swing.component.model.MangoTableModel;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Iterator;
import java.util.List;

import static com.gengoai.tuple.Tuples.$;

@Log
public class DocumentViewerModel extends MangoTableModel implements Iterable<Annotation> {
   public static final int ANNOTATION_INDEX = 4;
   public static final int CONFIDENCE_INDEX = 3;
   public static final int END_INDEX = 1;
   public static final int START_INDEX = 0;
   public static final int TYPE_INDEX = 2;
   private final AnnotationLayer annotationLayer;
   private final Document document;

   public DocumentViewerModel(Document document, AnnotationLayer layer) {
      super($("Start", Integer.class, false),
            $("End", Integer.class, false),
            $("Type", Tag.class, true),
            $("Confidence", Double.class, false),
            $("Annotation", Annotation.class, false));
      this.document = document;
      annotationLayer = layer;
      for(Annotation annotation : document.annotations(layer.getAnnotationType())) {
         addAnnotation(annotation);
      }
   }

   private void addAnnotation(@NonNull Annotation annotation) {
      addRow(annotation.start(),
             annotation.end(),
             annotation.attribute(annotationLayer.getAttributeType()),
             annotation.attribute(Types.CONFIDENCE, 1.0),
             annotation);
   }

   public void createAnnotation(int start, int end, String tag) {
      createAnnotation(start, end, annotationLayer.getAttributeType().decode(tag));
   }

   public void createAnnotation(int start, int end, Tag tag) {
      if(annotationLayer.getAnnotationType() == Types.TOKEN) {
         return;
      }
      Annotation annotation = document.annotationBuilder(annotationLayer.getAnnotationType())
                                      .start(start)
                                      .end(end)
                                      .attribute(annotationLayer.getAttributeType(), tag)
                                      .createAttached();
      LogUtils.logInfo(log, "Creating annotation: {0}", annotation);
      addRow(annotation.start(),
             annotation.end(),
             annotation.attribute(annotationLayer.getAttributeType()),
             annotation.attribute(Types.CONFIDENCE, 1.0),
             annotation);
   }

   public Annotation getAnnotationForRow(int row) {
      return Cast.as(dataVector.get(row).get(ANNOTATION_INDEX));
   }

   public List<Annotation> getEnclosingAnnotations(int start, int end) {
      var target = Span.of(start, end);
      return document.annotations(annotationLayer.getAnnotationType(), target, target::encloses);
   }

   public List<Annotation> getOverlappingAnnotations(int start, int end) {
      return document.annotations(annotationLayer.getAnnotationType(), Span.of(start, end));
   }

   public String getStyleForAnnotation(Annotation annotation) {
      return Cast.<Tag>as(annotation.attribute(annotationLayer.getAttributeType())).label();
   }

   public Tag getTagForRow(int row) {
      return Cast.as(dataVector.get(row).get(TYPE_INDEX));
   }

   public int indexOf(@NonNull Annotation annotation) {
      for(int row = 0; row < getRowCount(); row++) {
         if(annotation.equals(getValueAt(row, ANNOTATION_INDEX))) {
            return row;
         }
      }
      return -1;
   }

   @Override
   public Iterator<Annotation> iterator() {
      return document.annotations(annotationLayer.getAnnotationType()).iterator();
   }

   public void remove(@NonNull Annotation annotation) {
      removeRow(indexOf(annotation));
   }

   public void removeAll(@NonNull Iterable<Annotation> toDelete) {
      for(Annotation annotation : toDelete) {
         removeRow(indexOf(annotation));
      }
   }

   @Override
   public void removeRow(int row) {
      Annotation a = getAnnotationForRow(row);
      LogUtils.logInfo(log, "Removing Annotation: {0}", a);
      document.remove(a);
      super.removeRow(row);
   }

   public int size() {
      return dataVector.size();
   }
}//END OF AnnotationModel
