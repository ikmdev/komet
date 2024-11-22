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

import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.framework.EditedConceptTracker;
import dev.ikm.komet.reasoner.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;

public class RunReasonerIncrementalTask extends TrackingCallable<ReasonerService> {

	private static final Logger LOG = LoggerFactory.getLogger(RunReasonerIncrementalTask.class);

	private final ReasonerService reasonerService;

	private final Consumer<ClassifierResults> classifierResultsConsumer;

	public RunReasonerIncrementalTask(ReasonerService reasonerService,
			Consumer<ClassifierResults> classifierResultsConsumer) {
		super(true, true);
		this.reasonerService = reasonerService;
		this.classifierResultsConsumer = classifierResultsConsumer;
		updateTitle("Running reasoner (" + reasonerService.getClass().getSimpleName() + "): "
				+ reasonerService.getViewCalculator()
						.getPreferredDescriptionTextWithFallbackOrNid(reasonerService.getStatedAxiomPattern()));
		updateProgress(0, 4);
	}

	@Override
	protected ReasonerService compute() throws Exception {
		if (!reasonerService.isIncrementalReady())
			throw new Exception("Need to run full reasoner first");
		final int maxWork = 4;
		int workDone = 1;
		String msg;
		//
		msg = "Step " + workDone + ": Build changes";
		updateMessage(msg);
		LOG.info(msg);
		logParents();
		for (SemanticVersionRecord edit : EditedConceptTracker.getEdits()) {
			DiTreeEntity def = (DiTreeEntity) edit.fieldValues().get(0);
			LOG.info("Edit: " + edit.referencedComponentNid() + " " + def);
			reasonerService.processIncremental(def, edit.referencedComponentNid());
		}
		updateProgress(workDone++, maxWork);
		//
		msg = "Step " + workDone + ": Computing taxonomy";
		updateMessage(msg);
		LOG.info(msg);
		reasonerService.computeInferences();
		logParents();
		EditedConceptTracker.removeEdits();
		updateProgress(workDone++, maxWork);
		//
		msg = "Step " + workDone + ": Processing results";
		updateMessage(msg);
		LOG.info(msg);
		ProcessResultsTask processResultsTask = new ProcessResultsTask(reasonerService);
		Future<ClassifierResults> processResultsFuture = TinkExecutor.threadPool().submit(processResultsTask);
		ClassifierResults classifierResults = processResultsFuture.get();
		updateProgress(workDone++, maxWork);
		//
		classifierResultsConsumer.accept(classifierResults);
		updateMessage("Reasoner run complete in " + durationString());
		return reasonerService;
	}

	private void logParents() {
		LOG.info(">>>>>");
		for (SemanticVersionRecord edit : EditedConceptTracker.getEdits()) {
			int editNid = edit.referencedComponentNid();
			LOG.info("Con: " + editNid + " " + PrimitiveData.text(editNid));
			reasonerService.getParents(editNid).forEach(parent -> {
				LOG.info("Parent: " + parent + " " + PrimitiveData.text(parent));
			});
		}
		LOG.info(">>>>>");
	}

}
