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

import javafx.scene.image.Image;

/**
 * The {@code DraggableWithImage} interface defines a contract for draggable entities that provide
 * a visual representation (image) during drag-and-drop operations.
 * <p>
 * Implementing this interface allows an object to supply an image that visually represents it
 * when it is being dragged. Additionally, it provides an offset value to correctly position
 * the drag image relative to the cursor.
 * </p>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>{@code
 * public class DraggableItem implements DraggableWithImage {
 *
 *     private Image dragImage;
 *     private double dragOffsetX;
 *
 *     public DraggableItem() {
 *         // Initialize dragImage and dragOffsetX as needed
 *     }
 *
 *     @Override
 *     public Image getDragImage() {
 *         return this.dragImage;
 *     }
 *
 *     @Override
 *     public double getDragViewOffsetX() {
 *         return this.dragOffsetX;
 *     }
 * }
 * }</pre>
 * </p>
 *
 * <p>
 * This interface is particularly useful in GUI applications where visual feedback during
 * drag-and-drop enhances user experience. By providing a custom image and offset, developers
 * can ensure that the drag operation is intuitive and visually consistent.
 * </p>
 *
 * @see DragImageMaker
 * @see javafx.scene.input.TransferMode
 */
public interface DraggableWithImage {

    /**
     * Retrieves the image to be used as the visual representation during a drag operation.
     * <p>
     * This image is typically displayed under the cursor or at a specific offset when the
     * user initiates a drag-and-drop action. It should accurately represent the draggable
     * entity to provide clear visual feedback.
     * </p>
     *
     * @return an {@link Image} object representing the drag image. This image should not be
     * {@code null} and should be properly sized to fit the draggable context.
     * @throws IllegalStateException if the drag image cannot be generated or is unavailable
     */
    Image getDragImage();

    /**
     * Retrieves the horizontal offset to be applied to the drag image relative to the cursor.
     * <p>
     * This offset ensures that the drag image is positioned correctly during the drag operation.
     * It accounts for any discrepancies between the cursor's position and the desired location
     * of the drag image to provide a seamless user experience.
     * </p>
     *
     * @return a {@code double} value representing the horizontal offset in pixels. This offset
     * is added to the X-coordinate of the drag image's position.
     */
    double getDragViewOffsetX();
}
