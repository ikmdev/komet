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
package dev.ikm.tinkar.reasoner.elkowl;

import java.util.UUID;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom.Atom.ConceptAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;

public class TempEditUtil {

	private static final Logger LOG = LoggerFactory.getLogger(TempEditUtil.class);

	private ViewCalculator viewCalculator;

	private PatternFacade statedAxiomPattern;

	public TempEditUtil(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern) {
		super();
		this.viewCalculator = viewCalculator;
		this.statedAxiomPattern = statedAxiomPattern;
	}

	private int getStatedSemanticNid(int concept) {
		int[] statedSemanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(concept,
				statedAxiomPattern.nid());
		if (statedSemanticNids.length == 0)
			throw new IllegalStateException("No stated form for concept: " + PrimitiveData.text(concept));
		if (statedSemanticNids.length > 1)
			throw new IllegalStateException("More than one stated form for concept: " + PrimitiveData.text(concept));
		return statedSemanticNids[0];
	}

	private void update(int statedSemanticNid, LogicalExpression newStatedExpression) {
		Transaction updateStatedTransaction = Transaction.make();
		StampEntity<?> updateStamp = updateStatedTransaction.getStamp(State.ACTIVE,
				viewCalculator.viewCoordinateRecord().getAuthorNidForChanges(),
				viewCalculator.viewCoordinateRecord().getDefaultModuleNid(),
				viewCalculator.viewCoordinateRecord().getDefaultPathNid());
		updateStatedTransaction.addComponent(statedSemanticNid);
		SemanticRecord statedSemanticRecord = viewCalculator.updateFields(statedSemanticNid,
				Lists.immutable.of(newStatedExpression.sourceGraph()), updateStamp.nid());
		EntityService.get().putEntity(statedSemanticRecord);
		updateStatedTransaction.commit();
	}

	public DiTreeEntity setParent(UUID conceptUuid, UUID parentUuid) {
		int conceptNid = PrimitiveData.nid(conceptUuid);
		int parentNid = PrimitiveData.nid(parentUuid);
		int statedSemanticNid = getStatedSemanticNid(conceptNid);
		Latest<SemanticEntityVersion> latestStatedSemantic = viewCalculator.latest(statedSemanticNid);
		LOG.info("Stated: " + latestStatedSemantic.get());
		DiTreeEntity def = (DiTreeEntity) latestStatedSemantic.get().fieldValues().getFirst();
		LOG.info("Def: " + def);

		LogicalExpressionBuilder statedBuilder = new LogicalExpressionBuilder();
		ImmutableList<ConceptAxiom> parents = Lists.immutable.of(statedBuilder.ConceptAxiom(parentNid));
		statedBuilder.NecessarySet(statedBuilder.And(parents));
		LogicalExpression newStatedExpression = statedBuilder.build();

		update(statedSemanticNid, newStatedExpression);

		DiTree<EntityVertex> sg = newStatedExpression.sourceGraph();
		return new DiTreeEntity(sg.root(), sg.vertexMap(), sg.successorMap(), sg.predecessorMap());
	}

	public DiTreeEntity makeEquivalent(UUID conceptUuid) {
		int conceptNid = PrimitiveData.nid(conceptUuid);
		int statedSemanticNid = getStatedSemanticNid(conceptNid);
		Latest<SemanticEntityVersion> latestStatedSemantic = viewCalculator.latest(statedSemanticNid);
		LOG.info("Stated: " + latestStatedSemantic.get());
		DiTreeEntity def = (DiTreeEntity) latestStatedSemantic.get().fieldValues().getFirst();
		LOG.info("Def: " + def);

		LogicalExpressionBuilder statedBuilder = new LogicalExpressionBuilder(def);
		LogicalAxiom axiom = statedBuilder.get(7);
		LOG.info("Axiom: " + axiom);
		switch (axiom) {
		case LogicalAxiom.LogicalSet setAxiom -> {
			statedBuilder.changeSetType(setAxiom, TinkarTerm.SUFFICIENT_SET);
		}
		default -> throw new IllegalStateException("Unexpected value: " + axiom);
		}
		LogicalExpression newStatedExpression = statedBuilder.build();

		update(statedSemanticNid, newStatedExpression);

		DiTree<EntityVertex> sg = newStatedExpression.sourceGraph();
		DiTreeEntity new_def = new DiTreeEntity(sg.root(), sg.vertexMap(), sg.successorMap(), sg.predecessorMap());
		LOG.info("Def: " + new_def);
		return new_def;
	}

}
