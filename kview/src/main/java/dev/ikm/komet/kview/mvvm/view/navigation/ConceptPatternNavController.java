package dev.ikm.komet.kview.mvvm.view.navigation;


import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.ConceptNavigatorUtils;
import dev.ikm.komet.kview.controls.InvertedTree;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.kview.controls.KLSearchControl;
import dev.ikm.komet.kview.events.pattern.PatternSavedEvent;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.komet.navigator.graph.ViewNavigator;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.PATTERN;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.INSTANCES;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.PATTERN_FACADE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

public class ConceptPatternNavController {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptPatternNavController.class);

    public static final String PATTERN_NAV_ENTRY_FXML = "pattern-nav-entry.fxml";

    private static final int maxChildrenInPatternViewer = 150;

    @FXML
    private ToggleGroup conPatToggleGroup;

    @FXML
    private ToggleButton conceptsToggleButton;

    @FXML
    private ToggleButton patternsToggleButton;

    @FXML
    private BorderPane navContentPane;

    private ScrollPane patternNavigationPane;

    private VBox patternsVBox;


    private Pane conceptNavigator;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox scrollPaneContent;

    @InjectViewModel
    private PatternNavViewModel patternNavViewModel;

    private JournalController journalController;

    private Subscriber<PatternSavedEvent> patternCreationEventSubscriber;
    private Subscriber<RefreshCalculatorCacheEvent> refreshCalculatorEventSubscriber;

    private KLConceptNavigatorControl conceptNavigatorControl;

    public ConceptPatternNavController(JournalController journalController) {
        this.journalController = journalController;
    }

    @FXML
    public void initialize() {
        patternNavigationPane = new ScrollPane();
        patternNavigationPane.setFitToWidth(true);
        patternNavigationPane.getStyleClass().add("pattern-navigation-scroll-pane");

        patternsVBox = new VBox();
        patternsVBox.getStyleClass().add("pattern-navigation-container");

        patternNavigationPane.setContent(patternsVBox);

        patternsVBox.getChildren().clear();

        // default to classic concept navigation
        conceptsToggleButton.setSelected(true);

        conceptNavigator = loadConceptNavigatorPanel();

        // set the Pattern ScrollPane preferred width so the toggle button's
        // panel won't change widths when button selected

        Platform.runLater(() -> {
            patternNavigationPane.setPrefWidth(conceptNavigator.getPrefWidth());
            patternNavigationPane.setMinWidth(conceptNavigator.getWidth());
        });

        navContentPane.setCenter(conceptNavigator);

        // set up listeners when the toggle button changes
        conPatToggleGroup.selectedToggleProperty().subscribe((oldVal, newVal) -> {
            // if they click the same toggle twice, we can just ignore it
            if (newVal == null) {
                if (oldVal.equals(conceptsToggleButton)) {
                    showConcepts();
                } else if (oldVal.equals(patternsToggleButton)) {
                    showPatterns();
                }
                return;
            }
            if (newVal.equals(conceptsToggleButton)) {
                showConcepts();
            } else {
                showPatterns();
            }
        });

        // Refresh the patterns and semantics TODO: FIXME Primitive Data has caches that don't refresh. See reload() method.
        patternCreationEventSubscriber = _ -> {
            LOG.info("A New Pattern has been added/created. Reloading all the Patterns.");
            patternsVBox.getChildren().clear();
            patternNavViewModel.reload();
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(SAVE_PATTERN_TOPIC, PatternSavedEvent.class, patternCreationEventSubscriber);

        ViewProperties viewProperties = patternNavViewModel.getPropertyValue(VIEW_PROPERTIES);

        // callback when all patterns are loaded. For each build up children instances.
        patternNavViewModel.setOnReload(stream -> {
            stream.forEach(patternItem -> {
                int patternNid = patternItem.nid();
                // load the pattern instances into an observable list
                ObservableList<Object> patternChildren = FXCollections.observableArrayList();
                AtomicInteger childCount = new AtomicInteger();
                // populate the collection of instance for each pattern
                PrimitiveData.get().forEachSemanticNidOfPattern(patternNid, semanticNid -> {
                    if (childCount.incrementAndGet() < maxChildrenInPatternViewer) {
                        patternChildren.add(semanticNid);
                    }
                });

                if (childCount.get() >= maxChildrenInPatternViewer) {
                    NumberFormat numberFormat = NumberFormat.getInstance();
                    patternChildren.add(numberFormat.format(childCount.get() - maxChildrenInPatternViewer) + " additional semantics suppressed...");
                }

                Platform.runLater(() -> {
                    // load the pattern entry FXML and controller
                    Config patternInstanceConfig = new Config()
                            .fxml(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML))
                            .updateViewModel("instancesViewModel", viewModel ->
                                    viewModel.addProperty(VIEW_PROPERTIES, viewProperties)
                                            .addProperty(CURRENT_JOURNAL_WINDOW_TOPIC, (Property) patternNavViewModel.getProperty(CURRENT_JOURNAL_WINDOW_TOPIC))
                                            .addProperty(PATTERN_FACADE, patternItem)
                                            .addProperty(INSTANCES, patternChildren, true)
                            );

                    JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader.make(patternInstanceConfig);

                    HBox patternHBox = (HBox) patternNavEntryJFXNode.node();
                    patternHBox.getStyleClass().add("pattern-instance-container");

                    setUpDraggable(patternHBox, patternItem, PATTERN);

                    patternsVBox.getChildren().add(patternHBox);
                });
            });
        });


        // initial loading of the patterns.
        patternNavViewModel.reload();
    }

    /**
     * Creates the Nextgen ConceptNavigator tree view
     */
    private Pane loadConceptNavigatorPanel() {
        ViewProperties viewProperties = patternNavViewModel.getPropertyValue(VIEW_PROPERTIES);
        Navigator navigator = new ViewNavigator(viewProperties.nodeView());
        KLSearchControl searchControl = new KLSearchControl();
        searchControl.setNavigator(navigator);
        searchControl.setOnAction(_ -> {
            ViewCalculator calculator = viewProperties.calculator();
            searchControl.setResultsPlaceholder("Searching..."); // DUMMY, resources?
            TinkExecutor.threadPool().execute(() -> {
                try {
                    List<LatestVersionSearchResult> results = calculator.search(searchControl.getText(), 1000).toList();
                    results.sort((o1, o2) -> Float.compare(o2.score(), o1.score()));
                    Map<Integer, List<LatestVersionSearchResult>> topNidMatchMap = new LinkedHashMap<>();
                    results.forEach(result -> topNidMatchMap.computeIfAbsent(result.latestVersion().get()
                            .chronology().topEnclosingComponentNid(), _ -> new ArrayList<>()).add(result));
                    Map<KLSearchControl.SearchResult, List<LatestVersionSearchResult>> searchResultsMap = new LinkedHashMap<>();
                    topNidMatchMap.keySet().forEach(key ->
                            navigator.getViewCalculator().latest(key).ifPresent(_ -> {
                                // Add one search result per parent, ignoring concepts or patterns that don't have a parent
                                for (int parentNid : navigator.getParentNids(key)) {
                                    searchResultsMap.put(new KLSearchControl.SearchResult(ConceptFacade.make(parentNid),
                                            ConceptFacade.make(key), searchControl.getText()), topNidMatchMap.get(key));
                                }
                            }));
                    searchResultsMap.forEach((_, v) -> v.sort((o1, o2) -> Float.compare(o1.score(), o2.score())));

                    List<Map.Entry<KLSearchControl.SearchResult, List<LatestVersionSearchResult>>> entries =
                            new ArrayList<>(searchResultsMap.entrySet());
                    entries.sort((m1, m2) -> Float.compare(m2.getValue().getFirst().score(), m1.getValue().getFirst().score()));
                    // ignore map.Entry::values for now (a list of description semantics)
                    List<KLSearchControl.SearchResult> searchResults = entries.stream().map(Map.Entry::getKey).toList();

                    Platform.runLater(() -> {
                        searchControl.setResultsPlaceholder(null);
                        searchControl.resultsProperty().addAll(searchResults);
                    });

                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        });

        searchControl.setOnFilterAction(_ -> {
            // TODO
        });

        conceptNavigatorControl = new KLConceptNavigatorControl();
        conceptNavigatorControl.setNavigator(navigator);
        conceptNavigatorControl.setHeader("Concept Header");
        conceptNavigatorControl.setShowTags(false);
        conceptNavigatorControl.setOnAction(action -> switch (action) {
            // single selection
            case OPEN_IN_WORKSPACE -> journalController::createConceptWindow;
            case SHOW_RELATED_CONCEPTS -> {
                // Dummy, for now just add the parents of the selected item as related content:
                TreeItem<ConceptFacade> selectedItem = conceptNavigatorControl.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    conceptNavigatorControl.getNavigator().getParentNids(selectedItem.getValue().nid());
                    List<ConceptFacade> list = Arrays.stream(conceptNavigatorControl.getNavigator().getParentNids(selectedItem.getValue().nid())).boxed()
                            .map(nid -> (ConceptFacade) Entity.getFast(nid)).toList();
                    ((ConceptNavigatorTreeItem) selectedItem).setRelatedConcepts(list);
                }
                yield i -> LOG.info("Click on {}", i.description());
            }
            // multiple selection
            case POPULATE_SELECTION -> journalController::createConceptWindow;
            case SEND_TO_JOURNAL, SEND_TO_CHAPTER, COPY, SAVE_TO_FAVORITES -> _ -> {}; // TODO: Add implementation
        });
        searchControl.setOnLongHover(conceptNavigatorControl::expandAndHighlightConcept);
        searchControl.setOnSearchResultClick(_ -> conceptNavigatorControl.unhighlightConceptsWithDelay());
        searchControl.setOnClearSearch(_ -> ConceptNavigatorUtils.resetConceptNavigator(conceptNavigatorControl));

        VBox nodePanel = new VBox(searchControl, conceptNavigatorControl);
        nodePanel.getStyleClass().add("concept-navigator-container");
        VBox.setVgrow(conceptNavigatorControl, Priority.ALWAYS);

        return nodePanel;
    }

    @FXML
    private void showConcepts() {
        navContentPane.setCenter(conceptNavigator);
        conceptsToggleButton.setSelected(true);
    }

    @FXML
    private void showPatterns() {
        navContentPane.setCenter(patternNavigationPane);
        patternsToggleButton.setSelected(true);
    }

    private void setUpDraggable(Node node, EntityFacade entity, DragAndDropType dropType) {
        Objects.requireNonNull(node, "The node must not be null.");
        Objects.requireNonNull(entity, "The entity must not be null.");

        // Associate the node with the entity's public ID and type for later retrieval or identification
        node.setUserData(new DragAndDropInfo(dropType, entity.publicId()));

        // Set up the drag detection event handler
        node.setOnDragDetected(mouseEvent -> {
            // Initiate a drag-and-drop gesture with copy or move transfer mode
            Dragboard dragboard = node.startDragAndDrop(TransferMode.COPY_OR_MOVE);

            // Create the content to be placed on the dragboard
            // Here, KometClipboard is used to encapsulate the entity's unique identifier (nid)
            KometClipboard content = new KometClipboard(EntityFacade.make(entity.nid()));

            // Generate the drag image using DragImageMaker
            DragImageMaker dragImageMaker = new DragImageMaker(node);
            Image dragImage = dragImageMaker.getDragImage();
            // Set the drag image on the dragboard
            if (dragImage != null) {
                dragboard.setDragView(dragImage);
            }

            // Place the content on the dragboard
            dragboard.setContent(content);

            // Log the drag event details for debugging or auditing
            LOG.info("Drag detected on node: " + mouseEvent.toString());

            // Consume the mouse event to prevent further processing
            mouseEvent.consume();
        });
    }

    public void toggleConcepts() {
        conceptsToggleButton.setSelected(true);
    }

    public void showConcept(final int conceptNid) {
        if (conceptNavigatorControl == null) {
            LOG.error("Concept navigator control is null");
            return;
        }
        conceptNavigatorControl.expandAndSelectConcept(new InvertedTree.ConceptItem(-1, conceptNid, ""));
    }
}