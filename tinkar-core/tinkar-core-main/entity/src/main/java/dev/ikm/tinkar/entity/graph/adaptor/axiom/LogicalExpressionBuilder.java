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
package dev.ikm.tinkar.entity.graph.adaptor.axiom;

import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.isomorphic.SetElementKey;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import java.util.UUID;

/**
 *
 */
public class LogicalExpressionBuilder {

    LogicalExpression logicalExpression;
    DiTreeEntity.Builder builder;
    final int rootIndex;

    static UUID generateRandomUuid() {
        return UuidT5Generator.get(UuidT5Generator.STAMP_NAMESPACE, Thread.currentThread().threadId() + "-" + System.nanoTime());
    }

    public LogicalExpressionBuilder(UUID rootVertexUuid) {
        this.builder = DiTreeEntity.builder();
        this.logicalExpression = new LogicalExpression(builder);
        EntityVertex rootVertex = EntityVertex.make(rootVertexUuid, LogicalAxiomSemantic.DEFINITION_ROOT.nid);
        builder.addVertex(rootVertex);
        builder.setRoot(rootVertex);
        this.rootIndex = rootVertex.vertexIndex();
        new LogicalAxiomAdaptor.DefinitionRootAdaptor(logicalExpression, rootIndex);

    }

    public LogicalExpressionBuilder() {
        this(generateRandomUuid());
    }

    public LogicalExpressionBuilder(LogicalExpression logicalExpression) {
        this.builder = DiTreeEntity.builder(logicalExpression.sourceGraph);
        this.rootIndex = logicalExpression.definitionRoot().vertexIndex();
        this.logicalExpression = new LogicalExpression(this.builder);
    }

    public LogicalExpressionBuilder(DiTreeEntity logicalExpressionTree) {
        this.builder = DiTreeEntity.builder(logicalExpressionTree);
        this.rootIndex = logicalExpressionTree.root().vertexIndex();
        this.logicalExpression = new LogicalExpression(this.builder);
    }

    enum NormalizeResult {
        NO_CHANGES,
        NODES_REMOVED
    }

    public NormalizeResult normalize() {
        NormalizeResult result = NormalizeResult.NO_CHANGES;
        // Check for multiple necessary sets.
        ImmutableList<LogicalAxiom.LogicalSet.NecessarySet> necessarySets = logicalExpression.nodesOfType(LogicalAxiom.LogicalSet.NecessarySet.class);
        if (necessarySets.size() > 1) {
            MutableList<LogicalAxiom> duplicateAxioms = Lists.mutable.empty();
            MutableList<LogicalAxiom> axiomsToRetain = Lists.mutable.empty();
            MutableSet<SetElementKey> usedKeys = Sets.mutable.empty();
            DiTreeEntity diTree = builder.build();
            LogicalAxiom.LogicalSet.NecessarySet setToSave = null;
            LogicalAxiom.Atom.Connective connectiveForMerge = null;

            for (LogicalAxiom.LogicalSet.NecessarySet necessarySet : necessarySets) {
                for (LogicalAxiom.Atom.Connective connective : necessarySet.elements()) {
                    if (setToSave == null) {
                        setToSave = necessarySet;
                        connectiveForMerge = connective;
                    } else {
                        duplicateAxioms.add(necessarySet);
                        duplicateAxioms.add(connective);
                    }
                    connective.elements().forEach(element -> {
                        SetElementKey key = new SetElementKey(element.vertexIndex(), diTree);
                        if (usedKeys.contains(key)) {
                            duplicateAxioms.add(element);
                        } else {
                            axiomsToRetain.add(element);
                            usedKeys.add(key);
                        }
                    });
                }
            }
            // Modify the graph...
            final int connectiveIndex = connectiveForMerge.vertexIndex();
            axiomsToRetain.forEach(axiom -> {
                builder.predecessorIndex(axiom.vertexIndex()).ifPresent(predecessorIndex -> {
                    if (predecessorIndex != connectiveIndex) {
                        builder.setPredecessorIndex(axiom.vertexIndex(), connectiveIndex);
                    }
                });
            });
            if (duplicateAxioms.notEmpty()) {
                result = NormalizeResult.NODES_REMOVED;
            }
            duplicateAxioms.forEach(axiom -> builder.removeNotRecursive(axiom.vertexIndex()));
        }
        return result;
    }

    public LogicalExpression build() {
        NormalizeResult result = normalize();
        if (result == NormalizeResult.NODES_REMOVED) {
            builder.compress();
        }
        this.logicalExpression = new LogicalExpression(this.builder);
        return logicalExpression.build();
    }

    public LogicalAxiom get(int axiomIndex) {
        return logicalExpression.adaptors.get(axiomIndex);
    }

    /**
     * NOTE: Not thread safe...
     *
     * @param axiomToRemove
     * @return A newly constructed logical expression with the axiom recursively removed.
     */
    public LogicalExpressionBuilder removeAxiom(LogicalAxiom axiomToRemove) {
        if (this.rootIndex == axiomToRemove.vertexIndex()) {
            throw new IllegalStateException("Removing root vertex is not allowed. ");
        }
        DiTreeEntity axiomTree = this.builder.build();
        this.builder = axiomTree.removeVertex(axiomToRemove.vertexIndex());
        this.logicalExpression = new LogicalExpression(this.builder);
        return this;
    }

    public LogicalExpressionBuilder addToRoot(LogicalAxiom.LogicalSet logicalSet) {
        builder.addEdge(logicalSet.vertexIndex(), this.rootIndex);
        return this;
    }

    public LogicalAxiom.LogicalSet.SufficientSet SufficientSet(LogicalAxiom.Atom... elements) {
        return SufficientSet(generateRandomUuid(), elements);
    }

    public LogicalAxiom.LogicalSet.SufficientSet SufficientSet(UUID vertexUuid, LogicalAxiom.Atom... elements) {
        EntityVertex sufficientSet = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.SUFFICIENT_SET.nid);
        builder.addVertex(sufficientSet);
        builder.addEdge(sufficientSet.vertexIndex(), rootIndex);
        for (LogicalAxiom.Atom element : elements) {
            builder.addEdge(element.vertexIndex(), sufficientSet.vertexIndex());
        }
        return new LogicalAxiomAdaptor.SufficientSetAdaptor(logicalExpression, sufficientSet.vertexIndex());
    }

    public LogicalAxiom.LogicalSet.NecessarySet NecessarySet(LogicalAxiom.Atom... elements) {
        return NecessarySet(generateRandomUuid(), elements);
    }

    public LogicalAxiom.LogicalSet.NecessarySet NecessarySet(UUID vertexUuid, LogicalAxiom.Atom... elements) {
        EntityVertex necessarySet = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.NECESSARY_SET.nid);
        builder.addVertex(necessarySet);
        builder.addEdge(necessarySet.vertexIndex(), rootIndex);
        for (LogicalAxiom.Atom element : elements) {
            builder.addEdge(element.vertexIndex(), necessarySet.vertexIndex());
        }
        return new LogicalAxiomAdaptor.NecessarySetAdaptor(logicalExpression, necessarySet.vertexIndex());
    }

    public LogicalAxiom.LogicalSet.InclusionSet InclusionSet(LogicalAxiom.Atom... elements) {
        return InclusionSet(generateRandomUuid(), elements);
    }

    public LogicalAxiom.LogicalSet.InclusionSet InclusionSet(UUID vertexUuid, LogicalAxiom.Atom... elements) {
        EntityVertex propertySet = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.INCLUSION_SET.nid);
        builder.addVertex(propertySet);
        builder.addEdge(propertySet.vertexIndex(), rootIndex);
        for (LogicalAxiom.Atom element : elements) {
            builder.addEdge(element.vertexIndex(), propertySet.vertexIndex());
        }
        return new LogicalAxiomAdaptor.InclusionSetAdaptor(logicalExpression, propertySet.vertexIndex());
    }


    public LogicalAxiom.LogicalSet.PropertySet PropertySet(LogicalAxiom.Atom... elements) {
        return PropertySet(generateRandomUuid(), elements);
    }

    public LogicalAxiom.LogicalSet.PropertySet PropertySet(UUID vertexUuid, LogicalAxiom.Atom... elements) {
        EntityVertex propertySet = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.PROPERTY_SET.nid);
        builder.addVertex(propertySet);
        builder.addEdge(propertySet.vertexIndex(), rootIndex);
        for (LogicalAxiom.Atom element : elements) {
            builder.addEdge(element.vertexIndex(), propertySet.vertexIndex());
        }
        return new LogicalAxiomAdaptor.PropertySetAdaptor(logicalExpression, propertySet.vertexIndex());
    }

    public LogicalAxiom.LogicalSet.DataPropertySet DataPropertySet(LogicalAxiom.Atom... elements) {
        return DataPropertySet(generateRandomUuid(), elements);
    }

    public LogicalAxiom.LogicalSet.DataPropertySet DataPropertySet(UUID vertexUuid, LogicalAxiom.Atom... elements) {
        EntityVertex propertySet = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.DATA_PROPERTY_SET.nid);
        builder.addVertex(propertySet);
        builder.addEdge(propertySet.vertexIndex(), rootIndex);
        for (LogicalAxiom.Atom element : elements) {
            builder.addEdge(element.vertexIndex(), propertySet.vertexIndex());
        }
        return new LogicalAxiomAdaptor.DataPropertySetAdaptor(logicalExpression, propertySet.vertexIndex());
    }

	public LogicalAxiom.LogicalSet.IntervalPropertySet IntervalPropertySet(LogicalAxiom.Atom... elements) {
		return IntervalPropertySet(generateRandomUuid(), elements);
	}

	public LogicalAxiom.LogicalSet.IntervalPropertySet IntervalPropertySet(UUID vertexUuid,
			LogicalAxiom.Atom... elements) {
		EntityVertex propertySet = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.INTERVAL_PROPERTY_SET.nid);
		builder.addVertex(propertySet);
		builder.addEdge(propertySet.vertexIndex(), rootIndex);
		for (LogicalAxiom.Atom element : elements) {
			builder.addEdge(element.vertexIndex(), propertySet.vertexIndex());
		}
		return new LogicalAxiomAdaptor.IntervalPropertySetAdaptor(logicalExpression, propertySet.vertexIndex());
	}

	public LogicalAxiom.Atom.TypedAtom.IntervalRole IntervalRole(ConceptFacade intervalRoleType, int lowerBound,
			boolean lowerOpen, int upperBound, boolean upperOpen, ConceptFacade units) {
		return IntervalRole(generateRandomUuid(), intervalRoleType, lowerBound, lowerOpen, upperBound, upperOpen,
				units);
	}

	public LogicalAxiom.Atom.TypedAtom.IntervalRole IntervalRole(UUID vertexUuid, ConceptFacade intervalRoleType,
			int lowerBound, boolean lowerOpen, int upperBound, boolean upperOpen, ConceptFacade units) {
		EntityVertex intervalRole = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.INTERVAL_ROLE.nid);
		builder.addVertex(intervalRole);
		intervalRole.putUncommittedProperty(TinkarTerm.INTERVAL_ROLE_TYPE.nid(), intervalRoleType);
		intervalRole.putUncommittedProperty(TinkarTerm.INTERVAL_LOWER_BOUND.nid(), lowerBound);
		intervalRole.putUncommittedProperty(TinkarTerm.LOWER_BOUND_OPEN.nid(), lowerOpen);
		intervalRole.putUncommittedProperty(TinkarTerm.INTERVAL_UPPER_BOUND.nid(), upperBound);
		intervalRole.putUncommittedProperty(TinkarTerm.UPPER_BOUND_OPEN.nid(), upperOpen);
		intervalRole.putUncommittedProperty(TinkarTerm.UNIT_OF_MEASURE.nid(), units);
		intervalRole.commitProperties();
		return new LogicalAxiomAdaptor.IntervalRoleAxiomAdaptor(logicalExpression, intervalRole.vertexIndex());
	}

    public LogicalAxiom.Atom.Connective.And And(UUID vertexUuid, ImmutableList<? extends LogicalAxiom.Atom> atoms) {
        EntityVertex and = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.AND.nid);
        builder.addVertex(and);
        for (LogicalAxiom.Atom atom : atoms) {
            builder.addEdge(atom.vertexIndex(), and.vertexIndex());
        }
        return new LogicalAxiomAdaptor.AndAdaptor(logicalExpression, and.vertexIndex());
    }

    public LogicalAxiom.Atom.Connective.And And(ImmutableList<? extends LogicalAxiom.Atom> atoms) {
        return And(generateRandomUuid(), atoms);
    }


    public void addToSet(LogicalAxiom.LogicalSet setToAddTo, LogicalAxiom... axioms) {
        addToFirstAnd(setToAddTo.vertexIndex(), axioms);
    }

    private int findFirstAnd(int vertexIndex) {
        EntityVertex vertex = this.builder.vertex(vertexIndex);
        if (vertex.getMeaningNid() == TinkarTerm.AND.nid()) {
            return vertex.vertexIndex();
        } else {
            for (int successorIndex : this.builder.successors(vertexIndex).toArray()) {
                int andIndex = findFirstAnd(successorIndex);
                if (andIndex > -1) {
                    return andIndex;
                }
            }
        }
        return -1;
    }

    public void addToFirstAnd(int vertexIndex, LogicalAxiom... axioms) {
        int andIndex = findFirstAnd(vertexIndex);
        if (andIndex < 0) {
            throw new IllegalStateException("No and vertex at index or below. Index: " + vertexIndex + " in graph: " + this.builder.build());
        }
        for (LogicalAxiom axiom : axioms) {
            this.builder.addEdge(axiom.vertexIndex(), andIndex);
        }
    }


    public LogicalAxiom.Atom.Connective.And And(LogicalAxiom.Atom... atoms) {
        return And(generateRandomUuid(), atoms);
    }

    public LogicalAxiom.Atom.Connective.And And(UUID vertexUuid, LogicalAxiom.Atom... atoms) {
        EntityVertex and = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.AND.nid);
        builder.addVertex(and);
        for (LogicalAxiom.Atom atom : atoms) {
            builder.addEdge(atom.vertexIndex(), and.vertexIndex());
        }
        return new LogicalAxiomAdaptor.AndAdaptor(logicalExpression, and.vertexIndex());
    }

    public LogicalAxiom.Atom.Connective.Or Or(LogicalAxiom.Atom... atoms) {
        return Or(generateRandomUuid(), atoms);
    }

    public LogicalAxiom.Atom.Connective.Or Or(UUID vertexUuid, LogicalAxiom.Atom... atoms) {
        EntityVertex or = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.OR.nid);
        builder.addVertex(or);
        for (LogicalAxiom.Atom atom : atoms) {
            builder.addEdge(atom.vertexIndex(), or.vertexIndex());
        }
        return new LogicalAxiomAdaptor.OrAdaptor(logicalExpression, or.vertexIndex());
    }

    public LogicalAxiom.Atom.TypedAtom.Role SomeRole(ConceptFacade roleType, LogicalAxiom.Atom restriction) {
        return SomeRole(generateRandomUuid(), roleType, restriction);
    }

    public LogicalAxiom.Atom.TypedAtom.Role SomeRole(UUID vertexUuid, ConceptFacade roleType, LogicalAxiom.Atom restriction) {
        EntityVertex someRole = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.ROLE.nid);
        builder.addVertex(someRole);
        someRole.putUncommittedProperty(TinkarTerm.ROLE_TYPE.nid(), roleType);
        someRole.putUncommittedProperty(TinkarTerm.ROLE_OPERATOR.nid(), TinkarTerm.EXISTENTIAL_RESTRICTION);
        someRole.commitProperties();
        builder.addEdge(restriction.vertexIndex(), someRole.vertexIndex());
        return new LogicalAxiomAdaptor.RoleAxiomAdaptor(logicalExpression, someRole.vertexIndex());
    }

    public LogicalAxiom.Atom.TypedAtom.Role AllRole(ConceptFacade roleType, LogicalAxiom.Atom restriction) {
        return AllRole(generateRandomUuid(), roleType, restriction);
    }

    public LogicalAxiom.Atom.TypedAtom.Role AllRole(UUID vertexUuid, ConceptFacade roleType, LogicalAxiom.Atom restriction) {
        EntityVertex allRole = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.ROLE.nid);
        builder.addVertex(allRole);
        allRole.putUncommittedProperty(TinkarTerm.ROLE_TYPE.nid(), roleType);
        allRole.putUncommittedProperty(TinkarTerm.ROLE_OPERATOR.nid(), TinkarTerm.UNIVERSAL_RESTRICTION);
        allRole.commitProperties();
        builder.addEdge(restriction.vertexIndex(), allRole.vertexIndex());
        return new LogicalAxiomAdaptor.RoleAxiomAdaptor(logicalExpression, allRole.vertexIndex());
    }

    public LogicalAxiom.Atom.TypedAtom.Role Role(ConceptFacade roleOperator, ConceptFacade roleType, LogicalAxiom.Atom restriction) {
        return Role(generateRandomUuid(), roleOperator, roleType, restriction);
    }

    public LogicalAxiom.Atom.TypedAtom.Role Role(UUID vertexUuid, ConceptFacade roleOperator, ConceptFacade roleType, LogicalAxiom.Atom restriction) {
        EntityVertex role = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.ROLE.nid);
        builder.addVertex(role);
        role.putUncommittedProperty(TinkarTerm.ROLE_TYPE.nid(), roleType);
        role.putUncommittedProperty(TinkarTerm.ROLE_OPERATOR.nid(), roleOperator);
        role.commitProperties();
        builder.addEdge(restriction.vertexIndex(), role.vertexIndex());
        return new LogicalAxiomAdaptor.RoleAxiomAdaptor(logicalExpression, role.vertexIndex());
    }

    public LogicalAxiom.Atom.ConceptAxiom ConceptAxiom(int conceptNid) {
        return ConceptAxiom(generateRandomUuid(), ConceptFacade.make(conceptNid));
    }

    public LogicalAxiom.Atom.ConceptAxiom ConceptAxiom(UUID vertexUuid, int conceptNid) {
        return ConceptAxiom(vertexUuid, ConceptFacade.make(conceptNid));
    }

    public LogicalAxiom.Atom.ConceptAxiom ConceptAxiom(ConceptFacade concept) {
        return ConceptAxiom(generateRandomUuid(), concept);
    }

    public LogicalAxiom.Atom.ConceptAxiom ConceptAxiom(UUID vertexUuid, ConceptFacade concept) {
        EntityVertex conceptAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.CONCEPT.nid);
        builder.addVertex(conceptAxiom);
        conceptAxiom.putUncommittedProperty(TinkarTerm.CONCEPT_REFERENCE.nid(), concept);
        conceptAxiom.commitProperties();
        return new LogicalAxiomAdaptor.ConceptAxiomAdaptor(logicalExpression, conceptAxiom.vertexIndex());
    }

    public LogicalAxiom.Atom.DisjointWithAxiom DisjointWithAxiom(ConceptFacade disjointConcept) {
        return DisjointWithAxiom(generateRandomUuid(), disjointConcept);
    }

    public LogicalAxiom.Atom.DisjointWithAxiom DisjointWithAxiom(UUID vertexUuid, ConceptFacade disjointConcept) {
        EntityVertex disjointWithAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.DISJOINT_WITH.nid);
        builder.addVertex(disjointWithAxiom);
        disjointWithAxiom.putUncommittedProperty(TinkarTerm.DISJOINT_WITH.nid(), disjointConcept);
        disjointWithAxiom.commitProperties();
        return new LogicalAxiomAdaptor.DisjointWithAxiomAdaptor(logicalExpression, disjointWithAxiom.vertexIndex());
    }

    public LogicalAxiom.Atom.TypedAtom.Feature FeatureAxiom(ConceptFacade featureType, ConceptFacade concreteDomainOperator,
                                                            Object literalValue) {
        return FeatureAxiom(generateRandomUuid(), featureType, concreteDomainOperator, literalValue);
    }

    public LogicalAxiom.Atom.TypedAtom.Feature FeatureAxiom(UUID vertexUuid, ConceptFacade featureType, ConceptFacade concreteDomainOperator,
                                                            Object literal) {
        EntityVertex featureAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.FEATURE.nid);
        builder.addVertex(featureAxiom);
        featureAxiom.putUncommittedProperty(TinkarTerm.FEATURE_TYPE.nid(), featureType);
        featureAxiom.putUncommittedProperty(TinkarTerm.CONCRETE_DOMAIN_OPERATOR.nid(), concreteDomainOperator);
        featureAxiom.putUncommittedProperty(TinkarTerm.LITERAL_VALUE.nid(), literal);

        featureAxiom.commitProperties();
        return new LogicalAxiomAdaptor.FeatureAxiomAdaptor(logicalExpression, featureAxiom.vertexIndex());
    }

    public LogicalAxiom.Atom.PropertySequenceImplication PropertySequenceImplicationAxiom(ImmutableList<ConceptFacade> propertySequence,
                                                                                          ConceptFacade implication) {
        return PropertySequenceImplicationAxiom(generateRandomUuid(), propertySequence, implication);
    }

    public LogicalAxiom.Atom.PropertySequenceImplication PropertySequenceImplicationAxiom(UUID vertexUuid,
                                                                                          ImmutableList<ConceptFacade> propertySequence, ConceptFacade implication) {
        EntityVertex propertySequenceImplicationAxiom = EntityVertex.make(vertexUuid,
                LogicalAxiomSemantic.PROPERTY_SEQUENCE_IMPLICATION.nid);
        builder.addVertex(propertySequenceImplicationAxiom);

//        boolean isPropertySeqPresent = EntityService.get().getEntity(TinkarTerm.PROPERTY_SEQUENCE.publicId()).isPresent();
//        EntityProxy.Concept propertyGroupConcept = isPropertySeqPresent ? TinkarTerm.PROPERTY_SEQUENCE : TinkarTerm.PROPERTY_SET;
        propertySequenceImplicationAxiom.putUncommittedProperty(TinkarTerm.PROPERTY_SEQUENCE.nid(),
                IntIds.list.of(propertySequence.castToList(), (ConceptFacade conceptFacade) -> conceptFacade.nid()));
        propertySequenceImplicationAxiom.putUncommittedProperty(TinkarTerm.PROPERTY_SEQUENCE_IMPLICATION.nid(),
                implication);

        propertySequenceImplicationAxiom.commitProperties();
        return new LogicalAxiomAdaptor.PropertySequenceImplicationAdaptor(logicalExpression,
                propertySequenceImplicationAxiom.vertexIndex());
    }

    /**
     * Creates and returns a recursive clone of the given logical axiom node.
     *
     * @param rootToClone the root node to be cloned. The node can be of type And,
     *                    ConceptAxiom, DefinitionRoot, DisjointWithAxiom, Feature,
     *                    NecessarySet, Or, PropertySequenceImplication, PropertySet,
     *                    Role, or SufficientSet.
     * @param <A>         the type of logical axiom to be returned, extending LogicalAxiom
     * @return a cloned copy of the provided logical axiom node along with its recursive structure added to
     * the logical expression.
     */
    public <A extends LogicalAxiom> A addCloneOfNode(LogicalAxiom rootToClone) {
        return switch (rootToClone) {
            case LogicalAxiom.Atom.Connective.And and -> {
                MutableList<LogicalAxiom.Atom> childElements = Lists.mutable.empty();
                ImmutableSet<LogicalAxiom.Atom> elements = and.elements();
                for (LogicalAxiom.Atom element : elements) {
                    childElements.add(addCloneOfNode(element));
                }
                yield (A) And(and.vertexUUID(), childElements.toArray(new LogicalAxiom.Atom[childElements.size()]));
            }

            case LogicalAxiom.Atom.ConceptAxiom conceptAxiom ->
                    (A) ConceptAxiom(conceptAxiom.vertexUUID(), conceptAxiom.concept());

            case LogicalAxiom.DefinitionRoot definitionRoot -> {
                MutableList<LogicalAxiom.Atom> childSets = Lists.mutable.empty();
                definitionRoot.sets().forEach(logicalSet -> childSets.add(addCloneOfNode(logicalSet)));
                // Definition root was created when builder was created...
                yield (A) logicalExpression.definitionRoot();
            }

            case LogicalAxiom.Atom.DisjointWithAxiom disjointWithAxiom ->
                    (A) DisjointWithAxiom(disjointWithAxiom.vertexUUID(), disjointWithAxiom.disjointWith());

            case LogicalAxiom.Atom.TypedAtom.Feature feature ->
                    (A) FeatureAxiom(feature.vertexUUID(), feature.type(), feature.concreteDomainOperator(), feature.literal());

            case LogicalAxiom.LogicalSet.NecessarySet necessarySet -> {
                // TODO consider removing ANDs the set, and make the sets a connective... Will make isomorphic calculations faster... ?
                MutableList<LogicalAxiom.Atom.Connective> childElements = Lists.mutable.empty();
                necessarySet.elements().forEach(element -> childElements.add(addCloneOfNode(element)));
                yield (A) NecessarySet(necessarySet.vertexUUID(), childElements.toArray(new LogicalAxiom.Atom[childElements.size()]));
            }

            case LogicalAxiom.Atom.Connective.Or or -> {
                MutableList<LogicalAxiom.Atom> childElements = Lists.mutable.empty();
                or.elements().forEach(element -> childElements.add(addCloneOfNode(element)));
                yield (A) Or(or.vertexUUID(), childElements.toArray(childElements.toArray(new LogicalAxiom.Atom[childElements.size()])));
            }

            case LogicalAxiom.Atom.PropertySequenceImplication propertySequenceImplication ->
                    (A) PropertySequenceImplicationAxiom(propertySequenceImplication.vertexUUID(), propertySequenceImplication.propertySequence(), propertySequenceImplication.implication());

            case LogicalAxiom.LogicalSet.PropertySet propertySet -> {
                // TODO remove the AND from the set... Will make isomorphic calculations faster... ?
                MutableList<LogicalAxiom.Atom.Connective> childElements = Lists.mutable.empty();
                propertySet.elements().forEach(element -> childElements.add(addCloneOfNode(element)));
                yield (A) PropertySet(propertySet.vertexUUID(), childElements.toArray(new LogicalAxiom.Atom[childElements.size()]));
            }

            case LogicalAxiom.Atom.TypedAtom.Role role ->
                    (A) Role(role.vertexUUID(), role.roleOperator(), role.type(), addCloneOfNode(role.restriction()));

            case LogicalAxiom.LogicalSet.SufficientSet sufficientSet -> {
                // TODO remove the AND from the set... Will make isomorphic calculations faster... ?
                MutableList<LogicalAxiom.Atom.Connective> childElements = Lists.mutable.empty();
                sufficientSet.elements().forEach(element -> childElements.add(addCloneOfNode(element)));
                yield (A) SufficientSet(sufficientSet.vertexUUID(), childElements.toArray(new LogicalAxiom.Atom[childElements.size()]));
            }
            default -> throw new IllegalStateException("Unexpected value: " + rootToClone);
        };
    }

    /**
     * Updates the concept reference within the specified concept axiom to the new concept reference.
     *
     * @param conceptAxiom        The concept axiom whose concept reference is to be updated.
     * @param newConceptReference The new concept reference to update within the concept axiom.
     */
    public void updateConceptReference(LogicalAxiom.Atom.ConceptAxiom conceptAxiom, ConceptFacade newConceptReference) {
        EntityVertex conceptReferenceVertex = this.builder.vertex(conceptAxiom.vertexIndex());
        conceptReferenceVertex.putUncommittedProperty(TinkarTerm.CONCEPT_REFERENCE.nid(),
                EntityProxy.Concept.make(newConceptReference.description(), newConceptReference.publicId()));
        conceptReferenceVertex.commitProperties();
    }

    /**
     * Updates the concept reference within the specified concept vertex to the new concept reference.
     *
     * @param conceptReferenceVertex The entity vertex representing the concept reference to be updated.
     * @param newConceptReference    The new concept reference to update within the vertex.
     */
    public void updateConceptReference(EntityVertex conceptReferenceVertex, ConceptFacade newConceptReference) {
        conceptReferenceVertex.putUncommittedProperty(TinkarTerm.CONCEPT_REFERENCE.nid(),
                EntityProxy.Concept.make(newConceptReference.description(), newConceptReference.publicId()));
        conceptReferenceVertex.commitProperties();
    }

    /**
     * Updates the concept reference within the specified concept axiom to the new concept reference ID.
     *
     * @param conceptAxiom           The concept axiom whose concept reference is to be updated.
     * @param newConceptReferenceNid The new concept reference ID.
     */
    public void updateConceptReference(LogicalAxiom.Atom.ConceptAxiom conceptAxiom, int newConceptReferenceNid) {
        updateConceptReference(conceptAxiom, EntityProxy.Concept.make(PrimitiveData.text(newConceptReferenceNid),
                PrimitiveData.publicId(newConceptReferenceNid)));
    }

    /**
     * Updates the role type of the specified role axiom.
     *
     * @param roleAxiom         The logical axiom representing the role whose type is to be updated.
     * @param conceptToChangeTo The new concept to which the role type will be updated.
     */
    public void updateRoleType(LogicalAxiom.Atom.TypedAtom.Role roleAxiom, ConceptFacade conceptToChangeTo) {
        EntityVertex roleVertex = this.builder.vertex(roleAxiom.vertexIndex());
        roleVertex.putUncommittedProperty(TinkarTerm.ROLE_TYPE.nid(), EntityProxy.Concept.make(conceptToChangeTo));
        roleVertex.commitProperties();
    }
    
	/**
	 * Updates the interval role type of the specified interval role axiom.
	 *
	 * @param intervalRoleAxiom The logical axiom representing the role whose type
	 *                          is to be updated.
	 * @param conceptToChangeTo The new concept to which the role type will be
	 *                          updated.
	 */
	public void updateIntervalRoleType(LogicalAxiom.Atom.TypedAtom.IntervalRole intervalRoleAxiom,
			ConceptFacade conceptToChangeTo) {
		EntityVertex roleVertex = this.builder.vertex(intervalRoleAxiom.vertexIndex());
		roleVertex.putUncommittedProperty(TinkarTerm.INTERVAL_ROLE_TYPE.nid(),
				EntityProxy.Concept.make(conceptToChangeTo));
		roleVertex.commitProperties();
	}

	/**
	 * Updates the interval role unit of measure of the specified interval role
	 * axiom.
	 *
	 * @param intervalRoleAxiom The logical axiom representing the role whose type
	 *                          is to be updated.
	 * @param conceptToChangeTo The new concept to which the role type will be
	 *                          updated.
	 */
	public void updateIntervalRoleUnitOfMeasure(LogicalAxiom.Atom.TypedAtom.IntervalRole intervalRoleAxiom,
			ConceptFacade conceptToChangeTo) {
		EntityVertex roleVertex = this.builder.vertex(intervalRoleAxiom.vertexIndex());
		roleVertex.putUncommittedProperty(TinkarTerm.UNIT_OF_MEASURE.nid(),
				EntityProxy.Concept.make(conceptToChangeTo));
		roleVertex.commitProperties();
	}

	public void updateIntervalRoleValue(LogicalAxiom.Atom.TypedAtom.IntervalRole intervalRoleAxiom, int lowerBound,
			boolean lowerOpen, int upperBound, boolean upperOpen) {
		EntityVertex roleVertex = this.builder.vertex(intervalRoleAxiom.vertexIndex());
		roleVertex.putUncommittedProperty(TinkarTerm.INTERVAL_LOWER_BOUND.nid(), lowerBound);
		roleVertex.putUncommittedProperty(TinkarTerm.LOWER_BOUND_OPEN.nid(), lowerOpen);
		roleVertex.putUncommittedProperty(TinkarTerm.INTERVAL_UPPER_BOUND.nid(), upperBound);
		roleVertex.putUncommittedProperty(TinkarTerm.UPPER_BOUND_OPEN.nid(), upperOpen);
		roleVertex.commitProperties();
	}

    /**
     * Updates the type of the specified feature axiom to a new type.
     *
     * @param featureAxiom      The logical feature axiom whose type is to be updated.
     * @param conceptToChangeTo The new concept to which the feature type will be changed.
     */
    public void updateFeatureType(LogicalAxiom.Atom.TypedAtom.Feature featureAxiom, ConceptFacade conceptToChangeTo) {
        EntityVertex vertex = this.builder.vertex(featureAxiom.vertexIndex());
        vertex.putUncommittedProperty(TinkarTerm.FEATURE_TYPE.nid(), EntityProxy.Concept.make(conceptToChangeTo));
        vertex.commitProperties();
    }

    public void updateFeatureLiteralValue(LogicalAxiom.Atom.TypedAtom.Feature featureAxiom, Object literalValue) {
        EntityVertex vertex = this.builder.vertex(featureAxiom.vertexIndex());
        vertex.putUncommittedProperty(TinkarTerm.LITERAL_VALUE.nid(), literalValue);
        vertex.commitProperties();
    }

    /**
     * Updates the operator (equals, less than, greater than, ...) of a given feature axiom.
     *
     * @param featureAxiom      The logical feature axiom whose operator is to be updated.
     * @param conceptToChangeTo The new concept to which the operator will be changed.
     */
    public void updateFeatureOperator(LogicalAxiom.Atom.TypedAtom.Feature featureAxiom, ConceptFacade conceptToChangeTo) {
        EntityVertex vertex = this.builder.vertex(featureAxiom.vertexIndex());
        vertex.putUncommittedProperty(TinkarTerm.CONCRETE_DOMAIN_OPERATOR.nid(), EntityProxy.Concept.make(conceptToChangeTo));
        vertex.commitProperties();
    }

    /**
     * Updates the concept restriction (universal or extensional) for a given role axiom.
     *
     * @param roleAxiom         The logical axiomatic role whose restriction is to be updated.
     * @param conceptToChangeTo The new concept to which the restriction will be changed.
     */
    public void updateRoleRestriction(LogicalAxiom.Atom.TypedAtom.Role roleAxiom, ConceptFacade conceptToChangeTo) {
        EntityVertex roleVertex = this.builder.vertex(roleAxiom.vertexIndex());
        ImmutableList<EntityVertex> successors = this.builder.successors(roleVertex);
        if (successors.size() != 1) {
            throw new IllegalStateException("Role should have 1 child for the concept restriction... " + builder.build());
        }
        updateConceptReference(successors.get(0), conceptToChangeTo);
    }

    /**
     * Changes the type of the specified logical set axiom to a new type (property set, necessary set, sufficient set,
     * inclusion set).
     *
     * @param setAxiom          the logical set axiom to be altered
     * @param conceptToChangeTo the new concept to change the type to
     */
    public void changeSetType(LogicalAxiom.LogicalSet setAxiom, ConceptFacade conceptToChangeTo) {
        EntityVertex changedSet = EntityVertex.make(setAxiom.vertexUUID(), conceptToChangeTo.nid());
        builder.setVertexIndex(changedSet, setAxiom.vertexIndex());
        builder.replaceVertex(changedSet);
    }

}
