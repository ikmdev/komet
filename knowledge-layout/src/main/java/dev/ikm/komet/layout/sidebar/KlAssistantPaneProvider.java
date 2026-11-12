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

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.UUID;

/**
 * Discovery SPI for the assistant a window's {@linkplain KlAssistantSidebar sidebar} shows. A plugin provides
 * one with a {@code provides KlAssistantPaneProvider with …} clause; a window with no provider gets no
 * sidebar and no button to open one.
 */
public interface KlAssistantPaneProvider {

    /**
     * Makes the pane for one window.
     *
     * @param host the window the pane is for
     * @return the pane
     */
    KlAssistantPane create(Host host);

    /**
     * The window a pane is made for.
     *
     * @param preferences    the node the pane keeps its state under, this window's alone
     * @param viewProperties the window's view, which the pane's own view overrides
     * @param journalTopic   the journal's event topic, or {@code null} when the window is not a journal
     * @param collapse       hides the sidebar; the pane's header carries the control that runs it
     */
    record Host(KometPreferences preferences, ViewProperties viewProperties, UUID journalTopic,
                Runnable collapse) {
    }
}
