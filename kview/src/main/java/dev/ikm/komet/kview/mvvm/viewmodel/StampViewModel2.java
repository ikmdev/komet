package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
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
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.IS_STAMP_VALUES_THE_SAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.CURRENT_STAMP;
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

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public enum StampProperties {
        CURRENT_STAMP,                  // The current stamp

        STATUS,                         // User selected Status
        MODULE,                         // User selected Module
        PATH,                           // User selected Path

        IS_STAMP_VALUES_THE_SAME,       // Are the Stamp values in the properties the same as of the current Stamp

        STATUSES,
        MODULES,
        PATHS
    }

    public StampViewModel2() {
        super();
        addProperty(CURRENT_STAMP, (Stamp) null);
        addProperty(STATUS, State.ACTIVE);
        addProperty(MODULE, (ConceptEntity) null);
        addProperty(PATH, (ConceptEntity) null);

        addProperty(IS_STAMP_VALUES_THE_SAME, true);
        addValidator(IS_STAMP_VALUES_THE_SAME, "Validator Property", (ValidationResult vr, ViewModel vm) -> {
            boolean same = updateIsStampValuesChanged();
            if (same) {
                // if UI’s stamp is the same as the previous stamp than it is invalid.
                vr.error("Cannot submit stamp because the data is the same.");
            }
        });

        addProperty(MODULES, Collections.emptyList(), true);
        addProperty(PATHS, Collections.emptyList(), true);
        addProperty(STATUSES, Collections.emptyList(), true);

        this.eventBus = EvtBusFactory.getDefaultEvtBus();
    }

    public void init(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        this.entityFacade = entity;
        this.viewProperties = viewProperties;
        this.topic = topic;

        // listen to events that the properties panel is going to be closed
        closePropertiesPanelEventSubscriber = evt -> onPropertiesPanelClose();
        eventBus.subscribe(topic, ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);

        // initialize observable lists
        setPropertyValues(MODULES, fetchDescendentsOfConcept(viewProperties, TinkarTerm.MODULE.publicId()));
        setPropertyValues(PATHS, fetchDescendentsOfConcept(viewProperties, TinkarTerm.PATH.publicId()));
        setPropertyValues(STATUSES, List.of(State.values()));

        loadStamp();
        loadStampValuesFromDB();
        save(true);

        doOnChange(this::validate, STATUS, MODULE, PATH);
    }

    private void onPropertiesPanelClose() {
        loadStampValuesFromDB(); // every time the properties panel closes we reset the form
    }

    private void loadStamp() {
        EntityVersion latestVersion = viewProperties.calculator().latest(entityFacade).get();
        StampEntity stampEntity = latestVersion.stamp();

        setPropertyValue(StampProperties.CURRENT_STAMP, stampEntity);
    }

    private void loadStampValuesFromDB() {
        StampEntity stampEntity = getPropertyValue(StampProperties.CURRENT_STAMP);

        setPropertyValue(STATUS, stampEntity.state());
        setPropertyValue(MODULE, stampEntity.module());
        setPropertyValue(PATH, stampEntity.path());
    }

    private boolean updateIsStampValuesChanged() {
        StampEntity stampEntity = getPropertyValue(StampProperties.CURRENT_STAMP);

        boolean same = stampEntity.state() == getPropertyValue(STATUS)
                && stampEntity.path() == getPropertyValue(PATH)
                && stampEntity.module() == getPropertyValue(MODULE);

        setPropertyValue(IS_STAMP_VALUES_THE_SAME, same);

        return same;
    }

    public void cancel(Node eventSource) {
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
    public StampViewModel2 save(boolean force) {
        return (StampViewModel2) super.save(force);
    }
}
