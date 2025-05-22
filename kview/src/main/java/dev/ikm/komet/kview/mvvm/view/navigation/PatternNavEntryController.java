package dev.ikm.komet.kview.mvvm.view.navigation;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static dev.ikm.komet.kview.controls.KometIcon.IconValue.PLUS;
import static dev.ikm.komet.kview.controls.KometIcon.IconValue.TRASH;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.*;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.INSTANCES;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.PATTERN_FACADE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

public class PatternNavEntryController {
    private static final Logger LOG = LoggerFactory.getLogger(PatternNavEntryController.class);

    private static final int LIST_VIEW_CELL_SIZE = 40;
    public enum PatternNavEntry{
        PATTERN_FACADE,
        INSTANCES,
    }
    @FXML
    private HBox patternEntryHBox;

    @FXML
    private HBox semanticElementHBox;

    @FXML
    private ImageView identicon;

    @FXML
    private Label patternName;

    @FXML
    private StackPane dragHandleAffordance;

    @FXML
    private ContextMenu contextMenu;

    @FXML
    private TitledPane instancesTitledPane;

    @FXML
    private ListView patternInstancesListView;

    @InjectViewModel
    private SimpleViewModel instancesViewModel;

    @FXML
    private void initialize() {

        instancesTitledPane.setExpanded(false);
        dragHandleAffordance.setVisible(false);
        contextMenu = new ContextMenu();
        contextMenu.setHideOnEscape(true);
        patternEntryHBox.setOnMouseEntered(mouseEvent -> dragHandleAffordance.setVisible(true));
        patternEntryHBox.setOnMouseExited(mouseEvent -> {
            if (!contextMenu.isShowing()) {
                dragHandleAffordance.setVisible(false);
            }
        });

        KometIcon kometPlusIcon = KometIcon.create(PLUS,"icon-klcontext-menu");
        KometIcon kometTrashIcon = KometIcon.create(TRASH, "icon-klcontext-menu");

        MenuItem addNewSemanticElement = new MenuItem("Add New Semantic Element",kometPlusIcon);
        MenuItem removeSemanticElement = new MenuItem("Remove",kometTrashIcon);
        contextMenu.getItems().addAll(addNewSemanticElement,removeSemanticElement);
        this.contextMenu.getStyleClass().add("klcontext-menu");

        //Context Menu appears on the Pattern tile and Six Dots Icon as well.
        semanticElementHBox.setOnContextMenuRequested(contextMenuEvent ->
                contextMenu.show(semanticElementHBox,contextMenuEvent.getScreenX(),contextMenuEvent.getScreenY())
        );

        EntityFacade patternFacade = instancesViewModel.getPropertyValue(PATTERN_FACADE);
        addNewSemanticElement.setOnAction(actionEvent -> {
            LOG.info("Summon create new Semantic Element. " + patternFacade.description());
            EvtBusFactory.getDefaultEvtBus().publish(instancesViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                    new MakeGenEditingWindowEvent(this,
                        MakeGenEditingWindowEvent.OPEN_GEN_AUTHORING, patternFacade, instancesViewModel.getPropertyValue(VIEW_PROPERTIES)));
        });
        removeSemanticElement.setOnAction(actionEvent -> {
            LOG.info("TODO: Verify if the Pattern needs to be removed. "+patternFacade.description());
        });

        // set identicon
        Image identiconImage = Identicon.generateIdenticonImage(patternFacade.publicId());
        identicon.setImage(identiconImage);

        // set the pattern's name
        patternName.setText(retriveDisplayName((PatternFacade)patternFacade));

        // add listener for double click to summon the pattern into the journal view
        patternEntryHBox.setOnMouseClicked(mouseEvent -> {
            // double left click creates the concept window
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    EvtBusFactory.getDefaultEvtBus().publish(instancesViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                            new MakePatternWindowEvent(this,
                                    MakePatternWindowEvent.OPEN_PATTERN, instancesViewModel.getPropertyValue(PATTERN_FACADE), instancesViewModel.getPropertyValue(VIEW_PROPERTIES)));
                }
            }
        });
        setupListView();
    }

    private String retriveDisplayName(PatternFacade patternFacade) {
        ViewProperties viewProperties = instancesViewModel.getPropertyValue(VIEW_PROPERTIES);
        ViewCalculator viewCalculator = viewProperties.calculator();
        Optional<String> optionalStringRegularName = viewCalculator.getRegularDescriptionText(patternFacade);
        Optional<String> optionalStringFQN = viewCalculator.getFullyQualifiedNameText(patternFacade);
        return optionalStringRegularName.orElseGet(optionalStringFQN::get);
    }

    private void setupListView() {

        patternInstancesListView.setFixedCellSize(LIST_VIEW_CELL_SIZE);
        patternInstancesListView.itemsProperty().addListener(observable -> updateListViewPrefHeight());
        patternInstancesListView.setOnMouseClicked(mouseEvent -> {
            // double click creates the concept window
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    System.out.println(" selected row class = " + patternInstancesListView.getSelectionModel().getSelectedItem().getClass());
                    System.out.println(" selected row value = " + patternInstancesListView.getSelectionModel().getSelectedItem());
                    System.out.println("    pick item = " + mouseEvent.getPickResult().getIntersectedNode());
                    if (patternInstancesListView.getSelectionModel().getSelectedItem() instanceof Integer nid) {

                        EntityFacade semanticChronology = EntityService.get().getEntity(nid).get();
                        EvtBusFactory.getDefaultEvtBus().publish(instancesViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                                new MakeGenEditingWindowEvent(this,
                                        MakeGenEditingWindowEvent.OPEN_GEN_EDIT, semanticChronology, instancesViewModel.getPropertyValue(VIEW_PROPERTIES)));
                    }
                }
            }
        });

        ViewProperties viewProperties = instancesViewModel.getPropertyValue(VIEW_PROPERTIES);
        Function<Integer, String> fetchDescriptionByNid = (nid -> viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid));
        Function<EntityFacade, String> fetchDescriptionByFacade = (facade -> viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(facade));
        // set the cell factory for each pattern's instances list
        patternInstancesListView.setCellFactory(p -> new ListCell<>() {

            private final Label label;
            private Tooltip tooltip;

            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                label = new Label();
                label.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(label, Priority.ALWAYS);

                tooltip = new Tooltip();
                Tooltip.install(label, tooltip);
            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                //setGraphic(null);
                if (item != null && !empty) {
                    if (item instanceof String stringItem) {
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        setText(stringItem);
                    } else if (item instanceof Integer nid) {
                        String entityDescriptionText = fetchDescriptionByNid.apply(nid);
                        EntityFacade entity = Entity.getFast(nid);
                        if (entity instanceof SemanticEntity<?> semanticEntity) {
                            if (semanticEntity.patternNid() == IDENTIFIER_PATTERN_PROXY.nid()) {
                                //TODO Move better string descriptions to language calculator
                                Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
                                ImmutableList fields = latestId.get().fieldValues();
                                entityDescriptionText = fetchDescriptionByFacade.apply((EntityFacade) fields.get(0)) +
                                        ": " + fields.get(1);
                            } else if (semanticEntity.patternNid() == INFERRED_DEFINITION_PATTERN_PROXY.nid()) {
                                entityDescriptionText =
                                        "Inferred definition for: " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == INFERRED_NAVIGATION_PATTERN_PROXY.nid()) {
                                entityDescriptionText =
                                        "Inferred is-a relationships for: " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == PATH_MEMBERSHIP_PROXY.nid()) {
                                entityDescriptionText =
                                        fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == STATED_DEFINITION_PATTERN_PROXY.nid()) {
                                entityDescriptionText =
                                        "Stated definition for: " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == STATED_NAVIGATION_PATTERN_PROXY.nid()) {
                                entityDescriptionText =
                                        "Stated is-a relationships for: " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == UK_DIALECT_PATTERN_PROXY.nid()) {
                                Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                                ImmutableList fields = latestAcceptability.get().fieldValues();
                                entityDescriptionText =
                                        "UK dialect " + fetchDescriptionByFacade.apply((EntityFacade) fields.get(0)) +
                                                ": " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == US_DIALECT_PATTERN_PROXY.nid()) {
                                Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                                ImmutableList fields = latestAcceptability.get().fieldValues();
                                entityDescriptionText =
                                        "US dialect " + fetchDescriptionByFacade.apply((EntityFacade) fields.get(0)) +
                                                ": " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                            } else if (semanticEntity.patternNid() == VERSION_CONTROL_PATH_ORIGIN_PATTERN_PROXY.nid()) {
                                Latest<? extends SemanticEntityVersion> latestPathOrigins = viewProperties.calculator().latest(semanticEntity);
                                ImmutableList fields = latestPathOrigins.get().fieldValues();
                                entityDescriptionText =
                                        fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid()) +
                                                " origin: " + DateTimeUtil.format((Instant) fields.get(1)) +
                                                " on " + fetchDescriptionByFacade.apply((EntityFacade) fields.get(0));
                            }
                        }

                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        label.setText(entityDescriptionText);
                        tooltip.setText(entityDescriptionText);

                        if (!entityDescriptionText.isEmpty()) {
                            Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                            ImageView imageView = new ImageView(identicon);
                            imageView.setFitWidth(16);
                            imageView.setFitHeight(16);
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
                        // make ListCell (row) draggable to the desktop
                        setUpDraggable(hbox, entity, DragAndDropType.SEMANTIC);
                    }
                } else {
                    setGraphic(null);
                }
            }
        });

        // display each row (ListCell) of this ListView
        Platform.runLater(() ->{
            // make items the same as the list by the caller.
            ObservableList<Object> items = instancesViewModel.getObservableList(INSTANCES);
            patternInstancesListView.setItems(items);
            if (items.isEmpty()) {
                instancesTitledPane.setVisible(false);
                instancesTitledPane.setManaged(false);
            }
        });
    }

    private void updateListViewPrefHeight() {
        int itemsNumber = patternInstancesListView.getItems().size();
        /* adding a number to LIST_VIEW_CELL_SIZE to account for padding, etc */
        double newPrefHeight = itemsNumber * (LIST_VIEW_CELL_SIZE + 10);
        double maxHeight = patternInstancesListView.getMaxHeight();

        patternInstancesListView.setPrefHeight(Math.min(newPrefHeight, maxHeight));
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
}
