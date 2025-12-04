package dev.ikm.tinkar.ext.lang.owl;

import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.Role;
import dev.ikm.elk.snomed.model.RoleGroup;
import dev.ikm.elk.snomed.model.RoleType;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom.Atom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom.Atom.Connective.And;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom.Atom.TypedAtom.IntervalRole;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OwlElToLogicalExpression {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(OwlElToLogicalExpression.class);

	protected LogicalExpressionBuilder builder;

	private Set<ConcreteRoleType> intervalRoleTypes = Set.of();

	public OwlElToLogicalExpression() {
		super();
	}

	public OwlElToLogicalExpression(Set<ConcreteRoleType> intervalRoleTypes) {
		this.intervalRoleTypes = intervalRoleTypes;
	}

	public LogicalExpression build(Definition def) throws Exception {
		builder = new LogicalExpressionBuilder();
		processDefinition(def, false);
		return builder.build();
	}

	private ConceptFacade getConceptFacade(long id) {
		// TODO Maybe (ConceptFacade) EntityProxy.Concept.make((int) role_type.getId()
		Optional<? extends ConceptFacade> role_type_cf = EntityService.get().getEntity((int) id);
		return role_type_cf.get();
	}

	protected void process(RoleType role_type) {
		List<Atom> exprs = new ArrayList<>();
		for (RoleType sup : role_type.getSuperRoleTypes()) {
			exprs.add(builder.ConceptAxiom((int) sup.getId()));
		}
		if (role_type.isTransitive())
			exprs.add(builder.ConceptAxiom(TinkarTerm.TRANSITIVE_PROPERTY));
		if (role_type.isReflexive())
			exprs.add(builder.ConceptAxiom(TinkarTerm.REFLEXIVE_PROPERTY));
		if (role_type.getChained() != null) {
			ImmutableList<ConceptFacade> chain = Lists.immutable.of(getConceptFacade(role_type.getId()),
					getConceptFacade(role_type.getChained().getId()));
			exprs.add(builder.PropertySequenceImplicationAxiom(chain, getConceptFacade(role_type.getId())));
		}
		And expr = builder.And(toArray(exprs));
		builder.PropertySet(expr);
	}

	protected void process(ConcreteRoleType role_type) {
		List<Atom> exprs = new ArrayList<>();
		for (ConcreteRoleType sup : role_type.getSuperConcreteRoleTypes()) {
			exprs.add(builder.ConceptAxiom((int) sup.getId()));
		}
		And expr = builder.And(toArray(exprs));
		builder.DataPropertySet(expr);
	}

	protected void process(Concept con) {
		for (Definition def : con.getDefinitions()) {
			processDefinition(def, false);
		}
		for (Definition def : con.getGciDefinitions()) {
			processDefinition(def, true);
		}
	}

	private Atom[] toArray(List<Atom> exprs) {
		return exprs.toArray(new Atom[exprs.size()]);
	}

	protected void processDefinition(Definition def, boolean gci) {
		List<Atom> exprs = new ArrayList<>();
		for (Concept sup : def.getSuperConcepts()) {
			exprs.add(builder.ConceptAxiom((int) sup.getId()));
		}
		exprs.addAll(buildRoles(def.getUngroupedRoles()));
		exprs.addAll(buildConcreteRoles(def.getUngroupedConcreteRoles()));
		for (RoleGroup rg : def.getRoleGroups()) {
			List<Atom> roles = buildRoles(rg.getRoles());
			roles.addAll(buildConcreteRoles(rg.getConcreteRoles()));
			exprs.add(builder.SomeRole(TinkarTerm.ROLE_GROUP, builder.And(toArray(roles))));
		}
		And expr = builder.And(toArray(exprs));
		if (gci) {
			builder.InclusionSet(expr);
		} else {
			switch (def.getDefinitionType()) {
			case EquivalentConcept -> builder.SufficientSet(expr);
			case SubConcept -> builder.NecessarySet(expr);
			}
		}
	}

	private List<Atom> buildRoles(Set<Role> roles) {
		List<LogicalAxiom.Atom> exprs = new ArrayList<>();
		for (Role role : roles) {
			exprs.add(builder.SomeRole(getConceptFacade(role.getRoleType().getId()),
					builder.ConceptAxiom((int) role.getConcept().getId())));
		}
		return exprs;
	}

	private List<Atom> buildConcreteRoles(Set<ConcreteRole> roles) {
		List<LogicalAxiom.Atom> exprs = new ArrayList<>();
		for (ConcreteRole role : roles) {
			if (intervalRoleTypes.contains(role.getConcreteRoleType())) {
				Interval interval = Interval.fromString(role.getValue());
				IntervalRole interval_role = builder.IntervalRole(getConceptFacade(role.getConcreteRoleType().getId()),
						interval.getLowerBound(), interval.isLowerOpen(), interval.getUpperBound(),
						interval.isUpperOpen(), getConceptFacade(interval.getUnitOfMeasure().getId()));
				exprs.add(interval_role);
			} else {
				Object value = switch (role.getValueType()) {
				case Boolean -> Boolean.parseBoolean(role.getValue());
				case Decimal -> new BigDecimal(role.getValue());
				case Double -> Double.parseDouble(role.getValue());
				case Float -> Float.parseFloat(role.getValue());
				case Integer -> Integer.parseInt(role.getValue());
				case Long -> Long.parseLong(role.getValue());
				case String -> role.getValue();
				};
				exprs.add(builder.FeatureAxiom(getConceptFacade(role.getConcreteRoleType().getId()),
						TinkarTerm.EQUAL_TO, value));
			}
		}
		return exprs;
	}

}
