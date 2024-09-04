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
package dev.ikm.komet.kview.mvvm.view.descriptionname;

import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent;
import dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;

public class DescriptionNameController {

    private static final Logger LOG = LoggerFactory.getLogger(DescriptionNameController.class);

    @FXML
    private TextField nameTextField;

    @FXML
    private ComboBox<ConceptEntity> nameDescriptionType;

    @FXML
    private ComboBox<ConceptEntity> moduleComboBox;

    @FXML
    private ComboBox<ConceptEntity> statusComboBox;

    @FXML
    private ComboBox<ConceptEntity> caseSignificanceComboBox;

    @FXML
    private ComboBox<ConceptEntity> languageComboBox;

    @FXML
    private Label editDescriptionTitleLabel;

    @FXML
    private Label dialect1;

    @FXML
    private Label dialect2;

    @FXML
    private Label dialect3;

    @FXML
    private ComboBox dialectComboBox1;

    @FXML
    private ComboBox dialectComboBox2;

    @FXML
    private ComboBox dialectComboBox3;

    @FXML
    private Button submitButton;

    @FXML
    private Button cancelButton;

    @InjectViewModel
    private DescrNameViewModel descrNameViewModel;

    public DescriptionNameController() { }


    @FXML
    public void initialize() {

        populateDialectComboBoxes();

        //TODO These are temp hard coded values:
        // Can use below code later?
        // setupComboBox(nameDescriptionType, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.DESCRIPTION_TYPE.publicId()));

        setupComboBox(nameDescriptionType, fetchDescriptionTypes()); // Hard coded
        nameDescriptionType.valueProperty().bind(descrNameViewModel.getProperty(NAME_TYPE));

        editDescriptionTitleLabel.textProperty().bind(descrNameViewModel.getProperty(TITLE_TEXT));
        nameTextField.textProperty().bindBidirectional(descrNameViewModel.getProperty(NAME_TEXT));

        setupComboBox(moduleComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.MODULE.publicId()));
        moduleComboBox.valueProperty().bindBidirectional(descrNameViewModel.getProperty(MODULE));

        setupComboBox(statusComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.STATUS_VALUE.publicId()));
        statusComboBox.valueProperty().bindBidirectional(descrNameViewModel.getProperty(STATUS));

        //TODO These are temp hard coded values:
        // Can use below code later?
        // setupComboBox(caseSignificanceComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE.publicId())); // Hard Coded
        setupComboBox(caseSignificanceComboBox, descrNameViewModel.findAllCaseSignificants(getViewProperties()));
        caseSignificanceComboBox.valueProperty().bindBidirectional(descrNameViewModel.getProperty(CASE_SIGNIFICANCE));

        setupComboBox(languageComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.LANGUAGE.publicId()));
        languageComboBox.valueProperty().bindBidirectional(descrNameViewModel.getProperty(LANGUAGE));
        validateForm();
        //FIXME we haven't determined what is required and not required for Pattern>FQN or Pattern>OtherName
        // also we don't have the drop downs populated, so even if we wanted to validate, we would
        // have to get values for them in order to test the DONE functionality
        // therefore, turning off the validation for now until those stories

//        nameTextField.textProperty().addListener(invalidationListener);
//        moduleComboBox.valueProperty().addListener(invalidationListener);
//        caseSignificanceComboBox.valueProperty().addListener(invalidationListener);
//        statusComboBox.valueProperty().addListener(invalidationListener);
//        languageComboBox.valueProperty().addListener(invalidationListener);
//        validateForm();
    }

    @FXML
    private void handleCancelButtonEvent(ActionEvent actionEvent) {
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(),
                new PatternPropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        clearView();
    }

    private boolean isFormPopulated() {
        return (nameTextField.getText() != null && !nameTextField.getText().toString().isEmpty())
                && (moduleComboBox.getSelectionModel().getSelectedItem() != null)
                && (statusComboBox.getSelectionModel().getSelectedItem() != null)
                && (caseSignificanceComboBox.getSelectionModel().getSelectedItem() != null)
                && (languageComboBox.getSelectionModel().getSelectedItem() != null);
    }

    @FXML
    private void validateForm() {
        boolean isOtherNameTextFieldEmpty = nameTextField.getText().trim().isEmpty();
        boolean isModuleComboBoxSelected = moduleComboBox.getValue() != null;
        boolean isCaseSignificanceComboBoxSelected = caseSignificanceComboBox.getValue() != null;
        boolean isStatusComboBoxComboBoxSelected = statusComboBox.getValue() != null;
        boolean isLanguageComboBoxComboBoxSelected = languageComboBox.getValue() != null;

        submitButton.setDisable(
                isOtherNameTextFieldEmpty || !isModuleComboBoxSelected
                || !isCaseSignificanceComboBoxSelected || !isLanguageComboBoxComboBoxSelected
                || !isStatusComboBoxComboBoxSelected);
    }

    private void populateDialectComboBoxes() {
        // currently no UNACCEPTABLE in TinkarTerm
        Entity<? extends EntityVersion> acceptable = EntityService.get().getEntityFast(TinkarTerm.ACCEPTABLE);
        Entity<? extends EntityVersion> preferred = EntityService.get().getEntityFast(TinkarTerm.PREFERRED);

        // each combo box has a separate list instance
        setupComboBox(dialectComboBox1, Arrays.asList(Entity.getFast(acceptable.nid()), Entity.getFast(preferred.nid())));
        dialectComboBox1.getSelectionModel().select(Entity.getFast(acceptable.nid()));
        setupComboBox(dialectComboBox2, Arrays.asList(Entity.getFast(acceptable.nid()), Entity.getFast(preferred.nid())));
        dialectComboBox2.getSelectionModel().select(Entity.getFast(preferred.nid()));
        setupComboBox(dialectComboBox3, Arrays.asList(Entity.getFast(acceptable.nid()), Entity.getFast(preferred.nid())));
        dialectComboBox3.getSelectionModel().select(Entity.getFast(preferred.nid()));
    }

    public void setEditDescriptionTitleLabel(String addAxiomTitleLabelText) {
        this.editDescriptionTitleLabel.setText(addAxiomTitleLabelText);
    }

    public void clearView() {
        nameTextField.clear();
        descrNameViewModel.setPropertyValue(STATUS, null);
        descrNameViewModel.setPropertyValue(MODULE, null);
        descrNameViewModel.setPropertyValue(CASE_SIGNIFICANCE, null);
        descrNameViewModel.setPropertyValue(LANGUAGE, null);
        submitButton.setDisable(true);
    }

    public void cleanup() {

    }

    private ViewProperties getViewProperties() {
        return descrNameViewModel.getPropertyValue(VIEW_PROPERTIES);
    }
    private UUID getPatternTopic() {
        return descrNameViewModel.getPropertyValue(PATTERN_TOPIC);
    }


    private String getDisplayText(ConceptEntity conceptEntity) {
        if (conceptEntity != null) {
            Optional<String> stringOptional = getViewProperties().calculator().getRegularDescriptionText(conceptEntity.nid());
            return stringOptional.orElse("");
        } else {
            return "";
        }
    }

    private void setupComboBox(ComboBox comboBox, Collection<ConceptEntity> conceptEntities) {

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
        comboBox.getItems().addAll(conceptEntities);
    }

    @FXML
    private void submitForm(ActionEvent actionEvent) {
        actionEvent.consume();
        descrNameViewModel.setPropertyValue(IS_SUBMITTED, true);
        descrNameViewModel.save();

        if (!descrNameViewModel.hasNoErrorMsgs()) {
            // publish event with the otherNameViewModel.
            return;
        }
        LOG.info("Ready to update to the concept view model: " + descrNameViewModel);

        if (descrNameViewModel.getPropertyValue(NAME_TYPE) == FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE) {
            EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(), new PatternDescriptionEvent(submitButton,
                    PatternDescriptionEvent.PATTERN_ADD_FQN, descrNameViewModel.create()));

        } else if (descrNameViewModel.getPropertyValue(NAME_TYPE) == REGULAR_NAME_DESCRIPTION_TYPE) {
            EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(), new PatternDescriptionEvent(submitButton,
                    PatternDescriptionEvent.PATTERN_ADD_OTHER_NAME, descrNameViewModel.create()));
        }

        //publish close env
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(),
                new PatternPropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        clearView();

    }

}
