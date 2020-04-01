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

import com.formdev.flatlaf.FlatDarculaLaf;
import com.gengoai.SystemInfo;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.tools.ui.components.CorpusView;
import com.gengoai.io.Resources;
import com.gengoai.swing.SwingApplication;
import com.gengoai.swing.components.TabbedPane;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collections;
import java.util.Set;

public class Test extends SwingApplication {
   private final JFileChooser fileChooser = new JFileChooser();
   private final TabbedPane tabbedPane = new TabbedPane();
   private Corpus openCorpus;
   private File lastFile = new File(SystemInfo.USER_HOME);
   private AnnotationTask[] annotationTasks = null;

   public static void main(String[] args) {
      FlatDarculaLaf.install();
      runApplication(Test::new, "Test", args);
   }

   private void createAnnotationViewer(Document document, AnnotationTask task) {

   }

   private void createCorpusView() {
      var cv = new CorpusView(lastFile.toString(),
                              openCorpus,
                              annotationTasks);
      cv.setOnDocumentOpen((docId, task) -> {
         if(tabbedPane.indexOfTab(docId) != -1) {
            JOptionPane.showMessageDialog(null,
                                          "Document is already open for annotation",
                                          "Cannot Annotate Document",
                                          JOptionPane.ERROR_MESSAGE);
            return;
         }
         var dae = new DocumentAnnotationEditor(openCorpus.getDocument(docId),
                                                task.getAnnotationType(),
                                                task.createTagModel());
         tabbedPane.addTab(docId, dae);
         tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
      });
      tabbedPane.addTab("Corpus", cv);
      tabbedPane.setCloseButtonVisible(tabbedPane.getTabCount() - 1, false);
   }

   @Override
   public Set<String> getDependentPackages() {
      return Collections.singleton("com.gengoai.hermes");
   }

   @Override
   protected void initControls() throws Exception {
      annotationTasks = Resources.fromClasspath("annotation/tasks")
                                 .getChildren("*.json")
                                 .stream()
                                 .map(AnnotationTask::load)
                                 .toArray(AnnotationTask[]::new);
      setTitle("HERMES Annotation Editor");
      setPreferredSize(new Dimension(1024, 768));
      setLayout(new BorderLayout());
      add(tabbedPane, BorderLayout.CENTER);
      initMenu();
      pack();
      invalidate();

      tabbedPane.setOnCloseClicked(c -> {
         DocumentAnnotationEditor dae = Cast.as(c);
         if(dae.hasChanged.get()) {
            openCorpus.update(dae.document);
         }
         tabbedPane.remove(c);
      });

   }

   private void initMenu() {
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(Box.createRigidArea(new Dimension(5, 25)));
      setJMenuBar(menuBar);

      JMenu menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);
      JMenuItem fileOpen = new JMenuItem("Open...", KeyEvent.VK_O);

      fileOpen.addActionListener(a -> {
         fileChooser.setName("Open Corpus...");
         fileChooser.setCurrentDirectory(lastFile);
         fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         if(fileChooser.showOpenDialog(mainWindowFrame) == JFileChooser.APPROVE_OPTION) {
            lastFile = fileChooser.getSelectedFile();
            openCorpus = Corpus.open(Resources.fromFile(lastFile));
            tabbedPane.removeAll();
            createCorpusView();
         }
      });
      menu.add(fileOpen);
      menuBar.add(menu);
   }

}//END OF Test
