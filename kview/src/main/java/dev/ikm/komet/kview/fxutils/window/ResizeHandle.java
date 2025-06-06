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
package dev.ikm.komet.kview.fxutils.window;

import javafx.scene.Node;

/**
 * Interface for resize handle components that encapsulates their visual appearance and behavior.
 * <p>
 * Resize handles provide the user interface elements that allow users to resize windows
 * or panels by dragging from different positions (corners and edges). This interface
 * defines the contract that all resize handle implementations must fulfill, regardless
 * of their visual representation or specific behavior.
 * <p>
 * Typical implementations include:
 * <ul>
 *   <li>{@code CornerResizeHandle} - For corners (NW, NE, SE, SW), usually represented as circles</li>
 *   <li>{@code EdgeResizeHandle} - For edges (N, E, S, W), usually represented as lines</li>
 * </ul>
 *
 * @see AbstractResizeHandle
 * @see CornerResizeHandle
 * @see EdgeResizeHandle
 * @see CursorMappings.ResizeDirection
 */
public interface ResizeHandle {
    /**
     * Updates the position of this handle based on the current window dimensions.
     * <p>
     * This method should position the handle appropriately, according to its resize
     * direction and the window's current size.
     *
     * @param width The current width of the window or panel
     * @param height The current height of the window or panel
     */
    void updatePosition(double width, double height);

    /**
     * Returns the underlying node that renders this resize handle.
     * <p>
     * This node is what gets added to the scene graph to represent the handle visually.
     * The node should have appropriate event handlers attached to it (typically in the
     * implementing class) to handle mouse interactions for resizing.
     *
     * @return the node (Circle, Line, etc.) representing this handle
     */
    Node getNode();

    /**
     * Returns the resize direction this handle represents.
     * <p>
     * Each handle controls resizing in a specific direction.
     * This direction determines both the handle's positioning and the
     * type of resize operation that occurs when the user interacts with it.
     *
     * @return The resize direction enum value associated with this handle
     * @see CursorMappings.ResizeDirection
     */
    CursorMappings.ResizeDirection getDirection();

    /**
     * Returns the z-order priority of this handle for proper layering in the scene graph.
     * <p>
     * Corner handles (NW, NE, SE, SW) are assigned a higher priority (1) to be drawn on top
     * of edge handles (N, E, S, W), which have a lower priority (0). This ensures that when
     * a corner and edge handle might overlap visually, the corner handle remains accessible.
     *
     * @return the z-order value (higher values are drawn on top of lower values)
     */
    default int getZOrder() {
        return CursorMappings.isCorner(getDirection()) ? 1 : 0;
    }
}