package dev.ikm.komet.framework.builder;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ConceptEntityBuilder {
    private final StampEntity stampEntity;
    MutableList<DescriptionBuilderRecord> descriptionsToBuild = Lists.mutable.empty();
    AxiomBuilderRecord axiomBuilder;
    Transaction transaction;

    public ConceptEntityBuilder(StampEntity stampEntity) {
        this.stampEntity = stampEntity;
        Transaction.forStamp(stampEntity).ifPresentOrElse(transaction1 -> transaction = transaction1, () -> {
            throw new IllegalStateException("No transaction for stamp: " + stampEntity);
        });
    }

    public static ConceptEntityBuilder builder(StampEntity stampEntity) {
        return new ConceptEntityBuilder(stampEntity);
    }

    public ConceptEntityBuilder makeRegularName(String newConceptText) {
        return with(DescriptionBuilderRecord.makeRegularName(newConceptText));
    }

    public ConceptEntityBuilder with(DescriptionBuilderRecord descriptionRecord) {
        descriptionsToBuild.add(descriptionRecord);
        return this;
    }

    public AxiomBuilderRecord axiomBuilder() {
        if (axiomBuilder == null) {
            axiomBuilder = new AxiomBuilderRecord(new AtomicInteger());
        }
        return axiomBuilder;
    }

    /**
     * @return list of built entities, starting with the top enclosing component (the concept).
     */
    public ImmutableList<EntityFacade> build() {
        MutableList<EntityFacade> entities = Lists.mutable.empty();
        ConceptRecord concept = ConceptRecord.build(UUID.randomUUID(), stampEntity.lastVersion());
        processEntity(entities, concept);
        for (DescriptionBuilderRecord descriptionToBuild : descriptionsToBuild) {
            /*
SemanticRecord{<-2146949296> [e868e2ad-fcfe-3841-b676-44466832d331], of pattern: «Description pattern <-2147483633> [a4de0039-2625-5842-8a4c-d1ce6aebf021]», rc: «Fallot trilogy <-2142762195> [02ec8012-d6ca-32f5-9d1b-1571aefc208d]»,
v: ≤s:Active t:2002-01-30 16:00 a:User m:SNOMED CT core p:Development path
Field 1: ‹Language for description: English language›
Field 2: ‹Text: Fallot's trilogy (disorder)›
Field 3: ‹Description case significance: Initial character case insensitive›
Field 4: ‹Description type: Fully specified name›
≥}
             */

            ImmutableList<Object> descriptionFields = Lists.immutable.of(descriptionToBuild.language(),
                    descriptionToBuild.text(),
                    descriptionToBuild.caseSensitivity(),
                    descriptionToBuild.descriptionType());
            SemanticRecord description = SemanticRecord.build(UUID.randomUUID(),
                    TinkarTerm.DESCRIPTION_PATTERN.nid(),
                    concept.nid(),
                    stampEntity.lastVersion(),
                    descriptionFields);
            processEntity(entities, description);
            for (AcceptabilityRecord acceptabilityRecord : descriptionToBuild.acceptabilityRecords()) {
/*
SemanticRecord{<-2146572527> [96951270-10d5-5ed1-a289-2f5f0c5411eb], of pattern: «US Dialect Pattern <-2147483638> [08f9112c-c041-56d3-b89b-63258f070074]», rc: «<-2146572526> <-2146572526> [55eef32b-4800-3aca-ad92-329373c62700]»,
v: ≤s:Active t:2002-01-30 16:00 a:User m:SNOMED CT core p:Development path
Field 1: ‹US English: Preferred›
≥}
 */
                ImmutableList<Object> acceptabilityFields = Lists.immutable.of(acceptabilityRecord.acceptabilityValue());
                SemanticRecord acceptability = SemanticRecord.build(UUID.randomUUID(),
                        acceptabilityRecord.acceptabilityPattern().nid(),
                        description.nid(),
                        stampEntity.lastVersion(),
                        acceptabilityFields);
                processEntity(entities, acceptability);
            }
        }

        if (axiomBuilder != null) {
            // Construct tree entity...
            DiTreeEntity.Builder axiomTreeEntityBuilder = DiTreeEntity.builder();
            EntityVertex rootVertex = EntityVertex.make(axiomBuilder);
            axiomTreeEntityBuilder.setRoot(rootVertex);
            recursiveAddChildren(axiomTreeEntityBuilder, rootVertex, axiomBuilder);

            ImmutableList<Object> axiomField = Lists.immutable.of(axiomTreeEntityBuilder.build());
            SemanticRecord statedAxioms = SemanticRecord.build(UUID.randomUUID(),
                    TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
                    concept.nid(),
                    stampEntity.lastVersion(),
                    axiomField);

            processEntity(entities, statedAxioms);
        }

        return entities.toImmutable();
    }

    private void recursiveAddChildren(DiTreeEntity.Builder axiomTreeBuilder, EntityVertex parentVertex, AxiomBuilderRecord parentAxiom) {
        for (AxiomBuilderRecord child : parentAxiom.children()) {
            EntityVertex childVertex = EntityVertex.make(child);
            axiomTreeBuilder.addVertex(childVertex);
            axiomTreeBuilder.addEdge(childVertex, parentVertex);
            recursiveAddChildren(axiomTreeBuilder, childVertex, child);
        }
    }

    private void processEntity(MutableList<EntityFacade> entities, Entity<? extends EntityVersion> entity) {
        this.transaction.addComponent(entity);
        Entity.provider().putEntity(entity);
        entities.add(entity);
    }
}
