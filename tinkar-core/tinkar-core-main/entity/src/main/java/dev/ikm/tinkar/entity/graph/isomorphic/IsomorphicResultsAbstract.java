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
import dev.ikm.tinkar.entity.graph.VertexVisitData;
import dev.ikm.tinkar.entity.graph.VisitProcessor;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.factory.primitive.ObjectIntMaps;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static dev.ikm.tinkar.common.util.Symbols.NULL_SIGN;

/**
 *
 */
public abstract class IsomorphicResultsAbstract<VVD extends VertexVisitData>
        implements IsomorphicResults, Callable<IsomorphicResults> {

    protected final int referencedConceptNid;

    protected final DiTreeEntity referenceTree;
    private final VVD referenceVisitData;
    protected final DiTreeEntity comparisonTree;
    private final VVD comparisonVisitData;
    protected IndexCorrelationSolution referenceToComparisonIndexCorrelation;
    /**
     * The isomorphic expression.
     */
    protected DiTreeEntity isomorphicTree;

    /**
     * The merged expression.
     */
    protected DiTreeEntity mergedTree;

    /**
     * Nodes that are relationship roots in the referenceExpression.
     */
    protected final MutableObjectIntMap<SetElementKey> referenceRelationshipNodesMap = ObjectIntMaps.mutable.empty();

    /**
     * Nodes that are relationship roots in the comparisonExpression.
     */
    protected final MutableObjectIntMap<SetElementKey> comparisonRelationshipNodesMap = ObjectIntMaps.mutable.empty();
    /**
     * The comparison deletion roots.
     */
    protected final BitSet comparisonDeletionRoots;

    /**
     * The reference addition roots.
     */
    protected final BitSet referenceAdditionRoots;
    /**
     * The reference tree to merged vertex index map.
     */
    protected final int[] referenceToMergedIndexMap;

    /**
     * The reference tree vertex index to merged vertex index map.
     */
    protected final int[] referenceToIsomorphicIndexMap;
    /**
     * The comparison tree vertex index to reference vertex index map.
     */
    protected final int[] comparisonToReferenceIndexMap;

    protected final MultipleEndpointTimer.Stopwatch stopwatch;

    public IsomorphicResultsAbstract(DiTreeEntity referenceTree, DiTreeEntity comparisonTree, int referencedConceptNid, MultipleEndpointTimer.Stopwatch stopwatch) {
        this.referenceTree = referenceTree;
        this.comparisonTree = comparisonTree;
        this.referenceVisitData = makeVertexVisitData(referenceTree.vertexCount(), this::vertexStartProcessor, this::vertexEndProcessor);
        this.comparisonVisitData = makeVertexVisitData(comparisonTree.vertexCount(), this::vertexStartProcessor, this::vertexEndProcessor);
        this.referenceToMergedIndexMap = ArrayUtil.createAndFillWithMinusOne(referenceTree.vertexMap().size());
        this.referenceToIsomorphicIndexMap = ArrayUtil.createAndFillWithMinusOne(referenceTree.vertexMap().size());
        this.comparisonToReferenceIndexMap = ArrayUtil.createAndFillWithMinusOne(comparisonTree.vertexMap().size());
        this.referencedConceptNid = referencedConceptNid;
        this.comparisonDeletionRoots = new BitSet(this.comparisonTree.vertexCount());
        this.referenceAdditionRoots = new BitSet(this.referenceTree.vertexCount());
        this.stopwatch = stopwatch;
    }


    public VVD referenceVisitData() {
        return this.referenceVisitData;
    }
    public VVD comparisonVisitData() {
        return this.comparisonVisitData;
    }

    protected VVD makeVertexVisitData(int graphSize) {
        return makeVertexVisitData(graphSize, null, null);
    }
    protected VVD makeVertexVisitData(int graphSize, VisitProcessor<VVD> vertexStartProcessor) {
        return makeVertexVisitData(graphSize, vertexStartProcessor, null);
    }

    protected abstract VVD makeVertexVisitData(int graphSize, VisitProcessor<VVD> vertexStartProcessor, VisitProcessor<VVD> vertexEndProcessor);

    protected abstract void vertexStartProcessor(EntityVertex vertex, DiGraphAbstract<EntityVertex> graph, VVD visitData);

    protected abstract void vertexEndProcessor(EntityVertex vertex, DiGraphAbstract<EntityVertex> graph, VVD visitData);
    /**
     * Compute additions.
     */
    protected final void computeReferenceInclusionsAndDeletions() {
        final BitSet referenceVertexesInSolution = new BitSet(this.referenceTree.vertexCount());
        final BitSet referenceVertexesNotInSolution = new BitSet(this.referenceTree.vertexCount());

        for (int i = 0; i < referenceToComparisonIndexCorrelation.solution().size(); i++) {
            if (this.referenceToComparisonIndexCorrelation.solution().get(i) >= 0) {
                referenceVertexesInSolution.set(i);
            } else {
                referenceVertexesNotInSolution.set(i);
            }
        }

        referenceVertexesNotInSolution.stream().forEach((referenceVertexIndex) -> {
            int referenceDeletionRoot = referenceVertexIndex;

            OptionalInt predecessorIndex = this.referenceVisitData.predecessorIndex(referenceDeletionRoot);
            while (predecessorIndex.isPresent() && referenceVertexesNotInSolution.get(predecessorIndex.getAsInt())) {
                referenceDeletionRoot = predecessorIndex.getAsInt();
                predecessorIndex = this.referenceVisitData.predecessorIndex(referenceDeletionRoot);
            }

            this.referenceAdditionRoots.set(referenceDeletionRoot);
        });
    }

    /**
     * Compute deletions.
     */
    protected final void computeComparisonInclusionsAndDeletions() {
        final BitSet comparisonVertexesInSolution = new BitSet(this.comparisonTree.vertexCount());

        referenceToComparisonIndexCorrelation.solution().forEach((vertexIndex) -> {
            if (vertexIndex >= 0) {
                comparisonVertexesInSolution.set(vertexIndex);
            }
        });

        final BitSet comparisonVertexesNotInSolution = new BitSet(this.comparisonTree.vertexCount());

        IntStream.range(0, this.comparisonVisitData.vertexesVisitedCount())
                .forEach((vertexIndex) -> {
                    if (!comparisonVertexesInSolution.get(vertexIndex)) {
                        comparisonVertexesNotInSolution.set(vertexIndex);
                    }
                });
        comparisonVertexesNotInSolution.stream().forEach((deletedVertexIndex) -> {
            int deletedRootIndex = deletedVertexIndex;

            OptionalInt predecessorNid = this.comparisonVisitData.predecessorIndex(deletedRootIndex);
            while (predecessorNid.isPresent() && comparisonVertexesNotInSolution.get(predecessorNid.getAsInt())) {
                deletedRootIndex = predecessorNid.getAsInt();
                predecessorNid = this.comparisonVisitData.predecessorIndex(deletedRootIndex);
            }
            this.comparisonDeletionRoots.set(deletedRootIndex);
        });
    }

    @Override
    public final boolean equivalent() {
        if (referenceVisitData.graphSize() == comparisonVisitData.graphSize()) {
            if (!referenceAdditionRoots.isEmpty()) {
                return false;
            }
            if (!comparisonDeletionRoots.isEmpty()) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public final List<EntityVertex> getAddedSetElements() {
        final TreeSet<SetElementKey> addedSetElementKeys = getAddedSetElementKeys();
        List<EntityVertex> results = new ArrayList<>();
        for (SetElementKey setElementKey : addedSetElementKeys) {
            results.add(this.comparisonTree.vertex(this.comparisonRelationshipNodesMap.getOrThrow(setElementKey)));
        }
        return results;
    }

    @Override
    public final TreeSet<SetElementKey> getAddedSetElementKeys() {
        final TreeSet<SetElementKey> addedSetElementKeys = new TreeSet<>(this.comparisonRelationshipNodesMap.keySet());
        addedSetElementKeys.removeAll(this.referenceRelationshipNodesMap.keySet());
        return addedSetElementKeys;
    }

    @Override
    public final List<EntityVertex> getAdditionalVertexRoots() {
        List<EntityVertex> results = new ArrayList<>();
        this.referenceAdditionRoots.stream().forEach(rootVertexIndex -> results.add(this.referenceTree.vertex(rootVertexIndex)));
        return results;
    }

    @Override
    public final DiTreeEntity getComparisonTree() {
        return this.comparisonTree;
    }

    @Override
    public final List<EntityVertex> getDeletedVertexRoots() {
        List<EntityVertex> results = new ArrayList<>();
        this.comparisonDeletionRoots.stream().forEach(deletedVertexIndex -> results.add(this.comparisonTree.vertex(deletedVertexIndex)));
        return results;
    }

    @Override
    public final List<EntityVertex> getDeletedSetElements() {
        final TreeSet<SetElementKey> deletedSetElementKeys = getDeletedSetElementKeys();
        List<EntityVertex> results = new ArrayList<>();
        for (SetElementKey deletedElementKey : deletedSetElementKeys) {
            results.add(this.referenceTree.vertex(this.referenceRelationshipNodesMap.getOrThrow(deletedElementKey)));
        }
        return results;
    }

    @Override
    public final TreeSet<SetElementKey> getDeletedSetElementKeys() {
        final TreeSet<SetElementKey> deletedElementKeys = new TreeSet<>(this.referenceRelationshipNodesMap.keySet());
        deletedElementKeys.removeAll(this.comparisonRelationshipNodesMap.keySet());
        return deletedElementKeys;
    }

    @Override
    public final DiTreeEntity getIsomorphicTree() {
        return this.isomorphicTree;
    }

    public final IndexCorrelationSolution getIsomorphicSolution() {
        return this.referenceToComparisonIndexCorrelation;
    }

    @Override
    public final DiTreeEntity getMergedTree() {
        return this.mergedTree;
    }

    @Override
    public final DiTreeEntity getReferenceTree() {
        return this.referenceTree;
    }

    @Override
    public final List<EntityVertex> getSharedSetElements() {
        final TreeSet<SetElementKey> sharedRelationshipRoots = new TreeSet<>(this.referenceRelationshipNodesMap.keySet());
        sharedRelationshipRoots.retainAll(this.comparisonRelationshipNodesMap.keySet());
        List<EntityVertex> results = new ArrayList<>();
        for (SetElementKey sharedRootKey : sharedRelationshipRoots) {
            results.add(this.comparisonTree.vertex(this.comparisonRelationshipNodesMap.get(sharedRootKey)));
        }
        return results;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Path Hash Isomorphic Analysis for:")
                .append(PrimitiveData.text(this.referencedConceptNid))
                .append("\n     ").append(PrimitiveData.publicId(this.referencedConceptNid).idString())
                .append("\n\n");
        builder.append("Reference:\n\n ");
        builder.append(this.referenceTree.toString("r"));
        builder.append("\nComparison:\n\n ");
        builder.append(this.comparisonTree.toString("c"));
        if (this.isomorphicTree != null) {
            builder.append("\nIsomorphic:\n\n ");
            builder.append(this.isomorphicTree.toString("i"));
        }

        if (referenceToIsomorphicIndexMap != null) {
            builder.append("\nReference To Isomorphic Index Map:\n\n ");
            builder.append("[");
            for (int i = 0; i < referenceToIsomorphicIndexMap.length; i++) {
                builder.append(i).append("r:");
                if (referenceToIsomorphicIndexMap[i] == -1) {
                    builder.append(NULL_SIGN);
                } else {
                    builder.append(referenceToIsomorphicIndexMap[i]).append("i");
                }
                if (i < referenceToIsomorphicIndexMap.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]\n");
        }

        if (referenceToMergedIndexMap != null) {
            builder.append("\nReference To Merged Index Map:\n\n ");
            builder.append("[");
            for (int i = 0; i < referenceToMergedIndexMap.length; i++) {
                builder.append(i).append("r:");
                if (referenceToMergedIndexMap[i] == -1) {
                    builder.append(NULL_SIGN);
                } else {
                    builder.append(referenceToMergedIndexMap[i]).append("m");
                }
                if (i < referenceToMergedIndexMap.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]\n");
        }

        if (comparisonToReferenceIndexMap != null) {
            builder.append("\nReference To Comparison Index Map:\n\n ");
            int[] referenceToComparisonIndexMap = ArrayUtil.createAndFillWithMinusOne(referenceToMergedIndexMap.length);
            for (int i = 0; i < comparisonToReferenceIndexMap.length; i++) {
                if (comparisonToReferenceIndexMap[i] >= 0) {
                    referenceToComparisonIndexMap[comparisonToReferenceIndexMap[i]] = i;
                }
            }
            builder.append("[");
            for (int i = 0; i < referenceToComparisonIndexMap.length; i++) {
                builder.append(i).append("r:");
                if (referenceToComparisonIndexMap[i] == -1) {
                    builder.append(NULL_SIGN);
                } else {
                    builder.append(referenceToComparisonIndexMap[i]).append("c");
                }
                if (i < referenceToComparisonIndexMap.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]\n");
            builder.append("\nComparison To Reference Index Map:\n\n ");
            builder.append("[");
            for (int i = 0; i < comparisonToReferenceIndexMap.length; i++) {
                builder.append(i).append("c:");
                if (comparisonToReferenceIndexMap[i] == -1) {
                    builder.append(NULL_SIGN);
                } else {
                    builder.append(comparisonToReferenceIndexMap[i]).append("r");
                }
                if (i < comparisonToReferenceIndexMap.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]\n");
        }

        if (this.referenceToComparisonIndexCorrelation != null) {
            builder.append("\nIsomorphic solution: \n");
            String formatString = "[%2d";
            String nullString = " " + NULL_SIGN + " ";
            if (this.referenceToComparisonIndexCorrelation.solution().size() < 10) {
                formatString = "[%d";
                nullString = " " + NULL_SIGN + " ";
            }
            if (this.referenceToComparisonIndexCorrelation.solution().size() > 99) {
                formatString = "[%3d";
                nullString = " " + NULL_SIGN + " ";
            }
            for (int i = 0; i < this.referenceToComparisonIndexCorrelation.solution().size(); i++) {
                builder.append("  ");
                builder.append(String.format(formatString, i));
                builder.append("r] \u279e ");
                if (this.referenceToComparisonIndexCorrelation.solution().get(i) == -1) {
                    builder.append(nullString);
                } else {
                    builder.append(String.format(formatString, this.referenceToComparisonIndexCorrelation.solution().get(i)));
                }
                if (this.referenceToComparisonIndexCorrelation.solution().get(i) < 0) {
                    builder.append("\n");
                } else if (i != this.referenceToComparisonIndexCorrelation.solution().get(i)) {
                    builder.append("c]* ");
                    builder.append(this.referenceTree.vertex(i).toString("r"));
                    builder.append("\n");
                } else {
                    builder.append("c]  ");
                    builder.append(this.referenceTree.vertex(i).toString("r"));
                    builder.append("\n");
                }
            }
            builder.append("\nAdditions: \n\n");
            getAdditionalVertexRoots().forEach((EntityVertex additionRoot) -> {
                builder.append("  ").append(this.referenceTree.fragmentToString("r", additionRoot));
                builder.append("\n");
            });
            builder.append("\nDeletions: \n\n");
            getDeletedVertexRoots().forEach((EntityVertex deletionRoot) -> {
                builder.append("  ").append(this.comparisonTree.fragmentToString("c", deletionRoot));
                builder.append("\n");
            });
            builder.append("\nShared set elements: \n\n");
            getSharedSetElements().forEach((EntityVertex sharedRelRoot) -> {
                builder.append("  ").append(this.referenceTree.fragmentToString(sharedRelRoot));
                builder.append("\n");
            });
            builder.append("\nNew set elements: \n\n");
            getAddedSetElements().forEach((EntityVertex addedRelRoot) -> {
                builder.append("  ").append(this.comparisonTree.fragmentToString(addedRelRoot));
                builder.append("\n");
            });
            builder.append("\nDeleted set elements: \n\n");
            getDeletedSetElements().forEach((EntityVertex deletedRelRoot) -> {
                builder.append("  ").append(this.referenceTree.fragmentToString(deletedRelRoot));
                builder.append("\n");
            });
            builder.append("\nMerged: \n\n");
            if (this.mergedTree != null) {
                builder.append(this.mergedTree.toString("m"));
            } else {
                builder.append("null");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

}
