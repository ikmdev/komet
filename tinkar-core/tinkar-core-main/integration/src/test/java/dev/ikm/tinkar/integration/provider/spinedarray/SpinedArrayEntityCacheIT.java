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

class SpinedArrayEntityCacheIT {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayEntityCacheIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            SpinedArrayEntityCacheIT.class);

    @BeforeAll
    static void beforeAll() {
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
    }

    @AfterAll
    static void afterAll() {
        TestHelper.stopDatabase();;
    }

    // Entity Cache Refresh Cases:
    //   Case 1: addInitialVersion
    //      Existing Cache: [ ] *Empty*
    //      New Version: Version1->CurrentStamp
    //      Expected New Cache: [ Version1->CurrentStamp ]
    //   Case 2: addNewVersionToExisting
    //      Existing Cache: [ Version1->PremundaneStamp ]
    //      New Version: Version2->CurrentStamp
    //      Expected New Cache: [ Version1->PremundaneStamp, Version2->CurrentStamp ]

    @Test
    @DisplayName("Case 1: addInitialVersion")
    public void addInitialVersion() {
        EntityProxy.Concept conceptProxy = EntityProxy.Concept.make(PublicIds.newRandom());

        /* Start: Seed database with one STAMP */
        Transaction transaction = new Transaction();
        StampEntity currentStampEntity = transaction.getStamp(State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.DEVELOPMENT_MODULE,
                TinkarTerm.DEVELOPMENT_PATH);
        transaction.commit();
        /* End: Seed database with one STAMP */

        //Write Concept to database (without invalidating entity cache)
        ConceptEntity newConceptVersion = writeConceptHelper(conceptProxy, currentStampEntity);
        EntityService.get().putEntity(newConceptVersion);

        int expectedUuidCount = 1;
        int actualUuidCount = EntityService.get().getEntityFast(conceptProxy).publicId().asUuidArray().length;
        assertEquals(expectedUuidCount, actualUuidCount,
                String.format("UUID count is not correct. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCount));

        int expectedVersionCount = 1;
        int actualVersionCount = EntityService.get().getEntityFast(conceptProxy).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Version count is not correct. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCount));
    }

    @Test
    @DisplayName("Case 2: addNewVersionToExisting")
    public void addNewVersionToExisting() {
        EntityProxy.Concept conceptProxy = EntityProxy.Concept.make(PublicIds.newRandom());

        /* Start: Seed database with two STAMPs and a Concept Version */
        Transaction transaction = new Transaction();
        StampEntity premundaneStampEntity = transaction.getStamp(State.ACTIVE,
                PrimitiveData.PREMUNDANE_TIME,
                TinkarTerm.USER.publicId(),
                TinkarTerm.DEVELOPMENT_MODULE.publicId(),
                TinkarTerm.DEVELOPMENT_PATH.publicId());
        StampEntity currentStampEntity = transaction.getStamp(State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.DEVELOPMENT_MODULE,
                TinkarTerm.DEVELOPMENT_PATH);
        //Write Concept Version to database
        ConceptEntity seedConceptVersion = writeConceptHelper(conceptProxy, premundaneStampEntity);
        transaction.addComponent(seedConceptVersion);
        EntityService.get().putEntity(seedConceptVersion);
        transaction.commit(); //Commit Transaction to add STAMPs & Concept Version and refresh cache
        /* End: Seed database with two STAMPs and a Concept Version */

        //Write new Concept Version to database (without invalidating entity cache)
        ConceptEntity conceptRecordWithMultipleUuids = writeConceptHelper(conceptProxy, currentStampEntity);
        EntityService.get().putEntity(conceptRecordWithMultipleUuids);

        int expectedUuidCount = 1;
        int actualUuidCount = EntityService.get().getEntityFast(conceptProxy).publicId().asUuidArray().length;
        assertEquals(expectedUuidCount, actualUuidCount,
                String.format("UUID count is not correct. Expect: %s, Actual: %s", expectedUuidCount, actualUuidCount));

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(conceptProxy).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Version count is not correct. Expect: %s, Actual: %s", expectedVersionCount, actualVersionCount));
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
