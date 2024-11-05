package dev.ikm.komet.kview.mvvm.view.navigation;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel.PATTERN_COLLECTION;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel;
import dev.ikm.tinkar.common.id.PublicId;
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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;

import java.text.NumberFormat;
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

    private ListView<EntityFacade> patternListView;


    private Pane classicConceptNavigator;

    @InjectViewModel
    private PatternNavViewModel patternNavViewModel;

    public ConceptPatternNavController(Pane navigatorNodePanel) {
        classicConceptNavigator = navigatorNodePanel;
    }

    @FXML
    public void initialize() {
        patternNavigationPane = new Pane();
        patternListView = new ListView<>();

        patternNavigationPane.getChildren().add(patternListView);
        patternListView.getItems().clear();

        patternListView.setCellFactory(p -> {
                    return new ListCell<>() {
                        private final HBox patternHBox;
                        {
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            patternHBox = new HBox();
                        }

                        @Override
                        protected void updateItem(EntityFacade item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item == null || empty) {
                                setGraphic(null);
                            } else {
                                //will need the EntityFacade as the underlying value..???
                                //FIXME
                                //patternHBox.setFill(item);
                                JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
                                        .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
                                HBox patternHBox = (HBox) patternNavEntryJFXNode.node();

                                setGraphic(patternHBox);
                            }
                        }




                    };
                });

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
                if ((Integer)o1.nid() instanceof Integer nid1 && (Integer)o2.nid() instanceof Integer nid2) {
                    return NaturalOrder.compareStrings(viewProperties.calculator().getDescriptionTextOrNid(nid1),
                            viewProperties.calculator().getDescriptionTextOrNid(nid2));
                } else {
                    return NaturalOrder.compareStrings(o1.toString(), o2.toString());
                }
            });
            Platform.runLater(() -> this.patternListView.setItems(patternItems));

            for (EntityFacade patternItem : patternListView.getItems()) {
                ObservableList<SemanticRecord> patternChildren = FXCollections.observableArrayList();
                int patternNid = patternItem.nid();
                AtomicInteger childCount = new AtomicInteger();

                ListView instancesListView = new ListView<>();
                instancesListView.getItems().clear();
                PrimitiveData.get().forEachSemanticNidOfPattern(patternNid, semanticNid -> {
                    if (childCount.incrementAndGet() < maxChildrenInPatternViewer) {
                        patternChildren.add(EntityService.get().getEntityFast(semanticNid));
                    }
                });
                Platform.runLater(() -> instancesListView.setItems(patternChildren));
                // need to add the child ListView into the parent ListView...

            }
        });
    }



    // need to create a listView, and set the cell factory to populate it in code

    private List<Label> createInstances(List<SemanticRecord> listOfSemantics) {
        List<Label> instances = new ArrayList<>(listOfSemantics.size());
        listOfSemantics.forEach(s -> {
            Label instanceName = new Label(s.description());
            instanceName.getStyleClass().add("search-entry-description-label");
            instances.add(instanceName);
        });
        return instances;
    }


}
