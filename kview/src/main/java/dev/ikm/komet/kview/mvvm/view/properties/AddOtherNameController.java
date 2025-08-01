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
package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.*;

public class AddOtherNameController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(AddOtherNameController.class);

    private UUID conceptTopic;

    @FXML
    private Label addOtherNameTitleLabel;

    @FXML
    private ComboBox<ConceptEntity> moduleComboBox;

    @FXML
    private TextField otherNameTextField;

    @FXML
    private ComboBox<ConceptEntity> statusComboBox;

    @FXML
    private ComboBox<ConceptEntity> caseSignificanceComboBox;

    @FXML
    private ComboBox<ConceptEntity> languageComboBox;

    @FXML
    private Button submitButton;

    private EvtBus eventBus;

    private ViewProperties viewProperties;

    private EntityFacade entityFacade;

    @InjectViewModel
    private DescrNameViewModel otherNameViewModel;


    public AddOtherNameController() { }

    public AddOtherNameController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        clearView();
        submitButton.setDisable(true);
        setAddOtherNameTitleLabel("Add New Description: Other Name");
        // Initialize view models
        otherNameViewModel
                .setPropertyValue(NAME_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(STATUS, TinkarTerm.ACTIVE_STATE);
        // register listeners
        InvalidationListener formValid = (obs) -> {
            boolean isFormValid = isFormPopulated();
            if (isFormValid) {
                copyUIToViewModelProperties();
            }
            submitButton.setDisable(!isFormValid);
        };

        setupComboBox(moduleComboBox, formValid);
        setupComboBox(statusComboBox, formValid);
        setupComboBox(caseSignificanceComboBox, formValid);
        setupComboBox(languageComboBox, formValid);
    }

    /**
     * TODO: This is appropriate. A better solution is binding properties on the view model. If so, we'd need to unbind.
     * This copies form values into the ViewModel's property values. It does not save or validate.
     */
    private void copyUIToViewModelProperties() {
        if (otherNameViewModel != null) {
            otherNameViewModel.setPropertyValue(NAME_TEXT, otherNameTextField.getText())
                    .setPropertyValue(NAME_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE)
                    .setPropertyValue(CASE_SIGNIFICANCE, caseSignificanceComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(STATUS, statusComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(MODULE, moduleComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(LANGUAGE, languageComboBox.getSelectionModel().getSelectedItem());
        }
    }

    public void updateModel(final ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    public void populate(ComboBox comboBox, Collection<ConceptEntity> entities) {
        comboBox.getItems().addAll(entities);
    }


    @Override
    public DescrNameViewModel getViewModel() {
        return otherNameViewModel;
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
        return (otherNameTextField.getText() != null && !otherNameTextField.getText().toString().isEmpty())
                && (moduleComboBox.getSelectionModel().getSelectedItem() != null)
                && (statusComboBox.getSelectionModel().getSelectedItem() != null)
                && (caseSignificanceComboBox.getSelectionModel().getSelectedItem() != null)
                && (languageComboBox.getSelectionModel().getSelectedItem() != null);
    }

    public void setAddOtherNameTitleLabel(String addDescriptionTitleLabelText) {
        this.addOtherNameTitleLabel.setText(addDescriptionTitleLabelText);
    }

    @FXML
    private void saveOtherName(ActionEvent actionEvent) {
        actionEvent.consume();

        // TODO assuming it's valid save() check for errors and publish event
        otherNameViewModel.setPropertyValue(IS_SUBMITTED, true);
        otherNameViewModel.save();
        if (otherNameViewModel.hasNoErrorMsgs()) {
            // publish event with the otherNameViewModel.
            // ...
            LOG.info("Ready to add to the concept view model: " + otherNameViewModel);
            eventBus.publish(conceptTopic, new CreateConceptEvent(this, CreateConceptEvent.ADD_OTHER_NAME,
                    otherNameViewModel.create()));
            clearView();
            close();
        }

    }


    @Override
    public void updateView() {
        // populate form combo fields module, status, case significance, lang.
        populate(moduleComboBox, otherNameViewModel.findAllModules(getViewProperties()));
        populate(statusComboBox, otherNameViewModel.findAllStatuses(getViewProperties()));
        populate(caseSignificanceComboBox, otherNameViewModel.findAllCaseSignificants(getViewProperties()));
        populate(languageComboBox, otherNameViewModel.findAllLanguages(getViewProperties()));
    }


    @Override
    public void clearView() {
        otherNameTextField.setText("");
        moduleComboBox.setValue(null);
        statusComboBox.setValue(null);
        caseSignificanceComboBox.setValue(null);
        languageComboBox.setValue(null);
        moduleComboBox.getItems().clear();
        statusComboBox.getItems().clear();
        caseSignificanceComboBox.getItems().clear();
        languageComboBox.getItems().clear();
    }

    @FXML
    public void cancel() {
        close();
    }

    private void close() {
        // close the properties bump out
        eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(submitButton,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    @Override
    public void cleanup() {
    }
}
