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

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.TrackingCallable;

public class ComputeElkOwlInferencesTask extends TrackingCallable<Void> {

	private static final Logger LOG = LoggerFactory.getLogger(ComputeElkOwlInferencesTask.class);

	final OWLReasoner reasoner;

	public ComputeElkOwlInferencesTask(OWLReasoner reasoner) {
		super(false, true);
		this.reasoner = reasoner;
		updateTitle("Computing taxonomy. ");
	}

	@Override
	protected Void compute() throws Exception {
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		updateMessage("Computed taxonomy in " + durationString());
		LOG.info("Computed taxonomy in " + durationString());
		return null;
	}

}