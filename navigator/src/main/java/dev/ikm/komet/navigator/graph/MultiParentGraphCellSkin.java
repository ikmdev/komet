/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.navigator.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

 import dev.ikm.komet.navigator.graph.MultiParentGraphCell;
 import dev.ikm.komet.navigator.graph.MultiParentVertexImpl;
 import javafx.beans.property.DoubleProperty;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.CellSkinBase;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class MultiParentGraphCellSkin extends CellSkinBase<TreeCell<ConceptFacade>> {
    private static final Logger LOG = LoggerFactory.getLogger(MultiParentGraphCellSkin.class);

    /**
     * The amount of space to multiply by the methodTreeItem.level to get the left
     * margin for this tree cell. This is settable from CSS
     */
    private DoubleProperty indent = null;

    public final void setIndent(double value) {
        indentProperty().set(value);
    }

    public final double getIndent() {
        return indent == null ? 10.0 : indent.get();
    }

    public final DoubleProperty indentProperty() {
        if (indent == null) {
            indent = new StyleableDoubleProperty(10.0) {
                @Override
                public Object getBean() {
                    return MultiParentGraphCellSkin.this;
                }

                @Override
                public String getName() {
                    return "indent";
                }

                @Override
                public CssMetaData<TreeCell<?>, Number> getCssMetaData() {
                    return MultiParentGraphCellSkin.StyleableProperties.INDENT;
                }
            };
        }
        return indent;
    }

    private boolean disclosureNodeDirty = true;

    private double fixedCellSize;
    private boolean fixedCellSizeEnabled;

    private final double defaultDisclosureWidth = 18;

    public MultiParentGraphCellSkin(TreeCell control) {
        super(control);
        new MultiParentGraphCellBehavior(control);

        this.fixedCellSize = control.getTreeView().getFixedCellSize();
        this.fixedCellSizeEnabled = fixedCellSize > 0;

        registerChangeListener(control.treeItemProperty(), observableValue -> {
            disclosureNodeDirty = true;
            getSkinnable().requestLayout();
        });
        registerChangeListener(control.textProperty(), observableValue -> {
            getSkinnable().requestLayout();
        });
        registerChangeListener(control.getTreeView().fixedCellSizeProperty(), observableValue -> {
            this.fixedCellSize = getSkinnable().getTreeView().getFixedCellSize();
            this.fixedCellSizeEnabled = fixedCellSize > 0;
        });
    }

    @Override
    protected void layoutChildren(double x, final double y,
                                  double w, final double h) {
        // RT-25876: can not null-check here as this prevents empty rows from
        // being cleaned out.
        // if (methodTreeItem == null) return;

        TreeView<ConceptFacade> tree = getSkinnable().getTreeView();
        if (tree == null) {
            return;
        }
        MultiParentVertexImpl methodTreeItem = (MultiParentVertexImpl) getSkinnable().getTreeItem();

        if (disclosureNodeDirty) {
            updateDisclosureNode();
            disclosureNodeDirty = false;
        }

        Node disclosureNode = getSkinnable().getDisclosureNode();

        int level = tree.getTreeItemLevel(methodTreeItem);
        if (!tree.isShowRoot()) {
            level--;
        }
        double leftMargin = getIndent() * level;
        if (methodTreeItem != null) {
            leftMargin += methodTreeItem.getMultiParentDepth() * getIndent();
        }
        x += leftMargin;

        // position the disclosure node so that it is at the proper indent
        boolean disclosureVisible = disclosureNode != null && methodTreeItem != null && !methodTreeItem.isLeaf();

        double disclosureWidth = defaultDisclosureWidth;

        if (disclosureVisible) {
            if (disclosureNode == null || disclosureNode.getScene() == null) {
                updateChildren();
            }

            if (disclosureNode != null) {
                //disclosureWidth = disclosureNode.prefWidth(h);

                double ph = disclosureNode.prefHeight(disclosureWidth);

                disclosureNode.resize(disclosureWidth, ph);
                positionInArea(disclosureNode, x, y,
                        disclosureWidth, ph, /*baseline ignored*/ 0,
                        HPos.CENTER, VPos.CENTER);
            }
        }

        // determine starting point of the graphic or cell node, and the
        // remaining width available to them
        final int padding = methodTreeItem != null && methodTreeItem.getGraphic() == null ? 0 : 3;
        x += disclosureWidth + padding;
        w -= (leftMargin + disclosureWidth + padding);

        // Rather ugly fix for RT-38519, where graphics are disappearing in
        // certain circumstances
        Node graphic = getSkinnable().getGraphic();
        if (graphic != null && !getChildren().contains(graphic)) {
            getChildren().add(graphic);
        }

        layoutLabelInArea(x, y, w, h);
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double labelWidth = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);

        double pw = snappedLeftInset() + snappedRightInset();

        TreeView<ConceptFacade> tree = getSkinnable().getTreeView();
        if (tree == null) {
            return pw;
        }
        MultiParentVertexImpl methodTreeItem = (MultiParentVertexImpl) getSkinnable().getTreeItem();

        if (methodTreeItem == null) {
            return pw;
        }

        pw = labelWidth;

        // determine the amount of indentation
        int level = tree.getTreeItemLevel(methodTreeItem);
        if (!tree.isShowRoot()) {
            level--;
        }
        pw += getIndent() * level;

        // include the disclosure node width
        Node disclosureNode = getSkinnable().getDisclosureNode();
        double disclosureNodePrefWidth = disclosureNode == null ? 0 : disclosureNode.prefWidth(-1);
        pw += Math.max(defaultDisclosureWidth, disclosureNodePrefWidth);

        return pw;
    }

    private void updateDisclosureNode() {
        if (getSkinnable().isEmpty()) {
            return;
        }

        Node disclosureNode = getSkinnable().getDisclosureNode();
        if (disclosureNode == null) {
            return;
        }
        MultiParentVertexImpl methodTreeItem = (MultiParentVertexImpl) getSkinnable().getTreeItem();

        boolean disclosureVisible = methodTreeItem != null && !methodTreeItem.isLeaf();
        disclosureNode.setVisible(disclosureVisible);

        if (!disclosureVisible) {
            getChildren().remove(disclosureNode);
        } else if (disclosureNode.getParent() == null) {
            getChildren().add(disclosureNode);
            disclosureNode.toFront();
        } else {
            disclosureNode.toBack();
        }

        // RT-26625: [TreeView, TreeTableView] can lose arrows while scrolling
        // RT-28668: Ensemble tree arrow disappears
        if (disclosureNode.getScene() != null) {
            disclosureNode.applyCss();
        }
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        updateDisclosureNode();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (fixedCellSizeEnabled) {
            return fixedCellSize;
        }

        double pref = super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
        Node d = getSkinnable().getDisclosureNode();
        return (d == null) ? pref : Math.max(d.minHeight(-1), pref);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (fixedCellSizeEnabled) {
            return fixedCellSize;
        }

        final MultiParentGraphCell cell = (MultiParentGraphCell) getSkinnable();

        final double pref = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        final Node d = cell.getDisclosureNode();
        final double prefHeight = (d == null) ? pref : Math.max(d.prefHeight(-1), pref);

        // RT-30212: TreeCell does not honor minSize of cells.
        // snapSize for RT-36460
        return snapSize(Math.max(cell.getMinHeight(), prefHeight));
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (fixedCellSizeEnabled) {
            return fixedCellSize;
        }

        return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /***************************************************************************
     *                                                                         *
     *                         Stylesheet Handling                             *
     *                                                                         *
     **************************************************************************/

    /**
     * @treatAsPrivate
     */
    private static class StyleableProperties {

        private static final CssMetaData<TreeCell<?>, Number> INDENT =
                new CssMetaData<TreeCell<?>, Number>("-fx-indent",
                        SizeConverter.getInstance(), 10.0) {

                    @Override
                    public boolean isSettable(TreeCell<?> n) {
                        DoubleProperty p = ((MultiParentGraphCellSkin) n.getSkin()).indentProperty();
                        return p == null || !p.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(TreeCell<?> n) {
                        final MultiParentGraphCellSkin skin = (MultiParentGraphCellSkin) n.getSkin();
                        return (StyleableProperty<Number>) (WritableValue<Number>) skin.indentProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(CellSkinBase.getClassCssMetaData());
            styleables.add(INDENT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}

