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
package dev.ikm.komet.reasoner.elk;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import dev.ikm.komet.reasoner.AxiomData;
import dev.ikm.komet.reasoner.expression.LogicalAxiom;
import dev.ikm.komet.reasoner.expression.LogicalExpression;
import dev.ikm.komet.reasoner.expression.LogicalExpressionAdaptorFactory;
import dev.ikm.komet.reasoner.sorocket.ExtractSnoRocketAxiomsTask;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.semanticweb.elk.loading.AxiomLoader;
import org.semanticweb.elk.loading.ElkLoadingException;
import org.semanticweb.elk.owl.interfaces.*;
import org.semanticweb.elk.owl.iris.ElkFullIri;
import org.semanticweb.elk.owl.managers.ElkObjectEntityRecyclingFactory;
import org.semanticweb.elk.owl.visitors.ElkAxiomProcessor;
import org.semanticweb.elk.reasoner.Reasoner;
import org.semanticweb.elk.reasoner.ReasonerFactory;
import org.semanticweb.elk.reasoner.completeness.IncompleteResult;
import org.semanticweb.elk.reasoner.completeness.Incompleteness;
import org.semanticweb.elk.reasoner.completeness.IncompletenessMonitor;
import org.semanticweb.elk.reasoner.config.ReasonerConfiguration;
import org.semanticweb.elk.reasoner.indexing.classes.ChangeIndexingProcessor;
import org.semanticweb.elk.reasoner.indexing.classes.DirectIndex;
import org.semanticweb.elk.reasoner.indexing.conversion.ElkAxiomConverterImpl;
import org.semanticweb.elk.reasoner.indexing.conversion.ElkPolarityExpressionConverter;
import org.semanticweb.elk.reasoner.indexing.conversion.ElkPolarityExpressionConverterImpl;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableOntologyIndex;
import org.semanticweb.elk.reasoner.taxonomy.model.Taxonomy;
import org.semanticweb.elk.util.concurrent.computation.InterruptMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtractLoadReasonWithElkTask extends TrackingCallable<AxiomData<ElkAxiom>> implements AxiomLoader.Factory {
    private static final Logger LOG = LoggerFactory.getLogger(ExtractSnoRocketAxiomsTask.class);
    private final ElkObjectEntityRecyclingFactory elkObjectFactory = new ElkObjectEntityRecyclingFactory();

    final ModifiableOntologyIndex index = new DirectIndex(elkObjectFactory);
    final ElkAxiomProcessor inserter = new ChangeIndexingProcessor(new ElkAxiomConverterImpl(elkObjectFactory, index, 1), 1, index);
    ElkPolarityExpressionConverter converter = new ElkPolarityExpressionConverterImpl(elkObjectFactory, index);

    final ViewCalculator viewCalculator;
    final PatternFacade statedAxiomPattern;
    AxiomData<ElkAxiom> axiomData = new AxiomData();


    public ExtractLoadReasonWithElkTask(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern) {
        super(false, true);
        this.viewCalculator = viewCalculator;
        this.statedAxiomPattern = statedAxiomPattern;
        updateTitle("Fetching ELK axioms from: " + viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(statedAxiomPattern));
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

        LogicalExpressionAdaptorFactory logicalExpressionAdaptor = new LogicalExpressionAdaptorFactory();
        ConcurrentHashSet<Integer> includedConceptNids = new ConcurrentHashSet<>(totalAxiomCount);


        // TODO back to parallel when ELK is ready... Consider replacing ElkObjectEntityRecyclingFactory with atomic spined array
        // viewCalculator.forEachSemanticVersionOfPatternParallel
        viewCalculator.forEachSemanticVersionOfPattern(
                logicCoordinate.statedAxiomsPatternNid(),
                (semanticEntityVersion, patternEntityVersion) -> {
                    updateProgress(axiomCounter.incrementAndGet(), totalAxiomCount);
                    int conceptNid = semanticEntityVersion.referencedComponentNid();
                    includedConceptNids.add(conceptNid);
                    // TODO: In some cases, may wish to classify axioms from inactive concepts. Put in logic coordinate?
                    if (viewCalculator.latestIsActive(conceptNid)) {
                        DiTreeEntity definitionAsTree = (DiTreeEntity) semanticEntityVersion.fieldValues().get(0);
                        LogicalExpression logicalExpression = logicalExpressionAdaptor.adapt(definitionAsTree);

                        ImmutableList<ElkAxiom> axiomList = processLogicalExpression(logicalExpression, conceptNid);
                        axiomList.forEach(elkAxiom -> inserter.visit(elkAxiom));

                        if (axiomData.nidAxiomsMap.compareAndSet(semanticEntityVersion.nid(), null, axiomList)) {
                            axiomData.axiomsSet.addAll(axiomList.castToList());
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


        ReasonerFactory reasoningFactory = new ReasonerFactory();
        ReasonerConfiguration configuration = ReasonerConfiguration.getConfiguration();
        Reasoner reasoner = reasoningFactory.createReasoner(reasoningFactory.createReasoner(configuration),
                elkObjectFactory,
                configuration);

        reasoner.registerAxiomLoader(this);
        reasoner.ensureLoading();

        // Classify the ontology.

        IncompleteResult<? extends Taxonomy<ElkClass>> taxonomyResult = reasoner.getTaxonomy();
        IncompletenessMonitor incompletenessMonitor = taxonomyResult.getIncompletenessMonitor();
        incompletenessMonitor.isIncompletenessDetected();
        incompletenessMonitor.logStatus(LOG);
        Taxonomy<ElkClass> taxonomy = Incompleteness.getValue(taxonomyResult);

        LOG.info("getTaxonomyQuietly complete: " + taxonomy.getTopNode());
        updateMessage("Load in " + durationString());

        return axiomData;
    }

    private ImmutableList<ElkAxiom> processLogicalExpression(LogicalExpression logicalExpression, int conceptNid) {
        return processRoot(logicalExpression.definitionRoot(), conceptNid, logicalExpression, Lists.mutable.empty());
    }

    private ImmutableList<ElkAxiom> processRoot(LogicalAxiom.DefinitionRoot definitionRoot,
                                                int conceptNid,
                                                LogicalExpression logicalExpression, MutableList<ElkAxiom> elkAxioms)
            throws IllegalStateException {

        for (final LogicalAxiom.LogicalSet set : definitionRoot.sets()) {
            switch (set) {
                case LogicalAxiom.LogicalSet.SufficientSet sufficientSet -> processSufficientSet(sufficientSet, conceptNid, logicalExpression, elkAxioms);
                case LogicalAxiom.LogicalSet.NecessarySet necessarySet -> processNecessarySet(necessarySet, conceptNid, logicalExpression, elkAxioms);
                case LogicalAxiom.LogicalSet.PropertySet propertySet -> processPropertySet(propertySet, conceptNid, logicalExpression, elkAxioms);
            }
        }
        return elkAxioms.toImmutable();
    }

    private void processNecessarySet(LogicalAxiom.LogicalSet.NecessarySet necessarySet,
                                     int conceptNid,
                                     LogicalExpression logicalExpression, MutableList<ElkAxiom> elkAxioms) {
        final LogicalAxiom.Atom child = necessarySet.elements().getOnly();

        if (!(child instanceof LogicalAxiom.Atom.Connective.And)) {
            throw new IllegalStateException("necessarySetNode can only have AND for a child. Concept: " + conceptNid +
                    " graph: " + logicalExpression);
        }

        final Optional<? extends ElkObject> optionalConjunctionConcept = generateAxioms(child, conceptNid, logicalExpression, elkAxioms);

        if (optionalConjunctionConcept.isPresent()) {
            ElkSubClassOfAxiom subClassOfAxiom = elkObjectFactory.getSubClassOfAxiom((ElkClassExpression) optionalConjunctionConcept.get(),
                    getConcept(conceptNid));
            elkAxioms.add(subClassOfAxiom);
        } else {
            throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                    " graph: " + logicalExpression);
        }
    }

    private void processSufficientSet(LogicalAxiom.LogicalSet.SufficientSet sufficientSet,
                                      int conceptNid,
                                      LogicalExpression logicalExpression, MutableList<ElkAxiom> elkAxioms) {
        final ImmutableSet<LogicalAxiom.Atom> setElements = sufficientSet.elements();
        if (setElements.size() == 1) {
            final Optional<? extends ElkObject> optionalConjunctionConcept = generateAxioms(setElements.getOnly(), conceptNid, logicalExpression, elkAxioms);

            if (optionalConjunctionConcept.isPresent()) {
                ElkClassExpression conjunctionConcept = (ElkClassExpression) optionalConjunctionConcept.get();
                ElkSubClassOfAxiom axAsubB = elkObjectFactory.getSubClassOfAxiom(getConcept(conceptNid), conjunctionConcept);
                ElkSubClassOfAxiom axBsubA = elkObjectFactory.getSubClassOfAxiom(conjunctionConcept, getConcept(conceptNid));

                elkAxioms.add(axAsubB);
                elkAxioms.add(axBsubA);

            } else {
                throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                        " logicalExpression: " + logicalExpression);
            }
        } else {
            throw new IllegalStateException("Sufficient sets require a single AND child... " + setElements);
        }
    }

    ElkClass getConcept(int conceptNid) {
        return elkObjectFactory.getClass(new ElkFullIri(Integer.toString(conceptNid)));
    }

    /**
     * Generate axioms.
     *
     * @param logicalAxiom the logic node
     * @param conceptNid  the concept nid
     * @param logicalExpression  the logical logicalExpression
     * @return the optional
     */
    private Optional<? extends ElkObject> generateAxioms(LogicalAxiom logicalAxiom,
                                              int conceptNid,
                                                         LogicalExpression logicalExpression, MutableList<ElkAxiom> elkAxioms) {
        return switch (logicalAxiom) {
            case LogicalAxiom.Atom.Connective.And and -> processAnd(and, conceptNid, logicalExpression, elkAxioms);

            case LogicalAxiom.Atom.ConceptAxiom conceptAxiom -> Optional.of(getConcept(conceptAxiom.concept().nid()));

            case LogicalAxiom.Atom.DisjointWithAxiom disjointWithAxiom -> throw new UnsupportedOperationException("Not supported by EL++.");

            case LogicalAxiom.Atom.TypedAtom.Feature feature -> processFeatureNode(feature, conceptNid, logicalExpression, elkAxioms);

            case LogicalAxiom.Atom.Connective.Or or -> throw new UnsupportedOperationException("Or not supported by EL++. " + or);

            case LogicalAxiom.Atom.TypedAtom.Role role -> {
                if (role.roleOperator().nid() == TinkarTerm.EXISTENTIAL_RESTRICTION.nid()) {
                    yield  processRoleNodeSome(role, conceptNid, logicalExpression, elkAxioms);
                } else {
                    throw new UnsupportedOperationException("Role operator: " + PrimitiveData.text(role.roleOperator().nid()) + " not supported. ");
                }
            }

            case LogicalAxiom.Atom.Literal.LiteralBoolean literal ->
                    throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " +
                            literal + " Concept: " + conceptNid + " logicalExpression: " + logicalExpression);

            case LogicalAxiom.Atom.Literal.LiteralFloat literal ->
                    throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " +
                            literal + " Concept: " + conceptNid + " logicalExpression: " + logicalExpression);

            case LogicalAxiom.Atom.Literal.LiteralInstant literal ->
                    throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " +
                            literal + " Concept: " + conceptNid + " logicalExpression: " + logicalExpression);

            case LogicalAxiom.Atom.Literal.LiteralInteger literal ->
                    throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " +
                            literal + " Concept: " + conceptNid + " logicalExpression: " + logicalExpression);

            case LogicalAxiom.Atom.Literal.LiteralString literal ->
                    throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " +
                            literal + " Concept: " + conceptNid + " logicalExpression: " + logicalExpression);

            case LogicalAxiom.LogicalSet.NecessarySet set ->
                    throw new UnsupportedOperationException("Set not expected here: " + set);

            case LogicalAxiom.LogicalSet.SufficientSet set->
                    throw new UnsupportedOperationException("Set not expected here: " + set);

            case LogicalAxiom.LogicalSet.PropertySet propertySet ->
                    throw new UnsupportedOperationException("PropertySet not expected here: " + propertySet);

            case LogicalAxiom.Atom.PropertyPatternImplication propertyPatternImplication ->
                throw new UnsupportedOperationException("Property pattern implication not expected here: " + propertyPatternImplication);

            case LogicalAxiom.DefinitionRoot definitionRoot->
                    throw new UnsupportedOperationException("DefinitionRoot not expected here: " + definitionRoot);

            default -> throw new IllegalStateException("Unexpected value: " + logicalAxiom);
        };
    }

    private void processPropertySet(LogicalAxiom.LogicalSet.PropertySet propertySetNode,
                                    int conceptNid,
                                    LogicalExpression definition, MutableList<ElkAxiom> elkAxioms) {
        final ImmutableSet<LogicalAxiom.Atom> children = propertySetNode.elements();
        LogicalAxiom.Atom connective = children.getOnly();
        if (connective instanceof LogicalAxiom.Atom.Connective.And and) {
            for (LogicalAxiom.Atom andSetElement : and.elements()) {
                switch (andSetElement) {
                    case LogicalAxiom.Atom.ConceptAxiom conceptAxiom -> {
                        final ConceptFacade successorConcept = conceptAxiom.concept();
                        ElkSubObjectPropertyExpression subProperty = elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(conceptNid)));
                        ElkObjectPropertyExpression superProperty = elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(successorConcept.nid())));
                        ElkSubObjectPropertyOfAxiom subObjectPropertyOfAxiom = elkObjectFactory.getSubObjectPropertyOfAxiom(subProperty, superProperty);
                        elkAxioms.add(subObjectPropertyOfAxiom);
                    }


                    case LogicalAxiom.Atom.PropertyPatternImplication propertyPatternImplication -> {
                        ElkObjectProperty subProperty = elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(conceptNid)));

                        List<ElkObjectPropertyExpression> propertyChainList = new ArrayList<>(propertyPatternImplication.propertyPattern().size());
                        for (ConceptFacade conceptFacade: propertyPatternImplication.propertyPattern()) {
                            propertyChainList.add(elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(conceptFacade.nid()))));
                        }
                        ElkObjectPropertyChain propertyChain = elkObjectFactory.getObjectPropertyChain(propertyChainList);
                        ElkSubObjectPropertyOfAxiom subObjectPropertyOfAxiom = elkObjectFactory.getSubObjectPropertyOfAxiom(propertyChain, subProperty);
                        elkAxioms.add(subObjectPropertyOfAxiom);
                    }

                    default -> throw new UnsupportedOperationException("Can't handle: " + andSetElement + " in: " + definition);
                }
            }
        }
    }

    /**
     * Process and.
     *
     * @param and    the and node
     * @param conceptNid the concept nid
     * @param logicalExpression the logical logicalExpression
     * @return the optional
     */
    private Optional<ElkObjectIntersectionOf> processAnd(LogicalAxiom.Atom.Connective.And and, int conceptNid, LogicalExpression logicalExpression,
                                                         MutableList<ElkAxiom> elkAxioms) {
        final ImmutableSet<LogicalAxiom.Atom> andElements = and.elements();
        final List<ElkClassExpression> conjunctionConcepts = new ArrayList<>(andElements.size());
        andElements.forEach(andElement -> {
            Optional<? extends ElkObject> optionalClassExpression = generateAxioms(andElement,
                    conceptNid, logicalExpression, elkAxioms);
            if (optionalClassExpression.isPresent()) {
                conjunctionConcepts.add((ElkClassExpression) optionalClassExpression.get());
            } else {
                throw new IllegalStateException("ElkClassExpression expected...");
            }
        });
        return Optional.of(elkObjectFactory.getObjectIntersectionOf(conjunctionConcepts));
    }

    /**
     * Process role node some.
     *
     * @param role the role node some
     * @param conceptNid   the concept nid
     * @param logicalExpression   the logical logicalExpression
     * @return the optional
     */
    private Optional<? extends ElkObject> processRoleNodeSome(LogicalAxiom.Atom.TypedAtom.Role role,
                                                  int conceptNid,
                                                              LogicalExpression logicalExpression, MutableList<ElkAxiom> elkAxioms) {

        Optional<? extends ElkObject> optionalRestriction = generateAxioms(role.restriction(), conceptNid, logicalExpression, elkAxioms);

        if (optionalRestriction.isPresent()) {
            ElkObjectProperty x = elkObjectFactory.getObjectProperty(new ElkFullIri(Integer.toString(conceptNid)));
            ElkClassExpression y = (ElkClassExpression) optionalRestriction.get();
            ElkObjectSomeValuesFrom elkObjectSomeValuesFrom = elkObjectFactory.getObjectSomeValuesFrom(x, y);
            return Optional.of(elkObjectSomeValuesFrom);
        }
        throw new UnsupportedOperationException("Child of role node can not return null concept. Concept: " +
                conceptNid + " graph: " + logicalExpression);
    }

    /**
     * Process feature node.
     *
     * @param featureAxiom the feature
     * @param conceptNid  the concept nid
     * @param logicalExpression  the logical logicalExpression
     * @return the optional
     */
    private Optional<? extends ElkObject> processFeatureNode(LogicalAxiom.Atom.TypedAtom.Feature featureAxiom,
                                                 int conceptNid,
                                                             LogicalExpression logicalExpression, MutableList<ElkAxiom> elkAxioms) {
        throw new UnsupportedOperationException("Feature node unsupported for Elk");
        /*
        EntityFacade featureFacade = featureNode.propertyFast(TinkarTerm.FEATURE);
        final Feature theFeature = getFeature(featureFacade.nid());
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


    @Override
    public AxiomLoader getAxiomLoader(InterruptMonitor interrupter) {
        return new ElkLoader(interrupter);
    }

    private class ElkLoader implements AxiomLoader {
        private boolean started = false;
        private volatile boolean finished = false;
        /**
         * the exception created if something goes wrong
         */
        protected volatile Exception exception;

        private final InterruptMonitor interrupter;

        public ElkLoader(InterruptMonitor interrupter) {
            this.interrupter = interrupter;
        }

        @Override
        public void load(ElkAxiomProcessor axiomInserter, ElkAxiomProcessor axiomDeleter) throws ElkLoadingException {
            if (finished)
                return;

            if (!started) {
                started = true;
            }
            for (ElkAxiom elkAxiom : axiomData.axiomsSet) {
                if (isInterrupted()) {
                    break;
                }
                axiomInserter.visit(elkAxiom);
            }

            if (exception != null) {
                throw new ElkLoadingException(exception);
            }
            this.finished = true;
        }

        @Override
        public boolean isLoadingFinished() {
            return finished;
        }

        @Override
        public void dispose() {

        }

        @Override
        public boolean isInterrupted() {
            return interrupter.isInterrupted();
        }
    }
}
