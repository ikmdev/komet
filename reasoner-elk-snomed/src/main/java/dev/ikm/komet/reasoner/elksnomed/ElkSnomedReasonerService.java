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

import java.util.Set;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.komet.reasoner.service.ReasonerServiceBase;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.PatternFacade;

public class ElkSnomedReasonerService extends ReasonerServiceBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedReasonerService.class);

	private ElkSnomedData axiomData;

	private ElkSnomedDataBuilder builder;

	private SnomedOntology ontology;

	private SnomedOntologyReasoner reasoner;

	@Override
	public void init(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern,
			PatternFacade inferredAxiomPattern) {
		super.init(viewCalculator, statedAxiomPattern, inferredAxiomPattern);
		this.axiomData = null;
		this.reasoner = null;
	}

	@Override
	public void extractData() throws Exception {
		axiomData = new ElkSnomedData();
		builder = new ElkSnomedDataBuilder(viewCalculator, statedAxiomPattern, axiomData);
		builder.setProgressUpdater(progressUpdater);
		builder.build();
	};

	@Override
	public void loadData() throws Exception {
		int axiomCount = this.axiomData.processedSemantics.get();
		progressUpdater.updateProgress(0, axiomCount);
		LOG.info("Create ontology");
		ontology = new SnomedOntology(axiomData.nidConceptMap.values(), axiomData.nidRoleMap.values());
		LOG.info("Create reasoner");
		reasoner = SnomedOntologyReasoner.create(ontology);
	};

	public void computeInferences() {
		// Already done in SnomedOntologyReasoner.create
	}

	@Override
	public boolean isIncrementalReady() {
		return reasoner != null;
	}

	@Override
	public void processIncremental(DiTreeEntity definition, int conceptNid) {
		Concept concept = builder.processIncremental(definition, conceptNid);
		reasoner.process(concept);
		reasoner.flush();
	}

	@Override
	public int getConceptCount() {
		return axiomData.activeConceptCount.get();
	}

	@Override
	public ImmutableIntList getClassificationConceptSet() {
		return axiomData.classificationConceptSet;
	}

	private ImmutableIntSet toIntSet(Set<Long> classes) {
		if (classes == null)
			return null;
		MutableIntSet parentNids = IntSets.mutable.withInitialCapacity(classes.size());
		for (long parent : classes) {
			parentNids.add((int) parent);
		}
		return parentNids.toImmutable();
	}

	@Override
	public ImmutableIntSet getEquivalent(int id) {
		Set<Long> eqs = reasoner.getEquivalentConcepts(id);
		return toIntSet(eqs);
	}

	@Override
	public ImmutableIntSet getParents(int id) {
		Set<Long> supers = reasoner.getSuperConcepts(id);
		return toIntSet(supers);
	}

	@Override
	public ImmutableIntSet getChildren(int id) {
		Set<Long> subs = reasoner.getSubConcepts(id);
		return toIntSet(subs);
	}

}
