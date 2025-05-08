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
package dev.ikm.komet.kview.klwindows.concept;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.EntityKlWindowFactory;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.layout.context.KlContextFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.NidTextEnum;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalViewProperties;

/**
 * A factory for creating {@link ConceptKlWindow} instances to display and manage
 * concept details within a Komet-based application.
 * <p>
 * This class extends {@link EntityKlWindowFactory} to build
 * specialized windows for concept creation and editing. It also associates
 * relevant view settings to the resulting windows.
 */
public class ConceptKlWindowFactory implements EntityKlWindowFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptKlWindowFactory.class);

    @Override
    public ConceptKlWindow create(UUID journalTopic,
                                  EntityFacade entityFacade,
                                  ViewProperties viewProperties,
                                  KometPreferences preferences) {
        return new ConceptKlWindow(journalTopic, entityFacade, viewProperties, preferences);
    }

    @Override
    public ConceptKlWindow create(KlPreferencesFactory preferencesFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ConceptKlWindow createWithContext(KlPreferencesFactory preferencesFactory, KlContextFactory contextFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ConceptKlWindow restore(KometPreferences preferences) {
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
                final NidTextEnum nidTextEnum = NidTextEnum.fromString(windowState.getEntityNidType())
                        .orElse(NidTextEnum.NID_TEXT);
                ConceptFacade conceptFacade = null;
                if (entityNid != 0) {
                    conceptFacade = createConceptEntity(entityNid, nidTextEnum);
                }

                // Create the window with the extracted parameters
                ConceptKlWindow window = create(journalTopic, conceptFacade, viewProperties, preferences);

                // Restore the window state
                window.revert();

                LOG.info("Successfully restored concept window: {}", window.getWindowTopic());
                return window;
            }
            return null;
        } catch (Exception e) {
            LOG.error("Failed to restore concept window from preferences", e);
            throw new RuntimeException("Concept window restoration failed", e);
        }
    }

    @Override
    public Class<ConceptKlWindow> klImplementationClass() {
        return ConceptKlWindow.class;
    }

    @Override
    public String klDescription() {
        return "Concept Details Chapter Window are displayed inside of the Journal Window desktop workspace";
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.CONCEPT;
    }

    /**
     * Creates a concept entity based on the entity NID and NID type.
     *
     * @param entityNid   the NID of the entity to create
     * @param nidTextEnum the NID type (e.g., SEMANTIC_ENTITY, NID_TEXT)
     * @return the concept entity or null if creation failed
     */
    @SuppressWarnings("unchecked")
    private ConceptFacade createConceptEntity(int entityNid, NidTextEnum nidTextEnum) {
        // Only SEMANTIC_ENTITY needs special handling
        if (nidTextEnum == NidTextEnum.SEMANTIC_ENTITY) {
            return Entity.getConceptForSemantic(entityNid)
                    .orElseGet(() -> {
                        LOG.warn("Referenced semantic entity with NID {} no longer exists, falling back to direct access", entityNid);
                        return Entity.getFast(entityNid);
                    });
        } else {
            // For other types, just return the entity directly
            return Entity.getFast(entityNid);
        }
    }
}
