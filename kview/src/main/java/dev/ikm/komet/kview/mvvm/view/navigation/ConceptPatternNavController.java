package dev.ikm.komet.kview.mvvm.view.navigation;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.stream.IntStream;

public class ConceptPatternNavController {

    public static final String PATTERN_NAV_ENTRY_FXML = "pattern-nav-entry.fxml";

    @FXML
    private ToggleGroup conPatToggleGroup;

    @FXML
    private ToggleButton conceptsToggleButton;

    @FXML
    private ToggleButton patternsToggleButton;

    @FXML
    private BorderPane navContentPane;


    private Pane patternNavigationPane;


    private Pane classicConceptNavigator;

    public ConceptPatternNavController(Pane navigatorNodePanel) {
        classicConceptNavigator = navigatorNodePanel;
    }

    @FXML
    public void initialize() {
        patternNavigationPane = new Pane();
        patternNavigationPane.getChildren().add(addPatternComponent());

        // default to classic concept navigation
        navContentPane.setCenter(classicConceptNavigator);
    }

    @FXML
    private void showConcepts() {
        navContentPane.setCenter(classicConceptNavigator);
    }

    @FXML
    private void showPatterns() {
        navContentPane.setCenter(patternNavigationPane);
    }

    public VBox addPatternComponent() {
        //FIXME this is mocked up
        VBox resultsVBox = new VBox();
        IntStream.range(0, 3).forEachOrdered(n -> {
            JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
                    .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
            HBox pattern = (HBox) patternNavEntryJFXNode.node();
            PatternNavEntryController controller = patternNavEntryJFXNode.controller();
            resultsVBox.setSpacing(4); // space between pattern entries
            pattern.setAlignment(Pos.CENTER);
            Region leftPadding = new Region();
            leftPadding.setPrefWidth(12); // pad each entry with an empty region
            leftPadding.setPrefHeight(1);
            if (n > 0) {
                controller.setPatternName("Test Performed Pattern");
            }
            resultsVBox.getChildren().addAll(new HBox(leftPadding, pattern));
        });
        return resultsVBox;
    }

}
