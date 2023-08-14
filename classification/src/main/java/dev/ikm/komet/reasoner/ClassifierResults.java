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
package dev.ikm.komet.reasoner;

import org.eclipse.collections.api.block.procedure.primitive.IntObjectProcedure;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import dev.ikm.tinkar.common.binary.*;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ClassifierResults implements Encodable {

    public static final int marshalVersion = 1;
    /**
     * Set of concepts potentially affected by the last classification.
     */
    private final ImmutableIntList classificationConceptSet;

    private final ImmutableIntList conceptsWithInferredChanges;

    /**
     * The equivalent sets.
     */
    private final ImmutableSet<ImmutableIntList> equivalentSets;

    /**
     * The commit record.
     */
    private final ViewCoordinateRecord viewCoordinate;
    //A map of a concept nid, to a HashSet of int arrays, where each int[] is a cycle present on the concept.
    private final ImmutableIntObjectMap<Set<int[]>> conceptsWithCycles;
    private final ImmutableIntSet orphanedConcepts;

    private ClassifierResults(DecoderInput data) {
        this.classificationConceptSet = IntLists.immutable.of(data.readNidArray());
        this.conceptsWithInferredChanges = IntLists.immutable.of(data.readNidArray());
        int equivalentSetSize = data.readInt();
        MutableSet<ImmutableIntList> equivalentMutibleSets = Sets.mutable.ofInitialCapacity(equivalentSetSize);
        for (int i = 0; i < equivalentSetSize; i++) {
            equivalentMutibleSets.add(IntLists.immutable.of(data.readNidArray()));
        }
        this.equivalentSets = equivalentMutibleSets.toImmutable();
        if (data.readBoolean()) {
            int cycleMapSize = data.readInt();
            MutableIntObjectMap<Set<int[]>> conceptsWithCyclesMutable = IntObjectMaps.mutable.ofInitialCapacity(cycleMapSize);
            for (int i = 0; i < cycleMapSize; i++) {
                int key = data.readInt();
                int setCount = data.readInt();
                Set<int[]> cycleSets = new TreeSet<>();
                for (int j = 0; j < setCount; j++) {
                    cycleSets.add(data.readNidArray());
                }
                conceptsWithCyclesMutable.put(key, cycleSets);
            }
            this.conceptsWithCycles = conceptsWithCyclesMutable.toImmutable();
        } else {
            this.conceptsWithCycles = IntObjectMaps.immutable.empty();
        }
        this.orphanedConcepts = IntSets.immutable.of(data.readNidArray());
        this.viewCoordinate = ViewCoordinateRecord.decode(data);
    }

    /**
     * Instantiates a new classifier results.
     *
     * @param classificationConceptSet the affected concepts
     * @param equivalentSets           the equivalent sets
     * @param viewCoordinateRecord
     */
    public ClassifierResults(ImmutableIntList classificationConceptSet,
                             ImmutableIntList conceptsWithInferredChanges,
                             Set<ImmutableIntList> equivalentSets,
                             ViewCoordinateRecord viewCoordinateRecord) {
        this.classificationConceptSet = classificationConceptSet;
        this.conceptsWithInferredChanges = conceptsWithInferredChanges;
        MutableSet<ImmutableIntList> equivalentMutableSets = Sets.mutable.ofInitialCapacity(equivalentSets.size());
        for (ImmutableIntList set : equivalentSets) {
            equivalentMutableSets.add(IntLists.immutable.of(set.toSortedArray()));
        }

        this.equivalentSets = equivalentMutableSets.toImmutable();
        this.orphanedConcepts = IntSets.immutable.empty();
        this.conceptsWithCycles = IntObjectMaps.immutable.empty();
        this.viewCoordinate = viewCoordinateRecord;
        verifyCoordinates();
    }

    private void verifyCoordinates() {
        if (viewCoordinate.stampCoordinate().stampPosition().time() == Long.MAX_VALUE) {
            throw new IllegalStateException("Filter position time must reflect the actual commit time, not 'latest' (Long.MAX_VALUE) ");
        }
    }

    /**
     * This constructor is only intended to be used when a classification wasn't performed, because there were cycles present.
     *
     * @param conceptsWithCycles
     * @param orphans
     * @param viewCoordinateRecord
     */
    public ClassifierResults(ImmutableIntList classificationConceptSet,
                             ImmutableIntObjectMap<Set<int[]>> conceptsWithCycles,
                             ImmutableIntSet orphans,
                             ViewCoordinateRecord viewCoordinateRecord) {
        this.classificationConceptSet = classificationConceptSet;
        this.conceptsWithInferredChanges = IntLists.immutable.empty();
        this.equivalentSets = Sets.immutable.empty();
        this.conceptsWithCycles = conceptsWithCycles;
        this.orphanedConcepts = orphans;
        this.viewCoordinate = viewCoordinateRecord;
        verifyCoordinates();
    }

    @Decoder
    public static ClassifierResults decode(DecoderInput in) {
        return new ClassifierResults(in);
    }

    @Encoder
    public final void encode(EncoderOutput out) {
        out.writeNidArray(this.classificationConceptSet.toArray());
        out.writeNidArray(this.conceptsWithInferredChanges.toArray());
        out.writeInt(equivalentSets.size());
        for (ImmutableIntList equivalentSet : equivalentSets) {
            out.writeNidArray(equivalentSet.toArray());
        }
        if (!conceptsWithCycles.isEmpty()) {
            out.writeBoolean(true);
            out.writeInt(conceptsWithCycles.size());
            conceptsWithCycles.forEachKeyValue(new IntObjectProcedure<Set<int[]>>() {
                @Override
                public void value(int conceptNid, Set<int[]> cycleNids) {
                    out.writeNid(conceptNid);
                    out.writeInt(cycleNids.size());
                    for (int[] cycle : cycleNids) {
                        out.writeNidArray(cycle);
                    }
                }
            });
        } else {
            out.writeBoolean(false);
        }
        out.writeNidArray(orphanedConcepts.toArray());
        this.viewCoordinate.encode(out);
    }

    @Override
    public String toString() {
        return "ClassifierResults{"
                + " affectedConcepts=" + this.classificationConceptSet.size() + ", equivalentSets="
                + this.equivalentSets.size() + ", Orphans detected=" + orphanedConcepts.size()
                + " Concepts with cycles=" + conceptsWithCycles.size() + '}';
    }

    public ImmutableIntList getClassificationConceptSet() {
        return this.classificationConceptSet;
    }

    public ImmutableSet<ImmutableIntList> getEquivalentSets() {
        return this.equivalentSets;
    }

    public ImmutableIntObjectMap<Set<int[]>> getCycles() {
        return conceptsWithCycles;
    }

    public ImmutableIntSet getOrphans() {
        return orphanedConcepts.toImmutable();
    }

    public ViewCoordinateRecord getViewCoordinate() {
        return viewCoordinate;
    }

    public Instant getCommitTime() {
        return this.viewCoordinate.stampCoordinate().stampPosition().instant();
    }

    public ImmutableIntList getConceptsWithInferredChanges() {
        return conceptsWithInferredChanges;
    }
}
