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


import static dev.ikm.tinkar.events.FrameworkTopics.SEARCH_SORT_TOPIC;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_COMPONENT;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_COMPONENT_ALPHA;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_SEMANTIC;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_SEMANTIC_ALPHA;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.kview.events.SearchSortOptionEvent;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SortOptionsController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(SortOptionsController.class);

    @FXML
    private ToggleGroup sortToggleGroup;

    @FXML
    private RadioButton radioSemanticAlpha;

    @FXML
    private RadioButton radioSemantic;

    @FXML
    private RadioButton radioComponent;

    @FXML
    private RadioButton radioComponentAlpha;

    private EvtBus eventBus;

    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();

        // default to TOP COMPONENT
        radioComponent.setSelected(true);

        // when the user selects a radio button for one of the sort options
        sortToggleGroup.selectedToggleProperty().addListener(((observable, toggle, t1) -> {
            // show semantic results if the option selected is either
            //  - matched description semantic (alphabetical)
            //  - matched description semantic
            //  and show the concept (aka component) results if the option selected is either
            //  - top component (alphabetical)
            //  - top component
            if (t1.equals(radioSemanticAlpha)) {
                eventBus.publish(SEARCH_SORT_TOPIC, new SearchSortOptionEvent(sortToggleGroup, SORT_BY_SEMANTIC_ALPHA));
            } else if (t1.equals(radioSemantic)) {
                eventBus.publish(SEARCH_SORT_TOPIC, new SearchSortOptionEvent(sortToggleGroup, SORT_BY_SEMANTIC));
            } else if (t1.equals(radioComponentAlpha)) {
                eventBus.publish(SEARCH_SORT_TOPIC, new SearchSortOptionEvent(sortToggleGroup, SORT_BY_COMPONENT_ALPHA));
            } else if (t1.equals(radioComponent)) {
                eventBus.publish(SEARCH_SORT_TOPIC, new SearchSortOptionEvent(sortToggleGroup, SORT_BY_COMPONENT));
            }
        }));
    }

    @Override
    public <T extends ViewModel> T getViewModel() {
        return null;
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
}
