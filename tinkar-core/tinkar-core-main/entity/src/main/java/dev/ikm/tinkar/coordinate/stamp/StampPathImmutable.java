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
package dev.ikm.tinkar.coordinate.stamp;

import dev.ikm.tinkar.collection.ConcurrentReferenceHashMap;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.ImmutableCoordinate;
import dev.ikm.tinkar.coordinate.PathService;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import java.util.Objects;

public final class StampPathImmutable implements StampPath, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<Integer, StampPathImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);
    private final int pathConceptNid;
    private final ImmutableSet<StampPositionRecord> pathOrigins;

    private StampPathImmutable(ConceptFacade pathConcept, ImmutableSet<StampPositionRecord> pathOrigins) {
        this.pathConceptNid = pathConcept.nid();
        this.pathOrigins = pathOrigins;
    }

    private StampPathImmutable(int pathConceptNid, ImmutableSet<StampPositionRecord> pathOrigins) {
        this.pathConceptNid = pathConceptNid;
        this.pathOrigins = pathOrigins;
    }


    private StampPathImmutable(DecoderInput in) {
        this.pathConceptNid = in.readNid();
        int pathOriginsSize = in.readVarInt();
        MutableSet<StampPositionRecord> mutableOrigins = Sets.mutable.ofInitialCapacity(pathOriginsSize);
        for (int i = 0; i < pathOriginsSize; i++) {
            mutableOrigins.add(StampPositionRecord.make(in.readLong(), in.readNid()));
        }
        this.pathOrigins = mutableOrigins.toImmutable();
    }

    public static StampPathImmutable make(ConceptFacade pathConcept, ImmutableSet<StampPositionRecord> pathOrigins) {
        return make(pathConcept, pathOrigins);
    }

    public static StampPathImmutable make(int pathConceptNid, ImmutableSet<StampPositionRecord> pathOrigins) {
        if (pathConceptNid == TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
            return new StampPathImmutable(pathConceptNid, pathOrigins);
        }
        return SINGLETONS.computeIfAbsent(pathConceptNid,
                pathNid -> new StampPathImmutable(pathConceptNid, pathOrigins));
    }

    public static StampPathImmutable make(ConceptFacade pathConcept) {
        return make(pathConcept.nid());
    }

    public static StampPathImmutable make(int pathConceptNid) {
        if (pathConceptNid == TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
            return new StampPathImmutable(pathConceptNid, Sets.immutable.empty());
        }
        return SINGLETONS.computeIfAbsent(pathConceptNid,
                pathNid -> {
                    ImmutableSet<StampPositionRecord> pathOrigins = PathService.get().getPathOrigins(pathNid);
                    return new StampPathImmutable(pathNid, pathOrigins);
                });
    }

    @Decoder
    public static StampPathImmutable make(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                StampPathImmutable stampPath = new StampPathImmutable(in);
                if (stampPath.pathConceptNid == TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
                    return stampPath;
                }
                return SINGLETONS.computeIfAbsent(stampPath.pathConceptNid(),
                        pathNid -> stampPath);
        }
    }

    @Override
    public int pathConceptNid() {
        return this.pathConceptNid;
    }

    @Override
    public ImmutableSet<StampPositionRecord> getPathOrigins() {
        return this.pathOrigins;
    }

    public StampPathImmutable toStampPathImmutable() {
        return this;
    }

    public static final StampCoordinateRecord getStampFilter(StampPath stampPath) {
        return StampCoordinateRecord.make(StateSet.ACTIVE_AND_INACTIVE,
                StampPositionRecord.make(Long.MAX_VALUE, stampPath.pathConceptNid()),
                IntIds.set.empty());
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        out.writeInt(this.pathConceptNid);
        out.writeVarInt(this.pathOrigins.size());
        for (StampPositionRecord stampPosition : this.pathOrigins) {
            stampPosition.encode(out);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathConceptNid(), getPathOrigins());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StampPathImmutable that)) return false;
        return pathConceptNid() == that.pathConceptNid() &&
                getPathOrigins().equals(that.getPathOrigins());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StampPathImmutable:{");
        sb.append(PrimitiveData.text(this.pathConceptNid));
        sb.append(" Origins: ").append(this.pathOrigins).append("}");
        return sb.toString();
    }

    public static class CachingProvider implements CachingService {

        @Override
        public void reset() {
            SINGLETONS.clear();
        }
    }

}
