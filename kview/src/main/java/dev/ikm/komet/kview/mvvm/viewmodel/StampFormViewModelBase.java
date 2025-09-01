package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;

import java.util.UUID;

public abstract class StampFormViewModelBase extends FormViewModel {

    public enum StampProperties {
        CURRENT_STAMP,                  // The current stamp

        STATUS,                         // User selected Status
        AUTHOR,                         // Author of the Stamp
        MODULE,                         // User selected Module
        PATH,                           // User selected Path
        TIME,                           // Time of the Stamp

        IS_STAMP_VALUES_THE_SAME_OR_EMPTY,       // Are the Stamp values in the properties the same as of the current Stamp.
                                                 // Cn also indicate if the current stamp values are empty.

        IS_CONFIRMED,                   // Signals whether the user has pressed confirmed on the form with valid stamp values

        STATUSES,
        MODULES,
        PATHS,

        FORM_TITLE,
        TIME_TEXT,

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

    protected ViewProperties viewProperties;

    protected StampType stampType;

    public StampFormViewModelBase(StampType stampType) {
        this.stampType = stampType;
    }

    public abstract void init(EntityFacade entity, UUID topic, ViewProperties viewProperties);

    public abstract void cancel();

    public abstract void resetOrClearForm(ActionEvent actionEvent);

    public abstract void submitOrConfirm();

    public ViewProperties getViewProperties() { return viewProperties; }
}