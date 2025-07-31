package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javafx.scene.Node;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PREV_STAMP;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUSES;

public class StampViewModel2 extends FormViewModel {

    /**
     * Provide the standard Confirm Clear dialog title for use in other classes
     */
    public static final String CONFIRM_CLEAR_TITLE = "Confirm Clear Form";
    /**
     * Provide the standard Confirm Clear dialog message for use in other classes
     */
    public static final String CONFIRM_CLEAR_MESSAGE =  "Are you sure you want to clear the form? All entered data will be lost.";

    private EvtBus eventBus;
    private EntityFacade entityFacade;
    private ViewProperties viewProperties;
    private UUID topic;

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

    public void init(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        this.entityFacade = entity;
        this.viewProperties = viewProperties;
        this.topic = topic;
        this.eventBus = EvtBusFactory.getDefaultEvtBus();

        setPropertyValues(MODULES, fetchDescendentsOfConcept(viewProperties, TinkarTerm.MODULE.publicId()));
        setPropertyValues(PATHS, fetchDescendentsOfConcept(viewProperties, TinkarTerm.PATH.publicId()));
        setPropertyValues(STATUSES, List.of(State.values()));

        loadStampValuesFromDB();
    }

    private void loadStampValuesFromDB() {
        EntityVersion latestVersion = viewProperties.calculator().latest(entityFacade).get();
        StampEntity stampEntity = latestVersion.stamp();

        setPropertyValue(StampViewModel2.StampProperties.STATUS, stampEntity.state());
        setPropertyValue(StampViewModel2.StampProperties.MODULE, stampEntity.module());
        setPropertyValue(PATH, stampEntity.path());
    }

    public void cancel(Node eventSource) {
        loadStampValuesFromDB();
        eventBus.publish(topic, new ClosePropertiesPanelEvent(eventSource,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    public void reset(Node eventSource) {
        ConfirmationDialogController.showConfirmationDialog(eventSource, CONFIRM_CLEAR_TITLE, CONFIRM_CLEAR_MESSAGE)
            .thenAccept(confirmed -> {
                if (confirmed) {
                    loadStampValuesFromDB();
                }
            });
    }

    @Override
    public StampViewModel save(boolean force) {
        return (StampViewModel) super.save(force);
    }

}
