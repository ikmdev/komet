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
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ObservableField.Editable behavior.
 * <p>
 * Tests the editable field functionality within the Observable framework,
 * including property binding, change tracking, value modification, and
 * integration with semantic version editing.
 * <p>
 * Uses real Tinkar entities loaded from test data to ensure the Observable
 * framework works correctly with actual database operations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(JavaFXThreadExtension.class)
class ObservableEditableFieldTestFX {

    private static final Logger LOG = LoggerFactory.getLogger(ObservableEditableFieldTestFX.class);
    private static final File TEST_DATA_DIR = new File("target/data");
    private static final File PB_STARTER_DATA = new File(TEST_DATA_DIR, "tinkar-starter-data-reasoned-pb.zip");

    private EntityCountSummary loadedEntitiesSummary;
    private ObservableConcept testConcept;
    private ObservableSemantic testSemantic;
    private ObservableComposer testComposer;

    @BeforeAll
    void setupDatabase() {
        LOG.info("Setting up integration test environment for ObservableField.Editable");

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
    @RunOnJavaFXThread
    void createTestSemanticWithFields() {
        // Create a composer for entity creation
        testComposer = ObservableComposer.builder()
                .viewCalculator(Calculators.View.Default())
                .author(TinkarTerm.USER)
                .module(TinkarTerm.PRIMORDIAL_MODULE)
                .path(TinkarTerm.DEVELOPMENT_PATH)
                .defaultState(State.ACTIVE)
                .transactionComment("Create test semantic for field testing")
                .build();

        // Create a test concept
        ObservableComposer.EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> conceptComposer =
                testComposer.composeConcept(dev.ikm.tinkar.common.id.PublicIds.newRandom());
        conceptComposer.save();
        testConcept = conceptComposer.getEntity();
        assertNotNull(testConcept);
        LOG.info("Created test concept with nid: {}", testConcept.nid());

        // Create a test semantic with multiple fields (using description pattern)
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticComposer =
                testComposer.composeSemantic(
                        dev.ikm.tinkar.common.id.PublicIds.newRandom(),
                        testConcept,
                        TinkarTerm.DESCRIPTION_PATTERN
                );

        // Get editable fields and set initial values
        ObservableSemanticVersion.Editable editableVersion = semanticComposer.getEditableVersion();
        ObservableList<ObservableField.Editable<?>> fields = editableVersion.getEditableFields();

        LOG.info("Created semantic with {} editable fields", fields.size());
        assertTrue(fields.size() >= 4, "Description pattern should have at least 4 fields");

        // Initialize field values
        ((ObservableField.Editable<String>) fields.get(0)).setValue("Initial description text");
        ((ObservableField.Editable<Object>) fields.get(1)).setValue(TinkarTerm.ENGLISH_LANGUAGE);
        ((ObservableField.Editable<Object>) fields.get(2)).setValue(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
        ((ObservableField.Editable<Object>) fields.get(3)).setValue(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE);

        semanticComposer.save();
        testComposer.commit();

        testSemantic = semanticComposer.getEntity();
        assertNotNull(testSemantic);
        LOG.info("Created test semantic with nid: {}", testSemantic.nid());
    }

    @Test
    @Order(3)
    @RunOnJavaFXThread
    void testFieldIndexTracking() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test field index tracking");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();

        // Verify field indices match their positions
        for (int i = 0; i < fields.size(); i++) {
            ObservableField.Editable<?> field = fields.get(i);
            assertEquals(i, field.getFieldIndex(), "Field index should match position in list");
            LOG.debug("Field {} has index {}", i, field.getFieldIndex());
        }

        LOG.info("✓ Field index tracking working correctly");
        composer.cancel();
    }

    @Test
    @Order(4)
    @RunOnJavaFXThread
    void testEditableValuePropertyNotNull() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test editable value property");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();

        for (ObservableField.Editable<?> field : fields) {
            assertNotNull(field.editableValueProperty(), "Editable value property should not be null");
            assertNotNull(field.getObservableFeature(), "Observable feature reference should not be null");
        }

        LOG.info("✓ Editable value properties properly initialized");
        composer.cancel();
    }

    @Test
    @Order(5)
    @RunOnJavaFXThread
    void testGetAndSetValue() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test get and set value");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();

        // Test string field (index 0 - description text)
        ObservableField.Editable<String> textField = (ObservableField.Editable<String>) fields.get(0);
        String originalValue = textField.getValue();
        assertNotNull(originalValue, "Initial value should not be null");

        String newValue = "Modified description text for testing";
        textField.setValue(newValue);

        assertEquals(newValue, textField.getValue(), "Value should be updated");
        assertNotEquals(originalValue, textField.getValue(), "Value should have changed");

        LOG.info("✓ Get and set value working correctly");
        composer.cancel();
    }

    @Test
    @Order(6)
    @RunOnJavaFXThread
    void testPropertyChangeListener() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test property change listener");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();
        ObservableField.Editable<String> textField = (ObservableField.Editable<String>) fields.get(0);

        // Setup listener
        AtomicInteger listenerCallCount = new AtomicInteger(0);
        AtomicReference<String> capturedOldValue = new AtomicReference<>();
        AtomicReference<String> capturedNewValue = new AtomicReference<>();

        textField.editableValueProperty().addListener((obs, oldVal, newVal) -> {
            listenerCallCount.incrementAndGet();
            capturedOldValue.set(oldVal);
            capturedNewValue.set(newVal);
            LOG.debug("Listener fired: {} -> {}", oldVal, newVal);
        });

        String originalValue = textField.getValue();
        String newValue = "Updated value to test listener";

        textField.setValue(newValue);

        assertEquals(1, listenerCallCount.get(), "Listener should be called once");
        assertEquals(originalValue, capturedOldValue.get(), "Old value should match original");
        assertEquals(newValue, capturedNewValue.get(), "New value should match set value");

        LOG.info("✓ Property change listener working correctly");
        composer.cancel();
    }

    @Test
    @Order(7)
    @RunOnJavaFXThread
    void testMultipleValueChanges() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test multiple value changes");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();
        ObservableField.Editable<String> textField = (ObservableField.Editable<String>) fields.get(0);

        AtomicInteger listenerCallCount = new AtomicInteger(0);
        textField.editableValueProperty().addListener((obs, oldVal, newVal) -> {
            listenerCallCount.incrementAndGet();
        });

        textField.setValue("Change 1");
        textField.setValue("Change 2");
        textField.setValue("Change 3");
        textField.setValue("Change 4");

        assertEquals(4, listenerCallCount.get(), "Listener should be called for each change");
        assertEquals("Change 4", textField.getValue(), "Final value should be the last set value");

        LOG.info("✓ Multiple value changes tracked correctly");
        composer.cancel();
    }

    @Test
    @Order(8)
    @RunOnJavaFXThread
    void testBidirectionalPropertyBinding() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test bidirectional binding");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();
        ObservableField.Editable<String> textField = (ObservableField.Editable<String>) fields.get(0);

        // Create external property and bind bidirectionally
        SimpleObjectProperty<String> externalProperty = new SimpleObjectProperty<>("external value");
        textField.editableValueProperty().bindBidirectional(externalProperty);

        // Field should adopt external property's value
        assertEquals("external value", textField.getValue());

        // Changing field updates external property
        textField.setValue("from field");
        assertEquals("from field", externalProperty.get());

        // Changing external property updates field
        externalProperty.set("from external");
        assertEquals("from external", textField.getValue());

        LOG.info("✓ Bidirectional property binding working correctly");
        composer.cancel();
    }

    @Test
    @Order(9)
    @RunOnJavaFXThread
    void testUnidirectionalPropertyBinding() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test unidirectional binding");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();
        ObservableField.Editable<String> textField = (ObservableField.Editable<String>) fields.get(0);

        // Create source property and bind unidirectionally
        SimpleObjectProperty<String> sourceProperty = new SimpleObjectProperty<>("source value");
        textField.editableValueProperty().bind(sourceProperty);

        assertEquals("source value", textField.getValue());

        // Changing source updates field
        sourceProperty.set("updated source");
        assertEquals("updated source", textField.getValue());

        LOG.info("✓ Unidirectional property binding working correctly");
        composer.cancel();
    }

    @Test
    @Order(10)
    @RunOnJavaFXThread
    void testFieldIsDirty() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test field hasUnsavedChanges");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableField.Editable<String> textField =
                (ObservableField.Editable<String>) editor.getEditableVersion().getEditableFields().get(0);

        String originalValue = textField.getValue();

        // Initially should not be dirty
        assertFalse(textField.hasUnsavedChanges(), "Field should not be dirty initially");

        // Modify the value
        textField.setValue("Modified value");
        assertTrue(textField.hasUnsavedChanges(), "Field should be dirty after modification");

        // Reset to original
        textField.reset();
        assertFalse(textField.hasUnsavedChanges(), "Field should not be dirty after reset");
        assertEquals(originalValue, textField.getValue(), "Value should be restored after reset");

        LOG.info("✓ Field hasUnsavedChanges and reset working correctly");
        composer.cancel();
    }

    @Test
    @Order(11)
    @RunOnJavaFXThread
    void testFieldReset() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test field reset");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();
        ObservableField.Editable<String> textField = (ObservableField.Editable<String>) fields.get(0);

        String originalValue = textField.getValue();

        // Make multiple changes
        textField.setValue("Change 1");
        textField.setValue("Change 2");
        textField.setValue("Change 3");

        assertNotEquals(originalValue, textField.getValue());

        // Reset should restore original
        textField.reset();
        assertEquals(originalValue, textField.getValue(), "Reset should restore original value");

        LOG.info("✓ Field reset working correctly");
        composer.cancel();
    }

    @Test
    @Order(12)
    @RunOnJavaFXThread
    void testMultipleFieldsIndependence() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test multiple fields independence");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();

        // Get multiple fields
        ObservableField.Editable<String> field0 = (ObservableField.Editable<String>) fields.get(0);
        ObservableField.Editable<?> field1 = fields.get(1);
        ObservableField.Editable<?> field2 = fields.get(2);

        String original0 = field0.getValue();
        Object original1 = field1.getValue();
        Object original2 = field2.getValue();

        // Modify field 0
        field0.setValue("Modified field 0");

        // Other fields should be unchanged
        assertEquals(original1, field1.getValue(), "Field 1 should be unchanged");
        assertEquals(original2, field2.getValue(), "Field 2 should be unchanged");

        // Field 0 should be dirty, others should not
        assertTrue(field0.hasUnsavedChanges(), "Modified field should be dirty");
        assertFalse(field1.hasUnsavedChanges(), "Unmodified field should not be dirty");
        assertFalse(field2.hasUnsavedChanges(), "Unmodified field should not be dirty");

        LOG.info("✓ Multiple fields maintain independence");
        composer.cancel();
    }

    @Test
    @Order(13)
    @RunOnJavaFXThread
    void testFieldGetObservableFeature() {
        assertNotNull(testSemantic, "Test semantic should be created");

        ObservableComposer composer = createComposer("Test getObservableFeature");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor =
                composer.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableList<ObservableField.Editable<?>> fields = editor.getEditableVersion().getEditableFields();

        for (ObservableField.Editable<?> editableField : fields) {
            ObservableField<?> observableField = editableField.getObservableFeature();
            assertNotNull(observableField, "Should have reference to observable field");
            assertNotNull(observableField.field(), "Observable field should have underlying field data");
        }

        LOG.info("✓ getObservableFeature() returns valid references");
        composer.cancel();
    }

    @Test
    @Order(14)
    @RunOnJavaFXThread
    void testFieldValuePersistence() {
        assertNotNull(testSemantic, "Test semantic should be created");

        // Create first composer and modify a field
        ObservableComposer composer1 = createComposer("Test field persistence - modify");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor1 =
                composer1.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableField.Editable<String> field1 =
                (ObservableField.Editable<String>) editor1.getEditableVersion().getEditableFields().get(0);

        String newValue = "Persisted value test " + System.currentTimeMillis();
        field1.setValue(newValue);

        editor1.save();
        composer1.commit();

        // Create second composer and verify the value persisted
        ObservableComposer composer2 = createComposer("Test field persistence - verify");
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> editor2 =
                composer2.composeSemantic(testSemantic.publicId(), testConcept, TinkarTerm.DESCRIPTION_PATTERN);

        ObservableField.Editable<String> field2 =
                (ObservableField.Editable<String>) editor2.getEditableVersion().getEditableFields().get(0);

        assertEquals(newValue, field2.getValue(), "Field value should persist across composer instances");

        LOG.info("✓ Field value persistence working correctly");
        composer2.cancel();
    }

    /**
     * Helper method to create a composer with standard test configuration.
     */
    private ObservableComposer createComposer(String transactionComment) {
        return ObservableComposer.builder()
                .viewCalculator(Calculators.View.Default())
                .author(TinkarTerm.USER)
                .module(TinkarTerm.PRIMORDIAL_MODULE)
                .path(TinkarTerm.DEVELOPMENT_PATH)
                .defaultState(State.ACTIVE)
                .transactionComment(transactionComment)
                .build();
    }

    @AfterAll
    void tearDownDatabase() {
        LOG.info("Tearing down integration test environment");
        PrimitiveData.stop();
        LOG.info("PrimitiveData stopped");
    }
}
