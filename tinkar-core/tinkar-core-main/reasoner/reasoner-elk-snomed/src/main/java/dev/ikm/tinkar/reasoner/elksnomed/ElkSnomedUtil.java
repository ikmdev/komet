package dev.ikm.tinkar.reasoner.elksnomed;

import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;

public class ElkSnomedUtil {

	private static int getStatedSemanticNid(int conceptNid) {
		int[] statedSemanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid,
				TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid());
		if (statedSemanticNids.length == 0)
			throw new IllegalStateException("No stated form for concept: " + PrimitiveData.text(conceptNid));
		if (statedSemanticNids.length > 1)
			throw new IllegalStateException("More than one stated form for concept: " + PrimitiveData.text(conceptNid));
		return statedSemanticNids[0];
	}

	public static SemanticEntityVersion getStatedSemantic(ViewCalculator viewCalculator, int conceptNid) {
		int statedSemanticNid = getStatedSemanticNid(conceptNid);
		Latest<SemanticEntityVersion> latestStatedSemantic = viewCalculator.latest(statedSemanticNid);
		return latestStatedSemantic.get();
	}

	public static Concept buildConcept(SemanticEntityVersion semanticEntityVersion) {
		ElkSnomedDataBuilder builder = new ElkSnomedDataBuilder(null, null, new ElkSnomedData());
		return builder.buildConcept(semanticEntityVersion);
	}

	public static Concept getConcept(ViewCalculator viewCalculator, int conceptNid) {
		SemanticEntityVersion sev = ElkSnomedUtil.getStatedSemantic(viewCalculator, conceptNid);
		return ElkSnomedUtil.buildConcept(sev);
	}

	public static void updateStatedSemantic(ViewCalculator viewCalculator, int conceptNid,
			LogicalExpression newStatedExpression) {
		int statedSemanticNid = getStatedSemanticNid(conceptNid);
		Transaction updateStatedTransaction = Transaction.make();
		StampEntity<?> updateStamp = updateStatedTransaction.getStamp(State.ACTIVE,
				viewCalculator.viewCoordinateRecord().getAuthorNidForChanges(),
				viewCalculator.viewCoordinateRecord().getDefaultModuleNid(),
				viewCalculator.viewCoordinateRecord().getDefaultPathNid());
		updateStatedTransaction.addComponent(statedSemanticNid);
		Entity<? extends EntityVersion> statedSemanticRecord = viewCalculator.updateFields(statedSemanticNid,
				Lists.immutable.of(newStatedExpression.sourceGraph()), updateStamp.nid());
		EntityService.get().putEntity(statedSemanticRecord);
		updateStatedTransaction.commit();
	}

	@SuppressWarnings("serial")
	public static class SemanticStateException extends IllegalStateException {

		public SemanticStateException() {
			super();
		}

		public SemanticStateException(String message, Throwable cause) {
			super(message, cause);
		}

		public SemanticStateException(String s) {
			super(s);
		}

		public SemanticStateException(Throwable cause) {
			super(cause);
		}

	}

	public static SemanticEntityVersion getLatestSemantic(ViewCalculator vc, int patternNid, int nid) {
		int[] semanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(nid, patternNid);
		if (semanticNids.length == 1) {
			Latest<SemanticEntityVersion> latestSemantic = vc.latest(semanticNids[0]);
			if (latestSemantic.isPresent())
				return latestSemantic.get();
			throw new SemanticStateException("No LATEST semantic of pattern " + PrimitiveData.text(patternNid)
					+ " for component: " + PrimitiveData.text(nid));
		}
		if (semanticNids.length == 0)
			throw new SemanticStateException("No semantic of pattern " + PrimitiveData.text(patternNid)
					+ " for component: " + PrimitiveData.text(nid));
		throw new SemanticStateException("More than one semantic of pattern " + PrimitiveData.text(patternNid)
				+ " for component: " + PrimitiveData.text(nid));
	}

	public static Set<Integer> getInferredParents(ViewCalculator vc, long sctid) {
		int nid = ElkSnomedData.getNid(sctid);
		SemanticEntityVersion sev = getLatestSemantic(vc, TinkarTerm.INFERRED_NAVIGATION_PATTERN.nid(), nid);
		ImmutableList<Object> latestInferredNavigationFields = sev.fieldValues();
		IntIdSet parent_nids = (IntIdSet) latestInferredNavigationFields.get(1);
		return parent_nids.mapToSet(x -> x);
	}

	public static Set<Integer> getInferredChildren(ViewCalculator vc, long sctid) {
		int nid = ElkSnomedData.getNid(sctid);
		SemanticEntityVersion sev = getLatestSemantic(vc, TinkarTerm.INFERRED_NAVIGATION_PATTERN.nid(), nid);
		ImmutableList<Object> latestInferredNavigationFields = sev.fieldValues();
		IntIdSet parent_nids = (IntIdSet) latestInferredNavigationFields.get(0);
		return parent_nids.mapToSet(x -> x);
	}

}
