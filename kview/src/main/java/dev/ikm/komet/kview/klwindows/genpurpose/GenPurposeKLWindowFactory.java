package dev.ikm.komet.kview.klwindows.genpurpose;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_ID;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_TYPE;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowFactory;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.layout.LayoutComputer;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.preferences.NidTextEnum;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_USER_WINDOWS_DIR;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    @Override
    public GenPurposeKLWindow create(KlPreferencesFactory preferencesFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public AbstractEntityChapterKlWindow restore(KometPreferences preferences) {
        Objects.requireNonNull(preferences, "Preferences cannot be null");
        return null;
    }

    @Override
    public GenPurposeKLWindow restore(WindowSettings windowSettings, KometPreferences preferences,
                                      ViewProperties journalViewProperties) {
        Objects.requireNonNull(preferences, "Preferences cannot be null");
        try {
            // Load window state from preferences
            EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);

            // Extract journal topic from saved state
            Optional<UUID> journalTopicOpt = preferences.getUuid(JOURNAL_TOPIC);
            if (journalTopicOpt.isPresent()) {
                final UUID journalTopic = journalTopicOpt.get();

                // Derive from the journal's LIVE view so a restored window tracks the live coordinate/author
                // like a new window, not a coordinate reconstructed from preferences (ike-issues#756).
                final ViewProperties viewProperties = journalViewProperties;

                // Try to extract entity facade from saved state
                final int entityNid = windowState.getEntityNid();
                final NidTextEnum nidTextEnum = NidTextEnum.fromString(windowState.getEntityNidType())
                        .orElse(NidTextEnum.NID_TEXT);
                EntityFacade entityFacade = null;
                if (entityNid != 0) {
                    entityFacade = fetchEntity(entityNid, nidTextEnum);
                }

                // Resolve the KL-editor window definition (title, sections, fields) the window
                // is built from, by its persisted title + folder (komet-desktop#20); the journal seeds
                // the standard-window definitions before restoring. A window without its definition
                // would be an empty shell, so it is skipped: no title was saved by builds older than
                // the #20 fix, and a saved title stops resolving once its definition node is gone.
                final String editorWindowTitle =
                        windowState.getStringProperty(GenPurposeKLWindow.KL_EDITOR_WINDOW_TITLE, null);
                if (editorWindowTitle == null) {
                    LOG.warn("Skipping general-purpose KL window {}: no KL-editor window definition was saved with it",
                            windowState.getWindowId());
                    return null;
                }
                final String editorWindowDir = windowState.getStringProperty(
                        GenPurposeKLWindow.KL_EDITOR_WINDOW_DIR, KL_USER_WINDOWS_DIR);
                final KometPreferences editorWindowsPreferences =
                        KometPreferencesImpl.getConfigurationRootPreferences().node(KL_EDITOR_APP).node(editorWindowDir);
                if (!editorWindowsPreferences.nodeExists(editorWindowTitle)) {
                    LOG.warn("Skipping general-purpose KL window {}: its KL-editor window definition '{}/{}' no longer exists",
                            windowState.getWindowId(), editorWindowDir, editorWindowTitle);
                    return null;
                }

                // Create the window built from the definition, as the journal does at creation.
                GenPurposeKLWindow window = create(journalTopic, entityFacade, viewProperties, preferences,
                        editorWindowsPreferences.node(editorWindowTitle));

                // Restore the window state (geometry) after content init so the saved size/position wins.
                window.revert();

                LOG.info("Successfully restored general-purpose KL window: {}", window.getWindowTopic());
                return window;
            }
            return null;
        } catch (Exception e) {
            LOG.error("Failed to restore concept window from preferences", e);
            throw new RuntimeException("Concept window restoration failed", e);
        }
    }

    @Override
    public String name() {
        return EntityKlWindowFactory.super.name();
    }

    @Override
    public AbstractEntityChapterKlWindow create(KlPreferencesFactory preferencesFactory, LayoutComputer layoutComputer) {
        return EntityKlWindowFactory.super.create(preferencesFactory, layoutComputer);
    }

    @Override
    public AbstractEntityChapterKlWindow create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaLayoutForArea) {
        return EntityKlWindowFactory.super.create(preferencesFactory, areaLayoutForArea);
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.GEN_PURPOSE_KL;
    }

    /**
     * Fetches an entity based on the entity NID and NID type.
     *
     * @param entityNid   the NID of the entity to create
     * @param nidTextEnum the NID type (e.g., SEMANTIC_ENTITY, NID_TEXT)
     * @return the concept entity or null if creation failed
     */
    @SuppressWarnings("unchecked")
    private EntityFacade fetchEntity(int entityNid, NidTextEnum nidTextEnum) {
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