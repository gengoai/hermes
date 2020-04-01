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

import com.gengoai.HierarchicalEnumValue;
import com.gengoai.Tag;
import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import com.gengoai.swing.ColorUtils;
import com.gengoai.swing.components.FilterableTreeView;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The type Tag tree view.
 */
public class TagTreeView extends FilterableTreeView {
   private final Map<Tag, DefaultMutableTreeNode> tag2View = new HashMap<>();
   private TagModel tagModel = null;
   private String[] nonRootTags;
   @NonNull
   private Vector<Consumer<TagInfo>> selectActions = new Vector<>();
   @NonNull
   private Supplier<Boolean> canPerformShortcut = () -> true;

   /**
    * Instantiates a new Tag tree view.
    */
   public TagTreeView() {
      super((filter, tagInfo) -> {
         return Cast.<TagInfo>as(tagInfo).getTag().name().toLowerCase().contains(filter.toLowerCase());
      });
      setCellRenderer(new CustomCellRenderer())
            .singleSelectionModel()
            .removeAllKeyListeners()
            .onMouseClicked(($, e) -> {
               if(e.isControlDown()) {
                  TreePath tp = $.getSelectionPath();
                  if(tp != null) {
                     DefaultMutableTreeNode node = Cast.as(tp.getLastPathComponent());
                     performSelectionTag(Cast.as(node.getUserObject()));
                  }
               }
            });
   }

   private DefaultMutableTreeNode createTreeItem(TagInfo tagInfo) {
      var treeItem = new DefaultMutableTreeNode(tagInfo);
      if(tagInfo.getShortcut() != null) {
         KeyboardFocusManager.getCurrentKeyboardFocusManager()
                             .addKeyEventDispatcher(e -> {
                                if(canPerformShortcut.get()) {
                                   KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
                                   if(tagInfo.getShortcut().equals(ks)) {
                                      final DefaultMutableTreeNode node = tag2View.get(tagInfo.getTag());
                                      setSelectionPath(new TreePath(node.getPath()));
                                      performSelectionTag(tagInfo);
                                   }
                                }
                                return false;
                             });
      }
      return treeItem;
   }

   /**
    * Gets node for.
    *
    * @param tagInfo the tag info
    * @return the node for
    */
   public DefaultMutableTreeNode getNodeFor(TagInfo tagInfo) {
      return tag2View.get(tagInfo.getTag());
   }

   /**
    * Gets node for.
    *
    * @param tag the tag
    * @return the node for
    */
   public DefaultMutableTreeNode getNodeFor(Tag tag) {
      return tag2View.get(tag);
   }

   /**
    * Gets selected tag info.
    *
    * @return the selected tag info
    */
   public TagInfo getSelectedTagInfo() {
      if(getSelectionPath() != null) {
         return Cast.as(Cast.<DefaultMutableTreeNode>as(getSelectionPath().getLastPathComponent()).getUserObject());
      }
      return tagModel.getTagInfo(getFilterText());
   }

   /**
    * Gets tag model.
    *
    * @return the tag model
    */
   public TagModel getTagModel() {
      return tagModel;
   }

   /**
    * Get tags string [ ].
    *
    * @return the string [ ]
    */
   public String[] getTags() {
      return nonRootTags;
   }

   /**
    * On tag select tag tree view.
    *
    * @param selectFunction the select function
    * @return the tag tree view
    */
   public TagTreeView onTagSelect(@NonNull Consumer<TagInfo> selectFunction) {
      selectActions.add(selectFunction);
      return this;
   }

   private void performSelectionTag(TagInfo tagInfo) {
      for(Consumer<TagInfo> selectAction : selectActions) {
         selectAction.accept(tagInfo);
      }
   }

   /**
    * Sets can perform shortcut.
    *
    * @param canPerformShortcut the can perform shortcut
    */
   public void setCanPerformShortcut(Supplier<Boolean> canPerformShortcut) {
      this.canPerformShortcut = canPerformShortcut;
   }

   /**
    * Sets tag model.
    *
    * @param newTagModel the new tag model
    */
   public void setTagModel(TagModel newTagModel) {
      this.tagModel = newTagModel;
      updateView();
      setAutocomplete(Arrays.asList(nonRootTags));
   }

   private void updateView() {
      DefaultMutableTreeNode ROOT;
      tag2View.clear();
      final DefaultTreeModel model = Cast.as(getModel());
      if(tagModel.getRoots().size() == 1) {
         TagInfo ti = Iterables.getFirst(tagModel.getRoots(), null);
         ROOT = createTreeItem(ti);
         tag2View.put(ti.getTag(), ROOT);
      } else {
         ROOT = new DefaultMutableTreeNode();
         for(TagInfo root : tagModel.getRoots()) {
            DefaultMutableTreeNode node = createTreeItem(root);
            tag2View.put(root.getTag(), node);
            ROOT.add(node);
         }
      }
      setRoot(ROOT);
      setRootVisible(false);
      final List<String> tags = new ArrayList<>();
      for(TagInfo n : tagModel) {
         if(tag2View.containsKey(n.getTag())) {
            continue;
         }
         Tag p = n.parent();
         while(!tag2View.containsKey(p)) {
            p = Cast.<HierarchicalEnumValue<?>>as(p).parent();
         }
         DefaultMutableTreeNode ti = tag2View.get(p);
         DefaultMutableTreeNode node = createTreeItem(n);
         tag2View.put(n.getTag(), node);
         ti.add(node);
         if(node != ROOT) {
            tags.add(n.toString());
         }
      }
      model.nodeStructureChanged(ROOT);
      for(int i = 0; i < getRowCount(); i++) {
         expandRow(i);
      }
      nonRootTags = tags.toArray(new String[0]);
   }

   /**
    * The type Custom cell renderer.
    */
   static class CustomCellRenderer extends DefaultTreeCellRenderer {
      private Icon createIcon(TagInfo ti) {
         int w = 15;
         int h = 15;
         Font font = new Font(Font.DIALOG, Font.BOLD, 12);
         String shortcut = null;
         if(ti.getShortcut() != null) {
            shortcut = ti.getShortcut().toString().replaceAll("pressed", "");
            w = getFontMetrics(font).stringWidth(shortcut) + 6;
            h = h + 5;
         }

         BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
         var g = image.getGraphics();
         g.setColor(ti.getColor());
         g.fillRect(0, 0, w, h);

         if(Strings.isNotNullOrBlank(shortcut)) {
            g.setFont(font);
            g.setColor(ColorUtils.calculateBestFontColor(ti.getColor()));
            int y = h - font.getSize() / 2 + 1;
            g.drawString(shortcut, 2, y);
         }

         return new ImageIcon(image);
      }

      public Component getTreeCellRendererComponent(JTree tree,
                                                    Object value, boolean sel, boolean expanded, boolean leaf,
                                                    int row, boolean hasFocus) {
         super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
         DefaultMutableTreeNode node = Cast.as(value);
         if(node.getUserObject() instanceof TagInfo) {
            TagInfo nodeObj = Cast.as(node.getUserObject());
            if(nodeObj == null) {
               return this;
            }
            setFont(new Font(
                  getFont().getName(),
                  Font.BOLD,
                  getFont().getSize()
            ));
            setLeafIcon(null);
            setIcon(createIcon(nodeObj));
            setHorizontalAlignment(SwingConstants.CENTER);
         }
         return this;
      }

   }

}//END OF TagView
