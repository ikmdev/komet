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
package dev.ikm.komet.framework.dnd;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.transform.NonInvertibleTransformException;
import dev.ikm.komet.framework.Dialogs;

/**
 *
 * 
 */
public class DragImageMaker implements DraggableWithImage {

    private double dragOffset = 0;

    Region node;

    public DragImageMaker(Node node) {
        Node originalNode = node;
        while (!(node instanceof Region) && (node != null)) {
            node = node.getParent();
        }
        this.node = (Region) node;
        if (this.node == null) {
            Dialogs.showErrorDialog("Drag error" ,
                    "Can't make drag image of " + originalNode.getClass(),
                    "Can't find region for node: " + originalNode);
            node = new Label("Can't make drag image of " + originalNode.getClass());
        }
    }

    @Override
    public Image getDragImage() {
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        Paint fill = null;
        if (node.getBackground() == null && node.getParent() != null && node.getParent() instanceof Region) {
            Background background = null;
            Region parent = (Region) node.getParent();
            while (background == null
                    && parent != null
                    && parent instanceof Region) {
                background = parent.getBackground();
                if (parent.getParent() instanceof Region) {
                    parent = (Region) parent.getParent();
                } else {
                    parent = null;
                }
            }
            if (background != null) {
                if (!background.getFills().isEmpty()) {
                    BackgroundFill backgroundFill = background.getFills().get(0);
                    fill = backgroundFill.getFill();
                }

            }
        }

        dragOffset = 0;

        // The height difference and width difference are to account for possible
        // changes in size of an object secondary to a hover (which might cause a
        // -fx-effect:  dropshadow... or similar, whicn will create a diference in the
        // tile pane height, but not cause a change in getLayoutBounds()...
        // I don't know if this is a workaround for a bug, or if this is expected
        // behaviour for some reason...
        double layoutWidth = node.getLayoutBounds()
                .getWidth();
        double widthDifference = node.getBoundsInParent()
                .getWidth() - layoutWidth;
        double widthAdjustment = 0;
        if (widthDifference > 0) {
            widthDifference = Math.rint(widthDifference);
            widthAdjustment = widthDifference / 2;
        }

        dragOffset = node.getBoundsInParent()
                .getMinX() + widthAdjustment;
        double width = node.getLayoutBounds().getWidth();// - dragOffset;
        double height = node.getLayoutBounds().getHeight();

        try {
            snapshotParameters.setTransform(node.getLocalToParentTransform().createInverse());
        } catch (NonInvertibleTransformException ex) {
            throw new RuntimeException(ex);
        }
        snapshotParameters.setViewport(new Rectangle2D(0, 0, width, height));
        snapshotParameters.setFill(fill);
        Image snapshotImage = node.snapshot(snapshotParameters, null);
        return snapshotImage;
    }

    @Override
    public double getDragViewOffsetX() {
        return dragOffset;
    }

}
