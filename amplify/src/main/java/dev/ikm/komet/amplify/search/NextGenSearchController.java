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
package dev.ikm.komet.amplify.search;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class NextGenSearchController {

    private static final Logger LOG = LoggerFactory.getLogger(NextGenSearchController.class);

    public static final String SORT_OPTIONS_FXML = "sort-options.fxml";

    public static final String SORT_RESULT_CONCEPT_FXML = "search-result-concept-entry.fxml";

    @FXML
    private VBox resultsVBox;

    @FXML
    private ToggleGroup sortToggleGroup;

    private PopOver sortOptions;

    private SortOptionsController sortOptionsController;


    @FXML
    public void initialize() {
        populateSearchResults();
    }

    @FXML
    private void showSearchOptions(ActionEvent event) {
        if (sortOptions != null && sortOptionsController != null) {
            sortOptions.show((Node) event.getSource());
            return;
        }
        Node node = (Node) event.getSource();

        JFXNode<Pane, SortOptionsController> sortJFXNode = FXMLMvvmLoader
                .make(SortOptionsController.class.getResource(SORT_OPTIONS_FXML));

        Pane sortOptionsPane = sortJFXNode.node();
        PopOver popOver = new PopOver(sortOptionsPane);
        //FIXME: add styles?
        SortOptionsController sortOptionsController = sortJFXNode.controller();

        popOver.setOnHidden(windowEvent -> {
            //TODO save vm?, get values and publish to the eventbus?
        });

        //popOver.show((Node) event.getSource());

        sortOptions = popOver;
        this.sortOptionsController = sortOptionsController;
    }


    private void populateSearchResults() {
        Config searchEntryConfig = new Config()
                .fxml(SortResultConceptEntryController.class.getResource(SORT_RESULT_CONCEPT_FXML))
                //.addNamedViewModel(new NamedVm())
                ;
        JFXNode<Pane, SortResultConceptEntryController> searchEntryJFXNode = FXMLMvvmLoader.make(searchEntryConfig);

        Node entry = searchEntryJFXNode.node();
        resultsVBox.getChildren().addAll(entry);
    }
}

