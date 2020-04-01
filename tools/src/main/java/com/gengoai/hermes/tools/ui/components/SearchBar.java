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

import com.formdev.flatlaf.util.HSLColor;
import com.gengoai.hermes.HString;
import com.gengoai.string.Strings;
import com.gengoai.swing.ColorUtils;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.fluent.FluentJButton;
import com.gengoai.swing.fluent.FluentJTextField;
import com.gengoai.swing.fluent.FluentJToggleButton;
import com.gengoai.swing.fluent.HBox;
import com.gengoai.swing.listeners.FocusEventType;
import com.gengoai.swing.listeners.SwingListeners;
import lombok.NonNull;

import javax.swing.ButtonGroup;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import static com.gengoai.swing.components.Components.*;
import static javax.swing.BorderFactory.*;

public class SearchBar extends HBox {
   private final Vector<Consumer<List<HString>>> onSearchListeners = new Vector<>();
   private final Vector<Consumer<HString>> onSearchSelectionChange = new Vector<>();
   private final HBox pnlSearchPanel;
   private final FluentJTextField txtSearchText;
   private final HString document;
   private final Color backgroundColor;
   private final Color foregroundColor;
   private final FluentJButton btnSearch;
   private final FluentJButton btnPrevResult;
   private final FluentJButton btnNextResult;
   private final FluentJToggleButton btnString;
   private final FluentJToggleButton btnRegex;
   private final FluentJToggleButton btnTokenRegex;
   private final FluentJButton btnHide;
   private final Color clrTextBackground;
   private final Color clrTextError = new Color(116, 58, 58);
   private final Color clrTextForeground;
   private int searchResultIndex = 0;
   private List<HString> searchResultList = new ArrayList<>();

   public SearchBar(HString document) {
      super(2);
      this.document = document;
      this.backgroundColor = getBackground();
      this.foregroundColor = getForeground();

      //--------------------------------------------------------------------------------------------------------------
      //Define and Layout Controls
      //--------------------------------------------------------------------------------------------------------------
      minimumSize(0, 32)
            .compoundBorder(createMatteBorder(0, 0, 2, 0, backgroundColor.darker()),
                            createEmptyBorder(4, 12, 4, 4))
            .setVisible(false);
      pnlSearchPanel = hbox(4).emptyBorderWithMargin(4, 4, 4, 4);

      HSLColor hslColor = new HSLColor(pnlSearchPanel.getBackground());
      var clrBorder = ColorUtils.calculateBestFontColor(hslColor.getRGB()) == Color.WHITE
                      ? hslColor.adjustTone(50)
                      : hslColor.adjustShade(20);
      clrTextBackground = ColorUtils.calculateBestFontColor(hslColor.getRGB()) == Color.WHITE
                          ? hslColor.adjustTone(20)
                          : hslColor.adjustShade(10);
      var bdrComponentInActive = createCompoundBorder(createLineBorder(clrBorder, 2, true),
                                                      createEmptyBorder(4, 4, 4, 4));
      var bdrComponentActive = createCompoundBorder(createLineBorder(UIManager.getColor("activeCaption"), 2, true),
                                                    createEmptyBorder(4, 4, 4, 4));
      pnlSearchPanel.setBorder(bdrComponentInActive);
      pnlSearchPanel.setBackground(clrTextBackground);
      txtSearchText = new FluentJTextField().columns(30)
                                            .emptyBorder()
                                            .translucent()
                                            .fontStyle(Font.BOLD)
                                            .minFontSize(12);
      clrTextForeground = txtSearchText.getForeground();

      btnSearch = panelButton(FontAwesome.SEARCH, clrTextBackground, foregroundColor).setDisabled();
      btnPrevResult = panelButton(FontAwesome.ARROW_UP, backgroundColor, foregroundColor).setDisabled();
      btnNextResult = panelButton(FontAwesome.ARROW_DOWN, backgroundColor, foregroundColor).setDisabled();
      btnString = panelToggleButton(FontAwesome.RECEIPT, backgroundColor, foregroundColor)
            .tooltip("Perform a string search");
      btnRegex = panelToggleButton(FontAwesome.CODE, backgroundColor, foregroundColor)
            .tooltip("Perform a string based regular expression");
      btnTokenRegex = panelToggleButton(FontAwesome.AT, backgroundColor, foregroundColor)
            .tooltip("Perform a token based regular expression");
      btnHide = panelButton(FontAwesome.TIMES, backgroundColor, foregroundColor);
      onToggleButtonClick(btnString, null);
      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(btnRegex);
      buttonGroup.add(btnString);
      buttonGroup.add(btnTokenRegex);
      buttonGroup.setSelected(btnString.getModel(), true);

      add(pnlSearchPanel.add(txtSearchText).add(btnSearch).setResizeWithComponent(0))
            .add(btnPrevResult)
            .add(btnNextResult)
            .add(hbox(0, btnTokenRegex, btnRegex, btnString).background(backgroundColor)
                                                            .lineBorder(backgroundColor.darker(), 1, true))
            .add(btnHide)
            .setResizeWithComponent(0);
      //--------------------------------------------------------------------------------------------------------------

      //--------------------------------------------------------------------------------------------------------------
      //Define Listeners
      //--------------------------------------------------------------------------------------------------------------
      txtSearchText.onDocumentUpdate(($, e) -> updateSearch());
      btnSearch.actionListener(($, e) -> performSearch());
      btnNextResult.actionListener(($, e) -> {
         searchResultIndex++;
         fireSearchChangeEvent();
      });
      btnPrevResult.actionListener(($, e) -> {
         searchResultIndex--;
         fireSearchChangeEvent();
      });
      btnRegex.actionListener(this::onToggleButtonClick);
      btnTokenRegex.actionListener(this::onToggleButtonClick);
      btnString.actionListener(this::onToggleButtonClick);
      btnHide.actionListener(($, e) -> setVisible(false));
      onComponentShown(($, e) -> txtSearchText.requestFocus());
      txtSearchText.addFocusListener(SwingListeners.focusListener((type, event) -> {
         if(type == FocusEventType.FOCUS_GAINED) {
            pnlSearchPanel.setBorder(bdrComponentActive);
         } else {
            pnlSearchPanel.setBorder(bdrComponentInActive);
         }
      }));
      //--------------------------------------------------------------------------------------------------------------
   }

   public void clearSearch() {
      searchResultIndex = 0;
      searchResultList.clear();
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

   private SearchStrategy getSearchStrategy() {
      if(btnTokenRegex.isSelected()) {
         return SearchStrategy.TOKEN_REGEX_MATCH;
      } else if(btnRegex.isSelected()) {
         return SearchStrategy.REGEX_EXACT_MATCH;
      }
      return SearchStrategy.CASE_INSENSITIVE_EXACT_MATCH;
   }

   public String getSearchText() {
      return txtSearchText.getText();
   }

   public SearchBar onSearch(@NonNull Consumer<List<HString>> resultListener) {
      onSearchListeners.add(resultListener);
      return this;
   }

   public SearchBar onSearchSelectionChange(@NonNull Consumer<HString> resultListener) {
      onSearchSelectionChange.add(resultListener);
      return this;
   }

   private void onToggleButtonClick(FluentJToggleButton $, ActionEvent event) {
      var selectedBackgroundColor = getBackground().darker().darker();
      var selectedForegroundColor = ColorUtils.calculateBestFontColor(selectedBackgroundColor);
      for(FluentJToggleButton btn : new FluentJToggleButton[]{btnString, btnTokenRegex, btnRegex}) {
         if(btn == $) {
            btn.background(selectedBackgroundColor);
            btn.foreground(selectedForegroundColor);
            btn.opaque();
         } else {
            btn.foreground(foregroundColor);
            btn.translucent();
         }
         btn.repaint();
      }
      updateSearch();
   }

   public void performSearch() {
      searchResultList.clear();
      searchResultList.addAll(getSearchStrategy().findAll(txtSearchText.getText(), document));
      searchResultIndex = 0;
      fireSearchChangeEvent();
   }

   public SearchBar setSearchText(String text) {
      txtSearchText.setText(text);
      return this;
   }

   private void updateSearch() {
      Exception exception = getSearchStrategy().tryParse(txtSearchText.getText());
      if(exception != null) {
         btnSearch.setDisabled();
         txtSearchText.setForeground(Color.WHITE);
         pnlSearchPanel.setBackground(clrTextError);
         txtSearchText.setBackground(clrTextError);
         txtSearchText.setToolTipText(exception.getMessage());
      } else {
         pnlSearchPanel.setBackground(clrTextBackground);
         btnSearch.setEnabled(Strings.isNotNullOrBlank(txtSearchText.getText()));
         txtSearchText.setForeground(clrTextForeground);
         txtSearchText.setToolTipText(null);
      }
      pnlSearchPanel.repaint();
   }

}//END OF SearchBar
