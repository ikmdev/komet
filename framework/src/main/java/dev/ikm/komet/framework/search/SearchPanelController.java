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
package dev.ikm.komet.framework.search;

import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.OptionalInt;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class SearchPanelController implements ListChangeListener<TreeItem<Object>> {
    private static final Logger LOG = LoggerFactory.getLogger(SearchPanelController.class);
    protected ReadOnlyObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty = new SimpleObjectProperty<>();
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private TextField queryString;
    @FXML
    private MenuButton navigationMenuButton;
    @FXML
    private Menu navigationCoordinateMenu;
    @FXML
    private BorderPane treeBorderPane;
    private SearchTreeView searchTreeView = new SearchTreeView();
    @FXML
    private ComboBox<RESULT_LAYOUT_OPTIONS> resultsLayoutCombo;
    private Region parentNode;
    private ViewProperties viewProperties;
    private KometPreferences nodePreferences;
    private ViewMenuModel viewMenuModel;
    private TreeItem<Object> resultsRoot = new TreeItem<>("root");

    @FXML
    void initialize() {
        assert queryString != null : "fx:id=\"queryString\" was not injected: check your FXML file 'SearchPanel.fxml'.";
        assert treeBorderPane != null : "fx:id=\"treeBorderPane\" was not injected: check your FXML file 'SearchPanel.fxml'.";

        treeBorderPane.setCenter(searchTreeView);
        searchTreeView.getSelectionModel().getSelectedItems().addListener(this);
        searchTreeView.setRoot(resultsRoot);
        resultsRoot.setExpanded(true);
        searchTreeView.setShowRoot(false);
        searchTreeView.setFixedCellSize(USE_COMPUTED_SIZE);
        searchTreeView.setMinWidth(350);
        searchTreeView.setPrefWidth(350);
        searchTreeView.setMaxWidth(350);

        searchTreeView.setCellFactory(param -> new SearchResultCell());

        resultsLayoutCombo.getItems().addAll(RESULT_LAYOUT_OPTIONS.values());
        resultsLayoutCombo.getSelectionModel().select(RESULT_LAYOUT_OPTIONS.TOP_COMPONENT_SEMANTIC_SCORE);
        resultsLayoutCombo.setOnAction(event ->{
            doSearch(event);
            clearDropDown();
        });

    }

    @FXML
    void doSearch(ActionEvent event) {
        searchTreeView.getSelectionModel().clearSelection();
        resultsRoot.getChildren().clear();
        if (queryString.getText() == null || queryString.getText().isEmpty()) {
            return;
        }
        // TODO: add to activity.
        LOG.info("start search...");
        String queryText = queryString.getText().strip();
        if (queryText.startsWith("-") && parseInt(queryText).isPresent()) {
            addComponentFromNid(queryText);
        } else if (queryText.startsWith("[") && queryText.endsWith("]")) {
            queryText = queryText.replace("[", "").replace("]", "");
            String[] nidStrings = queryText.split(",");
            for (String nidString : nidStrings) {
                addComponentFromNid(nidString.strip());
            }
        } else if (queryText.length() == 36 && UuidUtil.isUUID(queryText)) {
            UuidUtil.getUUID(queryText).ifPresent(uuid -> {
                addComponentFromNid(PrimitiveData.nid(PublicIds.of(uuid)));
            });
        } else {
            TinkExecutor.threadPool().execute(() -> {
                try {
                    TreeItem<Object> tempRoot = new TreeItem<>("Temp root");
                    ImmutableList<LatestVersionSearchResult> results = viewProperties.calculator().search(queryString.getText().strip(), 1000);
                    LOG.info("Finished search. Hits: " + results.size());
                    switch (resultsLayoutCombo.getSelectionModel().getSelectedItem()) {
                        case MATCHED_SEMANTIC_SCORE -> {
                            ImmutableList<LatestVersionSearchResult> resultsSortedOnScore = results
                                    .toSortedList((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                    .toImmutable();

                            for (LatestVersionSearchResult result : resultsSortedOnScore) {
                                tempRoot.getChildren().add(new TreeItem<>(result));
                            }
                        }
                        case MATCHED_SEMANTIC_NATURAL_ORDER -> {
                            ImmutableList<LatestVersionSearchResult> resultsSortedOnNaturalOrder = results
                                    .toSortedList((o1, o2) -> {
                                        String string1 = (String) o1.latestVersion().get().fieldValues().get(o1.fieldIndex());
                                        String string2 = (String) o2.latestVersion().get().fieldValues().get(o2.fieldIndex());
                                        return NaturalOrder.compareStrings(string1, string2);
                                    }).toImmutable();
                            for (LatestVersionSearchResult result : resultsSortedOnNaturalOrder) {
                                tempRoot.getChildren().add(new TreeItem<>(result));
                            }
                        }
                        case TOP_COMPONENT_NATURAL_ORDER -> {
                            ImmutableList<LatestVersionSearchResult> resultsTopComponentNaturalOrder = results
                                    .toSortedList((o1, o2) -> {
                                        String string1 = (String) o1.latestVersion().get().fieldValues().get(o1.fieldIndex());
                                        String string2 = (String) o2.latestVersion().get().fieldValues().get(o2.fieldIndex());
                                        return NaturalOrder.compareStrings(string1, string2);
                                    }).toImmutableList();
                            populateTempRoot(tempRoot, resultsTopComponentNaturalOrder);
                            tempRoot.getChildren().sort((o1, o2) ->
                                    NaturalOrder.compareStrings(o1.getValue().toString(),
                                            o2.getValue().toString()));
                            for (TreeItem child : tempRoot.getChildren()) {
                                child.getChildren().sort((o1, o2) -> NaturalOrder.compareStrings(o1.toString(), o2.toString()));
                            }
                        }

                        case TOP_COMPONENT_SEMANTIC_SCORE -> {
                            ImmutableList<LatestVersionSearchResult> resultsTopComponentScoreOrder = results
                                    .toSortedList((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                    .toImmutableList();
                            populateTempRoot(tempRoot, resultsTopComponentScoreOrder);
                            for (TreeItem<Object> topItem : tempRoot.getChildren()) {
                                topItem.getChildren().sort((o1, o2) ->
                                        Float.compare(((LatestVersionSearchResult) o1.getValue()).score(),
                                                ((LatestVersionSearchResult) o2.getValue()).score()));
                            }
                            tempRoot.getChildren().sort((o1, o2) -> Float.compare(((LatestVersionSearchResult) o2.getChildren().get(0).getValue()).score(),
                                    ((LatestVersionSearchResult) o1.getChildren().get(0).getValue()).score()));
                        }
                    }
                    Platform.runLater(() -> {
                        resultsRoot.getChildren().setAll(tempRoot.getChildren());
                    });
                } catch (Throwable e) {
                    AlertStreams.getRoot().dispatch(AlertObject.makeError(e.getClass().getSimpleName() + " during search", queryString.getText().strip(), e));
                }
            });
        }
    }

    private void clearDropDown() {
        MouseEvent mouseEvent = new MouseEvent(
                MouseEvent.MOUSE_PRESSED,
                0, 0, 0, 0,
                MouseButton.PRIMARY, 1,
                false, false, false, false,
                true, false, false, false,
                true, false, null
        );
        resultsLayoutCombo.fireEvent(mouseEvent);
    }


    private OptionalInt parseInt(String possibleInt) {
        try {
            return OptionalInt.of(Integer.parseInt(possibleInt));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    private void addComponentFromNid(String queryText) {
        int nid = parseInt(queryText).getAsInt();
        addComponentFromNid(nid);
    }

    private void addComponentFromNid(int nid) {
        String topText = viewProperties.nodeView().calculator().getDescriptionTextOrNid(nid);
        Latest<EntityVersion> latestTopVersion = viewProperties.nodeView().calculator().latest(nid);
        TreeItem<Object> topItem = new TreeItem<>();
        latestTopVersion.ifPresentOrElse(entityVersion ->
                        topItem.setValue(new NidTextRecord(nid, topText, entityVersion.active())),
                () -> topItem.setValue(new NidTextRecord(nid, topText, false)));
        resultsRoot.getChildren().add(topItem);
        topItem.setExpanded(true);
    }

    private void populateTempRoot(TreeItem<Object> tempRoot, ImmutableList<LatestVersionSearchResult> results) {
        MutableIntObjectMap<MutableList<LatestVersionSearchResult>> topNidMatchMap = IntObjectMaps.mutable.empty();
        for (LatestVersionSearchResult result : results) {
            topNidMatchMap.getIfAbsentPut(result.latestVersion().get().chronology().topEnclosingComponentNid(),
                    () -> Lists.mutable.empty()).add(result);
        }
        for (int topNid : topNidMatchMap.keySet().toArray()) {
            String topText = viewProperties.nodeView().calculator().getDescriptionTextOrNid(topNid);
            Latest<EntityVersion> latestTopVersion = viewProperties.nodeView().calculator().latest(topNid);
            latestTopVersion.ifPresent(entityVersion -> {
                TreeItem<Object> topItem = new TreeItem<>();
                topItem.setValue(new NidTextRecord(topNid, topText, entityVersion.active()));
                tempRoot.getChildren().add(topItem);
                topNidMatchMap.get(topNid).forEach(latestVersionSearchResult -> topItem.getChildren().add(new TreeItem<>(latestVersionSearchResult)));
                topItem.setExpanded(true);
            });
        }
    }

    public void doSearch() {
        doSearch(null);
    }

    public boolean hasResults() {
        return !resultsRoot.getChildren().isEmpty();
    }

    public List<Consumer<Object>> getDoubleCLickConsumers() {
        return searchTreeView.getDoubleCLickConsumers();
    }
    public void setItemContextMenu(Function<SearchTreeView, ContextMenu> contextMenuConsumer) {
        searchTreeView.setContextMenu(contextMenuConsumer.apply(searchTreeView));
    }

    public void setProperties(Region parentNode, ReadOnlyObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty,
                              ViewProperties viewProperties, KometPreferences nodePreferences) {
        this.activityStreamKeyProperty = activityStreamKeyProperty;
        this.parentNode = parentNode;
        this.viewProperties = viewProperties;
        this.nodePreferences = nodePreferences;
        this.navigationMenuButton.setGraphic(Icon.VIEW.makeIcon());
        this.navigationCoordinateMenu.setGraphic(Icon.COORDINATES.makeIcon());
        this.viewMenuModel = new ViewMenuModel(viewProperties, navigationMenuButton, navigationCoordinateMenu, "SearchPanelController");
        this.viewProperties.nodeView().addListener((observable, oldValue, newValue) -> {
            menuUpdate();
        });
        this.parentNode.widthProperty().addListener((observable, oldValue, newValue) -> {
            setWidth(newValue.doubleValue());
            //LOG.info("Width: " + newValue);
        });
        this.setWidth(parentNode.widthProperty().get());
    }

    private void menuUpdate() {
        if (!resultsRoot.getChildren().isEmpty()) {
            resultsRoot.getChildren().clear();
            Platform.runLater(() -> doSearch(null));
        }
    }

    private void setWidth(Double width) {
        this.searchTreeView.setMinWidth(width);
        this.searchTreeView.setPrefWidth(width);
        this.searchTreeView.setMaxWidth(width);
        Platform.runLater(() -> {
            doSearch(null);
//            VirtualFlow<IndexedCell> vf = (VirtualFlow<IndexedCell>) this.searchTreeView.getChildrenUnmodifiable().get(0);
//            int cellCount = vf.getCellCount();
//            for (int i = 0; i < cellCount; i++) {
//                SearchResultCell cell = (SearchResultCell) vf.getCell(i);
//                Platform.runLater(() -> {
//                    cell.updateItem(cell.getItem(), cell.isEmpty(), width);
//                    cell.layout();
//                });
//            }
//            Platform.runLater(() -> this.searchTreeView.layout());
        });
    }

    @Override
    public void onChanged(Change<? extends TreeItem<Object>> c) {
        if (c.getList().isEmpty()) {
            // nothing to do...
        } else {
            MutableList<EntityFacade> selectedItems = Lists.mutable.withInitialCapacity(c.getList().size());
            for (TreeItem<Object> selectedItem : c.getList()) {
                if(selectedItem !=null){
                    if (selectedItem.getValue() instanceof LatestVersionSearchResult latestVersionSearchResult) {
                        selectedItems.add(latestVersionSearchResult.latestVersion().get().chronology());
                    } else if (selectedItem.getValue() instanceof NidTextRecord nidTextRecord) {
                        selectedItems.add(EntityProxy.make(nidTextRecord.nid));
                    }
                }
            }
            dispatch(selectedItems.toImmutable());
        }
    }

    private void dispatch(ImmutableList<EntityFacade> selectedItems) {
        if (activityStreamKeyProperty != null && activityStreamKeyProperty.get() != null) {
            ActivityStreams.get(activityStreamKeyProperty.get()).dispatch(selectedItems);
        }
    }

    public ComboBox<RESULT_LAYOUT_OPTIONS> getResultsLayoutCombo() {
        return resultsLayoutCombo;
    }

    public String getQueryString() {
        return queryString.getText();
    }

    public void setQueryString(String queryString) {
        this.queryString.setText(queryString);
    }

    public enum RESULT_LAYOUT_OPTIONS {
        MATCHED_SEMANTIC_NATURAL_ORDER("Matched semantic with lexicographic order"),
        MATCHED_SEMANTIC_SCORE("Matched semantic with score order"),
        TOP_COMPONENT_NATURAL_ORDER("Top component with lexicographic order"),
        TOP_COMPONENT_SEMANTIC_SCORE("Top component with score order");

        String menuText;

        RESULT_LAYOUT_OPTIONS(String menuText) {
            this.menuText = menuText;
        }

        @Override
        public String toString() {
            return menuText;
        }
    }

    public record NidTextRecord(int nid, String text, boolean active) {
    }
}
