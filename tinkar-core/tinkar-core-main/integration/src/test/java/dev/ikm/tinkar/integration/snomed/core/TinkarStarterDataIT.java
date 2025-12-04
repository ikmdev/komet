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


import com.fasterxml.jackson.databind.node.JsonNodeType;
import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecordBuilder;
import dev.ikm.tinkar.entity.StampVersionRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.ACTIVE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DELOITTE_USER;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_AUTHOR;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_STARTER_DATA_MODULE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.buildConceptChronology;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.buildPatternChronology;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.buildStampChronology;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.generateAndPublishConcept;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.getNid;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.getSnomedFieldDataAsLong;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.getSnomedFieldDataAsText;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.getStampVersionRecord;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.openSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Stale")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TinkarStarterDataIT {

    @Test
    @Order(1)
    public void testDataIsPresent() {
        openSession((mockStaticEntityService, starterData) -> {
            assertNotNull(starterData, "No starter data found.");
        });
    }

    @Test
    @Order(2)
    public void testDataIsJsonObject() {
        openSession((mockStaticEntityService, starterData) -> {
            assertTrue(starterData.getNodeType() == JsonNodeType.OBJECT, "Something wrong with the type of starter data.");
        });

    }

    @Test
    @Order(3)
    public void testForNestedFields() {
        openSession((mockStaticEntityService, starterData) -> {
            assertTrue(getSnomedFieldDataAsText(starterData, "namespace").equals("48b004d4-6457-4648-8d58-e3287126d96b"),
                    "Not the right nested value for namespace");
            assertTrue(getSnomedFieldDataAsText(starterData, "STAMP", "status").equals("Active"), "Not an active field");
        });

    }

    @Test
    @Order(4)
    public void testForNid() {
        openSession((mockStaticEntityService, starterData) -> {
            assertEquals(MockEntity.getNid(ACTIVE), getNid(starterData, "STAMP", "status"), "Stamp status is not active.");
        });

    }

    @Test
    @Order(5)
    public void testForMockEntity() {
        openSession((mockStaticEntityService, starterData) -> {
            assertEquals(MockEntity.getNid(DELOITTE_USER), EntityService.get().nidForUuids(DELOITTE_USER), "Not a deloitte user");
            assertEquals(MockEntity.getNid(ACTIVE), EntityService.get().nidForUuids(ACTIVE), "Concept is not active");
        });
    }

    @Test
    @Order(6)
    public void testForStampVersionRecord() {
        openSession((mockStaticEntityService, starterData) -> {
            long effectiveTime = getSnomedFieldDataAsLong(starterData, "STAMP", "time");
            StampVersionRecord actualStampVersion = getStampVersionRecord(starterData, effectiveTime, StampRecordBuilder.builder().leastSignificantBits(1L).mostSignificantBits(2L).nid(3).versions(RecordListBuilder.make()).build());

            assertEquals(effectiveTime, actualStampVersion.time());
            assertEquals(MockEntity.getNid(ACTIVE), actualStampVersion.stateNid());
            assertEquals(MockEntity.getNid(DELOITTE_USER), actualStampVersion.authorNid());
            assertEquals(MockEntity.getNid(SNOMED_CT_STARTER_DATA_MODULE), actualStampVersion.moduleNid());
            assertEquals(MockEntity.getNid(DEVELOPMENT_PATH), actualStampVersion.pathNid());
        });
    }

    @Test
    @Order(6)
    public void testForGeneratingAndPersistingEntity() {
        openSession((mockStaticEntityService, starterData) -> {
            ConceptRecord userConcept = generateAndPublishConcept(DELOITTE_USER);
            ConceptRecord statusConcept = generateAndPublishConcept(ACTIVE);
            ConceptRecord moduleConcept = generateAndPublishConcept(SNOMED_CT_STARTER_DATA_MODULE);
            ConceptRecord pathConcept = generateAndPublishConcept(DEVELOPMENT_PATH);

            assertEquals(MockEntity.getNid(ACTIVE), statusConcept.nid());
            assertEquals(MockEntity.getNid(ACTIVE), EntityService.get().nidForUuids(ACTIVE));
            assertEquals(MockEntity.getNid(DELOITTE_USER), userConcept.nid());
            assertEquals(MockEntity.getNid(DELOITTE_USER), EntityService.get().nidForUuids(DELOITTE_USER));
            assertEquals(MockEntity.getNid(SNOMED_CT_STARTER_DATA_MODULE), moduleConcept.nid());
            assertEquals(MockEntity.getNid(SNOMED_CT_STARTER_DATA_MODULE), EntityService.get().nidForUuids(SNOMED_CT_STARTER_DATA_MODULE));
            assertEquals(MockEntity.getNid(DEVELOPMENT_PATH), pathConcept.nid());
            assertEquals(MockEntity.getNid(DEVELOPMENT_PATH), EntityService.get().nidForUuids(DEVELOPMENT_PATH));
        });
    }

    @Test
    @Order(7)
    public void testingForEntityService() {
        openSession((mockStaticEntityService, starterData) -> {
            assertEquals(MockEntity.getNid(ACTIVE), EntityService.get().nidForUuids(ACTIVE));
            assertEquals(MockEntity.getNid(DELOITTE_USER), EntityService.get().nidForUuids(DELOITTE_USER));
            assertEquals(MockEntity.getNid(SNOMED_CT_STARTER_DATA_MODULE), EntityService.get().nidForUuids(SNOMED_CT_STARTER_DATA_MODULE));
            assertEquals(MockEntity.getNid(DEVELOPMENT_PATH), EntityService.get().nidForUuids(DEVELOPMENT_PATH));
        });
    }

    @Test
    @Order(8)
    public void testingForStampVersion() {
        openSession((mockStaticEntityService, starterData) -> {
            long effectiveTime = getSnomedFieldDataAsLong(starterData, "STAMP", "time");
            StampVersionRecord stampRecord = getStampVersionRecord(starterData, effectiveTime, StampRecordBuilder.builder().leastSignificantBits(1L).mostSignificantBits(2L).nid(3).versions(RecordListBuilder.make()).build());
            assertEquals(MockEntity.getNid(ACTIVE), stampRecord.stateNid());
            assertEquals(MockEntity.getNid(DELOITTE_USER), stampRecord.authorNid());
            assertEquals(MockEntity.getNid(SNOMED_CT_STARTER_DATA_MODULE), stampRecord.moduleNid());
            assertEquals(MockEntity.getNid(DEVELOPMENT_PATH), stampRecord.pathNid());
            assertEquals(effectiveTime, stampRecord.time());
        });
    }

    @Test
    @Order(9)
    public void testingForStampChronology()
            throws IOException {
        openSession((mockStaticEntityService, starterData) -> {
            StampEntity stampRecord = buildStampChronology(starterData);
            assertEquals(MockEntity.getNid(DEVELOPMENT_PATH), stampRecord.pathNid());
            List<PatternEntity> patternEntities = buildPatternChronology(starterData);
            assertEquals(2, patternEntities.size(), "There are more than 2 expected patterns");
        });
    }

    @Test
    @Order(10)
    public void testMockitoMock() {
        openSession((mockStaticEntityService, starterData) -> {
            assertEquals(MockEntity.getNid(DELOITTE_USER), EntityService.get().nidForUuids(DELOITTE_USER));
            assertEquals(MockEntity.getNid(SNOMED_CT_AUTHOR), EntityService.get().nidForUuids(SNOMED_CT_AUTHOR));
        });
    }

    @Test
    @Order(11)
    public void testForConceptEntities() {
        openSession((mockStaticEntityService, starterData) -> {
            List<ConceptEntity> conceptEntities = buildConceptChronology(starterData);
            assertEquals(10, conceptEntities.size(), "The concepts are not 10");
        });

    }

    @Test
    @Order(12)
    public void testForConceptEntity() {
        openSession((mockStaticEntityService, starterData) -> {
            List<ConceptEntity> conceptEntities = buildConceptChronology(starterData);
            ConceptEntity conceptEntity = conceptEntities.get(0);
            assertEquals(FieldDataType.CONCEPT_CHRONOLOGY, conceptEntity.entityDataType());
            assertEquals(EntityService.get().nidForUuids(UUID.fromString("1b41d227-0eda-5745-8231-c39ec3a9ae98")),
                    conceptEntity.nid());
        });
    }

    @Test
    @Order(13)
    public void testForPatternEntities() throws IOException {
        openSession((mockStaticEntityService, starterData) -> {
            List<PatternEntity> patternEntities = buildPatternChronology(starterData);
            assertEquals(2, patternEntities.size());
        });
    }

}
