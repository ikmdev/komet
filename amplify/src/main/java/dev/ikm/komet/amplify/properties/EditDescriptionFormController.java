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
package dev.ikm.komet.amplify.properties;

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;
import static dev.ikm.tinkar.terms.TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN;

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.RequestRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.GeneratedActionImmediate;
import dev.ikm.komet.framework.rulebase.GeneratedActionSuggested;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private PublicId publicId;

    public EditDescriptionFormController() { }

    public EditDescriptionFormController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @Override
    @FXML
    public void initialize() {
        clearView();
        setEditDescriptionTitleLabel("Edit Description: Other Name");
        populateDialectComboBoxes();
        submitButton.setOnAction(this::saveOtherName);
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
        Optional<String> stringOptional = getViewProperties().calculator().getRegularDescriptionText(conceptEntity.nid());
        return stringOptional.orElse("");
    }

    private void setupComboBox(ComboBox comboBox, List<ConceptEntity> conceptEntities) {
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
        List<ConceptEntity> allModules =
                moduleDescendents.intStream()
                        .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                        .toList();
        setupComboBox(moduleComboBox, allModules);

        // populate the current module and select it (e.g. 'SNOMED CT core module')
        ConceptEntity currentModule = (ConceptEntity) stampEntity.module();
        moduleComboBox.getSelectionModel().select(currentModule);


        // get all statuses
        Entity<? extends EntityVersion> statusEntity = EntityService.get().getEntityFast(TinkarTerm.STATUS_VALUE);
        IntIdSet statusDescendents = viewProperties.calculator().descendentsOf(statusEntity.nid());
        List<ConceptEntity> allStatuses = statusDescendents.intStream()
                .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                .toList();
        setupComboBox(statusComboBox, allStatuses);

        // populate the current status (ACTIVE | INACTIVE) and select it
        ConceptEntity currentStatus = Entity.getFast(stampEntity.state().nid());
        statusComboBox.getSelectionModel().select(currentStatus);


        // populate all case significance choices
        Entity<? extends EntityVersion> caseSenseEntity = EntityService.get().getEntityFast(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE);
        IntIdSet caseSenseDescendents = viewProperties.calculator().descendentsOf(caseSenseEntity.nid());
        List<ConceptEntity> allCaseDescendents = caseSenseDescendents.intStream()
                .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                .toList();
        setupComboBox(caseSignificanceComboBox, allCaseDescendents);

        // get case concept's case sensitivity (e.g. 'Case insensitive')
        PatternEntity<PatternEntityVersion> patternEntity = latestEntityVersion.get().pattern();
        PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();
        int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);
        ConceptFacade caseSigConceptFacade = (ConceptFacade) latestEntityVersion.get().fieldValues().get(indexCaseSig);
        ConceptEntity caseSigConcept = Entity.getFast(caseSigConceptFacade.nid());
        caseSignificanceComboBox.getSelectionModel().select(caseSigConcept);

        // get all available languages
        Entity<? extends EntityVersion> languageEntity = EntityService.get().getEntityFast(TinkarTerm.LANGUAGE);
        IntIdSet languageDescendents = viewProperties.calculator().descendentsOf(languageEntity.nid());
        List<ConceptEntity> allLangs = languageDescendents.intStream()
                .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                .toList();
        setupComboBox(languageComboBox, allLangs);

        // get the language (e.g. 'English language')
        int indexLang = patternEntityVersion.indexForMeaning(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);
        ConceptFacade langConceptFacade = (ConceptFacade) latestEntityVersion.get().fieldValues().get(indexLang);

        ConceptEntity langConcept = Entity.getFast(langConceptFacade.nid());
        languageComboBox.getSelectionModel().select(langConcept);

        LOG.info(publicId.toString());
    }


    private void saveOtherName(ActionEvent actionEvent) {
        Transaction transaction = Transaction.make();

        StampEntity stampEntity = transaction.getStamp(
                State.fromConcept(statusComboBox.getValue()), // active, inactive, etc
                System.currentTimeMillis(),
                TinkarTerm.USER.nid(),
                moduleComboBox.getValue().nid(), // SNOMED CT, LOINC, etc
                TinkarTerm.DEVELOPMENT_PATH.nid());


        // existing semantic
        SemanticEntity theSemantic = EntityService.get().getEntityFast(publicId.asUuidList());


        // the versions that we will first populate with the existing versions of the semantic
        RecordListBuilder versions = RecordListBuilder.make();

        SemanticRecord descriptionSemantic = SemanticRecord.makeNew(publicId, TinkarTerm.DESCRIPTION_PATTERN.nid(),
                theSemantic.referencedComponentNid(), versions);

        // we grabbing the form data
        // populating the field values for the new version we are writing
        MutableList<Object> descriptionFields = Lists.mutable.empty();
        descriptionFields.add(languageComboBox.getValue());
        descriptionFields.add(otherNameTextField.getText());
        descriptionFields.add(caseSignificanceComboBox.getValue());
        descriptionFields.add(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);

        // iterating over the existing versions and adding them to a new record list builder
        theSemantic.versions().forEach(version -> versions.add(version));

        // adding the new (edit form) version here
        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(descriptionSemantic)
                .stampNid(stampEntity.nid())
                .fieldValues(descriptionFields.toImmutable())
                .build());

        // apply the updated versions to the new semantic record
        SemanticRecord newSemanticRecord = SemanticRecordBuilder.builder(descriptionSemantic).versions(versions.toImmutable()).build();

        // put the new semantic record in the transaction
        transaction.addComponent(newSemanticRecord);

        // perform the save
        Entity.provider().putEntity(newSemanticRecord);

        // commit the transaction
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);

        LOG.info("transaction complete");
    }



}
