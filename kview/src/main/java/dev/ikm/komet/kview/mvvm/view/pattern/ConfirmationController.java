package dev.ikm.komet.kview.mvvm.view.pattern;

import dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

public class ConfirmationController {

    @FXML
    private Label confirmationTitle;

    @FXML
    private Label confirmationMessage;

    @InjectViewModel
    private ConfirmationViewModel viewModel;

    @FXML
    public void closeProperties(ActionEvent actionEvent) {
        viewModel.sendNotification();
    }

    @FXML
    public void initialize() {
        confirmationTitle.textProperty().bind(viewModel.titleProperty());
        confirmationMessage.textProperty().bind(viewModel.messageProperty());
    }

}
