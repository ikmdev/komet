package dev.ikm.komet.kview.mvvm.viewmodel.stamp;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.FORM_TITLE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_STAMP_VALUES_THE_SAME_OR_EMPTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUSES;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.TIME;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.FORM_TIME_TEXT;

public abstract class StampFormViewModelBase extends FormViewModel {
    protected EntityFacade entityFacade;
    protected UUID topic;
    protected ViewProperties viewProperties;

    protected Type type;

    public StampFormViewModelBase(Type type) {
        this.type = type;

        // Stamp Properties
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

        addProperty(IS_CONFIRMED_OR_SUBMITTED, false);

        addProperty(MODULES, Collections.emptyList(), true);
        addProperty(PATHS, Collections.emptyList(), true);
        addProperty(STATUSES, Collections.emptyList(), true);

        addProperty(FORM_TITLE, "");
        addProperty(FORM_TIME_TEXT, "");

        // run validators when the following properties change.
        doOnChange(this::validate, STATUS, MODULE, PATH);
    }

    protected abstract boolean updateIsStampValuesChanged();

    public final void update(EntityFacade entity, UUID topic, ViewProperties viewProperties){
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

        doUpdate(entity, topic, viewProperties);
    }

    protected abstract void doUpdate(EntityFacade entity, UUID topic, ViewProperties viewProperties);

    public void resetOrClearForm(ActionEvent actionEvent) {
        ConfirmationDialogController.showConfirmationDialog(
                (Node) actionEvent.getSource(),
                getClearOrResetConfirmationTitle(),
                getClearOrResetConfirmationMsg()
            )
            .thenAccept(confirmed -> {
                if (confirmed) {
                    reset();
                }
            }
       );
    }

    public void submitOrConfirm() {
        save();
        showSucessToast();

        setPropertyValue(IS_CONFIRMED_OR_SUBMITTED, true);
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("Stamp From View Model cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    public void cancel() {
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("StampViewModel2 cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
        reset();
    }

    protected abstract void showSucessToast();

    public ViewProperties getViewProperties() { return viewProperties; }

    /**
     * Provide the standard Confirm Clear/Reset dialog title.
     */
    protected abstract String getClearOrResetConfirmationMsg();

    /**
     * Provide the standard Confirm Clear/Reset dialog message.
     */
    protected abstract String getClearOrResetConfirmationTitle();

    /***************************************************************************
     *                                                                         *
     * Support Classes                                                         *
     *                                                                         *
     **************************************************************************/

    public enum Properties {
        CURRENT_STAMP,                  // The current stamp

        STATUS,                         // User selected Status
        AUTHOR,                         // Author of the Stamp
        MODULE,                         // User selected Module
        PATH,                           // User selected Path
        TIME,                           // Time of the Stamp

        IS_STAMP_VALUES_THE_SAME_OR_EMPTY,       // Are the Stamp values in the properties the same as of the current Stamp.
        // Cn also indicate if the current stamp values are empty.

        IS_CONFIRMED_OR_SUBMITTED,                   // Signals whether the user has pressed confirmed on the form with valid stamp values

        STATUSES,
        MODULES,
        PATHS,

        FORM_TITLE,
        FORM_TIME_TEXT,

        CLEAR_RESET_BUTTON_TEXT,
        SUBMIT_BUTTON_TEXT
    }

    public enum Type {
        CONCEPT("Concept"),
        PATTERN("Pattern"),
        SEMANTIC("Semantic");

        private final String textDescription;

        Type(String textDescription) {
            this.textDescription = textDescription;
        }

        public String getTextDescription() { return textDescription; }
    }
}