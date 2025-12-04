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
 * Implementation for corner-based resize handles represented as circular regions.
 * <p>
 * This class provides resize handles for the corner positions of a resizable component
 * (NW, NE, SE, SW). Each handle is implemented as a Circle shape positioned in the
 * corresponding corner of the parent container with the appropriate cursor feedback.
 *
 * @see AbstractResizeHandle
 * @see EdgeResizeHandle
 * @see ResizeHandle
 * @see CursorMappings.ResizeDirection
 */
public class CornerResizeHandle extends AbstractResizeHandle {

    private static final Logger LOG = LoggerFactory.getLogger(CornerResizeHandle.class);

    /**
     * The circle shape representing this corner handle's interactive area.
     */
    private final Circle circle;

    /**
     * Creates a new corner handle for the specified direction.
     *
     * @param direction The resize direction (should be one of NW, NE, SE, SW)
     * @param radius    The radius in pixels of the circular hit area for this handle,
     *                  determining how easy it is for the user to grab the corner for resizing
     * @param debugMode When true, handles are rendered in red for visual debugging;
     *                  when false, handles are transparent but still interactive
     */
    public CornerResizeHandle(ResizeDirection direction, double radius, boolean debugMode) {
        super(direction);
        this.circle = createHitAreaCircle(radius, debugMode ? Color.ORANGERED : Color.TRANSPARENT);
    }

    /**
     * Creates a Circle node configured as the hit area for this resize handle.
     *
     * @param radius The radius of the circle in pixels
     * @param fillColor The fill color for the circle (typically transparent in production)
     * @return A configured Circle node with appropriate visual and interaction properties
     */
    private Circle createHitAreaCircle(double radius, Color fillColor) {
        Circle hitArea = new Circle(radius);
        hitArea.setFill(fillColor);
        hitArea.setStroke(Color.TRANSPARENT);
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