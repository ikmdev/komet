package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.model.WindowModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class KLEditorWindowController {
    @FXML
    private Label titleLabel;

    public void initialize() {
        WindowModel windowModel = WindowModel.instance();
        titleLabel.textProperty().bind(windowModel.titleProperty());
    }
}