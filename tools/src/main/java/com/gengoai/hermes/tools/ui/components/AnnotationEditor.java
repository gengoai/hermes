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

import com.gengoai.collection.Lists;
import com.gengoai.collection.tree.IntervalTree;
import com.gengoai.collection.tree.Span;
import com.gengoai.string.Strings;
import com.gengoai.swing.components.AutoExpandAction;
import com.gengoai.swing.components.RectanglePainter;
import com.gengoai.swing.components.StyledSpan;
import com.gengoai.swing.listeners.SwingListeners;
import com.gengoai.tuple.IntPair;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;

public class AnnotationEditor extends JComponent {
   public static final AutoExpandAction DEFAULT_AUTO_EXPANSION = (s, e, t, span) -> {
      if(span == null) {
         return Strings.expand(t, s, e);
      }
      return IntPair.of(span.start(), span.end());
   };
   public static final AutoExpandAction NO_AUTO_EXPANSION = (s, e, t, o) -> IntPair.of(s, e);
   private final JTextPane editorPane;
   private final IntervalTree<StyledSpan> range2Style = new IntervalTree<>();
   private Style DEFAULT;
   @NonNull
   private AutoExpandAction autoExpand = DEFAULT_AUTO_EXPANSION;
   private AtomicBoolean keepHighlighting = new AtomicBoolean(false);
   private AtomicInteger minimumEditorHeight = new AtomicInteger(0);

   public AnnotationEditor() {
      super.setBorder(BorderFactory.createEmptyBorder());
      editorPane = with(new JTextPane(new DefaultStyledDocument()), $_ -> {
         $_.setBorder(BorderFactory.createEtchedBorder());
         $_.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
         DEFAULT = $_.addStyle("DEFAULT", null);
         $_.setEditable(false);
         $_.setCharacterAttributes(DEFAULT, true);
         $_.setSelectionColor(Color.WHITE);
         $_.setSelectedTextColor(Color.BLACK);

         $_.setFont(new Font(Font.MONOSPACED, Font.PLAIN, $_.getFont().getSize()));
         $_.setCaret(new DefaultCaret() {
            @Override
            public void setSelectionVisible(boolean visible) {
               super.setSelectionVisible(visible || keepHighlighting.get());
            }
         });
         final AtomicReference<IntPair> selected = new AtomicReference<>(IntPair.of(0, 0));
         final RectanglePainter painter = new RectanglePainter(Color.BLACK);
         $_.addCaretListener(c -> {
            IntPair selection = selected.get();
            if(selection.v1 != selection.v2) {
               $_.getHighlighter().removeAllHighlights();
            }
            if(Strings.isNotNullOrBlank($_.getSelectedText())) {
               selected.set(IntPair.of($_.getSelectionStart(), $_.getSelectionEnd()));
               try {
                  $_.getHighlighter().addHighlight($_.getSelectionStart(), $_.getSelectionEnd(), painter);
               } catch(BadLocationException e) {
                  // pass;
               }
            }
         });
         $_.addKeyListener(SwingListeners.keyReleased(this::zoom));
         $_.addMouseListener(SwingListeners.mouseReleased(e -> {
            if(!e.isAltDown()) {
               autoExpandSelection();
            }
         }));
         $_.addComponentListener(SwingListeners.componentResized(e -> calculateMinHeight()));
      });
      var scrollPane = new JScrollPane(editorPane);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());

      setLayout(new BorderLayout());
      add(scrollPane, BorderLayout.CENTER);
   }

   public void addCaretListener(CaretListener listener) {
      editorPane.addCaretListener(listener);
   }

   @Override
   public synchronized void addFocusListener(FocusListener l) {
      super.addFocusListener(l);
      editorPane.addFocusListener(l);
   }

   @Override
   public synchronized void addMouseListener(MouseListener mouseListener) {
      editorPane.addMouseListener(mouseListener);
   }

   public void addMouseMotionListener(MouseMotionListener listener) {
      editorPane.addMouseMotionListener(listener);
   }

   public void addStyle(String name, Consumer<Style> styleInitializer) {
      styleInitializer.accept(editorPane.addStyle(name, null));
   }

   private void autoExpandSelection() {
      IntPair span = autoExpand.expand(editorPane.getSelectionStart(),
                                       editorPane.getSelectionEnd(),
                                       editorPane.getText(),
                                       getOverlappingStyledSpan(editorPane.getSelectionStart(),
                                                                editorPane.getSelectionEnd()));
      editorPane.setSelectionEnd(span.v2);
      editorPane.setSelectionStart(span.v1);
   }

   private void calculateMinHeight() {
      if(getText().length() == 0) {
         minimumEditorHeight.set(16);
         return;
      }
      AttributedString text = new AttributedString(editorPane.getText());
      FontRenderContext frc = editorPane.getFontMetrics(editorPane.getFont())
                                        .getFontRenderContext();
      AttributedCharacterIterator charIt = text.getIterator();
      LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(charIt, frc);
      float formatWidth = (float) editorPane.getSize().width;
      lineMeasurer.setPosition(charIt.getBeginIndex());
      int noLines = 0;
      while(lineMeasurer.getPosition() < charIt.getEndIndex()) {
         lineMeasurer.nextLayout(formatWidth);
         noLines++;
      }
      noLines += 2;
      int fh = editorPane.getFontMetrics(editorPane.getFont()).getHeight();
      int lineHeight = (int) Math.ceil(fh + (fh * 0.5));
      minimumEditorHeight.set((int) Math.ceil(noLines * lineHeight) + 16);
   }

   public void clearStyle(int start, int end) {
      for(StyledSpan styledSpan : getStyleName(start, end)) {
         range2Style.remove(styledSpan);
         editorPane.getStyledDocument()
                   .setCharacterAttributes(styledSpan.start(), styledSpan.length(), DEFAULT, true);
      }
   }

   @Override
   public JToolTip createToolTip() {
      return editorPane.createToolTip();
   }

   public boolean editorHasFocus() {
      return editorPane.hasFocus();
   }

   public Highlighter getHighlighter() {
      return editorPane.getHighlighter();
   }

   @Override
   public Dimension getMinimumSize() {
      return new Dimension(100, minimumEditorHeight.get());
   }

   public StyledSpan getOverlappingStyledSpan(final int start, final int end) {
      List<StyledSpan> spans = Lists.asArrayList(range2Style.overlapping(Span.of(start, end)));
      return spans.stream()
                  .max((s1, s2) -> {
                     int cmp = Integer.compare(Math.abs(s1.start() - start),
                                               Math.abs(s2.start() - start));
                     if(cmp == 0) {
                        cmp = Integer.compare(s1.length(), s2.length());
                     } else {
                        cmp = -cmp;
                     }
                     return cmp;
                  }).orElse(null);
   }

   public String getSelectedText() {
      return editorPane.getSelectedText();
   }

   public int getSelectionEnd() {
      return editorPane.getSelectionEnd();
   }

   public int getSelectionStart() {
      return editorPane.getSelectionStart();
   }

   public List<StyledSpan> getStyleName(int start, int end) {
      return Lists.asArrayList(range2Style.overlapping(Span.of(start, end)));
   }

   public String getText(int start, int length) throws BadLocationException {
      return editorPane.getText(start, length);
   }

   public String getText() {
      return editorPane.getText();
   }

   public int getTextAtPosition(Point2D point2D) {
      return editorPane.viewToModel2D(point2D);
   }

   public boolean hasSelection() {
      return editorPane.getSelectionStart() < editorPane.getSelectionEnd();
   }

   public void highlight(int start, int end, String style) {
      range2Style.add(new StyledSpan(start, end, style, style));
      editorPane.getStyledDocument()
                .setCharacterAttributes(start, end - start, editorPane.getStyle(style), true);
   }

   public boolean isKeepHighlighting() {
      return keepHighlighting.get();
   }

   public void setAutoExpandAction(@NonNull AutoExpandAction action) {
      this.autoExpand = action;
   }

   @Override
   public void setBorder(Border border) {
      if(editorPane != null) {
         editorPane.setBorder(border);
      }
   }

   @Override
   public void setComponentPopupMenu(JPopupMenu jPopupMenu) {
      editorPane.setComponentPopupMenu(jPopupMenu);
   }

   public void setEditable(boolean isEnabled) {
      editorPane.setEditable(isEnabled);
   }

   @Override
   public void setFont(@NonNull Font font) {
      if(editorPane != null) {
         editorPane.setFont(font);
         calculateMinHeight();
      }
   }

   public void setFontSize(float size) {
      editorPane.setFont(editorPane.getFont().deriveFont(size));
      calculateMinHeight();
   }

   public void setKeepHighlighting(boolean value) {
      keepHighlighting.set(value);
   }

   public void setSelectionRange(int start, int end) {
      editorPane.requestFocus();
      editorPane.select(start, end);
   }

   public void setStyle(int start, int end, String styleName) {
      editorPane.getStyledDocument()
                .setCharacterAttributes(start, end - start, editorPane.getStyle(styleName), true);
   }

   public void setText(String text) {
      range2Style.clear();
      editorPane.setText(text);
   }

   @Override
   public void setToolTipText(String text) {
      editorPane.setToolTipText(text);
   }

   public void updateHighlight(int start, int end, String oldStyle, String newStyle) {
      range2Style.remove(new StyledSpan(start, end, oldStyle, oldStyle));
      highlight(start, end, newStyle);
   }

   private void zoom(KeyEvent e) {
      if(e.isControlDown() && e.getKeyChar() == '+') {
         Font font = editorPane.getFont();
         if(font.getSize() < 24) {
            Font f2 = new Font(font.getName(), font.getStyle(), font.getSize() + 2);
            editorPane.setFont(f2);
            StyleConstants.setFontSize(editorPane.getStyle(StyleContext.DEFAULT_STYLE), f2.getSize());
         }
      } else if(e.isControlDown() && e.getKeyChar() == '-') {
         Font font = editorPane.getFont();
         if(font.getSize() > 12) {
            Font f2 = new Font(font.getName(), font.getStyle(), font.getSize() - 2);
            editorPane.setFont(f2);
         }
      }
      calculateMinHeight();
   }

}//END OF AnnotationEditor
