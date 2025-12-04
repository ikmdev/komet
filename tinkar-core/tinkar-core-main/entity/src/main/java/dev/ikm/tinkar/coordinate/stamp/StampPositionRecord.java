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


import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.ImmutableCoordinate;
import dev.ikm.tinkar.coordinate.PathService;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.eclipse.collections.api.set.ImmutableSet;

import java.time.Instant;

@RecordBuilder
public record StampPositionRecord(long time, int pathForPositionNid)
        implements StampPosition, Comparable<StampPosition>, ImmutableCoordinate, StampPositionRecordBuilder.With {

    public static StampPositionRecord make(Instant time, ConceptFacade pathForPosition) {
        return new StampPositionRecord(DateTimeUtil.instantToEpochMs(time), pathForPosition.nid());
    }

    public static StampPositionRecord make(Instant time, int pathForPositionNid) {
        return new StampPositionRecord(DateTimeUtil.instantToEpochMs(time), pathForPositionNid);
    }

    public static StampPositionRecord make(long time, int pathForPositionNid) {
        return new StampPositionRecord(time, pathForPositionNid);
    }

    public static StampPositionRecord make(long time, ConceptFacade pathForPosition) {
        return new StampPositionRecord(time, pathForPosition.nid());
    }

    @Decoder
    public static StampPositionRecord decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                return new StampPositionRecord(in.readLong(), in.readInt());
        }
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        out.writeLong(this.time);
        out.writeInt(this.pathForPositionNid);
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    @Override
    public long time() {
        return this.time;
    }

    /**
     * Compare to.
     *
     * @param o the o
     * @return the int
     */
    @Override
    public int compareTo(StampPosition o) {
        final int comparison = Long.compare(this.time, o.time());

        if (comparison != 0) {
            return comparison;
        }

        return Integer.compare(this.pathForPositionNid, o.getPathForPositionNid());
    }

    @Override
    public int getPathForPositionNid() {
        return this.pathForPositionNid;
    }

    /**
     * Gets the stamp path ConceptFacade.
     *
     * @return the stamp path ConceptFacade
     */
    public ConceptFacade getPathForPositionConcept() {
        return Entity.getFast(this.pathForPositionNid);
    }

    @Override
    public StampPositionRecord withTime(long time) {
        return StampPositionRecordBuilder.With.super.withTime(time);
    }

    @Override
    public StampPositionRecord withPathForPositionNid(int pathForPositionNid) {
        return StampPositionRecordBuilder.With.super.withPathForPositionNid(pathForPositionNid);
    }

    @Override
    public StampPositionRecord toStampPositionImmutable() {
        return this;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof final StampPosition other)) {
            return false;
        }

        if (this.time != other.time()) {
            return false;
        }

        return this.pathForPositionNid == other.getPathForPositionNid();
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = 83 * hash + (int) (this.time ^ (this.time >>> 32));
        hash = 83 * hash + Integer.hashCode(this.pathForPositionNid);
        return hash;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("StampPosition:{");

        if (this.time == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.time == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(instant());
        }

        sb.append(" on '")
                .append(PrimitiveData.text(this.pathForPositionNid))
                .append("'}");
        return sb.toString();
    }

    public ImmutableSet<StampPositionRecord> getPathOrigins() {
        return PathService.get().getPathOrigins(this.pathForPositionNid);
    }
}
