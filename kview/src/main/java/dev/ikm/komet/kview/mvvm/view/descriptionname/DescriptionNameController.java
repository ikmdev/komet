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

import static dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent.PATTERN_ADD_FQN;
import static dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent.PATTERN_ADD_OTHER_NAME;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescriptionTypes;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CONCEPT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_INVALID;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.PARENT_PROCESS;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.PATTERN;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.VIEW_PROPERTIES;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
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
import javafx.beans.value.ChangeListener;
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

    public static final String ADD_FQN_TITLE_TEXT = "Add Description: Add Fully Qualified Name";

    public static final String EDIT_FQN_TITLE_TEXT = "Edit Description: Edit Fully Qualified Name";

    public static final String ADD_OTHER_NAME_TITLE_TEXT = "Add Description: Add Other Name";

    public static final String EDIT_OTHER_NAME_TITLE_TEXT = "Edit Description: Edit Other Name";

    private static final Logger LOG = LoggerFactory.getLogger(DescriptionNameController.class);

    @FXML
    private TextField nameTextField;

    private PublicId publicId;

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

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;

    public DescriptionNameController() { }


    @FXML
    public void initialize() {
        ChangeListener fieldsValidationListener = (obs, oldValue, newValue) -> {
            descrNameViewModel.validate();
            descrNameViewModel.setPropertyValue(IS_INVALID, descrNameViewModel.hasErrorMsgs());
        };

        submitButton.disableProperty().bind(descrNameViewModel.getProperty(IS_INVALID));

        populateDialectComboBoxes();

        editDescriptionTitleLabel.textProperty().bind(descrNameViewModel.getProperty(TITLE_TEXT));

        //TODO These are temp hard coded values:
        // Can use below code later?
        // setupComboBox(nameDescriptionType, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.DESCRIPTION_TYPE.publicId()));

        setupComboBox(nameDescriptionType, fetchDescriptionTypes()); // Hard coded
        ObjectProperty<ConceptEntity> nameTypeProp = descrNameViewModel.getProperty(NAME_TYPE);
        nameDescriptionType.valueProperty().bind(nameTypeProp);
        nameTypeProp.addListener(fieldsValidationListener);

        StringProperty nameTextProp = descrNameViewModel.getProperty(NAME_TEXT);
        nameTextField.textProperty().bindBidirectional(nameTextProp);
        nameTextProp.addListener(fieldsValidationListener);

        setupComboBox(moduleComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.MODULE.publicId()));
        ObjectProperty<ConceptEntity> moduleProp = descrNameViewModel.getProperty(MODULE);
        moduleComboBox.valueProperty().bindBidirectional(moduleProp);
        moduleProp.addListener(fieldsValidationListener);

        setupComboBox(statusComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.STATUS_VALUE.publicId()));
        ObjectProperty<ConceptEntity> statusProp = descrNameViewModel.getProperty(STATUS);
        statusComboBox.valueProperty().bindBidirectional(statusProp);
        statusProp.addListener(fieldsValidationListener);

        //TODO These are temp hard coded values:
        // Can use below code later?
        // setupComboBox(caseSignificanceComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE.publicId())); // Hard Coded
        setupComboBox(caseSignificanceComboBox, descrNameViewModel.findAllCaseSignificants(getViewProperties()));
        ObjectProperty<ConceptEntity> caseSignificanceProp = descrNameViewModel.getProperty(CASE_SIGNIFICANCE);
        caseSignificanceComboBox.valueProperty().bindBidirectional(caseSignificanceProp);
        caseSignificanceProp.addListener(fieldsValidationListener);

        setupComboBox(languageComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.LANGUAGE.publicId()));
        ObjectProperty<ConceptEntity> languageProp = descrNameViewModel.getProperty(LANGUAGE);
        languageComboBox.valueProperty().bindBidirectional(languageProp);
        languageProp.addListener(fieldsValidationListener);
    }

    @FXML
    private void handleCancelButtonEvent(ActionEvent actionEvent) {
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(getTopic(),
                new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        clearView();
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


    public void clearView() {
        nameTextField.clear();
        descrNameViewModel.setPropertyValue(STATUS, null);
        descrNameViewModel.setPropertyValue(MODULE, null);
        descrNameViewModel.setPropertyValue(CASE_SIGNIFICANCE, null);
        descrNameViewModel.setPropertyValue(LANGUAGE, null);
    }

    public void cleanup() {

    }

    private ViewProperties getViewProperties() {
        return descrNameViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    /**
     * the DescriptionNameController can be used by Concepts and Patterns
     * and therefore use a different topic for the instance in which was created
     * @return
     */
    private UUID getTopic() {
        return descrNameViewModel.getPropertyValue(TOPIC);
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

        if (descrNameViewModel.getPropertyValue(PARENT_PROCESS) == PATTERN) {
            if (descrNameViewModel.getPropertyValue(NAME_TYPE) == FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE) {
                EvtBusFactory.getDefaultEvtBus().publish(getTopic(), new PatternDescriptionEvent(submitButton,
                        PATTERN_ADD_FQN, descrNameViewModel.create()));
            } else if (descrNameViewModel.getPropertyValue(NAME_TYPE) == REGULAR_NAME_DESCRIPTION_TYPE) {
                EvtBusFactory.getDefaultEvtBus().publish(getTopic(), new PatternDescriptionEvent(submitButton,
                        PATTERN_ADD_OTHER_NAME, descrNameViewModel.create()));
            }
        } else if (descrNameViewModel.getPropertyValue(PARENT_PROCESS) == CONCEPT) {
            if (descrNameViewModel.getPropertyValue(NAME_TYPE) == FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE) {
                DescrName fqnDescrName = descrNameViewModel.create();
                EvtBusFactory.getDefaultEvtBus().publish(getTopic(), new CreateConceptEvent(this, CreateConceptEvent.ADD_FQN, fqnDescrName));

                clearView();
                close();
            }
        }

        clearView();
    }

    private void close() {
        // close the properties bump out
        EvtBusFactory.getDefaultEvtBus().publish(getTopic(), new ClosePropertiesPanelEvent(submitButton,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    public void populateFormWithFqn(DescrName descrName) {
        descrNameViewModel.setPropertyValue(NAME_TEXT, descrName.getNameText());
        descrNameViewModel.setPropertyValue(CASE_SIGNIFICANCE, descrName.getCaseSignificance());
        descrNameViewModel.setPropertyValue(LANGUAGE, descrName.getLanguage());
    }

    public void setConceptAndPopulateForm(PublicId publicId) {
        this.publicId = publicId;

        ViewCalculator viewCalculator = getViewProperties().calculator();

        int nid = EntityService.get().nidForPublicId(publicId);

        // this is the Other Name
        Latest<SemanticEntityVersion> latestEntityVersion = viewCalculator.latest(nid);

        StampEntity stampEntity = latestEntityVersion.get().stamp();

        //FIX ME: we need to get the Fully Qualified Name and not the Other Name
        // populate the other name text field (e.g. 'Chronic lung disease')
        String otherName = viewCalculator.getDescriptionText(nid).get();
        this.nameTextField.setText(otherName);


        // get all descendant modules
        IntIdSet moduleDescendents = getViewProperties().calculator().descendentsOf(TinkarTerm.MODULE.nid());
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
        IntIdSet caseSenseDescendents = getViewProperties().calculator().descendentsOf(DESCRIPTION_CASE_SIGNIFICANCE.nid());
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
        //submitButton.setDisable(true);

        LOG.info(publicId.toString());
    }

    public void setConceptAndPopulateForm(DescrName descrName) {
        setupComboBox(moduleComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.MODULE.publicId()));
        setupComboBox(statusComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.STATUS_VALUE.publicId()));
        setupComboBox(caseSignificanceComboBox, descrNameViewModel.findAllCaseSignificants(getViewProperties()));
        setupComboBox(languageComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.LANGUAGE.publicId()));
        descrNameViewModel.setPropertyValue(NAME_TEXT, descrName.getNameText())
                .setPropertyValue(CASE_SIGNIFICANCE, descrName.getCaseSignificance())
                .setPropertyValue(STATUS, descrName.getStatus())
                .setPropertyValue(MODULE, descrName.getModule())
                .setPropertyValue(LANGUAGE, descrName.getLanguage());
    }

}
