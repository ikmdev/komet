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

import static dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CASE_SIGNIFICANCE;

import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.DESCRIPTION_NAME_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.SEMANTIC_PUBLIC_ID;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent;
import dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class DescriptionNameController {

    private static final Logger LOG = LoggerFactory.getLogger(DescriptionNameController.class);

    @FXML
    private TextField nameTextField;

    @FXML
    private ComboBox<String> nameDescriptionType;

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
        clearView();

        descrNameViewModel
                .setPropertyValue(STATUS, TinkarTerm.ACTIVE_STATE);

        populateDialectComboBoxes();

        //InvalidationListener invalidationListener = obs -> validateForm();

        if (descrNameViewModel.getPropertyValue(TITLE_TEXT) != null) {
            StringProperty titleTextProperty = descrNameViewModel.getProperty(TITLE_TEXT);
            editDescriptionTitleLabel.textProperty().bind(titleTextProperty);
        }

        if (descrNameViewModel.getProperty(DESCRIPTION_NAME_TYPE) != null) {
            StringProperty descriptionTypeProperty = descrNameViewModel.getProperty(DESCRIPTION_NAME_TYPE);
            nameDescriptionType.valueProperty().bind(descriptionTypeProperty);
        }

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
    private void handleCancelButtonEvent() {
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(), new ClosePropertiesPanelEvent(cancelButton,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    private boolean isFormPopulated() {
        return (nameTextField.getText() != null && !nameTextField.getText().toString().isEmpty())
                && (moduleComboBox.getSelectionModel().getSelectedItem() != null)
                && (statusComboBox.getSelectionModel().getSelectedItem() != null)
                && (caseSignificanceComboBox.getSelectionModel().getSelectedItem() != null)
                && (languageComboBox.getSelectionModel().getSelectedItem() != null);
    }

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
        caseSignificanceComboBox.getItems().clear();
        statusComboBox.getItems().clear();
        moduleComboBox.getItems().clear();
        languageComboBox.getItems().clear();
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

    public void setConceptAndPopulateForm(PublicId publicId) {
        clearView();
        ViewCalculator viewCalculator = getViewProperties().calculator();

        int nid = EntityService.get().nidForPublicId(publicId);

        // this is the Other Name
        Latest<SemanticEntityVersion> latestEntityVersion = viewCalculator.latest(nid);

        StampEntity stampEntity = latestEntityVersion.get().stamp();

        // populate the other name text field (e.g. 'Chronic lung disease')
        String otherName = viewCalculator.getDescriptionText(nid).get();
        this.nameTextField.setText(otherName);

        Entity<? extends EntityVersion> moduleEntity = EntityService.get().getEntityFast(TinkarTerm.MODULE);
        IntIdSet moduleDescendents = getViewProperties().calculator().descendentsOf(moduleEntity.nid());

        // get all descendant modules
        Set<ConceptEntity> allModules =
                moduleDescendents.intStream()
                        .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                        .collect(Collectors.toSet());
        setupComboBox(moduleComboBox, allModules);

        // populate the current module and select it (e.g. 'SNOMED CT core module')
        ConceptEntity currentModule = (ConceptEntity) stampEntity.module();
        moduleComboBox.getSelectionModel().select(currentModule);

        // get all statuses
        IntIdSet statusDescendents = getViewProperties().calculator().descendentsOf(TinkarTerm.STATUS_VALUE.nid());
        Set<ConceptEntity> allStatuses = statusDescendents.intStream()
                .mapToObj(statusNid -> (ConceptEntity) Entity.getFast(statusNid))
                .collect(Collectors.toSet());
        setupComboBox(statusComboBox, allStatuses);

        // populate the current status (ACTIVE | INACTIVE) and select it
        ConceptEntity currentStatus = Entity.getFast(stampEntity.state().nid());
        statusComboBox.getSelectionModel().select(currentStatus);

        // populate all case significance choices
        IntIdSet caseSenseDescendents = getViewProperties().calculator().descendentsOf(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE.nid());
        Set<ConceptEntity> allCaseDescendents = caseSenseDescendents.intStream()
                .mapToObj(caseNid -> (ConceptEntity) Entity.getFast(caseNid))
                .collect(Collectors.toSet());
        setupComboBox(caseSignificanceComboBox, allCaseDescendents);

        // get case concept's case sensitivity (e.g. 'Case insensitive')
        PatternEntity<PatternEntityVersion> patternEntity = latestEntityVersion.get().pattern();
        PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();
        int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);
        ConceptFacade caseSigConceptFacade = (ConceptFacade) latestEntityVersion.get().fieldValues().get(indexCaseSig);
        ConceptEntity caseSigConcept = Entity.getFast(caseSigConceptFacade.nid());
        caseSignificanceComboBox.getSelectionModel().select(caseSigConcept);

        // get all available languages
        IntIdSet languageDescendents = getViewProperties().calculator().descendentsOf(TinkarTerm.LANGUAGE.nid());
        Set<ConceptEntity> allLangs = languageDescendents.intStream()
                .mapToObj(langNid -> (ConceptEntity) Entity.getFast(langNid))
                .collect(Collectors.toSet());
        setupComboBox(languageComboBox, allLangs);

        // get the language (e.g. 'English language')
        int indexLang = patternEntityVersion.indexForMeaning(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);
        ConceptFacade langConceptFacade = (ConceptFacade) latestEntityVersion.get().fieldValues().get(indexLang);

        ConceptEntity langConcept = Entity.getFast(langConceptFacade.nid());
        languageComboBox.getSelectionModel().select(langConcept);

        //initial state of edit screen, the submit button should be disabled
        submitButton.setDisable(true);

        LOG.info(publicId.toString());
    }

    private void copyUIToViewModelProperties() {
        if (descrNameViewModel != null) {
            descrNameViewModel.setPropertyValue(NAME_TEXT, nameTextField.getText())
                    .setPropertyValue(CASE_SIGNIFICANCE, caseSignificanceComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(STATUS, statusComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(MODULE, moduleComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(LANGUAGE, languageComboBox.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void submitForm(ActionEvent actionEvent) {
        actionEvent.consume();
        descrNameViewModel.setPropertyValue(IS_SUBMITTED, true);
        copyUIToViewModelProperties();
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
    }

}
