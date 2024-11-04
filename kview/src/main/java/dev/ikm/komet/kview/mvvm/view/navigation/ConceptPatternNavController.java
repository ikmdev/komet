package dev.ikm.komet.kview.mvvm.view.navigation;

import static dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel.PATTERN_COLLECTION;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.List;

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

    @InjectViewModel
    private PatternNavViewModel patternNavViewModel;

    public ConceptPatternNavController(Pane navigatorNodePanel) {
        classicConceptNavigator = navigatorNodePanel;
    }

    @FXML
    public void initialize() {
        patternNavigationPane = new Pane();

        // default to classic concept navigation
        navContentPane.setCenter(classicConceptNavigator);
        loadPatterns();
    }

    private void loadPatterns() {
        loadPatternsIntoPane(patternNavViewModel.loadAllPatterns());
    }

    @FXML
    private void showConcepts() {
        navContentPane.setCenter(classicConceptNavigator);
    }

    @FXML
    private void showPatterns() {
        //TODO get the patterns and refactor the mock code to load them
        navContentPane.setCenter(patternNavigationPane);
    }

    public void loadPatternsIntoPane(List<EntityFacade> patterns) {
        if (patterns.isEmpty()) {
            return;
        }
        patternNavViewModel.setPropertyValue(PATTERN_COLLECTION, patterns);

        VBox resultsVBox = new VBox();
        patternNavigationPane.getChildren().add(resultsVBox);
        patterns.forEach(p -> {
            JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
                    .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
            HBox pattern = (HBox) patternNavEntryJFXNode.node();

            PatternNavEntryController controller = patternNavEntryJFXNode.controller();
            resultsVBox.setSpacing(4); // space between pattern entries
            pattern.setAlignment(Pos.CENTER);
            Region leftPadding = new Region();
            leftPadding.setPrefWidth(12); // pad each entry with an empty region
            leftPadding.setPrefHeight(1);

            controller.setPatternName(p.description());

            resultsVBox.getChildren().addAll(new HBox(leftPadding, pattern));
        });
    }


}
