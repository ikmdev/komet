package dev.ikm.komet.kview.mvvm.viewmodel;

import org.carlfx.cognitive.viewmodel.SimpleViewModel;

/**
 * The view model for the confirmation pane.  This view model manages the text that is displayed
 * on the confirmation pane, and the notification topic and event that is sent when the close
 * button is pressed.
 */
public class ConfirmationPaneViewModel extends SimpleViewModel {

    /**
     * The properties of the view model.
     */
    public enum ConfirmationPropertyName {
        CONFIRMATION_TITLE,
        CONFIRMATION_MESSAGE,
        CLOSE_CONFIRMATION_PANEL
    }

    /**
     * Initialize the SimpleViewModel with the properties.
     */
    public ConfirmationPaneViewModel() {
        addProperty(ConfirmationPropertyName.CONFIRMATION_TITLE, "");
        addProperty(ConfirmationPropertyName.CONFIRMATION_MESSAGE, "");
        addProperty(ConfirmationPropertyName.CLOSE_CONFIRMATION_PANEL, false);
    }

}
