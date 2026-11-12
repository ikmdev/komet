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
package dev.ikm.komet.layout.sidebar;

import javafx.beans.property.StringProperty;
import javafx.scene.layout.Region;

/**
 * The assistant as a window's {@linkplain KlAssistantSidebar sidebar} shows it: a pane with its own header,
 * made by a {@link KlAssistantPaneProvider} the first time the sidebar opens and kept for the window's life.
 */
public interface KlAssistantPane {

    /** @return the pane's node, which the sidebar docks at the window's right edge */
    Region fxObject();

    /** The window is closing: save what the pane keeps and let go of what outlives it. */
    default void shutdown() {
    }

    // -- host name
    /**
     * The name of the window the pane is in, as the person knows it, such as a journal's name. The sidebar
     * binds it to its own {@linkplain KlAssistantSidebar#hostNameProperty() host name}.
     *
     * @return the host name property
     */
    StringProperty hostNameProperty();

    /** @return the name of the window the pane is in */
    default String getHostName() { return hostNameProperty().get(); }

    /** @param value the name of the window the pane is in */
    default void setHostName(String value) { hostNameProperty().set(value); }
}
