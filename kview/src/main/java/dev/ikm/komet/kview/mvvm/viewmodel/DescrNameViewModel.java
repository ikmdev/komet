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
package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import org.carlfx.cognitive.validator.MessageType;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper.CASE_SIGNIFICANCE_OPTIONS;

public class DescrNameViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(DescrNameViewModel.class);

    public static final String NAME_TEXT = "nameText";
    public static final String NAME_TYPE = "nameType";
    public static final String CASE_SIGNIFICANCE = "caseSignificance";
    public static final String STATUS = "status";
    public static final String MODULE = "module";
    public static final String LANGUAGE = "language";
    public static final String IS_SUBMITTED = "isSubmitted";
    public static final String MODULES_PROPERTY = "modules";
    public static final String PATHS_PROPERTY = "paths";

    public static final String PARENT_PUBLIC_ID = "parentPublidId";

    public static final String SEMANTIC_PUBLIC_ID = "semanticPublidId";

    public static final String TITLE_TEXT = "titleText";

    public static final String DESCRIPTION_NAME_TYPE = "descrNameType";

    public static final String IS_INVALID = "isInvalid";

    public static final String PREVIOUS_DESCRIPTION_DATA = "previousDescriptionData";

    public DescrNameViewModel() {
        super(); // defaults to View mode
                addProperty(NAME_TEXT, "")
                .addValidator(NAME_TEXT, "Name Text", (ReadOnlyStringProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isEmpty().get()) {
                        validationResult.error("${%s} is required".formatted(NAME_TEXT));
                    }
                })
                .addProperty(NAME_TYPE, (ConceptEntity) null)
                .addValidator(NAME_TYPE, "Name Type", (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(NAME_TYPE));
                    }
                })

                .addProperty(CASE_SIGNIFICANCE, (ConceptEntity) null)
                .addValidator(CASE_SIGNIFICANCE, "Case Significance", (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(CASE_SIGNIFICANCE));
                    }
                })
                .addProperty(STATUS, (ConceptEntity) null)
                .addValidator(STATUS, "Status", (ReadOnlyObjectProperty prop,ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(STATUS));
                    }
                })
                .addProperty(MODULE, (ConceptEntity) null)
                .addValidator(MODULE, "Module", (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(MODULE));
                    }
                })
                .addProperty(LANGUAGE, (ConceptEntity) null)
                .addValidator(LANGUAGE, "Language", (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(LANGUAGE));
                    }
                })
                .addProperty(IS_SUBMITTED, false)
                .addProperty(PARENT_PUBLIC_ID, (PublicId) null)
                .addProperty(SEMANTIC_PUBLIC_ID, (PublicId) null)
                .addProperty(TITLE_TEXT, "")
                .addProperty(DESCRIPTION_NAME_TYPE, "")
                .addProperty(IS_INVALID, true)
                .addProperty(PREVIOUS_DESCRIPTION_DATA, (DescrName) null)
            ;
    }

    public Set<ConceptEntity> findAllLanguages(ViewProperties viewProperties) {
        IntIdSet languageDescendents = viewProperties.calculator().descendentsOf(TinkarTerm.LANGUAGE.nid());
        Set<ConceptEntity> allLangs = languageDescendents.intStream()
                .mapToObj(langNid -> (ConceptEntity) Entity.getFast(langNid))
                .collect(Collectors.toSet());
        return allLangs;
    }

    public Set<ConceptEntity> findAllStatuses(ViewProperties viewProperties) {
        Entity<? extends EntityVersion> statusEntity = EntityService.get().getEntityFast(TinkarTerm.STATUS_VALUE);
        IntIdSet statusDescendents = viewProperties.calculator().descendentsOf(statusEntity.nid());
        Set<ConceptEntity> allStatuses = statusDescendents.intStream()
                .mapToObj(statusNid -> (ConceptEntity) Entity.getFast(statusNid))
                .collect(Collectors.toSet());
        return allStatuses;
    }

    public Set<ConceptEntity> findAllCaseSignificants(ViewProperties viewProperties) {


        //FIXME after connect-a-thon put this query back
//        IntIdSet caseSenseDescendents = viewProperties.calculator().descendentsOf(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE.nid());
//        Set<ConceptEntity> allCaseDescendents = caseSenseDescendents.intStream()
//                .mapToObj(caseNid -> (ConceptEntity) Entity.getFast(caseNid))
//                .collect(Collectors.toSet());

        return CASE_SIGNIFICANCE_OPTIONS;
    }
    public List<ConceptEntity> findAllModules(ViewProperties viewProperties) {
        try {
            Entity<? extends EntityVersion> moduleEntity = EntityService.get().getEntityFast(TinkarTerm.MODULE);
            IntIdSet moduleDescendents = viewProperties.calculator().descendentsOf(moduleEntity.nid());

            // get all descendant modules
            List<ConceptEntity> allModules =
                    moduleDescendents.intStream()
                            .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                            .toList();
            return allModules;
        } catch (Throwable th) {
            addValidator(MODULES_PROPERTY, "Module Entities", (Void prop, ViewModel vm) -> new ValidationMessage(MessageType.ERROR, "PrimitiveData services are not up. Attempting to retrieve ${%s}. Must call start().".formatted(MODULES_PROPERTY), th));
            return List.of();
        }
    }
    public List<ConceptEntity<ConceptEntityVersion>> findAllPaths(ViewProperties viewProperties) {
        try {
            //List of Concepts that represent available Paths in the data
            List<ConceptEntity<ConceptEntityVersion>> paths = new ArrayList<>();
            //Get all Path semantics from the Paths Pattern
            int[] pathSemanticNids = EntityService.get().semanticNidsOfPattern(TinkarTerm.PATHS_PATTERN.nid());
            //For each Path semantic get the concept that the semantic is referencing
            for (int pathSemanticNid : pathSemanticNids) {
                SemanticEntity<SemanticEntityVersion> semanticEntity = Entity.getFast(pathSemanticNid);
                int pathConceptNid = semanticEntity.referencedComponentNid();
                paths.add(EntityService.get().getEntityFast(pathConceptNid));
            }
            return paths;
        } catch (Throwable th) {
            addValidator(PATHS_PROPERTY, "Path Entities", (Void prop, ViewModel vm) -> new ValidationMessage(MessageType.ERROR, "PrimitiveData services are not up. Attempting to retrieve ${%s}. Must call start().".formatted(PATHS_PROPERTY), th));
            return List.of();
        }
    }

    public void updateFullyQualifiedName(PublicId publicId) {
        Transaction transaction = Transaction.make();

        StampEntity stampEntity = transaction.getStamp(
                State.fromConcept(getValue(STATUS)), // active, inactive, etc
                System.currentTimeMillis(),
                TinkarTerm.USER.nid(),
                ((ConceptEntity)getValue(MODULE)).nid(), // SNOMED CT, LOINC, etc
                TinkarTerm.DEVELOPMENT_PATH.nid()); //TODO should this path come from the parent concept's path?


        // existing semantic
        SemanticEntity theSemantic = EntityService.get().getEntityFast(publicId.asUuidList());


        // the versions that we will first populate with the existing versions of the semantic
        RecordListBuilder versions = RecordListBuilder.make();

        SemanticRecord descriptionSemantic = SemanticRecord.makeNew(publicId, TinkarTerm.DESCRIPTION_PATTERN.nid(),
                theSemantic.referencedComponentNid(), versions);

        // we are grabbing the form data
        // populating the field values for the new version we are writing
        MutableList<Object> descriptionFields = Lists.mutable.empty();
        descriptionFields.add(getValue(LANGUAGE));
        descriptionFields.add(getValue(NAME_TEXT));
        descriptionFields.add(getValue(CASE_SIGNIFICANCE));
        descriptionFields.add(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);

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
    }

    public DescrName create() {
        return new DescrName(getValue(PARENT_PUBLIC_ID),
                getValue(NAME_TEXT),
                getValue(NAME_TYPE),
                getValue(CASE_SIGNIFICANCE),
                getValue(STATUS),
                getValue(MODULE),
                getValue(LANGUAGE),
                getValue(SEMANTIC_PUBLIC_ID)
        );
    }

    public void updateData(DescrName editDescrName) {
        editDescrName.setParentConcept(getValue(PARENT_PUBLIC_ID));
        editDescrName.setNameText(getValue(NAME_TEXT));
        editDescrName.setNameType(getValue(NAME_TYPE));
        editDescrName.setCaseSignificance(getValue(CASE_SIGNIFICANCE));
        editDescrName.setStatus(getValue(STATUS));
        editDescrName.setModule(getValue(MODULE));
        editDescrName.setLanguage(getValue(LANGUAGE));
        editDescrName.setSemanticPublicId(getValue(SEMANTIC_PUBLIC_ID));
    }

    public void updateOtherName(PublicId publicId) {
        Transaction transaction = Transaction.make();

        StampEntity stampEntity = transaction.getStamp(
                State.fromConcept(getValue(STATUS)), // active, inactive, etc
                System.currentTimeMillis(),
                TinkarTerm.USER.nid(),
                ((ConceptEntity)getValue(MODULE)).nid(), // SNOMED CT, LOINC, etc
                TinkarTerm.DEVELOPMENT_PATH.nid()); //TODO should this path come from the parent concept's path?

        // existing semantic
        SemanticEntity theSemantic = EntityService.get().getEntityFast(publicId.asUuidList());


        // the versions that we will first populate with the existing versions of the semantic
        RecordListBuilder versions = RecordListBuilder.make();

        SemanticRecord descriptionSemantic = SemanticRecord.makeNew(publicId, TinkarTerm.DESCRIPTION_PATTERN.nid(),
                theSemantic.referencedComponentNid(), versions);

        // we grabbing the form data
        // populating the field values for the new version we are writing
        MutableList<Object> descriptionFields = Lists.mutable.empty();
        descriptionFields.add(getValue(LANGUAGE));
        descriptionFields.add(getValue(NAME_TEXT));
        descriptionFields.add(getValue(CASE_SIGNIFICANCE));
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
