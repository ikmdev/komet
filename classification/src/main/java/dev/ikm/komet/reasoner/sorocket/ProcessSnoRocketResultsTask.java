package dev.ikm.komet.reasoner.sorocket;

import au.csiro.ontology.Node;
import au.csiro.ontology.Ontology;
import au.csiro.ontology.classification.IReasoner;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import dev.ikm.komet.reasoner.AxiomData;
import dev.ikm.komet.reasoner.ClassifierResults;
import dev.ikm.komet.reasoner.expression.*;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.common.util.time.MultipleEndpointTimer;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.JGraphUtil;
import dev.ikm.tinkar.entity.graph.isomorphic.IsomorphicResults;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.jgrapht.Graph;
import org.jgrapht.alg.isomorphism.AHURootedTreeIsomorphismInspector;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessSnoRocketResultsTask extends TrackingCallable<ClassifierResults> {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessSnoRocketResultsTask.class);
    static final int classificationCountDuplicatesToLog = 10;
    final IReasoner reasoner;
    final ViewCalculator viewCalculator;
    final PatternFacade inferredAxiomPattern;
    final AxiomData axiomData;

    ViewCoordinateRecord commitView;
    AtomicInteger classificationDuplicateCount = new AtomicInteger(-1);


    public ProcessSnoRocketResultsTask(IReasoner reasoner, ViewCalculator viewCalculator, PatternFacade inferredAxiomPattern,
                                       AxiomData axiomData) {
        super(false, true);
        this.reasoner = reasoner;
        this.viewCalculator = viewCalculator;
        this.inferredAxiomPattern = inferredAxiomPattern;
        this.axiomData = axiomData;
        updateTitle("Processing reasoner results. ");
    }

    @Override
    protected ClassifierResults compute() throws Exception {
        updateMessage("Getting classified results");
        LOG.info("Getting classified results...");
        final Ontology inferredAxioms = this.reasoner.getClassifiedOntology(this);
        Transaction updateTransaction = Transaction.make("Committing classification");
        ClassifierResults classifierResults = collectResults(inferredAxioms, this.axiomData.classificationConceptSet, updateTransaction);

        updateMessage("Processed results in " + durationString());
        return classifierResults;
    }

    /**
     * Collect results.
     *
     * @param classifiedResult the classified result
     * @param classificationConceptSet the concepts processed by the classifier
     * @return the classifier results
     */
    private ClassifierResults collectResults(Ontology classifiedResult, ImmutableIntList classificationConceptSet, Transaction updateTransaction) {
        updateMessage("Collecting reasoner results. ");
        LOG.info("Collecting reasoner results...", classificationConceptSet.size());
        addToTotalWork(classificationConceptSet.size() * 2); // get each node, then write back inferred.
        final HashSet<ImmutableIntList> equivalentSets = new HashSet<>();
        LOG.debug("collect results begins for {} concepts", classificationConceptSet.size());
        classificationConceptSet.primitiveParallelStream().forEach((conceptNid) -> {
            completedUnitOfWork();
            final Node node = classifiedResult.getNode(Integer.toString(conceptNid));

            if (node == null) {
                LOG.error("Null node for: {} {} {} will be skipped in classifier results collect", conceptNid,
                        PrimitiveData.publicId(conceptNid).idString(), PrimitiveData.text(conceptNid));
                // TODO possibly propagate error in gui...
            } else {
                final Set<String> equivalentConcepts = node.getEquivalentConcepts();

                if (equivalentConcepts.size() > 1) {
                    MutableIntList equivalentNids = IntLists.mutable.withInitialCapacity(equivalentConcepts.size());
                    for (String equivalentConcept : equivalentConcepts) {
                        int equivalentNid = Integer.parseInt(equivalentConcept);
                        equivalentNids.add(equivalentNid);
                    }
                    equivalentNids.sortThis();
                    equivalentSets.add(equivalentNids.toImmutable());
                }
            }
        });
//        if (!equivalentSets.isEmpty()) {
//            log.info("Equivalent set count: " + equivalentSets.size());
//            int setCount = 1;
//            for (IntArrayList equivalentSet: equivalentSets) {
//                StringBuilder sb = new StringBuilder("Set " + setCount++ + ":\n");
//                for (int nid: equivalentSet.elements()) {
//                    sb.append(Get.conceptDescriptionText(nid)).append("\n");
//                }
//                log.info(sb.toString());
//            }
//        }

        updateMessage("Writing back inferred. ");
        LOG.info("Writing back inferred...");

        ConcurrentHashSet<Integer> affectedConcepts = new ConcurrentHashSet<>();

        ViewCoordinateRecord commitView = writeBackInferred(classifiedResult, axiomData.classificationConceptSet,
                affectedConcepts, updateTransaction);
        int[] affectedConceptNidArray = affectedConcepts.stream().mapToInt(boxedInt -> (int) boxedInt).toArray();
        Arrays.sort(affectedConceptNidArray);
        ImmutableIntList affectedConceptsAsList = IntLists.immutable.of(affectedConceptNidArray);

        return new ClassifierResults(classificationConceptSet,
                affectedConceptsAsList,
                equivalentSets, commitView);
    }

    /**
     * Write back inferred.
     *
     * @param inferredAxioms   the inferred axioms
     * @param classificationConceptSet the set of concepts fed to the reasoner
     * @param conceptNidsWithInferredChanges the concepts with changes identified by comparing inferred form with previous form
     * @return the commit view
     */
    private ViewCoordinateRecord writeBackInferred(Ontology inferredAxioms,
                                                   ImmutableIntList classificationConceptSet,
                                                   ConcurrentHashSet<Integer> conceptNidsWithInferredChanges,
                                                   Transaction updateTransaction) {
        //TODO change type of affectedConcepts to a parallel friendly primitive class.
        final AtomicInteger sufficientSets = new AtomicInteger();
        StampEntity updateStamp = updateTransaction.getStamp(State.ACTIVE,
                getViewCoordinateRecord().getAuthorNidForChanges(),
                getViewCoordinateRecord().getDefaultModuleNid(),
                getViewCoordinateRecord().getDefaultPathNid());
        int updateStampNid = updateStamp.nid();

        final ConcurrentHashSet<Integer> inferredChanges = new ConcurrentHashSet<>();

        LOG.debug("Write back inferred begins with {} axioms", inferredAxioms.getInferredAxioms().size());
        // TODO Dan notes, for reasons not yet understood, this parallelStream call isn't working.
        // JVisualVM tells me that all of this work is occurring on a single thread.  Need to figure out why...
        final int statedPatternNid = getViewCoordinateRecord().logicCoordinate().statedAxiomsPatternNid();
        final int inferredPatternNid = getViewCoordinateRecord().logicCoordinate().inferredAxiomsPatternNid();
        OptionalInt optionalAxiomsIndex = this.viewCalculator.getIndexForMeaning(inferredPatternNid, TinkarTerm.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS.nid());
        if (optionalAxiomsIndex.isEmpty() || optionalAxiomsIndex.getAsInt() != 0) {
            throw new IllegalStateException("Index for " +
                    this.viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(TinkarTerm.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS) +
                    " is " + optionalAxiomsIndex);
        }
        int axiomsIndex = optionalAxiomsIndex.getAsInt();

        LogicalExpressionAdaptorFactory logicalExpressionAdaptor = new LogicalExpressionAdaptorFactory();

        MultipleEndpointTimer multipleEndpointTimer = new MultipleEndpointTimer(IsomorphicResults.EndPoints.class);
        classificationConceptSet.primitiveParallelStream().forEach(conceptNid -> {
            final LogicalExpressionBuilder inferredBuilder = new LogicalExpressionBuilder();
            int[] statedSemanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid, statedPatternNid);
            if (statedSemanticNids.length == 0) { // Solor concept length == 0...
                if (conceptNid != TinkarTerm.SOLOR_CONCEPT.nid()) {
                    AlertStreams.dispatchToRoot(new IllegalStateException("No stated form for concept: " + PrimitiveData.text(conceptNid)));
                }
            } else {
                if (statedSemanticNids.length > 1) {
                    AlertStreams.dispatchToRoot(new IllegalStateException("More than one stated form for concept: " + PrimitiveData.text(conceptNid)));
                }
                Latest<SemanticEntityVersion> latestStatedSemantic = this.viewCalculator.latest(statedSemanticNids[0]);
                latestStatedSemantic.ifPresent(statedSemantic -> {
                    DiTreeEntity statedGraph = (DiTreeEntity) statedSemantic.fieldValues().get(0);
                    LogicalExpression statedDefinition = statedGraph.adapt(logicalExpressionAdaptor);


                    if (statedDefinition.contains(LogicalAxiomSemantic.SUFFICIENT_SET)) {
                        sufficientSets.incrementAndGet();

                        // Sufficient sets are copied exactly to the inferred form.
                        statedDefinition.nodesOfType(LogicalAxiomSemantic.SUFFICIENT_SET).forEach((sufficientSetNode) -> {
                            LogicalAxiom cloneRootAxiom = inferredBuilder.addCloneOfNode(sufficientSetNode);
                        });
                    }

                    // Need to construct the necessary set from classifier results.
                    final Node inferredNode
                            = inferredAxioms.getNode(Integer.toString(conceptNid));
                    final MutableList<LogicalAxiom.Atom.ConceptAxiom> parentList = Lists.mutable.withInitialCapacity(8);
                    if (inferredNode != null) {
                        inferredNode.getParents().forEach((parent) -> {
                            parent.getEquivalentConcepts().forEach((parentString) -> {
                                try {
                                    int parentNid = Integer.parseInt(parentString);
                                    parentList.add(inferredBuilder.ConceptAxiom(parentNid));
                                } catch (final NumberFormatException numberFormatException) {
                                    if (parentString.equals("_BOTTOM_") || parentString.equals("_TOP_")) {
                                        // do nothing.
                                    } else {
                                        AlertStreams.dispatchToRoot(numberFormatException);
                                    }
                                }
                            });
                        });
                    }

                    if (!parentList.isEmpty()) {
                        inferredBuilder.NecessarySet(inferredBuilder.And(parentList.toImmutable()));

                        final LogicalExpression newInferredExpression = inferredBuilder.build();

                        ImmutableList<Object> fields = Lists.immutable.of(newInferredExpression.sourceGraph());


                        int[] inferredSemanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid, inferredPatternNid);
                        switch (inferredSemanticNids.length) {
                            case 0 -> {
                                SemanticRecordBuilder semanticRecordBuilder = SemanticRecordBuilder.builder();
                                // NOTE: we use the single semantic uuid generator in cases of distributed development
                                // creating inferred results for same referenced component.
                                // TODO: how useful is the singleSemanticUuid? Is a better approach merging of ids?
                                UUID uuidForSemantic = UuidT5Generator.singleSemanticUuid(
                                        Entity.getFast(inferredPatternNid),
                                        Entity.getFast(conceptNid));
                                // Create new semantic...
                                RecordListBuilder<SemanticVersionRecord> versionRecords = RecordListBuilder.make();
                                SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                                        .leastSignificantBits(uuidForSemantic.getLeastSignificantBits())
                                        .mostSignificantBits(uuidForSemantic.getMostSignificantBits())
                                        .nid(PrimitiveData.nid(uuidForSemantic))
                                        .referencedComponentNid(conceptNid)
                                        .patternNid(inferredPatternNid)
                                        .versions(versionRecords)
                                        .build();
                                versionRecords.add(new SemanticVersionRecord(semanticRecord, updateStampNid, fields));
                                processSemantic(semanticRecord, updateTransaction);
                                conceptNidsWithInferredChanges.add(conceptNid);
                            }
                            case 1 -> {
                                // TODO: ensure that equals is implemented for DiTree<EntityVertex> and possibly DiGraph and similar...
                                // TODO: ensure that VertexIds are preserved across versions of the graph...

                                // latest is the existing, potentially the "old" definition if newInferredExpression contains changes.
                                Latest<SemanticEntityVersion> latestInferredSemantic = this.viewCalculator.latest(inferredSemanticNids[0]);
                                boolean changed = true;
                                if (latestInferredSemantic.isPresent()) {
                                    ImmutableList<Object> latestInferredFields = latestInferredSemantic.get().fieldValues();
                                    DiTreeEntity latestInferredTree = (DiTreeEntity) latestInferredFields.get(0);
                                    DiTreeEntity correlatedTree = latestInferredTree.makeCorrelatedTree((DiTreeEntity) newInferredExpression.sourceGraph(),
                                            conceptNid, multipleEndpointTimer.startNew());

                                    changed = correlatedTree != latestInferredTree;
                                    //TODO performance comparisons of JGraph and internal implementation. Maybe in integration tests with challenging test data.
                                    boolean testJGraph = false;
                                    if (testJGraph) {
                                        Graph<EntityVertex, DefaultEdge> correlatedTreeJGraph = JGraphUtil.toJGraph(correlatedTree);
                                        Graph<EntityVertex, DefaultEdge> latestInferredJGraph = JGraphUtil.toJGraph(latestInferredTree);
                                        boolean equals = latestInferredJGraph.equals(correlatedTreeJGraph);
                                        AHURootedTreeIsomorphismInspector inspector =
                                                new AHURootedTreeIsomorphismInspector(
                                                        correlatedTreeJGraph, correlatedTree.root(),
                                                        latestInferredJGraph, latestInferredTree.root());
                                        if (inspector.isomorphismExists()) {
                                            IsomorphicGraphMapping<EntityVertex, DefaultEdge> mapping = inspector.getMapping();
                                            mapping.toString();
                                        }
                                    }
                                }
                                if (changed) {
                                    conceptNidsWithInferredChanges.add(conceptNid);
                                    processSemantic(viewCalculator.updateFields(inferredSemanticNids[0], fields, updateStampNid), updateTransaction);
                                }
                            }
                            default -> {
                                throw new IllegalStateException("More than one inferred semantic of pattern " + PrimitiveData.text(inferredPatternNid) +
                                        "for component: " + PrimitiveData.text(conceptNid));
                            }
                        }
                    }
                });
            }
            if (updateIntervalElapsed()) {
                updateMessage(multipleEndpointTimer.summary());
            }
            completedUnitOfWork();
        });
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

    private ViewCoordinateRecord getViewCoordinateRecord() {
        return this.viewCalculator.viewCoordinateRecord();
    }

    private void processSemantic(Entity<? extends EntityVersion> entity, Transaction updateTransaction) {
        updateTransaction.addComponent(entity);
        Entity.provider().putEntity(entity);
    }

    private void testForProperSetSize(int[] inferredSemanticNids,
                                      int conceptNid,
                                      int[] statedSemanticNids)
            throws IllegalStateException {
        if (inferredSemanticNids.length > 1) {
            classificationDuplicateCount.incrementAndGet();
            if (classificationDuplicateCount.get() < classificationCountDuplicatesToLog) {
                LOG.error("Cannot have more than one inferred definition per concept. Found: "
                        + inferredSemanticNids + "\n\nProcessing concept: " + PrimitiveData.text(conceptNid));
            }
        }

        if (statedSemanticNids.length != 1) {
            final StringBuilder builder = new StringBuilder();

            builder.append("Must have exactly one stated definition per concept. Found: ")
                    .append(statedSemanticNids)
                    .append("\n");

            if (statedSemanticNids.length == 0) {
                builder.append("No stated definition for concept: ")
                        .append(PrimitiveData.text(conceptNid))
                        .append("\n");
            } else {
                builder.append("Processing concept: ")
                        .append(PrimitiveData.text(conceptNid))
                        .append("\n");
                Arrays.stream(statedSemanticNids).forEach(nid -> {
                    builder.append("Found stated definition: ")
                            .append(Entity.getFast(nid))
                            .append("\n");
                });
            }
            String alertString = builder.toString();
            AlertStreams.dispatchToRoot(new IllegalStateException(alertString));
            LOG.error(alertString);
        }
    }

}
