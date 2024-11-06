package dev.ikm.komet.kview.mvvm.view.navigation;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel.PATTERN_COLLECTION;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ConceptPatternNavController {

    public static final String PATTERN_NAV_ENTRY_FXML = "pattern-nav-entry-listview.fxml";

    //FIXME remove this and have a scrollpane instead for the instances' listView
    private static final int maxChildrenInPatternViewer = 10;

    @FXML
    private ToggleGroup conPatToggleGroup;

    @FXML
    private ToggleButton conceptsToggleButton;

    @FXML
    private ToggleButton patternsToggleButton;

    @FXML
    private BorderPane navContentPane;

    private Pane patternNavigationPane;

    private VBox patternsVBox;


    private Pane classicConceptNavigator;

    @InjectViewModel
    private PatternNavViewModel patternNavViewModel;

    public ConceptPatternNavController(Pane navigatorNodePanel) {
        classicConceptNavigator = navigatorNodePanel;
    }

    @FXML
    public void initialize() {
        patternNavigationPane = new Pane();
        patternsVBox = new VBox();

        patternNavigationPane.getChildren().add(patternsVBox);
        patternsVBox.getChildren().clear();

        //FIXME save this for the instances
//        patternListView.setCellFactory(p -> {
//                    return new ListCell<>() {
//                        private final HBox patternHBox;
//                        {
//                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//                            patternHBox = new HBox();
//                        }
//
//                        @Override
//                        protected void updateItem(EntityFacade item, boolean empty) {
//                            super.updateItem(item, empty);
//
//                            if (item == null || empty) {
//                                setGraphic(null);
//                            } else {
//                                //will need the EntityFacade as the underlying value..???
//                                //FIXME
//                                //patternHBox.setFill(item);
//                                JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
//                                        .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
//                                HBox patternHBox = (HBox) patternNavEntryJFXNode.node();
//
//                                setGraphic(patternHBox);
//                            }
//                        }
//                    };
//                });

        // default to classic concept navigation
        navContentPane.setCenter(classicConceptNavigator);
        loadAllPatterns();

        /*
        Cognitive notes:

        ListView already has an internal observable list...

        for MVVM we can have a parallel list, and we can bidirectionally bind them but that will be a ton of objects in memory so we won't do that
        since this is read only

        (cell factory is a HBox for each instance...)
         */

    }

    @FXML
    private void showConcepts() {
        navContentPane.setCenter(classicConceptNavigator);
    }

    @FXML
    private void showPatterns() {
        navContentPane.setCenter(patternNavigationPane);
    }

    public void loadPatternsIntoPane(Map<EntityFacade, List<SemanticRecord>> patterns) {
        if (patterns.isEmpty()) {
            return;
        }
        patternNavViewModel.setPropertyValue(PATTERN_COLLECTION, patterns);

        //VBox resultsVBox = new VBox(); //FIXME turn into a ListView


        patterns.forEach((pattern, listOfSemantics) -> {
            JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
                    .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
            HBox patternHBox = (HBox) patternNavEntryJFXNode.node();


            PatternNavEntryController controller = patternNavEntryJFXNode.controller();


//            resultsVBox.setSpacing(4); // space between pattern entries
//            patternHBox.setAlignment(Pos.CENTER);
//            Region leftPadding = new Region();
//            leftPadding.setPrefWidth(12); // pad each entry with an empty region
//            leftPadding.setPrefHeight(1);

            controller.setPatternName(pattern.description());

//            controller.getInstancesVBox().getChildren().clear();
//            controller.getInstancesVBox().getChildren().addAll(createInstances(listOfSemantics));


            //resultsVBox.getChildren().addAll(new HBox(leftPadding, patternHBox));
        });
    }

    public void loadAllPatterns() {
        ViewProperties viewProperties = patternNavViewModel.getPropertyValue(VIEW_PROPERTIES);

        TinkExecutor.threadPool().execute(() -> {
            ObservableList<EntityFacade> patternItems = FXCollections.observableArrayList();
            PrimitiveData.get().forEachPatternNid(patternNid -> {
                Latest<PatternEntityVersion> latestPattern = viewProperties.calculator().latest(patternNid);
                latestPattern.ifPresent(patternEntityVersion -> {
                    if (EntityService.get().getEntity(patternNid).isPresent()) {
                        patternItems.add(EntityService.get().getEntity(patternNid).get());
                    }
                });
            });
            patternItems.sort((o1, o2) -> {
                if ((Integer) o1.nid() instanceof Integer nid1 && (Integer) o2.nid() instanceof Integer nid2) {
                    return NaturalOrder.compareStrings(viewProperties.calculator().getDescriptionTextOrNid(nid1),
                            viewProperties.calculator().getDescriptionTextOrNid(nid2));
                } else {
                    return NaturalOrder.compareStrings(o1.toString(), o2.toString());
                }
            });
            Platform.runLater(() -> {
                //this.patternsVBox.setItems(patternItems)
                //TODO do a .sorted() in front of the forEach and move the above sort into it
                patternItems.stream().forEach(patternItem -> { // each pattern row
                    // load the pattern instances into an observable list
                    ObservableList<SemanticRecord> patternChildren = FXCollections.observableArrayList();
                    int patternNid = patternItem.nid();
                    AtomicInteger childCount = new AtomicInteger();
                    //Platform.runLater(() -> {
                    // populate the collection of instance for each pattern
                        PrimitiveData.get().forEachSemanticNidOfPattern(patternNid, semanticNid -> {
                            if (childCount.incrementAndGet() < maxChildrenInPatternViewer) {
                                patternChildren.add(EntityService.get().getEntityFast(semanticNid));
                            }
                        });
                    //});
                    // load the pattern entry FXML and controller
                    JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
                            .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
                    HBox patternHBox = (HBox) patternNavEntryJFXNode.node();
                    PatternNavEntryController patternNavEntryController = patternNavEntryJFXNode.controller();

                    patternsVBox.setSpacing(4); // space between pattern entries
                    patternHBox.setAlignment(Pos.CENTER);
                    Region leftPadding = new Region();
                    leftPadding.setPrefWidth(12); // pad each entry with an empty region
                    leftPadding.setPrefHeight(1);
                    patternsVBox.getChildren().addAll(new HBox(leftPadding, patternHBox));
                    // set the pattern's name
                    patternNavEntryController.setPatternName(patternItem.description());

                    // populate the pattern instances as a list view
                    ListView<SemanticRecord> patternInstances = patternNavEntryController.getPatternInstancesListView();

                    // set the cell factory for each pattern's instances list
                    patternInstances.setCellFactory(p -> new ListCell<>() {
                        private final Label label;

                        {
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            label = new Label();
                        }

                        @Override
                        protected void updateItem(SemanticRecord item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item == null || empty) {
                                setGraphic(null);
                            } else {

                                String name = viewProperties.calculator().getRegularDescriptionText(item).orElse("");
                                label.setText(name);
                                label.getStyleClass().add("search-entry-description-label");
                                setGraphic(label);
                            }
                        }
                    });

                    Platform.runLater(() -> {
                        patternInstances.setItems(patternChildren);
                        //if (patternChildren.size() == 0) {
                        //    patternNavEntryController.disableInstancesListView();
                        //}
                    });



                });

            });
        });
    }

}
