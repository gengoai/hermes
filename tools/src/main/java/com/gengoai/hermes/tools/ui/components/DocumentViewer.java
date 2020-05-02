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
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.*;
import com.gengoai.string.Strings;
import com.gengoai.swing.Colors;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.Menus;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.MangoTitlePane;
import com.gengoai.swing.component.listener.MenuListeners;
import com.gengoai.swing.component.listener.SwingListeners;
import com.gengoai.swing.component.renderer.CustomTableCellRender;
import com.gengoai.swing.component.renderer.TableCellRenderers;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.gengoai.function.Functional.with;
import static com.gengoai.hermes.tools.ui.components.DocumentViewerModel.*;
import static com.gengoai.swing.Menus.menuItem;
import static com.gengoai.swing.Menus.popupMenu;
import static com.gengoai.swing.component.Components.*;
import static com.gengoai.tuple.Tuples.$;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;

public class DocumentViewer extends JPanel {
   public static final int ANNOTATION_TABLE_INDEX = 1;
   public static final int CONCORDANCE_TABLE_INDEX = 3;
   public static final int DOC_ATTRIBUTES_TABLE_INDEX = 0;
   public static final int SEARCH_RESULTS_INDEX = 2;
   public static final int SENTENCE_LIST_INDEX = 4;
   final Document document;
   final HStringViewer documentView;
   final JTabbedPane tbPaneTools;
   final TagTreeView tagView;
   final SearchBar searchBar;
   final MangoTable tblConcordance;
   final MangoTable tblAnnotations;
   final MangoTable tblSentences;
   final MangoTable tblSearchResults;
   final MangoTable tblDocumentAttributes;
   final AnnotationType annotationType;
   final AnnotationFloatWindow dlgFloatingWindow;
   final DocumentViewerController controller;
   private final AtomicBoolean hasChanged = new AtomicBoolean(false);
   @Getter
   private DocumentViewerModel model;
   @Getter
   private AnnotationLayer annotationLayer;
   private int searchResultIndex = 0;

   public DocumentViewer(@NonNull Document document, @NonNull AnnotationLayer annotationLayer) {
      setLayout(new BorderLayout());
      this.controller = new DocumentViewerController(this);
      this.annotationLayer = annotationLayer;
      this.document = document;
      this.annotationType = annotationLayer.getAnnotationType();
      this.model = new DocumentViewerModel(document, annotationLayer);
      this.tagView = new TagTreeView(annotationLayer);
      this.tblSearchResults = initSearchResultView();
      this.documentView = initDocumentViewer();
      this.tblConcordance = initConcordanceView();
      this.tblAnnotations = initAnnotationTable();
      this.tblSentences = initSentenceListView();
      this.tblDocumentAttributes = initDocumentAttributes();
      this.searchBar = new SearchBar(document);

      this.tbPaneTools = with(new JTabbedPane(), $ -> {
         $.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
         $.setTabPlacement(JTabbedPane.BOTTOM);

         $.addTab("Document Attributes", new JScrollPane(tblDocumentAttributes));
         $.setToolTipTextAt(DOC_ATTRIBUTES_TABLE_INDEX, "Ctrl+" + (DOC_ATTRIBUTES_TABLE_INDEX + 1));

         $.addTab("Annotation Table", new JScrollPane(tblAnnotations));
         $.setToolTipTextAt(ANNOTATION_TABLE_INDEX, "Ctrl+" + (ANNOTATION_TABLE_INDEX + 1));

         $.addTab("Search Results", new JScrollPane(tblSearchResults));
         $.setToolTipTextAt(SEARCH_RESULTS_INDEX, "Ctrl+" + (SEARCH_RESULTS_INDEX + 1));

         $.addTab("Concordance", new JScrollPane(tblConcordance));
         $.setToolTipTextAt(CONCORDANCE_TABLE_INDEX, "Ctrl+" + (CONCORDANCE_TABLE_INDEX + 1));

         $.addTab("Sentence List", new JScrollPane(tblSentences));
         $.setToolTipTextAt(SENTENCE_LIST_INDEX, "Ctrl+" + (SENTENCE_LIST_INDEX + 1));
      });
      var splitPane = with(new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentView, tbPaneTools), $ -> {
         $.setDividerLocation(400);
         $.setDividerSize(5);
         $.setResizeWeight(1);
      });
      var ttlTagTitlePane = new MangoTitlePane("Tag Search", false, tagView);
      ttlTagTitlePane.setPreferredSize(dim(300, 0));
      add(splitPaneHorizontal(0, panel($(NORTH, searchBar), $(CENTER, splitPane)), ttlTagTitlePane), CENTER);
      searchBar.setVisible(false);
      documentView.scrollToTopLeft();
      dlgFloatingWindow = new AnnotationFloatWindow(this);
      dlgFloatingWindow.setAnnotationLayer(annotationLayer);
      controller.postInit();
   }

   private MangoTable initAnnotationTable() {
      return with(new MangoTable(model), $ -> {
         Fonts.setFontSize($, 16f);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(true);
         $.setRowHeightPadding(3);
         $.setShowGrid(true);
         $.setReorderingAllowed(false);
         $.withColumn(START_INDEX, c -> {
            c.setPreferredWidth(75);
            c.setMaxWidth(75);
            c.setResizable(false);
         });
         $.withColumn(END_INDEX, c -> {
            c.setPreferredWidth(75);
            c.setMaxWidth(75);
            c.setResizable(false);
         });
         $.withColumn(TYPE_INDEX, c -> {
            c.setPreferredWidth(150);
            c.setMaxWidth(150);
            c.setCellEditor(new DefaultCellEditor(new JComboBox<>(annotationLayer.getValidTagsArray())));
            c.setCellRenderer(new AnnotationTableCellRender());
            c.setPreferredWidth(350);
         });
         $.withColumn(CONFIDENCE_INDEX, c -> {
            c.setPreferredWidth(100);
            c.setMaxWidth(100);
            c.setResizable(false);
         });
         $.getSelectionModel().addListSelectionListener(l -> {
            if(l.getFirstIndex() < model.size()) {
               int row = $.convertRowIndexToView(l.getFirstIndex());
               Rectangle cellRect = $.getCellRect(row, 0, true);
               $.scrollRectToVisible(cellRect);
            }
            controller.DELETE_ANNOTATION_IN_TABLE.setEnabled($.getSelectedRowCount() > 0);
         });
         $.addMouseListener(SwingListeners.mouseDoubleClicked(e -> {
            if($.getSelectedRow() >= 0) {
               HString h = Cast.as($.getValueAt($.getSelectedRow(), ANNOTATION_INDEX));
               documentView.setSelectionRange(h.start(), h.end());
            }
         }));
         $.addKeyListener(SwingListeners.keyPressed(e -> {
            if(e.getKeyCode() == KeyEvent.VK_DELETE) {
               int r = $.getSelectedRow();
               if(r >= 0) {
                  controller.DELETE_ANNOTATION_IN_TABLE.actionPerformed(null);
                  if(r < $.getModel().getRowCount()) {
                     $.getSelectionModel().setSelectionInterval(r, r);
                  }
               }
            }
         }));
         $.getModel().addTableModelListener(l -> {
            if(l.getType() == TableModelEvent.INSERT) {
               Integer start = Cast.as(model.getValueAt(l.getFirstRow(), START_INDEX));
               Integer end = Cast.as(model.getValueAt(l.getFirstRow(), END_INDEX));
               refreshStyle(start, end);
               documentView.setSelectionRange(documentView.getSelectionStart(), documentView.getSelectionEnd());
            }
         });
         $.setComponentPopupMenu(popupMenu(menuItem(controller.DELETE_ANNOTATION_IN_TABLE)));
      });
   }

   private MangoTable initConcordanceView() {
      MangoTable table = with(new MangoTable("Left Context", "Search String", "Right Context", ""), $ -> {
         Fonts.setFontSize($, 16f);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(false);
         $.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         $.setRowHeightPadding(3);
         $.setShowGrid(false);
         $.setReorderingAllowed(false);
         $.withColumn(0, c -> {
            c.setCellRenderer(TableCellRenderers.RIGHT_ALIGN);
            c.setResizable(false);
         });
         $.withColumn(1, c -> {
            c.setCellRenderer(new CustomTableCellRender().alignment(SwingConstants.CENTER)
                                                         .foreground(
                                                               Colors.isDark(getForeground())
                                                               ? getForeground().darker()
                                                               : getForeground().brighter())
                                                         .fontStyle(Font.BOLD));
            c.setResizable(false);
         });
         $.addMouseListener(SwingListeners.mouseClicked(e -> {
            if(e.getClickCount() == 2 && $.getSelectedRow() >= 0) {
               Integer start = Cast.as($.getModel().getValueAt($.convertRowIndexToModel($.getSelectedRow()), 3));
               int length = $.getValueAt($.getSelectedRow(), 1).toString().length();
               documentView.setSelectionRange(start, start + length);
            }
         }));
      });
      TableColumnModel tcm = table.getColumnModel();
      tcm.removeColumn(tcm.getColumn(3));
      table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      return table;
   }

   private MangoTable initDocumentAttributes() {
      MangoTable tbl = with(new MangoTable("Attribute", "Value"), $ -> {
         Fonts.setFontSize($, 16f);
         $.setRowHeight($.getRowHeight() + 3);
         $.setShowGrid(true);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(true);
         $.getTableHeader().setReorderingAllowed(false);
         $.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      });
      document.attributeMap().forEach((a, v) -> tbl.addRow(a.name(), v));
      return tbl;
   }

   private HStringViewer initDocumentViewer() {
      var view = with(new HStringViewer(document), $ -> {
         $.setAllowZoom(true);
         $.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
         $.setSelectionColor(Color.YELLOW);
         $.setSelectedTextColor(Color.BLACK);
         $.addStylesFrom(annotationLayer);
         $.highlightAnnotations(model);
         $.setAlwaysHighlight(true);
      });
      InputMap inputMap = view.getEditorInputMap();
      inputMap.put(controller.TOGGLE_SEARCHBAR.getAccelerator(), controller.TOGGLE_SEARCHBAR);
      inputMap.put(controller.FOCUS_ON_TAGVIEW.getAccelerator(), controller.FOCUS_ON_TAGVIEW);
      inputMap.put(controller.DELETE_ANNOTATION.getAccelerator(), controller.DELETE_ANNOTATION);
      inputMap.put(controller.FOCUS_ON_TAGVIEW_AND_CLEAR.getAccelerator(), controller.FOCUS_ON_TAGVIEW_AND_CLEAR);
      inputMap.put(controller.INSPECT.getAccelerator(), controller.INSPECT);
      inputMap.put(controller.CONCORDANCE.getAccelerator(), controller.CONCORDANCE);
      inputMap.put(controller.SEARCH_WITH.getAccelerator(), controller.SEARCH_WITH);

      //      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK),
      //                   SwingListeners.action("ShowTagSelector", e -> {
      //                      JOptionPane pane = new JOptionPane();
      //                      TagTreeView2 tv = new TagTreeView();
      //                      tv.seta(tagModel);
      //                      pane.setMessage(tv);
      //                      pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
      //                      JDialog dialog = pane.createDialog("Tag Selector");
      //                      SwingUtilities.invokeLater(() -> {
      //                         dialog.dispatchEvent(new FocusEvent(tv, FocusEvent.FOCUS_GAINED));
      //                      });
      //                      dialog.addComponentListener(SwingListeners.componentShown(ev -> {
      //                         tv.focusOnFilter();
      //                      }));
      //                      dialog.setVisible(true);
      //                   }));
      var menu = popupMenu(menuItem(controller.INSPECT),
                           Menus.SEPARATOR,
                           menuItem(controller.CONCORDANCE),
                           menuItem(controller.SEARCH_WITH));
      menu.addPopupMenuListener(MenuListeners.popupMenuWillBecomeVisible(e -> {
         boolean shouldBeVisible = Strings.isNotNullOrBlank(documentView.getSelectedText());
         controller.CONCORDANCE.setEnabled(shouldBeVisible);
         controller.INSPECT.setEnabled(shouldBeVisible);
         controller.SEARCH_WITH.setEnabled(shouldBeVisible);
      }));
      view.setComponentPopupMenu(menu);
      return view;
   }

   private MangoTable initSearchResultView() {
      return with(new MangoTable($("Start", Integer.class),
                                 $("End", Integer.class),
                                 $("Search Result", HString.class)), $ -> {
         Fonts.setFontSize($, 16f);
         $.setRowHeight($.getRowHeight() + 3);
         $.setShowGrid(true);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(true);
         $.getTableHeader().setReorderingAllowed(false);
         $.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         $.addMouseListener(SwingListeners.mouseClicked(e -> {
            if(e.getClickCount() == 2) {
               if($.getSelectedRow() >= 0) {
                  HString hString = Cast.as($.getValueAt($.getSelectedRow(), 2));
                  documentView.setSelectionRange(hString.start(), hString.end());
               }
            }
         }));
         $.withColumn(0, c -> {
            c.setCellRenderer(TableCellRenderers.RIGHT_ALIGN);
            c.setPreferredWidth(75);
            c.setMaxWidth(75);
            c.setResizable(false);
         });
         $.withColumn(1, c -> {
            c.setCellRenderer(TableCellRenderers.RIGHT_ALIGN);
            c.setPreferredWidth(75);
            c.setMaxWidth(75);
            c.setResizable(false);
         });
      });
   }

   private MangoTable initSentenceListView() {
      return with(new MangoTable($("#", Integer.class), $("Content", HString.class)), $ -> {
         Fonts.setFontSize($, 16f);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(false);
         $.setReorderingAllowed(false);
         $.setAlternateRowColor(Color::brighter);
         $.setRowHeightPadding(3);
         $.setReorderingAllowed(false);
         $.withColumn(0, c -> c.setMaxWidth(100));
         $.addMouseListener(SwingListeners.mouseClicked(e -> {
            if(e.getClickCount() == 2) {
               Integer index = Cast.as($.getValueAt($.getSelectedRow(), 0));
               Annotation sentence = document.sentences().get(index);
               documentView.setSelectionRange(sentence.start(), sentence.end());
            }
         }));
         for(Annotation sentence : document.sentences()) {
            $.addRow(sentence.<Integer>attribute(Types.INDEX), sentence);
         }
      });
   }

   public boolean isDirty() {
      return hasChanged.get();
   }

   protected void markDirty() {
      hasChanged.set(true);
   }

   protected void refreshStyle(int start, int end) {
      documentView.clearStyle(start, end);
      getModel().getOverlappingAnnotations(start, end)
                .stream()
                .max(Comparator.comparingInt(HString::length))
                .ifPresent(a -> documentView.highlight(a.start(), a.end(), model.getStyleForAnnotation(a)));
   }

   public void setAnnotationLayer(@NonNull AnnotationLayer layer) {
      documentView.clearAllStyles();
      if(annotationLayer != null) {
         for(String validTagLabel : annotationLayer.getValidTagLabels()) {
            documentView.removeStyle(validTagLabel);
         }
      }
      documentView.addStylesFrom(layer);
      model = new DocumentViewerModel(document, layer);
      tblAnnotations.setModel(model);
      tagView.setAnnotationLayer(layer);
      documentView.highlightAnnotations(model);
      this.annotationLayer = layer;
   }

   private class AnnotationTableCellRender extends DefaultTableCellRenderer {
      @Override
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {
         super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         row = table.convertRowIndexToModel(row);
         setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
         Tag tag = Cast.as(value);
         setText(tag.label());
         if(isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
         } else {
            Color bg = annotationLayer.getColor(tag);
            setForeground(Colors.calculateBestFontColor(bg));
            setBackground(bg);
         }
         return this;
      }
   }

}//END OF DocumentViewer
