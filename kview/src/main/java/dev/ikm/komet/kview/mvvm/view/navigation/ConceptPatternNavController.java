package dev.ikm.komet.kview.mvvm.view.navigation;


import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.kview.events.pattern.PatternCreationEvent.PATTERN_CREATION_EVENT;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.PATTERN;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.IDENTIFIER_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.INFERRED_DEFINITION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.INFERRED_NAVIGATION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.PATH_MEMBERSHIP_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.STATED_DEFINITION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.STATED_NAVIGATION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.UK_DIALECT_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.US_DIALECT_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.VERSION_CONTROL_PATH_ORIGIN_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent;
import dev.ikm.komet.kview.events.pattern.PatternCreationEvent;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.time.Instant;
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
        patternsVBox = new VBox();

        patternNavigationPane.setContent(patternsVBox);
        patternsVBox.getChildren().clear();

        // default to classic concept navigation
        navContentPane.setCenter(classicConceptNavigator);

        patternCreationEventSubscriber = (evt) -> {
            LOG.info("A New Pattern has been added/created. Reloading all the Patterns.");
            if(evt.getEventType() == PATTERN_CREATION_EVENT){
                LOG.info("A New Pattern has been added/created. Reloading all the Patterns.");
                patternsVBox.getChildren().clear();
                loadAllPatterns();
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(SAVE_PATTERN_TOPIC, PatternCreationEvent.class, patternCreationEventSubscriber);

        loadAllPatterns();

        System.out.println("TESTING");
    }




    @FXML
    private void showConcepts() {
        navContentPane.setCenter(classicConceptNavigator);
    }

    @FXML
    private void showPatterns() {
        navContentPane.setCenter(patternNavigationPane);
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
                patternItems.stream().forEach(patternItem -> { // each pattern row
                    // load the pattern instances into an observable list
                    ObservableList<Object> patternChildren = FXCollections.observableArrayList();
                    int patternNid = patternItem.nid();
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
                    boolean hasChildren = patternChildren.size() > 0;

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
                    if (!hasChildren) {
                        patternNavEntryController.disableInstancesListView();
                    }

                    // add listener for double click to summon the pattern into the journal view
                    patternHBox.setOnMouseClicked(mouseEvent -> {
                        // double left click creates the concept window
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                            if (mouseEvent.getClickCount() == 2) {
                                EvtBusFactory.getDefaultEvtBus().publish(patternNavViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                                        new MakePatternWindowEvent(this,
                                        MakePatternWindowEvent.OPEN_PATTERN, patternItem, viewProperties));
                            }
                        }
                    });
                    PatternEntity patternEntity = EntityService.get().getEntityFast(patternItem.nid());
                    setUpDraggable(patternHBox, patternEntity, PATTERN);

                    patternsVBox.getChildren().addAll(new HBox(leftPadding, patternHBox));
                    // set the pattern's name
                    patternNavEntryController.setPatternName(patternItem.description());

                    // populate the pattern instances as a list view
                    ListView<Object> patternInstances = patternNavEntryController.getPatternInstancesListView();

                    // set the cell factory for each pattern's instances list
                    patternInstances.setCellFactory(p -> new ListCell<>() {
                        private final Label label;
                        {
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            label = new Label();
                        }

                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(null);
                            if (item != null && !empty) {
                                if (item instanceof String stringItem) {
                                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                                    setText(stringItem);
                                } else if (item instanceof Integer nid) {
                                    String entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid);
                                    Entity entity = Entity.getFast(nid);
                                    if (entity instanceof SemanticEntity<?> semanticEntity) {
                                        if (semanticEntity.patternNid() == IDENTIFIER_PATTERN_PROXY.nid()) {
                                            //TODO Move better string descriptions to language calculator
                                            Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
                                            ImmutableList fields = latestId.get().fieldValues();
                                            entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                                    ": " + fields.get(1);
                                        } else if (semanticEntity.patternNid() == INFERRED_DEFINITION_PATTERN_PROXY.nid()) {
                                            entityDescriptionText =
                                                    "Inferred definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == INFERRED_NAVIGATION_PATTERN_PROXY.nid()) {
                                            entityDescriptionText =
                                                    "Inferred is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == PATH_MEMBERSHIP_PROXY.nid()) {
                                            entityDescriptionText =
                                                    viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == STATED_DEFINITION_PATTERN_PROXY.nid()) {
                                            entityDescriptionText =
                                                    "Stated definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == STATED_NAVIGATION_PATTERN_PROXY.nid()) {
                                            entityDescriptionText =
                                                    "Stated is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == UK_DIALECT_PATTERN_PROXY.nid()) {
                                            Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                                            ImmutableList fields = latestAcceptability.get().fieldValues();
                                            entityDescriptionText =
                                                    "UK dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                                            ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == US_DIALECT_PATTERN_PROXY.nid()) {
                                            Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                                            ImmutableList fields = latestAcceptability.get().fieldValues();
                                            entityDescriptionText =
                                                    "US dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                                            ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == VERSION_CONTROL_PATH_ORIGIN_PATTERN_PROXY.nid()) {
                                            Latest<? extends SemanticEntityVersion> latestPathOrigins = viewProperties.calculator().latest(semanticEntity);
                                            ImmutableList fields = latestPathOrigins.get().fieldValues();
                                            entityDescriptionText =
                                                    viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()) +
                                                            " origin: " + DateTimeUtil.format((Instant) fields.get(1)) +
                                                            " on " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0));
                                        }
                                    }

                                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                    label.setText(entityDescriptionText);
                                    if (!entityDescriptionText.isEmpty()) {
                                        Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                                        ImageView imageView = new ImageView(identicon);
                                        imageView.setFitWidth(24);
                                        imageView.setFitHeight(24);
                                        label.setGraphic(imageView);
                                    }
                                    HBox hbox = new HBox();
                                    hbox.getStyleClass().add("pattern-instance-hbox");
                                    hbox.getChildren().add(label);
                                    StackPane stackPane = new StackPane();
                                    hbox.getChildren().add(stackPane);
                                    stackPane.getStyleClass().add("pattern-instance-hover-icon");
                                    label.getStyleClass().add("pattern-instance");
                                    setGraphic(hbox);
                                }
                            }
                        }
                    });
                    if (hasChildren) {
                        Platform.runLater(() -> patternInstances.setItems(patternChildren));
                    }

                });

            });
        });
    }

    private void setUpDraggable(Node node, Entity<?> entity, DragAndDropType dropType) {
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

}
