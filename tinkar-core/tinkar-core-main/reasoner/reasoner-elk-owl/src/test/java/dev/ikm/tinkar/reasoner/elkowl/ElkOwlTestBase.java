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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.owl.SnomedOwlOntology;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class ElkOwlTestBase extends PrimitiveDataTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkOwlTestBase.class);

	protected static int stated_count = Integer.MIN_VALUE;
	protected static int active_count = Integer.MIN_VALUE;
	protected static int inactive_count = Integer.MIN_VALUE;
	protected static String test_case;

	protected Path getWritePath(String filePart) throws IOException {
		Path path = Paths.get("target", test_case, test_case + "-" + filePart + ".txt");
		LOG.info("Write path: " + path);
		Files.createDirectories(path.getParent());
		return path;
	}

	private Path getExpectPath(String filePart) {
		Path path = Paths.get("src", "test", "resources", test_case, test_case + "-" + filePart + ".txt");
		assumeTrue(Files.exists(path));
		LOG.info("Expect patch: " + path);
		return path;
	}

	protected void compare(String filePart) throws IOException {
		LOG.info("Compare: " + filePart);
		assumeTrue(Files.exists(getExpectPath(filePart)));
		assumeTrue(Files.exists(getWritePath(filePart)));
		HashSet<String> expect = new HashSet<>(Files.readAllLines(getExpectPath(filePart)));
		HashSet<String> actual = new HashSet<>(Files.readAllLines(getWritePath(filePart)));
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

	public ElkOwlData buildElkOwlAxiomData() throws Exception {
		LOG.info("buildAxiomData");
		ViewCalculator viewCalculator = getViewCalculator();
		OWLDataFactory df = SnomedOwlOntology.createOntology().getDataFactory();
		ElkOwlData axiomData = new ElkOwlData(df);
		ElkOwlDataBuilder builder = new ElkOwlDataBuilder(viewCalculator,
				TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, axiomData, df);
		builder.build();
		return axiomData;
	}

	public ArrayList<String> getSupercs(ElkOwlData axiomData, OWLReasoner reasoner) {
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
		return lines;
	}

	// TODO put this back in. fails with -ea on
	// ProcessElkOwlResultsTask processResultsTask = new
	// ProcessElkOwlResultsTask(reasoner, getViewCalculator(),
	// EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN, axiomData);
	// processResultsTask.compute();

	public void runElkOwlReasoner() throws Exception {
		LOG.info("runElkOwlReasoner");
		ElkOwlData axiomData = buildElkOwlAxiomData();
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
		Files.createDirectories(getWritePath("supercs").getParent());
		Path path = getWritePath("supercs");
		ArrayList<String> lines = getSupercs(axiomData, reasoner);
		Files.write(path, lines);
	}

	public ReasonerService initReasonerService() {
		ReasonerService rs = PluggableService.load(ReasonerService.class).stream()
				.filter(x -> x.type().getSimpleName().equals(ElkOwlReasonerService.class.getSimpleName()))
				.findFirst().get().get();
		rs.init(getViewCalculator(), TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
				TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN);
		rs.setProgressUpdater(null);
		return rs;
	}

	public ArrayList<String> getSupercs(ReasonerService rs) {
		ArrayList<String> lines = new ArrayList<>();
		for (int con_id : rs.getReasonerConceptSet().toArray()) {
			String con_str = PrimitiveData.publicId(con_id).asUuidArray()[0] + "\t" + PrimitiveData.text(con_id);
			for (int sup_id : rs.getParents(con_id).toArray()) {
				String sup_str = PrimitiveData.publicId(sup_id).asUuidArray()[0] + "\t" + PrimitiveData.text(sup_id);
				lines.add(con_str + "\t" + sup_str);
			}
		}
		Collections.sort(lines);
		return lines;
	}

	public void runElkOwlReasonerService() throws Exception {
		LOG.info("runElkOwlReasonerService");
		ReasonerService rs = initReasonerService();
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		Files.createDirectories(getWritePath("supercs").getParent());
		Path path = getWritePath("supercs");
		ArrayList<String> lines = getSupercs(rs);
		Files.write(path, lines);
	}

}
