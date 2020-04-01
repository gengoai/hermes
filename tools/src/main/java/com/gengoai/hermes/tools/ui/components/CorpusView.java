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

import com.gengoai.conversion.Cast;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.tools.annotation.AnnotationTask;
import com.gengoai.swing.ColorUtils;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.fluent.FluentJPanel;
import lombok.Getter;
import lombok.Setter;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.gengoai.swing.components.Components.*;
import static com.gengoai.swing.fluent.FluentJLabel.label;
import static javax.swing.BorderFactory.createCompoundBorder;

public class CorpusView extends FluentJPanel {
   @Getter
   private final Corpus corpus;
   @Getter
   private final String corpusSpecification;
   private final JComboBox<AnnotationTask> cboxTasks;
   private final AtomicReference<String> selectedDocumentId = new AtomicReference<>();
   @Setter
   private BiConsumer<String, AnnotationTask> onDocumentOpen = (s, a) -> System.out.println(selectedDocumentId.get());

   public CorpusView(String corpusSpecification, Corpus corpus) {
      this(corpusSpecification, corpus, null);
   }

   public CorpusView(String corpusSpecification, Corpus corpus, AnnotationTask[] tasks) {
      setLayout(new BorderLayout(0, 10));
      emptyBorderWithMargin(12, 12, 12, 12);

      this.corpusSpecification = corpusSpecification;
      this.corpus = corpus;
      if(tasks == null || tasks.length == 0) {
         this.cboxTasks = null;
      } else {
         this.cboxTasks = new JComboBox<>(tasks);
      }

      north(createStatsPanel());
      center(createDocumentPicker());
   }

   private JPanel createDocumentPicker() {
      var tblDocumentIds = table(false, "Document Id")
            .alternateRowColor(Color::darker)
            .nonEditable()
            .singleSelectionModel()
            .onMouseClicked(($, e) -> selectedDocumentId.set($.getValueAt($.getSelectedRow(), 0).toString()))
            .onMouseDoubleClicked(($, e) -> performDocumentOpen());
      corpus.getIds().forEach(tblDocumentIds::addRow);
      return vbox(5)
            .add(hbox(5).add(label("Filter:").font(Font.BOLD, 18))
                        .add(textField().onKeyReleased(($, e) -> {
                           if($.getText().length() >= 3) {
                              TableRowSorter<TableModel> sorter = new TableRowSorter<>(tblDocumentIds.getModel());
                              sorter.setRowFilter(RowFilter.regexFilter("^" + $.getText()));
                              tblDocumentIds.setRowSorter(sorter);
                           } else {
                              tblDocumentIds.setRowSorter(null);
                           }
                        }))
                        .setResizeWithComponent(1))
            .add(scrollPane(tblDocumentIds))
            .addIf($ -> cboxTasks != null,
                   () -> hbox(5, cboxTasks).add(label("Task:").font(Font.BOLD, 18))
                                           .add(cboxTasks))
            .add(hbox(button(cboxTasks == null
                             ? "View"
                             : "Annotate").actionListener(($, e) -> performDocumentOpen()))
                       .setResizeWithComponent(0))
            .setResizeWithComponent(1);
   }

   private JPanel createStatsPanel() {
      return vbox(10, $ -> {
         $.border(createCompoundBorder(BorderFactory.createLineBorder($.getBackground().brighter(), 2),
                                       BorderFactory.createEmptyBorder(12, 12, 12, 12)))
          .background($.getBackground().darker());
         Color fontColor = ColorUtils.calculateBestFontColor($.getBackground());
         var lblCSName = label("Corpus Specification:")
               .emptyBorder()
               .minFontSize(18)
               .foreground(fontColor)
               .fontStyle(Font.BOLD)
               .preferredSize($lbl -> new Dimension(Fonts.getFontWidth($lbl, $lbl.getText()),
                                                    Fonts.getFontHeight($lbl)));
         var lblCSValue = label(corpusSpecification)
               .emptyBorder()
               .minFontSize(18)
               .fontStyle(Font.BOLD)
               .foreground(fontColor)
               .preferredSize($lbl -> new Dimension(Fonts.getAverageFontWidth($lbl) * 40,
                                                    Fonts.getFontHeight($lbl)));
         var lblDCName = label("# of Documents:")
               .emptyBorder()
               .minFontSize(18)
               .fontStyle(Font.BOLD)
               .foreground(fontColor)
               .alignTextHorizontalRight()
               .preferredSize($lbl -> new Dimension(Fonts.getFontWidth($lbl, lblCSName.getText()),
                                                    Fonts.getFontHeight($lbl)));
         var lblDCValue = label(DecimalFormat.getIntegerInstance().format(corpus.size()))
               .emptyBorder()
               .minFontSize(18)
               .fontStyle(Font.BOLD)
               .foreground(fontColor)
               .preferredSize($lbl -> new Dimension(Fonts.getAverageFontWidth($lbl) * 40,
                                                    Fonts.getFontHeight($lbl)));
         $.add(hbox(5, lblCSValue).add(lblCSName).add(lblCSValue).translucent())
          .add(hbox(5, lblDCValue).add(lblDCName).add(lblDCValue).translucent());
      });
   }

   private void performDocumentOpen() {
      if(onDocumentOpen != null) {
         AnnotationTask task = Cast.as(cboxTasks.getSelectedItem());
         onDocumentOpen.accept(selectedDocumentId.get(), task);
      }
   }

}//END OF CorpusView
