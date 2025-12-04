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
import dev.ikm.tinkar.common.util.Validator;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;

import java.util.UUID;

/**
 * Utility class to simplify writing identifier data to entity records.
 */
public record PublicIdentifierRecord(long mostSignificantBits,
                                     long leastSignificantBits,
                                     long[] additionalUuidLongs) {

    public PublicIdentifierRecord {
        Validator.notZero(mostSignificantBits);
        Validator.notZero(leastSignificantBits);
    }
    public static PublicIdentifierRecord make(PublicId publicId) {
        UUID[] uuids = publicId.asUuidArray();

        if (uuids.length > 1) {
            UUID[] additionalUuids = new UUID[uuids.length - 1];
            for (int i = 1; i < uuids.length; i++) {
                additionalUuids[i - 1] = uuids[i];
            }
            long[] additionalUuidLongs = UuidUtil.asArray(additionalUuids);
            return new PublicIdentifierRecord(uuids[0].getMostSignificantBits(), uuids[0].getLeastSignificantBits(), additionalUuidLongs);
        }
        return new PublicIdentifierRecord(uuids[0].getMostSignificantBits(), uuids[0].getLeastSignificantBits(), null);
    }
}
