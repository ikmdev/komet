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
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.PatternFacade;

public class RunElkOwlReasonerIncrementalTask extends TrackingCallable<ReasonerService> {

	private static final Logger LOG = LoggerFactory.getLogger(RunElkOwlReasonerIncrementalTask.class);

	private final ReasonerService reasonerService;

	private final Consumer<ClassifierResults> classifierResultsConsumer;

	private final ViewCalculator viewCalculator;

	private final PatternFacade statedAxiomPattern;

	private final PatternFacade inferredAxiomPattern;

	public RunElkOwlReasonerIncrementalTask(ReasonerService reasonerService,
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
		updateMessage("Step " + workDone + ": Computing taxonomy");
		LOG.info(msg);
		reasonerService.computeInferences();
		logParents();
		EditedConceptTracker.removeEdits();
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

//	protected ElkOwlAxiomData compute1() throws Exception {
//
//		// Occupations [753d2b35-3924-5f9c-a6c7-a5c3a55fda29]
//		// Occupation [4d0506d1-d961-5bf9-9a7f-bb1a702c7425]
//		int occupationsNid = PrimitiveData.nid(UUID.fromString("753d2b35-3924-5f9c-a6c7-a5c3a55fda29"));
//		int occupationNid = PrimitiveData.nid(UUID.fromString("4d0506d1-d961-5bf9-9a7f-bb1a702c7425"));
//		TempEditUtil editor = new TempEditUtil(viewCalculator, statedAxiomPattern);
//		DiTreeEntity editedDefinition = editor.setParent(occupationNid, occupationsNid);
//
//		// Do the elk incremental part...
//		final int maxWork = 4;
//		int workDone = 1;
//		updateMessage("Step " + workDone + ": Build changes");
//		ElkOwlAxiomData axiomData = ElkOwlReasonerIncremental.getInstance().getAxiomData();
//		ElkOwlAxiomDataBuilder builder = new ElkOwlAxiomDataBuilder(viewCalculator, statedAxiomPattern, axiomData);
//		IncrementalChanges changes = builder.processIncremental(editedDefinition, occupationNid);
//		updateProgress(workDone++, maxWork);
//		updateMessage("Step " + workDone + ": Process changes");
//		ElkOwlReasonerIncremental.getInstance().processChanges(changes);
//		LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>");
//		OWLClass sub = axiomData.nidConceptMap.get(occupationNid);
//		OWLClass sup = axiomData.nidConceptMap.get(occupationsNid);
//		LOG.info("Sub: " + sub + " " + PrimitiveData.text(Integer.parseInt(sub.getIRI().getShortForm())));
//		LOG.info("Sup: " + sup + " " + PrimitiveData.text(Integer.parseInt(sup.getIRI().getShortForm())));
//		ElkOwlReasonerIncremental.getInstance().getReasoner().flush();
//		for (OWLClass parent : ElkOwlReasonerIncremental.getInstance().getReasoner().getSuperClasses(sub, true)
//				.getFlattened()) {
//			LOG.info("Parent: " + parent + " " + PrimitiveData.text(Integer.parseInt(parent.getIRI().getShortForm())));
//		}
//		LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>");
//		updateProgress(workDone++, maxWork);
//		updateMessage("Step " + workDone + ": Processing results");
//		ProcessElkOwlResultsTask processResultsTask = new ProcessElkOwlResultsTask(
//				ElkOwlReasonerIncremental.getInstance().getReasoner(), this.viewCalculator, this.inferredAxiomPattern,
//				axiomData);
//		Future<ClassifierResults> processResultsFuture = TinkExecutor.threadPool().submit(processResultsTask);
//		ClassifierResults classifierResults = processResultsFuture.get();
//
//		updateProgress(workDone++, maxWork);
//		classifierResultsConsumer.accept(classifierResults);
//
//		updateMessage("Reasoner run complete in " + durationString());
//		return axiomData;
//	}

}
