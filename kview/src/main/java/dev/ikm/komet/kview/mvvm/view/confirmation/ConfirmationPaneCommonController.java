package dev.ikm.komet.kview.mvvm.view.confirmation;

import dev.ikm.komet.kview.mvvm.viewmodel.confirmation.ConfirmationPaneCommonViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

public class ConfirmationPaneCommonController {

    /**
     * button to close the properties bump out
     */
    @FXML
    private Button closePropsButton;

    /**
     * text heading at the top of this pane
     */
    @FXML
    private Label confirmationTitle;

    @FXML
    private Label confirmationMessage;

    /**
     * View model for display text and notification topic and event
     */
    @InjectViewModel
    private ConfirmationPaneCommonViewModel viewModel;

    /**
     * action fired by closing the properties bump out
     * @param event property panel event -> close panel
     */
    @FXML
    private void closeProperties(ActionEvent event) {
        viewModel.sendNotification();
    }

    @FXML
    public void initialize() {
        confirmationTitle.textProperty().bind(viewModel.getTitleProperty());
        confirmationMessage.textProperty().bind(viewModel.getMessageProperty());
    }

}
