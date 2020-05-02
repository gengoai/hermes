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

import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.tools.ui.components.AnnotationLayer;
import com.gengoai.hermes.tools.ui.components.AvailableAnnotationLayers;
import com.gengoai.hermes.tools.ui.components.CorpusView;
import com.gengoai.hermes.tools.ui.components.DocumentViewer;
import com.gengoai.io.Resources;
import com.gengoai.json.Json;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.component.MangoLoggingWindow;
import com.gengoai.swing.component.MangoTitlePane;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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

   public static void main(String[] args) {
      runApplication(AnnotationGUI::new, "AnnotationGUI", args);
   }

   @Override
   protected void initControls() throws Exception {
      AvailableAnnotationLayers.loadFrom(Resources.fromClasspath("annotation/layers/"));

      AnnotationLayer annotationLayer = Json.parse(Resources.fromClasspath("annotation/layers/entities.json"),
                                                   AnnotationLayer.class);

      var tbPane = new JTabbedPane();

      var cView = new CorpusView("/data/corpora/hermes_data/ontonotes_ner",
                                 Corpus.open("/data/corpora/hermes_data/ontonotes_ner"),
                                 AvailableAnnotationLayers.values()
      );
      cView.setOnDocumentOpen((id, layer) -> {
         tbPane.addTab(id, new DocumentViewer(cView.getCorpus().getDocument(id), layer));
      });
      tbPane.addTab("Corpus", cView);

      var d2 = Document.fromJson(Resources.from("/home/ik/doc.json").readToString());
      //      var d2 = Document.create(
      //            "President Donald Trump is preparing to announce a second coronavirus task force solely focused on reopening the nation's economy, multiple sources told CNN.\n" +
      //                  "\n" +
      //                  "Senior aides such as Treasury Secretary Steven Mnuchin and National Economic Council Director Larry Kudlow have been solely focused on the issue of restarting a wounded American economy for weeks, along with a coterie of aides.\n" +
      //                  "But in recent days, inside and outside advisers have appealed to Trump to formally create a separate task force in order to streamline the process so it can focus primarily on reopening the economy. This task force would likely include senior staff from the Treasury Department, the National Economic Council, the Labor Department and the Department of Commerce.\n" +
      //                  "But it wouldn't only have administration officials involved. There has been outreach to figures such as Gary Cohn, Blackstone CEO Steve Schwarzman, Art Laffer and even major sports teams and well-known athletes.\n" +
      //                  "Disastrous jobs data sharpens Trump&#39;s dilemma over closed economy\n" +
      //                  "Disastrous jobs data sharpens Trump's dilemma over closed economy\n" +
      //                  "Other Trump allies have proposed naming a recovery \"czar\" from the private sector to oversee efforts to revive the consumer economy and address unemployment after coronavirus forced the closure of businesses across sectors.\n" +
      //                  "Influential conservatives have floated Laffer as the leader of the task force, and he has presented some ideas to senior White House aides on a plan to revive the economy. His plans have included a proposal to tax nonprofits, cut the pay of some public officials and offer a payroll tax holiday.");
      //      d2.annotate(Types.ENTITY, Types.PHRASE_CHUNK);
      var vbox2 = new DocumentViewer(d2, annotationLayer);
      tbPane.addTab("Document 2", vbox2);

      tbPane.getModel()
            .addChangeListener(c -> {
               //               if(tbPane.getTitleAt(tbPane.getSelectedIndex()).equals("Corpus")) {
               //                  lblAnnotationLayer.setText("");
               //               } else {
               //                  lblAnnotationLayer.setText(tabs.get(tbPane.getSelectedIndex() - 1).getAnnotationLayer().getName());
               //               }
            });
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
}//END OF AnnotationGUI
