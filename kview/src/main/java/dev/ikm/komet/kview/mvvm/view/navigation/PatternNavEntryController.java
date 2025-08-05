package dev.ikm.komet.kview.mvvm.view.navigation;

import static dev.ikm.komet.kview.controls.KometIcon.IconValue.PLUS;
import static dev.ikm.komet.kview.controls.KometIcon.IconValue.TRASH;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.INSTANCES;
import static dev.ikm.komet.kview.mvvm.view.navigation.PatternNavEntryController.PatternNavEntry.PATTERN_FACADE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

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
    private VBox mainVBox;

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
    private ListView<Object> patternInstancesListView;

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

        instancesTitledPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
            if (isNowExpanded) {
                if (!mainVBox.getStyleClass().contains("search-entry-title-pane-pattern")) {
                    mainVBox.getStyleClass().add("search-entry-title-pane-pattern");
                }
            } else {
                mainVBox.getStyleClass().remove("search-entry-title-pane-pattern");
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

        var patternNameText = retriveDisplayName((PatternFacade)patternFacade);

        // set the pattern's name
        patternName.setText(patternNameText);

        // set the pattern name label Tooltip
        Tooltip.install(patternName, new Tooltip(patternNameText));

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

        Function<Integer, String> fetchDescription = (nid -> viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid));

        // set the cell factory for each pattern's instance list
        patternInstancesListView.setCellFactory(_ -> new PatternSemanticListCell(fetchDescription, viewProperties));

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

}
