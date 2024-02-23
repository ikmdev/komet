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
package dev.ikm.komet.reasoner.elkowl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiomSemantic;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

public class ElkOwlAxiomDataBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(ElkOwlAxiomDataBuilder.class);

	private final ViewCalculator viewCalculator;

	private final PatternFacade statedAxiomPattern;

	private final ElkOwlAxiomData axiomData;

	private OWLDataFactory owlDataFactory;

	private TrackingCallable<?> progressUpdater = null;

	public ElkOwlAxiomDataBuilder(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern,
			ElkOwlAxiomData axiomData) {
		super();
		this.viewCalculator = viewCalculator;
		this.statedAxiomPattern = statedAxiomPattern;
		this.axiomData = axiomData;
		owlDataFactory = ElkOwlManager.getOWLDataFactory();
	}

	public void setProgressUpdater(TrackingCallable<?> progressUpdater) {
		this.progressUpdater = progressUpdater;
	}

	private void updateProgress(int count, int total) {
		if (progressUpdater != null)
			progressUpdater.updateProgress(count, total);
	}

	private void alert(Exception ex) {
		try {
			AlertStreams.dispatchToRoot(ex);
		} catch (Exception e) {
			LOG.error("AlertStreams.dispatchToRoot failed");
			LOG.error("Alert ex", ex.getMessage());
		}
	}

	public static class IncrementalChanges {

		private ImmutableList<OWLAxiom> additions;

		private ImmutableList<OWLAxiom> deletions;

		public IncrementalChanges(ImmutableList<OWLAxiom> additions, ImmutableList<OWLAxiom> deletions) {
			super();
			this.additions = additions;
			this.deletions = deletions;
		}

		public ImmutableList<OWLAxiom> getAdditions() {
			return additions;
		}

		public ImmutableList<OWLAxiom> getDeletions() {
			return deletions;
		}

	}

	public void build() throws Exception {
//		AtomicInteger processedSemanticsCounter = axiomData.processedSemantics;
		AtomicInteger totalCounter = new AtomicInteger();
		PrimitiveData.get().forEachSemanticNidOfPattern(statedAxiomPattern.nid(), i -> totalCounter.incrementAndGet());
		final int totalCount = totalCounter.get();
		LOG.info("Total axioms: " + totalCount);
		updateProgress(0, totalCount);
		LogicCoordinateRecord logicCoordinate = viewCalculator.logicCalculator().logicCoordinateRecord();
		axiomData.processedSemantics.set(0);
		// TODO get a native concurrent collector for roaring
		// https://stackoverflow.com/questions/29916881/how-to-implement-a-thread-safe-collector
		ConcurrentHashSet<Integer> includedConceptNids = new ConcurrentHashSet<>(totalCount);
		viewCalculator.forEachSemanticVersionOfPatternParallel(logicCoordinate.statedAxiomsPatternNid(),
				(semanticEntityVersion, patternEntityVersion) -> {
					int conceptNid = semanticEntityVersion.referencedComponentNid();
					// TODO: In some cases, may wish to classify axioms from inactive concepts. Put
					// in logic coordinate?
					if (viewCalculator.latestIsActive(conceptNid)) {
						// For now, only classify active until we get snomed data issues worked out
						includedConceptNids.add(conceptNid);
//						OWLClass concept = getConcept(conceptNid);
						DiTreeEntity definition = (DiTreeEntity) semanticEntityVersion.fieldValues().get(0);
						ImmutableList<OWLAxiom> axiomsForDefinition = processDefinition(definition, conceptNid);
//						LOG.info(axiomsForDefinition.size() + " " + axiomsForDefinition);
						if (axiomData.nidAxiomsMap.compareAndSet(conceptNid, null, axiomsForDefinition)) {
							axiomData.axiomsSet.addAll(axiomsForDefinition.castToList());
						} else {
							alert(new IllegalStateException("Definition for " + conceptNid + " already exists"));
						}
						axiomData.activeConceptCount.incrementAndGet();
					} else {
						axiomData.inactiveConceptCount.incrementAndGet();
					}
					int processedCount = axiomData.processedSemantics.incrementAndGet();
					if (processedCount % 100 == 0) {
						updateProgress(processedCount, totalCount);
					}
//					if (axiomCounter.get() < 5) {
//						LOG.info("Axiom: \n" + semanticEntityVersion);
//					}
				});
		int[] includedConceptNidArray = includedConceptNids.stream().mapToInt(boxedInt -> (int) boxedInt).toArray();
		Arrays.sort(includedConceptNidArray);
		axiomData.classificationConceptSet = IntLists.immutable.of(includedConceptNidArray);
		updateProgress(totalCount, totalCount);
//		updateMessage("Extract in " + durationString());
//		LOG.info("Extract in " + durationString());
		LOG.info("Total axioms: " + totalCount + " " + axiomData.processedSemantics.get() + " "
				+ axiomData.axiomsSet.size());
		LOG.info("Active concepts: " + axiomData.activeConceptCount.get());
		LOG.info("Inactive concepts: " + axiomData.inactiveConceptCount.get());
	}

	public IncrementalChanges processIncremental(DiTreeEntity definition, int conceptNid) {
		ImmutableList<OWLAxiom> additions = processDefinition(definition, conceptNid);
		ImmutableList<OWLAxiom> deletions = axiomData.nidAxiomsMap.get(conceptNid);
		axiomData.nidAxiomsMap.put(conceptNid, additions);
		deletions.forEach(axiomData.axiomsSet::remove);
//		axiomData.axiomsSet.removeAll(deletions.castToList());
		// TODO update active concept count etc. ??
		return new IncrementalChanges(additions, deletions);
	}

	private ImmutableList<OWLAxiom> processDefinition(DiTreeEntity definition, int conceptNid) {
		return processRoot(definition.root(), conceptNid, definition, Lists.mutable.empty());
	}

	private ImmutableList<OWLAxiom> processRoot(EntityVertex rootVertex, int conceptNid, DiTreeEntity definition,
			MutableList<OWLAxiom> axioms) throws IllegalStateException {
		for (final EntityVertex childVertex : definition.successors(rootVertex)) {
			switch (LogicalAxiomSemantic.get(childVertex.getMeaningNid())) {
			case SUFFICIENT_SET -> {
				processSufficientSet(childVertex, conceptNid, definition, axioms);
			}
			case NECESSARY_SET -> {
				processNecessarySet(childVertex, conceptNid, definition, axioms);
			}
			case PROPERTY_SET -> {
				processPropertySet(childVertex, conceptNid, definition, axioms);
			}
			default ->
				throw new IllegalStateException("Unexpected value: " + PrimitiveData.text(childVertex.getMeaningNid()));
			}
		}
		return axioms.toImmutable();
	}

	private void processNecessarySet(EntityVertex sufficientSetVertex, int conceptNid, DiTreeEntity definition,
			MutableList<OWLAxiom> axioms) {
		final ImmutableList<EntityVertex> childVertexList = definition.successors(sufficientSetVertex);
		if (childVertexList.size() == 1) {
			final Optional<OWLClassExpression> conjunctionConcept = generateAxioms(childVertexList.get(0), conceptNid,
					definition, axioms);
			if (conjunctionConcept.isPresent()) {
				OWLSubClassOfAxiom axiom = owlDataFactory.getOWLSubClassOfAxiom(axiomData.getConcept(conceptNid),
						conjunctionConcept.get());
				axioms.add(axiom);
			} else {
				throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid
						+ " definition: " + definition);
			}
		} else {
			throw new IllegalStateException("Necessary sets require a single AND child... " + childVertexList);
		}
	}

	private void processSufficientSet(EntityVertex necessarySetVertex, int conceptNid, DiTreeEntity definition,
			MutableList<OWLAxiom> axioms) {
		final ImmutableList<EntityVertex> childVertexList = definition.successors(necessarySetVertex);
		if (childVertexList.size() == 1) {
			final Optional<OWLClassExpression> conjunctionConcept = generateAxioms(childVertexList.get(0), conceptNid,
					definition, axioms);
			if (conjunctionConcept.isPresent()) {
				OWLEquivalentClassesAxiom axiom = owlDataFactory
						.getOWLEquivalentClassesAxiom(axiomData.getConcept(conceptNid), conjunctionConcept.get());
				axioms.add(axiom);
			} else {
				throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid
						+ " definition: " + definition);
			}
		} else {
			throw new IllegalStateException("Sufficient sets require a single AND child... " + childVertexList);
		}
	}

	/**
	 * Generate axioms.
	 *
	 * @param logicVertex the logic node
	 * @param conceptNid  the concept nid
	 * @param definition  the logical definition
	 * @return the optional
	 */
	private Optional<OWLClassExpression> generateAxioms(EntityVertex logicVertex, int conceptNid,
			DiTreeEntity definition, MutableList<OWLAxiom> axioms) {
		switch (LogicalAxiomSemantic.get(logicVertex.getMeaningNid())) {
		case AND:
			return processAnd(logicVertex, conceptNid, definition, axioms);

		case CONCEPT:
			final ConceptFacade concept = logicVertex.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
			return Optional.of(axiomData.getConcept(concept.nid()));

		case DEFINITION_ROOT:
			processRoot(logicVertex, conceptNid, definition, axioms);
			break;

		case DISJOINT_WITH:
			throw new UnsupportedOperationException("Not supported");

		case FEATURE:
			return processFeatureNode(logicVertex, conceptNid, definition, axioms);

		case PROPERTY_SET:
			processPropertySet(logicVertex, conceptNid, definition, axioms);
			break;

		case OR:
			throw new UnsupportedOperationException("Not supported");

		case ROLE:
			ConceptFacade roleOperator = logicVertex.propertyFast(TinkarTerm.ROLE_OPERATOR);
			if (roleOperator.nid() == TinkarTerm.EXISTENTIAL_RESTRICTION.nid()) {
				return processRoleNodeSome(logicVertex, conceptNid, definition, axioms);
			} else {
				throw new UnsupportedOperationException(
						"Role: " + PrimitiveData.text(roleOperator.nid()) + " not supported. ");
			}

//		case LITERAL_BOOLEAN:
//		case LITERAL_FLOAT:
//		case LITERAL_INSTANT:
//		case LITERAL_INTEGER:
//		case LITERAL_STRING:
//			throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: "
//					+ logicVertex + " Concept: " + conceptNid + " definition: " + definition);

		case SUFFICIENT_SET:
		case NECESSARY_SET:
			throw new UnsupportedOperationException("Not expected here: " + logicVertex);
		case PROPERTY_PATTERN_IMPLICATION:
			throw new UnsupportedOperationException();
		}

		return Optional.empty();
	}

	private void processPropertySet(EntityVertex propertySetNode, int conceptNid, DiTreeEntity definition,
			MutableList<OWLAxiom> axioms) {
		final ImmutableList<EntityVertex> children = definition.successors(propertySetNode);
		if (children.size() != 1) {
			throw new IllegalStateException(
					"PropertySetNode can only have one child. Concept: " + conceptNid + " definition: " + definition);
		}
		if (!(children.get(0).getMeaningNid() == TinkarTerm.AND.nid())) {
			throw new IllegalStateException("PropertySetNode can only have AND for a child. Concept: " + conceptNid
					+ " definition: " + definition);
		}
		for (EntityVertex node : definition.successors(children.get(0))) {
			switch (LogicalAxiomSemantic.get(node.getMeaningNid())) {
			case CONCEPT:
				final ConceptFacade successorConcept = node.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
				axioms.add(owlDataFactory.getOWLSubObjectPropertyOfAxiom(axiomData.getRole(conceptNid),
						axiomData.getRole(successorConcept.nid())));
				break;
			case PROPERTY_PATTERN_IMPLICATION:
				final ConceptFacade pi = node.propertyFast(TinkarTerm.PROPERTY_PATTERN_IMPLICATION);
				final IntIdList ps = node.propertyFast(TinkarTerm.PROPERTY_SET);
				List<OWLObjectProperty> chain = ps.intStream().mapToObj(x -> axiomData.getRole(x)).toList();
				OWLSubPropertyChainOfAxiom axiom = owlDataFactory.getOWLSubPropertyChainOfAxiom(chain,
						axiomData.getRole(pi.nid()));
				axioms.add(axiom);
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
	private Optional<OWLClassExpression> processAnd(EntityVertex andNode, int conceptNid, DiTreeEntity definition,
			MutableList<OWLAxiom> axioms) {
		final ImmutableList<EntityVertex> childrenLogicNodes = definition.successors(andNode);
		final OWLClassExpression[] conjunctionConcepts = new OWLClassExpression[childrenLogicNodes.size()];
		for (int i = 0; i < childrenLogicNodes.size(); i++) {
			conjunctionConcepts[i] = generateAxioms(childrenLogicNodes.get(i), conceptNid, definition, axioms).get();
		}
		if (conjunctionConcepts.length == 1)
			return Optional.of(conjunctionConcepts[0]);
		OWLObjectIntersectionOf expr = owlDataFactory.getOWLObjectIntersectionOf(conjunctionConcepts);
		return Optional.of(expr);
	}

	/**
	 * Process role node some.
	 *
	 * @param roleNodeSome the role node some
	 * @param conceptNid   the concept nid
	 * @param definition   the logical definition
	 * @return the optional
	 */
	private Optional<OWLClassExpression> processRoleNodeSome(EntityVertex roleNodeSome, int conceptNid,
			DiTreeEntity definition, MutableList<OWLAxiom> axioms) {
		ConceptFacade roleType = roleNodeSome.propertyFast(TinkarTerm.ROLE_TYPE);
		final OWLObjectProperty theRole = axiomData.getRole(roleType.nid());
		final ImmutableList<EntityVertex> children = definition.successors(roleNodeSome);
		if (children.size() != 1) {
			throw new IllegalStateException(
					"RoleNodeSome can only have one child. Concept: " + conceptNid + " definition: " + definition);
		}
		final Optional<OWLClassExpression> restrictionConcept = generateAxioms(children.get(0), conceptNid, definition,
				axioms);
		if (restrictionConcept.isPresent()) {
			return Optional.of(owlDataFactory.getOWLObjectSomeValuesFrom(theRole, restrictionConcept.get()));
		}
		throw new UnsupportedOperationException("Child of role node can not return null concept. Concept: " + conceptNid
				+ " definition: " + definition);
	}

	/**
	 * Process feature node.
	 *
	 * @param featureNode the feature node
	 * @param conceptNid  the concept nid
	 * @param definition  the logical definition
	 * @return the optional
	 */
	private Optional<OWLClassExpression> processFeatureNode(EntityVertex featureNode, int conceptNid,
			DiTreeEntity definition, MutableList<OWLAxiom> axioms) {
//		EntityFacade featureFacade = featureNode.propertyFast(TinkarTerm.FEATURE);
//		final Feature theFeature = getFeature(featureFacade.nid());
		throw new UnsupportedOperationException();
		/*
		 * final ImmutableList<EntityVertex> children =
		 * logicGraph.successors(featureNode);
		 * 
		 * if (children.size() != 1) { throw new
		 * IllegalStateException("FeatureNode can only have one child. Concept: " +
		 * conceptNid + " graph: " + logicGraph); }
		 * 
		 * final Optional<Literal> optionalLiteral = generateLiterals(children[0],
		 * getConcept(conceptNid), logicGraph);
		 * 
		 * if (optionalLiteral.isPresent()) { switch (featureNode.getOperator()) { case
		 * EQUALS: return Optional.of(Factory.createDatatype(theFeature,
		 * Operator.EQUALS, optionalLiteral.get()));
		 * 
		 * case GREATER_THAN: return Optional.of(Factory.createDatatype(theFeature,
		 * Operator.GREATER_THAN, optionalLiteral.get()));
		 * 
		 * case GREATER_THAN_EQUALS: return
		 * Optional.of(Factory.createDatatype(theFeature, Operator.GREATER_THAN_EQUALS,
		 * optionalLiteral.get()));
		 * 
		 * case LESS_THAN: return Optional.of(Factory.createDatatype(theFeature,
		 * Operator.LESS_THAN, optionalLiteral.get()));
		 * 
		 * case LESS_THAN_EQUALS: return Optional.of(Factory.createDatatype(theFeature,
		 * Operator.LESS_THAN_EQUALS, optionalLiteral.get()));
		 * 
		 * default: throw new
		 * UnsupportedOperationException(featureNode.getOperator().toString()); } }
		 * 
		 * throw new
		 * UnsupportedOperationException("Child of FeatureNode node cannot return null concept. Concept: "
		 * + conceptNid + " graph: " + logicGraph);
		 * 
		 */
	}

}
