package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.*;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.Collections;
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

    private EntityFacade entityFacade;
    private UUID topic;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public StampCreateFormViewModel(StampType stampType) {
        super(stampType);
        // Add Properties
        addProperty(STATUS, (State) null);
        addProperty(TIME, 0L);
        addProperty(AUTHOR, (ComponentWithNid) null);
        addProperty(MODULE, (ComponentWithNid) null);
        addProperty(PATH, (ComponentWithNid) null);

        addProperty(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, true);
        addValidator(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, "Validator Property", (ValidationResult vr, ViewModel vm) -> {
            boolean same = updateIsStampValuesChanged();
            if (same) {
                // if UI’s stamp is the same as the previous stamp than it is invalid.
                vr.error("Cannot submit stamp because the data is the same.");
            }
        });

        addProperty(IS_CONFIRMED, false);

        addProperty(MODULES, Collections.emptyList(), true);
        addProperty(PATHS, Collections.emptyList(), true);
        addProperty(STATUSES, Collections.emptyList(), true);

        addProperty(FORM_TITLE, "");
        addProperty(TIME_TEXT, "");

        addProperty(CLEAR_RESET_BUTTON_TEXT, "CLEAR");
        addProperty(SUBMIT_BUTTON_TEXT, "CONFIRM");

        // run validators when the following properties change.
        doOnChange(this::validate, STATUS, MODULE, PATH);

        // Initialize
        setPropertyValue(FORM_TITLE, "");
        setPropertyValue(TIME_TEXT, "");
    }

    public void init(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.topic = topic;

        // listen to events that the properties panel is going to be closed
        closePropertiesPanelEventSubscriber = evt -> onPropertiesPanelClose();
        EvtBusFactory.getDefaultEvtBus().subscribe(topic, ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);

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

    protected void onPropertiesPanelClose() {
        reset();
    }

    private boolean updateIsStampValuesChanged() {
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
        setPropertyValue(IS_CONFIRMED, true);
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

        return this;
    }
}