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
import com.gengoai.swing.Colors;
import com.gengoai.swing.component.view.MangoFilteredTreeView;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.tree.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.gengoai.swing.component.listener.SwingListeners.mouseClicked;

/**
 * The type Tag tree view.
 */
public class TagTreeView extends MangoFilteredTreeView {
   private final Map<Tag, DefaultMutableTreeNode> tag2View = new HashMap<>();
   @Getter
   private AnnotationLayer annotationLayer;

   /**
    * Instantiates a new Tag tree view.
    *
    * @param annotationLayer the annotation layer
    */
   public TagTreeView(@NonNull AnnotationLayer annotationLayer) {
      super((filter, tagInfo) -> {
         return Cast.<Tag>as(tagInfo).name().toLowerCase().contains(filter.toLowerCase());
      });
      setCellRenderer(new CustomCellRenderer());
      getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      addMouseListener(mouseClicked(this::onMouseClick));
      setAnnotationLayer(annotationLayer);
   }

   private void onMouseClick(MouseEvent e) {
      if(e.getButton() == 1) {
         TreePath tp = getSelectionPath();
         if(tp != null) {
            DefaultMutableTreeNode node = Cast.as(tp.getLastPathComponent());
            fireItemSelection(node.getUserObject());
         }
      }
   }

   /**
    * Sets annotation layer.
    *
    * @param layer the layer
    */
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
      setAutocomplete(annotationLayer.getValidTagLabels());
   }

   private class CustomCellRenderer extends DefaultTreeCellRenderer {

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
                          Colors.toHexString(annotationLayer.getColor(nodeObj)) +
                          "'>&nbsp;&nbsp;</span>&nbsp;" +
                          nodeObj.label() +
                          "</html>");
            setHorizontalAlignment(SwingConstants.LEFT);
         }
         return this;
      }

   }

}//END OF TagView
