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

import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.reasoner.service.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;

public abstract class RunReasonerTaskBase extends TrackingCallable<ReasonerService> {

	private static final Logger LOG = LoggerFactory.getLogger(RunReasonerTaskBase.class);

	protected final ReasonerService reasonerService;

	protected final Consumer<ClassifierResults> classifierResultsConsumer;

	private final int maxWork = 4;

	public RunReasonerTaskBase(ReasonerService reasonerService, Consumer<ClassifierResults> classifierResultsConsumer) {
		super(true, true);
		this.reasonerService = reasonerService;
		this.classifierResultsConsumer = classifierResultsConsumer;
		updateTitle("Running reasoner (" + reasonerService.getClass().getSimpleName() + "): "
				+ reasonerService.getViewCalculator()
						.getPreferredDescriptionTextWithFallbackOrNid(reasonerService.getStatedAxiomPattern()));
	}

	@Override
	public void updateMessage(String msg) {
		super.updateMessage(msg);
		LOG.info(msg);
	}

	protected void updateProgress(int workDone) {
		updateProgress(workDone, maxWork);
	}

	@Override
	protected ReasonerService compute() throws Exception {
		reasonerService.setProgressUpdater(this);
		int workDone = 0;
		updateProgress(workDone++);
		loadData(workDone++);
		computeInferences(workDone++);
		buildNecessaryNormalForm(workDone++);
		processResults(workDone++);
		updateMessage("Reasoner run complete in " + durationString());
		return reasonerService;
	}

	protected abstract void loadData(int workDone) throws Exception;

	protected void computeInferences(int workDone) throws Exception {
		updateMessage("Step " + workDone + " of " + maxWork + ": Computing inferences");
		ComputeInferencesTask task = new ComputeInferencesTask(reasonerService);
		Future<ReasonerService> future = TinkExecutor.threadPool().submit(task);
		future.get();
		updateProgress(workDone);
	}

	protected void buildNecessaryNormalForm(int workDone) throws Exception {
		updateMessage("Step " + workDone + " of " + maxWork + ": Building necessary normal form");
		BuildNecessaryNormalFormTask task = new BuildNecessaryNormalFormTask(reasonerService);
		Future<ReasonerService> future = TinkExecutor.threadPool().submit(task);
		future.get();
		updateProgress(workDone);

	}

	protected void processResults(int workDone) throws Exception {
		updateMessage("Step " + workDone + " of " + maxWork + ": Processing results");
		ProcessResultsTask task = new ProcessResultsTask(reasonerService);
		Future<ClassifierResults> future = TinkExecutor.threadPool().submit(task);
		ClassifierResults classifierResults = future.get();
		updateProgress(workDone);
		classifierResultsConsumer.accept(classifierResults);
	}

}
