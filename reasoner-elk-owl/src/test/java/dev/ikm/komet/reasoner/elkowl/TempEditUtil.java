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

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;

public class TempEditUtil {

	private ViewCalculator viewCalculator;

	private PatternFacade statedAxiomPattern;

	public TempEditUtil(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern) {
		super();
		this.viewCalculator = viewCalculator;
		this.statedAxiomPattern = statedAxiomPattern;
	}

	public DiTreeEntity setParent(int concept, int parent) {
		// Update stated definition...
		int[] statedSemanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(concept,
				statedAxiomPattern.nid());
		if (statedSemanticNids.length == 0) { // Solor concept length == 0...
			if (concept != TinkarTerm.SOLOR_CONCEPT.nid()) {
				AlertStreams.dispatchToRoot(
						new IllegalStateException("No stated form for concept: " + PrimitiveData.text(concept)));
			}
			return null;
		}
		if (statedSemanticNids.length > 1) {
			AlertStreams.dispatchToRoot(
					new IllegalStateException("More than one stated form for concept: " + PrimitiveData.text(concept)));
		}
		Latest<SemanticEntityVersion> latestStatedSemantic = viewCalculator.latest(statedSemanticNids[0]);

		final LogicalExpressionBuilder statedBuilder = new LogicalExpressionBuilder();
		final MutableList<LogicalAxiom.Atom.ConceptAxiom> parentList = Lists.mutable.withInitialCapacity(1);
		parentList.add(statedBuilder.ConceptAxiom(parent));
		statedBuilder.NecessarySet(statedBuilder.And(parentList.toImmutable()));
		final LogicalExpression newStatedExpression = statedBuilder.build();

		Transaction updateStatedTransaction = Transaction.make();
		StampEntity updateStamp = updateStatedTransaction.getStamp(State.ACTIVE,
				viewCalculator.viewCoordinateRecord().getAuthorNidForChanges(),
				viewCalculator.viewCoordinateRecord().getDefaultModuleNid(),
				viewCalculator.viewCoordinateRecord().getDefaultPathNid());

		updateStatedTransaction.addComponent(statedSemanticNids[0]);
		SemanticRecord statedSemanticRecord = viewCalculator.updateFields(statedSemanticNids[0],
				Lists.immutable.of(newStatedExpression.sourceGraph()), updateStamp.nid());
		EntityService.get().putEntity(statedSemanticRecord);
		updateStatedTransaction.commit();

		DiTree<EntityVertex> sg = newStatedExpression.sourceGraph();
		return new DiTreeEntity(sg.root(), sg.vertexMap(), sg.successorMap(), sg.predecessorMap());
	}

}
