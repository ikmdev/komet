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
package dev.ikm.tinkar.common.id.impl;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.VertexId;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.LongConsumer;

public class PublicId1 extends PublicIdA implements VertexId {

    protected final long msb;
    protected final long lsb;

    public PublicId1(UUID uuid) {
        this.msb = uuid.getMostSignificantBits();
        this.lsb = uuid.getLeastSignificantBits();
    }

    public PublicId1(long msb, long lsb) {
        this.msb = msb;
        this.lsb = lsb;
    }

    @Override
    public int uuidCount() {
        return 1;
    }

    @Override
    public long mostSignificantBits() {
        return msb;
    }

    @Override
    public long leastSignificantBits() {
        return lsb;
    }

    @Override
    public UUID[] asUuidArray() {
        return new UUID[]{new UUID(msb, lsb)};
    }

    @Override
    public void forEach(LongConsumer consumer) {
        consumer.accept(msb);
        consumer.accept(lsb);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof PublicId publicId) {
            if (o instanceof PublicId1 publicId1) {
                return msb == publicId1.msb && lsb == publicId1.lsb;
            }
            if (o instanceof PublicId2 publicId2) {
                return msb == publicId2.msb && lsb == publicId2.lsb ||
                        msb == publicId2.msb2 && lsb == publicId2.lsb2;
            }
            if (o instanceof PublicId3 publicId3) {
                return msb == publicId3.msb && lsb == publicId3.lsb ||
                        msb == publicId3.msb2 && lsb == publicId3.lsb2 ||
                        msb == publicId3.msb3 && lsb == publicId3.lsb3;
            }
            UUID[] thisUuids = asUuidArray();
            return Arrays.stream(publicId.asUuidArray()).anyMatch(uuid -> {
                for (UUID thisUuid : thisUuids) {
                    if (uuid.equals(thisUuid)) {
                        return true;
                    }
                }
                return false;
            });
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new long[] {msb, lsb});
    }
}
