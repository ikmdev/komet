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
package dev.ikm.komet.kview.klwindows.genpurpose;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.klwindows.EntityKlWindowFactory;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.preferences.NidTextEnum;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_USER_WINDOWS_DIR;

/**
 * A factory for creating general purpose knowledge layout windows.
 * General Purpose Knowledge Layout Chapter Windows are displayed inside of the Journal Window desktop workspace
 */
public class GenPurposeKLWindowFactory implements EntityKlWindowFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeKLWindowFactory.class);

    /**
     * A general-purpose window is built from a KL-editor window definition, which this generic
     * signature has no slot for; see
     * {@link #create(UUID, EntityFacade, ViewProperties, KometPreferences, KometPreferences)}.
     */
    @Override
    public GenPurposeKLWindow create(UUID journalTopic,
                                     EntityFacade entityFacade,
                                     ViewProperties viewProperties,
                                     KometPreferences preferences) {
        throw new UnsupportedOperationException(
                "A general-purpose KL window needs the KL-editor window definition it is built from");
    }

    /**
     * Creates a general-purpose window built from the KL-editor window definition held at
     * {@code editorWindowPreferences}.
     *
     * @param editorWindowPreferences the {@code kl-editor-app/{user,standard}-windows/<title>} node of the definition
     */
    public GenPurposeKLWindow create(UUID journalTopic,
                                     EntityFacade entityFacade,
                                     ViewProperties viewProperties,
                                     KometPreferences preferences,
                                     KometPreferences editorWindowPreferences) {
        return new GenPurposeKLWindow(journalTopic, entityFacade, viewProperties, preferences, editorWindowPreferences);
    }

    /**
     * Restores a window from the state it saved to {@code preferences}, built from the same
     * KL-editor window definition, on the same component and with its saved geometry. Derives from
     * the journal's live view so the restored window tracks the live coordinate and author like a
     * new one, not a coordinate reconstructed from preferences (ike-issues#756).
     *
     * @return the restored window, or null when the state names no journal or its definition is gone
     */
    @Override
    public GenPurposeKLWindow restore(WindowSettings windowSettings, KometPreferences preferences,
                                      ViewProperties journalViewProperties) {
        EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);

        Optional<UUID> journalTopic = preferences.getUuid(JOURNAL_TOPIC);
        if (journalTopic.isEmpty()) {
            return null;
        }

        OptionalLong entityNid = windowState.resolveEntityNid();
        NidTextEnum nidTextEnum = NidTextEnum.fromString(windowState.getEntityReadType())
                .orElse(NidTextEnum.NID_TEXT);
        EntityFacade entityFacade = entityNid.isPresent() ? fetchEntity(entityNid.getAsLong(), nidTextEnum) : null;

        // Resolve the KL-editor window definition (title, sections, fields) the window
        // is built from, by its persisted title + folder (komet-desktop#20); the journal seeds
        // the standard-window definitions before restoring. A window without its definition
        // would be an empty shell, so it is skipped: no title was saved by builds older than
        // the #20 fix, and a saved title stops resolving once its definition node is gone.
        String editorWindowTitle = windowState.getStringProperty(GenPurposeKLWindow.KL_EDITOR_WINDOW_TITLE, null);
        if (editorWindowTitle == null) {
            LOG.warn("Skipping general-purpose KL window {}: no KL-editor window definition was saved with it",
                    windowState.getWindowId());
            return null;
        }
        String editorWindowDir = windowState.getStringProperty(GenPurposeKLWindow.KL_EDITOR_WINDOW_DIR, KL_USER_WINDOWS_DIR);
        KometPreferences editorWindowsPreferences =
                KometPreferencesImpl.getConfigurationRootPreferences().node(KL_EDITOR_APP).node(editorWindowDir);
        boolean definitionExists;
        try {
            definitionExists = editorWindowsPreferences.nodeExists(editorWindowTitle);
        } catch (BackingStoreException e) {
            throw new IllegalStateException("Could not read the KL-editor window definitions in " + editorWindowDir, e);
        }
        if (!definitionExists) {
            LOG.warn("Skipping general-purpose KL window {}: its KL-editor window definition '{}/{}' no longer exists",
                    windowState.getWindowId(), editorWindowDir, editorWindowTitle);
            return null;
        }

        // Create the window built from the definition, as the journal does at creation.
        GenPurposeKLWindow window = create(journalTopic.get(), entityFacade, journalViewProperties, preferences,
                editorWindowsPreferences.node(editorWindowTitle));

        // Restore the window state (geometry) after content init so the saved size/position wins.
        window.revert();

        LOG.info("Successfully restored general-purpose KL window: {}", window.getWindowTopic());
        return window;
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.GEN_PURPOSE_KL;
    }

    /**
     * Fetches the entity a window was saved on. A saved semantic resolves to the concept it
     * describes, falling back to reading the NID as a concept when no such concept resolves.
     *
     * @param entityNid   the NID of the saved entity
     * @param nidTextEnum how the NID was saved (e.g., SEMANTIC_ENTITY, NID_TEXT)
     * @return the entity, or null when nothing resolves
     */
    private EntityFacade fetchEntity(long entityNid, NidTextEnum nidTextEnum) {
        if (nidTextEnum == NidTextEnum.SEMANTIC_ENTITY) {
            return Entity.getConceptForSemantic(entityNid)
                    .orElseGet(() -> {
                        LOG.warn("Referenced semantic entity with NID {} no longer exists, falling back to direct access", entityNid);
                        return EntityHandle.get(entityNid).asConcept().orElse(null);
                    });
        }
        return EntityHandle.get(entityNid).orNull();
    }
}
