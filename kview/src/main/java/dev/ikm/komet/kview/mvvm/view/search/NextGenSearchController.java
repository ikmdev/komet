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

import static dev.ikm.komet.framework.events.FrameworkTopics.SEARCH_SORT_TOPIC;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_COMPONENT;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_COMPONENT_ALPHA;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_SEMANTIC;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_SEMANTIC_ALPHA;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.kview.events.SearchSortOptionEvent;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.controlsfx.control.PopOver;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class NextGenSearchController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(NextGenSearchController.class);

    public static final String SORT_OPTIONS_FXML = "sort-options.fxml";

    public static final String SORT_CONCEPT_RESULT_CONCEPT_FXML = "search-result-concept-entry.fxml";

    public static final String SORT_SEMANTIC_RESULT_CONCEPT_FXML = "search-result-semantic-entry.fxml";

    public static final String BUTTON_TEXT_TOP_COMPONENT = "SORT BY: TOP COMPONENT";

    public static final String BUTTON_TEXT_TOP_COMPONENT_ALPHA = "SORT BY: TOP COMPONENT (ALPHABETICAL)";

    public static final String BUTTON_TEXT_DESCRIPTION_SEMANTIC = "SORT BY: MATCHED DESCRIPTION SEMANTIC";

    public static final String BUTTON_TEXT_DESCRIPTION_SEMANTIC_ALPHA = "SORT BY: MATCHED DESCRIPTION SEMANTIC (ALPHABETICAL)";

    public static final int MAX_RESULT_SIZE = 1000;

    @FXML
    private VBox resultsVBox;

    @FXML
    private Button sortByButton;

    private PopOver sortOptions;

    private SortOptionsController sortOptionsController;

    private EvtBus eventBus;



    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        clearView();

        setUpSearchOptionsPopOver();

        Subscriber<SearchSortOptionEvent> searchSortOptionListener = (evt -> {
            if (evt.getEventType() == SORT_BY_COMPONENT) {
                sortByButton.setText(BUTTON_TEXT_TOP_COMPONENT);
            } else if (evt.getEventType() == SORT_BY_COMPONENT_ALPHA) {
                sortByButton.setText(BUTTON_TEXT_TOP_COMPONENT_ALPHA);
            } else if (evt.getEventType() == SORT_BY_SEMANTIC) {
                sortByButton.setText(BUTTON_TEXT_DESCRIPTION_SEMANTIC);
            } else if (evt.getEventType() == SORT_BY_SEMANTIC_ALPHA) {
                sortByButton.setText(BUTTON_TEXT_DESCRIPTION_SEMANTIC_ALPHA);
            }
            sortOptions.hide();
        });
        eventBus.subscribe(SEARCH_SORT_TOPIC, SearchSortOptionEvent.class, searchSortOptionListener);
    }


    private void setUpSearchOptionsPopOver() {
        JFXNode<Pane, SortOptionsController> sortJFXNode = FXMLMvvmLoader
                .make(SortOptionsController.class.getResource(SORT_OPTIONS_FXML));

        Pane sortOptionsPane = sortJFXNode.node();
        PopOver popOver = new PopOver(sortOptionsPane);
        SortOptionsController sortOptionsController = sortJFXNode.controller();
        popOver.setArrowSize(0);
        popOver.setDetachable(false);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.setOnHidden(windowEvent -> {
            //TODO save vm?, get values and publish to the eventbus?
        });

        sortOptions = popOver;
        this.sortOptionsController = sortOptionsController;
    }


    @FXML
    private void showSearchOptions(ActionEvent event) {
        Button button = (Button) event.getSource();
        sortOptions.show(button, -12);
    }

    @FXML
    private void doSearch(ActionEvent actionEvent) {
        clearView();

        String queryText = ((TextField) actionEvent.getSource()).getText().strip();

        try {
            List<LatestVersionSearchResult> results = getViewProperties().calculator().search(queryText, MAX_RESULT_SIZE).toList();
            LOG.info(String.valueOf(results.size()));

            switch (sortByButton.getText()) {
                case BUTTON_TEXT_TOP_COMPONENT -> {
                    // sort by top component score order
                    results.sort((o1, o2) -> Float.compare(o2.score(), o1.score()));

                    MutableIntObjectMap<MutableList<LatestVersionSearchResult>> topNidMatchMap = IntObjectMaps.mutable.empty();
                    for (LatestVersionSearchResult result : results) {
                        topNidMatchMap.getIfAbsentPut(result.latestVersion().get().chronology().topEnclosingComponentNid(),
                                () -> Lists.mutable.empty()).add(result);
                    }
                    // topItems is similar to tempRoot
                    Map<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>> topItems = new HashMap<>();
                    for (int topNid : topNidMatchMap.keySet().toArray()) {
                        String topText = getViewProperties().nodeView().calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(topNid);
                        Latest<EntityVersion> latestTopVersion = getViewProperties().nodeView().calculator().latest(topNid);
                        latestTopVersion.ifPresent(entityVersion -> {
                            topItems.put(new SearchPanelController.NidTextRecord(topNid, topText, entityVersion.active()),
                                    topNidMatchMap.get(topNid));
                        });
                    }
                    // sort topItems by the sort o
                    topItems.forEach((k, v) -> Collections.sort(v, (o1,o2) -> Float.compare(o1.score(), o2.score())));

                    List<Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>>> myList = new ArrayList<>(topItems.entrySet());


                    Collections.sort(myList, (m1, m2) -> Float.compare(m2.getValue().get(0).score(), m1.getValue().get(0).score()));

                    for (Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>> entry: myList) {
                        resultsVBox.getChildren().addAll(buildResultEntry(entry.getKey(), entry.getValue()));
                    }

                }
                //FIXME implement the other searches
                case BUTTON_TEXT_TOP_COMPONENT_ALPHA -> {
                    LOG.info(queryText);
                    LOG.info(String.valueOf(resultsVBox.getChildren().size()));
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Node buildResultEntry(SearchPanelController.NidTextRecord nidTextRecord, List<LatestVersionSearchResult> latestVersionSearchResults) {
        int topNid = nidTextRecord.nid();
        String topText = getViewProperties().nodeView().calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(topNid); // top text I assume is the title text
        Latest<EntityVersion> latestTopVersion = getViewProperties().nodeView().calculator().latest(topNid);

        AtomicReference<Node> entry = new AtomicReference<>();
        latestTopVersion.ifPresent(entityVersion -> {

            JFXNode<Pane, SortResultConceptEntryController> searchConceptEntryJFXNode = FXMLMvvmLoader
                    .make(SortResultConceptEntryController.class.getResource(SORT_CONCEPT_RESULT_CONCEPT_FXML));
            entry.set(searchConceptEntryJFXNode.node());
            SortResultConceptEntryController controller = searchConceptEntryJFXNode.controller();

            controller.setIdenticon(Identicon.generateIdenticonImage(entityVersion.publicId()));

            controller.setComponentText(topText);

            // add the custom descriptions
            controller.getDescriptionsVBox().getChildren().clear();
            latestVersionSearchResults.forEach(latestVersionSearchResult -> {
                Label descrLabel = new Label(formatHighlightedString(latestVersionSearchResult.highlightedString()));
                descrLabel.getStyleClass().add("search-entry-description-label");
                controller.getDescriptionsVBox().getChildren().add(descrLabel);
                controller.getDescriptionsVBox().getStyleClass().add("search-entry-descr-container");
                VBox.setMargin(descrLabel, new Insets(0, 7, 7, 7));
                descrLabel.setPadding(new Insets(8));
            });

            entry.get().setUserData(topNid);

            if (entityVersion.active()) {
                controller.getRetiredHBox().getChildren().remove(controller.getRetiredLabel());
            }
            controller.setRetired(!entityVersion.active());
            VBox.setMargin(entry.get(), new Insets(8, 0, 8, 0));

        });

        return entry.get();
    }


    private String formatHighlightedString(String highlightedString) {
        return highlightedString.replaceAll("<B>", "")
                .replaceAll("</B>", "")
                .replaceAll("\\s+", " ");
    }


    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        resultsVBox.getChildren().clear();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public <T extends ViewModel> T getViewModel() {
        return null;
    }
}

