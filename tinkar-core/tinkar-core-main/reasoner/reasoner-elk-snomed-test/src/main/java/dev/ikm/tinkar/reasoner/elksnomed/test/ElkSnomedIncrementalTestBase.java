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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.PrimitiveData;

public class ElkSnomedIncrementalTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedIncrementalTestBase.class);

	protected static final int version_start = 20210731; // 20240101;

	protected static boolean includeVersion(String version) {
		return Integer.parseInt(version) >= version_start;
	}

	protected String getDir() {
		return "target/data/snomed-test-data-" + getEditionDir() + "-full";
	}

	protected String getEdition() {
		return "INT";
	}

	protected String getEditionDir() {
		return "intl";
	}

	protected String getVersion() {
		return "20250101";
	}

	protected Path axioms_file = Paths.get(getDir(),
			"sct2_sRefset_OWLExpressionFull_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path concepts_file = Paths.get(getDir(),
			"sct2_Concept_Full_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path rels_file = Paths.get(getDir(),
			"sct2_Relationship_Full_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path values_file = Paths.get(getDir(),
			"sct2_RelationshipConcreteValues_Full_" + getEdition() + "_" + getVersion() + ".txt");

	protected Path descriptions_file = Paths.get(getDir(),
			"sct2_Description_Full-en_" + getEdition() + "_" + getVersion() + ".txt");

	private static String test_case = "snomed-intl-20250101";

	private static List<String> effectiveTimes;

	@BeforeAll
	public static void startPrimitiveData() throws IOException {
		PrimitiveDataTestUtil.setupPrimitiveData(test_case + "-sa");
		PrimitiveData.start();
	}

	@AfterAll
	public static void stopPrimitiveData() {
		PrimitiveDataTestUtil.stopPrimitiveData();
	}

	@BeforeEach
	protected void filesExist() {
		assertTrue(Files.exists(axioms_file), "No file: " + axioms_file);
		assertTrue(Files.exists(concepts_file), "No file: " + concepts_file);
		assertTrue(Files.exists(rels_file), "No file: " + rels_file);
		assertTrue(Files.exists(values_file), "No file: " + values_file);
		assertTrue(Files.exists(descriptions_file), "No file: " + descriptions_file);
		LOG.info("Files exist");
		LOG.info("\t" + axioms_file);
		LOG.info("\t" + concepts_file);
		LOG.info("\t" + rels_file);
		LOG.info("\t" + values_file);
		LOG.info("\t" + descriptions_file);
	}

	public List<String> getEffectiveTimes() throws IOException {
		if (effectiveTimes == null) {
			// id effectiveTime active moduleId definitionStatusId
			try (Stream<String> st = Files.lines(concepts_file)) {
				effectiveTimes = st.skip(1).map(line -> line.split("\\t")) //
						.map(fields -> fields[1]) // effectiveTime
						.distinct().sorted().toList();
			}
			;
		}
		return effectiveTimes;
	}

}
