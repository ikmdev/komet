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
package dev.ikm.tinkar.reasoner.elksnomed.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.ConceptComparer;
import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.SnomedLoader;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedDataBuilder;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedUtil;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedUtil.SemanticStateException;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class ElkSnomedReasonerWriteTestBase extends ElkSnomedTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedReasonerWriteTestBase.class);

	@Test
	public void reasonerWrite() throws Exception {
		ReasonerService rs = initReasonerService();
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		rs.buildNecessaryNormalForm();
		rs.writeInferredResults();
		int inferredPatternNid = rs.getViewCalculator().viewCoordinateRecord().logicCoordinate()
				.inferredAxiomsPatternNid();
		SnomedIsa isas = SnomedIsa.init(rels_file);
		SnomedDescriptions descr = SnomedDescriptions.init(descriptions_file);
		SnomedOntology inferredOntology = new SnomedLoader().load(concepts_file, descriptions_file, rels_file,
				values_file);
		inferredOntology.setDescriptions(descr);
		inferredOntology.setNames();
		ElkSnomedData data = buildSnomedData();
		NidToSctid nid_to_sctid = new NidToSctid(data, inferredOntology);
		nid_to_sctid.build();
		ConceptComparer cc = new ConceptComparer(inferredOntology);
		int parent_miss = 0;
		int child_miss = 0;
		int mis_match_cnt = 0;
		HashSet<Long> child_miss_sctids = new HashSet<>();
		for (long sctid : isas.getOrderedConcepts()) {
			int nid = ElkSnomedData.getNid(sctid);
			{
				Set<Integer> expected_parent_nids = isas.getParents(sctid).stream().map(ElkSnomedData::getNid)
						.collect(Collectors.toSet());
				if (sctid == SnomedIds.root) {
					expected_parent_nids = Set.of(TinkarTerm.PHENOMENON.nid());
					LOG.warn("Reset expected parents for " + sctid + " " + PrimitiveData.text(nid));
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
			{
				if (sctid == SnomedIds.root) {
					LOG.warn("Skipping compare for " + sctid + " " + PrimitiveData.text(nid));
					continue;
				}
				try {
					SemanticEntityVersion sev = ElkSnomedUtil.getLatestSemantic(getViewCalculator(), inferredPatternNid,
							nid);
					ElkSnomedDataBuilder builder = new ElkSnomedDataBuilder(null, null, new ElkSnomedData());
					Concept concept = builder.buildConcept(sev);
					Definition new_def = nid_to_sctid.makeNewDefinition(concept.getDefinitions().getFirst());
					Concept new_concept = new Concept(sctid);
					new_concept.addDefinition(new_def);
					if (!cc.compare(new_concept)) {
						LOG.error("Mis match: " + new_concept);
						mis_match_cnt++;
					}
				} catch (SemanticStateException ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		assertEquals(0, parent_miss);
		// TODO
		// 609096000 Role group (attribute)
		// 1295447006 Annotation attribute (attribute)
		// 900000000000447004 Case significance (core metadata concept)
		// 900000000000446008 Description type (core metadata concept)
		assertEquals(4, child_miss);
		assertEquals(Set.of(609096000l, 1295447006l, 900000000000447004l, 900000000000446008l), child_miss_sctids);
		assertEquals(0, mis_match_cnt);
	}

}
