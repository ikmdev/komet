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
package dev.ikm.komet.reasoner.elksnomed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.tinkar.common.service.PrimitiveData;

public class SolorElkSnomedIncrementalClassifierTest extends ElkSnomedTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(SolorElkSnomedIncrementalClassifierTest.class);

	// Chronic lung disease: [23e07078-f1e2-3f6a-9b7a-9397bcd91cfe]

	private static final UUID cld_uuid = UUID.fromString("23e07078-f1e2-3f6a-9b7a-9397bcd91cfe");

	public ArrayList<String> classifyAll() throws Exception {
		LOG.info("classifyAll");
		ElkSnomedData axiomData = buildSnomedData();
		int id = PrimitiveData.nid(cld_uuid);
		LOG.info(PrimitiveData.text(id));
		Concept con = axiomData.getConcept(id);
		LOG.info("CLD: " + con + " " + con.getDefinitions().getFirst().getDefinitionType());
		con.getDefinitions().getFirst().setDefinitionType(DefinitionType.EquivalentConcept);
		LOG.info("CLD: " + con + " " + con.getDefinitions().getFirst().getDefinitionType());
		LOG.info("Create ontology");
		SnomedOntology ontology = new SnomedOntology(axiomData.nidConceptMap.values(), axiomData.nidRoleMap.values());
		LOG.info("Create reasoner");
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(ontology);
		assertEquals(2, reasoner.getSuperConcepts(con).size());
		assertEquals(23, reasoner.getSubConcepts(con).size());
		ArrayList<String> lines = getSupercs(axiomData, reasoner);
		return lines;
	}

	public ArrayList<String> classifyInc() throws Exception {
		LOG.info("classifyInc");
		ElkSnomedData axiomData = buildSnomedData();
		int id = PrimitiveData.nid(cld_uuid);
		LOG.info(PrimitiveData.text(id));
		Concept con = axiomData.getConcept(id);
		LOG.info("CLD: " + con + " " + con.getDefinitions().getFirst().getDefinitionType());

		LOG.info("Create ontology");
		SnomedOntology ontology = new SnomedOntology(axiomData.nidConceptMap.values(), axiomData.nidRoleMap.values());
		LOG.info("Create reasoner");
		SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(ontology);
		// TODO have this go through digraph
		con.getDefinitions().getFirst().setDefinitionType(DefinitionType.EquivalentConcept);
		LOG.info("CLD: " + con + " " + con.getDefinitions().getFirst().getDefinitionType());
		reasoner.process(con);
		reasoner.flush();
		assertEquals(2, reasoner.getSuperConcepts(con).size());
		assertEquals(23, reasoner.getSubConcepts(con).size());
		ArrayList<String> lines = getSupercs(axiomData, reasoner);
		return lines;
	}

	@Test
	public void classifyCompare() throws Exception {
		SolorElkSnomedDataBuilderTest.startPrimitiveData();
		HashSet<String> all = new HashSet<>(classifyAll());
		HashSet<String> inc = new HashSet<>(classifyInc());
		assertEquals(all, inc);
	}

}
