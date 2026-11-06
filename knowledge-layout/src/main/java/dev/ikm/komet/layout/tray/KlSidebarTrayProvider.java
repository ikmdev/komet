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

import java.util.UUID;

/**
 * A tray for the journal's left sidebar, discovered as a service (a {@code provides
 * KlSidebarTrayProvider with …} clause), so a plugin can add a sidebar entry without the journal
 * naming it. The journal draws the toggle and slides the tray; everything inside is the plugin's.
 */
public interface KlSidebarTrayProvider {

    /** @return the sidebar toggle's node id, unique among toggles, by which a card can select it */
    String toggleId();

    /** @return the toggle's tooltip */
    String tooltip();

    /** @return the toggle's icon as an SVG path in a 24-unit box */
    String iconShape();

    /**
     * Makes the tray for one journal.
     *
     * @param journalTopic the journal's event topic, on which the tray asks for windows
     * @return the tray
     */
    KlSidebarTray create(UUID journalTopic);
}
