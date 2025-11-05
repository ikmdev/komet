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
import javafx.application.Platform;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ObservableEditableVersion and related editable classes.
 * Tests editing functionality with real entity data and database operations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(JavaFXThreadExtension.class)
class ObservableEditableVersionIT {

    private static final Logger LOG = LoggerFactory.getLogger(ObservableEditableVersionIT.class);
    private static final File TEST_DATA_DIR = new File("target/data");
    private static final File PB_STARTER_DATA = new File(TEST_DATA_DIR, "tinkar-starter-data-reasoned-pb.zip");

    private EntityCountSummary loadedEntitiesSummary;
    private ObservableConcept testConcept;
    private ObservableSemantic testSemantic;

    @BeforeAll
    void setupDatabase() {
        LOG.info("Setting up integration test environment for editable versions");

        // Setup ephemeral data store
        CachingService.clearAll();
        LOG.info("Cleared caches");

        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        LOG.info("Started PrimitiveData with ephemeral store");
    }

    @Test
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
    @Order(2)
    void testCreateTestEntities() throws Exception {
        runOnJavaFXThread(() -> {
            // Create composer for entity creation
            ObservableComposer composer = ObservableComposer.builder()
                    .stampCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
                    .author(TinkarTerm.USER)
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .defaultState(State.ACTIVE)
                    .transactionComment("Create test entities")
                    .build();

            // Create a test concept using the composer
            ObservableComposer.EntityComposer<ObservableEditableConceptVersion, ObservableConcept> conceptComposer =
                    composer.composeConcept(dev.ikm.tinkar.common.id.PublicIds.newRandom());
            ObservableEditableConceptVersion conceptVersion = conceptComposer.getEditableVersion();

            conceptVersion.save();
            composer.commit();

            testConcept = conceptComposer.getEntity();
            assertNotNull(testConcept);
            LOG.info("Created test concept with nid: {}", testConcept.nid());

            // Create a new composer for the semantic
            ObservableComposer composer2 = ObservableComposer.builder()
                    .stampCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
                    .author(TinkarTerm.USER)
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .defaultState(State.ACTIVE)
                    .transactionComment("Create test semantic")
                    .build();

            // Create a test semantic on the concept using the composer
            ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> semanticComposer =
                    composer2.composeSemantic(dev.ikm.tinkar.common.id.PublicIds.newRandom(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);
            ObservableEditableSemanticVersion semanticVersion = semanticComposer.getEditableVersion();

            // Set field values for the description semantic
            javafx.collections.ObservableList<ObservableEditableField<?>> fields = semanticVersion.getEditableFields();
            if (fields.size() >= 3) {
                ((ObservableEditableField<String>) fields.get(0)).setValue("Test semantic description");
                ((ObservableEditableField<Object>) fields.get(1)).setValue(TinkarTerm.ENGLISH_LANGUAGE);
                ((ObservableEditableField<Object>) fields.get(2)).setValue(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
                ((ObservableEditableField<Object>) fields.get(3)).setValue(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE);
            }

            semanticVersion.save();
            composer2.commit();

            testSemantic = semanticComposer.getEntity();
            assertNotNull(testSemantic);
            LOG.info("Created test semantic with nid: {} on concept nid: {}", testSemantic.nid(), testConcept.nid());
        });
    }

    @Test
    @Order(3)
    void testGetObservableConceptVersion() throws Exception {
        runOnJavaFXThread(() -> {
            assertNotNull(testConcept, "Test concept should be created");

            ObservableConceptVersion latestVersion = testConcept.versions().getLast();
            assertNotNull(latestVersion);

            LOG.info("Retrieved concept version with stamp nid: {}", latestVersion.stampNid());
        });
    }

    @Test
    @Order(4)
    void testCreateEditableConceptVersion() throws Exception {
        runOnJavaFXThread(() -> {
            assertNotNull(testConcept, "Test concept should be created");

            ObservableComposer composer = ObservableComposer.create(
                    Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                    State.ACTIVE,
                    TinkarTerm.USER,
                    TinkarTerm.PRIMORDIAL_MODULE,
                    TinkarTerm.DEVELOPMENT_PATH,
                    "Test editable concept version"
            );

            ObservableComposer.EntityComposer<ObservableEditableConceptVersion, ObservableConcept> editor =
                    composer.composeConcept(testConcept.publicId());
            assertNotNull(editor);

            ObservableEditableConceptVersion editableVersion = editor.getEditableVersion();
            assertNotNull(editableVersion);
            assertNotNull(editableVersion.getEditStamp());

            LOG.info("Created editable concept version");

            composer.cancel();
        });
    }

    @Test
    @Order(5)
    void testGetObservableSemanticVersion() throws Exception {
        runOnJavaFXThread(() -> {
            assertNotNull(testSemantic, "Test semantic should be created");

            ObservableSemanticVersion latestVersion = testSemantic.versions().getLast();
            assertNotNull(latestVersion);
            assertNotNull(latestVersion.fields());
            assertTrue(latestVersion.fields().size() > 0);

            LOG.info("Retrieved semantic version with {} fields", latestVersion.fields().size());
        });
    }

    @Test
    @Order(6)
    void testCreateEditableSemanticVersion() throws Exception {
        runOnJavaFXThread(() -> {
            assertNotNull(testSemantic, "Test semantic should be created");

            ObservableComposer composer = ObservableComposer.create(
                    Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                    State.ACTIVE,
                    TinkarTerm.USER,
                    TinkarTerm.PRIMORDIAL_MODULE,
                    TinkarTerm.DEVELOPMENT_PATH,
                    "Test editable semantic version"
            );

            ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> editor =
                    composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);
            assertNotNull(editor);

            ObservableEditableSemanticVersion editableVersion = editor.getEditableVersion();
            assertNotNull(editableVersion);
            assertNotNull(editableVersion.getEditStamp());

            LOG.info("Created editable semantic version");

            composer.cancel();
        });
    }

    @Test
    @Order(7)
    void testEditableSemanticFields() throws Exception {
        runOnJavaFXThread(() -> {
            assertNotNull(testSemantic, "Test semantic should be created");

            ObservableComposer composer = ObservableComposer.create(
                    Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                    State.ACTIVE,
                    TinkarTerm.USER,
                    TinkarTerm.PRIMORDIAL_MODULE,
                    TinkarTerm.DEVELOPMENT_PATH
            );

            ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> editor =
                    composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);
            ObservableEditableSemanticVersion editableVersion = editor.getEditableVersion();

            // Get editable fields
            javafx.collections.ObservableList<ObservableEditableField<?>> fields = editableVersion.getEditableFields();
            assertNotNull(fields);
            assertTrue(fields.size() > 0, "Should have editable fields");

            LOG.info("Editable semantic has {} fields", fields.size());

            // Test accessing individual fields
            for (int i = 0; i < fields.size(); i++) {
                ObservableEditableField<?> field = fields.get(i);
                assertNotNull(field);
                assertEquals(i, field.getFieldIndex());
                assertNotNull(field.editableValueProperty());

                LOG.info("Field {}: index={}, value={}", i, field.getFieldIndex(), field.getValue());
            }

            composer.cancel();
        });
    }

    @Test
    @Order(8)
    void testEditableFieldPropertyBinding() throws Exception {
        runOnJavaFXThread(() -> {
            assertNotNull(testSemantic, "Test semantic should be created");

            ObservableComposer composer = ObservableComposer.create(
                    Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                    State.ACTIVE,
                    TinkarTerm.USER,
                    TinkarTerm.PRIMORDIAL_MODULE,
                    TinkarTerm.DEVELOPMENT_PATH
            );

            ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> editor =
                    composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);
            ObservableEditableSemanticVersion editableVersion = editor.getEditableVersion();

            javafx.collections.ObservableList<ObservableEditableField<?>> fields = editableVersion.getEditableFields();

            if (fields.size() > 0) {
                ObservableEditableField<?> firstField = fields.get(0);
                Object originalValue = firstField.getValue();

                // Create a bound property
                javafx.beans.property.SimpleObjectProperty<Object> boundProperty =
                        new javafx.beans.property.SimpleObjectProperty<>();
                boundProperty.bind(firstField.editableValueProperty());

                assertEquals(originalValue, boundProperty.get());

                LOG.info("Field property binding working correctly");
            }

            composer.cancel();
        });
    }

    @Test
    @Order(9)
    void testEditableVersionIsDirty() throws Exception {
        runOnJavaFXThread(() -> {
            assertNotNull(testSemantic, "Test semantic should be created");

            ObservableComposer composer = ObservableComposer.create(
                    Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                    State.ACTIVE,
                    TinkarTerm.USER,
                    TinkarTerm.PRIMORDIAL_MODULE,
                    TinkarTerm.DEVELOPMENT_PATH
            );

            ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> editor =
                    composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);
            ObservableEditableSemanticVersion editableVersion = editor.getEditableVersion();

            // Initially should not be dirty
            assertFalse(editableVersion.isDirty(), "New editable version should not be dirty");

            // Modify a field
            javafx.collections.ObservableList<ObservableEditableField<?>> fields = editableVersion.getEditableFields();
            if (fields.size() > 0 && fields.get(0).getValue() instanceof String) {
                ObservableEditableField<String> field = (ObservableEditableField<String>) fields.get(0);
                String originalValue = field.getValue();
                field.setValue("Modified value");

                // Now should be dirty
                assertTrue(editableVersion.isDirty(), "Editable version should be dirty after modification");

                // Reset should make it not dirty again
                editableVersion.reset();
                assertFalse(editableVersion.isDirty(), "Editable version should not be dirty after reset");
                assertEquals(originalValue, field.getValue(), "Value should be restored after reset");

                LOG.info("isDirty() and reset() working correctly");
            }

            composer.cancel();
        });
    }

    @Test
    @Order(10)
    void testMultipleEditableVersionsWithDifferentStamps() throws Exception {
        runOnJavaFXThread(() -> {
            assertNotNull(testConcept, "Test concept should be created");

            ObservableComposer composer1 = ObservableComposer.create(
                    Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                    State.ACTIVE,
                    TinkarTerm.USER,
                    TinkarTerm.PRIMORDIAL_MODULE,
                    TinkarTerm.DEVELOPMENT_PATH,
                    "Composer 1"
            );

            ObservableComposer composer2 = ObservableComposer.create(
                    Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                    State.ACTIVE,
                    TinkarTerm.KOMET_USER,
                    TinkarTerm.PRIMORDIAL_MODULE,
                    TinkarTerm.DEVELOPMENT_PATH,
                    "Composer 2"
            );

            ObservableComposer.EntityComposer<ObservableEditableConceptVersion, ObservableConcept> editor1 =
                    composer1.composeConcept(testConcept.publicId());
            ObservableComposer.EntityComposer<ObservableEditableConceptVersion, ObservableConcept> editor2 =
                    composer2.composeConcept(testConcept.publicId());

            ObservableEditableConceptVersion editable1 = editor1.getEditableVersion();
            ObservableEditableConceptVersion editable2 = editor2.getEditableVersion();

            assertNotNull(editable1);
            assertNotNull(editable2);

            // Should have different stamps (different authors)
            assertNotEquals(editable1.getEditStamp().nid(), editable2.getEditStamp().nid(),
                    "Editable versions with different authors should have different stamps");

            LOG.info("Multiple editable versions with different stamps work correctly");

            composer1.cancel();
            composer2.cancel();
        });
    }

    @AfterAll
    void tearDownDatabase() {
        LOG.info("Tearing down integration test environment");
        PrimitiveData.stop();
        LOG.info("PrimitiveData stopped");
    }

    /**
     * Helper method to run code on JavaFX thread and wait for completion.
     */
    private void runOnJavaFXThread(Runnable runnable) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(100, TimeUnit.SECONDS), "Test timed out");

        if (exception.get() != null) {
            throw exception.get();
        }
    }
}
