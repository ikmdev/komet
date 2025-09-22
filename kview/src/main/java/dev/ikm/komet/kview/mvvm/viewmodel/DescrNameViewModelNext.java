package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PublicIdService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.*;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;

import dev.ikm.tinkar.terms.ComponentWithNid;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.tinkar.terms.TinkarTerm.*;


public class DescrNameViewModelNext extends ValidationViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(DescrNameViewModelNext.class);

    public enum DescrPropKeys {
        ///  displayable Name
        NAME,

        SELECTED_NAME_TYPE,
        /// all possible associated name types
        NAME_TYPE_VARIANTS, // ( FQN or otherName ? )

        SELECTED_CASE_SIGNIFICANCE,
        /// all possible associated case types
        CASE_SIGNIFICANCE_VARIANTS,

        SELECTED_STATUS,
        /// all possible associated statuses
        STATUS_VARIANTS,

        SELECTED_MODULE,
        /// all possible associated modules
        MODULE_VARIANTS,

        SELECTED_LANGUAGE,
        /// all possible associated languages
        LANGUAGE_VARIANTS,

        // TODO: check if the following are needed in this model
        /// the public ID of the description semantic that this class represents
        SEMANTIC_PUBLIC_ID,
        /// the public ID of the parent concept of this description semantic
        PARENT_CONCEPT_ID,

        // TODO: this are old ones used before
        // used to display the title of the add/edit screen?
        TITLE_TEXT,

        VIEW_PROPERTIES,
    }

    public DescrNameViewModelNext (ViewProperties viewProperties) {


        addProperty(DescrPropKeys.NAME, (String) null)
                .addProperty(DescrPropKeys.SELECTED_NAME_TYPE, (ComponentWithNid) null)
                .addProperty(DescrPropKeys.NAME_TYPE_VARIANTS, Collections.emptyList(), true)

                .addProperty(DescrPropKeys.SELECTED_CASE_SIGNIFICANCE, (ComponentWithNid) null)
                .addProperty(DescrPropKeys.CASE_SIGNIFICANCE_VARIANTS, Collections.emptyList(), true)

                .addProperty(DescrPropKeys.SELECTED_STATUS, (ComponentWithNid) null)
                .addProperty(DescrPropKeys.STATUS_VARIANTS, Collections.emptyList(), true)

                .addProperty(DescrPropKeys.SELECTED_MODULE, (ComponentWithNid) null)
                .addProperty(DescrPropKeys.MODULE_VARIANTS, Collections.emptyList(), true)

                .addProperty(DescrPropKeys.SELECTED_LANGUAGE, (ComponentWithNid) null)
                .addProperty(DescrPropKeys.LANGUAGE_VARIANTS, Collections.emptyList(), true)

                .addProperty(DescrPropKeys.VIEW_PROPERTIES, (ViewProperties) viewProperties)
                .addProperty(DescrPropKeys.TITLE_TEXT, (String) "hello world");

        // This works only for a DescNameViewModel that works ontop of a concept
        fetchSemanticFieldChoicesViaParentConcept(viewProperties);

        // run validators when the following properties change.
        doOnChange(this::validate,
                DescrPropKeys.NAME,
                DescrPropKeys.SELECTED_NAME_TYPE,
                DescrPropKeys.SELECTED_CASE_SIGNIFICANCE,
                DescrPropKeys.SELECTED_STATUS,
                DescrPropKeys.SELECTED_MODULE,
                DescrPropKeys.SELECTED_LANGUAGE
        );



    }

    ///  Copy View values into the Model
    @Override
    public DescrNameViewModelNext save() {
        LOG.info("Copy: View State --> Model state");
        super.save();

        return this;
    }

    /// Copy Model values into the View
    @Override
    public DescrNameViewModelNext reset() {
        LOG.info("Copy: View State <-- Model state");
        super.reset();

        return this;
    }


    private void fetchSemanticFieldChoicesViaParentConcept(ViewProperties viewProperties) {
        Set<ConceptEntity> nameTypes = fetchDescendentsOfConcept(viewProperties, TinkarTerm.DESCRIPTION_TYPE.publicId());
        Set<ConceptEntity> caseSignificances = fetchDescendentsOfConcept(viewProperties, DESCRIPTION_CASE_SIGNIFICANCE.publicId());
        Set<ConceptEntity> states = fetchDescendentsOfConcept(viewProperties, TinkarTerm.STATUS_VALUE.publicId());
        Set<ConceptEntity> languages = fetchDescendentsOfConcept(viewProperties, TinkarTerm.LANGUAGE.publicId());
        Set<ConceptEntity> modules = fetchDescendentsOfConcept(viewProperties, TinkarTerm.MODULE.publicId());

        setPropertyValues(DescrPropKeys.NAME_TYPE_VARIANTS, nameTypes);
        setPropertyValues(DescrPropKeys.CASE_SIGNIFICANCE_VARIANTS, caseSignificances);
        setPropertyValues(DescrPropKeys.STATUS_VARIANTS, states);
        setPropertyValues(DescrPropKeys.LANGUAGE_VARIANTS, languages);
        setPropertyValues(DescrPropKeys.MODULE_VARIANTS, modules);
    }


    ///  Create a new semantic that points to a Concept derived from the provided ViewProperties
    ///
    ///  Notice: should only be used on ViewProperties that actually include a "parent" Concept
    public void createNewSemantic(ViewProperties viewProperties) {
        fetchSemanticFieldChoicesViaParentConcept(viewProperties);

        setPropertyValue(DescrPropKeys.NAME, (String) null);
        setPropertyValue(DescrPropKeys.SELECTED_NAME_TYPE, (ComponentWithNid) null);
        setPropertyValue(DescrPropKeys.SELECTED_CASE_SIGNIFICANCE, (ComponentWithNid) null);
        setPropertyValue(DescrPropKeys.SELECTED_STATUS, (ComponentWithNid) null);
        setPropertyValue(DescrPropKeys.SELECTED_MODULE, (ComponentWithNid) null);
        setPropertyValue(DescrPropKeys.SELECTED_LANGUAGE, (ComponentWithNid) null);
    }

    /// Update a semantic
    ///
    /// Useful when creating a new Concept and the Concept was not commited
    ///
    /// @param descrName provides the semantic that wants to be updated
    public void updateNonCommitedSemantic(DescrName descrName, ViewProperties viewProperties) {
        fetchSemanticFieldChoicesViaParentConcept(viewProperties);

        setPropertyValue(DescrPropKeys.NAME, (String) descrName.getNameText());
        setPropertyValue(DescrPropKeys.SELECTED_NAME_TYPE, (ComponentWithNid) descrName.getNameType());
        setPropertyValue(DescrPropKeys.SELECTED_CASE_SIGNIFICANCE, (ComponentWithNid) descrName.getCaseSignificance());
        setPropertyValue(DescrPropKeys.SELECTED_STATUS, (ComponentWithNid) descrName.getStatus());
        setPropertyValue(DescrPropKeys.SELECTED_MODULE, (ComponentWithNid) descrName.getModule());
        setPropertyValue(DescrPropKeys.SELECTED_LANGUAGE, (ComponentWithNid) descrName.getLanguage());
    }


    public void updateExistingSemantic(PublicId semanticNameId, ViewProperties viewProperties) {
        int semanticNid = EntityService.get().nidForPublicId(semanticNameId);
        Latest<SemanticEntityVersion> semanticEntityVersionLatest = viewProperties.calculator().latest(semanticNid);
        SemanticEntityVersion bla = semanticEntityVersionLatest.get();
        LOG.info("SemanticEntityVersion we got on updatingExistingSemantic call");
        LOG.info(bla.toString());
    }


    public void updateFullyQualifiedName(PublicId publicId, ViewProperties viewProperties) {
        EntityProxy.Concept semanticType = TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;

        updateSemanticName(publicId, viewProperties , semanticType);
        LOG.info("transaction complete");
    }

    public void updateOtherName(PublicId publicId, ViewProperties viewProperties) {
        EntityProxy.Concept semanticType =  TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;

        updateSemanticName(publicId, viewProperties , semanticType);
        LOG.info("transaction complete");
    }

    private void updateSemanticName(PublicId publicId, ViewProperties viewProperties, EntityProxy.Concept semanticType) {
        Transaction transaction = Transaction.make();

        StampEntity stampEntity = transaction.getStamp(
                State.fromConcept(getValue(DescrPropKeys.SELECTED_STATUS)), // active, inactive, etc
                System.currentTimeMillis(),
                viewProperties.nodeView().editCoordinate().getAuthorForChanges().nid(),
                ((ConceptEntity)getValue(DescrPropKeys.SELECTED_MODULE)).nid(), // SNOMED CT, LOINC, etc
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
        descriptionFields.add(getValue(DescrPropKeys.SELECTED_LANGUAGE));
        descriptionFields.add(getValue(DescrPropKeys.NAME));
        descriptionFields.add(getValue(DescrPropKeys.SELECTED_CASE_SIGNIFICANCE));
        descriptionFields.add(semanticType);

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

}
