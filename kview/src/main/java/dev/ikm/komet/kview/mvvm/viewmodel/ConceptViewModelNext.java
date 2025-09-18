package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.builder.AxiomBuilderRecord;
import dev.ikm.komet.framework.builder.ConceptEntityBuilder;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.*;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUS;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;

public class ConceptViewModelNext extends SimpleViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptViewModelNext.class);

    ///  ViewModel Property Keys
    public enum ConceptPropertyKeys {
        /// A newly added concept does not have a nid at the start.
        ///
        /// If new concept (true) we commit every associated created
        /// semantics ( e.g fqn / otherNames) in one go with the "same" stamp that we commit the concept for. This
        /// happens currently when the user adds a Axiom.
        ///
        /// Its also crucial that we first create a ValidStamp, because than we can create a initial stamp record and then
        /// use the then created ConceptEntity facades nid to create an set of Semantic NameDescription. Without a Concept
        /// Semantic descriptions would not know where to point to. E.g the problem would be that a new Semantic Description
        /// would not know what type fields it should provide (language, case_significance, modules etc) for selection as this is depending on the Concept?
        ///
        /// If new concept (false) for every change to a associated description semantic we create a new semantic record
        /// and directly commit it with in that moment generated stamp.
        THIS_IS_A_NEW_CONCEPT,

        THIS_CONCEPT_ENTITY_FACADE,
        ///  On window creation we got a uuid from our parent identifying our window. Only related to this window instance
        THIS_UNIQUE_CONCEPT_TOPIC,

        ASOCIATED_FQN_DESCRIPTION_SEMANTICS,
        ASOCIATED_OTHER_NAME_DESCRIPTION_SEMANTICS,

        ///  Either point to ourselves, to other fqn semantics or otherName (regular Name) semantics.
        ///
        /// If this is null we know that we need to create a new semantic from scratch in nameForm
        SELECTED_DESCRIPTION_SEMANTIC,

        // stuff from god
        VIEW_PROPERTIES,
        ///  Each journal window has a unique topic. E.g a uuid identifying our journal parent
        ASOCIATED_JOURNAL_WINDOW_TOPIC,

        SELECTED_PROPERTY_WINDOW_KIND,
        PROPERTY_WINDOW_OPEN,

        UNCOMMITED_CHANGES,

        HAS_VALID_STAMP,

        AXIOM,

    }

    // --- Property ValuesTypes ---

    ///  The current PaneType in Property View
    public enum SelectedPropertyWindowKind{
        STAMP,
        MENU,
        NAME_MENU,
        NAME_FORM,
        HISTORY,
        HIERARCHY,
        COMMENTS,
        NONE
    }

    public sealed interface SelectedDescriptionSemantic
            permits SemanticPublicId, UncommittedSemanticNameDescr {}

    public record SemanticPublicId(PublicId value) implements SelectedDescriptionSemantic {}
    public record UncommittedSemanticNameDescr(DescrName value) implements SelectedDescriptionSemantic {}


    public static String SUFFICIENT_SET = "Sufficient Set";
    public static String NECESSARY_SET = "Necessary Set";

    public ConceptViewModelNext() {
        addProperty(ConceptPropertyKeys.THIS_IS_A_NEW_CONCEPT, true)
                .addProperty(ConceptPropertyKeys.THIS_CONCEPT_ENTITY_FACADE, (EntityFacade) null)
                .addProperty(ConceptPropertyKeys.THIS_UNIQUE_CONCEPT_TOPIC, (UUID) null)
                .addProperty(ConceptPropertyKeys.ASOCIATED_FQN_DESCRIPTION_SEMANTICS, new ArrayList<DescrName>())
                .addProperty(ConceptPropertyKeys.ASOCIATED_OTHER_NAME_DESCRIPTION_SEMANTICS, new ArrayList<DescrName>())
                .addProperty(ConceptPropertyKeys.SELECTED_DESCRIPTION_SEMANTIC, (SelectedDescriptionSemantic) null)
                .addProperty(ConceptPropertyKeys.VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(ConceptPropertyKeys.ASOCIATED_JOURNAL_WINDOW_TOPIC, (UUID) null)
                .addProperty(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, (SelectedPropertyWindowKind) null)
                .addProperty(ConceptPropertyKeys.PROPERTY_WINDOW_OPEN, false)
                .addProperty(ConceptPropertyKeys.UNCOMMITED_CHANGES, false)
                .addProperty(ConceptPropertyKeys.HAS_VALID_STAMP, false)
                .addProperty(ConceptPropertyKeys.AXIOM, (String) null);

    }

    public ViewProperties getViewProperties() {
        return getPropertyValue(ConceptPropertyKeys.VIEW_PROPERTIES);
    }

    public void updateModel() {
        // TODO: entity facade && newConcept?

        // <BANNER>
        // identicon effected by, VIEW_PROPERTIES, THIS_CONCEPT_ENTITY_FACADE
        // stamp effected by, THIS_IS_A_NEW_CONCEPT HAS_VALID_STAMP THIS_CONCEPT_ENTITY_FACADE

        // <SEMANTIC DETAILS DESCRIPTION>
        // addButton effected by, HAS_VALID_STAMP
        // TODO: fqn displayed and otherName should be reactive to entityFacade changes
        // fqn displayed effected by, ASOCIATED_FQN_CONCEPTS, HAS_VALID_STAMP
        // otherName displayed effected by, ASOCIATED_OTHER_NAME_CONCEPTS, HAS_VALID_STAMP

        // <AXIOMS>


    }

    /**
     * Validates the view model and if there are no errors, save to the database.
     *
     * @return
     */
    public boolean createConcept(StampFormViewModelBase stampFormViewModel) {
        save(); // View Model xfer values. does not save to the database but validates data and then copies data from properties to model values.

        // Validation errors will not create record.
        // TODO: add new validator
//        if (!getValidationMessages().isEmpty()) {
//            return false;
//        }

        // stamp is populated?
        if (!(Boolean)stampFormViewModel.getPropertyValue(IS_CONFIRMED_OR_SUBMITTED)) {
            return false;
        }

        // Create concept
        List<DescrName> fqnList = getObservableList(ConceptPropertyKeys.ASOCIATED_FQN_DESCRIPTION_SEMANTICS); // TODO: this still assumes that on creation we can only have a FQN
        DescrName fqnDescrName = fqnList.get(0);
        Transaction transaction = Transaction.make("New concept for: " + fqnDescrName.getNameText());


        // Copy STAMP info
        // - status
        State status = stampFormViewModel.getValue(STATUS);
        // - getAuthor from editCoordinate
        ViewProperties viewProperties = getViewProperties();
        EntityFacade authorConcept = viewProperties.nodeView().editCoordinate().getAuthorForChanges();
        // - module
        ConceptEntity module = stampFormViewModel.getValue(MODULE);
        // - path
        ConceptEntity path = stampFormViewModel.getValue(PATH);

        StampEntity stampEntity = transaction.getStamp(status, authorConcept.nid(),
                module.nid(), path.nid());

        ConceptEntityBuilder newConceptBuilder = ConceptEntityBuilder.builder(stampEntity);


        PublicId conceptPublicId = PublicIds.newRandom();
        ConceptRecord conceptRecord = ConceptRecord.build(conceptPublicId.asUuidList().get(0), stampEntity.lastVersion());

        ConceptFacade conceptFacade = EntityProxy.Concept.make(conceptRecord.publicId()) ;

        // add the Fully Qualified Name to the new concept
        saveFQNwithinCreateConcept(transaction, stampEntity, fqnDescrName, conceptFacade);


        AxiomBuilderRecord ab = newConceptBuilder.axiomBuilder();

        // determine sufficient or necessary
        if (NECESSARY_SET.equals(getValue(ConceptPropertyKeys.AXIOM))) {
            ab.withNecessarySet(
//                    ab.makeConceptReference(TinkarTerm.LANGUAGE),
//                    ab.makeConceptReference(TinkarTerm.DESCRIPTION_ASSEMBLAGE),
                    ab.makeRoleGroup(
                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.ANONYMOUS_CONCEPT)
                            /*ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.LANGUAGE)*/)
            );
        } else if (SUFFICIENT_SET.equals(getValue(ConceptPropertyKeys.AXIOM))) {
            ab.withSufficientSet(
//                    ab.makeConceptReference(TinkarTerm.LANGUAGE),
//                    ab.makeConceptReference(TinkarTerm.DESCRIPTION_ASSEMBLAGE),
                    ab.makeRoleGroup(
                            ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.ANONYMOUS_CONCEPT)
                            /*ab.makeSome(TinkarTerm.PART_OF, TinkarTerm.LANGUAGE)*/));
        }

        // add the axiom
        buildAxiom(ab, conceptRecord, stampEntity, transaction);

        // TODO: clean up this type mess ...
        List<DescrName> otherNames = (List<DescrName>) getValueMap().get(ConceptPropertyKeys.ASOCIATED_OTHER_NAME_DESCRIPTION_SEMANTICS);
        if (otherNames.size() > 0) {
            // if there are other names defined, then add them to the newly created concept
            saveOtherNameWithinCreateConcept(transaction, stampEntity, otherNames, conceptFacade);
        }

        transaction.addComponent(conceptRecord);
        Entity.provider().putEntity(conceptRecord);

        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        try {
            TinkExecutor.threadPool().submit(commitTransactionTask).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        String pathText = getViewProperties().calculator().getDescriptionTextOrNid(path.nid());
        String moduleText = getViewProperties().calculator().getDescriptionTextOrNid(module.nid());
        String statusText = getViewProperties().calculator().getDescriptionTextOrNid(status.nid());

        // alert the user of the concept being created and were it exists
        JournalController.toast()
                .show(
                        Toast.Status.SUCCESS,
                        String.format("Concept created %s, %s, %s", pathText, moduleText, statusText)
                );

        // place inside as current Concept
        // TODO: ;(
        setValue(ConceptPropertyKeys.THIS_CONCEPT_ENTITY_FACADE, conceptFacade);
        setPropertyValue(ConceptPropertyKeys.THIS_CONCEPT_ENTITY_FACADE, conceptFacade);
        setValue(ConceptPropertyKeys.THIS_IS_A_NEW_CONCEPT, false);
        setPropertyValue(ConceptPropertyKeys.THIS_IS_A_NEW_CONCEPT, false);
        return true;
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

    private void buildAxiom(AxiomBuilderRecord axiomBuilder, ConceptRecord conceptRecord, StampEntity stampEntity, Transaction transaction) {
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
        transaction.addComponent(statedAxioms);
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


}
