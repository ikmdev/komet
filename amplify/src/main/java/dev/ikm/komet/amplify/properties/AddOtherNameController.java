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

import dev.ikm.komet.amplify.commons.BasicController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.ikm.komet.amplify.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticRecordBuilder;
import dev.ikm.tinkar.entity.SemanticVersionRecordBuilder;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddOtherNameController implements BasicController  {

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

    private PublicId publicId;

    public AddOtherNameController() { }

    public AddOtherNameController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        clearView();
        setAddOtherNameTitleLabel("Add New Description: Other Name");
        submitButton.setDisable(true);
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


        String otherName = viewCalculator.getDescriptionText(nid).get();

        Entity<? extends EntityVersion> moduleEntity = EntityService.get().getEntityFast(TinkarTerm.MODULE);
        IntIdSet moduleDescendents = viewProperties.calculator().descendentsOf(moduleEntity.nid());

        otherNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            submitButton.setDisable(!isFormPopulated());
        });
        // get all descendant modules
        List<ConceptEntity> allModules =
                moduleDescendents.intStream()
                        .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                        .toList();
        setupComboBox(moduleComboBox, allModules);

        moduleComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            submitButton.setDisable(!isFormPopulated());
        });


        // get all statuses
        Entity<? extends EntityVersion> statusEntity = EntityService.get().getEntityFast(TinkarTerm.STATUS_VALUE);
        IntIdSet statusDescendents = viewProperties.calculator().descendentsOf(statusEntity.nid());
        List<ConceptEntity> allStatuses = statusDescendents.intStream()
                .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                .toList();
        setupComboBox(statusComboBox, allStatuses);
        statusComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            submitButton.setDisable(!isFormPopulated());
        });


        // populate all case significance choices
        Entity<? extends EntityVersion> caseSenseEntity = EntityService.get().getEntityFast(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE);
        IntIdSet caseSenseDescendents = viewProperties.calculator().descendentsOf(caseSenseEntity.nid());
        List<ConceptEntity> allCaseDescendents = caseSenseDescendents.intStream()
                .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                .toList();
        setupComboBox(caseSignificanceComboBox, allCaseDescendents);
        caseSignificanceComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            submitButton.setDisable(!isFormPopulated());
        });

        // get all available languages
        Entity<? extends EntityVersion> languageEntity = EntityService.get().getEntityFast(TinkarTerm.LANGUAGE);
        IntIdSet languageDescendents = viewProperties.calculator().descendentsOf(languageEntity.nid());
        List<ConceptEntity> allLangs = languageDescendents.intStream()
                .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                .toList();
        setupComboBox(languageComboBox, allLangs);
        languageComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            submitButton.setDisable(!isFormPopulated());
        });

    }

    private boolean isFormPopulated() {
        return (otherNameTextField.getText() != null && !otherNameTextField.getText().toString().isEmpty())
                && (moduleComboBox.getValue() != null && !moduleComboBox.getValue().toString().isEmpty())
                && (statusComboBox.getValue() != null && !statusComboBox.getValue().toString().isEmpty())
                && (caseSignificanceComboBox.getValue() != null && !caseSignificanceComboBox.getValue().toString().isEmpty())
                && (languageComboBox.getValue() != null && !languageComboBox.getValue().toString().isEmpty());
    }

    public void setAddOtherNameTitleLabel(String addDescriptionTitleLabelText) {
        this.addOtherNameTitleLabel.setText(addDescriptionTitleLabelText);
    }

    @FXML
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
        eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(submitButton,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));

        // clear the form after saving.  otherwise when you navigate back to Add Other Name
        // you would have the previous form values still there
        clearView();
    }


    @Override
    public void updateView() {
    }


    @Override
    public void clearView() {
        otherNameTextField.setText("");
        moduleComboBox.getSelectionModel().clearSelection();
        //FIXME the prompt text isn't getting set for some reason
        moduleComboBox.setPromptText("Select Module");
        statusComboBox.getSelectionModel().clearSelection();
        statusComboBox.setPromptText("Select Status");
        caseSignificanceComboBox.getSelectionModel().clearSelection();
        caseSignificanceComboBox.setPromptText("Select case sensitivity");
        languageComboBox.getSelectionModel().clearSelection();
        languageComboBox.setPromptText("Select Language");
    }

    @Override
    public void cleanup() {
    }
}
