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
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.Validator;
import dev.ikm.tinkar.terms.EntityBinding;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Arrays;
import java.util.Objects;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;

@RecordBuilder
public record PatternRecord(
        long mostSignificantBits, long leastSignificantBits,
        long[] additionalUuidLongs, int nid,
        ImmutableList<PatternVersionRecord> versions)
        implements PatternEntity<PatternVersionRecord>, ImmutableEntity<PatternVersionRecord>, PatternRecordBuilder.With {

    public PatternRecord {
        Validator.notZero(mostSignificantBits);
        Validator.notZero(leastSignificantBits);
        Validator.notZero(nid);
        Objects.requireNonNull(versions);
    }

    public static PatternRecord makeNew(PublicId publicId, RecordListBuilder versionListBuilder) {
        PublicIdentifierRecord publicIdRecord = PublicIdentifierRecord.make(publicId);

        int nid = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Pattern.pattern().publicId())
                .call(() -> PrimitiveData.nid(publicId));

        return new PatternRecord(publicIdRecord.mostSignificantBits(), publicIdRecord.leastSignificantBits(),
                publicIdRecord.additionalUuidLongs(), nid, versionListBuilder);
    }

    @Override
    public byte[] getBytes() {
        return EntityRecordFactory.getBytes(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof PublicId publicId) {
            return PublicId.equals(this.publicId(), publicId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nid);
    }

    @Override
    public String toString() {
        return entityToString();
    }

    public boolean deepEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternRecord that = (PatternRecord) o;
        return mostSignificantBits == that.mostSignificantBits &&
                leastSignificantBits == that.leastSignificantBits &&
                nid == that.nid && Arrays.equals(additionalUuidLongs, that.additionalUuidLongs) &&
                versions.equals(that.versions);
    }

    /**
     * If there is a version with the same stamp as versionToAdd, it will be removed prior to adding the
     * new version so you don't get duplicate versions with the same stamp.
     *
     * @param versionToAdd
     * @return PatternAnalogueBuilder
     */

    public PatternAnalogueBuilder with(PatternEntityVersion versionToAdd) {
        return analogueBuilder().add(versionToAdd);
    }

    public PatternAnalogueBuilder analogueBuilder() {
        RecordListBuilder<PatternVersionRecord> versionRecords = RecordListBuilder.make();
        PatternRecord patternRecord = new PatternRecord(mostSignificantBits, leastSignificantBits, additionalUuidLongs,
                nid, versionRecords);
        for (PatternVersionRecord version : versions) {
            versionRecords.add(new PatternVersionRecord(patternRecord, version.stampNid(),
                    version.semanticPurposeNid(), version.semanticMeaningNid(), version.fieldDefinitions()));
        }
        return new PatternAnalogueBuilder(patternRecord, versionRecords);
    }

    public PatternAnalogueBuilder without(PatternEntityVersion versionToAdd) {
        return analogueBuilder().remove(versionToAdd);
    }

    public PublicId publicId() {
        return PublicIds.of(asUuidArray());
    }
}
