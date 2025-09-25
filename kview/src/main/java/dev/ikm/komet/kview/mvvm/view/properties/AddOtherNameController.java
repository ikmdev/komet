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

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.DESCRIPTION_LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.HAS_OTHER_NAME;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_TYPE;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class AddOtherNameController extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(AddOtherNameController.class);

    private UUID conceptTopic;

    @FXML
    private Label addOtherNameTitleLabel;

    @FXML
    private TextField otherNameTextField;

    @FXML
    private ComboBox<EntityFacade> typeDisplayComboBox;

    @FXML
    private ComboBox<EntityFacade> caseSignificanceComboBox;

    @FXML
    private ComboBox<EntityFacade> statusComboBox;

    @FXML
    private ComboBox<EntityFacade> moduleComboBox;

    @FXML
    private ComboBox<EntityFacade> languageComboBox;

    @FXML
    private Button submitButton;

    private EvtBus eventBus;

    private ViewProperties viewProperties;

    private EntityFacade entityFacade;

    @InjectViewModel
    private OtherNameViewModel otherNameViewModel;


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

        otherNameTextField.textProperty().addListener(formValid);
        setupComboBox(moduleComboBox, formValid);
        setupComboBox(statusComboBox, formValid);
        setupComboBox(caseSignificanceComboBox, formValid);
        setupComboBox(languageComboBox, formValid);
        setupComboBox(typeDisplayComboBox, formValid);
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
    public OtherNameViewModel getViewModel() {
        return otherNameViewModel;
    }


    private String getDisplayText(ConceptFacade conceptFacade) {
        return getViewProperties().calculator().languageCalculator().getDescriptionTextOrNid(conceptFacade.nid());
    }

    private void setupComboBox(ComboBox comboBox, InvalidationListener listener) {
        comboBox.setConverter(new StringConverter<ConceptFacade>() {

            @Override
            public String toString(ConceptFacade conceptFacade) {
                return getDisplayText(conceptFacade.toProxy());
            }

            @Override
            public ConceptFacade fromString(String string) {
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
            public ListCell<ConceptFacade> call(Object param) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(ConceptFacade conceptFacade, boolean b) {
                        super.updateItem(conceptFacade, b);
                        if (conceptFacade != null) {
                            setText(getDisplayText(conceptFacade));
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
        populate(typeDisplayComboBox, fetchDescendentsOfConcept(getViewProperties(), DESCRIPTION_TYPE.publicId()));
        populate(moduleComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.MODULE.publicId()));
        populate(statusComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.STATUS_VALUE.publicId()));
        populate(caseSignificanceComboBox, otherNameViewModel.findAllCaseSignificants(getViewProperties()));
        populate(languageComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.LANGUAGE.publicId()));

        typeDisplayComboBox.setValue(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);

        boolean hasOtherName = getViewModel().getValue(HAS_OTHER_NAME);

        if (hasOtherName) {
            caseSignificanceComboBox.setValue(getViewModel().getValue(DESCRIPTION_CASE_SIGNIFICANCE));
        } else {
            caseSignificanceComboBox.setValue(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE);
        }
        statusComboBox.setValue(Entity.getFast(State.ACTIVE.nid()));
        moduleComboBox.setValue(TinkarTerm.DEVELOPMENT_MODULE);
        if (hasOtherName) {
            languageComboBox.setValue(getViewModel().getValue(DESCRIPTION_LANGUAGE));
        } else {
            languageComboBox.setValue(TinkarTerm.ENGLISH_LANGUAGE);
        }
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
    public void handleCancelButtonEvent() {
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
