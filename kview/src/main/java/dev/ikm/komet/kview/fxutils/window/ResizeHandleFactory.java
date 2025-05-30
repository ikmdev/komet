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

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

import static dev.ikm.komet.kview.fxutils.window.CursorMappings.ResizeDirection;
import static dev.ikm.komet.kview.fxutils.window.CursorMappings.getCornerDirections;
import static dev.ikm.komet.kview.fxutils.window.CursorMappings.getEdgeDirections;

/**
 * Utility for creating and managing window resize handles.
 * <p>
 * This class provides functionality to create and configure {@link ResizeHandle}
 * instances for window resizing operations. It centralizes the creation logic and
 * configuration parameters for both edge handles (N, E, S, W) and corner handles
 * (NW, NE, SE, SW).
 * <p>
 * The class allows for consistent configuration across all resize handles by
 * specifying common properties once at initialization:
 * <ul>
 *   <li>Edge hit area width - controls the thickness of edge resize handles</li>
 *   <li>Corner hit area radius - controls the size of corner resize handles</li>
 *   <li>Debug mode - determines whether handles are visible (for development)</li>
 * </ul>
 *
 * @see ResizeHandle
 * @see EdgeResizeHandle
 * @see CornerResizeHandle
 * @see CursorMappings.ResizeDirection
 */
public class ResizeHandleFactory {

    private final double edgeHitArea;
    private final double cornerHitArea;
    private final boolean debugMode;

    /**
     * Creates a new factory with the specified configuration parameters.
     *
     * @param edgeHitArea   The hit area width in pixels for edge handles (N, E, S, W)
     * @param cornerHitArea The radius in pixels for corner handles (NW, NE, SE, SW)
     * @param debugMode     When true, handles are rendered with red color for visibility
     *                      during development; when false, handles are transparent but
     *                      still interactive for production use
     */
    public ResizeHandleFactory(double edgeHitArea, double cornerHitArea, boolean debugMode) {
        this.edgeHitArea = edgeHitArea;
        this.cornerHitArea = cornerHitArea;
        this.debugMode = debugMode;
    }

    /**
     * Creates a complete set of resize handles for all supported directions.
     * <p>
     * This convenience method creates all eight standard resize handles (four edges
     * and four corners) with a consistent configuration. The handles are mapped by
     * their direction for easy retrieval and positioning.
     *
     * @return A map of resize handles indexed by their direction
     */
    public MutableMap<ResizeDirection, ResizeHandle> createAllHandles() {
        MutableMap<ResizeDirection, ResizeHandle> handles = Maps.mutable.empty();

        // Create edge handles
        for (ResizeDirection direction : getEdgeDirections()) {
            handles.put(direction, createEdgeHandle(direction));
        }

        // Create corner handles
        for (ResizeDirection direction : getCornerDirections()) {
            handles.put(direction, createCornerHandle(direction));
        }

        return handles;
    }

    /**
     * Creates an edge handle for the specified direction.
     *
     * @param direction The resize direction for the edge handle
     * @return A new edge handle configured with this factory's settings
     * @see EdgeResizeHandle
     */
    public ResizeHandle createEdgeHandle(ResizeDirection direction) {
        return new EdgeResizeHandle(direction, edgeHitArea, debugMode);
    }

    /**
     * Creates a corner handle for the specified direction.
     *
     * @param direction The resize direction for the corner handle
     * @return A new corner handle configured with this factory's settings
     * @see CornerResizeHandle
     */
    public ResizeHandle createCornerHandle(ResizeDirection direction) {
        return new CornerResizeHandle(direction, cornerHitArea, debugMode);
    }
}