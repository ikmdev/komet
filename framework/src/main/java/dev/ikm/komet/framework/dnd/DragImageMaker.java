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
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import javafx.stage.Window;

import java.util.Objects;

/**
 * The {@code DragImageMaker} class is responsible for creating a visual representation (image)
 * of a JavaFX {@link Node} that can be used during drag-and-drop operations.
 * <p>
 * It captures a snapshot of the specified node, applies scaling based on the screen's DPI,
 * adds a rounded border to the image, and calculates the appropriate offset for the drag view.
 * </p>
 *
 * <p>
 * Usage Example:
 * <pre>{@code
 * Node someNode = ...; // Initialize your node
 * DragImageMaker dragImageMaker = new DragImageMaker(someNode);
 * Image dragImage = dragImageMaker.getDragImage();
 * double offsetX = dragImageMaker.getDragViewOffsetX();
 * // Use dragImage and offsetX in your drag-and-drop logic
 * }</pre>
 * </p>
 *
 * <p>
 * Note: This class requires that the provided node is part of a scene graph and is attached
 * to a {@link Scene}. If the node is not attached, an error dialog is displayed.
 * </p>
 *
 * @see DraggableWithImage
 * @see Node
 */
public class DragImageMaker implements DraggableWithImage {

    private static final double STANDARD_DPI = 96.0;

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
     * <p>
     * The constructor initializes the instance with the provided node. If the node is {@code null},
     * a {@link NullPointerException} is thrown.
     * </p>
     *
     * @param node the JavaFX node for which the drag image is to be created
     * @throws NullPointerException if the provided node is {@code null}
     */
    public DragImageMaker(Node node) {
        Objects.requireNonNull(node, "The node must not be null.");
        this.node = node;
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

        // Get the screen DPI where the scene is displayed
        final double screenDpi = getDpiOfScreenForScene(scene);

        // Calculate the scale factor based on the screen DPI
        final double scaleFactor = Math.ceil(screenDpi / STANDARD_DPI);

        // Calculate the node's dimensions with scaling
        final double layoutWidth = node.getLayoutBounds().getWidth();
        final double layoutHeight = node.getLayoutBounds().getHeight();
        final double scaledWidth = layoutWidth * scaleFactor;
        final double scaledHeight = layoutHeight * scaleFactor;

        // Ensure dimensions are at least 1 pixel
        final int width = (int) Math.max(Math.ceil(scaledWidth), 1);
        final int height = (int) Math.max(Math.ceil(scaledHeight), 1);

        // Create SnapshotParameters with scaling
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setTransform(Transform.scale(scaleFactor, scaleFactor));
        snapshotParameters.setFill(Color.TRANSPARENT);

        // Create a WritableImage with the scaled dimensions
        WritableImage writableImage = new WritableImage(width, height);

        // Take the snapshot image of the node
        Image snapshotImage = node.snapshot(snapshotParameters, writableImage);

        // Apply the border with rounded corners to the snapshot image
        Image borderedImage = addRoundedBorderToImage(snapshotImage, 4 * scaleFactor,
                Color.web("#5DCF16") /* -Primary-04 */, 2 * scaleFactor);

        // Calculate dragOffset
        double widthDifference = node.getBoundsInParent().getWidth() - node.getLayoutBounds().getWidth();
        double widthAdjustment = 0;
        if (widthDifference > 0) {
            widthDifference = Math.rint(widthDifference);
            widthAdjustment = widthDifference / 2;
        }
        dragOffset = node.getBoundsInParent().getMinX() + widthAdjustment;

        return borderedImage;
    }

    @Override
    public double getDragViewOffsetX() {
        return dragOffset;
    }

    /**
     * Retrieves the DPI (Dots Per Inch) of the screen where the specified scene is currently displayed.
     *
     * @param scene the JavaFX {@link Scene} for which to determine the screen DPI
     * @return the DPI of the screen, or {@code 96.0} if unable to determine
     */
    private double getDpiOfScreenForScene(Scene scene) {
        // Get the window that contains the scene
        final Window window = scene.getWindow();
        if (window == null) {
            // Handle the case where the scene is not attached to any window
            return STANDARD_DPI; // Return standard DPI as fallback
        }

        // Get the window's position and size
        final double windowX = window.getX();
        final double windowY = window.getY();
        final double windowWidth = window.getWidth();
        final double windowHeight = window.getHeight();

        // Create a rectangle based on the window's position and size
        final Rectangle2D windowBounds = new Rectangle2D(windowX, windowY, windowWidth, windowHeight);

        // Get the screens that intersect with the window bounds
        final ObservableList<Screen> screens = Screen.getScreensForRectangle(windowBounds);
        if (screens.isEmpty()) {
            // Fallback to primary screen DPI
            return Screen.getPrimary().getDpi();
        }

        // Assuming the first screen is the primary one for the window
        final Screen screen = screens.getFirst();
        return screen.getDpi();
    }

    /**
     * Adds a solid rounded border to the provided image.
     *
     * @param image        the original {@link Image} to which the border will be added
     * @param borderSize   the thickness of the border in pixels
     * @param borderColor  the {@link Color} of the border
     * @param borderRadius the radius of the corners for rounding, in pixels
     * @return a new {@link Image} with the rounded border applied
     * @throws NullPointerException if the provided image is {@code null}
     */
    private Image addRoundedBorderToImage(Image image, double borderSize, Color borderColor, double borderRadius) {
        Objects.requireNonNull(image, "The image must not be null.");

        final double originalWidth = image.getWidth();
        final double originalHeight = image.getHeight();

        final double borderedWidth = originalWidth + borderSize * 2;
        final double borderedHeight = originalHeight + borderSize * 2;

        // Create a Canvas to draw the border and the original image
        final Canvas canvas = new Canvas(borderedWidth, borderedHeight);
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fill the background with transparent color
        gc.setFill(Color.TRANSPARENT);
        gc.fillRect(0, 0, borderedWidth, borderedHeight);

        // Draw the rounded border
        gc.setStroke(borderColor);
        gc.setLineWidth(borderSize);
        gc.strokeRoundRect(borderSize / 2.0, borderSize / 2.0,
                originalWidth + borderSize, originalHeight + borderSize, borderRadius, borderRadius);

        // Draw the original image onto the canvas, offset by the border size
        gc.drawImage(image, borderSize, borderSize);

        // Snapshot the canvas to create the bordered image
        final int imageWidth = (int) Math.ceil(borderedWidth);
        final int imageHeight = (int) Math.ceil(borderedHeight);
        final WritableImage borderedImage = new WritableImage(imageWidth, imageHeight);
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        canvas.snapshot(params, borderedImage);

        return borderedImage;
    }
}
