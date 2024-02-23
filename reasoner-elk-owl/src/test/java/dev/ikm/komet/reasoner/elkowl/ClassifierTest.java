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
import java.util.ArrayList;
import java.util.Collections;

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

import dev.ikm.elk.snomed.owl.SnomedOwlOntology;
import dev.ikm.tinkar.common.service.PrimitiveData;

public abstract class ClassifierTest extends AxiomDataBuilderTest {

	private static final Logger LOG = LoggerFactory.getLogger(ClassifierTest.class);

	@Test
	public void classify() throws Exception {
		LOG.info("classify");
		ElkOwlAxiomData axiomData = buildAxiomData();
		LOG.info("Create ontology");
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = mgr.createOntology();
		LOG.info("Add axioms");
		mgr.addAxioms(ontology, axiomData.axiomsSet);
		LOG.info("Create reasoner");
		OWLReasonerFactory rf = (OWLReasonerFactory) new ElkReasonerFactory();
		OWLReasoner reasoner = rf.createReasoner(ontology);
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		LOG.info("Classified");
		// TODO put this back in. fails with -ea on
//		ProcessElkOwlResultsTask processResultsTask = new ProcessElkOwlResultsTask(reasoner, getViewCalculator(),
//				EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN, axiomData);
//		processResultsTask.compute();
		Files.createDirectories(getWritePath("supercs").getParent());
		Path path = getWritePath("supercs");
		ArrayList<String> lines = new ArrayList<>();
		for (OWLClass clazz : axiomData.nidConceptMap.values()) {
			int clazz_id = (int) SnomedOwlOntology.getId(clazz);
			String clazz_str = PrimitiveData.publicId(clazz_id).asUuidArray()[0] + "\t" + PrimitiveData.text(clazz_id);
			for (OWLClass sup : reasoner.getSuperClasses(clazz, true).getFlattened()) {
				if (sup.isTopEntity())
					continue;
				int sup_id = (int) SnomedOwlOntology.getId(sup);
				String sup_str = PrimitiveData.publicId(sup_id).asUuidArray()[0] + "\t" + PrimitiveData.text(sup_id);
				lines.add(clazz_str + "\t" + sup_str);
			}
		}
		Collections.sort(lines);
		Files.write(path, lines);
	}

}
