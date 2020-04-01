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

import com.gengoai.conversion.Cast;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.swing.listeners.SwingListeners;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.DecimalFormat;

import static com.gengoai.function.Functional.with;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.TRAILING;

public class CorpusPane extends JPanel {
   public final Corpus openCorpus;
   public final String corpusLocation;
   private final DefaultTableModel tableModel = new DefaultTableModel() {
      @Override
      public boolean isCellEditable(int row, int column) {
         return false;
      }
   };
   private final JTabbedPane tabbedPane;
   private final JTable tblDocumentIds;
   private final JComboBox<AnnotationTask> cboxTasks;

   public CorpusPane(JTabbedPane tabbedPane, String corpusLocation, Corpus corpus, AnnotationTask[] tasks) {
      super(new GridBagLayout());
      this.tabbedPane = tabbedPane;
      this.openCorpus = corpus;
      this.corpusLocation = corpusLocation;
      tableModel.addColumn("Document Id");
      this.tblDocumentIds = with(new JTable(tableModel), table -> {
         for(String id : openCorpus.getIds()) {
            tableModel.addRow(new String[]{id});
         }
         table.addMouseListener(SwingListeners.mouseClicked(e -> {
            if(e.getClickCount() == 2) {
               openDocument();
            }
         }));
      });
      this.cboxTasks = new JComboBox<>(tasks);

      createStatsView();
      addDocumentSelection();
   }

   private void addDocumentSelection() {
      GridBagConstraints c = new GridBagConstraints();
      c.gridy = 2;
      c.fill = GridBagConstraints.HORIZONTAL;
      var filterPanel = add(with(new JPanel(), p -> {
         p.setLayout(new FlowLayout());
         p.add(new JLabel("Filter"));
         var filter = new JTextField();
         filter.addKeyListener(SwingListeners.keyReleased(e -> {
            if(filter.getText().length() >= 3) {
               TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
               sorter.setRowFilter(RowFilter.regexFilter("^" + filter.getText()));
               tblDocumentIds.setRowSorter(sorter);
            }
         }));
         filter.setColumns(30);
         p.add(filter);
      }));
      add(filterPanel, c);
      c.gridy = 3;
      add(new JScrollPane(tblDocumentIds), c);

      c.gridy = 4;
      add(with(new JPanel(), p -> {
         p.setLayout(new FlowLayout());
         p.add(new JLabel("Task"));
         p.add(cboxTasks);
         cboxTasks.setPreferredSize(new Dimension(500, 30));
      }), c);

      c.gridy = 5;
      JButton btnOpen = new JButton("Annotate...");
      btnOpen.addActionListener(a -> openDocument());
      add(btnOpen, c);
   }

   private void createStatsView() {
      JPanel panel = new JPanel();
      GroupLayout layout = new GroupLayout(panel);
      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);
      panel.setLayout(layout);
      var lblCorpus = with(new JLabel("Corpus"), l -> {
         l.setFont(l.getFont().deriveFont(Font.BOLD));
         l.setVerticalAlignment(JLabel.CENTER);
      });
      var txtCorpusLoc = with(new JLabel(corpusLocation), l -> {
         l.setFont(l.getFont().deriveFont(Font.BOLD));
         l.setVerticalAlignment(JLabel.CENTER);
      });
      var lblSize = with(new JLabel("# of Documents"), l -> {
         l.setFont(l.getFont().deriveFont(Font.BOLD));
         l.setVerticalAlignment(JLabel.CENTER);
      });
      var txtSize = with(new JLabel(DecimalFormat.getIntegerInstance().format(openCorpus.size())), l -> {
         l.setFont(l.getFont().deriveFont(Font.BOLD));
         l.setVerticalAlignment(JLabel.CENTER);
      });

      layout.setHorizontalGroup(layout.createSequentialGroup()
                                      .addGroup(layout.createParallelGroup(TRAILING)
                                                      .addComponent(lblCorpus)
                                                      .addComponent(lblSize))
                                      .addGroup(layout.createParallelGroup()
                                                      .addComponent(txtCorpusLoc)
                                                      .addComponent(txtSize))
                               );
      layout.setVerticalGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(BASELINE)
                                                    .addComponent(lblCorpus)
                                                    .addComponent(txtCorpusLoc))
                                    .addGroup(layout.createParallelGroup(BASELINE)
                                                    .addComponent(lblSize)
                                                    .addComponent(txtSize))
                             );
      add(panel);
   }

   private void openDocument() {
      final var doc = tableModel.getValueAt(tblDocumentIds.convertRowIndexToModel(tblDocumentIds.getSelectedRow()),
                                            0)
                                .toString();
      if(tabbedPane.indexOfTab(doc) != -1) {
         JOptionPane.showMessageDialog(this,
                                       "Document is already open for annotation",
                                       "Cannot Annotate Document",
                                       JOptionPane.ERROR_MESSAGE);
         return;
      }
      AnnotationTask task = Cast.as(cboxTasks.getSelectedItem());
      var dae = new DocumentAnnotationEditor(openCorpus.getDocument(doc),
                                             task.getAnnotationType(),
                                             task.createTagModel());
      tabbedPane.addTab(doc, dae);
      tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
   }
}//END OF CorpusPane
