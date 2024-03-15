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

import dev.ikm.komet.reasoner.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.PatternFacade;

/**
 * Reasoning Tasks
 * <p>
 * Reasoning tasks are: axiom consistency, class satisfiability, classification
 * <p>
 * Consistency: this is the task of ensuring that the set of axioms is free of
 * contradictions. For example, consider the following two facts: 1) all birds
 * can fly and 2) penguins are birds which cannot fly. This would trigger an
 * inconsistency because if a penguin is a bird then it can fly but another fact
 * is stating otherwise.
 * <p>
 * Class Satisfiability: this is the task of determining whether it is possible
 * for a class to have instances without causing inconsistency. For example, if
 * we say that students can either be good OR bad, then the class
 * GoodAndBadStudent would be unsatisfiable (i.e., it cannot have instances if
 * the set of axioms are to be consistent). Typically, we are interested in
 * modeling classes that can have at least one instance; therefore, having
 * unsatisfiable classes usually suggests a modeling error.
 * <p>
 * Compute taxonomy (aka Classification): this is the task of determining the
 * subclass relationships between classes in order to complete the class
 * hierarchy. For example, .. (left for the reader :P)
 */
public class RunElkOwlReasonerTask extends TrackingCallable<ReasonerService> {

	private static final Logger LOG = LoggerFactory.getLogger(RunElkOwlReasonerTask.class);

	private final ReasonerService reasonerService;

	private final Consumer<ClassifierResults> classifierResultsConsumer;

	private final ViewCalculator viewCalculator;

	private final PatternFacade statedAxiomPattern;

	private final PatternFacade inferredAxiomPattern;

	public RunElkOwlReasonerTask(ReasonerService reasonerService,
			Consumer<ClassifierResults> classifierResultsConsumer) {
		super(true, true);
		this.reasonerService = reasonerService;
		this.classifierResultsConsumer = classifierResultsConsumer;
		this.viewCalculator = reasonerService.getViewCalculator();
		this.statedAxiomPattern = reasonerService.getStatedAxiomPattern();
		this.inferredAxiomPattern = reasonerService.getInferredAxiomPattern();
		updateTitle("Running reasoner (" + reasonerService.getClass().getSimpleName() + "): "
				+ reasonerService.getViewCalculator()
						.getPreferredDescriptionTextWithFallbackOrNid(reasonerService.getStatedAxiomPattern()));
		updateProgress(0, 4);
	}

	@Override
	protected ReasonerService compute() throws Exception {
		reasonerService.setProgressUpdater(this);
		final int maxWork = 4;
		int workDone = 1;
		String msg;
		//
		msg = "Step " + workDone + ": Extracting data";
		updateMessage(msg);
		LOG.info(msg);
		ExtractElkOwlAxiomsTask extractTask = new ExtractElkOwlAxiomsTask(reasonerService);
		Future<ReasonerService> extractFuture = TinkExecutor.threadPool().submit(extractTask);
		extractFuture.get();
		updateProgress(workDone++, maxWork);
		//
		msg = "Step " + workDone + ": Loading data into reasoner";
		updateMessage(msg);
		LOG.info(msg);
		LoadElkOwlAxiomsTask loadTask = new LoadElkOwlAxiomsTask(reasonerService);
		Future<ReasonerService> loadFuture = TinkExecutor.threadPool().submit(loadTask);
		loadFuture.get();
		updateProgress(workDone++, maxWork);
		//
		msg = "Step " + workDone + ": Computing taxonomy";
		updateMessage("Step " + workDone + ": Computing taxonomy");
		LOG.info(msg);
		ComputeElkOwlInferencesTask classifyTask = new ComputeElkOwlInferencesTask(reasonerService);
		Future<ReasonerService> classifyFuture = TinkExecutor.threadPool().submit(classifyTask);
		classifyFuture.get();
		updateProgress(workDone++, maxWork);
		//
		msg = "Step " + workDone + ": Processing results";
		updateMessage(msg);
		LOG.info(msg);
		ProcessElkOwlResultsTask processResultsTask = new ProcessElkOwlResultsTask(reasonerService);
		Future<ClassifierResults> processResultsFuture = TinkExecutor.threadPool().submit(processResultsTask);
		ClassifierResults classifierResults = processResultsFuture.get();
		updateProgress(workDone++, maxWork);
		//
		classifierResultsConsumer.accept(classifierResults);
		updateMessage("Reasoner run complete in " + durationString());
		return reasonerService;
	}

}
