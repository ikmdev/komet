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
import dev.ikm.komet.amplify.details.DetailsController;
import dev.ikm.komet.amplify.details.DetailsNode;
import dev.ikm.komet.amplify.details.DetailsNodeFactory;
import dev.ikm.komet.amplify.window.WindowSupport;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.annotations.KometNodeFactoryFilter;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.navigator.graph.GraphNavigatorNode;
import dev.ikm.komet.search.SearchNode;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static dev.ikm.komet.amplify.commons.SlideOutTrayHelper.setupSlideOutTrayPane;
import static dev.ikm.komet.amplify.commons.ViewportHelper.clipChildren;

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
    private ToggleButton navigatorToggleButton;
    @FXML
    private ToggleButton searchToggleButton;

    @FXML
    private ToggleButton settingsToggleButton;

    private Pane navigatorNodePanel;
    private Pane searchNodePanel;

    private ActivityStream navigatorActivityStream;
    private ActivityStream searchActivityStream;

    private volatile boolean isSlideOutOpen = false;

    private List<PublicIdStringKey<ActivityStream>> activityStreams = new ArrayList<>();
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
     * @param windowView
     * @param navigationFactory
     */
    public void launchKometFactoryNodes(String journalName, ObservableViewNoOverride windowView, KometNodeFactory navigationFactory, KometNodeFactory searchFactory) {
        // Generate a unique activity stream for a navigator for each journal launched. Children (window Panels will subscribe to them).
        String uniqueNavigatorTopic = "navigation-%s".formatted(journalName);
        UUID uuid = UuidT5Generator.get(uniqueNavigatorTopic);
        final PublicIdStringKey<ActivityStream> navigationActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueNavigatorTopic);
        navigatorActivityStream = ActivityStreams.create(navigationActivityStreamKey);
        activityStreams.add(navigationActivityStreamKey);

        // display any factory windows onto the journal view.
        AtomicInteger staggerWindows = new AtomicInteger();
        KometNodeFactory
                .getKometNodeFactories()
                .stream()
                .filter(factoryProvider ->
                        KometNodeFactoryFilter.shouldDisplayOnJournalView(factoryProvider.type()))
                .forEach(kometNodeFactoryProvider -> {
                    // For each window or panel subscribe to the navigation activity stream. As a user clicks on the navigator the child windows display detail views.
                    KometNodeFactory factory = kometNodeFactoryProvider.get();
                    PublicIdStringKey<ActivityStreamOption> activityStreamOptionKey = ActivityStreamOption.SUBSCRIBE.keyForOption();

                    // Factory creates a panel to be added to the journal
                    KometNode kometNode = factory.create(windowView,
                            navigationActivityStreamKey, activityStreamOptionKey, AlertStreams.ROOT_ALERT_STREAM_KEY, false);

                    Pane kometNodePanel = (Pane) kometNode.getNode();
                    Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
                    Node[] draggables = new Node[draggableToolbar.size()];
                    WindowSupport windowSupport = new WindowSupport(kometNodePanel, draggableToolbar.toArray(draggables));
                    staggerWindows.getAndAdd(20);
                    kometNodePanel.setLayoutX(staggerWindows.get());
                    kometNodePanel.setLayoutY(staggerWindows.get());
                    desktopSurfacePane.getChildren().add(kometNodePanel);

                });

        loadNavigationPanel(navigationActivityStreamKey, windowView, navigationFactory);

        String uniqueSearchTopic = "search-%s".formatted(journalName);
        UUID uuidSearch = UuidT5Generator.get(uniqueSearchTopic);
        final PublicIdStringKey<ActivityStream> searchActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuidSearch.toString()), uniqueSearchTopic);
        searchActivityStream = ActivityStreams.create(searchActivityStreamKey);

        loadSearchPanel(searchActivityStreamKey, windowView, searchFactory);
        isSlideOutOpen = false;
    }

    private void loadSearchPanel(PublicIdStringKey<ActivityStream> searchActivityStreamKey,
                                 ObservableViewNoOverride windowView,
                                 KometNodeFactory searchFactory) {
        // Create search panel and publish on the search activity stream
        SearchNode searchNode = (SearchNode) searchFactory.create(windowView,
                searchActivityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        // What to do when you can double-click on a cell
        SearchPanelController controller = searchNode.getController();
        Consumer<Object> displayInDetailsView = (treeItem) -> {
            LOG.debug("tree item is a " + treeItem.getClass().getName());
            ConceptFacade conceptFacade = null;
            if (treeItem instanceof SearchPanelController.NidTextRecord nidTextRecord) {
                conceptFacade = Entity.getFast(nidTextRecord.nid());
            } else if (treeItem instanceof SemanticEntityVersion semanticEntityVersion) {
                conceptFacade = Entity.getConceptForSemantic(semanticEntityVersion.nid()).get();
            } else {
                return;
            }

            // each detail window will publish on their own activity stream.
            String uniqueDetailsTopic = "details-%s".formatted(conceptFacade.nid());
            UUID uuid = UuidT5Generator.get(uniqueDetailsTopic);
            final PublicIdStringKey<ActivityStream> detailsActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueDetailsTopic);
            ActivityStream detailActivityStream = ActivityStreams.create(detailsActivityStreamKey);
            activityStreams.add(detailsActivityStreamKey);
            KometNodeFactory detailsNodeFactory = new DetailsNodeFactory();
            DetailsNode detailsNode = (DetailsNode) detailsNodeFactory.create(windowView,
                    detailsActivityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY, true);
            Pane kometNodePanel = (Pane) detailsNode.getNode();
            DetailsController detailsController = detailsNode.getDetailsViewController();
            detailsController.updateView(windowView.makeOverridableViewProperties(), conceptFacade);
            Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
            Node[] draggables = new Node[draggableToolbar.size()];

            WindowSupport windowSupport = new WindowSupport(kometNodePanel, draggableToolbar.toArray(draggables));

            desktopSurfacePane.getChildren().add(kometNodePanel);

        };
        controller.getDoubleCLickConsumers().add(displayInDetailsView);
        searchNodePanel = (Pane) searchNode.getNode();
        setupSlideOutTrayPane(searchNodePanel, searchSlideoutTrayPane);
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
        GraphNavigatorNode navigatorNode = (GraphNavigatorNode) navigationFactory.create(windowView,
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

                String uniqueDetailsTopic = "details-%s".formatted(conceptFacade.nid());
                UUID uuid = UuidT5Generator.get(uniqueDetailsTopic);
                final PublicIdStringKey<ActivityStream> detailsActivityStreamKey = new PublicIdStringKey(PublicIds.of(uuid.toString()), uniqueDetailsTopic);
                ActivityStream detailActivityStream = ActivityStreams.create(detailsActivityStreamKey);
                activityStreams.add(detailsActivityStreamKey);
                KometNodeFactory detailsNodeFactory = new DetailsNodeFactory();
                DetailsNode detailsNode = (DetailsNode) detailsNodeFactory.create(windowView,
                        detailsActivityStreamKey, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY, true);
                Pane kometNodePanel = (Pane) detailsNode.getNode();
                DetailsController detailsController = detailsNode.getDetailsViewController();
                detailsController.updateView(windowView.makeOverridableViewProperties(), conceptFacade);
                Set<Node> draggableToolbar = kometNodePanel.lookupAll(".draggable-region");
                Node[] draggables = new Node[draggableToolbar.size()];
                WindowSupport windowSupport = new WindowSupport(kometNodePanel, draggableToolbar.toArray(draggables));
                desktopSurfacePane.getChildren().add(kometNodePanel);
            }
        });

        navigatorNodePanel = (Pane) navigatorNode.getNode();
        setupSlideOutTrayPane(navigatorNodePanel, navSlideoutTrayPane);
    }

}
