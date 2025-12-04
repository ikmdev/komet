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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.reasoner.hybrid.snomed.StatementSnomedOntology;
import dev.ikm.reasoner.hybrid.snomed.StatementSnomedOntology.SwecIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class HybridReasonerServiceTestBase extends HybridReasonerTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(HybridReasonerServiceTestBase.class);

	protected int expected_swec_children = -1;

	@Test
	public void runReasonerService() throws Exception {
		LOG.info("runReasonerService");
		ReasonerService rs = initReasonerService();
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		rs.buildNecessaryNormalForm();
		rs.getReasonerConceptSet().forEach(rs::getParents);
		rs.getReasonerConceptSet().forEach(rs::getChildren);
		checkRoot(rs);
	}

	@Test
	public void ids() throws Exception {
		for (long sctid : List.of(StatementSnomedOntology.swec_id, StatementSnomedOntology.finding_context_id,
				StatementSnomedOntology.known_absent_id)) {
			int nid = ElkSnomedData.getNid(sctid);
			LOG.info(PrimitiveData.text(nid) + " " + nid + " " + sctid);
		}
		SwecIds swecNids = HybridReasonerService.getSwecNids();
		for (long nid : List.of(HybridReasonerService.getRootId(), swecNids.swec(), swecNids.swec_parent(),
				swecNids.findingContext(), swecNids.knownAbsent())) {
			LOG.info(PrimitiveData.text((int) nid) + " " + nid);
		}
		{
			int nid = ElkSnomedData.getNid(SnomedIds.root);
			LOG.info(PrimitiveData.text(nid) + " " + nid);
		}
		{
			int nid = TinkarTerm.ROOT_VERTEX.nid();
			LOG.info(PrimitiveData.text(nid) + " " + nid);
		}
	}

	private void checkRoot(ReasonerService rs) {
		SwecIds swecNids = HybridReasonerService.getSwecNids();
		rs.getParents((int) swecNids.swec()).forEach(nid -> LOG.info(PrimitiveData.text(nid)));
		assertEquals(2, rs.getParents((int) swecNids.swec()).size());
		assertTrue(rs.getParents((int) swecNids.swec()).contains((int) swecNids.swec_parent()));
		assertEquals(expected_swec_children, rs.getChildren((int) swecNids.swec()).size());
		rs.getChildren((int) swecNids.swec()).forEach(id -> {
			assertEquals(1, rs.getParents(id).size());
			assertEquals(swecNids.swec(), rs.getParents(id).toArray()[0]);
		});
	}

}
