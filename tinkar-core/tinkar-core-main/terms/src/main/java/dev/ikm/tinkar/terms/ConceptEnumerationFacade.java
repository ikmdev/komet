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
package dev.ikm.tinkar.terms;

import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;

public interface ConceptEnumerationFacade<E extends Enum<E>>
        extends ConceptFacade, Encodable {
    ConceptFacade conceptForEnum();

    String name();

    default PublicId publicId() {
        return this.conceptForEnum().publicId();
    }

    default int nid() {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                .call(() -> PrimitiveData.nid(this.conceptForEnum().publicId()));
    }

    default E enumValue() {
        return (E) this;
    }

    static <E extends Enum<E>> E decode(DecoderInput in, Class<E> enumClass) {
        switch (Encodable.checkVersion(in)) {
            default:
                String encodedName = in.readString();
                PublicId encodedId = PublicIds.of(UuidUtil.fromString(in.readString()));
                ConceptEnumerationFacade<E> enumElement = (ConceptEnumerationFacade<E>) Enum.valueOf(enumClass, encodedName);
                if (enumElement.conceptForEnum().publicId().equals(encodedId)) {
                    return enumElement.enumValue();
                }
                throw new IllegalStateException("Unknown enum concept " + encodedName + "with id " + encodedId);
        }
    }

    @Override
    default void encode(EncoderOutput out) {
        out.writeString(this.name());
        out.writeString(UuidUtil.toString(this.conceptForEnum().publicId()));
    }

}
