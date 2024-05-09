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
package dev.ikm.komet.amplify.data.persistence;

import dev.ikm.komet.amplify.data.om.PatternDetail;
import dev.ikm.komet.amplify.data.om.PatternFieldDetail;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PatternWriter implements Writer {

    public final PublicId stamp;

    public PatternWriter(PublicId stamp) {
        this.stamp = stamp;
    }

    public void write(PublicId pattern,
                      PatternDetail patternDetail,
                      List<PatternFieldDetail> patternFieldDetails){
        //Create empty version list
        RecordListBuilder<PatternVersionRecord> versions = RecordListBuilder.make();

        //Pull out primordial UUID from PublicId
        UUID primordialUUID = pattern.asUuidArray()[0];

        //Process additional UUID longs from PublicId
        long[] additionalLongs = createAdditionalLongs(pattern);

        //Assign nids for Pattern component Concepts
        int patternNid = EntityService.get().nidForPublicId(pattern);
        int meaningConceptNid = EntityService.get().nidForPublicId(patternDetail.meaning());
        int purposeConceptNid = EntityService.get().nidForPublicId(patternDetail.purpose());
        int stampNid = EntityService.get().nidForPublicId(stamp);

        //Create Pattern Chronology
        PatternRecord patternRecord = PatternRecordBuilder.builder()
                .nid(patternNid)
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .versions(versions.toImmutable())
                .build();

        //Create individual pattern definitions
        final AtomicInteger patternIndex = new AtomicInteger(0);
        MutableList<FieldDefinitionRecord> fieldDefinitions = Lists.mutable.empty();
        patternFieldDetails.forEach(patternFieldDetail -> {
            int meaningNid = EntityService.get().nidForPublicId(patternFieldDetail.meaning());
            int purposeNid = EntityService.get().nidForPublicId(patternFieldDetail.purpose());
            int dataTypeNid = EntityService.get().nidForPublicId(patternFieldDetail.dataType());

            FieldDefinitionRecord fieldDefinitionRecord = FieldDefinitionRecordBuilder.builder()
                    .patternNid(patternNid)
                    .meaningNid( meaningNid)
                    .purposeNid(purposeNid)
                    .dataTypeNid(dataTypeNid)
                    .indexInPattern(patternIndex.getAndIncrement())
                    .patternVersionStampNid(stampNid)
                    .build();
            fieldDefinitions.add(fieldDefinitionRecord);
        });

        //Create Pattern Version
        versions.add(PatternVersionRecordBuilder.builder()
                .chronology(patternRecord)
                .stampNid(stampNid)
                .semanticMeaningNid(meaningConceptNid)
                .semanticPurposeNid(purposeConceptNid)
                .fieldDefinitions(fieldDefinitions.toImmutable())
                .build());

        //Rebuild the Pattern with the now populated version data
        PatternEntity<? extends PatternEntityVersion> patternEntity = PatternRecordBuilder.builder(patternRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(patternEntity);
    }
}
