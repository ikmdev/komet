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
package dev.ikm.komet.framework.observable;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;

/**
 * Integration tests for ObservableComposer using loaded entity data.
 * Tests composer functionality with real entities loaded from protobuf files.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(JavaFXThreadExtension.class)
class ObservableComposerITestFX {

    private static final Logger LOG = LoggerFactory.getLogger(ObservableComposerITestFX.class);
    private static final File TEST_DATA_DIR = new File("target/data");
    private static final File PB_STARTER_DATA = new File(TEST_DATA_DIR, "tinkar-starter-data-reasoned-pb.zip");

    private EntityCountSummary loadedEntitiesSummary;

    @BeforeAll
    void setupDatabase() {
        LOG.info("Setting up integration test environment");

        // Setup ephemeral data store
        CachingService.clearAll();
        LOG.info("Cleared caches");

        // Configure for ephemeral store
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        LOG.info("Started PrimitiveData with ephemeral store");
    }

    @Test
    @RunOnJavaFXThread
    @Order(1)
    void loadTestData() {
        if (PB_STARTER_DATA.exists()) {
            LOG.info("Loading test data from: {}", PB_STARTER_DATA.getAbsolutePath());
            LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA);
            loadedEntitiesSummary = loadProto.compute();
            LOG.info("{} entities loaded: {}", loadedEntitiesSummary.getTotalCount(), loadProto.summarize());

            assertTrue(loadedEntitiesSummary.getTotalCount() > 0, "Should load entities from protobuf file");
        } else {
            LOG.warn("Test data file not found at: {}. Some tests may be limited.", PB_STARTER_DATA.getAbsolutePath());
            LOG.info("Tests will proceed using built-in TinkarTerm entities");
        }
    }

    @Test
    @RunOnJavaFXThread
    @Order(2)
    void testCreateComposerWithLoadedEntities() {

    ObservableComposer composer = ObservableComposer.create(
            Calculators.View.Default(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH,
            "Integration test transaction"
    );

    assertNotNull(composer);
    assertEquals(State.ACTIVE, composer.getDefaultState());
    assertEquals(TinkarTerm.USER.nid(), composer.getAuthorNid());
    assertEquals(TinkarTerm.PRIMORDIAL_MODULE.nid(), composer.getModuleNid());
    assertEquals(TinkarTerm.DEVELOPMENT_PATH.nid(), composer.getPathNid());

    LOG.info("Created composer with loaded entity context");
    }

    @Test
    @RunOnJavaFXThread
    @Order(3)
    void testTransactionCreationWithRealEntities() {

    ObservableComposer composer = ObservableComposer.create(
            Calculators.View.Default(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH
    );

    assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());

    Transaction transaction = composer.getOrCreateTransaction();

    assertNotNull(transaction);
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer.getTransactionState());

    LOG.info("Transaction created successfully with real entity store");
    }

    @Test
    @RunOnJavaFXThread
    @Order(4)
    void testTransactionLifecycleWithDatabase() {

    ObservableComposer composer = ObservableComposer.builder()
            .viewCalculator(Calculators.View.Default())
            .author(TinkarTerm.USER)
            .module(TinkarTerm.PRIMORDIAL_MODULE)
            .path(TinkarTerm.DEVELOPMENT_PATH)
            .defaultState(State.ACTIVE)
            .transactionComment("Lifecycle test")
            .build();

    // Initial state
    assertNull(composer.getTransaction());
    assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());
    assertFalse(composer.hasUncommittedChanges());

    // Create transaction
    Transaction transaction1 = composer.getOrCreateTransaction();
    assertNotNull(transaction1);
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer.getTransactionState());

    // Verify same transaction on repeated calls
    Transaction transaction2 = composer.getOrCreateTransaction();
    assertSame(transaction1, transaction2);

    // Cancel transaction
    composer.cancel();
    assertNull(composer.getTransaction());
    assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());

    LOG.info("Transaction lifecycle completed successfully");
    }

    @Test
    @RunOnJavaFXThread
    @Order(5)
    void testMultipleComposersIndependence() {

    ObservableComposer composer1 = ObservableComposer.create(
            Calculators.View.Default(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH,
            "Composer 1"
    );

    ObservableComposer composer2 = ObservableComposer.create(
            Calculators.View.Default(),
            State.INACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH,
            "Composer 2"
    );

    // Create transactions independently
    Transaction tx1 = composer1.getOrCreateTransaction();
    Transaction tx2 = composer2.getOrCreateTransaction();

    assertNotNull(tx1);
    assertNotNull(tx2);
    assertNotSame(tx1, tx2, "Each composer should have independent transactions");

    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer1.getTransactionState());
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer2.getTransactionState());

    // Cancel one should not affect the other
    composer1.cancel();
    assertEquals(ObservableComposer.TransactionState.NONE, composer1.getTransactionState());
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer2.getTransactionState());

    composer2.cancel();

    LOG.info("Multiple composers maintain independence");
    }

    @Test
    @RunOnJavaFXThread
    @Order(6)
    void testComposerWithDifferentStates() {

    State[] states = {State.ACTIVE, State.INACTIVE, State.CANCELED, State.PRIMORDIAL, State.WITHDRAWN};

    for (State state : states) {
        ObservableComposer composer = ObservableComposer.create(
                Calculators.View.Default(),
                state,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertEquals(state, composer.getDefaultState());
        assertNotNull(composer.getOrCreateTransaction());

        composer.cancel();

        LOG.info("Composer created with state: {}", state);
    }
    }

    @Test
    @RunOnJavaFXThread
    @Order(7)
    void testPropertyNotificationsWithDatabase() {

    ObservableComposer composer = ObservableComposer.create(
            Calculators.View.Default(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH
    );

    AtomicReference<ObservableComposer.TransactionState> capturedState = new AtomicReference<>();
    AtomicReference<Boolean> capturedHasChanges = new AtomicReference<>();

    composer.transactionStateProperty().addListener((obs, oldVal, newVal) -> {
        capturedState.set(newVal);
        LOG.debug("Transaction state changed: {} -> {}", oldVal, newVal);
    });

    composer.hasUncommittedChangesProperty().addListener((obs, oldVal, newVal) -> {
        capturedHasChanges.set(newVal);
        LOG.debug("Has uncommitted changes: {} -> {}", oldVal, newVal);
    });

    // Trigger state changes
    composer.getOrCreateTransaction();
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, capturedState.get());

    composer.cancel();
    assertEquals(ObservableComposer.TransactionState.NONE, capturedState.get());

    LOG.info("Property notifications working correctly with database");
    }

    @Test
    @RunOnJavaFXThread
    @Order(8)
    void testComposerWithDifferentAuthors() {

    // Test with different author entities
    EntityProxy[] authors = {TinkarTerm.USER, TinkarTerm.KOMET_USER};

    for (EntityProxy author : authors) {
        ObservableComposer composer = ObservableComposer.builder()
                .viewCalculator(Calculators.View.Default())
                .author(author)
                .module(TinkarTerm.PRIMORDIAL_MODULE)
                .path(TinkarTerm.DEVELOPMENT_PATH)
                .build();

        assertNotNull(composer);
        assertEquals(author.nid(), composer.getAuthorNid());

        Transaction tx = composer.getOrCreateTransaction();
        assertNotNull(tx);

        composer.cancel();

        LOG.info("Composer created with author: {} (nid: {})", author, author.nid());
    }
    }

    @Test
    @RunOnJavaFXThread
    @Order(9)
    void testConcurrentTransactionCreation() {

    ObservableComposer composer = ObservableComposer.create(
            Calculators.View.Default(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH
    );

    // Call getOrCreateTransaction multiple times rapidly
    Transaction tx1 = composer.getOrCreateTransaction();
    Transaction tx2 = composer.getOrCreateTransaction();
    Transaction tx3 = composer.getOrCreateTransaction();

    // All should return the same transaction instance
    assertSame(tx1, tx2);
    assertSame(tx2, tx3);

    composer.cancel();

    LOG.info("Concurrent transaction creation handled correctly");
    }

    @AfterAll
    void tearDownDatabase() {
        LOG.info("Tearing down integration test environment");
        PrimitiveData.stop();
        LOG.info("PrimitiveData stopped");
    }

}
