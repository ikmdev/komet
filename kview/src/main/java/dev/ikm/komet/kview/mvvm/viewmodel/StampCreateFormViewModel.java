package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.*;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.*;

public class StampCreateFormViewModel extends StampFormViewModelBase {
    /**
     * Provide the standard Confirm Clear dialog title for use in other classes
     */
    public static final String CONFIRM_RESET_TITLE = "Confirm Clear Form";
    /**
     * Provide the standard Confirm Clear dialog message for use in other classes
     */
    public static final String CONFIRM_RESET_MESSAGE =  "Are you sure you want to clear the form? All entered data will be lost.";

    public StampCreateFormViewModel(StampType stampType) {
        super(stampType);

        addProperty(CLEAR_RESET_BUTTON_TEXT, "CLEAR");
        addProperty(SUBMIT_BUTTON_TEXT, "CONFIRM");
    }

    public void init(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.topic = topic;

        // initialize observable lists
        Set<ConceptEntity> modules = fetchDescendentsOfConcept(viewProperties, TinkarTerm.MODULE.publicId());
        Set<ConceptEntity> paths = fetchDescendentsOfConcept(viewProperties, TinkarTerm.PATH.publicId());

        if (getObservableList(MODULES).isEmpty()) {
            setPropertyValues(MODULES, modules);
        }
        if (getObservableList(PATHS).isEmpty()) {
            setPropertyValues(PATHS, paths);
        }
        if (getObservableList(STATUSES).isEmpty()) {
            setPropertyValues(STATUSES, List.of(State.values()));
        }

        setPropertyValue(FORM_TITLE, "New " + stampType.getTextDescription() + " Version");
        setPropertyValue(TIME_TEXT, "Uncommitted");
        ConceptFacade authorConcept = viewProperties.nodeView().editCoordinate().getAuthorForChanges();
        setPropertyValue(AUTHOR, authorConcept);

        save(true);
    }

    @Override
    protected boolean updateIsStampValuesChanged() {
        boolean empty = null == getPropertyValue(STATUS)
                        || null == getPropertyValue(PATH)
                        || null == getPropertyValue(MODULE);

        setPropertyValue(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, empty);
        return empty;
    }

    public void cancel() {
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("Stamp From View Model cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
        reset();
    }

    public void resetOrClearForm(ActionEvent actionEvent) {
        ConfirmationDialogController.showConfirmationDialog((Node) actionEvent.getSource(), CONFIRM_RESET_TITLE, CONFIRM_RESET_MESSAGE)
            .thenAccept(confirmed -> {
                if (confirmed) {
                    reset();
                }
            });
    }

    @Override
    public void submitOrConfirm() {
        save();

        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("Stamp From View Model cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    @Override
    public StampCreateFormViewModel save() {
        super.save();

        if (invalidProperty().get()) {
            // Validation error so returning and not going to run the code to save to DB
            return this;
        }

        save(true);

        setPropertyValue(IS_CONFIRMED_OR_SUBMITTED, true);
        // We're not going to create a stamp here. Just saving the stamp properties to the view model
        // so that they can be used later to create a new Concept.

        return this;
    }
}