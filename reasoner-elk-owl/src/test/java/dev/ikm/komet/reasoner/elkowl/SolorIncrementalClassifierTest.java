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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.reasoner.elkowl.ElkOwlAxiomDataBuilder.IncrementalChanges;
import dev.ikm.tinkar.common.service.PrimitiveData;

public class SolorIncrementalClassifierTest extends PrimitiveDataTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(SolorIncrementalClassifierTest.class);

	@BeforeAll
	public static void startPrimitiveData() throws IOException {
		setupPrimitiveData("solor-08-27-256-sa");
		PrimitiveData.start();
	}

	public OWLReasoner classify(ElkOwlAxiomData axiomData, OWLOntologyManager mgr, OWLReasonerFactory rf)
			throws Exception {
		LOG.info("Create ontology");
		OWLOntology ontology = mgr.createOntology();
		LOG.info("Add axioms");
		mgr.addAxioms(ontology, axiomData.axiomsSet);
		LOG.info("Create reasoner");
		OWLReasoner reasoner = rf.createReasoner(ontology);
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		LOG.info("Classified");
		return reasoner;
	}

	public HashMap<Integer, Set<OWLClass>> getParentsMap(ElkOwlAxiomData axiomData, OWLReasoner reasoner) {
		HashMap<Integer, Set<OWLClass>> parents_map = new HashMap<>();
		for (Entry<Integer, OWLClass> es : axiomData.nidConceptMap.entrySet()) {
			Set<OWLClass> parents = reasoner.getSuperClasses(es.getValue(), true).getFlattened();
			parents_map.put(es.getKey(), parents);
			assertTrue(parents.size() > 0);
		}
		return parents_map;
	}

	public IncrementalChanges makeEquivalent(ElkOwlAxiomData axiomData, int id) {
		ImmutableList<OWLAxiom> axioms = axiomData.nidAxiomsMap.get(id);
		assertEquals(1, axioms.size());
		OWLSubClassOfAxiom ax = (OWLSubClassOfAxiom) axioms.get(0);
		LOG.info("Axiom: " + ax);
		OWLClassExpression sub = ax.getSubClass();
		OWLClassExpression sup = ax.getSuperClass();
		OWLEquivalentClassesAxiom eq = ElkOwlManager.getOWLDataFactory().getOWLEquivalentClassesAxiom(sub, sup);
		LOG.info("Eq: " + eq);
		assertTrue(axiomData.axiomsSet.remove(ax));
		assertTrue(axiomData.axiomsSet.add(eq));
		axiomData.nidAxiomsMap.put(id, Lists.immutable.of(eq));
		IncrementalChanges inc = new ElkOwlAxiomDataBuilder.IncrementalChanges(Lists.immutable.of(eq),
				Lists.immutable.of(ax));
		return inc;
	}

	public void checkChildren(OWLReasoner reasoner, OWLClass clazz, int expect) {
		Set<OWLClass> children = reasoner.getSubClasses(clazz, true).getFlattened();
		LOG.info("Children: " + children.size() + " " + children);
		assertEquals(expect, children.size());
	}

	public HashMap<Integer, Set<OWLClass>> getChangedParents(HashMap<Integer, Set<OWLClass>> original_parents,
			HashMap<Integer, Set<OWLClass>> new_parents) {
		HashMap<Integer, Set<OWLClass>> changed_parents = new HashMap<>();
		for (Entry<Integer, Set<OWLClass>> es : new_parents.entrySet()) {
			if (!es.getValue().equals(original_parents.get(es.getKey())))
				changed_parents.put(es.getKey(), es.getValue());
		}
		assertEquals(24, changed_parents.size());
		return changed_parents;
	}

	// Chronic lung disease: [23e07078-f1e2-3f6a-9b7a-9397bcd91cfe]

	public HashMap<Integer, Set<OWLClass>> classifyAll() throws Exception {
		LOG.info("classifyAll");
		ElkOwlAxiomData axiomData = buildAxiomData();
		int id = PrimitiveData.nid(UUID.fromString("23e07078-f1e2-3f6a-9b7a-9397bcd91cfe"));
		LOG.info(PrimitiveData.text(id));
		OWLClass clazz = axiomData.nidConceptMap.get(id);
		LOG.info("" + clazz);
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		OWLReasonerFactory rf = (OWLReasonerFactory) new ElkReasonerFactory();
		HashMap<Integer, Set<OWLClass>> original_parents;
		{
			OWLReasoner reasoner = classify(axiomData, mgr, rf);
			checkChildren(reasoner, clazz, 1);
			original_parents = getParentsMap(axiomData, reasoner);
			makeEquivalent(axiomData, id);
		}
		{
			OWLReasoner reasoner = classify(axiomData, mgr, rf);
			checkChildren(reasoner, clazz, 23);
			HashMap<Integer, Set<OWLClass>> new_parents = getParentsMap(axiomData, reasoner);
			assertEquals(original_parents.keySet(), new_parents.keySet());
			HashMap<Integer, Set<OWLClass>> changed_parents = getChangedParents(original_parents, new_parents);
			return changed_parents;
		}
	}

	public HashMap<Integer, Set<OWLClass>> classifyInc() throws Exception {
		LOG.info("classifyInc");
		ElkOwlAxiomData axiomData = buildAxiomData();
		int id = PrimitiveData.nid(UUID.fromString("23e07078-f1e2-3f6a-9b7a-9397bcd91cfe"));
		LOG.info(PrimitiveData.text(id));
		OWLClass clazz = axiomData.nidConceptMap.get(id);
		LOG.info("" + clazz);
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		OWLReasonerFactory rf = (OWLReasonerFactory) new ElkReasonerFactory();
		{
			OWLReasoner reasoner = classify(axiomData, mgr, rf);
			checkChildren(reasoner, clazz, 1);
			HashMap<Integer, Set<OWLClass>> original_parents = getParentsMap(axiomData, reasoner);
			IncrementalChanges inc = makeEquivalent(axiomData, id);
			ElkOwlReasonerIncremental ir = ElkOwlReasonerIncremental.getInstance();
			ir.init(reasoner, axiomData);
			ir.processChanges(inc);
			ir.getReasoner().flush();
			checkChildren(ir.getReasoner(), clazz, 23);
			HashMap<Integer, Set<OWLClass>> new_parents = getParentsMap(axiomData, reasoner);
			assertEquals(original_parents.keySet(), new_parents.keySet());
			HashMap<Integer, Set<OWLClass>> changed_parents = getChangedParents(original_parents, new_parents);
			return changed_parents;
		}
	}

	@Test
	public void classifyCompare() throws Exception {
		HashMap<Integer, Set<OWLClass>> all = classifyAll();
		HashMap<Integer, Set<OWLClass>> inc = classifyInc();
		assertEquals(all, inc);
	}

}
