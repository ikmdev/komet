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

import static dev.ikm.komet.framework.dnd.KometClipboard.MULTI_PARENT_GRAPH_DRAG_FORMAT;
import static dev.ikm.tinkar.events.FrameworkTopics.CALCULATOR_CACHE_TOPIC;
import static dev.ikm.tinkar.events.FrameworkTopics.PROGRESS_TOPIC;
import static dev.ikm.komet.framework.events.appevents.ProgressEvent.SUMMON;
import static dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;
import static dev.ikm.komet.kview.controls.KLWorkspace.DESKTOP_PANE_STYLE_CLASS;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.fxutils.FXUtils.runOnFxThread;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.setupSlideOutTrayPane;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowFactory.Registry.createFromEntity;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowFactory.Registry.createFromUuids;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowFactory.Registry.createWindow;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowFactory.Registry.extractEntityFromDragInfo;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowFactory.Registry.restoreWindow;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowState.ENTITY_NID_TYPE;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowTypes.GEN_EDITING;
import static dev.ikm.komet.kview.klwindows.EntityKlWindowTypes.PATTERN;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalPreferences;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.shortenUUID;
import static dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageController.DEMO_AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.JournalViewModel.WINDOW_VIEW;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.CANCEL_BUTTON_TEXT_PROP;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.TASK_PROPERTY;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_AUTHOR;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_DIR_NAME;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_HEIGHT;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_LAST_EDIT;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_TITLE;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_WIDTH;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_XPOS;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_YPOS;
import static dev.ikm.komet.preferences.JournalWindowSettings.WINDOW_COUNT;
import static dev.ikm.komet.preferences.JournalWindowSettings.WINDOW_NAMES;
import static dev.ikm.komet.preferences.NidTextEnum.NID_TEXT;
import static javafx.stage.PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.events.appevents.ProgressEvent;
import dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.framework.tabs.DetachableTab;
import dev.ikm.komet.framework.tabs.TabGroup;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewMenuTask;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.controls.NotificationPopup;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.events.ShowNavigationalPanelEvent;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent;
import dev.ikm.komet.kview.events.reasoner.CloseReasonerPanelEvent;
import dev.ikm.komet.kview.fxutils.FXUtils;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.fxutils.SlideOutTrayHelper;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.ChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils;
import dev.ikm.komet.kview.klwindows.concept.ConceptKlWindow;
import dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.view.details.DetailsNode;
import dev.ikm.komet.kview.mvvm.view.navigation.ConceptPatternNavController;
import dev.ikm.komet.kview.mvvm.view.progress.ProgressController;
import dev.ikm.komet.kview.mvvm.view.reasoner.NextGenReasonerController;
import dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController;
import dev.ikm.komet.kview.mvvm.viewmodel.JournalViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.NextGenSearchViewModel;
import dev.ikm.komet.navigator.graph.GraphNavigatorNode;
import dev.ikm.komet.preferences.KometPreferences;
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
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.SemanticFacade;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DataFormat;
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
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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

    private Pane conceptNavigatorSlideoutTrayPane;

    private Pane nexGenSearchSlideoutTrayPane;

    private Pane nextGenReasonerSlideoutTrayPane;

    private NotificationPopup progressNotificationPopup;

    @FXML
    private MenuButton coordinatesMenuButton;

    @FXML
    private Menu windowCoordinates;

    @FXML
    private ToggleButton reasonerToggleButton;

    @FXML
    private ToggleButton conceptNavigatorToggleButton;

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

    private static Toast toast;


    /////////////////////////////////////////////////////////////////
    // Private Data
    /// //////////////////////////////////////////////////////////////

    private WindowSettings windowSettings;
    private KometPreferences nodePreferences;

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
    private UUID journalTopic;
    private final EvtBus journalEventBus = EvtBusFactory.getDefaultEvtBus();
    private volatile boolean isSlideOutOpen = false;

    private final Executor VIRTUAL_TASK_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private final List<PublicIdStringKey<ActivityStream>> activityStreams = new ArrayList<>();

    private static Consumer<ToggleButton> reasonerToggleConsumer;

    private GraphNavigatorNode navigatorNode;
    private ObservableList<ChapterKlWindow<Pane>> journalWindows;

    private static final String NEXT_GEN_SEARCH_FXML_URL = "next-gen-search.fxml";

    private static final String CONCEPT_PATTERN_NAV_FXML_URL = "navigation-concept-pattern.fxml";

    private static final String NEXT_GEN_REASONER_FXML_URL = "reasoner.fxml";

    private NextGenSearchController nextGenSearchController;

    private NextGenReasonerController nextGenReasonserController;

    private ConceptPatternNavController conceptPatternNavController;

    //////////////////// Subscribers /////////////////////////////////////////////////
    private Subscriber<MakeConceptWindowEvent> makeConceptWindowEventSubscriber;

    private Subscriber<MakePatternWindowEvent> makePatternWindowEventSubscriber;

    private Subscriber<MakeGenEditingWindowEvent> makeGenEditWindowEventSubscriber;

    private Subscriber<ShowNavigationalPanelEvent> showNavigationalPanelEventSubscriber;

    private Subscriber<CloseReasonerPanelEvent> closeReasonerPanelEventSubscriber;

    private Subscriber<RefreshCalculatorCacheEvent> refreshCalculatorEventSubscriber;
    //////////////////////////////////////////////////////////////////////////////////

    @InjectViewModel
    private NextGenSearchViewModel nextGenSearchViewModel;

    @InjectViewModel
    private JournalViewModel journalViewModel;

    private ObservableViewNoOverride windowView;

    /**
     * Called after JavaFX FXML DI has occurred. Any annotated items above should be valid.
     */
    @FXML
    public void initialize() {
        // Initialize journal topic (UUID) value
        journalTopic = journalViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC);

        // Initialize the journal window view
        windowView = journalViewModel.getPropertyValue(WINDOW_VIEW);

        // Initialize the journal windows list
        journalWindows = FXCollections.unmodifiableObservableList(workspace.getWindows());

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

        journalWindows.addListener((ListChangeListener<ChapterKlWindow<Pane>>) change -> {
            while (change.next()) {
                PrefX journalWindowPref = PrefX.create();
                journalWindowPref.setValue(WINDOW_COUNT, journalWindows.size());
                journalWindowPref.setValue(JOURNAL_TOPIC, getJournalTopic());
                journalWindowPref.setValue(JOURNAL_TITLE, getTitle());
                journalWindowPref.setValue(JOURNAL_DIR_NAME, getJournalDirName());

                journalEventBus.publish(JOURNAL_TOPIC,
                        new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowPref));
            }
        });
        reasonerToggleConsumer = createReasonerToggleConsumer();

        makeConceptWindowEventSubscriber = evt -> {
            EntityFacade entityFacade = evt.getEntityFacade();
            if (entityFacade instanceof ConceptFacade conceptFacade) {
                createConceptWindow(conceptFacade, NID_TEXT, null);
            } else if (entityFacade instanceof PatternFacade patternFacade) {
                createPatternWindow(patternFacade, getNavigatorNode().getViewProperties());
            } else if (entityFacade instanceof SemanticFacade semanticFacade) {
                createGenEditWindow(semanticFacade, getNavigatorNode().getViewProperties(), false);
            }
        };
        journalEventBus.subscribe(journalTopic, MakeConceptWindowEvent.class, makeConceptWindowEventSubscriber);

        makePatternWindowEventSubscriber = evt ->
                createPatternWindow(evt.getPatternFacade(), evt.getViewProperties());
        journalEventBus.subscribe(journalTopic, MakePatternWindowEvent.class, makePatternWindowEventSubscriber);

        // Listening for when a General authoring Window needs to be summoned.
        makeGenEditWindowEventSubscriber = evt ->
            // If the pattern is passed as a component, then it is a right click > new semantic and therefore
            // the flag for opening the properties panel should be set to true.
            // If the semantic is passed as a component, then we are in general editing mode and therefore
            // the flag for opening the properties panel should be set to false.
            createGenEditWindow(evt.getComponent(), evt.getViewProperties(), evt.getComponent() instanceof PatternFacade);

        journalEventBus.subscribe(journalTopic, MakeGenEditingWindowEvent.class, makeGenEditWindowEventSubscriber);

        showNavigationalPanelEventSubscriber = evt -> {
            navigatorToggleButton.setSelected(true);
            // expand and select concept in conceptNavigator
            conceptPatternNavController.showConcept(evt.getConceptFacade().nid());

            // toggle CONCEPTS inside conceptPatternNavController
            conceptPatternNavController.toggleConcepts();
        };
        journalEventBus.subscribe(journalTopic, ShowNavigationalPanelEvent.class, showNavigationalPanelEventSubscriber);

        // listening to the event fired when the user clicks the 'X' on the reasoner slide out
        // and wire into the toggle group because we already have a listener on this property
        closeReasonerPanelEventSubscriber = evt -> sidebarToggleGroup.selectToggle(null);
        journalEventBus.subscribe(JOURNAL_TOPIC, CloseReasonerPanelEvent.class, closeReasonerPanelEventSubscriber);

        // initialize drag and drop for search results of next gen search
        setupDragNDrop(workspace);

        // Hide the progress toggle button by default
        progressToggleButton.setVisible(false);

        // Listen for progress tasks
        setupProgressListener();

        workspace.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClickedOnDesktopSurfacePane);

        // Refresh Concept navigator
        refreshCalculatorEventSubscriber = _ -> {
            // TODO Must refresh the cache or invalidate after a gitsync or import.
            getNavigatorNode().getController().refresh();
        };

        journalEventBus.subscribe(CALCULATOR_CACHE_TOPIC, RefreshCalculatorCacheEvent.class, refreshCalculatorEventSubscriber);

        toast = new Toast(workspace);
    }

    /**
     * Setup the controller to have a filter coordinates menu.  KometPreferences is required to create the
     * WindowSettings object, which is used to get access to the view that the filter coordinates are applied to.
     * @param nodePreferences The preferences for the Journal window
     */
    public void setup(KometPreferences nodePreferences) {
        this.nodePreferences = nodePreferences;
        this.windowSettings = new WindowSettings(nodePreferences);

        windowView = windowSettings.getView();

        ViewCalculatorWithCache viewCalculator = ViewCalculatorWithCache.getCalculator(windowView.toViewCoordinateRecord());

        TinkExecutor.threadPool().execute(TaskWrapper.make(new ViewMenuTask(viewCalculator, windowView, "JournalController"),
                (List<MenuItem> result) -> {
                    FXUtils.runOnFxThread(() -> windowCoordinates.getItems().addAll(result));
                }));

        windowSettings.getView().addListener((observable, oldValue, newValue) -> {
            windowCoordinates.getItems().clear();
            TinkExecutor.threadPool().execute(TaskWrapper.make(new ViewMenuTask(viewCalculator, windowView, "JournalController"),
                    (List<MenuItem> result) ->
                            FXUtils.runOnFxThread(() -> windowCoordinates.getItems().addAll(result))
            ));
        });
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
        conceptNavigatorSlideoutTrayPane = newTrayPane();

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
                conceptNavigatorSlideoutTrayPane,
                nexGenSearchSlideoutTrayPane,
                nextGenReasonerSlideoutTrayPane
        );
    }

    private Pane newTrayPane() {
        Pane pane = new VerticallyFilledPane();
        pane.getStyleClass().add("slideout-tray-pane");
        return pane;
    }

    /**
     * Configures drag and drop event handling for the specified workspace node.
     * Sets up a chain of handlers to process different drag data formats (UUID arrays and entity-based drags)
     * with comprehensive error handling and logging.
     *
     * @param node the JavaFX node to enable drag and drop functionality on
     */
    private void setupDragNDrop(Node node) {
        node.setOnDragDropped(event -> {
            boolean success = false;
            try {
                Optional<Dragboard> dragboardOpt = Optional.of(event.getDragboard());
                success = dragboardOpt
                        .filter(db -> db.hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT))
                        .map(db -> handleUuidArrayDrag(db, CONCEPT_NAVIGATOR_DRAG_FORMAT))
                        .or(() -> dragboardOpt
                                .filter(db -> db.hasContent(MULTI_PARENT_GRAPH_DRAG_FORMAT))
                                .map(db -> handleUuidArrayDrag(db, MULTI_PARENT_GRAPH_DRAG_FORMAT)))
                        .or(() -> dragboardOpt
                                .filter(Dragboard::hasString)
                                .map(db -> handleEntityDrag(event.getGestureSource())))
                        .orElse(false);
            } catch (Exception ex) {
                LOG.error("Error while processing drag and drop: ", ex);
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Processes drag operations containing UUID arrays from concept navigator or proxy list formats.
     * Extracts nested UUID arrays from the drag board content and creates corresponding windows
     * for each valid UUID array found.
     *
     * @param dragboard the JavaFX drag board containing the drag data
     * @param dataFormat the specific data format to extract
     * @return true if at least one window was successfully created, false otherwise
     */
    private boolean handleUuidArrayDrag(Dragboard dragboard, DataFormat dataFormat) {
        Object content = dragboard.getContent(dataFormat);
        if (!(content instanceof List<?> list) || list.isEmpty()) {
            return false;
        }

        ImmutableList<UUID[]> uuidArrays = extractUuidArrays(list).toImmutable();
        uuidArrays.forEach(this::createWindowFromUuids);
        return uuidArrays.notEmpty();
    }

    /**
     * Recursively extracts UUID arrays from a potentially nested list structure.
     * Handles arbitrary nesting levels commonly found in drag-and-drop data formats,
     * ignoring null values and unexpected item types with debug logging.
     *
     * @param list the list to process, may contain UUID arrays, nested lists, or other objects
     * @return mutable list containing all UUID arrays found at any nesting level
     */
    private MutableList<UUID[]> extractUuidArrays(List<?> list) {
        MutableList<UUID[]> result = Lists.mutable.empty();

        for (Object item : list) {
            switch (item) {
                case UUID[] uuids -> result.add(uuids);
                case List<?> nested -> result.addAll(extractUuidArrays(nested));
                case null -> LOG.debug("Ignoring null item in drag data");
                default -> LOG.debug("Ignoring unrecognized drag item: {}",
                        item.getClass().getSimpleName());
            }
        }

        return result;
    }

    /**
     * Processes entity-based drag operations from UI components like search results or tree cells.
     * Extracts the EntityFacade from the gesture source and creates an appropriate window
     * if a valid entity is found.
     *
     * @param gestureSource the UI component that initiated the drag operation
     * @return true if a window was successfully created, false if no valid entity was found
     */
    private boolean handleEntityDrag(Object gestureSource) {
        EntityFacade entity = extractEntityFromSource(gestureSource);
        if (entity != null) {
            createWindowFromEntity(entity);
            return true;
        }
        return false;
    }

    /**
     * Extracts EntityFacade instances from various UI drag sources using type-specific logic.
     * Handles SearchResultCell components and Node objects with DragAndDropInfo user data,
     * delegating to specialized extraction methods for each source type.
     *
     * @param source the drag gesture source object, typically a UI component
     * @return the extracted EntityFacade, or null if the source type is unsupported or contains no valid entity
     */
    private EntityFacade extractEntityFromSource(Object source) {
        return switch (source) {
            case SearchResultCell cell -> extractFromSearchCell(cell);
            case Node node when node.getUserData() instanceof DragAndDropInfo info -> extractEntityFromDragInfo(info);
            default -> null;
        };
    }

    /**
     * Extracts EntityFacade from SearchResultCell items, handling both direct concept records
     * and semantic search results. For semantic results, attempts to find the associated
     * concept using the semantic's referenced component.
     *
     * @param cell the SearchResultCell containing the dragged search result
     * @return the extracted EntityFacade (typically ConceptFacade), or null if extraction fails
     */
    private EntityFacade extractFromSearchCell(SearchResultCell cell) {
        return switch (cell.getItem()) {
            case SearchPanelController.NidTextRecord record ->
                    ConceptFacade.make(record.nid());
            case LatestVersionSearchResult result when result.latestVersion().isPresent() ->
                    Entity.getConceptForSemantic(result.latestVersion().get().nid()).orElse(null);
            default -> null;
        };
    }

    /**
     * Creates and displays a new window from a UUID array using the entity factory.
     * Delegates to the generic window creation helper with appropriate error context for logging.
     * The UUID array is used to resolve the entity and determine the appropriate window type.
     *
     * @param uuids the UUID array identifying the entity to display in the new window
     */
    public void createWindowFromUuids(UUID[] uuids) {
        createAndSetupWindow(() -> createFromUuids(uuids, journalTopic,
                        windowView.makeOverridableViewProperties(), null),
                "UUID array: " + ArrayIterate.makeString(uuids));
    }

    /**
     * Creates and displays a new window for the specified EntityFacade. Performs null checking and delegates
     * to the generic window creation helper with appropriate error context. The entity type determines the specific
     * window implementation.
     *
     * @param entityFacade the entity to display in the new window, or null to skip creation
     */
    private void createWindowFromEntity(EntityFacade entityFacade) {
        if (entityFacade == null) return;

        createAndSetupWindow(() -> createFromEntity(entityFacade, journalTopic,
                        windowView.makeOverridableViewProperties(), null),
                "entity " + entityFacade.nid());
    }

    /**
     * Generic window creation helper that handles factory delegation, error handling, and workspace setup.
     * Successfully created windows are automatically added to the workspace.
     *
     * @param windowFactory supplier that creates the window instance using factory methods
     * @param errorContext descriptive context used in error logging
     */
    private void createAndSetupWindow(Supplier<AbstractEntityChapterKlWindow> windowFactory, String errorContext) {
        try {
            AbstractEntityChapterKlWindow window = windowFactory.get();
            if (window != null) {
                setupWorkspaceWindow(window);
            } else {
                LOG.warn("Failed to create window for {}", errorContext);
            }
        } catch (Exception e) {
            LOG.error("Error creating window for {}: {}", errorContext, e.getMessage(), e);
        }
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
                runOnFxThread(() -> {
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

                    // Before adding the progress UI to the popup's vertical container
                    // Check if we already have 4 progress panes and remove the oldest one if needed
                    if (progressPopupPane.getChildren().size() >= 4) {
                        // Remove the oldest progress pane (first child)
                        Node oldestPane = progressPopupPane.getChildren().getFirst();
                        progressPopupPane.getChildren().remove(oldestPane);
                    }

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

        return FXMLMvvmLoader.make(config);
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
        } else if (conceptNavigatorToggleButton.equals(selectedToggleButton)) {
            return conceptNavigatorSlideoutTrayPane;
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
        LOG.info("Journal Window is shutting down...");

        // remove the listener, the node and activity streams
        navigatorNode.getController().getTreeView().getSelectionModel().getSelectedItems().removeListener(navigatorNode.getSelectionListener());
        navigatorNode = null;
        activityStreams.forEach(ActivityStreams::delete);

        journalEventBus.unsubscribe(makeConceptWindowEventSubscriber,
                makePatternWindowEventSubscriber,
                makeGenEditWindowEventSubscriber,
                showNavigationalPanelEventSubscriber,
                closeReasonerPanelEventSubscriber,
                refreshCalculatorEventSubscriber);
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

        loadNavigationPanel(this.windowView);
        loadClassicConceptNavigatorPanel(navigationActivityStreamKey, this.windowView, navigationFactory);

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
                            nextGenSearchViewModel.setPropertyValue(MODE, CREATE)
                                .setPropertyValue(VIEW_PROPERTIES, this.windowView.makeOverridableViewProperties())
                                .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                );

        JFXNode<Pane, NextGenSearchController> nextGenSearchJFXNode = FXMLMvvmLoader.make(nextGenSearchConfig);
        nextGenSearchController = nextGenSearchJFXNode.controller();
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
                    createConceptWindow(conceptFacade, nidTextEnum, null);
                } else if (entity instanceof PatternFacade patternFacade) {
                    createPatternWindow(patternFacade, getNavigatorNode().getViewProperties());
                } else if (entity instanceof SemanticFacade semanticFacade) {
                    createGenEditWindow(semanticFacade, getNavigatorNode().getViewProperties(), false);
                }
            } else if (treeItemValue instanceof SemanticEntityVersion semanticEntityVersion) {
                SemanticFacade semanticFacade = semanticEntityVersion.entity();
                createGenEditWindow(semanticFacade, getNavigatorNode().getViewProperties(), false);
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

    private void loadClassicConceptNavigatorPanel(PublicIdStringKey<ActivityStream> navigationActivityStreamKey,
                                                  ObservableViewNoOverride windowView, KometNodeFactory navigationFactory) {
        Pane navigatorNodePanel = loadClassicConceptNavigator(navigationActivityStreamKey, windowView, navigationFactory);

        setupSlideOutTrayPane(navigatorNodePanel, conceptNavigatorSlideoutTrayPane);
    }

    /**
     * Creates and displays a concept window for the given concept using default settings.
     * <p>
     * This method is a convenience overload that delegates to
     * {@link #createConceptWindow(ConceptFacade, NidTextEnum, KometPreferences)}
     * with the default {@link NidTextEnum} value of {@code NID_TEXT} and no custom preferences.
     *
     * @param conceptFacade the {@link ConceptFacade} representing the concept to be displayed,
     *                     or null to create an empty concept window
     */
    public void createConceptWindow(ConceptFacade conceptFacade) {
        createConceptWindow(conceptFacade, NID_TEXT, null);
    }

    /**
     * Creates and displays a concept window for the given concept with specific settings.
     * <p>
     * This method creates a window to display or edit a concept, configures it with the
     * specified settings, and adds it to the workspace. An on-close handler is attached
     * to ensure proper cleanup when the window is closed.
     *
     * @param conceptFacade the {@link ConceptFacade} representing the concept to be displayed,
     *                     or null to create an empty concept window
     * @param nidTextEnum the {@link NidTextEnum} indicating the entity type display mode
     *                   (e.g., {@code NID_TEXT} for concept view or {@code SEMANTIC_ENTITY} for semantic view)
     * @param preferences preferences for persisting window settings, or null to use default settings
     */
    private void createConceptWindow(ConceptFacade conceptFacade,
                                     NidTextEnum nidTextEnum,
                                     KometPreferences preferences) {
        if (preferences != null) {
            preferences.put(ENTITY_NID_TYPE, nidTextEnum.name());
        }

        // TODO: Test and Fixme this takes a snapshot of the navigator's view coordinate
        //       and makes a new view properties.
        ViewProperties viewProperties = windowView.makeOverridableViewProperties();

        // copying the observable view
        viewProperties.nodeView().setValue(navigatorNode.getViewProperties().nodeView().getValue());

        AbstractEntityChapterKlWindow chapterKlWindow = createWindow(EntityKlWindowTypes.CONCEPT,
                journalTopic, conceptFacade, viewProperties, preferences);
        setupWorkspaceWindow(chapterKlWindow);
    }

    /**
     * Creates and displays a pattern window for the given pattern using default settings.
     * <p>
     * This method is a convenience overload that delegates to
     * {@link #createPatternWindow(EntityFacade, ViewProperties, KometPreferences)}
     * with no custom preferences.
     *
     * @param patternFacade the {@link EntityFacade} representing the pattern to be displayed,
     *                     or null to create an empty pattern window
     * @param viewProperties the view properties to be used for rendering the pattern
     */
    private void createPatternWindow(EntityFacade patternFacade, ViewProperties viewProperties) {
        createPatternWindow(patternFacade, viewProperties, null);
    }

    /**
     * Creates and displays a pattern window for the given pattern with specific settings.
     * <p>
     * This method creates a window to display or edit a pattern, configures it with the
     * specified settings, and adds it to the workspace. An on-close handler is attached
     * to ensure proper cleanup when the window is closed.
     *
     * @param patternFacade the {@link EntityFacade} representing the pattern to be displayed,
     *                     or null to create an empty pattern window
     * @param viewProperties the view properties to be used for rendering the pattern
     * @param preferences preferences for persisting window settings, or null to use default settings
     */
    private void createPatternWindow(EntityFacade patternFacade, ViewProperties viewProperties, KometPreferences preferences) {
        AbstractEntityChapterKlWindow patternKlWindow = createWindow(PATTERN,
                journalTopic, patternFacade, viewProperties, preferences);
        setupWorkspaceWindow(patternKlWindow);
    }

    /**
     * Creates and displays a general editing window for the given entity with default settings.
     * <p>
     * This method is a convenience overload that delegates to
     * {@link #createGenEditWindow(EntityFacade, ViewProperties, KometPreferences, boolean)}
     * with no custom preferences.
     *
     * @param entityFacade the {@link EntityFacade} representing the entity to be edited,
     *                    or null to create an empty general editing window
     * @param viewProperties the view properties to be used for rendering the editor
     * @param openProperties whether to automatically open the properties panel on window creation
     *                      (true when creating from a pattern, false when editing an existing semantic)
     */
    private void createGenEditWindow(EntityFacade entityFacade, ViewProperties viewProperties, boolean openProperties) {
        createGenEditWindow(entityFacade, viewProperties, null, openProperties);
    }

    /**
     * Creates and displays a general editing window for the given entity with specific settings.
     * <p>
     * This method creates a window for general-purpose semantic editing, configures it with the
     * specified settings, and adds it to the workspace. An on-close handler is attached
     * to ensure proper cleanup when the window is closed. If requested, the properties panel
     * is automatically opened when the window is created.
     *
     * @param entityFacade the {@link EntityFacade} representing the entity to be edited,
     *                    or null to create an empty general editing window
     * @param viewProperties the view properties to be used for rendering the editor
     * @param preferences preferences for persisting window settings, or null to use default settings
     * @param openProperties whether to automatically open the properties panel on window creation
     *                      (true when creating from a pattern, false when editing an existing semantic)
     */
    private void createGenEditWindow(EntityFacade entityFacade, ViewProperties viewProperties,
                                     KometPreferences preferences, boolean openProperties) {
        AbstractEntityChapterKlWindow genEditingKlWindow = createWindow(GEN_EDITING,
                journalTopic, entityFacade, viewProperties, preferences);
        setupWorkspaceWindow(genEditingKlWindow);

        // flag set by caller to open the properties bump-out on window creation
        if (openProperties) {
            EvtBusFactory.getDefaultEvtBus().publish(genEditingKlWindow.getWindowTopic(),
                    new dev.ikm.komet.kview.events.genediting.PropertyPanelEvent(entityFacade, PropertyPanelEvent.NO_SELECTION_MADE_PANEL));
        }
    }

    /**
     * Creates and displays a new LIDR window for editing or creating a LIDR record.
     * <p>
     * This method delegates to {@link #createLidrWindow(ObservableViewNoOverride, ConceptFacade, KometPreferences)}
     * with a null device concept to indicate that a new LIDR record should be created rather than
     * an existing one edited.
     *
     * @param windowView the current window view context (of type {@link ObservableViewNoOverride})
     * @param preferences preferences for persisting window settings, or null to use default settings
     */
    private void createLidrWindow(ObservableViewNoOverride windowView, KometPreferences preferences) {
        createLidrWindow(windowView, null, preferences);
    }

    /**
     * Creates and displays a LIDR window for the specified device concept.
     * <p>
     * This method creates a window for viewing or editing a LIDR (Local Identifier for Devices Registry)
     * record, configures it with the specified settings, and adds it to the workspace. An on-close
     * handler is attached to ensure proper cleanup when the window is closed.
     *
     * @param windowView the current window view context (of type {@link ObservableViewNoOverride})
     * @param deviceConcept the {@link ConceptFacade} representing the device concept to be viewed or edited,
     *                     or null to create a new LIDR record
     * @param preferences preferences for persisting window settings, or null to use default settings
     */
    private void createLidrWindow(ObservableViewNoOverride windowView,
                                  ConceptFacade deviceConcept,
                                  KometPreferences preferences) {
        AbstractEntityChapterKlWindow lidrKlWindow = createWindow(EntityKlWindowTypes.LIDR,
                journalTopic, deviceConcept, windowView.makeOverridableViewProperties(), preferences);
        setupWorkspaceWindow(lidrKlWindow);
    }

    /**
     * Configures a window and adds it to the workspace with proper event handling.
     * <p>
     * This method performs common setup operations for all window types in the workspace:
     * <ul>
     *   <li>Configures the window's close handler to perform proper cleanup</li>
     *   <li>Adds the window to the workspace's collection of managed windows</li>
     *   <li>Triggers the window's onShown event</li>
     *   <li>For concept windows specifically, registers activity streams and configures
     *       the reasoner slideout tray integration</li>
     * </ul>
     * <p>
     * Window lifecycle is managed through this central method to ensure consistent
     * behavior across different window types (Concept, Pattern, General Editing, LIDR).
     *
     * @param chapterKlWindow the window to be configured and added to the workspace
     */
    private void setupWorkspaceWindow(ChapterKlWindow<Pane> chapterKlWindow) {
        // Calls the remove method to remove the windows that were closed by the user.
        chapterKlWindow.setOnClose(() -> {
            chapterKlWindow.delete();
            workspace.getWindows().remove(chapterKlWindow);

            if (chapterKlWindow instanceof ConceptKlWindow conceptKlWindow) {
                final PublicIdStringKey<ActivityStream> streamKey = conceptKlWindow.getDetailsActivityStreamKey();
                if (streamKey != null) {
                    activityStreams.remove(streamKey);
                }
            }
        });

        // Adding the concept window panel as a child to the workspace.
        workspace.getWindows().add(chapterKlWindow);
        chapterKlWindow.onShown();

        if (chapterKlWindow instanceof ConceptKlWindow conceptKlWindow) {
            final PublicIdStringKey<ActivityStream> streamKey = conceptKlWindow.getDetailsActivityStreamKey();
            if (streamKey != null) {
                activityStreams.add(streamKey);
            }

            // Getting the details node from the concept window
            DetailsNode detailsNode = conceptKlWindow.getDetailsNode();
            detailsNode.getDetailsViewController().onReasonerSlideoutTray(reasonerToggleConsumer);
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
            createLidrWindow(windowView, item.getValue(), null);
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

                createConceptWindow(conceptFacade);

            }
        });

        return (Pane) navigatorNode.getNode();
    }

    private void loadNavigationPanel(ObservableViewNoOverride windowView) {
        ViewProperties viewProperties = windowView.makeOverridableViewProperties();
        Config patternConceptConfig = new Config(ConceptPatternNavController.class.getResource(CONCEPT_PATTERN_NAV_FXML_URL))
                .controller(new ConceptPatternNavController(this))
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

        // create a function to handle a context menu of one option to compare concepts (launching windows)
        Function<TreeView<StringWithOptionalConceptFacade>, ContextMenu> contextMenuConsumer = (treeView) -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openNewWindows = new MenuItem("Compare Concepts");
            openNewWindows.setOnAction(actionEvent ->
                    treeView.getSelectionModel().getSelectedItems()
                            .forEach(treeItem ->
                                    treeItem.getValue().getOptionalConceptSpecification()
                                            .ifPresent((this::createConceptWindow))));
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

    public String getTitle() {
        Stage stage = (Stage) journalBorderPane.getScene().getWindow();
        return stage.getTitle();
    }

    public void close() {
        Stage stage = (Stage) journalBorderPane.getScene().getWindow();
        stage.close();
    }

    /**
     * Generates a unique journal directory name based on the journal's UUID.
     * This ensures a consistent identifier for preference storage even if the journal is renamed.
     *
     * @return A string in the format "JOURNAL_" + UUID for use as a preference folder name
     */
    public String getJournalDirName() {
        return KlWindowPreferencesUtils.getJournalDirName(journalTopic);
    }

    /**
     * Returns the unique UUID that identifies this journal instance.
     * This UUID is used for event publication and subscription within the application.
     *
     * @return The UUID that uniquely identifies this journal
     */
    public UUID getJournalTopic() {
        return journalTopic;
    }

    /**
     * Saves the current state of all windows in the journal workspace to the specified preferences.
     * <p>
     * This method iterates through all open windows in the workspace, saves their individual
     * states (including size, position, and content), and stores the list of window IDs
     * in the provided preferences node. Only windows with a valid topic UUID are saved.
     *
     * @param journalWindowPreferences The preferences node to save the window states to
     * @throws NullPointerException if journalWindowPreferences is null
     */
    public void saveWindows(KometPreferences journalWindowPreferences) {
        Objects.requireNonNull(journalWindowPreferences, "journalWindowPreferences cannot be null");

        final ImmutableList<String> windowNames = Lists.immutable.fromStream(workspace.getWindows()
                .stream().map(window -> {
                    final UUID windowTopic = window.getWindowTopic();
                    final String prefix = window.getWindowType().getPrefix();
                    return prefix + shortenUUID(windowTopic);
                }));

        // Put journal metadata in our preferences.
        final Stage stage = (Stage) journalBorderPane.getScene().getWindow();
        journalWindowPreferences.putUuid(JOURNAL_TOPIC, getJournalTopic());
        journalWindowPreferences.put(JOURNAL_TITLE, stage.getTitle());
        journalWindowPreferences.put(JOURNAL_DIR_NAME, getJournalDirName());
        journalWindowPreferences.putDouble(JOURNAL_WIDTH, stage.getWidth());
        journalWindowPreferences.putDouble(JOURNAL_HEIGHT, stage.getHeight());
        journalWindowPreferences.putDouble(JOURNAL_XPOS, stage.getX());
        journalWindowPreferences.putDouble(JOURNAL_YPOS, stage.getY());
        journalWindowPreferences.put(JOURNAL_AUTHOR, DEMO_AUTHOR);
        journalWindowPreferences.putLong(JOURNAL_LAST_EDIT, (LocalDateTime.now())
                .atZone(ZoneId.systemDefault()).toEpochSecond());

        // Putting the list of windows in our preferences.
        journalWindowPreferences.putList(WINDOW_NAMES, windowNames.castToList());
        try {
            journalWindowPreferences.flush();
            LOG.info("Saved state for {} window(s) in journal '{}'", windowNames.size(), getTitle());
        } catch (BackingStoreException ex) {
            LOG.error("Error saving window states for journal '{}'", getTitle(), ex);
        }
    }

    /**
     * Restores previously saved windows for this journal from the provided settings.
     * <p>
     * This method retrieves the list of saved window IDs from the journal preferences,
     * then attempts to recreate each window with its saved state and content.
     * Window restoration statistics are logged for diagnostic purposes.
     *
     * @param journalWindowSettings The settings object containing journal metadata
     * @throws NullPointerException if journalWindowSettings is null
     */
    public void restoreWindows(PrefX journalWindowSettings) {
        Objects.requireNonNull(journalWindowSettings, "journalWindowSettings cannot be null");
        final String journalName = journalWindowSettings.getValue(JOURNAL_TITLE);

        try {
            final KometPreferences journalPreferences = getJournalPreferences(journalTopic);
            if (journalPreferences == null) {
                LOG.info("No saved windows found for journal '{}'", journalName);
                return;
            }

            // Looping through each window in each journal
            final List<String> windowsList = journalPreferences.getList(WINDOW_NAMES);

            // Restore windows
            for (String windowId : windowsList) {
                if (!journalPreferences.nodeExists(windowId)) {
                    LOG.warn("Window preferences not found for window: {}", windowId);
                    continue;
                }
                final KometPreferences windowPreferences = journalPreferences.node(windowId);
                windowPreferences.putUuid(JOURNAL_TOPIC, getJournalTopic());
                try {
                    setupWorkspaceWindow(restoreWindow(windowPreferences));
                } catch (Exception e) {
                    LOG.error("Error restoring window: {}", windowId, e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error recreating concept windows for journal '{}'", journalName, e);
        }
    }

    /**
     * Asynchronously saves the current state of all windows in the journal workspace.
     * <p>
     * This method provides a non-blocking alternative to {@link #saveWindows(KometPreferences)}
     * by executing the save operation on a background thread pool. It delegates to the synchronous
     * version after scheduling the task.
     *
     * @param journalWindowPreferences The preferences node to save the window states to
     * @throws NullPointerException if journalWindowPreferences is null
     * @see #saveWindows(KometPreferences)
     */
    public void saveWindowsAsync(KometPreferences journalWindowPreferences) {
        CompletableFuture.runAsync(() -> {
            try {
                saveWindows(journalWindowPreferences);
            } catch (Exception e) {
                LOG.error("Error in asynchronous window save operation for journal '{}'", getTitle(), e);
                throw new CompletionException(e);
            }
        }, TinkExecutor.ioThreadPool());
    }

    /**
     * Asynchronously restores previously saved windows for this journal.
     * <p>
     * This method provides a non-blocking alternative to {@link #restoreWindows(PrefX)}
     * by executing the restoration operation on a background thread pool. It delegates
     * to the synchronous version after scheduling the task.
     *
     * @param journalWindowSettings The settings object containing journal metadata and window information
     * @throws NullPointerException if journalWindowSettings is null
     * @see #restoreWindows(PrefX)
     */
    public void restoreWindowsAsync(PrefX journalWindowSettings) {
        restoreWindows(journalWindowSettings);
        TinkExecutor.ioThreadPool();
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
     * Creates a new concept window when triggered from the menu.
     * <p>
     * This method creates a new concept window with default NID_TEXT display type
     * and no predefined concept. The user can then create or select a concept to work with.
     *
     * @param actionEvent The event triggered by selecting the menu item
     */
    @FXML
    public void newCreateConceptWindow(ActionEvent actionEvent) {
        createConceptWindow(null, NID_TEXT, null);
    }

    /**
     * Creates a new LIDR window when triggered from the menu.
     * <p>
     * This method initializes a new LIDR window in creation mode with the current
     * window view context but no predefined device concept.
     *
     * @param actionEvent The event triggered by selecting the menu item
     */
    @FXML
    public void newCreateLidrWindow(ActionEvent actionEvent) {
        createLidrWindow(windowView, null);
    }

    /**
     * Creates a new pattern window when triggered from the menu.
     * <p>
     * This method initializes a new pattern window with the current window view
     * properties but no predefined pattern, allowing the user to create or select
     * a pattern to work with.
     *
     * @param actionEvent The event triggered by selecting the menu item
     */
    @FXML
    public void newCreatePatternWindow(ActionEvent actionEvent) {
        createPatternWindow(null, windowView.makeOverridableViewProperties());
    }

    public static Toast toast() { return toast; }

}
