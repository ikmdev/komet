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
package dev.ikm.komet.amplify.journal;

import dev.ikm.komet.amplify.commons.SlideOutTrayHelper;
import dev.ikm.komet.amplify.details.ConceptPreference;
import dev.ikm.komet.amplify.details.DetailsNode;
import dev.ikm.komet.amplify.details.DetailsNodeFactory;
import dev.ikm.komet.amplify.window.WindowSupport;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.navigator.graph.GraphNavigatorNode;
import dev.ikm.komet.preferences.ConceptWindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.preferences.NidTextEnum;
import dev.ikm.komet.reasoner.ReasonerResultsController;
import dev.ikm.komet.reasoner.ReasonerResultsNode;
import dev.ikm.komet.reasoner.StringWithOptionalConceptFacade;
import dev.ikm.komet.search.SearchNode;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.amplify.commons.SlideOutTrayHelper.setupSlideOutTrayPane;
import static dev.ikm.komet.amplify.commons.ViewportHelper.clipChildren;
import static dev.ikm.komet.amplify.details.ConceptPreferenceUtil.findElement;
import static dev.ikm.komet.preferences.ConceptWindowPreferences.*;
import static dev.ikm.komet.preferences.ConceptWindowSettings.*;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNAL_WINDOW;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;
import static dev.ikm.komet.preferences.JournalWindowSettings.CONCEPT_NAMES;
import static dev.ikm.komet.preferences.NidTextEnum.NID_TEXT;
import static dev.ikm.komet.preferences.NidTextEnum.SEMANTIC_ENTITY;

/**
 * This controller is responsible for updating the Amplify journal window by loading a navigation panel
 * and a concept details panel. Activity streams are dynamically created to be used in context to a journal instance.
 * This makes the navigator (published data) able to update windows downstream such as the Concept Details Panel
 * This is associated with the FXML file amplify-journal.fxml.
 * @see dev.ikm.komet.amplify.details.DetailsNode
 * @see JournalViewFactory
 */
public class JournalController {
    private static final Logger LOG = LoggerFactory.getLogger(JournalController.class);

    /**
     * Top level journal root pane for Scene.
     */
    @FXML
    private BorderPane journalBorderPane;

    @FXML
    private Pane desktopSurfacePane;

    @FXML
    private HBox chapterHeaderbarHBox;

    @FXML
    private HBox projectBarHBox;
    @FXML
    private ToggleGroup sidebarToggleGroup;

    @FXML
    private Pane navSlideoutTrayPane;

    @FXML
    private Pane searchSlideoutTrayPane;

    @FXML
    private Pane reasonerSlideoutTrayPane;

    @FXML
    private ToggleButton reasonerToggleButton;

    @FXML
    private ToggleButton navigatorToggleButton;
    @FXML
    private ToggleButton searchToggleButton;

    @FXML
    private ToggleButton settingsToggleButton;

    private Pane navigatorNodePanel;
    private Pane searchNodePanel;
    private Pane reasonerNodePanel;

    private ActivityStream navigatorActivityStream;
    private ActivityStream searchActivityStream;
    private ActivityStream reasonerActivityStream;

    private volatile boolean isSlideOutOpen = false;

    private List<PublicIdStringKey<ActivityStream>> activityStreams = new ArrayList<>();

    private static Consumer<ToggleButton> reasonerToggleConsumer;

    private GraphNavigatorNode navigatorNode;
    private List<ConceptPreference> conceptWindows = new ArrayList<>();

    /**
     * Called after JavaFX FXML DI has occurred. Any annotated items above should be valid.
     */
    @FXML
    public void initialize() {
        // According to the JavaFX docs an ordinary Pane does not clip region.
        clipChildren(desktopSurfacePane, 0);

        // When user clicks on sidebar tray's toggle buttons.
        sidebarToggleGroup.selectedToggleProperty().addListener((observableValue, oldValue, newValue) -> {
            // slide in previous panel
            slideIn(oldValue);

            // slide out new panel selected
            slideOut(newValue);
        });

        reasonerToggleConsumer = createReasonerToggleConsumer();
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
        LOG.info("Amplify Concept Details Viewer Journal is shutting down...");
        activityStreams.forEach( activityStreamKey -> ActivityStreams.delete(activityStreamKey));
    }

    /**
     * Iterate through all available KometNodeFactories that will be displayed on the journal.
     * Note: Each journal will have a unique navigation activity stream.
     * @param windowView The window view properties
     * @param navigationFactory A factory to create navigation view.
     * @param reasonerFactory A factory to create reasoner results view.
     */
    public void launchKometFactoryNodes(String journalName,
                                        ObservableViewNoOverride windowView,
                                        KometNodeFactory navigationFactory,
                                        KometNodeFactory searchFactory,
                                        KometNodeFactory reasonerFactory) {
        // Generate a unique activity stream for a navigator for each journal launched. Children (window Panels will subscribe to them).
        String uniqueNavigatorTopic = "navigation-%s".formatted(journalName);
        UUID uuid = UuidT5Generator.get(uniqueNavigatorTopic);
        final PublicIdStringKey<ActivityStream> navigationActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueNavigatorTopic);
        navigatorActivityStream = ActivityStreams.create(navigationActivityStreamKey);
        activityStreams.add(navigationActivityStreamKey);

        loadNavigationPanel(navigationActivityStreamKey, windowView, navigationFactory);

        String uniqueSearchTopic = "search-%s".formatted(journalName);
        UUID uuidSearch = UuidT5Generator.get(uniqueSearchTopic);
        final PublicIdStringKey<ActivityStream> searchActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuidSearch.toString()), uniqueSearchTopic);
        searchActivityStream = ActivityStreams.create(searchActivityStreamKey);

        loadSearchPanel(searchActivityStreamKey, windowView, searchFactory);
        loadReasonerPanel(ActivityStreams.REASONER, windowView, reasonerFactory);
        isSlideOutOpen = false;
    }

    public GraphNavigatorNode getNavigatorNode() {
        return navigatorNode;
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
    private void makeConceptWindow(ObservableViewNoOverride windowView, ConceptFacade conceptFacade, NidTextEnum nidTextEnum, Map<ConceptWindowSettings, Object> conceptWindowSettingsMap) {

        // each detail window will publish on their own activity stream.
        String uniqueDetailsTopic = "details-%s".formatted(conceptFacade.nid());
        UUID uuid = UuidT5Generator.get(uniqueDetailsTopic);
        final PublicIdStringKey<ActivityStream> detailsActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueDetailsTopic);
        ActivityStream detailActivityStream = ActivityStreams.create(detailsActivityStreamKey);
        activityStreams.add(detailsActivityStreamKey);
        KometNodeFactory detailsNodeFactory = new DetailsNodeFactory();
        DetailsNode detailsNode = (DetailsNode) detailsNodeFactory.create(windowView,
                detailsActivityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY, true);
        detailsNode.getDetailsViewController().onReasonerSlideoutTray(reasonerToggleConsumer);

        //Getting the concept window pane
        Pane kometNodePanel = (Pane) detailsNode.getNode();
        //Appling the CSS from draggable-region to the panel (makes it movable/sizable).
        Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
        Node[] draggables = new Node[draggableToolbar.size()];

        WindowSupport windowSupport = new WindowSupport(kometNodePanel, draggableToolbar.toArray(draggables));
        //Adding the concept window panel as a child to the desktop pane.
        desktopSurfacePane.getChildren().add(kometNodePanel);

        // This will refresh the Concept details, history, timeline
        detailsNode.handleActivity(Lists.immutable.of(conceptFacade));
        conceptWindows.add(new ConceptPreference(nidTextEnum, conceptFacade.nid(),
                kometNodePanel));

        //Calls the remove method to remove and concepts that were closed by the user.
        detailsNode.getDetailsViewController().setOnCloseConceptWindow(windowEvent -> {
            removeConceptSetting(nidTextEnum, conceptFacade.nid(), detailsNode);
        });
        //Checking if map is null (if yes not values are set) if not null, setting position of concept windows.
        if (conceptWindowSettingsMap != null) {
            kometNodePanel.setPrefHeight((Double)conceptWindowSettingsMap.get(CONCEPT_HEIGHT));
            kometNodePanel.setPrefWidth((Double)conceptWindowSettingsMap.get(CONCEPT_WIDTH));
            kometNodePanel.setLayoutX((Double)conceptWindowSettingsMap.get(CONCEPT_XPOS));
            kometNodePanel.setLayoutY((Double)conceptWindowSettingsMap.get(CONCEPT_YPOS));
        }
    }

    private void removeConceptSetting(NidTextEnum nidTextEnum, Integer nid, DetailsNode detailsNode) {
        conceptWindows.remove(findElement(nidTextEnum, nid, conceptWindows));
        detailsNode.close();
    }

    /**
     * Loads up a navigation panel into the sidebar area.
     * @param navigationActivityStreamKey The newly generated navigation activity stream for this Journal window and all children.
     * @param windowView Any window view information
     * @param navigationFactory The navigation factory to create the navigation panel to be used in the sidebar.
     */
    private void loadNavigationPanel(PublicIdStringKey<ActivityStream> navigationActivityStreamKey,
                                     ObservableViewNoOverride windowView,
                                     KometNodeFactory navigationFactory) {

        // Create navigator panel and publish on the navigation activity stream
        navigatorNode = (GraphNavigatorNode) navigationFactory.create(windowView,
                navigationActivityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        // What to do when you can double-click on a cell
        TreeView<ConceptFacade> treeView = navigatorNode.getController().getTreeView();

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

        navigatorNodePanel = (Pane) navigatorNode.getNode();
        setupSlideOutTrayPane(navigatorNodePanel, navSlideoutTrayPane);
    }
    private  void loadReasonerPanel(PublicIdStringKey<ActivityStream> activityStreamKey,
                                    ObservableViewNoOverride windowView,
                                    KometNodeFactory nodeFactory) {
        // Create reasoner panel and publish on the search activity stream
        ReasonerResultsNode reasonerNode = (ReasonerResultsNode) nodeFactory.create(windowView,
                activityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        reasonerNodePanel = (Pane) reasonerNode.getNode();
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
                        detailsActivityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY, true);
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

                WindowSupport windowSupport = new WindowSupport(kometNodePanel, draggableToolbar.toArray(draggables));
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

    public void saveConceptWindowPreferences(KometPreferences journalSubWindowPreferences){
        List<String> conceptFolderNames = new ArrayList<>();
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();

        //Looping through each concept window to save position and size to preferences.
        for (ConceptPreference conceptPreference : conceptWindows) {
            String journalSubWindowPrefFolder = CONCEPT_FOLDER_PREFIX + UUID.randomUUID();
            conceptFolderNames.add(journalSubWindowPrefFolder);

            //Applying the preferences naming convention to the files.
            KometPreferences conceptPreferences = appPreferences.node(JOURNAL_WINDOW +
                    File.separator + journalSubWindowPrefFolder);
            conceptPreferences.put(NID_TYPE, conceptPreference.getNidType().toString());
            conceptPreferences.putInt(NID_VALUE, conceptPreference.getNid());
            conceptPreferences.putDouble(CONCEPT_HEIGHT, conceptPreference.getConceptPane().getPrefHeight());
            conceptPreferences.putDouble(CONCEPT_WIDTH, conceptPreference.getConceptPane().getPrefWidth());
            conceptPreferences.putDouble(CONCEPT_XPOS, conceptPreference.getConceptPane().getBoundsInParent().getMinX());
            conceptPreferences.putDouble(CONCEPT_YPOS, conceptPreference.getConceptPane().getBoundsInParent().getMinY());
        }
        //Putting the list of concepts in our preferences.
        journalSubWindowPreferences.putList(CONCEPT_NAMES, conceptFolderNames);
        try {
            journalSubWindowPreferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void recreateConceptWindows(List<String> conceptList) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        //Looping through each concept in each journal.
        for(String conceptFolder: conceptList){
            KometPreferences conceptPreferences = appPreferences.node(JOURNAL_WINDOW +
                    File.separator + conceptFolder);
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
            conceptWindowSettingsMap.put(CONCEPT_HEIGHT,conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_HEIGHT), DEFAULT_CONCEPT_HEIGHT));
            conceptWindowSettingsMap.put(CONCEPT_WIDTH, conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_WIDTH), DEFAULT_CONCEPT_HEIGHT));
            conceptWindowSettingsMap.put(CONCEPT_XPOS, conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_XPOS), DEFAULT_CONCEPT_XPOS));
            conceptWindowSettingsMap.put(CONCEPT_YPOS, conceptPreferences.getDouble(conceptPreferences.enumToGeneralKey(CONCEPT_YPOS), DEFAULT_CONCEPT_YPOS));

            //Calling make concept window to finish.
            makeConceptWindow(window, conceptFacade, nidTextEnum, conceptWindowSettingsMap);
        }
    }
    }
