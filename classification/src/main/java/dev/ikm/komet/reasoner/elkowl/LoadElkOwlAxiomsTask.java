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

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.TrackingCallable;

public class LoadElkOwlAxiomsTask extends TrackingCallable<OWLReasoner> {

	private static final Logger LOG = LoggerFactory.getLogger(LoadElkOwlAxiomsTask.class);

	final ElkOwlAxiomData axiomData;

	private OWLReasoner reasoner;

	public LoadElkOwlAxiomsTask(ElkOwlAxiomData axiomData) {
		super(true, true);
		this.axiomData = axiomData;
		updateTitle("Loading axioms into reasoner. ");
	}

	@Override
	protected OWLReasoner compute() throws Exception {
		int axiomCount = this.axiomData.processedSemantics.get();
		updateProgress(0, axiomCount);
		LOG.info("Create ontology");
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = mgr.createOntology();
		LOG.info("Add axioms");
		mgr.addAxioms(ontology, axiomData.axiomsSet);
		LOG.info("Create reasoner");
		OWLReasonerFactory rf = (OWLReasonerFactory) new ElkReasonerFactory();
		reasoner = rf.createReasoner(ontology);
		updateMessage("Load in " + durationString());
		LOG.info("Load in " + durationString());
		return this.reasoner;
	}

}
