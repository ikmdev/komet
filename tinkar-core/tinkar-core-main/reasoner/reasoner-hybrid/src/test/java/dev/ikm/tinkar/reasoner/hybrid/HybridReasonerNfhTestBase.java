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
package dev.ikm.tinkar.reasoner.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.reasoner.hybrid.snomed.FamilyHistoryIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.ext.lang.owl.OwlElToLogicalExpression;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedUtil;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedUtil.SemanticStateException;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class HybridReasonerNfhTestBase extends HybridReasonerTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(HybridReasonerNfhTestBase.class);
	
	protected int expected_child_miss = -1;

	private void updateNfh() throws Exception {
		ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculator();
		SemanticEntityVersion sev = ElkSnomedUtil.getStatedSemantic(vc,
				ElkSnomedData.getNid(FamilyHistoryIds.no_family_history_swec));
		LOG.info("SEV:\n" + sev);
		Concept nfh_con = ElkSnomedUtil.getConcept(vc, ElkSnomedData.getNid(FamilyHistoryIds.no_family_history_swec));
		Definition def = nfh_con.getDefinitions().getFirst();
		def.setDefinitionType(DefinitionType.SubConcept);
		def.getSuperConcepts().clear();
		def.addSuperConcept(ElkSnomedUtil.getConcept(vc, ElkSnomedData.getNid(FamilyHistoryIds.family_history_swec)));
		def.addSuperConcept(ElkSnomedUtil.getConcept(vc, ElkSnomedData.getNid(FamilyHistoryIds.finding_swec)));
		def.getRoleGroups().clear();
		LogicalExpression le = new OwlElToLogicalExpression().build(def);
		LOG.info("LE:\n" + le);
		ElkSnomedUtil.updateStatedSemantic(vc, (int) nfh_con.getId(), le);
		// 704008007 |No family history of asthma (situation)|
		updateParent(704008007, FamilyHistoryIds.no_family_history_swec);
		// 160274005 |No family history of diabetes mellitus (situation)|
		updateParent(160274005, FamilyHistoryIds.no_family_history_swec);
		// 1344634002 |No family history of multiple sclerosis (situation)|
		updateParent(1344634002, FamilyHistoryIds.no_family_history_swec);
	}

	private void updateParent(long sctid, long parent_sctid) throws Exception {
		ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculator();
		Concept con = ElkSnomedUtil.getConcept(vc, ElkSnomedData.getNid(sctid));
		Definition def = con.getDefinitions().getFirst();
		def.getSuperConcepts().clear();
		Concept parent_con = ElkSnomedUtil.getConcept(vc, ElkSnomedData.getNid(parent_sctid));
		def.addSuperConcept(parent_con);
		LogicalExpression le = new OwlElToLogicalExpression().build(def);
		ElkSnomedUtil.updateStatedSemantic(vc, (int) con.getId(), le);
	}

	@Test
	public void nfh() throws Exception {
		updateNfh();
		ReasonerService rs = initReasonerService();
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		rs.buildNecessaryNormalForm();
		rs.writeInferredResults();
		SnomedIsa isas = SnomedIsa.init(rels_file);
		SnomedDescriptions descr = SnomedDescriptions.init(descriptions_file);
		int parent_miss = 0;
		int child_miss = 0;
		HashSet<Long> child_miss_sctids = new HashSet<>();
		for (long sctid : isas.getOrderedConcepts()) {
			if (sctid == FamilyHistoryIds.no_family_history_swec)
				continue;
			if (isas.hasAncestor(sctid, FamilyHistoryIds.no_family_history_swec))
				continue;
			int nid = ElkSnomedData.getNid(sctid);
			{
				Set<Integer> expected_parent_nids = isas.getParents(sctid).stream().map(ElkSnomedData::getNid)
						.collect(Collectors.toSet());
				if (sctid == SnomedIds.root) {
					expected_parent_nids = Set.of(TinkarTerm.PHENOMENON.nid());
					LOG.warn("Reset expected for " + sctid + " " + PrimitiveData.text(nid));
				}
				Set<Integer> expected_child_nids = isas.getChildren(sctid).stream().map(ElkSnomedData::getNid)
						.collect(Collectors.toSet());
				try {
					Set<Integer> actual_child_nids = ElkSnomedUtil.getInferredChildren(getViewCalculator(), sctid);
					if (!expected_child_nids.equals(actual_child_nids)) {
						LOG.error("Children: " + sctid + " " + descr.getFsn(sctid));
						child_miss++;
						child_miss_sctids.add(sctid);
					}
				} catch (SemanticStateException ex) {
					LOG.error(ex.getMessage());
				}
				try {
					Set<Integer> actual_parent_nids = ElkSnomedUtil.getInferredParents(getViewCalculator(), sctid);
					if (!expected_parent_nids.equals(actual_parent_nids)) {
						LOG.error("Parents: " + sctid + " " + descr.getFsn(sctid));
						parent_miss++;
					}
				} catch (SemanticStateException ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		assertEquals(0, parent_miss);
		assertEquals(expected_child_miss, child_miss);
	}

}
