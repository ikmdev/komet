package dev.ikm.komet.kview.mvvm.view.navigation;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

public class ConceptPatternNavController {

    @FXML
    private ToggleGroup conPatToggleGroup;

    @FXML
    private ToggleButton conceptsToggleButton;

    @FXML
    private ToggleButton patternsToggleButton;

    @FXML
    private Pane navContentPane;

    private Pane classicConceptNavigator;

    public ConceptPatternNavController(/*Pane navigatorNodePanel*/) {
//        classicConceptNavigator = navigatorNodePanel;
//
//        navContentPane = classicConceptNavigator;
    }

    @FXML
    public void initialize() {
        // by default the concept button toggle is selected and the concepts are showm

    }

    private void showClassicConceptNavigation(Pane classic) {

    }


}
