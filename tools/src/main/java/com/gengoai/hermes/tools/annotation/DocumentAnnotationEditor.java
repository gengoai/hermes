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

import com.gengoai.collection.Iterables;
import com.gengoai.collection.tree.Span;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.*;
import com.gengoai.hermes.extraction.regex.TokenMatcher;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.hermes.tools.ui.components.AnnotationEditor;
import com.gengoai.hermes.tools.ui.components.TagInfo;
import com.gengoai.hermes.tools.ui.components.TagModel;
import com.gengoai.hermes.tools.ui.components.TagTreeView;
import com.gengoai.string.Strings;
import com.gengoai.swing.ColorUtils;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.borders.ComponentBorder;
import com.gengoai.swing.components.*;
import com.gengoai.swing.fluent.FluentJButton;
import com.gengoai.swing.listeners.SwingListeners;
import com.gengoai.tuple.IntPair;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.components.Components.button;
import static com.gengoai.swing.components.Components.table;

public class DocumentAnnotationEditor extends JPanel {
   public final TagModel tagModel;
   public final AnnotationModel annotationModel;
   public final Document document;
   public final AnnotationType annotationType;
   public final AtomicBoolean hasChanged = new AtomicBoolean(false);
   private final AnnotationEditor editor = new AnnotationEditor();
   private final TagTreeView tagView = new TagTreeView();
   private final JList<Snippet> selectSearchList = new JList<>();
   private final JList<Snippet> tokenRegexSearchList = new JList<>();
   private final JTable annotationTable;
   private final JSplitPane editorSearchSplit = new JSplitPane();
   private final JSplitPane searchSplitPane = new JSplitPane();
   private final JToolBar toolBar = new JToolBar();
   private boolean viewTooltips = true;

   @NonNull
   private Consumer<Boolean> onMessageWindowVisibilityChange = b -> {
   };


   public DocumentAnnotationEditor(@NonNull Document document,
                                   @NonNull AnnotationType annotationType,
                                   @NonNull TagModel tagModel) {
      super(true);
      this.document = document;
      this.annotationType = annotationType;
      this.tagModel = tagModel;
      this.annotationModel = new AnnotationModel(annotationType);

      createToolbar();

      //---- Editor -------------------------
      with(editor, e -> {
         e.setKeepHighlighting(true);
         e.setText(document.toString());
         e.setEditable(false);
         if(document.isCompleted(Types.TOKEN)) {
            e.setAutoExpandAction(this::expand);
         } else if(document.getLanguage().usesWhitespace()) {
            e.setAutoExpandAction(EditorPanel.DEFAULT_AUTO_EXPANSION);
         } else {
            e.setAutoExpandAction(EditorPanel.NO_AUTO_EXPANSION);
         }
         e.setFontSize(18);
         e.setMinimumSize(new Dimension(400, 300));
         for(TagInfo t : tagModel) {
            e.addStyle(t.toString(), style -> {
               StyleConstants.setForeground(style, ColorUtils.calculateBestFontColor(t.getColor()));
               StyleConstants.setBackground(style, t.getColor());
               StyleConstants.setBold(style, true);
            });
         }
         KeyboardFocusManager.getCurrentKeyboardFocusManager()
                             .addKeyEventDispatcher(key -> {
                                if(editor.editorHasFocus()) {
                                   if(key.getKeyCode() == KeyEvent.VK_DELETE) {
                                      hasChanged.set(true);
                                      onDeleteAnnotation(e.getSelectionStart(), e.getSelectionEnd());
                                   }
                                }
                                return false;
                             });
         e.addCaretListener(this::editorCaretListener);
         e.addMouseMotionListener(SwingListeners.mouseMoved(this::toolTip));
      });


      //---- Tag View -------------------------
      with(tagView, tv -> {
         tv.setTagModel(tagModel);
         tv.setCanPerformShortcut(editor::editorHasFocus);
         tv.onTagSelect(this::onAddAnnotation);
      });

      updateAnnotations();

      annotationModel.onRemoveAnnotation(($, annotation) -> {
         hasChanged.set(true);
         editor.clearStyle(annotation.start(), annotation.end());
         Iterables.getFirst($.getOverlappingAnnotations(annotation.start(), annotation.end()))
                  .ifPresent(c -> editor.highlight(c.start(), c.end(), getStyleForAnnotation(c)));
      }).tableModelListener(($, e) -> {
         int r = e.getFirstRow();
         if(e.getType() == TableModelEvent.UPDATE) {
            hasChanged.set(true);
            editor.updateHighlight($.get(r).start(),
                                   $.get(r).end(),
                                   $.getLastTag(r),
                                   $.getValueAt(r, 2).toString());
         }
      });


      annotationTable = table(annotationModel)
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
               c.setCellEditor(new DefaultCellEditor(new JComboBox<>(tagView.getTags())));
               c.setCellRenderer(new AnnotationTableCellRender());
               c.setPreferredWidth(350);
            })
            .showGrid()
            .fillViewPortHeight()
            .autoCreateRowSorter(true)
            .disableColumnReordering()
            .onMouseDoubleClicked(($, e) -> onAnnotationTableMouseClick(e));
//            .onSelectionChanged(($, l) -> {
//               if(l.getFirstIndex() < annotationModel.size()) {
//                  int row = $.convertRowIndexToView(l.getFirstIndex());
//                  Rectangle cellRect = $.getCellRect(row, 0, true);
//                  $.scrollRectToVisible(cellRect);
//               }
//            });


      //---- Annotation Table -------------------------
//      with(annotationTable, table -> {
//         table.setModel(annotationModel);
//         with(table.getColumnModel().getColumn(0), c -> {
//            c.setPreferredWidth(75);
//            c.setMaxWidth(75);
//            c.setResizable(false);
//         });
//         with(table.getColumnModel().getColumn(1), c -> {
//            c.setPreferredWidth(75);
//            c.setMaxWidth(75);
//            c.setResizable(false);
//         });
//         with(table.getColumnModel().getColumn(2), c -> {
//            c.setPreferredWidth(150);
//            c.setMaxWidth(150);
//            String[] tags = tagView.getTags();
//            Arrays.sort(tags);
//            var comboBox = new JComboBox<>(tags);
//            c.setCellEditor(new DefaultCellEditor(comboBox));
//            c.setCellRenderer(new AnnotationTableCellRender());
//         });
//         with(table.getColumnModel().getColumn(2), c -> c.setPreferredWidth(350));
//         table.setShowGrid(true);
//         table.setFillsViewportHeight(true);
//         table.getTableHeader().setReorderingAllowed(false);
//         table.setAutoCreateRowSorter(true);
//         table.getRowSorter().toggleSortOrder(0);
//         table.getSelectionModel()
//              .addListSelectionListener(l -> {
//                 if(l.getFirstIndex() < annotationModel.size()) {
//                    int row = table.convertRowIndexToView(l.getFirstIndex());
//                    Rectangle cellRect = table.getCellRect(row, 0, true);
//                    table.scrollRectToVisible(cellRect);
//                 }
//              });
//         table.addMouseListener(MouseListeners.mouseClicked(this::onAnnotationTableMouseClick));
//      });

      with(selectSearchList, mw -> {
         mw.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         //Select the text in the editor when selected in the search window
         mw.addMouseListener(SwingListeners.mouseClicked(e -> onSnippetClick(e, mw)));
      });

      with(tokenRegexSearchList, mw -> {
         mw.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         //Select the text in the editor when selected in the search window
         mw.addMouseListener(SwingListeners.mouseClicked(e -> onSnippetClick(e, mw)));
      });

      var editorSplit = with(new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor, new JScrollPane(annotationTable)),
                             s -> {
                                s.setDividerLocation(0.8);
                                s.setResizeWeight(1);
                             });


      var selectSearchTitlePane = with(new TitlePane("Selection Search", false, selectSearchList), t -> {
         t.setOnCloseHandler(e -> t.setVisible(false));
      });


      with(searchSplitPane, s -> {
         s.setOrientation(JSplitPane.VERTICAL_SPLIT);
         s.setTopComponent(selectSearchTitlePane);
         s.setBottomComponent(new TitlePane("TokenRegex Results", false, tokenRegexSearchList));
      });


      addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            editorSplit.setDividerLocation(0.8);
            searchSplitPane.setDividerLocation(0.5);
            removeComponentListener(this);
         }
      });
      with(editorSearchSplit, s -> {
         s.setLeftComponent(searchSplitPane);
         s.setRightComponent(editorSplit);
         s.setResizeWeight(0);
         s.setDividerLocation(0.25);
      });

      selectSearchTitlePane.addComponentListener(new ComponentAdapter() {
         int dividerSize = 5;

         @Override
         public void componentHidden(ComponentEvent e) {
            editorSearchSplit.setLeftComponent(null);
            dividerSize = editorSearchSplit.getDividerSize();
            editorSearchSplit.setDividerSize(0);
            onMessageWindowVisibilityChange.accept(false);
         }

         @Override
         public void componentShown(ComponentEvent e) {
            editorSearchSplit.setDividerSize(dividerSize);
            editorSearchSplit.setLeftComponent(selectSearchTitlePane);
            onMessageWindowVisibilityChange.accept(true);
         }
      });

      //---- Layout -------------------------
      setLayout(new BorderLayout());

      add(with(toolBar, tb -> {
         tb.setFloatable(false);
      }), BorderLayout.NORTH);
      add(with(new JPanel(new BorderLayout()), m -> {
         m.add(new TitlePane("Tag Set", false, tagView), BorderLayout.EAST);
         m.add(editorSearchSplit, BorderLayout.CENTER);
      }), BorderLayout.CENTER);
   }

   private void createToolbar() {
      final int height = 48;
      final int imgSize = 32;
      toolBar.setPreferredSize(new Dimension(400, height));
      Color selected = ColorUtils.calculateBestFontColor(toolBar.getBackground());
      Color notSelected = ColorUtils.calculateBestFontColor(selected);

      var btnViewMessageWindow = button(FontAwesome.COLUMNS.asString())
            .font(FontAwesome.getFontName(), Font.PLAIN, 32)
            .selected(true)
            .tooltip("Hide the search results window.")
            .foreground(selected)
            .onHover(this::onToolbarButtonHover)
            .actionListener(($, event) -> onToolbarButtonClicked($,
                                                                 "Hide the search results window.",
                                                                 "Show the search results window."));

      var btnTooltipToggle = button(FontAwesome.COMMENT.asString())
            .font(FontAwesome.getFontName(), Font.PLAIN, 32)
            .selected(true)
            .tooltip("Turn off tooltips for annotations.")
            .foreground(selected)
            .onHover(this::onToolbarButtonHover)
            .actionListener(($, event) -> {
               viewTooltips = !$.isSelected();
               $.setSelected(viewTooltips);
               editor.setToolTipText(null);
               onToolbarButtonClicked($,
                                      "Turn off tooltips for annotations.",
                                      "Turn on tooltips for annotations.");
            });


      var btnAnnotate = button(FontAwesome.BOLT.asString())
            .font(FontAwesome.getFontName(), Font.PLAIN, 32)
            .tooltip("Annotate Document")
            .foreground(Color.ORANGE)
            .actionListener(($, event) -> {
               if(annotationType != Types.TOKEN) {
                  document.setUncompleted(annotationType);
               }
               document.setUncompleted(annotationType.getTagAttribute());
               setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               document.annotate(Types.ENTITY, Types.LEMMA, Types.DEPENDENCY);
               if(annotationType != Types.TOKEN) {
                  for(Iterator<Annotation> itr = document.annotations(annotationType).iterator(); itr.hasNext(); ) {
                     Annotation anno = itr.next();
                     if(anno.enclosedAnnotations(annotationType).size() > 1) {
                        itr.remove();
                        document.remove(anno);
                     }
                  }
               }
               updateAnnotations();
               setCursor(Cursor.getDefaultCursor());
            });


      toolBar.add(btnViewMessageWindow);
      toolBar.add(btnTooltipToggle);
      toolBar.add(Box.createRigidArea(new Dimension(32, 32)));
      toolBar.add(btnAnnotate);
      toolBar.addSeparator();
      toolBar.add(Box.createHorizontalGlue());


      final JTextField searchText = new JTextField(20);
      searchText.setToolTipText("TokenRegex Search");


      var btnDoSearch = button(FontAwesome.SEARCH.asString())
            .font(FontAwesome.getFontName(), Font.PLAIN, 24)
            .foreground(Color.WHITE)
            .emptyBorderWithMargin(0, 0, 0, 0)
            .translucent()
            .selected(true)
            .onHover(this::onToolbarButtonHover)
            .actionListener(($, event) -> {
               if(Strings.isNullOrBlank(searchText.getText())) {
                  return;
               }
               try {
                  TokenMatcher activeSearch = TokenRegex.compile(searchText.getText()).matcher(document);
                  Vector<Snippet> snippets = new Vector<>();
                  RectanglePainter green = new RectanglePainter(Color.GREEN);
                  editor.getHighlighter().removeAllHighlights();
                  while(activeSearch.find()) {
                     snippets.add(new Snippet(activeSearch.group().start(),
                                              activeSearch.group().end(),
                                              activeSearch.group().toString()));
                     editor.getHighlighter().addHighlight(activeSearch.group().start(),
                                                          activeSearch.group().end(),
                                                          green);
                  }
                  tokenRegexSearchList.setListData(snippets);
                  tokenRegexSearchList.setToolTipText(searchText.getText());
               } catch(Exception e) {
                  JOptionPane.showMessageDialog(this, e.getMessage(), "Error Parsing Regex", JOptionPane.ERROR_MESSAGE);
               }
            });

      ComponentBorder search = new ComponentBorder(btnDoSearch);
      search.setParent(searchText);
      toolBar.add(searchText);


      btnViewMessageWindow.addActionListener(a -> {
         searchSplitPane.setVisible(!searchSplitPane.isVisible());
         btnViewMessageWindow.setSelected(searchSplitPane.isVisible());
         if(searchSplitPane.isVisible()) {
            editorSearchSplit.setLeftComponent(searchSplitPane);
         } else {
            editorSearchSplit.setLeftComponent(null);
         }
      });
   }

   private void editorCaretListener(CaretEvent c) {
      if(editor.hasSelection()) {
         int start = editor.getSelectionStart();
         int end = editor.getSelectionEnd();
         annotationModel.getOverlappingAnnotations(start, end)
                        .stream()
                        .findFirst()
                        .ifPresent(a -> {
                           int row = annotationTable.convertRowIndexToView(annotationModel.indexOf(a));
                           annotationTable.getSelectionModel().setSelectionInterval(row, row);
                        });
         findSelected(start, end);
      } else {
         selectSearchList.removeAll();
      }
   }

   private IntPair expand(int start, int end, String text, StyledSpan span) {
      if(span == null) {
         HString hSpan = HString.union(document.annotations(Types.TOKEN, Span.of(start, end)));
         if(hSpan.isEmpty()) {
            return IntPair.of(start, end);
         }
         return IntPair.of(hSpan.start(), hSpan.end());
      }
      return IntPair.of(span.start(), span.end());
   }

   private void findSelected(int start, int end) {
      selectSearchList.removeAll();
      int index = -1;
      final String toFind = editor.getSelectedText();
      final String text = editor.getText();
      final int selLength = toFind.length();
      Vector<Snippet> snippets = new Vector<>();
      int selectedIndex = -1;
      while((index = text.indexOf(toFind, index + 1)) != -1) {
         int cStart = Math.max((index > 0)
                               ? index - 10
                               : 0, 0);
         int cEnd = Math.min(((index + selLength) < text.length())
                             ? index + selLength + 10
                             : text.length(), text.length());
         String ctxt = text.substring(cStart, cEnd);
         if(cStart > 0) {
            ctxt = "... " + ctxt;
         }
         if(cEnd < text.length() - 1) {
            ctxt += " ...";
         }
         ctxt = ctxt.replaceAll("(\r?\n)+", " ");
         snippets.add(new Snippet(
               index,
               index + toFind.length(),
               ctxt
         ));
         if(index == start) {
            selectedIndex = snippets.size() - 1;
         }
      }
      selectSearchList.setListData(snippets);
      selectSearchList.setSelectedIndex(selectedIndex);
   }

   protected String getStyleForAnnotation(Annotation annotation) {
      return tagModel.getTagInfo(annotation.getTag()).toString();
   }

   private void onAddAnnotation(TagInfo ti) {
      if(editor.getSelectionStart() >= editor.getSelectionEnd()) {
         return;
      }
      if(annotationModel.getOverlappingAnnotations(editor.getSelectionStart(), editor.getSelectionEnd())
                        .stream()
                        .anyMatch(a -> a.getTag() == ti.getTag())) {
         return;
      }
      if(annotationType == Types.TOKEN) {
         return;
      }
      Annotation annotation = document.annotationBuilder(annotationType)
                                      .start(editor.getSelectionStart())
                                      .end(editor.getSelectionEnd())
                                      .attribute(annotationType.getTagAttribute(), ti.getTag())
                                      .createAttached();
      hasChanged.set(true);
      annotationModel.addAnnotation(annotation);
      editor.highlight(editor.getSelectionStart(), editor.getSelectionEnd(), ti.toString());
   }

   private void onAnnotationTableMouseClick(MouseEvent e) {
      if(annotationTable.getSelectedRow() >= 0) {
         int row = annotationTable.getRowSorter().convertRowIndexToModel(annotationTable.getSelectedRow());
         editor.setSelectionRange(annotationModel.get(row).start(), annotationModel.get(row).end());
         annotationTable.requestFocus();
      }
   }

   private void onDeleteAnnotation(int start, int end) {
      final java.util.List<Annotation> toDelete = annotationModel.getEnclosingAnnotations(start, end);
      if(toDelete.isEmpty()) {
         return;
      }
      if(toDelete.size() == 1) {
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
                  annotationModel.remove(toDelete.get(i));
               }
            }
         }
      }
   }

   private void onSnippetClick(MouseEvent e, JList<Snippet> list) {
      if(list.getModel().getSize() > 0) {
         int index = list.getSelectedIndex();
         Snippet snippet = list.getModel().getElementAt(index);
         editor.setSelectionRange(snippet.getStart(), snippet.getEnd());
      }
   }

   private void onToolbarButtonClicked(FluentJButton btn, String selectedTooltip, String tooltip) {
      Color selected = ColorUtils.calculateBestFontColor(toolBar.getBackground());
      Color notSelected = ColorUtils.calculateBestFontColor(selected);
      if(btn.isSelected()) {
         btn.tooltip(selectedTooltip).foreground(selected);
      } else {
         btn.tooltip(tooltip).foreground(notSelected);
      }
   }

   private void onToolbarButtonHover(FluentJButton btn, Boolean isMouseOver) {
      Color selected = ColorUtils.calculateBestFontColor(toolBar.getBackground());
      Color notSelected = ColorUtils.calculateBestFontColor(selected);
      if(isMouseOver) {
         btn.setForeground(Color.RED);
      } else {
         btn.setForeground(btn.isSelected()
                           ? selected
                           : notSelected);
      }
   }

   private void toolTip(MouseEvent mouseEvent) {

      if(!viewTooltips) {
         setToolTipText(null);
         return;
      }

      int index = editor.getTextAtPosition(mouseEvent.getPoint());
      List<Annotation> styledSpans = annotationModel.getOverlappingAnnotations(index, index + 1);

      if(styledSpans.isEmpty() && document.annotations(Types.TOKEN, Span.of(index, index + 1)).isEmpty()) {
         setToolTipText(null);
         return;
      }

      Span r = styledSpans.stream()
                          .max(Comparator.comparingInt(Span::length))
                          .map(Cast::<Span>as)
                          .orElse(Span.of(index, index + 1));

      StringBuilder annotationSection = new StringBuilder();
      AtomicInteger odd = new AtomicInteger(0);

      if(styledSpans.size() > 0) {
         annotationSection.append("<table>");
         for(Annotation span : styledSpans) {
            annotationSection.append("<tr bgcolor=\"")
                             .append(ColorUtils.toHexString(tagModel.getColor(span.getTag())))
                             .append("\" style=\"font-weight:bold; color:")
                             .append(ColorUtils.toHexString(ColorUtils.calculateBestFontColor(tagModel.getColor(
                                   span.getTag()))))
                             .append("\"><td>")
                             .append(span)
                             .append("</td><td>")
                             .append(span.getTag().name())
                             .append("</td></tr>");
         }
         annotationSection.append("</table>");
      }
      StringBuilder html = new StringBuilder("<html>");
      if(annotationSection.length() > 0) {
         html.append("<h1 align=\"center\">")
             .append(annotationType.label())
             .append("</h1>")
             .append("<font size=\"4\">")
             .append(annotationSection);
      } else {
         html.append("</h1>")
             .append("<font size=\"4\">");
      }
      html.append("<h3 align=\"center\">Tokens</h3>");
      html.append("<table width=\"100%\">");
      odd.set(0);
      document.annotations(Types.TOKEN, r)
              .forEach(a -> {
                 html.append("<tr bgcolor=\"")
                     .append(odd.incrementAndGet() % 2 == 0
                             ? "##484c63"
                             : "#363636")
                     .append("\"><td>")
                     .append(a)
                     .append("</td><td>")
                     .append(a.pos())
                     .append("</td></tr>");
                 String cats = a.categories()
                                .stream()
                                .map(BasicCategories::name)
                                .collect(Collectors.joining(", "));
                 if(Strings.isNotNullOrBlank(cats)) {
                    html.append("<tr bgcolor=\"")
                        .append(odd.get() % 2 == 0
                                ? "##484c63"
                                : "#363636")
                        .append("\"><td colspan=\"2\">")
                        .append(cats)
                        .append("</td></tr>");
                 }
              });
      html.append("</table></font></html>");
      editor.setToolTipText(html.toString());
   }

   private void updateAnnotations() {
      annotationModel.clear();
      for(Annotation annotation : document.annotations(annotationType)) {
         String style = getStyleForAnnotation(annotation);
         annotationModel.addAnnotation(annotation);
         editor.highlight(annotation.start(),
                          annotation.end(),
                          style);
      }
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

}//END OF DocumentAnnotationEditor
