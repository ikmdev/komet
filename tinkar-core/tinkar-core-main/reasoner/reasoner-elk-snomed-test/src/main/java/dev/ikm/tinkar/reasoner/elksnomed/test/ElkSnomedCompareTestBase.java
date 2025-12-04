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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.ConceptComparer;
import dev.ikm.elk.snomed.OwlElTransformer;
import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.elk.snomed.owlel.OwlElOntology;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;

public abstract class ElkSnomedCompareTestBase extends ElkSnomedTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedCompareTestBase.class);

	public static void mergeSubConceptDefinitions(Concept con) {
		// This merges multiple subConcept definition into one
		List<Definition> sc_defs = con.getDefinitions().stream()
				.filter(def -> def.getDefinitionType() == DefinitionType.SubConcept).toList();
		if (sc_defs.size() > 1) {
			Definition merged_def = new Definition();
			merged_def.setDefinitionType(DefinitionType.SubConcept);
			sc_defs.forEach(sc_def -> {
				sc_def.getSuperConcepts().forEach(merged_def::addSuperConcept);
				sc_def.getUngroupedRoles().forEach(merged_def::addUngroupedRole);
				sc_def.getUngroupedConcreteRoles().forEach(merged_def::addUngroupedConcreteRole);
				sc_def.getRoleGroups().forEach(merged_def::addRoleGroup);
			});
			con.getDefinitions().removeAll(sc_defs);
			con.addDefinition(merged_def);
		}
	}

	@Test
	public void compare() throws Exception {
		assumeTrue(Files.exists(axioms_file), "No file: " + axioms_file);
		LOG.info("Files exist");
		LOG.info("\t" + axioms_file);
		ElkSnomedData data = buildSnomedData();
		{
			Concept us_con = data.getConcept(ElkSnomedData.getNid(SnomedIds.us_nlm_module));
			if (getEdition().startsWith("US")) {
				assertNotNull(us_con);
			} else {
				assertNull(us_con);
			}
		}
		OwlElOntology ontology = new OwlElOntology();
		ontology.load(axioms_file);
		SnomedOntology snomedOntology = new OwlElTransformer().transform(ontology);
		snomedOntology.setDescriptions(SnomedDescriptions.init(descriptions_file));
		snomedOntology.setNames();
		{
			Concept us_con = snomedOntology.getConcept(SnomedIds.us_nlm_module);
			if (getEdition().startsWith("US")) {
				assertNotNull(us_con);
			} else {
				assertNull(us_con);
			}
		}
		SnomedOntology dataOntology = new NidToSctid(data, snomedOntology).build();
		dataOntology.setDescriptions(SnomedDescriptions.init(descriptions_file));
		dataOntology.setNames();
		int missing_count = 0;
		ConceptComparer cc = new ConceptComparer(snomedOntology);
		for (Concept concept : snomedOntology.getConcepts()) {
			if (concept.getId() == SnomedIds.root) {
				LOG.info("Skipping: " + concept);
				continue;
			}
			mergeSubConceptDefinitions(concept);
			Concept data_concept = dataOntology.getConcept(concept.getId());
			if (data_concept == null) {
				// TODO this should be in ConceptCompare
				LOG.error("Missing: " + concept);
				missing_count++;
			} else if (!cc.compare(data_concept))
				LOG.error("Mis match: " + data_concept);
		}
		LOG.info("Mis match count: " + cc.getMisMatchCount());
		LOG.info("Missing count: " + missing_count);
		assertEquals(0, cc.getMisMatchCount());
		assertEquals(0, missing_count);
	}

}
