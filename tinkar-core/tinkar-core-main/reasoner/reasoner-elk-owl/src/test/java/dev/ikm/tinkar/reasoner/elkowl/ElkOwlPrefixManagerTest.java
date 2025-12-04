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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.owl.SnomedOwlOntology;

public class ElkOwlPrefixManagerTest {

	private static final Logger LOG = LoggerFactory.getLogger(ElkOwlPrefixManagerTest.class);

	private static ElkOwlData axiomData;

	@BeforeAll
	public static void setup() throws Exception {
		axiomData = new ElkOwlData(SnomedOwlOntology.createOntology().getDataFactory());
	}

	@Test
	public void removePrefix() throws Exception {
		OWLDataFactory df = SnomedOwlOntology.createOntology().getDataFactory();
		OWLClass sub = axiomData.getConcept(1);
		OWLClass sup = axiomData.getConcept(2);
		OWLSubClassOfAxiom axiom = df.getOWLSubClassOfAxiom(sub, sup);
		LOG.info("Ax: " + ElkOwlPrefixManager.removePrefix(axiom));
		assertEquals("SubClassOf(:1 :2)", ElkOwlPrefixManager.removePrefix(axiom));
	}

}
