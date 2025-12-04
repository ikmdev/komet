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
package dev.ikm.tinkar.reasoner.elkowl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.owl.SnomedOwlOntology;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.reasoner.elkowl.ElkOwlDataBuilder.IncrementalChanges;
import dev.ikm.tinkar.reasoner.service.ReasonerServiceBase;
import dev.ikm.tinkar.terms.PatternFacade;

/**
 * @deprecated
 * No longer maintained.
 * 
 * Use dev.ikm.tinkar.reasoner.elksnomed
 */
@Deprecated
public class ElkOwlReasonerService extends ReasonerServiceBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkOwlReasonerService.class);

	private ElkOwlData axiomData;

	private ElkOwlDataBuilder builder;

	private SnomedOwlOntology ontology;

	private boolean computedInferences = false;

	@Override
	public void init(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern,
			PatternFacade inferredAxiomPattern) {
		super.init(viewCalculator, statedAxiomPattern, inferredAxiomPattern);
		this.axiomData = null;
	}

	@Override
	public void extractData() throws Exception {
		ontology = SnomedOwlOntology.createOntology();
		axiomData = new ElkOwlData(ontology.getDataFactory());
		builder = new ElkOwlDataBuilder(viewCalculator, statedAxiomPattern, axiomData, ontology.getDataFactory());
		builder.setProgressUpdater(progressUpdater);
		builder.build();
	};

	@Override
	public void loadData() throws Exception {
		int axiomCount = this.axiomData.processedSemantics.get();
		progressUpdater.updateProgress(0, axiomCount);
		LOG.info("Create ontology");
		LOG.info("Add axioms");
		ontology.addAxioms(axiomData.axiomsSet);
	};

	public void computeInferences() {
		if (!computedInferences) {
			ontology.classify();
			computedInferences = true;
		} else {
			ontology.getReasoner().flush();
			ontology.getReasoner().precomputeInferences(InferenceType.CLASS_HIERARCHY);
		}
	}

	@Override
	public boolean isIncrementalReady() {
		return computedInferences;
	}

	@Override
	public void processIncremental(DiTreeEntity definition, int conceptNid) {
		IncrementalChanges changes = builder.processIncremental(definition, conceptNid);
		ontology.removeAxioms(new HashSet<>(changes.getDeletions().castToList()));
		ontology.addAxioms(new HashSet<>(changes.getAdditions().castToList()));
	}

	@Override
	public void processIncremental(SemanticEntityVersion semanticEntityVersion) {
		// TODO easy to implement, but this module is on backlog
		throw new UnsupportedOperationException();
	}

	@Override
	public void processIncremental(List<Integer> deletes, List<SemanticEntityVersion> updates) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void buildNecessaryNormalForm() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getConceptCount() {
		return axiomData.getActiveConceptCount();
	}

	@Override
	public ImmutableIntList getReasonerConceptSet() {
		return axiomData.classificationConceptSet;
	}

	private ImmutableIntSet toIntSet(Set<OWLClass> classes) {
		MutableIntSet parentNids = IntSets.mutable.withInitialCapacity(classes.size());
		for (OWLClass parent : classes) {
			if (!parent.isTopEntity() && !parent.isBottomEntity())
				try {
					int parentNid = Integer.parseInt(parent.getIRI().getShortForm());
					parentNids.add(parentNid);
				} catch (NumberFormatException ex) {
					LOG.error("Concept IRI error: " + parent, ex);
					throw ex;
				}
		}
		return parentNids.toImmutable();
	}

	@Override
	public ImmutableIntSet getEquivalent(int id) {
		OWLClass concept = axiomData.nidConceptMap.get(id);
		if (concept == null)
			return null;
		Set<OWLClass> equivalentClasses = ontology.getEquivalentClasses(concept);
		return toIntSet(equivalentClasses);
	}

	@Override
	public ImmutableIntSet getParents(int id) {
		OWLClass concept = axiomData.nidConceptMap.get(id);
		if (concept == null)
			return null;
		Set<OWLClass> superClasses = ontology.getSuperClasses(concept);
		return toIntSet(superClasses);
	}

	@Override
	public ImmutableIntSet getChildren(int id) {
		OWLClass concept = axiomData.nidConceptMap.get(id);
		if (concept == null)
			return null;
		Set<OWLClass> subClasses = ontology.getSubClasses(concept);
		return toIntSet(subClasses);
	}

	@Override
	public LogicalExpression getNecessaryNormalForm(int id) {
		throw new UnsupportedOperationException();
	}

}
