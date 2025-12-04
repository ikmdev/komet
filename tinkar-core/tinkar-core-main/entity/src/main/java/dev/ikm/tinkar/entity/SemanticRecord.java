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
import dev.ikm.tinkar.terms.PatternFacade;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;
import static dev.ikm.tinkar.terms.TinkarTermV2.STAMP_PATTERN;

@RecordBuilder
public record SemanticRecord(
        long mostSignificantBits, long leastSignificantBits,
        long[] additionalUuidLongs, int nid, int patternNid, int referencedComponentNid,
        ImmutableList<SemanticVersionRecord> versions)
        implements SemanticEntity<SemanticVersionRecord>, ImmutableEntity<SemanticVersionRecord>, SemanticRecordBuilder.With {

        public SemanticRecord {
            Validator.notZero(mostSignificantBits);
            Validator.notZero(leastSignificantBits);
            Validator.notZero(nid);
            Validator.notZero(patternNid);
            Validator.notZero(referencedComponentNid);
            Objects.requireNonNull(versions);
        }

    public static SemanticRecord makeNew(PublicId publicId, PatternFacade patternFacade, int referencedComponentNid,
                                         RecordListBuilder versionListBuilder) {
        return makeNew(publicId, patternFacade.nid(), referencedComponentNid, versionListBuilder);
    }

    public static SemanticRecord makeNew(PublicId publicId, int patternNid, int referencedComponentNid,
                                         RecordListBuilder versionListBuilder) {
        PublicIdentifierRecord publicIdRecord = PublicIdentifierRecord.make(publicId);

        int nid = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, PrimitiveData.publicId(patternNid))
                .call(() -> PrimitiveData.nid(publicId));

        return new SemanticRecord(publicIdRecord.mostSignificantBits(), publicIdRecord.leastSignificantBits(),
                publicIdRecord.additionalUuidLongs(), nid, patternNid, referencedComponentNid,
                versionListBuilder);
    }

    public static SemanticRecord build(UUID semanticUuid,
                                       int patternNid,
                                       int referencedComponentNid,
                                       StampEntityVersion stampVersion,
                                       ImmutableList<Object> fields) {
        RecordListBuilder<SemanticVersionRecord> versionRecords = RecordListBuilder.make();
        int semanticNid = ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, Entity.getFast(patternNid))
                .call(() -> PrimitiveData.nid(semanticUuid));

        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .leastSignificantBits(semanticUuid.getLeastSignificantBits())
                .mostSignificantBits(semanticUuid.getMostSignificantBits())
                .nid(semanticNid)
                .patternNid(patternNid)
                .referencedComponentNid(referencedComponentNid)
                .versions(versionRecords).build();
        versionRecords.add(new SemanticVersionRecord(semanticRecord, stampVersion.stampNid(), fields)).build();
        return semanticRecord;
    }

    @Override
    public byte[] getBytes() {
        return EntityRecordFactory.getBytes(this);
    }

    @Override
    public String entityToStringExtras() {

            String patternUuidStr = EntityHandle.get(patternNid).isPresent()
            ? PrimitiveData.publicId(patternNid).toString()
            : "not found";

        String refCompUuidStr = EntityHandle.get(referencedComponentNid).isPresent()
                ? PrimitiveData.publicId(referencedComponentNid).toString()
                : "not found";

        return "of pattern: «" + PrimitiveData.text(patternNid) +
                " <" +
                patternNid +
                "> " + patternUuidStr +
                "», rc: «" + PrimitiveData.text(referencedComponentNid) +
                " <" +
                referencedComponentNid +
                "> " + refCompUuidStr +
                "»,";
    }

    public boolean deepEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticRecord that = (SemanticRecord) o;
        return mostSignificantBits == that.mostSignificantBits &&
                leastSignificantBits == that.leastSignificantBits &&
                nid == that.nid && patternNid == that.patternNid &&
                referencedComponentNid == that.referencedComponentNid &&
                Arrays.equals(additionalUuidLongs, that.additionalUuidLongs) &&
                versions.equals(that.versions);
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

    public SemanticAnalogueBuilder with(SemanticVersionRecord versionToAdd) {
        return analogueBuilder().add(versionToAdd);
    }

    public SemanticAnalogueBuilder analogueBuilder() {
        RecordListBuilder<SemanticVersionRecord> versionRecords = RecordListBuilder.make();
        SemanticRecord semanticRecord = new SemanticRecord(mostSignificantBits, leastSignificantBits, additionalUuidLongs,
                nid, patternNid, referencedComponentNid, versionRecords);
        for (SemanticVersionRecord version : versions) {
            versionRecords.add(new SemanticVersionRecord(semanticRecord, version.stampNid(), version.fieldValues()));
        }
        return new SemanticAnalogueBuilder(semanticRecord, versionRecords);
    }

    public SemanticAnalogueBuilder without(SemanticVersionRecord versionToAdd) {
        return analogueBuilder().remove(versionToAdd);
    }

    public PublicId publicId() {
        return PublicIds.of(asUuidArray());
    }

}
