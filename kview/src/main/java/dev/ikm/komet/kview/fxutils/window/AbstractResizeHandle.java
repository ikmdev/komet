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

import javafx.scene.Cursor;

import static dev.ikm.komet.kview.fxutils.window.CursorMappings.ResizeDirection;

/**
 * Base class for resize handle implementations that provides common functionality.
 * <p>
 * This class serves as the foundation for concrete resize handle implementations by
 * providing storage for the resize direction and common utility methods such as cursor
 * mapping. It implements the core behaviors required by the {@link ResizeHandle}
 * interface while allowing subclasses to focus on specific visual representation and
 * positioning logic.
 *
 * @see ResizeHandle
 * @see CornerResizeHandle
 * @see EdgeResizeHandle
 * @see CursorMappings
 */
abstract class AbstractResizeHandle implements ResizeHandle {

    /**
     * The resize direction associated with this handle.
     */
    private final ResizeDirection direction;

    /**
     * Creates a new resize handle for the specified direction.
     * <p>
     * All subclasses should call this constructor to initialize the
     * resize direction, which determines both the handle's positioning and
     * the cursor feedback provided during user interaction.
     *
     * @param direction The resize direction for this handle, which must not be null
     */
    protected AbstractResizeHandle(ResizeDirection direction) {
        this.direction = direction;
    }

    @Override
    public ResizeDirection getDirection() {
        return direction;
    }

    protected Cursor getCursor() {
        return CursorMappings.getCursor(direction);
    }
}