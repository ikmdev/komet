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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedDataBuilder;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedReasonerService;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class ElkSnomedTestBase extends SnomedTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedTestBase.class);

	protected static String test_case;

	protected Path getWritePath(String filePart) throws IOException {
		Path path = Paths.get("target", test_case, test_case + "-" + filePart + ".txt");
		LOG.info("Write path: " + path);
		Files.createDirectories(path.getParent());
		return path;
	}

	private Path getExpectPath(String filePart) {
		Path path = Paths.get("src", "test", "resources", test_case, test_case + "-" + filePart + ".txt");
		LOG.info("Expect patch: " + path);
		assumeTrue(Files.exists(path));
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

	// This is overridden in the version test cases
	protected ViewCalculator getViewCalculator() {
		return PrimitiveDataTestUtil.getViewCalculator();
	}

	public ElkSnomedData buildSnomedData() throws Exception {
		LOG.info("buildSnomedData");
		ViewCalculator viewCalculator = getViewCalculator();
		ElkSnomedData data = new ElkSnomedData();
		ElkSnomedDataBuilder builder = new ElkSnomedDataBuilder(viewCalculator,
				TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, data);
		builder.build();
		return data;
	}

	public ArrayList<String> getSupercs(SnomedOntologyReasoner reasoner) {
		ArrayList<String> lines = new ArrayList<>();
		for (Concept con : reasoner.getSnomedOntology().getConcepts()) {
			int con_id = (int) con.getId();
			String con_str = PrimitiveData.publicId(con_id).asUuidArray()[0] + "\t" + PrimitiveData.text(con_id);
			for (Concept sup : reasoner.getSuperConcepts(con)) {
				int sup_id = (int) sup.getId();
				String sup_str = PrimitiveData.publicId(sup_id).asUuidArray()[0] + "\t" + PrimitiveData.text(sup_id);
				lines.add(con_str + "\t" + sup_str);
			}
		}
		Collections.sort(lines);
		return lines;
	}

	public void writeSupercs(ArrayList<String> lines) throws Exception {
		Files.createDirectories(getWritePath("supercs").getParent());
		Path path = getWritePath("supercs");
		Files.write(path, lines);
	}

	public SnomedOntologyReasoner runSnomedReasoner() throws Exception {
		LOG.info("runSnomedReasoner");
		ElkSnomedData data = buildSnomedData();
		LOG.info("Create ontology");
		SnomedOntology ontology = new SnomedOntology(data.getConcepts(), data.getRoleTypes(),
				data.getConcreteRoleTypes());
		LOG.info("Create reasoner");
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(ontology);
		return reasoner;
	}

	public static ReasonerService getElkSnomedReasonerService() {
		return PluggableService.load(ReasonerService.class).stream()
				.filter(x -> x.type().getSimpleName().equals(ElkSnomedReasonerService.class.getSimpleName())) //
				.findFirst().get().get();
	}

	public ReasonerService initReasonerService() {
		ReasonerService rs = getElkSnomedReasonerService();
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

	public ReasonerService runSnomedReasonerService() throws Exception {
		LOG.info("runSnomedReasonerService");
		ReasonerService rs = initReasonerService();
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		return rs;
	}

	public ReasonerService runReasonerServiceNNF() throws Exception {
		LOG.info("runReasonerServiceNNF");
		ReasonerService rs = initReasonerService();
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		rs.buildNecessaryNormalForm();
		return rs;
	}

}
