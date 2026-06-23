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

import dev.ikm.komet.framework.Dialogs;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.Objects;

/**
 * The {@code DragImageMaker} class is responsible for creating a visual representation (image)
 * of a JavaFX {@link Node} that can be used during drag-and-drop operations.
 * <p>It captures a snapshot of the specified node, applies scaling based on the screen's DPI,
 * adds a rounded border to the image, and calculates the appropriate offset for the drag view.
 *
 * <p>Usage Example:
 * <pre>{@code
 * Node someNode = ...; // Initialize your node
 * DragImageMaker dragImageMaker = new DragImageMaker(someNode);
 * Image dragImage = dragImageMaker.getDragImage();
 * double offsetX = dragImageMaker.getDragViewOffsetX();
 * // Use dragImage and offsetX in your drag-and-drop logic
 * }</pre>
 *
 * <p>Note: This class requires that the provided node is part of a scene graph and is attached
 * to a {@link Scene}. If the node is not attached, an error dialog is displayed.
 *
 * @see DraggableWithImage
 * @see Node
 */
public class DragImageMaker implements DraggableWithImage {

    /**
     * The single, authoritative drag-image height (px) for every concept/component drag in the app.
     * Each image produced by {@link #getDragImage()} is rescaled to this height (aspect preserved),
     * regardless of the source node's size — so a concept dragged from a dense field view and the
     * same concept dragged from a card look identical. Adjust here to change drag-glyph size
     * everywhere; do not introduce per-site sizes.
     */
    public static final double STANDARD_DRAG_IMAGE_HEIGHT = 32.0;
    private static final PseudoClass SNAPSHOT_PSEUDO_CLASS = PseudoClass.getPseudoClass("snapshot");

    /**
     * The {@link Node} from which the drag image is generated.
     */
    private final Node node;

    /**
     * The horizontal offset for the drag view, calculated based on the node's bounds.
     */
    private double dragOffset = 0;

    /**
     * Constructs a {@code DragImageMaker} for the specified JavaFX {@link Node}.
     * <p>     * The constructor initializes the instance with the provided node. If the node is {@code null},
     * a {@link NullPointerException} is thrown.
     *
     * @param node the JavaFX node for which the drag image is to be created
     * @throws NullPointerException if the provided node is {@code null}
     */
    public DragImageMaker(Node node) {
        Objects.requireNonNull(node, "The node must not be null.");
        this.node = node;
        this.node.getStyleClass().add("draggable-node");
    }

    @Override
    public Image getDragImage() {
        Objects.requireNonNull(node, "The node must not be null.");

        final Scene scene = node.getScene();
        if (scene == null) {
            // Handle the case where the node is not attached to a scene
            Dialogs.showErrorDialog("Snapshot Error",
                    "Node is not attached to any scene.",
                    "Cannot take a snapshot of the node.");
            return null;
        }

        // Change the pseudo-class state to 'snapshot' (adds the drag-affordance border in CSS).
        node.pseudoClassStateChanged(SNAPSHOT_PSEUDO_CLASS, true);
        node.applyCss();

        // Render the drag image from the node at its UNCONSTRAINED preferred size, not its on-screen
        // (possibly width-constrained, ellipsized) size. A concept dragged from a narrow pane must
        // show its full label and full border — identical to the same concept dragged from a wide
        // list — otherwise the right edge (and the snapshot border) is clipped. The node is briefly
        // resized to its preferred size and restored within this one synchronous call, so the live
        // scene never repaints the resized state and nothing flickers.
        final Region region = (node instanceof Region r) ? r : null;
        final Parent parent = node.getParent();
        final double restoreWidth = region != null ? region.getWidth() : 0;
        final double restoreHeight = region != null ? region.getHeight() : 0;
        if (region != null) {
            final double prefWidth = region.prefWidth(-1);
            final double prefHeight = region.prefHeight(prefWidth);
            region.resize(prefWidth, prefHeight);
            region.layout();
        } else if (parent != null) {
            parent.layout();
        }

        // Use the full visual bounds (boundsInLocal), which include the snapshot border and any
        // effect, so no edge is clipped — layoutBounds would exclude an outset border/effect.
        final Bounds visual = node.getBoundsInLocal();

        // Rescale so every drag image is a STANDARD height regardless of the source node's size —
        // otherwise the image is the size of whatever was dragged (large title rows drag big, tiny
        // axiom rows drag small). Width follows proportionally, preserving the aspect ratio.
        final double targetScale = visual.getHeight() > 0 ? STANDARD_DRAG_IMAGE_HEIGHT / visual.getHeight() : 1.0;
        final int width = (int) Math.max(Math.ceil(visual.getWidth() * targetScale), 1);
        final int height = (int) Math.max(Math.ceil(visual.getHeight() * targetScale), 1);

        // Scale to the standard height, then translate so the (possibly negative) top-left of the
        // visual bounds lands on the image origin — captures borders/effects on every edge.
        final Affine transform = new Affine();
        transform.appendTranslation(-visual.getMinX() * targetScale, -visual.getMinY() * targetScale);
        transform.appendScale(targetScale, targetScale);

        final SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setTransform(transform);
        snapshotParameters.setFill(Color.TRANSPARENT);

        final WritableImage writableImage = new WritableImage(width, height);
        final Image snapshotImage = node.snapshot(snapshotParameters, writableImage);

        node.pseudoClassStateChanged(SNAPSHOT_PSEUDO_CLASS, false);

        // Legacy horizontal offset (used only by the deprecated DraggableWithImage placement path;
        // KonceptDragSource computes its own cursor placement and ignores this).
        dragOffset = Math.max(0, visual.getMinX() * targetScale);

        // Restore the on-screen layout: undo the temporary preferred-size resize.
        if (region != null) {
            region.resize(restoreWidth, restoreHeight);
            region.layout();
        }
        if (parent != null) {
            parent.requestLayout();
        }

        return snapshotImage;
    }

    @Override
    public double getDragViewOffsetX() {
        return dragOffset;
    }
}
