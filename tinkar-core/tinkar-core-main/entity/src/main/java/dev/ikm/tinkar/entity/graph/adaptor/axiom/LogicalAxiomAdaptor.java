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

import java.util.Optional;
import java.util.UUID;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract sealed class LogicalAxiomAdaptor implements LogicalAxiom {
	final LogicalExpression adaptedExpression;
	final int vertexIndex;

	public LogicalAxiomAdaptor(LogicalExpression adaptedExpression, int vertexIndex) {
		this.adaptedExpression = adaptedExpression;
		this.vertexIndex = vertexIndex;
		if (this.adaptedExpression.adaptors instanceof MutableList<LogicalAxiomAdaptor> adaptorList) {
			if (vertexIndex > -1 && vertexIndex < adaptorList.size()) {
				adaptorList.set(vertexIndex, this);
			} else if (vertexIndex == adaptorList.size()) {
				adaptorList.add(this);
			} else {
				while (vertexIndex > adaptorList.size()) {
					adaptorList.add(null);
				}
				adaptorList.add(this);
			}
		} else {
			throw new IllegalStateException("Adaptors is not an instanceof MutableList<LogicalAxiomAdaptor>");
		}
	}

	public int vertexIndex() {
		return vertexIndex;
	}

	@Override
	public UUID vertexUUID() {
		return adaptedExpression.sourceGraph.vertex(vertexIndex).asUuid();
	}

	protected <A extends LogicalAxiom> ImmutableSet<A> children(Class<A> setType) {
		ImmutableIntList sourceElements = adaptedExpression.sourceGraph.successors(vertexIndex);
		MutableSet<A> elements = Sets.mutable.ofInitialCapacity(sourceElements.size());
		for (int vertexIndex : sourceElements.toArray()) {
			elements.add((A) adaptedExpression.adaptors.get(vertexIndex));
		}
		return elements.toImmutable();
	}

	protected <O> O property(ConceptFacade propertyKey) {
		return adaptedExpression.sourceGraph.vertex(vertexIndex).propertyFast(propertyKey);
	}

	public static final class AndAdaptor extends LogicalAxiomAdaptor implements LogicalAxiom.Atom.Connective.And {

		public AndAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.AND);
		}

		@Override
		public ImmutableSet<Atom> elements() {
			return children(Atom.class);
		}
	}

	public static final class OrAdaptor extends LogicalAxiomAdaptor implements LogicalAxiom.Atom.Connective.Or {

		public OrAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.OR);
		}

		@Override
		public ImmutableSet<Atom> elements() {
			return children(Atom.class);
		}
	}

	public static final class ConceptAxiomAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.Atom.ConceptAxiom {

		public ConceptAxiomAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.CONCEPT_REFERENCE);
		}

		@Override
		public ConceptFacade concept() {
			return property(TinkarTerm.CONCEPT_REFERENCE);
		}

		@Override
		public String toString() {
			return "ConceptAxiomAdaptor: " + PrimitiveData.textWithNid(concept().nid());
		}
	}

	public static final class DisjointWithAxiomAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.Atom.DisjointWithAxiom {

		public DisjointWithAxiomAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.DISJOINT_WITH);
		}

		@Override
		public ConceptFacade disjointWith() {
			return property(TinkarTerm.DISJOINT_WITH);
		}
	}

	public static final class DefinitionRootAdaptor extends LogicalAxiomAdaptor implements LogicalAxiom.DefinitionRoot {

		public DefinitionRootAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.DEFINITION_ROOT);
		}

		@Override
		public ImmutableSet<LogicalSet> sets() {
			return children(LogicalSet.class);
		}
	}

	public static final class NecessarySetAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.LogicalSet.NecessarySet {

		public NecessarySetAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.NECESSARY_SET);
		}

		@Override
		public ImmutableSet<Atom.Connective> elements() {
			return children(Atom.Connective.class);
		}
	}

	public static final class SufficientSetAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.LogicalSet.SufficientSet {

		public SufficientSetAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.SUFFICIENT_SET);
		}

		@Override
		public ImmutableSet<Atom.Connective> elements() {
			return children(Atom.Connective.class);
		}
	}

	public static final class PropertySetAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.LogicalSet.PropertySet {

		public PropertySetAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.PROPERTY_SET);
		}

		@Override
		public ImmutableSet<Atom.Connective> elements() {
			return children(Atom.Connective.class);
		}
	}

	public static final class DataPropertySetAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.LogicalSet.DataPropertySet {

		public DataPropertySetAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.DATA_PROPERTY_SET);
		}

		@Override
		public ImmutableSet<Atom.Connective> elements() {
			return children(Atom.Connective.class);
		}
	}

	public static final class IntervalPropertySetAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.LogicalSet.IntervalPropertySet {

		public IntervalPropertySetAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning()
					.equals(TinkarTerm.INTERVAL_PROPERTY_SET);
		}

		@Override
		public ImmutableSet<Atom.Connective> elements() {
			return children(Atom.Connective.class);
		}
	}

	public static final class InclusionSetAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.LogicalSet.InclusionSet {

		public InclusionSetAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.INCLUSION_SET);
		}

		@Override
		public ImmutableSet<Atom.Connective> elements() {
			return children(Atom.Connective.class);
		}
	}

	public static final class RoleAxiomAdaptor extends LogicalAxiomAdaptor implements LogicalAxiom.Atom.TypedAtom.Role {

		public RoleAxiomAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.ROLE);
		}

		@Override
		public ConceptFacade type() {
			return property(TinkarTerm.ROLE_TYPE);
		}

		@Override
		public ConceptFacade roleOperator() {
			return property(TinkarTerm.ROLE_OPERATOR);
		}

		@Override
		public Atom restriction() {
			ImmutableSet<Atom> children = children(Atom.class);
			if (children.size() != 1) {
				throw new IllegalStateException("Should only be one child for restriction. Found: " + children);
			}
			return children.getOnly();
		}

		@Override
		public String toString() {
			return "RoleAxiomAdaptor: " + PrimitiveData.textWithNid(roleOperator().nid()) + " "
					+ PrimitiveData.textWithNid(type().nid()) + " " + restriction().toString();
		}
	}

	public static final class IntervalRoleAxiomAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.Atom.TypedAtom.IntervalRole {

		public IntervalRoleAxiomAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.INTERVAL_ROLE);
		}

		@Override
		public ConceptFacade type() {
			return property(TinkarTerm.INTERVAL_ROLE_TYPE);
		}

		@Override
		public String interval() {
			return property(TinkarTerm.LOWER_BOUND_OPEN) + " " + property(TinkarTerm.INTERVAL_LOWER_BOUND)
					+ " , " + property(TinkarTerm.INTERVAL_UPPER_BOUND) + " "
					+ property(TinkarTerm.UPPER_BOUND_OPEN) + " "
					+ property(TinkarTerm.UNIT_OF_MEASURE);
		}

		@Override
		public String toString() {
			return "IntervalRoleAxiomAdaptor: " + PrimitiveData.textWithNid(type().nid()) + " " + interval();
		}
	}

	public static final class FeatureAxiomAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.Atom.TypedAtom.Feature {

		public FeatureAxiomAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning().equals(TinkarTerm.FEATURE);
		}

		@Override
		public ConceptFacade type() {
			return property(TinkarTerm.FEATURE_TYPE);
		}

		@Override
		public Object literal() {
			return this.adaptedExpression.sourceGraph.vertex(vertexIndex).propertyFast(TinkarTerm.LITERAL_VALUE);
		}

		@Override
		public ConceptFacade concreteDomainOperator() {
			return property(TinkarTerm.CONCRETE_DOMAIN_OPERATOR);
		}
	}

	public static final class PropertySequenceImplicationAdaptor extends LogicalAxiomAdaptor
			implements LogicalAxiom.Atom.PropertySequenceImplication {

		public PropertySequenceImplicationAdaptor(LogicalExpression enclosingExpression, int vertexIndex) {
			super(enclosingExpression, vertexIndex);
			assert enclosingExpression.sourceGraph.vertex(vertexIndex).meaning()
					.equals(TinkarTerm.PROPERTY_SEQUENCE_IMPLICATION);
		}

		@Override
		public ImmutableList<ConceptFacade> propertySequence() {
			Optional<IntIdList> optionalPattern = this.adaptedExpression.sourceGraph.vertex(this.vertexIndex)
					.property(TinkarTerm.PROPERTY_SEQUENCE);
			if (optionalPattern.isPresent()) {
				IntIdList pattern = optionalPattern.get();
				return pattern.map(nid -> EntityProxy.Concept.make(nid));
			}
			throw new IllegalStateException("No property sequence found... ");
		}

		@Override
		public ConceptFacade implication() {
			throw new UnsupportedOperationException();
			// return
			// enclosingExpression.sourceGraph.vertex(vertexIndex).propertyFast(TinkarTerm);
		}
	}

}
