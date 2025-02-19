package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.controls.ConceptNavigatorModel;
import dev.ikm.komet.controls.KLConceptNavigatorControl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SamplerConceptNavigatorController {

    @FXML
    private Label samplerDescription;

    @FXML
    private KLConceptNavigatorControl conceptNavigatorControl;

    public void initialize() {
        samplerDescription.setText("The Concept Navigator control is a tree view to display a hierarchy of concepts");
        conceptNavigatorControl.setHeader("Concept Header");
        conceptNavigatorControl.setOnDoubleClick(item -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setContentText(item.toString());
            alert.showAndWait();
        });
        conceptNavigatorControl.getRoot().getChildren().addAll(generateChildren(1));
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