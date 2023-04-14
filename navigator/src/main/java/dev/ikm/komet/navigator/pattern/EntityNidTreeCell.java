package dev.ikm.komet.navigator.pattern;

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

public class EntityNidTreeCell extends TreeCell<Object>
        implements DraggableWithImage {

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
