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
package dev.ikm.komet.layout.tray;

import javafx.beans.value.ObservableIntegerValue;
import javafx.scene.layout.Pane;

/**
 * One journal's copy of a tray a plugin adds to the journal's left sidebar. The journal slides
 * {@link #fxObject()} out when the person selects the tray's toggle.
 */
public interface KlSidebarTray {

    /** @return the tray's content, at the width it wants */
    Pane fxObject();

    /**
     * A count the journal draws on the tray's toggle while it is above zero, as the Trash shows
     * how many items are in it: what is waiting in the tray to be looked at.
     *
     * @return the count, or null for a tray that has none
     */
    default ObservableIntegerValue badge() {
        return null;
    }

    /** Called as the tray slides out, so a look at it starts from a fresh read. */
    default void shown() {
    }

    /** Called when the journal closes. */
    default void shutdown() {
    }
}
