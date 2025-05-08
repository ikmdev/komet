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
package dev.ikm.komet.kview.klwindows.pattern;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.*;
import dev.ikm.komet.kview.klwindows.lidr.LidrKlWindow;
import dev.ikm.komet.layout.context.KlContextFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalViewProperties;

/**
 * A factory able to create a pattern details window (entity chapter type window) to be managed and displayed
 * in the Journal View.
 */
public class PatternKlWindowFactory implements EntityKlWindowFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PatternKlWindowFactory.class);

    @Override
    public PatternKlWindow create(UUID journalTopic, EntityFacade entityFacade,
                                  ViewProperties viewProperties, KometPreferences preferences) {
        return new PatternKlWindow(journalTopic, entityFacade, viewProperties, preferences);
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
    public PatternKlWindow restore(KometPreferences preferences) {
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
                PatternFacade patternFacade = null;
                if (entityNid != 0) {
                    patternFacade = Entity.getFast(entityNid);
                }

                // Create the window with the extracted parameters
                PatternKlWindow window = create(journalTopic, patternFacade, viewProperties, preferences);

                // Restore the window state
                window.revert();

                LOG.info("Successfully restored pattern window: {}", window.getWindowTopic());
                return window;
            }
            return null;
        } catch (Exception ex) {
            LOG.error("Failed to restore pattern window from preferences", ex);
            throw new RuntimeException("Pattern window restoration failed", ex);
        }
    }

    @Override
    public Class<PatternKlWindow> klImplementationClass() {
        return PatternKlWindow.class;
    }

    @Override
    public String klDescription() {
        return "Pattern Details Chapter Window are displayed inside of the Journal Window desktop workspace";
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.PATTERN;
    }
}
