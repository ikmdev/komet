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
package dev.ikm.komet.navigator.pattern;

import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.transform.NonInvertibleTransformException;
import dev.ikm.komet.framework.dnd.DragDetectedCellEventHandler;
import dev.ikm.komet.framework.dnd.DragDoneEventHandler;
import dev.ikm.komet.framework.dnd.DraggableWithImage;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.PatternEntity;
import org.eclipse.collections.api.list.ImmutableList;

import java.time.Instant;
import java.util.UUID;

public class EntityNidTreeCell extends TreeCell<Object>
        implements DraggableWithImage {

    static final EntityProxy identifierPatternProxy = EntityProxy.make("Identifier pattern",
            new UUID[] {UUID.fromString("65dd3f06-71ff-5650-8fb3-ce4019e50642")});

    static final EntityProxy inferredDefinitionPatternProxy = EntityProxy.make("Inferred definition pattern",
            new UUID[] {UUID.fromString("9f011812-15c9-5b1b-85f8-bb262bc1b2a2")});

    static final EntityProxy inferredNavigationPatternProxy = EntityProxy.make("Inferred navigation pattern",
            new UUID[] {UUID.fromString("a53cc42d-c07e-5934-96b3-2ede3264474e")});

    static final EntityProxy pathMembershipProxy = EntityProxy.make("Path membership",
            new UUID[] {UUID.fromString("add1db57-72fe-53c8-a528-1614bda20ec6")});

    static final EntityProxy statedDefinitionPatternProxy = EntityProxy.make("Stated definition pattern",
            new UUID[] {UUID.fromString("e813eb92-7d07-5035-8d43-e81249f5b36e")});

    static final EntityProxy statedNavigationPatternProxy = EntityProxy.make("Stated navigation pattern",
            new UUID[] {UUID.fromString("d02957d6-132d-5b3c-adba-505f5778d998")});

     static final EntityProxy ukDialectPatternProxy = EntityProxy.make("UK Dialect Pattern",
            new UUID[] {UUID.fromString("561f817a-130e-5e56-984d-910e9991558c")});

    static final EntityProxy usDialectPatternProxy = EntityProxy.make("US Dialect Pattern",
            new UUID[] {UUID.fromString("08f9112c-c041-56d3-b89b-63258f070074")});

    static final EntityProxy versionControlPathOriginPatternProxy = EntityProxy.make("Version control path origin pattern",
            new UUID[] {UUID.fromString("70f89dd5-2cdb-59bb-bbaa-98527513547c")});





    final ViewProperties viewProperties;
    TilePane graphicTilePane;
    private double dragOffset = 0;

    public EntityNidTreeCell(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        // Allow drags

        this.setOnDragDetected(new DragDetectedCellEventHandler());
        this.setOnDragDone(new DragDoneEventHandler());
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
        if (item != null && !empty) {
            if (item instanceof String stringItem) {
                setContentDisplay(ContentDisplay.TEXT_ONLY);
                setText(stringItem);
            } else if (item instanceof Integer nid) {
                String entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid);
                Entity entity = Entity.getFast(nid);
                Node icon;
                if (entity instanceof PatternEntity) {
                    icon = Icon.PATTERN.makeIcon();
                } else {
                    icon = Icon.PAPER_CLIP.makeIcon();
                }
                if (entity instanceof SemanticEntity<?> semanticEntity) {
                    if (semanticEntity.patternNid() == identifierPatternProxy.nid()) {
                        //TODO Move better string descriptions to language calculator
                        Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
                        ImmutableList fields = latestId.get().fieldValues();
                        entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                ": " + fields.get(1);
                    } else if (semanticEntity.patternNid() == inferredDefinitionPatternProxy.nid()) {
                        entityDescriptionText =
                                "Inferred definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == inferredNavigationPatternProxy.nid()) {
                        entityDescriptionText =
                                "Inferred is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == pathMembershipProxy.nid()) {
                        entityDescriptionText =
                                viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == statedDefinitionPatternProxy.nid()) {
                        entityDescriptionText =
                                "Stated definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == statedNavigationPatternProxy.nid()) {
                        entityDescriptionText =
                                "Stated is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == ukDialectPatternProxy.nid()) {
                        Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                        ImmutableList fields = latestAcceptability.get().fieldValues();
                        entityDescriptionText =
                                "UK dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                        ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == usDialectPatternProxy.nid()) {
                        Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                        ImmutableList fields = latestAcceptability.get().fieldValues();
                        entityDescriptionText =
                                "US dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                        ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == versionControlPathOriginPatternProxy.nid()) {
                        Latest<? extends SemanticEntityVersion> latestPathOrigins = viewProperties.calculator().latest(semanticEntity);
                        ImmutableList fields = latestPathOrigins.get().fieldValues();
                        entityDescriptionText =
                                viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()) +
                                        " origin: " + DateTimeUtil.format((Instant) fields.get(1))  +
                                        " on " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0));
                    }
                }
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                GridPane.setConstraints(icon, 0, 0, 1, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
                Label itemLabel = new Label(entityDescriptionText);
                GridPane.setConstraints(itemLabel, 1, 0, 1, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
                GridPane gridPane = new GridPane();
                gridPane.getChildren().setAll(icon, itemLabel);
                this.setGraphic(gridPane);
            }
        } else {
            setText("");
        }
    }

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
}
