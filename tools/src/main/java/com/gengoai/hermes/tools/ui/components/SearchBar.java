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

import com.gengoai.collection.Collect;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.hermes.extraction.RegexExtractor;
import com.gengoai.hermes.extraction.SearchExtractor;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.string.Strings;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.MangoButtonGroup;
import com.gengoai.swing.component.listener.FluentAction;
import com.gengoai.swing.component.view.MangoButtonedTextField;
import lombok.NonNull;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.Borders.createMatteBorderWithMargin;
import static com.gengoai.swing.Fonts.setFontStyle;
import static com.gengoai.swing.Rect.margin;
import static com.gengoai.swing.Rect.rect;
import static com.gengoai.swing.component.Components.*;
import static com.gengoai.swing.component.listener.SwingListeners.*;
import static javax.swing.Box.createRigidArea;

public class SearchBar extends JToolBar {
   private static final int TOKEN_REGEX = 0;
   private static final int STRING_REGEX = 1;
   private static final int STRING_MATCH = 2;
   private static final int BUTTON_HEIGHT = 16;
   private final Vector<Consumer<List<HString>>> onSearchListeners = new Vector<>();
   private final Vector<Consumer<HString>> onSearchSelectionChange = new Vector<>();
   private final MangoButtonedTextField pnlSearchPanel;
   private final HString document;
   private final JButton btnPrevResult;
   private final JButton btnNextResult;
   private final Color textColor;
   private final JCheckBox checkBoxFuzzyMatch;
   private final JCheckBox checkBoxMatchCase;
   private final MangoButtonGroup buttonGroup;
   private int searchResultIndex = 0;
   private List<HString> searchResultList = new ArrayList<>();
   private final FluentAction actionStringSearch = fluentAction("StringSearch", e -> {
      clearSearch();
      updateSearch();
   }).smallIcon(FontAwesome.RECEIPT.createIcon(BUTTON_HEIGHT))
     .shortDescription("Perform a string search");
   private final FluentAction actionRegexSearch = fluentAction("RegexSearch", e -> {
      clearSearch();
      updateSearch();
   }).smallIcon(FontAwesome.CODE.createIcon(BUTTON_HEIGHT))
     .shortDescription("Perform a string based regular expression");
   private final FluentAction actionTokenRegexSearch = fluentAction("TokenRegexSearch", e -> {
      clearSearch();
      updateSearch();
   }).smallIcon(FontAwesome.AT.createIcon(BUTTON_HEIGHT))
     .shortDescription("Perform a token based regular expression");
   private final FluentAction actionNextResult = fluentAction("NextResult", e -> {
      searchResultIndex++;
      fireSearchChangeEvent();
   }).smallIcon(FontAwesome.ARROW_DOWN.createIcon(BUTTON_HEIGHT));
   private final FluentAction actionPreviousResult = fluentAction("PreviousResult", e -> {
      searchResultIndex--;
      fireSearchChangeEvent();
   }).smallIcon(FontAwesome.ARROW_UP.createIcon(BUTTON_HEIGHT));

   public SearchBar(HString document) {
      this.document = document;
      final Color backgroundColor = getBackground();
      setMinimumSize(dim(0, 28));
      setBorder(createMatteBorderWithMargin(margin(4, 12, 4, 4), rect(0, 0, 1, 0), backgroundColor.darker()));

      pnlSearchPanel = with(new MangoButtonedTextField(30, FontAwesome.SEARCH, FontAwesome.BACKSPACE), $ -> {
         $.getRoot().setBackground(new Color(116, 58, 58));
         $.getRoot().setOpaque(false);
         setFontStyle($, Font.BOLD);
         $.getRightButton().setVisible(false);
         $.addActionListener(a -> performSearch());
         $.addDocumentListener(documentListener((type, e) -> {
            $.getRightButton().setVisible(Strings.isNotNullOrBlank($.getText()));
            updateSearch();
         }));
         $.addLeftButtonActionListener(e -> performSearch());
         $.addRightButtonActionListener(e -> {
            $.setText("");
            clearSearch();
         });
      });
      textColor = pnlSearchPanel.getForeground();

      buttonGroup = with(buttonGroup(toggleButtonSmallIconWithoutText(actionTokenRegexSearch),
                                     toggleButtonSmallIconWithoutText(actionRegexSearch),
                                     toggleButtonSmallIconWithoutText(actionStringSearch)), $ -> {
         $.selectLast();
         $.forEach(btn -> {
            btn.setBorderPainted(true);
            btn.setBackground(backgroundColor);
            btn.setPreferredSize(dim(BUTTON_HEIGHT + 4, BUTTON_HEIGHT + 4));
         });
      });

      var btnHide = FontAwesome.TIMES.createButton(BUTTON_HEIGHT);
      btnHide.addActionListener(a -> setVisible(false));

      add(pnlSearchPanel.getRoot());
      add(new JSeparator());
      add(btnPrevResult = with(buttonSmallIconWithoutText(actionPreviousResult), $ -> {
         $.setBorderPainted(false);
         $.setEnabled(false);
      }));
      add(btnNextResult = with(buttonSmallIconWithoutText(actionNextResult), $ -> {
         $.setBorderPainted(false);
         $.setEnabled(false);
      }));
      add(new JSeparator());
      add(createRigidArea(dim(10, 0)));
      buttonGroup.forEach(this::add);
      add(new JSeparator());
      add(createRigidArea(dim(20, 0)));
      add(checkBoxMatchCase = with(new JCheckBox("Match Case"),
                                   $ -> $.setToolTipText("Case sensitive matching.")));
      add(checkBoxFuzzyMatch = with(new JCheckBox("Match Span"),
                                    $ -> $.setToolTipText("Search must match a full span of words.")));
      add(createRigidArea(dim(20, 0)));
      add(btnHide);
      addComponentListener(componentShown(e -> pnlSearchPanel.requestFocus()));
   }

   public SearchBar addSearchNavigationListener(@NonNull Consumer<HString> resultListener) {
      onSearchSelectionChange.add(resultListener);
      return this;
   }

   public SearchBar addSearchResultListListener(@NonNull Consumer<List<HString>> resultListener) {
      onSearchListeners.add(resultListener);
      return this;
   }

   public void clearSearch() {
      searchResultIndex = 0;
      searchResultList.clear();
      btnNextResult.setEnabled(false);
      btnPrevResult.setEnabled(false);
      fireSearchChangeEvent();
   }

   private void fireSearchChangeEvent() {
      onSearchListeners.forEach(l -> l.accept(searchResultList));
      if(searchResultList.isEmpty()) {
         onSearchSelectionChange.forEach(l -> l.accept(null));
      } else {
         onSearchSelectionChange.forEach(l -> l.accept(searchResultList.get(searchResultIndex)));
      }
      btnPrevResult.setEnabled(searchResultIndex - 1 >= 0);
      btnNextResult.setEnabled(searchResultIndex + 1 < searchResultList.size());
   }

   private Extractor getSearchStrategy() throws Exception {
      switch(buttonGroup.getSelectedIndex()) {
         case TOKEN_REGEX:
            return TokenRegex.compile(getSearchText());
         case STRING_REGEX:
            return new RegexExtractor(getSearchText(),
                                      checkBoxMatchCase.isSelected(),
                                      checkBoxFuzzyMatch.isSelected());
         default:
            return new SearchExtractor(getSearchText(),
                                       checkBoxMatchCase.isSelected(),
                                       checkBoxFuzzyMatch.isSelected());
      }
   }

   public String getSearchText() {
      return pnlSearchPanel.getText();
   }

   public void performSearch() {
      try {
         searchResultList.clear();
         searchResultList.addAll(Collect.asCollection(getSearchStrategy().extract(document)));
         searchResultIndex = 0;
         if(searchResultList.size() > 1) {
            btnNextResult.setEnabled(true);
         }
         fireSearchChangeEvent();
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
   }

   public SearchBar setSearchText(String text) {
      pnlSearchPanel.setText(text);
      return this;
   }

   private void updateSearch() {

      checkBoxMatchCase.setEnabled(buttonGroup.getSelectedIndex() != TOKEN_REGEX);
      checkBoxFuzzyMatch.setEnabled(buttonGroup.getSelectedIndex() != TOKEN_REGEX);

      if(Strings.isNullOrBlank(pnlSearchPanel.getText())) {
         pnlSearchPanel.getLeftButton().setEnabled(false);
         pnlSearchPanel.setToolTipText(null);
         pnlSearchPanel.getRoot().setOpaque(false);
         pnlSearchPanel.setForeground(textColor);
      } else {
         try {
            getSearchStrategy();
            pnlSearchPanel.getLeftButton().setEnabled(true);
            pnlSearchPanel.setToolTipText(null);
            pnlSearchPanel.getRoot().setOpaque(false);
            pnlSearchPanel.setForeground(textColor);
         } catch(Exception e) {
            pnlSearchPanel.getRoot().setOpaque(true);
            pnlSearchPanel.getLeftButton().setEnabled(false);
            pnlSearchPanel.setToolTipText(e.getMessage());
            pnlSearchPanel.setForeground(Color.WHITE);
         }
      }
      invalidate();
      repaint();
   }

}//END OF SearchBar
