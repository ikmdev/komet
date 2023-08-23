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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.OWLAPIConfigProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.functional.parser.OWLFunctionalSyntaxOWLParser;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolorTest {

	private static final Logger LOG = LoggerFactory.getLogger(SolorTest.class);

	private OWLReasoner reasoner;

	@Test
	public void loadAndClassify() throws Exception {
		Path solor_axioms_file = Paths.get("src", "test", "resources", "solor", "solor-axioms.txt");
		assumeTrue(Files.exists(solor_axioms_file), "No file: " + solor_axioms_file);
		LOG.info("Create ontology");
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = mgr.createOntology();
		LOG.info("Read axioms");
		List<String> lines = Files.readAllLines(solor_axioms_file);
		LOG.info("Read axioms: " + lines.size());
		lines.add(0, "Prefix(:=<" + ElkOwlManager.PREFIX + ">) Ontology(");
		lines.add(")");
		OWLOntologyLoaderConfiguration config = new OWLAPIConfigProvider().get();
		new OWLFunctionalSyntaxOWLParser().parse(new StringDocumentSource(String.join("\n", lines)), ontology, config);
		LOG.info("Axioms: " + ontology.getAxiomCount());
		LOG.info("Create reasoner");
		OWLReasonerFactory rf = (OWLReasonerFactory) new ElkReasonerFactory();
		reasoner = rf.createReasoner(ontology);
		reasoner.flush();
		LOG.info("Compute inferences");
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		LOG.info("Done");
		ElkOwlManager.writeOntology(ontology, Paths.get("target", "solor.ofn"));
		assertEquals(364192, ontology.getAxiomCount());
	}

}
