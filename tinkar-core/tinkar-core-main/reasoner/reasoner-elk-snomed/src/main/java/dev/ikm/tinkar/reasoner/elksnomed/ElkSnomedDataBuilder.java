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
package dev.ikm.tinkar.reasoner.elksnomed;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRole.ValueType;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.elk.snomed.model.Role;
import dev.ikm.elk.snomed.model.RoleGroup;
import dev.ikm.elk.snomed.model.RoleType;
import dev.ikm.elk.snomed.model.SnomedEntity;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiomSemantic;
import dev.ikm.tinkar.ext.lang.owl.IntervalUtil;
import dev.ikm.tinkar.reasoner.service.UnsupportedReasonerProcessIncremental;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

public class ElkSnomedDataBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedDataBuilder.class);

	private static final boolean log_property_sets = false;

	private final ViewCalculator viewCalculator;

	private final PatternFacade statedAxiomPattern;

	private final ElkSnomedData data;

	private TrackingCallable<?> progressUpdater = null;

	public ElkSnomedDataBuilder(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern, ElkSnomedData data) {
		super();
		this.viewCalculator = viewCalculator;
		this.statedAxiomPattern = statedAxiomPattern;
		this.data = data;
	}

	public void setProgressUpdater(TrackingCallable<?> progressUpdater) {
		this.progressUpdater = progressUpdater;
	}

	private void updateProgress(int count, int total) {
		if (progressUpdater != null && count % 100 == 0)
			progressUpdater.updateProgress(count, total);
	}

	private int computeTotalCount() {
		AtomicInteger totalCounter = new AtomicInteger();
		PrimitiveData.get().forEachSemanticNidOfPattern(statedAxiomPattern.nid(), _ -> totalCounter.incrementAndGet());
		return totalCounter.get();
	}

	public void build() throws Exception {
		final int totalCount = computeTotalCount();
		LOG.info("Total axioms: " + totalCount);
		updateProgress(0, totalCount);
		final AtomicInteger processedCount = new AtomicInteger();
		LogicCoordinateRecord logicCoordinate = viewCalculator.logicCalculator().logicCoordinateRecord();
		AtomicInteger ex_cnt = new AtomicInteger();
		viewCalculator.forEachSemanticVersionOfPatternParallel(logicCoordinate.statedAxiomsPatternNid(),
				(semanticEntityVersion, _) -> {
					try {
						if (semanticEntityVersion.active()) {
							processDefinition(semanticEntityVersion);
							data.incrementActiveConceptCount();
						} else {
							data.incrementInactiveConceptCount();
						}
						updateProgress(processedCount.incrementAndGet(), totalCount);
					} catch (Exception ex) {
						if (ex_cnt.incrementAndGet() < 10) {
							LOG.error(ex.getMessage());
							LOG.error("", ex);
						}
					}
				});
		buildRoleConcepts();
		data.initializeReasonerConceptSet();
		for (Concept con : data.getConcepts()) {
			if (con.getDefinitions().isEmpty())
				LOG.warn("No definitions: " + con.getId() + " " + PrimitiveData.text((int) con.getId()));
		}
		updateProgress(totalCount, totalCount);
		LOG.info("Total processed: " + totalCount + " " + processedCount.get());
		LOG.info("Active concepts: " + data.getActiveConceptCount());
		LOG.info("Inactive concepts: " + data.getInactiveConceptCount());
		if (ex_cnt.get() != 0) {
			String msg = "Exceptions: " + ex_cnt.get();
			LOG.error(msg);
			throw new Exception(msg);
		}
	}

	private void buildRoleConcepts() {
		// Create concepts for role types and concrete role types
		// Should eventually do this in the write back of inferred
		for (RoleType role : data.getRoleTypes()) {
			if (ElkSnomedData.getNid(SnomedIds.concept_model_object_attribute) == role.getId()) {
				LOG.info("Skipping root " + PrimitiveData.text((int) role.getId()));
				continue;
			}
			Concept con = data.getOrCreateConcept((int) role.getId());
			if (!con.getDefinitions().isEmpty()) {
				LOG.error("Has defs: " + con);
				con.removeAllDefinitions();
			}
			Definition def = new Definition();
			def.setDefinitionType(DefinitionType.SubConcept);
			con.addDefinition(def);
			for (RoleType sup_role : role.getSuperRoleTypes()) {
				Concept sup_con = data.getOrCreateConcept((int) sup_role.getId());
				def.addSuperConcept(sup_con);
			}
		}
		for (ConcreteRoleType role : data.getConcreteRoleTypes()) {
			if (ElkSnomedData.getNid(SnomedIds.concept_model_data_attribute) == role.getId()) {
				LOG.info("Skipping root " + PrimitiveData.text((int) role.getId()));
				continue;
			}
			Concept con = data.getOrCreateConcept((int) role.getId());
			if (!con.getDefinitions().isEmpty()) {
				LOG.error("Has defs: " + con);
				con.removeAllDefinitions();
			}
			Definition def = new Definition();
			def.setDefinitionType(DefinitionType.SubConcept);
			con.addDefinition(def);
			for (ConcreteRoleType sup_role : role.getSuperConcreteRoleTypes()) {
				Concept sup_con = data.getOrCreateConcept((int) sup_role.getId());
				def.addSuperConcept(sup_con);
			}
		}
	}

	public Concept processDelete(int nid) {
		if (data.getConcept(nid) != null) {
			return data.deleteConcept(nid);
		}
		if (data.getRoleType(nid) != null) {
			RoleType role = data.getRoleType(nid);
			role.setName(PrimitiveData.text(nid));
			throw new UnsupportedReasonerProcessIncremental("Delete: " + role.getClass() + " " + role);
		}
		if (data.getConcreteRoleType(nid) != null) {
			ConcreteRoleType role = data.getConcreteRoleType(nid);
			role.setName(PrimitiveData.text(nid));
			throw new UnsupportedReasonerProcessIncremental("Delete: " + role.getClass() + " " + role);
		}
		return null;
	}

	public Concept processUpdate(SemanticEntityVersion update) {
		int nid = update.referencedComponentNid();
		{
			Concept concept = data.getConcept(nid);
			if (concept != null) {
				concept.removeAllDefinitions();
				concept.removeAllGciDefinitions();
			}
		}
		SnomedEntity entity = processDefinition(update);
		if (entity == null) {
			LOG.error("\n" + PrimitiveData.text(nid) + "\n" + update);
			throw new UnsupportedReasonerProcessIncremental("processDefinition failed: " + PrimitiveData.text(nid));
		}
		return switch (entity) {
		case Concept concept -> concept;
		case RoleType role -> {
			role.setName(PrimitiveData.text(nid));
			throw new UnsupportedReasonerProcessIncremental("Update: " + role);
		}
		case ConcreteRoleType role -> {
			role.setName(PrimitiveData.text(nid));
			throw new UnsupportedReasonerProcessIncremental("Update: " + role);
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + entity);
		};

	}

	private LogicalAxiomSemantic getMeaning(EntityVertex node) {
		return LogicalAxiomSemantic.get(node.getMeaningNid());
	}

	public static int getNid(EntityVertex node, dev.ikm.tinkar.terms.EntityProxy.Concept concept) {
		ConceptFacade cf = node.propertyFast(concept);
		return cf.nid();
	}

	private EntityVertex getFirstChildCheck(int conceptNid, EntityVertex node, DiTreeEntity definition,
			dev.ikm.tinkar.terms.EntityProxy.Concept meaning) {
		final ImmutableList<EntityVertex> children = definition.successors(node);
		if (children.size() != 1)
			throw new IllegalStateException(
					node + " can only have one child. Concept: " + conceptNid + " Definition: " + definition);
		EntityVertex child = children.getFirst();
		if (meaning != null && child.getMeaningNid() != meaning.nid())
			throw new IllegalStateException(node + " can only have " + meaning + " for a child. Concept: " + conceptNid
					+ " definition: " + definition);
		return child;
	}

	private void checkRoleOperator(EntityVertex node) {
		int role_operator_nid = getNid(node, TinkarTerm.ROLE_OPERATOR);
		if (role_operator_nid != TinkarTerm.EXISTENTIAL_RESTRICTION.nid())
			throw new UnsupportedOperationException(
					"Role: " + PrimitiveData.text(role_operator_nid) + " not supported. ");
	}

	// This is just used to support the WriteTest ITs
	public Concept buildConcept(SemanticEntityVersion semanticEntityVersion) {
		return (Concept) processDefinition(semanticEntityVersion);
	}

	private SnomedEntity processDefinition(SemanticEntityVersion semanticEntityVersion) {
		SnomedEntity ret = null;
		int conceptNid = semanticEntityVersion.referencedComponentNid();
		DiTreeEntity definition = (DiTreeEntity) semanticEntityVersion.fieldValues().getFirst();
		EntityVertex root = definition.root();
		if (definition.successors(root).isEmpty()) {
			LOG.warn("No definition for: " + PrimitiveData.text(conceptNid));
		}
		for (EntityVertex child : definition.successors(root)) {
			SnomedEntity result = null;
			switch (getMeaning(child)) {
			case SUFFICIENT_SET -> {
				Concept concept = data.getOrCreateConcept(conceptNid);
				Definition def = new Definition();
				def.setDefinitionType(DefinitionType.EquivalentConcept);
				processDefinition(def, child, definition);
				concept.addDefinition(def);
				result = concept;
			}
			case NECESSARY_SET -> {
				Concept concept = data.getOrCreateConcept(conceptNid);
				Definition def = new Definition();
				def.setDefinitionType(DefinitionType.SubConcept);
				processDefinition(def, child, definition);
				concept.addDefinition(def);
				result = concept;
			}
			case INCLUSION_SET -> {
				Concept concept = data.getOrCreateConcept(conceptNid);
				Definition def = new Definition();
				def.setDefinitionType(DefinitionType.SubConcept);
				processDefinition(def, child, definition);
				concept.addGciDefinition(def);
				result = concept;
			}
			case PROPERTY_SET -> {
				processPropertySet(conceptNid, child, definition);
				result = data.getRoleType(conceptNid);
			}
			case DATA_PROPERTY_SET -> {
				processDataPropertySet(conceptNid, child, definition);
				result = data.getConcreteRoleType(conceptNid);
			}
			case INTERVAL_PROPERTY_SET -> {
				LOG.info("Interval PS: " + PrimitiveData.text(conceptNid) + "\n" + definition);
				processDataPropertySet(conceptNid, child, definition);
				data.addIntervalRoleType(data.getConcreteRoleType(conceptNid));
				result = data.getConcreteRoleType(conceptNid);
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + getMeaning(child));
			}
			if (ret == null)
				ret = result;
			if (ret != result)
				throw new IllegalStateException("Expect " + ret.getClass() + " was " + result.getClass());
		}
		return ret;
	}

	private void processDefinition(Definition def, EntityVertex node, DiTreeEntity definition) {
		EntityVertex child = getFirstChildCheck(-1, node, definition, null);
		switch (getMeaning(child)) {
		case AND -> {
			processAnd(def, child, definition);
		}
		case CONCEPT -> {
			int nid = getNid(child, TinkarTerm.CONCEPT_REFERENCE);
			def.addSuperConcept(data.getOrCreateConcept(nid));
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + getMeaning(child));
		}
	}

	private void processPropertySet(int conceptNid, EntityVertex propertySetNode, DiTreeEntity definition) {
		if (log_property_sets)
			LOG.info("PropertySet: " + PrimitiveData.text(conceptNid) + " " + propertySetNode + "\n" + definition);
		EntityVertex child = getFirstChildCheck(conceptNid, propertySetNode, definition, TinkarTerm.AND);
		for (EntityVertex node : definition.successors(child)) {
			switch (getMeaning(node)) {
			case CONCEPT -> {
				ConceptFacade nodeConcept = node.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
				RoleType roleType = data.getOrCreateRoleType(conceptNid);
				if (nodeConcept.nid() == TinkarTerm.TRANSITIVE_PROPERTY.nid()) {
					roleType.setTransitive(true);
				} else if (nodeConcept.nid() == TinkarTerm.REFLEXIVE_PROPERTY.nid()) {
					roleType.setReflexive(true);
				} else {
					roleType.addSuperRoleType(data.getOrCreateRoleType(nodeConcept.nid()));
				}
			}
			case PROPERTY_SEQUENCE_IMPLICATION -> {
				RoleType roleType = data.getOrCreateRoleType(conceptNid);
				ConceptFacade ppi = node.propertyFast(TinkarTerm.PROPERTY_SEQUENCE_IMPLICATION);
				if (ppi.nid() != conceptNid)
					throw new IllegalStateException(
							"Property chain malformed. Concept: " + conceptNid + " definition: " + definition);
				IntIdList ps = node.propertyFast(TinkarTerm.PROPERTY_SEQUENCE);
				if (ps == null)
					throw new IllegalStateException(
							"Property chain malformed. Expected " + TinkarTerm.PROPERTY_SEQUENCE.description()
									+ " Concept: " + conceptNid + " definition: " + definition);
				if (ps.size() != 2)
					throw new IllegalStateException("Property chain " + ps.size() + " != 2. Concept: " + conceptNid
							+ " definition: " + definition);
				if (ps.get(0) != conceptNid)
					throw new IllegalStateException(
							"Property chain malformed. Concept: " + conceptNid + " definition: " + definition);
				RoleType prop1 = data.getOrCreateRoleType(ps.get(0));
				RoleType prop2 = data.getOrCreateRoleType(ps.get(1));
				if (!roleType.equals(prop1))
					throw new IllegalStateException("This is a bug.");
				roleType.setChained(prop2);
			}
			default -> throw new UnsupportedOperationException("Can't handle: " + node + " in: " + definition);
			}
		}
	}

	private void processDataPropertySet(int conceptNid, EntityVertex propertySetNode, DiTreeEntity definition) {
		if (log_property_sets)
			LOG.info("DataPropertySet: " + PrimitiveData.text(conceptNid) + " " + propertySetNode + "\n" + definition);
		EntityVertex child = getFirstChildCheck(conceptNid, propertySetNode, definition, TinkarTerm.AND);
		for (EntityVertex node : definition.successors(child)) {
			switch (getMeaning(node)) {
			case CONCEPT -> {
				ConceptFacade nodeConcept = node.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
				ConcreteRoleType roleType = data.getOrCreateConcreteRoleType(conceptNid);
				roleType.addSuperConcreteRoleType(data.getOrCreateConcreteRoleType(nodeConcept.nid()));
			}
			default -> throw new UnsupportedOperationException("Can't handle: " + node + " in: " + definition);
			}
		}
	}

	private void processAnd(Definition def, EntityVertex node, DiTreeEntity definition) {
		for (EntityVertex child : definition.successors(node)) {
			switch (getMeaning(child)) {
			case CONCEPT -> {
				int concept_nid = getNid(child, TinkarTerm.CONCEPT_REFERENCE);
				def.addSuperConcept(data.getOrCreateConcept(concept_nid));
			}
			case ROLE -> {
				checkRoleOperator(child);
				int role_type_nid = getNid(child, TinkarTerm.ROLE_TYPE);
				if (role_type_nid == TinkarTerm.ROLE_GROUP.nid()) {
					// TODO Placeholder for now so the tests work
//					data.getOrCreateRoleType(role_type_nid);
					processRoleGroup(def, child, definition);
				} else {
					Role role = makeRole(child, definition);
					def.addUngroupedRole(role);
				}
			}
			case FEATURE -> {
				ConcreteRole role = makeConcreteRole(child, definition);
				def.addUngroupedConcreteRole(role);
			}
			case INTERVAL_ROLE -> {
				ConcreteRole role = makeIntervalRole(child, definition);
				def.addUngroupedConcreteRole(role);
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + getMeaning(child));
			}
		}
	}

	private Role makeRole(EntityVertex node, DiTreeEntity definition) {
		EntityVertex child = getFirstChildCheck(-1, node, definition, null);
		int role_type_nid = getNid(node, TinkarTerm.ROLE_TYPE);
		RoleType role_type = data.getOrCreateRoleType(role_type_nid);
		int concept_nid = getNid(child, TinkarTerm.CONCEPT_REFERENCE);
		return new Role(role_type, data.getOrCreateConcept(concept_nid));
	}

	private ConcreteRole makeConcreteRole(EntityVertex node, DiTreeEntity definition) {
		int role_type_nid = getNid(node, TinkarTerm.FEATURE_TYPE);
		ConcreteRoleType role_type = data.getOrCreateConcreteRoleType(role_type_nid);
		Object value = node.propertyFast(TinkarTerm.LITERAL_VALUE);
		ValueType value_type = switch (value) {
		case BigDecimal _ -> ValueType.Decimal;
		case Double _ -> ValueType.Double;
		case Float _ -> ValueType.Float;
		case Integer _ -> ValueType.Integer;
		case String _ -> ValueType.String;
        case Long _ -> ValueType.Long;
		default -> throw new UnsupportedOperationException("Value type: " + value.getClass().getName());
		};
		return new ConcreteRole(role_type, value.toString(), value_type);
	}

	private ConcreteRole makeIntervalRole(EntityVertex node, DiTreeEntity definition) {
		int role_type_nid = getNid(node, TinkarTerm.INTERVAL_ROLE_TYPE);
		ConcreteRoleType role_type = data.getOrCreateConcreteRoleType(role_type_nid);
		data.addIntervalRoleType(role_type);
		Interval interval = IntervalUtil.makeInterval(node);
//		LOG.info(">>>>>" + getIntervalRoleString(viewCalculator, node));
		return new ConcreteRole(role_type, interval.toString(), ValueType.String);
	}

	private void processRoleGroup(Definition def, EntityVertex node, DiTreeEntity definition) {
		EntityVertex child = getFirstChildCheck(-1, node, definition, null);
		switch (getMeaning(child)) {
		case ROLE -> {
			checkRoleOperator(child);
			RoleGroup rg = new RoleGroup();
			def.addRoleGroup(rg);
			Role role = makeRole(child, definition);
			rg.addRole(role);
		}
		case FEATURE -> {
			RoleGroup rg = new RoleGroup();
			def.addRoleGroup(rg);
			ConcreteRole role = makeConcreteRole(child, definition);
			rg.addConcreteRole(role);
		}
		case AND -> {
			processRoleGroupAnd(def, child, definition);
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + getMeaning(child));
		}
	}

	private void processRoleGroupAnd(Definition def, EntityVertex node, DiTreeEntity definition) {
		RoleGroup rg = new RoleGroup();
		def.addRoleGroup(rg);
		for (EntityVertex child : definition.successors(node)) {
			switch (getMeaning(child)) {
			case ROLE -> {
				checkRoleOperator(child);
				Role role = makeRole(child, definition);
				rg.addRole(role);
			}
			case FEATURE -> {
				ConcreteRole role = makeConcreteRole(child, definition);
				rg.addConcreteRole(role);
			}
			case INTERVAL_ROLE -> {
				ConcreteRole role = makeIntervalRole(child, definition);
				rg.addConcreteRole(role);
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + getMeaning(child));
			}
		}
	}

}
