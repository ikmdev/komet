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
package dev.ikm.tinkar.integration.snomed.concept;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptRecordBuilder;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecordBuilder;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.MockDataType.ENTITYREF;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.ACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DEFINITION_STATUS_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.IDENTIFIER_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.INACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_AUTHOR;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_IDENTIFIER;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.loadSnomedFile;

public class SnomedTextToConcept {
    protected static class Concept {
        String id;
        long effectiveTime;
        int active;
        String moduleId;
        String definitionStatusId;

        public Concept(String input)
        {
            String[] row = input.split(("\t"));
            this.id = row[0];
            this.effectiveTime = Long.parseLong(row[1]);
            this.active = Integer.parseInt(row[2]);
            this.moduleId = row[3];
            this.definitionStatusId = row[4];
        }

        @Override public String toString() {
            return id+effectiveTime+active+moduleId+definitionStatusId;
        }

    }

    /*
    Generating Stamp Chronology for Concepts
    */

    public static UUID getStampUUID(String row) {
        Concept textConcept = new Concept(row);
        UUID nameSpaceUUID = TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;

        UUID stampUUID = UuidT5Generator.get(nameSpaceUUID, textConcept.toString());
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
        Concept textConceptEntity = new Concept(row);

        StampVersionRecordBuilder recordBuilder = StampVersionRecordBuilder.builder();
        if (textConceptEntity.active == 1){
            recordBuilder.stateNid(EntityService.get().nidForUuids(ACTIVE));
        } else {
            recordBuilder.stateNid(EntityService.get().nidForUuids(INACTIVE));
        }

        return recordBuilder
                .chronology(record)
                .time(Instant.ofEpochSecond(textConceptEntity.effectiveTime).toEpochMilli())
                .authorNid(EntityService.get().nidForUuids(SNOMED_CT_AUTHOR))
                .moduleNid(EntityService.get().nidForUuids(UuidT5Generator.get(SNOMED_CT_NAMESPACE, textConceptEntity.moduleId)))
                .pathNid(EntityService.get().nidForUuids(DEVELOPMENT_PATH))
                .build();

    }

    /*
    Generating Concepts for Concept File
    */

    public static UUID getConceptUUID(String row) {
        Concept textConcept = new Concept(row);
        UUID conceptUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, textConcept.id);
        MockEntity.populateMockData(conceptUUID.toString(), ENTITYREF);
        return conceptUUID;
    }

    public static ConceptRecord createConceptChronology(String row) {
        // Get ConceptUUID from row
        UUID conceptUUID = getConceptUUID(row);
        ConceptRecord existingConceptRecord = (ConceptRecord)MockEntity.getEntity(conceptUUID);

        // Create record builder based on conceptUUID (if it exists or else it creates new)
        ConceptRecordBuilder recordBuilder = existingConceptRecord == null ?
                ConceptRecordBuilder.builder().versions(RecordListBuilder.make()) :
                ConceptRecordBuilder.builder(existingConceptRecord).versions(existingConceptRecord.versions());

        recordBuilder.leastSignificantBits(conceptUUID.getLeastSignificantBits())
                .mostSignificantBits(conceptUUID.getMostSignificantBits())
                .nid(EntityService.get().nidForUuids(conceptUUID));

        ConceptVersionRecord conceptVersionRecord = createConceptVersion(recordBuilder.build(), row);

        ConceptRecord newConceptRecord = ConceptRecordBuilder
                .builder(recordBuilder.build())
                .versions(recordBuilder.versions().newWith(conceptVersionRecord))
                .build();

        MockEntity.putEntity(conceptUUID, newConceptRecord);

        return newConceptRecord;
    }

    public static ConceptVersionRecord createConceptVersion(ConceptRecord conceptRecord, String row) {
        StampRecord stampRecord = createStampChronology(row);

        return ConceptVersionRecordBuilder.builder()
                .chronology(conceptRecord)
                .stampNid(stampRecord.nid())
                .build();
    }

    public static List<ConceptRecord> createConceptFromMultipleVersions(Class<?> aClass, String fileName) {
        Set<PublicId> uniquePublicIds = new HashSet<>();
        List<ConceptRecord> conceptRecords = new ArrayList<>();

        List<String> rows = loadSnomedFile(aClass, fileName);
        for(String row: rows) {
            ConceptRecord conceptRecord = createConceptChronology(row);
            if (uniquePublicIds.contains(conceptRecord.publicId())) {
                conceptRecords.add(conceptRecord);
            } else {
                uniquePublicIds.add(conceptRecord.publicId());
            }
        }
        return conceptRecords;
    }

    /*
    Identifier & Definition Status Semantic Chronology and Version
     */

    // creates identifier semantic for concept
    public static SemanticRecord createConceptIdentifierSemantic(String row) {
        UUID patternUUID = getIdentifierPatternUUID();
        UUID semanticUUID = getIdentifierSemanticUUID(row);
        UUID referencedComponentUUID = getReferenceComponentUUID(row);
        SemanticRecord identifierSemanticRecord = createSemantic(patternUUID, semanticUUID, referencedComponentUUID);
        SemanticVersionRecord semanticVersionRecord = createIdentifierSemanticVersion(identifierSemanticRecord, row);

        return SemanticRecordBuilder.builder(identifierSemanticRecord)
                .versions(identifierSemanticRecord.versions().newWith(semanticVersionRecord))
                .build();
    }

    // creates definition status semantic for concept
    public static SemanticRecord createConceptDefinitionStatusSemantic(String row) {
        UUID patternUUID = getDefinitionStatusPatternUUID();
        UUID semanticUUID = getDefinitionStatusSemanticUUID(row);
        UUID referencedComponentUUID = getReferenceComponentUUID(row);
        SemanticRecord definitionStatusSemanticRecord = createSemantic(patternUUID, semanticUUID, referencedComponentUUID);
        SemanticVersionRecord semanticVersionRecord = createDefinitionStatusSemanticVersion(definitionStatusSemanticRecord, row);

        return SemanticRecordBuilder.builder(definitionStatusSemanticRecord)
                .versions(definitionStatusSemanticRecord.versions().newWith(semanticVersionRecord))
                .build();
    }

    // creates identifier semantic version for identifier semantic for concept
    public static SemanticVersionRecord createIdentifierSemanticVersion(SemanticRecord semanticRecord, String row) {
        Concept textConcept = new Concept(row);
        Object[] fields = new Object[] {textConcept.id, SNOMED_CT_IDENTIFIER};
        return createSemanticVersion(fields, row, semanticRecord);
    }

    // creates definition status semantic version for definition status semantic for concept
    public static SemanticVersionRecord createDefinitionStatusSemanticVersion(SemanticRecord semanticRecord, String row) {
        Concept textConcept = new Concept(row);
        Object[] fields = new Object[] {UuidT5Generator.get(SNOMED_CT_NAMESPACE, textConcept.definitionStatusId)};
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
        Concept textConcept = new Concept(row);
        UUID semanticUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, IDENTIFIER_PATTERN.toString() + textConcept.id);
        MockEntity.populateMockData(semanticUUID.toString(), ENTITYREF);
        return semanticUUID;
    }

    // generate and return identifier semantic pattern UUID
    public static UUID getIdentifierPatternUUID() {
        MockEntity.populateMockData(IDENTIFIER_PATTERN.toString(), ENTITYREF);
        return IDENTIFIER_PATTERN;
    }

    // generate and return definition status semantic pattern UUID
    public static UUID getDefinitionStatusPatternUUID() {
        MockEntity.populateMockData(DEFINITION_STATUS_PATTERN.toString(), ENTITYREF);
        return DEFINITION_STATUS_PATTERN;
    }

    // generate and return reference component UUID
    public static UUID getReferenceComponentUUID(String row) {
        Concept textConcept = new Concept(row);
        UUID referenceComponentUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, textConcept.id);
        MockEntity.populateMockData(referenceComponentUUID.toString(), ENTITYREF);
        return referenceComponentUUID;
    }

    // generate and return definition status semantic UUID
    public static UUID getDefinitionStatusSemanticUUID(String row) {
        Concept textConcept = new Concept(row);
        UUID definitionStatusSemanticUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, DEFINITION_STATUS_PATTERN.toString() + textConcept.definitionStatusId + textConcept.id);
        MockEntity.populateMockData(definitionStatusSemanticUUID.toString(), ENTITYREF);
        return definitionStatusSemanticUUID;
    }

}
