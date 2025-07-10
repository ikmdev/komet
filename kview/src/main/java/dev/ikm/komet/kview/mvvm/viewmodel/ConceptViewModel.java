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

import dev.ikm.komet.framework.builder.AxiomBuilderRecord;
import dev.ikm.komet.framework.builder.ConceptEntityBuilder;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.*;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import org.carlfx.cognitive.validator.MessageType;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.NAME_TYPE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;

public class ConceptViewModel extends FormViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptViewModel.class);

    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String CURRENT_ENTITY = "entityFacade";
    public static String FULLY_QUALIFIED_NAME = "fqn";
    public static String CONCEPT_STAMP_VIEW_MODEL = "stampViewModel";
    public static String OTHER_NAMES = "otherNames";

    public static String AXIOM = "axiom";
    // Axiom values
    public static String SUFFICIENT_SET = "Sufficient Set";
    public static String NECESSARY_SET = "Necessary Set";


    public ConceptViewModel() {
        super(); // addProperty(MODE, VIEW); By default
        addProperty(CURRENT_ENTITY, (EntityFacade) null)
                .addProperty(FULLY_QUALIFIED_NAME, (Object) null)
                .addProperty(OTHER_NAMES, (Collection) new ArrayList<>())
                .addProperty(CONCEPT_STAMP_VIEW_MODEL, (ViewModel) null)
                .addProperty(AXIOM, (String) null);

        //FIXME add a STAMP validator

        // In Create Mode the fqn is required.
        addValidator(FULLY_QUALIFIED_NAME, "Fully Qualified Name",(ReadOnlyObjectProperty prop, ViewModel vm) -> {
            if (prop.isNull().get()
                    || prop.get() instanceof DescrNameViewModel fqnViewModel
                    && fqnViewModel.getPropertyValue(NAME_TYPE) != TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE
                    && fqnViewModel.getPropertyValue(NAME_TEXT) != null
                    && fqnViewModel.getPropertyValue(NAME_TEXT).toString().isBlank()) {

                return new ValidationMessage(FULLY_QUALIFIED_NAME, MessageType.ERROR, "${%s} is required".formatted(FULLY_QUALIFIED_NAME));
            }
            return VALID;
        });

        // Axiom should be selected
        addValidator(AXIOM, "Axiom",(ReadOnlyStringProperty prop, ViewModel vm) -> {
            if (prop.isNull().get()
                    || (prop.get() instanceof String axiom
                    && !(SUFFICIENT_SET.equals(axiom) || NECESSARY_SET.equals(axiom)))) {
                return new ValidationMessage(AXIOM, MessageType.ERROR, "${%s} is required and must be a %s or %s. Axiom = %s".formatted(AXIOM, SUFFICIENT_SET, NECESSARY_SET, prop.get()));
            }
            return VALID;
        });

//        addValidator("isReadyToCreate", (vm) ->
//                vm.getPropertyValue("mode").equals(CREATE)
//                        && vm.getPropertyValue("axiom").isEmpty()
//                        && vm.getPropertyValue("fqn").isEmpty()
//                        ? new ValidationMessage(ERROR, "Must have a ${fqn} & ${axiom} to create")
//                        : VALID);


    }

    /**
     * Validates the view model and if there are no errors, save to the database.
     * Is called everytime user is adding data to ConceptViewModel.
     *
     * @param editCoordinate
     * @return
     */
    public boolean createConcept(EditCoordinateRecord editCoordinate) {
        save(); // View Model xfer values. does not save to the database but validates data and then copies data from properties to model values.

        // Validation errors will not create record.
        if (!getValidationMessages().isEmpty()) {
            return false;
        }

        // stamp exists and is populated?
        StampViewModel stampViewModel = getValue(CONCEPT_STAMP_VIEW_MODEL);
        if (stampViewModel != null) {
            stampViewModel.save(); // View Model xfer values
            if (!stampViewModel.getValidationMessages().isEmpty()) {
                return false;
            }
        } else {
            return false;
        }

        // Create concept
        DescrName fqnDescrName = getPropertyValue(FULLY_QUALIFIED_NAME);
        Transaction transaction = Transaction.make("New concept for: " + fqnDescrName.getNameText());

        // Copy STAMP info
        ConceptEntity module = stampViewModel.getValue(MODULE);
        ConceptEntity path = stampViewModel.getValue(PATH);

        StampEntity stampEntity = transaction.getStamp(State.ACTIVE, editCoordinate.getAuthorNidForChanges(),
                module.nid(), path.nid());

        ConceptEntityBuilder newConceptBuilder = ConceptEntityBuilder.builder(stampEntity);


        PublicId conceptPublicId = PublicIds.newRandom();
        ConceptRecord conceptRecord = ConceptRecord.build(conceptPublicId.asUuidList().get(0), stampEntity.lastVersion());

        ConceptFacade conceptFacade = EntityProxy.Concept.make(conceptRecord.publicId()) ;

        // add the Fully Qualified Name to the new concept
        saveFQNwithinCreateConcept(transaction, stampEntity, getValue(FULLY_QUALIFIED_NAME), conceptFacade);


        AxiomBuilderRecord ab = newConceptBuilder.axiomBuilder();

        // determine sufficient or necessary
        if (NECESSARY_SET.equals(getValue(AXIOM))) {
            ab.withNecessarySet(
//                    ab.makeConceptReference(TinkarTerm.LANGUAGE),
//                    ab.makeConceptReference(TinkarTerm.DESCRIPTION_ASSEMBLAGE),
                    ab.makeRoleGroup(
                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.ANONYMOUS_CONCEPT)
                            /*ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.LANGUAGE)*/)
            );
        } else if (SUFFICIENT_SET.equals(getValue(AXIOM))) {
            ab.withSufficientSet(
//                    ab.makeConceptReference(TinkarTerm.LANGUAGE),
//                    ab.makeConceptReference(TinkarTerm.DESCRIPTION_ASSEMBLAGE),
                    ab.makeRoleGroup(
                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.ANONYMOUS_CONCEPT)
                            /*ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.LANGUAGE)*/));
        }

        // add the axiom
        buildAxiom(ab, conceptRecord, stampEntity);

        List<DescrName> otherNames = (List<DescrName>) getValueMap().get(OTHER_NAMES);
        if (otherNames.size() > 0) {
            // if there are other names defined, then add them to the newly created concept
            saveOtherNameWithinCreateConcept(transaction, stampEntity, otherNames, conceptFacade);
        }

        Entity.provider().putEntity(conceptRecord);

        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);
        // place inside as current Concept
        setValue(CURRENT_ENTITY, conceptFacade);
        setPropertyValue(CURRENT_ENTITY, conceptFacade);
        setValue(MODE, EDIT);
        setPropertyValue(MODE, EDIT);
        return true;
    }

    private void buildAxiom(AxiomBuilderRecord axiomBuilder, ConceptRecord conceptRecord,  StampEntity stampEntity) {
        DiTreeEntity.Builder axiomTreeEntityBuilder = DiTreeEntity.builder();
        EntityVertex rootVertex = EntityVertex.make(axiomBuilder);
        axiomTreeEntityBuilder.setRoot(rootVertex);
        recursiveAddChildren(axiomTreeEntityBuilder, rootVertex, axiomBuilder);

        ImmutableList<Object> axiomField = Lists.immutable.of(axiomTreeEntityBuilder.build());
        SemanticRecord statedAxioms = SemanticRecord.build(UUID.randomUUID(),
                TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
                conceptRecord.nid(),
                stampEntity.lastVersion(),
                axiomField);
        Entity.provider().putEntity(statedAxioms);
    }

    private void recursiveAddChildren(DiTreeEntity.Builder axiomTreeBuilder, EntityVertex parentVertex, AxiomBuilderRecord parentAxiom) {
        for (AxiomBuilderRecord child : parentAxiom.children()) {
            EntityVertex childVertex = EntityVertex.make(child);
            axiomTreeBuilder.addVertex(childVertex);
            axiomTreeBuilder.addEdge(childVertex, parentVertex);
            recursiveAddChildren(axiomTreeBuilder, childVertex, child);
        }
    }

    private void saveFQNwithinCreateConcept(Transaction transaction, StampEntity stampEntity, DescrName fqnNameDescr, ConceptFacade conceptFacade) {

        // create a new public id for the FQN semantic
        PublicId fqnPublicId = PublicIds.of(UUID.randomUUID());

        // the versions that we will first populate with the existing versions of the semantic
        RecordListBuilder versions = RecordListBuilder.make();

        SemanticRecord descriptionSemantic = SemanticRecord.makeNew(fqnPublicId, TinkarTerm.DESCRIPTION_PATTERN.nid(),
                conceptFacade.nid(), versions);

        // we are grabbing the form data
        // populating the field values for the new version we are writing
        MutableList<Object> descriptionFields = Lists.mutable.empty();

        // get these from the view model
        descriptionFields.add(fqnNameDescr.getLanguage());
        descriptionFields.add(fqnNameDescr.getNameText());
        descriptionFields.add(fqnNameDescr.getCaseSignificance());
        descriptionFields.add(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);


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
    }

    private void saveOtherNameWithinCreateConcept(Transaction transaction, StampEntity stampEntity, List<DescrName> otherNames, ConceptFacade conceptFacade) {

        otherNames.forEach(descrName -> {
            //vm.save();

            descrName.setParentConcept(conceptFacade.publicId());

            PublicId otherNamePublicId = PublicIds.of(UUID.randomUUID()); /////  update the VM with our new public ID
            descrName.setSemanticPublicId(otherNamePublicId);

            // the versions that we will first populate with the existing versions of the semantic
            RecordListBuilder versions = RecordListBuilder.make();

            SemanticRecord descriptionSemantic = SemanticRecord.makeNew(otherNamePublicId, TinkarTerm.DESCRIPTION_PATTERN.nid(),
                    conceptFacade.nid(), versions);

            // we are grabbing the form data
            // populating the field values for the new version we are writing
            MutableList<Object> descriptionFields = Lists.mutable.empty();
            descriptionFields.add(descrName.getLanguage());
            descriptionFields.add(descrName.getNameText());
            descriptionFields.add(descrName.getCaseSignificance());
            descriptionFields.add(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);

            // iterating over the existing versions and adding them to a new record list builder
            descriptionSemantic.versions().forEach(version -> versions.add(version));

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
        });
    }

    public void addOtherName(EditCoordinateRecord editCoordinateRecord, DescrName otherName) {

        Transaction transaction = Transaction.make();

        StampEntity stampEntity = transaction.getStamp(
                State.fromConcept(otherName.getStatus()), // active, inactive, etc
                System.currentTimeMillis(),
                TinkarTerm.USER.nid(),
                otherName.getModule().nid(), // SNOMED CT, LOINC, etc
                TinkarTerm.DEVELOPMENT_PATH.nid()); // Should this be defaulted???

        // get the public id of the referenced concept
        PublicId conceptRecordPublicId =  otherName.getParentConcept();

        int conceptNid = EntityService.get().nidForPublicId(conceptRecordPublicId);

        // the versions that we will first populate with the existing versions of the semantic
        RecordListBuilder versions = RecordListBuilder.make();

        // the new semantic will need a new public id
        PublicId newOtherNamePublicId = PublicIds.newRandom();

        SemanticRecord descriptionSemantic = SemanticRecord.makeNew(newOtherNamePublicId, TinkarTerm.DESCRIPTION_PATTERN.nid(),
                conceptNid, versions);

        // we are grabbing the form data
        // populating the field values for the new version we are writing
        MutableList<Object> descriptionFields = Lists.mutable.empty();
        descriptionFields.add(otherName.getLanguage());
        descriptionFields.add(otherName.getNameText());
        descriptionFields.add(otherName.getCaseSignificance());
        descriptionFields.add(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);

        // iterating over the existing versions and adding them to a new record list builder
        descriptionSemantic.versions().forEach(version -> versions.add(version));

        // adding the new (edit form) version here
        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(descriptionSemantic)
                .stampNid(stampEntity.nid())
                .fieldValues(descriptionFields.toImmutable())
                .build());

        // apply the updated versions to the new semantic record
        SemanticRecord newSemanticRecord = SemanticRecordBuilder.builder(descriptionSemantic).versions(versions.toImmutable()).build();

        otherName.setSemanticPublicId(newSemanticRecord.publicId());
        // put the new semantic record in the transaction
        transaction.addComponent(newSemanticRecord);

        // perform the save
        Entity.provider().putEntity(newSemanticRecord);

        // commit the transaction
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);

        LOG.info("transaction complete");
    }

    public ViewProperties getViewProperties() {
        return getPropertyValue(VIEW_PROPERTIES);
    }

}