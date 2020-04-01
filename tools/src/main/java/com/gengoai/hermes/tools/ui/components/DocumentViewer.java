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
import com.gengoai.hermes.tools.annotation.AnnotationModel;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.string.Strings;
import com.gengoai.swing.ColorUtils;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.components.TitlePane;
import com.gengoai.swing.fluent.*;
import com.gengoai.swing.listeners.FocusListeners;
import com.gengoai.swing.listeners.SwingListeners;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.components.Components.*;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createMatteBorder;

public class DocumentViewer extends FluentJPanel {
   public static final int ANNOTATION_TABLE_INDEX = 0;
   public static final int CONCORDANCE_TABLE_INDEX = 2;
   public static final int INSPECT_SELECTION_INDEX = 4;
   public static final int SEARCH_RESULTS_INDEX = 1;
   public static final int SENTENCE_LIST_INDEX = 3;
   public final AtomicBoolean hasChanged = new AtomicBoolean(false);
   private final Document document;
   private final TagModel tagModel;
   private final HStringViewer documentView;
   private final JTabbedPane tbPaneTools;
   private final TagTreeView2 tagView;
   private final SearchBar searchBar;
   private final FluentJTable tblConcordance;
   private final FluentJTable tblAnnotations;
   private final FluentJTable tblSentences;
   private final FluentJTable tblSearchResults;
   private final AnnotationType annotationType;
   private final FluentJTextPane txtInspectionWindow;
   private final FluentJLabel lblInspectionTitle;
   private AnnotationModel annotationModel;
   private AnnotationLayer annotationLayer;
   private int searchResultIndex = 0;

   public DocumentViewer(Document document, TagModel tagModel, AnnotationType annotationType) {
      this.annotationLayer = new AnnotationLayer(annotationType);
      this.document = document;
      this.annotationType = annotationType;
      //--------------------------------------------------------------------------------------------------------------
      //Initialize Models
      //--------------------------------------------------------------------------------------------------------------
      this.tagModel = tagModel;
      this.annotationModel = new AnnotationModel(annotationType);
      this.annotationModel.addAnnotations(document);
      //--------------------------------------------------------------------------------------------------------------

      //--------------------------------------------------------------------------------------------------------------
      //Define and Layout Controls
      //--------------------------------------------------------------------------------------------------------------
      this.tagView = new TagTreeView2(new AnnotationLayer(Types.ENTITY));
      this.tblSearchResults = initSearchResultList();
      this.documentView = initDocumentViewer();
      this.tblConcordance = initContextualSearchTable();
      this.tblAnnotations = initAnnotationTable();
      this.tblSentences = initSentenceList();
      this.lblInspectionTitle = label("").alignTextHorizontalCenter()
                                         .font(Font.BOLD, 20)
                                         .opaque()
                                         .background(UIManager.getColor("inactiveCaption"))
                                         .foreground(UIManager.getColor("inactiveCaptionText"))
                                         .compoundBorder(createMatteBorder(1,
                                                                           0,
                                                                           2,
                                                                           0,
                                                                           getBackground().brighter()),
                                                         createEmptyBorder(4, 4, 4, 4));
      this.txtInspectionWindow = initInspectionWindow();
      this.searchBar = new SearchBar(document);

      tbPaneTools = new JTabbedPane();
      tbPaneTools.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      tbPaneTools.addTab("Annotation Table", new JScrollPane(tblAnnotations));
      tbPaneTools.setToolTipTextAt(0, "Ctrl+1");
      tbPaneTools.addTab("Search Results", new JScrollPane(tblSearchResults));
      tbPaneTools.setToolTipTextAt(1, "Ctrl+2");
      tbPaneTools.addTab("Concordance", new JScrollPane(tblConcordance));
      tbPaneTools.setToolTipTextAt(2, "Ctrl+3");
      tbPaneTools.addTab("Sentence List", new JScrollPane(tblSentences));
      tbPaneTools.setToolTipTextAt(3, "Ctrl+4");
      tbPaneTools.addTab("Inspection Window", borderLayoutPanel($ -> {
         $.north(lblInspectionTitle);
         $.center(new JScrollPane(txtInspectionWindow));
      }));
      tbPaneTools.setToolTipTextAt(4, "Ctrl+5");
      tbPaneTools.setTabPlacement(JTabbedPane.BOTTOM);

      txtInspectionWindow.addFocusListener(FocusListeners.onFocusGained(e -> {
         lblInspectionTitle.setBackground(UIManager.getColor("activeCaption"));
         lblInspectionTitle.setForeground(UIManager.getColor("activeCaptionText"));
      }));
      txtInspectionWindow.addFocusListener(FocusListeners.onFocusLost(e -> {
         lblInspectionTitle.setBackground(UIManager.getColor("inactiveCaption"));
         lblInspectionTitle.setForeground(UIManager.getColor("inactiveCaptionText"));
      }));

      var splitPane = with(new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentView, tbPaneTools), $ -> {
         $.setDividerLocation(400);
         $.setDividerSize(5);
         $.setResizeWeight(1);
      });

      var ttlTagTitlePane = new TitlePane("Tag Search", false, tagView);
      ttlTagTitlePane.background(UIManager.getColor("Button.highlight"));
      JToolBar toolBar = new JToolBar();
      var mc = button("POS");
      mc.addActionListener(l -> {
         setAnnotationLayer(new AnnotationLayer(Types.TOKEN));
      });
      toolBar.add(mc);
      var ac = button("ENTITY");
      ac.addActionListener(l -> {
         try {
            AnnotationLayer layer = Json.parse(Resources.fromClasspath("annotation/layers/entities.json"),
                                               AnnotationLayer.class);
            setAnnotationLayer(layer);
         } catch(Exception e) {
            e.printStackTrace();
         }
      });
      toolBar.add(ac);
      borderLayout()
            .north(toolBar)
            .center(borderLayoutPanel($ -> $.north(searchBar).center(splitPane)))
            .east(ttlTagTitlePane);

      searchBar.setVisible(false);
      documentView.scrollToTopLeft();
      //--------------------------------------------------------------------------------------------------------------

      //--------------------------------------------------------------------------------------------------------------
      //Keyboard Hooks
      //--------------------------------------------------------------------------------------------------------------
      KeyboardFocusManager.getCurrentKeyboardFocusManager()
                          .addKeyEventDispatcher(e -> {
                             KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
                             if((ks.getModifiers() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
                                switch(ks.getKeyCode()) {
                                   case KeyEvent.VK_HOME:
                                      documentView.focusOnEditor();
                                      break;
                                   case KeyEvent.VK_INSERT:
                                      tagView.focusOnFilter();
                                      break;
                                   case KeyEvent.VK_1:
                                      tbPaneTools.setSelectedIndex(ANNOTATION_TABLE_INDEX);
                                      break;
                                   case KeyEvent.VK_2:
                                      tbPaneTools.setSelectedIndex(SEARCH_RESULTS_INDEX);
                                      break;
                                   case KeyEvent.VK_3:
                                      tbPaneTools.setSelectedIndex(CONCORDANCE_TABLE_INDEX);
                                      break;
                                   case KeyEvent.VK_4:
                                      tbPaneTools.setSelectedIndex(SENTENCE_LIST_INDEX);
                                      break;
                                   case KeyEvent.VK_5:
                                      tbPaneTools.setSelectedIndex(INSPECT_SELECTION_INDEX);
                                      break;
                                }
                             }
                             return false;
                          });
      //--------------------------------------------------------------------------------------------------------------
      //Define Listeners
      //--------------------------------------------------------------------------------------------------------------
      searchBar.onSearch(list -> {
         tblSearchResults.clear();
         for(HString result : list) {
            tblSearchResults.addRow(result.start(), result.end(), result);
         }
         tbPaneTools.setSelectedIndex(SEARCH_RESULTS_INDEX);
      }).onSearchSelectionChange(h -> {
         if(h != null) {
            documentView.setSelectionRange(h.start(), h.end());
         }
      });
      tagView.setCanPerformShortcut(documentView::hasFocus);

      documentView.onSelectionChange(($, e) -> {
         if(e.getNewSelection() != null) {
            int start = e.getNewSelection().v1;
            int end = e.getNewSelection().v2;
            annotationModel.getOverlappingAnnotations(start, end)
                           .stream()
                           .findFirst()
                           .ifPresent(a -> {
                              int row = tblAnnotations.convertRowIndexToView(annotationModel.indexOf(a));
                              tblAnnotations.getSelectionModel().setSelectionInterval(row, row);
                           });
         }
      });

      tagView.onTagSelect(this::performAddAnnotation);

   }

   private FluentJButton createSearchPanelActionButton(FontAwesome text, Color bg) {
      var dfColor = button("").getForeground();
      return button(text)
            .fontSize(12)
            .emptyBorder()
            .translucent()
            .onHover(($, isOver) -> {
               if(isOver) {
                  $.background(bg);
                  $.opaque();
                  $.repaint();
               } else {
                  $.translucent();
                  $.setForeground(dfColor);
               }
            })
            .preferredSize(24, 24);
   }

   private JPopupMenu createViewerPopupMenu() {
      var menu = new FluentJPopupMenu();
      var inspect = menu.addItem("Inspect", item -> {
         item.addActionListener(a -> performInspection());
         item.setIcon(FontAwesome.INFO.create(16, ColorUtils.calculateBestFontColor(menu.getBackground())));
      });
      menu.add(new JSeparator());
      var concordance = menu.addItem("Concordance", item -> {
         item.addActionListener(a -> performConcordance(documentView.getSelectedText()));
         item.setIcon(FontAwesome.LIST.create(16, ColorUtils.calculateBestFontColor(menu.getBackground())));
      });
      var searchWith = menu.addItem("Search with...", item -> {
         item.addActionListener(a -> {
            searchBar.setVisible(true);
            searchBar.setSearchText(documentView.getSelectedText());
            searchBar.performSearch();
            tbPaneTools.setSelectedIndex(SEARCH_RESULTS_INDEX);
         });
         item.setIcon(FontAwesome.SEARCH.create(16, ColorUtils.calculateBestFontColor(menu.getBackground())));
      });
      menu.onPopupMenuWillBecomeVisible(($, e) -> {
         if(Strings.isNullOrBlank(documentView.getSelectedText())) {
            searchWith.setEnabled(false);
            concordance.setEnabled(false);
         } else {
            searchWith.setEnabled(true);
            concordance.setEnabled(true);
         }
      });
      return menu;
   }

   protected String getStyleForAnnotation(Annotation annotation) {
      return tagModel.getTagInfo(annotation.getTag()).toString();
   }

   private void htmlTableBegin(StringBuilder b, Color borderColor, String sectionName, Color sectionBackground) {
      b.append("<table width='100%' cellpadding='1' bgcolor='")
       .append(ColorUtils.toHexString(borderColor))
       .append("'>")
       .append("<table width='100%' border=0 cellpadding='0' cellspacing='0'><tr bgcolor='")
       .append(ColorUtils.toHexString(sectionBackground))
       .append("' style='font-weight:bold; text-align: center; color:")
       .append(ColorUtils.toHexString(ColorUtils.calculateBestFontColor(sectionBackground)))
       .append("'><td colspan=2 style='margin: 5 5 5 5'>")
       .append(sectionName)
       .append("</td></tr>");
   }

   private void htmlTableEnd(StringBuilder b) {
      b.append("</table></table>");
   }

   private void htmlTableRow(StringBuilder html, Color background, int nc, String... columns) {
      htmlTableRow(html, background, ColorUtils.calculateBestFontColor(background), nc, columns);
   }

   private void htmlTableRow(StringBuilder html, Color background, Color foreground, int nc, String... columns) {
      html.append("<tr bgcolor='")
          .append(ColorUtils.toHexString(background))
          .append("' style='font-weight:bold; color:")
          .append(ColorUtils.toHexString(foreground))
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

   private FluentJTable initAnnotationTable() {
      return table(annotationModel)
            .withColumn(0, c -> {
               c.setPreferredWidth(75);
               c.setMaxWidth(75);
               c.setResizable(false);
            })
            .withColumn(1, c -> {
               c.setPreferredWidth(75);
               c.setMaxWidth(75);
               c.setResizable(false);
            })
            .withColumn(2, c -> {
               c.setPreferredWidth(150);
               c.setMaxWidth(150);
               c.setCellEditor(new DefaultCellEditor(new JComboBox<>(annotationLayer.getValidTagsArray())));
               c.setCellRenderer(new AnnotationTableCellRender());
               c.setPreferredWidth(350);
            })
            .fontSize(16f)
            .rowHeightPadding(3)
            .showGrid()
            .fillViewPortHeight()
            .autoCreateRowSorter(true)
            .disableColumnReordering()
            .onSelectionChanged(($, l) -> {
               if(l.getFirstIndex() < annotationModel.size()) {
                  int row = $.convertRowIndexToView(l.getFirstIndex());
                  Rectangle cellRect = $.getCellRect(row, 0, true);
                  $.scrollRectToVisible(cellRect);
               }
            })
            .onMouseDoubleClicked(($, e) -> {
               if($.getSelectedRow() >= 0) {
                  HString h = Cast.as($.getValueAt($.getSelectedRow(), 3));
                  documentView.setSelectionRange(h.start(), h.end());
               }
            });
   }

   private FluentJTable initContextualSearchTable() {
      FluentJTable table = table(false, "Left Context", "Search String", "Right Context", "")
            .withColumn(0, c -> {
               c.setCellRenderer(TableCellRenderers.RIGHT_ALIGN);
               c.setResizable(false);
            }).withColumn(1, c -> {
               c.setCellRenderer(new CustomTableCellRender()
                                       .alignment(SwingConstants.CENTER)
                                       .foreground(Color.RED)
                                       .fontStyle(Font.BOLD));
               c.setResizable(false);
            })
            .fontSize(16f)
            .fillViewPortHeight()
            .autoCreateRowSorter(false)
            .singleSelectionModel()
            .rowHeightPadding(3)
            .hideGrid()
            .disableColumnReordering()
            .onMouseReleased(($, e) -> {
               if(e.getClickCount() == 2 && $.getSelectedRow() >= 0) {
                  Integer start = Cast.as($.getModel().getValueAt($.convertRowIndexToModel($.getSelectedRow()), 3));
                  int length = $.getValueAt($.getSelectedRow(), 1).toString().length();
                  documentView.setSelectionRange(start, start + length);
               }
            });
      TableColumnModel tcm = table.getColumnModel();
      tcm.removeColumn(tcm.getColumn(3));
      table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      return table;
   }

   private HStringViewer initDocumentViewer() {
      var view = new HStringViewer(document)
            .setAllowZoom(true)
            .fontSize(18f)
            .selectionColor(Color.YELLOW)
            .selectedTextColor(Color.BLACK)
            .addStylesFrom(tagModel)
            .highlightAnnotations(annotationModel)
            .font(Font.MONOSPACED)
            .keepHighlightWhenFocusLost();
      InputMap inputMap = view.getEditorInputMap();
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK),
                   SwingListeners.action("ToggleSearchBar", e -> searchBar.setVisible(!searchBar.isVisible())));
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                   SwingListeners.action("FocusOnTagView", e -> tagView.focusOnFilter()));
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                   SwingListeners.action("DeleteAnnotation",
                                         e -> onDeleteAnnotation(documentView.getSelectionStart(),
                                                                 documentView.getSelectionEnd())));
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
                   SwingListeners.action("FocusOnTagView", e -> {
                      tagView.focusOnFilter();
                      tagView.setFilterText("");
                   }));

      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK),
                   SwingListeners.action("ShowTagSelector", e -> {
                      JOptionPane pane = new JOptionPane();
                      TagTreeView tv = new TagTreeView();
                      tv.setTagModel(tagModel);
                      pane.setMessage(tv);
                      pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
                      JDialog dialog = pane.createDialog("Tag Selector");
                      SwingUtilities.invokeLater(() -> {
                         dialog.dispatchEvent(new FocusEvent(tv, FocusEvent.FOCUS_GAINED));
                      });
                      dialog.addComponentListener(SwingListeners.componentShown(ev -> {
                         tv.focusOnFilter();
                      }));
                      dialog.setVisible(true);
                   }));

      view.popupMenu(createViewerPopupMenu());
      return view;
   }

   private FluentJTextPane initInspectionWindow() {
      var txt = textPane().nonEditable();
      txt.setContentType("text/html");
      return txt;
   }

   private FluentJTable initSearchResultList() {
      return table(false, "Start", "End", "Search Result")
            .withColumn(0, c -> {
               c.setCellRenderer(TableCellRenderers.RIGHT_ALIGN);
               c.setPreferredWidth(75);
               c.setMaxWidth(75);
               c.setResizable(false);
            })
            .withColumn(1, c -> {
               c.setCellRenderer(TableCellRenderers.RIGHT_ALIGN);
               c.setPreferredWidth(75);
               c.setMaxWidth(75);
               c.setResizable(false);
            })
            .fontSize(16f)
            .rowHeightPadding(3)
            .showGrid()
            .fillViewPortHeight()
            .autoCreateRowSorter(true)
            .disableColumnReordering()
            .onMouseDoubleClicked(($, e) -> {
               if($.getSelectedRow() >= 0) {
                  HString hString = Cast.as($.getValueAt($.getSelectedRow(), 2));
                  documentView.setSelectionRange(hString.start(), hString.end());
               }
            });
   }

   private FluentJTable initSentenceList() {
      var sents = table(false, "#", "Content")
            .withColumn(0, c -> {
               c.setPreferredWidth(30);
               c.setMaxWidth(30);
               c.setResizable(false);
            })
            .withColumn(1, c -> {
               c.setResizable(false);
            })
            .fillViewPortHeight()
            .autoCreateRowSorter(false)
            .alternateRowColor(Color::brighter)
            .fontSize(16)
            .singleSelectionModel()
            .rowHeightPadding(3)
            .disableColumnReordering()
            .onMouseDoubleClicked(($, e) -> {
               Integer index = Cast.as($.getValueAt($.getSelectedRow(), 0));
               Annotation sentence = document.sentences().get(index);
               documentView.setSelectionRange(sentence.start(), sentence.end());
            });
      DefaultTableModel model = sents.model();
      for(Annotation sentence : document.sentences()) {
         model.addRow(new Object[]{sentence.<Integer>attribute(Types.INDEX), sentence.toString()});
      }
      return sents;
   }

   private void onDeleteAnnotation(int start, int end) {
      final java.util.List<Annotation> toDelete = annotationModel.getEnclosingAnnotations(start, end);
      if(toDelete.isEmpty()) {
         return;
      }
      boolean deleted = false;
      if(toDelete.size() == 1) {
         deleted = true;
         annotationModel.removeAll(toDelete);
      } else {
         JCheckBox[] checkBoxes = new JCheckBox[toDelete.size()];
         for(int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i] = new JCheckBox(getStyleForAnnotation(toDelete.get(i)) + " (" + toDelete.get(i) + ")");
            checkBoxes[i].setSelected(true);
         }
         if(JOptionPane.showOptionDialog(this,
                                         checkBoxes,
                                         "Select the annotations to delete",
                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null,
                                         new String[]{"Delete", "Cancel"},
                                         "Delete") == JOptionPane.OK_OPTION) {
            for(int i = 0; i < checkBoxes.length; i++) {
               if(checkBoxes[i].isSelected()) {
                  deleted = true;
                  annotationModel.remove(toDelete.get(i));
               }
            }
         }
      }
      if(deleted) {
         hasChanged.set(true);
         documentView.clearStyle(start, end);
         annotationModel.getEnclosingAnnotations(start, end)
                        .stream()
                        .max(Comparator.comparingInt(HString::length))
                        .ifPresent(a -> documentView.highlight(a.start(), a.end(), getStyleForAnnotation(a)));
      }
   }

   private void performAddAnnotation(Tag tag) {
      if(documentView.hasSelection()) {
         if(annotationModel.getOverlappingAnnotations(documentView.getSelectionStart(), documentView.getSelectionEnd())
                           .stream()
                           .anyMatch(a -> a.getTag() == tag)) {
            return;
         }
         if(annotationType == Types.TOKEN) {
            return;
         }
         Annotation annotation = document.annotationBuilder(annotationType)
                                         .start(documentView.getSelectionStart())
                                         .end(documentView.getSelectionEnd())
                                         .attribute(annotationType.getTagAttribute(), tag)
                                         .createAttached();
         annotationModel.addAnnotation(annotation);
         documentView.highlight(documentView.getSelectionStart(), documentView.getSelectionEnd(), tag.label());
         documentView.setSelectionRange(documentView.getSelectionStart(), documentView.getSelectionEnd());
         hasChanged.set(true);
      }
   }

   private void performConcordance(String text) {
      tblConcordance.rowCount(0);
      if(Strings.isNotNullOrBlank(text)) {
         AtomicInteger maxChars = new AtomicInteger();
         SearchStrategy.CASE_INSENSITIVE_EXACT_MATCH
               .findAll(text, document)
               .forEach(h -> {
                  CharSequence lContext = h.leftContext(4);
                  maxChars.set(Math.max(maxChars.get(), lContext.length()));
                  tblConcordance.addRow(lContext, h, h.rightContext(4), h.start());
               });
         tblConcordance.resizeColumnWidth(20);
         tbPaneTools.setSelectedIndex(CONCORDANCE_TABLE_INDEX);
      }
   }

   private void performInspection() {
      if(documentView.hasSelection()) {
         HString selection = document.substring(documentView.getSelectionStart(), documentView.getSelectionEnd());
         var background = documentView.getBackground();
         var foreground = documentView.getForeground();
         Color borderColor;
         Color sectionColor;
         if(ColorUtils.calculateBestFontColor(background) == Color.WHITE) {
            borderColor = background.brighter();
            sectionColor = background.darker();
         } else {
            borderColor = background.darker();
            sectionColor = background.darker();
         }
         lblInspectionTitle.setText(selection.toString());
         StringBuilder html = new StringBuilder("<html>");
         java.util.List<Annotation> styledSpans = documentView.getAnnotations(annotationType,
                                                                              documentView.getSelectionStart(),
                                                                              documentView.getSelectionEnd());

         if(styledSpans.size() > 0) {
            htmlTableBegin(html, borderColor, annotationType.getTagAttribute().label(), sectionColor);
            for(Annotation span : styledSpans) {
               var c = tagModel.getTagInfo(span.getTag()).getColor();
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
            if(!annotationType.equals(type)) {
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
         txtInspectionWindow.setText(html.toString());
         tbPaneTools.setSelectedIndex(INSPECT_SELECTION_INDEX);
      }
   }

   public void setAnnotationLayer(@NonNull AnnotationLayer layer) {
      documentView.clearAllStyles();
      if(annotationLayer != null) {
         for(String validTagLabel : annotationLayer.getValidTagLabels()) {
            documentView.removeStyle(validTagLabel);
         }
      }

      documentView.addStylesFrom(layer);
      annotationModel = new AnnotationModel(layer.getAnnotationType());
      annotationModel.addAnnotations(document);
      tblAnnotations.setModel(annotationModel);
      tblAnnotations.withColumn(0, c -> {
         c.setPreferredWidth(75);
         c.setMaxWidth(75);
         c.setResizable(false);
      }).withColumn(1, c -> {
         c.setPreferredWidth(75);
         c.setMaxWidth(75);
         c.setResizable(false);
      }).withColumn(2, c -> {
         c.setPreferredWidth(150);
         c.setMaxWidth(150);
         c.setCellEditor(new DefaultCellEditor(new JComboBox<>(annotationLayer.getValidTagsArray())));
         c.setCellRenderer(new AnnotationTableCellRender());
         c.setPreferredWidth(350);
      });
      tagView.setAnnotationLayer(layer);
      documentView.highlightAnnotations(annotationModel);

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
         row = table.getRowSorter().convertRowIndexToModel(row);
         setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
         if(isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
         } else {
            Color bg = tagModel.getColor(annotationModel.get(row).getTag());
            setForeground(ColorUtils.calculateBestFontColor(bg));
            setBackground(bg);
         }
         return this;
      }
   }

}//END OF DocumentViewer
