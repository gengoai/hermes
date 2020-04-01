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
import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import com.gengoai.swing.ColorUtils;
import com.gengoai.swing.components.FilterableTreeView;
import com.gengoai.swing.listeners.SwingListeners;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The type Tag tree view.
 */
public class TagTreeView2 extends FilterableTreeView {
   private final Map<Tag, DefaultMutableTreeNode> tag2View = new HashMap<>();
   @Getter
   private AnnotationLayer annotationLayer;
   private String[] nonRootTags;
   @NonNull
   private Vector<Consumer<Tag>> selectActions = new Vector<>();
   @NonNull
   private Supplier<Boolean> canPerformShortcut = () -> true;

   /**
    * Instantiates a new Tag tree view.
    */
   public TagTreeView2(AnnotationLayer annotationLayer) {
      super((filter, tagInfo) -> {
         return Cast.<Tag>as(tagInfo).name().toLowerCase().contains(filter.toLowerCase());
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
            }).onKeyPressed(($, e) -> {
         if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            TreePath tp = $.getSelectionPath();
            if(tp != null) {
               DefaultMutableTreeNode node = Cast.as(tp.getLastPathComponent());
               performSelectionTag(Cast.as(node.getUserObject()));
            }
         }
      });
      txtFilter.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                                  SwingListeners.action("DOWN", a -> {
                                     tree.setSelectionRow(0);
                                     tree.requestFocus();
                                  }));
      setAnnotationLayer(annotationLayer);
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
   public Tag getSelectedTagInfo() {
      if(getSelectionPath() != null) {
         return Cast.as(Cast.<DefaultMutableTreeNode>as(getSelectionPath().getLastPathComponent()).getUserObject());
      }
      return null;//tagModel.getTagInfo(getFilterText());
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
   public TagTreeView2 onTagSelect(@NonNull Consumer<Tag> selectFunction) {
      selectActions.add(selectFunction);
      return this;
   }

   private void performSelectionTag(Tag tagInfo) {
      for(Consumer<Tag> selectAction : selectActions) {
         selectAction.accept(tagInfo);
      }
   }

   public void setAnnotationLayer(@NonNull AnnotationLayer layer) {
      this.annotationLayer = layer;
      final Set<Tag> validTags = layer.getValidTags();
      tag2View.clear();
      final Set<Tag> rootNodes = new HashSet<>();
      for(Tag tag : validTags) {
         tag2View.put(tag, new DefaultMutableTreeNode(tag));
         if(tag.parent() == null || !validTags.contains(tag.parent())) {
            rootNodes.add(tag);
         }
      }
      for(Tag tag : validTags) {
         if(tag.parent() != null && validTags.contains(tag.parent())) {
            tag2View.get(tag.parent()).add(tag2View.get(tag));
         }
      }
      final DefaultMutableTreeNode ROOT;
      if(rootNodes.size() == 1) {
         ROOT = tag2View.get(Iterables.getFirst(rootNodes, null));
      } else {
         ROOT = new DefaultMutableTreeNode();
         for(Tag rootNode : rootNodes) {
            ROOT.add(tag2View.get(rootNode));
         }
      }
      setRoot(ROOT);
      setRootVisible(false);
      final DefaultTreeModel model = Cast.as(getModel());
      model.nodeStructureChanged(ROOT);
      for(int i = 0; i < getRowCount(); i++) {
         expandRow(i);
      }
      nonRootTags = validTags.stream().map(Tag::label).sorted().toArray(String[]::new);
      setAutocomplete(annotationLayer.getValidTagLabels());
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
    * The type Custom cell renderer.
    */
   class CustomCellRenderer extends DefaultTreeCellRenderer {

      public Component getTreeCellRendererComponent(JTree tree,
                                                    Object value, boolean sel, boolean expanded, boolean leaf,
                                                    int row, boolean hasFocus) {
         super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
         DefaultMutableTreeNode node = Cast.as(value);
         if(node.getUserObject() instanceof Tag) {
            Tag nodeObj = Cast.as(node.getUserObject());
            if(nodeObj == null) {
               return this;
            }
            setFont(new Font(
                  getFont().getName(),
                  Font.BOLD,
                  getFont().getSize()
            ));

            setIcon(null);
            setIconTextGap(4);
            int indent = 0;
            if(node.getParent() == tree.getModel().getRoot()) {
               setIcon(expanded
                       ? UIManager.getIcon("Tree.expandedIcon")
                       : UIManager.getIcon("Tree.collapsedIcon"));
            } else {
               indent = 1;
            }

            setText("<html>" +
                          Strings.repeat("&nbsp;", indent) +
                          "<span style='background:" +
                          ColorUtils.toHexString(annotationLayer.getColor(nodeObj)) +
                          "'>&nbsp;&nbsp;</span>&nbsp;" +
                          nodeObj.label() +
                          "</html>");
            setHorizontalAlignment(SwingConstants.LEFT);
         }
         return this;
      }

   }

}//END OF TagView
