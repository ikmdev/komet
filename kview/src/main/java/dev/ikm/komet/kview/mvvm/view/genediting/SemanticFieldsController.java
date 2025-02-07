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
package dev.ikm.komet.kview.mvvm.view.genediting;


import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.PUBLISH;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static java.util.concurrent.CompletableFuture.runAsync;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SemanticFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(SemanticFieldsController.class);

    @FXML
    private VBox editFieldsVBox;
    @FXML
    private Button cancelButton;

    @FXML
    private Button clearFormButton;

    @FXML
    private Button submitButton;

    @InjectViewModel
    private ValidationViewModel semanticFieldsViewModel;

    private List<ObservableField<?>> observableFields = new ArrayList<>();

    @FXML
    private void initialize() {
        // clear all semantic details.
        editFieldsVBox.setSpacing(8.0);
        editFieldsVBox.getChildren().clear();

        EntityFacade semantic = semanticFieldsViewModel.getPropertyValue(SEMANTIC);
        if (semantic != null) {
            StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
            Latest<SemanticEntityVersion> semanticEntityVersionLatest = stampCalculator.latest(semantic.nid());
            if (semanticEntityVersionLatest.isPresent()) {
                // Populate the Semantic Details
                // Displaying editable controls and populating the observable fields array list.
                observableFields.addAll(KlFieldHelper
                        .displayEditableSemanticFields(getViewProperties(),
                                editFieldsVBox,
                                semanticEntityVersionLatest));
            } else {
                // TODO Add a new semantic based on a pattern (blank fields).
            }

        }
    }

    public ViewProperties getViewProperties() {
        return semanticFieldsViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        actionEvent.consume();
        // if previous state was closed cancel will close properties bump out.
        // else show
    }

    @FXML
    private void clearForm(ActionEvent actionEvent) {
        actionEvent.consume();
    }

    @FXML
    public void submit(ActionEvent actionEvent) {
        actionEvent.consume();
        List<ObservableField<?>> list = new ArrayList<>();
        observableFields.forEach(observableField -> {
            if (observableField != null) {
                runAsync(() -> {
                    observableField.writeToDataBase();
                });
            }
            list.add(observableField);
        });
        EntityFacade semantic = semanticFieldsViewModel.getPropertyValue(SEMANTIC);
        //EventBus implementation changes to refresh the details area
        EvtBusFactory.getDefaultEvtBus().publish(semanticFieldsViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new GenEditingEvent(actionEvent.getSource(), PUBLISH, list, semantic.nid()));

    }
}
