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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class AxiomDataBuilderTest extends PrimitiveDataTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(AxiomDataBuilderTest.class);

	@Test
	public void statedPattern() throws Exception {
		ViewCalculator viewCalculator = getViewCalculator();
		LogicCoordinateRecord logicCoordinateRecord = viewCalculator.logicCalculator().logicCoordinateRecord();
		assertEquals(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
				logicCoordinateRecord.statedAxiomsPatternNid());
	}

	protected static int stated_count = Integer.MIN_VALUE;
	protected static int active_count = Integer.MIN_VALUE;
	protected static int inactive_count = Integer.MIN_VALUE;
	protected static String test_case;

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

	private Path getWritePath(String filePart) {
		Path path = Paths.get("target", test_case, test_case + "-" + filePart + ".txt");
		LOG.info("Write path: " + path);
		return path;
	}

	private Path getExpectPath(String filePart) {
		Path path = Paths.get("src", "test", "resources", test_case, test_case + "-" + filePart + ".txt");
		assumeTrue(Files.exists(path));
		LOG.info("Expect patch: " + path);
		return path;
	}

	@SuppressWarnings("unused")
	private void compare(String filePart) throws IOException {
		List<String> expect = Files.readAllLines(getExpectPath(filePart));
		List<String> actual = Files.readAllLines(getWritePath(filePart));
		if (!expect.equals(actual)) {
			Set<String> expect_copy = new HashSet<>(expect);
			expect_copy.removeAll(actual);
			expect_copy.forEach(l -> LOG.error("Missing: " + l));
			Set<String> actual_copy = new HashSet<>(actual);
			actual_copy.removeAll(expect);
			actual_copy.forEach(l -> LOG.error("Extra: " + l));
		}
		// assertEquals verbose if the test fails
		assertTrue(expect.equals(actual), filePart);
	}

	@Test
	public void build() throws Exception {
		ElkOwlAxiomData axiomData = buildAxiomData();
		assertEquals(active_count, axiomData.activeConceptCount.get());
		assertEquals(inactive_count, axiomData.inactiveConceptCount.get());
//		Files.createDirectories(getWritePath("concepts").getParent());
//		axiomData.writeConcepts(getWritePath("concepts"));
//		axiomData.writeRoles(getWritePath("roles"));
//		axiomData.writeAxioms(getWritePath("axioms"));
//		compare("concepts");
//		compare("roles");
//		compare("axioms");
	}

}
