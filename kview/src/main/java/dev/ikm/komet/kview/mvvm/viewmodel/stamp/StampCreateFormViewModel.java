package dev.ikm.komet.kview.mvvm.viewmodel.stamp;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.tinkar.terms.*;

import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.*;

public class StampCreateFormViewModel extends StampFormViewModelBase {

    public StampCreateFormViewModel(Type type) {
        super(type);

        addProperty(CLEAR_RESET_BUTTON_TEXT, "CLEAR");
        addProperty(SUBMIT_BUTTON_TEXT, "CONFIRM");

        setPropertyValue(FORM_TITLE, "New " + type.getTextDescription() + " Version");
        setPropertyValue(FORM_TIME_TEXT, "Uncommitted");
    }

    @Override
    protected void doUpdate(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        ConceptFacade authorConcept = viewProperties.nodeView().editCoordinate().getAuthorForChanges();
        setPropertyValue(AUTHOR, authorConcept);

        save(true);
    }

    @Override
    protected String getClearOrResetConfirmationMsg() {
        return "Are you sure you want to clear the form? All entered data will be lost.";
    }

    @Override
    protected String getClearOrResetConfirmationTitle() {
        return "Confirm Clear Form";
    }

    @Override
    protected boolean updateIsStampValuesChanged() {
        boolean empty = null == getPropertyValue(STATUS)
                        || null == getPropertyValue(PATH)
                        || null == getPropertyValue(MODULE);

        setPropertyValue(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, empty);
        return empty;
    }

    protected void showSucessToast() {
        State status = getValue(STATUS);
        EntityFacade module = getValue(MODULE);
        EntityFacade path = getValue(PATH);

        String statusString = getViewProperties().calculator().getDescriptionTextOrNid(status.nid());
        String moduleString = getViewProperties().calculator().getDescriptionTextOrNid(module.nid());
        String pathString = getViewProperties().calculator().getDescriptionTextOrNid(path.nid());

        String submitMessage = "Stamp definition stored for later use (" + statusString +
                ", " + moduleString + ", " + pathString + ")";

        JournalController.toast()
            .show(
                    Toast.Status.SUCCESS,
                    submitMessage
            );
    }

    @Override
    public StampCreateFormViewModel save() {
        super.save();

        if (invalidProperty().get()) {
            // Validation error so returning and not going to run the code to save to DB
            return this;
        }

        save(true);

        // We're not going to create a stamp here. Just saving the stamp properties to the view model
        // so that they can be used later to create a new Concept.

        return this;
    }
}