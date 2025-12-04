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
package dev.ikm.tinkar.integration.snomed.language;

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
import dev.ikm.tinkar.integration.snomed.core.MockEntity;
import dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.MockDataType.ENTITYREF;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.ACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DESCRIPTION_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.IDENTIFIER_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.INACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.LANGUAGE_ACCEPTABILITY_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_AUTHOR;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_IDENTIFIER;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;

public class SnomedToLanguageTransform {
    protected static class Language {
        String id;
        long effectiveTime;
        int active;
        String moduleId;
        String refsetId;
        String referencedComponentId;
        String acceptabilityId;

        public Language(String input)
        {
            String[] row = input.split(("\t"));
            this.id = row[0];
            this.effectiveTime = Long.parseLong(row[1]);
            this.active = Integer.parseInt(row[2]);
            this.moduleId = row[3];
            this.refsetId = row[4];
            this.referencedComponentId = row[5];
            this.acceptabilityId = row[6];
        }

        @Override public String toString() {
            return id+effectiveTime+active+moduleId+refsetId+referencedComponentId+acceptabilityId;
        }

    }

    /*
    Generating Semantic Chronology for LanguageFile
    */

    public static UUID getStampUUID(String row) {
        Language textLanguage = new Language(row);
        UUID nameSpaceUUID = TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;

        UUID stampUUID = UuidT5Generator.get(nameSpaceUUID, textLanguage.toString());
        MockEntity.populateMockData(stampUUID.toString(), ENTITYREF);
        return stampUUID;
    }


    public static StampRecord createStampChronology(String row){

        UUID stampUUID = getStampUUID(row);

        StampRecord record = StampRecordBuilder.builder()
                .mostSignificantBits(stampUUID.getMostSignificantBits())
                .leastSignificantBits(stampUUID.getLeastSignificantBits())
                .nid(EntityService.get().nidForUuids(stampUUID))
                .versions(RecordListBuilder.make())
                .build();

        StampVersionRecord versionsRecord = createStampVersion(record, row);

        return StampRecordBuilder.builder(record)
                .versions(record.versions().newWith(versionsRecord))
                .build();

    }

   public static StampVersionRecord createStampVersion(StampRecord record, String row)
    {
        Language textLanguageEntity = new Language(row);

        StampVersionRecordBuilder recordBuilder = StampVersionRecordBuilder.builder();
        if (textLanguageEntity.active == 1){
            recordBuilder.stateNid(EntityService.get().nidForUuids(ACTIVE));
        } else {
            recordBuilder.stateNid(EntityService.get().nidForUuids(INACTIVE));
        }

        return recordBuilder
                .chronology(record)
                .time(Instant.ofEpochSecond(textLanguageEntity.effectiveTime).toEpochMilli())
                .authorNid(EntityService.get().nidForUuids(SNOMED_CT_AUTHOR))
                .moduleNid(EntityService.get().nidForUuids(UuidT5Generator.get(SNOMED_CT_NAMESPACE, textLanguageEntity.moduleId)))
                .pathNid(EntityService.get().nidForUuids(DEVELOPMENT_PATH))
                .build();

    }

   /*
    Identifier & Language acceptability Semantic Chronology and Version
     */

    // creates identifier semantic for concept
    public static SemanticRecord createLanguageIdentifierSemantic(String row) {
        UUID patternUUID = getIdentifierPatternUUID();
        UUID semanticUUID = getIdentifierSemanticUUID(row);
        UUID referencedComponentUUID = getIdentifierReferenceComponentUUID(row);
        SemanticRecord identifierSemanticRecord = createSemantic(patternUUID, semanticUUID, referencedComponentUUID);
        SemanticVersionRecord semanticVersionRecord = createIdentifierSemanticVersion(identifierSemanticRecord, row);

        return SemanticRecordBuilder.builder(identifierSemanticRecord)
                .versions(identifierSemanticRecord.versions().newWith(semanticVersionRecord))
                .build();
    }

    // creates definition status semantic for concept
    public static SemanticRecord createLanguageAceeptabilitySemantic(String row) {
        UUID patternUUID = getLanguageAcceptabilityPatternUUID();
        UUID semanticUUID = getLanguageAcceptabilitySemanticUUID(row);
        UUID referencedComponentUUID = getLanguageAcceptabilityReferenceComponentUUID(row);
        SemanticRecord languageAcceptabilitySemanticRecord = createSemantic(patternUUID, semanticUUID, referencedComponentUUID);
        SemanticVersionRecord semanticVersionRecord = createLanguageAcceptabilitySemanticVersion(languageAcceptabilitySemanticRecord, row);

        return SemanticRecordBuilder.builder(languageAcceptabilitySemanticRecord)
                .versions(languageAcceptabilitySemanticRecord.versions().newWith(semanticVersionRecord))
                .build();
    }

    // creates identifier semantic version for identifier semantic for concept
    public static SemanticVersionRecord createIdentifierSemanticVersion(SemanticRecord semanticRecord, String row) {
        Language textLanguage = new Language(row);
        Object[] fields = new Object[] {textLanguage.id, SNOMED_CT_IDENTIFIER};
        return createSemanticVersion(fields, row, semanticRecord);
    }

    // creates definition status semantic version for definition status semantic for concept
    public static SemanticVersionRecord createLanguageAcceptabilitySemanticVersion(SemanticRecord semanticRecord, String row) {
        Language textLanguage = new Language(row);
        Object[] fields = new Object[] {UuidT5Generator.get(SNOMED_CT_NAMESPACE, textLanguage.acceptabilityId)};
        return createSemanticVersion(fields, row, semanticRecord);
    }

    // method to create (identifier and definition status) semantic for concept
    private static SemanticRecord createSemantic(UUID patternUUID, UUID semanticUUID, UUID referencedComponentUUID) {
        return SemanticRecordBuilder.builder()
                .versions(RecordListBuilder.make())
                .nid(EntityService.get().nidForUuids(semanticUUID))
                .referencedComponentNid(EntityService.get().nidForUuids(referencedComponentUUID))
                .patternNid(EntityService.get().nidForUuids(patternUUID))
                .leastSignificantBits(semanticUUID.getLeastSignificantBits())
                .mostSignificantBits(semanticUUID.getMostSignificantBits())
                .build();
    }

    // method to create (identifier and definition status) semantic version for concept
    private static SemanticVersionRecord createSemanticVersion(Object[] fields, String row, SemanticRecord semanticRecord) {
        StampRecord stampRecord = createStampChronology(row);
        return SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(stampRecord.nid())
                .fieldValues(RecordListBuilder.make().newWithAll(List.of(fields)))
                .build();
    }

    // generate and return identifier semantic UUID
    public static UUID getIdentifierSemanticUUID(String row) {
        Language textLanguage = new Language(row);
        UUID semanticUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, IDENTIFIER_PATTERN.toString() + textLanguage.id);
        MockEntity.populateMockData(semanticUUID.toString(), ENTITYREF);
        return semanticUUID;
    }

    // generate and return identifier semantic pattern UUID
    public static UUID getIdentifierPatternUUID() {
        MockEntity.populateMockData(IDENTIFIER_PATTERN.toString(), ENTITYREF);
        return IDENTIFIER_PATTERN;
    }

    // generate and return definition status semantic pattern UUID
    public static UUID getLanguageAcceptabilityPatternUUID() {
        MockEntity.populateMockData(LANGUAGE_ACCEPTABILITY_PATTERN.toString(), ENTITYREF);
        return LANGUAGE_ACCEPTABILITY_PATTERN;
    }

    // generate and return reference component UUID
    public static UUID getIdentifierReferenceComponentUUID(String row) {
        Language textLanguage = new Language(row);
        UUID referenceComponentUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, LANGUAGE_ACCEPTABILITY_PATTERN.toString() +textLanguage.id);
        MockEntity.populateMockData(referenceComponentUUID.toString(), ENTITYREF);
        return referenceComponentUUID;
    }

    // generate and return definition status semantic UUID
    public static UUID getLanguageAcceptabilitySemanticUUID(String row) {
        Language textLanguage = new Language(row);
        UUID definitionStatusSemanticUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, LANGUAGE_ACCEPTABILITY_PATTERN.toString() + textLanguage.id);
        MockEntity.populateMockData(definitionStatusSemanticUUID.toString(), ENTITYREF);
        return definitionStatusSemanticUUID;
    }

    // generate and return reference component UUID
    public static UUID getLanguageAcceptabilityReferenceComponentUUID(String row) {
        Language textLanguage = new Language(row);
        UUID referenceComponentUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, DESCRIPTION_PATTERN.toString() +textLanguage.referencedComponentId);
        MockEntity.populateMockData(referenceComponentUUID.toString(), ENTITYREF);
        return referenceComponentUUID;
    }

}
