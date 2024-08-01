package dev.ikm.komet.kview.mvvm.view.pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternFieldsController.class);

    @FXML
    private Button cancelButton;

    @FXML
    private Button doneButton;

    @FXML
    private void clearView(ActionEvent actionEvent) {
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
    }
}
