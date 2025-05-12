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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ikm.komet.kview.fxutils.window.CursorMappings.ResizeDirection;

/**
 * Implementation for corner-based resize handles represented as circular hit areas.
 * <p>
 * This class provides resize handles for the corner positions of a resizable component
 * (NW, NE, SE, SW). Each handle is implemented as a Circle shape with appropriate
 * cursor feedback for the corresponding resize direction. When the user interacts
 * with a corner handle, the parent container can determine which corner was grabbed
 * and resize accordingly.
 *
 * @see AbstractResizeHandle
 * @see ResizeHandle
 * @see EdgeResizeHandle
 */
public class CornerResizeHandle extends AbstractResizeHandle {
    private static final Logger LOG = LoggerFactory.getLogger(CornerResizeHandle.class);

    private final Circle circle;
    private final double radius;

    /**
     * Creates a new corner handle for the specified direction.
     *
     * @param direction The resize direction (should be one of NW, NE, SE, SW)
     * @param radius The radius of the corner handle circle in pixels, determining the size
     *               of the hit area for user interaction
     * @param debugMode When true, handles are rendered in red for visual debugging;
     *                  when false, handles are transparent but still interactive
     */
    public CornerResizeHandle(ResizeDirection direction, double radius, boolean debugMode) {
        super(direction);
        this.radius = radius;
        this.circle = createCircleHitArea(debugMode ? Color.RED : Color.TRANSPARENT);
    }

    /**
     * Creates a Circle node to serve as the hit area for this resize handle.
     *
     * @param color The fill color for the circle (typically transparent in production)
     * @return A configured Circle node with the appropriate cursor
     */
    private Circle createCircleHitArea(Color color) {
        Circle hitArea = new Circle(radius, color);
        hitArea.setUserData(getCursor());
        return hitArea;
    }

    @Override
    public void updatePosition(double width, double height) {
        switch (getDirection()) {
            case NW -> {
                circle.setCenterX(0);
                circle.setCenterY(0);
            }
            case NE -> {
                circle.setCenterX(width);
                circle.setCenterY(0);
            }
            case SE -> {
                circle.setCenterX(width);
                circle.setCenterY(height);
            }
            case SW -> {
                circle.setCenterX(0);
                circle.setCenterY(height);
            }
            default -> LOG.warn("Unexpected corner direction: {}", getDirection());
        }
    }

    @Override
    public Node getNode() {
        return circle;
    }
}