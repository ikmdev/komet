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
import javafx.scene.shape.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ikm.komet.kview.fxutils.window.CursorMappings.ResizeDirection;

/**
 * Implementation for edge-based resize handles represented as line segments.
 * <p>
 * This class provides resize handles for the edge positions of a resizable component
 * (N, E, S, W). Each handle is implemented as a Line shape positioned along the
 * corresponding edge of the parent container with the appropriate cursor feedback.
 *
 * @see AbstractResizeHandle
 * @see CornerResizeHandle
 * @see ResizeHandle
 * @see CursorMappings.ResizeDirection
 */
public class EdgeResizeHandle extends AbstractResizeHandle {

    private static final Logger LOG = LoggerFactory.getLogger(EdgeResizeHandle.class);

    /**
     * The line shape representing this-edge handle's interactive area.
     */
    private final Line line;

    /**
     * Creates a new edge handle for the specified direction.
     *
     * @param direction The resize direction (should be one of N, E, S, W)
     * @param hitArea   The width in pixels of the hit area for this handle, determining
     *                  how easy it is for the user to grab the edge for resizing
     * @param debugMode When true, handles are rendered in red for visual debugging;
     *                  when false, handles are transparent but still interactive
     */
    public EdgeResizeHandle(ResizeDirection direction, double hitArea, boolean debugMode) {
        super(direction);
        this.line = createHitAreaLine(hitArea, debugMode ? Color.ORANGERED : Color.TRANSPARENT);
    }

    /**
     * Creates a Line node configured as the hit area for this resize handle.
     *
     * @param strokeWidth The width of the line stroke in pixels
     * @param strokeColor The stroke color for the line (typically transparent in production)
     * @return A configured Line node with appropriate visual and interaction properties
     */
    private Line createHitAreaLine(double strokeWidth, Color strokeColor) {
        Line hitArea = new Line();
        hitArea.setStrokeWidth(strokeWidth);
        hitArea.setStroke(strokeColor);
        hitArea.setUserData(getCursor());
        return hitArea;
    }

    @Override
    public void updatePosition(double width, double height) {
        switch (getDirection()) {
            case N -> {
                line.setStartX(0);
                line.setStartY(0);
                line.setEndX(width);
                line.setEndY(0);
            }
            case E -> {
                line.setStartX(width);
                line.setStartY(0);
                line.setEndX(width);
                line.setEndY(height);
            }
            case S -> {
                line.setStartX(0);
                line.setStartY(height);
                line.setEndX(width);
                line.setEndY(height);
            }
            case W -> {
                line.setStartX(0);
                line.setStartY(0);
                line.setEndX(0);
                line.setEndY(height);
            }
            default -> LOG.warn("Unexpected edge direction: {}", getDirection());
        }
    }

    @Override
    public Node getNode() {
        return line;
    }
}