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
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.Instant;
import java.util.Objects;

@RecordBuilder
public record StampBranchRecord(int branchConceptNid, long branchOriginTime)
        implements StampBranch, ImmutableCoordinate, StampBranchRecordBuilder.With {

    @Decoder
    public static StampBranchRecord decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                return make(in.readNid(), in.readLong());
        }
    }

    public static StampBranchRecord make(int pathConceptNid, long branchOriginTime) {
        return new StampBranchRecord(pathConceptNid, branchOriginTime);
    }

    public static StampBranchRecord make(int pathConceptNid, Instant branchOriginInstant) {
        return new StampBranchRecord(pathConceptNid, DateTimeUtil.instantToEpochMs(branchOriginInstant));
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        out.writeNid(this.branchConceptNid);
        out.writeLong(this.branchOriginTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StampBranch that)) return false;
        return getPathOfBranchNid() == that.getPathOfBranchNid() &&
                getBranchOriginTime() == that.getBranchOriginTime();
    }

    @Override
    public long getBranchOriginTime() {
        return branchOriginTime;
    }

    public int getPathOfBranchNid() {
        return this.branchConceptNid;
    }

    @Override
    public StampBranchRecord toStampBranchRecord() {
        return this;
    }

    public String toUserString() {
        final StringBuilder sb = new StringBuilder("At date/time ");

        if (this.branchOriginTime == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.branchOriginTime == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }

        sb.append(" start branch for '")
                .append(PrimitiveData.text(this.branchConceptNid))
                .append("'}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPathOfBranchNid(), getBranchOriginTime());
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("StampBranchImmutable:{ At date/time ");

        if (this.branchOriginTime == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.branchOriginTime == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }

        sb.append(" start branch for '")
                .append(PrimitiveData.text(this.branchConceptNid))
                .append("'}");
        return sb.toString();
    }
}
