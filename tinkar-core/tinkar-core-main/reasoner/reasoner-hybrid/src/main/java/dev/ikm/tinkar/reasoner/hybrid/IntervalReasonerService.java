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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.reasoner.hybrid.snomed.IntervalNecessaryNormalFormBuilder;
import dev.ikm.reasoner.hybrid.snomed.IntervalReasoner;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedReasonerService;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

public class IntervalReasonerService extends ElkSnomedReasonerService {

	private static final Logger LOG = LoggerFactory.getLogger(IntervalReasonerService.class);

	@Override
	public String getName() {
		return "Interval Reasoner";
	}

	@Override
	public void init(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern,
			PatternFacade inferredAxiomPattern) {
		super.init(viewCalculator, statedAxiomPattern, inferredAxiomPattern);
		reasoner = null;
	}

	@Override
	public void loadData() throws Exception {
		progressUpdater.updateProgress(0, data.getActiveConceptCount());
		LOG.info("Create ontology");
		ontology = new SnomedOntology(data.getConcepts(), data.getRoleTypes(), List.of());
		LOG.info("Create reasoner");
		List<ConcreteRoleType> intervalRoles = List.copyOf(data.getIntervalRoleTypes());
		intervalRoles.forEach(x -> LOG.info("IR: " + PrimitiveData.text((int) x.getId())));
		reasoner = IntervalReasoner.create(ontology, intervalRoles);
	};

//	@Override
//	public void computeInferences() {
//		sso = StatementSnomedOntology.create(ontology, IntervalReasonerService.getRootId(), getSwecNids());
//		sso.classify();
//	}

	@Override
	public boolean isIncrementalReady() {
		return false;
	}

	@Override
	public void processIncremental(DiTreeEntity definition, int conceptNid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void buildNecessaryNormalForm() {
		List<ConcreteRoleType> intervalRoles = List.copyOf(data.getIntervalRoleTypes());
		nnfb = IntervalNecessaryNormalFormBuilder.create(ontology, reasoner.getSuperConcepts(),
				reasoner.getSuperRoleTypes(false), TinkarTerm.ROOT_VERTEX.nid(), intervalRoles);
		nnfb.generate();
	}

}
