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

import java.util.Arrays;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.jgrapht.Graph;
import org.jgrapht.alg.isomorphism.AHURootedTreeIsomorphismInspector;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.reasoner.ClassifierResults;
import dev.ikm.komet.reasoner.ReasonerResultsNode;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.common.util.time.MultipleEndpointTimer;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticRecordBuilder;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.JGraphUtil;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiomSemantic;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionAdaptorFactory;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.entity.graph.isomorphic.IsomorphicResults;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;

public class ProcessElkOwlResultsTask extends TrackingCallable<ClassifierResults> {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessElkOwlResultsTask.class);

	private final ReasonerService reasonerService;

	static final int classificationCountDuplicatesToLog = 10;

	private final ViewCalculator viewCalculator;

	private final PatternFacade inferredAxiomPattern;

	private ViewCoordinateRecord commitView;

	private AtomicInteger classificationDuplicateCount = new AtomicInteger(-1);

	// (Hcys)2 Ur Ql: [c6dfb251-a75f-547e-944a-3fd2d115ceb2]
	// final int watchNid =
	// PrimitiveData.nid(UUID.fromString("c6dfb251-a75f-547e-944a-3fd2d115ceb2"));

	// Chronic lung disease: [23e07078-f1e2-3f6a-9b7a-9397bcd91cfe]
	private final int watchNid = PrimitiveData.nid(UUID.fromString("23e07078-f1e2-3f6a-9b7a-9397bcd91cfe"));

	public ProcessElkOwlResultsTask(ReasonerService reasonerService) {
		super(false, true);
		this.reasonerService = reasonerService;
		this.viewCalculator = reasonerService.getViewCalculator();
		this.inferredAxiomPattern = reasonerService.getInferredAxiomPattern();
		updateTitle("Processing reasoner results");
	}

	@Override
	protected ClassifierResults compute() throws Exception {
		updateMessage("Getting classified results");
		LOG.info("Getting classified results...");
		Transaction updateTransaction = Transaction.make("Committing classification");
		ClassifierResults classifierResults = collectResults(reasonerService.getReasonerConceptSet(),
				updateTransaction);
		updateMessage("Processed results in " + durationString());
		return classifierResults;
	}

	/**
	 * Collect results.
	 *
	 * @param classificationConceptSet the concepts processed by the classifier
	 * @return the classifier results
	 */
	private ClassifierResults collectResults(ImmutableIntList classificationConceptSet, Transaction updateTransaction) {
		updateMessage("Collecting reasoner results. ");
		LOG.info("Collecting reasoner results... {}", classificationConceptSet.size());
		addToTotalWork(classificationConceptSet.size() * 2); // get each node, then write back inferred.
		final HashSet<ImmutableIntList> equivalentSets = new HashSet<>();
		LOG.debug("collect results begins for {} concepts", classificationConceptSet.size());
		classificationConceptSet.primitiveParallelStream().forEach((conceptNid) -> {
			completedUnitOfWork();
			ImmutableIntSet equivalentNids = reasonerService.getEquivalent(conceptNid);
			if (equivalentNids == null) {
				LOG.error("Null node for: {} {} {} will be skipped in classifier results collect", conceptNid,
						PrimitiveData.publicId(conceptNid).idString(), PrimitiveData.text(conceptNid));
				// TODO possibly propagate error in gui...
			} else {
				if (equivalentNids.size() > 1) {
					equivalentSets.add(equivalentNids.toSortedList().toImmutable());
				}
			}
		});

		updateMessage("Writing back inferred. ");
		LOG.info("Writing back inferred...");

		ConcurrentHashSet<Integer> conceptsWithInferredChanges = new ConcurrentHashSet<>();
		ConcurrentHashSet<Integer> conceptsWithNavigationChanges = new ConcurrentHashSet<>();

		ViewCoordinateRecord commitView = writeBackInferred(reasonerService.getReasonerConceptSet(),
				conceptsWithInferredChanges, conceptsWithNavigationChanges, updateTransaction);

		int[] conceptsWithNavigationChangesNidArray = conceptsWithNavigationChanges.stream()
				.mapToInt(boxedInt -> (int) boxedInt).toArray();
		Arrays.sort(conceptsWithNavigationChangesNidArray);
		ImmutableIntList conceptsWithNavigationChangesAsList = IntLists.immutable
				.of(conceptsWithNavigationChangesNidArray);

		int[] conceptsWithInferredChangesNidArray = conceptsWithInferredChanges.stream()
				.mapToInt(boxedInt -> (int) boxedInt).toArray();
		Arrays.sort(conceptsWithInferredChangesNidArray);
		ImmutableIntList conceptsWithInferredChangesAsList = IntLists.immutable.of(conceptsWithInferredChangesNidArray);

		return new ClassifierResults(classificationConceptSet, conceptsWithInferredChangesAsList,
				conceptsWithNavigationChangesAsList, equivalentSets, commitView);
	}

	/**
	 * Write back inferred.
	 */
	private ViewCoordinateRecord writeBackInferred(ImmutableIntList classificationConceptSet,
			ConcurrentHashSet<Integer> conceptNidsWithInferredChanges,
			ConcurrentHashSet<Integer> conceptsWithNavigationChanges, Transaction updateTransaction) {
		// TODO change type of affectedConcepts to a parallel friendly primitive class.
		final AtomicInteger sufficientSets = new AtomicInteger();
		StampEntity updateStamp = updateTransaction.getStamp(State.ACTIVE,
				getViewCoordinateRecord().getAuthorNidForChanges(), getViewCoordinateRecord().getDefaultModuleNid(),
				getViewCoordinateRecord().getDefaultPathNid());
		int updateStampNid = updateStamp.nid();

//		LOG.debug("Write back inferred begins with {} axioms", inferredAxioms.getInferredAxioms().size());
		// TODO Dan notes, for reasons not yet understood, this parallelStream call
		// isn't working.
		// JVisualVM tells me that all of this work is occurring on a single thread.
		// Need to figure out why...
		final int statedPatternNid = getViewCoordinateRecord().logicCoordinate().statedAxiomsPatternNid();
		final int inferredPatternNid = getViewCoordinateRecord().logicCoordinate().inferredAxiomsPatternNid();
		final int inferredNavigationPatternNid = TinkarTerm.INFERRED_NAVIGATION_PATTERN.nid();

		OptionalInt optionalAxiomsIndex = this.viewCalculator.getIndexForMeaning(inferredPatternNid,
				TinkarTerm.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS.nid());
		if (optionalAxiomsIndex.isEmpty() || optionalAxiomsIndex.getAsInt() != 0) {
			throw new IllegalStateException(
					"Index for "
							+ this.viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(
									TinkarTerm.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS)
							+ " is " + optionalAxiomsIndex);
		}
		int axiomsIndex = optionalAxiomsIndex.getAsInt();

		LogicalExpressionAdaptorFactory logicalExpressionAdaptor = new LogicalExpressionAdaptorFactory();
//		AtomicInteger writtenCounter = new AtomicInteger();
		MultipleEndpointTimer multipleEndpointTimer = new MultipleEndpointTimer(IsomorphicResults.EndPoints.class);

		classificationConceptSet.primitiveParallelStream().forEach(conceptNid -> {
//			int cnt = writtenCounter.incrementAndGet();
//			if (cnt % 1000 == 0)
//				LOG.info("Wrote " + cnt);

			if (conceptNid == watchNid) {
				LOG.info("found it... ");
			}
			boolean watchFound = conceptNid == watchNid;

			final LogicalExpressionBuilder inferredBuilder = new LogicalExpressionBuilder();
			int[] statedSemanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid,
					statedPatternNid);
			if (statedSemanticNids.length == 0) { // Solor concept length == 0...
				if (conceptNid != TinkarTerm.SOLOR_CONCEPT.nid()) {
					AlertStreams.dispatchToRoot(
							new IllegalStateException("No stated form for concept: " + PrimitiveData.text(conceptNid)));
				}
			} else {
				if (statedSemanticNids.length > 1) {
					AlertStreams.dispatchToRoot(new IllegalStateException(
							"More than one stated form for concept: " + PrimitiveData.text(conceptNid)));
				}
				Latest<SemanticEntityVersion> latestStatedSemantic = this.viewCalculator.latest(statedSemanticNids[0]);
				latestStatedSemantic.ifPresent(statedSemantic -> {
					DiTreeEntity statedGraph = (DiTreeEntity) statedSemantic.fieldValues().get(0);
					LogicalExpression statedDefinition = statedGraph.adapt(logicalExpressionAdaptor);

					if (statedDefinition.contains(LogicalAxiomSemantic.SUFFICIENT_SET)) {
						sufficientSets.incrementAndGet();

						// Sufficient sets are copied exactly to the inferred form.
						statedDefinition.nodesOfType(LogicalAxiomSemantic.SUFFICIENT_SET.axiomClass)
								.forEach((sufficientSetNode) -> {
									LogicalAxiom cloneRootAxiom = inferredBuilder.addCloneOfNode(sufficientSetNode);
								});
					}

					// Need to construct the necessary set from classifier results.
					ImmutableIntSet parentNids = reasonerService.getParents(conceptNid);
					if (parentNids != null) {
						final MutableList<LogicalAxiom.Atom.ConceptAxiom> parentList = Lists.mutable
								.withInitialCapacity(parentNids.size());

						parentNids.forEach(parentNid -> parentList.add(inferredBuilder.ConceptAxiom(parentNid)));

						if (!parentList.isEmpty()) {
							inferredBuilder.NecessarySet(inferredBuilder.And(parentList.toImmutable()));

							final LogicalExpression newInferredExpression = inferredBuilder.build();

							ImmutableList<Object> fields = Lists.immutable.of(newInferredExpression.sourceGraph());

							int[] inferredSemanticNids = PrimitiveData.get()
									.semanticNidsForComponentOfPattern(conceptNid, inferredPatternNid);
							switch (inferredSemanticNids.length) {
							case 0 -> {
								// NOTE: we use the single semantic uuid generator in cases of distributed
								// development
								// creating inferred results for same referenced component.
								// TODO: how useful is the singleSemanticUuid? Is a better approach merging of
								// ids?
								UUID uuidForSemantic = UuidT5Generator.singleSemanticUuid(
										Entity.getFast(inferredPatternNid), Entity.getFast(conceptNid));
								// Create new semantic...
								RecordListBuilder<SemanticVersionRecord> versionRecords = RecordListBuilder.make();
								SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
										.leastSignificantBits(uuidForSemantic.getLeastSignificantBits())
										.mostSignificantBits(uuidForSemantic.getMostSignificantBits())
										.nid(PrimitiveData.nid(uuidForSemantic)).referencedComponentNid(conceptNid)
										.patternNid(inferredPatternNid).versions(versionRecords).build();
								versionRecords.add(new SemanticVersionRecord(semanticRecord, updateStampNid, fields));
								processSemantic(semanticRecord, updateTransaction);
								conceptNidsWithInferredChanges.add(conceptNid);
							}
							case 1 -> {
								// TODO: ensure that equals is implemented for DiTree<EntityVertex> and possibly
								// DiGraph and similar...
								// TODO: ensure that VertexIds are preserved across versions of the graph...

								// latest is the existing, potentially the "old" definition if
								// newInferredExpression contains changes.
								Latest<SemanticEntityVersion> latestInferredSemantic = this.viewCalculator
										.latest(inferredSemanticNids[0]);
								boolean changed = true;
								if (latestInferredSemantic.isPresent()) {
									ImmutableList<Object> latestInferredFields = latestInferredSemantic.get()
											.fieldValues();
									DiTreeEntity latestInferredTree = (DiTreeEntity) latestInferredFields.get(0);
									DiTreeEntity correlatedTree = latestInferredTree.makeCorrelatedTree(
											(DiTreeEntity) newInferredExpression.sourceGraph(), conceptNid,
											multipleEndpointTimer.startNew());

									changed = correlatedTree != latestInferredTree;
									// TODO performance comparisons of JGraph and internal implementation. Maybe in
									// integration tests with challenging test data.
									boolean testJGraph = false;
									if (testJGraph) {
										Graph<EntityVertex, DefaultEdge> correlatedTreeJGraph = JGraphUtil
												.toJGraph(correlatedTree);
										Graph<EntityVertex, DefaultEdge> latestInferredJGraph = JGraphUtil
												.toJGraph(latestInferredTree);
										boolean equals = latestInferredJGraph.equals(correlatedTreeJGraph);
										AHURootedTreeIsomorphismInspector inspector = new AHURootedTreeIsomorphismInspector(
												correlatedTreeJGraph, correlatedTree.root(), latestInferredJGraph,
												latestInferredTree.root());
										if (inspector.isomorphismExists()) {
											IsomorphicGraphMapping<EntityVertex, DefaultEdge> mapping = inspector
													.getMapping();
											mapping.toString();
										}
									}
								}
								if (changed || ReasonerResultsNode.reinferAllHierarchy) {
									conceptNidsWithInferredChanges.add(conceptNid);
									conceptsWithNavigationChanges.add(conceptNid);
									processSemantic(viewCalculator.updateFields(inferredSemanticNids[0], fields,
											updateStampNid), updateTransaction);
								}
							}
							default -> throw new IllegalStateException("More than one inferred semantic of pattern "
									+ PrimitiveData.text(inferredPatternNid) + "for component: "
									+ PrimitiveData.text(conceptNid));
							}
						}
						// TODO this approach for updating navigation changes requires a second scan of
						// the changed
						// concepts. Consider if this approach meets performance requirements, or if
						// another approach
						// should be developed and implemented.
						updateConceptsWithInferredNavigationChanges(conceptNid, inferredNavigationPatternNid,
								conceptsWithNavigationChanges, parentNids.toSet().toImmutable());
					}
				});
			}
			if (updateIntervalElapsed()) {
				updateMessage(multipleEndpointTimer.summary());
			}
			completedUnitOfWork();
		});

		updateInferredNavigationSemantics(conceptsWithNavigationChanges, inferredNavigationPatternNid, updateStampNid,
				updateTransaction);

		LOG.info("Timing info: " + multipleEndpointTimer.summary());
		updateMessage("Commiting " + updateTransaction.componentsInTransactionCount() + " components. ");

		LOG.debug("Comitting {} semantics", updateTransaction.componentsInTransactionCount());
		updateMessage("Commiting " + updateTransaction.componentsInTransactionCount() + " components. ");
		updateTransaction.commit();
		ViewCoordinateRecord commitCoordinate = this.viewCalculator.viewCoordinateRecord();
		commitCoordinate = commitCoordinate.withStampCoordinate(
				commitCoordinate.stampCoordinate().withStampPositionTime(updateTransaction.commitTime()));

		if (classificationDuplicateCount.get() > 0) {
			LOG.warn("Inferred duplicates found: " + classificationDuplicateCount);
		}
		LOG.info("Processed " + sufficientSets + " sufficient sets.");
		LOG.info("stampCoordinate: " + getViewCoordinateRecord().stampCoordinate());
		LOG.info("logicCoordinate: " + getViewCoordinateRecord().logicCoordinate());
		return commitCoordinate;
	}

	private void updateConceptsWithInferredNavigationChanges(int conceptNid, int inferredNavigationPatternNid,
			ConcurrentHashSet<Integer> conceptsWithNavigationChanges, ImmutableIntSet parentNids) {
		if (watchNid == conceptNid) {
			LOG.info("Found it");
		}
		int[] inferredNavigationNids = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid,
				inferredNavigationPatternNid);
		switch (inferredNavigationNids.length) {
		case 0 -> parentNids.forEach(parentNid -> conceptsWithNavigationChanges.add(parentNid));
		case 1 -> {
			Latest<SemanticEntityVersion> latestInferredNavigationSemantic = this.viewCalculator
					.latest(inferredNavigationNids[0]);
			if (latestInferredNavigationSemantic.isPresent()) {
				ImmutableList<Object> latestInferredNavigationFields = latestInferredNavigationSemantic.get()
						.fieldValues();
				// TODO I think the pattern is labeled the opposite of the use. We need to fix
				// that.
				IntIdSet parentIds = (IntIdSet) latestInferredNavigationFields.get(1);
				ImmutableIntSet immutableParentIdSet = IntSets.immutable.of(parentIds.toArray());
				ImmutableIntSet difference = immutableParentIdSet.symmetricDifference(parentNids);
				difference.forEach(changedParentNid -> conceptsWithNavigationChanges.add(changedParentNid));
			} else {
				parentNids.forEach(parentNid -> conceptsWithNavigationChanges.add(parentNid));
			}
		}
		default -> throw new IllegalStateException(
				"More than one semantic of pattern " + PrimitiveData.text(inferredNavigationPatternNid)
						+ "for component: " + PrimitiveData.text(conceptNid));
		}
	}

	private void updateInferredNavigationSemantics(ConcurrentHashSet<Integer> conceptsWithNavigationChanges,
			int inferredNavigationPatternNid, int updateStampNid, Transaction updateTransaction) {
		AtomicInteger axiomDataNotFoundCounter = new AtomicInteger();
		conceptsWithNavigationChanges.parallelStream().forEach(conceptNid -> {

			ImmutableIntSet parentNids = reasonerService.getParents(conceptNid);
			ImmutableIntSet childNids = reasonerService.getChildren(conceptNid);
			if (parentNids == null) {
				parentNids = IntSets.immutable.of();
				childNids = IntSets.immutable.of();
				axiomDataNotFoundCounter.incrementAndGet();
			}

			int[] inferredNavigationNids = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid,
					inferredNavigationPatternNid);
			switch (inferredNavigationNids.length) {
			case 0 -> {
				if (parentNids.notEmpty() || childNids.notEmpty()) {
					// NOTE: we use the single semantic uuid generator in cases of distributed
					// development creating inferred results for same referenced component.
					// TODO: how useful is the singleSemanticUuid? Is a better approach merging of
					// ids?
					UUID uuidForSemantic = UuidT5Generator.singleSemanticUuid(
							Entity.getFast(inferredNavigationPatternNid), Entity.getFast(conceptNid));
					// Create new semantic...
					RecordListBuilder<SemanticVersionRecord> versionRecords = RecordListBuilder.make();
					SemanticRecord navigationRecord = SemanticRecordBuilder.builder()
							.leastSignificantBits(uuidForSemantic.getLeastSignificantBits())
							.mostSignificantBits(uuidForSemantic.getMostSignificantBits())
							.nid(PrimitiveData.nid(uuidForSemantic)).referencedComponentNid(conceptNid)
							.patternNid(inferredNavigationPatternNid).versions(versionRecords).build();
					IntIdSet parentIds = IntIds.set.of(parentNids.toArray());
					IntIdSet childrenIds = IntIds.set.of(childNids.toArray());
					versionRecords.add(new SemanticVersionRecord(navigationRecord, updateStampNid,
							Lists.immutable.of(childrenIds, parentIds)));
					processSemantic(navigationRecord, updateTransaction);
				}
			}
			case 1 -> {
				Latest<SemanticEntityVersion> latestInferredNavigationSemantic = this.viewCalculator
						.latest(inferredNavigationNids[0]);
				boolean navigationChanged = true;
				if (latestInferredNavigationSemantic.isPresent()) {
					ImmutableList<Object> latestInferredNavigationFields = latestInferredNavigationSemantic.get()
							.fieldValues();
					IntIdSet parentIds = (IntIdSet) latestInferredNavigationFields.get(0);
					IntIdSet childIds = (IntIdSet) latestInferredNavigationFields.get(1);

					if (parentNids.equals(IntSets.immutable.of(parentIds.toArray()))
							&& childNids.equals(IntSets.immutable.of(childIds.toArray()))) {
						navigationChanged = false;
					}
				}
				if (navigationChanged) {
					IntIdSet newParentIds = IntIds.set.of(parentNids.toArray());
					IntIdSet newChildIds = IntIds.set.of(childNids.toArray());
					processSemantic(viewCalculator.updateFields(inferredNavigationNids[0],
							Lists.immutable.of(newChildIds, newParentIds), updateStampNid), updateTransaction);
				}
			}
			default -> throw new IllegalStateException(
					"More than one semantic of pattern " + PrimitiveData.text(inferredNavigationPatternNid)
							+ "for component: " + PrimitiveData.text(conceptNid));
			}
		});
		LOG.info("NavigationSemantics processed not in AxiomData: " + axiomDataNotFoundCounter.get());
	}

	private ViewCoordinateRecord getViewCoordinateRecord() {
		return this.viewCalculator.viewCoordinateRecord();
	}

	private void processSemantic(Entity<? extends EntityVersion> entity, Transaction updateTransaction) {
		updateTransaction.addComponent(entity);
		Entity.provider().putEntity(entity);
	}

	private void testForProperSetSize(int[] inferredSemanticNids, int conceptNid, int[] statedSemanticNids)
			throws IllegalStateException {
		if (inferredSemanticNids.length > 1) {
			classificationDuplicateCount.incrementAndGet();
			if (classificationDuplicateCount.get() < classificationCountDuplicatesToLog) {
				LOG.error("Cannot have more than one inferred definition per concept. Found: " + inferredSemanticNids
						+ "\n\nProcessing concept: " + PrimitiveData.text(conceptNid));
			}
		}

		if (statedSemanticNids.length != 1) {
			final StringBuilder builder = new StringBuilder();

			builder.append("Must have exactly one stated definition per concept. Found: ").append(statedSemanticNids)
					.append("\n");

			if (statedSemanticNids.length == 0) {
				builder.append("No stated definition for concept: ").append(PrimitiveData.text(conceptNid))
						.append("\n");
			} else {
				builder.append("Processing concept: ").append(PrimitiveData.text(conceptNid)).append("\n");
				Arrays.stream(statedSemanticNids).forEach(nid -> {
					builder.append("Found stated definition: ").append(Entity.getFast(nid)).append("\n");
				});
			}
			String alertString = builder.toString();
			AlertStreams.dispatchToRoot(new IllegalStateException(alertString));
			LOG.error(alertString);
		}
	}

}
