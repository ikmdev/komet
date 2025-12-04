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
package dev.ikm.tinkar.integration.changeSet;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.entity.transform.EntityToTinkarSchemaTransformer;
import dev.ikm.tinkar.integration.StarterDataEphemeralProvider;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test for forward reference handling in changeset imports.
 * Tests the multi-pass import mechanism that resolves forward references where
 * a semantic references a concept that appears later in the changeset.
 *
 * The multi-pass algorithm:
 * - Pass 1: Imports all non-semantics (Concepts, Patterns, Stamps)
 * - Pass 2+: Imports semantics whose referenced components exist in the database
 * - Repeats until all semantics are imported or no progress is made
 *
 * Tests are ordered to ensure the 1-pass test runs first (before entities exist in datastore).
 */
@ExtendWith(StarterDataEphemeralProvider.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ForwardReferenceChangeSetIT {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardReferenceChangeSetIT.class);

    @TempDir
    Path tempDir;

    private File changesetFile;
    private PublicId newConceptPublicId;
    private PublicId descriptionSemanticPublicId;
    private StampEntity testStamp;

    @BeforeEach
    void setUp() {
        // Just create unique IDs - entities will be created when changeset is generated
        newConceptPublicId = PublicIds.of(UUID.randomUUID());
        descriptionSemanticPublicId = PublicIds.of(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        if (changesetFile != null && changesetFile.exists()) {
            changesetFile.delete();
        }
    }

    /**
     * Test that 1-pass import fails when encountering a forward reference.
     * The semantic is written before the concept it references, causing
     * the transformer to fail when trying to resolve the concept.
     *
     * This test runs first to ensure the entities don't exist in the datastore yet.
     */
    @Test
    @Order(1)
    @DisplayName("1-pass import should fail with forward reference")
    void testOnePassImportFailsWithForwardReference() throws IOException {
        LOG.info("Testing 1-pass import with forward reference - expecting failure");

        // Create the changeset file with forward reference
        changesetFile = createChangeSetWithForwardReference();

        // Create loader with 1-pass mode (useTwoPassImport = false)
        LoadEntitiesFromProtobufFile loader = new LoadEntitiesFromProtobufFile(changesetFile, false);

        // Should throw exception when trying to resolve the concept that doesn't exist yet
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loader.compute();
        });

        LOG.info("1-pass import failed as expected: {}", exception.getMessage());
        assertNotNull(exception);
    }

    /**
     * Test that multi-pass import succeeds with forward references.
     * Pass 1: Imports all non-semantics (concept exists)
     * Pass 2: Imports the semantic that references the now-existing concept
     *
     * This test runs second, after the 1-pass test has demonstrated the failure scenario.
     */
    @Test
    @Order(2)
    @DisplayName("Multi-pass import should succeed with forward reference")
    void testMultiPassImportSucceedsWithForwardReference() throws IOException {
        LOG.info("Testing multi-pass import with forward reference - expecting success");

        // Create the changeset file with forward reference
        changesetFile = createChangeSetWithForwardReference();

        // Create loader with multi-pass mode (default)
        LoadEntitiesFromProtobufFile loader = new LoadEntitiesFromProtobufFile(changesetFile, true);

        // Should succeed - Pass 1 imports concept, Pass 2 imports semantic
        var summary = loader.compute();

        LOG.info("Multi-pass import succeeded: {}", summary);
        assertNotNull(summary);

        // Verify both entities were loaded using EntityHandle
        ConceptEntity loadedConcept = EntityHandle.get(newConceptPublicId).expectConcept();
        SemanticEntity loadedSemantic = EntityHandle.get(descriptionSemanticPublicId).expectSemantic();

        assertNotNull(loadedConcept, "New concept should be loaded");
        assertNotNull(loadedSemantic, "Description semantic should be loaded");

        LOG.info("Successfully loaded concept: {}", loadedConcept);
        LOG.info("Successfully loaded semantic: {}", loadedSemantic);
    }

    /**
     * Creates a protobuf changeset file with a forward reference scenario.
     * The semantic description is written BEFORE the concept it references,
     * simulating a forward reference issue.
     */
    private File createChangeSetWithForwardReference() throws IOException {
        // Create a test stamp - now that the datastore is initialized
        testStamp = StampRecord.make(
                UUID.randomUUID(),
                State.ACTIVE,
                System.currentTimeMillis(),
                TinkarTerm.USER.publicId(),
                TinkarTerm.PRIMORDIAL_MODULE.publicId(),
                TinkarTerm.DEVELOPMENT_PATH.publicId()
        );
        EntityService.get().putEntity(testStamp);

        File changesetFile = tempDir.resolve("forward-reference-changeset.zip").toFile();

        try (FileOutputStream fos = new FileOutputStream(changesetFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Create and write manifest
            writeManifest(zos);

            // Start protobuf data entry
            ZipEntry dataEntry = new ZipEntry("changeset.pb");
            zos.putNextEntry(dataEntry);

            EntityToTinkarSchemaTransformer transformer = EntityToTinkarSchemaTransformer.getInstance();

            // FORWARD REFERENCE: Write description semantic BEFORE the concept
            SemanticRecord descriptionSemantic = createDescriptionSemantic();
            TinkarMsg semanticMsg = transformer.transform(descriptionSemantic);
            semanticMsg.writeDelimitedTo(zos);
            LOG.info("Wrote description semantic BEFORE concept (forward reference)");

            // Write the new concept AFTER the semantic that references it
            ConceptRecord newConcept = createNewConcept();
            TinkarMsg conceptMsg = transformer.transform(newConcept);
            conceptMsg.writeDelimitedTo(zos);
            LOG.info("Wrote concept AFTER description semantic");

            zos.closeEntry();
        }

        LOG.info("Created changeset with forward reference at: {}", changesetFile.getAbsolutePath());
        return changesetFile;
    }

    /**
     * Creates a new concept entity.
     */
    private ConceptRecord createNewConcept() {
        return ConceptRecord.build(
                newConceptPublicId,
                testStamp.lastVersion()
        );
    }

    /**
     * Creates a description semantic that references the new concept.
     * This semantic uses the DESCRIPTION_PATTERN from starter data.
     */
    private SemanticRecord createDescriptionSemantic() {
        // Create field values for description pattern
        // Description pattern fields: language, text, case significance, description type
        ImmutableList<Object> fieldValues = org.eclipse.collections.api.factory.Lists.immutable.of(
                TinkarTerm.ENGLISH_LANGUAGE.nid(),  // Language
                "Test Description for Forward Reference", // Text
                TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE.nid(), // Case significance
                TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE.nid()  // Description type
        );

        // Note: We need to get the NID for the concept that doesn't exist yet
        // This will be registered in Pass 1 of the 2-pass import
        int conceptNid = EntityService.get().nidForPublicId(newConceptPublicId);

        return SemanticRecord.build(
                descriptionSemanticPublicId.asUuidArray()[0],
                TinkarTerm.DESCRIPTION_PATTERN.nid(),
                conceptNid,  // References the concept that will be written AFTER this semantic
                testStamp.lastVersion(),
                fieldValues
        );
    }

    /**
     * Writes the manifest entry to the changeset zip file.
     */
    private void writeManifest(ZipOutputStream zos) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Total-Count", "2"); // 1 semantic + 1 concept

        ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
        zos.putNextEntry(manifestEntry);
        manifest.write(zos);
        zos.closeEntry();
    }
}
