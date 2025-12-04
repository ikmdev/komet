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
package dev.ikm.tinkar.integration.snomed.description;

import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticRecordBuilder;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecordBuilder;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.StampRecordBuilder;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.entity.StampVersionRecordBuilder;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.ACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DESCRIPTION_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.IDENTIFIER_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.INACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_AUTHOR;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_IDENTIFIER;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;

public class SnomedToEntityIdentifier {
    public static final int ID_INDEX = 0;
    public static final int EFFECTIVE_TIME_INDEX = 1;
    public static final int ACTIVE_INDEX = 2;
    public static final int DEV_PATH_INDEX = 3;
    public static final int CONCEPT_ID_INDEX = 4;
    public static final int LANGUAGE_CODE_INDEX = 5;
    public static final int TYPE_ID_INDEX = 6;
    public static final int TERM_INDEX = 7;
    public static final int CASE_SIGNIFICANCE_ID_INDEX = 8;

    public SemanticRecord createIdentifierSemantic(String row){
        String[] values = splitRow(row);

        UUID patternUUID = IDENTIFIER_PATTERN;
        UUID semanticUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE,patternUUID.toString()+values[ID_INDEX]);
        UUID referencedComponentUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE,DESCRIPTION_PATTERN.toString()+values[ID_INDEX]);

        SemanticRecord record = SemanticRecordBuilder.builder()
                .leastSignificantBits(semanticUUID.getLeastSignificantBits())
                .mostSignificantBits(semanticUUID.getMostSignificantBits())
                .nid(EntityService.get().nidForUuids(semanticUUID))
                .patternNid(EntityService.get().nidForUuids(patternUUID))
                .referencedComponentNid(EntityService.get().nidForUuids(referencedComponentUUID))
                .versions(RecordListBuilder.make().build())
                .build();

        SemanticVersionRecord versionRecord = createIdentifierSemanticVersion(semanticUUID, record, row);

        return record.withVersions(RecordListBuilder.make().newWith(versionRecord));
    }

    SemanticVersionRecord createIdentifierSemanticVersion(UUID semanticUUID, SemanticRecord record, String row){

        String[] values = splitRow(row);

        Object[] fields = {
                values[ID_INDEX],
                EntityService.get().nidForUuids(SNOMED_CT_IDENTIFIER)};

        return SemanticVersionRecordBuilder.builder()
                .chronology(record)
                .stampNid(createSTAMPChronology(row).nid())
                .fieldValues(RecordListBuilder.make().newWithAll(Arrays.asList(fields)))
                .build();
    }

    public StampRecord createSTAMPChronology(String row){

        UUID stampUUID = generateStampUuid(row);
        StampRecord record = StampRecordBuilder.builder()
                .mostSignificantBits(stampUUID.getMostSignificantBits())
                .leastSignificantBits(stampUUID.getLeastSignificantBits())
                .nid(EntityService.get().nidForUuids(stampUUID))
                .versions(RecordListBuilder.make())
                .build();

        StampVersionRecord versionsRecord = createSTAMPVersion(stampUUID, record, row);

        return record.withVersions(RecordListBuilder.make().newWith(versionsRecord));

    }

    StampVersionRecord createSTAMPVersion(UUID stampUUID, StampRecord record, String row){
        String[] values = row.split("\t");

        StampVersionRecordBuilder recordBuilder = StampVersionRecordBuilder.builder();
        if(Integer.parseInt(values[ACTIVE_INDEX]) == 1){
            recordBuilder.stateNid(EntityService.get().nidForUuids(ACTIVE));
        }
        else {
            recordBuilder.stateNid(EntityService.get().nidForUuids(INACTIVE));
        }
        return recordBuilder
                .chronology(record)
                .time(LocalDate.parse(values[EFFECTIVE_TIME_INDEX], DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(12,0,0).toInstant(ZoneOffset.UTC).toEpochMilli())
                .authorNid(EntityService.get().nidForUuids(SNOMED_CT_AUTHOR))
                .moduleNid(EntityService.get().nidForUuids(UuidT5Generator.get(SNOMED_CT_NAMESPACE,values[DEV_PATH_INDEX])))
                .pathNid(EntityService.get().nidForUuids(DEVELOPMENT_PATH))
                .build();

    }

    private UUID generateStampUuid(String row) {
        return UuidT5Generator.get(SNOMED_CT_NAMESPACE, row.replaceAll("\t",""));
    }

    private String[] splitRow(String row) {
        return row.split("\t");
    }


}
