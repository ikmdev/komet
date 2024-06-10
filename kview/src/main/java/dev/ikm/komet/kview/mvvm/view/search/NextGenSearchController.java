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
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.kview.events.SearchSortOptionEvent;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class NextGenSearchController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(NextGenSearchController.class);

    public static final String SORT_OPTIONS_FXML = "sort-options.fxml";

    public static final String SORT_CONCEPT_RESULT_CONCEPT_FXML = "search-result-concept-entry.fxml";

    public static final String SORT_SEMANTIC_RESULT_CONCEPT_FXML = "search-result-semantic-entry.fxml";

    public static final String BUTTON_TEXT_TOP_COMPONENT = "SORT BY: TOP COMPONENT";

    public static final String BUTTON_TEXT_TOP_COMPONENT_ALPHA = "SORT BY: TOP COMPONENT (ALPHABETICAL)";

    public static final String BUTTON_TEXT_DESCRIPTION_SEMANTIC = "SORT BY: MATCHED DESCRIPTION SEMANTIC";

    public static final String BUTTON_TEXT_DESCRIPTION_SEMANTIC_ALPHA = "SORT BY: MATCHED DESCRIPTION SEMANTIC (ALPHABETICAL)";

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

        populateComponentSearchResults();
        setUpSearchOptionsPopOver(); // FIXME set a component radio as the default selected

        Subscriber<SearchSortOptionEvent> searchSortOptionListener = (evt -> {
            if (evt.getEventType() == SORT_BY_COMPONENT) {
                populateComponentSearchResults();
                sortByButton.setText(BUTTON_TEXT_TOP_COMPONENT);
            } else if (evt.getEventType() == SORT_BY_COMPONENT_ALPHA) {
                populateComponentSearchResults();
                sortByButton.setText(BUTTON_TEXT_TOP_COMPONENT_ALPHA);
            } else if (evt.getEventType() == SORT_BY_SEMANTIC) {
                popuplateSemanticSearchResults();
                sortByButton.setText(BUTTON_TEXT_DESCRIPTION_SEMANTIC);
            } else if (evt.getEventType() == SORT_BY_SEMANTIC_ALPHA) {
                popuplateSemanticSearchResults();
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


    private void populateComponentSearchResults() {
        resultsVBox.getChildren().clear();
        JFXNode<Pane, SortResultConceptEntryController> searchConceptEntryJFXNode = FXMLMvvmLoader
                .make(SortResultConceptEntryController.class.getResource(SORT_CONCEPT_RESULT_CONCEPT_FXML));

        Node entry = searchConceptEntryJFXNode.node();
        resultsVBox.getChildren().addAll(entry);
    }

    private void popuplateSemanticSearchResults() {
        resultsVBox.getChildren().clear();
        JFXNode<Pane, SortResultSemanticEntryController> searchSemanticEntryJFXNode = FXMLMvvmLoader
                .make(SortResultSemanticEntryController.class.getResource(SORT_SEMANTIC_RESULT_CONCEPT_FXML));

        Node entry = searchSemanticEntryJFXNode.node();
        resultsVBox.getChildren().addAll(entry);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public <T extends ViewModel> T getViewModel() {
        return null;
    }
}

