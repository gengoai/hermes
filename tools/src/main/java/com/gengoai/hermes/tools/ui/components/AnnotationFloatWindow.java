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

import com.gengoai.hermes.Annotation;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.component.HBox;
import com.gengoai.swing.component.StyledSpan;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import static com.gengoai.function.Functional.with;
import static javax.swing.BorderFactory.*;

public class AnnotationFloatWindow extends JDialog {
   private static final Icon DELETED_ICON = FontAwesome.TIMES_CIRCLE.createIcon(24, Color.RED.darker());
   private final DocumentViewer owner;
   private final JButton btnDeleteAnnotation;
   private final JButton btnAddAnnotation;
   private final JComboBox<String> cbTags = new JComboBox<>();
   private List<Annotation> annotations;
   private int start;
   private int end;

   public AnnotationFloatWindow(DocumentViewer owner) {
      this.owner = owner;
      setUndecorated(true);
      setLayout(new BorderLayout());

      btnDeleteAnnotation = with(new JButton(FontAwesome.TIMES_CIRCLE.createIcon(24)), $ -> {
         $.setRolloverIcon(FontAwesome.TIMES_CIRCLE.createIcon(24, Color.RED));
         $.setToolTipText("Delete Selected Annotation");
         $.addActionListener(this::onDeleteClick);
         $.setContentAreaFilled(false);
         $.setEnabled(false);
      });

      btnAddAnnotation = with(new JButton(FontAwesome.CHECK_CIRCLE.createIcon(24)), $ -> {
         $.setRolloverIcon(FontAwesome.CHECK_CIRCLE.createIcon(24, Color.GREEN));
         $.setDisabledIcon(FontAwesome.CHECK_CIRCLE.createIcon(24, Color.GREEN.darker()));
         $.setToolTipText("Add Selected Annotation");
         $.addActionListener(this::onAnnotateClick);
         $.setContentAreaFilled(false);
         $.setEnabled(true);
      });

      add(with(new HBox(), $ -> {
         $.setBackground(UIManager.getColor("activeCaption"));
         $.setBorder(createCompoundBorder(createLineBorder($.getBackground().darker(), 2, true),
                                          createEmptyBorder(4, 2, 4, 2)));
         $.add(cbTags);
         $.add(btnAddAnnotation);
         $.add(btnDeleteAnnotation);
      }), BorderLayout.CENTER);
   }

   protected void onAnnotateClick(ActionEvent a) {
      if(cbTags.getSelectedItem() != null) {
         btnDeleteAnnotation.setEnabled(true);
         btnAddAnnotation.setEnabled(false);
         owner.getModel().createAnnotation(start, end, cbTags.getSelectedItem().toString());
         cbTags.setEnabled(false);
      }
   }

   protected void onDeleteClick(ActionEvent a) {
      owner.controller.deleteSelectedAnnotationsInDocumentView();
      btnDeleteAnnotation.setEnabled(false);
      btnAddAnnotation.setEnabled(true);
      cbTags.setEnabled(true);
   }

   public void setAnnotationLayer(AnnotationLayer layer) {
      cbTags.removeAllItems();
      int maxWidth = 0;
      for(String validTagLabel : layer.getValidTagLabels()) {
         cbTags.addItem(validTagLabel);
         maxWidth = Math.max(maxWidth, Fonts.getFontWidth(cbTags, validTagLabel));
      }
      cbTags.setPreferredSize(new Dimension(maxWidth, 28));
      setMinimumSize(new Dimension(maxWidth + (2 * 32), 36));
   }

   public void setSelection(int start, int end, StyledSpan span) {
      this.start = start;
      this.end = end;
      btnAddAnnotation.setEnabled(span == null);
      btnDeleteAnnotation.setEnabled(span != null);
      cbTags.setEnabled(span == null);
      if(span != null) {
         btnDeleteAnnotation.setDisabledIcon(DELETED_ICON);
         cbTags.setSelectedItem(span.label);
      } else {
         btnDeleteAnnotation.setDisabledIcon(null);
      }
   }

}//END OF AnnotationFloatWindow
