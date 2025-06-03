package dev.ikm.komet.kview.mvvm.view.genediting;

import dev.ikm.komet.kview.mvvm.viewmodel.ClosePropertiesViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

/**
 * controller for the confirmation screen post save on the
 * general editing/authoring bump out screen
 */
public class ClosePropertiesController {

    /**
     * button to close the properties bump out
     */
    @FXML
    private Button closePropsButton;

    /**
     * text heading at the top of this pane
     */
    @FXML
    private Label headingText;

    @FXML
    private Label messageText;

    /**
     * View model for display text and notification topic and event
     */
    @InjectViewModel
    private ClosePropertiesViewModel viewModel;

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
        headingText.textProperty().bind(viewModel.titleProperty());
        messageText.textProperty().bind(viewModel.messageProperty());
    }

}
