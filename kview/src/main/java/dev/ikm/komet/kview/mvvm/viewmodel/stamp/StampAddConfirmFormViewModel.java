package dev.ikm.komet.kview.mvvm.viewmodel.stamp;

import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;

import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.SUBMIT_BUTTON_TEXT;

public class StampAddConfirmFormViewModel extends StampAddFormViewModelBase {

    public StampAddConfirmFormViewModel(StampFormViewModelBase.StampType stampType) {
        super(stampType);

        addProperty(SUBMIT_BUTTON_TEXT, "CONFIRM");
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
    public StampAddConfirmFormViewModel save() {
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