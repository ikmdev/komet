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
package dev.ikm.komet.reasoner.ui;

import dev.ikm.komet.reasoner.ReasonerResultsNode;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.reasoner.service.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessResultsTask extends TrackingCallable<ClassifierResults> {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessResultsTask.class);

	private final ReasonerService reasonerService;

	public ProcessResultsTask(ReasonerService reasonerService) {
		super(false, true);
		this.reasonerService = reasonerService;
		updateTitle("Processing reasoner results");
	}

	@Override
	protected ClassifierResults compute() throws Exception {
		updateMessage("Getting classified results");
		LOG.info("Getting classified results...");
		ClassifierResults classifierResults = reasonerService.writeInferredResults();
		updateMessage("Processed results in " + durationString());
		return classifierResults;
	}
}
