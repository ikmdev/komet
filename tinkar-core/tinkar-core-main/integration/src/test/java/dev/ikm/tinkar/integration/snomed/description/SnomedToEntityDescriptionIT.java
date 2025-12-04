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
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.StampRecordBuilder;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.entity.StampVersionRecordBuilder;
import dev.ikm.tinkar.integration.snomed.core.MockEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.MockDataType.ENTITYREF;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.ACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DESCRIPTION_PATTERN;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.INACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_AUTHOR;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_TEXT_MODULE_ID;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.loadSnomedFile;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.openSession;
import static dev.ikm.tinkar.integration.snomed.description.SnomedToEntityDescription.CASE_SIGNIFICANCE_ID_INDEX;
import static dev.ikm.tinkar.integration.snomed.description.SnomedToEntityDescription.CONCEPT_ID_INDEX;
import static dev.ikm.tinkar.integration.snomed.description.SnomedToEntityDescription.ID_INDEX;
import static dev.ikm.tinkar.integration.snomed.description.SnomedToEntityDescription.TERM_INDEX;
import static dev.ikm.tinkar.integration.snomed.description.SnomedToEntityDescription.TYPE_ID_INDEX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Stale")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SnomedToEntityDescriptionIT {
    SnomedToEntityDescription transformer;

    public static final long EXPECTED_DATE = LocalDate.parse("20020131", DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(12,0,0).toInstant(ZoneOffset.UTC).toEpochMilli();

    @BeforeEach
    public void beforeEach() {
        transformer = new SnomedToEntityDescription();
    }

    @Test
    @DisplayName("Creating a Stamp Chronology with Active State")
    public void testCreateStampChronologyWithActiveState(){
        //Given a row of snomed data
        openSession((mockStaticEntityService, starterData) -> {
            UUID namespaceUuid = SNOMED_CT_NAMESPACE;
            UUID stampUUID = UuidT5Generator.get(namespaceUuid, "101013200201311900000000000207008126813005en900000000000013009Neoplasm of anterior aspect of epiglottis900000000000020002");

            MockEntity.populateMockData(stampUUID.toString(), ENTITYREF);

            StampRecord expectedRecord = StampRecordBuilder.builder()
                    .leastSignificantBits(stampUUID.getLeastSignificantBits())
                    .mostSignificantBits(stampUUID.getMostSignificantBits())
                    .nid(MockEntity.getNid(stampUUID))
                    .versions(RecordListBuilder.make().build())
                    .build();

            StampVersionRecord expectedVersionRecord = StampVersionRecordBuilder.builder()
                    .stateNid(MockEntity.getNid(ACTIVE))
                    .chronology(expectedRecord)
                    .time(EXPECTED_DATE)
                    .authorNid(MockEntity.getNid(SNOMED_CT_AUTHOR))
                    .moduleNid(MockEntity.getNid(SNOMED_TEXT_MODULE_ID))
                    .pathNid(MockEntity.getNid(DEVELOPMENT_PATH))
                    .build();

            expectedRecord = expectedRecord.withVersions(RecordListBuilder.make().newWith(expectedVersionRecord));

            List<String> rows = loadSnomedFile(this.getClass(),"sct2_Description_Full-en_US1000124_20220901_1.txt");
            assertEquals(1, rows.size(),"Read file should only have one row");
            String testValues = rows.get(0);

            //When creating Stamp Chronology
            StampRecord actualRecord = transformer.createSTAMPChronology(testValues);

            //Then the created Stamp Chronology should match expected values
            assertEquals(expectedRecord.leastSignificantBits(), actualRecord.leastSignificantBits(), "StampRecord leastSignificantBits do not match expected");
            assertEquals(expectedRecord.mostSignificantBits(), actualRecord.mostSignificantBits(), "StampRecord mostSignificantBits do not match expected");
            assertEquals(expectedRecord.nid(), actualRecord.nid(), "StampRecord nid does not match expected");
            assertEquals(expectedRecord.versions(), actualRecord.versions(), "StampRecord versions do not match expected");
        });
    }

    @Test
    @DisplayName("Creating a Stamp Chronology with Inactive State")
    public void testCreateStampChronologyWithInactiveState(){
        //Given a row of snomed data
        openSession((mockStaticEntityService, starterData) -> {
            UUID namespaceUuid = SNOMED_CT_NAMESPACE;
            UUID stampUUID = UuidT5Generator.get(namespaceUuid, "157016200707310900000000000207008126869001en900000000000013009Neoplasm of the mesentery900000000000020002");

            MockEntity.populateMockData(stampUUID.toString(), ENTITYREF);

            StampRecord expectedRecord = StampRecordBuilder.builder()
                    .leastSignificantBits(stampUUID.getLeastSignificantBits())
                    .mostSignificantBits(stampUUID.getMostSignificantBits())
                    .nid(MockEntity.getNid(stampUUID))
                    .versions(RecordListBuilder.make().build())
                    .build();

            StampVersionRecord expectedVersionRecord = StampVersionRecordBuilder.builder()
                    .stateNid(MockEntity.getNid(INACTIVE))
                    .chronology(expectedRecord)
                    .time(LocalDate.parse("20070731", DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(12,0,0).toInstant(ZoneOffset.UTC).toEpochMilli())
                    .authorNid(MockEntity.getNid(SNOMED_CT_AUTHOR))
                    .moduleNid(MockEntity.getNid(SNOMED_TEXT_MODULE_ID))
                    .pathNid(MockEntity.getNid(DEVELOPMENT_PATH))
                    .build();

            expectedRecord = expectedRecord.withVersions(RecordListBuilder.make().newWith(expectedVersionRecord));

            List<String> rows = loadSnomedFile(this.getClass(),"sct2_Description_Full-en_US1000124_20220901_2.txt");
            Assertions.assertEquals(1, rows.size(),"Read file should only have one row");
            String testValues = rows.get(0);

            //When creating Stamp Chronology
            StampRecord actualRecord = transformer.createSTAMPChronology(testValues);

            //Then the created Stamp Chronology should match expected values
            Assertions.assertEquals(expectedRecord.leastSignificantBits(),actualRecord.leastSignificantBits(),"StampRecord leastSignificantBits do not match expected");
            Assertions.assertEquals(expectedRecord.mostSignificantBits(),actualRecord.mostSignificantBits(), "StampRecord mostSignificantBits do not match expected");
            Assertions.assertEquals(expectedRecord.nid(),actualRecord.nid(),"StampRecord nid does not match expected");
            Assertions.assertEquals(expectedRecord.versions(),actualRecord.versions(), "StampRecord versions do not match expected");
        });

    }

    @Test
    @DisplayName("Building StampRecord without Most Significant Bits throws IllegalStateException")
    public void testExceptionOnEmptyMostSignificantBits(){
        //Given a StampRecord without most significant bits
        StampRecordBuilder expectedRecord = StampRecordBuilder.builder()
                .leastSignificantBits(SNOMED_CT_NAMESPACE.getLeastSignificantBits())
                .nid(24)
                .versions(RecordListBuilder.make().build());
        //When we build the record
        //Then we expect an exception
        assertThrows(IllegalStateException.class, expectedRecord::build, "Expected IllegalStateException when Most Significant Bits is Zero");

    }
    @Test
    @DisplayName("Building StampRecord without Least Significant Bits throws IllegalStateException")
    public void testExceptionOnEmptyLeastSignificantBits(){
        //Given a StampRecord without least significant bits
        StampRecordBuilder expectedRecord = StampRecordBuilder.builder()
                .mostSignificantBits(SNOMED_CT_NAMESPACE.getMostSignificantBits())
                .nid(24)
                .versions(RecordListBuilder.make().build());
        //When we build the record
        //Then we expect an exception
        assertThrows(IllegalStateException.class, expectedRecord::build, "Expected IllegalStateException when Least Significant Bits is Zero");

    }
    @Test
    @DisplayName("Building StampRecord without Nid throws IllegalStateException")
    public void testExceptionOnEmptyNid(){
        //Given a StampRecord without nid
        StampRecordBuilder expectedRecord = StampRecordBuilder.builder()
                .leastSignificantBits(SNOMED_CT_NAMESPACE.getLeastSignificantBits())
                .mostSignificantBits(SNOMED_CT_NAMESPACE.getMostSignificantBits())
                .versions(RecordListBuilder.make().build());
        //When we build the record
        //Then we expect an exception
        assertThrows(IllegalStateException.class, expectedRecord::build, "Expected IllegalStateException when NID is Zero");
    }

    @Test
    @DisplayName("Building StampRecord without Version list throws NullPointerException")
    public void testExceptionOnNullVersionsList(){
        //Given a StampRecord with null version list
        StampRecordBuilder expectedRecord = StampRecordBuilder.builder()
                .leastSignificantBits(SNOMED_CT_NAMESPACE.getLeastSignificantBits())
                .mostSignificantBits(SNOMED_CT_NAMESPACE.getMostSignificantBits())
                .nid(24);
        //When we build the record
        //Then we expect an exception
        assertThrows(NullPointerException.class, expectedRecord::build, "Expected NullPointerException when NID is Zero");
    }

    @Test
    public void testExceptionOnEmptyStateNid() {
        //Given a StampRecord with StampVersionRecord without nid
        StampRecord expectedRecord = StampRecordBuilder.builder()
                .leastSignificantBits(SNOMED_CT_NAMESPACE.getLeastSignificantBits())
                .mostSignificantBits(SNOMED_CT_NAMESPACE.getMostSignificantBits())
                .nid(24)
                .versions(RecordListBuilder.make().build())
                .build();

        StampVersionRecordBuilder expectedVersionRecord = StampVersionRecordBuilder.builder()
                .chronology(expectedRecord)
                .time(LocalDate.parse("20020131", DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(12, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli())
                .authorNid(6)
                .moduleNid(23)
                .pathNid(5);

        //When we build the record
        //Then we expect an exception
        assertThrows(IllegalStateException.class, expectedVersionRecord::build, "Expected IllegalStateException when State Nid is Zero");
    }

    @Test
    public void testExceptionOnEmptyTime(){
        //Given a StampRecord with StampVersionRecord without time
        StampRecord expectedRecord = StampRecordBuilder.builder()
                .leastSignificantBits(SNOMED_CT_NAMESPACE.getLeastSignificantBits())
                .mostSignificantBits(SNOMED_CT_NAMESPACE.getMostSignificantBits())
                .nid(24)
                .versions(RecordListBuilder.make().build())
                .build();

        StampVersionRecordBuilder expectedVersionRecord = StampVersionRecordBuilder.builder()
                .stateNid(2)
                .chronology(expectedRecord)
                .authorNid(6)
                .moduleNid(23)
                .pathNid(5);

        //When we build the record
        //Then we expect an exception
        assertThrows(IllegalStateException.class, expectedVersionRecord::build, "Expected IllegalStateException when Time is Zero");

    }
    @Test
    public void testExceptionOnEmptyAuthorNid(){
        StampRecord expectedRecord = StampRecordBuilder.builder()
                .leastSignificantBits(SNOMED_CT_NAMESPACE.getLeastSignificantBits())
                .mostSignificantBits(SNOMED_CT_NAMESPACE.getMostSignificantBits())
                .nid(24)
                .versions(RecordListBuilder.make().build())
                .build();

        StampVersionRecordBuilder expectedVersionRecord = StampVersionRecordBuilder.builder()
                .stateNid(2)
                .chronology(expectedRecord)
                .time(LocalDate.parse("20020131", DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(12, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli())
                .moduleNid(23)
                .pathNid(5);

        //When we build the record
        //Then we expect an exception
        assertThrows(IllegalStateException.class, expectedVersionRecord::build, "Expected IllegalStateException when Author Nid is Zero");

    }
    @Test
    public void testExceptionOnEmptyModuleNid(){
        //Given a StampRecord with StampVersionRecord without module nid
        StampRecord expectedRecord = StampRecordBuilder.builder()
                .leastSignificantBits(SNOMED_CT_NAMESPACE.getLeastSignificantBits())
                .mostSignificantBits(SNOMED_CT_NAMESPACE.getMostSignificantBits())
                .nid(24)
                .versions(RecordListBuilder.make().build())
                .build();

        StampVersionRecordBuilder expectedVersionRecord = StampVersionRecordBuilder.builder()
                .stateNid(2)
                .chronology(expectedRecord)
                .time(LocalDate.parse("20020131", DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(12, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli())
                .authorNid(6)
                .pathNid(5);

        //When we build the record
        //Then we expect an exception
        assertThrows(IllegalStateException.class, expectedVersionRecord::build, "Expected IllegalStateException when Module Nid is Zero");

    }
    @Test
    public void testExceptionOnEmptyPathNid(){
        //Given a StampRecord with StampVersionRecord without path nid
        StampRecord expectedRecord = StampRecordBuilder.builder()
                .leastSignificantBits(SNOMED_CT_NAMESPACE.getLeastSignificantBits())
                .mostSignificantBits(SNOMED_CT_NAMESPACE.getMostSignificantBits())
                .nid(24)
                .versions(RecordListBuilder.make().build())
                .build();

        StampVersionRecordBuilder expectedVersionRecord = StampVersionRecordBuilder.builder()
                .stateNid(2)
                .chronology(expectedRecord)
                .time(LocalDate.parse("20020131", DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(12, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli())
                .authorNid(6)
                .moduleNid(23);

        //When we build the record
        //Then we expect an exception
        assertThrows(IllegalStateException.class, expectedVersionRecord::build, "Expected IllegalStateException when Path Nid is Zero");

    }

    @Test
    @DisplayName("Creating a Description Semantic from test file 4")
    public void testDescriptionFile4() throws IOException {
        List<String> rows = loadSnomedFile(this.getClass(),"sct2_Description_Full-en_US1000124_20220901_4.txt");
        testAndCompareTransformation(rows);
    }


    private void testAndCompareTransformation(List<String> rows) {
        openSession((mockStaticEntityService, starterData) -> {

            //Create Description Semantic for every row of test file
            List<SemanticRecord> actualRecords = new ArrayList<>();
            for (String row : rows) {

                //Create expected values and populate entity service with Nids
                String[] values = row.split("\t");
                UUID expectedStampUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, row.replaceAll("\t",""));
                UUID expectedPatternUUID = DESCRIPTION_PATTERN;
                UUID expectedSemanticUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE,DESCRIPTION_PATTERN.toString()+values[ID_INDEX]);
                UUID expectedReferencedComponentUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE, values[CONCEPT_ID_INDEX]);

                MockEntity.populateMockData(expectedStampUUID.toString(), ENTITYREF);
                MockEntity.populateMockData(expectedPatternUUID.toString(), ENTITYREF);
                MockEntity.populateMockData(expectedSemanticUUID.toString(),ENTITYREF);
                MockEntity.populateMockData(expectedReferencedComponentUUID.toString(), ENTITYREF);

                //Create expected field values for SemanticVersionRecord
                Object[] expectedFields = new Object[]{
                        ENGLISH_LANGUAGE,
                        values[TERM_INDEX],
                        UuidT5Generator.get(SNOMED_CT_NAMESPACE, values[CASE_SIGNIFICANCE_ID_INDEX]),
                        UuidT5Generator.get(SNOMED_CT_NAMESPACE, values[TYPE_ID_INDEX])};

                //Create actual semantic description
                SemanticRecord actualRecord = transformer.createDescriptionSemantic(row);

                //Check SemanticRecord values against expected
                assertEquals(expectedSemanticUUID.getMostSignificantBits(), actualRecord.mostSignificantBits(), "SemanticRecord most significant bits do not match expected");
                assertEquals(expectedSemanticUUID.getLeastSignificantBits(), actualRecord.leastSignificantBits(), "SemanticRecord least significant bits do not match expected");
                assertEquals(MockEntity.getNid(expectedSemanticUUID), actualRecord.nid(), "SemanticRecord nid does not match expected");
                assertEquals(MockEntity.getNid(expectedPatternUUID), actualRecord.patternNid(), "SemanticRecord most patternNid does not match expected");
                assertEquals(MockEntity.getNid(expectedReferencedComponentUUID), actualRecord.referencedComponentNid(), "SemanticRecord referencedComponentNid does not match expected");
                assertTrue(actualRecord.versions().size() > 0, "No SemanticRecordVersions");


                SemanticVersionRecord actualVersionRecord = actualRecord.versions().get(0);
                assertEquals(actualVersionRecord.stampNid(), MockEntity.getNid(expectedStampUUID));

                //Check field values to expected
                assertEquals(expectedFields[0], actualVersionRecord.fieldValues().get(0), "SemanticVersionRecord Language does not match expected");
                assertEquals(expectedFields[1], actualVersionRecord.fieldValues().get(1), "SemanticVersionRecord term does not match expected");
                assertEquals(expectedFields[2], actualVersionRecord.fieldValues().get(2), "SemanticVersionRecord case significance id does not match expected");
                assertEquals(expectedFields[3], actualVersionRecord.fieldValues().get(3), "SemanticVersionRecord type id does not match expected");
            }
        });
    }


}
