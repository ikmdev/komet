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
package dev.ikm.tinkar.entity.export;

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.aggregator.DefaultEntityAggregator;
import dev.ikm.tinkar.entity.aggregator.EntityAggregator;
import dev.ikm.tinkar.entity.aggregator.MembershipEntityAggregator;
import dev.ikm.tinkar.entity.aggregator.TemporalEntityAggregator;
import dev.ikm.tinkar.entity.transform.EntityToTinkarSchemaTransformer;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportEntitiesToProtobufFile extends TrackingCallable<EntityCountSummary> {
    private static final Logger LOG =
            LoggerFactory.getLogger(ExportEntitiesToProtobufFile.class);
    private final File protobufFile;
    private final EntityToTinkarSchemaTransformer entityTransformer =
            EntityToTinkarSchemaTransformer.getInstance();
    private final Set<PublicId> moduleList = new HashSet<>();
    private final Set<PublicId> authorList = new HashSet<>();
    private final EntityAggregator entityAggregator;


    public ExportEntitiesToProtobufFile(File file, EntityAggregator entityAggregator) {
        super(false, true);
        this.protobufFile = file;
        LOG.info("Exporting entities to: " + file);
        this.entityAggregator = entityAggregator;
        if (getTitle()==null || getTitle().isBlank()) {
            updateTitle("Export to Protobuf");
        }
    }

    public ExportEntitiesToProtobufFile(File file) {
        this(file, new DefaultEntityAggregator());
        updateTitle("Full Export to Protobuf");
    }

    public ExportEntitiesToProtobufFile(File file, long fromEpochMillis, long toEpochMillis) {
        this(file, new TemporalEntityAggregator(fromEpochMillis, toEpochMillis));
        updateTitle("Time-Based Export to Protobuf");
    }

    public ExportEntitiesToProtobufFile(File file, List<PublicId> membershipTags) {
        this(file, new MembershipEntityAggregator(membershipTags));
        updateTitle("Tag-Based Export to Protobuf");
    }

    @Override
    public EntityCountSummary compute() {
        updateMessage("Analyzing Entities...");
        updateProgress(-1, 1);

        EntityCountSummary entityCountSummary = null;
        updateMessage("Exporting Entities...");
        addToTotalWork(entityAggregator.totalCount());

        try (FileOutputStream fos = new FileOutputStream(protobufFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            // Create a single entry
            ZipEntry zipEntry = new ZipEntry(protobufFile.getName().replace(".zip", ""));
            zos.putNextEntry(zipEntry);

            IntConsumer exportNidConsumer = (nid) -> {
                Entity<? extends EntityVersion> entity = EntityService.get().getEntityFast(nid);
                // Store Module & Author Dependencies for Manifest
                if (entity instanceof StampEntity stampEntity) {
                    moduleList.add(stampEntity.module().publicId());
                    authorList.add(stampEntity.author().publicId());
                }
                // Transform and Write data
                TinkarMsg pbTinkarMsg = entityTransformer.transform(entity);
                try {
                    pbTinkarMsg.writeDelimitedTo(zos);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                completedUnitOfWork();
            };

            entityCountSummary = entityAggregator.aggregate(exportNidConsumer);

            zos.closeEntry();
            zos.flush();
            LOG.info("Data zipEntry size: " + zipEntry.getSize());
            LOG.info("Data zipEntry compressed size: " + zipEntry.getCompressedSize());

            // Write Manifest File
            ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
            zos.putNextEntry(manifestEntry);
            zos.write(generateManifestContent(entityCountSummary.getTotalCount(),
                    entityCountSummary.conceptsCount(),
                    entityCountSummary.semanticsCount(),
                    entityCountSummary.patternsCount(),
                    entityCountSummary.stampsCount(),
                    moduleList,
                    authorList
                ).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.flush();

            // Cleanup
            zos.finish();
        } catch (Throwable e) {
            LOG.error("Caught " + e + " while Exporting Entities");
            if (!(e instanceof RuntimeException rx && rx.getCause() instanceof InterruptedException)) {
                AlertStreams.dispatchToRoot(e);
                throw new RuntimeException(e);
            }
        } finally {
            updateMessage("In " + durationString());
            updateProgress(1,1);
        }    

        logCounts(entityCountSummary);
        return entityCountSummary;
    }

    public static String generateManifestContent(long entityCount,
                                           long conceptsCount,
                                           long semanticsCount,
                                           long patternsCount,
                                           long stampsCount,
                                           Set<PublicId> moduleList,
                                           Set<PublicId> authorList){
        StringBuilder manifestContent = new StringBuilder()
                // TODO: Dynamically populate this user
                .append("Packager-Name: ").append(TinkarTerm.KOMET_USER.description()).append("\n")
                .append("Package-Date: ").append(LocalDateTime.now(Clock.systemUTC())).append("\n")
                .append("Total-Count: ").append(entityCount).append("\n")
                .append("Concept-Count: ").append(conceptsCount).append("\n")
                .append("Semantic-Count: ").append(semanticsCount).append("\n")
                .append("Pattern-Count: ").append(patternsCount).append("\n")
                .append("Stamp-Count: ").append(stampsCount).append("\n")
                .append(idsToManifestEntry(moduleList))
                .append(idsToManifestEntry(authorList))
                .append("\n"); // Final new line necessary per Manifest spec
        return manifestContent.toString();
    }

    public static String idsToManifestEntry(Collection<PublicId> publicIds) {
        StringBuilder manifestEntry = new StringBuilder();
        publicIds.forEach((publicId) -> {
            // Convert PublicId to Manifest Entry Name
            String idString = publicId.asUuidList().stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));
            // Get Description
            Optional<Entity<EntityVersion>> entity = EntityService.get().getEntity(PrimitiveData.nid(publicId));
            String manifestDescription = "Description Undefined";
            if (entity.isPresent()) {
                manifestDescription = entity.get().description();
            }
            // Create Manifest Entry
            manifestEntry.append("\n")
                    .append("Name: ").append(idString).append("\n")
                    .append("Description: ").append(manifestDescription).append("\n");
        });
        return manifestEntry.toString();
    }

    private void logCounts(EntityCountSummary summary) {
        LOG.info("Exported " + summary.getTotalCount() + " total entities.");
        LOG.info("Exported " + summary.conceptsCount() + " concepts.");
        LOG.info("Exported " + summary.semanticsCount() + " semantics.");
        LOG.info("Exported " + summary.patternsCount() + " patterns.");
        LOG.info("Exported " + summary.stampsCount() + " stamps.");
    }
}
