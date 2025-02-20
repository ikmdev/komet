package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.controls.ConceptNavigatorModel;
import dev.ikm.komet.controls.KLConceptNavigatorControl;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static dev.ikm.komet.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;

public class SamplerConceptNavigatorController {

    @FXML
    private Label samplerDescription;

    @FXML
    private KLConceptNavigatorControl conceptNavigatorControl;

    @FXML
    private VBox conceptArea;

    private static final String STYLE = """
            data:text/css,
            
            .sample-control-container > .center-container {
                -fx-background-color: #fbfbfb;
                -fx-border-color: #e6e6e6;
                -fx-background-radius: 5px;
                -fx-border-radius: 5px;
                -fx-alignment: center;
                -fx-padding: 1.5em;
            }
            
            .sample-control-container > .center-container.dashed-border {
                -fx-border-color: #5DCF16;
                -fx-border-width: 3;
                -fx-border-style: segments(15, 12, 15, 12) line-cap round;
            }
            
            .sample-control-container > .center-container > .label {
                -fx-font-family: "Noto Sans";
                -fx-font-size: 12;
                -fx-font-weight: 600;
                -fx-text-fill: #2E3240;
                -fx-alignment: center-left;
                -fx-wrap-text: true;
            }
            
            """;

    public void initialize() {
        samplerDescription.setText("The Concept Navigator control is a tree view to display a hierarchy of concepts");
        conceptNavigatorControl.setHeader("Concept Header");
        conceptNavigatorControl.setOnDoubleClick(item ->
                conceptArea.getChildren().setAll(new Label(item.getText())));

        conceptNavigatorControl.getRoot().getChildren().addAll(generateChildren(1));
        conceptArea.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)) {
                Dragboard dragboard = event.getDragboard();
                conceptArea.getChildren().setAll(new Label(dragboard.getString()));
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        conceptArea.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        conceptArea.setOnDragEntered(event -> {
            if (event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)) {
                if (!conceptArea.getStyleClass().contains("dashed-border")) {
                    conceptArea.getStyleClass().add("dashed-border");
                }
                event.consume();
            }
        });
        conceptArea.setOnDragExited(event -> {
            conceptArea.getStyleClass().remove("dashed-border");
            event.consume();
        });

        conceptArea.sceneProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (conceptArea.getScene() != null) {
                    conceptArea.getScene().getStylesheets().add(STYLE);
                    conceptArea.sceneProperty().removeListener(this);
                }
            }
        });
    }

    private static List<TreeItem<ConceptNavigatorModel>> generateChildren(int level) {
        List<TreeItem<ConceptNavigatorModel>> children = new ArrayList<>();
        for (int idx = 0; idx < Math.max(5, new Random().nextInt(10)); idx++) {
            ConceptNavigatorModel conceptNavigatorModel = new ConceptNavigatorModel("Concept Navigator - this is text for item for level " + level + " and index " + idx);
            conceptNavigatorModel.setDefined(new Random().nextInt(10) < 2);
            TreeItem<ConceptNavigatorModel> child = new TreeItem<>(conceptNavigatorModel);
            if (level < 10 && new Random().nextBoolean()) {
                child.getChildren().addAll(generateChildren(level + 1));
            }
            children.add(child);
        }
        return children;
    }
}