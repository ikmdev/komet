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
package dev.ikm.tinkar.entity.load;

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.DataActivity;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transform.TinkarSchemaToEntityTransformer;
import dev.ikm.tinkar.schema.TinkarMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The purpose of this class is to successfully load all Protobuf messages from a protobuf file and transform them into entities.
 */
public class LoadEntitiesFromProtobufFile extends TrackingCallable<EntityCountSummary> {

    protected static final Logger LOG = LoggerFactory.getLogger(LoadEntitiesFromProtobufFile.class.getName());

    private final TinkarSchemaToEntityTransformer entityTransformer =
            TinkarSchemaToEntityTransformer.getInstance();
    private static final String MANIFEST_RELPATH = "META-INF/MANIFEST.MF";
    private final File importFile;
    private final AtomicLong importCount = new AtomicLong();
    private final AtomicLong importConceptCount = new AtomicLong();
    private final AtomicLong importSemanticCount = new AtomicLong();
    private final AtomicLong importPatternCount = new AtomicLong();
    private final AtomicLong importStampCount = new AtomicLong();
    
    private final AtomicLong identifierCount = new AtomicLong();
    private final boolean useMultiPassImport;

    /**
     * Create a loader with default multi-pass import mode.
     * @param importFile the protobuf file to import
     */
    public LoadEntitiesFromProtobufFile(File importFile) {
        this(importFile, true);
    }

    /**
     * Create a loader with configurable pass mode.
     * @param importFile the protobuf file to import
     * @param useMultiPassImport if true, use multi-pass import (default); if false, use 1-pass import
     */
    public LoadEntitiesFromProtobufFile(File importFile, boolean useMultiPassImport) {
        super(false, true);
        this.importFile = importFile;
        this.useMultiPassImport = useMultiPassImport;
        LOG.info("Loading entities from: " + importFile.getAbsolutePath() + " using " +
                (useMultiPassImport ? "multi-pass" : "1-pass") + " import mode");
    }

    /**
     * This purpose of this method is to process all protobuf messages and call the entity transformer.
     * @return EntityCountSummary count of all entities/protobuf messages loaded/exported
     */
    public EntityCountSummary compute() {
        initCounts();

        updateTitle("Import Protobuf Data from " + importFile.getName());
        updateProgress(-1, 1);
        updateMessage("Analyzing Import File...");

        // Analyze Manifest and update tracking callable
        long expectedImports = analyzeManifest();
        LOG.info(expectedImports + " Entities to process...");

        if (useMultiPassImport) {
            return computeMultiPass(expectedImports);
        } else {
            return computeOnePass(expectedImports);
        }
    }

    /**
     * Multi-pass import: Imports entities in dependency order to handle forward references.
     * Pass 1: Import non-semantics (Concepts, Patterns, Stamps) - these have no referenced components
     * Pass 2+: Import semantics whose referenced components now exist in the database
     * Repeats until all semantics are successfully imported or no progress is made.
     */
    private EntityCountSummary computeMultiPass(long expectedImports) {
        updateMessage("Starting multi-pass import...");

        // Read all messages from the file into memory first
        Map<String, TinkarMsg> allMessages = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(importFile))) {
            ZipEntry zipEntry;
            int messageIndex = 0;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().equals(MANIFEST_RELPATH)) {
                    continue;
                }

                while (zis.available() > 0) {
                    TinkarMsg pbTinkarMsg = TinkarMsg.parseDelimitedFrom(zis);
                    if (pbTinkarMsg != null) {
                        allMessages.put("msg_" + messageIndex++, pbTinkarMsg);
                    }
                }
            }
            LOG.info("Read {} messages from changeset", allMessages.size());
        } catch (Exception e) {
            LOG.error("Failed to read messages from file", e);
            AlertStreams.dispatchToRoot(e);
            throw new RuntimeException("Failed to read changeset file", e);
        }

        EntityService.get().beginLoadPhase();

        // Consumer to be run for each transformed Entity
        Consumer<Entity<? extends EntityVersion>> entityConsumer = entity -> {
            EntityService.get().putEntityQuietly(entity, DataActivity.LOADING_CHANGE_SET);
            updateCounts(entity);
        };

        try {
            // PASS 1: Import all non-semantics (Concepts, Patterns, Stamps)
            updateMessage("Pass 1: Importing Concepts, Patterns, and Stamps...");
            Map<String, TinkarMsg> remainingMessages = new HashMap<>();

            for (Map.Entry<String, TinkarMsg> entry : allMessages.entrySet()) {
                TinkarMsg msg = entry.getValue();
                boolean isSemantic = msg.getValueCase() == TinkarMsg.ValueCase.SEMANTIC_CHRONOLOGY;

                if (!isSemantic) {
                    // Import non-semantics immediately - they have no referenced components
                    try {
                        entityTransformer.transform(msg, entityConsumer, (stampEntity) -> {});
                        importCount.incrementAndGet();
                    } catch (Exception e) {
                        LOG.error("Pass 1 - Failed to import non-semantic: {}", e.getMessage());
                        throw e;
                    }
                } else {
                    // Defer semantics for later passes
                    remainingMessages.put(entry.getKey(), msg);
                }

                // Progress update
                if (importCount.get() % 1000 == 0) {
                    updateProgress(importCount.get(), expectedImports);
                }
            }

            LOG.info("Pass 1 complete: Imported {} non-semantics, {} semantics remaining",
                    importCount.get(), remainingMessages.size());

            // PASS 2+: Import semantics in multiple passes until all are imported
            int passNumber = 2;
            int maxPasses = 100; // Safety limit to prevent infinite loops

            while (!remainingMessages.isEmpty() && passNumber <= maxPasses) {
                updateMessage(String.format("Pass %d: Importing semantics (%d remaining)...",
                        passNumber, remainingMessages.size()));

                Map<String, TinkarMsg> stillRemaining = new HashMap<>();
                long importedThisPass = 0;

                for (Map.Entry<String, TinkarMsg> entry : remainingMessages.entrySet()) {
                    TinkarMsg msg = entry.getValue();

                    try {
                        entityTransformer.transform(msg, entityConsumer, (stampEntity) -> {});
                        importCount.incrementAndGet();
                        importedThisPass++;

                        // Progress update
                        if (importCount.get() % 1000 == 0) {
                            updateProgress(importCount.get(), expectedImports);
                        }
                    } catch (Exception e) {
                        // Referenced component not found yet - defer to next pass
                        stillRemaining.put(entry.getKey(), msg);
                    }
                }

                LOG.info("Pass {} complete: Imported {} semantics, {} remaining",
                        passNumber, importedThisPass, stillRemaining.size());

                // Check for progress
                if (importedThisPass == 0 && !stillRemaining.isEmpty()) {
                    // No progress made - we have circular dependencies or missing references
                    LOG.error("Unable to import {} semantics due to unresolved references after {} passes",
                            stillRemaining.size(), passNumber);
                    throw new IllegalStateException(
                            String.format("Unable to resolve %d semantics after %d passes. " +
                                    "Possible circular dependencies or missing referenced components.",
                                    stillRemaining.size(), passNumber));
                }

                remainingMessages = stillRemaining;
                passNumber++;
            }

            if (!remainingMessages.isEmpty()) {
                throw new IllegalStateException(
                        String.format("Exceeded maximum passes (%d). %d semantics still unresolved.",
                                maxPasses, remainingMessages.size()));
            }

            LOG.info("Multi-pass import complete: {} passes, {} entities imported",
                    passNumber - 1, importCount.get());

        } catch (Exception e) {
            LOG.error("Multi-pass import failed", e);
            updateTitle("Failed: Import Protobuf data from " + importFile.getName());
            AlertStreams.dispatchToRoot(e);
            throw new RuntimeException("Multi-pass import failed", e);
        } finally {
            try {
                EntityService.get().endLoadPhase();
            } catch (Exception e) {
                LOG.error("Encountered exception {}", e.getMessage());
            }
            updateMessage("In " + durationString());
            updateProgress(1, 1);
        }

        if (importCount.get() != expectedImports) {
            IllegalStateException e = new IllegalStateException(
                    String.format("Import Failed: Expected %d entities, but imported %d",
                            expectedImports, importCount.get()));
            AlertStreams.dispatchToRoot(e);
            throw e;
        }
        return summarize();
    }

    /**
     * One-pass import: Load entities directly without pre-registering NIDs.
     * This may fail if the changeset contains forward references.
     */
    private EntityCountSummary computeOnePass(long expectedImports) {
        updateMessage("Importing Protobuf Data (1-pass mode)...");
        LOG.debug("Expected imports: " + expectedImports);

        EntityService.get().beginLoadPhase();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(importFile))) {
            // Consumer to be run for each transformed Entity
            Consumer<Entity<? extends EntityVersion>> entityConsumer = entity -> {
                EntityService.get().putEntityQuietly(entity, DataActivity.LOADING_CHANGE_SET);
                updateCounts(entity);
            };

            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().equals(MANIFEST_RELPATH)) {
                    continue;
                }

                while (zis.available() > 0) {
                    TinkarMsg pbTinkarMsg = TinkarMsg.parseDelimitedFrom(zis);
                    if (pbTinkarMsg == null) {
                        continue;
                    }

                    // Transform without pre-registered NIDs - may fail on forward references
                    try {
                        entityTransformer.transform(pbTinkarMsg, entityConsumer, (stampEntity) -> {});
                    } catch (Exception e) {
                        LOG.error("1-pass - Error transforming message: {}", e.getMessage(), e);
                        throw e; // Re-throw to fail the test
                    }

                    // Batch progress updates
                    if (importCount.incrementAndGet() % 1000 == 0) {
                        updateProgress(importCount.get(), expectedImports);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("1-pass - Entity loading failed", e);
            updateTitle("Failed: Import Protobuf data from " + importFile.getName());
            AlertStreams.dispatchToRoot(e);
            throw new RuntimeException("1-pass failed", e);
        } finally {
            try {
                EntityService.get().endLoadPhase();
            } catch (Exception e) {
                LOG.error("Encountered exception {}", e.getMessage());
            }
            updateMessage("In " + durationString());
            updateProgress(1, 1);
        }

        if (importCount.get() != expectedImports) {
            IllegalStateException e = new IllegalStateException("Import Failed: Expected " + expectedImports + " Entities, but imported " + importCount.get());
            AlertStreams.dispatchToRoot(e);
            throw e;
        }
        return summarize();
    }

    /**
     * Generate NID for a protobuf message to register the entity in the datastore.
     * This allows Pass 2 to resolve references.
     */
    private int makeNidForMessage(TinkarMsg pbTinkarMsg) {
        return switch (pbTinkarMsg.getValueCase()) {
            case CONCEPT_CHRONOLOGY -> 
                Entity.nidForConcept(getEntityPublicId(pbTinkarMsg.getConceptChronology().getPublicId()));
            case SEMANTIC_CHRONOLOGY -> {
                var semanticChronology = pbTinkarMsg.getSemanticChronology();
                PublicId patternPublicId = getEntityPublicId(semanticChronology.getPatternForSemanticPublicId());
                PublicId semanticPublicId = getEntityPublicId(semanticChronology.getPublicId());
                yield Entity.nidForSemantic(patternPublicId, semanticPublicId);
            }
            case PATTERN_CHRONOLOGY ->
                Entity.nidForPattern(getEntityPublicId(pbTinkarMsg.getPatternChronology().getPublicId()));
            case STAMP_CHRONOLOGY ->
                Entity.nidForStamp(getEntityPublicId(pbTinkarMsg.getStampChronology().getPublicId()));
            case VALUE_NOT_SET ->
                throw new IllegalStateException("Tinkar message value not set");
        };
    }

    /**
     * Extract PublicId from protobuf PublicId message.
     */
    private static PublicId getEntityPublicId(dev.ikm.tinkar.schema.PublicId pbPublicId) {
        return PublicIds.of(pbPublicId.getUuidsList().stream()
                .map(UUID::fromString)
                .toList());
    }

    protected void initCounts() {
        identifierCount.set(0);
        importCount.set(0);
        importConceptCount.set(0);
        importSemanticCount.set(0);
        importPatternCount.set(0);
        importStampCount.set(0);
    }

    private void updateCounts(Entity entity){
        switch (entity) {
            case ConceptEntity ignored -> importConceptCount.incrementAndGet();
            case SemanticEntity ignored -> importSemanticCount.incrementAndGet();
            case PatternEntity ignored -> importPatternCount.incrementAndGet();
            case StampEntity ignored -> importStampCount.incrementAndGet();
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        }
    }

    public EntityCountSummary summarize() {
        LOG.info("Imported: " + importCount.get() + " entities in: " + durationString());
        return new EntityCountSummary(
                importConceptCount.get(),
                importSemanticCount.get(),
                importPatternCount.get(),
                importStampCount.get()
        );
    }

    private long analyzeManifest() {
        long expectedImports = -1;
        Map<PublicId, String> manifestEntryData = new HashMap<>();

        // Read Manifest from Zip
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(importFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().equals(MANIFEST_RELPATH)) {
                    Manifest manifest = new Manifest(zis);
                    expectedImports = Long.parseLong(manifest.getMainAttributes().getValue("Total-Count"));
                    // Get Dependent Module / Author PublicIds and Descriptions
                    manifest.getEntries().keySet().forEach((publicIdKey) -> {
                        PublicId publicId = PublicIds.of(publicIdKey.split(","));
                        String description = manifest.getEntries().get(publicIdKey).getValue("Description");
                        manifestEntryData.put(publicId, description);
                    });
                }
                zis.closeEntry();
                LOG.info(zipEntry.getName() + " zip entry size: " + zipEntry.getSize());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        manifestEntryData.keySet().forEach((publicId) -> {
            if (!PrimitiveData.get().hasPublicId(publicId)) {
                LOG.warn("Dependent Module or Author is not Present -" +
                        " PublicId: " + publicId.idString() +
                        " Description: " + manifestEntryData.get(publicId));
            }
        });

        return expectedImports;
    }
}
