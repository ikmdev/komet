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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.reasoner.service.ReasonerService;

public class SnomedUS20200901ElkOwlIncrementalClassifierTestIT extends ElkOwlTestBase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(SnomedUS20200901ElkOwlIncrementalClassifierTestIT.class);

	{
		test_case = "snomed-us-20200901";
	}

	public static final String db = "SnomedCT_US_20200901_SpinedArray-20240502";

	// Chronic lung disease: [23e07078-f1e2-3f6a-9b7a-9397bcd91cfe]
	private static final UUID ChronicLungDiseaseUuid = UUID.fromString("23e07078-f1e2-3f6a-9b7a-9397bcd91cfe");

	private DiTreeEntity makeEquivalent(ReasonerService rs) {
		TempEditUtil editor = new TempEditUtil(rs.getViewCalculator(), rs.getStatedAxiomPattern());
		DiTreeEntity editedDefinition = editor.makeEquivalent(ChronicLungDiseaseUuid);
		return editedDefinition;
	}

	public ArrayList<String> classifyAll() throws Exception {
		String db_all = db + "-all";
		copyDb(db, db_all);
		setupPrimitiveData(db_all);
		PrimitiveData.start();
		ReasonerService rs = initReasonerService();
		makeEquivalent(rs);
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		ArrayList<String> lines = getSupercs(rs);
		return lines;
	}

	public ArrayList<String> classifyInc() throws Exception {
		String db_inc = db + "-inc";
		copyDb(db, db_inc);
		setupPrimitiveData(db_inc);
		PrimitiveData.start();
		ReasonerService rs = initReasonerService();
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		DiTreeEntity def = makeEquivalent(rs);
		int cldNid = PrimitiveData.nid(ChronicLungDiseaseUuid);
		rs.processIncremental(def, cldNid);
		rs.computeInferences();
		ArrayList<String> lines = getSupercs(rs);
		return lines;
	}

	// Run this to create supercs-inc and then copy to resources
	// @Test
	public void classifyInit() throws Exception {
		Path path = getWritePath("supercs-inc");
		ArrayList<String> lines = classifyAll();
		Files.write(path, lines);
	}

	@Test
	public void classifyCompare() throws Exception {
		Path path = getWritePath("supercs-inc");
		ArrayList<String> lines = classifyInc();
		Files.write(path, lines);
		compare("supercs-inc");
	}

}
