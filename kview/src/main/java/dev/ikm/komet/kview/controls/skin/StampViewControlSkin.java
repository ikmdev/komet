package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.StampViewControl;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.Node;

public class StampViewControlSkin extends SkinBase<StampViewControl> {
    private final VBox root;

    private final Label statusLabel;
    private final Label lastUpdatedLabel;
    private final Label authorLabel;
    private final Label moduleLabel;
    private final Label pathLabel;

    public StampViewControlSkin(StampViewControl control) {
        super(control);

        root = new VBox(2.0);
        root.setPrefWidth(208);
        root.getStyleClass().add("stamp-container");

        root.setOnMousePressed(this::onMousePressedOnStamp);

        // --- Status ---
        Label statusText = new Label("Status: ");
        statusText.getStyleClass().add("stamp-label");

        statusLabel = new Label();
        statusLabel.getStyleClass().add("stamp-text");
        statusLabel.textProperty().bind(control.statusProperty());

        HBox statusBox = new HBox(statusText, statusLabel);

        // --- Last Updated ---
        Label lastUpdatedText = new Label("Last Updated: ");
        lastUpdatedText.getStyleClass().add("stamp-label");

        lastUpdatedLabel = new Label();
        lastUpdatedLabel.getStyleClass().add("stamp-text");
        lastUpdatedLabel.textProperty().bind(control.lastUpdatedProperty());

        HBox lastUpdatedBox = new HBox(lastUpdatedText, lastUpdatedLabel);

        // --- Author ---
        Label authorText = new Label("Author: ");
        authorText.getStyleClass().add("stamp-label");

        authorLabel = new Label();
        authorLabel.getStyleClass().add("stamp-text");
        authorLabel.textProperty().bind(control.authorProperty());

        HBox authorBox = new HBox(authorText, authorLabel);

        // --- Module ---
        Label moduleText = new Label("Module: ");
        moduleText.getStyleClass().add("stamp-label");

        moduleLabel = new Label();
        moduleLabel.getStyleClass().add("stamp-text");
        moduleLabel.textProperty().bind(control.moduleProperty());

        HBox moduleBox = new HBox(moduleText, moduleLabel);

        // --- Path ---
        Label pathText = new Label("Path: ");
        pathText.getStyleClass().add("stamp-label");

        pathLabel = new Label();
        pathLabel.getStyleClass().add("stamp-text");
        pathLabel.textProperty().bind(control.pathProperty());

        HBox pathBox = new HBox(pathText, pathLabel);

        // Add all rows
        root.getChildren().addAll(statusBox, lastUpdatedBox, authorBox, moduleBox, pathBox);

        getChildren().add(root);
    }

    private void onMousePressedOnStamp(MouseEvent mouseEvent) {
        getSkinnable().setSelected(!getSkinnable().isSelected());
    }
}