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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.reasoner.elkowl.ElkOwlAxiomDataBuilder.IncrementalChanges;
import dev.ikm.komet.reasoner.service.ReasonerServiceBase;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.PatternFacade;

public class ElkOwlReasonerService extends ReasonerServiceBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkOwlReasonerService.class);

	private ElkOwlAxiomData axiomData;

	private ElkOwlAxiomDataBuilder builder;

	private OWLOntology ontology;

	private OWLReasoner reasoner;

	@Override
	public void init(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern,
			PatternFacade inferredAxiomPattern) {
		super.init(viewCalculator, statedAxiomPattern, inferredAxiomPattern);
		this.axiomData = null;
		this.reasoner = null;
	}

	@Override
	public void extractData() throws Exception {
		axiomData = new ElkOwlAxiomData();
		builder = new ElkOwlAxiomDataBuilder(viewCalculator, statedAxiomPattern, axiomData);
		builder.setProgressUpdater(progressUpdater);
		builder.build();
	};

	@Override
	public void loadData() throws Exception {
		int axiomCount = this.axiomData.processedSemantics.get();
		progressUpdater.updateProgress(0, axiomCount);
		LOG.info("Create ontology");
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		ontology = mgr.createOntology();
		LOG.info("Add axioms");
		mgr.addAxioms(ontology, axiomData.axiomsSet);
		LOG.info("Create reasoner");
		OWLReasonerFactory rf = (OWLReasonerFactory) new ElkReasonerFactory();
		reasoner = rf.createReasoner(ontology);
	};

	public void computeInferences() {
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	}

	@Override
	public boolean isIncrementalReady() {
		return reasoner != null;
	}

	@Override
	public void processIncremental(DiTreeEntity definition, int conceptNid) {
		IncrementalChanges changes = builder.processIncremental(definition, conceptNid);
		OWLOntologyManager mgr = ontology.getOWLOntologyManager();
		mgr.removeAxioms(ontology, new HashSet<>(changes.getDeletions().castToList()));
		mgr.addAxioms(ontology, new HashSet<>(changes.getAdditions().castToList()));
	}

	@Override
	public int getConceptCount() {
		return axiomData.activeConceptCount.get();
	}

	@Override
	public ImmutableIntList getClassificationConceptSet() {
		return axiomData.classificationConceptSet;
	}

	private ImmutableIntSet toIntSet(Set<OWLClass> classes) {
		MutableIntSet parentNids = IntSets.mutable.withInitialCapacity(classes.size());
		for (OWLClass parent : classes) {
			if (!parent.isTopEntity() && !parent.isBottomEntity())
				try {
					int parentNid = Integer.parseInt(parent.getIRI().getShortForm());
					parentNids.add(parentNid);
				} catch (final NumberFormatException numberFormatException) {
					LOG.error("Concept IRI error: " + parent, numberFormatException);
					// TODO
					AlertStreams.dispatchToRoot(numberFormatException);
				}
		}
		return parentNids.toImmutable();
	}

	@Override
	public ImmutableIntSet getEquivalent(int id) {
		OWLClass concept = axiomData.nidConceptMap.get(id);
		if (concept == null)
			return null;
		Set<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(concept).getEntities();
		return toIntSet(equivalentClasses);
	}

	@Override
	public ImmutableIntSet getParents(int id) {
		OWLClass concept = axiomData.nidConceptMap.get(id);
		if (concept == null)
			return null;
		Set<OWLClass> superClasses = reasoner.getSuperClasses(concept, true).getFlattened();
		return toIntSet(superClasses);
	}

	@Override
	public ImmutableIntSet getChildren(int id) {
		OWLClass concept = axiomData.nidConceptMap.get(id);
		if (concept == null)
			return null;
		Set<OWLClass> subClasses = reasoner.getSubClasses(concept, true).getFlattened();
		return toIntSet(subClasses);
	}

	private boolean write = false;

	private Path getPath(String filePart) {
		Path path = Paths.get("..", "classification", "target",
				this.getClass().getSimpleName() + "-" + filePart + ".txt");
		LOG.info("Write to : " + path);
		return path;
	}

	@SuppressWarnings("unused")
	private void writeAxioms(ElkOwlAxiomData axiomData) throws Exception {
		if (write) {
			LOG.info(">>>>>");
			LOG.info("Writing axioms: " + write);
			Files.write(getPath("concepts"), axiomData.nidConceptMap.entrySet().stream() //
					.map(Entry::getKey) //
					.map(key -> key + "\t" + PrimitiveData.publicId(key).asUuidArray()[0] + "\t"
							+ PrimitiveData.text(key)) //
					.collect(Collectors.toList()));
			Files.write(getPath("roles"), axiomData.nidRoleMap.entrySet().stream() //
					.map(Entry::getKey) //
					.map(key -> key + "\t" + PrimitiveData.publicId(key).asUuidArray()[0] + "\t"
							+ PrimitiveData.text(key)) //
					.collect(Collectors.toList()));
			Files.write(getPath("axioms"), axiomData.axiomsSet.stream() //
					.map(ElkOwlManager::removePrefix) //
					.collect(Collectors.toList()));
//			LOG.info("Write axioms in " + durationString());
		}
	}

	@SuppressWarnings("unused")
	private void writeOntology(OWLOntology ontology) throws Exception {
		if (write) {
			LOG.info(">>>>>");
			LOG.info("Writing ontology: " + write);
			ElkOwlManager.writeOntology(ontology, getPath("ofn"));
//			LOG.info("Write ontology in " + durationString());
		}
	}

}
