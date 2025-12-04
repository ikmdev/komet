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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.DataActivity;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.broadcast.Broadcaster;
import dev.ikm.tinkar.component.Chronology;
import dev.ikm.tinkar.component.ChronologyService;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.Version;
import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import dev.ikm.tinkar.entity.internal.EntityServiceFinder;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.list.ImmutableList;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;

public interface EntityService extends ChronologyService, Broadcaster<Integer> {
    static EntityService get() {
        return EntityServiceFinder.INSTANCE.get();
    }

    default CompletableFuture<EntityCountSummary> fullExport(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TinkExecutor.ioThreadPool().submit(new ExportEntitiesToProtobufFile(file)).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, TinkExecutor.ioThreadPool());
    }

    default CompletableFuture<EntityCountSummary> temporalExport(File file, long fromEpoch, long toEpoch) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TinkExecutor.ioThreadPool().submit(new ExportEntitiesToProtobufFile(file, fromEpoch, toEpoch)).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, TinkExecutor.ioThreadPool());
    }

    default CompletableFuture<EntityCountSummary> membershipExport(File file, List<PublicId> membershipTags) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TinkExecutor.ioThreadPool().submit(new ExportEntitiesToProtobufFile(file, membershipTags)).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, TinkExecutor.ioThreadPool());
    }

    default CompletableFuture<EntityCountSummary> loadData(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TinkExecutor.ioThreadPool().submit(new LoadEntitiesFromProtobufFile(file)).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, TinkExecutor.ioThreadPool());
    }

    @Override
    default <T extends Chronology<V>, V extends Version> Optional<T> getChronology(UUID... uuids) {
        return getChronology(nidForUuids(uuids));
    }

    @Override
    default <T extends Chronology<V>, V extends Version> Optional<T> getChronology(Component component) {
        return getChronology(nidForPublicId(component.publicId()));
    }

    <T extends Chronology<V>, V extends Version> Optional<T> getChronology(int nid);

    default int nidForUuids(UUID... uuids) {
        return nidForPublicId(PublicIds.of(uuids));
    }

    int nidForPublicId(PublicId publicId);

    /**
     *
     * @param component
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     */
        default <T extends Entity<V>, V extends EntityVersion> Optional<T> getEntity(Component component) {
            return getEntity(nidForPublicId(component.publicId()));
        }

        /**
         * @deprecated Use {@link EntityHandle#get(PublicId)} instead.
         * <p>
         * This method is being phased out in favor of the fluent {@link EntityHandle} API,
         * which provides better type safety, null handling, and composability.
         * <p>
         * <b>Migration:</b>
         * <pre>{@code
         * // Old (deprecated):
         * Optional<Entity> entity = EntityService.get().getEntity(publicId);
         *
         * // New (recommended):
         * EntityHandle handle = EntityHandle.get(publicId);
         * Optional<Entity<?>> entity = handle.entity();
         * }</pre>
         *
         * @see EntityHandle#get(PublicId)
         * TODO: We should search for all methods that do this silent type casting, and replace them with
         * a fluent API that better manages type determination.
         */
        @Deprecated(since = "Current", forRemoval = true)
        default <T extends Entity<V>, V extends EntityVersion> Optional<T> getEntity(PublicId publicId) {
            return getEntity(nidForPublicId(publicId));
        }

        /**
         * @deprecated Use {@link EntityHandle#get(int)} instead.
         * <p>
         * This method is being phased out in favor of the fluent {@link EntityHandle} API,
         * which provides better type safety, null handling, and composability.
         * <p>
         * <b>Migration:</b>
         * <pre>{@code
         * // Old (deprecated):
         * Optional<Entity> entity = EntityService.get().getEntity(nid);
         *
         * // New (recommended):
         * EntityHandle handle = EntityHandle.get(nid);
         * Optional<Entity<?>> entity = handle.entity();
         *
         * // Or with type safety:
         * ConceptEntity concept = EntityHandle.getConceptOrThrow(nid);
         * }</pre>
         *
         * @see EntityHandle#get(int)
         * @see EntityHandle#getConceptOrThrow(int)
         * @see EntityHandle#getSemanticOrThrow(int)
         * @see EntityHandle#getPatternOrThrow(int)
         * @see EntityHandle#getStampOrThrow(int)
         * TODO: We should search for all methods that do this silent type casting, and replace them with
         * a fluent API that better manages type determination.
         */
        @Deprecated(since = "Current", forRemoval = true)
        default <T extends Entity<V>, V extends EntityVersion> Optional<T> getEntity(int nid) {
            T entity = getEntityFast(nid);
            if (entity == null || entity.canceled()) {
                return Optional.empty();
            }
            return Optional.of(entity);
        }
    default Optional<Entity<?>> packagePrivateGetEntity(int nid) {
        Entity<?> entity = getEntityFast(nid);
        if (entity == null || entity.canceled()) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }
    /**
     *
     * @param nid
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     * @deprecated Use {@link EntityHandle#get(int)} instead.
     */
    @Deprecated(since = "Current", forRemoval = true)
        <T extends Entity<V>, V extends EntityVersion> T getEntityFast(int nid);

    /**
     *
     * @param uuidList
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     * @deprecated Use {@link EntityHandle#get(PublicId)} instead.
     */
    @Deprecated(since = "Current", forRemoval = true)
        default <T extends Entity<V>, V extends EntityVersion> Optional<T> getEntity(ImmutableList<UUID> uuidList) {
            return getEntity(nidForUuids(uuidList));
        }


        default int nidForUuids(ImmutableList<UUID> uuidList) {
            return nidForPublicId(PublicIds.of(uuidList.toArray(new UUID[uuidList.size()])));
        }

        /**
         * @deprecated Use {@link EntityHandle#get(PublicId)} instead.
         * <p>
         * This method is being phased out in favor of the fluent {@link EntityHandle} API,
         * which provides better type safety, null handling, and composability.
         * <p>
         * <b>Migration:</b>
         * <pre>{@code
         * // Old (deprecated):
         * Optional<Entity> entity = EntityService.get().getEntity(uuids);
         *
         * // New (recommended):
         * EntityHandle handle = EntityHandle.get(PublicIds.of(uuids));
         * Optional<Entity<?>> entity = handle.entity();
         * }</pre>
         *
         * @see EntityHandle#get(PublicId)
         * TODO: We should search for all methods that do this silent type casting, and replace them with
         * a fluent API that better manages type determination.
         * @deprecated Use {@link EntityHandle#get(PublicId)} instead.
         */
        @Deprecated(since = "Current", forRemoval = true)
        default <T extends Entity<V>, V extends EntityVersion> Optional<T> getEntity(UUID... uuids) {
            return getEntity(nidForUuids(uuids));
        }

        /**
         * @deprecated Use {@link EntityHandle#get(EntityFacade)} instead.
         * <p>
         * This method is being phased out in favor of the fluent {@link EntityHandle} API,
         * which provides better type safety, null handling, and composability.
         * <p>
         * <b>Migration:</b>
         * <pre>{@code
         * // Old (deprecated):
         * Optional<Entity> entity = EntityService.get().getEntity(entityFacade);
         *
         * // New (recommended):
         * EntityHandle handle = EntityHandle.get(entityFacade);
         * Optional<Entity<?>> entity = handle.entity();
         * }</pre>
         *
         * @see EntityHandle#get(EntityFacade)
         * TODO: We should search for all methods that do this silent type casting, and replace them with
         * a fluent API that better manages type determination.
         * @deprecated Use {@link EntityHandle#get(EntityFacade)} instead.
         */
        @Deprecated(since = "Current", forRemoval = true)
        default <T extends Entity<V>, V extends EntityVersion> Optional<T> getEntity(EntityFacade entityFacade) {
            return getEntity(entityFacade.nid());
        }

    /**
     *
     * @param uuidList
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     * @deprecated Use {@link EntityHandle#get(PublicId)} instead.
     */
    @Deprecated(since = "Current", forRemoval = true)
        default <T extends Entity<V>, V extends EntityVersion> T getEntityFast(ImmutableList<UUID> uuidList) {
            return getEntityFast(nidForUuids(uuidList));
        }

    /**
     *
     * @param uuids
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     * @deprecated Use {@link EntityHandle#get(PublicId)} instead.
     */
    @Deprecated(since = "Current", forRemoval = true)
        default <T extends Entity<V>, V extends EntityVersion> T getEntityFast(UUID... uuids) {
            return getEntityFast(nidForUuids(uuids));
        }

    /**
     *
     * @param entityFacade
     * @return
     * @param <T>
     * @param <V>
     * TODO: We should search for all methods that do this silent type casting, and replace them with
     * a fluent API that better manages type determination.
     * @deprecated Use {@link EntityHandle#get(EntityFacade)} instead.
     */
    @Deprecated(since = "Current", forRemoval = true)
        default <T extends Entity<V>, V extends EntityVersion> T getEntityFast(EntityFacade entityFacade) {
            return getEntityFast(entityFacade.nid());
        }

        default Optional<StampEntity<StampEntityVersion>> getStamp(Component component) {
        return getStamp(nidForPublicId(component.publicId()));
    }

    default Optional<StampEntity<StampEntityVersion>> getStamp(int nid) {
        StampEntity entity = getEntityFast(nid);
        if (entity == null || entity.canceled()) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    default Optional<StampEntity<StampEntityVersion>> getStamp(ImmutableList<UUID> uuidList) {
        return getStamp(nidForUuids(uuidList));
    }

    default Optional<StampEntity<StampEntityVersion>> getStamp(UUID... uuids) {
        return getStamp(nidForUuids(uuids));
    }

    default StampEntity<StampEntityVersion> getStampFast(ImmutableList<UUID> uuidList) {
        return getStampFast(nidForUuids(uuidList));
    }

    <T extends StampEntity<? extends StampEntityVersion>> T getStampFast(int nid);

    default StampEntity<StampEntityVersion> getStampFast(UUID... uuids) {
        return getStampFast(nidForUuids(uuids));
    }

    /**
     * Each time an entity is put via this method, each Flow.Subscriber
     * is notified that the entity may have changed by publishing the
     * nid of the entity.
     *
     * Defaults to an activity of DataActivity.SYNCHRONIZABLE_EDIT.
     *
     * @param entity
     */
    default void putEntity(Entity entity) {
        putEntity(entity, DataActivity.SYNCHRONIZABLE_EDIT);
    }

    /**
     * Inserts or updates a given entity and associates it with a specified data activity.
     * Each time an entity is put via this method, each Flow.Subscriber is notified that the
     * entity may have changed by publishing the nid of the entity.
     * TODO: convert to the event bus instead of the Flow.Subscriber
     * @param entity The entity to be put into the system.
     * @param activity The data activity associated with the entity.
     */
    void putEntity(Entity entity, DataActivity activity);

    /**
     * Each time an entity is put via this method, Flow.Subscriber
     * is not notified that the entity may have changed.
     * @param entity
     */
    void putEntityQuietly(Entity entity, DataActivity activity);
    /**
     * Each time an entity is put via this method, Flow.Subscriber
     * is not notified that the entity may have changed.
     *
     * Defaults to an activity of DataActivity.SYNCHRONIZABLE_EDIT.
     *
     * @param entity
     */
    default void putEntityQuietly(Entity entity) {
        putEntityQuietly(entity, DataActivity.SYNCHRONIZABLE_EDIT);
    }

    /**
     * @param stampEntity
     * @deprecated Use putEntity instead
     */
    @Deprecated
    void putStamp(StampEntity stampEntity);

    default int nidForComponent(Component component) {
        if (component instanceof ComponentWithNid) {
            return ((ComponentWithNid) component).nid();
        }
        return nidForPublicId(component.publicId());
    }

    void invalidateCaches(Entity entity);

    void invalidateCaches(int... nids);

    <T extends Chronology<V>, V extends Version> T unmarshalChronology(byte[] bytes);

    default void addSortedUuids(List<UUID> uuidList, IntIdList idList) throws NoSuchElementException {
        addSortedUuids(uuidList, idList.toArray());
    }

    /**
     * Note, this method does not sort the provided uuidList,
     * it only ensures that the UUIDs assigned to each nid are added to the existing list
     * in a sorted order. This method is to create reproducible identifiers for objects.
     *
     * @param uuidList
     * @param nids
     * @throws NoSuchElementException
     */
    default void addSortedUuids(List<UUID> uuidList, int... nids) throws NoSuchElementException {
        for (int nid : nids) {
            UUID[] uuids = getEntityFast(nid).publicId().asUuidArray();
            Arrays.sort(uuids);
            for (UUID nidUuid : uuids) {
                uuidList.add(nidUuid);
            }
        }
    }

    void forEachSemanticOfPattern(int patternNid, Consumer<SemanticEntity<SemanticEntityVersion>> procedure);

    int[] semanticNidsOfPattern(int patternNid);

    void forEachSemanticForComponent(int componentNid, Consumer<SemanticEntity<SemanticEntityVersion>> procedure);

    int[] semanticNidsForComponent(int componentNid);

    void forEachSemanticForComponentOfPattern(int componentNid, int patternNid, Consumer<SemanticEntity<SemanticEntityVersion>> procedure);

    int[] semanticNidsForComponentOfPattern(int componentNid, int patternNid);

    void notifyRefreshRequired(Transaction transaction);

    boolean isLoadPhase();

    void endLoadPhase();

    void beginLoadPhase();


    /**
     * Retrieves the NID for an entity of a specific pattern.
     * <p>
     * <b>Why use this method:</b> This method supports the evolution toward making EntityKey more efficient
     * by incorporating a Pattern part and a sequence within that pattern part. This approach enables better
     * organization and performance optimization of entity identifiers.
     * <p>
     * <b>Deprecation Note:</b> The {@code Entity.nid()} methods have been deprecated in favor of these
     * {@code nidForXxx} methods, which provide explicit pattern context and prepare the codebase for
     * enhanced identifier management.
     *
     * @param patternNid the NID of the pattern that defines the entity's structure
     * @param entityPublicId the public ID of the entity
     * @return the NID associated with the given entity within the specified pattern context
     */
    default int nidFor(int patternNid, PublicId entityPublicId) {
        PublicId patternPublicId = EntityHandle.get(patternNid).expectEntity().publicId();
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, patternPublicId)
                .call(() -> nidForPublicId(entityPublicId));
    }

    /**
     * Retrieves the NID for a semantic entity associated with a specific pattern.
     * <p>
     * <b>Why use this method:</b> This method supports the evolution toward making EntityKey more efficient
     * by incorporating a Pattern part and a sequence within that pattern part. By explicitly specifying the
     * pattern context, this method enables better performance and more accurate identifier resolution for semantics.
     * <p>
     * <b>Deprecation Note:</b> The {@code Entity.nid()} methods have been deprecated in favor of these
     * {@code nidForXxx} methods, which provide explicit pattern context and prepare the codebase for
     * enhanced identifier management.
     *
     * @param patternPublicId the public ID of the pattern that defines the semantic's structure
     * @param semanticPublicId the public ID of the semantic entity
     * @return the NID associated with the given semantic within the specified pattern context
     */
    default int nidForSemantic(PublicId patternPublicId, PublicId semanticPublicId) {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, patternPublicId)
                .call(() -> nidForPublicId(semanticPublicId));
    }

    /**
     * Retrieves the NID for a pattern entity.
     * <p>
     * <b>Why use this method:</b> This method supports the evolution toward making EntityKey more efficient
     * by incorporating a Pattern part and a sequence within that pattern part. By explicitly identifying
     * pattern entities through this method, the system can optimize identifier allocation and retrieval
     * for pattern-specific operations.
     * <p>
     * <b>Deprecation Note:</b> The {@code Entity.nid()} methods have been deprecated in favor of these
     * {@code nidForXxx} methods, which provide explicit pattern context and prepare the codebase for
     * enhanced identifier management.
     *
     * @param patternPublicId the public ID of the pattern entity
     * @return the NID associated with the given pattern
     */
    default int nidForPattern(PublicId patternPublicId) {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Pattern.pattern())
                .call(() -> nidForPublicId(patternPublicId));
    }

    /**
     * Retrieves the NID for a STAMP (Status, Time, Author, Module, Path) entity.
     * <p>
     * <b>Why use this method:</b> This method supports the evolution toward making EntityKey more efficient
     * by incorporating a Pattern part and a sequence within that pattern part. By explicitly handling
     * STAMP entities through this method, the system can optimize identifier management for versioning
     * and provenance tracking operations.
     * <p>
     * <b>Deprecation Note:</b> The {@code Entity.nid()} methods have been deprecated in favor of these
     * {@code nidForXxx} methods, which provide explicit pattern context and prepare the codebase for
     * enhanced identifier management.
     *
     * @param stampPublicId the public ID of the STAMP entity
     * @return the NID associated with the given STAMP
     */
    default int nidForStamp(PublicId stampPublicId) {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Stamp.pattern())
                .call(() -> nidForPublicId(stampPublicId));
    }

    /**
     * Retrieves the NID for a concept entity.
     * <p>
     * <b>Why use this method:</b> This method supports the evolution toward making EntityKey more efficient
     * by incorporating a Pattern part and a sequence within that pattern part. By explicitly identifying
     * concept entities through this method, the system can optimize identifier allocation and retrieval
     * for concept-specific operations.
     * <p>
     * <b>Deprecation Note:</b> The {@code Entity.nid()} methods have been deprecated in favor of these
     * {@code nidForXxx} methods, which provide explicit pattern context and prepare the codebase for
     * enhanced identifier management.
     *
     * @param conceptPublicId the public ID of the concept entity
     * @return the NID associated with the given concept
     */
    default int nidForConcept(PublicId conceptPublicId) {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                .call(() -> nidForPublicId(conceptPublicId));
    }
}
