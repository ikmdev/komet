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
package dev.ikm.tinkar.entity.graph.isomorphic;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.ArrayUtil;
import dev.ikm.tinkar.common.util.time.MultipleEndpointTimer;
import dev.ikm.tinkar.entity.graph.DiGraphAbstract;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.VisitProcessor;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static dev.ikm.tinkar.entity.graph.isomorphic.IsomorphicResults.EndPoints.FULL_COMPARISON;

//~--- classes ----------------------------------------------------------------

/**
 * The Class IsomorphicResultsLeafHash.
 * <p>
 * A (Sub)Graph Isomorphism Algorithm for Matching Large Graphs
 * IEEE TRANSACTIONS ON PATTERN ANALYSIS AND MACHINE INTELLIGENCE,
 * VOL. 26, NO. 10, OCTOBER 2004 1367
 * Luigi P. Cordella, Pasquale Foggia, Carlo Sansone, and Mario Vento
 * <p>
 * Graphs, Vertexes, Edges, Nodes, Branches, ...
 * <p>
 * The above paper uses: G1 = (N1, B1) or translated Graph 1 is a set of Nodes of 1 and Branches of 1...
 * To minimise confusion between other uses of Node (javafx.scene.Node), we preferentially use
 * Vertexes and Edges to describe the Graphs.
 * <p>
 * https://digitalcommons.calpoly.edu/cgi/viewcontent.cgi?article=3437&amp;context=theses
 * <p>
 * VF2++ — An Improved Subgraph Isomorphism Algorithm
 * Alpar Juttner and Peter Madarasi
 * https://web.cs.elte.hu/egres/tr/egres-18-03.pdf
 * <p>
 * MET: a Java package for fast molecule equivalence testing
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745470/
 * <p>
 * Given a query graph and a target graph, it calculates all possible subgraphs of the target graph isomorphic to
 * the query graph. Both the query graph and target graph are stored in the same Neo4j database.
 * https://github.com/jiatistuta/subgraph-isomorphism-neo4j
 * <p>
 * TODO: reconsider use of BitSet
 * Performance of nextSetBit(), get(), and iteration are key performance areas.
 * Also consider other types of sets... Eclipse intSet for example.
 * https://stackoverflow.com/questions/37080045/java-jdk-bitset-vs-lucene-openbitset
 * <p>
 * org.apache.lucene.util.OpenBitSet
 * <p>
 * The goals of OpenBitSet are the fastest implementation possible, and maximum code reuse.
 * Extra safety and encapsulation may always be built on top, but if that's built in, the
 * cost can never be removed (and hence people re-implement their own version in order to
 * get better performance). If you want a "safe", totally encapsulated (and slower and limited) B
 * itSet class, use java.util.BitSet.
 * <p>
 * Also consider Roaring BitMaps...
 * https://roaringbitmap.org
 * <p>
 * Maybe make a custom structure that will switch between direct representation or bitmap depending on
 * size and cardinality.
 */
public class IsomorphicResultsLeafHash<VVD extends VertexVisitDataLeafHash> extends IsomorphicResultsAbstract<VVD> {

    private static final Logger LOG = LoggerFactory.getLogger(IsomorphicResultsLeafHash.class);


    /**
     * Will not allow a hash value of -1. And vertexes where not unique if another and was below them, and there was an
     * And vertex above...
     * In example below nodes 6 and 5 had same hashcode. Revised to include meaning nid for vertex
     * in addition to the nidList...
     * <p>
     * <pre>
     * DiTreeEntity{
     *    [0]➞[9,13] Definition root
     *      [9]➞[8] Sufficient set
     *        [8]➞[2,6,7] And
     *          [2]➞[1] Role type: Has dose form
     *             •Role operator: Existential restriction
     *            [1] Concept reference: Oral dosage form
     *          [6]➞[5] Role type: Role group
     *             •Role operator: Existential restriction
     *            [5]➞[4] And
     *              [4]➞[3] Role type: Has active ingredient
     *                 •Role operator: Existential restriction
     *                [3] Concept reference: Cycloserine
     *          [7] Concept reference: Medicinal product
     *      [13]➞[12] Necessary set
     *        [12]➞[10,11] And
     *          [10] Concept reference: Product containing cycloserine
     *          [11] Concept reference: Product manufactured as oral dose form
     * }
     * </pre>
     * @param sortedNids a sorted array of nids
     * @return a hashcode, other than -1
     */
    public static final int makeNidListHash(int vertexMeaning, int[] sortedNids) {
        int arrayHash = Arrays.hashCode(sortedNids);
        arrayHash = 31 * arrayHash + vertexMeaning;
        while (arrayHash == -1) {
            arrayHash = 31 * arrayHash + 7;
        }
        return arrayHash;
    }
    /**
     * The isomorphic solution.
     */
    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new isomorphic results bottom up.
     *
     * @param referenceTree  the reference tree
     * @param comparisonTree the comparison tree
     */
    public IsomorphicResultsLeafHash(DiTreeEntity referenceTree, DiTreeEntity comparisonTree, int referencedConceptNid, MultipleEndpointTimer.Stopwatch stopwatch) {
        super(referenceTree, comparisonTree, referencedConceptNid, stopwatch);
    }

    public IsomorphicResultsLeafHash(DiTreeEntity referenceTree, DiTreeEntity comparisonTree, int referencedConceptNid) {
        super(referenceTree, comparisonTree, referencedConceptNid, null);
    }

    @Override
    protected VVD makeVertexVisitData(int graphSize, VisitProcessor<VVD> vertexStartProcessor, VisitProcessor<VVD> vertexEndProcessor) {
        return (VVD) new VertexVisitDataLeafHash(graphSize, vertexStartProcessor, vertexEndProcessor);
    }

    public void vertexStartProcessor(EntityVertex vertex, DiGraphAbstract<EntityVertex> graph, VVD visitData) {
        MutableIntSet nidsReferencedByThisVertexOrAbove = visitData.nidsReferencedAtVertexOrAboveIndexMap.getIfAbsentPut(
                vertex.vertexIndex(), () -> IntSets.mutable.empty());
        nidsReferencedByThisVertexOrAbove.add(vertex.getMeaningNid());
        // Add nids from this vertex.
        for (Object value : vertex.properties().values()) {
            if (value instanceof ConceptFacade conceptFacade) {
                nidsReferencedByThisVertexOrAbove.add(conceptFacade.nid());
            }
        }

        OptionalInt predecessorIndex = visitData.predecessorIndex(vertex.vertexIndex());
        if (predecessorIndex.isPresent()) {
            // Add nids from predecessor, which transitively already has nids from its predecessor...
            visitData.nidsReferencedAtVertexOrAboveIndexMap.getIfAbsentPut(predecessorIndex.getAsInt(),
                    () -> IntSets.mutable.empty()).forEach(conceptNid -> nidsReferencedByThisVertexOrAbove.add(conceptNid));
        }
        int vertexNidHash = makeNidListHash(vertex.getMeaningNid(), nidsReferencedByThisVertexOrAbove.toSortedArray());
        visitData.vertexHashArray[vertex.vertexIndex()] = vertexNidHash;
        visitData.vertexHashToVertexIndexMap.getIfAbsentPut(vertexNidHash, () -> new BitSet(graph.vertexCount())).set(vertex.vertexIndex());

    }

    public void vertexEndProcessor(EntityVertex vertex, DiGraphAbstract<EntityVertex> graph, VVD visitData) {
        BitSet leafIndexesAtVertexOrBelow = ArrayUtil.getIfAbsentPut(visitData.leafIndexesAtVertexOrBelowArray, vertex.vertexIndex(), () -> new BitSet(graph.vertexCount()));
        if (visitData.leafVertexIndexes().get(vertex.vertexIndex())) {
            leafIndexesAtVertexOrBelow.set(vertex.vertexIndex());
        }
        int[] leafHashCodes = new int[leafIndexesAtVertexOrBelow.size()];
        for (int i = 0; i < leafHashCodes.length; i++) {
            int setIndex = leafIndexesAtVertexOrBelow.nextSetBit(i);
            if (setIndex > -1) {
                leafHashCodes[i] = visitData.vertexHashArray[setIndex];
            }
        }
        visitData.leafHashesAtVertexOrBelowArray[vertex.vertexIndex()] = IntSets.immutable.of(leafHashCodes);

        visitData.predecessorIndex(vertex.vertexIndex()).ifPresent(predecessorIndex -> {
            ArrayUtil.getIfAbsentPut(visitData.leafIndexesAtVertexOrBelowArray, predecessorIndex,
                    () -> new BitSet(graph.vertexCount())).or(leafIndexesAtVertexOrBelow);
        });

    }

    public static final int score(ImmutableIntList correlation) {
        return correlation.primitiveStream().reduce(0, (partialResult, nextItem) -> nextItem >= 0 ? ++partialResult : partialResult);
    }

    private record Pair(int referenceIndex, int comparisonIndex, AtomicInteger score) {
        Pair(int referenceIndex, int comparisonIndex) {
            this(referenceIndex, comparisonIndex, new AtomicInteger());
        }
    }

    private static final AtomicInteger possibleSolutionSize = new AtomicInteger(1024);

    /**
     * Inspired by VF2++ with optimizations for Komet's use case and data structures.
     * There is potential to parallelize this method..
     * https://en.wikipedia.org/wiki/Parallel_breadth-first_search
     */
    public final IndexCorrelationSolution bfsMatchWithVertexHash() {
        final Queue<Integer> bfsQueue = new LinkedList<>();
        int referenceTreeVertexCount = this.referenceTree.vertexCount();
        BitSet[] possibleComparisonIndexes = new BitSet[referenceTreeVertexCount];
        for (int referenceIndex = 0; referenceIndex < referenceTreeVertexCount; referenceIndex++) {
            int referenceVertexHash = this.referenceVisitData().vertexHashArray[referenceIndex];
            possibleComparisonIndexes[referenceIndex] = this.comparisonVisitData().vertexHashToVertexIndexMap.get(referenceVertexHash);
        }

        int[] seedSolutionMap = ArrayUtil.createAndFillWithMinusOne(referenceTreeVertexCount);
        int referenceRootVertexIndex = this.referenceTree.root().vertexIndex();
        seedSolutionMap[referenceRootVertexIndex] = this.comparisonTree.root().vertexIndex();
        IndexCorrelationSolution seedSolution = new IndexCorrelationSolution(seedSolutionMap);

        final BitSet processedIndexes = new BitSet(seedSolutionMap.length);
        processedIndexes.set(this.referenceTree.root().vertexIndex());

        ImmutableList<IndexCorrelationSolution> currentSolutions = Lists.immutable.of(seedSolution);
        MutableList<IndexCorrelationSolution> nextLevelSolutions = Lists.mutable.empty();

        int maxScore = seedSolution.score();
        this.referenceTree.successors(referenceRootVertexIndex).forEach(childVertexIndex -> bfsQueue.add(childVertexIndex));
        final Queue<Integer> nextLevelQueue = new LinkedList<>();
        while (!bfsQueue.isEmpty()) {
            final int currentIndex = bfsQueue.remove();
            final ImmutableIntList childIndexes = this.referenceTree.successors(currentIndex);
            MutableList<IndexCorrelationSolution> solutionsToSeed = Lists.mutable.withAll(currentSolutions);
            solutionsToSeed.addAll(nextLevelSolutions);
            currentSolutions = cut(solutionsToSeed);

            for (IndexCorrelationSolution possibleSolution : currentSolutions) {
                for (IndexCorrelationSolution extendedSolution : extend(currentIndex, possibleSolution, possibleComparisonIndexes[currentIndex])) {
                    if (extendedSolution.score() > maxScore) {
                        maxScore = extendedSolution.score();
                    }
                    // TODO: examine here. Prior to adding this conditional add, got a next level solutions of size 544,368...
                    // The first several hundred options had score of 40 while max score was 86. Max possible is 136.
                    // Product containing cobicistat 150 milligram and elvitegravir 150 milligram and emtricitabine 200 milligram and tenofovir alafenamide 10 milligram in oral tablet
                    // 19b2f9ac-228f-32d2-aef4-8c4aaa6ef199
                    if (extendedSolution.score() == maxScore) {
                        nextLevelSolutions.add(extendedSolution);
                    }
                    if (nextLevelSolutions.size() > possibleSolutionSize.get()) {
                        LOG.warn("nextLevelSolutions.size() = " + nextLevelSolutions.size() + " for " +
                                PrimitiveData.textWithNid(referencedConceptNid));
                        possibleSolutionSize.set(possibleSolutionSize.get() * 2);
                        if (possibleSolutionSize.get() > 4194305) {
                            LOG.error("nextLevelSolutions.size() to large... ");
                        }
                    }
                }
            }
            processedIndexes.set(currentIndex);
            childIndexes.forEach(childIndex -> {
                if (!processedIndexes.get(childIndex)) {
                    nextLevelQueue.add(childIndex);
                }
            });
            if (bfsQueue.isEmpty()) {
                if (!nextLevelSolutions.isEmpty()) {
                    bfsQueue.addAll(nextLevelQueue);
                    nextLevelQueue.clear();
                    // Only retain the highest scores for the level.
                    final int maxScoreForLevel = maxScore;
                    currentSolutions = Lists.immutable.fromStream(nextLevelSolutions.stream()
                            .filter(solution -> solution.score() == maxScoreForLevel));
                    nextLevelSolutions.clear();
                }
            }
        }
        if (currentSolutions.size() > 0 && currentSolutions.get(0).score() == referenceTreeVertexCount) {
            // TODO indicates duplicate content, turn into a metric. Look for other measures of redundancy.
            // TODO this shows error with constructing inferred form... Fix it... CASE: 2 solution, both equal to the size of the reference
        }
        return currentSolutions.stream().max((s1, s2) -> Integer.compare(s1.score(), s2.score())).get();
    }

    private ImmutableList<IndexCorrelationSolution> cut(MutableList<IndexCorrelationSolution> solutionsToSeed) {
        if (solutionsToSeed.size() > 1) {
            MutableList<IndexCorrelationSolution> cutList = Lists.mutable.ofInitialCapacity(solutionsToSeed.size());
            solutionsToSeed.sort((o1, o2) -> Integer.compare(o2.score(), o1.score()));
            for (IndexCorrelationSolution solution : solutionsToSeed) {
                if (cutList.isEmpty()) {
                    cutList.add(solution);
                } else if (cutList.get(0).score() == solution.score()) {
                    cutList.add(solution);
                }
            }
            return cutList.toImmutable();
        }
        return solutionsToSeed.toImmutable();
    }

    private void order(MutableList<Pair> extensions) {
        // TODO order so that node with more descendents (look at discovery and finish sequence) are ordered first...
        // This means the node with the greatest potential to increase score will be considered first.
        // Order "rarest node" and "greatest number of descendents"
    }

    private boolean consistent(IndexCorrelationSolution solutionToExtend, Pair extension) {
        BitSet referenceLeafIndexesBelow = this.referenceVisitData().leafIndexesAtVertexOrBelowArray[extension.referenceIndex];
        BitSet comparisonLeafIndexesBelow = this.comparisonVisitData().leafIndexesAtVertexOrBelowArray[extension.comparisonIndex];

        // For the leaf nodes below, are any equivalent? If none, then not consistent.
        IntSet referenceLeafHashes = IntSets.immutable.of(
                referenceLeafIndexesBelow.stream().map(leafIndex -> this.referenceVisitData().vertexHashArray[leafIndex]).toArray()
        );
        IntSet comparisonLeafHashes = IntSets.immutable.of(
                comparisonLeafIndexesBelow.stream().map(leafIndex -> this.comparisonVisitData().vertexHashArray[leafIndex]).toArray()
        );

        // test to make sure at least one is equivalent...
        if (!comparisonLeafHashes.containsAny(referenceLeafHashes)) {
            return false;
        }

        EntityVertex referenceExtensionVertex = this.referenceTree.vertex(extension.referenceIndex);
        EntityVertex comparisonExtensionVertex = this.comparisonTree.vertex(extension.comparisonIndex);
        if (referenceExtensionVertex.equivalent(comparisonExtensionVertex)) {
            Optional<EntityVertex> referenceExtensionParent = this.referenceTree.predecessor(referenceExtensionVertex);
            Optional<EntityVertex> comparisonExtensionParent = this.comparisonTree.predecessor(comparisonExtensionVertex);
            if (referenceExtensionParent.isPresent() && comparisonExtensionParent.isPresent()) {
                return solutionToExtend.solution().get(referenceExtensionParent.get().vertexIndex())
                        == comparisonExtensionParent.get().vertexIndex();
            } else {
                return referenceExtensionParent.isEmpty() && comparisonExtensionParent.isEmpty();
            }

        }
        return false;
    }

    /**
     * Cut based strictly on the evaluation of the Pair itself.
     *
     * @param solutionToExtend
     * @param extension
     * @return
     */
    private boolean cut(IndexCorrelationSolution solutionToExtend, Pair extension) {
        // cut if the reference tree already has a vertex in the solution.
        return solutionToExtend.solution().get(extension.referenceIndex) != -1;
    }

    /**
     * Cut based on comparing the Pairs to each other.
     *
     * @param solutionToExtend
     * @param extensions
     */
    private void cut(IndexCorrelationSolution solutionToExtend, MutableList<Pair> extensions) {
        if (extensions.size() == 1) {
            return;
        }
        // TODO
        // IF vertex Ids equal in one, but only equivalent in another, prefer the equal vertex ids
        // and remove the equivalent pair. Cut because they are competing for the same spot, only the
        // best match should win.
        int maxScore = -1;
        MutableList<Pair> maxScoreExtensions = Lists.mutable.withInitialCapacity(extensions.size());
        for (Pair extension : extensions) {
            if (referenceTree.vertex(extension.referenceIndex).vertexId().equals(comparisonTree.vertex(extension.comparisonIndex).vertexId())) {
                extensions.clear();
                extensions.add(extension);
                return;
            }
            ImmutableIntSet referenceLeafHashes = referenceVisitData().leafHashesAtVertexOrBelowArray[extension.referenceIndex];
            ImmutableIntSet comparisonLeafHashes = comparisonVisitData().leafHashesAtVertexOrBelowArray[extension.comparisonIndex];

            IntSet intersection = referenceLeafHashes.intersect(comparisonLeafHashes);
            extension.score.set(intersection.size());
            if (maxScore < extension.score.get()) {
                maxScore = extension.score.get();
                maxScoreExtensions.clear();
                maxScoreExtensions.add(extension);
            } else if (maxScore == extension.score.get()) {
                maxScoreExtensions.add(extension);
            }
        }

        // Only keep max scoring extension Pairs...
        extensions.clear();
        extensions.addAll(maxScoreExtensions);
    }

    private ImmutableList<IndexCorrelationSolution> extend(int referenceIndex, IndexCorrelationSolution incomingSolution, BitSet possibleComparisonIndexes) {
        MutableList<Pair> extensions = Lists.mutable.empty();
        if (possibleComparisonIndexes != null) {
            possibleComparisonIndexes.stream().forEach(comparisonIndex -> {
                Pair possibleExtension = new Pair(referenceIndex, comparisonIndex);
                if (consistent(incomingSolution, possibleExtension) && !cut(incomingSolution, possibleExtension)) {
                    extensions.add(possibleExtension);
                }
            });
        }
        cut(incomingSolution, extensions);
        order(extensions);

        MutableList<IndexCorrelationSolution> extendedSolutions = Lists.mutable.empty();
        for (Pair pair : extensions) {
            int[] extendedCorrelation = incomingSolution.solution().toArray();
            extendedCorrelation[pair.referenceIndex] = pair.comparisonIndex;
            extendedSolutions.add(new IndexCorrelationSolution(extendedCorrelation));
        }
        return extendedSolutions.toImmutable();
    }

    @Override
    public IsomorphicResults call() throws Exception {
        this.referenceTree.depthFirstProcess(this.referenceTree.root().vertexIndex(), this.referenceVisitData());
        this.comparisonTree.depthFirstProcess(this.comparisonTree.root().vertexIndex(), this.comparisonVisitData());
        this.referenceToComparisonIndexCorrelation = bfsMatchWithVertexHash();


        for (int referenceVertexIndex = 0; referenceVertexIndex < this.referenceToComparisonIndexCorrelation.solution().size(); referenceVertexIndex++) {
            if (this.referenceToComparisonIndexCorrelation.solution().get(referenceVertexIndex) > -1) {
                this.comparisonToReferenceIndexMap[this.referenceToComparisonIndexCorrelation.solution().get(referenceVertexIndex)]
                        = referenceVertexIndex;
            }
        }

        // TODO verify that this is a proper test for isomorphic
        final boolean isomorphic = this.referenceTree.vertexCount() == this.comparisonTree.vertexCount() &&
                this.referenceToComparisonIndexCorrelation.score() == this.referenceTree.vertexCount();
        // If test if isomorphic, and if so, isomorphic tree = reference tree...
        // TODO this.isomorphicTree = reference tree if equivalent.
        if (isomorphic) {
            this.isomorphicTree = this.referenceTree;
            for (int i = 0; i < referenceToMergedIndexMap.length; i++) {
                referenceToIsomorphicIndexMap[i] = i;
            }

        } else {
            this.isomorphicTree = DiTreeEntity.addVertexesFromSolution(this.referenceTree,
                    this.referenceToComparisonIndexCorrelation.solution(), this.referenceToIsomorphicIndexMap).build();
        }

        // The depth of 3 is below:
        //      definition root -> necessary set -> and
        //      definition root -> sufficient set -> and
        //      definition root -> implication set -> and
        //      definition root -> property set -> and

        this.referenceVisitData().vertexIndexesForDepth(3).stream().forEach(vertexIndex ->
                IsomorphicResultsLeafHash.this.referenceRelationshipNodesMap.put(
                        new SetElementKey(vertexIndex, IsomorphicResultsLeafHash.this.referenceTree), vertexIndex));

        this.comparisonVisitData().vertexIndexesForDepth(3).stream().forEach(vertexIndex -> {
            this.comparisonRelationshipNodesMap.put(
                    new SetElementKey(vertexIndex, this.comparisonTree), vertexIndex);
        });

        computeReferenceInclusionsAndDeletions();
        computeComparisonInclusionsAndDeletions();

        final int[] identityMap = new int[this.referenceTree.vertexCount()];

        for (int i = 0; i < identityMap.length; i++) {
            identityMap[i] = i;
        }

        if (isomorphic) {
            this.mergedTree = referenceTree;
            for (int i = 0; i < referenceToMergedIndexMap.length; i++) {
                referenceToMergedIndexMap[i] = i;
            }

        } else {
            DiTreeEntity.Builder treeBuilder = DiTreeEntity.addVertexesFromSolution(this.referenceTree,
                    IntLists.immutable.of(identityMap),
                    this.referenceToMergedIndexMap);

            // make a node mapping from comparison expression to the merged tree
            final int[] comparisonToMergedMap = ArrayUtil.createAndFillWithMinusOne(comparisonTree.vertexCount());
            for (int referenceVertexIndex = 0; referenceVertexIndex < this.referenceToComparisonIndexCorrelation.solution().size(); referenceVertexIndex++) {
                if (this.referenceToComparisonIndexCorrelation.solution().get(referenceVertexIndex) >= 0) {
                    comparisonToMergedMap[this.referenceToComparisonIndexCorrelation.solution().get(referenceVertexIndex)] = referenceVertexIndex;
                }
            }

            getAddedSetElements().forEach((elementToAdd) -> {
                // added elements come from the comparison tree.
                OptionalInt predecessorNid = this.comparisonVisitData().predecessorIndex(elementToAdd.vertexIndex());
                if (predecessorNid.isPresent()) {
                    int comparisonTreeToReferenceTreeVertexIndex = this.comparisonToReferenceIndexMap[predecessorNid.getAsInt()];
                    if (comparisonTreeToReferenceTreeVertexIndex >= 0) {
                        // put it below the shared predecessor...
                        final int rootToAddParentIndexInMergedTree
                                = this.referenceToMergedIndexMap[comparisonTreeToReferenceTreeVertexIndex];
                        addFragment(elementToAdd, this.comparisonTree, rootToAddParentIndexInMergedTree, treeBuilder);
                    } else {
                        // Decide where to put it.
                        if (this.comparisonTree.hasPredecessorVertexWithMeaning(elementToAdd, TinkarTerm.NECESSARY_SET)) {
                            // Easy case is if it is inside  the necessary set, since there is only one necessary set
                            BitSet necessarySetIndexes = this.referenceVisitData().necessarySetIndexes();
                            switch (necessarySetIndexes.cardinality()) {
                                case 0 -> {
                                    // If there are no necessary sets, we can start the merge with a new necessary set...
                                    int necessarySetIndexInComparison =
                                            this.comparisonVisitData().necessarySetIndexes().nextSetBit(0);
                                    EntityVertex necessarySetVertexInComparison = this.comparisonTree.vertex(necessarySetIndexInComparison);
                                    addFragment(necessarySetVertexInComparison, this.comparisonTree, treeBuilder.getRoot().vertexIndex(), treeBuilder);
                                }
                                case 1 -> {
                                    // Find the AND under the necessary set...
                                    DiTreeEntity tree = treeBuilder.build();
                                    EntityVertex necessarySet = tree.firstVertexWithMeaning(TinkarTerm.NECESSARY_SET).get();
                                    ImmutableList<EntityVertex> necessarySetSuccessors = tree.successors(necessarySet);

                                    necessarySetSuccessors.getFirstOptional().ifPresent(andVertex -> {
                                        if (andVertex.getMeaningNid() != TinkarTerm.AND.nid()) {
                                            throw new IllegalStateException("Missing necessary set and: " + tree);
                                        }
                                        addFragment(elementToAdd, this.comparisonTree, andVertex.vertexIndex(), treeBuilder);
                                    });


                                }
                                default ->
                                        throw new IllegalStateException("More than one necessary set found: " + necessarySetIndexes);
                            }
                        } else if (this.comparisonTree.hasPredecessorVertexWithMeaning(elementToAdd, TinkarTerm.SUFFICIENT_SET)) {
                            //@TODO simple algorithm for now, just add all sufficient sets. In the future, do more complete comparison.
                            int andParentIndex =
                                    this.comparisonVisitData().predecessorIndex(elementToAdd.vertexIndex()).getAsInt();
                            if (this.comparisonTree.vertex(andParentIndex).getMeaningNid() != TinkarTerm.AND.nid()) {
                                throw new IllegalStateException("Element to add does not have AND for its parent: " + this.comparisonTree.vertex(andParentIndex));
                            }
                            int sufficientSetIndexInComparison = this.comparisonVisitData().predecessorIndex(andParentIndex).getAsInt();
                            EntityVertex sufficientSetVertexInComparison = this.comparisonTree.vertex(sufficientSetIndexInComparison);
                            if (sufficientSetVertexInComparison.getMeaningNid() != TinkarTerm.SUFFICIENT_SET.nid()) {
                                throw new IllegalStateException("Element to add does not have SUFFICIENT_SET for its ancestor: " + sufficientSetVertexInComparison);
                            }
                            AtomicBoolean notAdded = new AtomicBoolean(true);
                            treeBuilder.vertexMap().forEach((Consumer<EntityVertex>) treeBuilderEntityVertex -> {
                                if (treeBuilderEntityVertex.mostSignificantBits() == sufficientSetVertexInComparison.mostSignificantBits() &&
                                        treeBuilderEntityVertex.leastSignificantBits() == sufficientSetVertexInComparison.leastSignificantBits()) {
                                    notAdded.set(false);
                                }
                            });
                            if (notAdded.get()) {
                                addFragment(sufficientSetVertexInComparison, this.comparisonTree, treeBuilder.getRoot().vertexIndex(), treeBuilder);
                            }
                        } else if (this.comparisonTree.hasPredecessorVertexWithMeaning(elementToAdd, TinkarTerm.INCLUSION_SET)) {
                            // There can be multiple implication sets
                            //@TODO simple algorithm for now, just add all implication sets. In the future, do more complete comparison.
                            int andParentIndex =
                                    this.comparisonVisitData().predecessorIndex(elementToAdd.vertexIndex()).getAsInt();
                            if (this.comparisonTree.vertex(andParentIndex).getMeaningNid() != TinkarTerm.AND.nid()) {
                                throw new IllegalStateException("Element to add does not have AND for its parent: " + this.comparisonTree.vertex(andParentIndex));
                            }
                            int implicationSetIndexInComparison = this.comparisonVisitData().predecessorIndex(andParentIndex).getAsInt();
                            EntityVertex implicationSetVertexInComparison = this.comparisonTree.vertex(implicationSetIndexInComparison);
                            if (implicationSetVertexInComparison.getMeaningNid() != TinkarTerm.INCLUSION_SET.nid()) {
                                throw new IllegalStateException("Element to add does not have IMPLICATION_SET for its ancestor: " + implicationSetVertexInComparison);
                            }
                            AtomicBoolean notAdded = new AtomicBoolean(true);
                            treeBuilder.vertexMap().forEach((Consumer<EntityVertex>) treeBuilderEntityVertex -> {
                                if (treeBuilderEntityVertex.mostSignificantBits() == implicationSetVertexInComparison.mostSignificantBits() &&
                                        treeBuilderEntityVertex.leastSignificantBits() == implicationSetVertexInComparison.leastSignificantBits()) {
                                    notAdded.set(false);
                                }
                            });
                            if (notAdded.get()) {
                                addFragment(implicationSetVertexInComparison, this.comparisonTree, treeBuilder.getRoot().vertexIndex(), treeBuilder);
                            }
                        } else if (this.comparisonTree.hasPredecessorVertexWithMeaning(elementToAdd, TinkarTerm.PROPERTY_SET)) {
                            // Easy case is if it is inside  the property set, since there is only one property set
                            BitSet propertySetIndexes = this.referenceVisitData().propertySetIndexes();
                            switch (propertySetIndexes.cardinality()) {
                                case 0 -> {
                                    // If there are no property sets, we can start the merge with a new property set...
                                    int propertySetIndexInComparison =
                                            this.comparisonVisitData().propertySetIndexes().nextSetBit(0);
                                    EntityVertex propertySetVertexInComparison = this.comparisonTree.vertex(propertySetIndexInComparison);
                                    addFragment(propertySetVertexInComparison, this.comparisonTree, treeBuilder.getRoot().vertexIndex(), treeBuilder);
                                }
                                case 1 -> {
                                    // Find the AND under the property set...
                                    DiTreeEntity tree = treeBuilder.build();
                                    EntityVertex propertySet = tree.firstVertexWithMeaning(TinkarTerm.PROPERTY_SET).get();
                                    ImmutableList<EntityVertex> propertySetSuccessors = tree.successors(propertySet);

                                    propertySetSuccessors.getFirstOptional().ifPresent(andVertex -> {
                                        if (andVertex.getMeaningNid() != TinkarTerm.AND.nid()) {
                                            throw new IllegalStateException("Missing property set and: " + tree);
                                        }
                                        addFragment(elementToAdd, this.comparisonTree, andVertex.vertexIndex(), treeBuilder);
                                    });
                                }
                                default ->
                                        throw new IllegalStateException("More than one property set found: " + propertySetIndexes);
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("Element to add does not have a predecessor: " + elementToAdd);
                }
                this.mergedTree = treeBuilder.build();
                if (stopwatch != null) {
                    stopwatch.end(FULL_COMPARISON);
                }
            });
        }
        return this;
    }

    /**
     * Adds the fragment.
     *
     * @param rootToAdd                        the root to add
     * @param originTree                       the origin expression
     * @param rootToAddParentIndexInMergedTree the root to add parent sequence
     */
    private void addFragment(EntityVertex rootToAdd,
                             DiTreeEntity originTree,
                             int rootToAddParentIndexInMergedTree, DiTreeEntity.Builder treeBuilder) {
        final ImmutableList<EntityVertex> descendents = originTree.descendents(rootToAdd);
        int mergedExpressionIndex = treeBuilder.vertexCount();
        final int[] additionSolution = ArrayUtil.createAndFillWithMinusOne(originTree.vertexCount());
        additionSolution[rootToAdd.vertexIndex()] = mergedExpressionIndex++;

        for (final EntityVertex descendent : descendents) {
            additionSolution[descendent.vertexIndex()] = mergedExpressionIndex++;
        }

        final EntityVertex[] addedNodes = treeBuilder.addVertexesWithMap(originTree,
                additionSolution,
                rootToAdd.vertexIndex());

        treeBuilder.addEdge(addedNodes[0].vertexIndex(), rootToAddParentIndexInMergedTree);
    }
}
