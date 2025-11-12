package dev.ikm.komet.kview.klwindows.genpurpose;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.klwindows.EntityKlWindowFactory;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.klwindows.concept.ConceptKlWindowFactory;
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
import java.util.UUID;

import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_ID;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_TYPE;

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
    public GenPurposeKLWindow createWithContext(KlPreferencesFactory preferencesFactory, KlContextFactory contextFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public GenPurposeKLWindow restore(WindowSettings windowSettings, KometPreferences preferences) {
        Objects.requireNonNull(preferences, "Preferences cannot be null");
//        try {
            // Load window state from preferences
//            EntityKlWindowState windowState = EntityKlWindowState.fromPreferences(preferences);
//
//            // Extract journal topic from saved state
//            Optional<UUID> journalTopicOpt = preferences.getUuid(JOURNAL_TOPIC);
//            if (journalTopicOpt.isPresent()) {
//                final UUID journalTopic = journalTopicOpt.get();
//
//                // don't call this
//                final ViewProperties viewProperties = getJournalViewProperties(windowSettings, journalTopic);
//
//                // Try to extract entity facade from saved state
//                final int entityNid = windowState.getEntityNid();
//                final NidTextEnum nidTextEnum = NidTextEnum.fromString(windowState.getEntityNidType())
//                        .orElse(NidTextEnum.NID_TEXT);
//                ConceptFacade conceptFacade = null;
//                if (entityNid != 0) {
//                    conceptFacade = createConceptEntity(entityNid, nidTextEnum);
//                }
//
//                // Create the window with the extracted parameters
//                ConceptKlWindow window = create(journalTopic, conceptFacade, viewProperties, preferences);
//
//                // Restore the window state
//                window.revert();
//
//                LOG.info("Successfully restored concept window: {}", window.getWindowTopic());
//                return window;
//            }
            return null;
//        } catch (Exception e) {
//            LOG.error("Failed to restore concept window from preferences", e);
//            throw new RuntimeException("Concept window restoration failed", e);
//        }
    }

    @Override
    public Class<GenPurposeKLWindow> klImplementationClass() {
        return GenPurposeKLWindow.class;
    }

    @Override
    public String klDescription() {
        return "General Purpose Knowledge Layout Chapter Windows are displayed inside of the Journal Window desktop workspace";
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.GEN_PURPOSE_KL;
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