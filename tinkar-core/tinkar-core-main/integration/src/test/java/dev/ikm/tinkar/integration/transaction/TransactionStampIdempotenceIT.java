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
package dev.ikm.tinkar.integration.transaction;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.integration.StarterDataEphemeralProvider;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Transaction stamp creation idempotence.
 * Verifies that multiple calls to getStamp() with identical parameters
 * return the same stamp entity, which is critical for maintaining
 * canonical ObservableStamp instances and preventing duplicate versions.
 */
@ExtendWith(StarterDataEphemeralProvider.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionStampIdempotenceIT {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionStampIdempotenceIT.class);

    @Test
    @Order(1)
    @DisplayName("getStamp() should be idempotent with same parameters")
    void testGetStampIsIdempotent() {
        LOG.info("Test that Transaction.getStamp() is idempotent with same parameters");

        // Create a single transaction
        Transaction transaction = Transaction.make();

        // Define stamp coordinates
        State state = State.ACTIVE;
        long time = Long.MAX_VALUE;
        PublicId authorId = PublicIds.of(UUID.randomUUID());
        PublicId moduleId = PublicIds.of(UUID.randomUUID());
        PublicId pathId = PublicIds.of(UUID.randomUUID());

        // Call getStamp() multiple times with the same parameters
        StampEntity stamp1 = transaction.getStamp(state, time, authorId, moduleId, pathId);
        StampEntity stamp2 = transaction.getStamp(state, time, authorId, moduleId, pathId);
        StampEntity stamp3 = transaction.getStamp(state, time, authorId, moduleId, pathId);

        // Verify all stamps have the same NID (are the same entity)
        assertEquals(stamp1.nid(), stamp2.nid(),
            "First and second getStamp() calls should return stamps with the same nid");
        assertEquals(stamp1.nid(), stamp3.nid(),
            "First and third getStamp() calls should return stamps with the same nid");

        // Verify all stamps have the same public ID (UUID)
        assertEquals(stamp1.publicId(), stamp2.publicId(),
            "First and second getStamp() calls should return stamps with the same publicId");
        assertEquals(stamp1.publicId(), stamp3.publicId(),
            "First and third getStamp() calls should return stamps with the same publicId");

        // Verify stamps are equal
        assertEquals(stamp1, stamp2,
            "First and second getStamp() calls should return equal StampEntity objects");
        assertEquals(stamp1, stamp3,
            "First and third getStamp() calls should return equal StampEntity objects");

        LOG.info("✓ Transaction.getStamp() is idempotent: all calls returned stamp with nid={}, publicId={}",
            stamp1.nid(), stamp1.publicId());
    }

    @Test
    @Order(2)
    @DisplayName("getStampForEntities() should be idempotent with same parameters")
    void testGetStampForEntitiesIsIdempotent() {
        LOG.info("Test that Transaction.getStampForEntities() is idempotent with same parameters");

        // Create a single transaction
        Transaction transaction = Transaction.make();

        // Define stamp coordinates
        State state = State.ACTIVE;
        PublicId authorId = PublicIds.of(UUID.randomUUID());
        PublicId moduleId = PublicIds.of(UUID.randomUUID());
        PublicId pathId = PublicIds.of(UUID.randomUUID());

        // Create a mock entity facade for testing
        PublicId entityId = PublicIds.of(UUID.randomUUID());
        int entityNid = PrimitiveData.nid(entityId);
        dev.ikm.tinkar.terms.EntityFacade entityFacade = dev.ikm.tinkar.terms.EntityFacade.make(entityNid);

        // Call getStampForEntities() multiple times with the same parameters
        StampEntity stamp1 = transaction.getStampForEntities(state,
            PrimitiveData.nid(authorId),
            PrimitiveData.nid(moduleId),
            PrimitiveData.nid(pathId),
            entityFacade);

        StampEntity stamp2 = transaction.getStampForEntities(state,
            PrimitiveData.nid(authorId),
            PrimitiveData.nid(moduleId),
            PrimitiveData.nid(pathId),
            entityFacade);

        StampEntity stamp3 = transaction.getStampForEntities(state,
            PrimitiveData.nid(authorId),
            PrimitiveData.nid(moduleId),
            PrimitiveData.nid(pathId),
            entityFacade);

        // Verify all stamps have the same NID (are the same entity)
        assertEquals(stamp1.nid(), stamp2.nid(),
            "First and second getStampForEntities() calls should return stamps with the same nid");
        assertEquals(stamp1.nid(), stamp3.nid(),
            "First and third getStampForEntities() calls should return stamps with the same nid");

        // Verify all stamps have the same public ID (UUID)
        assertEquals(stamp1.publicId(), stamp2.publicId(),
            "First and second getStampForEntities() calls should return stamps with the same publicId");
        assertEquals(stamp1.publicId(), stamp3.publicId(),
            "First and third getStampForEntities() calls should return stamps with the same publicId");

        // Verify stamps are equal
        assertEquals(stamp1, stamp2,
            "First and second getStampForEntities() calls should return equal StampEntity objects");
        assertEquals(stamp1, stamp3,
            "First and third getStampForEntities() calls should return equal StampEntity objects");

        LOG.info("✓ Transaction.getStampForEntities() is idempotent: all calls returned stamp with nid={}, publicId={}",
            stamp1.nid(), stamp1.publicId());
    }

    @Test
    @Order(3)
    @DisplayName("getStamp() should return different stamps for different parameters")
    void testGetStampWithDifferentParametersReturnsDifferentStamps() {
        LOG.info("Test that Transaction.getStamp() returns different stamps for different parameters");

        Transaction transaction = Transaction.make();

        PublicId authorId = PublicIds.of(UUID.randomUUID());
        PublicId moduleId = PublicIds.of(UUID.randomUUID());
        PublicId pathId = PublicIds.of(UUID.randomUUID());

        // Get stamp with ACTIVE state
        StampEntity activeStamp = transaction.getStamp(State.ACTIVE, Long.MAX_VALUE,
            authorId, moduleId, pathId);

        // Get stamp with INACTIVE state (different parameter)
        StampEntity inactiveStamp = transaction.getStamp(State.INACTIVE, Long.MAX_VALUE,
            authorId, moduleId, pathId);

        // Verify stamps are different
        assertNotEquals(activeStamp.nid(), inactiveStamp.nid(),
            "Stamps with different states should have different nids");
        assertNotEquals(activeStamp.publicId(), inactiveStamp.publicId(),
            "Stamps with different states should have different publicIds");
        assertNotEquals(activeStamp, inactiveStamp,
            "Stamps with different states should not be equal");

        LOG.info("✓ Different parameters produce different stamps: activeStamp.nid={}, inactiveStamp.nid={}",
            activeStamp.nid(), inactiveStamp.nid());
    }

    @Test
    @Order(4)
    @DisplayName("Different transactions should produce different stamps for same parameters")
    void testDifferentTransactionsProduceDifferentStamps() {
        LOG.info("Test that different transactions produce different stamps even with same coordinates");

        PublicId authorId = PublicIds.of(UUID.randomUUID());
        PublicId moduleId = PublicIds.of(UUID.randomUUID());
        PublicId pathId = PublicIds.of(UUID.randomUUID());

        // Create two different transactions
        Transaction transaction1 = Transaction.make();
        Transaction transaction2 = Transaction.make();

        // Get stamps with same coordinates but different transactions
        StampEntity stamp1 = transaction1.getStamp(State.ACTIVE, Long.MAX_VALUE,
            authorId, moduleId, pathId);
        StampEntity stamp2 = transaction2.getStamp(State.ACTIVE, Long.MAX_VALUE,
            authorId, moduleId, pathId);

        // Verify stamps are different (because they belong to different transactions)
        assertNotEquals(stamp1.nid(), stamp2.nid(),
            "Stamps from different transactions should have different nids");
        assertNotEquals(stamp1.publicId(), stamp2.publicId(),
            "Stamps from different transactions should have different publicIds");
        assertNotEquals(stamp1, stamp2,
            "Stamps from different transactions should not be equal");

        LOG.info("✓ Different transactions produce different stamps: transaction1.stamp.nid={}, transaction2.stamp.nid={}",
            stamp1.nid(), stamp2.nid());
    }

    @Test
    @Order(5)
    @DisplayName("getStamp() should reuse existing stamp from database if it exists")
    void testGetStampReusesExistingStampFromDatabase() {
        LOG.info("Test that Transaction.getStamp() reuses existing stamp if already in database");

        PublicId authorId = PublicIds.of(UUID.randomUUID());
        PublicId moduleId = PublicIds.of(UUID.randomUUID());
        PublicId pathId = PublicIds.of(UUID.randomUUID());

        // Create first transaction and get a stamp
        Transaction transaction1 = Transaction.make();
        StampEntity stamp1 = transaction1.getStamp(State.ACTIVE, Long.MAX_VALUE,
            authorId, moduleId, pathId);
        PublicId stamp1PublicId = stamp1.publicId();
        int stamp1Nid = stamp1.nid();

        LOG.info("Created stamp1 with publicId={}, nid={}", stamp1PublicId, stamp1Nid);

        // Create second transaction with same UUID as first
        // This simulates the case where a stamp already exists in the database
        Transaction transaction2 = Transaction.make(transaction1.transactionUuid().toString());
        StampEntity stamp2 = transaction2.getStamp(State.ACTIVE, Long.MAX_VALUE,
            authorId, moduleId, pathId);

        // Verify stamps are the same (reused from database)
        assertEquals(stamp1.nid(), stamp2.nid(),
            "Stamps with same transaction UUID and coordinates should have same nid");
        assertEquals(stamp1.publicId(), stamp2.publicId(),
            "Stamps with same transaction UUID and coordinates should have same publicId");
        assertEquals(stamp1, stamp2,
            "Stamps with same transaction UUID and coordinates should be equal");

        LOG.info("✓ Transaction.getStamp() correctly reused existing stamp from database");
    }
}
