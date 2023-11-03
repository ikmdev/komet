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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.reasoner.ClassifierResults;
import dev.ikm.tinkar.common.service.PrimitiveData;
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
public class RunElkOwlReasonerTask extends TrackingCallable<ElkOwlAxiomData> {

	private static final Logger LOG = LoggerFactory.getLogger(RunElkOwlReasonerTask.class);

	final ViewCalculator viewCalculator;

	final PatternFacade statedAxiomPattern;

	final PatternFacade inferredAxiomPattern;

	final Consumer<ClassifierResults> classifierResultsConsumer;

	public RunElkOwlReasonerTask(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern,
			PatternFacade inferredAxiomPattern, Consumer<ClassifierResults> classifierResultsConsumer) {
		super(true, true);
		this.viewCalculator = viewCalculator;
		this.statedAxiomPattern = statedAxiomPattern;
		this.inferredAxiomPattern = inferredAxiomPattern;
		this.classifierResultsConsumer = classifierResultsConsumer;
		updateTitle(
				"Running reasoner: " + viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(statedAxiomPattern));
		updateProgress(0, 4);
	}

	// mvn javafx:run -f pom.xml

	@Override
	protected ElkOwlAxiomData compute() throws Exception {
		final int maxWork = 4;
		int workDone = 1;
		String msg;
		//
		msg = "Step " + workDone + ": Extracting axioms ("
				+ viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(statedAxiomPattern) + ")";
		updateMessage(msg);
		LOG.info(msg);
		ExtractElkOwlAxiomsTask extractTask = new ExtractElkOwlAxiomsTask(viewCalculator, statedAxiomPattern);
		Future<ElkOwlAxiomData> axiomDataFuture = TinkExecutor.threadPool().submit(extractTask);
		ElkOwlAxiomData axiomData = axiomDataFuture.get();
		updateProgress(workDone++, maxWork);
		//
		msg = "Step " + workDone + ": Loading axioms into reasoner";
		updateMessage(msg);
		LOG.info(msg);
		LoadElkOwlAxiomsTask loadTask = new LoadElkOwlAxiomsTask(axiomData);
		Future<OWLReasoner> reasonerFuture = TinkExecutor.threadPool().submit(loadTask);
		OWLReasoner reasoner = reasonerFuture.get();
		updateProgress(workDone++, maxWork);
		//
		msg = "Step " + workDone + ": Computing taxonomy";
		updateMessage("Step " + workDone + ": Computing taxonomy");
		LOG.info(msg);
		ComputeElkOwlInferencesTask classifyTask = new ComputeElkOwlInferencesTask(reasoner);
		Future<Void> classifyFuture = TinkExecutor.threadPool().submit(classifyTask);
		classifyFuture.get();
		updateProgress(workDone++, maxWork);
		//
		msg = "Step " + workDone + ": Processing results";
		updateMessage(msg);
		LOG.info(msg);
		ProcessElkOwlResultsTask processResultsTask = new ProcessElkOwlResultsTask(reasoner, this.viewCalculator,
				this.inferredAxiomPattern, axiomData);
		Future<ClassifierResults> processResultsFuture = TinkExecutor.threadPool().submit(processResultsTask);
		ClassifierResults classifierResults = processResultsFuture.get();
		updateProgress(workDone++, maxWork);
		//
		classifierResultsConsumer.accept(classifierResults);

		ElkOwlReasonerIncremental.getInstance().init(reasoner, axiomData);

		updateMessage("Reasoner run complete in " + durationString());
		return axiomData;
	}

	private boolean write = true;

	private Path getPath(String filePart) {
		Path path = Paths.get("..", "classification", "target",
				this.getClass().getSimpleName() + "-" + filePart + ".txt");
		LOG.info("Write to : " + path);
		return path;
	}

	private void writeAxioms(ElkOwlAxiomData axiomData) throws Exception {
		LOG.info(">>>>>");
		LOG.info("Writing axioms: " + write);
		if (write) {
			Files.write(getPath("concepts"), axiomData.nidConceptMap.entrySet().stream() //
					.map(Entry::getKey) //
					.map(key -> key + "\t" + PrimitiveData.publicId(key).asUuidArray()[0] + "\t"
							+ PrimitiveData.text(key)) //
					.collect(Collectors.toList()));
			Files.write(getPath("roles"), axiomData.nidRoleMap.entrySet().stream() //
					.map(Entry::getKey) //
					.map(key -> key + "\t" + PrimitiveData.publicId(key).asUuidArray()[0] + "\t"
							+ PrimitiveData.text(key)) //
					.collect(Collectors.toList()));
			Files.write(getPath("axioms"), axiomData.axiomsSet.stream() //
					.map(ElkOwlManager::removePrefix) //
					.collect(Collectors.toList()));
			LOG.info("Write axioms in " + durationString());
		}
	}

	private void writeOntology(OWLOntology ontology) throws Exception {
		LOG.info(">>>>>");
		LOG.info("Writing ontology: " + write);
		if (write) {
			ElkOwlManager.writeOntology(ontology, getPath("ofn"));
			LOG.info("Write ontology in " + durationString());
		}
	}

}
