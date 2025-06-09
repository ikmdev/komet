package dev.ikm.komet.kview.mvvm.view.confirmation;

import dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel.ConfirmationPropertyName.*;

/**
 * Controller of the Confirmation pane that has a Title label, Message label, and Close Properties Pane button.
 * The labels are bound to the view model properties, where the text comes from the view model.
 * The Close Properties Pane button action method calls the view model to send the notification to the
 * provided topic using the provided event.  This is done because the confirmation pane is used in different
 * windows within Komet, and each window has it's own event topic on the event bus.
 */
public class ConfirmationPaneController {

    /**
     * The resource file location of the FMXL file for this controller
     */
    public static final String FXML_FILE = "/dev/ikm/komet/kview/mvvm/view/confirmation/confirmation-pane-common.fxml";

    /**
     * The view model instance variable name of this class.
     */
    public static final String VIEW_MODEL_NAME = "viewModel";

    /**
     * Button to close the properties bump out
     */
    @FXML
    private Button closePropsButton;

    /**
     * Title text label at the top of this pane
     */
    @FXML
    private Label confirmationTitle;

    /**
     * Message text label
     */
    @FXML
    private Label confirmationMessage;

    /**
     * View model for confirmation display text and notification topic and event
     */
    @InjectViewModel
    private ConfirmationPaneViewModel viewModel;

    /**
     * Action fired by closing the properties bump out
     * @param event property panel event -> close panel
     */
    @FXML
    private void closeProperties(ActionEvent event) {
        viewModel.setPropertyValue(CLOSE_CONFIRMATION_PANEL, true);
    }

    /**
     * Bind the view model properties to the view components.
     */
    @FXML
    public void initialize() {
        confirmationTitle.textProperty().bind(viewModel.getProperty(CONFIRMATION_TITLE));
        confirmationMessage.textProperty().bind(viewModel.getProperty(CONFIRMATION_MESSAGE));
    }

}
