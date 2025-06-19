package dev.ikm.komet.kview.mvvm.viewmodel;

import org.carlfx.cognitive.viewmodel.SimpleViewModel;

import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationDialogViewModel.ConfirmationPropertyName.CONFIRMATION_DIALOG_MESSAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationDialogViewModel.ConfirmationPropertyName.CONFIRMATION_DIALOG_TITLE;

public class ConfirmationDialogViewModel extends SimpleViewModel {

    /**
     * The properties of the view model.
     */
    public enum ConfirmationPropertyName {
        CONFIRMATION_DIALOG_TITLE,
        CONFIRMATION_DIALOG_MESSAGE
    }

    /**
     * Initialize the SimpleViewModel with the properties.
     */
    public ConfirmationDialogViewModel() {
        addProperty(CONFIRMATION_DIALOG_TITLE, "");
        addProperty(CONFIRMATION_DIALOG_MESSAGE, "");
    }

}
