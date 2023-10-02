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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.TinkarTerm;

public class SampleIncrementalClassifierTest extends PrimitiveDataTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(SampleIncrementalClassifierTest.class);

	@BeforeAll
	public static void startPrimitiveData() throws IOException {
		PrimitiveDataTestBase.setupPrimitiveData("sample-data-3-sa");
		PrimitiveData.start();
	}

	@Test
	public void build() throws Exception {
		ViewCalculator viewCalculator = AxiomDataBuilderTest.getViewCalculator();
		ElkOwlAxiomData axiomData = new ElkOwlAxiomData();
		ElkOwlAxiomDataBuilder builder = new ElkOwlAxiomDataBuilder(viewCalculator,
				TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, axiomData);
		builder.build();
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = mgr.createOntology();
		LOG.info("Add axioms");
		mgr.addAxioms(ontology, axiomData.axiomsSet);
		LOG.info("Create reasoner");
		OWLReasonerFactory rf = (OWLReasonerFactory) new ElkReasonerFactory();
		OWLReasoner reasoner = rf.createReasoner(ontology);
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		ElkOwlReasonerIncremental.getInstance().init(reasoner, axiomData);
		// Occupations [753d2b35-3924-5f9c-a6c7-a5c3a55fda29]
		// Occupation [4d0506d1-d961-5bf9-9a7f-bb1a702c7425]
		int occupationsNid = PrimitiveData.nid(UUID.fromString("753d2b35-3924-5f9c-a6c7-a5c3a55fda29"));
		int occupationNid = PrimitiveData.nid(UUID.fromString("4d0506d1-d961-5bf9-9a7f-bb1a702c7425"));
		TempEditUtil editor = new TempEditUtil(viewCalculator, TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN);
		DiTreeEntity editedDefinition = editor.setParent(occupationNid, occupationsNid);
		axiomData = ElkOwlReasonerIncremental.getInstance().getAxiomData();
		builder = new ElkOwlAxiomDataBuilder(viewCalculator, TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, axiomData);
		IncrementalChanges changes = builder.processIncremental(editedDefinition, occupationNid);
		ElkOwlReasonerIncremental.getInstance().processChanges(changes);
		OWLClass sub = axiomData.nidConceptMap.get(occupationNid);
		OWLClass sup = axiomData.nidConceptMap.get(occupationsNid);
		LOG.info("Sub: " + sub + " " + PrimitiveData.text(Integer.parseInt(sub.getIRI().getShortForm())));
		LOG.info("Sup: " + sup + " " + PrimitiveData.text(Integer.parseInt(sup.getIRI().getShortForm())));
		ElkOwlReasonerIncremental.getInstance().getReasoner().flush();
		Set<OWLClass> parents = ElkOwlReasonerIncremental.getInstance().getReasoner().getSuperClasses(sub, true)
				.getFlattened();
		for (OWLClass parent : parents) {
			LOG.info("Parent: " + parent + " " + PrimitiveData.text(Integer.parseInt(parent.getIRI().getShortForm())));
		}
		assertEquals(1, parents.size());
		assertEquals(Integer.parseInt(sup.getIRI().getShortForm()),
				Integer.parseInt(parents.iterator().next().getIRI().getShortForm()));
	}

}
