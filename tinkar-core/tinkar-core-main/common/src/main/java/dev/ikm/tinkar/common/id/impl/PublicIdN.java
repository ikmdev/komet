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
import dev.ikm.tinkar.common.util.uuid.UuidUtil;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.LongConsumer;

public class PublicIdN implements PublicId {

    private final long[] uuidParts;

    public PublicIdN(UUID... uuids) {
        uuidParts = UuidUtil.asArray(uuids);
    }

    public PublicIdN(long... uuidParts) {
        this.uuidParts = uuidParts;
    }

    @Override
    public int uuidCount() {
        return uuidParts.length/2;
    }

    @Override
    public UUID[] asUuidArray() {
        return UuidUtil.toArray(uuidParts);
    }

    @Override
    public void forEach(LongConsumer consumer) {
        for (long uuidPart: uuidParts) {
            consumer.accept(uuidPart);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof PublicId publicId) {
            UUID[] thisUuids = asUuidArray();
            return Arrays.stream(publicId.asUuidArray()).anyMatch(uuid -> {
                for (UUID thisUuid: thisUuids) {
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
        return Arrays.hashCode(uuidParts);
    }

}
