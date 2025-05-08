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
package dev.ikm.komet.kview.klwindows.lidr;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.EntityKlWindowFactory;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.layout.context.KlContextFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalViewProperties;

/**
 * A factory for creating {@link LidrKlWindow} instances, which display LIDR (Logical Instrumentation,
 * Devices, and Records) details within a Komet-based application.
 * <p>
 * This class extends {@link EntityKlWindowFactory} to provide implementations
 * for constructing specialized LIDR windows. It also supplies a description of the widget
 * and its implementing class for use within a desktop workspace or user interface.
 */
public class LidrKlWindowFactory implements EntityKlWindowFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LidrKlWindowFactory.class);

    @Override
    public LidrKlWindow create(UUID journalTopic, EntityFacade deviceConcept,
                               ViewProperties viewProperties, KometPreferences preferences) {
        return new LidrKlWindow(journalTopic, deviceConcept, viewProperties, preferences);
    }

    @Override
    public LidrKlWindow create(KlPreferencesFactory preferencesFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public LidrKlWindow createWithContext(KlPreferencesFactory preferencesFactory, KlContextFactory contextFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public LidrKlWindow restore(KometPreferences preferences) {
        Objects.requireNonNull(preferences, "Preferences cannot be null");
        try {
            // Load window state from preferences
            EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);

            // Extract journal topic from saved state
            Optional<UUID> journalTopicOpt = preferences.getUuid(JOURNAL_TOPIC);
            if (journalTopicOpt.isPresent()) {
                final UUID journalTopic = journalTopicOpt.get();
                final ViewProperties viewProperties = getJournalViewProperties(journalTopic);

                // Try to extract entity facade from saved state
                final int entityNid = windowState.getEntityNid();
                EntityFacade entityFacade = null;
                if (entityNid != 0) {
                    entityFacade = Entity.getFast(entityNid);
                }

                // Create the window with the extracted parameters
                LidrKlWindow window = create(journalTopic, entityFacade, viewProperties, preferences);

                // Restore the window state
                window.revert();

                LOG.info("Successfully restored LIDR window: {}", window.getWindowTopic());
                return window;
            }
            return null;
        } catch (Exception e) {
            LOG.error("Failed to restore LIDR window from preferences", e);
            throw new RuntimeException("LIDR window restoration failed", e);
        }
    }

    @Override
    public Class<LidrKlWindow> klImplementationClass() {
        return LidrKlWindow.class;
    }

    @Override
    public String klDescription() {
        return "Lidr Details Chapter Window are displayed inside of the Journal Window desktop workspace";
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.LIDR;
    }
}
