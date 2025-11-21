package dev.ikm.komet.kview.klwindows.genpurpose;

import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_ID;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.WINDOW_TYPE;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowFactory;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.klwindows.concept.ConceptKlWindowFactory;
import dev.ikm.komet.layout.LayoutComputer;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.NidTextEnum;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
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