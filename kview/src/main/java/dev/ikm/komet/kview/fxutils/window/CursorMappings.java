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

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Subscription;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Provides bidirectional mappings and handlers for window resize operations.
 * <p>
 * This utility class implements a singleton enum pattern to provide a centralized registry
 * for mappings between resize directions and their corresponding cursor types, as well as
 * specialized handlers for resize operations.
 *
 * @see ResizeDirection
 * @see WindowSupport
 * @see ResizeHandle
 * @see Subscription
 */
public enum CursorMappings {

    /**
     * Singleton instance of the cursor mappings registry.
     */
    INSTANCE;

    /**
     * Defines the eight cardinal directions for window resizing, plus a NONE value.
     * <p>
     * These directions correspond to the standard window resize handles positions:
     * <ul>
     *   <li>Four corners: NW (northwest), NE (northeast), SE (southeast), SW (southwest)</li>
     *   <li>Four edges: N (north), E (east), S (south), W (west)</li>
     *   <li>NONE: Represents no resize direction (default state)</li>
     * </ul>
     * <p>
     * Each direction maps to a specific cursor type to provide visual feedback during resize
     * operations and to specific resize behaviors when mouse events occur.
     *
     * @see Cursor
     */
    public enum ResizeDirection {
        NONE, NW, N, NE, E, SE, S, SW, W
    }

    /**
     * Maps from resize direction to the appropriate cursor type.
     */
    private static final EnumMap<ResizeDirection, Cursor> DIRECTION_TO_CURSOR = new EnumMap<>(ResizeDirection.class);

    /**
     * Maps from cursor type back to the corresponding resize direction.
     */
    private static final Map<Cursor, ResizeDirection> CURSOR_TO_DIRECTION = new HashMap<>();

    /**
     * Maps from resize direction to the appropriate resize handler function.
     */
    private static final Map<ResizeDirection, BiConsumer<WindowSupport, MouseEvent>> RESIZE_HANDLERS = new EnumMap<>(ResizeDirection.class);

    static {
        // Initialize bidirectional mappings
        mapDirectionAndCursor(ResizeDirection.NW, Cursor.NW_RESIZE);
        mapDirectionAndCursor(ResizeDirection.N, Cursor.N_RESIZE);
        mapDirectionAndCursor(ResizeDirection.NE, Cursor.NE_RESIZE);
        mapDirectionAndCursor(ResizeDirection.E, Cursor.E_RESIZE);
        mapDirectionAndCursor(ResizeDirection.SE, Cursor.SE_RESIZE);
        mapDirectionAndCursor(ResizeDirection.S, Cursor.S_RESIZE);
        mapDirectionAndCursor(ResizeDirection.SW, Cursor.SW_RESIZE);
        mapDirectionAndCursor(ResizeDirection.W, Cursor.W_RESIZE);
        // NONE doesn't have a corresponding cursor, default cursor is used

        // Initialize resize handlers
        RESIZE_HANDLERS.put(ResizeDirection.NW, (ws, e) -> {
            ws.resizeNorth(e);
            ws.resizeWest(e);
        });
        RESIZE_HANDLERS.put(ResizeDirection.N, WindowSupport::resizeNorth);
        RESIZE_HANDLERS.put(ResizeDirection.NE, (ws, e) -> {
            ws.resizeNorth(e);
            ws.resizeEast(e);
        });
        RESIZE_HANDLERS.put(ResizeDirection.E, WindowSupport::resizeEast);
        RESIZE_HANDLERS.put(ResizeDirection.SE, (ws, e) -> {
            ws.resizeSouth(e);
            ws.resizeEast(e);
        });
        RESIZE_HANDLERS.put(ResizeDirection.S, WindowSupport::resizeSouth);
        RESIZE_HANDLERS.put(ResizeDirection.SW, (ws, e) -> {
            ws.resizeSouth(e);
            ws.resizeWest(e);
        });
        RESIZE_HANDLERS.put(ResizeDirection.W, WindowSupport::resizeWest);

        // Add a no-op handler for NONE direction
        RESIZE_HANDLERS.put(ResizeDirection.NONE, (ws, e) -> {
            // No resize action needed
        });
    }

    /**
     * Adds a bidirectional mapping between a resize direction and cursor type.
     * <p>
     * This method populates both the direction-to-cursor and cursor-to-direction
     * maps to enable efficient lookups in either direction.
     *
     * @param direction The resize direction to map
     * @param cursor    The cursor type to associate with the direction
     */
    private static void mapDirectionAndCursor(ResizeDirection direction, Cursor cursor) {
        DIRECTION_TO_CURSOR.put(direction, cursor);
        CURSOR_TO_DIRECTION.put(cursor, direction);
    }

    /**
     * Returns the appropriate cursor for a given resize direction.
     * <p>
     * This method is used to determine which cursor should be displayed when
     * the mouse is over a specific type of resize handle or during a resize
     * operation in progress.
     *
     * @param direction The resize direction
     * @return The corresponding resize cursor, or DEFAULT cursor for NONE direction
     */
    public static Cursor getCursor(ResizeDirection direction) {
        return direction == ResizeDirection.NONE ? Cursor.DEFAULT : DIRECTION_TO_CURSOR.get(direction);
    }

    /**
     * Determines the resize direction that corresponds to a given cursor type.
     * <p>
     * This reverse mapping is useful when processing mouse events where the cursor
     * type is known and the appropriate resize behavior needs to be determined.
     *
     * @param cursor The cursor to look up
     * @return The corresponding resize direction, or NONE if not a resize cursor
     */
    public static ResizeDirection getDirection(Cursor cursor) {
        return CURSOR_TO_DIRECTION.getOrDefault(cursor, ResizeDirection.NONE);
    }

    /**
     * Determines if the given direction represents a corner resize operation.
     * <p>
     * Corner resize operations affect both width and height simultaneously,
     * while edge operations affect only one dimension.
     *
     * @param direction The direction to check
     * @return true if the direction is one of NW, NE, SE, or SW (corner directions)
     */
    public static boolean isCorner(ResizeDirection direction) {
        return EnumSet.of(ResizeDirection.NW, ResizeDirection.NE,
                        ResizeDirection.SE, ResizeDirection.SW)
                .contains(direction);
    }

    /**
     * Returns the set of all edge (non-corner) resize directions.
     * <p>
     * Edge directions represent resize operations that affect only one dimension
     * (either width or height, but not both).
     *
     * @return A set containing N, E, S, W directions
     */
    public static Set<ResizeDirection> getEdgeDirections() {
        return EnumSet.of(ResizeDirection.N, ResizeDirection.E,
                ResizeDirection.S, ResizeDirection.W);
    }

    /**
     * Returns the set of all corner resize directions.
     * <p>
     * Corner directions represent resize operations that affect both width
     * and height simultaneously.
     *
     * @return A set containing NW, NE, SE, SW directions
     */
    public static Set<ResizeDirection> getCornerDirections() {
        return EnumSet.of(ResizeDirection.NW, ResizeDirection.NE,
                ResizeDirection.SE, ResizeDirection.SW);
    }

    /**
     * Returns a set of all valid resize directions (excluding NONE).
     * <p>
     * This method returns both edge and corner directions, which is useful
     * when iterating through all possible resize handles.
     *
     * @return A set of all valid resize directions
     */
    public static Set<ResizeDirection> getAllDirections() {
        return EnumSet.complementOf(EnumSet.of(ResizeDirection.NONE));
    }

    /**
     * Executes the appropriate resize operation based on the specified direction.
     * <p>
     * This method selects and executes the appropriate resize handler based on the
     * provided direction. For corner directions (like NW), it combines multiple
     * resize operations (e.g., North and West) to provide the expected diagonal
     * resize behavior.
     *
     * @param direction The resize direction determining which resize operation to perform
     * @param support   The WindowSupport instance that will execute the resize
     * @param event     The mouse event containing position information for the resize
     */
    public static void handleResize(ResizeDirection direction, WindowSupport support, MouseEvent event) {
        if (direction == null || support == null || event == null) {
            return;
        }

        BiConsumer<WindowSupport, MouseEvent> handler = RESIZE_HANDLERS.get(direction);
        if (handler != null) {
            handler.accept(support, event);
        }
    }

    /**
     * Creates a subscription for cursor changes based on mouse interactions with a node.
     * <p>
     * This method sets up mouse enter/exit event handlers that change the scene's cursor
     * when the mouse enters or exits the specified node.
     *
     * @param node      The node to manage cursor changes for
     * @param direction The resize direction that determines which cursor to display
     * @return A Subscription that can be used to unregister the cursor handlers
     */
    public static Subscription createCursorSubscription(Node node, ResizeDirection direction) {
        if (node == null) {
            return Subscription.EMPTY;
        }

        Cursor cursor = getCursor(direction);

        EventHandler<MouseEvent> enterHandler = e -> {
            if (node.getScene() != null) {
                node.getScene().setCursor(cursor);
            }
        };

        EventHandler<MouseEvent> exitHandler = e -> {
            if (node.getScene() != null) {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
        };

        node.addEventHandler(MouseEvent.MOUSE_ENTERED, enterHandler);
        node.addEventHandler(MouseEvent.MOUSE_EXITED, exitHandler);

        return () -> {
            node.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterHandler);
            node.removeEventHandler(MouseEvent.MOUSE_EXITED, exitHandler);
        };
    }
}