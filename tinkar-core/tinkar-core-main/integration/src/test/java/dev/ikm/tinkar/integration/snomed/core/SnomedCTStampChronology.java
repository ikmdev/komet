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

import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.StampRecordBuilder;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.entity.StampVersionRecordBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.MockDataType.ENTITYREF;
import static dev.ikm.tinkar.integration.snomed.core.MockDataType.MODULE;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.ACTIVE_UUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.DEVELOPMENT_PATH_UUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.INACTIVE_UUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.SNOMED_CT_AUTHOR_UUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.SNOMED_CT_NAMESPACE_UUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.loadSnomedFile;

public class SnomedCTStampChronology {

    public static List<StampRecord> createSTAMPChronologyForAllRecords(Object test, String snomedCTDataFile) {
        SnomedCTData snomedCTData = loadSnomedFile(test.getClass(), snomedCTDataFile);
        List<StampRecord> stampRecords = new ArrayList<>();
        int totalValueRows = snomedCTData.getTotalRows();
        for(int rowNumber =0 ; rowNumber < totalValueRows ; rowNumber++ ){
            StampRecord stampRecord = createSTAMPChronology(rowNumber, snomedCTData);
            stampRecords.add(stampRecord);
        }
        return stampRecords;
    }

    public static StampRecord createSTAMPChronology(int rowNumber, SnomedCTData snomedCTData) {
        UUID stampUUID = getStampUUID(snomedCTData.toString(rowNumber));
        StampRecord record = StampRecordBuilder.builder()
                .mostSignificantBits(stampUUID.getMostSignificantBits())
                .leastSignificantBits(stampUUID.getLeastSignificantBits())
                .nid(EntityService.get().nidForUuids(stampUUID))
                .versions(RecordListBuilder.make())
                .build();
        StampVersionRecord versionsRecord = createSTAMPVersion(record,rowNumber, snomedCTData);
        return record.withVersions(RecordListBuilder.make().newWith(versionsRecord));
    }

    public static StampVersionRecord createSTAMPVersion(StampRecord record, int rowNumber, SnomedCTData snomedCTData) {
        StampVersionRecordBuilder recordBuilder = StampVersionRecordBuilder.builder();
        if(snomedCTData.getActive(rowNumber) == 1){
            recordBuilder.stateNid(EntityService.get().nidForUuids(ACTIVE_UUID));
        }
        else if(snomedCTData.getActive(rowNumber) == 0){
            recordBuilder.stateNid(EntityService.get().nidForUuids(INACTIVE_UUID));
        }
        return recordBuilder
                .chronology(record)
                .time(snomedCTData.getEffectiveTime(rowNumber))
                .authorNid(EntityService.get().nidForUuids(SNOMED_CT_AUTHOR_UUID))
                .moduleNid(EntityService.get().nidForUuids(getSnomedTextModuleId(snomedCTData,rowNumber)))
                .pathNid(EntityService.get().nidForUuids(DEVELOPMENT_PATH_UUID))
                .build();
    }

    public static UUID getStampUUID(String uniqueString ) {
        UUID stampUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE_UUID, uniqueString);
        MockEntity.populateMockData(stampUUID.toString(), ENTITYREF);
        return stampUUID;
    }

    public static UUID getSnomedTextModuleId(SnomedCTData snomedCTData, int rowNumber){
        UUID moduleUUID = UuidT5Generator.get(snomedCTData.getNamespaceUUID(), snomedCTData.getModuleId(rowNumber));
        MockEntity.populateMockData(moduleUUID.toString(), MODULE);
        return moduleUUID;
    }
}
