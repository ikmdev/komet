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
package dev.ikm.tinkar.common.id;

import dev.ikm.tinkar.common.id.impl.PublicId1;
import dev.ikm.tinkar.common.id.impl.PublicId2;
import dev.ikm.tinkar.common.id.impl.PublicId3;
import dev.ikm.tinkar.common.id.impl.PublicIdN;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PublicIds {
    public static final PublicIdListFactory list = PublicIdListFactory.INSTANCE;
    public static final PublicIdSetFactory set = PublicIdSetFactory.INSTANCE;

    public static final PublicId newRandom() {
        return new PublicId1(UUID.randomUUID());
    }

    public static final PublicId of(long msb, long lsb) {
        return new PublicId1(msb, lsb);
    }

    public static final PublicId of(long msb, long lsb, long msb2, long lsb2) {
        return new PublicId2(msb, lsb, msb2, lsb2);
    }

    public static final PublicId of(long msb, long lsb, long msb2, long lsb2, long msb3, long lsb3) {
        return new PublicId3(msb, lsb, msb2, lsb2, msb3, lsb3);
    }

    public static final PublicId of(ImmutableList<UUID> list) {
        return of(list.toArray(new UUID[list.size()]));
    }

    public static final PublicId of(UUID... uuids) {
        if (uuids == null) {
            throw new IllegalStateException("UUIDs cannot be null");
        }
        if (uuids.length == 1) {
            return new PublicId1(uuids[0]);
        }
        if (uuids.length == 2) {
            return new PublicId2(uuids[0], uuids[1]);
        }
        if (uuids.length == 3) {
            return new PublicId3(uuids[0], uuids[1], uuids[2]);
        }
        return new PublicIdN(uuids);
    }

    public static final PublicId of(String... uuidStrings) {
        return of(Arrays.stream(uuidStrings).map(s -> UUID.fromString(s)).toList());
    }

    public static final PublicId of(List<UUID> list) {
        return of(list.toArray(new UUID[list.size()]));
    }

    public static final PublicId of(long... uuidParts) {
        if (uuidParts == null) {
            throw new IllegalStateException("uuidParts cannot be null");
        }
        if (uuidParts.length == 2) {
            return new PublicId1(uuidParts[0], uuidParts[1]);
        }
        if (uuidParts.length == 4) {
            return new PublicId2(uuidParts[0], uuidParts[1], uuidParts[2], uuidParts[3]);
        }
        if (uuidParts.length == 6) {
            return new PublicId3(uuidParts[0], uuidParts[1], uuidParts[2], uuidParts[3], uuidParts[4], uuidParts[5]);
        }
        return new PublicIdN(uuidParts);
    }

    public static final PublicId singleSemanticId(PublicId patternId, PublicId referencedComponentId) {
        return PublicIds.of(UuidT5Generator.singleSemanticUuid(patternId.asUuidArray(), referencedComponentId.asUuidArray()));
    }
}
