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

import java.util.HashMap;
import java.util.Map;

public enum CursorMappings {
    INSTANCE;
    /**
     * The 8 cursor directions to resize the current window.
     */
    public enum RESIZE_DIRECTION {
        NONE, NW, N, NE, E, SE, S, SW, W
    }
    public static Map<RESIZE_DIRECTION, Cursor> cursorMap = new HashMap<>();
    static {
        cursorMap.put(RESIZE_DIRECTION.NW, Cursor.NW_RESIZE);
        cursorMap.put(RESIZE_DIRECTION.N, Cursor.N_RESIZE);
        cursorMap.put(RESIZE_DIRECTION.NE, Cursor.NE_RESIZE);
        cursorMap.put(RESIZE_DIRECTION.E, Cursor.E_RESIZE);
        cursorMap.put(RESIZE_DIRECTION.SE, Cursor.SE_RESIZE);
        cursorMap.put(RESIZE_DIRECTION.S, Cursor.S_RESIZE);
        cursorMap.put(RESIZE_DIRECTION.SW, Cursor.SW_RESIZE);
        cursorMap.put(RESIZE_DIRECTION.W, Cursor.W_RESIZE);
    }

    public static Cursor findCursorType(RESIZE_DIRECTION direction) {
        return cursorMap.get(direction);
    }
}