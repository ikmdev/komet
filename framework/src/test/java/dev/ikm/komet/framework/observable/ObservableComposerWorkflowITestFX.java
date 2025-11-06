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
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;

/**
 * Integration tests for complete ObservableComposer workflows.
 * Tests end-to-end scenarios: create → edit → save → commit → verify.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(JavaFXThreadExtension.class)
class ObservableComposerWorkflowITestFX {

    private static final Logger LOG = LoggerFactory.getLogger(ObservableComposerWorkflowITestFX.class);
    private static final File TEST_DATA_DIR = new File("target/data");
    private static final File PB_STARTER_DATA = new File(TEST_DATA_DIR, "tinkar-starter-data-reasoned-pb.zip");

    private EntityCountSummary loadedEntitiesSummary;

    @BeforeAll
    void setupDatabase() {
        LOG.info("Setting up integration test environment for workflow tests");

        // Setup ephemeral data store
        CachingService.clearAll();
        LOG.info("Cleared caches");

        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        LOG.info("Started PrimitiveData with ephemeral store");
    }

    @Test
    @RunOnJavaFXThread
    @Order(1)
    void loadTestData() {
        assertTrue(PB_STARTER_DATA.exists(),
                "Test data file not found at: " + PB_STARTER_DATA.getAbsolutePath() +
                ". Ensure maven-dependency-plugin has downloaded tinkar-starter-data.");

        LOG.info("Loading test data from: {}", PB_STARTER_DATA.getAbsolutePath());
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA);
        loadedEntitiesSummary = loadProto.compute();
        LOG.info("{} entities loaded: {}", loadedEntitiesSummary.getTotalCount(), loadProto.summarize());

        assertTrue(loadedEntitiesSummary.getTotalCount() > 0, "Should load entities from protobuf file");
    }

    @Test
    @RunOnJavaFXThread
    @Order(2)
    void testCompleteCreateConceptWorkflow() {

    LOG.info("=== Testing Complete Create Concept Workflow ===");

    // 1. Create composer
    ObservableComposer composer = ObservableComposer.builder()
            .stampCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
            .author(TinkarTerm.USER)
            .module(TinkarTerm.PRIMORDIAL_MODULE)
            .path(TinkarTerm.DEVELOPMENT_PATH)
            .defaultState(State.ACTIVE)
            .transactionComment("Create concept workflow test")
            .build();

    assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());

    // 2. Create a concept using the composer
    ObservableComposer.EntityComposer<ObservableEditableConceptVersion, ObservableConcept> conceptComposer =
            composer.composeConcept(dev.ikm.tinkar.common.id.PublicIds.newRandom());
    ObservableConcept observableConcept = conceptComposer.getEntity();

    assertNotNull(observableConcept);
    LOG.info("Step 1: Created concept with nid: {}", observableConcept.nid());

    // 3. Get the editable version
    ObservableEditableConceptVersion editableVersion = conceptComposer.getEditableVersion();

    assertNotNull(editableVersion);
    LOG.info("Step 2: Created editable version");

    // 4. Verify transaction state
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer.getTransactionState());
    LOG.info("Step 3: Transaction state is UNCOMMITTED");

    // 5. Clean up
    composer.cancel();
    assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());
    LOG.info("Step 4: Transaction cancelled, state is NONE");

    LOG.info("=== Create Concept Workflow Complete ===\n");
    }

    @Test
    @RunOnJavaFXThread
    @Order(3)
    void testCompleteEditSemanticWorkflow() {

    LOG.info("=== Testing Complete Edit Semantic Workflow ===");

    // 1. Create a concept first using composer
    ObservableComposer composer1 = ObservableComposer.builder()
            .stampCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
            .author(TinkarTerm.USER)
            .module(TinkarTerm.PRIMORDIAL_MODULE)
            .path(TinkarTerm.DEVELOPMENT_PATH)
            .defaultState(State.ACTIVE)
            .transactionComment("Create concept for semantic test")
            .build();

    ObservableComposer.EntityComposer<ObservableEditableConceptVersion, ObservableConcept> conceptComposer =
            composer1.composeConcept(dev.ikm.tinkar.common.id.PublicIds.newRandom());
    conceptComposer.getEditableVersion().save();
    composer1.commit();
    ObservableConcept observableConcept = conceptComposer.getEntity();
    LOG.info("Step 1: Created concept with nid: {}", observableConcept.nid());

    // 2. Create a semantic on the concept using composer
    ObservableComposer composer2 = ObservableComposer.builder()
            .stampCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
            .author(TinkarTerm.USER)
            .module(TinkarTerm.PRIMORDIAL_MODULE)
            .path(TinkarTerm.DEVELOPMENT_PATH)
            .defaultState(State.ACTIVE)
            .transactionComment("Create semantic for test")
            .build();

    ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> semanticComposer =
            composer2.composeSemantic(dev.ikm.tinkar.common.id.PublicIds.newRandom(), observableConcept, TinkarTerm.DESCRIPTION_PATTERN);
    ObservableEditableSemanticVersion semanticVersion = semanticComposer.getEditableVersion();

    // Set field values
    javafx.collections.ObservableList<ObservableEditableField<?>> fields = semanticVersion.getEditableFields();
    if (fields.size() >= 3) {
        ((ObservableEditableField<String>) fields.get(0)).setValue("Original description");
        ((ObservableEditableField<Object>) fields.get(1)).setValue(TinkarTerm.ENGLISH_LANGUAGE);
        ((ObservableEditableField<Object>) fields.get(2)).setValue(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
    }

    semanticVersion.save();
    composer2.commit();
    ObservableSemantic observableSemantic = semanticComposer.getEntity();
    LOG.info("Step 2: Created semantic with nid: {}", observableSemantic.nid());

    // 3. Create composer and edit semantic
    ObservableComposer composer3 = ObservableComposer.create(
            Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH,
            "Edit semantic workflow test"
    );

    ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> editor =
            composer3.composeSemantic(observableSemantic.publicId(), observableConcept, TinkarTerm.DESCRIPTION_PATTERN);
    ObservableEditableSemanticVersion editableVersion = editor.getEditableVersion();
    LOG.info("Step 3: Created editable semantic version");

    // 4. Get and verify editable fields
    javafx.collections.ObservableList<ObservableEditableField<?>> editableFields = editableVersion.getEditableFields();
    assertTrue(editableFields.size() > 0, "Should have editable fields");
    LOG.info("Step 4: Editable semantic has {} fields", editableFields.size());

    // 5. Verify first field (description text)
    if (editableFields.size() > 0) {
        ObservableEditableField<?> firstField = editableFields.get(0);
        assertNotNull(firstField.getValue());
        LOG.info("Step 5: First field value: {}", firstField.getValue());
    }

    // 6. Verify not dirty initially
    assertFalse(editableVersion.isDirty(), "Should not be dirty initially");
    LOG.info("Step 6: Editable version is not dirty");

    // 7. Clean up
    composer3.cancel();
    LOG.info("Step 7: Transaction cancelled");

    LOG.info("=== Edit Semantic Workflow Complete ===\n");
    }

    @Test
    @RunOnJavaFXThread
    @Order(4)
    void testPropertyNotificationWorkflow() {

    LOG.info("=== Testing Property Notification Workflow ===");

    ObservableComposer composer = ObservableComposer.create(
            Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH,
            "Property notification test"
    );

    // Track notifications
    AtomicInteger stateChangeCount = new AtomicInteger(0);
    AtomicInteger hasChangesCount = new AtomicInteger(0);

    composer.transactionStateProperty().addListener((obs, oldVal, newVal) -> {
        stateChangeCount.incrementAndGet();
        LOG.info("State changed: {} → {}", oldVal, newVal);
    });

    composer.hasUncommittedChangesProperty().addListener((obs, oldVal, newVal) -> {
        hasChangesCount.incrementAndGet();
        LOG.info("Has changes: {} → {}", oldVal, newVal);
    });

    // Trigger notifications
    composer.getOrCreateTransaction();
    assertTrue(stateChangeCount.get() > 0, "Should have state change notifications");

    composer.cancel();
    assertTrue(stateChangeCount.get() >= 2, "Should have multiple state changes");

    LOG.info("Total state changes: {}", stateChangeCount.get());
    LOG.info("=== Property Notification Workflow Complete ===\n");
    }

    @Test
    @RunOnJavaFXThread
    @Order(5)
    void testMultipleComposersWorkflow() {

    LOG.info("=== Testing Multiple Composers Workflow ===");

    // Create multiple composers for different tasks
    ObservableComposer composer1 = ObservableComposer.create(
            Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH,
            "Composer 1 - User edits"
    );

    ObservableComposer composer2 = ObservableComposer.create(
            Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
            State.ACTIVE,
            TinkarTerm.KOMET_USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH,
            "Composer 2 - System edits"
    );

    composer1.getOrCreateTransaction();
    composer2.getOrCreateTransaction();

    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer1.getTransactionState());
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer2.getTransactionState());

    LOG.info("Both composers have independent transactions");

    // Cancel first composer
    composer1.cancel();
    assertEquals(ObservableComposer.TransactionState.NONE, composer1.getTransactionState());
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer2.getTransactionState());

    LOG.info("Composer 1 cancelled, Composer 2 still active");

    // Cancel second composer
    composer2.cancel();
    assertEquals(ObservableComposer.TransactionState.NONE, composer2.getTransactionState());

    LOG.info("Both composers cancelled");
    LOG.info("=== Multiple Composers Workflow Complete ===\n");
    }

    @Test
    @RunOnJavaFXThread
    @Order(6)
    void testEditableFieldModificationWorkflow() {

    LOG.info("=== Testing Editable Field Modification Workflow ===");

    // Create entities using composer
    ObservableComposer composer1 = ObservableComposer.builder()
            .stampCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
            .author(TinkarTerm.USER)
            .module(TinkarTerm.PRIMORDIAL_MODULE)
            .path(TinkarTerm.DEVELOPMENT_PATH)
            .defaultState(State.ACTIVE)
            .transactionComment("Create entities for field modification test")
            .build();

    ObservableComposer.EntityComposer<ObservableEditableConceptVersion, ObservableConcept> conceptComposer =
            composer1.composeConcept(dev.ikm.tinkar.common.id.PublicIds.newRandom());
    conceptComposer.getEditableVersion().save();
    composer1.commit();
    ObservableConcept observableConcept = conceptComposer.getEntity();

    ObservableComposer composer2 = ObservableComposer.builder()
            .stampCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
            .author(TinkarTerm.USER)
            .module(TinkarTerm.PRIMORDIAL_MODULE)
            .path(TinkarTerm.DEVELOPMENT_PATH)
            .defaultState(State.ACTIVE)
            .transactionComment("Create semantic for field modification test")
            .build();

    ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> semanticComposer =
            composer2.composeSemantic(dev.ikm.tinkar.common.id.PublicIds.newRandom(), observableConcept, TinkarTerm.DESCRIPTION_PATTERN);
    ObservableEditableSemanticVersion semanticVersion = semanticComposer.getEditableVersion();

    // Set field values
    javafx.collections.ObservableList<ObservableEditableField<?>> initialFields = semanticVersion.getEditableFields();
    if (initialFields.size() >= 3) {
        ((ObservableEditableField<String>) initialFields.get(0)).setValue("Test description");
        ((ObservableEditableField<Object>) initialFields.get(1)).setValue(TinkarTerm.ENGLISH_LANGUAGE);
        ((ObservableEditableField<Object>) initialFields.get(2)).setValue(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
    }

    semanticVersion.save();
    composer2.commit();
    ObservableSemantic observableSemantic = semanticComposer.getEntity();

    // Edit semantic
    ObservableComposer composer = ObservableComposer.create(
            Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH
    );

    ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> editor =
            composer.composeSemantic(observableSemantic.publicId(), observableConcept, TinkarTerm.DESCRIPTION_PATTERN);
    ObservableEditableSemanticVersion editableVersion = editor.getEditableVersion();
    javafx.collections.ObservableList<ObservableEditableField<?>> fields = editableVersion.getEditableFields();

    if (fields.size() > 0 && fields.get(0).getValue() instanceof String) {
        ObservableEditableField<String> field = (ObservableEditableField<String>) fields.get(0);
        String originalValue = field.getValue();
        LOG.info("Original value: {}", originalValue);

        // Track field changes
        AtomicInteger changeCount = new AtomicInteger(0);
        field.editableValueProperty().addListener((obs, oldVal, newVal) -> {
            changeCount.incrementAndGet();
            LOG.info("Field changed: {} → {}", oldVal, newVal);
        });

        // Modify field
        field.setValue("Modified description");
        assertEquals(1, changeCount.get(), "Should have one change notification");
        assertTrue(editableVersion.isDirty(), "Version should be dirty after change");

        // Reset field
        editableVersion.reset();
        assertEquals(originalValue, field.getValue(), "Value should be restored");
        assertFalse(editableVersion.isDirty(), "Version should not be dirty after reset");

        LOG.info("Field modification and reset successful");
    }

    composer.cancel();
    LOG.info("=== Editable Field Modification Workflow Complete ===\n");
    }

    @Test
    @RunOnJavaFXThread
    @Order(7)
    void testTransactionStateTransitions() {

    LOG.info("=== Testing Transaction State Transitions ===");

    ObservableComposer composer = ObservableComposer.create(
            Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
            State.ACTIVE,
            TinkarTerm.USER,
            TinkarTerm.PRIMORDIAL_MODULE,
            TinkarTerm.DEVELOPMENT_PATH
    );

    // Track all state transitions
    AtomicReference<String> transitions = new AtomicReference<>("");
    composer.transactionStateProperty().addListener((obs, oldVal, newVal) -> {
        String transition = oldVal + " → " + newVal;
        transitions.updateAndGet(s -> s.isEmpty() ? transition : s + ", " + transition);
    });

    // State 1: NONE
    assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());

    // Transition: NONE → UNCOMMITTED
    composer.getOrCreateTransaction();
    assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer.getTransactionState());

    // Transition: UNCOMMITTED → NONE
    composer.cancel();
    assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());

    LOG.info("State transitions: {}", transitions.get());
    assertTrue(transitions.get().contains("NONE → UNCOMMITTED"), "Should have NONE → UNCOMMITTED transition");
    assertTrue(transitions.get().contains("UNCOMMITTED → NONE"), "Should have UNCOMMITTED → NONE transition");

    LOG.info("=== Transaction State Transitions Complete ===\n");
    }

    @AfterAll
    void tearDownDatabase() {
        LOG.info("Tearing down integration test environment");
        PrimitiveData.stop();
        LOG.info("PrimitiveData stopped");
    }

}
