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
package dev.ikm.komet.reasoner.expression;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.time.Instant;
import java.util.UUID;

public class LogicalExpressionBuilder {

    final LogicalExpression logicalExpression = new LogicalExpression(DiTreeEntity.builder());
    final DiTreeEntity.Builder builder;
    final int rootIndex;

    public LogicalExpressionBuilder(UUID rootVertexUuid) {
        if (logicalExpression.sourceGraph instanceof DiTreeEntity.Builder builder) {
            this.builder = builder;
            EntityVertex rootVertex = EntityVertex.make(rootVertexUuid, LogicalAxiomSemantic.DEFINITION_ROOT.nid);
            builder.addVertex(rootVertex);
            builder.setRoot(rootVertex);
            this.rootIndex = rootVertex.vertexIndex();
            new LogicalAxiomAdaptor.DefinitionRootAdaptor(logicalExpression, rootIndex);
        } else {
            throw new IllegalStateException("sourceGraph is not an instance of DiTreeEntity.Builder.");
        }
    }
    public LogicalExpressionBuilder() {
        this(UUID.randomUUID());
    }

    public LogicalExpression build() {
        return logicalExpression.build();
    }

    public LogicalExpressionBuilder addToRoot(LogicalAxiom.LogicalSet logicalSet) {
        builder.addEdge(logicalSet.vertexIndex(), this.rootIndex);
        return this;
    }

    public LogicalAxiom.LogicalSet.SufficientSet SufficientSet(LogicalAxiom.Atom... elements) {
        return SufficientSet(UUID.randomUUID(), elements);
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
        return NecessarySet(UUID.randomUUID(), elements);
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
    public LogicalAxiom.LogicalSet.PropertySet PropertySet(LogicalAxiom.Atom... elements) {
        return PropertySet(UUID.randomUUID(), elements);
    }
    public LogicalAxiom.LogicalSet.PropertySet PropertySet(UUID vertexUuid, LogicalAxiom.Atom... elements) {
        EntityVertex propertySet = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.PROPERTY_SET.nid);
        builder.addEdge(propertySet.vertexIndex(), rootIndex);
        builder.addVertex(propertySet);
        for (LogicalAxiom.Atom element : elements) {
            builder.addEdge(element.vertexIndex(), propertySet.vertexIndex());
        }
        return new LogicalAxiomAdaptor.PropertySetAdaptor(logicalExpression, propertySet.vertexIndex());
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
        return And(UUID.randomUUID(), atoms);
    }
    public LogicalAxiom.Atom.Connective.And And(LogicalAxiom.Atom... atoms) {
        return And(UUID.randomUUID(), atoms);
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
        return Or(UUID.randomUUID(), atoms);
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
        return SomeRole(UUID.randomUUID(), roleType, restriction);
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
        return AllRole(UUID.randomUUID(), roleType, restriction);
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
        return Role(UUID.randomUUID(), roleOperator, roleType, restriction);
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
        return ConceptAxiom(UUID.randomUUID(), ConceptFacade.make(conceptNid));
    }
    public LogicalAxiom.Atom.ConceptAxiom ConceptAxiom(UUID vertexUuid, int conceptNid) {
        return ConceptAxiom(vertexUuid, ConceptFacade.make(conceptNid));
    }
    public LogicalAxiom.Atom.ConceptAxiom ConceptAxiom(ConceptFacade concept) {
        return ConceptAxiom(UUID.randomUUID(), concept);
    }

    public LogicalAxiom.Atom.ConceptAxiom ConceptAxiom(UUID vertexUuid, ConceptFacade concept) {
        EntityVertex conceptAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.CONCEPT.nid);
        builder.addVertex(conceptAxiom);
        conceptAxiom.putUncommittedProperty(TinkarTerm.CONCEPT_REFERENCE.nid(), concept);
        conceptAxiom.commitProperties();
        return new LogicalAxiomAdaptor.ConceptAxiomAdaptor(logicalExpression, conceptAxiom.vertexIndex());
    }
    public LogicalAxiom.Atom.DisjointWithAxiom DisjointWithAxiom(ConceptFacade disjointConcept) {
        return DisjointWithAxiom(UUID.randomUUID(),  disjointConcept);
    }

    public LogicalAxiom.Atom.DisjointWithAxiom DisjointWithAxiom(UUID vertexUuid, ConceptFacade disjointConcept) {
        EntityVertex disjointWithAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.DISJOINT_WITH.nid);
        builder.addVertex(disjointWithAxiom);
        disjointWithAxiom.putUncommittedProperty(TinkarTerm.DISJOINT_WITH.nid(), disjointConcept);
        disjointWithAxiom.commitProperties();
        return new LogicalAxiomAdaptor.DisjointWithAxiomAdaptor(logicalExpression, disjointWithAxiom.vertexIndex());
    }

    public LogicalAxiom.Atom.TypedAtom.Feature FeatureAxiom(ConceptFacade featureType, ConceptFacade concreteDomainOperator,
                                                            LogicalAxiom.Atom.Literal literal) {
        return FeatureAxiom(UUID.randomUUID(), featureType, concreteDomainOperator, literal);
    }

    public LogicalAxiom.Atom.TypedAtom.Feature FeatureAxiom(UUID vertexUuid, ConceptFacade featureType, ConceptFacade concreteDomainOperator,
                                                            LogicalAxiom.Atom.Literal literal) {
        EntityVertex featureAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.FEATURE.nid);
        builder.addVertex(featureAxiom);
        featureAxiom.putUncommittedProperty(TinkarTerm.FEATURE_TYPE.nid(), featureType);
        featureAxiom.putUncommittedProperty(TinkarTerm.CONCRETE_DOMAIN_OPERATOR.nid(), concreteDomainOperator);

        featureAxiom.commitProperties();
        builder.addEdge(literal.vertexIndex(), featureAxiom.vertexIndex());
        return new LogicalAxiomAdaptor.FeatureAxiomAdaptor(logicalExpression, featureAxiom.vertexIndex());
    }
    public LogicalAxiom.Atom.PropertyPatternImplication PropertyPatternImplicationAxiom(ImmutableList<ConceptFacade> propertyPattern,
                                                                                        ConceptFacade implication) {
        return PropertyPatternImplicationAxiom(UUID.randomUUID(), propertyPattern, implication);
    }

        public LogicalAxiom.Atom.PropertyPatternImplication PropertyPatternImplicationAxiom(UUID vertexUuid,
                                                                                            ImmutableList<ConceptFacade> propertyPattern,
                                                                                        ConceptFacade implication) {
        EntityVertex propertyPatternImplicationAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.PROPERTY_PATTERN_IMPLICATION.nid);
        builder.addVertex(propertyPatternImplicationAxiom);
        throw new UnsupportedOperationException();
//        propertyPatternImplicationAxiom.putUncommittedProperty(TinkarTerm.PATTERN.nid(), featureType);
//        propertyPatternImplicationAxiom.putUncommittedProperty(TinkarTerm.PROPERTY_PATTERN_IMPLICATION.nid(), implication);
//
//        propertyPatternImplicationAxiom.commitProperties();
//        return new LogicalAxiomAdaptor.PropertyPatternImplicationAdaptor(logicalExpression, propertyPatternImplicationAxiom.vertexIndex());
    }

    public LogicalAxiom.Atom.Literal.LiteralBoolean LiteralBoolean(Boolean booleanValue) {
        return LiteralBoolean(UUID.randomUUID(), booleanValue);
    }
    public LogicalAxiom.Atom.Literal.LiteralBoolean LiteralBoolean(UUID vertexUuid, Boolean booleanValue) {
        EntityVertex literalAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.LITERAL_BOOLEAN.nid);
        builder.addVertex(literalAxiom);
        literalAxiom.putUncommittedProperty(TinkarTerm.BOOLEAN_LITERAL.nid(), booleanValue);
        literalAxiom.commitProperties();
        return new LogicalAxiomAdaptor.LiteralBooleanAdaptor(logicalExpression, literalAxiom.vertexIndex());
    }
    public LogicalAxiom.Atom.Literal.LiteralFloat LiteralFloat(float floatValue) {
        return LiteralFloat(UUID.randomUUID(), floatValue);
    }


    public LogicalAxiom.Atom.Literal.LiteralFloat LiteralFloat(UUID vertexUuid, float floatValue) {
        EntityVertex literalAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.LITERAL_FLOAT.nid);
        builder.addVertex(literalAxiom);
        literalAxiom.putUncommittedProperty(TinkarTerm.FLOAT_LITERAL.nid(), floatValue);
        literalAxiom.commitProperties();
        return new LogicalAxiomAdaptor.LiteralFloatAdaptor(logicalExpression, literalAxiom.vertexIndex());
    }
    public LogicalAxiom.Atom.Literal.LiteralInteger LiteralInteger( int integerValue) {
        return LiteralInteger(UUID.randomUUID(), integerValue);
    }

    public LogicalAxiom.Atom.Literal.LiteralInteger LiteralInteger(UUID vertexUuid, int integerValue) {
        EntityVertex literalAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.LITERAL_INTEGER.nid);
        builder.addVertex(literalAxiom);
        literalAxiom.putUncommittedProperty(TinkarTerm.INTEGER_LITERAL.nid(), integerValue);
        literalAxiom.commitProperties();
        return new LogicalAxiomAdaptor.LiteralIntegerAdaptor(logicalExpression, literalAxiom.vertexIndex());
    }

    public LogicalAxiom.Atom.Literal.LiteralInstant LiteralInstant(Instant instantValue) {
        return LiteralInstant(UUID.randomUUID(), instantValue);
    }
    public LogicalAxiom.Atom.Literal.LiteralInstant LiteralInstant(UUID vertexUuid, Instant instantValue) {
        EntityVertex literalAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.LITERAL_INSTANT.nid);
        builder.addVertex(literalAxiom);
        literalAxiom.putUncommittedProperty(TinkarTerm.INSTANT_LITERAL.nid(), instantValue);
        literalAxiom.commitProperties();
        return new LogicalAxiomAdaptor.LiteralInstantAdaptor(logicalExpression, literalAxiom.vertexIndex());
    }
    public LogicalAxiom.Atom.Literal.LiteralString LiteralString(String stringValue) {
        return LiteralString(UUID.randomUUID(), stringValue);
    }

    public LogicalAxiom.Atom.Literal.LiteralString LiteralString(UUID vertexUuid, String stringValue) {
        EntityVertex literalAxiom = EntityVertex.make(vertexUuid, LogicalAxiomSemantic.LITERAL_STRING.nid);
        builder.addVertex(literalAxiom);
        literalAxiom.putUncommittedProperty(TinkarTerm.STRING_LITERAL.nid(), stringValue);
        literalAxiom.commitProperties();
        return new LogicalAxiomAdaptor.LiteralStringAdaptor(logicalExpression, literalAxiom.vertexIndex());
    }

    public LogicalAxiom addCloneOfNode(LogicalAxiom rootToClone) {
        return switch (rootToClone) {
            case LogicalAxiom.Atom.Connective.And and -> {
                ImmutableSet<LogicalAxiom.Atom> elements = and.elements();
                MutableList<LogicalAxiom.Atom> childElements = Lists.mutable.ofInitialCapacity(elements.size());
                for (LogicalAxiom.Atom element : elements) {
                    childElements.add((LogicalAxiom.Atom) addCloneOfNode(element));
                }
                yield And(and.vertexUUID(), childElements.toArray(new LogicalAxiom.Atom[childElements.size()]));
            }

            case LogicalAxiom.Atom.ConceptAxiom conceptAxiom -> ConceptAxiom(conceptAxiom.vertexUUID(), conceptAxiom.concept());

            case LogicalAxiom.DefinitionRoot definitionRoot -> {
                ImmutableSet<LogicalAxiom.LogicalSet> sets = definitionRoot.sets();
                MutableList<LogicalAxiom.Atom> childSets = Lists.mutable.ofInitialCapacity(sets.size());
                for (LogicalAxiom.LogicalSet logicalSet : sets) {
                    childSets.add((LogicalAxiom.Atom) addCloneOfNode(logicalSet));
                }
                // Definition root was created when builder was created...
                yield logicalExpression.definitionRoot();
            }
            case LogicalAxiom.Atom.DisjointWithAxiom disjointWithAxiom -> DisjointWithAxiom(disjointWithAxiom.vertexUUID(), disjointWithAxiom.disjointWith());

            case LogicalAxiom.Atom.TypedAtom.Feature feature -> FeatureAxiom(feature.vertexUUID(), feature.type(), feature.concreteDomainOperator(), feature.literal());
            case LogicalAxiom.Atom.Literal.LiteralBoolean literalBoolean -> LiteralBoolean(literalBoolean.vertexUUID(), literalBoolean.value());
            case LogicalAxiom.Atom.Literal.LiteralFloat literalFloat -> LiteralFloat(literalFloat.vertexUUID(), literalFloat.value());
            case LogicalAxiom.Atom.Literal.LiteralInstant literalInstant -> LiteralInstant(literalInstant.vertexUUID(), literalInstant.value());
            case LogicalAxiom.Atom.Literal.LiteralInteger literalInteger -> LiteralInteger(literalInteger.vertexUUID(), literalInteger.value());
            case LogicalAxiom.Atom.Literal.LiteralString literalString -> LiteralString(literalString.vertexUUID(), literalString.value());
            case LogicalAxiom.LogicalSet.NecessarySet necessarySet -> {
                // TODO remove the AND from the set... Will make isomorphic calculations faster... ?
                ImmutableSet<LogicalAxiom.Atom> elements = necessarySet.elements();
                MutableList<LogicalAxiom.Atom.Connective> childElements = Lists.mutable.ofInitialCapacity(elements.size());
                for (LogicalAxiom.Atom element : elements) {
                    childElements.add((LogicalAxiom.Atom.Connective) addCloneOfNode(element));
                }
                yield NecessarySet(necessarySet.vertexUUID(), childElements.toArray(new LogicalAxiom.Atom[childElements.size()]));
            }
            case LogicalAxiom.Atom.Connective.Or or -> {
                ImmutableSet<LogicalAxiom.Atom> elements = or.elements();
                MutableList<LogicalAxiom.Atom> childElements = Lists.mutable.ofInitialCapacity(elements.size());
                for (LogicalAxiom.Atom element : elements) {
                    childElements.add((LogicalAxiom.Atom) addCloneOfNode(element));
                }
                yield Or(or.vertexUUID(), childElements.toArray(childElements.toArray(new LogicalAxiom.Atom[childElements.size()])));
            }
            case LogicalAxiom.Atom.PropertyPatternImplication propertyPatternImplication ->
                    PropertyPatternImplicationAxiom(propertyPatternImplication.vertexUUID(), propertyPatternImplication.propertyPattern(), propertyPatternImplication.implication());
            case LogicalAxiom.LogicalSet.PropertySet propertySet -> {
                // TODO remove the AND from the set... Will make isomorphic calculations faster... ?
                ImmutableSet<LogicalAxiom.Atom> elements = propertySet.elements();
                MutableList<LogicalAxiom.Atom.Connective> childElements = Lists.mutable.ofInitialCapacity(elements.size());
                for (LogicalAxiom.Atom element : elements) {
                    childElements.add((LogicalAxiom.Atom.Connective) addCloneOfNode(element));
                }
                yield PropertySet(propertySet.vertexUUID(), childElements.toArray(new LogicalAxiom.Atom[childElements.size()]));
            }
            case LogicalAxiom.Atom.TypedAtom.Role role -> Role(role.vertexUUID(), role.roleOperator(), role.type(), (LogicalAxiom.Atom) addCloneOfNode(role.restriction()));
            case LogicalAxiom.LogicalSet.SufficientSet sufficientSet -> {
                // TODO remove the AND from the set... Will make isomorphic calculations faster... ?
                ImmutableSet<LogicalAxiom.Atom> elements = sufficientSet.elements();
                MutableList<LogicalAxiom.Atom.Connective> childElements = Lists.mutable.ofInitialCapacity(elements.size());
                for (LogicalAxiom.Atom element : elements) {
                    childElements.add((LogicalAxiom.Atom.Connective) addCloneOfNode(element));
                }
                yield SufficientSet(sufficientSet.vertexUUID(), childElements.toArray(new LogicalAxiom.Atom[childElements.size()]));
            }
            default -> throw new IllegalStateException("Unexpected value: " + rootToClone);
        };
    }

}
