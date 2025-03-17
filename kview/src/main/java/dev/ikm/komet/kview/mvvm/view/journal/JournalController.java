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
import static dev.ikm.komet.kview.controls.KLWorkspace.DESKTOP_PANE_STYLE_CLASS;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.events.MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT;
import static dev.ikm.komet.kview.events.MakeConceptWindowEvent.OPEN_CONCEPT_FROM_SEMANTIC;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.setupSlideOutTrayPane;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.CREATE;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.MODE;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.CONCEPT;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.CANCEL_BUTTON_TEXT_PROP;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.TASK_PROPERTY;
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
import static javafx.stage.PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT;
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
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.framework.tabs.DetachableTab;
import dev.ikm.komet.framework.tabs.TabGroup;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.controls.NotificationPopup;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.events.ShowNavigationalPanelEvent;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent;
import dev.ikm.komet.kview.events.reasoner.CloseReasonerPanelEvent;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.fxutils.SlideOutTrayHelper;
import dev.ikm.komet.kview.klwindows.concept.ConceptKlWindow;
import dev.ikm.komet.kview.klwindows.concept.ConceptKlWindowFactory;
import dev.ikm.komet.kview.klwindows.genediting.GenEditingKlWindow;
import dev.ikm.komet.kview.klwindows.genediting.GenEditingKlWindowFactory;
import dev.ikm.komet.kview.klwindows.lidr.LidrKlWindow;
import dev.ikm.komet.kview.klwindows.lidr.LidrKlWindowFactory;
import dev.ikm.komet.kview.klwindows.pattern.PatternKlWindow;
import dev.ikm.komet.kview.klwindows.pattern.PatternKlWindowFactory;
import dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.komet.kview.mvvm.view.details.ConceptPreference;
import dev.ikm.komet.kview.mvvm.view.details.DetailsNode;
import dev.ikm.komet.kview.mvvm.view.navigation.ConceptPatternNavController;
import dev.ikm.komet.kview.mvvm.view.progress.ProgressController;
import dev.ikm.komet.kview.mvvm.view.reasoner.NextGenReasonerController;
import dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController;
import dev.ikm.komet.kview.mvvm.viewmodel.NextGenSearchViewModel;
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
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;

/**
 * This view is responsible for updating the kView journal window by loading a navigation panel
 * and a concept details panel. Activity streams are dynamically created to be used in context to a journal instance.
 * This makes the navigator (published data) able to update windows downstream such as the Concept Details Panel
 * This is associated with the FXML file journal.fxml.
 *
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
    private KLWorkspace workspace;

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

    private NotificationPopup progressNotificationPopup;

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
    /// //////////////////////////////////////////////////////////////
    private final VBox progressPopupPane = new VBox();
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

    private Subscriber<MakePatternWindowEvent> makePatternWindowEventSubscriber;

    private Subscriber<ShowNavigationalPanelEvent> showNavigationalPanelEventSubscriber;

    private Subscriber<CloseReasonerPanelEvent> closeReasonerPanelEventSubscriber;

    @InjectViewModel
    private NextGenSearchViewModel nextGenSearchViewModel;

    private ObservableViewNoOverride windowView;

    public JournalController() {
        journalTopic = UUID.randomUUID();
    }

    /**
     * Called after JavaFX FXML DI has occurred. Any annotated items above should be valid.
     */
    @FXML
    public void initialize() {
        reasonerNodePanel = new BorderPane();
        progressPopupPane.getStyleClass().add("progress-popup-pane");

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

        makePatternWindowEventSubscriber = evt -> {
            LOG.info("FIXME... edit pattern window");
            makePatternWindow(evt.getPatternFacade(), evt.getViewProperties());
        };
        journalEventBus.subscribe(JOURNAL_TOPIC, MakePatternWindowEvent.class, makePatternWindowEventSubscriber);

        // Listening for when a General authoring Window needs to be summoned.
        Subscriber<MakeGenEditingWindowEvent> makeGenEditWindowEventSubscriber = evt ->
            makeGenEditWindow(evt.getComponent(), evt.getViewProperties());

        journalEventBus.subscribe(journalTopic, MakeGenEditingWindowEvent.class, makeGenEditWindowEventSubscriber);

        showNavigationalPanelEventSubscriber = evt -> {
            try {
                getNavigatorNode().getController().showConcept(evt.getConceptFacade().nid());
            } catch (Exception e) {
                LOG.error("Unable to process event: ", e);
            }
            navigatorToggleButton.setSelected(true);

            // toggle CONCEPTS inside conceptPatternNavController
            conceptPatternNavController.toggleConcepts();
        };
        journalEventBus.subscribe(JOURNAL_TOPIC, ShowNavigationalPanelEvent.class, showNavigationalPanelEventSubscriber);

        // listening to the event fired when the user clicks the 'X' on the reasoner slide out
        // and wire into the toggle group because we already have a listener on this property
        closeReasonerPanelEventSubscriber = evt -> sidebarToggleGroup.selectToggle(null);
        journalEventBus.subscribe(JOURNAL_TOPIC, CloseReasonerPanelEvent.class, closeReasonerPanelEventSubscriber);

        // initialize drag and drop for search results of next gen search
        setupDragNDrop(workspace, (publicId) -> {
        });

        workspace.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClickedOnDesktopSurfacePane);
    }

    private void onMouseClickedOnDesktopSurfacePane(MouseEvent mouseEvent) {
        final Node intersectedNode = mouseEvent.getPickResult().getIntersectedNode();
        if (intersectedNode.getStyleClass().contains(DESKTOP_PANE_STYLE_CLASS)) {
            // We are indeed just clicking on the background and none of its children
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

    private void setupDragNDrop(Node node, Consumer<PublicId> consumer) {
        node.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasString()) {
                try {
                    LOG.info("publicId: {}", dragboard.getString());

                    ConceptFacade conceptFacade = null;
                    PatternFacade patternFacade = null;
                    EntityFacade semanticFacade = null;
                    DragAndDropType dragAndDropType = null;
                    if (event.getGestureSource() instanceof SearchResultCell searchResultCell) {
                        dragAndDropType = CONCEPT;
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
                        dragAndDropType = CONCEPT;
                    } else if (event.getGestureSource() instanceof Node sourceNode && sourceNode.getUserData() instanceof DragAndDropInfo) {
                        // could be a concept or a pattern
                        DragAndDropInfo dragAndDropInfo = (DragAndDropInfo) sourceNode.getUserData();
                        if (dragAndDropInfo.type().equals(DragAndDropType.CONCEPT)) {
                            dragAndDropType = DragAndDropType.CONCEPT;
                            conceptFacade = ConceptFacade.make(PrimitiveData.nid(dragAndDropInfo.publicId()));
                        } else if (dragAndDropInfo.type().equals(DragAndDropType.PATTERN)) {
                            dragAndDropType = DragAndDropType.PATTERN;
                            patternFacade = PatternFacade.make(PrimitiveData.nid(dragAndDropInfo.publicId()));
                        } else if (dragAndDropInfo.type().equals(DragAndDropType.SEMANTIC)) {
                            dragAndDropType = DragAndDropType.SEMANTIC;
                            semanticFacade = EntityService.get().getEntityFast(PrimitiveData.nid(dragAndDropInfo.publicId()));
                        }
                        LOG.info("wait a sec");
                    }

                    // TODO: This code isn't scalable. JournalController now supports a third window (semantic editing). If you have a new window each if/then/else needs to be changed.
                    if (conceptFacade == null && patternFacade == null && semanticFacade == null) {
                        return;
                    }
                    PublicId publicId = null;
                    if (dragAndDropType.equals(DragAndDropType.CONCEPT)) {
                        publicId = conceptFacade.publicId();
                        Entity<?> entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                        makeConceptWindow(windowView, ConceptFacade.make(entity.nid()));
                    } else if (dragAndDropType.equals(DragAndDropType.PATTERN)) {
                        publicId = patternFacade.publicId();
                        makePatternWindow(patternFacade, windowView.makeOverridableViewProperties());
                    } else if (dragAndDropType.equals(DragAndDropType.SEMANTIC)) {

                        publicId = semanticFacade.publicId();
                        // TODO save preferences of window's (position and size) such as the general editing chapter window.
                        makeGenEditWindow(semanticFacade, windowView.makeOverridableViewProperties());
                    }
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
        });

        // by default hide progress toggle button
        progressToggleButton.setVisible(false);
        // Listen for progress tasks
        setupProgressListener();
    }

    /**
     * Subscribes to progress events (on {@code PROGRESS_TOPIC}) and displays a
     * {@link NotificationPopup} to show progress information.
     * <p>
     * When a new {@link ProgressEvent} with the event type {@code SUMMON} is received,
     * this method:
     * <ul>
     *   <li>Creates and configures the {@link NotificationPopup} that displays the
     *       current progress tasks.</li>
     *   <li>Makes the {@code progressToggleButton} visible (so the user can manually
     *       show or hide the popup).</li>
     *   <li>Builds the progress user interface and attaches it to the popup.</li>
     * </ul>
     */
    private void setupProgressListener() {
        // Subscribe to progress events on the event bus
        Subscriber<ProgressEvent> progressPopupSubscriber = evt -> {
            // if SUMMON event type, load stuff and reference task to progress popup
            if (evt.getEventType() == SUMMON) {
                Platform.runLater(() -> {
                    // Make the toggle button visible so users can open the popover
                    progressToggleButton.setVisible(true);

                    Task<Void> task = evt.getTask();

                    // Build the UI (Pane + Controller) for the progress popup
                    JFXNode<Pane, ProgressController> progressJFXNode = createProgressBox(task, evt.getCancelButtonText());
                    ProgressController progressController = progressJFXNode.controller();
                    Pane progressPane = progressJFXNode.node();

                    // Create a new NotificationPopup to show the progress pane
                    progressNotificationPopup = new NotificationPopup(progressPopupPane);
                    progressNotificationPopup.setAnchorLocation(WINDOW_BOTTOM_LEFT);

                    // Hide popup when clicking on the progressPopupPane background (if autoHide is enabled)
                    progressPopupPane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                        if (e.getPickResult().getIntersectedNode() == progressPopupPane
                                && progressNotificationPopup.isAutoHide()) {
                            progressNotificationPopup.hide();
                        }
                    });

                    // Close button handler in the progress pane
                    progressController.getCloseProgressButton().setOnAction(actionEvent -> {
                        // Cancel the task
                        ProgressHelper.cancel(task);

                        // Remove the progress pane from the popup
                        progressPopupPane.getChildren().remove(progressPane);
                        if (progressPopupPane.getChildren().isEmpty()) {
                            progressToggleButton.setSelected(false);
                            progressToggleButton.setVisible(false);
                        }
                    });

                    progressNotificationPopup.setOnShown(windowEvent -> {
                        // Select the toggle button when the popup is shown
                        progressToggleButton.setSelected(true);
                    });

                    progressNotificationPopup.setOnHidden(windowEvent -> {
                        // Deselect the toggle button when the popup is hidden
                        progressToggleButton.setSelected(false);
                    });

                    progressToggleButton.setOnAction(actionEvent -> {
                        // Toggle button logic to show/hide the popup
                        if (progressToggleButton.isSelected()) {
                            if (progressNotificationPopup.isShowing()) {
                                progressNotificationPopup.hide();
                            } else {
                                progressNotificationPopup.show(progressToggleButton, this::supplyProgressPopupAnchorPoint);
                            }
                        } else {
                            progressNotificationPopup.hide();
                        }
                    });

                    // Add the progress UI to the popup's vertical container
                    progressPopupPane.getChildren().add(progressPane);

                    // Show the progress popup immediately for this new task
                    progressNotificationPopup.show(progressToggleButton, this::supplyProgressPopupAnchorPoint);
                });
            }
        };
        journalEventBus.subscribe(PROGRESS_TOPIC, ProgressEvent.class, progressPopupSubscriber);
    }

    /**
     * Computes and returns the coordinates at which the progress popup
     * ({@link #progressNotificationPopup}) should be anchored, ensuring it appears to
     * the right of the {@code progressToggleButton} and near the lower edge of
     * the workspace.
     * <p>
     * The resulting anchor point is used by {@link NotificationPopup#show(Node, Supplier)}
     * or similar popup methods to place the popup on the screen.
     *
     * @return a {@code Point2D} representing the (X, Y) coordinates where the progress
     * popup should be anchored
     */
    private Point2D supplyProgressPopupAnchorPoint() {
        final Bounds progressToggleButtonScreenBounds =
                progressToggleButton.localToScreen(progressToggleButton.getBoundsInLocal());
        final Bounds workspaceScreenBounds = workspace.localToScreen(workspace.getBoundsInLocal());
        final double progressListVBoxPadding = 12.0;  // Padding around the progress list VBox

        // Adjust the progress popup’s height to fit within the workspace bounds.
        progressPopupPane.setPrefHeight(workspaceScreenBounds.getHeight() - (4 * progressListVBoxPadding - 4.0));

        // Position the popup to the right of the toggle button, near the bottom of the workspace.
        final double popupAnchorX = progressToggleButtonScreenBounds.getMinX()
                + progressToggleButton.getWidth() + progressListVBoxPadding;
        final double popupAnchorY = workspaceScreenBounds.getMaxY() - 2 * progressListVBoxPadding;
        return new Point2D(popupAnchorX, popupAnchorY);
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
        activityStreams.forEach(activityStreamKey -> ActivityStreams.delete(activityStreamKey));

        journalEventBus.unsubscribe(makeConceptWindowEventSubscriber,
                makePatternWindowEventSubscriber,
                showNavigationalPanelEventSubscriber,
                closeReasonerPanelEventSubscriber);
    }

    /**
     * Iterate through all available KometNodeFactories that will be displayed on the journal.
     * Note: Each journal will have a unique navigation activity stream.
     *
     * @param navigationFactory A factory to create navigation view.
     * @param searchFactory     A factory to create a search bump out view.
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
            NidTextEnum nidTextEnum = null;
            if (treeItemValue instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                nidTextEnum = NID_TEXT;
                Entity entity = Entity.getFast(nidTextRecord.nid());
                if (entity instanceof ConceptFacade conceptFacade) {
                    makeConceptWindow(windowView, conceptFacade, nidTextEnum, null);
                } else if (entity instanceof PatternFacade patternFacade) {
                    makePatternWindow(patternFacade, getNavigatorNode().getViewProperties());
                }

            } else if (treeItemValue instanceof SemanticEntityVersion semanticEntityVersion) {
                nidTextEnum = SEMANTIC_ENTITY;
                ConceptFacade conceptFacade = Entity.getConceptForSemantic(semanticEntityVersion.nid()).get();
                makeConceptWindow(windowView, conceptFacade, nidTextEnum, null);
            }
        };
        controller.getDoubleCLickConsumers().add(displayInDetailsView);
        searchNodePanel = (Pane) searchNode.getNode();
        setupSlideOutTrayPane(searchNodePanel, searchSlideoutTrayPane);

        // When user right clicks selected item in search results (tree view)
        controller.setItemContextMenu((searchTreeView -> {
            // Context menu to allow user to right-click a searched item to show concept in navigator view.
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openNewWindow = new MenuItem("Open");
            openNewWindow.setOnAction(actionEvent -> {
                TreeItem<Object> treeItem = searchTreeView.getSelectionModel().getSelectedItem();
                switch (treeItem.getValue()) {
                    case LatestVersionSearchResult latestVersionSearchResult ->
                            displayInDetailsView.accept(latestVersionSearchResult.latestVersion().get());
                    default -> displayInDetailsView.accept(treeItem.getValue());
                }
            });
            contextMenu.getItems().add(openNewWindow);

            Runnable showInConceptNavigator = () -> {
                TreeItem<Object> treeItem = searchTreeView.getSelectionModel().getSelectedItem();
                switch (treeItem.getValue()) {
                    case LatestVersionSearchResult latestVersionSearchResult -> {
                        int conceptNid = latestVersionSearchResult.latestVersion().get().nid();
                        getNavigatorNode().getController().showConcept(conceptNid);
                        getNavigatorNode().getController().expandAndSelect(IntIds.list.of(conceptNid));
                    }
                    default -> {
                    }
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

    /**
     * Creates and displays a concept window for the given concept using default settings.
     * <p>
     * This method is a convenience overload that delegates to
     * {@link #makeConceptWindow(ObservableViewNoOverride, ConceptFacade, NidTextEnum, Map)}
     * with the default {@link NidTextEnum} value of {@code NID_TEXT} and no concept window settings.
     *
     * @param windowView    the current window view context (of type {@link ObservableViewNoOverride})
     * @param conceptFacade the {@link ConceptFacade} representing the concept to be displayed
     */
    private void makeConceptWindow(ObservableViewNoOverride windowView, ConceptFacade conceptFacade) {
        // This is our overloaded method to call makeConceptWindow when no map is created yet.
        makeConceptWindow(windowView, conceptFacade, NID_TEXT, null);
    }

    /**
     * Creates and displays a concept window for the given concept.
     * <p>
     * An on-close handler is attached so that when the window is closed, it is removed from the workspace and its
     * associated preferences are cleaned up.
     *
     * @param windowView               the current window view context (of type {@link ObservableViewNoOverride})
     * @param conceptFacade            the {@link ConceptFacade} representing the concept to be displayed
     * @param nidTextEnum              the {@link NidTextEnum} indicating the type of the concept (e.g.,
     *                                 {@code NID_TEXT} or {@code SEMANTIC_ENTITY})
     * @param conceptWindowSettingsMap an optional map of {@link ConceptWindowSettings} to configure the window's
     *                                 initial properties. May be {@code null} if no concept window settings are
     *                                 provided
     */
    private void makeConceptWindow(ObservableViewNoOverride windowView, ConceptFacade conceptFacade,
                                   NidTextEnum nidTextEnum, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        ConceptKlWindowFactory conceptKlWindowFactory = new ConceptKlWindowFactory();
        ViewProperties viewProperties = windowView.makeOverridableViewProperties();
        ConceptKlWindow conceptKlWindow = conceptKlWindowFactory.create(journalTopic, conceptFacade, windowView,
                viewProperties, null);
        activityStreams.add(conceptKlWindow.getDetailsActivityStreamKey());

        // Adding the concept window panel as a child to the workspace.
        workspace.getWindows().add(conceptKlWindow);

        // Getting the details node from the concept window
        DetailsNode detailsNode = conceptKlWindow.getDetailsNode();
        detailsNode.getDetailsViewController().onReasonerSlideoutTray(reasonerToggleConsumer);

        // This will refresh the Concept details, history, timeline
        detailsNode.handleActivity(Lists.immutable.of(conceptFacade));

        // Getting the concept window pane
        final Pane conceptWindowPane = conceptKlWindow.fxGadget();

        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        final String conceptFolderName = createConceptFolderName(conceptWindowPane, conceptWindowSettingsMap);

        // Add to the list of concept windows
        conceptWindows.add(new ConceptPreference(conceptFolderName, nidTextEnum, conceptFacade.nid(), conceptWindowPane));

        // Calls the remove method to remove and concepts that were closed by the user.
        conceptKlWindow.setOnClose(() -> {
            removeConceptSetting(conceptFolderName, detailsNode);
            workspace.getWindows().remove(conceptKlWindow);
        });
    }

    /**
     * Creates and displays a blank concept window intended for creating a new concept.
     * <p>
     * An on-close handler is attached to ensure that when the window is closed, it is removed from the workspace and
     * its associated preferences are cleaned up.
     *
     * @param windowView               the current window view context (of type {@link ObservableViewNoOverride})
     * @param nidTextEnum              the {@link NidTextEnum} representing the type of the concept window
     *                                 (e.g., {@code NID_TEXT})
     * @param conceptWindowSettingsMap an optional map of {@link ConceptWindowSettings} to configure the window's
     *                                 initial properties (such as folder name, position, and size). May be
     *                                 {@code null} if no concept window settings are provided
     */
    private void makeCreateConceptWindow(ObservableViewNoOverride windowView, NidTextEnum nidTextEnum,
                                         Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        ConceptKlWindowFactory conceptKlWindowFactory = new ConceptKlWindowFactory();
        ViewProperties viewProperties = windowView.makeOverridableViewProperties();
        ConceptKlWindow conceptKlWindow = conceptKlWindowFactory.create(journalTopic,
                null, windowView, viewProperties, null);
        activityStreams.add(conceptKlWindow.getDetailsActivityStreamKey());

        // Adding the concept window panel as a child to the workspace.
        workspace.getWindows().add(conceptKlWindow);

        // Getting the details node from the concept window
        DetailsNode detailsNode = conceptKlWindow.getDetailsNode();
        detailsNode.getDetailsViewController().onReasonerSlideoutTray(reasonerToggleConsumer);

        // Getting the concept window pane
        final Pane conceptWindowPane = conceptKlWindow.fxGadget();

        // This will refresh the Concept details, history, timeline
        //detailsNode.handleActivity(Lists.immutable.of(conceptFacade));

        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        final String conceptFolderName = createConceptFolderName(conceptWindowPane, conceptWindowSettingsMap);

        // add to the list of concept windows
        conceptWindows.add(new ConceptPreference(conceptFolderName, nidTextEnum, -1, conceptWindowPane));

        // Calls the remove method to remove and concepts that were closed by the user.
        conceptKlWindow.setOnClose(() -> {
            removeConceptSetting(conceptFolderName, detailsNode);
            workspace.getWindows().remove(conceptKlWindow);
        });
    }

    /**
     * Creates and displays a new LIDR window for editing or creating a LIDR record.
     * <p>
     * An on-close handler is attached to ensure that when the window is closed, it is removed from the workspace and
     * its associated preferences are cleaned up.
     *
     * @param windowView               the current window view context (of type {@link ObservableViewNoOverride})
     * @param nidTextEnum              the {@link NidTextEnum} representing the type of the window for display purposes
     * @param conceptWindowSettingsMap an optional map of {@link ConceptWindowSettings} to configure the window's
     *                                 initial properties. May be {@code null} if no concept window settings are
     *                                 provided
     */
    private void makeCreateLidrWindow(ObservableViewNoOverride windowView, NidTextEnum nidTextEnum,
                                      Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        LidrKlWindowFactory lidrKlWindowFactory = new LidrKlWindowFactory();
        LidrKlWindow lidrKlWindow = lidrKlWindowFactory.create(journalTopic, null, null,
                windowView.makeOverridableViewProperties(), null);
        // Adding the concept window panel as a child to the workspace.
        workspace.getWindows().add(lidrKlWindow);

        // Getting the concept window pane
        final Pane conceptWindowPane = lidrKlWindow.fxGadget();

        // This will refresh the Concept details, history, timeline
        //detailsNode.handleActivity(Lists.immutable.of(conceptFacade));

        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        final String conceptFolderName = createConceptFolderName(conceptWindowPane, conceptWindowSettingsMap);

        // Add to the list of concept windows
        conceptWindows.add(new ConceptPreference(conceptFolderName, nidTextEnum, -1, conceptWindowPane));

        // Calls the remove method to remove and concepts that were closed by the user.
        lidrKlWindow.setOnClose(() -> {
            removeLidrSetting(conceptFolderName);
            workspace.getWindows().remove(lidrKlWindow);
        });
    }

    /**
     * Creates and displays a view/edit LIDR window for the specified device concept.
     * <p>
     * An on-close handler is attached to ensure that when the window is closed, it is removed from the workspace and
     * its associated preferences are cleaned up.
     *
     * @param windowView               the current window view context (of type {@link ObservableViewNoOverride})
     * @param deviceConcept            the {@link ConceptFacade} representing the device concept to be viewed or edited
     * @param nidTextEnum              the {@link NidTextEnum} representing the type of the window for display purposes
     * @param conceptWindowSettingsMap an optional map of {@link ConceptWindowSettings} to configure the window's
     *                                 initial properties. May be {@code null} if no concept window settings are
     *                                 provided
     */
    private void makeViewEditLidrWindow(ObservableViewNoOverride windowView, ConceptFacade deviceConcept,
                                        NidTextEnum nidTextEnum, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        LidrKlWindowFactory lidrKlWindowFactory = new LidrKlWindowFactory();
        LidrKlWindow lidrKlWindow = lidrKlWindowFactory.create(journalTopic, null, deviceConcept,
                windowView.makeOverridableViewProperties(), null);
        // Adding the concept window panel as a child to the workspace.
        workspace.getWindows().add(lidrKlWindow);

        // Getting the concept window pane
        final Pane conceptWindowPane = lidrKlWindow.fxGadget();

        // This will refresh the Concept details, history, timeline
        //detailsNode.handleActivity(Lists.immutable.of(conceptFacade));

        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        final String conceptFolderName = createConceptFolderName(conceptWindowPane, conceptWindowSettingsMap);

        // add to the list of concept windows
        conceptWindows.add(new ConceptPreference(conceptFolderName, nidTextEnum, -1, conceptWindowPane));

        // Calls the remove method to remove and concepts that were closed by the user.
        lidrKlWindow.setOnClose(() -> {
            removeLidrSetting(conceptFolderName);
            workspace.getWindows().remove(lidrKlWindow);
        });
    }

    /**
     * Generates a unique concept folder name for a concept window and configures the associated pane's layout
     * properties based on the provided settings.
     * <p>
     * If a non-null {@code conceptWindowSettingsMap} is provided, this method retrieves the folder name using the
     * {@code CONCEPT_PREF_NAME} key (defaulting to a generated value if not present) and then sets the pane's
     * translation (X and Y) and preferred size (width and height) based on the values in the map.
     * <p>
     * If the {@code conceptWindowSettingsMap} is {@code null}, a new unique folder name is generated using a
     * predefined prefix and a random UUID. In this case, a new settings map is created via
     * {@link #createConceptPrefMap(String, Pane)} and is set as the user data on the provided {@code conceptWindowPane}.
     *
     * @param conceptWindowPane        the JavaFX {@link Pane} representing the concept window to be configured
     * @param conceptWindowSettingsMap a map containing settings (such as folder name, position, and size) for the
     *                                 concept window; may be {@code null} if no settings are provided
     * @return the generated or retrieved unique concept folder name
     */
    private String createConceptFolderName(Pane conceptWindowPane, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {
        final String conceptFolderName;
        if (conceptWindowSettingsMap != null) {
            conceptFolderName = String.valueOf(conceptWindowSettingsMap.getOrDefault(CONCEPT_PREF_NAME,
                    CONCEPT_FOLDER_PREFIX + UUID.randomUUID()));
        } else {
            conceptFolderName = CONCEPT_FOLDER_PREFIX + UUID.randomUUID();
            // create a conceptWindowSettingsMap
            Map<ConceptWindowSettings, Object> conceptWindowSettingsObjectMap =
                    createConceptPrefMap(conceptFolderName, conceptWindowPane);
            conceptWindowPane.setUserData(conceptWindowSettingsObjectMap);
        }

        // Checking if map is null (if yes not values are set) if not null, setting position of concept windows.
        if (conceptWindowSettingsMap != null) {
            conceptWindowPane.setTranslateX((Double) conceptWindowSettingsMap.get(CONCEPT_XPOS));
            conceptWindowPane.setTranslateY((Double) conceptWindowSettingsMap.get(CONCEPT_YPOS));
            conceptWindowPane.setPrefWidth((Double) conceptWindowSettingsMap.get(CONCEPT_WIDTH));
            conceptWindowPane.setPrefHeight((Double) conceptWindowSettingsMap.get(CONCEPT_HEIGHT));
        }

        return conceptFolderName;
    }

    /**
     * Creates a map containing the current concept panel (window's) preferences.
     *
     * @param conceptPrefDirName a unique name used as the directory name in preferences and for identifying
     *                           the concept window
     * @param conceptWindowPane  the JavaFX pane representing the concept window whose settings are being captured
     * @return a map of {@link ConceptWindowSettings} keys to their corresponding preference values
     */
    private Map<ConceptWindowSettings, Object> createConceptPrefMap(String conceptPrefDirName, Pane conceptWindowPane) {
        Map<ConceptWindowSettings, Object> conceptWindowSettingsMap = new HashMap<>();
        conceptWindowSettingsMap.put(CONCEPT_PREF_NAME, conceptPrefDirName);
        conceptWindowSettingsMap.put(CONCEPT_HEIGHT, conceptWindowPane.getPrefHeight());
        conceptWindowSettingsMap.put(CONCEPT_WIDTH, conceptWindowPane.getPrefWidth());
        conceptWindowSettingsMap.put(CONCEPT_XPOS, conceptWindowPane.getLayoutX());
        conceptWindowSettingsMap.put(CONCEPT_YPOS, conceptWindowPane.getLayoutX());
        return conceptWindowSettingsMap;
    }

    /**
     * Removes the concept details node (Pane) from the scene graph, closes activity streams, and removes preferences from locally.
     *
     * @param conceptDirectoryName - The unique concept dir name used in each journal window.
     * @param detailsNode          - The Concept detailsNode - referencing both JavaFX Node and view.
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
     *
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
     *
     * @param navigationActivityStreamKey The newly generated navigation activity stream for this Journal window and all children.
     * @param windowView                  Any window view information
     * @param navigationFactory           The navigation factory to create the navigation panel to be used in the sidebar.
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
            makeViewEditLidrWindow(windowView, item.getValue(), null, null);
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

        ViewProperties viewProperties = windowView.makeOverridableViewProperties();
        Pane navigatorNodePanel = loadClassicConceptNavigator(navigationActivityStreamKey, windowView, navigationFactory);
        Config patternConceptConfig = new Config(ConceptPatternNavController.class.getResource(CONCEPT_PATTERN_NAV_FXML_URL))
                .controller(new ConceptPatternNavController(navigatorNodePanel))
                .updateViewModel("patternNavViewModel", (patternNavViewModel) ->
                        patternNavViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties)
                                .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                );
        JFXNode<StackPane, ConceptPatternNavController> conPatJFXNode = FXMLMvvmLoader.make(patternConceptConfig);
        patternConceptNavigationPanel = conPatJFXNode.node();
        conceptPatternNavController = conPatJFXNode.controller();
        setupSlideOutTrayPane(patternConceptNavigationPanel, navSlideoutTrayPane);
    }

    private void loadReasonerPanel(PublicIdStringKey<ActivityStream> activityStreamKey,
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
                ConceptKlWindowFactory conceptKlWindowFactory = new ConceptKlWindowFactory();
                ViewProperties viewProperties = windowView.makeOverridableViewProperties();
                ConceptKlWindow conceptKlWindow = conceptKlWindowFactory.create(journalTopic, conceptFacade, windowView, viewProperties, null);
                activityStreams.add(conceptKlWindow.getDetailsActivityStreamKey());

                // Getting the details node from the concept window
                DetailsNode detailsNode = conceptKlWindow.getDetailsNode();
                detailsNode.getDetailsViewController().onReasonerSlideoutTray(reasonerToggleConsumer);

                // Getting the concept window pane
                Pane kometNodePanel = conceptKlWindow.fxGadget();

                // Make the window compact sized.
                detailsNode.getDetailsViewController().compactSizeWindow();

                double x = kometNodePanel.getPrefWidth() * (staggerWindowsX.getAndAdd(1) % 3) + 5; // stagger windows
                double y = kometNodePanel.getPrefHeight() * (staggerWindowsY.get()) + 5; // stagger windows

                kometNodePanel.setLayoutX(x);
                kometNodePanel.setLayoutY(y);

                // Adding the concept window panel as a child to the workspace.
                if (staggerWindowsX.get() % 3 == 0) {
                    staggerWindowsY.incrementAndGet();
                }

                // Adding the concept window panel as a child to the workspace.
                workspace.getWindows().add(conceptKlWindow);

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
            } else if (detailToggleReasonerButton.isSelected() && !reasonerToggleButton.isSelected()) {
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
        return jStage.getWidth();
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
     *
     * @return
     */
    public String generateJournalDirNameBasedOnTitle() {
        return JOURNAL_FOLDER_PREFIX + getTitle().replace(" ", "_");
    }

    public void saveConceptWindowPreferences(KometPreferences journalSubWindowPreferences) {
        List<String> conceptFolderNames = new ArrayList<>();
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();

        //Looping through each concept window to save position and size to preferences.
        for (ConceptPreference conceptPreference : conceptWindows) {

            // skip concept windows without a proper nid, these could be concepts that are not fully created
            if (conceptPreference.getNid().equals(-1)) {
                continue;
            }

            final String conceptPrefName = conceptPreference.getDirectoryName();
            conceptFolderNames.add(conceptPrefName);

            // Applying the preferences naming convention to the files.
            // e.g., journal-window/JOURNAL_Journal_1/CONCEPT_XXX
            try {
                KometPreferences conceptPreferences = journalSubWindowPreferences.node(conceptPrefName);
                conceptPreferences.put(CONCEPT_PREF_NAME, conceptPrefName);
                conceptPreferences.put(NID_TYPE, conceptPreference.getNidType().toString());
                conceptPreferences.putInt(NID_VALUE, conceptPreference.getNid());
                conceptPreferences.putDouble(CONCEPT_HEIGHT, conceptPreference.getConceptPane().getPrefHeight());
                conceptPreferences.putDouble(CONCEPT_WIDTH, conceptPreference.getConceptPane().getPrefWidth());
                conceptPreferences.putDouble(CONCEPT_XPOS, conceptPreference.getConceptPane().getTranslateX());
                conceptPreferences.putDouble(CONCEPT_YPOS, conceptPreference.getConceptPane().getTranslateY());
                conceptPreferences.flush();
            } catch (Exception e) {
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
        for (String conceptFolder : conceptList) {
            KometPreferences conceptPreferences = appPreferences.node(JOURNAL_WINDOW +
                    separator + journalPref.getValue(JOURNAL_DIR_NAME) +
                    separator + conceptFolder);
            KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
            WindowSettings windowSettings = new WindowSettings(windowPreferences);
            ObservableViewNoOverride window = windowSettings.getView();

            //Getting nid type via the Enum.
            String nidTextString = conceptPreferences.get(conceptPreferences.enumToGeneralKey(NID_TYPE)).get();
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
            conceptWindowSettingsMap.put(CONCEPT_PREF_NAME, conceptPreferences.get(CONCEPT_PREF_NAME).orElse(null));
            conceptWindowSettingsMap.put(CONCEPT_HEIGHT, conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_HEIGHT), DEFAULT_CONCEPT_HEIGHT));
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
     *
     * @param styleClass
     * @return
     */
    private Region createMenuIcon(String styleClass) {
        Region graphic = new Region();
        graphic.getStyleClass().add(styleClass);
        return graphic;
    }

    /**
     * When user selects menuitem to create a new concept
     *
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
        makePatternWindow(null, windowSettings.getView().makeOverridableViewProperties());
    }

    private void makePatternWindow(EntityFacade patternFacade, ViewProperties viewProperties) {
        // TODO: Use pluggable service loader to load KlWindowFactories. and locate GenEditingKlWindow.
        PatternKlWindowFactory entityKlWindowFactory = new PatternKlWindowFactory();
        PatternKlWindow patternKlWindow = entityKlWindowFactory.create(journalTopic, patternFacade, viewProperties, null);
        workspace.getWindows().add(patternKlWindow);

        // FIXME are both LIDR and Pattern windows borrowing the concept folder for preferences?
        // If a concept window is newly launched assign it a unique id 'CONCEPT_XXX-XXXX-XX'
        Optional<String> conceptFolderName;
        conceptFolderName = Optional.of(CONCEPT_FOLDER_PREFIX + UUID.randomUUID());
        // create a conceptWindowSettingsMap
        Pane chapterWindow = patternKlWindow.fxGadget();
        Map<ConceptWindowSettings, Object> conceptWindowSettingsObjectMap = createConceptPrefMap(conceptFolderName.get(), chapterWindow);
        chapterWindow.setUserData(conceptWindowSettingsObjectMap);

        // add to the list of concept windows
        final String finalConceptFolderName = conceptFolderName.get();
        conceptWindows.add(new ConceptPreference(conceptFolderName.get(), null, -1, chapterWindow));

        patternKlWindow.setOnClose(() -> {
            removeLidrSetting(finalConceptFolderName);
            workspace.getWindows().remove(patternKlWindow);
        });
        patternKlWindow.onShown();
    }

    private void makeGenEditWindow(EntityFacade entityFacade, ViewProperties viewProperties) {
        // TODO: Use pluggable service loader to load KlWindowFactories. and locate GenEditingKlWindow.
        GenEditingKlWindowFactory entityKlWindowFactory = new GenEditingKlWindowFactory();
        GenEditingKlWindow genEditingKlWindow = entityKlWindowFactory.create(journalTopic, entityFacade, viewProperties, null);
        // Adding the concept window panel to the workspace.
        workspace.getWindows().add(genEditingKlWindow);
        genEditingKlWindow.onShown(); // TODO: Revisit. JavaFX post render issue. Must be called after Node is rendered (realized). When not realized the implied style classes don't exist and returns null.

        genEditingKlWindow.setOnClose(() -> workspace.getWindows().remove(genEditingKlWindow));
    }
}
