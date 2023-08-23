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

import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import dev.ikm.komet.reasoner.elkowl.ElkOwlAxiomDataBuilder.IncrementalChanges;

public class ElkOwlReasonerIncremental {

	private static ElkOwlReasonerIncremental instance;

	private OWLReasoner reasoner;

	ElkOwlAxiomData axiomData;

	public static ElkOwlReasonerIncremental getInstance() {
		if (instance == null)
			instance = new ElkOwlReasonerIncremental();
		return instance;
	}

	public void init(OWLReasoner reasoner, ElkOwlAxiomData axiomData) {
		this.reasoner = reasoner;
		this.axiomData = axiomData;
	}

	public OWLReasoner getReasoner() {
		return reasoner;
	}

	public ElkOwlAxiomData getAxiomData() {
		return axiomData;
	}

	public OWLOntologyManager getOntologyManager() {
		return getOntology().getOWLOntologyManager();
	}

	public OWLOntology getOntology() {
		return reasoner.getRootOntology();
	}

	public void addAxiom(OWLAxiom axiom) {
		getOntologyManager().addAxiom(getOntology(), axiom);
	}

	public void processChanges(IncrementalChanges changes) {
		getOntologyManager().removeAxioms(getOntology(), new HashSet<>(changes.getDeletions().castToList()));
		getOntologyManager().addAxioms(getOntology(), new HashSet<>(changes.getAdditions().castToList()));
	}

}
