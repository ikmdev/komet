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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.owl.SnomedOwlOntology;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;

public class SnomedUS20230901ElkOwlClassifierTestIT extends SnomedUS20230901ElkOwlDataBuilderTestIT {

	private static final Logger LOG = LoggerFactory.getLogger(SnomedUS20230901ElkOwlClassifierTestIT.class);

	private HashMap<Integer, Long> nid_sctid_map;

	private Set<Long> toSctids(Set<Long> nids) {
		return nids.stream().map(x -> nid_sctid_map.get(x.intValue())).collect(Collectors.toSet());
	}

	@Test
	public void isas() throws Exception {
		SnomedOwlOntology ontology = SnomedOwlOntology.createOntology();
		ViewCalculator viewCalculator = getViewCalculator();
		OWLDataFactory df = ontology.getDataFactory();
		ElkOwlData axiomData = new ElkOwlData(df);
		ElkOwlDataBuilder builder = new ElkOwlDataBuilder(viewCalculator, TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
				axiomData, df);
		builder.build();
		ontology.loadOntology(axiomData.axiomsSet);
		{
//			LOG.info("Props>>>>>");
			OWLOntology oo = ontology.getOntology();
//			assertEquals(371575, oo.getAxiomCount());
//			assertEquals(370018, oo.getSignature().size());
//			assertEquals(369879, oo.getClassesInSignature().size());
//			assertEquals(126, oo.getObjectPropertiesInSignature().size());
//			assertEquals(5, oo.getAxioms(AxiomType.SUB_PROPERTY_CHAIN_OF).size());
//			oo.getAxioms(AxiomType.SUB_PROPERTY_CHAIN_OF).forEach(x -> LOG.info("" + x));
			assertEquals(4, oo.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY).size());
//			oo.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY).forEach(x -> {
//				LOG.info("" + x);
//				long id = SnomedOwlOntology.getId(x.getProperty().asOWLObjectProperty());
//				LOG.info("" + axiomData.nidAxiomsMap.get((int) id));
//			});
			assertEquals(2, oo.getAxioms(AxiomType.REFLEXIVE_OBJECT_PROPERTY).size());
//			oo.getAxioms(AxiomType.REFLEXIVE_OBJECT_PROPERTY).forEach(x -> LOG.info("" + x));
		}
		ontology.classify();
		TreeSet<Long> misses = new TreeSet<>();
		TreeSet<Long> other_misses = new TreeSet<>();
		int miss_cnt = 0;
		int pharma_miss_cnt = 0;
		int other_miss_cnt = 0;
		SnomedIsa isas = SnomedIsa.init(rels_file);
		nid_sctid_map = new HashMap<>();
		for (long sctid : isas.getOrderedConcepts()) {
			UUID uuid = UuidUtil.fromSNOMED("" + sctid);
			int nid = PrimitiveData.nid(uuid);
			nid_sctid_map.put(nid, sctid);
			// 417163006 |Traumatic or non-traumatic injury (disorder)|
//			if (sctid == 417163006)
//				LOG.info(">>>>>>>>>>>>" + sctid + " " + nid + " " + PrimitiveData.text(nid));
		}
		for (OWLClass clazz : ontology.getOwlClasses()) {
			long nid = SnomedOwlOntology.getId(clazz);
			Set<Long> sups = toSctids(ontology.getSuperClasses(nid));
			Long sctid = nid_sctid_map.get((int) nid);
			if (sctid == null) {
//				LOG.error("No concept: " + nid + " " + PrimitiveData.text((int) nid));
				continue;
			}
			Set<Long> parents = isas.getParents(sctid);
			if (sctid == SnomedIds.root) {
				assertTrue(parents.isEmpty());
			} else {
				assertNotNull(parents);
			}
			if (!parents.equals(sups)) {
				misses.add(sctid);
				miss_cnt++;
				if (isas.hasAncestor(sctid, 373873005)) {
					// 373873005 |Pharmaceutical / biologic product (product)|
					pharma_miss_cnt++;
				} else if (isas.hasAncestor(sctid, 127785005)) {
					// 127785005 |Administration of substance to produce immunity, either active or
					// passive (procedure)|
				} else if (isas.hasAncestor(sctid, 713404003)) {
					// 713404003 |Vaccination given (situation)|
				} else if (isas.hasAncestor(sctid, 591000119102l)) {
					// 591000119102 |Vaccine declined by patient (situation)|
				} else if (isas.hasAncestor(sctid, 90351000119108l)) {
					// 90351000119108 |Vaccination not done (situation)|
				} else if (isas.hasAncestor(sctid, 293104008)) {
					// 293104008 |Adverse reaction to component of vaccine product (disorder)|
				} else if (isas.hasAncestor(sctid, 266758009)) {
					// 266758009 |Immunization contraindicated (situation)|
				} else {
					other_misses.add(sctid);
					other_miss_cnt++;
				}
			}
		}
		isas.getOrderedConcepts().stream().filter(other_misses::contains)
//		.limit(10)
		.forEach((sctid) -> {
			UUID uuid = UuidUtil.fromSNOMED("" + sctid);
			int nid = PrimitiveData.nid(uuid);
			LOG.error("Miss: " + sctid + " " + PrimitiveData.text(nid));
			Set<Long> sups = toSctids(ontology.getSuperClasses(nid));
			Set<Long> parents = isas.getParents(sctid);
			HashSet<Long> par = new HashSet<>(parents);
			par.removeAll(sups);
			HashSet<Long> sup = new HashSet<>(sups);
			sup.removeAll(parents);
			LOG.error("Sno:  " + par);
			LOG.error("Elk:  " + sup);
		});
		LOG.error("Miss cnt: " + miss_cnt);
		LOG.error("Pharma cnt: " + pharma_miss_cnt);
		LOG.error("Other cnt: " + other_miss_cnt);
//		assertEquals(expected_miss_cnt, miss_cnt);
		// TODO this should be 0 after all the data issues are fixed
		assertEquals(251, other_miss_cnt);
	}

}
