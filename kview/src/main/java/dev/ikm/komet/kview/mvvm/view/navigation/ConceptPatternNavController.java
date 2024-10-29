package dev.ikm.komet.kview.mvvm.view.navigation;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

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
        JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
                .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
        Node pattern = patternNavEntryJFXNode.node();
        PatternNavEntryController controller = patternNavEntryJFXNode.controller();
        VBox resultsVBox = new VBox();
        resultsVBox.getChildren().add(pattern);

        return resultsVBox;
    }

    private void showClassicConceptNavigation(Pane classic) {

    }

    public void showPatternNavigation() {

    }


}
