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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedConcepts;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class ElkSnomedDataBuilderTestBase extends ElkSnomedTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedDataBuilderTestBase.class);

	protected int inactive_count = -1;

	@Test
	public void statedPattern() throws Exception {
		ViewCalculator viewCalculator = PrimitiveDataTestUtil.getViewCalculator();
		LogicCoordinateRecord logicCoordinateRecord = viewCalculator.logicCalculator().logicCoordinateRecord();
		assertEquals(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
				logicCoordinateRecord.statedAxiomsPatternNid());
	}

	@Test
	public void count() throws Exception {
		ViewCalculator viewCalculator = PrimitiveDataTestUtil.getViewCalculator();
		AtomicInteger cnt = new AtomicInteger();
		AtomicInteger active_cnt = new AtomicInteger();
		AtomicInteger inactive_cnt = new AtomicInteger();
		viewCalculator.forEachSemanticVersionOfPatternParallel(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
				(semanticEntityVersion, _) -> {
					if (semanticEntityVersion.active()) {
						active_cnt.incrementAndGet();
					} else {
						inactive_cnt.incrementAndGet();
					}
					cnt.incrementAndGet();
				});
		LOG.info("Cnt: " + cnt.intValue());
		LOG.info("Active cnt: " + active_cnt.intValue());
		LOG.info("Inactive cnt: " + inactive_cnt.intValue());
		SnomedConcepts snomed_concepts = SnomedConcepts.init(concepts_file);
		LOG.info("Snomed active cnt: " + snomed_concepts.getActiveCount());
		LOG.info("Snomed inactive cnt: " + snomed_concepts.getInactiveCount());
		int primordial_cnt = PrimitiveDataTestUtil.getPrimordialNids().size();
		LOG.info("Primordial cnt: " + primordial_cnt);
		int primordial_sctid_cnt = PrimitiveDataTestUtil.getPrimordialNidsWithSctids().size();
		LOG.info("Primordial sctid cnt: " + primordial_sctid_cnt);
		assertEquals(snomed_concepts.getActiveCount(), active_cnt.intValue() - primordial_cnt + primordial_sctid_cnt);
		assertEquals(inactive_count, inactive_cnt.intValue());
		assertEquals(primordial_cnt, PrimitiveDataTestUtil.getPrimordialNids().size());
	}

	@Test
	public void build() throws Exception {
		ElkSnomedData data = buildSnomedData();
		SnomedConcepts snomed_concepts = SnomedConcepts.init(concepts_file);
		int primordial_cnt = PrimitiveDataTestUtil.getPrimordialNids().size();
		int primordial_sctid_cnt = PrimitiveDataTestUtil.getPrimordialNidsWithSctids().size();
		// TODO +2 is for annotation properties
		assertEquals(snomed_concepts.getActiveCount(),
				data.getActiveConceptCount() - primordial_cnt + primordial_sctid_cnt + 2);
		assertEquals(inactive_count, data.getInactiveConceptCount());
		assertEquals(data.getActiveConceptCount(), data.getConcepts().size());
		assertEquals(data.getReasonerConceptSet().size(), data.getConcepts().size());
		// TODO get these to work again
//		Files.createDirectories(getWritePath("concepts").getParent());
//		data.writeConcepts(getWritePath("concepts"));
//		data.writeRoleTypes(getWritePath("roles"));
//		compare("concepts");
//		compare("roles");
	}

	@Test
	public void primordialCount() throws Exception {
		ViewCalculator primordial_vc = PrimitiveDataTestUtil.getViewCalculatorPrimordial();
		AtomicInteger cnt = new AtomicInteger();
		AtomicInteger active_cnt = new AtomicInteger();
		AtomicInteger inactive_cnt = new AtomicInteger();
		primordial_vc.forEachSemanticVersionOfPattern(TinkarTerm.IDENTIFIER_PATTERN.nid(),
				(semanticEntityVersion, _) -> {
					int conceptNid = semanticEntityVersion.referencedComponentNid();
					if (primordial_vc.latestIsActive(conceptNid)) {
						active_cnt.incrementAndGet();
					} else {
						inactive_cnt.incrementAndGet();
					}
					cnt.incrementAndGet();
				});
		LOG.info("Primordial:");
		LOG.info("\tCnt: " + cnt.intValue());
		LOG.info("\tActive Cnt: " + active_cnt.intValue());
		LOG.info("\tInactive Cnt: " + inactive_cnt.intValue());
		assertEquals(active_cnt.intValue(), PrimitiveDataTestUtil.getPrimordialNids().size());
		assertEquals(0, inactive_cnt.intValue());
	}

	@Test
	public void primordialSctidCount() throws Exception {
		ViewCalculator primordial_vc = PrimitiveDataTestUtil.getViewCalculatorPrimordial();
		AtomicInteger cnt = new AtomicInteger();
		primordial_vc.forEachSemanticVersionOfPattern(TinkarTerm.IDENTIFIER_PATTERN.nid(),
				(semanticEntityVersion, _) -> {
					int conceptNid = semanticEntityVersion.referencedComponentNid();
					ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculator();
					Latest<PatternEntityVersion> latestIdPattern = vc
							.latestPatternEntityVersion(TinkarTerm.IDENTIFIER_PATTERN);
					EntityService.get().forEachSemanticForComponentOfPattern(conceptNid,
							TinkarTerm.IDENTIFIER_PATTERN.nid(), (semanticEntity) -> {
								if (vc.latest(semanticEntity).isPresent()) {
									SemanticEntityVersion latestSemanticVersion = vc.latest(semanticEntity).get();
									EntityProxy identifierSource = latestIdPattern.get()
											.getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestSemanticVersion);
									boolean has_sctid = false;
									if (PublicId.equals(identifierSource, TinkarTerm.SCTID)) {
										// Just in case it has more than one sctid
										if (!has_sctid)
											cnt.incrementAndGet();
										has_sctid = true;
										String idSourceName = vc
												.getPreferredDescriptionTextWithFallbackOrNid(identifierSource);
										String idValue = latestIdPattern.get().getFieldWithMeaning(
												TinkarTerm.IDENTIFIER_VALUE, latestSemanticVersion);
										LOG.info("Primordial: " + conceptNid + " " + PrimitiveData.text(conceptNid));
										LOG.info("ID: " + idSourceName + " " + idValue);
									}
								} else {
									throw new RuntimeException(
											"No latest for " + conceptNid + " " + PrimitiveData.text(conceptNid));
								}
							});
				});
		assertEquals(cnt.intValue(), PrimitiveDataTestUtil.getPrimordialNidsWithSctids().size());
	}

}
