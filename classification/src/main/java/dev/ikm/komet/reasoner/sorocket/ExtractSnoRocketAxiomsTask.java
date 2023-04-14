package dev.ikm.komet.reasoner.sorocket;

import au.csiro.ontology.Factory;
import au.csiro.ontology.model.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import dev.ikm.komet.reasoner.AxiomData;
import dev.ikm.komet.reasoner.expression.LogicalAxiomSemantic;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtractSnoRocketAxiomsTask extends TrackingCallable<AxiomData> {
    private static final Logger LOG = LoggerFactory.getLogger(ExtractSnoRocketAxiomsTask.class);
    final ViewCalculator viewCalculator;
    final PatternFacade statedAxiomPattern;
    AxiomData<Axiom> axiomData = new AxiomData();


    public ExtractSnoRocketAxiomsTask(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern) {
        super(false, true);
        this.viewCalculator = viewCalculator;
        this.statedAxiomPattern = statedAxiomPattern;
        updateTitle("Fetching SnoRocket axioms from: " + viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(statedAxiomPattern));
    }

    @Override
    protected AxiomData compute() throws Exception {


        AtomicInteger axiomCounter = axiomData.processedSemantics;
        PrimitiveData.get().forEachSemanticNidOfPattern(statedAxiomPattern.nid(), i -> axiomCounter.incrementAndGet());
        final int totalAxiomCount = axiomCounter.get();
        updateProgress(0, totalAxiomCount);
        LogicCoordinateRecord logicCoordinate = viewCalculator.logicCalculator().logicCoordinateRecord();
        axiomCounter.set(0);
        // TODO get a native concurrent collector for roaring https://stackoverflow.com/questions/29916881/how-to-implement-a-thread-safe-collector

        ConcurrentHashSet<Integer> includedConceptNids = new ConcurrentHashSet<>(totalAxiomCount);
        viewCalculator.forEachSemanticVersionOfPatternParallel(
                logicCoordinate.statedAxiomsPatternNid(),
                (semanticEntityVersion, patternEntityVersion) -> {
                    updateProgress(axiomCounter.incrementAndGet(), totalAxiomCount);
                    int conceptNid = semanticEntityVersion.referencedComponentNid();
                    includedConceptNids.add(conceptNid);
                    // TODO: In some cases, may wish to classify axioms from inactive concepts. Put in logic coordinate?
                    if (viewCalculator.latestIsActive(conceptNid)) {
                        Concept concept = getConcept(conceptNid);
                        DiTreeEntity definition = (DiTreeEntity) semanticEntityVersion.fieldValues().get(0);
                        ImmutableList<Axiom> axiomsForDefinition = processDefinition(definition, conceptNid);
                        if (axiomData.nidAxiomsMap.compareAndSet(semanticEntityVersion.nid(), null, axiomsForDefinition)) {
                            axiomData.axiomsSet.addAll(axiomsForDefinition.castToList());
                        } else {
                            AlertStreams.dispatchToRoot(new IllegalStateException("Definition for " + conceptNid + " already exists. "));
                        }
                    }
                    int axiomCount = axiomCounter.incrementAndGet();
                    if (axiomCount % 100 == 0) {
                        updateProgress(axiomCount, totalAxiomCount);
                    }
                    if (axiomCounter.get() < 5) {
                        LOG.info("Axiom: \n" + semanticEntityVersion);
                    }
                }
        );
        int[] includedConceptNidArray = includedConceptNids.stream().mapToInt(boxedInt -> (int) boxedInt).toArray();
        Arrays.sort(includedConceptNidArray);
        axiomData.classificationConceptSet = IntLists.immutable.of(includedConceptNidArray);
        updateProgress(totalAxiomCount, totalAxiomCount);
        updateMessage("In " + durationString());
        return axiomData;
    }

    private ImmutableList<Axiom> processDefinition(DiTreeEntity definition, int conceptNid) {
        return processRoot(definition.root(), conceptNid, definition, Lists.mutable.empty());
    }

    private ImmutableList<Axiom> processRoot(EntityVertex rootVertex,
                                             int conceptNid,
                                             DiTreeEntity definition, MutableList<Axiom> snorocketAxioms)
            throws IllegalStateException {

        for (final EntityVertex childVertex : definition.successors(rootVertex)) {
            switch (LogicalAxiomSemantic.get(childVertex.getMeaningNid())) {
                case SUFFICIENT_SET -> {
                    processSufficientSet(childVertex, conceptNid, definition, snorocketAxioms);
                }
                case NECESSARY_SET -> {
                    processNecessarySet(childVertex, conceptNid, definition, snorocketAxioms);
                }
                case PROPERTY_SET -> {
                    processPropertySet(childVertex, conceptNid, definition, snorocketAxioms);
                }

                default -> throw new IllegalStateException("Unexpected value: " + PrimitiveData.text(childVertex.getMeaningNid()));
            }
        }
        return snorocketAxioms.toImmutable();
    }

    private void processNecessarySet(EntityVertex sufficientSetVertex,
                                     int conceptNid,
                                     DiTreeEntity definition, MutableList<Axiom> snorocketAxioms) {
        final ImmutableList<EntityVertex> childVertexList = definition.successors(sufficientSetVertex);
        if (childVertexList.size() == 1) {
            final Optional<Concept> conjunctionConcept = generateAxioms(childVertexList.get(0), conceptNid, definition, snorocketAxioms);

            if (conjunctionConcept.isPresent()) {
                snorocketAxioms.add(new ConceptInclusion(getConcept(conceptNid), conjunctionConcept.get()));
            } else {
                throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                        " definition: " + definition);
            }
        } else {
            throw new IllegalStateException("Necessary sets require a single AND child... " + childVertexList);
        }
    }

    private void processSufficientSet(EntityVertex necessarySetVertex,
                                      int conceptNid,
                                      DiTreeEntity definition, MutableList<Axiom> snorocketAxioms) {
        final ImmutableList<EntityVertex> childVertexList = definition.successors(necessarySetVertex);
        if (childVertexList.size() == 1) {
            final Optional<Concept> conjunctionConcept = generateAxioms(childVertexList.get(0), conceptNid, definition, snorocketAxioms);

            if (conjunctionConcept.isPresent()) {
                snorocketAxioms.add(
                        new ConceptInclusion(
                                getConcept(conceptNid),
                                conjunctionConcept.get()));
                snorocketAxioms.add(
                        new ConceptInclusion(
                                conjunctionConcept.get(),
                                getConcept(conceptNid)));
            } else {
                throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                        " definition: " + definition);
            }
        } else {
            throw new IllegalStateException("Sufficient sets require a single AND child... " + childVertexList);
        }
    }

    /**
     * Gets the role.
     *
     * @param roleNid the name
     * @return the role
     */
    private Role getRole(int roleNid) {
        //return Factory.createNamedRole(Integer.toString(roleNid));
        return axiomData.nidRoleMap.computeIfAbsent(roleNid, nid -> Factory.createNamedRole(Integer.toString(roleNid)));
    }

    private Concept getConcept(int conceptNid) {
        //return Factory.createNamedConcept(Integer.toString(conceptNid));
        return axiomData.nidConceptMap.computeIfAbsent(conceptNid, nid -> Factory.createNamedConcept(Integer.toString(conceptNid)));
    }

    private Feature getFeature(int featureNid) {
        //return Factory.createNamedFeature(Integer.toString(featureNid));
        return axiomData.nidFeatureMap.computeIfAbsent(featureNid, nid -> Factory.createNamedFeature(Integer.toString(featureNid)));
    }

    /**
     * Generate axioms.
     *
     * @param logicVertex the logic node
     * @param conceptNid  the concept nid
     * @param definition  the logical definition
     * @return the optional
     */
    private Optional<Concept> generateAxioms(EntityVertex logicVertex,
                                             int conceptNid,
                                             DiTreeEntity definition, MutableList<Axiom> snorocketAxioms) {
        switch (LogicalAxiomSemantic.get(logicVertex.getMeaningNid())) {
            case AND:
                return processAnd(logicVertex, conceptNid, definition, snorocketAxioms);

            case CONCEPT:
                final ConceptFacade concept = logicVertex.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
                return Optional.of(getConcept(concept.nid()));

            case DEFINITION_ROOT:
                processRoot(logicVertex, conceptNid, definition, snorocketAxioms);
                break;

            case DISJOINT_WITH:
                throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");

            case FEATURE:
                return processFeatureNode(logicVertex, conceptNid, definition, snorocketAxioms);

            case PROPERTY_SET:
                processPropertySet(logicVertex, conceptNid, definition, snorocketAxioms);
                break;

            case OR:
                throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");

            case ROLE:
                ConceptFacade roleOperator = logicVertex.propertyFast(TinkarTerm.ROLE_OPERATOR);
                if (roleOperator.nid() == TinkarTerm.EXISTENTIAL_RESTRICTION.nid()) {
                    return processRoleNodeSome(logicVertex, conceptNid, definition, snorocketAxioms);
                } else {
                    throw new UnsupportedOperationException("Role: " + PrimitiveData.text(roleOperator.nid()) + " not supported. ");
                }

            case LITERAL_BOOLEAN:
            case LITERAL_FLOAT:
            case LITERAL_INSTANT:
            case LITERAL_INTEGER:
            case LITERAL_STRING:
                throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " + logicVertex +
                        " Concept: " + conceptNid + " definition: " + definition);

            case SUFFICIENT_SET:
            case NECESSARY_SET:
                throw new UnsupportedOperationException("Not expected here: " + logicVertex);
            case PROPERTY_PATTERN_IMPLICATION:
                throw new UnsupportedOperationException();
        }

        return Optional.empty();
    }

    private void processPropertySet(EntityVertex propertySetNode,
                                    int conceptNid,
                                    DiTreeEntity definition, MutableList<Axiom> snorocketAxioms) {
        final ImmutableList<EntityVertex> children = definition.successors(propertySetNode);

        if (children.size() != 1) {
            throw new IllegalStateException("PropertySetNode can only have one child. Concept: " + conceptNid +
                    " definition: " + definition);
        }

        if (!(children.get(0).getMeaningNid() == TinkarTerm.AND.nid())) {
            throw new IllegalStateException("PropertySetNode can only have AND for a child. Concept: " + conceptNid +
                    " definition: " + definition);
        }


        for (EntityVertex node : definition.successors(children.get(0))) {
            switch (LogicalAxiomSemantic.get(node.getMeaningNid())) {
                case CONCEPT:
                    final ConceptFacade successorConcept = node.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
                    // TODO is this right? Getting roles for a property set?
                    snorocketAxioms.add(new RoleInclusion(
                            getRole(conceptNid),
                            getRole(successorConcept.nid())));
                    break;

                case PROPERTY_PATTERN_IMPLICATION:
                    LOG.warn("Can't currently handle: " + node + " in: " + definition);
                    break;

                default:
                    throw new UnsupportedOperationException("Can't handle: " + node + " in: " + definition);
            }
        }
    }

    /**
     * Process and.
     *
     * @param andNode    the and node
     * @param conceptNid the concept nid
     * @param definition the logical definition
     * @return the optional
     */
    private Optional<Concept> processAnd(EntityVertex andNode, int conceptNid, DiTreeEntity definition,
                                         MutableList<Axiom> snorocketAxioms) {
        final ImmutableList<EntityVertex> childrenLogicNodes = definition.successors(andNode);
        final Concept[] conjunctionConcepts = new Concept[childrenLogicNodes.size()];

        for (int i = 0; i < childrenLogicNodes.size(); i++) {
            conjunctionConcepts[i] = generateAxioms(childrenLogicNodes.get(i), conceptNid, definition, snorocketAxioms).get();
        }

        return Optional.of(Factory.createConjunction(conjunctionConcepts));
    }

    /**
     * Process role node some.
     *
     * @param roleNodeSome the role node some
     * @param conceptNid   the concept nid
     * @param definition   the logical definition
     * @return the optional
     */
    private Optional<Concept> processRoleNodeSome(EntityVertex roleNodeSome,
                                                  int conceptNid,
                                                  DiTreeEntity definition, MutableList<Axiom> snorocketAxioms) {
        ConceptFacade roleType = roleNodeSome.propertyFast(TinkarTerm.ROLE_TYPE);
        final Role theRole = getRole(roleType.nid());
        final ImmutableList<EntityVertex> children = definition.successors(roleNodeSome);

        if (children.size() != 1) {
            throw new IllegalStateException("RoleNodeSome can only have one child. Concept: " + conceptNid + " definition: " +
                    definition);
        }

        final Optional<Concept> restrictionConcept = generateAxioms(children.get(0), conceptNid, definition, snorocketAxioms);

        if (restrictionConcept.isPresent()) {
            return Optional.of(Factory.createExistential(theRole, restrictionConcept.get()));
        }

        throw new UnsupportedOperationException("Child of role node can not return null concept. Concept: " +
                conceptNid + " definition: " + definition);
    }

    /**
     * Process feature node.
     *
     * @param featureNode the feature node
     * @param conceptNid  the concept nid
     * @param definition  the logical definition
     * @return the optional
     */
    private Optional<Concept> processFeatureNode(EntityVertex featureNode,
                                                 int conceptNid,
                                                 DiTreeEntity definition, MutableList<Axiom> snorocketAxioms) {
        EntityFacade featureFacade = featureNode.propertyFast(TinkarTerm.FEATURE);
        final Feature theFeature = getFeature(featureFacade.nid());
        throw new UnsupportedOperationException();
        /*
        final ImmutableList<EntityVertex> children = logicGraph.successors(featureNode);

        if (children.size() != 1) {
            throw new IllegalStateException("FeatureNode can only have one child. Concept: " + conceptNid + " graph: " +
                    logicGraph);
        }

        final Optional<Literal> optionalLiteral = generateLiterals(children[0], getConcept(conceptNid), logicGraph);

        if (optionalLiteral.isPresent()) {
            switch (featureNode.getOperator()) {
                case EQUALS:
                    return Optional.of(Factory.createDatatype(theFeature, Operator.EQUALS, optionalLiteral.get()));

                case GREATER_THAN:
                    return Optional.of(Factory.createDatatype(theFeature, Operator.GREATER_THAN, optionalLiteral.get()));

                case GREATER_THAN_EQUALS:
                    return Optional.of(Factory.createDatatype(theFeature, Operator.GREATER_THAN_EQUALS, optionalLiteral.get()));

                case LESS_THAN:
                    return Optional.of(Factory.createDatatype(theFeature, Operator.LESS_THAN, optionalLiteral.get()));

                case LESS_THAN_EQUALS:
                    return Optional.of(Factory.createDatatype(theFeature, Operator.LESS_THAN_EQUALS, optionalLiteral.get()));

                default:
                    throw new UnsupportedOperationException(featureNode.getOperator().toString());
            }
        }

        throw new UnsupportedOperationException("Child of FeatureNode node cannot return null concept. Concept: " +
                conceptNid + " graph: " + logicGraph);

         */
    }
}
