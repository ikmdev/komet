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
package dev.ikm.tinkar.integration.snomed.core;

import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticRecordBuilder;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecordBuilder;
import dev.ikm.tinkar.entity.StampRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.SNOMED_CT_IDENTIFIER_UUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.getPatternTypeUUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.getReferenceComponentUUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.getSemanticUUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.loadSnomedFile;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTStampChronology.createSTAMPChronology;

public class SnomedCTIdentifierSemantic {

    public static List<SemanticRecord> createIdentifierSemantics(Object test, String snomedCTDataFile, String pattern){
        SnomedCTData snomedCTData = loadSnomedFile(test.getClass(), snomedCTDataFile);
        List<StampRecord> stampRecords = new ArrayList<>();
        List<SemanticRecord> semanticRecords = new ArrayList<>();
        int totalValueRows = snomedCTData.getTotalRows();
        for(int rowNumber =0 ; rowNumber < totalValueRows ; rowNumber++ ){
            SemanticRecord semanticRecord = createIdentifierSemantic(pattern, rowNumber, snomedCTData);
            StampRecord stampRecord = createSTAMPChronology(rowNumber, snomedCTData);
            stampRecords.add(stampRecord);
            semanticRecords.add(semanticRecord);
        }
        return semanticRecords;
    }

    private static SemanticRecord createIdentifierSemantic(String patternType, int rowNumber, SnomedCTData snomedCTData) {

        UUID semanticUUID = getSemanticUUID(SNOMED_CT_IDENTIFIER_UUID, snomedCTData.getID(rowNumber));
        UUID patternTypeUUID = getPatternTypeUUID(patternType);
        UUID referenceComponenetUUID = getReferenceComponentUUID(patternTypeUUID, snomedCTData.getID(rowNumber));
        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .mostSignificantBits(semanticUUID.getMostSignificantBits())
                .leastSignificantBits(semanticUUID.getLeastSignificantBits())
                .patternNid(EntityService.get().nidForUuids(patternTypeUUID))
                .referencedComponentNid(EntityService.get().nidForUuids(referenceComponenetUUID))
                .nid(EntityService.get().nidForUuids(semanticUUID))
                .versions(RecordListBuilder.make())
                .build();
        SemanticVersionRecord versionsRecord = createIdentifierSemanticVersion(semanticRecord,rowNumber, snomedCTData);
        return semanticRecord.withVersions(RecordListBuilder.make().newWith(versionsRecord));
    }

    private static SemanticVersionRecord createIdentifierSemanticVersion(SemanticRecord semanticRecord, int rowNumber, SnomedCTData snomedCTData) {
        Object[] fields = { snomedCTData.getID(rowNumber), SNOMED_CT_IDENTIFIER_UUID };
        StampRecord stampRecord  = createSTAMPChronology(rowNumber,snomedCTData);
        SemanticVersionRecordBuilder semanticVersionRecordBuilder = SemanticVersionRecordBuilder.builder();
        return semanticVersionRecordBuilder
                .chronology(semanticRecord)
                .stampNid(EntityService.get().nidForUuids(stampRecord.asUuidArray()))
                .fieldValues(RecordListBuilder.make().newWith(fields))
                .build();
    }


}
