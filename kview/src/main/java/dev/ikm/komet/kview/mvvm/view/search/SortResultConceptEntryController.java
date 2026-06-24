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
package dev.ikm.komet.kview.mvvm.view.search;

import static dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController.getDragAndDropType;
import static dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController.setUpDraggable;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.VIEW_PROPERTIES;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.search.HighlightedSegments;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.events.MakeKLWindowEvent;
import dev.ikm.komet.kview.events.ShowNavigationalPanelEvent;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.provider.grpc.GrpcSearchService;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.List;
import java.util.UUID;

public class SortResultConceptEntryController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(SortResultConceptEntryController.class);
    private static final int LIST_VIEW_CELL_SIZE = 40;

    @FXML
    private Pane searchEntryContainer;

    @FXML
    private ImageView identicon;

    @FXML
    private TextFlow componentTextFlow;

    @FXML
    private HBox retiredHBox;

    @FXML
    private Label retiredLabel;

    @FXML
    private ListView<LatestVersionSearchResult> descriptionsListView;

    @FXML
    private Node dragIndicator;

    private boolean retired;

    private EvtBus eventBus;

    private Entity<EntityVersion> entity;

    /** Public UUIDs carried from a gRPC search result when no local entity is available. */
    private List<UUID> grpcPublicIds;

    private ObservableViewNoOverride windowView;

    @InjectViewModel
    private SimpleViewModel searchEntryViewModel;

    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        dragIndicator.setVisible(false);
        searchEntryContainer.setOnMouseEntered(mouseEvent -> dragIndicator.setVisible(entity instanceof ConceptEntity));
        searchEntryContainer.setOnMouseExited(mouseEvent -> dragIndicator.setVisible(false));

        searchEntryContainer.setOnMouseClicked(mouseEvent -> {
            // double left click creates the concept window
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    if (entity instanceof ConceptEntity conceptEntity) {
                        eventBus.publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new MakeConceptWindowEvent(this, MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT,
                                conceptEntity));
                    } else if (entity instanceof PatternEntity patternEntity) {
                        eventBus.publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new MakePatternWindowEvent(this, MakePatternWindowEvent.OPEN_PATTERN, patternEntity, getViewProperties()));
                    } else if (grpcPublicIds != null && !grpcPublicIds.isEmpty()) {
                        // gRPC mode: fetch full entity graph from server, load into ephemeral store,
                        // then open the concept window as normal.
                        openGrpcConcept();
                    }
                }
            }
        });

        descriptionsListView.setFixedCellSize(LIST_VIEW_CELL_SIZE);
        descriptionsListView.getItems().addListener((ListChangeListener<? super LatestVersionSearchResult>) change -> updateListViewPrefHeight());
        updateListViewPrefHeight();

        descriptionsListView.setCellFactory(param -> new DescriptionSemanticListCell());
    }

    public void populateConcept(ActionEvent actionEvent) {
        if (entity instanceof ConceptEntity conceptEntity) {
            eventBus.publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new MakeConceptWindowEvent(this,
                    MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity));
        }
    }

    public void openInConceptNavigator(ActionEvent actionEvent) {
        if (entity instanceof ConceptEntity conceptEntity) {
            eventBus.publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new ShowNavigationalPanelEvent(this, ShowNavigationalPanelEvent.SHOW_CONCEPT_NAVIGATIONAL_FROM_CONCEPT, conceptEntity));
        }
    }

    public void openAsKLWindow(ActionEvent actionEvent, String windowTitle) {
        UUID journalTopic = searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC);
        eventBus.publish(journalTopic,
                new MakeKLWindowEvent(this, MakeKLWindowEvent.OPEN_ENTITY_FROM_ENTITY, entity, windowTitle));
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public HBox getRetiredHBox() {
        return this.retiredHBox;
    }

    public Label getRetiredLabel() {
        return this.retiredLabel;
    }

    public void setIdenticon(Image identiconImage) {
        this.identicon.setImage(identiconImage);
    }

    /**
     * Render the concept's title into the cell header. The input may be a
     * Lucene-formatted highlighted snippet from
     * {@link dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator#highlight(String, String)}
     * — words wrapped in {@code <B>...</B>} get the {@code highlight} CSS class
     * on their containing {@code StackPane}, matching the per-word visual
     * treatment of the description-semantic rows below the header.
     *
     * @param highlightedTopText the marked-up title (or plain text if no terms
     *                           matched, or null/empty)
     */
    public void setComponentText(String highlightedTopText) {
        HighlightedSegments.renderHighlightedInto(componentTextFlow, highlightedTopText);
    }

    public ObservableList<LatestVersionSearchResult> getDescriptionListViewItems() { return descriptionsListView.getItems(); }

    @Override
    public void updateView() {
    }

    @Override
    public void clearView() {
    }

    @Override
    public void cleanup() {
    }

    public void setData(Entity<EntityVersion> entity) {
        this.entity = entity;
    }

    /**
     * Sets the public UUIDs from a gRPC search result. Used when no local entity is
     * available (gRPC mode) so that double-click can fetch the full concept from the server.
     */
    public void setGrpcPublicIds(List<UUID> publicIds) {
        this.grpcPublicIds = publicIds;
    }

    /**
     * Background-fetches the concept entity graph via gRPC, loads it into the local
     * ephemeral entity store, then fires {@link MakeConceptWindowEvent} on the UI thread.
     */
    private void openGrpcConcept() {
        List<UUID> ids = List.copyOf(grpcPublicIds);
        UUID journalTopic = searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC);
        Thread.ofVirtual().start(() -> {
            try {
                int nid = GrpcSearchService.get().loadConceptWithSemantics(ids);
                Entity<?> loaded = Entity.getFast(nid);
                if (loaded instanceof ConceptEntity loadedConcept) {
                    Platform.runLater(() ->
                        eventBus.publish(journalTopic,
                            new MakeConceptWindowEvent(this,
                                MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT,
                                loadedConcept)));
                } else {
                    LOG.warn("Loaded entity for {} is not a ConceptEntity: {}", ids, loaded);
                }
            } catch (Exception ex) {
                LOG.warn("Failed to load concept details from gRPC for {}: {}", ids, ex.getMessage());
            }
        });
    }

    public void setWindowView(ObservableViewNoOverride windowView) {
        this.windowView = windowView;
    }

    @Override
    public ViewProperties getViewProperties() {
        return searchEntryViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @Override
    public <T extends ViewModel> T getViewModel() {
        return null;
    }

    private void updateListViewPrefHeight() {
        int itemsNumber = descriptionsListView.getItems().size();
        /* adding a number to LIST_VIEW_CELL_SIZE to account for padding, etc */
        double newPrefHeight = itemsNumber * (LIST_VIEW_CELL_SIZE + 3);
        double maxHeight = descriptionsListView.getMaxHeight();

        descriptionsListView.setPrefHeight(Math.min(newPrefHeight, maxHeight));
    }

    /***************************************************************************
     *                                                                         *
     * Support Classes                                                         *
     *                                                                         *
     **************************************************************************/

    class DescriptionSemanticListCell extends ListCell<LatestVersionSearchResult> {
        private HBox cellContainer = new HBox();
        private TextFlow textFlow = new TextFlow();
        private ImageView identicon = new ImageView();
        private int currentNid = -1;

        @SuppressWarnings("removal")
        public DescriptionSemanticListCell() {
            cellContainer.getChildren().addAll(
                    identicon,
                    textFlow
            );

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            identicon.setFitHeight(16);
            identicon.setFitWidth(16);

            cellContainer.setOnMouseClicked(mouseEvent -> {
                if (currentNid == -1) {
                    return;
                }

                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        EntityFacade semanticChronology = EntityService.get().getEntity(currentNid).get();
                        EvtBusFactory.getDefaultEvtBus().publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                                new MakeGenEditingWindowEvent(this,
                                        MakeGenEditingWindowEvent.OPEN_GEN_EDIT, semanticChronology, searchEntryViewModel.getPropertyValue(VIEW_PROPERTIES)));
                    }
                }
            });

            cellContainer.getStyleClass().add("cell-container");
            textFlow.getStyleClass().add("text-container");
        }

        @Override
        protected void updateItem(LatestVersionSearchResult item, boolean empty) {
            if (item == null || empty) {
                setGraphic(null);
                currentNid = -1;
            } else {
                item.latestVersion().ifPresent(semanticEntityVersion -> {
                    identicon.setImage(Identicon.generateIdenticonImage(semanticEntityVersion.publicId()));
                    currentNid = semanticEntityVersion.nid();
                    setUpDraggable(cellContainer, semanticEntityVersion.entity(), getDragAndDropType(semanticEntityVersion.entity()));
                });
                HighlightedSegments.renderHighlightedInto(textFlow, item.highlightedString());
                setGraphic(cellContainer);
            }
        }
    }
}
