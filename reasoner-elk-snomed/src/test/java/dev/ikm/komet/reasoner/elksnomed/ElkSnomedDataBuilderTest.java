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
package dev.ikm.komet.reasoner.elksnomed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class ElkSnomedDataBuilderTest extends ElkSnomedTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedDataBuilderTest.class);

	@Test
	public void statedPattern() throws Exception {
		ViewCalculator viewCalculator = getViewCalculator();
		LogicCoordinateRecord logicCoordinateRecord = viewCalculator.logicCalculator().logicCoordinateRecord();
		assertEquals(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
				logicCoordinateRecord.statedAxiomsPatternNid());
	}

	@Test
	public void count() throws Exception {
		ViewCalculator viewCalculator = getViewCalculator();
		AtomicInteger cnt = new AtomicInteger();
		AtomicInteger active_cnt = new AtomicInteger();
		AtomicInteger inactive_cnt = new AtomicInteger();
		viewCalculator.forEachSemanticVersionOfPatternParallel(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
				(semanticEntityVersion, patternEntityVersion) -> {
					int conceptNid = semanticEntityVersion.referencedComponentNid();
					if (viewCalculator.latestIsActive(conceptNid)) {
						active_cnt.incrementAndGet();
					} else {
						inactive_cnt.incrementAndGet();
					}
					cnt.incrementAndGet();
				});
		LOG.info("Cnt: " + cnt.intValue());
		LOG.info("Active Cnt: " + active_cnt.intValue());
		LOG.info("Inactive Cnt: " + inactive_cnt.intValue());
		assertEquals(stated_count, cnt.intValue());
		assertEquals(active_count, active_cnt.intValue());
		assertEquals(inactive_count, inactive_cnt.intValue());
	}

	@Test
	public void build() throws Exception {
		ElkSnomedData axiomData = buildSnomedData();
		assertEquals(active_count, axiomData.activeConceptCount.get());
		assertEquals(inactive_count, axiomData.inactiveConceptCount.get());
		Files.createDirectories(getWritePath("concepts").getParent());
		axiomData.writeConcepts(getWritePath("concepts"));
		axiomData.writeRoles(getWritePath("roles"));
		compare("concepts");
		compare("roles");
		assertEquals(axiomData.classificationConceptSet.size(), axiomData.nidConceptMap.entrySet().size());
	}

}
