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
import java.util.function.Consumer;

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
		if (!reasonerService.isIncrementalReady())
			throw new Exception("Need to run full reasoner first");
		if (EditedConceptTracker.getEdits().isEmpty())
			throw new Exception("No edits to process");
		return super.compute();
	}

	private final boolean logParents = false;

	protected void loadData(int workDone) {
		updateMessage("Step " + workDone + ": Build changes");
		if (logParents)
			logParents();
		for (SemanticEntityVersion edit : EditedConceptTracker.getEdits()) {
			LOG.info("\nEdit: " + edit.referencedComponentNid() + " "
					+ PrimitiveData.text(edit.referencedComponentNid()) + "\n" + edit);
		}
		reasonerService.processIncremental(List.of(), EditedConceptTracker.getEdits());
		EditedConceptTracker.removeEdits();
		updateProgress(workDone);
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
