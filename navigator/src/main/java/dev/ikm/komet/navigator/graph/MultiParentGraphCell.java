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

import dev.ikm.komet.framework.MenuItemWithText;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.dnd.DragDetectedCellEventHandler;
import dev.ikm.komet.framework.dnd.DragDoneEventHandler;
import dev.ikm.komet.framework.dnd.DraggableWithImage;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.TilePane;
import javafx.scene.transform.NonInvertibleTransformException;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;

import static dev.ikm.komet.framework.StyleClasses.MULTI_PARENT_TREE_CELL;

/**
 * A {@link TreeCell} for rendering {@link ConceptEntity < ConceptEntityVersion >} objects.
 */
final public class MultiParentGraphCell
        extends TreeCell<ConceptFacade>
        implements DraggableWithImage {
    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MultiParentGraphCell.class);

    //~--- fieldValues --------------------------------------------------------------

    private double dragOffset = 0;
    private TilePane graphicTilePane;
    private String conceptDescriptionText; // Cached to speed up updates

    //~--- constructors --------------------------------------------------------

    MultiParentGraphCell(TreeView<ConceptFacade> treeView) {
        super();
        this.getStyleClass().add(MULTI_PARENT_TREE_CELL.toString());
        updateTreeView(treeView);
        setSkin(new MultiParentGraphCellSkin(this));

        // Allow drags

        this.setOnDragDetected(new DragDetectedCellEventHandler());
        this.setOnDragDone(new DragDoneEventHandler());

    }

    //~--- methods -------------------------------------------------------------

    @Override
    protected void updateItem(ConceptFacade concept, boolean empty) {
        // Handle right-clicks.7c21b6c5-cf11-5af9-893b-743f004c97f5

        //profiling showed set context menu very slow. Maybe only set on right click...
        //setContextMenu(buildContextMenu(concept));

        try {
            super.updateItem(concept, empty);

            if (empty) {
                setText("");
                conceptDescriptionText = null;
                setGraphic(null);
                this.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, false);
            } else {
                final MultiParentVertexImpl treeItem = (MultiParentVertexImpl) getTreeItem();
                conceptDescriptionText = treeItem.toString();

                try {
                    if (!treeItem.isLeaf()) {
                        Node iv = treeItem.isExpanded() ? Icon.TAXONOMY_CLICK_TO_CLOSE.makeIcon()
                                : Icon.TAXONOMY_CLICK_TO_OPEN.makeIcon();

                        setDisclosureNode(iv);
                    }
                } catch (IllegalStateException e) {
                    LOG.error("IllegalStateException checking leaf", e);
                }
                if (concept != null) {
                    boolean conceptActive = treeItem.getGraphController().getObservableView().calculator().isLatestActive(concept);
                    this.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, !conceptActive);
                    setText(conceptDescriptionText);
                    setGraphic(treeItem.computeGraphic());
                }
            }
        } catch (Exception e) {
            LOG.error("Unexpected error updating cell", e);
            setText("Internal error!");
            setGraphic(null);
        }
    }

    private ContextMenu buildContextMenu(ConceptEntity concept) {
        if (concept != null) {
            MultiParentVertexImpl graphItem = (MultiParentVertexImpl) getTreeItem();
            MultiParentGraphViewController graphView = graphItem.getGraphController();
            ObservableView view = graphView.getObservableView();

            ContextMenu cm = new ContextMenu();
            MenuItem item1 = new MenuItemWithText("About " + view.calculator().getDescriptionTextOrNid(concept.nid()));

            item1.setOnAction(
                    (ActionEvent e) -> {
                        int conceptNid = ((MultiParentVertexImpl) getTreeItem()).getConceptNid();
                        ObservableView manifold = ((MultiParentVertexImpl) getTreeItem()).getGraphController().getObservableView();
                        graphItem.getValue();
                    });

            MenuItem item2 = new MenuItemWithText("Preferences");

            item2.setOnAction(
                    (ActionEvent e) -> {
                        LOG.debug("Preferences");
                    });
            cm.getItems()
                    .addAll(item1, item2);
            return cm;
        }
        return null;
    }

    protected void openOrCloseParent(MultiParentVertexImpl treeItem) {
        ConceptFacade value = treeItem.getValue();

        if (value != null) {
            treeItem.setValue(null);

            MultiParentVertexImpl parentItem = (MultiParentVertexImpl) treeItem.getParent();
            ObservableList<TreeItem<ConceptFacade>> siblings = parentItem.getChildren();

            if (treeItem.isSecondaryParentOpened()) {
                removeExtraParents(treeItem, siblings);
            } else {
                ImmutableCollection<Edge> allParents = treeItem.getGraphController()
                        .getNavigator()
                        .getParentEdges(value.nid());
                ArrayList<MultiParentVertexImpl> secondaryParentItems = new ArrayList<>();

                for (Edge parentLink : allParents) {
                    if ((allParents.size() == 1) || (parentLink.destinationNid() != parentItem.getValue().nid())) {
                        ConceptEntity parentChronology = Entity.getFast(parentLink.destinationNid());
                        MultiParentVertexImpl extraParentItem = new MultiParentVertexImpl(parentChronology, treeItem.getGraphController(), parentLink.typeNids(), null);
                        ObservableView observableView = treeItem.getGraphController().getObservableView();
                        extraParentItem.setDefined(observableView.calculator().hasSufficientSet(parentChronology));
                        extraParentItem.setMultiParentDepth(treeItem.getMultiParentDepth() + 1);
                        secondaryParentItems.add(extraParentItem);
                    }
                }

                Collections.sort(secondaryParentItems);
                Collections.reverse(secondaryParentItems);

                int startIndex = siblings.indexOf(treeItem);

                for (MultiParentVertexImpl extraParentItem : secondaryParentItems) {
                    parentItem.getChildren()
                            .add(startIndex++, extraParentItem);
                    treeItem.getExtraParents()
                            .add(extraParentItem);
                }
            }

            treeItem.setValue(value);
            treeItem.setSecondaryParentOpened(!treeItem.isSecondaryParentOpened());
            treeItem.computeGraphic();
        }
    }

    private void removeExtraParents(MultiParentVertexImpl treeItem,
                                    ObservableList<TreeItem<ConceptFacade>> siblings) {
        treeItem.getExtraParents().stream().map((extraParent) -> {
            removeExtraParents(extraParent, siblings);
            return extraParent;
        }).forEachOrdered((extraParent) -> {
            siblings.remove(extraParent);
        });
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    public Image getDragImage() {
        //TODO see if we can replace this method with DragImageMaker...
        SnapshotParameters snapshotParameters = new SnapshotParameters();

        dragOffset = 0;

        double width = this.getWidth();
        double height = this.getHeight();

        if (graphicTilePane != null) {
            // The height difference and width difference are to account for possible
            // changes in size of an object secondary to a hover (which might cause a
            // -fx-effect:  dropshadow... or similar, whicn will create a difference in the
            // tile pane height, but not cause a change in getLayoutBounds()...
            // I don't know if this is a workaround for a bug, or if this is expected
            // behaviour for some reason...

            double layoutWidth = graphicTilePane.getLayoutBounds()
                    .getWidth();
            double widthDifference = graphicTilePane.getBoundsInParent()
                    .getWidth() - layoutWidth;
            double widthAdjustment = 0;
            if (widthDifference > 0) {
                widthDifference = Math.rint(widthDifference);
                widthAdjustment = widthDifference / 2;
            }

            dragOffset = graphicTilePane.getBoundsInParent()
                    .getMinX() + widthAdjustment;
            width = this.getWidth() - dragOffset;
            height = this.getLayoutBounds().getHeight();
        }

        try {
            snapshotParameters.setTransform(this.getLocalToParentTransform().createInverse());
        } catch (NonInvertibleTransformException ex) {
            throw new RuntimeException(ex);
        }
        snapshotParameters.setViewport(new Rectangle2D(dragOffset - 2, 0, width, height));
        return snapshot(snapshotParameters, null);
    }

    @Override
    public double getDragViewOffsetX() {
        return dragOffset;
    }


    @Override
    public String toString() {
        if (conceptDescriptionText == null) {
            MultiParentVertexImpl treeItem = (MultiParentVertexImpl) getTreeItem();
            if (treeItem != null) {
                conceptDescriptionText = treeItem.toString();
            } else {
                conceptDescriptionText = "null tree item";
            }
        }
        return conceptDescriptionText;
    }


}

