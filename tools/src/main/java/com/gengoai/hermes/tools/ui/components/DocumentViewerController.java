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

import com.gengoai.Tag;
import com.gengoai.collection.Arrays2;
import com.gengoai.conversion.Cast;
import com.gengoai.graph.io.GraphViz;
import com.gengoai.hermes.*;
import com.gengoai.hermes.extraction.SearchExtractor;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import com.gengoai.swing.Colors;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.SelectionChangeEvent;
import com.gengoai.swing.component.listener.FluentAction;

import javax.swing.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gengoai.hermes.tools.ui.components.DocumentViewer.*;
import static com.gengoai.swing.component.listener.SwingListeners.fluentAction;

final class DocumentViewerController {
   private static final int SMALL_ICON_SIZE = 16;
   private static final int LARGE_ICON_SIZE = 32;
   private final DocumentViewer _viewer;
   public final FluentAction DELETE_ANNOTATION_IN_TABLE = fluentAction("Delete",
                                                                       a -> deleteSelectedAnnotationInTable())
         .smallIcon(FontAwesome.TRASH.createIcon(SMALL_ICON_SIZE))
         .largeIcon(FontAwesome.TRASH.createIcon(LARGE_ICON_SIZE))
         .shortDescription("Delete annotations.");
   public final FluentAction DELETE_ANNOTATION = fluentAction("Delete Annotation",
                                                              a -> deleteSelectedAnnotationsInDocumentView())
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))
         .smallIcon(FontAwesome.TRASH.createIcon(SMALL_ICON_SIZE))
         .largeIcon(FontAwesome.TRASH.createIcon(LARGE_ICON_SIZE))
         .shortDescription("Delete annotation(s) from the currently selected text.");
   public final FluentAction CONCORDANCE = fluentAction("Concordance",
                                                        a -> createConcordanceSelectedText())
         .mnemonic(KeyEvent.VK_C)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0))
         .smallIcon(FontAwesome.LIST.createIcon(SMALL_ICON_SIZE))
         .largeIcon(FontAwesome.TRASH.createIcon(LARGE_ICON_SIZE))
         .shortDescription(
               "Create a concordance view around the currently selected text.");
   public final FluentAction INSPECT = fluentAction("Inspect",
                                                    a -> inspectSelectedText())
         .mnemonic(KeyEvent.VK_I)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0))
         .largeIcon(FontAwesome.INFO.createIcon(LARGE_ICON_SIZE))
         .smallIcon(FontAwesome.INFO.createIcon(SMALL_ICON_SIZE));

   public final FluentAction SEARCH_WITH = fluentAction("Search With...",
                                                        a -> onActionSearchWith())
         .mnemonic(KeyEvent.VK_S)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0))
         .largeIcon(FontAwesome.SEARCH.createIcon(LARGE_ICON_SIZE))
         .smallIcon(FontAwesome.SEARCH.createIcon(SMALL_ICON_SIZE));
   public final FluentAction TOGGLE_SEARCHBAR = fluentAction("ToggleSearchBar",
                                                             a -> view().searchBar.setVisible(!view().searchBar
                                                                   .isVisible()))
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                             KeyEvent.CTRL_DOWN_MASK))
         .shortDescription("View or Hide the Search Bar.")
         .largeIcon(FontAwesome.SEARCH.createIcon(LARGE_ICON_SIZE))
         .smallIcon(FontAwesome.SEARCH.createIcon(SMALL_ICON_SIZE));
   public final FluentAction FOCUS_ON_TAGVIEW = fluentAction("FocusOnTagView",
                                                             a -> view().tagView.focusOnFilter())
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                                             0));
   public final FluentAction FOCUS_ON_TAGVIEW_AND_CLEAR = fluentAction("FocusOnTagView",
                                                                       a -> {
                                                                          view().tagView.setFilterText("");
                                                                          view().tagView.focusOnFilter();
                                                                       })
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                                             KeyEvent.CTRL_DOWN_MASK));
   public final FluentAction FOCUS_ON_DOCUMENT_VIEW = fluentAction("FocusOnDocumentView",
                                                                   a -> view().documentView.focusOnEditor());
   public final FluentAction SELECT_ANNOTATION_TABLE = fluentAction("SelectAnnotationTable",
                                                                    a -> view().tbPaneTools
                                                                          .setSelectedIndex(ANNOTATION_TABLE_INDEX))
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
                                             KeyEvent.CTRL_DOWN_MASK));
   public final FluentAction SELECT_SEARCH_RESULTS_TABLE = fluentAction("SelectSearchResultsTable",
                                                                        a -> view().tbPaneTools
                                                                              .setSelectedIndex(SEARCH_RESULTS_INDEX))
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
                                             KeyEvent.CTRL_DOWN_MASK));
   public final FluentAction SELECT_CONCORDANCE_TABLE = fluentAction("SelectConcordanceTable",
                                                                     a -> view().tbPaneTools
                                                                           .setSelectedIndex(CONCORDANCE_TABLE_INDEX))
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,
                                             KeyEvent.CTRL_DOWN_MASK));
   public final FluentAction SELECT_SENTENCES_TABLE = fluentAction("SelectSentencesTable",
                                                                   a -> view().tbPaneTools
                                                                         .setSelectedIndex(SENTENCE_LIST_INDEX))
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,
                                             KeyEvent.CTRL_DOWN_MASK));
   public final FluentAction SELECT_INSPECTION_WINDOW = fluentAction("SelectInspectionWindow",
                                                                     a -> view().tbPaneTools
                                                                           .setSelectedIndex(INSPECT_SELECTION_INDEX))
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5,
                                             KeyEvent.CTRL_DOWN_MASK));

   DocumentViewerController(DocumentViewer viewer) {
      this._viewer = viewer;

      InputMap inputMap = viewer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      ActionMap actionMap = viewer.getActionMap();

      inputMap.put(SELECT_ANNOTATION_TABLE.getAccelerator(), SELECT_ANNOTATION_TABLE.getName());
      actionMap.put(SELECT_ANNOTATION_TABLE.getName(), SELECT_ANNOTATION_TABLE);

      inputMap.put(SELECT_SEARCH_RESULTS_TABLE.getAccelerator(), SELECT_SEARCH_RESULTS_TABLE.getName());
      actionMap.put(SELECT_SEARCH_RESULTS_TABLE.getName(), SELECT_SEARCH_RESULTS_TABLE);

      inputMap.put(SELECT_CONCORDANCE_TABLE.getAccelerator(), SELECT_CONCORDANCE_TABLE.getName());
      actionMap.put(SELECT_CONCORDANCE_TABLE.getName(), SELECT_CONCORDANCE_TABLE);

      inputMap.put(SELECT_SENTENCES_TABLE.getAccelerator(), SELECT_SENTENCES_TABLE.getName());
      actionMap.put(SELECT_SENTENCES_TABLE.getName(), SELECT_SENTENCES_TABLE);

      inputMap.put(SELECT_INSPECTION_WINDOW.getAccelerator(), SELECT_INSPECTION_WINDOW.getName());
      actionMap.put(SELECT_INSPECTION_WINDOW.getName(), SELECT_INSPECTION_WINDOW);

      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK), FOCUS_ON_TAGVIEW.getName());
      actionMap.put(FOCUS_ON_TAGVIEW.getName(), FOCUS_ON_TAGVIEW);

      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), FOCUS_ON_DOCUMENT_VIEW.getName());
      actionMap.put(FOCUS_ON_DOCUMENT_VIEW.getName(), FOCUS_ON_DOCUMENT_VIEW);

      DELETE_ANNOTATION_IN_TABLE.setEnabled(false);
   }

   private void createConcordanceSelectedText() {
      _viewer.tblConcordance.clear();
      String text = _viewer.documentView.getSelectedText();
      if(Strings.isNotNullOrBlank(text)) {
         AtomicInteger maxChars = new AtomicInteger();
         new SearchExtractor(text, false, false)
               .extract(_viewer.document)
               .forEach(h -> {
                  CharSequence lContext = h.leftContext(4);
                  maxChars.set(Math.max(maxChars.get(), lContext.length()));
                  _viewer.tblConcordance.addRow(lContext, h, h.rightContext(4), h.start());
               });
         _viewer.tblConcordance.resizeColumnWidth(20);
         _viewer.tbPaneTools.setSelectedIndex(CONCORDANCE_TABLE_INDEX);
      }
   }

   private void deleteSelectedAnnotationInTable() {
      MangoTable table = _viewer.tblAnnotations;
      DocumentViewerModel model = _viewer.getModel();
      for(Annotation annotation : IntStream.of(table.getSelectedRows())
                                           .map(table::convertRowIndexToModel)
                                           .mapToObj(model::getAnnotationForRow).collect(Collectors.toList())) {
         model.remove(annotation);
         _viewer.refreshStyle(annotation.start(), annotation.end());
         _viewer.markDirty();
      }
   }

   protected void deleteSelectedAnnotationsInDocumentView() {
      HStringViewer view = _viewer.documentView;
      if(view.hasSelection()) {
         int start = view.getSelectionStart();
         int end = view.getSelectionEnd();
         final java.util.List<Annotation> toDelete = _viewer.getModel().getEnclosingAnnotations(start, end);
         if(toDelete.isEmpty()) {
            return;
         }
         boolean deleted = false;
         if(toDelete.size() == 1) {
            deleted = true;
            _viewer.getModel().removeAll(toDelete);
         } else {
            java.util.List<Integer> deleteIndices = showDeleteAnnotationSelector(toDelete);
            if(deleteIndices.size() > 0) {
               deleted = true;
               deleteIndices.forEach(_viewer.getModel()::removeRow);
            }
         }
         if(deleted) {
            _viewer.markDirty();
            _viewer.refreshStyle(start, end);
         }
      }
   }

   private void htmlTableBegin(StringBuilder b, Color borderColor, String sectionName, Color sectionBackground) {
      b.append("<table width='100%' cellpadding='1' bgcolor='")
       .append(Colors.toHexString(borderColor))
       .append("'>")
       .append("<table width='100%' border=0 cellpadding='0' cellspacing='0'><tr bgcolor='")
       .append(Colors.toHexString(sectionBackground))
       .append("' style='font-weight:bold; text-align: center; color:")
       .append(Colors.toHexString(Colors.calculateBestFontColor(sectionBackground)))
       .append("'><td colspan=2 style='margin: 5 5 5 5'>")
       .append(sectionName)
       .append("</td></tr>");
   }

   private void htmlTableEnd(StringBuilder b) {
      b.append("</table></table>");
   }

   private void htmlTableRow(StringBuilder html, Color background, int nc, String... columns) {
      htmlTableRow(html, background, Colors.calculateBestFontColor(background), nc, columns);
   }

   private void htmlTableRow(StringBuilder html, Color background, Color foreground, int nc, String... columns) {
      html.append("<tr bgcolor='")
          .append(Colors.toHexString(background))
          .append("' style='font-weight:bold; color:")
          .append(Colors.toHexString(foreground))
          .append("'>");
      int span = nc - columns.length;
      for(String column : columns) {
         html.append("<td style='margin: 5 5 5 5'");
         if(span > 0) {
            html.append(" colspan=").append(span + 1);
            span = 0;
         }
         html.append(" >").append(column).append("</td>");
      }
      html.append("</tr>");
   }

   private void inspectSelectedText() {
      if(_viewer.documentView.hasSelection()) {
         final Document document = _viewer.document;
         final HStringViewer documentView = _viewer.documentView;

         HString selection = document.substring(documentView.getSelectionStart(), documentView.getSelectionEnd());
         var background = documentView.getBackground();
         var foreground = documentView.getForeground();
         Color borderColor;
         Color sectionColor;
         if(Colors.calculateBestFontColor(background) == Color.WHITE) {
            borderColor = background.brighter();
            sectionColor = background.darker();
         } else {
            borderColor = background.darker();
            sectionColor = background.darker();
         }
         _viewer.ttlInspectionWindow.heading(selection.toString());
         StringBuilder html = new StringBuilder("<html>");
         java.util.List<Annotation> styledSpans = documentView.getAnnotations(_viewer.annotationType,
                                                                              documentView.getSelectionStart(),
                                                                              documentView.getSelectionEnd());

         if(styledSpans.size() > 0) {
            htmlTableBegin(html, borderColor, _viewer.annotationType.getTagAttribute().label(), sectionColor);
            for(Annotation span : styledSpans) {
               var c = _viewer.getAnnotationLayer().getColor(span.getTag());
               htmlTableRow(html, c, 2,
                            span.toString() + " (" + span.getTag().label() + ")",
                            span.hasAttribute(Types.CONFIDENCE)
                            ? span.attribute(Types.CONFIDENCE).toString()
                            : ""
                           );
            }
            htmlTableEnd(html);
         }
         html.append("<br/>");

         htmlTableBegin(html, borderColor, "TOKEN", sectionColor);
         int row = 0;
         for(Annotation token : selection.tokens()) {
            var bg = row % 2 == 0
                     ? background
                     : sectionColor;
            htmlTableRow(html, bg, 2, token.toString() + " (" + token.pos().label() + ")");
            Set<BasicCategories> categoriesSet = token.categories();
            if(categoriesSet.size() > 0) {
               htmlTableRow(html,
                            bg,
                            foreground,
                            2,
                            "Categories<hr/><font size=8pt>" + Strings.join(categoriesSet, " ") + "</font>");
            } else {
               htmlTableRow(html,
                            bg,
                            foreground,
                            2, "");
            }
            row++;
         }
         htmlTableEnd(html);

         for(AnnotationType type : Arrays2.arrayOf(Types.PHRASE_CHUNK, Types.ENTITY)) {
            if(!_viewer.annotationType.equals(type)) {
               html.append("<br/>");
               htmlTableBegin(html, borderColor, type.name().replace('_', ' '), sectionColor);
               row = 0;
               for(Annotation annotation : selection.annotations(type)) {
                  var bg = row % 2 == 0
                           ? background
                           : sectionColor;
                  htmlTableRow(html, bg, 2,
                               annotation.toString() + " (" + annotation.getTag().label() + ")",
                               annotation.hasAttribute(Types.CONFIDENCE)
                               ? annotation.attribute(Types.CONFIDENCE).toString()
                               : ""
                              );
                  row++;
               }
               htmlTableEnd(html);
            }
         }

         if(document.isCompleted(Types.DEPENDENCY)) {
            html.append("<br/>");
            RelationGraph rg = selection.dependencyGraph();
            Resource tmp = Resources.temporaryFile();
            tmp.deleteOnExit();
            try {
               rg.render(tmp, GraphViz.Format.PNG);
               htmlTableBegin(html, borderColor, "DEPENDENCY STRUCTURE", sectionColor);
               htmlTableRow(html, Color.WHITE, 2, "<center><img src=" + tmp.descriptor() + "/></center>");
               htmlTableEnd(html);
            } catch(IOException e) {
               e.printStackTrace();
            }
         }

         html.append("</html>");
         _viewer.txtInspectionWindow.setText(html.toString());
         _viewer.tbPaneTools.setSelectedIndex(INSPECT_SELECTION_INDEX);
      }
   }

   private void onActionSearchWith() {
      _viewer.searchBar.setVisible(true);
      _viewer.searchBar.setSearchText(_viewer.documentView.getSelectedText());
      _viewer.searchBar.performSearch();
      _viewer.tbPaneTools.setSelectedIndex(SEARCH_RESULTS_INDEX);
   }

   protected void onDocumentViewSelectionChange(SelectionChangeEvent e) {
      if(e.getNewSelection() != null) {
         int start = e.getNewSelection().start();
         int end = e.getNewSelection().end();

         //                  Point sp = documentView.modelToView(start);
         //                  Point loc = getParent().getLocation();
         //                  SwingUtilities.convertPointToScreen(loc, getParent());
         //                  dlgFloatingWindow.setLocation(new Point(loc.x + sp.x, loc.y + sp.y));
         //                  dlgFloatingWindow.setSelection(start, end, documentView.getBestMatchingStyledSpan(start, end));
         //                  //            dlgFloatingWindow.setMinimumSize(new Dimension(100, 28));
         //                  dlgFloatingWindow.setVisible(true);
         _viewer.getModel().getOverlappingAnnotations(start, end)
                .stream()
                .findFirst()
                .ifPresent(a -> {
                   int row = _viewer.tblAnnotations.convertRowIndexToView(_viewer.getModel().indexOf(a));
                   _viewer.tblAnnotations.getSelectionModel().setSelectionInterval(row, row);
                   Rectangle cellRect = _viewer.tblAnnotations.getCellRect(row, 0, true);
                   _viewer.tblAnnotations.scrollRectToVisible(cellRect);
                });
      } else {
         //                  dlgFloatingWindow.setVisible(false);
      }
   }

   private void onSearchNavigation(HString h) {
      if(h != null) {
         _viewer.documentView.setSelectionRange(h.start(), h.end());
      }
   }

   private void onSearchResultList(java.util.List<HString> list) {
      _viewer.tblSearchResults.clear();
      for(HString result : list) {
         _viewer.tblSearchResults.addRow(result.start(), result.end(), result);
      }
      if(list.size() > 0) {
         _viewer.tbPaneTools.setSelectedIndex(SEARCH_RESULTS_INDEX);
      }
   }

   private void performAddAnnotation(Object o) {
      if(_viewer.documentView.hasSelection()) {
         _viewer.getModel().createAnnotation(_viewer.documentView.getSelectionStart(),
                                             _viewer.documentView.getSelectionEnd(),
                                             Cast.<Tag>as(o));
         _viewer.markDirty();
      }
   }

   public void postInit() {
      _viewer.searchBar.addSearchResultListListener(this::onSearchResultList);
      _viewer.searchBar.addSearchNavigationListener(this::onSearchNavigation);
      _viewer.documentView.addSelectionChangeListener(this::onDocumentViewSelectionChange);
      _viewer.tagView.addSelectedItemListener(this::performAddAnnotation);
   }

   private java.util.List<Integer> showDeleteAnnotationSelector(java.util.List<Annotation> annotationList) {
      JCheckBox[] checkBoxes = new JCheckBox[annotationList.size()];
      for(int i = 0; i < checkBoxes.length; i++) {
         checkBoxes[i] = new JCheckBox(_viewer.getModel()
                                              .getStyleForAnnotation(annotationList.get(i)) + " (" + annotationList.get(
               i) + ")");
         checkBoxes[i].setSelected(true);
      }
      if(JOptionPane.showOptionDialog(_viewer,
                                      checkBoxes,
                                      "Select the annotations to delete",
                                      JOptionPane.OK_CANCEL_OPTION,
                                      JOptionPane.QUESTION_MESSAGE,
                                      null,
                                      new String[]{"Delete", "Cancel"},
                                      "Delete") == JOptionPane.OK_OPTION) {
         ArrayList<Integer> toDelete = new ArrayList<>();
         for(int i = 0; i < checkBoxes.length; i++) {
            if(checkBoxes[i].isSelected()) {
               toDelete.add(i);
            }
         }
         return toDelete;
      }
      return Collections.emptyList();
   }

   private DocumentViewer view() {
      return _viewer;
   }

}//END OF DocumentViewerActions
