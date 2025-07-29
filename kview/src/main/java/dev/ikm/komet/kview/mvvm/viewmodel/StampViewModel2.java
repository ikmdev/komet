package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;

import java.util.Collections;

import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PREV_STAMP;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.SAME_AS_PREVIOUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUSES;

public class StampViewModel2 extends FormViewModel {

    public enum StampProperties {
        ENTITY,               // The component (Concept, Pattern, Semantic) EntityFacade
        PREV_STAMP,           // The previous stamp. Stamp
        STATUS,               // User selected Status
//        LAST_MOD_DATE,        // The previous stamp date time (read-only?) we could use PREV_STAMP's time
        MODULE,               // User selected Module (EntityFacade)
        PATH,                 // User selected Path (EntityFacade)
        SAME_AS_PREVIOUS,     // Custom validator
        SUBMITTED,             // Flag when user pressed submit.

        STATUSES,
        MODULES,
        PATHS
    }

    public StampViewModel2() {
        super();
        addProperty(ENTITY, (EntityFacade) null);
        addProperty(PREV_STAMP, (Stamp) null);
        addProperty(STATUS, State.ACTIVE);
        addProperty(MODULE, (ConceptEntity) null);
        addProperty(PATH, (ConceptEntity) null);

        // TODO:
//        addValidator(SAME_AS_PREVIOUS, )

        addProperty(MODULES, Collections.emptyList(), true);
        addProperty(PATHS, Collections.emptyList(), true);
        addProperty(STATUSES, Collections.emptyList(), true);
    }

    @Override
    public StampViewModel save(boolean force) {
        return (StampViewModel) super.save(force);
    }

}
