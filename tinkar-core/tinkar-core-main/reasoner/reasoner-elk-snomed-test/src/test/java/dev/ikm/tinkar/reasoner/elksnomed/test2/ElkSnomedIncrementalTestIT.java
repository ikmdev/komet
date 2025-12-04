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
package dev.ikm.tinkar.reasoner.elksnomed.test2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedIncrementalTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.test.PrimitiveDataTestUtil;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.reasoner.service.UnsupportedReasonerProcessIncremental;
import dev.ikm.tinkar.terms.TinkarTerm;

public class ElkSnomedIncrementalTestIT extends ElkSnomedIncrementalTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedIncrementalTestIT.class);

	public ReasonerService initReasonerService(String version) throws Exception {
		LOG.info("Init reasoner service");
		ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculator(version);
		ReasonerService rs = ElkSnomedTestBase.getElkSnomedReasonerService();
		rs.init(vc, TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN);
		rs.setProgressUpdater(null);
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		return rs;
	}

	@Test
	public void classify() throws Exception {
		LOG.info("classify");
		List<Long> full_times = new ArrayList<>();
		List<Long> incr_times = new ArrayList<>();
		List<Long> nnf_times = new ArrayList<>();
		ReasonerService rs = null;
		for (String effective_time : getEffectiveTimes()) {
			if (!includeVersion(effective_time))
				continue;
			LOG.info("Snomed version: " + effective_time);
			if (Integer.parseInt(effective_time) == version_start) {
				rs = initReasonerService(effective_time);
				continue;
			}
			ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculator(effective_time);
			long time = ((StampCalculatorWithCache) vc.stampCalculator()).filter().time();
			LOG.info("\tView calculator time: " + Instant.ofEpochMilli(time) + " " + time);
			HashMap<Integer, SemanticEntityVersion> active = new HashMap<>();
			HashMap<Integer, SemanticEntityVersion> inactive = new HashMap<>();
			vc.forEachSemanticVersionOfPattern(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
					(semanticEntityVersion, _) -> {
						if (semanticEntityVersion.time() == time) {
							int nid = semanticEntityVersion.referencedComponentNid();
							if (semanticEntityVersion.active()) {
								if (active.containsKey(nid))
									throw new RuntimeException("" + nid);
								active.put(nid, semanticEntityVersion);
							} else {
								if (inactive.containsKey(nid))
									throw new RuntimeException("" + nid);
								inactive.put(nid, semanticEntityVersion);
							}
						}
					});
			assertEquals(0, active.keySet().stream().filter(el -> inactive.keySet().contains(el)).count());
			LOG.info("\tA: " + active.size() + " I: " + inactive.size());
			{
				// 1295448001 |Attribution (attribute)|
				active.remove(ElkSnomedData.getNid(1295448001));
				// 1295449009 |Additional relationship attribute (attribute)|
				active.remove(ElkSnomedData.getNid(1295449009));
			}
			try {
				long beg = System.currentTimeMillis();
				rs.processIncremental(new ArrayList<>(inactive.keySet()), new ArrayList<>(active.values()));
				incr_times.add(System.currentTimeMillis() - beg);
			} catch (UnsupportedReasonerProcessIncremental ex) {
				LOG.error(ex.getMessage());
				Matcher m = Pattern.compile("-\\d+").matcher(ex.getMessage());
				if (m.find()) {
					String nid_str = m.group();
					int nid = Integer.parseInt(nid_str);
					LOG.error("\tSctid: " + PrimitiveDataTestUtil.getSctid(nid, vc));
					LOG.error("\t" + nid_str + " " + PrimitiveData.text(nid));
				}
				long beg = System.currentTimeMillis();
				rs = initReasonerService(effective_time);
				full_times.add(System.currentTimeMillis() - beg);
			}
			long beg = System.currentTimeMillis();
//			rs.buildNecessaryNormalForm();
			nnf_times.add(System.currentTimeMillis() - beg);
			checkParents(rs, Integer.parseInt(effective_time));
		}
		LOG.info("Full: " + full_times.size() + " " + full_times.stream().collect(Collectors.averagingLong(x -> x)));
		LOG.info("Incr: " + incr_times.size() + " " + incr_times.stream().collect(Collectors.averagingLong(x -> x)));
		LOG.info("NNF: " + nnf_times.size() + " " + nnf_times.stream().collect(Collectors.averagingLong(x -> x)));
	}

	private Set<Long> toSctids(ImmutableIntSet nids, HashMap<Integer, Long> nid_sctid_map) {
		return Arrays.stream(nids.toArray()).mapToObj(nid -> nid_sctid_map.get(nid)).collect(Collectors.toSet());
	}

	private void checkParents(ReasonerService rs, int version) throws Exception {
		TreeSet<Long> misses = new TreeSet<>();
		TreeSet<Long> other_misses = new TreeSet<>();
		int non_snomed_cnt = 0;
		int miss_cnt = 0;
		SnomedIsa isas = SnomedIsa.init(rels_file, version);
		HashMap<Integer, Long> nid_sctid_map = new HashMap<>();
		for (long sctid : isas.getOrderedConcepts()) {
			int nid = ElkSnomedData.getNid(sctid);
			nid_sctid_map.put(nid, sctid);
		}
		for (int nid : rs.getReasonerConceptSet().toArray()) {
			Set<Long> sups = toSctids(rs.getParents(nid), nid_sctid_map);
			Long sctid = nid_sctid_map.get((int) nid);
			if (sctid == null) {
				non_snomed_cnt++;
				continue;
			}
			Set<Long> parents = isas.getParents(sctid);
			if (sctid == SnomedIds.root) {
				assertTrue(parents.isEmpty());
				// has a parent in the db
				assertEquals(1, sups.size());
				assertEquals(TinkarTerm.PHENOMENON.nid(), rs.getParents(nid).intIterator().next());
				continue;
			} else {
				assertNotNull(parents);
			}
			if (!parents.equals(sups)) {
				misses.add(sctid);
				miss_cnt++;
			}
		}
		isas.getOrderedConcepts().stream().filter(other_misses::contains) //
				.limit(10) //
				.forEach((sctid) -> {
					UUID uuid = UuidUtil.fromSNOMED("" + sctid);
					int nid = PrimitiveData.nid(uuid);
					LOG.error("Miss: " + sctid + " " + PrimitiveData.text(nid));
					Set<Long> sups = toSctids(rs.getParents(nid), nid_sctid_map);
					Set<Long> parents = isas.getParents(sctid);
					HashSet<Long> par = new HashSet<>(parents);
					par.removeAll(sups);
					HashSet<Long> sup = new HashSet<>(sups);
					sup.removeAll(parents);
					LOG.error("Sno:  " + par);
					LOG.error("Elk:  " + sup);
					if (sups.contains(null)) {
						rs.getParents(nid).forEach(sup_nid -> LOG.error("   :  " + PrimitiveData.text((sup_nid))));
					}
				});
		if (miss_cnt != 0)
			LOG.error("Miss cnt: " + miss_cnt);
		int expected_non_snomed_cnt = PrimitiveDataTestUtil.getPrimordialNids().size()
				- PrimitiveDataTestUtil.getPrimordialNidsWithSctids().size();
		if (expected_non_snomed_cnt != non_snomed_cnt)
			LOG.error("Non-snomed: Expect: " + expected_non_snomed_cnt + " Actual: " + non_snomed_cnt);
		assertEquals(0, miss_cnt);
		assertEquals(expected_non_snomed_cnt, non_snomed_cnt);
	}

}
