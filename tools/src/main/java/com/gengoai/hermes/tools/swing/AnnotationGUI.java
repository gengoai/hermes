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

package com.gengoai.hermes.tools.swing;

import com.gengoai.SystemInfo;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.tools.ui.components.AnnotationLayer;
import com.gengoai.hermes.tools.ui.components.AvailableAnnotationLayers;
import com.gengoai.hermes.tools.ui.components.CorpusView;
import com.gengoai.hermes.tools.ui.components.DocumentViewer;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.Menus;
import com.gengoai.swing.component.MangoLoggingWindow;
import com.gengoai.swing.component.MangoTabbedPane;
import com.gengoai.swing.component.MangoTitlePane;
import com.gengoai.swing.component.listener.FluentAction;
import com.gengoai.swing.component.listener.SwingListeners;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import static com.gengoai.swing.component.Components.dim;
import static com.gengoai.swing.component.Components.label;

public class AnnotationGUI extends HermesGUI {
   private final JFileChooser openDialog = new JFileChooser();
   private JLabel lblCorpusName;
   private JLabel lblAnnotationLayer;
   private CorpusView corpusView;
   private MangoTabbedPane tbPane;
   private final FluentAction openCorpus = SwingListeners.fluentAction("Open", this::onOpenCorpus)
                                                         .mnemonic('O')
                                                         .accelerator(KeyStroke.getKeyStroke('O',
                                                                                             KeyEvent.CTRL_DOWN_MASK));

   public static void main(String[] args) {
      runApplication(AnnotationGUI::new, "AnnotationGUI", args);
   }

   private void closeAll() {
      for(int i = tbPane.getTabCount() - 1; i >= 0; i--) {
         if(tbPane.getComponentAt(i) instanceof DocumentViewer) {
            DocumentViewer documentViewer = Cast.as(tbPane.getComponentAt(i));
            if(documentViewer.isDirty()) {
               corpusView.getCorpus().update(documentViewer.getDocument());
            }
         }
         tbPane.remove(i);
      }
   }

   @Override
   protected void initControls() throws Exception {
      mainWindowFrame.setTitle("Hermes Annotation GUI");
      ImageIcon ii = new ImageIcon(Resources.fromClasspath("img/editor.png").readBytes());
      mainWindowFrame.setIconImage(ii.getImage());

      openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      openDialog.setCurrentDirectory(properties.get("open_dialog_directory").as(File.class,
                                                                                new File(SystemInfo.USER_HOME)));
      menuBar(Menus.menu("File", 'F',
                         Menus.menuItem(openCorpus)));

      tbPane = new MangoTabbedPane();
      tbPane.addTabClosedListener(t -> {
         if(t == corpusView) {
            closeAll();
         } else {
            DocumentViewer documentViewer = Cast.as(t);
            if(documentViewer.isDirty()) {
               corpusView.getCorpus().update(documentViewer.getDocument());
            }
         }
      });
      AvailableAnnotationLayers.loadFrom(Resources.fromClasspath("annotation/layers/"));
      Resource layerDir = Resources.from("annotation_layers");
      if(layerDir.exists()) {
         AvailableAnnotationLayers.loadFrom(layerDir);
      }

      var loggingWindow = initLoggingWindow();
      var sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                              tbPane,
                              loggingWindow);
      sp.setResizeWeight(0);
      sp.setContinuousLayout(true);
      setCenterComponent(sp);

      var btn = FontAwesome.WINDOW_MAXIMIZE.createButton(12);
      btn.addActionListener(a -> {
         loggingWindow.setVisible(!loggingWindow.isVisible());
         if(loggingWindow.isVisible()) {
            sp.setDividerLocation(mainWindowFrame.getHeight() - 300);
         }
      });
      statusBar(lblCorpusName = label("AnnotationGUI", Font.BOLD, 12),
                new Dimension(10, 10),
                lblAnnotationLayer = label("", Font.BOLD, 12),
                new Dimension(10, 10),
                Box.createHorizontalGlue(),
                btn
               );
   }

   private MangoTitlePane initLoggingWindow() {
      MangoLoggingWindow loggingWindow = new MangoLoggingWindow();
      loggingWindow.setMinimumSize(dim(0, 300));
      var tp = new MangoTitlePane("Logs", true, loggingWindow);
      tp.setVisible(false);
      return tp;
   }

   @Override
   protected void onClose() throws Exception {
      super.onClose();
      if(openDialog.getSelectedFile() != null) {
         properties.set("open_dialog_directory", openDialog.getSelectedFile().getParentFile().getAbsolutePath());
      }
   }

   private void onOpenCorpus(ActionEvent a) {
      if(openDialog.showDialog(null, "OK") == JFileChooser.APPROVE_OPTION) {
         closeAll();
         File dir = openDialog.getSelectedFile();
         lblCorpusName.setText("Corpus: " + dir.getAbsolutePath());
         corpusView = new CorpusView(dir.getAbsolutePath(),
                                     Corpus.open(dir.getAbsolutePath()),
                                     AvailableAnnotationLayers.values());
         tbPane.addTab("Corpus", corpusView);
         corpusView.setOnDocumentOpen(this::openDocument);
      }
   }

   private void openDocument(String id, AnnotationLayer layer) {
      int index = -1;
      for(int i = 1; i < tbPane.getTabCount(); i++) {
         if(Strings.safeEquals(tbPane.getToolTipTextAt(i), id, true)) {
            index = i;
            break;
         }
      }
      if(index == -1) {
         tbPane.addTab(Strings.abbreviate(id, 7), new DocumentViewer(corpusView.getCorpus().getDocument(id), layer));
         index = tbPane.getTabCount() - 1;
         tbPane.setToolTipTextAt(index, id);
      }
      tbPane.setSelectedIndex(index);
   }
}//END OF AnnotationGUI
