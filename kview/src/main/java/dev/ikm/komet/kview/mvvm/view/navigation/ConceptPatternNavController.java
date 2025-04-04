package dev.ikm.komet.kview.mvvm.view.navigation;


import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.kview.events.pattern.PatternCreationEvent.PATTERN_CREATION_EVENT;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.PATTERN;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.INSTANCES;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.PATTERN_FACADE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.pattern.PatternCreationEvent;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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


    private Pane classicConceptNavigator;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox scrollPaneContent;

    @InjectViewModel
    private PatternNavViewModel patternNavViewModel;

    private Subscriber<PatternCreationEvent> patternCreationEventSubscriber;

    public ConceptPatternNavController(Pane navigatorNodePanel) {
        classicConceptNavigator = navigatorNodePanel;
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
        navContentPane.setCenter(classicConceptNavigator);

        // set up listeners when the toggle button changes
        conPatToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle oldVal, Toggle newVal) {
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
            }
        });
        //conceptsToggleButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> showConcepts());
        //patternsToggleButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> showPatterns());

        patternCreationEventSubscriber = (evt) -> {
            LOG.info("A New Pattern has been added/created. Reloading all the Patterns.");
            if(evt.getEventType() == PATTERN_CREATION_EVENT){
                LOG.info("A New Pattern has been added/created. Reloading all the Patterns.");
                patternsVBox.getChildren().clear();
                patternNavViewModel.reload();
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(SAVE_PATTERN_TOPIC, PatternCreationEvent.class, patternCreationEventSubscriber);

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

    @FXML
    private void showConcepts() {
        navContentPane.setCenter(classicConceptNavigator);
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
}