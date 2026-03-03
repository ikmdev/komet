
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

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import dev.ikm.tinkar.common.service.TinkExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.framework.EditedConceptTracker;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.reasoner.service.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;

public class RunReasonerIncrementalTask extends RunReasonerTaskBase {

	private static final Logger LOG = LoggerFactory.getLogger(RunReasonerIncrementalTask.class);

	public RunReasonerIncrementalTask(ReasonerService reasonerService,
	                                  Consumer<ClassifierResults> classifierResultsConsumer) {
		super(reasonerService, classifierResultsConsumer);
	}

	@Override
	protected ReasonerService compute() throws Exception {
		LOG.info("========================================");
		LOG.info("STARTING INCREMENTAL REASONER TASK");
		LOG.info("========================================");

		EditedConceptTracker.ensureSubscribed();
		EditedConceptTracker.addEditsFromChanges(reasonerService.getViewCalculator());

		if (!reasonerService.isIncrementalReady()) {
			updateMessage("Incremental reasoner not ready; running full reasoner");
			LOG.warn("*** Incremental reasoner NOT READY - falling back to FULL reasoner ***");
			return new RunReasonerFullTask(reasonerService, classifierResultsConsumer).compute();
		}

		int editCount = EditedConceptTracker.getEdits().size();
		if (editCount == 0) {
			updateMessage("No incremental edits found; running full reasoner");
			LOG.warn("*** NO EDITS FOUND - falling back to FULL reasoner ***");
			return new RunReasonerFullTask(reasonerService, classifierResultsConsumer).compute();
		}

		LOG.info("Proceeding with incremental reasoning for {} edits", editCount);
		return super.compute();
	}

	private final boolean logParents = false;

	protected void loadData(int workDone) throws Exception {
		updateMessage("Step " + workDone + ": Loading data into reasoner");
		LoadDataTask task = new LoadDataTask(reasonerService);
		Future<ReasonerService> future = TinkExecutor.threadPool().submit(task);
		future.get();
		updateProgress(workDone);
		
		// After full reasoning completes, reset the tracker for incremental updates
		LOG.info("Full reasoning complete - resetting EditedConceptTracker");
		EditedConceptTracker.removeEdits();
		EditedConceptTracker.ensureSubscribed();
	}

	private void logParents() {
		LOG.info(">>>>>");
		for (SemanticEntityVersion edit : EditedConceptTracker.getEdits()) {
			int editNid = edit.referencedComponentNid();
			LOG.info("Con: " + editNid + " " + PrimitiveData.text(editNid));
			reasonerService.getParents(editNid).forEach(parent -> {
				LOG.info("Parent: " + parent + " " + PrimitiveData.text(parent));
			});
		}
		LOG.info(">>>>>");
	}

}