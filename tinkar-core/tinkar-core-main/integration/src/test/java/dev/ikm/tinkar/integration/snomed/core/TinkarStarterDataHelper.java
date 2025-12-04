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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.*;
import org.eclipse.collections.api.list.ImmutableList;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.loadJsonData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
Helper class to load starter data and load primitives for EntityService to
return when referenced from tests
 */
public class TinkarStarterDataHelper {

    // filenames to load on bootstrap

    public static final String TEST_SNOMEDCT_STARTER_DATA_JSON ="snomedct-starter-data-9.json";

    // to keep count of nids
    static int nidCount = 1;

    /*
    This inner class helps create Mock data as we read mock-data.json file.
     */

    public static void openSession(BiConsumer<MockedStatic<EntityService>, JsonNode> session) {
        // open session is a biconsumer that makes mockStaticEntityService available
        // in tests to customize and mock more methods or override mock methods.
        MockEntity.clearCache();
        JsonNode starterData = loadStarterData(TEST_SNOMEDCT_STARTER_DATA_JSON);

        try (MockedStatic<EntityService> mockStaticEntityService = Mockito.mockStatic(EntityService.class)) {
            EntityService entityService = mock(EntityService.class);
            mockStaticEntityService.when(EntityService::get).thenReturn(entityService);
            when(EntityService.get().nidForUuids(any(UUID.class))).thenAnswer((y) -> MockEntity.getNid(y.getArgument(0)));
            session.accept(mockStaticEntityService, starterData);
        }
    }

    /*
    Methods to load starter data and mock data
     */
    public static JsonNode loadStarterData(String fileName) {
       return loadJsonData(TinkarStarterDataHelper.class, fileName);
    }

    /*
       Stamp Chronology Methods
   */
    public static UUID generateAndPublishStampUuid(JsonNode starterData) {
        UUID nameSpaceUUID = UUID.fromString(getSnomedFieldDataAsText(starterData, "namespace"));
        String status = getSnomedFieldDataAsText(starterData, "STAMP", "status");
        long effectiveTime = getSnomedFieldDataAsLong(starterData, "STAMP","time");
        String module = getSnomedFieldDataAsText(starterData, "STAMP","module");
        String author = getSnomedFieldDataAsText(starterData, "STAMP","author");
        String path = getSnomedFieldDataAsText(starterData, "STAMP","path");

        UUID stampUUID = UuidT5Generator.get(nameSpaceUUID, status+effectiveTime+module+author+path);
        MockEntity.populateMockData(stampUUID.toString(), MockDataType.ENTITYREF);
        return stampUUID;
    }



    public static StampVersionRecord getStampVersionRecord(JsonNode starterData, long effectiveTime, StampRecord stampRecord) {
        // Create stamp version with the record specific values
        return StampVersionRecordBuilder.builder()
                .stateNid(getNid(starterData, "STAMP", "status"))
                .time(effectiveTime)
                .authorNid(getNid(starterData, "STAMP", "author"))
                .moduleNid(getNid(starterData, "STAMP", "module"))
                .pathNid(getNid(starterData, "STAMP", "path"))
                .chronology(stampRecord)
                .build();
    }

    public static StampEntity buildStampChronology(JsonNode snomedData)
    {
        // Get stamp Uuid and effective time
        UUID stampUuid = generateAndPublishStampUuid(snomedData);
        long effectiveTime = getSnomedFieldDataAsLong(snomedData, "STAMP", "time");
        // Create stamp version with the record specific values
        RecordListBuilder<StampVersionRecord> stampVersionRecords =
                RecordListBuilder.make();

        // create Stamp Record with those versions created above.
        StampRecord stampRecord = StampRecordBuilder.builder()
                .versions(stampVersionRecords)
                .leastSignificantBits(stampUuid.getLeastSignificantBits())
                .mostSignificantBits(stampUuid.getMostSignificantBits())
                .nid(EntityService.get().nidForUuids(stampUuid))
                .build();

        StampVersionRecord stampVersionRecord = getStampVersionRecord(snomedData, effectiveTime, stampRecord);

        StampRecord updatedStampRecord = StampRecordBuilder.builder(stampRecord)
                .versions(RecordListBuilder.make().newWith(stampVersionRecord))
                .build();
        return updatedStampRecord;
    }

    /*
        Concept Chronology Methods
    */
    public static ConceptRecord conceptBuilder(UUID conceptUuid) {
        return ConceptRecordBuilder.builder()
                .leastSignificantBits(conceptUuid.getLeastSignificantBits())
                .mostSignificantBits(conceptUuid.getMostSignificantBits())
                .versions(RecordListBuilder.make().build())
                .nid(EntityService.get().nidForUuids(conceptUuid))
                .build();
    }

    public static SemanticRecord semanticBuilder(UUID semanticUuid, UUID conceptUUID,  UUID patternUUID) {
        return SemanticRecordBuilder.builder()
                .nid(EntityService.get().nidForUuids(semanticUuid))
                .mostSignificantBits(semanticUuid.getMostSignificantBits())
                .leastSignificantBits(semanticUuid.getLeastSignificantBits())
                .referencedComponentNid(EntityService.get().nidForUuids(conceptUUID))
                .patternNid(EntityService.get().nidForUuids(patternUUID)) // TODO how to get pattern nid
                .versions(RecordListBuilder.make())
                .build();
    }

    protected static ConceptRecord generateAndPublishConcept(UUID conceptUuid) {
        MockEntity.populateMockData(conceptUuid.toString(), MockDataType.ENTITYREF);
        // Creating uuid for all the fields to convert them in concepts.
        // this could have been a random uuid, but to provide more meaningful, uuid - doing it this way
        ConceptRecord concept = conceptBuilder(conceptUuid);
        // i have added map in Abstract
        return concept;
    }

    public static List<ConceptEntity> buildConceptChronology(JsonNode snomedData) {
        List<ConceptEntity> conceptEntities = new ArrayList<>();
        Iterator itr = snomedData.get("concepts").iterator();

        while(itr.hasNext()) {
            JsonNode conceptNode = (JsonNode) itr.next();
            UUID conceptUUID = generateConceptUuid(conceptNode);
            UUID descriptionUUID = generateDescriptionUuid(conceptNode);
            UUID navigationUUID = generateNavigationUuid(conceptNode);
            UUID stampUUID = generateAndPublishStampUuid(snomedData);

            SemanticEntity descriptionSemantic = semanticBuilder(descriptionUUID, conceptUUID, navigationUUID);
            SemanticEntity navigationSemantic = semanticBuilder(navigationUUID, conceptUUID, navigationUUID);
            RecordListBuilder<ConceptVersionRecord> conceptVersionRecords =
                    RecordListBuilder.make();


            ConceptRecordBuilder conceptRecordBuilder = ConceptRecordBuilder.builder()
                    .nid(EntityService.get().nidForUuids(conceptUUID))
                    .versions(conceptVersionRecords)
                    .leastSignificantBits(conceptUUID.getLeastSignificantBits())
                    .mostSignificantBits(conceptUUID.getMostSignificantBits());

            // TODO : which one goes before versionrecord or record..both refernce each other
            ConceptVersionRecord conceptVersionRecord = ConceptVersionRecordBuilder.builder()
                    .stampNid(EntityService.get().nidForUuids(stampUUID))
                    .chronology(conceptRecordBuilder.build()) // Todo Check with Andrew on cyclic dependencies
                    .build();

            conceptVersionRecords.add(conceptVersionRecord);
            conceptEntities.add(ConceptRecordBuilder
                    .builder(conceptRecordBuilder.build())
                    .versions(conceptVersionRecords)
                    .build());
        }

        return conceptEntities;
    }

    /*
      Pattern Chronology Methods
  */


    public static ImmutableList<FieldDefinitionRecord> getFieldDefinitions(JsonNode patternNode, PatternRecord patternRecord) {
        Iterator itr = patternNode.get("field-description").iterator();
        List<FieldDefinitionRecord> list = new LinkedList<>();
        while(itr.hasNext()) {
            JsonNode innerNode = (JsonNode) itr.next();
            FieldDefinitionRecord fieldRecord = FieldDefinitionRecordBuilder
                    .builder()
                    .patternVersionStampNid(Arrays.stream(patternRecord.stampNids().toArray()).findFirst().orElse(-1))
                    .meaningNid(getNid(innerNode, "meaning"))
                    .purposeNid(getNid(innerNode, "purpose"))
                    .dataTypeNid(getNid(innerNode, "datatype"))
                    .indexInPattern(getInt(innerNode, "index")) // TODO: can't populate 0, but file gives 0 value
                    .patternNid(patternRecord.nid())
                    .build();
            list.add(fieldRecord);
        }

        return RecordListBuilder.make().newWithAll(list);
    }

    public static PatternVersionRecord getPatternVersionRecord(JsonNode patternNode, PatternRecord patternRecord) {
        // Create pattern version with the record specific and field definition values
        ImmutableList<FieldDefinitionRecord> fieldDefinitions = getFieldDefinitions(patternNode, patternRecord);

        return PatternVersionRecordBuilder.builder()
                .stampNid(patternRecord.stampNids().intStream().findFirst().orElse(-1))
                .semanticMeaningNid(getNid(patternNode, "meaning"))
                .semanticPurposeNid(getNid(patternNode, "purpose"))
                .fieldDefinitions(fieldDefinitions)
                .chronology(patternRecord)
                .build();
    }

    public static List<PatternEntity> buildPatternChronology(JsonNode snomedData)
    {
        List<PatternEntity> patternEntities = new ArrayList<>();
        Iterator itr = snomedData.get("patterns")
                .iterator();

        while (itr.hasNext()) {
            JsonNode patternNode = (JsonNode) itr.next();
            // Get stamp Uuid and effective time
            UUID patternUuid = generatePatternUuid(patternNode);
            RecordListBuilder<PatternVersionRecord> patternVersionRecords =
                    RecordListBuilder.make();

            // create Stamp Record with those versions created above.
            PatternRecord patternRecord = PatternRecordBuilder.builder()
                    .versions(patternVersionRecords)
                    .leastSignificantBits(patternUuid.getLeastSignificantBits())
                    .mostSignificantBits(patternUuid.getMostSignificantBits())
                    .nid(EntityService.get().nidForUuids(patternUuid))
                    .build();

            PatternVersionRecord patternVersionRecord = getPatternVersionRecord(patternNode, patternRecord);

            PatternRecord updatedPatternRecord = PatternRecordBuilder.builder(patternRecord)
                    .versions(RecordListBuilder.make()
                            .newWith(patternVersionRecord))
                    .build();

            patternEntities.add(updatedPatternRecord);
        }
        return patternEntities;
    }

    public static UUID generateConceptUuid(JsonNode conceptNode) {
        ObjectMapper mapper = new ObjectMapper();
        String description = conceptNode.get("description").asText();
        //        UUID descriptionConcept = UuidT5Generator.get(conceptNode.get("description").asText());
        List<String> originsArray = conceptNode.findValues("origins").stream().map((x) -> {
            if (x.size() > 0) {
                return x.get(0).asText();
            } else {
                return "";
            }
        }).toList();
        List<String> destinationsArray = conceptNode.findValues("destinations").stream().map((x) -> {
            if (x.size() > 0) {
                return x.get(0).asText();
            } else {
                return "";
            }
        }).toList();
        UUID conceptUUID = UuidT5Generator.get(description+originsArray.stream().reduce((str1, str2) -> str1 + str2)+destinationsArray.stream().reduce((str1, str2) -> str1+str2));
        MockEntity.populateMockData(conceptUUID.toString(), MockDataType.ENTITYREF);
        return conceptUUID;
    }

    public static UUID generateNavigationUuid(JsonNode conceptNode)
    {
        List<String> originsArray = conceptNode.findValues("origins").stream().map((x) -> {
            if (x.size() > 0) {
                return x.get(0).asText();
            } else {
                return "";
            }
        }).toList();
        List<String> destinationsArray = conceptNode.findValues("destinations").stream().map((x) -> {
            if (x.size() > 0) {
                return x.get(0).asText();
            } else {
                return "";
            }
        }).toList();
        String uuidString = originsArray.stream().reduce((str1, str2) -> str1 + str2)+""+destinationsArray.stream().reduce((str1, str2) -> str1+str2);
        UUID navigationUUID = UuidT5Generator.get(uuidString);
        MockEntity.populateMockData(navigationUUID.toString(), MockDataType.ENTITYREF);
        return navigationUUID;
    }

    public static UUID generatePatternUuid(JsonNode patternNode) {

        String description = getSnomedFieldDataAsText(patternNode , "description");
        String meaning = getSnomedFieldDataAsText(patternNode,  "meaning");
        String purpose = getSnomedFieldDataAsText(patternNode, "purpose");

        UUID patternUuid = UuidT5Generator.get(description+meaning+purpose);
        MockEntity.populateMockData(patternUuid.toString(), MockDataType.ENTITYREF);
        return patternUuid;
    }

    public static UUID generateDescriptionUuid(JsonNode conceptNode) {
        MockEntity.populateMockData(conceptNode.get("description").asText(), MockDataType.CONCEPT);
        return UuidT5Generator.get(conceptNode.get("description").asText());
    }

    /*
    Helper methods to pick values from nested json files.
     */

    // This method will recursively parse fields passed as arguments from JSONNode.
    protected static JsonNode getSnomedFieldData(JsonNode snomedNode, String ... nestedFields)
    {
        JsonNode tempNode = snomedNode;
        boolean inloop = false;
        for (String field: nestedFields) {
            if (!inloop) {
                tempNode = tempNode.get(field);
                inloop = true;
            } else {
                tempNode = tempNode.get(field);
            }
        }
        return tempNode;
    }

    protected static String getSnomedFieldDataAsText(JsonNode patternNode, String ... fields)
    {
        return getSnomedFieldData(patternNode, fields).asText();
    }

    protected static UUID getSnomedFieldUUID(JsonNode patternNode, String ... fields)
    {
        return UuidT5Generator.get(getSnomedFieldDataAsText(patternNode, fields));
    }

    protected static Long getSnomedFieldDataAsLong(JsonNode patternNode,String ... fields)
    {
        return getSnomedFieldData(patternNode, fields).asLong();
    }

    protected static int getNid(JsonNode node, String ... nestedFields) {
//        UUID conceptUUID = getSnomedFieldUUID(node, nestedFields);
        // Everything is a concept in Tinkar. Create nids for all concepts to populate in builders
        return generateAndPublishConcept(getSnomedFieldUUID(node, nestedFields)).nid();
    }

    protected static int getInt(JsonNode node, String ... nestedFields) {
        // Everything is a concept in Tinkar. Create nids for all concepts to populate in builders
        return getSnomedFieldData(node, nestedFields).asInt()+1;
    }

}
