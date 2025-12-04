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
package dev.ikm.tinkar.integration.provider.spinedarray;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptRecordBuilder;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecordBuilder;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpinedArrayPublicIdMergeIT {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayPublicIdMergeIT.class);
    private static final File SAP_DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            SpinedArrayPublicIdMergeIT.class);

    @BeforeAll
    static void beforeAll() {
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
    }

    @AfterAll
    static void afterAll() {
        TestHelper.stopDatabase();;
    }

    // Merging PublicId Cases:
    //   Case 1: mergeSubsetSingleUuidWithMultipleUuid
    //      Existing PublicId: [ Uuid1, Uuid2 ]
    //      New PublicId: [ Uuid1 ]
    //      Merged PublicId to Write: [ Uuid1, Uuid2 ]
    //   Case 2: mergeMultipleUuidWithSubsetSingleUuid
    //      Existing PublicId: [ Uuid1 ]
    //      New PublicId: [ Uuid1, Uuid2 ]
    //      Merged PublicId to Write: [ Uuid1, Uuid2 ]
    //   Case 3: mergeMultipleUuidWithOverlappingMultipleUuid
    //      Existing PublicId: [ Uuid1, Uuid2 ]
    //      New PublicId: [ Uuid2, Uuid3 ]
    //      Merged PublicId to Write: [ Uuid1, Uuid2, Uuid3 ]

    @Test
    @DisplayName("Case 1: mergeSubsetSingleUuidWithMultipleUuid")
    public void mergeSubsetSingleUuidWithMultipleUuid() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        EntityProxy.Concept conceptWithMultipleUuids = EntityProxy.Concept.make(PublicIds.of(uuid1, uuid2));
        EntityProxy.Concept conceptWithSingleUuid = EntityProxy.Concept.make(PublicIds.of(uuid1));

        Transaction premundaneTransaction = new Transaction();
        StampEntity premundaneStampEntity = premundaneTransaction.getStamp(State.ACTIVE,
                PrimitiveData.PREMUNDANE_TIME,
                TinkarTerm.USER.publicId(),
                TinkarTerm.DEVELOPMENT_MODULE.publicId(),
                TinkarTerm.DEVELOPMENT_PATH.publicId());

        //Write Concept with Multiple UUIDs
        ConceptEntity conceptRecordWithMultipleUuids = writeConceptHelper(conceptWithMultipleUuids, premundaneStampEntity);
        premundaneTransaction.addComponent(conceptRecordWithMultipleUuids);
        EntityService.get().putEntity(conceptRecordWithMultipleUuids);
        premundaneTransaction.commit();

        Transaction currentTransaction = new Transaction();
        StampEntity currentStampEntity = currentTransaction.getStamp(State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.DEVELOPMENT_MODULE,
                TinkarTerm.DEVELOPMENT_PATH);

        //Then Write Concept with Single UUID - invoking PublicID merge process
        ConceptEntity conceptRecordWithSingleUuid = writeConceptHelper(conceptWithSingleUuid, currentStampEntity);
        currentTransaction.addComponent(conceptRecordWithSingleUuid);
        EntityService.get().putEntity(conceptRecordWithSingleUuid);
        currentTransaction.commit();

        int expectedUuidCount = 2;
        int actualUuidCountUuid1 = EntityService.get().getEntityFast(uuid1).publicId().asUuidArray().length;
        int actualUuidCountUuid2 = EntityService.get().getEntityFast(uuid2).publicId().asUuidArray().length;
        assertEquals(expectedUuidCount, actualUuidCountUuid1,
                String.format("UUID count is not correct for lookup on uuid1. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCountUuid1));
        assertEquals(expectedUuidCount, actualUuidCountUuid2,
                String.format("UUID count is not correct for lookup on uuid2. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCountUuid2));

        int expectedVersionCount = 2;
        int actualVersionCountUuid1 = EntityService.get().getEntityFast(uuid1).versions().size();
        int actualVersionCountUuid2 = EntityService.get().getEntityFast(uuid2).versions().size();
        assertEquals(expectedVersionCount, actualVersionCountUuid1,
                String.format("Version count is not correct for lookup on uuid1. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCountUuid1));
        assertEquals(expectedVersionCount, actualVersionCountUuid2,
                String.format("Version count is not correct for lookup on uuid2. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCountUuid2));
    }

    @Test
    @DisplayName("Case 2: mergeMultipleUuidWithSubsetSingleUuid")
    public void mergeMultipleUuidWithSubsetSingleUuid() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        EntityProxy.Concept conceptWithSingleUuid = EntityProxy.Concept.make(PublicIds.of(uuid1));
        EntityProxy.Concept conceptWithMultipleUuids = EntityProxy.Concept.make(PublicIds.of(uuid1, uuid2));

        Transaction premundaneTransaction = new Transaction();
        StampEntity premundaneStampEntity = premundaneTransaction.getStamp(State.ACTIVE,
                PrimitiveData.PREMUNDANE_TIME,
                TinkarTerm.USER.publicId(),
                TinkarTerm.DEVELOPMENT_MODULE.publicId(),
                TinkarTerm.DEVELOPMENT_PATH.publicId());

        //Write Concept with Single UUID
        ConceptEntity conceptRecordWithSingleUuid = writeConceptHelper(conceptWithSingleUuid, premundaneStampEntity);
        premundaneTransaction.addComponent(conceptRecordWithSingleUuid);
        EntityService.get().putEntity(conceptRecordWithSingleUuid);
        premundaneTransaction.commit();

        Transaction currentTransaction = new Transaction();
        StampEntity currentStampEntity = currentTransaction.getStamp(State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.DEVELOPMENT_MODULE,
                TinkarTerm.DEVELOPMENT_PATH);

        //Then Write Concept with Multiple UUIDs - invoking PublicID merge process
        ConceptEntity conceptRecordWithMultipleUuids = writeConceptHelper(conceptWithMultipleUuids, currentStampEntity);
        currentTransaction.addComponent(conceptRecordWithMultipleUuids);
        EntityService.get().putEntity(conceptRecordWithMultipleUuids);
        currentTransaction.commit();

        int expectedUuidCount = 2;
        int actualUuidCountUuid1 = EntityService.get().getEntityFast(uuid1).publicId().asUuidArray().length;
        int actualUuidCountUuid2 = EntityService.get().getEntityFast(uuid2).publicId().asUuidArray().length;
        assertEquals(expectedUuidCount, actualUuidCountUuid1,
                String.format("UUID count is not correct for lookup on uuid1. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCountUuid1));
        assertEquals(expectedUuidCount, actualUuidCountUuid2,
                String.format("UUID count is not correct for lookup on uuid2. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCountUuid2));

        int expectedVersionCount = 2;
        int actualVersionCountUuid1 = EntityService.get().getEntityFast(uuid1).versions().size();
        int actualVersionCountUuid2 = EntityService.get().getEntityFast(uuid2).versions().size();
        assertEquals(expectedVersionCount, actualVersionCountUuid1,
                String.format("Version count is not correct for lookup on uuid1. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCountUuid1));
        assertEquals(expectedVersionCount, actualVersionCountUuid2,
                String.format("Version count is not correct for lookup on uuid2. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCountUuid2));
    }

    @Test
    @DisplayName("Case 3: mergeMultipleUuidWithOverlappingMultipleUuid")
    public void mergeMultipleUuidWithOverlappingMultipleUuid() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        EntityProxy.Concept conceptWithFirstSetOfMultipleUuids = EntityProxy.Concept.make(PublicIds.of(uuid1, uuid2));
        EntityProxy.Concept conceptWithSecondSetOfMultipleUuids = EntityProxy.Concept.make(PublicIds.of(uuid2, uuid3));

        Transaction premundaneTransaction = new Transaction();
        StampEntity premundaneStampEntity = premundaneTransaction.getStamp(State.ACTIVE,
                PrimitiveData.PREMUNDANE_TIME,
                TinkarTerm.USER.publicId(),
                TinkarTerm.DEVELOPMENT_MODULE.publicId(),
                TinkarTerm.DEVELOPMENT_PATH.publicId());

        //Write Concept with the first set of Multiple Uuids (uuid1 & uuid2)
        ConceptEntity conceptRecordWithFirstSetOfMultipleUuids = writeConceptHelper(conceptWithFirstSetOfMultipleUuids, premundaneStampEntity);
        premundaneTransaction.addComponent(conceptRecordWithFirstSetOfMultipleUuids);
        EntityService.get().putEntity(conceptRecordWithFirstSetOfMultipleUuids);
        premundaneTransaction.commit();

        Transaction currentTransaction = new Transaction();
        StampEntity currentStampEntity = currentTransaction.getStamp(State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.DEVELOPMENT_MODULE,
                TinkarTerm.DEVELOPMENT_PATH);

        //Then Write Concept with second set of Multiple UUIDs (uuid2 & uuid3) - invoking PublicID merge process
        ConceptEntity conceptRecordWithSecondSetOfMultipleUuids = writeConceptHelper(conceptWithSecondSetOfMultipleUuids, currentStampEntity);
        currentTransaction.addComponent(conceptRecordWithSecondSetOfMultipleUuids);
        EntityService.get().putEntity(conceptRecordWithSecondSetOfMultipleUuids);
        currentTransaction.commit();

        int expectedUuidCount = 3;
        int actualUuidCountUuid1 = EntityService.get().getEntityFast(uuid1).publicId().asUuidArray().length;
        int actualUuidCountUuid2 = EntityService.get().getEntityFast(uuid2).publicId().asUuidArray().length;
        int actualUuidCountUuid3 = EntityService.get().getEntityFast(uuid3).publicId().asUuidArray().length;
        assertEquals(expectedUuidCount, actualUuidCountUuid1,
                String.format("UUID count is not correct for lookup on uuid1. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCountUuid1));
        assertEquals(expectedUuidCount, actualUuidCountUuid2,
                String.format("UUID count is not correct for lookup on uuid2. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCountUuid2));
        assertEquals(expectedUuidCount, actualUuidCountUuid3,
                String.format("UUID count is not correct for lookup on uuid3. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCountUuid3));

        int expectedVersionCount = 2;
        int actualVersionCountUuid1 = EntityService.get().getEntityFast(uuid1).versions().size();
        int actualVersionCountUuid2 = EntityService.get().getEntityFast(uuid2).versions().size();
        int actualVersionCountUuid3 = EntityService.get().getEntityFast(uuid3).versions().size();
        assertEquals(expectedVersionCount, actualVersionCountUuid1,
                String.format("Version count is not correct for lookup on uuid1. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCountUuid1));
        assertEquals(expectedVersionCount, actualVersionCountUuid2,
                String.format("Version count is not correct for lookup on uuid2. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCountUuid2));
        assertEquals(expectedVersionCount, actualVersionCountUuid3,
                String.format("Version count is not correct for lookup on uuid3. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCountUuid3));
    }

    /* Helper Functions */
    private static ConceptEntity writeConceptHelper(EntityProxy.Concept concept, PublicId stampId) {
        //Initialize Concept PublicId and version list to prepare for potential merging
        PublicId conceptId = concept.publicId();
        RecordListBuilder<ConceptVersionRecord> versions = RecordListBuilder.make();

        //Pull out primordial UUID from PublicId
        UUID primordialUUID = conceptId.asUuidArray()[0];
        //Process additional UUID longs from PublicId

        long[] additionalLongs = additionalLongsHelper(conceptId);

        //Assign nids for PublicIds
        int stampNid = EntityService.get().nidForPublicId(stampId);

        //Create Concept Chronology
        ConceptRecord conceptRecord = ConceptRecordBuilder.builder()
                .nid(concept.nid())
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .versions(versions)
                .build();

        //Append Concept Version
        versions.add(ConceptVersionRecordBuilder.builder()
                .chronology(conceptRecord)
                .stampNid(stampNid)
                .build());

        //Rebuild the ConceptRecord with the now populated version data
        return ConceptRecordBuilder.builder(conceptRecord).versions(versions.toImmutable()).build();
    }

    private static long[] additionalLongsHelper(PublicId publicId) {
        long[] additionalLongs = new long[(publicId.uuidCount() * 2) - 2];
        int index = 0;
        for (int i = 1; i < publicId.uuidCount(); i++) {
            UUID uuid = publicId.asUuidArray()[i];
            additionalLongs[index++] = uuid.getMostSignificantBits();
            additionalLongs[index++] = uuid.getLeastSignificantBits();
        }
        return additionalLongs.length == 0 ? null : additionalLongs;
    }

}
