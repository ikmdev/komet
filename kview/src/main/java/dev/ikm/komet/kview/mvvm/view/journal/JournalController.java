/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.journal;

import static dev.ikm.komet.framework.events.FrameworkTopics.PROGRESS_TOPIC;
import static dev.ikm.komet.framework.events.appevents.ProgressEvent.SUMMON;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.events.MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT;
import static dev.ikm.komet.kview.events.MakeConceptWindowEvent.OPEN_CONCEPT_FROM_SEMANTIC;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.setupSlideOutTrayPane;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.DEVICE_ENTITY;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.VIEW;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULES_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STATE_MACHINE;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.CANCEL_BUTTON_TEXT_PROP;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.TASK_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.PATHS_PROPERTY;
import static dev.ikm.komet.preferences.ConceptWindowPreferences.CONCEPT_FOLDER_PREFIX;
import static dev.ikm.komet.preferences.ConceptWindowPreferences.DEFAULT_CONCEPT_HEIGHT;
import static dev.ikm.komet.preferences.ConceptWindowPreferences.DEFAULT_CONCEPT_XPOS;
import static dev.ikm.komet.preferences.ConceptWindowPreferences.DEFAULT_CONCEPT_YPOS;
import static dev.ikm.komet.preferences.ConceptWindowSettings.CONCEPT_HEIGHT;
import static dev.ikm.komet.preferences.ConceptWindowSettings.CONCEPT_PREF_NAME;
import static dev.ikm.komet.preferences.ConceptWindowSettings.CONCEPT_WIDTH;
import static dev.ikm.komet.preferences.ConceptWindowSettings.CONCEPT_XPOS;
import static dev.ikm.komet.preferences.ConceptWindowSettings.CONCEPT_YPOS;
import static dev.ikm.komet.preferences.ConceptWindowSettings.NID_TYPE;
import static dev.ikm.komet.preferences.ConceptWindowSettings.NID_VALUE;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNAL_FOLDER_PREFIX;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNAL_WINDOW;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;
import static dev.ikm.komet.preferences.JournalWindowSettings.CONCEPT_COUNT;
import static dev.ikm.komet.preferences.JournalWindowSettings.CONCEPT_NAMES;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_DIR_NAME;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_TITLE;
import static dev.ikm.komet.preferences.NidTextEnum.NID_TEXT;
import static dev.ikm.komet.preferences.NidTextEnum.SEMANTIC_ENTITY;
import static java.io.File.separator;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.events.appevents.ProgressEvent;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.framework.tabs.DetachableTab;
import dev.ikm.komet.framework.tabs.TabGroup;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.events.ShowNavigationalPanelEvent;
import dev.ikm.komet.kview.events.reasoner.CloseReasonerPanelEvent;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.fxutils.SlideOutTrayHelper;
import dev.ikm.komet.kview.fxutils.window.WindowSupport;
import dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.lidr.mvvm.view.details.LidrDetailsController;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel;
import dev.ikm.komet.kview.mvvm.view.details.ConceptPreference;
import dev.ikm.komet.kview.mvvm.view.details.DetailsNode;
import dev.ikm.komet.kview.mvvm.view.details.DetailsNodeFactory;
import dev.ikm.komet.kview.mvvm.view.navigation.ConceptPatternNavController;
import dev.ikm.komet.kview.mvvm.view.pattern.PatternDetailsController;
import dev.ikm.komet.kview.mvvm.view.progress.ProgressController;
import dev.ikm.komet.kview.mvvm.view.reasoner.NextGenReasonerController;
import dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController;
import dev.ikm.komet.kview.mvvm.viewmodel.NextGenSearchViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.komet.kview.state.pattern.PatternDetailsPattern;
import dev.ikm.komet.navigator.graph.GraphNavigatorNode;
import dev.ikm.komet.navigator.graph.MultiParentGraphCell;
import dev.ikm.komet.preferences.ConceptWindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.preferences.NidTextEnum;
import dev.ikm.komet.progress.CompletionNodeFactory;
import dev.ikm.komet.progress.ProgressNodeFactory;
import dev.ikm.komet.reasoner.ReasonerResultsController;
import dev.ikm.komet.reasoner.ReasonerResultsNode;
import dev.ikm.komet.reasoner.ReasonerResultsNodeFactory;
import dev.ikm.komet.reasoner.StringWithOptionalConceptFacade;
import dev.ikm.komet.search.SearchNode;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.controlsfx.control.PopOver;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;

/**
 * This view is responsible for updating the kView journal window by loading a navigation panel
 * and a concept details panel. Activity streams are dynamically created to be used in context to a journal instance.
 * This makes the navigator (published data) able to update windows downstream such as the Concept Details Panel
 * This is associated with the FXML file journal.fxml.
 * @see DetailsNode
 * @see JournalViewFactory
 */
public class JournalController {
    private static final Logger LOG = LoggerFactory.getLogger(JournalController.class);

    @FXML
    private StackPane trayPaneContainer;

    /**
     * Top level journal root pane for Scene.
     */
    @FXML
    private BorderPane journalBorderPane;

    @FXML
    private ScrollPane desktopSurfaceScrollPane;

    @FXML
    private AnchorPane desktopSurfacePane;

    @FXML
    private Region desktopDropRegion;

    @FXML
    private MenuItem newConceptMenuItem;

    @FXML
    private HBox chapterHeaderbarHBox;

    @FXML
    private HBox projectBarHBox;

    @FXML
    private VBox sidebarVBox;

    @FXML
    private ToggleGroup sidebarToggleGroup;

    private Pane navSlideoutTrayPane;

    private Pane searchSlideoutTrayPane;

    private Pane reasonerSlideoutTrayPane;

    private Pane nexGenSearchSlideoutTrayPane;

    private Pane nextGenReasonerSlideoutTrayPane;

    private Pane progressSlideoutTrayPane;

    @FXML
    private ToggleButton reasonerToggleButton;

    @FXML
    private ToggleButton navigatorToggleButton;

    @FXML
    private ToggleButton searchToggleButton;

    @FXML
    private ToggleButton progressToggleButton;

    @FXML
    private ToggleButton settingsToggleButton;

    @FXML
    private ToggleButton nextGenSearchToggleButton;

    @FXML
    private ToggleButton nextGenReasonerToggleButton;

    @FXML
    private Button addButton;

    @FXML
    private ContextMenu addContextMenu;


    /////////////////////////////////////////////////////////////////
    // Private Data
    /////////////////////////////////////////////////////////////////
    private final VBox progressListVBox = new VBox();
    private Pane navigatorNodePanel;
    private Pane searchNodePanel;
    private Pane nextGenSearchPanel;

    private StackPane patternConceptNavigationPanel;

    private Pane nextGenReasonerPanel;

    private BorderPane reasonerNodePanel;

    private ActivityStream navigatorActivityStream;
    private ActivityStream searchActivityStream;
    private ActivityStream reasonerActivityStream;
    private final UUID journalTopic;
    private EvtBus journalEventBus = EvtBusFactory.getDefaultEvtBus();
    private volatile boolean isSlideOutOpen = false;

    private List<PublicIdStringKey<ActivityStream>> activityStreams = new ArrayList<>();

    private static Consumer<ToggleButton> reasonerToggleConsumer;

    private GraphNavigatorNode navigatorNode;
    private final ObservableList<ConceptPreference> conceptWindows = FXCollections.observableArrayList();

    private static final String NEXT_GEN_SEARCH_FXML_URL = "next-gen-search.fxml";

    private static final String CONCEPT_PATTERN_NAV_FXML_URL = "navigation-concept-pattern.fxml";

    private static final String NEXT_GEN_REASONER_FXML_URL = "reasoner.fxml";

    private NextGenSearchController nextGenSearchController;

    private NextGenReasonerController nextGenReasonserController;

    private ConceptPatternNavController conceptPatternNavController;

    private Subscriber<MakeConceptWindowEvent> makeConceptWindowEventSubscriber;
    private Subscriber<ShowNavigationalPanelEvent> showNavigationalPanelEventSubscriber;

    private Subscriber<CloseReasonerPanelEvent> closeReasonerPanelEventSubscriber;

    @InjectViewModel
    private NextGenSearchViewModel nextGenSearchViewModel;

    private ObservableViewNoOverride windowView;

    public JournalController(){
        journalTopic = UUID.randomUUID();;
    }

    /**
     * Called after JavaFX FXML DI has occurred. Any annotated items above should be valid.
     */
    @FXML
    public void initialize() {
        reasonerNodePanel = new BorderPane();

        createTrayPanes();

        // When user clicks on sidebar tray's toggle buttons.
        sidebarToggleGroup.selectedToggleProperty().addListener((observableValue, oldValue, newValue) -> {

            // slide in previous panel
            slideIn(oldValue);

            // slide out new panel selected
            slideOut(newValue);
        });

        conceptWindows.addListener((ListChangeListener<ConceptPreference>) change -> {
            PrefX journalWindowPref = PrefX.create();
            journalWindowPref.setValue(CONCEPT_COUNT, conceptWindows.size());
            journalWindowPref.setValue(JOURNAL_TITLE, getTitle());

            journalEventBus.publish(JOURNAL_TOPIC,
                    new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowPref));
        });
        reasonerToggleConsumer = createReasonerToggleConsumer();

        makeConceptWindowEventSubscriber = evt -> {
            ConceptFacade conceptFacade = evt.getConceptFacade();
            if (evt.getEventType().equals(OPEN_CONCEPT_FROM_SEMANTIC)) {
                makeConceptWindow(evt.getWindowView(), conceptFacade, SEMANTIC_ENTITY, null);
            } else if (evt.getEventType().equals(OPEN_CONCEPT_FROM_CONCEPT)) {
                makeConceptWindow(evt.getWindowView(), conceptFacade, NID_TEXT, null);
            }
        };
        journalEventBus.subscribe(JOURNAL_TOPIC, MakeConceptWindowEvent.class, makeConceptWindowEventSubscriber);

        showNavigationalPanelEventSubscriber = evt -> {
            try {
                getNavigatorNode().getController().showConcept(evt.getConceptFacade().nid());
            } catch (Exception e) {
                LOG.error("Unable to process event: ", e);
            }
            navigatorToggleButton.setSelected(true);
        };
        journalEventBus.subscribe(JOURNAL_TOPIC, ShowNavigationalPanelEvent.class, showNavigationalPanelEventSubscriber);

        // listening to the event fired when the user clicks the 'X' on the reasoner slide out
        // and wire into the toggle group because we already have a listener on this property
        closeReasonerPanelEventSubscriber = evt -> sidebarToggleGroup.selectToggle(null);
        journalEventBus.subscribe(JOURNAL_TOPIC, CloseReasonerPanelEvent.class, closeReasonerPanelEventSubscriber);

        // initially drop region is invisible
        desktopDropRegion.setVisible(false);

        // initialize drag and drop for search results of next gen search
        setupDragNDrop(desktopSurfacePane, (publicId) -> {});

        // setup scalable desktop
        setupScalableDesktop();

        desktopSurfacePane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> onMouseClickedOnDesktopSurfacePane(event));
    }

    private void onMouseClickedOnDesktopSurfacePane(MouseEvent mouseEvent) {
        if (mouseEvent.getTarget() == desktopSurfacePane) { // We are indeed just clicking on the background and none of its children
            collapseTrayPanes();
        }
    }

    private void collapseTrayPanes() {
        Toggle selectedToggle = sidebarToggleGroup.getSelectedToggle();
        if (selectedToggle == null) { // There's no actual toggle button selected so no need to actually collapse anything
            return;
        }

        slideIn(selectedToggle);
        selectedToggle.setSelected(false);
    }

    private void createTrayPanes() {
        progressSlideoutTrayPane = newTrayPane();
        navSlideoutTrayPane = newTrayPane();
        searchSlideoutTrayPane = newTrayPane();
        reasonerSlideoutTrayPane = newTrayPane();

        // nexGenSearchSlideoutTrayPane
        nexGenSearchSlideoutTrayPane = new VerticallyFilledPane();
        nexGenSearchSlideoutTrayPane.setLayoutX(10);
        nexGenSearchSlideoutTrayPane.setLayoutY(10);
        nexGenSearchSlideoutTrayPane.setMaxHeight(Double.MAX_VALUE);

        // nextGenReasonerSlideoutTrayPane
        nextGenReasonerSlideoutTrayPane = new VerticallyFilledPane();
        nextGenReasonerSlideoutTrayPane.setLayoutX(10);
        nextGenReasonerSlideoutTrayPane.setLayoutY(10);
        nextGenReasonerSlideoutTrayPane.setMaxHeight(Double.MAX_VALUE);

        trayPaneContainer.getChildren().addAll(
                progressSlideoutTrayPane,
                navSlideoutTrayPane,
                searchSlideoutTrayPane,
                reasonerSlideoutTrayPane,
                nexGenSearchSlideoutTrayPane,
                nextGenReasonerSlideoutTrayPane
        );
    }

    private Pane newTrayPane() {
        Pane pane = new VerticallyFilledPane();
        pane.getStyleClass().add("slideout-tray-pane");
        return pane;
    }

    private void setupScalableDesktop() {
        journalBorderPane.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            // if control key down make desktop surface mouse transparent.
            // Meaning don't allow the dragging of concept windows, but allow panning of desktop surface.
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                desktopSurfacePane.setMouseTransparent(true);
                desktopSurfaceScrollPane.setPannable(true);
                StackPane viewport = (StackPane) desktopSurfaceScrollPane.lookup(".viewport");
                viewport.setCursor(Cursor.OPEN_HAND); // Indicate dragging with open hand
                desktopSurfaceScrollPane.requestFocus();
            }
        });

        journalBorderPane.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            // Turn desktop surface back to listen for drag and mouse press and set cursor back to default.
            if (keyEvent.getCode() == KeyCode.CONTROL) {
                desktopSurfacePane.setMouseTransparent(false);
                desktopSurfaceScrollPane.setPannable(false);
                StackPane viewport = (StackPane) desktopSurfaceScrollPane.lookup(".viewport");
                viewport.setCursor(Cursor.DEFAULT); // Revert back to default cursor
            }
        });

        desktopSurfaceScrollPane.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown() && desktopSurfaceScrollPane.isPannable()) {
                StackPane viewport = (StackPane) desktopSurfaceScrollPane.lookup(".viewport");
                viewport.setCursor(Cursor.CLOSED_HAND); // Indicate dragging with closed hand
            }
        });

        desktopSurfaceScrollPane.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            if (desktopSurfaceScrollPane.isPannable()) {
                StackPane viewport = (StackPane) desktopSurfaceScrollPane.lookup(".viewport");
                viewport.setCursor(Cursor.OPEN_HAND); // Revert back to open hand after dragging
            }
        });
    }

    private void setupDragNDrop(Node node, Consumer<PublicId> consumer) {
        node.setOnDragOver(event -> {
            boolean isMouseOverNode = isMouseOverNode(node, event);
            desktopDropRegion.setVisible(isMouseOverNode);
            desktopDropRegion.setManaged(isMouseOverNode);

            /* data is dragged over the target */
            /* accept it only if it is not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != null &&
                    event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        node.setOnDragExited(event -> desktopDropRegion.setVisible(false));

        node.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasString()) {
                try {
                    LOG.info("publicId: {}", dragboard.getString());

                    ConceptFacade conceptFacade = null;
                    if (event.getGestureSource() instanceof SearchResultCell searchResultCell) {
                        if (searchResultCell.getItem() instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                            conceptFacade = Entity.getFast(nidTextRecord.nid());
                        } else if (searchResultCell.getItem() instanceof
                                LatestVersionSearchResult latestVersionSearchResult) {
                            if (latestVersionSearchResult.latestVersion().isPresent()) {
                                Optional<ConceptEntity> conceptEntity = Entity.getConceptForSemantic(
                                        latestVersionSearchResult.latestVersion().get().nid());
                                if (conceptEntity.isPresent()) {
                                    conceptFacade = conceptEntity.get();
                                }
                            }
                        }
                    } else if (event.getGestureSource() instanceof MultiParentGraphCell multiParentGraphCell) {
                        conceptFacade = multiParentGraphCell.getItem();
                    } else if (event.getGestureSource() instanceof Node sourceNode) {
                        PublicId publicId = (PublicId) sourceNode.getUserData();
                        conceptFacade = ConceptFacade.make(PrimitiveData.nid(publicId));
                    }

                    if (conceptFacade == null) {
                        return;
                    }

                    PublicId publicId = conceptFacade.publicId();
                    Entity<?> entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));

                    Map<ConceptWindowSettings, Object> conceptWindowSettingsMap = new HashMap<>();
                    conceptWindowSettingsMap.put(CONCEPT_XPOS, desktopDropRegion.getLayoutX());
                    conceptWindowSettingsMap.put(CONCEPT_YPOS, desktopDropRegion.getLayoutY());
                    conceptWindowSettingsMap.put(CONCEPT_WIDTH, desktopDropRegion.getWidth());
                    conceptWindowSettingsMap.put(CONCEPT_HEIGHT, desktopDropRegion.getHeight());
                    makeConceptWindow(windowView, ConceptFacade.make(entity.nid()), conceptWindowSettingsMap);

                    consumer.accept(publicId);
                    success = true;
                } catch (Exception ex) {
                    LOG.error("Error while dropping concept: ", ex);
                }
            }

            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
            desktopDropRegion.setVisible(false);
        });

        // by default hide progress toggle button
        progressToggleButton.setVisible(false);
        // Listen for progress tasks
        setupProgressListener();

        // add a vbox list for progress popups.
        setupSlideOutTrayPane(progressListVBox, progressSlideoutTrayPane);
        SlideOutTrayHelper.slideIn(progressSlideoutTrayPane);
    }

    private boolean isMouseOverNode(Node node, DragEvent event) {
        return event.getPickResult().getIntersectedNode() == node;
    }

    private void setupProgressListener() {
        Subscriber<ProgressEvent> progressPopupSubscriber = evt -> {
            // if summon event type, load stuff and reference task to progress popup
            if (evt.getEventType() == SUMMON) {
                Platform.runLater(() -> {
                    progressToggleButton.setVisible(true);
                    Task<Void> task = evt.getTask();
                    JFXNode<Pane, ProgressController> progressJFXNode = createProgressBox(task, evt.getCancelButtonText());
                    ProgressController progressController = progressJFXNode.controller();
                    Pane progressPane = progressJFXNode.node();
                    PopOver popOver = new PopOver(progressPane);

                    // setup close button
                    progressController.getCloseProgressButton().setOnAction(actionEvent -> {
                        popOver.hide();
                        progressController.cleanup();
                    });

                    popOver.setOnHidden(windowEvent -> progressController.cleanup());
                    popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
                    popOver.show(progressToggleButton);

                    // Create one inside the list for bump out
                    JFXNode<Pane, ProgressController> progressJFXNode2 = createProgressBox(task, evt.getCancelButtonText());
                    ProgressController progressController2 = progressJFXNode2.controller();
                    Pane progressBox2 = progressJFXNode2.node();
                    progressController2.getCloseProgressButton().setOnAction(actionEvent -> {
                        progressController2.cleanup();
                        progressListVBox.getChildren().remove(progressBox2);
                    });
                    progressListVBox.getChildren().addFirst(progressBox2);
                });
            }
        };
        journalEventBus.subscribe(PROGRESS_TOPIC, ProgressEvent.class, progressPopupSubscriber);
    }

    private JFXNode<Pane, ProgressController> createProgressBox(Task<Void> task, String cancelButtonText) {
        // Create one inside the list for bump out
        // Inject Stamp view model into form.
        Config config = new Config(ProgressController.class.getResource("progress.fxml"))
                .updateViewModel("progressViewModel", (viewModel -> viewModel
                        .setPropertyValue(TASK_PROPERTY, task)
                        .setPropertyValue(CANCEL_BUTTON_TEXT_PROP, cancelButtonText))
                );

        JFXNode<Pane, ProgressController> progressJFXNode = FXMLMvvmLoader.make(config);
        return progressJFXNode;
    }

    public ToggleButton getSettingsToggleButton() {
        return settingsToggleButton;
    }

    public BorderPane getJournalBorderPane() {
        return journalBorderPane;
    }

    private void slideOut(Toggle toggleButton) {
        Pane trayPane = getCurrentSideBarSelection(toggleButton);
        if (trayPane == null) return;
        SlideOutTrayHelper.slideOut(trayPane);
    }

    private Pane getCurrentSideBarSelection(Toggle selectedToggleButton) {
        if (navigatorToggleButton.equals(selectedToggleButton)) {
            return navSlideoutTrayPane;
        } else if (searchToggleButton.equals(selectedToggleButton)) {
            return searchSlideoutTrayPane;
        } else if (reasonerToggleButton.equals(selectedToggleButton)) {
            return reasonerSlideoutTrayPane;
        } else if (nextGenSearchToggleButton.equals(selectedToggleButton)) {
            return nexGenSearchSlideoutTrayPane;
        } else if (progressToggleButton.equals(selectedToggleButton)) {
            return progressSlideoutTrayPane;
        } else if (nextGenReasonerToggleButton.equals(selectedToggleButton)) {
            return nextGenReasonerSlideoutTrayPane;
        }
        return null;
    }

    private void slideIn(Toggle toggleButton) {
        if (toggleButton == null) return;
        Pane trayPane = getCurrentSideBarSelection(toggleButton);
        if (trayPane == null) return;

        SlideOutTrayHelper.slideIn(trayPane);

    }
    public void shutdown() {
        // cleanup code here...
        LOG.info("kView Concept Details Viewer Journal is shutting down...");
        activityStreams.forEach( activityStreamKey -> ActivityStreams.delete(activityStreamKey));
    }

    /**
     * Iterate through all available KometNodeFactories that will be displayed on the journal.
     * Note: Each journal will have a unique navigation activity stream.
     * @param navigationFactory A factory to create navigation view.
     * @param searchFactory A factory to create a search bump out view.
     */
    public void launchKometFactoryNodes(String journalName,
                                        KometNodeFactory navigationFactory,
                                        KometNodeFactory searchFactory) {
        // Generate a unique activity stream for a navigator for each journal launched. Children (window Panels will subscribe to them).
        String uniqueNavigatorTopic = "navigation-%s".formatted(journalName);
        UUID uuid = UuidT5Generator.get(uniqueNavigatorTopic);
        final PublicIdStringKey<ActivityStream> navigationActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueNavigatorTopic);
        navigatorActivityStream = ActivityStreams.create(navigationActivityStreamKey);
        activityStreams.add(navigationActivityStreamKey);

        loadNavigationPanel(navigationActivityStreamKey, this.windowView, navigationFactory);

        String uniqueSearchTopic = "search-%s".formatted(journalName);
        UUID uuidSearch = UuidT5Generator.get(uniqueSearchTopic);
        final PublicIdStringKey<ActivityStream> searchActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuidSearch.toString()), uniqueSearchTopic);
        searchActivityStream = ActivityStreams.create(searchActivityStreamKey);

        loadSearchPanel(searchActivityStreamKey, windowView, searchFactory);
        loadReasonerPanel(ActivityStreams.REASONER, windowView);


        isSlideOutOpen = false;
    }

    public GraphNavigatorNode getNavigatorNode() {
        return navigatorNode;
    }

    /**
     * Add a Next Gen Search, currently tied to the "comment" left lav button
     */
    public void loadNextGenSearchPanel() {
        Config nextGenSearchConfig = new Config(NextGenSearchController.class.getResource(NEXT_GEN_SEARCH_FXML_URL))
                .updateViewModel("nextGenSearchViewModel", (nextGenSearchViewModel) ->
                        nextGenSearchViewModel
                                .setPropertyValue(MODE, CREATE)
                );

        JFXNode<Pane, NextGenSearchController> nextGenSearchJFXNode = FXMLMvvmLoader.make(nextGenSearchConfig);
        nextGenSearchController = nextGenSearchJFXNode.controller();
        nextGenSearchController.updateModel(this.windowView.makeOverridableViewProperties());
        nextGenSearchController.setWindowView(this.windowView);
        nextGenSearchPanel = nextGenSearchJFXNode.node();

        setupSlideOutTrayPane(nextGenSearchPanel, nexGenSearchSlideoutTrayPane);
    }

    /**
     * Add a Next Gen Reasoner Results, currently tied to the "bell" left nav button
     */
    public void loadNextGenReasonerPanel() {
        JFXNode<Pane, NextGenReasonerController> reasonerJFXNode = FXMLMvvmLoader.make(
                NextGenReasonerController.class.getResource(NEXT_GEN_REASONER_FXML_URL));

        nextGenReasonserController = reasonerJFXNode.controller();
        nextGenReasonerPanel = reasonerJFXNode.node();

        setupSlideOutTrayPane(nextGenReasonerPanel, nextGenReasonerSlideoutTrayPane);
    }

    private void loadSearchPanel(PublicIdStringKey<ActivityStream> searchActivityStreamKey,
                                 ObservableViewNoOverride windowView,
                                 KometNodeFactory searchFactory) {
        // Create search panel and publish on the search activity stream
        SearchNode searchNode = (SearchNode) searchFactory.create(windowView,
                searchActivityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        // What to do when you can double-click on a cell
        SearchPanelController controller = searchNode.getController();
        Consumer<Object> displayInDetailsView = (treeItemValue) -> {
            ConceptFacade conceptFacade = null;
            NidTextEnum nidTextEnum = null;
            if (treeItemValue instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                nidTextEnum = NID_TEXT;
                conceptFacade = Entity.getFast(nidTextRecord.nid());
            } else if (treeItemValue instanceof SemanticEntityVersion semanticEntityVersion) {
                nidTextEnum = SEMANTIC_ENTITY;
                conceptFacade = Entity.getConceptForSemantic(semanticEntityVersion.nid()).get();
            }
            makeConceptWindow(windowView, conceptFacade, nidTextEnum, null);
        };
        controller.getDoubleCLickConsumers().add(displayInDetailsView);
        searchNodePanel = (Pane) searchNode.getNode();
        setupSlideOutTrayPane(searchNodePanel, searchSlideoutTrayPane);

        // When user right clicks selected item in search results (tree view)
        controller.setItemContextMenu((searchTreeView -> {
            // Context menu to allow user to right-click a searched item to show concept in navigator view.
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openNewWindow = new MenuItem("Open Concept");
            openNewWindow.setOnAction(actionEvent -> {
                TreeItem<Object> treeItem = searchTreeView.getSelectionModel().getSelectedItem();
                switch (treeItem.getValue()) {
                    case LatestVersionSearchResult latestVersionSearchResult -> displayInDetailsView.accept(latestVersionSearchResult.latestVersion().get());
                    default -> displayInDetailsView.accept(treeItem.getValue());
                }
            });
            contextMenu.getItems().add(openNewWindow);

            Runnable showInConceptNavigator = ()-> {
                TreeItem<Object> treeItem = searchTreeView.getSelectionModel().getSelectedItem();
                switch (treeItem.getValue()) {
                    case LatestVersionSearchResult latestVersionSearchResult -> {
                        int conceptNid = latestVersionSearchResult.latestVersion().get().nid();
                        getNavigatorNode().getController().showConcept(conceptNid);
                        getNavigatorNode().getController().expandAndSelect(IntIds.list.of(conceptNid));
                    }
                    default -> {}
                }
                ConceptFacade conceptFacade = null;
                Object treeItemValue = treeItem.getValue();
                if (treeItemValue instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                    conceptFacade = Entity.getFast(nidTextRecord.nid());
                } else if (treeItemValue instanceof SemanticEntityVersion semanticEntityVersion) {
                    conceptFacade = Entity.getConceptForSemantic(semanticEntityVersion.nid()).get();
                } else {
                    return;
                }
                getNavigatorNode().getController().showConcept(conceptFacade.nid());
            };

            MenuItem showInNavigator = new MenuItem("Show in Concept Navigator");
            showInNavigator.setOnAction(actionEvent -> {
                showInConceptNavigator.run();
                navigatorToggleButton.setSelected(true);
            });
            contextMenu.getItems().add(showInNavigator);

            return contextMenu;
        }));
    }

    private void makeConceptWindow(ObservableViewNoOverride windowView, ConceptFacade conceptFacade) {
        // This is our overloaded method to call makeConceptWindow when no map is created yet.
        makeConceptWindow(windowView, conceptFacade, NID_TEXT, null);
    }

    private void makeConceptWindow(ObservableViewNoOverride windowView, ConceptFacade conceptFacade, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        // This is our overloaded method to call makeConceptWindow when the settings map is available.
        makeConceptWindow(windowView, conceptFacade, NID_TEXT, conceptWindowSettingsMap);
    }

    private void makeConceptWindow(ObservableViewNoOverride windowView, ConceptFacade conceptFacade, NidTextEnum nidTextEnum, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        // each detail window will publish on their own activity stream.
        String uniqueDetailsTopic = "details-%s".formatted(conceptFacade.nid());
        UUID uuid = UuidT5Generator.get(uniqueDetailsTopic);
        final PublicIdStringKey<ActivityStream> detailsActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueDetailsTopic);
        ActivityStream detailActivityStream = ActivityStreams.create(detailsActivityStreamKey);
        activityStreams.add(detailsActivityStreamKey);
        KometNodeFactory detailsNodeFactory = new DetailsNodeFactory();
        DetailsNode detailsNode = (DetailsNode) detailsNodeFactory.create(windowView,
                detailsActivityStreamKey,
                ActivityStreamOption.PUBLISH.keyForOption(),
                AlertStreams.ROOT_ALERT_STREAM_KEY,
                true,
                journalTopic);
        detailsNode.getDetailsViewController().onReasonerSlideoutTray(reasonerToggleConsumer);

        //Getting the concept window pane
        Pane kometNodePanel = (Pane) detailsNode.getNode();
        //Appling the CSS from draggable-region to the panel (makes it movable/sizable).
        Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
        Node[] draggables = new Node[draggableToolbar.size()];

        WindowSupport windowSupport = new WindowSupport(kometNodePanel, desktopSurfacePane, draggableToolbar.toArray(draggables));
        //Adding the concept window panel as a child to the desktop pane.
        desktopSurfacePane.getChildren().add(kometNodePanel);

        // This will refresh the Concept details, history, timeline
        detailsNode.handleActivity(Lists.immutable.of(conceptFacade));

        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        Optional<String> conceptFolderName;
        if (conceptWindowSettingsMap != null) {
            conceptFolderName = Optional.of(String.valueOf(conceptWindowSettingsMap.getOrDefault(CONCEPT_PREF_NAME,
                    CONCEPT_FOLDER_PREFIX + UUID.randomUUID())));
        } else {
            conceptFolderName = Optional.of(CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
            // create a conceptWindowSettingsMap
            Map<ConceptWindowSettings, Object> conceptWindowSettingsObjectMap = createConceptPrefMap(conceptFolderName.get(), kometNodePanel);
            kometNodePanel.setUserData(conceptWindowSettingsObjectMap);
        }

        // add to the list of concept windows
        final String finalConceptFolderName = conceptFolderName.get();
        conceptWindows.add(new ConceptPreference(conceptFolderName.get(), nidTextEnum, conceptFacade.nid(), kometNodePanel));

        //Calls the remove method to remove and concepts that were closed by the user.
        detailsNode.getDetailsViewController().setOnCloseConceptWindow(windowEvent ->
                removeConceptSetting(finalConceptFolderName, detailsNode));

        //Checking if map is null (if yes not values are set) if not null, setting position of concept windows.
        if (conceptWindowSettingsMap != null) {
            kometNodePanel.setTranslateX((Double)conceptWindowSettingsMap.get(CONCEPT_XPOS));
            kometNodePanel.setTranslateY((Double)conceptWindowSettingsMap.get(CONCEPT_YPOS));
            kometNodePanel.setPrefWidth((Double)conceptWindowSettingsMap.get(CONCEPT_WIDTH));
            kometNodePanel.setPrefHeight((Double)conceptWindowSettingsMap.get(CONCEPT_HEIGHT));
        }
    }

    /**
     * TODO: This displays a blank concept window to allow user to Create a Concept.
     * @param windowView
     * @param nidTextEnum
     * @param conceptWindowSettingsMap
     */
    private void makeCreateConceptWindow(ObservableViewNoOverride windowView, NidTextEnum nidTextEnum, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {

        // each detail window will publish on their own activity stream.
        String uniqueDetailsTopic = "details-%s".formatted(UUID.randomUUID());
        UUID uuid = UuidT5Generator.get(uniqueDetailsTopic);
        final PublicIdStringKey<ActivityStream> detailsActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueDetailsTopic);
        ActivityStream detailActivityStream = ActivityStreams.create(detailsActivityStreamKey);
        activityStreams.add(detailsActivityStreamKey);
        KometNodeFactory detailsNodeFactory = new DetailsNodeFactory();
        DetailsNode detailsNode = (DetailsNode) detailsNodeFactory.create(windowView,
                detailsActivityStreamKey,
                ActivityStreamOption.PUBLISH.keyForOption(),
                AlertStreams.ROOT_ALERT_STREAM_KEY,
                true,
                journalTopic);
        detailsNode.getDetailsViewController().onReasonerSlideoutTray(reasonerToggleConsumer);
        ViewProperties viewProperties = windowView.makeOverridableViewProperties();
        // For Create mode for Creating a concept.
        detailsNode.getDetailsViewController()
                .getConceptViewModel()
                .setPropertyValue(MODE, CREATE);

        detailsNode.getDetailsViewController().updateModel(viewProperties);
        detailsNode.getDetailsViewController().updateView();

        //Getting the concept window pane
        Pane kometNodePanel = (Pane) detailsNode.getNode();
        //Appling the CSS from draggable-region to the panel (makes it movable/sizable).
        Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
        Node[] draggables = new Node[draggableToolbar.size()];

        WindowSupport windowSupport = new WindowSupport(kometNodePanel, desktopSurfacePane, draggableToolbar.toArray(draggables));
        //Adding the concept window panel as a child to the desktop pane.
        desktopSurfacePane.getChildren().add(kometNodePanel);

        // This will refresh the Concept details, history, timeline
        //detailsNode.handleActivity(Lists.immutable.of(conceptFacade));

        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        Optional<String> conceptFolderName;
        if (conceptWindowSettingsMap != null){
            conceptFolderName = (Optional<String>) conceptWindowSettingsMap.getOrDefault(CONCEPT_PREF_NAME, CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
        } else {
            conceptFolderName = Optional.of(CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
            // create a conceptWindowSettingsMap
            Map<ConceptWindowSettings, Object> conceptWindowSettingsObjectMap = createConceptPrefMap(conceptFolderName.get(), kometNodePanel);
            kometNodePanel.setUserData(conceptWindowSettingsObjectMap);
        }

        // add to the list of concept windows
        final String finalConceptFolderName = conceptFolderName.get();
        conceptWindows.add(new ConceptPreference(conceptFolderName.get(), nidTextEnum, -1, kometNodePanel));

        //Calls the remove method to remove and concepts that were closed by the user.
        detailsNode.getDetailsViewController().setOnCloseConceptWindow(windowEvent -> {
            removeConceptSetting(finalConceptFolderName, detailsNode);
        });
        //Checking if map is null (if yes not values are set) if not null, setting position of concept windows.
        if (conceptWindowSettingsMap != null) {
            kometNodePanel.setPrefHeight((Double)conceptWindowSettingsMap.get(CONCEPT_HEIGHT));
            kometNodePanel.setPrefWidth((Double)conceptWindowSettingsMap.get(CONCEPT_WIDTH));
            kometNodePanel.setLayoutX((Double)conceptWindowSettingsMap.get(CONCEPT_XPOS));
            kometNodePanel.setLayoutY((Double)conceptWindowSettingsMap.get(CONCEPT_YPOS));
        }
    }
    private void makeCreateLidrWindow(ObservableViewNoOverride windowView, NidTextEnum nidTextEnum, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        // create a unique topic for each concept detail instance
        UUID conceptTopic = UUID.randomUUID();

        ViewProperties viewProperties = windowView.makeOverridableViewProperties();

        // Prefetch modules and paths for view to populate radio buttons in form. Populate from database
        StampViewModel stampViewModel = new StampViewModel();
        stampViewModel.setPropertyValue(PATHS_PROPERTY, stampViewModel.findAllPaths(viewProperties), true)
                .setPropertyValue(MODULES_PROPERTY, stampViewModel.findAllModules(viewProperties), true);

        // In create mode setup lidrViewModel for injection
        ValidationViewModel lidrViewModel = new LidrViewModel()
                .setPropertyValue(CONCEPT_TOPIC, conceptTopic)
                .setPropertyValue(VIEW_PROPERTIES, viewProperties)
                .setPropertyValue(MODE, CREATE)
                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel);
        lidrViewModel.save(true); // xfer to model values.

        Config lidrConfig = new Config(LidrDetailsController.class.getResource("lidr-details.fxml"))
                .addNamedViewModel(new NamedVm("lidrViewModel", lidrViewModel));

        // create lidr window
        JFXNode<Pane, LidrDetailsController> lidrJFXNode = FXMLMvvmLoader.make(lidrConfig);
        lidrJFXNode.controller().updateView();

        //Getting the concept window pane
        Pane kometNodePanel = lidrJFXNode.node();
        //Appling the CSS from draggable-region to the panel (makes it movable/sizable).
        Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
        Node[] draggables = new Node[draggableToolbar.size()];

        WindowSupport windowSupport = new WindowSupport(kometNodePanel, desktopSurfacePane, draggableToolbar.toArray(draggables));
        //Adding the concept window panel as a child to the desktop pane.
        desktopSurfacePane.getChildren().add(kometNodePanel);

        // This will refresh the Concept details, history, timeline
        //detailsNode.handleActivity(Lists.immutable.of(conceptFacade));

        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        Optional<String> conceptFolderName;
        if (conceptWindowSettingsMap != null){
            conceptFolderName = (Optional<String>) conceptWindowSettingsMap.getOrDefault(CONCEPT_PREF_NAME, CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
        } else {
            conceptFolderName = Optional.of(CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
            // create a conceptWindowSettingsMap
            Map<ConceptWindowSettings, Object> conceptWindowSettingsObjectMap = createConceptPrefMap(conceptFolderName.get(), kometNodePanel);
            kometNodePanel.setUserData(conceptWindowSettingsObjectMap);
        }

        // add to the list of concept windows
        final String finalConceptFolderName = conceptFolderName.get();
        conceptWindows.add(new ConceptPreference(conceptFolderName.get(), nidTextEnum, -1, kometNodePanel));

        //Calls the remove method to remove and concepts that were closed by the user.
        lidrJFXNode.controller().setOnCloseConceptWindow(windowEvent -> {
            // TODO more clean up such as view models and listeners just in case (memory).
            removeLidrSetting(finalConceptFolderName);
        });
        //Checking if map is null (if yes not values are set) if not null, setting position of concept windows.
        if (conceptWindowSettingsMap != null) {
            kometNodePanel.setPrefHeight((Double)conceptWindowSettingsMap.get(CONCEPT_HEIGHT));
            kometNodePanel.setPrefWidth((Double)conceptWindowSettingsMap.get(CONCEPT_WIDTH));
            kometNodePanel.setLayoutX((Double)conceptWindowSettingsMap.get(CONCEPT_XPOS));
            kometNodePanel.setLayoutY((Double)conceptWindowSettingsMap.get(CONCEPT_YPOS));
        }

    }
    private void makeViewEditLidrWindow(ObservableViewNoOverride windowView, ConceptFacade deviceConcept, NidTextEnum nidTextEnum, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        // create a unique topic for each concept detail instance
        UUID conceptTopic = UUID.randomUUID();

        ViewProperties viewProperties = windowView.makeOverridableViewProperties();

        // fetch the device's concept entity let the JavaFX initialize() method populate stuff.

        // Prefetch modules and paths for view to populate radio buttons in form. Populate from database
        StampViewModel stampViewModel = new StampViewModel();
        stampViewModel.setPropertyValue(PATHS_PROPERTY, stampViewModel.findAllPaths(viewProperties), true)
                .setPropertyValue(MODULES_PROPERTY, stampViewModel.findAllModules(viewProperties), true);

        // In create mode setup lidrViewModel for injection
        ValidationViewModel lidrViewModel = new LidrViewModel()
                .setPropertyValue(CONCEPT_TOPIC, conceptTopic)
                .setPropertyValue(VIEW_PROPERTIES, viewProperties)
                .setPropertyValue(DEVICE_ENTITY, deviceConcept) /* Device concept is set. JavaFX view will load and populate fields */
                .setPropertyValue(MODE, VIEW)
                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel);
        lidrViewModel.save(true); // xfer to model values.

        Config lidrConfig = new Config(LidrDetailsController.class.getResource("lidr-details.fxml"))
                .addNamedViewModel(new NamedVm("lidrViewModel", lidrViewModel));

        // create lidr window
        JFXNode<Pane, LidrDetailsController> lidrJFXNode = FXMLMvvmLoader.make(lidrConfig);
        lidrJFXNode.controller().updateView();

        //Getting the concept window pane
        Pane kometNodePanel = lidrJFXNode.node();
        //Appling the CSS from draggable-region to the panel (makes it movable/sizable).
        Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
        Node[] draggables = new Node[draggableToolbar.size()];

        WindowSupport windowSupport = new WindowSupport(kometNodePanel, desktopSurfacePane, draggableToolbar.toArray(draggables));
        //Adding the concept window panel as a child to the desktop pane.
        desktopSurfacePane.getChildren().add(kometNodePanel);

        // This will refresh the Concept details, history, timeline
        //detailsNode.handleActivity(Lists.immutable.of(conceptFacade));

        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        Optional<String> conceptFolderName;
        if (conceptWindowSettingsMap != null){
            conceptFolderName = (Optional<String>) conceptWindowSettingsMap.getOrDefault(CONCEPT_PREF_NAME, CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
        } else {
            conceptFolderName = Optional.of(CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
            // create a conceptWindowSettingsMap
            Map<ConceptWindowSettings, Object> conceptWindowSettingsObjectMap = createConceptPrefMap(conceptFolderName.get(), kometNodePanel);
            kometNodePanel.setUserData(conceptWindowSettingsObjectMap);
        }

        // add to the list of concept windows
        final String finalConceptFolderName = conceptFolderName.get();
        conceptWindows.add(new ConceptPreference(conceptFolderName.get(), nidTextEnum, -1, kometNodePanel));

        //Calls the remove method to remove and concepts that were closed by the user.
        lidrJFXNode.controller().setOnCloseConceptWindow(windowEvent -> {
            // TODO more clean up such as view models and listeners just in case (memory).
            removeLidrSetting(finalConceptFolderName);
        });
        //Checking if map is null (if yes not values are set) if not null, setting position of concept windows.
        if (conceptWindowSettingsMap != null) {
            kometNodePanel.setPrefHeight((Double)conceptWindowSettingsMap.get(CONCEPT_HEIGHT));
            kometNodePanel.setPrefWidth((Double)conceptWindowSettingsMap.get(CONCEPT_WIDTH));
            kometNodePanel.setLayoutX((Double)conceptWindowSettingsMap.get(CONCEPT_XPOS));
            kometNodePanel.setLayoutY((Double)conceptWindowSettingsMap.get(CONCEPT_YPOS));
        }

    }
    /**
     * Creates a map containing the current concept panel (window's) preferences.
     * @param conceptPrefDirName - Unique name used in preferences as a directory name but also a way to remove a card.
     * @param kometNodePanel - The detail concept view window (panel)
     * @return
     */
    private Map<ConceptWindowSettings, Object> createConceptPrefMap(String conceptPrefDirName, Pane kometNodePanel) {
        Map<ConceptWindowSettings, Object> conceptWindowSettingsMap = new HashMap<>();
        conceptWindowSettingsMap.put(CONCEPT_PREF_NAME, conceptPrefDirName);
        conceptWindowSettingsMap.put(CONCEPT_HEIGHT, kometNodePanel.getPrefHeight());
        conceptWindowSettingsMap.put(CONCEPT_WIDTH, kometNodePanel.getPrefWidth());
        conceptWindowSettingsMap.put(CONCEPT_XPOS, kometNodePanel.getLayoutX());
        conceptWindowSettingsMap.put(CONCEPT_YPOS, kometNodePanel.getLayoutX());
        return conceptWindowSettingsMap;
    }

    /**
     * Removes the concept details node (Pane) from the scene graph, closes activity streams, and removes preferences from locally.
     * @param conceptDirectoryName - The unique concept dir name used in each journal window.
     * @param detailsNode - The Concept detailsNode - referencing both JavaFX Node and view.
     */
    private void removeConceptSetting(String conceptDirectoryName, DetailsNode detailsNode) {
        // locate concept by unique directory name and remove from list.
        conceptWindows.removeIf(c -> c.getDirectoryName().equals(conceptDirectoryName));
        detailsNode.close();
        removeConceptPreferences(conceptDirectoryName);
    }
    private void removeLidrSetting(String conceptDirectoryName) {
        // locate concept by unique directory name and remove from list.
        conceptWindows.removeIf(c -> c.getDirectoryName().equals(conceptDirectoryName));
        removeConceptPreferences(conceptDirectoryName);
    }

    /**
     * Removes a concept window's preference folder locally.
     * @param conceptPrefDirName A unique concept directory name. e.g., CONCEPT_1efe8e7d-c2ad-4a24-85ce-db8609f5d7ee
     */
    private void removeConceptPreferences(String conceptPrefDirName) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();

        // Applying the preferences naming convention to the files.
        // e.g., journal-window/JOURNAL_Journal_1/CONCEPT_XXX
        String path = JOURNAL_WINDOW +
                separator + generateJournalDirNameBasedOnTitle() +
                separator + conceptPrefDirName;
        try {
            if (appPreferences.nodeExists(path)) {
                appPreferences.node(path).removeNode();
            }
        } catch (BackingStoreException e) {
            // this should not get here. But if you do continue to remove the card from the display.
            LOG.error("Error removing concept folder %s".formatted(conceptPrefDirName), e);
        }
    }

    /**
     * Loads up a navigation panel into the sidebar area.
     * @param navigationActivityStreamKey The newly generated navigation activity stream for this Journal window and all children.
     * @param windowView Any window view information
     * @param navigationFactory The navigation factory to create the navigation panel to be used in the sidebar.
     */
    private Pane loadClassicConceptNavigator(PublicIdStringKey<ActivityStream> navigationActivityStreamKey,
                                             ObservableViewNoOverride windowView,
                                             KometNodeFactory navigationFactory) {

        // Create navigator panel and publish on the navigation activity stream
        navigatorNode = (GraphNavigatorNode) navigationFactory.create(windowView,
                navigationActivityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        // What to do when you can double-click on a cell
        ViewProperties viewProperties = windowView.makeOverridableViewProperties();
        TreeView<ConceptFacade> treeView = navigatorNode.getController().getTreeView();

        // Create a context menu allowing user to Launch as a Lidr Record window.
        ContextMenu contextMenu1 = treeView.getContextMenu();
        ObservableList<MenuItem> menuItems = FXCollections.observableArrayList();
        menuItems.addAll(contextMenu1.getItems());
        ContextMenu contextMenu2 = new ContextMenu();
        contextMenu2.getItems().addAll(menuItems);

        // set as new context menu
        treeView.setContextMenu(contextMenu2);

        MenuItem launchLidrRecord = new MenuItem("LIDR Viewer");
        launchLidrRecord.setOnAction(event -> {
            TreeItem<ConceptFacade> item = treeView.getSelectionModel().getSelectedItem();
            makeViewEditLidrWindow(windowView, item.getValue(), null,null);
        });
        contextMenu2.getItems().add(launchLidrRecord);
        // check if there is an existing context menu popup event handler. If so, proxy by adding additional behavior.
        // Additional behavior is to add a menu option to load a lidr based window.
        EventHandler<WindowEvent> eventEventHandler = contextMenu1.onShowingProperty().get();

        EventHandler<WindowEvent> eventEventHandlerProxy = windowEvent -> {
            TreeItem<ConceptFacade> item = treeView.getSelectionModel().getSelectedItem();
            ConceptFacade conceptFacade = item.getValue();

            // add menu item if it's a device, otherwise remove from context menu
            boolean isLidrDevice = DataModelHelper.isDevice(viewProperties.calculator().navigationCalculator(), conceptFacade.publicId());
            if (isLidrDevice) {
                launchLidrRecord.setDisable(false);
            } else {
                launchLidrRecord.setDisable(true);
            }

            if (eventEventHandler != null) {
                // call original event handler.
                eventEventHandler.handle(windowEvent);
            }
        };
        contextMenu2.setOnShowing(eventEventHandlerProxy);

        // When user double clicks launch a detail window display.
        treeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<ConceptFacade> item = treeView.getSelectionModel().getSelectedItem();
                if (item == null) return;
                ConceptFacade conceptFacade = item.getValue();
                if (conceptFacade == null) return;

                makeConceptWindow(windowView, conceptFacade);

            }
        });

        return (Pane) navigatorNode.getNode();
    }

    private void loadNavigationPanel(PublicIdStringKey<ActivityStream> navigationActivityStreamKey,
                                     ObservableViewNoOverride windowView, KometNodeFactory navigationFactory) {
        //FIXME load the classic concept nav pane into this one: IIA-975, it is loading but the concept icons are missing
        // and the styling seems off
        ViewProperties viewProperties = windowView.makeOverridableViewProperties();
        Pane navigatorNodePanel = loadClassicConceptNavigator(navigationActivityStreamKey, windowView, navigationFactory);
        Config patternConceptConfig = new Config(ConceptPatternNavController.class.getResource(CONCEPT_PATTERN_NAV_FXML_URL))
                .controller(new ConceptPatternNavController(navigatorNodePanel))
                .updateViewModel("patternNavViewModel", (patternNavViewModel) ->
                                patternNavViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties)
                        );
        JFXNode<StackPane, ConceptPatternNavController> conPatJFXNode = FXMLMvvmLoader.make(patternConceptConfig);
        patternConceptNavigationPanel = conPatJFXNode.node();
        conceptPatternNavController = conPatJFXNode.controller();
        setupSlideOutTrayPane(patternConceptNavigationPanel, navSlideoutTrayPane);
    }

    private  void loadReasonerPanel(PublicIdStringKey<ActivityStream> activityStreamKey,
                                    ObservableViewNoOverride windowView) {
        // set up a tab group to hold 3 tabs for the reasoner bump out
        TabGroup reasonerTabGroup = TabGroup.create(windowView, TabGroup.REMOVAL.DISALLOW);

        // set up the 3 tabs nodes for the reasoner bump out
        // 1) reasoner results; to run the reasoner
        ReasonerResultsNodeFactory reasonerResultsNodeFactory = new ReasonerResultsNodeFactory();
        ReasonerResultsNode reasonerNode = (ReasonerResultsNode) reasonerResultsNodeFactory.create(windowView,
                null, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab resonerResultsTab = new DetachableTab(reasonerNode);

        // 2) progress bar / activity tab
        ProgressNodeFactory progressNodeFactory = new ProgressNodeFactory();
        KometNode kometNode = progressNodeFactory.create(windowView,
                null, null, AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab progressTab = new DetachableTab(kometNode);

        // 3) completion tab
        CompletionNodeFactory completionNodeFactory = new CompletionNodeFactory();
        KometNode completionNode = completionNodeFactory.create(windowView,
                null, null, AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab completionTab = new DetachableTab(completionNode);

        // collect the 3 tabs and add them to the reasoner panel
        ImmutableList<DetachableTab> detachableTabs = Lists.immutable.of(resonerResultsTab, progressTab, completionTab);
        for (DetachableTab tab : detachableTabs) {
            reasonerTabGroup.getTabs().add(tab);
        }
        reasonerNodePanel.setCenter(reasonerTabGroup);

        ReasonerResultsController controller = reasonerNode.getResultsController();

        // display a concept window
        AtomicInteger staggerWindowsX = new AtomicInteger(0);
        AtomicInteger staggerWindowsY = new AtomicInteger(0);
        Consumer<StringWithOptionalConceptFacade> displayInDetailsView = (treeItem) -> {
            treeItem.getOptionalConceptSpecification().ifPresent((conceptFacade -> {
                // each detail window will publish on their own activity stream.
                String uniqueDetailsTopic = "details-%s".formatted(conceptFacade.nid());
                UUID uuid = UuidT5Generator.get(uniqueDetailsTopic);
                final PublicIdStringKey<ActivityStream> detailsActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueDetailsTopic);
                ActivityStream detailActivityStream = ActivityStreams.create(detailsActivityStreamKey);
                activityStreams.add(detailsActivityStreamKey);
                KometNodeFactory detailsNodeFactory = new DetailsNodeFactory();
                DetailsNode detailsNode = (DetailsNode) detailsNodeFactory.create(windowView,
                        detailsActivityStreamKey,
                        ActivityStreamOption.PUBLISH.keyForOption(),
                        AlertStreams.ROOT_ALERT_STREAM_KEY,
                        true,
                        journalTopic);
                detailsNode.getDetailsViewController().onReasonerSlideoutTray(reasonerToggleConsumer);
                Pane kometNodePanel = (Pane) detailsNode.getNode();

                // Make the window compact sized.
                detailsNode.getDetailsViewController().compactSizeWindow();

                Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
                Node[] draggables = new Node[draggableToolbar.size()];
                double x = kometNodePanel.getPrefWidth() * (staggerWindowsX.getAndAdd(1) % 3) + 5; // stagger windows
                double y = kometNodePanel.getPrefHeight() * (staggerWindowsY.get()) + 5; // stagger windows

                kometNodePanel.setLayoutX(x);
                kometNodePanel.setLayoutY(y);

                WindowSupport windowSupport = new WindowSupport(kometNodePanel, desktopSurfacePane, draggableToolbar.toArray(draggables));
                if (staggerWindowsX.get() % 3 == 0) {
                    staggerWindowsY.incrementAndGet();
                }
                desktopSurfacePane.getChildren().add(kometNodePanel);
                // This will refresh the Concept details, history, timeline
                detailsNode.handleActivity(Lists.immutable.of(conceptFacade));
            }));
        };

        // create a function to handle a context menu of one option to compare concepts (launching windows)
        Function<TreeView<StringWithOptionalConceptFacade>, ContextMenu> contextMenuConsumer = (treeView) -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openNewWindows = new MenuItem("Compare Concepts");
            openNewWindows.setOnAction(actionEvent -> {
                treeView.getSelectionModel().getSelectedItems()
                        .forEach(treeItem -> displayInDetailsView.accept(treeItem.getValue()));
                staggerWindowsX.set(0);
                staggerWindowsY.set(0);
            });
            contextMenu.getItems().add(openNewWindows);
            return contextMenu;
        };
        controller.setOnContextMenuForEquiv(contextMenuConsumer);
        setupSlideOutTrayPane(reasonerNodePanel, reasonerSlideoutTrayPane);

    }

    private Consumer<ToggleButton> createReasonerToggleConsumer() {
        return (detailToggleReasonerButton) -> {
            if (detailToggleReasonerButton.isSelected() && reasonerToggleButton.isSelected()) {
                // if global button is already selected don't do anything
            } else if (detailToggleReasonerButton.isSelected() && !reasonerToggleButton.isSelected()){
                // if global is not selected fire an event to trigger it.
                slideOut(reasonerToggleButton);
                reasonerToggleButton.setSelected(true);
            } else if (!detailToggleReasonerButton.isSelected() && !reasonerToggleButton.isSelected()) {
                // if details is not selected and the global is not selected ignore
            } else if (!detailToggleReasonerButton.isSelected() && reasonerToggleButton.isSelected()) {
                slideIn(reasonerToggleButton);
                reasonerToggleButton.setSelected(false);
            }
        };
    }

    //Getter and Setters for various JavaFX components
    public String getTitle() {
        Stage jStage = (Stage) this.getSettingsToggleButton().getScene().getWindow();
        return jStage.getTitle();
    }

    public double getHeight() {
        Stage jStage = (Stage) this.getSettingsToggleButton().getScene().getWindow();
        return jStage.getHeight();
    }

    public double getWidth() {
        Stage jStage = (Stage) this.getSettingsToggleButton().getScene().getWindow();
        return  jStage.getWidth();
    }

    public double getX() {
        Stage jStage = (Stage) this.getSettingsToggleButton().getScene().getWindow();
        return jStage.getX();
    }

    public double getY() {
        Stage jStage = (Stage) this.getSettingsToggleButton().getScene().getWindow();
        return jStage.getY();
    }

    public void close() {
        Stage jStage = (Stage) this.getSettingsToggleButton().getScene().getWindow();
        jStage.close();
    }

    /**
     * This will use the title of the journal project and convert it to a unique journal directory name.
     * This function will convert Journal 1 to JOURNAL_Journal_1.
     * Todo Refactor code to allow user to change the name of the journal project and a unique name that doesn't conflict with the file system.
     * @return
     */
    public String generateJournalDirNameBasedOnTitle() {
        return JOURNAL_FOLDER_PREFIX + getTitle().replace(" ", "_");
    }

    public void saveConceptWindowPreferences(KometPreferences journalSubWindowPreferences){
        List<String> conceptFolderNames = new ArrayList<>();
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();

        //Looping through each concept window to save position and size to preferences.
        for (ConceptPreference conceptPreference : conceptWindows) {

            // skip concept windows without a proper nid, these could be concepts that are not fully created
            if (conceptPreference.getNid().equals(-1)) {
                continue;
            }

            String conceptPrefName = conceptPreference.getDirectoryName();
            conceptFolderNames.add(conceptPrefName);

            // Applying the preferences naming convention to the files.
            // e.g., journal-window/JOURNAL_Journal_1/CONCEPT_XXX
            KometPreferences conceptPreferences =journalSubWindowPreferences.node(
                    conceptPreference.getDirectoryName());
            conceptPreferences.put(CONCEPT_PREF_NAME, conceptPreference.getDirectoryName());
            conceptPreferences.put(NID_TYPE, conceptPreference.getNidType().toString());
            conceptPreferences.putInt(NID_VALUE, conceptPreference.getNid());
            conceptPreferences.putDouble(CONCEPT_HEIGHT, conceptPreference.getConceptPane().getPrefHeight());
            conceptPreferences.putDouble(CONCEPT_WIDTH, conceptPreference.getConceptPane().getPrefWidth());
            conceptPreferences.putDouble(CONCEPT_XPOS, conceptPreference.getConceptPane().getBoundsInParent().getMinX());
            conceptPreferences.putDouble(CONCEPT_YPOS, conceptPreference.getConceptPane().getBoundsInParent().getMinY());
            try {
                conceptPreferences.flush();
            } catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }
        }
        //Putting the list of concepts in our preferences.
        journalSubWindowPreferences.putList(CONCEPT_NAMES, conceptFolderNames);
        try {
            journalSubWindowPreferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void recreateConceptWindows(PrefX journalPref) {
        List<String> conceptList = journalPref.getValue(CONCEPT_NAMES);
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        //Looping through each concept in each journal.
        for(String conceptFolder: conceptList){
            KometPreferences conceptPreferences = appPreferences.node(JOURNAL_WINDOW +
                    separator + journalPref.getValue(JOURNAL_DIR_NAME) +
                    separator + conceptFolder);
            KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
            WindowSettings windowSettings = new WindowSettings(windowPreferences);
            ObservableViewNoOverride window = windowSettings.getView();

            //Getting nid type via the Enum.
            String nidTextString= conceptPreferences.get(conceptPreferences.enumToGeneralKey(NID_TYPE)).get();
            Integer nidValue = Integer.valueOf(conceptPreferences.get(conceptPreferences.enumToGeneralKey(NID_VALUE)).get());
            ConceptFacade conceptFacade = null;
            NidTextEnum nidTextEnum = null;

            if (nidTextString.equals(NID_TEXT.toString())) {
                nidTextEnum = NID_TEXT;
                conceptFacade = Entity.getFast(nidValue);
            } else if (nidTextString.equals(SEMANTIC_ENTITY.toString())) {
                nidTextEnum = SEMANTIC_ENTITY;
                conceptFacade = Entity.getConceptForSemantic(nidValue).get();
            }
            //Creating a hashmap to store all position and size values for each concept.
            Map<ConceptWindowSettings, Object> conceptWindowSettingsMap = new HashMap<>();
            conceptWindowSettingsMap.put(CONCEPT_PREF_NAME, conceptPreferences.get(CONCEPT_PREF_NAME));
            conceptWindowSettingsMap.put(CONCEPT_HEIGHT,conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_HEIGHT), DEFAULT_CONCEPT_HEIGHT));
            conceptWindowSettingsMap.put(CONCEPT_WIDTH, conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_WIDTH), DEFAULT_CONCEPT_HEIGHT));
            conceptWindowSettingsMap.put(CONCEPT_XPOS, conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_XPOS), DEFAULT_CONCEPT_XPOS));
            conceptWindowSettingsMap.put(CONCEPT_YPOS, conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_YPOS), DEFAULT_CONCEPT_YPOS));

            //Calling make concept window to finish.
            makeConceptWindow(window, conceptFacade, nidTextEnum, conceptWindowSettingsMap);
        }
    }

    /**
     * Bring window to the front of all windows.
     */
    public void windowToFront() {
        if (journalBorderPane != null && journalBorderPane.getScene() != null && journalBorderPane.getScene().getWindow() != null) {
            ((Stage) journalBorderPane.getScene().getWindow()).toFront();
        }
    }

    @FXML
    private void popupAddContextMenu(ActionEvent actionEvent) {
        MenuHelper.fireContextMenuEvent(actionEvent, Side.BOTTOM, -50, 0);
    }
    /**
     * Returns a Region representing the icon for a menu item.
     * Utility to create a region with a style class defined in CSS as the icon graphic to the left of the menu item.
     * @param styleClass
     * @return
     */
    private Region createMenuIcon(String styleClass){
        Region graphic = new Region();
        graphic.getStyleClass().add(styleClass);
        return graphic;
    }

    /**
     * When user selects menuitem to create a new concept
     * @param actionEvent - button press
     */
    @FXML
    public void newCreateConceptWindow(ActionEvent actionEvent) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);

        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        makeCreateConceptWindow(windowSettings.getView(), NID_TEXT, null);

    }
    @FXML
    public void newCreateLidrWindow(ActionEvent actionEvent) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);

        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        makeCreateLidrWindow(windowSettings.getView(), null, null);

    }


    public void setWindowView(ObservableViewNoOverride windowView) {
        this.windowView = windowView;
    }

    @FXML
    public void newCreatePatternWindow(ActionEvent actionEvent) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);

        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        makeNewPatternWindow(windowSettings.getView(), null, null);
    }

    private void makeNewPatternWindow(ObservableViewNoOverride windowView, NidTextEnum nidTextEnum, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        ViewProperties viewProperties = windowView.makeOverridableViewProperties();

        // Prefetch modules and paths for view to populate radio buttons in form. Populate from database
        StampViewModel stampViewModel = new StampViewModel();
        stampViewModel.setPropertyValue(PATHS_PROPERTY, stampViewModel.findAllPaths(viewProperties), true)
                .setPropertyValue(MODULES_PROPERTY, stampViewModel.findAllModules(viewProperties), true);

        StateMachine patternSM = StateMachine.create(new PatternDetailsPattern());

        ValidationViewModel patternViewModel = new PatternViewModel()
                .setPropertyValue(VIEW_PROPERTIES, viewProperties)
                .setPropertyValue(MODE, CREATE)
                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel)
                .setPropertyValue(PATTERN_TOPIC, UUID.randomUUID())
                .setPropertyValue(STATE_MACHINE, patternSM);

        Config patternConfig = new Config(PatternDetailsController.class.getResource("pattern-details.fxml"))
                .addNamedViewModel(new NamedVm("patternViewModel", patternViewModel));

        // create lidr window
        JFXNode<Pane, PatternDetailsController> patternJFXNode = FXMLMvvmLoader.make(patternConfig);
        patternJFXNode.controller().updateView();

        //Getting the concept window pane
        Pane kometNodePanel = patternJFXNode.node();
        //Applying the CSS from draggable-region to the panel (makes it movable/sizable).
        Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
        Node[] draggables = new Node[draggableToolbar.size()];

        WindowSupport windowSupport = new WindowSupport(kometNodePanel, desktopSurfacePane, draggableToolbar.toArray(draggables));
        //Adding the concept window panel as a child to the desktop pane.
        desktopSurfacePane.getChildren().add(kometNodePanel);

        //FIXME are both LIDR and Pattern windows borrowing the concept folder for preferences?
        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        Optional<String> conceptFolderName;
        if (conceptWindowSettingsMap != null){
            conceptFolderName = (Optional<String>) conceptWindowSettingsMap.getOrDefault(CONCEPT_PREF_NAME, CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
        } else {
            conceptFolderName = Optional.of(CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
            // create a conceptWindowSettingsMap
            Map<ConceptWindowSettings, Object> conceptWindowSettingsObjectMap = createConceptPrefMap(conceptFolderName.get(), kometNodePanel);
            kometNodePanel.setUserData(conceptWindowSettingsObjectMap);
        }

        // add to the list of concept windows
        final String finalConceptFolderName = conceptFolderName.get();
        conceptWindows.add(new ConceptPreference(conceptFolderName.get(), nidTextEnum, -1, kometNodePanel));

        //Calls the remove method to remove and concepts that were closed by the user.
        patternJFXNode.controller().setOnCloseConceptWindow(windowEvent -> {
            // TODO more clean up such as view models and listeners just in case (memory).
            removeLidrSetting(finalConceptFolderName);
        });
        //Checking if map is null (if yes not values are set) if not null, setting position of concept windows.
        if (conceptWindowSettingsMap != null) {
            kometNodePanel.setPrefHeight((Double)conceptWindowSettingsMap.get(CONCEPT_HEIGHT));
            kometNodePanel.setPrefWidth((Double)conceptWindowSettingsMap.get(CONCEPT_WIDTH));
            kometNodePanel.setLayoutX((Double)conceptWindowSettingsMap.get(CONCEPT_XPOS));
            kometNodePanel.setLayoutY((Double)conceptWindowSettingsMap.get(CONCEPT_YPOS));
        }
        patternJFXNode.controller().putTitlePanesArrowOnRight();

        //FIXME opening the panel too soon creates a broken UI for the pattern window
        //EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(patternConfig, OPEN_PANEL));
    }
}
