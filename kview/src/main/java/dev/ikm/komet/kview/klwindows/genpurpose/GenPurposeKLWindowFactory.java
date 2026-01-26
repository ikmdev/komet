package dev.ikm.komet.kview.klwindows.genpurpose;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_ID;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_TYPE;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalViewProperties;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowFactory;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.klwindows.concept.ConceptKlWindowFactory;
import dev.ikm.komet.layout.LayoutComputer;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.NidTextEnum;
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

    private static final Logger LOG = LoggerFactory.getLogger(ConceptKlWindowFactory.class);

    @Override
    public GenPurposeKLWindow create(UUID journalTopic,
                                  EntityFacade entityFacade,
                                  ViewProperties viewProperties,
                                  KometPreferences preferences) {

        preferences.put(WINDOW_TYPE, getWindowType().toString());
        preferences.put(WINDOW_ID, UUID.randomUUID().toString());

        return new GenPurposeKLWindow(journalTopic, entityFacade, viewProperties, preferences);
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
    public GenPurposeKLWindow restore(WindowSettings windowSettings, KometPreferences preferences) {
        Objects.requireNonNull(preferences, "Preferences cannot be null");
        try {
            // Load window state from preferences
            EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);

            // Extract journal topic from saved state
            Optional<UUID> journalTopicOpt = preferences.getUuid(JOURNAL_TOPIC);
            if (journalTopicOpt.isPresent()) {
                final UUID journalTopic = journalTopicOpt.get();

                // don't call this
                final ViewProperties viewProperties = getJournalViewProperties(windowSettings, journalTopic);

                // Try to extract entity facade from saved state
                final int entityNid = windowState.getEntityNid();
                final NidTextEnum nidTextEnum = NidTextEnum.fromString(windowState.getEntityNidType())
                        .orElse(NidTextEnum.NID_TEXT);
                EntityFacade entityFacade = null;
                if (entityNid != 0) {
                    entityFacade = fetchEntity(entityNid, nidTextEnum);
                }

                // Create the window with the extracted parameters
                GenPurposeKLWindow window = create(journalTopic, entityFacade, viewProperties, preferences);

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