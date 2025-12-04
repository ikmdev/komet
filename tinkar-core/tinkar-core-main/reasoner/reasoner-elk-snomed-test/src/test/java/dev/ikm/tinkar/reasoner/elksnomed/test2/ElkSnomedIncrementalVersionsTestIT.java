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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedIncrementalTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.test.PrimitiveDataTestUtil;
import dev.ikm.tinkar.terms.TinkarTerm;

public class ElkSnomedIncrementalVersionsTestIT extends ElkSnomedIncrementalTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedIncrementalVersionsTestIT.class);

	@Test
	public void effectiveTimes() throws Exception {
		LOG.info("effectiveTimes");
		int cnt = 0;
		int cnt_20210731 = 0;
		for (String effective_time : getEffectiveTimes()) {
			LOG.info("Snomed version: " + effective_time);
			cnt++;
			if (includeVersion(effective_time))
				cnt_20210731++;
			ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculator(effective_time);
			long vc_time = ((StampCalculatorWithCache) vc.stampCalculator()).filter().time();
			LOG.info("\tView calculator time: " + Instant.ofEpochMilli(vc_time) + " " + vc_time);
			HashSet<Integer> no_sctid_nids = new HashSet<>();
			HashSet<Integer> active_nids = new HashSet<>();
			HashSet<Integer> inactive_nids = new HashSet<>();
			vc.forEachSemanticVersionOfPattern(TinkarTerm.IDENTIFIER_PATTERN.nid(), (semanticEntityVersion, _) -> {
				int nid = semanticEntityVersion.referencedComponentNid();
				String sctid = PrimitiveDataTestUtil.getSctid(nid, vc);
				if (sctid != null) {
					if (vc.latestIsActive(nid)) {
						active_nids.add(nid);
					} else {
						inactive_nids.add(nid);
					}
				} else {
					no_sctid_nids.add(nid);
				}
			});
			LOG.info("\tT: " + (active_nids.size() + inactive_nids.size()) + " A: " + active_nids.size() + " I: "
					+ inactive_nids.size() + " Non-Snomed: " + no_sctid_nids.size());
		}
		assertEquals(79, cnt);
		assertEquals(40, cnt_20210731);
	}

	@Test
	public void axioms() throws Exception {
		LOG.info("axioms");
		for (String effective_time : getEffectiveTimes()) {
			if (!includeVersion(effective_time))
				continue;
			LOG.info("Snomed version: " + effective_time);
			ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculator(effective_time);
			long time = ((StampCalculatorWithCache) vc.stampCalculator()).filter().time();
			LOG.info("View calculator time: " + Instant.ofEpochMilli(time) + " " + time);
			HashSet<Integer> no_sctid_nids = new HashSet<>();
			AtomicInteger active = new AtomicInteger();
			AtomicInteger inactive = new AtomicInteger();
			AtomicInteger time_cnt = new AtomicInteger();
			vc.forEachSemanticVersionOfPattern(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
					(semanticEntityVersion, _) -> {
						if (semanticEntityVersion.time() == time)
							time_cnt.incrementAndGet();
						int nid = semanticEntityVersion.referencedComponentNid();
						String sctid = PrimitiveDataTestUtil.getSctid(nid, vc);
						if (sctid != null) {
							ZonedDateTime zdt = Instant.ofEpochMilli(semanticEntityVersion.time())
									.atZone(ZoneId.of("UTC"));
							if (zdt.getHour() != 0)
								LOG.info("SEV time: " + Instant.ofEpochMilli(semanticEntityVersion.time()) + " " + sctid
										+ " " + PrimitiveData.text(nid));
							if (Long.parseLong(sctid) != SnomedIds.root)
								assertEquals(0, zdt.getHour());
							if (semanticEntityVersion.active()) {
								active.incrementAndGet();
							} else {
								inactive.incrementAndGet();
							}
						} else {
							no_sctid_nids.add(nid);
						}
					});
			LOG.info("\t" + (active.get() + inactive.get()) + " " + active.get() + " " + inactive.get() + " "
					+ no_sctid_nids.size() + " " + time_cnt.get());
		}
	}

	@Test
	public void timeZone() {
		LOG.info("Time zone: " + new SimpleDateFormat("yyyyMMdd").getCalendar().getTimeZone());
	}

}
