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

import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.tools.ui.components.AnnotationLayer;
import com.gengoai.hermes.tools.ui.components.AvailableAnnotationLayers;
import com.gengoai.hermes.tools.ui.components.CorpusView;
import com.gengoai.hermes.tools.ui.components.DocumentViewer;
import com.gengoai.io.Resources;
import com.gengoai.string.Strings;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.component.MangoLoggingWindow;
import com.gengoai.swing.component.MangoTabbedPane;
import com.gengoai.swing.component.MangoTitlePane;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import static com.gengoai.swing.component.Components.dim;
import static com.gengoai.swing.component.Components.label;

public class AnnotationGUI extends HermesGUI {
   private JLabel lblCorpusName;
   private JLabel lblAnnotationLayer;
   private List<DocumentViewer> tabs = new ArrayList<>();
   private MangoTabbedPane tbPane;
   private CorpusView cView;

   public static void main(String[] args) {
      runApplication(AnnotationGUI::new, "AnnotationGUI", args);
   }

   @Override
   protected void initControls() throws Exception {
      AvailableAnnotationLayers.loadFrom(Resources.fromClasspath("annotation/layers/"));
      tbPane = new MangoTabbedPane();
      cView = new CorpusView("/data/kant_corpus/",
                             Corpus.open("/data/kant_corpus/"),
                             AvailableAnnotationLayers.values());
      cView.setOnDocumentOpen(this::openDocument);
      tbPane.addTab("Corpus", cView);

      MangoLoggingWindow loggingWindow = new MangoLoggingWindow();
      loggingWindow.setMinimumSize(dim(0, 300));
      var tp = new MangoTitlePane("Logs", true, loggingWindow);
      tp.setVisible(false);
      var sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tbPane, tp);
      sp.setResizeWeight(0);
      sp.setContinuousLayout(true);
      setCenterComponent(sp);

      var btn = FontAwesome.WINDOW_MAXIMIZE.createButton(12);
      btn.addActionListener(a -> {
         tp.setVisible(!tp.isVisible());
         if(tp.isVisible()) {
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
      lblCorpusName.setText("Corpus: " + "/data/news_1m_sentences");
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
         tbPane.addTab(Strings.abbreviate(id, 7), new DocumentViewer(cView.getCorpus().getDocument(id), layer));
         index = tbPane.getTabCount() - 1;
         tbPane.setToolTipTextAt(index, id);
      }
      tbPane.setSelectedIndex(index);
   }
}//END OF AnnotationGUI
