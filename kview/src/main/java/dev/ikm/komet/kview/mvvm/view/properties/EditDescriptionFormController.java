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

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptFacade;
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

import java.util.*;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.*;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;

public class EditDescriptionFormController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(EditDescriptionFormController.class);

    private UUID conceptTopic;

    private EntityFacade entityFacade;

    private Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap;

    private ViewProperties viewProperties;

    @FXML
    private TextField otherNameTextField;

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

    private PublicId publicId;

    private EvtBus eventBus;

    @InjectViewModel
    private DescrNameViewModel otherNameViewModel;

    public EditDescriptionFormController() { }

    public EditDescriptionFormController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @Override
    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        clearView();
        setEditDescriptionTitleLabel("Edit Description: Other Name");

        otherNameViewModel
                .setPropertyValue(NAME_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(STATUS, TinkarTerm.ACTIVE_STATE);

        populateDialectComboBoxes();

        // bind with viewmodel.
        otherNameTextField.textProperty().bindBidirectional(otherNameViewModel.getProperty(NAME_TEXT));
        moduleComboBox.valueProperty().bindBidirectional(otherNameViewModel.getProperty(MODULE));
        caseSignificanceComboBox.valueProperty().bindBidirectional(otherNameViewModel.getProperty(CASE_SIGNIFICANCE));
        statusComboBox.valueProperty().bindBidirectional(otherNameViewModel.getProperty(STATUS));
        languageComboBox.valueProperty().bindBidirectional(otherNameViewModel.getProperty(LANGUAGE));

        InvalidationListener invalidationListener = obs -> validateForm();

        otherNameTextField.textProperty().addListener(invalidationListener);
        moduleComboBox.valueProperty().addListener(invalidationListener);
        caseSignificanceComboBox.valueProperty().addListener(invalidationListener);
        statusComboBox.valueProperty().addListener(invalidationListener);
        languageComboBox.valueProperty().addListener(invalidationListener);
        validateForm();
    }

    @FXML
    private void handleCancelButtonEvent() {
        eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(cancelButton,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    private boolean isFormPopulated() {
        return (otherNameTextField.getText() != null && !otherNameTextField.getText().toString().isEmpty())
                && (moduleComboBox.getSelectionModel().getSelectedItem() != null)
                && (statusComboBox.getSelectionModel().getSelectedItem() != null)
                && (caseSignificanceComboBox.getSelectionModel().getSelectedItem() != null)
                && (languageComboBox.getSelectionModel().getSelectedItem() != null);
    }

    private void validateForm() {
        boolean isOtherNameTextFieldEmpty = otherNameTextField.getText().trim().isEmpty();
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

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        caseSignificanceComboBox.getItems().clear();
        statusComboBox.getItems().clear();
        moduleComboBox.getItems().clear();
        languageComboBox.getItems().clear();
    }

    @Override
    public void cleanup() {

    }

    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade) {
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
    }

    private ViewProperties getViewProperties() {
        return this.viewProperties;
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
        editDescrName = null;
        this.publicId = publicId;
        ViewCalculator viewCalculator = viewProperties.calculator();

        int nid = EntityService.get().nidForPublicId(publicId);

        // this is the Other Name
        Latest<SemanticEntityVersion> latestEntityVersion = viewCalculator.latest(nid);

        StampEntity stampEntity = latestEntityVersion.get().stamp();

        // populate the other name text field (e.g. 'Chronic lung disease')
        String otherName = viewCalculator.getDescriptionText(nid).get();
        this.otherNameTextField.setText(otherName);

        Entity<? extends EntityVersion> moduleEntity = EntityService.get().getEntityFast(TinkarTerm.MODULE);
        IntIdSet moduleDescendents = viewProperties.calculator().descendentsOf(moduleEntity.nid());

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
        IntIdSet statusDescendents = viewProperties.calculator().descendentsOf(TinkarTerm.STATUS_VALUE.nid());
        Set<ConceptEntity> allStatuses = statusDescendents.intStream()
                .mapToObj(statusNid -> (ConceptEntity) Entity.getFast(statusNid))
                .collect(Collectors.toSet());
        setupComboBox(statusComboBox, allStatuses);

        // populate the current status (ACTIVE | INACTIVE) and select it
        ConceptEntity currentStatus = Entity.getFast(stampEntity.state().nid());
        statusComboBox.getSelectionModel().select(currentStatus);

        // populate all case significance choices
        IntIdSet caseSenseDescendents = viewProperties.calculator().descendentsOf(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE.nid());
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
        IntIdSet languageDescendents = viewProperties.calculator().descendentsOf(TinkarTerm.LANGUAGE.nid());
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

    @FXML
    private void updateOtherName(ActionEvent actionEvent) {
        actionEvent.consume();
        otherNameViewModel.save();

        if (!otherNameViewModel.hasNoErrorMsgs()) {
            otherNameViewModel.getValidationMessages().stream().forEach(msg -> LOG.error("Validation error " + msg));
            return;
        }

        otherNameViewModel.setPropertyValue(IS_SUBMITTED, true);


        LOG.info("Ready to update to the concept view model: " + otherNameViewModel);

        if(this.publicId != null) { //This if blocked is called when editing the exiting concept.
            otherNameViewModel.updateOtherName(this.publicId);
        }else{  // This block is called when editing the while creating the concept.
            otherNameViewModel.updateData(editDescrName);
            eventBus.publish(conceptTopic, new CreateConceptEvent(this,
                    CreateConceptEvent.EDIT_OTHER_NAME, editDescrName));
        }
        eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(submitButton,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    private DescrName editDescrName;

    /**
     * This method prepopulates and sets up the form in edit mode.
     * @param descrName model values that need to be prepopulated.
     */
    public void setConceptAndPopulateForm(DescrName descrName) {
        editDescrName = descrName;
        setupComboBox(moduleComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.MODULE.publicId()));
        setupComboBox(statusComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.STATUS_VALUE.publicId()));
        setupComboBox(caseSignificanceComboBox, otherNameViewModel.findAllCaseSignificants(getViewProperties()));
        setupComboBox(languageComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.LANGUAGE.publicId()));
        otherNameViewModel.setPropertyValue(NAME_TEXT, descrName.getNameText())
                .setPropertyValue(CASE_SIGNIFICANCE, descrName.getCaseSignificance())
                .setPropertyValue(STATUS, descrName.getStatus())
                .setPropertyValue(MODULE, descrName.getModule())
                .setPropertyValue(LANGUAGE, descrName.getLanguage());
    }

}
