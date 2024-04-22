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
package dev.ikm.komet.amplify.lidr.results;

import dev.ikm.komet.amplify.commons.AbstractBasicController;
import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.lidr.events.AddResultEvent;
import dev.ikm.komet.amplify.lidr.events.ShowPanelEvent;
import dev.ikm.komet.amplify.mvvm.loader.InjectViewModel;
import dev.ikm.komet.amplify.viewmodels.FormViewModel;
import dev.ikm.komet.amplify.lidr.viewmodels.ResultsViewModel;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.amplify.lidr.events.AddResultEvent.ADD_RESULT_TO_ANALYTE_GROUP;
import static dev.ikm.komet.amplify.lidr.events.ShowPanelEvent.SHOW_ADD_ANALYTE_GROUP;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.VIEW_PROPERTIES;

public class ResultsController extends AbstractBasicController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(ResultsController.class);

    @FXML
    private Button cancelButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button addButton;


    @InjectViewModel
    private ResultsViewModel resultsViewModel;

    EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
    @Override
    @FXML
    public void initialize() {
        clearView();

        // register listeners
        InvalidationListener formValid = (obs) -> {
            boolean isFormValid = isFormPopulated();
            if (isFormValid) {
                //FIXME
                //copyUIToViewModelProperties();
            }
            addButton.setDisable(!isFormValid);
        };

    }
    public ViewProperties getViewProperties() {
        return resultsViewModel.getPropertyValue(VIEW_PROPERTIES);
    }
    private UUID getConceptTopic() {
        return resultsViewModel.getPropertyValue(CONCEPT_TOPIC);
    }
    private String getDisplayText(ConceptEntity conceptEntity) {
        Optional<String> stringOptional = getViewProperties().calculator().getRegularDescriptionText(conceptEntity.nid());
        return stringOptional.orElse("");
    }

    private void setupComboBox(ComboBox comboBox, InvalidationListener listener) {
        comboBox.setConverter(new StringConverter<ConceptEntity>() {

            @Override
            public String toString(ConceptEntity conceptEntity) {
                return getDisplayText(conceptEntity);
            }

            @Override
            public ConceptEntity fromString(String string) {
                return null;
            }
        });

        comboBox.setCellFactory(new Callback<>() {

            /**
             * @param param The single argument upon which the returned value should be
             *              determined.
             * @return
             */
            @Override
            public ListCell<ConceptEntity> call(Object param) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(ConceptEntity conceptEntity, boolean b) {
                        super.updateItem(conceptEntity, b);
                        if (conceptEntity != null) {
                            setText(getDisplayText(conceptEntity));
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        });

        // register invalidation listener
        comboBox.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    private boolean isFormPopulated() {
        return true;
     //          (dataResultsTypeComboBox.getSelectionModel().getSelectedItem() != null)

    }

    @FXML
    void cancel(ActionEvent event) {

    }

    @FXML
    void clearView(ActionEvent event) {

    }

    @FXML
    void createOneResult(ActionEvent event) {
        LOG.info("createOneResult -> Todo publish event containing the result record to be added to analyte group controller.");
        // Todo publish event containing the result record to be added to the listener inside the Analyte group controller. payload is an object (not a view model).
        // 1. publish
        // 2. reset screen for next entry
        // 3. publish

        evtBus.publish(getConceptTopic(), new ShowPanelEvent(event.getSource(), SHOW_ADD_ANALYTE_GROUP));

        // TODO put a real entity or public id as the payload.
        evtBus.publish(getConceptTopic(), new AddResultEvent(event.getSource(), ADD_RESULT_TO_ANALYTE_GROUP, new Object(){
            @Override
            public String toString() {
                return "OneResult " + new Date();
            }
        }));
    }

    @Override
    public void updateView() {

        //dataResultsTypeComboBox.getItems().addAll(resultsViewModel.findAllDataResultsTypes());
    }

    public void populate(ComboBox comboBox, Collection<ConceptEntity> entities) {
        comboBox.getItems().addAll(entities);
    }

    @Override
    public void clearView() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public FormViewModel getViewModel() {
        return resultsViewModel;
    }
}
