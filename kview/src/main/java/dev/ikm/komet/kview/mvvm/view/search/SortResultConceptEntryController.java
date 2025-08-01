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

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
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
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;


public class SortResultConceptEntryController extends AbstractBasicController {

    private static final int LIST_VIEW_CELL_SIZE = 40;

    @FXML
    private Pane searchEntryContainer;

    @FXML
    private ImageView identicon;

    @FXML
    private Text componentText;

    @FXML
    private HBox retiredHBox;

    @FXML
    private Label retiredLabel;

    @FXML
    private ListView<LatestVersionSearchResult> descriptionsListView;

    @FXML
    private Button showContextButton;

    @FXML
    private ContextMenu contextMenu;

    private boolean retired;

    private EvtBus eventBus;

    private Entity<EntityVersion> entity;

    private ObservableViewNoOverride windowView;

    @InjectViewModel
    private SimpleViewModel searchEntryViewModel;

    @FXML
    @Override
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        showContextButton.setVisible(false);
        contextMenu.setHideOnEscape(true);
        searchEntryContainer.setOnMouseEntered(mouseEvent -> showContextButton.setVisible(entity instanceof ConceptEntity));
        searchEntryContainer.setOnMouseExited(mouseEvent -> {
            if (!contextMenu.isShowing()) {
                showContextButton.setVisible(false);
            }
        });
        showContextButton.setOnAction(event -> contextMenu.show(showContextButton, Side.BOTTOM, 0, 0));

        searchEntryContainer.setOnMouseClicked(mouseEvent -> {
            // double left click creates the concept window
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    if (entity instanceof ConceptEntity conceptEntity) {
                        eventBus.publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new MakeConceptWindowEvent(this, MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT,
                                conceptEntity));
                    } else if (entity instanceof PatternEntity patternEntity) {
                        eventBus.publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new MakePatternWindowEvent(this, MakePatternWindowEvent.OPEN_PATTERN, patternEntity, getViewProperties()));
                    }
                }
            }
            // right click shows the context menu
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY) && entity instanceof ConceptEntity) {
                contextMenu.show(showContextButton, Side.BOTTOM, 0, 0);
            }
        });

        descriptionsListView.setFixedCellSize(LIST_VIEW_CELL_SIZE);
        descriptionsListView.getItems().addListener((ListChangeListener<? super LatestVersionSearchResult>) change -> updateListViewPrefHeight());
        updateListViewPrefHeight();

        descriptionsListView.setCellFactory(param -> new DescriptionSemanticListCell());
    }

    @FXML
    private void populateConcept(ActionEvent actionEvent) {
        if (entity instanceof ConceptEntity conceptEntity) {
            eventBus.publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new MakeConceptWindowEvent(this,
                    MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity));
        }
    }

    @FXML
    private void openInConceptNavigator(ActionEvent actionEvent) {
        if (entity instanceof ConceptEntity conceptEntity) {
            eventBus.publish(searchEntryViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new ShowNavigationalPanelEvent(this, ShowNavigationalPanelEvent.SHOW_CONCEPT_NAVIGATIONAL_FROM_CONCEPT, conceptEntity));
        }
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

    public void setComponentText(String topText) {
        this.componentText.setText(topText);
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
                });
                updateTextFlow(textFlow, item.highlightedString());

                setGraphic(cellContainer);
            }
        }

        private void updateTextFlow(TextFlow textFlow, String highlightedString) {
            textFlow.getChildren().clear();
            String[] words = highlightedString.split(" ");
            for (String word : words) {
                Text text = new Text();
                StackPane textContainer = new StackPane(text);

                if (word.contains("<B>")) {
                    text.setText(word.replaceAll("<B>", "")
                            .replaceAll("</B>", "")
                            .replaceAll("\\s+", " "));

                    textContainer.getStyleClass().add("highlight");
                } else {
                    text.setText(word);
                }

                textContainer.getStyleClass().add("word-container");

                textFlow.getChildren().add(textContainer);
            }
        }
    }
}
