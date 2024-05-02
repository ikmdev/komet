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
package dev.ikm.komet.amplify.lidr.om;

import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityProxy;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record LidrRecord(PublicId lidrRecordId,
                         PublicId testPerformedId,
                         PublicId dataResultsTypeId,
                         AnalyteRecord analyte,
                         Set<TargetRecord> targets,
                         Set<SpecimenRecord> specimens,
                         Set<ResultConformanceRecord> resultConformances) {

    public static final int IDX_TEST_PERFORMED = 0;
    public static final int IDX_DATA_RESULTS_TYPE = 1;
    public static final int IDX_ANALYTES = 2;
    public static final int IDX_TARGETS = 3;
    public static final int IDX_SPECIMENS = 4;
    public static final int IDX_RESULT_CONFORMANCES = 5;
    public static final EntityProxy.Pattern LIDR_RECORD_PATTERN = EntityProxy.Pattern.make(null, UUID.fromString("c3d52f47-0565-5cfb-9b0b-d7501a33b35d"));


    public static LidrRecord make(PublicId lidrRecordId) {
        Optional<Entity> lidrRecordEntity = EntityService.get().getEntity(lidrRecordId.asUuidArray());
        if (lidrRecordEntity.isEmpty()) {
            throw new IllegalArgumentException("PublicId " + lidrRecordId + " is not in database.");
        }
        return make(lidrRecordEntity.get());
    }

    public static LidrRecord make(Entity lidrRecordEntity) {
        if (!isLidrRecord(lidrRecordEntity)) {
            throw new IllegalArgumentException("Entity is not associated with a LIDR Record: " + lidrRecordEntity);
        }
        SemanticEntityVersion lidrRecordSemanticVersion = (SemanticEntityVersion) lidrRecordEntity.versions().get(0);
        ImmutableList<Object> vals = lidrRecordSemanticVersion.fieldValues();

        PublicId testPerformedId = (PublicId) vals.get(IDX_TEST_PERFORMED);
        PublicId dataResultsTypeId = (PublicId) vals.get(IDX_DATA_RESULTS_TYPE);
        PublicId analyteId = (PublicId) vals.get(IDX_ANALYTES);
        Set<PublicId> targetIds = ((IntIdSet) vals.get(IDX_TARGETS)).mapToSet(PrimitiveData::publicId);
        Set<PublicId> specimenIds = ((IntIdSet) vals.get(IDX_SPECIMENS)).mapToSet(PrimitiveData::publicId);
        Set<PublicId> resultConformanceIds = ((IntIdSet) vals.get(IDX_RESULT_CONFORMANCES)).mapToSet(PrimitiveData::publicId);

        AnalyteRecord analyte = AnalyteRecord.make(analyteId);
        Set<TargetRecord> targets = targetIds.stream().map(TargetRecord::make).collect(Collectors.toSet());
        Set<SpecimenRecord> specimens = specimenIds.stream().map(SpecimenRecord::make).collect(Collectors.toSet());
        Set<ResultConformanceRecord> resultConformances = resultConformanceIds.stream().map(ResultConformanceRecord::make).collect(Collectors.toSet());

        return new LidrRecord(lidrRecordEntity.publicId(), testPerformedId, dataResultsTypeId, analyte,
                targets, specimens, resultConformances);
    }

    public static boolean isLidrRecord(Entity entity) {
        if (entity instanceof SemanticEntity semantic) {
            return semantic.pattern().equals(LIDR_RECORD_PATTERN);
        }
        return false;
    }

    public SemanticEntity toSemantic(Entity referencedComponent, StampEntity stampEntity) {
        MutableList<Object> lidrRecordFields = Lists.mutable.empty();

        Set<PublicId> targetIds = this.targets.stream().map(TargetRecord::targetId).collect(Collectors.toSet());
        Set<PublicId> specimenIds = this.specimens.stream().map(SpecimenRecord::specimenId).collect(Collectors.toSet());
        Set<PublicId> resultConformanceIds = this.resultConformances.stream().map(ResultConformanceRecord::resultConformanceId).collect(Collectors.toSet());

        lidrRecordFields.add(this.testPerformedId);
        lidrRecordFields.add(this.dataResultsTypeId);
        lidrRecordFields.add(this.analyte.analyteId());
        lidrRecordFields.add(targetIds);
        lidrRecordFields.add(specimenIds);
        lidrRecordFields.add(resultConformanceIds);

        UUID semanticUuid = UUID.randomUUID();

        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();

        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .nid(PrimitiveData.nid(semanticUuid))
                .mostSignificantBits(semanticUuid.getMostSignificantBits())
                .leastSignificantBits(semanticUuid.getLeastSignificantBits())
                .additionalUuidLongs(null)
                .patternNid(LIDR_RECORD_PATTERN.nid())
                .referencedComponentNid(referencedComponent.nid())
                .versions(versions.toImmutable())
                .build();

        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(stampEntity.nid())
                .fieldValues(lidrRecordFields.toImmutable())
                .build());

        return SemanticRecordBuilder
                .builder(semanticRecord)
                .versions(versions.toImmutable()).build();
    }

}
