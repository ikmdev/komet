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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;

import java.util.UUID;
import java.util.function.LongConsumer;

public interface IdentifierData extends PublicId {

    long mostSignificantBits();
    long leastSignificantBits();
    long[] additionalUuidLongs();
    int nid();

    @Override
    default UUID[] asUuidArray() {
        UUID[] uuidArray = new UUID[uuidCount()];
        uuidArray[0] = new UUID(mostSignificantBits(), leastSignificantBits());
        long[] additionalUuidLongs = additionalUuidLongs();
        if (additionalUuidLongs != null) {
            for (int i = 1; i < uuidArray.length; i++) {
                uuidArray[i] = new UUID(additionalUuidLongs[((i - 1) * 2)], additionalUuidLongs[((i - 1) * 2) + 1]);
            }
        }
        return uuidArray;
    }

    @Override
    default int uuidCount() {
        if (additionalUuidLongs() != null) {
            return (additionalUuidLongs().length / 2) + 1;
        }
        return 1;
    }

    @Override
    default void forEach(LongConsumer consumer) {
        consumer.accept(mostSignificantBits());
        consumer.accept(leastSignificantBits());
        if (additionalUuidLongs() != null) {
            for (long uuidPart : additionalUuidLongs()) {
                consumer.accept(uuidPart);
            }
        }
    }

    default PublicId publicId() {
        return PublicIds.of(asUuidArray());
    }
}
