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
package dev.ikm.komet.rules.annotated;

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.MethodPredicate;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;

import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.rules.actions.axiom.AddDataPropertySet;
import dev.ikm.komet.rules.actions.axiom.AddFeature;
import dev.ikm.komet.rules.actions.axiom.AddIntervalRole;
import dev.ikm.komet.rules.actions.axiom.AddIntervalPropertySet;
import dev.ikm.komet.rules.actions.axiom.AddIsA;
import dev.ikm.komet.rules.actions.axiom.AddNecessarySet;
import dev.ikm.komet.rules.actions.axiom.AddPropertySet;
import dev.ikm.komet.rules.actions.axiom.AddRoleGroup;
import dev.ikm.komet.rules.actions.axiom.AddSomeRole;
import dev.ikm.komet.rules.actions.axiom.AddSufficientSet;
import dev.ikm.komet.rules.actions.axiom.ChangeConcept;
import dev.ikm.komet.rules.actions.axiom.ChangeFeatureOperator;
import dev.ikm.komet.rules.actions.axiom.ChangeFeatureType;
import dev.ikm.komet.rules.actions.axiom.ChangeIntervalBound;
import dev.ikm.komet.rules.actions.axiom.ChangeIntervalBoundOpen;
import dev.ikm.komet.rules.actions.axiom.ChangeIntervalRoleType;
import dev.ikm.komet.rules.actions.axiom.ChangeIntervalRoleUnitOfMeasure;
import dev.ikm.komet.rules.actions.axiom.ChangeIntervalValuesMenu;
import dev.ikm.komet.rules.actions.axiom.ChangeRoleRestriction;
import dev.ikm.komet.rules.actions.axiom.ChangeRoleType;
import dev.ikm.komet.rules.actions.axiom.ChangeSetType;
import dev.ikm.komet.rules.actions.axiom.ChooseConceptMenu;
import dev.ikm.komet.rules.actions.axiom.ChooseConcreteOperatorMenu;
import dev.ikm.komet.rules.actions.axiom.RemoveAxiomAction;
import dev.ikm.komet.rules.actions.axiom.SetValueMenu;
import dev.ikm.tinkar.ext.lang.owl.IntervalUtil;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.control.MenuItem;

/**
 * Rules related to axiom-related observations.
 */
@RuleSet("Axiom focus rules")
public class AxiomFocusedRules extends RulesBase {

	/**
	 * @see RulesBase#isAxiomFocused(ObservationRecord)
	 * @see RulesBase#isNotDefinitionRoot(ObservationRecord)
	 */
	@Rule("Axiom of interest is not the definition root")
	@Where(methods = { @MethodPredicate(method = "isAxiomFocused", args = { "$observation" }),
			@MethodPredicate(method = "isNotDefinitionRoot", args = { "$observation" }) })
	public void axiomIsNotDefinitionRoot(ObservationRecord $observation) {
		// TODO would be nice if Evrete recognized the pattern variable "axiomSubject"
		// and could pass it as a parameter.
		// TODO Great idea, noted.
		if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
			RemoveAxiomAction removeAxiomAction = new RemoveAxiomAction("Remove axiom", axiomSubjectRecord,
					calculator(), editCoordinate());
			addGeneratedActions(removeAxiomAction);
		}
	}

	/**
	 * @see RulesBase#isAxiomFocused(ObservationRecord)
	 * @see RulesBase#isDefinitionRoot(ObservationRecord)
	 */
	@Rule("Axiom of interest is the definition root")
	@Where(methods = { @MethodPredicate(method = "isAxiomFocused", args = { "$observation" }),
			@MethodPredicate(method = "isDefinitionRoot", args = { "$observation" }) })
	public void axiomIsDefinitionRoot(ObservationRecord $observation) {
		if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
			if (!axiomSubjectRecord.axiomTree().containsVertexWithMeaning(TinkarTerm.NECESSARY_SET)) {
				// allow addition of necessary set
				AddNecessarySet addNecessarySet = new AddNecessarySet("Add necessary set", axiomSubjectRecord,
						calculator(), editCoordinate());
				addGeneratedActions(addNecessarySet);
			}
			// always allow addition of sufficient set (multiple sufficient sets allowed)
			AddSufficientSet addSufficientSet = new AddSufficientSet("Add sufficient set", axiomSubjectRecord,
					calculator(), editCoordinate());
			addGeneratedActions(addSufficientSet);
			AddPropertySet addPropertySet = new AddPropertySet("Add property set", axiomSubjectRecord, calculator(),
					editCoordinate());
			addGeneratedActions(addPropertySet);
			AddDataPropertySet addDataPropertySet = new AddDataPropertySet("Add data property set", axiomSubjectRecord,
					calculator(), editCoordinate());
			addGeneratedActions(addDataPropertySet);
			AddIntervalPropertySet addIntervalPropertySet = new AddIntervalPropertySet("Add interval property set",
					axiomSubjectRecord, calculator(), editCoordinate());
			addGeneratedActions(addIntervalPropertySet);
		}
	}

	/**
	 * @see RulesBase#isAxiomFocused(ObservationRecord)
	 * @see RulesBase#isAxiomSet(ObservationRecord)
	 */
	@Rule("Axiom of interest is a set")
	@Where(methods = { @MethodPredicate(method = "isAxiomFocused", args = { "$observation" }),
			@MethodPredicate(method = "isAxiomSet", args = { "$observation" }) })
	public void axiomIsSet(ObservationRecord $observation) {
		if ($observation.subject() instanceof AxiomSubjectRecord axiomSubject) {
			if (axiomSubject.axiomMeaning().equals(TinkarTerm.NECESSARY_SET)) {
				ChangeSetType changeToSufficientSet = new ChangeSetType(TinkarTerm.SUFFICIENT_SET,
						"Change to sufficient set", axiomSubject, calculator(), editCoordinate());
				addGeneratedActions(changeToSufficientSet);
			} else if (axiomSubject.axiomMeaning().equals(TinkarTerm.SUFFICIENT_SET)) {
				ChangeSetType changeToNecessarySet = new ChangeSetType(TinkarTerm.NECESSARY_SET,
						"Change to necessary set", axiomSubject, calculator(), editCoordinate());
				addGeneratedActions(changeToNecessarySet);
			}

			AddIsA addIsA = new AddIsA("Add is-a", axiomSubject, calculator(), editCoordinate());
			AddSomeRole addSomeRole = new AddSomeRole("Add role", axiomSubject, calculator(), editCoordinate());
			AddIntervalRole addIntervalRole = new AddIntervalRole("Add interval role", axiomSubject, calculator(),
					editCoordinate());
			AddRoleGroup addRoleGroup = new AddRoleGroup("Add role group", axiomSubject, calculator(),
					editCoordinate());
			AddFeature addFeature = new AddFeature("Add concrete role", axiomSubject, calculator(), editCoordinate());
			addGeneratedActions(addIsA, addSomeRole, addIntervalRole, addRoleGroup, addFeature);
		}
	}

	/**
	 * @see RulesBase#isAxiomFocused(ObservationRecord)
	 * @see RulesBase#isAxiomConcept(ObservationRecord)
	 */
	@Rule("Axiom of interest is a concept axiom")
	@Where(methods = { @MethodPredicate(method = "isAxiomFocused", args = { "$observation" }),
			@MethodPredicate(method = "isAxiomConcept", args = { "$observation" }) })
	public void axiomIsConceptAxiom(ObservationRecord $observation, RhsContext ctx) {
		if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
			addConsequenceMenu(new ChooseConceptMenu("Choose replacement is-a", calculator(),
					axiomSubjectRecord.nodeForPopover(), viewProperties(), o -> {
						ChangeConcept changeConcept = new ChangeConcept("Change is-a", o, axiomSubjectRecord,
								calculator(), editCoordinate());
						changeConcept.doAction();
					}));
		}
	}

	/**
	 * @see RulesBase#isAxiomFocused(ObservationRecord)
	 * @see RulesBase#isAxiomRoleGroup(ObservationRecord)
	 */
	@Rule("Axiom of interest is a role group")
	@Where(methods = { @MethodPredicate(method = "isAxiomFocused", args = { "$observation" }),
			@MethodPredicate(method = "isAxiomRoleGroup", args = { "$observation" }) })
	public void axiomIsRoleGroup(ObservationRecord $observation, RhsContext ctx) {

		if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {
			AddSomeRole addRole = new AddSomeRole("Add role", axiomSubjectRecord, calculator(), editCoordinate());
			AddIntervalRole addIntervalRole = new AddIntervalRole("Add interval role", axiomSubjectRecord, calculator(),
					editCoordinate());
			AddFeature addFeature = new AddFeature("Add concrete role", axiomSubjectRecord, calculator(),
					editCoordinate());
			addGeneratedActions(addRole, addIntervalRole, addFeature);
		}
	}

	/**
	 * @see RulesBase#isAxiomFocused(ObservationRecord)
	 * @see RulesBase#isAxiomRoleOnly(ObservationRecord)
	 */
	@Rule("Axiom of interest is a role but not a role group")
	@Where(methods = { @MethodPredicate(method = "isAxiomFocused", args = { "$observation" }),
			@MethodPredicate(method = "isAxiomRoleOnly", args = { "$observation" }) })
	public void axiomIsRoleButNotARoleGroup(ObservationRecord $observation) {

		if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {

			addConsequenceMenu(new ChooseConceptMenu("Choose role type", calculator(),
					axiomSubjectRecord.nodeForPopover(), viewProperties(), o -> {
						ChangeRoleType changeRoleType = new ChangeRoleType("Change role type", o, axiomSubjectRecord,
								calculator(), editCoordinate());
						changeRoleType.doAction();
					}),

					new ChooseConceptMenu("Choose role restriction", calculator(), axiomSubjectRecord.nodeForPopover(),
							viewProperties(), o -> {
								ChangeRoleRestriction changeRoleRestriction = new ChangeRoleRestriction(
										"Change role restriction", o, axiomSubjectRecord, calculator(),
										editCoordinate());
								changeRoleRestriction.doAction();
							}));
		}
	}

	/**
	 * @see RulesBase#isAxiomFocused(ObservationRecord)
	 * @see RulesBase#isAxiomIntervalRole(ObservationRecord)
	 */
	@Rule("Axiom of interest is an interval role")
	@Where(methods = { @MethodPredicate(method = "isAxiomFocused", args = { "$observation" }),
			@MethodPredicate(method = "isAxiomIntervalRole", args = { "$observation" }) })
	public void axiomIsIntervalRole(ObservationRecord $observation) {

		if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {

			addConsequenceMenu(new ChooseConceptMenu("Choose interval role type", calculator(),
					axiomSubjectRecord.nodeForPopover(), viewProperties(), o -> {
						ChangeIntervalRoleType changeRoleType = new ChangeIntervalRoleType("Change interval role type",
								o, axiomSubjectRecord, calculator(), editCoordinate());
						changeRoleType.doAction();
					}),
					// Alternative to inline block below
					// new ChangeIntervalValuesMenu("Change interval values", calculator(),
					// editCoordinate(),
					// axiomSubjectRecord),

					new ChooseConceptMenu("Choose unit of measure", calculator(), axiomSubjectRecord.nodeForPopover(),
							viewProperties(), o -> {
								ChangeIntervalRoleUnitOfMeasure changeUnitOfMeasure = new ChangeIntervalRoleUnitOfMeasure(
										"Change unit of measure", o, axiomSubjectRecord, calculator(),
										editCoordinate());
								changeUnitOfMeasure.doAction();
							}));

			{
				Interval interval = IntervalUtil.makeInterval(axiomSubjectRecord.getAxiomVertex());
				addGeneratedActions(
						new ChangeIntervalBoundOpen(interval, true,
								"Set Lower Bound " + (interval.isLowerOpen() ? "Closed" : "Open"), axiomSubjectRecord,
								calculator(), editCoordinate()),
						new ChangeIntervalBound(interval, true, "Set Lower Bound", axiomSubjectRecord, calculator(),
								editCoordinate()),
						new ChangeIntervalBound(interval, false, "Set Upper Bound", axiomSubjectRecord, calculator(),
								editCoordinate()),
						new ChangeIntervalBoundOpen(interval, false,
								"Set Upper Bound " + (interval.isUpperOpen() ? "Closed" : "Open"), axiomSubjectRecord,
								calculator(), editCoordinate()));
			}
		}
	}

	/**
	 * @see RulesBase#isAxiomFocused(ObservationRecord)
	 * @see RulesBase#isAxiomFeature(ObservationRecord)
	 */
	@Rule("Axiom of interest is a feature")
	@Where(methods = { @MethodPredicate(method = "isAxiomFocused", args = { "$observation" }),
			@MethodPredicate(method = "isAxiomFeature", args = { "$observation" }) })
	public void axiomIsFeature(ObservationRecord $observation) {

		if ($observation.subject() instanceof AxiomSubjectRecord axiomSubjectRecord) {

			addConsequenceMenu(new ChooseConceptMenu("Choose concrete role type", calculator(),
					axiomSubjectRecord.nodeForPopover(), viewProperties(), o -> {
						ChangeFeatureType changeFeatureType = new ChangeFeatureType("Change concrete role type", o,
								axiomSubjectRecord, calculator(), editCoordinate());
						changeFeatureType.doAction();
					}),

					new ChooseConcreteOperatorMenu("Choose comparison", calculator(), o -> {
						ChangeFeatureOperator changeFeatureOperator = new ChangeFeatureOperator("Change comparison", o,
								axiomSubjectRecord, calculator(), editCoordinate());
						changeFeatureOperator.doAction();
					}), new SetValueMenu("Set value", calculator(), editCoordinate(), axiomSubjectRecord));
		}
	}
}