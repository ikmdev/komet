package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.event.ActionEvent;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.Collections;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.FORM_TITLE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.IS_STAMP_VALUES_THE_SAME_OR_EMPTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.STATUSES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.TIME;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.FORM_TIME_TEXT;

public abstract class StampFormViewModelBase extends FormViewModel {
    protected EntityFacade entityFacade;
    protected UUID topic;
    protected ViewProperties viewProperties;

    protected StampType stampType;

    public StampFormViewModelBase(StampType stampType) {
        this.stampType = stampType;

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

    public abstract void init(EntityFacade entity, UUID topic, ViewProperties viewProperties);

    public abstract void cancel();

    public abstract void resetOrClearForm(ActionEvent actionEvent);

    public abstract void submitOrConfirm();

    public ViewProperties getViewProperties() { return viewProperties; }

    /***************************************************************************
     *                                                                         *
     * Support Classes                                                         *
     *                                                                         *
     **************************************************************************/

    public enum StampProperties {
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

    public enum StampType {
        CONCEPT("Concept"),
        PATTERN("Pattern"),
        SEMANTIC("Semantic");

        private final String textDescription;

        StampType(String textDescription) {
            this.textDescription = textDescription;
        }

        public String getTextDescription() { return textDescription; }
    }
}