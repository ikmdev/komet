package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.controls.KLConceptNavigatorControl;
import javafx.fxml.FXML;
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
        conceptNavigatorControl.getRoot().getChildren().addAll(generateChildren(1));
    }


    private static List<TreeItem<String>> generateChildren(int level) {
        List<TreeItem<String>> children = new ArrayList<>();
        for (int idx = 0; idx < Math.max(5, new Random().nextInt(10)); idx++) {
            TreeItem<String> child = new TreeItem<>("Concept Navigator - item for level " + level + " and index " + idx);
            if (level < 10 && new Random().nextBoolean()) {
                child.getChildren().addAll(generateChildren(level + 1));
            }
            children.add(child);
        }
        return children;
    }
}