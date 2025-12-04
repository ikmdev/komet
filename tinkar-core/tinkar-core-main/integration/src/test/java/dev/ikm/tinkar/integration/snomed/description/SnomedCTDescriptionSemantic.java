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
import dev.ikm.tinkar.integration.snomed.core.SnomedCTData;
import org.eclipse.collections.api.list.MutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.MockEntity.getEntity;
import static dev.ikm.tinkar.integration.snomed.core.MockEntity.putEntity;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTConstants.SNOMED_CT_NAMESPACE_UUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.getPatternTypeUUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.getReferenceComponentUUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.getSemanticUUID;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTHelper.loadSnomedFile;
import static dev.ikm.tinkar.integration.snomed.core.SnomedCTStampChronology.createSTAMPChronology;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.ENGLISH_LANGUAGE;

public class SnomedCTDescriptionSemantic {


    /**
     * This method  will take in test object class which is used to get reference to provided snomendCTDataFile name
     * The pattern is the description pattern that will be used to generate Semantic Description
     * for example: Description Pattern.
     * This method will load the file and parse each line of the file to return List of Semantic records.
     * @Param test
     * @param snomedCTDataFile
     * @param  pattern
     * @Return List
     * */
    public static List<SemanticRecord> createDescriptionSemantics(Object test, String snomedCTDataFile, String pattern){
        SnomedCTData snomedCTData = loadSnomedFile(test.getClass(), snomedCTDataFile);
        List<SemanticRecord> semanticRecords = new ArrayList<>();
        int totalValueRows = snomedCTData.getTotalRows();
        for(int rowNumber =0 ; rowNumber < totalValueRows ; rowNumber++ ){
            SemanticRecord semanticRecord = createDescriptionSemantic(pattern, rowNumber, snomedCTData);
            UUID mappedUUID = generateUniqueUUID(snomedCTData, rowNumber, pattern);
            SemanticRecord existingSemanticRecord = (SemanticRecord) getEntity(mappedUUID);
            if(existingSemanticRecord == null){
                putEntity(mappedUUID, semanticRecord);
                semanticRecords.add(semanticRecord);
            }else{
                SemanticRecord updatedSemanticRecord = updateSemanticVersions(semanticRecord, mappedUUID);
                semanticRecords.remove(existingSemanticRecord);
                putEntity(mappedUUID, updatedSemanticRecord);
                semanticRecords.add(updatedSemanticRecord);
            }
         }
        return semanticRecords;
    }

    /**
     * This method will create Description Semantic and returns SemanticRecord.
     * @Param String
     * @Param  int
     * @Param SnomedCTData
     * @Return SemanticRecord
     * */
    private static SemanticRecord createDescriptionSemantic(String patternType, int rowNumber, SnomedCTData snomedCTData) {
        UUID patternUUID =  getPatternTypeUUID(patternType);
        UUID semanticUUID = getSemanticUUID(patternUUID, snomedCTData.getID(rowNumber));
        UUID referenceComponenetUUID = getReferenceComponentUUID(patternUUID, snomedCTData.getID(rowNumber));
        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .mostSignificantBits(semanticUUID.getMostSignificantBits())
                .leastSignificantBits(semanticUUID.getLeastSignificantBits())
                .patternNid(EntityService.get().nidForUuids(patternUUID))
                .referencedComponentNid(EntityService.get().nidForUuids(referenceComponenetUUID))
                .nid(EntityService.get().nidForUuids(semanticUUID))
                .versions(RecordListBuilder.make())
                .build();
        SemanticVersionRecord versionsRecord = createDescriptionSemanticVersion(semanticRecord,rowNumber, snomedCTData);
        return semanticRecord.withVersions(RecordListBuilder.make().newWith(versionsRecord));
    }

    private static SemanticVersionRecord createDescriptionSemanticVersion(SemanticRecord semanticRecord, int rowNumber, SnomedCTData snomedCTData) {
        Object[] fields = getDescriptionFieldValues(snomedCTData, rowNumber);
        StampRecord stampRecord  = createSTAMPChronology(rowNumber,snomedCTData);
        SemanticVersionRecordBuilder semanticVersionRecordBuilder = SemanticVersionRecordBuilder.builder();
        return semanticVersionRecordBuilder
                .chronology(semanticRecord)
                .stampNid(EntityService.get().nidForUuids(stampRecord.asUuidArray()))
                .fieldValues(RecordListBuilder.make().newWith(fields))
                .build();
    }

    private static Object[] getDescriptionFieldValues(SnomedCTData snomedCTData, int rowNumber) {
        String languageCode = snomedCTData.toString(rowNumber, "languageCode");
        String term = snomedCTData.toString(rowNumber, "term");
        String caseSignificanceId = snomedCTData.toString(rowNumber, "caseSignificanceId");
        String typeId = snomedCTData.toString(rowNumber, "typeId");

        UUID languageUUID = null;
        if (languageCode.equals("en")){
            languageUUID = ENGLISH_LANGUAGE;
        }
        UUID caseSignificanceUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE_UUID, caseSignificanceId);
        UUID typeUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE_UUID, typeId);
        Object[] fields = {languageUUID,term,caseSignificanceUUID,typeUUID };
        return fields;
    }

    private static SemanticRecord updateSemanticVersions(SemanticRecord semanticRecord,UUID mappedUUID) {
        SemanticRecord existingSemanticRecord = (SemanticRecord) getEntity(mappedUUID);
        MutableList<SemanticVersionRecord> allVersions = existingSemanticRecord.versions().toList();
        allVersions.addAllIterable(semanticRecord.versions());
        return  existingSemanticRecord.withVersions(allVersions.toImmutable());
    }

    private static UUID generateUniqueUUID(SnomedCTData snomedCTData, int rowNumber, String pattern) {
        Object [] obj = getDescriptionFieldValues(snomedCTData, rowNumber);
        String valueFields = Arrays.toString(obj);
        UUID patternUUID = UuidT5Generator.get(pattern);
        UUID semanticUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE_UUID, String.valueOf(patternUUID) + snomedCTData.toString(rowNumber, "id"));
        return UuidT5Generator.get(String.valueOf(semanticUUID) + valueFields);
    }


}
