package dev.ikm.komet.kview.mvvm.view.navigation;

import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.IDENTIFIER_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.INFERRED_DEFINITION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.INFERRED_NAVIGATION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.PATH_MEMBERSHIP_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.STATED_DEFINITION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.STATED_NAVIGATION_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.UK_DIALECT_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.US_DIALECT_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.VERSION_CONTROL_PATH_ORIGIN_PATTERN_PROXY;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.INSTANCES;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.PATTERN_FACADE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.eclipse.collections.api.list.ImmutableList;

import java.time.Instant;
import java.util.function.Function;

public class PatternNavEntryController {

    private static final int LIST_VIEW_CELL_SIZE = 36;
    public enum PatternNavEntry{
        PATTERN_FACADE,
        INSTANCES,
    }
    @FXML
    private HBox patternEntryHBox;

    @FXML
    private ImageView identicon;

    @FXML
    private Text patternName;

    @FXML
    private Button showContextButton;

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
        showContextButton.setVisible(false);
        contextMenu.setHideOnEscape(true);
        patternEntryHBox.setOnMouseEntered(mouseEvent -> showContextButton.setVisible(true));
        patternEntryHBox.setOnMouseExited(mouseEvent -> {
            if (!contextMenu.isShowing()) {
                showContextButton.setVisible(false);
            }
        });
        EntityFacade patternFacade = instancesViewModel.getPropertyValue(PATTERN_FACADE);

        // set identicon
        Image identiconImage = Identicon.generateIdenticonImage(patternFacade.publicId());
        identicon.setImage(identiconImage);

        // set the pattern's name
        patternName.setText(patternFacade.description());

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
        showContextButton.setOnAction(event -> contextMenu.show(showContextButton, Side.BOTTOM, 0, 0));

        setupListView();
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
            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                label = new Label();
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
                        Entity entity = Entity.getFast(nid);
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
        double newPrefHeight = itemsNumber * LIST_VIEW_CELL_SIZE;
        double maxHeight = patternInstancesListView.getMaxHeight();
        patternInstancesListView.setPrefHeight(Math.min(newPrefHeight, maxHeight));
    }
}
