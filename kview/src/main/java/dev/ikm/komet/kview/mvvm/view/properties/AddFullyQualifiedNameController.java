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
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
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

public class AddFullyQualifiedNameController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(AddFullyQualifiedNameController.class);

    private UUID conceptTopic;

    @FXML
    private Label addFqnTitleLabel;

    @FXML
    private ComboBox<EntityFacade> moduleComboBox;

    @FXML
    private TextField fullyQualifiedNameTextField;

    @FXML
    private ComboBox<EntityFacade> statusComboBox;

    @FXML
    private ComboBox<EntityFacade> caseSignificanceComboBox;

    @FXML
    private ComboBox<EntityFacade> languageComboBox;

    @FXML
    private Button submitButton;

    private EvtBus eventBus;

    private ViewProperties viewProperties;

    private PublicId publicId;

    @InjectViewModel
    private DescrNameViewModel fqnViewModel;

    public AddFullyQualifiedNameController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        clearView();
        submitButton.setDisable(true);


        // Initialize the fqnViewModel
        fqnViewModel
                .setPropertyValue(NAME_TYPE, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(STATUS, TinkarTerm.ACTIVE_STATE);

        // register listeners
        InvalidationListener formValid = (obs) -> {
            boolean isFormValid = isFormPopulated();
            if (isFormValid) {
                copyUIToViewModelProperties();
            }
            submitButton.setDisable(!isFormValid);
        };

        fullyQualifiedNameTextField.textProperty().addListener(formValid);
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
        if (fqnViewModel != null) {
            fqnViewModel.setPropertyValue(NAME_TEXT, fullyQualifiedNameTextField.getText())
                    .setPropertyValue(NAME_TYPE, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                    .setPropertyValue(CASE_SIGNIFICANCE, caseSignificanceComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(STATUS, statusComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(MODULE, moduleComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(LANGUAGE, languageComboBox.getSelectionModel().getSelectedItem());
        }
    }

    public void populate(ComboBox comboBox, Collection<ConceptEntity> entities) {
        comboBox.getItems().setAll(entities);
    }

    @Override
    public void updateView() {

        // populate form combo fields module, status, case significance, lang.
        populate(moduleComboBox, fqnViewModel.findAllModules(getViewProperties()));
        populate(statusComboBox, fqnViewModel.findAllStatuses(getViewProperties()));
        populate(caseSignificanceComboBox, fqnViewModel.findAllCaseSignificants(getViewProperties()));
        populate(languageComboBox, fqnViewModel.findAllLanguages(getViewProperties()));

        // Set UI to default values
        caseSignificanceComboBox.setValue(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE);
        statusComboBox.setValue(Entity.getFast(State.ACTIVE.nid()));
        moduleComboBox.setValue(TinkarTerm.DEVELOPMENT_MODULE);
        languageComboBox.setValue(TinkarTerm.ENGLISH_LANGUAGE);
    }

    @Override
    public void clearView() {
        fullyQualifiedNameTextField.setText("");
        moduleComboBox.setValue(null);
        statusComboBox.setValue(null);
        caseSignificanceComboBox.setValue(null);
        languageComboBox.setValue(null);
//        if (fqnViewModel != null) {
//            copyUIToViewModelProperties();
//            fqnViewModel.save(true); // make UI properties as model values
//        }
        moduleComboBox.getItems().clear();
        statusComboBox.getItems().clear();
        caseSignificanceComboBox.getItems().clear();
        languageComboBox.getItems().clear();
    }

    @Override
    public void cleanup() {

    }

    private boolean isFormPopulated() {
        return (fullyQualifiedNameTextField.getText() != null && !fullyQualifiedNameTextField.getText().toString().isEmpty())
                && (moduleComboBox.getSelectionModel().getSelectedItem() != null)
                && (statusComboBox.getSelectionModel().getSelectedItem() != null)
                && (caseSignificanceComboBox.getSelectionModel().getSelectedItem() != null)
                && (languageComboBox.getSelectionModel().getSelectedItem() != null);
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

    @FXML
    private void saveFullQualifiedName(ActionEvent actionEvent) {
        actionEvent.consume();

        // TODO assuming it's valid save() check for errors and publish event

        fqnViewModel.save();
        if (fqnViewModel.getValidationMessages().size() == 0) {
            // publish event with the fqnViewModel.
            // ... This property may not be needed.
            fqnViewModel.setPropertyValue(IS_SUBMITTED, true);
        }

        LOG.info("Ready to add to the concept view model: " + fqnViewModel);

        //////////////////////////////////////////////////////////////////////////////////////////
        // event received in Details Controller that will call conceptViewModel.createConcept()
        //////////////////////////////////////////////////////////////////////////////////////////
        DescrName fqnDescrName = fqnViewModel.create();
        eventBus.publish(conceptTopic, new CreateConceptEvent(this, CreateConceptEvent.ADD_FQN, fqnDescrName));

        // clear the form after saving.  otherwise when you navigate back to Add Other Name
        // you would have the previous form values still there
        clearView();
        close();
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
    public DescrNameViewModel getViewModel() {
        return fqnViewModel;
    }
}
